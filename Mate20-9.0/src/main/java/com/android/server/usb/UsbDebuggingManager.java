package com.android.server.usb;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Slog;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.dump.DumpUtils;
import com.android.server.FgThread;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class UsbDebuggingManager {
    private static final String ADBD_SOCKET = "adbd";
    private static final String ADB_DIRECTORY = "misc/adb";
    private static final String ADB_KEYS_FILE = "adb_keys";
    private static final int BUFFER_SIZE = 4096;
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbDebuggingManager";
    /* access modifiers changed from: private */
    public boolean mAdbEnabled = false;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public String mFingerprints;
    /* access modifiers changed from: private */
    public final Handler mHandler = new UsbDebuggingHandler(FgThread.get().getLooper());
    private StatusBarManager mStatusBarManager;
    /* access modifiers changed from: private */
    public UsbDebuggingThread mThread;

    class UsbDebuggingHandler extends Handler {
        private static final int MESSAGE_ADB_ALLOW = 3;
        private static final int MESSAGE_ADB_CLEAR = 6;
        private static final int MESSAGE_ADB_CONFIRM = 5;
        private static final int MESSAGE_ADB_DENY = 4;
        private static final int MESSAGE_ADB_DISABLED = 2;
        private static final int MESSAGE_ADB_ENABLED = 1;

        public UsbDebuggingHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!UsbDebuggingManager.this.mAdbEnabled) {
                        boolean unused = UsbDebuggingManager.this.mAdbEnabled = true;
                        UsbDebuggingThread unused2 = UsbDebuggingManager.this.mThread = new UsbDebuggingThread();
                        UsbDebuggingManager.this.mThread.start();
                        return;
                    }
                    return;
                case 2:
                    if (UsbDebuggingManager.this.mAdbEnabled) {
                        boolean unused3 = UsbDebuggingManager.this.mAdbEnabled = false;
                        if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.stopListening();
                            UsbDebuggingThread unused4 = UsbDebuggingManager.this.mThread = null;
                            return;
                        }
                        return;
                    }
                    return;
                case 3:
                    String key = (String) msg.obj;
                    String fingerprints = UsbDebuggingManager.this.getFingerprints(key);
                    if (!fingerprints.equals(UsbDebuggingManager.this.mFingerprints)) {
                        Slog.e(UsbDebuggingManager.TAG, "Fingerprints do not match. Got " + fingerprints + ", expected " + UsbDebuggingManager.this.mFingerprints);
                        return;
                    }
                    if (msg.arg1 == 1) {
                        UsbDebuggingManager.this.writeKey(key);
                    }
                    if (UsbDebuggingManager.this.mThread != null) {
                        UsbDebuggingManager.this.mThread.sendResponse("OK");
                        return;
                    }
                    return;
                case 4:
                    if (UsbDebuggingManager.this.mThread != null) {
                        UsbDebuggingManager.this.mThread.sendResponse("NO");
                        return;
                    }
                    return;
                case 5:
                    if ("trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
                        Slog.d(UsbDebuggingManager.TAG, "Deferring adb confirmation until after vold decrypt");
                        if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.sendResponse("NO");
                            return;
                        }
                        return;
                    } else if (UsbDebuggingManager.this.mContext == null || UsbDebuggingManager.isDeviceProvisioned(UsbDebuggingManager.this.mContext)) {
                        String key2 = (String) msg.obj;
                        String fingerprints2 = UsbDebuggingManager.this.getFingerprints(key2);
                        if (!BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(fingerprints2)) {
                            String unused5 = UsbDebuggingManager.this.mFingerprints = fingerprints2;
                            UsbDebuggingManager.this.startConfirmation(key2, UsbDebuggingManager.this.mFingerprints);
                            return;
                        } else if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.sendResponse("NO");
                            return;
                        } else {
                            return;
                        }
                    } else {
                        Slog.d(UsbDebuggingManager.TAG, "Deferring adb confirmation until device is provisioned");
                        if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.sendResponse("NO");
                            return;
                        }
                        return;
                    }
                case 6:
                    UsbDebuggingManager.this.deleteKeyFile();
                    return;
                default:
                    return;
            }
        }
    }

    class UsbDebuggingThread extends Thread {
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private LocalSocket mSocket;
        private boolean mStopped;

        UsbDebuggingThread() {
            super(UsbDebuggingManager.TAG);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
            listenToSocket();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0012, code lost:
            android.os.SystemClock.sleep(1000);
         */
        public void run() {
            while (true) {
                synchronized (this) {
                    if (!this.mStopped) {
                        try {
                            openSocketLocked();
                        } catch (Exception e) {
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        private void openSocketLocked() throws IOException {
            try {
                LocalSocketAddress address = new LocalSocketAddress(UsbDebuggingManager.ADBD_SOCKET, LocalSocketAddress.Namespace.RESERVED);
                this.mInputStream = null;
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
            try {
                byte[] buffer = new byte[4096];
                while (true) {
                    int count = this.mInputStream.read(buffer);
                    if (count < 0) {
                        break;
                    } else if (buffer[0] == 80 && buffer[1] == 75) {
                        String key = new String(Arrays.copyOfRange(buffer, 2, count));
                        Slog.d(UsbDebuggingManager.TAG, "Received public key: " + key);
                        Message msg = UsbDebuggingManager.this.mHandler.obtainMessage(5);
                        msg.obj = key;
                        UsbDebuggingManager.this.mHandler.sendMessage(msg);
                    } else {
                        Slog.e(UsbDebuggingManager.TAG, "Wrong message: " + new String(Arrays.copyOfRange(buffer, 0, 2)));
                    }
                }
                synchronized (this) {
                    closeSocketLocked();
                }
            } catch (Throwable th) {
                synchronized (this) {
                    closeSocketLocked();
                    throw th;
                }
            }
        }

        private void closeSocketLocked() {
            try {
                if (this.mOutputStream != null) {
                    this.mOutputStream.close();
                    this.mOutputStream = null;
                }
            } catch (IOException e) {
                Slog.e(UsbDebuggingManager.TAG, "Failed closing output stream: " + e);
            }
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                    this.mSocket = null;
                }
            } catch (IOException ex) {
                Slog.e(UsbDebuggingManager.TAG, "Failed closing socket: " + ex);
            }
        }

        /* access modifiers changed from: package-private */
        public void stopListening() {
            synchronized (this) {
                this.mStopped = true;
                closeSocketLocked();
            }
        }

        /* access modifiers changed from: package-private */
        public void sendResponse(String msg) {
            synchronized (this) {
                if (!this.mStopped && this.mOutputStream != null) {
                    try {
                        this.mOutputStream.write(msg.getBytes());
                    } catch (IOException ex) {
                        Slog.e(UsbDebuggingManager.TAG, "Failed to write response:", ex);
                    }
                }
            }
        }
    }

    public UsbDebuggingManager(Context context) {
        this.mContext = context;
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
    }

    /* access modifiers changed from: private */
    public static boolean isDeviceProvisioned(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1;
    }

    /* access modifiers changed from: private */
    public String getFingerprints(String key) {
        StringBuilder sb = new StringBuilder();
        if (key == null) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        try {
            try {
                byte[] digest = MessageDigest.getInstance("MD5").digest(Base64.decode(key.split("\\s+")[0].getBytes(), 0));
                for (int i = 0; i < digest.length; i++) {
                    sb.append("0123456789ABCDEF".charAt((digest[i] >> 4) & 15));
                    sb.append("0123456789ABCDEF".charAt(digest[i] & UsbDescriptor.DESCRIPTORTYPE_BOS));
                    if (i < digest.length - 1) {
                        sb.append(":");
                    }
                }
                return sb.toString();
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "error doing base64 decoding", e);
                return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
        } catch (Exception ex) {
            Slog.e(TAG, "Error getting digester", ex);
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
    }

    /* access modifiers changed from: private */
    public void startConfirmation(String key, String fingerprints) {
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(ActivityManager.getCurrentUser());
        String componentString = (userInfo.isAdmin() || userInfo.isRepairMode()) ? Resources.getSystem().getString(17039774) : Resources.getSystem().getString(17039775);
        ComponentName componentName = ComponentName.unflattenFromString(componentString);
        if (!startConfirmationActivity(componentName, userInfo.getUserHandle(), key, fingerprints) && !startConfirmationService(componentName, userInfo.getUserHandle(), key, fingerprints)) {
            Slog.e(TAG, "unable to start customAdbPublicKeyConfirmation[SecondaryUser]Component " + componentString + " as an Activity or a Service");
        }
    }

    private boolean startConfirmationActivity(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = createConfirmationIntent(componentName, key, fingerprints);
        intent.addFlags(268435456);
        if (packageManager.resolveActivity(intent, 65536) != null) {
            try {
                if (this.mStatusBarManager != null) {
                    this.mStatusBarManager.collapsePanels();
                }
                this.mContext.startActivityAsUser(intent, userHandle);
                return true;
            } catch (ActivityNotFoundException e) {
                Slog.e(TAG, "unable to start adb whitelist activity: " + componentName, e);
            }
        }
        return false;
    }

    private boolean startConfirmationService(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        try {
            if (this.mContext.startServiceAsUser(createConfirmationIntent(componentName, key, fingerprints), userHandle) != null) {
                return true;
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "unable to start adb whitelist service: " + componentName, e);
        }
        return false;
    }

    private Intent createConfirmationIntent(ComponentName componentName, String key, String fingerprints) {
        Intent intent = new Intent();
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        intent.putExtra("key", key);
        intent.putExtra("fingerprints", fingerprints);
        return intent;
    }

    private File getUserKeyFile() {
        File adbDir = new File(Environment.getDataDirectory(), ADB_DIRECTORY);
        if (adbDir.exists()) {
            return new File(adbDir, ADB_KEYS_FILE);
        }
        Slog.e(TAG, "ADB data directory does not exist");
        return null;
    }

    /* access modifiers changed from: private */
    public void writeKey(String key) {
        try {
            File keyFile = getUserKeyFile();
            if (keyFile != null) {
                if (!keyFile.exists()) {
                    keyFile.createNewFile();
                    FileUtils.setPermissions(keyFile.toString(), 416, -1, -1);
                }
                FileOutputStream fo = new FileOutputStream(keyFile, true);
                fo.write(key.getBytes());
                fo.write(10);
                fo.close();
            }
        } catch (IOException ex) {
            Slog.e(TAG, "Error writing key:" + ex);
        }
    }

    /* access modifiers changed from: private */
    public void deleteKeyFile() {
        File keyFile = getUserKeyFile();
        if (keyFile != null) {
            keyFile.delete();
        }
    }

    public void setAdbEnabled(boolean enabled) {
        int i;
        Handler handler = this.mHandler;
        if (enabled) {
            i = 1;
        } else {
            i = 2;
        }
        handler.sendEmptyMessage(i);
    }

    public void allowUsbDebugging(boolean alwaysAllow, String publicKey) {
        Message msg = this.mHandler.obtainMessage(3);
        msg.arg1 = alwaysAllow;
        msg.obj = publicKey;
        this.mHandler.sendMessage(msg);
    }

    public void denyUsbDebugging() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void clearUsbDebuggingKeys() {
        this.mHandler.sendEmptyMessage(6);
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        dump.write("connected_to_adb", 1133871366145L, this.mThread != null);
        DumpUtils.writeStringIfNotNull(dump, "last_key_received", 1138166333442L, this.mFingerprints);
        try {
            dump.write("user_keys", 1138166333443L, FileUtils.readTextFile(new File("/data/misc/adb/adb_keys"), 0, null));
        } catch (IOException e) {
            Slog.e(TAG, "Cannot read user keys", e);
        }
        try {
            dump.write("system_keys", 1138166333444L, FileUtils.readTextFile(new File("/adb_keys"), 0, null));
        } catch (IOException e2) {
            Slog.e(TAG, "Cannot read system keys", e2);
        }
        dump.end(token);
    }
}
