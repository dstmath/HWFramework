package com.android.server.usb;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.dump.DumpUtils;
import com.android.server.FgThread;
import com.android.server.display.HwUibcReceiver;
import com.huawei.android.app.PackageManagerEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;

public class HwUsbHDBManagerImpl {
    private static final int BUFFER_SIZE = 4096;
    private static final boolean DEBUG = false;
    private static final String HDBD_SOCKET = "hdbd";
    private static final String HDB_DIRECTORY = "misc/adb";
    private static final String HDB_KEYS_FILE = "hdb_keys";
    private static final int HDB_KEY_BIT = 3;
    private static final int PK_KEY_BIT = 2;
    private static final int REPAIR_MODE_USER_ID = 127;
    private static final String TAG = "HwUsbHDBManager";
    private static volatile HwUsbHDBManagerImpl sHdbInStance = null;
    private final Context mContext;
    private String mFingerprints;
    private final Handler mHandler = new UsbDebuggingHandler(FgThread.get().getLooper());
    private boolean mIsHdbEnabled = false;
    private StatusBarManager mStatusBarManager;
    private UsbDebuggingThread mThread;

    private HwUsbHDBManagerImpl(Context context) {
        boolean isHdbEnabled = false;
        this.mContext = context;
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        isHdbEnabled = Settings.System.getInt(this.mContext.getContentResolver(), "hdb_enabled", 0) > 0 ? true : isHdbEnabled;
        Slog.i(TAG, "isHdbEnabled in constructor: " + isHdbEnabled);
        setHdbEnabled(isHdbEnabled);
    }

    static synchronized HwUsbHDBManagerImpl getInstance(Context context) {
        HwUsbHDBManagerImpl hwUsbHDBManagerImpl;
        synchronized (HwUsbHDBManagerImpl.class) {
            if (sHdbInStance == null) {
                sHdbInStance = new HwUsbHDBManagerImpl(context);
            }
            hwUsbHDBManagerImpl = sHdbInStance;
        }
        return hwUsbHDBManagerImpl;
    }

    /* access modifiers changed from: package-private */
    public class UsbDebuggingThread extends Thread {
        private InputStream mInputStream;
        private boolean mIsStopped;
        private OutputStream mOutputStream;
        private LocalSocket mSocket;

        UsbDebuggingThread() {
            super(HwUsbHDBManagerImpl.TAG);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                synchronized (this) {
                    if (!this.mIsStopped) {
                        try {
                            openSocketLocked();
                        } catch (Exception e) {
                        }
                    } else {
                        return;
                    }
                }
                try {
                    listenToSocket();
                } catch (Exception e2) {
                    SystemClock.sleep(1000);
                }
            }
        }

        private void openSocketLocked() throws IOException {
            try {
                this.mInputStream = null;
                LocalSocketAddress address = new LocalSocketAddress(HwUsbHDBManagerImpl.HDBD_SOCKET, LocalSocketAddress.Namespace.RESERVED);
                this.mSocket = new LocalSocket();
                this.mSocket.connect(address);
                this.mOutputStream = this.mSocket.getOutputStream();
                this.mInputStream = this.mSocket.getInputStream();
            } catch (IOException ioe) {
                closeSocketLocked();
                throw ioe;
            }
        }

        private void listenToSocket() throws IOException {
            byte[] buffer;
            try {
                buffer = new byte[4096];
                Slog.e(HwUsbHDBManagerImpl.TAG, "Wrong message: " + new String(Arrays.copyOfRange(buffer, 0, 2), Charset.defaultCharset()));
                synchronized (this) {
                    closeSocketLocked();
                }
                return;
            } catch (Throwable th) {
                synchronized (this) {
                    closeSocketLocked();
                    throw th;
                }
            }
            while (true) {
                int count = this.mInputStream.read(buffer);
                if (count < 0) {
                    Slog.e(HwUsbHDBManagerImpl.TAG, "Wrong message: count=" + count);
                    break;
                } else if (buffer[0] == 80 && buffer[1] == 75) {
                    sendConfirmMessage(new String(Arrays.copyOfRange(buffer, 2, count), Charset.defaultCharset()));
                } else if (buffer[0] != 72 || buffer[1] != 68 || buffer[2] != 66) {
                    break;
                } else {
                    String key = new String(Arrays.copyOfRange(buffer, 3, count), Charset.defaultCharset());
                    Slog.d(HwUsbHDBManagerImpl.TAG, "Received HDB KEY!");
                    PackageManagerEx.setHdbKey(key);
                }
            }
        }

        private void sendConfirmMessage(String key) {
            Slog.d(HwUsbHDBManagerImpl.TAG, "Received public key: " + key);
            Message msg = HwUsbHDBManagerImpl.this.mHandler.obtainMessage(5);
            msg.obj = key;
            HwUsbHDBManagerImpl.this.mHandler.sendMessage(msg);
        }

        private void closeSocketLocked() {
            try {
                if (this.mOutputStream != null) {
                    this.mOutputStream.close();
                    this.mOutputStream = null;
                }
            } catch (IOException e) {
                Slog.e(HwUsbHDBManagerImpl.TAG, "Failed closing output stream: " + e);
            }
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                    this.mSocket = null;
                }
            } catch (IOException ex) {
                Slog.e(HwUsbHDBManagerImpl.TAG, "Failed closing socket: " + ex);
            }
        }

        /* access modifiers changed from: package-private */
        public void stopListening() {
            synchronized (this) {
                this.mIsStopped = true;
                closeSocketLocked();
            }
        }

        /* access modifiers changed from: package-private */
        public void sendResponse(String msg) {
            synchronized (this) {
                if (!this.mIsStopped && this.mOutputStream != null) {
                    try {
                        this.mOutputStream.write(msg.getBytes(Charset.defaultCharset()));
                    } catch (IOException ex) {
                        Slog.e(HwUsbHDBManagerImpl.TAG, "Failed to write response:", ex);
                    }
                }
            }
        }
    }

    class UsbDebuggingHandler extends Handler {
        private static final int MESSAGE_HDB_ALLOW = 3;
        private static final int MESSAGE_HDB_CLEAR = 6;
        private static final int MESSAGE_HDB_CONFIRM = 5;
        private static final int MESSAGE_HDB_DENY = 4;
        private static final int MESSAGE_HDB_DISABLED = 2;
        private static final int MESSAGE_HDB_ENABLED = 1;

        UsbDebuggingHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!HwUsbHDBManagerImpl.this.mIsHdbEnabled) {
                        HwUsbHDBManagerImpl.this.mIsHdbEnabled = true;
                        HwUsbHDBManagerImpl hwUsbHDBManagerImpl = HwUsbHDBManagerImpl.this;
                        hwUsbHDBManagerImpl.mThread = new UsbDebuggingThread();
                        HwUsbHDBManagerImpl.this.mThread.start();
                        return;
                    }
                    return;
                case 2:
                    if (HwUsbHDBManagerImpl.this.mIsHdbEnabled) {
                        HwUsbHDBManagerImpl.this.mIsHdbEnabled = false;
                        if (HwUsbHDBManagerImpl.this.mThread != null) {
                            HwUsbHDBManagerImpl.this.mThread.stopListening();
                            HwUsbHDBManagerImpl.this.mThread = null;
                            return;
                        }
                        return;
                    }
                    return;
                case 3:
                    HwUsbHDBManagerImpl.this.handleMsgHdbAllow(msg);
                    return;
                case 4:
                    if (HwUsbHDBManagerImpl.this.mThread != null) {
                        HwUsbHDBManagerImpl.this.mThread.sendResponse("NO");
                        return;
                    }
                    return;
                case 5:
                    HwUsbHDBManagerImpl.this.handleHdbConfirm(msg);
                    return;
                case 6:
                    HwUsbHDBManagerImpl.this.deleteKeyFile();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMsgHdbAllow(Message msg) {
        String key = (String) msg.obj;
        String fingerprints = getFingerprints(key);
        if (!fingerprints.equals(this.mFingerprints)) {
            Slog.e(TAG, "Fingerprints do not match. Got " + fingerprints + ", expected " + this.mFingerprints);
            return;
        }
        if (msg.arg1 == 1) {
            writeKey(key);
        }
        UsbDebuggingThread usbDebuggingThread = this.mThread;
        if (usbDebuggingThread != null) {
            usbDebuggingThread.sendResponse("OK");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHdbConfirm(Message msg) {
        if ("trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
            Slog.d(TAG, "Deferring hdb confirmation until after vold decrypt");
            UsbDebuggingThread usbDebuggingThread = this.mThread;
            if (usbDebuggingThread != null) {
                usbDebuggingThread.sendResponse("NO");
            }
        } else if (!isDeviceProvisioned(this.mContext)) {
            Slog.d(TAG, "Deferring hdb confirmation until device is provisioned");
            UsbDebuggingThread usbDebuggingThread2 = this.mThread;
            if (usbDebuggingThread2 != null) {
                usbDebuggingThread2.sendResponse("NO");
            }
        } else {
            String key = (String) msg.obj;
            String fingerprints = getFingerprints(key);
            if ("".equals(fingerprints)) {
                UsbDebuggingThread usbDebuggingThread3 = this.mThread;
                if (usbDebuggingThread3 != null) {
                    usbDebuggingThread3.sendResponse("NO");
                    return;
                }
                return;
            }
            this.mFingerprints = fingerprints;
            startConfirmation(key, this.mFingerprints);
        }
    }

    private String getFingerprints(String key) {
        StringBuilder sb = new StringBuilder();
        if (key == null) {
            return "";
        }
        try {
            try {
                byte[] digest = MessageDigest.getInstance("MD5").digest(Base64.decode(key.split("\\s+")[0].getBytes(Charset.defaultCharset()), 0));
                for (int i = 0; i < digest.length; i++) {
                    sb.append("0123456789ABCDEF".charAt((digest[i] >> 4) & 15));
                    sb.append("0123456789ABCDEF".charAt(digest[i] & HwUibcReceiver.CurrentPacket.INPUT_MASK));
                    if (i < digest.length - 1) {
                        sb.append(AwarenessInnerConstants.COLON_KEY);
                    }
                }
                return sb.toString();
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "error doing base64 decoding", e);
                return "";
            }
        } catch (NoSuchAlgorithmException e2) {
            Slog.e(TAG, "Error getting digester");
            return "";
        }
    }

    private void startConfirmation(String key, String fingerprints) {
        String componentString;
        int currentUserId = ActivityManager.getCurrentUser();
        UserHandle userHandle = UserManager.get(this.mContext).getUserInfo(currentUserId).getUserHandle();
        if (currentUserId == 0 || currentUserId == 127) {
            componentString = "com.android.systemui/com.android.systemui.usb.HwUsbDebuggingActivity";
        } else {
            componentString = "com.android.systemui/com.android.systemui.usb.UsbDebuggingSecondaryUserActivity";
        }
        ComponentName componentName = ComponentName.unflattenFromString(componentString);
        if (componentName == null) {
            Slog.e(TAG, "startConfirmation componentName is null");
            return;
        }
        Slog.d(TAG, "startConfirmation componentName=" + componentName);
        if (!startConfirmationActivity(componentName, userHandle, key, fingerprints) && !startConfirmationService(componentName, userHandle, key, fingerprints)) {
            Slog.e(TAG, "unable to start customHdbPublicKeyConfirmation[SecondaryUser]Component " + componentString + " as an Activity or a Service");
        }
    }

    private boolean startConfirmationActivity(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = createConfirmationIntent(componentName, key, fingerprints);
        intent.addFlags(268435456);
        if (packageManager.resolveActivity(intent, 65536) == null) {
            return false;
        }
        try {
            if (this.mStatusBarManager != null) {
                this.mStatusBarManager.collapsePanels();
            }
            this.mContext.startActivityAsUser(intent, userHandle);
            return true;
        } catch (ActivityNotFoundException e) {
            Slog.e(TAG, "unable to start hdb whitelist activity: " + componentName, e);
            return false;
        }
    }

    private boolean startConfirmationService(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        try {
            if (this.mContext.startServiceAsUser(createConfirmationIntent(componentName, key, fingerprints), userHandle) != null) {
                return true;
            }
            return false;
        } catch (SecurityException e) {
            Slog.e(TAG, "unable to start hdb whitelist service: " + componentName, e);
            return false;
        }
    }

    private Intent createConfirmationIntent(ComponentName componentName, String key, String fingerprints) {
        Intent intent = new Intent();
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        intent.putExtra("key", key);
        intent.putExtra("fingerprints", fingerprints);
        intent.putExtra("hdb", "hdb");
        return intent;
    }

    private File getUserKeyFile() {
        File hdbDir = new File(Environment.getDataDirectory(), HDB_DIRECTORY);
        if (hdbDir.exists()) {
            return new File(hdbDir, HDB_KEYS_FILE);
        }
        Slog.e(TAG, "HDB data directory does not exist");
        return null;
    }

    private void writeKey(String key) {
        FileOutputStream fo = null;
        try {
            File keyFile = getUserKeyFile();
            if (keyFile != null) {
                if (!keyFile.exists()) {
                    boolean isSuccess = keyFile.createNewFile();
                    Slog.i(TAG, "writeKey createNewFile success ? " + isSuccess);
                    FileUtils.setPermissions(keyFile.toString(), 416, -1, -1);
                }
                FileOutputStream fo2 = new FileOutputStream(keyFile, true);
                fo2.write(key.getBytes(Charset.defaultCharset()));
                fo2.write(10);
                try {
                    fo2.close();
                } catch (IOException e) {
                    Slog.e(TAG, "close fo failed");
                }
            } else if (0 != 0) {
                try {
                    fo.close();
                } catch (IOException e2) {
                    Slog.e(TAG, "close fo failed");
                }
            }
        } catch (IOException ex) {
            Slog.e(TAG, "Error writing key:" + ex);
            if (0 != 0) {
                fo.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fo.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "close fo failed");
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteKeyFile() {
        File keyFile = getUserKeyFile();
        if (keyFile != null) {
            boolean isSuccess = keyFile.delete();
            Slog.i(TAG, "deleteKeyFile success ? " + isSuccess);
        }
    }

    public final void setHdbEnabled(boolean isEnabled) {
        this.mHandler.sendEmptyMessage(isEnabled ? 1 : 2);
    }

    public void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        Message msg = this.mHandler.obtainMessage(3);
        msg.arg1 = alwaysAllow ? 1 : 0;
        msg.obj = publicKey;
        this.mHandler.sendMessage(msg);
    }

    public void denyUsbHDB() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void clearUsbHDBKeys() {
        this.mHandler.sendEmptyMessage(6);
    }

    private static boolean isDeviceProvisioned(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1;
    }

    public void dumpHdb(DualDumpOutputStream dump, String idName, long id) {
        dump.write("connected_to_hdb", 1133871366149L, true);
        DumpUtils.writeStringIfNotNull(dump, "last_hdb_key_received", 1138166333446L, this.mFingerprints);
        try {
            dump.write("user_hdb_keys", 1138166333447L, FileUtils.readTextFile(new File("/data/misc/adb/hdb_keys"), 0, null));
        } catch (IOException e) {
            Slog.e(TAG, "Cannot read user keys", e);
        }
        try {
            dump.write("system_hdb_keys", 1138166333448L, FileUtils.readTextFile(new File("/hdb_keys"), 0, null));
        } catch (IOException e2) {
            Slog.e(TAG, "Cannot read system hdb keys", e2);
        }
        dump.end(dump.start(idName, id));
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        DualDumpOutputStream dump;
        if (com.android.internal.util.DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
            ArraySet<String> argsSet = new ArraySet<>();
            Collections.addAll(argsSet, args);
            boolean isDumpAsProto = false;
            if (argsSet.contains("--proto")) {
                isDumpAsProto = true;
            }
            if (args == null || args.length == 0 || args[0].equals("-a") || isDumpAsProto) {
                if (isDumpAsProto) {
                    dump = new DualDumpOutputStream(new ProtoOutputStream(fd));
                } else {
                    dump = new DualDumpOutputStream(new IndentingPrintWriter(pw, "  "));
                }
                dumpHdb(dump, "debugging_manager", 1146756268034L);
            }
        }
    }
}
