# Static audit of mixin injection targets against the BTA 8.0.1 jar.
#
# A mixin whose target method vanished only fails at launch, and with "required": true that is a
# hard crash -- so finding them by launching costs one ~2 minute cycle per broken mixin. This walks
# every @Mixin class, resolves its target through the imports, and checks each `method = "..."`
# selector plus the handler that follows it. One pass, no launch.
#
# Three things this checks, each of which produced a real break in the 8.0.1 port:
#
#   1. method exists        -- `startWorld` still exists in 8.0 but with different parameters, so a
#                              name-only check passes a mixin that will still fail at APPLY. When
#                              the mixin spells out a descriptor, the whole thing is compared.
#
#   2. method is DECLARED   -- mixin cannot inject into an inherited method. MobRendererPlayer
#                              inherits render() from MobRenderer in 8.0 where 7.3 overrode it, so
#                              the name still resolves up the chain but the injector fails anyway.
#                              Reported as 'inherited, not declared'.
#
#   3. handler params match -- for @Inject, the handler's leading parameters must mirror the
#                              target's, then CallbackInfo. The bulk `int x,y,z -> TilePosc pos`
#                              rewrite edited handler bodies as if they were ordinary methods,
#                              silently desynchronising them from the descriptor they inject on.
#
# Usage:  powershell -File check_mixins.ps1

$proj = Split-Path -Parent $MyInvocation.MyCommand.Path
$src  = Join-Path $proj "src\main\java\teamport\aether\mixin"
$j8   = "$env:USERPROFILE\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\8.0.1\minecraft-merged-deobf-8.0.1.jar"
$h6   = (Get-ChildItem "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\turniplabs\halplibe" -Filter "halplibe-6.1.4+8.0.jar" -Recurse | Select-Object -First 1).FullName
$cp   = "$j8;$h6"

# ---------------------------------------------------------------- javap helpers

# Strip balanced <...> so the generic bound in `class Foo<T extends Bound> extends Real` cannot be
# mistaken for the superclass -- a naive `extends\s+(\S+)` matches Bound first.
function StripGenerics([string]$s) {
    $out = New-Object System.Text.StringBuilder
    $depth = 0
    foreach ($c in $s.ToCharArray()) {
        if ($c -eq '<') { $depth++ }
        elseif ($c -eq '>') { if ($depth -gt 0) { $depth-- } }
        elseif ($depth -eq 0) { [void]$out.Append($c) }
    }
    return $out.ToString()
}

$declCache = @{}
# Methods DECLARED on exactly this class: name -> set of descriptors. Also returns the superclass
# so callers can walk the chain themselves.
function DeclaredOf($fq) {
    if ($declCache.ContainsKey($fq)) { return $declCache[$fq] }
    $sigs = @{}; $fields = @{}; $super = $null; $ifaces = @(); $found = $false
    $r = & javap -p -s -cp $cp $fq 2>&1
    if ("$r" -notmatch 'Error: class not found') {
        $found = $true
        $pending = $null; $pendingIsField = $false
        foreach ($l in $r) {
            if ($l -match '^\s*descriptor:\s*(\S+)') {
                if ($pending) {
                    $bag = if ($pendingIsField) { $fields } else { $sigs }
                    if (-not $bag.ContainsKey($pending)) { $bag[$pending] = @{} }
                    $bag[$pending][$matches[1]] = $true
                    $pending = $null
                }
            } elseif ($l -match '\s([\w$]+)\s*\(') {
                $pending = $matches[1]; $pendingIsField = $false
            } elseif ($l -match '\s([\w$]+)\s*;\s*$') {
                # a field: `public final int maxCharges;` with its descriptor on the next line
                $pending = $matches[1]; $pendingIsField = $true
            }
        }
        $head = StripGenerics (($r | Select-Object -First 2) -join ' ')
        if ($head -match 'extends\s+([\w.$]+)') { $super = $matches[1] }
        if ($head -match 'implements\s+(.+?)\s*\{') {
            $ifaces = @($matches[1] -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ -match '^[\w.$]+$' })
        }
    }
    $res = [pscustomobject]@{ Sigs = $sigs; Fields = $fields; Super = $super; Ifaces = $ifaces; Found = $found }
    $declCache[$fq] = $res
    return $res
}

# Does $fq, or anything it inherits from, have this member? @At targets name a call site, so an
# inherited member is legitimate there -- unlike an injection target.
#
# Interfaces have to be walked as well as superclasses: 8.0 moved getTotalProtectionAmount and
# damageArmor onto IArmorWearing as DEFAULT methods, so `PlayerLocal.getTotalProtectionAmount` is a
# perfectly valid call site even though PlayerLocal declares no such method. Skipping interfaces
# reports three healthy mixins as broken.
function HasMember($fq, $name, $desc, [bool]$isField) {
    $seen = @{}; $queue = New-Object System.Collections.Queue
    $queue.Enqueue($fq)
    $guard = 0
    while ($queue.Count -gt 0 -and $guard -lt 64) {
        $guard++
        $cur = $queue.Dequeue()
        if (-not $cur -or $seen.ContainsKey($cur)) { continue }
        $seen[$cur] = $true
        $d = DeclaredOf $cur
        if (-not $d.Found) { return $true }   # unresolvable class: stay quiet rather than guess
        $bag = if ($isField) { $d.Fields } else { $d.Sigs }
        if ($bag.ContainsKey($name)) {
            if (-not $desc) { return $true }
            if ($bag[$name].ContainsKey($desc)) { return $true }
        }
        if ($d.Super) { $queue.Enqueue($d.Super) }
        foreach ($i in $d.Ifaces) { $queue.Enqueue($i) }
    }
    return $false
}

# Everything visible from superclasses, for telling 'inherited' apart from 'gone entirely'.
function InheritedOf($fq) {
    $all = @{}; $cur = (DeclaredOf $fq).Super; $depth = 0
    while ($cur -and $depth -lt 8) {
        $d = DeclaredOf $cur
        foreach ($k in $d.Sigs.Keys) {
            if (-not $all.ContainsKey($k)) { $all[$k] = @{} }
            foreach ($desc in $d.Sigs[$k].Keys) { $all[$k][$desc] = $true }
        }
        $cur = $d.Super; $depth++
    }
    return $all
}

# ---------------------------------------------------------------- java source -> descriptor

$prims = @{ 'int'='I'; 'boolean'='Z'; 'float'='F'; 'double'='D'; 'long'='J'
            'short'='S'; 'byte'='B'; 'char'='C'; 'void'='V' }

# '?' means "could not resolve" and callers treat it as a match, so an unresolved type never
# invents a failure. Missing a break beats crying wolf across 200 healthy mixins.
function TypeToDesc([string]$t, $imports) {
    $t = (StripGenerics $t).Trim()
    $arr = 0
    while ($t -match '\[\s*\]\s*$') { $arr++; $t = ($t -replace '\[\s*\]\s*$', '').Trim() }
    $d = $null
    if ($prims.ContainsKey($t)) { $d = $prims[$t] }
    elseif ($imports.ContainsKey($t)) { $d = 'L' + ($imports[$t] -replace '\.', '/') + ';' }
    elseif ($t -match '^(String|Object|Integer|Boolean|Float|Double|Long|Short|Byte|Character)$') {
        $d = 'Ljava/lang/' + $t + ';'
    }
    elseif ($t -match '^[\w.]+\.\w+$') { $d = 'L' + ($t -replace '\.', '/') + ';' }
    if (-not $d) { return '?' }
    return ('[' * $arr) + $d
}

# Split a parameter list on top-level commas only, so generics and annotation args survive.
function SplitParams([string]$s) {
    $out = @(); $depth = 0; $cur = ''
    foreach ($c in $s.ToCharArray()) {
        if ($c -eq '<' -or $c -eq '(' -or $c -eq '[') { $depth++ }
        elseif ($c -eq '>' -or $c -eq ')' -or $c -eq ']') { $depth-- }
        if ($c -eq ',' -and $depth -eq 0) { $out += $cur; $cur = '' } else { $cur += $c }
    }
    if ($cur.Trim()) { $out += $cur }
    return $out
}

# Parameter type as written, with annotations and the variable name removed.
function ParamType([string]$p) {
    $p = $p.Trim()
    while ($p -match '^@[\w.]+') {
        $p = $p -replace '^@[\w.]+\s*', ''
        if ($p.StartsWith('(')) {
            $depth = 0; $i = 0
            foreach ($c in $p.ToCharArray()) {
                if ($c -eq '(') { $depth++ } elseif ($c -eq ')') { $depth--; if ($depth -eq 0) { $i++; break } }
                $i++
            }
            $p = $p.Substring($i)
        }
        $p = $p.Trim()
    }
    $p = $p -replace '^final\s+', ''
    $p = $p -replace '\s+\w+\s*$', ''   # trailing identifier = the parameter name
    # Drop generics last: CallbackInfoReturnable<Block<T>> has to reduce to a bare name for the
    # CallbackInfo test below, which is anchored at end-of-string.
    return (StripGenerics $p).Trim()
}

function DescParams([string]$desc) {
    if ($desc -notmatch '^\(([^)]*)\)') { return $null }
    $inner = $matches[1]
    $out = @(); $i = 0
    while ($i -lt $inner.Length) {
        $arr = ''
        while ($i -lt $inner.Length -and $inner[$i] -eq '[') { $arr += '['; $i++ }
        if ($i -ge $inner.Length) { break }
        if ($inner[$i] -eq 'L') {
            $e = $inner.IndexOf(';', $i)
            if ($e -lt 0) { break }
            $out += $arr + $inner.Substring($i, $e - $i + 1); $i = $e + 1
        } else {
            $out += $arr + $inner[$i]; $i++
        }
    }
    return ,$out
}

# Index of the ')' matching the '(' at $open, skipping over string literals. A regex cannot do
# this: @At(target = "...isSprinting()Z") nests parens both inside a string and outside it.
function MatchParen([string]$s, [int]$open) {
    $depth = 0; $i = $open; $inStr = $false
    while ($i -lt $s.Length) {
        $c = $s[$i]
        if ($inStr) {
            if ($c -eq '\') { $i += 2; continue }
            if ($c -eq '"') { $inStr = $false }
        } elseif ($c -eq '"') { $inStr = $true }
        elseif ($c -eq '(') { $depth++ }
        elseif ($c -eq ')') { $depth--; if ($depth -eq 0) { return $i } }
        $i++
    }
    return -1
}

# Pull out (annotationArgs, handlerName, handlerParams) for every injector annotation in a file.
# Hand-rolled rather than regex because handler names contain '$', signatures carry `throws`, and
# annotation arguments nest parentheses.
function FindInjectors([string]$t) {
    $kinds = 'Inject|ModifyExpressionValue|ModifyReturnValue|ModifyArg|ModifyArgs|ModifyVariable|Redirect|WrapOperation|WrapWithCondition'
    $res = @()
    foreach ($m in [regex]::Matches($t, "@($kinds)\s*\(")) {
        $kind = $m.Groups[1].Value
        $open = $t.IndexOf('(', $m.Index)
        $close = MatchParen $t $open
        if ($close -lt 0) { continue }
        $annArgs = $t.Substring($open + 1, $close - $open - 1)

        # Walk forward past any further annotations, modifiers, and the return type to the
        # handler's own name and parameter list.
        $i = $close + 1
        $guard = 0
        while ($i -lt $t.Length -and $guard -lt 40) {
            $guard++
            while ($i -lt $t.Length -and [char]::IsWhiteSpace($t[$i])) { $i++ }
            if ($i -ge $t.Length) { break }
            if ($t[$i] -eq '@') {
                $j = $i + 1
                while ($j -lt $t.Length -and ($t[$j] -match '[\w.]')) { $j++ }
                while ($j -lt $t.Length -and [char]::IsWhiteSpace($t[$j])) { $j++ }
                if ($j -lt $t.Length -and $t[$j] -eq '(') { $j = (MatchParen $t $j) + 1; if ($j -le 0) { break } }
                $i = $j; continue
            }
            # a token: modifier, return type, or the handler name if '(' follows
            $st = $i
            while ($i -lt $t.Length -and ($t[$i] -match '[\w$.\[\]]')) { $i++ }
            if ($i -eq $st) { break }
            $tok = $t.Substring($st, $i - $st)
            # skip a generic argument list attached to the return type
            if ($i -lt $t.Length -and $t[$i] -eq '<') {
                $d = 0
                while ($i -lt $t.Length) {
                    if ($t[$i] -eq '<') { $d++ } elseif ($t[$i] -eq '>') { $d--; if ($d -eq 0) { $i++; break } }
                    $i++
                }
                continue
            }
            $k = $i
            while ($k -lt $t.Length -and [char]::IsWhiteSpace($t[$k])) { $k++ }
            if ($k -lt $t.Length -and $t[$k] -eq '(') {
                $pc = MatchParen $t $k
                if ($pc -lt 0) { break }
                $res += [pscustomobject]@{
                    Kind = $kind; Ann = $annArgs; Handler = $tok
                    Params = $t.Substring($k + 1, $pc - $k - 1)
                }
                break
            }
        }
    }
    return $res
}

# ---------------------------------------------------------------- audit

$bad = @(); $checked = 0; $paramChecked = 0

foreach ($f in (Get-ChildItem $src -Filter *.java -Recurse)) {
    $t = [System.IO.File]::ReadAllText($f.FullName)

    $imports = @{}
    foreach ($m in [regex]::Matches($t, '(?m)^import\s+(?:static\s+)?([\w.]+)\.(\w+);')) {
        $imports[$m.Groups[2].Value] = $m.Groups[1].Value + '.' + $m.Groups[2].Value
    }
    # These two are routinely written fully-qualified inline rather than imported.
    $imports['CallbackInfo']           = 'org.spongepowered.asm.mixin.injection.callback.CallbackInfo'
    $imports['CallbackInfoReturnable'] = 'org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable'

    $mm = [regex]::Match($t, '@Mixin\s*\(\s*(?:value\s*=\s*)?\{?\s*([\w.]+)\.class')
    if (-not $mm.Success) { continue }
    $simple = $mm.Groups[1].Value
    $fq = if ($imports.ContainsKey($simple)) { $imports[$simple] } elseif ($simple -match '\.') { $simple } else { $null }
    if (-not $fq) { continue }

    # Every fully-qualified member reference in the file -- @At(target=..), @Definition(method=..),
    # @Definition(field=..). These name the CALL SITE a handler attaches to rather than the method
    # it injects into, and a stale one fails the same way: ModelBase was deleted outright in 8.0,
    # so `Lnet/minecraft/client/render/model/ModelBase;render(FFFFFF)V` can never resolve.
    foreach ($rm in [regex]::Matches($t, '"(L[\w/$]+;[\w$]+(?:\([^"]*\)[^";]*|:[^"]+))"')) {
        $ref = $rm.Groups[1].Value
        if ($ref -match '^L([\w/$]+);([\w$]+)\((.*)\)(.+)$') {
            $owner = $matches[1] -replace '/', '.'; $mname = $matches[2]
            $mdesc = "($($matches[3]))$($matches[4])"
            $od = DeclaredOf $owner
            if (-not $od.Found) {
                $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = '@At/@Definition'; Selector = $ref
                                           Why = "target class does not exist: $owner" }
            } elseif (-not (HasMember $owner $mname $mdesc $false)) {
                $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = '@At/@Definition'; Selector = $ref
                                           Why = "no such method on $($owner -replace '.*\.', '')" }
            }
        } elseif ($ref -match '^L([\w/$]+);([\w$]+):(.+)$') {
            $owner = $matches[1] -replace '/', '.'; $fname = $matches[2]; $fdesc = $matches[3]
            $od = DeclaredOf $owner
            if (-not $od.Found) {
                $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = '@At/@Definition'; Selector = $ref
                                           Why = "target class does not exist: $owner" }
            } elseif (-not (HasMember $owner $fname $fdesc $true)) {
                $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = '@At/@Definition'; Selector = $ref
                                           Why = "no such field on $($owner -replace '.*\.', '')" }
            }
        }
    }

    $decl = (DeclaredOf $fq).Sigs
    if ($decl.Count -eq 0) { continue }
    $inh = InheritedOf $fq

    # Each injector annotation is paired with the handler that follows it, so the handler's
    # parameters can be checked against the descriptor it claims to inject on.
    foreach ($im in (FindInjectors $t)) {
        $kind    = $im.Kind
        $annArgs = $im.Ann
        $handler = $im.Handler
        $hParams = $im.Params

        $sel = [regex]::Match($annArgs, 'method\s*=\s*"([^"]+)"')
        if (-not $sel.Success) { continue }
        $spec = $sel.Groups[1].Value.Trim()
        # Skip constructors and raw @At descriptors -- not method-name selectors.
        if ($spec -eq '' -or $spec -match '^[<L]') { continue }
        $checked++

        $name = $spec; $desc = $null
        if ($spec -match '^([^(]+)(\(.*)$') { $name = $matches[1]; $desc = $matches[2] }
        $wild = $name.EndsWith('*')
        $name = $name -replace '\*$', ''

        # A wildcard selector resolves to whatever declared names share the prefix. Resolve it here
        # so `renderSpecials*` is still checked rather than waved through.
        if ($wild) {
            $hits = @($decl.Keys | Where-Object { $_.StartsWith($name) })
            if ($hits.Count -eq 0) {
                $ihits = @($inh.Keys | Where-Object { $_.StartsWith($name) })
                $why = if ($ihits.Count -gt 0) { 'inherited, not declared' } else { 'no such method' }
                $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = $handler; Selector = $spec; Why = $why }
                continue
            }
            if ($hits.Count -eq 1) { $name = $hits[0]; $wild = $false }
        }

        if (-not $decl.ContainsKey($name)) {
            $why = if ($inh.ContainsKey($name)) { 'inherited, not declared' } else { 'no such method' }
            $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = $handler; Selector = $spec; Why = $why }
            continue
        }
        if ($desc -and -not $decl[$name].ContainsKey($desc)) {
            $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = $handler; Selector = $spec; Why = 'signature changed' }
            continue
        }

        # --- handler parameter check, @Inject only -------------------------------------------
        # Other injector kinds lead with a value or an Operation and carry their own sugar rules;
        # the @Inject rule is unambiguous, so only that one is enforced.
        if ($kind -ne 'Inject' -or $wild) { continue }

        $cands = @($decl[$name].Keys)
        if ($desc) { $cands = @($desc) }

        $types = @()
        foreach ($p in (SplitParams $hParams)) { if ($p.Trim()) { $types += (ParamType $p) } }

        $ciIdx = -1
        for ($i = 0; $i -lt $types.Count; $i++) {
            if ($types[$i] -match '(^|\.)CallbackInfo(Returnable)?$') { $ciIdx = $i; break }
        }
        if ($ciIdx -lt 0) {
            $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = $handler; Selector = $spec; Why = 'handler has no CallbackInfo' }
            continue
        }
        $paramChecked++

        $pre = @()
        for ($i = 0; $i -lt $ciIdx; $i++) { $pre += $types[$i] }
        if ($pre.Count -eq 0) { continue }   # legal short form: (CallbackInfo ci)

        # An overloaded name (or a bridge method alongside the real one) gives several candidate
        # descriptors. The handler only has to line up with ONE of them; flag it when it fits none.
        $fit = $false; $near = $null
        foreach ($c in $cands) {
            $tParams = DescParams $c
            if ($null -eq $tParams) { continue }
            if ($pre.Count -ne $tParams.Count) {
                if (-not $near) { $near = "handler takes $($pre.Count) leading params, target takes $($tParams.Count)" }
                continue
            }
            $ok = $true
            for ($i = 0; $i -lt $pre.Count; $i++) {
                $d = TypeToDesc $pre[$i] $imports
                if ($d -eq '?') { continue }
                if ($d -ne $tParams[$i]) {
                    $ok = $false
                    if (-not $near) { $near = "param $($i+1) is '$($pre[$i])', target wants $($tParams[$i])" }
                    break
                }
            }
            if ($ok) { $fit = $true; break }
        }
        if (-not $fit -and $near) {
            $bad += [pscustomobject]@{ Mixin = $f.Name; Handler = $handler; Selector = $spec; Why = $near }
        }
    }
}

Write-Output "injection targets checked: $checked  (handler params verified on $paramChecked @Inject)"
Write-Output "BROKEN: $($bad.Count)"
Write-Output ""
Write-Output "---- by cause ----"
$bad | Group-Object Why | Sort-Object Count -Descending |
    Format-Table @{n='Count';e={$_.Count}}, @{n='Cause';e={$_.Name}} -AutoSize -Wrap
Write-Output "---- detail ----"
foreach ($b in ($bad | Sort-Object Mixin, Handler)) {
    Write-Output ("{0}  [{1}]" -f $b.Mixin, $b.Handler)
    Write-Output ("    {0}" -f $b.Selector)
    Write-Output ("    -> {0}" -f $b.Why)
}
