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
import android.net.LocalSocketAddress.Namespace;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Base64;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.FgThread;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class UsbDebuggingManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbDebuggingManager";
    private final String ADBD_SOCKET;
    private final String ADB_DIRECTORY;
    private final String ADB_KEYS_FILE;
    private final int BUFFER_SIZE;
    private boolean mAdbEnabled;
    private final Context mContext;
    private String mFingerprints;
    private final Handler mHandler;
    private StatusBarManager mStatusBarManager;
    private UsbDebuggingThread mThread;

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
            String key;
            String fingerprints;
            switch (msg.what) {
                case MESSAGE_ADB_ENABLED /*1*/:
                    if (!UsbDebuggingManager.this.mAdbEnabled) {
                        UsbDebuggingManager.this.mAdbEnabled = true;
                        UsbDebuggingManager.this.mThread = new UsbDebuggingThread();
                        UsbDebuggingManager.this.mThread.start();
                    }
                case MESSAGE_ADB_DISABLED /*2*/:
                    if (UsbDebuggingManager.this.mAdbEnabled) {
                        UsbDebuggingManager.this.mAdbEnabled = UsbDebuggingManager.DEBUG;
                        if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.stopListening();
                            UsbDebuggingManager.this.mThread = null;
                        }
                    }
                case MESSAGE_ADB_ALLOW /*3*/:
                    key = msg.obj;
                    fingerprints = UsbDebuggingManager.this.getFingerprints(key);
                    if (fingerprints.equals(UsbDebuggingManager.this.mFingerprints)) {
                        if (msg.arg1 == MESSAGE_ADB_ENABLED) {
                            UsbDebuggingManager.this.writeKey(key);
                        }
                        if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.sendResponse("OK");
                            return;
                        }
                        return;
                    }
                    Slog.e(UsbDebuggingManager.TAG, "Fingerprints do not match. Got " + fingerprints + ", expected " + UsbDebuggingManager.this.mFingerprints);
                case MESSAGE_ADB_DENY /*4*/:
                    if (UsbDebuggingManager.this.mThread != null) {
                        UsbDebuggingManager.this.mThread.sendResponse("NO");
                    }
                case MESSAGE_ADB_CONFIRM /*5*/:
                    if ("trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
                        Slog.d(UsbDebuggingManager.TAG, "Deferring adb confirmation until after vold decrypt");
                        if (UsbDebuggingManager.this.mThread != null) {
                            UsbDebuggingManager.this.mThread.sendResponse("NO");
                            return;
                        }
                        return;
                    }
                    key = (String) msg.obj;
                    fingerprints = UsbDebuggingManager.this.getFingerprints(key);
                    if (!"".equals(fingerprints)) {
                        UsbDebuggingManager.this.mFingerprints = fingerprints;
                        UsbDebuggingManager.this.startConfirmation(key, UsbDebuggingManager.this.mFingerprints);
                    } else if (UsbDebuggingManager.this.mThread != null) {
                        UsbDebuggingManager.this.mThread.sendResponse("NO");
                    }
                case MESSAGE_ADB_CLEAR /*6*/:
                    UsbDebuggingManager.this.deleteKeyFile();
                default:
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            while (true) {
                synchronized (this) {
                    if (this.mStopped) {
                        return;
                    }
                    try {
                        openSocketLocked();
                    } catch (Exception e) {
                    }
                    try {
                        listenToSocket();
                    } catch (Exception e2) {
                        SystemClock.sleep(1000);
                    }
                }
            }
        }

        private void openSocketLocked() throws IOException {
            try {
                LocalSocketAddress address = new LocalSocketAddress("adbd", Namespace.RESERVED);
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void listenToSocket() throws IOException {
            byte[] buffer = new byte[DumpState.DUMP_PREFERRED];
            while (true) {
                int count = this.mInputStream.read(buffer);
                if (count < 0) {
                    break;
                }
                try {
                    if (buffer[0] != 80 || buffer[1] != 75) {
                        break;
                    }
                    String key = new String(Arrays.copyOfRange(buffer, 2, count));
                    Slog.d(UsbDebuggingManager.TAG, "Received public key: " + key);
                    Message msg = UsbDebuggingManager.this.mHandler.obtainMessage(5);
                    msg.obj = key;
                    UsbDebuggingManager.this.mHandler.sendMessage(msg);
                } catch (Throwable th) {
                    synchronized (this) {
                    }
                    closeSocketLocked();
                }
            }
            synchronized (this) {
                closeSocketLocked();
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

        void stopListening() {
            synchronized (this) {
                this.mStopped = true;
                closeSocketLocked();
            }
        }

        void sendResponse(String msg) {
            synchronized (this) {
                if (!(this.mStopped || this.mOutputStream == null)) {
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
        this.ADBD_SOCKET = "adbd";
        this.ADB_DIRECTORY = "misc/adb";
        this.ADB_KEYS_FILE = "adb_keys";
        this.BUFFER_SIZE = DumpState.DUMP_PREFERRED;
        this.mAdbEnabled = DEBUG;
        this.mHandler = new UsbDebuggingHandler(FgThread.get().getLooper());
        this.mContext = context;
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
    }

    private String getFingerprints(String key) {
        String hex = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder();
        if (key == null) {
            return "";
        }
        try {
            try {
                byte[] digest = MessageDigest.getInstance("MD5").digest(Base64.decode(key.split("\\s+")[0].getBytes(), 0));
                for (int i = 0; i < digest.length; i++) {
                    sb.append(hex.charAt((digest[i] >> 4) & 15));
                    sb.append(hex.charAt(digest[i] & 15));
                    if (i < digest.length - 1) {
                        sb.append(":");
                    }
                }
                return sb.toString();
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "error doing base64 decoding", e);
                return "";
            }
        } catch (Exception ex) {
            Slog.e(TAG, "Error getting digester", ex);
            return "";
        }
    }

    private void startConfirmation(String key, String fingerprints) {
        String componentString;
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(ActivityManager.getCurrentUser());
        if (userInfo.isAdmin()) {
            componentString = Resources.getSystem().getString(17039456);
        } else {
            componentString = Resources.getSystem().getString(17039457);
        }
        ComponentName componentName = ComponentName.unflattenFromString(componentString);
        if (!startConfirmationActivity(componentName, userInfo.getUserHandle(), key, fingerprints) && !startConfirmationService(componentName, userInfo.getUserHandle(), key, fingerprints)) {
            Slog.e(TAG, "unable to start customAdbPublicKeyConfirmation[SecondaryUser]Component " + componentString + " as an Activity or a Service");
        }
    }

    private boolean startConfirmationActivity(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = createConfirmationIntent(componentName, key, fingerprints);
        intent.addFlags(268435456);
        if (packageManager.resolveActivity(intent, DumpState.DUMP_INSTALLS) != null) {
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
        return DEBUG;
    }

    private boolean startConfirmationService(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        try {
            if (this.mContext.startServiceAsUser(createConfirmationIntent(componentName, key, fingerprints), userHandle) != null) {
                return true;
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "unable to start adb whitelist service: " + componentName, e);
        }
        return DEBUG;
    }

    private Intent createConfirmationIntent(ComponentName componentName, String key, String fingerprints) {
        Intent intent = new Intent();
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        intent.putExtra("key", key);
        intent.putExtra("fingerprints", fingerprints);
        return intent;
    }

    private File getUserKeyFile() {
        File adbDir = new File(Environment.getDataDirectory(), "misc/adb");
        if (adbDir.exists()) {
            return new File(adbDir, "adb_keys");
        }
        Slog.e(TAG, "ADB data directory does not exist");
        return null;
    }

    private void writeKey(String key) {
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

    private void deleteKeyFile() {
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
        msg.arg1 = alwaysAllow ? 1 : 0;
        msg.obj = publicKey;
        this.mHandler.sendMessage(msg);
    }

    public void denyUsbDebugging() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void clearUsbDebuggingKeys() {
        this.mHandler.sendEmptyMessage(6);
    }

    public void dump(IndentingPrintWriter pw) {
        boolean z = DEBUG;
        pw.println("USB Debugging State:");
        StringBuilder append = new StringBuilder().append("  Connected to adbd: ");
        if (this.mThread != null) {
            z = true;
        }
        pw.println(append.append(z).toString());
        pw.println("  Last key received: " + this.mFingerprints);
        pw.println("  User keys:");
        try {
            pw.println(FileUtils.readTextFile(new File("/data/misc/adb/adb_keys"), 0, null));
        } catch (IOException e) {
            pw.println("IOException: " + e);
        }
        pw.println("  System keys:");
        try {
            pw.println(FileUtils.readTextFile(new File("/adb_keys"), 0, null));
        } catch (IOException e2) {
            pw.println("IOException: " + e2);
        }
    }
}
