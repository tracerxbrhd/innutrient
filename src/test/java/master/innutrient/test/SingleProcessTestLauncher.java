package master.innutrient.test;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/** Fallback launcher for hosts that prohibit Gradle's loopback-based test-worker transport. */
public final class SingleProcessTestLauncher {
    private SingleProcessTestLauncher() {}

    public static void main(String[] args) {
        var request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectPackage("master.innutrient"))
            .build();
        var listener = new SummaryGeneratingListener();
        var launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        listener.getSummary().printTo(new java.io.PrintWriter(System.out, true));
        if (listener.getSummary().getTotalFailureCount() > 0) {
            listener.getSummary().printFailuresTo(new java.io.PrintWriter(System.err, true));
            throw new IllegalStateException(listener.getSummary().getTotalFailureCount() + " test(s) failed");
        }
    }
}
