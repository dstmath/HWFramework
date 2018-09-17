package com.android.server.usb;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.utils.ByteUtil;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class HwUsbHDBManagerImpl implements HwUsbHDBManager {
    private static final int BUFFER_SIZE = 4096;
    private static final boolean DEBUG = false;
    private static final String HDBD_SOCKET = "hdbd";
    private static final String HDB_DIRECTORY = "misc/adb";
    private static final String HDB_KEYS_FILE = "hdb_keys";
    private static final String TAG = "HwUsbHDBManager";
    private static volatile HwUsbHDBManagerImpl mInstance;
    private static final Object mLock;
    private final Context mContext;
    private String mFingerprints;
    private final Handler mHandler;
    private boolean mHdbEnabled;
    private StatusBarManager mStatusBarManager;
    private UsbDebuggingThread mThread;

    class UsbDebuggingHandler extends Handler {
        private static final int MESSAGE_HDB_ALLOW = 3;
        private static final int MESSAGE_HDB_CLEAR = 6;
        private static final int MESSAGE_HDB_CONFIRM = 5;
        private static final int MESSAGE_HDB_DENY = 4;
        private static final int MESSAGE_HDB_DISABLED = 2;
        private static final int MESSAGE_HDB_ENABLED = 1;

        public UsbDebuggingHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String key;
            String fingerprints;
            switch (msg.what) {
                case MESSAGE_HDB_ENABLED /*1*/:
                    if (!HwUsbHDBManagerImpl.this.mHdbEnabled) {
                        HwUsbHDBManagerImpl.this.mHdbEnabled = true;
                        HwUsbHDBManagerImpl.this.mThread = new UsbDebuggingThread();
                        HwUsbHDBManagerImpl.this.mThread.start();
                    }
                case MESSAGE_HDB_DISABLED /*2*/:
                    if (HwUsbHDBManagerImpl.this.mHdbEnabled) {
                        HwUsbHDBManagerImpl.this.mHdbEnabled = HwUsbHDBManagerImpl.DEBUG;
                        if (HwUsbHDBManagerImpl.this.mThread != null) {
                            HwUsbHDBManagerImpl.this.mThread.stopListening();
                            HwUsbHDBManagerImpl.this.mThread = null;
                        }
                    }
                case MESSAGE_HDB_ALLOW /*3*/:
                    key = msg.obj;
                    fingerprints = HwUsbHDBManagerImpl.this.getFingerprints(key);
                    if (fingerprints.equals(HwUsbHDBManagerImpl.this.mFingerprints)) {
                        if (msg.arg1 == MESSAGE_HDB_ENABLED) {
                            HwUsbHDBManagerImpl.this.writeKey(key);
                        }
                        if (HwUsbHDBManagerImpl.this.mThread != null) {
                            HwUsbHDBManagerImpl.this.mThread.sendResponse("OK");
                            return;
                        }
                        return;
                    }
                    Slog.e(HwUsbHDBManagerImpl.TAG, "Fingerprints do not match. Got " + fingerprints + ", expected " + HwUsbHDBManagerImpl.this.mFingerprints);
                case MESSAGE_HDB_DENY /*4*/:
                    if (HwUsbHDBManagerImpl.this.mThread != null) {
                        HwUsbHDBManagerImpl.this.mThread.sendResponse("NO");
                    }
                case MESSAGE_HDB_CONFIRM /*5*/:
                    if ("trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
                        Slog.d(HwUsbHDBManagerImpl.TAG, "Deferring hdb confirmation until after vold decrypt");
                        if (HwUsbHDBManagerImpl.this.mThread != null) {
                            HwUsbHDBManagerImpl.this.mThread.sendResponse("NO");
                            return;
                        }
                        return;
                    }
                    key = (String) msg.obj;
                    fingerprints = HwUsbHDBManagerImpl.this.getFingerprints(key);
                    if (!AppHibernateCst.INVALID_PKG.equals(fingerprints)) {
                        HwUsbHDBManagerImpl.this.mFingerprints = fingerprints;
                        HwUsbHDBManagerImpl.this.startConfirmation(key, HwUsbHDBManagerImpl.this.mFingerprints);
                    } else if (HwUsbHDBManagerImpl.this.mThread != null) {
                        HwUsbHDBManagerImpl.this.mThread.sendResponse("NO");
                    }
                case MESSAGE_HDB_CLEAR /*6*/:
                    HwUsbHDBManagerImpl.this.deleteKeyFile();
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
            super(HwUsbHDBManagerImpl.TAG);
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
                LocalSocketAddress address = new LocalSocketAddress(HwUsbHDBManagerImpl.HDBD_SOCKET, Namespace.RESERVED);
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
            byte[] buffer = new byte[HwUsbHDBManagerImpl.BUFFER_SIZE];
            while (true) {
                int count = this.mInputStream.read(buffer);
                if (count < 0) {
                    break;
                }
                try {
                    if (buffer[0] != 80 || buffer[1] != 75) {
                        break;
                    }
                    String key = new String(Arrays.copyOfRange(buffer, 2, count), StandardCharsets.UTF_8);
                    Slog.d(HwUsbHDBManagerImpl.TAG, "Received public key: " + key);
                    Message msg = HwUsbHDBManagerImpl.this.mHandler.obtainMessage(5);
                    msg.obj = key;
                    HwUsbHDBManagerImpl.this.mHandler.sendMessage(msg);
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
                        this.mOutputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException ex) {
                        Slog.e(HwUsbHDBManagerImpl.TAG, "Failed to write response:", ex);
                    }
                }
            }
        }
    }

    static {
        mInstance = null;
        mLock = new Object();
    }

    public static HwUsbHDBManagerImpl getInstance(Context context) {
        HwUsbHDBManagerImpl hwUsbHDBManagerImpl;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwUsbHDBManagerImpl(context);
            }
            hwUsbHDBManagerImpl = mInstance;
        }
        return hwUsbHDBManagerImpl;
    }

    public HwUsbHDBManagerImpl(Context context) {
        this.mHdbEnabled = DEBUG;
        this.mHandler = new UsbDebuggingHandler(FgThread.get().getLooper());
        this.mContext = context;
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
    }

    public static boolean isCreated() {
        boolean z;
        synchronized (mLock) {
            z = mInstance != null ? true : DEBUG;
        }
        return z;
    }

    private String getFingerprints(String key) {
        String hex = ByteUtil.HEX_TABLE;
        StringBuilder sb = new StringBuilder();
        if (key == null) {
            return AppHibernateCst.INVALID_PKG;
        }
        try {
            try {
                byte[] digest = MessageDigest.getInstance("MD5").digest(Base64.decode(key.split("\\s+")[0].getBytes(StandardCharsets.UTF_8), 0));
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
                return AppHibernateCst.INVALID_PKG;
            }
        } catch (Exception ex) {
            Slog.e(TAG, "Error getting digester", ex);
            return AppHibernateCst.INVALID_PKG;
        }
    }

    private void startConfirmation(String key, String fingerprints) {
        String componentString;
        int currentUserId = ActivityManager.getCurrentUser();
        UserHandle userHandle = UserManager.get(this.mContext).getUserInfo(currentUserId).getUserHandle();
        if (currentUserId == 0) {
            componentString = Resources.getSystem().getString(17039456);
        } else {
            componentString = Resources.getSystem().getString(17039457);
        }
        ComponentName componentName = ComponentName.unflattenFromString(componentString);
        if (!startConfirmationActivity(componentName, userHandle, key, fingerprints) && !startConfirmationService(componentName, userHandle, key, fingerprints)) {
            Slog.e(TAG, "unable to start customHdbPublicKeyConfirmation[SecondaryUser]Component " + componentString + " as an Activity or a Service");
        }
    }

    private boolean startConfirmationActivity(ComponentName componentName, UserHandle userHandle, String key, String fingerprints) {
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = createConfirmationIntent(componentName, key, fingerprints);
        intent.addFlags(268435456);
        if (packageManager.resolveActivity(intent, HwGlobalActionsData.FLAG_REBOOT) != null) {
            try {
                if (this.mStatusBarManager != null) {
                    this.mStatusBarManager.collapsePanels();
                }
                this.mContext.startActivityAsUser(intent, userHandle);
                return true;
            } catch (ActivityNotFoundException e) {
                Slog.e(TAG, "unable to start hdb whitelist activity: " + componentName, e);
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
            Slog.e(TAG, "unable to start hdb whitelist service: " + componentName, e);
        }
        return DEBUG;
    }

    private Intent createConfirmationIntent(ComponentName componentName, String key, String fingerprints) {
        Intent intent = new Intent();
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        intent.putExtra(DevicePublicKeyLoader.KEY, key);
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
        IOException ex;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        try {
            File keyFile = getUserKeyFile();
            if (keyFile != null) {
                if (!keyFile.exists()) {
                    keyFile.createNewFile();
                    FileUtils.setPermissions(keyFile.toString(), 416, -1, -1);
                }
                FileOutputStream fo = new FileOutputStream(keyFile, true);
                try {
                    fo.write(key.getBytes(StandardCharsets.UTF_8));
                    fo.write(10);
                    if (fo != null) {
                        try {
                            fo.close();
                        } catch (IOException e) {
                        }
                    }
                    fileOutputStream = fo;
                } catch (IOException e2) {
                    ex = e2;
                    fileOutputStream = fo;
                    try {
                        Slog.e(TAG, "Error writing key:" + ex);
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e3) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fo;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            }
        } catch (IOException e5) {
            ex = e5;
            Slog.e(TAG, "Error writing key:" + ex);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    private void deleteKeyFile() {
        File keyFile = getUserKeyFile();
        if (keyFile != null) {
            keyFile.delete();
        }
    }

    public void setHdbEnabled(boolean enabled) {
        int i;
        Handler handler = this.mHandler;
        if (enabled) {
            i = 1;
        } else {
            i = 2;
        }
        handler.sendEmptyMessage(i);
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

    public void dump(IndentingPrintWriter pw) {
        boolean z = DEBUG;
        pw.println("  USB HDB Debugging State:");
        StringBuilder append = new StringBuilder().append("    Connected to hdbd: ");
        if (this.mThread != null) {
            z = true;
        }
        pw.println(append.append(z).toString());
        pw.println("    Last key received: " + this.mFingerprints);
        pw.println("    User keys:");
        try {
            pw.println(FileUtils.readTextFile(new File("/data/misc/adb/hdb_keys"), 0, null));
        } catch (IOException e) {
            pw.println("IOException: " + e);
        }
        pw.println("  System keys:");
        try {
            pw.println(FileUtils.readTextFile(new File("/hdb_keys"), 0, null));
        } catch (IOException e2) {
            pw.println("IOException: " + e2);
        }
    }
}
