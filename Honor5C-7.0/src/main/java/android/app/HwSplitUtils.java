package android.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.WindowManager;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import java.math.BigDecimal;

public class HwSplitUtils {
    public static final int COLUMN_NUMBER_ONE = 1;
    public static final int COLUMN_NUMBER_TWO = 2;
    public static final String EXTRAS_HWSPLIT_SIZE = "extras_hw_split_size";
    public static final double SPLIT_LAND_DEFAULT = 5.5d;
    public static final double SPLIT_PORT_DEFAULT = 8.0d;
    private static String TAG = null;
    private static final int WIDTH_LIMIT_LAND = 592;
    private static final int WIDTH_LIMIT_PORT = 533;
    private static double mDeviceSize;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.HwSplitUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.HwSplitUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.HwSplitUtils.<clinit>():void");
    }

    public static boolean isNeedSplit(Context context) {
        if (context == null) {
            return false;
        }
        int appWidth = getAppWidth(context);
        double landSplitLimit = SPLIT_LAND_DEFAULT;
        double portSplitLimit = SPLIT_PORT_DEFAULT;
        Activity activity = (Activity) context;
        double[] splitSize = activity.getIntent().getDoubleArrayExtra(EXTRAS_HWSPLIT_SIZE);
        if (splitSize != null) {
            landSplitLimit = splitSize[0];
            portSplitLimit = splitSize[COLUMN_NUMBER_ONE];
        }
        return calculateColumnsNumber(activity, appWidth, landSplitLimit, portSplitLimit) == COLUMN_NUMBER_TWO;
    }

    private static boolean isScreenPotrait(Activity a) {
        int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == 0 || rotation == COLUMN_NUMBER_TWO) {
            return true;
        }
        return false;
    }

    private static int calculateColumnsNumber(Activity activity, int appWidth, double landSplitLimit, double portSplitLimit) {
        double sizeInch = calculateDeviceSize(activity);
        if (isScreenPotrait(activity)) {
            if (portSplitLimit > 0.0d && sizeInch > portSplitLimit && Math.abs(sizeInch - portSplitLimit) > 0.1d && appWidth >= dip2px(activity, 533.0f)) {
                return COLUMN_NUMBER_TWO;
            }
        } else if (landSplitLimit > 0.0d && sizeInch > landSplitLimit && Math.abs(sizeInch - landSplitLimit) > 0.1d && appWidth >= dip2px(activity, 592.0f)) {
            return COLUMN_NUMBER_TWO;
        }
        return COLUMN_NUMBER_ONE;
    }

    private static double calculateDeviceSize(Context context) {
        if (mDeviceSize > 0.0d) {
            return mDeviceSize;
        }
        IWindowManager iwm = Stub.asInterface(ServiceManager.checkService(FreezeScreenScene.WINDOW_PARAM));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay().getRealMetrics(dm);
        if (iwm != null) {
            Point point = new Point();
            try {
                iwm.getInitialDisplaySize(0, point);
                mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / dm.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / dm.ydpi), 2.0d))).setScale(COLUMN_NUMBER_TWO, 4).doubleValue();
                return mDeviceSize;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while calculate device size", e);
            }
        }
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(COLUMN_NUMBER_TWO, 4).doubleValue();
        return mDeviceSize;
    }

    private static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + HwFragmentMenuItemView.ALPHA_PRESSED);
    }

    public static int getAppWidth(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return (configuration.screenWidthDp * configuration.densityDpi) / PduHeaders.PREVIOUSLY_SENT_BY;
    }

    public static boolean isJumpedActivity(String invokerName, String calleesName) {
        if (invokerName.equals("com.huawei.android.hicloud.ui.activity.NewHiSyncSettingActivity")) {
            if ("com.huawei.android.hicloud.ui.activity.HisyncGuideActivity".equals(calleesName) || "com.huawei.android.hicloud.ui.activity.MainActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.huawei.android.hicloud.ui.activity.HisyncGuideActivity")) {
            if ("com.huawei.android.hicloud.ui.activity.PhoneFinderGuideActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.huawei.android.hicloud.ui.activity.PhoneFinderGuideActivity")) {
            if ("com.huawei.android.hicloud.ui.activity.MainActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.android.settings.fingerprint.FingerprintSettingsActivity")) {
            if ("com.android.settings.ChooseLockGeneric".equals(calleesName) || "com.android.settings.ConfirmLockPassword$InternalActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.android.settings.ChooseLockGeneric")) {
            if ("com.android.settings.ConfirmLockPattern$InternalActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.android.settings.Settings$StorageSettingsActivity") && "com.android.settings.deviceinfo.PrivateVolumeSettings".equals(calleesName)) {
            return true;
        } else {
            return false;
        }
    }
}
