package tmsdk.common;

import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.f;
import tmsdkobf.im;
import tmsdkobf.mi;
import tmsdkobf.qc;

public class DualSimTelephonyManager implements tmsdkobf.im.a {
    private static DualSimTelephonyManager wZ;
    private static final String[] xa = new String[]{"phone1", "phone2", "phoneEX"};
    private ArrayList<a> xb = new ArrayList(2);

    static class a {
        public WeakReference<PhoneStateListener> xc;
        public int xd;
        public boolean xe;
        public TelephonyManager xf;

        public a(PhoneStateListener phoneStateListener, int i, boolean z, TelephonyManager telephonyManager) {
            this.xc = new WeakReference(phoneStateListener);
            this.xd = i;
            this.xe = z;
            this.xf = telephonyManager;
        }
    }

    private DualSimTelephonyManager() {
        im.a(this);
    }

    private a a(PhoneStateListener phoneStateListener, int i, int i2) {
        TelephonyManager telephonyManager;
        boolean z = false;
        switch (i2) {
            case -1:
            case 0:
                telephonyManager = (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService("phone");
                break;
            case 1:
                qc qcVar = im.rE;
                if (qcVar == null || !qcVar.iu()) {
                    telephonyManager = getSecondTelephonyManager();
                    break;
                }
                telephonyManager = (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService("phone");
                break;
                break;
            default:
                return null;
        }
        if (telephonyManager != null) {
            try {
                telephonyManager.listen(phoneStateListener, i);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (i2 == 1) {
            z = true;
        }
        return new a(phoneStateListener, i, z, telephonyManager);
    }

    public static ITelephony getDefaultTelephony() {
        ITelephony iTelephony = null;
        qc qcVar = im.rE;
        if (qcVar != null) {
            iTelephony = qcVar.bS(0);
            if (iTelephony != null) {
                return iTelephony;
            }
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService("phone");
            if (telephonyManager == null) {
                return null;
            }
            Method declaredMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
            if (declaredMethod == null) {
                return null;
            }
            declaredMethod.setAccessible(true);
            iTelephony = (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
            return iTelephony;
        } catch (Throwable e) {
            f.b("DualSimTelephonyManager", "getDefaultTelephony", e);
        } catch (Throwable e2) {
            f.b("DualSimTelephonyManager", "getDefaultTelephony", e2);
        } catch (Throwable e22) {
            f.b("DualSimTelephonyManager", "getDefaultTelephony", e22);
        } catch (Throwable e222) {
            f.b("DualSimTelephonyManager", "getDefaultTelephony", e222);
        } catch (Throwable e2222) {
            f.b("DualSimTelephonyManager", "getDefaultTelephony", e2222);
        }
    }

    public static synchronized DualSimTelephonyManager getInstance() {
        DualSimTelephonyManager dualSimTelephonyManager;
        synchronized (DualSimTelephonyManager.class) {
            if (wZ == null) {
                wZ = new DualSimTelephonyManager();
            }
            dualSimTelephonyManager = wZ;
        }
        return dualSimTelephonyManager;
    }

    public static ITelephony getSecondTelephony() {
        qc qcVar = im.rE;
        if (qcVar != null) {
            ITelephony bS = qcVar.bS(1);
            if (bS != null) {
                return bS;
            }
        }
        for (String str : xa) {
            if (mi.checkService(str) != null) {
                IBinder service = mi.getService(str);
                if (service != null) {
                    return Stub.asInterface(service);
                }
            }
        }
        return null;
    }

    public TelephonyManager getSecondTelephonyManager() {
        Exception e;
        qc qcVar = im.rE;
        if (qcVar != null) {
            String iq = qcVar.iq();
            if (!(iq == null || mi.checkService(iq) == null)) {
                return (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService(iq);
            }
        }
        try {
            for (String str : xa) {
                if (mi.checkService(str) != null) {
                    return (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService(str);
                }
            }
        } catch (Exception e2) {
            f.f("DualSimTelephonyManager", e2);
        }
        try {
            for (String str2 : xa) {
                if (mi.checkService(str2) != null) {
                    return (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService(str2);
                }
            }
        } catch (Exception e3) {
            f.e("DualSimTelephonyManager", e3);
            e2 = e3;
        }
        return null;
    }

    public void handleSdkContextEvent(int i) {
        if (i == 1) {
            reListenPhoneState();
        }
    }

    public boolean listenPhonesState(int i, PhoneStateListener phoneStateListener, int i2) {
        a a = a(phoneStateListener, i2, i);
        if (a == null) {
            return false;
        }
        Iterator it = this.xb.iterator();
        Object obj = null;
        a aVar = null;
        while (obj == null && it.hasNext()) {
            aVar = (a) it.next();
            if (aVar.xe == (i == 1) && aVar.xc.get() == phoneStateListener) {
                obj = 1;
            }
        }
        if (obj == null) {
            if (i2 != 0) {
                this.xb.add(a);
            }
        } else if (i2 != 0) {
            aVar.xd = Integer.valueOf(i2).intValue();
        } else {
            it.remove();
        }
        return true;
    }

    public void reListenPhoneState() {
        Iterator it = this.xb.iterator();
        while (it.hasNext()) {
            a aVar = (a) it.next();
            PhoneStateListener phoneStateListener = (PhoneStateListener) aVar.xc.get();
            if (phoneStateListener != null) {
                if (aVar.xf != null) {
                    aVar.xf.listen(phoneStateListener, 0);
                }
                a a = a(phoneStateListener, aVar.xd, !aVar.xe ? 1 : 0);
                if (a != null) {
                    aVar.xf = a.xf;
                }
            }
        }
    }
}
