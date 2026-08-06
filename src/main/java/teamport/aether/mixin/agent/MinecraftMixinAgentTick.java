package teamport.aether.mixin.agent;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.agent.AetherAgent;

/// One line, so the harness costs nothing when `-Daether.agent=1` is absent.
@Mixin(Minecraft.class)
public class MinecraftMixinAgentTick {
    @Inject(method = "runTick", at = @At("TAIL"))
    private void aether$agentTick(CallbackInfo ci) {
        if (AetherAgent.ENABLED) AetherAgent.onTick((Minecraft) (Object) this);
    }
}
