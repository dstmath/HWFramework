package huawei.android.app;

import android.common.HwActivityThread;
import android.os.Build;
import android.os.SystemProperties;
import java.lang.reflect.Field;

public class HwActivityThreadImpl implements HwActivityThread {
    private static final String TAG = "HwActivityThreadImpl";
    private static HwActivityThreadImpl sInstance;

    public static synchronized HwActivityThreadImpl getDefault() {
        HwActivityThreadImpl hwActivityThreadImpl;
        synchronized (HwActivityThreadImpl.class) {
            if (sInstance == null) {
                sInstance = new HwActivityThreadImpl();
            }
            hwActivityThreadImpl = sInstance;
        }
        return hwActivityThreadImpl;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x003b A:{Splitter: B:8:0x0029, ExcHandler: java.lang.NoSuchFieldException (e java.lang.NoSuchFieldException)} */
    /* JADX WARNING: Missing block: B:11:0x003c, code:
            android.util.Log.e(TAG, "modify Build.MODEL fail!");
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeToSpecialModel(String pkgName) {
        String strHwModel = SystemProperties.get("ro.product.hw_model", "");
        if (pkgName != null && (strHwModel.equals("") ^ 1) != 0) {
            if (pkgName.equals("com.sina.weibo") || pkgName.equals("com.tencent.mobileqq")) {
                try {
                    Field field = Build.class.getField("MODEL");
                    field.setAccessible(true);
                    field.set(null, strHwModel);
                } catch (NoSuchFieldException e) {
                }
            }
        }
    }
}
