package android.telecom;

import android.telecom.Call.Callback;

class Call$8 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ CallbackRecord val$record;

    Call$8(Call this$0, Callback val$callback, Call val$call, CallbackRecord val$record) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$record = val$record;
    }

    public void run() {
        boolean isFinalRemoval = false;
        RuntimeException toThrow = null;
        try {
            this.val$callback.onCallDestroyed(this.val$call);
        } catch (RuntimeException e) {
            toThrow = e;
        }
        synchronized (this.this$0) {
            Call.-get0(this.this$0).remove(this.val$record);
            if (Call.-get0(this.this$0).isEmpty()) {
                isFinalRemoval = true;
            }
        }
        if (isFinalRemoval) {
            Call.-get1(this.this$0).internalRemoveCall(this.val$call);
        }
        if (toThrow != null) {
            throw toThrow;
        }
    }
}
