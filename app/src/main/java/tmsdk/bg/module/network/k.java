package tmsdk.bg.module.network;

import android.util.Log;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.jq;
import tmsdkobf.ms;
import tmsdkobf.py;
import tmsdkobf.qc;

/* compiled from: Unknown */
final class k {
    private final String TAG;
    private Method zv;
    private Method zw;
    private boolean zx;
    private boolean zy;
    private boolean zz;

    public k() {
        this.TAG = "TrafficStats";
        this.zz = false;
        try {
            Class cls = Class.forName("android.net.TrafficStats");
            this.zv = cls.getDeclaredMethod("getUidRxBytes", new Class[]{Integer.TYPE});
            this.zw = cls.getDeclaredMethod("getUidTxBytes", new Class[]{Integer.TYPE});
            this.zx = true;
        } catch (Exception e) {
            this.zx = false;
            e.printStackTrace();
        }
        if (this.zx) {
            jq.ct().a(new Runnable() {
                final /* synthetic */ k zA;

                {
                    this.zA = r1;
                }

                public void run() {
                    boolean a;
                    try {
                        a = this.zA.ek();
                    } catch (OutOfMemoryError e) {
                        Log.w("TrafficStats", e.getMessage());
                        a = true;
                    }
                    if (!a) {
                        this.zA.zy = new File("/proc/uid_stat").exists();
                        if (this.zA.zy) {
                            this.zA.zx = false;
                        } else {
                            this.zA.zx = true;
                        }
                    }
                    Log.i("TrafficStats", "^^ mAPISupported" + this.zA.zx + " mFileSupported " + this.zA.zy);
                }
            }, "checkAPIAvaliable");
            return;
        }
        this.zy = new File("/proc/uid_stat").exists();
        Log.i("TrafficStats", "^^ mAPISupported" + this.zx + " mFileSupported " + this.zy);
    }

    private boolean b(py pyVar) {
        boolean z = false;
        if (pyVar == null || pyVar.hF() == null) {
            return false;
        }
        for (Object equals : pyVar.hF()) {
            if ("android.permission.INTERNET".equals(equals)) {
                z = true;
                break;
            }
        }
        return z;
    }

    private long c(int i, String str, String str2) {
        long j = -1;
        File file = new File("/proc/uid_stat/" + i + "/" + str);
        File file2 = new File("/proc/uid_stat/" + i + "/" + str2);
        if (file.exists()) {
            String[] b = ms.b(file);
            if (b != null && b.length > 0) {
                j = Long.parseLong(b[0]);
            }
        }
        if (!file2.exists()) {
            return j;
        }
        String[] b2 = ms.b(file2);
        return (b2 == null || b2.length <= 0) ? j : !file.exists() ? Long.parseLong(b2[0]) : j + Long.parseLong(b2[0]);
    }

    private boolean ek() {
        long currentTimeMillis = System.currentTimeMillis();
        ArrayList c = ((qc) ManagerCreatorC.getManager(qc.class)).c(34, 0);
        if (c == null || c.size() == 0) {
            c = ((qc) ManagerCreatorC.getManager(qc.class)).c(34, 1);
        }
        ArrayList arrayList = c;
        if (arrayList == null || arrayList.size() == 0) {
            return false;
        }
        boolean z;
        int size = arrayList.size();
        String packageName = TMSDKContext.getApplicaionContext().getPackageName();
        int i = 0;
        while (i < size) {
            if (b((py) arrayList.get(i))) {
                if ((getUidRxBytes(((py) arrayList.get(i)).getUid()) <= 0 ? 1 : 0) == 0 && !((py) arrayList.get(i)).getPackageName().equals(packageName)) {
                    Log.i("TrafficStats", "^^ check traffic api avaliable count " + i);
                    z = true;
                    break;
                }
            }
            i++;
        }
        z = false;
        Log.i("TrafficStats", "^^ check time " + (System.currentTimeMillis() - currentTimeMillis) + " isAvaliable " + z);
        return z;
    }

    public long getUidRxBytes(int i) {
        if (this.zx) {
            try {
                return Long.valueOf(this.zv.invoke(null, new Object[]{Integer.valueOf(i)}).toString()).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (this.zy) {
            return c(i, "tcp_rcv", "udp_rcv");
        }
        return -1;
    }

    public long getUidTxBytes(int i) {
        if (this.zx) {
            try {
                return Long.valueOf(this.zw.invoke(null, new Object[]{Integer.valueOf(i)}).toString()).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (this.zy) {
            return c(i, "tcp_snd", "udp_snd");
        }
        return -1;
    }

    public boolean isSupportTrafficState() {
        return this.zy || this.zx;
    }
}
