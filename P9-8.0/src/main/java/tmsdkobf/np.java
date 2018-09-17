package tmsdkobf;

import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.exception.WifiApproveException;
import tmsdk.common.utils.u;

public class np implements tmsdkobf.nx.a {
    private static np DM = null;
    private static Object mLock = new Object();
    private int DN;
    private long DO;
    private boolean DP;
    private long DQ;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private class a extends Handler {
        public a(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    np.this.fV();
                    return;
                default:
                    return;
            }
        }
    }

    private np() {
        this.DN = -6;
        this.DO = 0;
        this.DP = false;
        this.DQ = 0;
        this.mHandlerThread = null;
        this.mHandler = null;
        this.mHandlerThread = ((ki) fj.D(4)).newFreeHandlerThread("Shark-Network-Detect-HandlerThread");
        this.mHandlerThread.start();
        this.mHandler = new a(this.mHandlerThread.getLooper());
        mb.n("NetworkDetector", "[detect_conn]init, register & start detect");
        nx.gs().a((tmsdkobf.nx.a) this);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    public static String bn(int i) {
        return "" + i;
    }

    public static np fS() {
        np npVar;
        synchronized (mLock) {
            if (DM == null) {
                DM = new np();
            }
            npVar = DM;
        }
        return npVar;
    }

    private boolean fU() {
        NetworkInfo networkInfo = null;
        try {
            networkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            mb.s("NetworkDetector", " NullPointerException: " + e.getMessage());
        }
        return networkInfo == null || !networkInfo.isConnected();
    }

    private boolean fV() {
        boolean z = false;
        mb.n("NetworkDetector", "[detect_conn]detectSync()");
        this.DP = true;
        Object obj = null;
        try {
            obj = u.a(new tmsdk.common.utils.u.a() {
                public void d(boolean z, boolean z2) {
                    mb.n("NetworkDetector", "[detect_conn]detectSync(), network error? " + z2);
                    if (z2) {
                        np.this.DN = -3;
                    } else if (z) {
                        np.this.DN = -2;
                    } else {
                        np.this.DN = 0;
                    }
                }
            });
        } catch (WifiApproveException e) {
            this.DN = -3;
            mb.o("NetworkDetector", "[detect_conn]detectSync(), exception: " + e.toString());
        }
        this.DP = false;
        this.DQ = System.currentTimeMillis();
        if (!TextUtils.isEmpty(obj)) {
            z = true;
        }
        mb.n("NetworkDetector", "[detect_conn]detectSync(),  isNeed wifi approve? " + z + " url: " + obj + " state: " + bn(this.DN));
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x002b A:{SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int c(boolean z, boolean z2) {
        int i = 0;
        if (fU()) {
            this.DN = -1;
        } else {
            int i2;
            if ((this.DQ <= 0 ? 1 : 0) == 0) {
                if ((Math.abs(System.currentTimeMillis() - this.DQ) > 300000 ? 1 : 0) == 0) {
                    i2 = 1;
                    if (z) {
                        if (z2 && i2 == 0) {
                            if (Math.abs(System.currentTimeMillis() - this.DQ) <= 60000) {
                                i = 1;
                            }
                            if (i == 0) {
                                this.mHandler.removeMessages(1);
                                this.mHandler.sendEmptyMessage(1);
                            }
                        }
                        if (this.DN == 0 && i2 == 0) {
                            this.DN = -5;
                        }
                    } else {
                        fV();
                    }
                }
            }
            i2 = 0;
            if (z) {
            }
        }
        mb.n("NetworkDetector", "[detect_conn]getNetworkState(), mNetworkState: " + bn(this.DN));
        return this.DN;
    }

    public void fT() {
        mb.n("NetworkDetector", "[detect_conn] onNetworkingchanging");
        this.DN = -4;
        this.DO = System.currentTimeMillis();
    }

    public void onConnected() {
        int i = 0;
        fT();
        if ((this.DQ <= 0 ? 1 : 0) == 0) {
            if ((Math.abs(System.currentTimeMillis() - this.DQ) >= 60000 ? 1 : 0) == 0) {
                i = 1;
            }
        }
        if (i == 0 && !this.DP) {
            mb.n("NetworkDetector", "[detect_conn]onConnected(), trigger detect in 5s");
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 5000);
            return;
        }
        mb.n("NetworkDetector", "[detect_conn]onConnected(), trigger detect in 60000");
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 60000);
    }

    public void onDisconnected() {
        mb.n("NetworkDetector", "[detect_conn]onDisconnected()");
        fT();
        this.mHandler.removeMessages(1);
        this.DN = -1;
    }

    public boolean x(long j) {
        if (this.DN == -4) {
            if (!(Math.abs(System.currentTimeMillis() - this.DO) >= j)) {
                return true;
            }
        }
        return false;
    }
}
