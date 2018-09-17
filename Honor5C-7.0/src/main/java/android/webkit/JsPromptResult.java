package android.webkit;

import android.webkit.JsResult.ResultReceiver;

public class JsPromptResult extends JsResult {
    private String mStringResult;

    public void confirm(String result) {
        this.mStringResult = result;
        confirm();
    }

    public JsPromptResult(ResultReceiver receiver) {
        super(receiver);
    }

    public String getStringResult() {
        return this.mStringResult;
    }
}
