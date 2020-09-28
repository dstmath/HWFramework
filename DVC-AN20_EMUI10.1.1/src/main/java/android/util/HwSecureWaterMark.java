package android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.nio.ByteBuffer;

public class HwSecureWaterMark {
    public static final boolean DEBUG = false;
    public static final int FIRST_SIM_SLOT = 0;
    private static final long FLAG_MSPES_CONFIG_WATERMARK = 4;
    public static final int MAX_NUMER = 60000;
    public static final int SECOND_SIM_SLOT = 1;
    static final String TAG = "HwSecureWaterMark";
    private static String mPhoneImei = null;
    private static String mPhoneMeid = null;
    private static int mWatermarkNumber = getWatermarkNumber();

    public static native int addWatermark_native(byte[] bArr, int i, int i2, int i3, int i4);

    static {
        System.loadLibrary("SecureWaterMark_jni");
    }

    public static boolean isWatermarkEnable() {
        String mspesConfig = SystemProperties.get("ro.mspes.config", (String) null);
        if (!TextUtils.isEmpty(mspesConfig)) {
            try {
                if ((4 & Long.decode(mspesConfig.trim()).longValue()) != 0) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                Slog.e(TAG, " ro.mspes.config  is not a number");
                return false;
            }
        } else if (!SystemProperties.getBoolean("ro.config.hw_watermark", false)) {
            return false;
        } else {
            boolean isFastbootUnlock = SystemProperties.getBoolean("ro.fastboot.unlock", false);
            if (SystemProperties.getBoolean("ro.build.hide", false) || isFastbootUnlock) {
                return true;
            }
            return false;
        }
    }

    public static boolean isWatermarkReady() {
        if (getWatermarkNumber() > 0) {
            return true;
        }
        return false;
    }

    public static int getWatermarkNumber(Context context) {
        mWatermarkNumber = getWatermarkNumber();
        int i = mWatermarkNumber;
        if (i > 0) {
            return i;
        }
        try {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
            if (telephony == null) {
                Log.e(TAG, "onCreate-> telephony is null, can not init imei or meid");
            } else {
                String imei1 = telephony.getImei(0);
                String secondImei = telephony.getImei(1);
                String meid1 = HwTelephonyManagerInner.getDefault().getMeid(0);
                String secondMeid = HwTelephonyManagerInner.getDefault().getMeid(1);
                if (imei1 != null && imei1.length() > 0) {
                    mPhoneImei = imei1.substring(imei1.length() - 5, imei1.length());
                    int parseInt = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                    mWatermarkNumber = parseInt;
                    return parseInt;
                } else if (secondImei != null && secondImei.length() > 0) {
                    mPhoneImei = secondImei.substring(secondImei.length() - 5, secondImei.length());
                    int parseInt2 = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                    mWatermarkNumber = parseInt2;
                    return parseInt2;
                } else if (meid1 != null && meid1.length() > 0) {
                    mPhoneMeid = meid1.substring(meid1.length() - 4, meid1.length());
                    int parseInt3 = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                    mWatermarkNumber = parseInt3;
                    return parseInt3;
                } else if (secondMeid != null && secondMeid.length() > 0) {
                    mPhoneMeid = secondMeid.substring(secondMeid.length() - 4, secondMeid.length());
                    int parseInt4 = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                    mWatermarkNumber = parseInt4;
                    return parseInt4;
                }
            }
        } catch (RuntimeException e) {
            Slog.e(TAG, "getWatermarkNumber, 1 telephony.getImei RuntimeException");
        }
        return mWatermarkNumber;
    }

    public static int getWatermarkNumber() {
        int i = mWatermarkNumber;
        if (i > 0) {
            return i;
        }
        mWatermarkNumber = -1;
        try {
            if (mPhoneImei != null) {
                mWatermarkNumber = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                return mWatermarkNumber;
            } else if (mPhoneMeid != null) {
                int parseInt = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                mWatermarkNumber = parseInt;
                return parseInt;
            } else {
                mPhoneImei = TelephonyManager.getDefault().getImei();
                if (mPhoneImei == null || "unknown".equals(mPhoneImei)) {
                    mPhoneImei = Build.getSerial();
                }
                if (mPhoneImei == null || mPhoneImei.length() <= 0) {
                    mPhoneMeid = HwTelephonyManagerInner.getDefault().getMeid();
                    if (mPhoneMeid != null) {
                        mPhoneMeid = mPhoneMeid.substring(mPhoneMeid.length() - 4, mPhoneMeid.length());
                        int parseInt2 = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                        mWatermarkNumber = parseInt2;
                        return parseInt2;
                    }
                    return mWatermarkNumber;
                }
                mPhoneImei = mPhoneImei.substring(mPhoneImei.length() - 5, mPhoneImei.length());
                mWatermarkNumber = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                return mWatermarkNumber;
            }
        } catch (RuntimeException e) {
            Slog.e(TAG, "getWatermarkNumber, RuntimeException");
        }
    }

    public static synchronized Bitmap addWatermark(Bitmap srcbmp, int watermarkNumber) {
        synchronized (HwSecureWaterMark.class) {
            if (!isWatermarkEnable()) {
                Slog.e(TAG, "addWatermark, watermark disable");
                return srcbmp;
            } else if (srcbmp == null) {
                Slog.e(TAG, "addWatermark, srcbmp == null");
                return null;
            } else if (watermarkNumber < 0 || watermarkNumber >= 60000) {
                Slog.e(TAG, "addWatermark, iWatermarkNumber invaid");
                return null;
            } else {
                Bitmap.Config bmpconfig = srcbmp.getConfig();
                int format = -1;
                if (bmpconfig == Bitmap.Config.RGB_565) {
                    format = 2;
                } else if (bmpconfig == Bitmap.Config.ARGB_8888) {
                    format = 4;
                }
                if (format < 0) {
                    Slog.e(TAG, "addWatermark, format invaid");
                    return null;
                }
                Bitmap copybitmap = srcbmp.copy(Bitmap.Config.ARGB_8888, true);
                if (copybitmap == null) {
                    Slog.e(TAG, "addWatermark, copybitmap fail");
                    return null;
                }
                ByteBuffer mBuffer = ByteBuffer.allocate(copybitmap.getByteCount());
                copybitmap.copyPixelsToBuffer(mBuffer);
                mBuffer.rewind();
                byte[] bytes = mBuffer.array();
                if (addWatermark_native(bytes, srcbmp.getWidth(), srcbmp.getHeight(), 4, watermarkNumber) < 0) {
                    return null;
                }
                Bitmap dscbitmap = copybitmap;
                dscbitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
                if (bmpconfig != dscbitmap.getConfig()) {
                    dscbitmap = dscbitmap.copy(bmpconfig, true);
                }
                return dscbitmap;
            }
        }
    }

    public static Bitmap addWatermark(Bitmap srcbmp) {
        return addWatermark(srcbmp, getWatermarkNumber());
    }

    public static Bitmap addWatermark(Bitmap srcbmp, Context context) {
        return addWatermark(srcbmp, getWatermarkNumber(context));
    }
}
