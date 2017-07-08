package android.telecom;

import android.telecom.Call.Callback;
import android.telecom.InCallService.VideoCall;

class Call$6 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ VideoCall val$videoCall;

    Call$6(Call this$0, Callback val$callback, Call val$call, VideoCall val$videoCall) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$videoCall = val$videoCall;
    }

    public void run() {
        this.val$callback.onVideoCallChanged(this.val$call, this.val$videoCall);
    }
}
