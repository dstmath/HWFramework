package android.util;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import java.nio.ByteBuffer;

public class HwSecureWaterMark {
    public static final boolean DEBUG = false;
    public static final int FIRST_SIM_SLOT = 0;
    public static final int MAX_NUMER = 60000;
    public static final int SECOND_SIM_SLOT = 1;
    static final String TAG = "HwSecureWaterMark";
    private static String mPhoneImei;
    private static String mPhoneMeid;
    private static int mWatermarkNumber;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.HwSecureWaterMark.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.HwSecureWaterMark.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.HwSecureWaterMark.<clinit>():void");
    }

    public static native int addWatermark_native(byte[] bArr, int i, int i2, int i3, int i4);

    public static boolean isWatermarkEnable() {
        if (!SystemProperties.getBoolean("ro.config.hw_watermark", DEBUG)) {
            return DEBUG;
        }
        boolean FASTBOOT_UNLOCK = SystemProperties.getBoolean("ro.fastboot.unlock", DEBUG);
        if (SystemProperties.getBoolean("ro.build.hide", DEBUG) || FASTBOOT_UNLOCK) {
            return true;
        }
        return DEBUG;
    }

    public static boolean isWatermarkReady() {
        if (getWatermarkNumber() > 0) {
            return true;
        }
        return DEBUG;
    }

    public static int getWatermarkNumber(Context context) {
        mWatermarkNumber = getWatermarkNumber();
        if (mWatermarkNumber > 0) {
            return mWatermarkNumber;
        }
        try {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
            if (telephony == null) {
                Log.e(TAG, "onCreate-> telephony is null, can not init imei or meid");
            } else {
                String imei1 = telephony.getImei(FIRST_SIM_SLOT);
                String secondImei = telephony.getImei(SECOND_SIM_SLOT);
                String meid1 = HwTelephonyManagerInner.getDefault().getMeid(FIRST_SIM_SLOT);
                String secondMeid = HwTelephonyManagerInner.getDefault().getMeid(SECOND_SIM_SLOT);
                int parseInt;
                if (imei1 != null && imei1.length() > 0) {
                    mPhoneImei = imei1.substring(imei1.length() - 5, imei1.length());
                    parseInt = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                    mWatermarkNumber = parseInt;
                    return parseInt;
                } else if (secondImei != null && secondImei.length() > 0) {
                    mPhoneImei = secondImei.substring(secondImei.length() - 5, secondImei.length());
                    parseInt = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                    mWatermarkNumber = parseInt;
                    return parseInt;
                } else if (meid1 != null && meid1.length() > 0) {
                    mPhoneMeid = meid1.substring(meid1.length() - 4, meid1.length());
                    parseInt = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                    mWatermarkNumber = parseInt;
                    return parseInt;
                } else if (secondMeid != null && secondMeid.length() > 0) {
                    mPhoneMeid = secondMeid.substring(secondMeid.length() - 4, secondMeid.length());
                    parseInt = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                    mWatermarkNumber = parseInt;
                    return parseInt;
                }
            }
        } catch (RuntimeException e) {
            Slog.e(TAG, "getWatermarkNumber, 1 telephony.getImei RuntimeException");
        }
        return mWatermarkNumber;
    }

    public static int getWatermarkNumber() {
        if (mWatermarkNumber > 0) {
            return mWatermarkNumber;
        }
        mWatermarkNumber = -1;
        try {
            if (mPhoneImei != null) {
                mWatermarkNumber = Integer.parseInt(mPhoneImei) % MAX_NUMER;
                return mWatermarkNumber;
            } else if (mPhoneMeid != null) {
                r3 = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                mWatermarkNumber = r3;
                return r3;
            } else {
                mPhoneImei = HwFrameworkFactory.getHwInnerTelephonyManager().getUniqueDeviceId(SECOND_SIM_SLOT);
                if (mPhoneImei == null || mPhoneImei.length() <= 0) {
                    mPhoneMeid = HwTelephonyManagerInner.getDefault().getMeid();
                    if (mPhoneMeid != null) {
                        mPhoneMeid = mPhoneMeid.substring(mPhoneMeid.length() - 4, mPhoneMeid.length());
                        r3 = Integer.parseInt(mPhoneMeid, 16) % MAX_NUMER;
                        mWatermarkNumber = r3;
                        return r3;
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
            if (isWatermarkEnable()) {
                if (srcbmp == null) {
                    Slog.e(TAG, "addWatermark, srcbmp == null");
                    return null;
                }
                int iWatermarkNumber = watermarkNumber;
                if (watermarkNumber < 0 || watermarkNumber >= MAX_NUMER) {
                    Slog.e(TAG, "addWatermark, iWatermarkNumber invaid");
                    return null;
                }
                Config bmpconfig = srcbmp.getConfig();
                int format = -1;
                if (bmpconfig == Config.RGB_565) {
                    format = 2;
                } else if (bmpconfig == Config.ARGB_8888) {
                    format = 4;
                }
                if (format < 0) {
                    Slog.e(TAG, "addWatermark, format invaid");
                    return null;
                }
                Bitmap copybitmap = srcbmp.copy(Config.ARGB_8888, true);
                if (copybitmap == null) {
                    Slog.e(TAG, "addWatermark, copybitmap fail");
                    return null;
                }
                ByteBuffer mBuffer = ByteBuffer.allocate(copybitmap.getByteCount());
                copybitmap.copyPixelsToBuffer(mBuffer);
                mBuffer.rewind();
                byte[] bytes = mBuffer.array();
                if (addWatermark_native(bytes, srcbmp.getWidth(), srcbmp.getHeight(), 4, watermarkNumber) >= 0) {
                    Bitmap dscbitmap = copybitmap;
                    copybitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
                    if (bmpconfig != copybitmap.getConfig()) {
                        dscbitmap = copybitmap.copy(bmpconfig, true);
                    }
                    return dscbitmap;
                }
                return null;
            } else {
                Slog.e(TAG, "addWatermark, watermark disable");
                return srcbmp;
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
