package android.widget.sr;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.ColorSpace.Rgb.TransferParameters;
import android.hwcontrol.HwWidgetFactory;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class HwSuperResolution {
    public static final int RET_ERR = -1;
    public static final int RET_OK = 0;
    public static final String TAG = "HwSuperResolution";
    private static boolean sFirstCheckLib;
    private HashMap<Bitmap, Integer> mBitmapFd = new HashMap();
    private Context mContext;
    private boolean mIsDeviceOwner;
    private PowerMode mPowerMode;
    private PowerModeReceiver mPowerModeReceiver;
    private SuperResolutionParameter mSupResPar;
    private HwSuperResolutionListener mSuperResolutionListener;
    private boolean mSupportSuperResolution;

    private native int nativeCancel(SuperResolutionParameter superResolutionParameter, int i);

    public static native void nativeErase(long j, int i);

    public static native int nativeGetFdFromPtr(long j);

    private native int nativeInitialize(HwSuperResolutionListener hwSuperResolutionListener, SuperResolutionParameter superResolutionParameter);

    private native int nativeProcess(SuperResolutionParameter superResolutionParameter, int i, int i2, int i3, int i4, int i5, int i6);

    public static native Bitmap nativeSRCreate(int[] iArr, int i, int i2, int i3, int i4, int i5, boolean z, float[] fArr, TransferParameters transferParameters);

    public static native int nativeSetReadOnly(int i);

    private native int nativeStart(HwSuperResolutionListener hwSuperResolutionListener, int i, SuperResolutionParameter superResolutionParameter);

    private native int nativeStop(SuperResolutionParameter superResolutionParameter);

    public HwSuperResolution(Context context, HwSuperResolutionListener superResolutionListener) {
        if (context == null) {
            this.mContext = setContextThroughReflect();
        } else {
            this.mContext = context.getApplicationContext();
        }
        this.mIsDeviceOwner = Utils.isDeviceOwner();
        this.mPowerMode = PowerMode.getInstance(this.mContext, this.mIsDeviceOwner);
        if (this.mContext != null) {
            initialBroadReceiver();
        }
        this.mSuperResolutionListener = superResolutionListener;
        this.mSupResPar = new SuperResolutionParameter();
        this.mSupportSuperResolution = Utils.isSuperResolutionSupport();
        if (sFirstCheckLib) {
            nativeInitialize(this.mSuperResolutionListener, this.mSupResPar);
        }
    }

    private void initialBroadReceiver() {
        this.mPowerModeReceiver = new PowerModeReceiver(this.mPowerMode);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PowerModeReceiver.CHANGE_MODE_ACTION);
        this.mContext.registerReceiver(this.mPowerModeReceiver, intentFilter);
    }

    private Context setContextThroughReflect() {
        try {
            return ((Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, (Object[]) null)).getApplicationContext();
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "ClassNotfound");
        } catch (NoSuchMethodException e2) {
            Log.w(TAG, "NoSuchMethodException");
        } catch (IllegalArgumentException e3) {
            Log.w(TAG, "IllegalArgumentException");
        } catch (IllegalAccessException e4) {
            Log.w(TAG, "IllegalAccessException");
        } catch (InvocationTargetException e5) {
            Log.w(TAG, "InvocationTargetException");
        }
        return null;
    }

    public int start(int scene) {
        if (sFirstCheckLib && this.mSupportSuperResolution && this.mIsDeviceOwner) {
            return nativeStart(this.mSuperResolutionListener, scene, this.mSupResPar);
        }
        return -1;
    }

    public int process(Bitmap bitmap, int ratio) {
        if (!this.mSupportSuperResolution || (sFirstCheckLib ^ 1) != 0) {
            Log.e(TAG, "SuperResolution is not supported in your platform");
            return -1;
        } else if (!this.mIsDeviceOwner) {
            Log.e(TAG, "You are not device owner, SuperResolution is not allowed");
            return -1;
        } else if (this.mPowerMode.isInSuperPowerState() || this.mPowerMode.isInNormalPowerState()) {
            Log.e(TAG, "Power is in save mode, process operation terminated");
            return -1;
        } else if (-1 == BitmapUtils.isBitmapIllegalSize(bitmap)) {
            Log.e(TAG, "Bitmap size is not illegal, quit process");
            return -1;
        } else {
            NativeBitmap srcBitmap = AshMemBitmap.createSrcNativeBitmap(bitmap);
            if (srcBitmap == null) {
                Log.e(TAG, "createSrcNativeBitmap failed");
                return -1;
            }
            NativeBitmap desBitmap = AshMemBitmap.createDesNativeBitmap(bitmap.getWidth(), bitmap.getHeight(), 4, ratio);
            if (desBitmap == null) {
                Log.e(TAG, "createDesNativeBitmap failed");
                return -1;
            }
            this.mSuperResolutionListener.addSrcFdBitmap(desBitmap.mFd, srcBitmap);
            this.mSuperResolutionListener.addDesFdBitmap(desBitmap.mFd, desBitmap);
            return nativeProcess(this.mSupResPar, srcBitmap.mFd, srcBitmap.getWidth(), srcBitmap.getHeight(), desBitmap.mFd, desBitmap.getWidth(), desBitmap.getHeight());
        }
    }

    public int cancel(Bitmap bitmap) {
        if (this.mBitmapFd == null || (this.mBitmapFd.containsKey(bitmap) ^ 1) != 0) {
            Log.e(TAG, "bitmap doesn't exist");
            return -1;
        }
        int ret = nativeCancel(this.mSupResPar, ((Integer) this.mBitmapFd.get(bitmap)).intValue());
        if (ret == 0) {
            this.mBitmapFd.remove(bitmap);
        }
        return ret;
    }

    public int stop() {
        if (!sFirstCheckLib || !this.mSupportSuperResolution || !this.mIsDeviceOwner) {
            return -1;
        }
        if (this.mSuperResolutionListener != null) {
            HwWidgetFactory.reportSrBigData(HwSuperResolutionListener.REPORT_PROCESS_ID, 0, Integer.valueOf(this.mSuperResolutionListener.getCurrentCount()));
            this.mSuperResolutionListener.resetCurrentCount();
        }
        return nativeStop(this.mSupResPar);
    }

    static {
        sFirstCheckLib = false;
        try {
            System.loadLibrary("jni_superresolution");
            sFirstCheckLib = true;
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "loadLibrary jni_superresolution failed");
        }
    }
}
