package android.webkit;

public class JsResult {
    private final ResultReceiver mReceiver;
    private boolean mResult;

    public interface ResultReceiver {
        void onJsResultComplete(JsResult jsResult);
    }

    public final void cancel() {
        this.mResult = false;
        wakeUp();
    }

    public final void confirm() {
        this.mResult = true;
        wakeUp();
    }

    public JsResult(ResultReceiver receiver) {
        this.mReceiver = receiver;
    }

    public final boolean getResult() {
        return this.mResult;
    }

    private final void wakeUp() {
        this.mReceiver.onJsResultComplete(this);
    }
}
