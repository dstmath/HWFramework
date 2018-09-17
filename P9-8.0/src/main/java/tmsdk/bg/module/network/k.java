package tmsdk.bg.module.network;

import android.util.Log;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.im;
import tmsdkobf.lu;
import tmsdkobf.ov;
import tmsdkobf.oz;

final class k {
    private final String TAG = "TrafficStats";
    private Method wF;
    private Method wG;
    private boolean wH;
    private boolean wI;
    private boolean wJ = false;

    public k() {
        try {
            Class cls = Class.forName("android.net.TrafficStats");
            this.wF = cls.getDeclaredMethod("getUidRxBytes", new Class[]{Integer.TYPE});
            this.wG = cls.getDeclaredMethod("getUidTxBytes", new Class[]{Integer.TYPE});
            this.wH = true;
        } catch (Exception e) {
            this.wH = false;
            e.printStackTrace();
        }
        if (this.wH) {
            im.bJ().addTask(new Runnable() {
                public void run() {
                    boolean z = true;
                    try {
                        z = k.this.dv();
                    } catch (OutOfMemoryError e) {
                        Log.w("TrafficStats", e.getMessage());
                    }
                    if (!z) {
                        k.this.wI = new File("/proc/uid_stat").exists();
                        if (k.this.wI) {
                            k.this.wH = false;
                        } else {
                            k.this.wH = true;
                        }
                    }
                    Log.i("TrafficStats", "^^ mAPISupported" + k.this.wH + " mFileSupported " + k.this.wI);
                }
            }, "checkAPIAvaliable");
            return;
        }
        this.wI = new File("/proc/uid_stat").exists();
        Log.i("TrafficStats", "^^ mAPISupported" + this.wH + " mFileSupported " + this.wI);
    }

    private boolean a(ov ovVar) {
        if (ovVar == null || ovVar.hA() == null) {
            return false;
        }
        boolean z = false;
        for (Object equals : ovVar.hA()) {
            if ("android.permission.INTERNET".equals(equals)) {
                z = true;
                break;
            }
        }
        return z;
    }

    private long c(int i, String str, String str2) {
        String[] e;
        long j = -1;
        File file = new File("/proc/uid_stat/" + i + "/" + str);
        File file2 = new File("/proc/uid_stat/" + i + "/" + str2);
        if (file.exists()) {
            e = lu.e(file);
            if (e != null && e.length > 0) {
                j = Long.parseLong(e[0]);
            }
        }
        if (!file2.exists()) {
            return j;
        }
        e = lu.e(file2);
        if (e == null || e.length <= 0) {
            return j;
        }
        return !file.exists() ? Long.parseLong(e[0]) : j + Long.parseLong(e[0]);
    }

    private boolean dv() {
        long currentTimeMillis = System.currentTimeMillis();
        ArrayList f = ((oz) ManagerCreatorC.getManager(oz.class)).f(34, 0);
        if (f == null || f.size() == 0) {
            f = ((oz) ManagerCreatorC.getManager(oz.class)).f(34, 1);
        }
        if (f == null || f.size() == 0) {
            return false;
        }
        int size = f.size();
        String packageName = TMSDKContext.getApplicaionContext().getPackageName();
        boolean z = false;
        int i = 0;
        while (i < size) {
            if (a((ov) f.get(i))) {
                int i2;
                if (getUidRxBytes(((ov) f.get(i)).getUid()) <= 0) {
                    i2 = 1;
                } else {
                    boolean i22 = false;
                }
                if (i22 == 0 && !((ov) f.get(i)).getPackageName().equals(packageName)) {
                    Log.i("TrafficStats", "^^ check traffic api avaliable count " + i);
                    z = true;
                    break;
                }
            }
            i++;
        }
        Log.i("TrafficStats", "^^ check time " + (System.currentTimeMillis() - currentTimeMillis) + " isAvaliable " + z);
        return z;
    }

    public long getUidRxBytes(int i) {
        long j = -1;
        if (!this.wH) {
            return !this.wI ? j : c(i, "tcp_rcv", "udp_rcv");
        } else {
            try {
                return Long.valueOf(this.wF.invoke(null, new Object[]{Integer.valueOf(i)}).toString()).longValue();
            } catch (Exception e) {
                e.printStackTrace();
                return j;
            }
        }
    }

    public long getUidTxBytes(int i) {
        long j = -1;
        if (!this.wH) {
            return !this.wI ? j : c(i, "tcp_snd", "udp_snd");
        } else {
            try {
                return Long.valueOf(this.wG.invoke(null, new Object[]{Integer.valueOf(i)}).toString()).longValue();
            } catch (Exception e) {
                e.printStackTrace();
                return j;
            }
        }
    }

    public boolean isSupportTrafficState() {
        return this.wI || this.wH;
    }
}
