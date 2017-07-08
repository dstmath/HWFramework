package huawei.android.hwgallerycache;

import android.util.Log;

public class Utils {
    private static final long INITIALCRC = -1;
    private static final long POLY64REV = -7661587058870466123L;
    private static final String TAG = "Utils";
    private static long[] sCrcTable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwgallerycache.Utils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwgallerycache.Utils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwgallerycache.Utils.<clinit>():void");
    }

    public static final long crc64Long(String in) {
        if (in == null || in.length() == 0) {
            return 0;
        }
        return crc64Long(getBytes(in));
    }

    public static final long crc64Long(byte[] buffer) {
        long crc = INITIALCRC;
        for (byte b : buffer) {
            crc = sCrcTable[(((int) crc) ^ b) & PduHeaders.STORE_STATUS_ERROR_END] ^ (crc >> 8);
        }
        return crc;
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[(in.length() * 2)];
        int output = 0;
        for (char ch : in.toCharArray()) {
            int i = output + 1;
            result[output] = (byte) (ch & PduHeaders.STORE_STATUS_ERROR_END);
            output = i + 1;
            result[i] = (byte) (ch >> 8);
        }
        return result;
    }

    public static boolean versionInRange(int checkedVersion, String versionRanage) {
        if (versionRanage == null) {
            return false;
        }
        int i;
        int versionIndex = versionRanage.indexOf(";");
        String versionPreRange;
        if (versionIndex >= 0) {
            versionPreRange = versionRanage.substring(0, versionIndex);
        } else {
            versionPreRange = versionRanage;
        }
        for (String split : versionPreRange.split(",")) {
            String[] VersionStartAndEnd = split.split("-");
            if (VersionStartAndEnd.length >= 2) {
                try {
                    int checkedVersionStart = Integer.parseInt(VersionStartAndEnd[0]);
                    int checkedVersionEnd = Integer.parseInt(VersionStartAndEnd[1]);
                    if (checkedVersion >= checkedVersionStart && checkedVersion <= checkedVersionEnd) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        if (versionIndex >= 0) {
            String[] versionPostArray = versionRanage.substring(versionIndex + 1).split(",");
            int versionPostArrayLen = versionPostArray.length;
            i = 0;
            while (i < versionPostArrayLen) {
                try {
                    if (checkedVersion == Integer.parseInt(versionPostArray[i])) {
                        return true;
                    }
                    i++;
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        return false;
    }
}
