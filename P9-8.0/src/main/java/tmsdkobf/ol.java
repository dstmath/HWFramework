package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import tmsdk.common.TMSDKContext;

public class ol {
    private boolean Iq = false;
    private a Ir = null;
    private long Is = 0;
    private c It = null;
    private b Iu = null;
    private Context mContext = null;
    private Handler mHandler = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            mb.d("HeartBeatPlot", "[h_b]handleMessage(), nodifyOnHeartBeat()");
            ol.this.hc();
            oj.a(ol.this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE", ((long) ol.this.Iu.ha()) * 1000);
        }
    };

    public interface b {
        int ha();
    }

    public interface c {
        void gZ();
    }

    private class a extends if {
        private a() {
        }

        /* synthetic */ a(ol olVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void doOnRecv(Context context, Intent intent) {
            mb.d("HeartBeatPlot", "[h_b]HeartBeatPlotReceiver.onReceive()");
            String action = intent.getAction();
            String str = intent.getPackage();
            if (action == null || str == null || !str.equals(TMSDKContext.getApplicaionContext().getPackageName())) {
                mb.d("HeartBeatPlot", "TcpControlReceiver.onReceive(), null action or from other pkg, ignore");
                return;
            }
            if (action.equals("com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE")) {
                ol.this.mHandler.sendEmptyMessage(0);
            }
        }
    }

    public ol(Context context, c cVar, b bVar) {
        this.mContext = context;
        this.It = cVar;
        this.Iu = bVar;
        this.Ir = new a(this, null);
    }

    private void hc() {
        if (this.It != null) {
            long currentTimeMillis = System.currentTimeMillis();
            if ((currentTimeMillis - this.Is < 30000 ? 1 : null) == null) {
                this.It.gZ();
                this.Is = currentTimeMillis;
                return;
            }
            mb.s("HeartBeatPlot", "[h_b]heartbeat frequency is too dense! lastHeartBeatTime: " + this.Is);
        }
    }

    public synchronized void reset() {
        mb.d("HeartBeatPlot", "[h_b]reset()");
        oj.h(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE");
        oj.a(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE", ((long) this.Iu.ha()) * 1000);
    }

    public synchronized void start() {
        int ha = this.Iu.ha();
        mb.d("HeartBeatPlot", "[h_b]start(), heartBeatIntervalInSeconds: " + ha);
        if (!this.Iq) {
            try {
                this.mContext.registerReceiver(this.Ir, new IntentFilter("com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE"));
                this.Iq = true;
            } catch (Throwable th) {
                mb.e("HeartBeatPlot", th);
            }
        }
        oj.a(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE", ((long) ha) * 1000);
        return;
    }

    public synchronized void stop() {
        mb.d("HeartBeatPlot", "[h_b]stop()");
        this.mHandler.removeMessages(0);
        oj.h(this.mContext, "com.tencent.tmsdk.HeartBeatPlot.ACTION_HEARTBEAT_PLOT_ALARM_CYCLE");
        if (this.Iq) {
            try {
                this.mContext.unregisterReceiver(this.Ir);
                this.Iq = false;
            } catch (Throwable th) {
                mb.e("HeartBeatPlot", th);
            }
        }
        return;
    }
}
