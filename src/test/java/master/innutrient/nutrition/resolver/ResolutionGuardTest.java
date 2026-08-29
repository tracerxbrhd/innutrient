package master.innutrient.nutrition.resolver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolutionGuardTest {
    @Test
    void terminatesCyclesAndExcessiveDepth() {
        Object a = new Object();
        Object b = new Object();
        ResolutionGuard<Object> guard = new ResolutionGuard<>(2);
        assertTrue(guard.enter(a, 0));
        assertTrue(guard.enter(b, 1));
        assertFalse(guard.enter(a, 2));
        assertFalse(guard.enter(new Object(), 3));
        guard.exit(b);
        guard.exit(a);
        assertTrue(guard.enter(a, 0));
    }
}
