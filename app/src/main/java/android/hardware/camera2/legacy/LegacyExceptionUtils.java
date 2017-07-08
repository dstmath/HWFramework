package android.hardware.camera2.legacy;

import android.os.ServiceSpecificException;
import android.system.OsConstants;
import android.util.AndroidException;

public class LegacyExceptionUtils {
    public static final int ALREADY_EXISTS = 0;
    public static final int BAD_VALUE = 0;
    public static final int DEAD_OBJECT = 0;
    public static final int INVALID_OPERATION = 0;
    public static final int NO_ERROR = 0;
    public static final int PERMISSION_DENIED = 0;
    private static final String TAG = "LegacyExceptionUtils";
    public static final int TIMED_OUT = 0;

    public static class BufferQueueAbandonedException extends AndroidException {
        public BufferQueueAbandonedException(String name) {
            super(name);
        }

        public BufferQueueAbandonedException(String name, Throwable cause) {
            super(name, cause);
        }

        public BufferQueueAbandonedException(Exception cause) {
            super(cause);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.legacy.LegacyExceptionUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.legacy.LegacyExceptionUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyExceptionUtils.<clinit>():void");
    }

    public static int throwOnError(int errorFlag) throws BufferQueueAbandonedException {
        if (errorFlag == 0) {
            return PERMISSION_DENIED;
        }
        if (errorFlag == (-OsConstants.ENODEV)) {
            throw new BufferQueueAbandonedException();
        } else if (errorFlag >= 0) {
            return errorFlag;
        } else {
            throw new UnsupportedOperationException("Unknown error " + errorFlag);
        }
    }

    public static void throwOnServiceError(int errorFlag) {
        if (errorFlag < 0) {
            int errorCode;
            String errorMsg;
            if (errorFlag == PERMISSION_DENIED) {
                errorCode = 1;
                errorMsg = "Lacking privileges to access camera service";
            } else if (errorFlag != ALREADY_EXISTS) {
                if (errorFlag == BAD_VALUE) {
                    errorCode = 3;
                    errorMsg = "Bad argument passed to camera service";
                } else if (errorFlag == DEAD_OBJECT) {
                    errorCode = 4;
                    errorMsg = "Camera service not available";
                } else if (errorFlag == TIMED_OUT) {
                    errorCode = 10;
                    errorMsg = "Operation timed out in camera service";
                } else if (errorFlag == (-OsConstants.EACCES)) {
                    errorCode = 6;
                    errorMsg = "Camera disabled by policy";
                } else if (errorFlag == (-OsConstants.EBUSY)) {
                    errorCode = 7;
                    errorMsg = "Camera already in use";
                } else if (errorFlag == (-OsConstants.EUSERS)) {
                    errorCode = 8;
                    errorMsg = "Maximum number of cameras in use";
                } else if (errorFlag == (-OsConstants.ENODEV)) {
                    errorCode = 4;
                    errorMsg = "Camera device not available";
                } else if (errorFlag == (-OsConstants.EOPNOTSUPP)) {
                    errorCode = 9;
                    errorMsg = "Deprecated camera HAL does not support this";
                } else if (errorFlag == INVALID_OPERATION) {
                    errorCode = 10;
                    errorMsg = "Illegal state encountered in camera service.";
                } else {
                    errorCode = 10;
                    errorMsg = "Unknown camera device error " + errorFlag;
                }
            } else {
                return;
            }
            throw new ServiceSpecificException(errorCode, errorMsg);
        }
    }

    private LegacyExceptionUtils() {
        throw new AssertionError();
    }
}
