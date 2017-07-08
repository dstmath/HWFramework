package com.android.server.security.deviceusage;

import android.util.Slog;
import java.lang.reflect.InvocationTargetException;

public class HwOEMInfoAdapter {
    public static final String CHR_ISBNREAD_CLASS = "com.huawei.android.os.HwDeviceInfoCustEx";
    public static final String CHR_OEMINFO_CLASS = "com.huawei.android.os.HwOemInfoCustEx";
    private static final String TAG = "HwOEMInfoAdapter";
    private static Class chrClass;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.deviceusage.HwOEMInfoAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.deviceusage.HwOEMInfoAdapter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.deviceusage.HwOEMInfoAdapter.<clinit>():void");
    }

    public static byte[] getByteArrayFromOeminfo(int type, int sizeOf) {
        Slog.e(TAG, "getByteArrayFromOeminfo has run ");
        try {
            chrClass = Class.forName(CHR_OEMINFO_CLASS);
            return (byte[]) chrClass.getMethod("getByteArrayFromOeminfo", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(type), Integer.valueOf(sizeOf)});
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "Unable to find class com.huawei.android.os.HwOemInfoCustEx");
            return new byte[0];
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "getByteArrayFromOeminfo method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return new byte[0];
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode getByteArrayFromOeminfo");
            return new byte[0];
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode getByteArrayFromOeminfo");
            return new byte[0];
        }
    }

    public static int writeByteArrayToOeminfo(int type, int sizeOf, byte[] mByte) {
        Slog.e(TAG, "writeByteArrayToOeminfo has run ");
        try {
            chrClass = Class.forName(CHR_OEMINFO_CLASS);
            return ((Integer) chrClass.getMethod("writeByteArrayToOeminfo", new Class[]{Integer.TYPE, Integer.TYPE, byte[].class}).invoke(null, new Object[]{Integer.valueOf(type), Integer.valueOf(sizeOf), mByte})).intValue();
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "Unable to find class com.huawei.android.os.HwOemInfoCustEx");
            return 0;
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "writeByteArrayToOeminfo method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return 0;
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode writeByteArrayToOeminfo");
            return 0;
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode writeByteArrayToOeminfo");
            return 0;
        }
    }

    public static String getISBNOrSN(int id) {
        Slog.e(TAG, "getISBNOrSN has run ");
        try {
            chrClass = Class.forName(CHR_ISBNREAD_CLASS);
            return (String) chrClass.getMethod("getISBNOrSN", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(id)});
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "Unable to find class com.huawei.android.os.HwDeviceInfoCustEx");
            return null;
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "getISBNOrSN method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return null;
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode getISBNOrSN");
            return null;
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode getISBNOrSN");
            return null;
        }
    }

    public static int getFastbootStatus() {
        try {
            chrClass = Class.forName(CHR_OEMINFO_CLASS);
            return ((Integer) chrClass.getMethod("getFastbootStatus", new Class[0]).invoke(null, new Object[0])).intValue();
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "getFastbootStatus Unable to find class com.huawei.android.os.HwDeviceInfoCustEx");
            return -1;
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "getFastbootStatus method not found in class com.huawei.android.os.HwOemInfoCustEx");
            return -1;
        } catch (IllegalAccessException e3) {
            Slog.e(TAG, "IllegalAccessException has been thrown while trying to invode getFastbootStatus");
            return -1;
        } catch (InvocationTargetException e4) {
            Slog.e(TAG, "InvocationTargetException has been thrown while trying to invode getFastbootStatus");
            return -1;
        }
    }
}
