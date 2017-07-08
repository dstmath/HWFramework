package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class po {
    private boolean Ip;
    private a Iq;
    private long Ir;
    private int Is;
    private long It;
    private b Iu;
    private Context mContext;
    private Handler mHandler;

    /* compiled from: Unknown */
    public interface b {
        void he();
    }

    /* compiled from: Unknown */
    class a extends jj {
        final /* synthetic */ po Iv;

        a(po poVar) {
            this.Iv = poVar;
        }

        public void doOnRecv(Context context, Intent intent) {
            d.e("HeartBeatPlot", "RetryPlotReceiver.onReceive()");
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE")) {
                    this.Iv.mHandler.sendEmptyMessage(0);
                }
                return;
            }
            d.e("HeartBeatPlot", "RetryPlotReceiver.onReceive() action");
        }
    }

    public po(Context context, b bVar) {
        this.Ip = false;
        this.Iq = null;
        this.mContext = null;
        this.mHandler = null;
        this.Ir = (long) (pk.HX * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
        this.Is = 30;
        this.It = 0;
        this.Iu = null;
        this.mContext = context;
        this.Iq = new a(this);
        if (this.mContext != null) {
            this.Iu = bVar;
            this.mHandler = new pq(this);
            return;
        }
        d.c("HeartBeatPlot", "mContext == null");
    }

    public void co(int i) {
        if (i < this.Is) {
            i = this.Is;
        }
        this.Ir = (long) (i * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
    }

    Context getContext() {
        return this.mContext;
    }

    void hj() {
        if (this.Iu != null) {
            long currentTimeMillis = System.currentTimeMillis();
            if ((this.It + ((long) (this.Is * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) >= currentTimeMillis ? 1 : null) == null) {
                pa.h("HeartBeatPlot", "heartbeat cycle. lastHeartBeatTime : " + this.It);
                this.Iu.he();
                this.It = currentTimeMillis;
                return;
            }
            pa.h("HeartBeatPlot", "heartbeat frequency is too dense\uff01 now : " + currentTimeMillis);
        }
    }

    public long hk() {
        return this.Ir;
    }

    public void reset() {
        pm.f(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE");
        pm.a(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE", hk());
    }

    public synchronized void start() {
        stop();
        if (!this.Ip) {
            try {
                this.mContext.registerReceiver(this.Iq, new IntentFilter("com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE"));
                this.Ip = true;
            } catch (Throwable th) {
                this.Ip = true;
            }
        }
        pm.a(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE", hk());
    }

    public synchronized void stop() {
        if (this.Ip) {
            try {
                this.mContext.unregisterReceiver(this.Iq);
                this.Ip = false;
            } catch (Throwable th) {
                this.Ip = false;
            }
        }
        pm.f(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE");
        this.mHandler.removeMessages(0);
    }
}
