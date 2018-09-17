package tmsdkobf;

import android.content.Context;
import android.os.HandlerThread;
import com.qq.taf.jce.JceStruct;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.roach.nest.ISharkCallBackNest;
import tmsdk.common.tcc.TccDiff;
import tmsdk.common.utils.q;

public class pt {
    public static final md KB = new md("r_entities_sp");

    public static void addTask(Runnable runnable, String str) {
        try {
            ps.i("addTask-task:[" + runnable + "]taskName:[" + str + "]");
            im.bJ().addTask(runnable, str);
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
        }
    }

    public static int bsPatch(String str, String str2, String str3, int i) {
        return TccDiff.bsPatch(str, str2, str3, i);
    }

    public static int download(String str, String str2, String str3) {
        int a;
        try {
            ps.i("download-url:[" + str + "]fileDir:[" + str2 + "]fileName:[" + str3 + "]");
            if (q.cK(str) || q.cK(str2) || q.cK(str3)) {
                return -57;
            }
            lx lxVar = new lx(TMSDKContext.getApplicaionContext());
            lxVar.bP(str2);
            lxVar.bQ(str3);
            a = lxVar.a(null, str, false, null);
            return a;
        } catch (Throwable th) {
            a = ErrorCode.ERR_GET;
            ps.h("e:[" + th + "]");
        }
    }

    public static String fileMd5(String str) {
        return TccDiff.fileMd5(str);
    }

    public static Context getAppContext() {
        return TMSDKContext.getApplicaionContext();
    }

    public static String getByteMd5(byte[] bArr) {
        return TccDiff.getByteMd5(bArr);
    }

    public static int getInt(String str, int i) {
        return KB.getInt(str, i);
    }

    public static long getLong(String str, long j) {
        return KB.getLong(str, j);
    }

    public static String getString(String str, String str2) {
        return KB.getString(str, str2);
    }

    public static HandlerThread newFreeHandlerThread(String str) {
        HandlerThread handlerThread = null;
        try {
            ps.i("newFreeHandlerThread-taskName:[" + str + "]");
            return im.bJ().newFreeHandlerThread(str);
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
            return handlerThread;
        }
    }

    public static Thread newFreeThread(Runnable runnable, String str) {
        Thread thread = null;
        try {
            ps.i("newFreeThread-task:[" + runnable + "]taskName:[" + str + "]");
            return im.bJ().newFreeThread(runnable, str);
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
            return thread;
        }
    }

    public static void putInt(String str, int i) {
        KB.a(str, i, true);
    }

    public static void putLong(String str, long j) {
        KB.a(str, j, true);
    }

    public static void putString(String str, String str2) {
        KB.a(str, str2, true);
    }

    public static void remove(String str) {
        KB.remove(str);
    }

    public static int runHttpSession(int i, String str, String str2, HashMap<String, Object> hashMap, String str3, Class<?> cls, AtomicReference<Object> atomicReference) {
        int a;
        try {
            if (q.cK(str) || q.cK(str2) || hashMap == null) {
                return -57;
            }
            pq pqVar = (pq) ManagerCreatorC.getManager(pq.class);
            hashMap.put("phonetype", pqVar.hV().ht());
            hashMap.put("userinfo", pqVar.hV().hu());
            pp ppVar = new pp(i, new pn(str, str2));
            ppVar.Kv = hashMap;
            a = pqVar.hV().a(ppVar);
            if (a != 0) {
                return a;
            }
            if (!(!q.cJ(str3) || cls == null || atomicReference == null)) {
                Object a2 = pqVar.hV().a(ppVar.Kx, str3, cls.newInstance());
                if (a2 != null) {
                    atomicReference.set(a2);
                }
            }
            return a;
        } catch (Throwable th) {
            a = ErrorCode.ERR_WUP;
        }
    }

    public static void saveActionData(int i) {
        try {
            kt.saveActionData(i);
        } catch (Throwable th) {
        }
    }

    public static void saveMultiValueData(int i, int i2) {
        try {
            kt.saveMultiValueData(i, i2);
        } catch (Throwable th) {
        }
    }

    public static void saveStringData(int i, String str) {
        try {
            kt.e(i, str);
        } catch (Throwable th) {
        }
    }

    public static boolean sendShark(int i, JceStruct jceStruct, JceStruct jceStruct2, int i2, final ISharkCallBackNest iSharkCallBackNest, long j) {
        if (jceStruct == null || iSharkCallBackNest == null) {
            return false;
        }
        try {
            ob bK = im.bK();
            ps.i("sendShark-sharkProxy:[" + bK + "]cmdId:[" + i + "]req:[" + jceStruct + "]resp:[" + jceStruct2 + "]flag:[" + i2 + "]callback:[" + iSharkCallBackNest + "]callBackTimeout:[" + j + "]");
            return bK.a(i, jceStruct, jceStruct2, i2, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    ps.i("sendShark-onFinish-retCode:[" + i3 + "]dataRetCode:[" + i4 + "]");
                    iSharkCallBackNest.onFinish(i, i2, i3, i4, jceStruct);
                }
            }, j) != null;
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
            return false;
        }
    }

    public static void tryReportData() {
        try {
            kr.p(true);
        } catch (Throwable th) {
        }
    }
}
