package android.hwclipboarddelayread;

import android.common.HwFrameworkFactory;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class HwClipboardReadDelayRegister {
    private static final String TAG = "HwClipboardReadDelayRegister";
    private static IHwClipboardReadDelayRegister serviceRegistInstance = null;

    public interface IHwClipboardReadDelayRegister {
        void addPrimaryClipGetedListener(ClipboardManager.OnPrimaryClipGetedListener onPrimaryClipGetedListener, Context context, Handler handler);

        void removePrimaryClipGetedListener();

        void setGetWaitTime(int i);
    }

    private static synchronized IHwClipboardReadDelayRegister getImplObject() {
        synchronized (HwClipboardReadDelayRegister.class) {
            if (serviceRegistInstance != null) {
                return serviceRegistInstance;
            }
            IHwClipboardReadDelayRegister instance = null;
            IHwClipboardReadDelayRegisterFactory obj = HwFrameworkFactory.getHwClipboardReadDelayRegisterFactory();
            if (obj != null) {
                instance = obj.getHwClipboardReadDelayRegisterInstance();
            }
            if (instance != null) {
                serviceRegistInstance = instance;
            } else {
                Log.d(TAG, "can't get impl object from vendor, use default implemention");
                serviceRegistInstance = new HwClipboardReadDelayRegisterDummy();
            }
            return serviceRegistInstance;
        }
    }

    public static boolean addPrimaryClipGetedListener(ClipboardManager.OnPrimaryClipGetedListener what, Context context, Handler handler) {
        if (getImplObject() == null) {
            return false;
        }
        getImplObject().addPrimaryClipGetedListener(what, context, handler);
        return true;
    }

    public static boolean removePrimaryClipGetedListener() {
        if (getImplObject() == null) {
            return false;
        }
        getImplObject().removePrimaryClipGetedListener();
        return true;
    }

    public static void setGetWaitTime(int waitTime) {
        if (getImplObject() != null) {
            getImplObject().setGetWaitTime(waitTime);
        }
    }
}
