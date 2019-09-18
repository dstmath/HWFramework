package android.hwclipboarddelayread;

import android.common.HwFrameworkFactory;
import android.util.Log;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public class HwClipboardReadDelayer {
    private static final String TAG = "HwClipboardReadDelayer";
    private static IHwClipboardReadDelayer sInstance = null;

    public interface IHwClipboardReadDelayer {
        boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener iOnPrimaryClipGetedListener, String str);

        void getPrimaryClipNotify();

        boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener iOnPrimaryClipGetedListener);

        int setGetWaitTime(int i);

        void setPrimaryClipNotify();
    }

    private static synchronized IHwClipboardReadDelayer getImplObject() {
        synchronized (HwClipboardReadDelayer.class) {
            if (sInstance != null) {
                IHwClipboardReadDelayer iHwClipboardReadDelayer = sInstance;
                return iHwClipboardReadDelayer;
            }
            IHwClipboardReadDelayer instance = null;
            IHwClipboardReadDelayerFactory obj = HwFrameworkFactory.getHwClipboardReadDelayerFactory();
            if (obj != null) {
                instance = obj.getHwClipboardReadDelayerInstance();
            }
            if (instance != null) {
                sInstance = instance;
            } else {
                Log.d(TAG, "can't get impl object from vendor, use default implemention");
                sInstance = new HwClipboardReadDelayerDummy();
            }
            IHwClipboardReadDelayer iHwClipboardReadDelayer2 = sInstance;
            return iHwClipboardReadDelayer2;
        }
    }

    public static boolean addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        if (getImplObject() != null) {
            return getImplObject().addPrimaryClipGetedListener(listener, callingPackage);
        }
        return false;
    }

    public static boolean removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        if (getImplObject() != null) {
            return getImplObject().removePrimaryClipGetedListener(listener);
        }
        return false;
    }

    public static void setPrimaryClipNotify() {
        if (getImplObject() != null) {
            getImplObject().setPrimaryClipNotify();
        }
    }

    public static void getPrimaryClipNotify() {
        if (getImplObject() != null) {
            getImplObject().getPrimaryClipNotify();
        }
    }

    public static int setGetWaitTime(int waitTime) {
        if (getImplObject() != null) {
            return getImplObject().setGetWaitTime(waitTime);
        }
        return -1;
    }
}
