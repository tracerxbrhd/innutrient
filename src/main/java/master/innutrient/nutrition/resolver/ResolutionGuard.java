package master.innutrient.nutrition.resolver;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Identity-based recursion guard shared by the production recipe traversal. */
final class ResolutionGuard<T> {
    private final int maxDepth;
    private final Set<T> visiting = Collections.newSetFromMap(new IdentityHashMap<>());

    ResolutionGuard(int maxDepth) {
        this.maxDepth = Math.max(0, maxDepth);
    }

    boolean enter(T value, int depth) {
        return depth <= maxDepth && visiting.add(value);
    }

    void exit(T value) {
        visiting.remove(value);
    }
}
