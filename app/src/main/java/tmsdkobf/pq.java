package tmsdkobf;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

/* compiled from: Unknown */
class pq extends Handler {
    private WeakReference<po> Iw;

    public pq(po poVar) {
        super(poVar.getContext().getMainLooper());
        this.Iw = null;
        this.Iw = new WeakReference(poVar);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        po poVar = (po) this.Iw.get();
        if (poVar != null) {
            poVar.hj();
            pm.a(poVar.getContext(), "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE", poVar.hk());
        }
    }
}
