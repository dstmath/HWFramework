package android.telecom;

import android.telecom.Call.Callback;
import java.util.List;

class Call$5 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ List val$cannedTextResponses;

    Call$5(Call this$0, Callback val$callback, Call val$call, List val$cannedTextResponses) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$cannedTextResponses = val$cannedTextResponses;
    }

    public void run() {
        this.val$callback.onCannedTextResponsesLoaded(this.val$call, this.val$cannedTextResponses);
    }
}
