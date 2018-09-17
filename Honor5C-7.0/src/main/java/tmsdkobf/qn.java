package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
final class qn extends BaseManagerC {
    private Handler mHandler;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.qn.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ qn JM;

        AnonymousClass1(qn qnVar, Looper looper) {
            this.JM = qnVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            Runnable runnable = (Runnable) message.obj;
            if (runnable != null) {
                jq.ct().c(runnable, "DefaultPhoneRunableTask").start();
            }
        }
    }

    qn() {
    }

    public boolean a(Runnable runnable, long j) {
        if (runnable == null) {
            return false;
        }
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.obj = runnable;
        return this.mHandler.sendMessageDelayed(obtainMessage, j);
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mHandler = new AnonymousClass1(this, context.getMainLooper());
    }
}
