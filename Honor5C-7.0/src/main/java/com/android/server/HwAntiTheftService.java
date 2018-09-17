package com.android.server;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import huawei.android.os.IHwAntiTheftManager.Stub;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HwAntiTheftService extends Stub {
    private static final int ASCII_ONE = 49;
    private static final int ASCII_ZERO = 48;
    private static final boolean DEBUG_EXE = false;
    private static final boolean DEBUG_TRANSMISSION = false;
    public static final int DIGEST_SIZE_BYTES = 32;
    private static final int MAX_BYTE_BUFFER_SIZE = 1024;
    private static final int MSG_BOOT_COMPLETED = 1;
    private static final int MSG_CANCEL_NOTIFICATION = 3;
    private static final int MSG_MESSAGE_LOG = 4;
    private static final int MSG_WIPE_ANTITHEFTDATA = 2;
    static final String TAG = "HwAntiTheftService";
    private static final int TYPE_ALL_PARTITION = 3;
    private static final int TYPE_DATA_PARTITION = 1;
    private static final int TYPE_SWITCH_PARTITION = 2;
    private static boolean mIsAntiTheftSupportedState;
    private Context mContext;
    private AntiTheftHandler mHandler;
    private final ServiceThread mHandlerThread;
    private final Object mLock;
    private NotificationManager mNotificationManager;

    private static final class AntiTheftHandler extends Handler {
        public AntiTheftHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwAntiTheftService.MSG_MESSAGE_LOG /*4*/:
                    Slog.d(HwAntiTheftService.TAG, "buffer data:" + ((String) msg.obj));
                default:
                    Slog.d(HwAntiTheftService.TAG, "Receive default");
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwAntiTheftService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwAntiTheftService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwAntiTheftService.<clinit>():void");
    }

    private static native boolean checkBootloaderLock() throws IOException;

    private static native int getDataBlockSize(int i) throws IOException;

    private static native int operateBootloaderLock(boolean z) throws IOException;

    private static native int read(int i, byte[] bArr) throws IOException;

    private static native int wipe(int i) throws IOException;

    private static native int write(int i, byte[] bArr, int i2) throws IOException;

    public HwAntiTheftService(Context context) {
        this.mLock = new Object();
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mHandlerThread = new ServiceThread(TAG, -4, DEBUG_TRANSMISSION);
        this.mHandlerThread.start();
        this.mHandler = new AntiTheftHandler(this.mHandlerThread.getLooper());
    }

    private void printDebugArrayLog(byte[] ViewBuffer, int length) {
        Message m = Message.obtain(this.mHandler, MSG_MESSAGE_LOG);
        m.obj = new String(ViewBuffer, 0, length, Charset.defaultCharset());
        this.mHandler.sendMessage(m);
    }

    public byte[] readAntiTheftData() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call read anti");
            return null;
        }
        int maxByteBufferSize = getDataBlockSizeNative(TYPE_DATA_PARTITION);
        if (maxByteBufferSize == 0) {
            Slog.e(TAG, "get data block size error!");
            return null;
        }
        byte[] readBuffer = new byte[maxByteBufferSize];
        int length = readBufferFromNative(TYPE_DATA_PARTITION, readBuffer);
        byte[] retAarray = new byte[length];
        if (length <= 0) {
            return null;
        }
        System.arraycopy(readBuffer, 0, retAarray, 0, length);
        return retAarray;
    }

    public int writeAntiTheftData(byte[] writeToNative) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call write anti");
            return 0;
        } else if (writeToNative == null) {
            return 0;
        } else {
            if (writeToNative.length == 0 || writeToNative.length > getDataBlockSizeNative(TYPE_DATA_PARTITION)) {
                Slog.e(TAG, "write too long data!!");
                return 0;
            }
            if (writeToNative.length > 0) {
                Slog.d(TAG, "HwAntiTheftService writeAntiTheftData length = " + writeToNative.length);
            } else {
                Slog.d(TAG, "HwAntiTheftService writeAntiTheftData length = " + writeToNative.length);
            }
            return writeBufferToNative(TYPE_DATA_PARTITION, writeToNative, writeToNative.length);
        }
    }

    public int wipeAntiTheftData() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call wipe anti");
            return -1;
        } else if (wipeDataNative(TYPE_ALL_PARTITION) == -1) {
            return -1;
        } else {
            return 0;
        }
    }

    public int getAntiTheftDataBlockSize() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (!ActivityManager.isUserAMonkey()) {
            return getDataBlockSizeNative(TYPE_DATA_PARTITION);
        }
        Slog.d(TAG, "ignoring monkey's attempt to call get anti block size");
        return 0;
    }

    public int setAntiTheftEnabled(boolean enable) {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call set anti enable");
            return -1;
        }
        int retSet = 0;
        try {
            ByteBuffer data = ByteBuffer.allocate(TYPE_DATA_PARTITION);
            data.put(enable ? (byte) 49 : (byte) 48);
            data.flip();
            retSet = writeBufferToNative(TYPE_SWITCH_PARTITION, data.array(), TYPE_DATA_PARTITION);
        } catch (Exception e) {
        }
        if (!enable) {
            wipeDataNative(TYPE_SWITCH_PARTITION);
        }
        operateBootloaderLockNative(enable);
        if (retSet <= 0) {
            i = -1;
        }
        return i;
    }

    public boolean getAntiTheftEnabled() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        boolean enable = DEBUG_TRANSMISSION;
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call get anti enable");
            return DEBUG_TRANSMISSION;
        }
        byte[] readBuffer = new byte[TYPE_DATA_PARTITION];
        if (readBufferFromNative(TYPE_SWITCH_PARTITION, readBuffer) > 0 && readBuffer[0] == ASCII_ONE) {
            enable = true;
        }
        return enable;
    }

    public boolean checkRootState() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        boolean ret = DEBUG_TRANSMISSION;
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call check root state");
            return DEBUG_TRANSMISSION;
        }
        String rootState = SystemProperties.get("huawei.check_root.hotapermit", "safe");
        Slog.d(TAG, "root state :" + rootState);
        if ("safe".equals(rootState)) {
            ret = DEBUG_TRANSMISSION;
        } else if ("risk".equals(rootState)) {
            ret = true;
        }
        return ret;
    }

    public boolean isAntiTheftSupported() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (!ActivityManager.isUserAMonkey()) {
            return mIsAntiTheftSupportedState;
        }
        Slog.d(TAG, "ignoring monkey's attempt to call if support anti theft");
        return DEBUG_TRANSMISSION;
    }

    private int readBufferFromNative(int type, byte[] readData) {
        int ret = 0;
        try {
            synchronized (this.mLock) {
                ret = read(type, readData);
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft read from native failed >>>>>" + e);
        } catch (IOException e2) {
        }
        return ret;
    }

    private int writeBufferToNative(int type, byte[] wrtieData, int length) {
        int ret = 0;
        try {
            synchronized (this.mLock) {
                ret = write(type, wrtieData, length);
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft write to native failed >>>>>" + e);
        } catch (IOException e2) {
        }
        return ret;
    }

    private int wipeDataNative(int type) {
        try {
            int wipe;
            synchronized (this.mLock) {
                wipe = wipe(type);
            }
            return wipe;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft wipe native failed >>>>>" + e);
            return -1;
        } catch (IOException e2) {
            return -1;
        }
    }

    private int getDataBlockSizeNative(int type) {
        int size = 0;
        try {
            synchronized (this.mLock) {
                size = getDataBlockSize(type);
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft get block size native failed >>>>>" + e);
        } catch (IOException e2) {
        }
        return size;
    }

    private boolean checkBootloaderLockNative() {
        try {
            boolean checkBootloaderLock;
            synchronized (this.mLock) {
                checkBootloaderLock = checkBootloaderLock();
            }
            return checkBootloaderLock;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft check boot lock failed >>>>>" + e);
            return DEBUG_TRANSMISSION;
        } catch (IOException e2) {
            return DEBUG_TRANSMISSION;
        }
    }

    private int operateBootloaderLockNative(boolean enable) {
        try {
            int operateBootloaderLock;
            synchronized (this.mLock) {
                operateBootloaderLock = operateBootloaderLock(enable);
            }
            return operateBootloaderLock;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft opt boot lock  failed >>>>>" + e);
            return -1;
        } catch (IOException e2) {
            return -1;
        }
    }
}
