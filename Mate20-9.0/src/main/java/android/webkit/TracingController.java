package android.webkit;

import java.io.OutputStream;
import java.util.concurrent.Executor;

public abstract class TracingController {
    public abstract boolean isTracing();

    public abstract void start(TracingConfig tracingConfig);

    public abstract boolean stop(OutputStream outputStream, Executor executor);

    public static TracingController getInstance() {
        return WebViewFactory.getProvider().getTracingController();
    }
}
