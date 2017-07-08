package defpackage;

import com.huawei.android.pushagent.utils.multicard.MultiCard;
import java.lang.reflect.InvocationTargetException;

/* renamed from: bp */
class bp implements MultiCard {
    private static bp ci;

    private bp() {
    }

    public static synchronized bp ck() {
        bp bpVar;
        synchronized (bp.class) {
            if (ci == null) {
                ci = new bp();
            }
            bpVar = ci;
        }
        return bpVar;
    }

    private static Object cl() {
        Object obj = null;
        try {
            Class cls = Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            obj = cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Exception e) {
            aw.v("mutiCardMTKImpl", " getDefaultTelephonyManagerEx wrong " + e.toString());
        }
        return obj;
    }

    public String getDeviceId(int i) {
        String str;
        String str2 = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object cl = bp.cl();
            if (cl != null) {
                str = (String) cl.getClass().getMethod("getDeviceId", clsArr).invoke(cl, objArr);
                return str != null ? "" : str;
            }
        } catch (NoSuchMethodException e) {
            aw.v("mutiCardMTKImpl", "MultiCardMTKImpl getDeviceId NoSuchMethodException:" + e.toString());
            str = str2;
        } catch (IllegalAccessException e2) {
            aw.v("mutiCardMTKImpl", "MultiCardMTKImpl getDeviceId IllegalAccessException:" + e2.toString());
            str = str2;
        } catch (InvocationTargetException e3) {
            aw.v("mutiCardMTKImpl", "MultiCardMTKImpl getDeviceId InvocationTargetException:" + e3.toString());
            str = str2;
        } catch (Exception e4) {
            aw.v("mutiCardMTKImpl", "MultiCardMTKImpl getDeviceId exception:" + e4.toString());
        }
        str = str2;
        if (str != null) {
        }
    }
}
