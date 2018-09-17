package android.app;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.KeyProperties;
import android.util.ArrayMap;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsCallback.Stub;
import com.android.internal.app.IAppOpsService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppOpsManager {
    public static final int MODE_ALLOWED = 0;
    public static final int MODE_DEFAULT = 3;
    public static final int MODE_ERRORED = 2;
    public static final int MODE_IGNORED = 1;
    public static final String OPSTR_ACTIVATE_VPN = "android:activate_vpn";
    public static final String OPSTR_ADD_VOICEMAIL = "android:add_voicemail";
    public static final String OPSTR_BODY_SENSORS = "android:body_sensors";
    public static final String OPSTR_CALL_PHONE = "android:call_phone";
    public static final String OPSTR_CAMERA = "android:camera";
    public static final String OPSTR_COARSE_LOCATION = "android:coarse_location";
    public static final String OPSTR_FINE_LOCATION = "android:fine_location";
    public static final String OPSTR_GET_ACCOUNTS = "android:get_accounts";
    public static final String OPSTR_GET_USAGE_STATS = "android:get_usage_stats";
    public static final String OPSTR_MOCK_LOCATION = "android:mock_location";
    public static final String OPSTR_MONITOR_HIGH_POWER_LOCATION = "android:monitor_location_high_power";
    public static final String OPSTR_MONITOR_LOCATION = "android:monitor_location";
    public static final String OPSTR_PROCESS_OUTGOING_CALLS = "android:process_outgoing_calls";
    public static final String OPSTR_READ_CALENDAR = "android:read_calendar";
    public static final String OPSTR_READ_CALL_LOG = "android:read_call_log";
    public static final String OPSTR_READ_CELL_BROADCASTS = "android:read_cell_broadcasts";
    public static final String OPSTR_READ_CONTACTS = "android:read_contacts";
    public static final String OPSTR_READ_EXTERNAL_STORAGE = "android:read_external_storage";
    public static final String OPSTR_READ_PHONE_STATE = "android:read_phone_state";
    public static final String OPSTR_READ_SMS = "android:read_sms";
    public static final String OPSTR_RECEIVE_MMS = "android:receive_mms";
    public static final String OPSTR_RECEIVE_SMS = "android:receive_sms";
    public static final String OPSTR_RECEIVE_WAP_PUSH = "android:receive_wap_push";
    public static final String OPSTR_RECORD_AUDIO = "android:record_audio";
    public static final String OPSTR_SEND_SMS = "android:send_sms";
    public static final String OPSTR_SYSTEM_ALERT_WINDOW = "android:system_alert_window";
    public static final String OPSTR_USE_FINGERPRINT = "android:use_fingerprint";
    public static final String OPSTR_USE_SIP = "android:use_sip";
    public static final String OPSTR_WRITE_CALENDAR = "android:write_calendar";
    public static final String OPSTR_WRITE_CALL_LOG = "android:write_call_log";
    public static final String OPSTR_WRITE_CONTACTS = "android:write_contacts";
    public static final String OPSTR_WRITE_EXTERNAL_STORAGE = "android:write_external_storage";
    public static final String OPSTR_WRITE_SETTINGS = "android:write_settings";
    public static final int OP_ACCESS_NOTIFICATIONS = 25;
    public static final int OP_ACTIVATE_VPN = 47;
    public static final int OP_ADD_VOICEMAIL = 52;
    public static final int OP_ASSIST_SCREENSHOT = 50;
    public static final int OP_ASSIST_STRUCTURE = 49;
    public static final int OP_AUDIO_ALARM_VOLUME = 37;
    public static final int OP_AUDIO_BLUETOOTH_VOLUME = 39;
    public static final int OP_AUDIO_MASTER_VOLUME = 33;
    public static final int OP_AUDIO_MEDIA_VOLUME = 36;
    public static final int OP_AUDIO_NOTIFICATION_VOLUME = 38;
    public static final int OP_AUDIO_RING_VOLUME = 35;
    public static final int OP_AUDIO_VOICE_VOLUME = 34;
    public static final int OP_BODY_SENSORS = 56;
    public static final int OP_CALL_PHONE = 13;
    public static final int OP_CAMERA = 26;
    public static final int OP_COARSE_LOCATION = 0;
    public static final int OP_FINE_LOCATION = 1;
    public static final int OP_GET_ACCOUNTS = 62;
    public static final int OP_GET_USAGE_STATS = 43;
    public static final int OP_GPS = 2;
    public static final int OP_MOCK_LOCATION = 58;
    public static final int OP_MONITOR_HIGH_POWER_LOCATION = 42;
    public static final int OP_MONITOR_LOCATION = 41;
    public static final int OP_MUTE_MICROPHONE = 44;
    public static final int OP_NEIGHBORING_CELLS = 12;
    public static final int OP_NONE = -1;
    public static final int OP_PLAY_AUDIO = 28;
    public static final int OP_POST_NOTIFICATION = 11;
    public static final int OP_PROCESS_OUTGOING_CALLS = 54;
    public static final int OP_PROJECT_MEDIA = 46;
    public static final int OP_READ_CALENDAR = 8;
    public static final int OP_READ_CALL_LOG = 6;
    public static final int OP_READ_CELL_BROADCASTS = 57;
    public static final int OP_READ_CLIPBOARD = 29;
    public static final int OP_READ_CONTACTS = 4;
    public static final int OP_READ_EXTERNAL_STORAGE = 59;
    public static final int OP_READ_ICC_SMS = 21;
    public static final int OP_READ_PHONE_STATE = 51;
    public static final int OP_READ_SMS = 14;
    public static final int OP_RECEIVE_EMERGECY_SMS = 17;
    public static final int OP_RECEIVE_MMS = 18;
    public static final int OP_RECEIVE_SMS = 16;
    public static final int OP_RECEIVE_WAP_PUSH = 19;
    public static final int OP_RECORD_AUDIO = 27;
    public static final int OP_RUN_IN_BACKGROUND = 63;
    public static final int OP_SEND_SMS = 20;
    public static final int OP_SYSTEM_ALERT_WINDOW = 24;
    public static final int OP_TAKE_AUDIO_FOCUS = 32;
    public static final int OP_TAKE_MEDIA_BUTTONS = 31;
    public static final int OP_TOAST_WINDOW = 45;
    public static final int OP_TURN_SCREEN_ON = 61;
    public static final int OP_USE_FINGERPRINT = 55;
    public static final int OP_USE_SIP = 53;
    public static final int OP_VIBRATE = 3;
    public static final int OP_WAKE_LOCK = 40;
    public static final int OP_WIFI_SCAN = 10;
    public static final int OP_WRITE_CALENDAR = 9;
    public static final int OP_WRITE_CALL_LOG = 7;
    public static final int OP_WRITE_CLIPBOARD = 30;
    public static final int OP_WRITE_CONTACTS = 5;
    public static final int OP_WRITE_EXTERNAL_STORAGE = 60;
    public static final int OP_WRITE_ICC_SMS = 22;
    public static final int OP_WRITE_SETTINGS = 23;
    public static final int OP_WRITE_SMS = 15;
    public static final int OP_WRITE_WALLPAPER = 48;
    private static final int[] RUNTIME_PERMISSIONS_OPS = null;
    public static final int _NUM_OP = 64;
    private static boolean[] sOpAllowSystemRestrictionBypass;
    private static int[] sOpDefaultMode;
    private static boolean[] sOpDisableReset;
    private static String[] sOpNames;
    private static String[] sOpPerms;
    private static String[] sOpRestrictions;
    private static HashMap<String, Integer> sOpStrToOp;
    private static String[] sOpToString;
    private static int[] sOpToSwitch;
    private static HashMap<String, Integer> sRuntimePermToOp;
    static IBinder sToken;
    final Context mContext;
    final ArrayMap<OnOpChangedListener, IAppOpsCallback> mModeWatchers;
    final IAppOpsService mService;

    /* renamed from: android.app.AppOpsManager.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ OnOpChangedListener val$callback;

        AnonymousClass1(OnOpChangedListener val$callback) {
            this.val$callback = val$callback;
        }

        public void opChanged(int op, int uid, String packageName) {
            if (this.val$callback instanceof OnOpChangedInternalListener) {
                ((OnOpChangedInternalListener) this.val$callback).onOpChanged(op, packageName);
            }
            if (AppOpsManager.sOpToString[op] != null) {
                this.val$callback.onOpChanged(AppOpsManager.sOpToString[op], packageName);
            }
        }
    }

    public interface OnOpChangedListener {
        void onOpChanged(String str, String str2);
    }

    public static class OnOpChangedInternalListener implements OnOpChangedListener {
        public void onOpChanged(String op, String packageName) {
        }

        public void onOpChanged(int op, String packageName) {
        }
    }

    public static class OpEntry implements Parcelable {
        public static final Creator<OpEntry> CREATOR = null;
        private final int mDuration;
        private final int mMode;
        private final int mOp;
        private final String mProxyPackageName;
        private final int mProxyUid;
        private final long mRejectTime;
        private final long mTime;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.AppOpsManager.OpEntry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.AppOpsManager.OpEntry.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.AppOpsManager.OpEntry.<clinit>():void");
        }

        public OpEntry(int op, int mode, long time, long rejectTime, int duration, int proxyUid, String proxyPackage) {
            this.mOp = op;
            this.mMode = mode;
            this.mTime = time;
            this.mRejectTime = rejectTime;
            this.mDuration = duration;
            this.mProxyUid = proxyUid;
            this.mProxyPackageName = proxyPackage;
        }

        public int getOp() {
            return this.mOp;
        }

        public int getMode() {
            return this.mMode;
        }

        public long getTime() {
            return this.mTime;
        }

        public long getRejectTime() {
            return this.mRejectTime;
        }

        public boolean isRunning() {
            return this.mDuration == AppOpsManager.OP_NONE;
        }

        public int getDuration() {
            return this.mDuration == AppOpsManager.OP_NONE ? (int) (System.currentTimeMillis() - this.mTime) : this.mDuration;
        }

        public int getProxyUid() {
            return this.mProxyUid;
        }

        public String getProxyPackageName() {
            return this.mProxyPackageName;
        }

        public int describeContents() {
            return AppOpsManager.OP_COARSE_LOCATION;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mOp);
            dest.writeInt(this.mMode);
            dest.writeLong(this.mTime);
            dest.writeLong(this.mRejectTime);
            dest.writeInt(this.mDuration);
            dest.writeInt(this.mProxyUid);
            dest.writeString(this.mProxyPackageName);
        }

        OpEntry(Parcel source) {
            this.mOp = source.readInt();
            this.mMode = source.readInt();
            this.mTime = source.readLong();
            this.mRejectTime = source.readLong();
            this.mDuration = source.readInt();
            this.mProxyUid = source.readInt();
            this.mProxyPackageName = source.readString();
        }
    }

    public static class PackageOps implements Parcelable {
        public static final Creator<PackageOps> CREATOR = null;
        private final List<OpEntry> mEntries;
        private final String mPackageName;
        private final int mUid;

        /* renamed from: android.app.AppOpsManager.PackageOps.1 */
        static class AnonymousClass1 implements Creator<PackageOps> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m32createFromParcel(Parcel source) {
                return createFromParcel(source);
            }

            public PackageOps createFromParcel(Parcel source) {
                return new PackageOps(source);
            }

            public /* bridge */ /* synthetic */ Object[] m33newArray(int size) {
                return newArray(size);
            }

            public PackageOps[] newArray(int size) {
                return new PackageOps[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.AppOpsManager.PackageOps.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.AppOpsManager.PackageOps.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.AppOpsManager.PackageOps.<clinit>():void");
        }

        public PackageOps(String packageName, int uid, List<OpEntry> entries) {
            this.mPackageName = packageName;
            this.mUid = uid;
            this.mEntries = entries;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public int getUid() {
            return this.mUid;
        }

        public List<OpEntry> getOps() {
            return this.mEntries;
        }

        public int describeContents() {
            return AppOpsManager.OP_COARSE_LOCATION;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mPackageName);
            dest.writeInt(this.mUid);
            dest.writeInt(this.mEntries.size());
            for (int i = AppOpsManager.OP_COARSE_LOCATION; i < this.mEntries.size(); i += AppOpsManager.OP_FINE_LOCATION) {
                ((OpEntry) this.mEntries.get(i)).writeToParcel(dest, flags);
            }
        }

        PackageOps(Parcel source) {
            this.mPackageName = source.readString();
            this.mUid = source.readInt();
            this.mEntries = new ArrayList();
            int N = source.readInt();
            for (int i = AppOpsManager.OP_COARSE_LOCATION; i < N; i += AppOpsManager.OP_FINE_LOCATION) {
                this.mEntries.add((OpEntry) OpEntry.CREATOR.createFromParcel(source));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.AppOpsManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.AppOpsManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.AppOpsManager.<clinit>():void");
    }

    public static int opToSwitch(int op) {
        return sOpToSwitch[op];
    }

    public static String opToName(int op) {
        if (op == OP_NONE) {
            return KeyProperties.DIGEST_NONE;
        }
        return op < sOpNames.length ? sOpNames[op] : "Unknown(" + op + ")";
    }

    public static int strDebugOpToOp(String op) {
        for (int i = OP_COARSE_LOCATION; i < sOpNames.length; i += OP_FINE_LOCATION) {
            if (sOpNames[i].equals(op)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown operation string: " + op);
    }

    public static String opToPermission(int op) {
        return sOpPerms[op];
    }

    public static String opToRestriction(int op) {
        return sOpRestrictions[op];
    }

    public static int permissionToOpCode(String permission) {
        Integer boxedOpCode = (Integer) sRuntimePermToOp.get(permission);
        return boxedOpCode != null ? boxedOpCode.intValue() : OP_NONE;
    }

    public static boolean opAllowSystemBypassRestriction(int op) {
        return sOpAllowSystemRestrictionBypass[op];
    }

    public static int opToDefaultMode(int op) {
        return sOpDefaultMode[op];
    }

    public static boolean opAllowsReset(int op) {
        return !sOpDisableReset[op];
    }

    AppOpsManager(Context context, IAppOpsService service) {
        this.mModeWatchers = new ArrayMap();
        this.mContext = context;
        this.mService = service;
    }

    public List<PackageOps> getPackagesForOps(int[] ops) {
        try {
            return this.mService.getPackagesForOps(ops);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
        try {
            return this.mService.getOpsForPackage(uid, packageName, ops);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUidMode(int code, int uid, int mode) {
        try {
            this.mService.setUidMode(code, uid, mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUidMode(String appOp, int uid, int mode) {
        try {
            this.mService.setUidMode(strOpToOp(appOp), uid, mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUserRestriction(int code, boolean restricted, IBinder token) {
        setUserRestriction(code, restricted, token, null);
    }

    public void setUserRestriction(int code, boolean restricted, IBinder token, String[] exceptionPackages) {
        setUserRestrictionForUser(code, restricted, token, exceptionPackages, this.mContext.getUserId());
    }

    public void setUserRestrictionForUser(int code, boolean restricted, IBinder token, String[] exceptionPackages, int userId) {
        try {
            this.mService.setUserRestriction(code, restricted, token, userId, exceptionPackages);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMode(int code, int uid, String packageName, int mode) {
        try {
            this.mService.setMode(code, uid, packageName, mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setRestriction(int code, int usage, int mode, String[] exceptionPackages) {
        try {
            this.mService.setAudioRestriction(code, usage, Binder.getCallingUid(), mode, exceptionPackages);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resetAllModes() {
        try {
            this.mService.resetAllModes(UserHandle.myUserId(), null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String permissionToOp(String permission) {
        Integer opCode = (Integer) sRuntimePermToOp.get(permission);
        if (opCode == null) {
            return null;
        }
        return sOpToString[opCode.intValue()];
    }

    public void startWatchingMode(String op, String packageName, OnOpChangedListener callback) {
        startWatchingMode(strOpToOp(op), packageName, callback);
    }

    public void startWatchingMode(int op, String packageName, OnOpChangedListener callback) {
        synchronized (this.mModeWatchers) {
            IAppOpsCallback cb = (IAppOpsCallback) this.mModeWatchers.get(callback);
            if (cb == null) {
                cb = new AnonymousClass1(callback);
                this.mModeWatchers.put(callback, cb);
            }
            try {
                this.mService.startWatchingMode(op, packageName, cb);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void stopWatchingMode(OnOpChangedListener callback) {
        synchronized (this.mModeWatchers) {
            IAppOpsCallback cb = (IAppOpsCallback) this.mModeWatchers.get(callback);
            if (cb != null) {
                try {
                    this.mService.stopWatchingMode(cb);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    private String buildSecurityExceptionMsg(int op, int uid, String packageName) {
        return packageName + " from uid " + uid + " not allowed to perform " + sOpNames[op];
    }

    public static int strOpToOp(String op) {
        Integer val = (Integer) sOpStrToOp.get(op);
        if (val != null) {
            return val.intValue();
        }
        throw new IllegalArgumentException("Unknown operation string: " + op);
    }

    public int checkOp(String op, int uid, String packageName) {
        return checkOp(strOpToOp(op), uid, packageName);
    }

    public int checkOpNoThrow(String op, int uid, String packageName) {
        return checkOpNoThrow(strOpToOp(op), uid, packageName);
    }

    public int noteOp(String op, int uid, String packageName) {
        return noteOp(strOpToOp(op), uid, packageName);
    }

    public int noteOpNoThrow(String op, int uid, String packageName) {
        return noteOpNoThrow(strOpToOp(op), uid, packageName);
    }

    public int noteProxyOp(String op, String proxiedPackageName) {
        return noteProxyOp(strOpToOp(op), proxiedPackageName);
    }

    public int noteProxyOpNoThrow(String op, String proxiedPackageName) {
        return noteProxyOpNoThrow(strOpToOp(op), proxiedPackageName);
    }

    public int startOp(String op, int uid, String packageName) {
        return startOp(strOpToOp(op), uid, packageName);
    }

    public int startOpNoThrow(String op, int uid, String packageName) {
        return startOpNoThrow(strOpToOp(op), uid, packageName);
    }

    public void finishOp(String op, int uid, String packageName) {
        finishOp(strOpToOp(op), uid, packageName);
    }

    public int checkOp(int op, int uid, String packageName) {
        try {
            int mode = this.mService.checkOperation(op, uid, packageName);
            if (mode != OP_GPS) {
                return mode;
            }
            throw new SecurityException(buildSecurityExceptionMsg(op, uid, packageName));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkOpNoThrow(int op, int uid, String packageName) {
        try {
            return this.mService.checkOperation(op, uid, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void checkPackage(int uid, String packageName) {
        try {
            if (this.mService.checkPackage(uid, packageName) != 0) {
                throw new SecurityException("Package " + packageName + " does not belong to " + uid);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkAudioOp(int op, int stream, int uid, String packageName) {
        try {
            int mode = this.mService.checkAudioOperation(op, stream, uid, packageName);
            if (mode != OP_GPS) {
                return mode;
            }
            throw new SecurityException(buildSecurityExceptionMsg(op, uid, packageName));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkAudioOpNoThrow(int op, int stream, int uid, String packageName) {
        try {
            return this.mService.checkAudioOperation(op, stream, uid, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int noteOp(int op, int uid, String packageName) {
        try {
            int mode = this.mService.noteOperation(op, uid, packageName);
            if (mode != OP_GPS) {
                return mode;
            }
            throw new SecurityException(buildSecurityExceptionMsg(op, uid, packageName));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int noteProxyOp(int op, String proxiedPackageName) {
        int mode = noteProxyOpNoThrow(op, proxiedPackageName);
        if (mode != OP_GPS) {
            return mode;
        }
        throw new SecurityException("Proxy package " + this.mContext.getOpPackageName() + " from uid " + Process.myUid() + " or calling package " + proxiedPackageName + " from uid " + Binder.getCallingUid() + " not allowed to perform " + sOpNames[op]);
    }

    public int noteProxyOpNoThrow(int op, String proxiedPackageName) {
        try {
            return this.mService.noteProxyOperation(op, this.mContext.getOpPackageName(), Binder.getCallingUid(), proxiedPackageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int noteOpNoThrow(int op, int uid, String packageName) {
        try {
            return this.mService.noteOperation(op, uid, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int noteOp(int op) {
        return noteOp(op, Process.myUid(), this.mContext.getOpPackageName());
    }

    public static IBinder getToken(IAppOpsService service) {
        synchronized (AppOpsManager.class) {
            if (sToken != null) {
                IBinder iBinder = sToken;
                return iBinder;
            }
            try {
                sToken = service.getToken(new Binder());
                iBinder = sToken;
                return iBinder;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int startOp(int op, int uid, String packageName) {
        try {
            int mode = this.mService.startOperation(getToken(this.mService), op, uid, packageName);
            if (mode != OP_GPS) {
                return mode;
            }
            throw new SecurityException(buildSecurityExceptionMsg(op, uid, packageName));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int startOpNoThrow(int op, int uid, String packageName) {
        try {
            return this.mService.startOperation(getToken(this.mService), op, uid, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int startOp(int op) {
        return startOp(op, Process.myUid(), this.mContext.getOpPackageName());
    }

    public void finishOp(int op, int uid, String packageName) {
        try {
            this.mService.finishOperation(getToken(this.mService), op, uid, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void finishOp(int op) {
        finishOp(op, Process.myUid(), this.mContext.getOpPackageName());
    }
}
