package com.android.server.adb;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
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
import android.util.AtomicFile;
import android.util.Base64;
import android.util.Slog;
import android.util.StatsLog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.dump.DumpUtils;
import com.android.server.FgThread;
import com.android.server.usage.UnixCalendar;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AdbDebuggingManager {
    private static final String ADBD_SOCKET = "adbd";
    private static final String ADB_DIRECTORY = "misc/adb";
    private static final String ADB_KEYS_FILE = "adb_keys";
    private static final String ADB_TEMP_KEYS_FILE = "adb_temp_keys.xml";
    private static final int BUFFER_SIZE = 65536;
    private static final boolean DEBUG = false;
    private static final String TAG = "AdbDebuggingManager";
    private boolean mAdbEnabled = false;
    private String mConfirmComponent;
    private final List<String> mConnectedKeys;
    private final Context mContext;
    private String mFingerprints;
    private final Handler mHandler = new AdbDebuggingHandler(FgThread.get().getLooper());
    private StatusBarManager mStatusBarManager;
    private final File mTestUserKeyFile;
    private AdbDebuggingThread mThread;

    public AdbDebuggingManager(Context context) {
        this.mContext = context;
        this.mTestUserKeyFile = null;
        this.mConnectedKeys = new ArrayList(1);
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
    }

    protected AdbDebuggingManager(Context context, String confirmComponent, File testUserKeyFile) {
        this.mContext = context;
        this.mConfirmComponent = confirmComponent;
        this.mTestUserKeyFile = testUserKeyFile;
        this.mConnectedKeys = new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public class AdbDebuggingThread extends Thread {
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private LocalSocket mSocket;
        private boolean mStopped;

        AdbDebuggingThread() {
            super(AdbDebuggingManager.TAG);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                synchronized (this) {
                    if (!this.mStopped) {
                        try {
                            openSocketLocked();
                        } catch (Exception e) {
                            SystemClock.sleep(1000);
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
                LocalSocketAddress address = new LocalSocketAddress(AdbDebuggingManager.ADBD_SOCKET, LocalSocketAddress.Namespace.RESERVED);
                this.mInputStream = null;
                this.mSocket = new LocalSocket(3);
                this.mSocket.connect(address);
                this.mOutputStream = this.mSocket.getOutputStream();
                this.mInputStream = this.mSocket.getInputStream();
            } catch (IOException ioe) {
                Slog.e(AdbDebuggingManager.TAG, "Caught an exception opening the socket: " + ioe);
                closeSocketLocked();
                throw ioe;
            }
        }

        private void listenToSocket() throws IOException {
            byte[] buffer;
            try {
                buffer = new byte[65536];
                Slog.e(AdbDebuggingManager.TAG, "Wrong message: " + new String(Arrays.copyOfRange(buffer, 0, 2)));
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
                if (count < 2) {
                    Slog.w(AdbDebuggingManager.TAG, "Read failed with count " + count);
                    break;
                } else if (buffer[0] == 80 && buffer[1] == 75) {
                    String key = new String(Arrays.copyOfRange(buffer, 2, count));
                    Slog.d(AdbDebuggingManager.TAG, "Received public key: " + key);
                    Message msg = AdbDebuggingManager.this.mHandler.obtainMessage(5);
                    msg.obj = key;
                    AdbDebuggingManager.this.mHandler.sendMessage(msg);
                } else if (buffer[0] == 68 && buffer[1] == 67) {
                    String key2 = new String(Arrays.copyOfRange(buffer, 2, count));
                    Slog.d(AdbDebuggingManager.TAG, "Received disconnected message: " + key2);
                    Message msg2 = AdbDebuggingManager.this.mHandler.obtainMessage(7);
                    msg2.obj = key2;
                    AdbDebuggingManager.this.mHandler.sendMessage(msg2);
                } else if (buffer[0] != 67 || buffer[1] != 75) {
                    break;
                } else {
                    String key3 = new String(Arrays.copyOfRange(buffer, 2, count));
                    Slog.d(AdbDebuggingManager.TAG, "Received connected key message: " + key3);
                    Message msg3 = AdbDebuggingManager.this.mHandler.obtainMessage(10);
                    msg3.obj = key3;
                    AdbDebuggingManager.this.mHandler.sendMessage(msg3);
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
                Slog.e(AdbDebuggingManager.TAG, "Failed closing output stream: " + e);
            }
            try {
                if (this.mSocket != null) {
                    this.mSocket.close();
                    this.mSocket = null;
                }
            } catch (IOException ex) {
                Slog.e(AdbDebuggingManager.TAG, "Failed closing socket: " + ex);
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
                        Slog.e(AdbDebuggingManager.TAG, "Failed to write response:", ex);
                    }
                }
            }
        }
    }

    class AdbDebuggingHandler extends Handler {
        static final int MESSAGE_ADB_ALLOW = 3;
        static final int MESSAGE_ADB_CLEAR = 6;
        static final int MESSAGE_ADB_CONFIRM = 5;
        static final int MESSAGE_ADB_CONNECTED_KEY = 10;
        static final int MESSAGE_ADB_DENY = 4;
        static final int MESSAGE_ADB_DISABLED = 2;
        static final int MESSAGE_ADB_DISCONNECT = 7;
        static final int MESSAGE_ADB_ENABLED = 1;
        static final int MESSAGE_ADB_PERSIST_KEYSTORE = 8;
        static final int MESSAGE_ADB_UPDATE_KEYSTORE = 9;
        static final long UPDATE_KEYSTORE_JOB_INTERVAL = 86400000;
        static final long UPDATE_KEYSTORE_MIN_JOB_INTERVAL = 60000;
        private AdbKeyStore mAdbKeyStore;
        private ContentObserver mAuthTimeObserver = new ContentObserver(this) {
            /* class com.android.server.adb.AdbDebuggingManager.AdbDebuggingHandler.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri) {
                Slog.d(AdbDebuggingManager.TAG, "Received notification that uri " + uri + " was modified; rescheduling keystore job");
                AdbDebuggingHandler.this.scheduleJobToUpdateAdbKeyStore();
            }
        };

        AdbDebuggingHandler(Looper looper) {
            super(looper);
        }

        AdbDebuggingHandler(Looper looper, AdbDebuggingThread thread, AdbKeyStore adbKeyStore) {
            super(looper);
            AdbDebuggingManager.this.mThread = thread;
            this.mAdbKeyStore = adbKeyStore;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean alwaysAllow = true;
            switch (msg.what) {
                case 1:
                    if (!AdbDebuggingManager.this.mAdbEnabled) {
                        registerForAuthTimeChanges();
                        AdbDebuggingManager.this.mAdbEnabled = true;
                        AdbDebuggingManager adbDebuggingManager = AdbDebuggingManager.this;
                        adbDebuggingManager.mThread = new AdbDebuggingThread();
                        AdbDebuggingManager.this.mThread.start();
                        this.mAdbKeyStore = new AdbKeyStore();
                        this.mAdbKeyStore.updateKeyStore();
                        scheduleJobToUpdateAdbKeyStore();
                        return;
                    }
                    return;
                case 2:
                    if (AdbDebuggingManager.this.mAdbEnabled) {
                        AdbDebuggingManager.this.mAdbEnabled = false;
                        if (AdbDebuggingManager.this.mThread != null) {
                            AdbDebuggingManager.this.mThread.stopListening();
                            AdbDebuggingManager.this.mThread = null;
                        }
                        if (!AdbDebuggingManager.this.mConnectedKeys.isEmpty()) {
                            for (String connectedKey : AdbDebuggingManager.this.mConnectedKeys) {
                                this.mAdbKeyStore.setLastConnectionTime(connectedKey, System.currentTimeMillis());
                            }
                            AdbDebuggingManager.this.sendPersistKeyStoreMessage();
                            AdbDebuggingManager.this.mConnectedKeys.clear();
                        }
                        scheduleJobToUpdateAdbKeyStore();
                        return;
                    }
                    return;
                case 3:
                    String key = (String) msg.obj;
                    String fingerprints = AdbDebuggingManager.this.getFingerprints(key);
                    if (!fingerprints.equals(AdbDebuggingManager.this.mFingerprints)) {
                        Slog.e(AdbDebuggingManager.TAG, "Fingerprints do not match. Got " + fingerprints + ", expected " + AdbDebuggingManager.this.mFingerprints);
                        return;
                    }
                    if (msg.arg1 != 1) {
                        alwaysAllow = false;
                    }
                    if (AdbDebuggingManager.this.mThread != null) {
                        AdbDebuggingManager.this.mThread.sendResponse("OK");
                        if (alwaysAllow) {
                            if (!AdbDebuggingManager.this.mConnectedKeys.contains(key)) {
                                AdbDebuggingManager.this.mConnectedKeys.add(key);
                            }
                            this.mAdbKeyStore.setLastConnectionTime(key, System.currentTimeMillis());
                            AdbDebuggingManager.this.sendPersistKeyStoreMessage();
                            scheduleJobToUpdateAdbKeyStore();
                        }
                        logAdbConnectionChanged(key, 2, alwaysAllow);
                        return;
                    }
                    return;
                case 4:
                    if (AdbDebuggingManager.this.mThread != null) {
                        AdbDebuggingManager.this.mThread.sendResponse("NO");
                        logAdbConnectionChanged(null, 3, false);
                        return;
                    }
                    return;
                case 5:
                    String key2 = (String) msg.obj;
                    if ("trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
                        Slog.d(AdbDebuggingManager.TAG, "Deferring adb confirmation until after vold decrypt");
                        if (AdbDebuggingManager.this.mThread != null) {
                            AdbDebuggingManager.this.mThread.sendResponse("NO");
                            logAdbConnectionChanged(key2, 6, false);
                            return;
                        }
                        return;
                    } else if (AdbDebuggingManager.this.mContext == null || AdbDebuggingManager.isDeviceProvisioned(AdbDebuggingManager.this.mContext)) {
                        String fingerprints2 = AdbDebuggingManager.this.getFingerprints(key2);
                        if (!"".equals(fingerprints2)) {
                            logAdbConnectionChanged(key2, 1, false);
                            AdbDebuggingManager.this.mFingerprints = fingerprints2;
                            AdbDebuggingManager adbDebuggingManager2 = AdbDebuggingManager.this;
                            adbDebuggingManager2.startConfirmation(key2, adbDebuggingManager2.mFingerprints);
                            return;
                        } else if (AdbDebuggingManager.this.mThread != null) {
                            AdbDebuggingManager.this.mThread.sendResponse("NO");
                            logAdbConnectionChanged(key2, 5, false);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        Slog.d(AdbDebuggingManager.TAG, "Deferring adb confirmation until device is provisioned");
                        if (AdbDebuggingManager.this.mThread != null) {
                            AdbDebuggingManager.this.mThread.sendResponse("NO");
                            return;
                        }
                        return;
                    }
                case 6:
                    Slog.d(AdbDebuggingManager.TAG, "Received a request to clear the adb authorizations");
                    AdbDebuggingManager.this.mConnectedKeys.clear();
                    AdbKeyStore adbKeyStore = this.mAdbKeyStore;
                    if (adbKeyStore != null) {
                        adbKeyStore.deleteKeyStore();
                        cancelJobToUpdateAdbKeyStore();
                        return;
                    }
                    return;
                case 7:
                    String key3 = (String) msg.obj;
                    boolean alwaysAllow2 = false;
                    if (key3 == null || key3.length() <= 0) {
                        Slog.w(AdbDebuggingManager.TAG, "Received a disconnected key message with an empty key");
                    } else if (AdbDebuggingManager.this.mConnectedKeys.contains(key3)) {
                        alwaysAllow2 = true;
                        this.mAdbKeyStore.setLastConnectionTime(key3, System.currentTimeMillis());
                        AdbDebuggingManager.this.sendPersistKeyStoreMessage();
                        scheduleJobToUpdateAdbKeyStore();
                        AdbDebuggingManager.this.mConnectedKeys.remove(key3);
                    }
                    logAdbConnectionChanged(key3, 7, alwaysAllow2);
                    return;
                case 8:
                    AdbKeyStore adbKeyStore2 = this.mAdbKeyStore;
                    if (adbKeyStore2 != null) {
                        adbKeyStore2.persistKeyStore();
                        return;
                    }
                    return;
                case 9:
                    if (!AdbDebuggingManager.this.mConnectedKeys.isEmpty()) {
                        for (String connectedKey2 : AdbDebuggingManager.this.mConnectedKeys) {
                            this.mAdbKeyStore.setLastConnectionTime(connectedKey2, System.currentTimeMillis());
                        }
                        AdbDebuggingManager.this.sendPersistKeyStoreMessage();
                        scheduleJobToUpdateAdbKeyStore();
                        return;
                    } else if (!this.mAdbKeyStore.isEmpty()) {
                        this.mAdbKeyStore.updateKeyStore();
                        scheduleJobToUpdateAdbKeyStore();
                        return;
                    } else {
                        return;
                    }
                case 10:
                    String key4 = (String) msg.obj;
                    if (key4 == null || key4.length() == 0) {
                        Slog.w(AdbDebuggingManager.TAG, "Received a connected key message with an empty key");
                        return;
                    }
                    if (!AdbDebuggingManager.this.mConnectedKeys.contains(key4)) {
                        AdbDebuggingManager.this.mConnectedKeys.add(key4);
                    }
                    this.mAdbKeyStore.setLastConnectionTime(key4, System.currentTimeMillis());
                    AdbDebuggingManager.this.sendPersistKeyStoreMessage();
                    scheduleJobToUpdateAdbKeyStore();
                    logAdbConnectionChanged(key4, 4, true);
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: package-private */
        public void registerForAuthTimeChanges() {
            AdbDebuggingManager.this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("adb_allowed_connection_time"), false, this.mAuthTimeObserver);
        }

        private void logAdbConnectionChanged(String key, int state, boolean alwaysAllow) {
            long lastConnectionTime = this.mAdbKeyStore.getLastConnectionTime(key);
            long authWindow = this.mAdbKeyStore.getAllowedConnectionTime();
            Slog.d(AdbDebuggingManager.TAG, "Logging key " + key + ", state = " + state + ", alwaysAllow = " + alwaysAllow + ", lastConnectionTime = " + lastConnectionTime + ", authWindow = " + authWindow);
            StatsLog.write(144, lastConnectionTime, authWindow, state, alwaysAllow);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public long scheduleJobToUpdateAdbKeyStore() {
            long delay;
            cancelJobToUpdateAdbKeyStore();
            long keyExpiration = this.mAdbKeyStore.getNextExpirationTime();
            if (keyExpiration == -1) {
                return -1;
            }
            if (keyExpiration == 0) {
                delay = 0;
            } else {
                delay = Math.max(Math.min(86400000L, keyExpiration), 60000L);
            }
            sendMessageDelayed(obtainMessage(9), delay);
            return delay;
        }

        private void cancelJobToUpdateAdbKeyStore() {
            removeMessages(9);
        }
    }

    /* access modifiers changed from: private */
    public static boolean isDeviceProvisioned(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getFingerprints(String key) {
        StringBuilder sb = new StringBuilder();
        if (key == null) {
            return "";
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
                return "";
            }
        } catch (Exception ex) {
            Slog.e(TAG, "Error getting digester", ex);
            return "";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startConfirmation(String key, String fingerprints) {
        String componentString;
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(ActivityManager.getCurrentUser());
        if (userInfo.isAdmin() || userInfo.isRepairMode()) {
            componentString = this.mConfirmComponent;
            if (componentString == null) {
                componentString = Resources.getSystem().getString(17039807);
            }
        } else {
            componentString = Resources.getSystem().getString(17039808);
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
            Slog.e(TAG, "unable to start adb whitelist activity: " + componentName, e);
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
            Slog.e(TAG, "unable to start adb whitelist service: " + componentName, e);
            return false;
        }
    }

    private Intent createConfirmationIntent(ComponentName componentName, String key, String fingerprints) {
        Intent intent = new Intent();
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        intent.putExtra("key", key);
        intent.putExtra("fingerprints", fingerprints);
        return intent;
    }

    private File getAdbFile(String fileName) {
        File adbDir = new File(Environment.getDataDirectory(), ADB_DIRECTORY);
        if (adbDir.exists()) {
            return new File(adbDir, fileName);
        }
        Slog.e(TAG, "ADB data directory does not exist");
        return null;
    }

    /* access modifiers changed from: package-private */
    public File getAdbTempKeysFile() {
        return getAdbFile(ADB_TEMP_KEYS_FILE);
    }

    /* access modifiers changed from: package-private */
    public File getUserKeyFile() {
        File file = this.mTestUserKeyFile;
        return file == null ? getAdbFile(ADB_KEYS_FILE) : file;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeKey(String key) {
        try {
            File keyFile = getUserKeyFile();
            if (keyFile != null) {
                FileOutputStream fo = new FileOutputStream(keyFile, true);
                fo.write(key.getBytes());
                fo.write(10);
                fo.close();
                FileUtils.setPermissions(keyFile.toString(), 416, -1, -1);
            }
        } catch (IOException ex) {
            Slog.e(TAG, "Error writing key:" + ex);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeKeys(Iterable<String> keys) {
        AtomicFile atomicKeyFile = null;
        try {
            File keyFile = getUserKeyFile();
            if (keyFile != null) {
                AtomicFile atomicKeyFile2 = new AtomicFile(keyFile);
                FileOutputStream fo = atomicKeyFile2.startWrite();
                for (String key : keys) {
                    fo.write(key.getBytes());
                    fo.write(10);
                }
                atomicKeyFile2.finishWrite(fo);
                FileUtils.setPermissions(keyFile.toString(), 416, -1, -1);
            }
        } catch (IOException ex) {
            Slog.e(TAG, "Error writing keys: " + ex);
            if (0 != 0) {
                atomicKeyFile.failWrite(null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    public void allowDebugging(boolean alwaysAllow, String publicKey) {
        Message msg = this.mHandler.obtainMessage(3);
        msg.arg1 = alwaysAllow ? 1 : 0;
        msg.obj = publicKey;
        this.mHandler.sendMessage(msg);
    }

    public void denyDebugging() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void clearDebuggingKeys() {
        this.mHandler.sendEmptyMessage(6);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPersistKeyStoreMessage() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(8));
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
        try {
            dump.write("keystore", 1138166333445L, FileUtils.readTextFile(getAdbTempKeysFile(), 0, null));
        } catch (IOException e3) {
            Slog.e(TAG, "Cannot read keystore: ", e3);
        }
        dump.end(token);
    }

    /* access modifiers changed from: package-private */
    public class AdbKeyStore {
        public static final long NO_PREVIOUS_CONNECTION = 0;
        private static final String SYSTEM_KEY_FILE = "/adb_keys";
        private static final String XML_ATTRIBUTE_KEY = "key";
        private static final String XML_ATTRIBUTE_LAST_CONNECTION = "lastConnection";
        private static final String XML_TAG_ADB_KEY = "adbKey";
        private AtomicFile mAtomicKeyFile;
        private File mKeyFile;
        private Map<String, Long> mKeyMap;
        private Set<String> mSystemKeys;

        AdbKeyStore() {
            init();
        }

        AdbKeyStore(File keyFile) {
            this.mKeyFile = keyFile;
            init();
        }

        private void init() {
            initKeyFile();
            this.mKeyMap = getKeyMap();
            this.mSystemKeys = getSystemKeysFromFile(SYSTEM_KEY_FILE);
            addUserKeysToKeyStore();
        }

        private void initKeyFile() {
            if (this.mKeyFile == null) {
                this.mKeyFile = AdbDebuggingManager.this.getAdbTempKeysFile();
            }
            File file = this.mKeyFile;
            if (file != null) {
                this.mAtomicKeyFile = new AtomicFile(file);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0036, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
            $closeResource(r3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
            throw r4;
         */
        private Set<String> getSystemKeysFromFile(String fileName) {
            Set<String> systemKeys = new HashSet<>();
            File systemKeyFile = new File(fileName);
            if (systemKeyFile.exists()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(systemKeyFile));
                    while (true) {
                        String key = in.readLine();
                        if (key == null) {
                            $closeResource(null, in);
                            break;
                        }
                        String key2 = key.trim();
                        if (key2.length() > 0) {
                            systemKeys.add(key2);
                        }
                    }
                } catch (IOException e) {
                    Slog.e(AdbDebuggingManager.TAG, "Caught an exception reading " + fileName + ": " + e);
                }
            }
            return systemKeys;
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        public boolean isEmpty() {
            return this.mKeyMap.isEmpty();
        }

        public void updateKeyStore() {
            if (filterOutOldKeys()) {
                AdbDebuggingManager.this.sendPersistKeyStoreMessage();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a9, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00aa, code lost:
            if (r2 != null) goto L_0x00ac;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ac, code lost:
            $closeResource(r0, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00af, code lost:
            throw r4;
         */
        private Map<String, Long> getKeyMap() {
            Map<String, Long> keyMap = new HashMap<>();
            if (this.mAtomicKeyFile == null) {
                initKeyFile();
                if (this.mAtomicKeyFile == null) {
                    Slog.e(AdbDebuggingManager.TAG, "Unable to obtain the key file, " + this.mKeyFile + ", for reading");
                    return keyMap;
                }
            }
            if (!this.mAtomicKeyFile.exists()) {
                return keyMap;
            }
            FileInputStream keyStream = this.mAtomicKeyFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(keyStream, StandardCharsets.UTF_8.name());
            XmlUtils.beginDocument(parser, XML_TAG_ADB_KEY);
            while (true) {
                if (parser.next() == 1) {
                    break;
                }
                String tagName = parser.getName();
                if (tagName == null) {
                    break;
                } else if (!tagName.equals(XML_TAG_ADB_KEY)) {
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    try {
                        keyMap.put(parser.getAttributeValue(null, XML_ATTRIBUTE_KEY), Long.valueOf(Long.valueOf(parser.getAttributeValue(null, XML_ATTRIBUTE_LAST_CONNECTION)).longValue()));
                    } catch (NumberFormatException e) {
                        Slog.e(AdbDebuggingManager.TAG, "Caught a NumberFormatException parsing the last connection time: " + e);
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            if (keyStream != null) {
                try {
                    $closeResource(null, keyStream);
                } catch (IOException | XmlPullParserException e2) {
                    Slog.e(AdbDebuggingManager.TAG, "Caught an exception parsing the XML key file: ", e2);
                }
            }
            return keyMap;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x003f, code lost:
            $closeResource(r3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
            throw r4;
         */
        private void addUserKeysToKeyStore() {
            File userKeyFile = AdbDebuggingManager.this.getUserKeyFile();
            boolean mapUpdated = false;
            if (userKeyFile != null && userKeyFile.exists()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(userKeyFile));
                    long time = System.currentTimeMillis();
                    while (true) {
                        String key = in.readLine();
                        if (key == null) {
                            break;
                        } else if (!this.mKeyMap.containsKey(key)) {
                            this.mKeyMap.put(key, Long.valueOf(time));
                            mapUpdated = true;
                        }
                    }
                    $closeResource(null, in);
                } catch (IOException e) {
                    Slog.e(AdbDebuggingManager.TAG, "Caught an exception reading " + userKeyFile + ": " + e);
                }
            }
            if (mapUpdated) {
                AdbDebuggingManager.this.sendPersistKeyStoreMessage();
            }
        }

        public void persistKeyStore() {
            filterOutOldKeys();
            if (this.mKeyMap.isEmpty()) {
                deleteKeyStore();
                return;
            }
            if (this.mAtomicKeyFile == null) {
                initKeyFile();
                if (this.mAtomicKeyFile == null) {
                    Slog.e(AdbDebuggingManager.TAG, "Unable to obtain the key file, " + this.mKeyFile + ", for writing");
                    return;
                }
            }
            try {
                XmlSerializer serializer = new FastXmlSerializer();
                FileOutputStream keyStream = this.mAtomicKeyFile.startWrite();
                serializer.setOutput(keyStream, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, true);
                for (Map.Entry<String, Long> keyEntry : this.mKeyMap.entrySet()) {
                    serializer.startTag(null, XML_TAG_ADB_KEY);
                    serializer.attribute(null, XML_ATTRIBUTE_KEY, keyEntry.getKey());
                    serializer.attribute(null, XML_ATTRIBUTE_LAST_CONNECTION, String.valueOf(keyEntry.getValue()));
                    serializer.endTag(null, XML_TAG_ADB_KEY);
                }
                serializer.endDocument();
                this.mAtomicKeyFile.finishWrite(keyStream);
            } catch (IOException e) {
                Slog.e(AdbDebuggingManager.TAG, "Caught an exception writing the key map: ", e);
                this.mAtomicKeyFile.failWrite(null);
            }
        }

        private boolean filterOutOldKeys() {
            boolean keysDeleted = false;
            long allowedTime = getAllowedConnectionTime();
            long systemTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, Long>> keyMapIterator = this.mKeyMap.entrySet().iterator();
            while (keyMapIterator.hasNext()) {
                long connectionTime = keyMapIterator.next().getValue().longValue();
                if (allowedTime != 0 && systemTime > connectionTime + allowedTime) {
                    keyMapIterator.remove();
                    keysDeleted = true;
                }
            }
            if (keysDeleted) {
                AdbDebuggingManager.this.writeKeys(this.mKeyMap.keySet());
            }
            return keysDeleted;
        }

        public long getNextExpirationTime() {
            long minExpiration = -1;
            long allowedTime = getAllowedConnectionTime();
            if (allowedTime == 0) {
                return -1;
            }
            long systemTime = System.currentTimeMillis();
            for (Map.Entry<String, Long> keyEntry : this.mKeyMap.entrySet()) {
                long keyExpiration = Math.max(0L, (keyEntry.getValue().longValue() + allowedTime) - systemTime);
                if (minExpiration == -1 || keyExpiration < minExpiration) {
                    minExpiration = keyExpiration;
                }
            }
            return minExpiration;
        }

        public void deleteKeyStore() {
            this.mKeyMap.clear();
            AdbDebuggingManager.this.deleteKeyFile();
            AtomicFile atomicFile = this.mAtomicKeyFile;
            if (atomicFile != null) {
                atomicFile.delete();
            }
        }

        public long getLastConnectionTime(String key) {
            return this.mKeyMap.getOrDefault(key, 0L).longValue();
        }

        public void setLastConnectionTime(String key, long connectionTime) {
            setLastConnectionTime(key, connectionTime, false);
        }

        public void setLastConnectionTime(String key, long connectionTime, boolean force) {
            if ((!this.mKeyMap.containsKey(key) || this.mKeyMap.get(key).longValue() < connectionTime || force) && !this.mSystemKeys.contains(key)) {
                if (!this.mKeyMap.containsKey(key)) {
                    AdbDebuggingManager.this.writeKey(key);
                }
                this.mKeyMap.put(key, Long.valueOf(connectionTime));
            }
        }

        public long getAllowedConnectionTime() {
            return Settings.Global.getLong(AdbDebuggingManager.this.mContext.getContentResolver(), "adb_allowed_connection_time", UnixCalendar.WEEK_IN_MILLIS);
        }

        public boolean isKeyAuthorized(String key) {
            if (this.mSystemKeys.contains(key)) {
                return true;
            }
            long lastConnectionTime = getLastConnectionTime(key);
            if (lastConnectionTime == 0) {
                return false;
            }
            long allowedConnectionTime = getAllowedConnectionTime();
            if (allowedConnectionTime == 0 || System.currentTimeMillis() < lastConnectionTime + allowedConnectionTime) {
                return true;
            }
            return false;
        }
    }
}
