package teamport.aether.helper;

import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;

import java.util.ArrayDeque;
import java.util.Deque;

/// Remembers GL states the accessory renderers change, so they can be put back afterwards.
///
/// Ported off raw `GL11.glEnable`/`glGetInteger` onto `GLRenderer`, which is both the only thing
/// that works in 8.0's context and the only way to read a capability back -- `glGetInteger` on one
/// is itself fixed-function.
///
/// Two parallel stacks rather than one of boxed ints: the previous version pushed the target and
/// then its value onto a single stack, but `restore` popped the value first and compared it against
/// 1 and 0 as though it were the target, so a target that happened to equal 0 or 1 would have been
/// misread and everything else silently dropped.
public class GLManager {
    private static final Deque<State> states = new ArrayDeque<>();
    private static final Deque<Boolean> previous = new ArrayDeque<>();

    private GLManager() {
    }

    public static void enable(State state) {
        remember(state);
        GLRenderer.enableState(state);
    }

    public static void disable(State state) {
        remember(state);
        GLRenderer.disableState(state);
    }

    private static void remember(State state) {
        states.push(state);
        previous.push(GLRenderer.isStateEnabled(state));
    }

    public static void restore() {
        while (!states.isEmpty()) {
            State state = states.pop();
            if (Boolean.TRUE.equals(previous.pop())) {
                GLRenderer.enableState(state);
            } else {
                GLRenderer.disableState(state);
            }
        }
    }
}
