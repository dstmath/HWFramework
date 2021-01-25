package com.android.server.inputmethod;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSystemProperty;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.IInputMethodPrivilegedOperations;
import com.android.internal.inputmethod.InputMethodDebug;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputBindResult;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.inputmethod.InputMethodManagerService;
import com.android.server.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.server.inputmethod.InputMethodUtils;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.inputmethod.IHwInputContentListener;
import com.huawei.android.inputmethod.IHwInputMethodListener;
import com.huawei.android.inputmethod.IHwInputMethodManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class InputMethodManagerService extends AbsInputMethodManagerService implements IHwInputMethodManagerInner, ServiceConnection, Handler.Callback {
    private static final String ACTION_SHOW_INPUT_METHOD_PICKER = "com.android.server.inputmethod.InputMethodManagerService.SHOW_INPUT_METHOD_PICKER";
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    static final boolean DEBUG_RESTORE = false;
    private static final int DIALOG_BOTTOM_PADDING = 24;
    private static final int FALLBACK_DISPLAY_ID = 0;
    private static final int IME_CONNECTION_BIND_FLAGS = 1082130437;
    private static final int IME_VISIBLE_BIND_FLAGS = 738725889;
    private static final boolean IS_TV = (SystemProperties.getBoolean("ro.config.remoteinput.debugtv", false) || "tv".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)));
    private static final boolean IS_USE_VOICE;
    static final int MSG_APPLY_IME_VISIBILITY = 3070;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HARD_KEYBOARD_SWITCH_CHANGED = 4000;
    static final int MSG_HIDE_CURRENT_INPUT_METHOD = 1035;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_INITIALIZE_IME = 1040;
    static final int MSG_REPORT_FULLSCREEN_MODE = 3045;
    static final int MSG_REPORT_PRE_RENDERED = 3060;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SHOW_IM_CONFIG = 3;
    static final int MSG_SHOW_IM_SUBTYPE_ENABLER = 2;
    static final int MSG_SHOW_IM_SUBTYPE_PICKER = 1;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_SYSTEM_UNLOCK_USER = 5000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final String MULTI_DISPLAY_INPUT_METHOD_PERMISSION = "com.huawei.permission.MULTI_DISPLAY_INPUT_METHOD";
    private static final int NOT_A_SUBTYPE_ID = -1;
    public static final String REMOTE_INPUT_METHOD_ID = "com.huawei.pcassistant/.ime.HwRemoteInputMethodService";
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    static final String TAG = "InputMethodManagerService";
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    static final long TIME_TO_RECONNECT = 3000;
    private boolean mAccessibilityRequestingNoSoftKeyboard;
    private SparseArray<ActivityViewInfo> mActivityViewDisplayIdToParentMap = new SparseArray<>();
    private final ArrayMap<String, List<InputMethodSubtype>> mAdditionalSubtypeMap = new ArrayMap<>();
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition = 0;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final ArrayMap<IBinder, ClientState> mClients = new ArrayMap<>();
    final Context mContext;
    private Matrix mCurActivityViewToScreenMatrix = null;
    EditorInfo mCurAttribute;
    private String mCurChangedImeId;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    int mCurFocusedWindowSoftInputMode;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurTokenDisplayId = -1;
    private InputMethodSubtype mCurrentSubtype;
    private AlertDialog.Builder mDialogBuilder;
    private final DisplayManagerInternal mDisplayManagerInternal;
    ArrayMap<String, Boolean> mEnabledFileMap = new ArrayMap<>();
    SessionState mEnabledSession;
    private int mFocusDisplayId = 0;
    final Handler mHandler;
    private final int mHardKeyboardBehavior;
    private final HardKeyboardListener mHardKeyboardListener;
    final boolean mHasFeature;
    boolean mHaveConnection;
    IHwInputMethodManagerServiceEx mHwIMMSEx = null;
    HwInnerInputMethodManagerService mHwInnerService = new HwInnerInputMethodManagerService(this);
    private final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    final ImeDisplayValidator mImeDisplayValidator;
    private PendingIntent mImeSwitchPendingIntent;
    private Notification.Builder mImeSwitcherNotification;
    @GuardedBy({"mMethodMap"})
    private final WeakHashMap<IBinder, IBinder> mImeTargetWindowMap = new WeakHashMap<>();
    int mImeWindowVis;
    private InputMethodInfo[] mIms;
    boolean mInFullscreenMode;
    boolean mInputShown;
    boolean mIsInteractive = true;
    private final boolean mIsLowRam;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    IBinder mLastImeTargetWindow;
    private int mLastSwitchUserId;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList<>();
    final ArrayMap<String, InputMethodInfo> mMethodMap = new ArrayMap<>();
    @GuardedBy({"mMethodMap"})
    private int mMethodMapUpdateCount = 0;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private NotificationManager mNotificationManager;
    private boolean mNotificationShown;
    private String mPreDefaultImeId;
    final Resources mRes;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans = new LruCache<>(20);
    final InputMethodUtils.InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    private boolean mShowImeWithHardKeyboard;
    private boolean mShowOngoingImeSwitcherForPhones;
    boolean mShowRequested;
    private final String mSlotIme;
    @GuardedBy({"mMethodMap"})
    private final StartInputHistory mStartInputHistory = new StartInputHistory();
    private StatusBarManagerService mStatusBar;
    private int[] mSubtypeIds;
    private final InputMethodSubtypeSwitchingController mSwitchingController;
    private AlertDialog mSwitchingDialog;
    private View mSwitchingDialogTitleView;
    private IBinder mSwitchingDialogToken = new Binder();
    boolean mSystemReady;
    private final UserManager mUserManager;
    private final UserManagerInternal mUserManagerInternal;
    boolean mVisibleBound = false;
    final ServiceConnection mVisibleConnection = new ServiceConnection() {
        /* class com.android.server.inputmethod.InputMethodManagerService.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (InputMethodManagerService.this.mVisibleBound) {
                    InputMethodManagerService.this.mContext.unbindService(InputMethodManagerService.this.mVisibleConnection);
                    InputMethodManagerService.this.mVisibleBound = false;
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    final WindowManagerInternal mWindowManagerInternal;

    @Retention(RetentionPolicy.SOURCE)
    private @interface HardKeyboardBehavior {
        public static final int WIRED_AFFORDANCE = 1;
        public static final int WIRELESS_AFFORDANCE = 0;
    }

    /* access modifiers changed from: package-private */
    @FunctionalInterface
    public interface ImeDisplayValidator {
        boolean displayCanShowIme(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface ShellCommandResult {
        public static final int FAILURE = -1;
        public static final int SUCCESS = 0;
    }

    static {
        boolean z = false;
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", "")) && "tablet".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR))) {
            z = true;
        }
        IS_USE_VOICE = z;
    }

    /* access modifiers changed from: private */
    public static final class DebugFlag {
        private static final Object LOCK = new Object();
        private final boolean mDefaultValue;
        private final String mKey;
        @GuardedBy({"LOCK"})
        private boolean mValue;

        public DebugFlag(String key, boolean defaultValue) {
            this.mKey = key;
            this.mDefaultValue = defaultValue;
            this.mValue = SystemProperties.getBoolean(key, defaultValue);
        }

        /* access modifiers changed from: package-private */
        public void refresh() {
            synchronized (LOCK) {
                this.mValue = SystemProperties.getBoolean(this.mKey, this.mDefaultValue);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean value() {
            boolean z;
            synchronized (LOCK) {
                z = this.mValue;
            }
            return z;
        }
    }

    /* access modifiers changed from: private */
    public static final class DebugFlags {
        static final DebugFlag FLAG_OPTIMIZE_START_INPUT = new DebugFlag("debug.optimize_startinput", false);
        static final DebugFlag FLAG_PRE_RENDER_IME_VIEWS = new DebugFlag("persist.pre_render_ime_views", false);

        private DebugFlags() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class SessionState {
        InputChannel channel;
        final ClientState client;
        final IInputMethod method;
        IInputMethodSession session;

        public String toString() {
            return "SessionState{uid " + this.client.uid + " pid " + this.client.pid + " method " + Integer.toHexString(System.identityHashCode(this.method)) + " session " + Integer.toHexString(System.identityHashCode(this.session)) + " channel " + this.channel + "}";
        }

        SessionState(ClientState _client, IInputMethod _method, IInputMethodSession _session, InputChannel _channel) {
            this.client = _client;
            this.method = _method;
            this.session = _session;
            this.channel = _channel;
        }
    }

    /* access modifiers changed from: private */
    public static final class ClientDeathRecipient implements IBinder.DeathRecipient {
        private final IInputMethodClient mClient;
        private final InputMethodManagerService mImms;

        ClientDeathRecipient(InputMethodManagerService imms, IInputMethodClient client) {
            this.mImms = imms;
            this.mClient = client;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.mImms.removeClient(this.mClient);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ClientState {
        final InputBinding binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        final IInputMethodClient client;
        final ClientDeathRecipient clientDeathRecipient;
        SessionState curSession;
        final IInputContext inputContext;
        final int pid;
        final int selfReportedDisplayId;
        boolean sessionRequested;
        boolean shouldPreRenderIme;
        final int uid;

        public String toString() {
            return "ClientState{" + Integer.toHexString(System.identityHashCode(this)) + " uid=" + this.uid + " pid=" + this.pid + " displayId=" + this.selfReportedDisplayId + "}";
        }

        ClientState(IInputMethodClient _client, IInputContext _inputContext, int _uid, int _pid, int _selfReportedDisplayId, ClientDeathRecipient _clientDeathRecipient) {
            this.client = _client;
            this.inputContext = _inputContext;
            this.uid = _uid;
            this.pid = _pid;
            this.selfReportedDisplayId = _selfReportedDisplayId;
            this.clientDeathRecipient = _clientDeathRecipient;
        }
    }

    /* access modifiers changed from: private */
    public static final class ActivityViewInfo {
        private final Matrix mMatrix;
        private final ClientState mParentClient;

        ActivityViewInfo(ClientState parentClient, Matrix matrix) {
            this.mParentClient = parentClient;
            this.mMatrix = matrix;
        }
    }

    /* access modifiers changed from: private */
    public static class StartInputInfo {
        private static final AtomicInteger sSequenceNumber = new AtomicInteger(0);
        final int mClientBindSequenceNumber;
        final EditorInfo mEditorInfo;
        final int mImeDisplayId;
        final String mImeId;
        final IBinder mImeToken;
        final int mImeUserId;
        final boolean mRestarting;
        final int mSequenceNumber = sSequenceNumber.getAndIncrement();
        final int mStartInputReason;
        final int mTargetDisplayId;
        final int mTargetUserId;
        final IBinder mTargetWindow;
        final int mTargetWindowSoftInputMode;
        final long mTimestamp = SystemClock.uptimeMillis();
        final long mWallTime = System.currentTimeMillis();

        StartInputInfo(int imeUserId, IBinder imeToken, int imeDisplayId, String imeId, int startInputReason, boolean restarting, int targetUserId, int targetDisplayId, IBinder targetWindow, EditorInfo editorInfo, int targetWindowSoftInputMode, int clientBindSequenceNumber) {
            this.mImeUserId = imeUserId;
            this.mImeToken = imeToken;
            this.mImeDisplayId = imeDisplayId;
            this.mImeId = imeId;
            this.mStartInputReason = startInputReason;
            this.mRestarting = restarting;
            this.mTargetUserId = targetUserId;
            this.mTargetDisplayId = targetDisplayId;
            this.mTargetWindow = targetWindow;
            this.mEditorInfo = editorInfo;
            this.mTargetWindowSoftInputMode = targetWindowSoftInputMode;
            this.mClientBindSequenceNumber = clientBindSequenceNumber;
        }
    }

    /* access modifiers changed from: private */
    public static final class StartInputHistory {
        private static final int ENTRY_SIZE_FOR_HIGH_RAM_DEVICE = 16;
        private static final int ENTRY_SIZE_FOR_LOW_RAM_DEVICE = 5;
        private final Entry[] mEntries;
        private int mNextIndex;

        private StartInputHistory() {
            this.mEntries = new Entry[getEntrySize()];
            this.mNextIndex = 0;
        }

        private static int getEntrySize() {
            if (ActivityManager.isLowRamDeviceStatic()) {
                return 5;
            }
            return 16;
        }

        /* access modifiers changed from: private */
        public static final class Entry {
            int mClientBindSequenceNumber;
            EditorInfo mEditorInfo;
            int mImeDisplayId;
            String mImeId;
            String mImeTokenString;
            int mImeUserId;
            boolean mRestarting;
            int mSequenceNumber;
            int mStartInputReason;
            int mTargetDisplayId;
            int mTargetUserId;
            int mTargetWindowSoftInputMode;
            String mTargetWindowString;
            long mTimestamp;
            long mWallTime;

            Entry(StartInputInfo original) {
                set(original);
            }

            /* access modifiers changed from: package-private */
            public void set(StartInputInfo original) {
                this.mSequenceNumber = original.mSequenceNumber;
                this.mTimestamp = original.mTimestamp;
                this.mWallTime = original.mWallTime;
                this.mImeUserId = original.mImeUserId;
                this.mImeTokenString = String.valueOf(original.mImeToken);
                this.mImeDisplayId = original.mImeDisplayId;
                this.mImeId = original.mImeId;
                this.mStartInputReason = original.mStartInputReason;
                this.mRestarting = original.mRestarting;
                this.mTargetUserId = original.mTargetUserId;
                this.mTargetDisplayId = original.mTargetDisplayId;
                this.mTargetWindowString = String.valueOf(original.mTargetWindow);
                this.mEditorInfo = original.mEditorInfo;
                this.mTargetWindowSoftInputMode = original.mTargetWindowSoftInputMode;
                this.mClientBindSequenceNumber = original.mClientBindSequenceNumber;
            }
        }

        /* access modifiers changed from: package-private */
        public void addEntry(StartInputInfo info) {
            int index = this.mNextIndex;
            Entry[] entryArr = this.mEntries;
            if (entryArr[index] == null) {
                entryArr[index] = new Entry(info);
            } else {
                entryArr[index].set(info);
            }
            this.mNextIndex = (this.mNextIndex + 1) % this.mEntries.length;
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw, String prefix) {
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            int i = 0;
            while (true) {
                Entry[] entryArr = this.mEntries;
                if (i < entryArr.length) {
                    Entry entry = entryArr[(this.mNextIndex + i) % entryArr.length];
                    if (entry != null) {
                        pw.print(prefix);
                        pw.println("StartInput #" + entry.mSequenceNumber + ":");
                        pw.print(prefix);
                        pw.println(" time=" + dataFormat.format(new Date(entry.mWallTime)) + " (timestamp=" + entry.mTimestamp + ") reason=" + InputMethodDebug.startInputReasonToString(entry.mStartInputReason) + " restarting=" + entry.mRestarting);
                        pw.print(prefix);
                        StringBuilder sb = new StringBuilder();
                        sb.append(" imeToken=");
                        sb.append(entry.mImeTokenString);
                        sb.append(" [");
                        sb.append(entry.mImeId);
                        sb.append("]");
                        pw.print(sb.toString());
                        pw.print(" imeUserId=" + entry.mImeUserId);
                        pw.println(" imeDisplayId=" + entry.mImeDisplayId);
                        pw.print(prefix);
                        pw.println(" targetWin=" + entry.mTargetWindowString + " [" + entry.mEditorInfo.packageName + "] targetUserId=" + entry.mTargetUserId + " targetDisplayId=" + entry.mTargetDisplayId + " clientBindSeq=" + entry.mClientBindSequenceNumber);
                        pw.print(prefix);
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(" softInputMode=");
                        sb2.append(InputMethodDebug.softInputModeToString(entry.mTargetWindowSoftInputMode));
                        pw.println(sb2.toString());
                        pw.print(prefix);
                        pw.println(" inputType=0x" + Integer.toHexString(entry.mEditorInfo.inputType) + " imeOptions=0x" + Integer.toHexString(entry.mEditorInfo.imeOptions) + " fieldId=0x" + Integer.toHexString(entry.mEditorInfo.fieldId) + " fieldName=" + entry.mEditorInfo.fieldName + " actionId=" + entry.mEditorInfo.actionId + " actionLabel=" + ((Object) entry.mEditorInfo.actionLabel));
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class SettingsObserver extends ContentObserver {
        String mLastEnabled = "";
        boolean mRegistered = false;
        int mUserId;

        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void registerContentObserverLocked(int userId) {
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = InputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    InputMethodManagerService.this.mContext.getContentResolver().unregisterContentObserver(this);
                    this.mRegistered = false;
                }
                if (this.mUserId != userId) {
                    this.mLastEnabled = "";
                    this.mUserId = userId;
                }
                resolver.registerContentObserver(Settings.Secure.getUriFor("default_input_method"), false, this, userId);
                resolver.registerContentObserver(Settings.Secure.getUriFor("enabled_input_methods"), false, this, userId);
                resolver.registerContentObserver(Settings.Secure.getUriFor("selected_input_method_subtype"), false, this, userId);
                resolver.registerContentObserver(Settings.Secure.getUriFor("show_ime_with_hard_keyboard"), false, this, userId);
                resolver.registerContentObserver(Settings.Secure.getUriFor("accessibility_soft_keyboard_mode"), false, this, userId);
                this.mRegistered = true;
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            String defaultInputMethodId;
            Uri showImeUri = Settings.Secure.getUriFor("show_ime_with_hard_keyboard");
            Uri accessibilityRequestingNoImeUri = Settings.Secure.getUriFor("accessibility_soft_keyboard_mode");
            Uri defaultImeUri = Settings.Secure.getUriFor("default_input_method");
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (defaultImeUri.equals(uri) && (defaultInputMethodId = Settings.Secure.getStringForUser(InputMethodManagerService.this.mContext.getContentResolver(), "default_input_method", InputMethodManagerService.this.mLastSwitchUserId)) != null && !defaultInputMethodId.equals(InputMethodManagerService.this.mCurChangedImeId)) {
                    InputMethodManagerService.this.mPreDefaultImeId = defaultInputMethodId;
                }
                if (showImeUri.equals(uri)) {
                    InputMethodManagerService.this.updateKeyboardFromSettingsLocked();
                } else if (accessibilityRequestingNoImeUri.equals(uri)) {
                    int accessibilitySoftKeyboardSetting = Settings.Secure.getIntForUser(InputMethodManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, this.mUserId);
                    InputMethodManagerService.this.mAccessibilityRequestingNoSoftKeyboard = (accessibilitySoftKeyboardSetting & 3) == 1;
                    if (InputMethodManagerService.this.mAccessibilityRequestingNoSoftKeyboard) {
                        boolean showRequested = InputMethodManagerService.this.mShowRequested;
                        InputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        InputMethodManagerService.this.mShowRequested = showRequested;
                    } else if (InputMethodManagerService.this.mShowRequested) {
                        InputMethodManagerService.this.showCurrentInputLocked(1, null);
                    }
                } else {
                    boolean enabledChanged = false;
                    String newEnabled = InputMethodManagerService.this.mSettings.getEnabledInputMethodsStr();
                    if (!this.mLastEnabled.equals(newEnabled)) {
                        this.mLastEnabled = newEnabled;
                        enabledChanged = true;
                    }
                    InputMethodManagerService.this.updateInputMethodsFromSettingsLocked(enabledChanged);
                }
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return "SettingsObserver{mUserId=" + this.mUserId + " mRegistered=" + this.mRegistered + " mLastEnabled=" + this.mLastEnabled + "}";
        }
    }

    /* access modifiers changed from: private */
    public final class ImmsBroadcastReceiverForSystemUser extends BroadcastReceiver {
        private ImmsBroadcastReceiverForSystemUser() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                InputMethodManagerService.this.updateCurrentProfileIds();
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                InputMethodManagerService.this.onActionLocaleChanged();
            } else if (InputMethodManagerService.ACTION_SHOW_INPUT_METHOD_PICKER.equals(action)) {
                InputMethodManagerService.this.mHandler.obtainMessage(1, 1, 0).sendToTarget();
            } else {
                Slog.w(InputMethodManagerService.TAG, "Unexpected intent " + intent);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ImmsBroadcastReceiverForAllUsers extends BroadcastReceiver {
        private ImmsBroadcastReceiverForAllUsers() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                BroadcastReceiver.PendingResult pendingResult = getPendingResult();
                if (pendingResult != null) {
                    int senderUserId = pendingResult.getSendingUserId();
                    if (senderUserId != -1) {
                        if ((InputMethodSystemProperty.PER_PROFILE_IME_ENABLED ? senderUserId : InputMethodManagerService.this.mUserManagerInternal.getProfileParentId(senderUserId)) != InputMethodManagerService.this.mSettings.getCurrentUserId()) {
                            return;
                        }
                    }
                    InputMethodManagerService.this.hideInputMethodMenu();
                    return;
                }
                return;
            }
            Slog.w(InputMethodManagerService.TAG, "Unexpected intent " + intent);
        }
    }

    /* access modifiers changed from: package-private */
    public void onActionLocaleChanged() {
        synchronized (this.mMethodMap) {
            LocaleList possibleNewLocale = this.mRes.getConfiguration().getLocales();
            if (possibleNewLocale == null || !possibleNewLocale.equals(this.mLastSystemLocales)) {
                buildInputMethodListLocked(true);
                resetDefaultImeLocked(this.mContext);
                updateFromSettingsLocked(true);
                this.mLastSystemLocales = possibleNewLocale;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class MyPackageMonitor extends PackageMonitor {
        private final ArrayList<String> mChangedPackages = new ArrayList<>();
        private boolean mImePackageAppeared = false;
        @GuardedBy({"mMethodMap"})
        private final ArraySet<String> mKnownImePackageNames = new ArraySet<>();

        MyPackageMonitor() {
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mMethodMap"})
        public void clearKnownImePackageNamesLocked() {
            this.mKnownImePackageNames.clear();
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mMethodMap"})
        public final void addKnownImePackageNameLocked(String packageName) {
            this.mKnownImePackageNames.add(packageName);
        }

        @GuardedBy({"mMethodMap"})
        private boolean isChangingPackagesOfCurrentUserLocked() {
            return getChangingUserId() == InputMethodManagerService.this.mSettings.getCurrentUserId();
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (!isChangingPackagesOfCurrentUserLocked()) {
                    return false;
                }
                String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                int N = InputMethodManagerService.this.mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i = 0; i < N; i++) {
                        InputMethodInfo imi = InputMethodManagerService.this.mMethodList.get(i);
                        if (imi.getId().equals(curInputMethodId)) {
                            for (String pkg : packages) {
                                if (imi.getPackageName().equals(pkg)) {
                                    return !doit ? true : true;
                                }
                            }
                            continue;
                        }
                    }
                }
                return false;
            }
        }

        public void onBeginPackageChanges() {
            clearPackageChangeState();
        }

        public void onPackageAppeared(String packageName, int reason) {
            if (!this.mImePackageAppeared && !InputMethodManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod").setPackage(packageName), 512, getChangingUserId()).isEmpty()) {
                this.mImePackageAppeared = true;
            }
            this.mChangedPackages.add(packageName);
        }

        public void onPackageDisappeared(String packageName, int reason) {
            this.mChangedPackages.add(packageName);
        }

        public void onPackageModified(String packageName) {
            this.mChangedPackages.add(packageName);
        }

        public void onPackagesSuspended(String[] packages) {
            for (String packageName : packages) {
                this.mChangedPackages.add(packageName);
            }
        }

        public void onPackagesUnsuspended(String[] packages) {
            for (String packageName : packages) {
                this.mChangedPackages.add(packageName);
            }
        }

        public void onFinishPackageChanges() {
            onFinishPackageChangesInternal();
            clearPackageChangeState();
        }

        private void clearPackageChangeState() {
            this.mChangedPackages.clear();
            this.mImePackageAppeared = false;
        }

        @GuardedBy({"mMethodMap"})
        private boolean shouldRebuildInputMethodListLocked() {
            if (this.mImePackageAppeared) {
                return true;
            }
            int N = this.mChangedPackages.size();
            for (int i = 0; i < N; i++) {
                if (this.mKnownImePackageNames.contains(this.mChangedPackages.get(i))) {
                    return true;
                }
            }
            return false;
        }

        private void onFinishPackageChangesInternal() {
            int change;
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (isChangingPackagesOfCurrentUserLocked()) {
                    if (shouldRebuildInputMethodListLocked()) {
                        InputMethodInfo curIm = null;
                        String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                        int N = InputMethodManagerService.this.mMethodList.size();
                        if (curInputMethodId != null) {
                            for (int i = 0; i < N; i++) {
                                InputMethodInfo imi = InputMethodManagerService.this.mMethodList.get(i);
                                if (imi.getId().equals(curInputMethodId)) {
                                    curIm = imi;
                                }
                                int change2 = isPackageDisappearing(imi.getPackageName());
                                if (isPackageModified(imi.getPackageName())) {
                                    InputMethodManagerService.this.mAdditionalSubtypeMap.remove(imi.getId());
                                }
                                if (change2 == 2 || change2 == 3) {
                                    Slog.i(InputMethodManagerService.TAG, "Input method uninstalled, disabling: " + imi.getComponent());
                                    InputMethodManagerService.this.setInputMethodEnabledLocked(imi.getId(), false);
                                }
                            }
                        }
                        InputMethodManagerService.this.buildInputMethodListLocked(false);
                        boolean changed = false;
                        if (curIm != null && ((change = isPackageDisappearing(curIm.getPackageName())) == 2 || change == 3)) {
                            ServiceInfo si = null;
                            try {
                                si = InputMethodManagerService.this.mIPackageManager.getServiceInfo(curIm.getComponent(), 0, InputMethodManagerService.this.mSettings.getCurrentUserId());
                            } catch (RemoteException e) {
                            }
                            if (si == null) {
                                Slog.i(InputMethodManagerService.TAG, "Current input method removed: " + curInputMethodId);
                                InputMethodManagerService.this.updateSystemUiLocked(0, InputMethodManagerService.this.mBackDisposition);
                                if (!InputMethodManagerService.this.chooseNewDefaultIMELocked()) {
                                    changed = true;
                                    curIm = null;
                                    Slog.i(InputMethodManagerService.TAG, "Unsetting current input method");
                                    InputMethodManagerService.this.resetSelectedInputMethodAndSubtypeLocked("");
                                }
                            }
                        }
                        if (curIm == null) {
                            changed = InputMethodManagerService.this.chooseNewDefaultIMELocked();
                        } else if (!changed && isPackageModified(curIm.getPackageName())) {
                            changed = true;
                        }
                        if (changed) {
                            InputMethodManagerService.this.updateFromSettingsLocked(false);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class MethodCallback extends IInputSessionCallback.Stub {
        private final InputChannel mChannel;
        private final IInputMethod mMethod;
        private final InputMethodManagerService mParentIMMS;

        MethodCallback(InputMethodManagerService imms, IInputMethod method, InputChannel channel) {
            this.mParentIMMS = imms;
            this.mMethod = method;
            this.mChannel = channel;
        }

        public void sessionCreated(IInputMethodSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mParentIMMS.onSessionCreated(this.mMethod, session, this.mChannel);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    public class HardKeyboardListener implements WindowManagerInternal.OnHardKeyboardStatusChangeListener {
        private HardKeyboardListener() {
        }

        public void onHardKeyboardStatusChange(boolean available) {
            InputMethodManagerService.this.mHandler.sendMessage(InputMethodManagerService.this.mHandler.obtainMessage(InputMethodManagerService.MSG_HARD_KEYBOARD_SWITCH_CHANGED, Integer.valueOf(available ? 1 : 0)));
        }

        public void handleHardKeyboardStatusChange(boolean available) {
            View switchSectionView;
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (!(InputMethodManagerService.this.mSwitchingDialog == null || InputMethodManagerService.this.mSwitchingDialogTitleView == null || !InputMethodManagerService.this.mSwitchingDialog.isShowing() || (switchSectionView = InputMethodManagerService.this.mSwitchingDialogTitleView.findViewById(16909007)) == null)) {
                    switchSectionView.setVisibility(available ? 0 : 8);
                }
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private InputMethodManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            HwServiceFactory.IHwInputMethodManagerService iinputmethodMS = HwServiceFactory.getHwInputMethodManagerService();
            if (iinputmethodMS != null) {
                this.mService = iinputmethodMS.getInstance(context);
            } else {
                this.mService = new InputMethodManagerService(context);
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.inputmethod.InputMethodManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.inputmethod.InputMethodManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            LocalServices.addService(InputMethodManagerInternal.class, new LocalServiceImpl(this.mService));
            publishBinderService("input_method", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemRunning((StatusBarManagerService) ServiceManager.getService("statusbar"));
            }
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            this.mService.mHandler.sendMessage(this.mService.mHandler.obtainMessage(InputMethodManagerService.MSG_SYSTEM_UNLOCK_USER, userHandle, 0));
        }
    }

    /* access modifiers changed from: package-private */
    public void onUnlockUser(int userId) {
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId == currentUserId) {
                this.mSettings.switchCurrentUser(currentUserId, !this.mSystemReady);
                if (this.mSystemReady) {
                    buildInputMethodListLocked(false);
                    updateInputMethodsFromSettingsLocked(true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public InputMethodManagerService(Context context) {
        this.mHwIMMSEx = HwServiceExFactory.getHwInputMethodManagerServiceEx(this, context);
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mContext = context;
        this.mRes = context.getResources();
        this.mHandler = new Handler(this);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mImeDisplayValidator = new ImeDisplayValidator() {
            /* class com.android.server.inputmethod.$$Lambda$InputMethodManagerService$oxpSIwENeEjKtHbxqUXuaXD0Gn8 */

            @Override // com.android.server.inputmethod.InputMethodManagerService.ImeDisplayValidator
            public final boolean displayCanShowIme(int i) {
                return InputMethodManagerService.this.lambda$new$0$InputMethodManagerService(i);
            }
        };
        this.mCaller = new HandlerCaller(context, (Looper) null, new HandlerCaller.Callback() {
            /* class com.android.server.inputmethod.InputMethodManagerService.AnonymousClass2 */

            public void executeMessage(Message msg) {
                InputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mHardKeyboardListener = new HardKeyboardListener();
        this.mHasFeature = context.getPackageManager().hasSystemFeature("android.software.input_methods");
        this.mSlotIme = this.mContext.getString(17041310);
        this.mHardKeyboardBehavior = this.mContext.getResources().getInteger(17694809);
        this.mIsLowRam = ActivityManager.isLowRamDeviceStatic();
        Bundle extras = new Bundle();
        extras.putBoolean("android.allowDuringSetup", true);
        this.mImeSwitcherNotification = new Notification.Builder(this.mContext, SystemNotificationChannels.VIRTUAL_KEYBOARD).setSmallIcon(17302750).setWhen(0).setOngoing(true).addExtras(extras).setCategory("sys").setColor(this.mContext.getColor(17170460));
        this.mImeSwitchPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_SHOW_INPUT_METHOD_PICKER).setPackage(this.mContext.getPackageName()), 0);
        this.mShowOngoingImeSwitcherForPhones = false;
        this.mNotificationShown = false;
        int userId = 0;
        try {
            userId = ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        this.mLastSwitchUserId = userId;
        this.mSettings = new InputMethodUtils.InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, userId, !this.mSystemReady);
        updateCurrentProfileIds();
        this.mSwitchingController = InputMethodSubtypeSwitchingController.createInstanceLocked(this.mSettings, context);
        createFlagIfNecessary(userId);
    }

    public /* synthetic */ boolean lambda$new$0$InputMethodManagerService(int displayId) {
        return this.mWindowManagerInternal.shouldShowIme(displayId);
    }

    private void resetDefaultImeLocked(Context context) {
    }

    @GuardedBy({"mMethodMap"})
    private void switchUserLocked(int newUserId) {
        createFlagIfNecessary(newUserId);
        this.mSettingsObserver.registerContentObserverLocked(newUserId);
        this.mSettings.switchCurrentUser(newUserId, !this.mSystemReady || !this.mUserManagerInternal.isUserUnlockingOrUnlocked(newUserId));
        updateCurrentProfileIds();
        boolean initialUserSwitch = TextUtils.isEmpty(this.mSettings.getSelectedInputMethod());
        this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
        if (this.mSystemReady) {
            hideCurrentInputLocked(0, null);
            resetCurrentMethodAndClient(6);
            buildInputMethodListLocked(initialUserSwitch);
            if (TextUtils.isEmpty(this.mSettings.getSelectedInputMethod())) {
                resetDefaultImeLocked(this.mContext);
            }
            updateFromSettingsLocked(true);
        }
        if (initialUserSwitch) {
            InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), newUserId, this.mContext.getBasePackageName());
        }
        this.mLastSwitchUserId = newUserId;
    }

    /* access modifiers changed from: package-private */
    public void updateCurrentProfileIds() {
        InputMethodUtils.InputMethodSettings inputMethodSettings = this.mSettings;
        inputMethodSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(inputMethodSettings.getCurrentUserId()));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        String[] imePkgName;
        synchronized (this.mMethodMap) {
            if (!this.mSystemReady) {
                this.mSystemReady = true;
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
                int currentUserId = this.mSettings.getCurrentUserId();
                boolean z = false;
                this.mSettings.switchCurrentUser(currentUserId, !this.mUserManagerInternal.isUserUnlockingOrUnlocked(currentUserId));
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                }
                updateSystemUiLocked(this.mImeWindowVis, this.mBackDisposition);
                this.mShowOngoingImeSwitcherForPhones = this.mRes.getBoolean(17891619);
                if (this.mShowOngoingImeSwitcherForPhones) {
                    this.mWindowManagerInternal.setOnHardKeyboardStatusChangeListener(this.mHardKeyboardListener);
                }
                this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
                this.mSettingsObserver.registerContentObserverLocked(currentUserId);
                IntentFilter broadcastFilterForSystemUser = new IntentFilter();
                broadcastFilterForSystemUser.addAction("android.intent.action.USER_ADDED");
                broadcastFilterForSystemUser.addAction("android.intent.action.USER_REMOVED");
                broadcastFilterForSystemUser.addAction("android.intent.action.LOCALE_CHANGED");
                broadcastFilterForSystemUser.addAction(ACTION_SHOW_INPUT_METHOD_PICKER);
                this.mContext.registerReceiver(new ImmsBroadcastReceiverForSystemUser(), broadcastFilterForSystemUser);
                IntentFilter broadcastFilterForAllUsers = new IntentFilter();
                broadcastFilterForAllUsers.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                this.mContext.registerReceiverAsUser(new ImmsBroadcastReceiverForAllUsers(), UserHandle.ALL, broadcastFilterForAllUsers, null, null);
                for (String str : new String[]{"com.baidu.input_huawei", "com.touchtype.swiftkey", "com.swiftkey.swiftkeyconfigurator"}) {
                    setDefaultImeEnable(str);
                }
                if (!(!TextUtils.isEmpty(this.mSettings.getSelectedInputMethod()))) {
                    z = true;
                }
                buildInputMethodListLocked(z);
                updateFromSettingsLocked(true);
                InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), currentUserId, this.mContext.getBasePackageName());
            }
        }
    }

    private void setDefaultImeEnable(String pkgImeName) {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            if (!(pm.getApplicationEnabledSetting(pkgImeName) == 1)) {
                Slog.i(TAG, "current default input disable,enable it");
                pm.setApplicationEnabledSetting(pkgImeName, 1, 0);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Unexpected exception" + e.getMessage());
        }
    }

    @GuardedBy({"mMethodMap"})
    private boolean calledFromValidUserLocked() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == 1000 || userId == this.mSettings.getCurrentUserId()) {
            return true;
        }
        if ((!InputMethodSystemProperty.PER_PROFILE_IME_ENABLED && this.mSettings.isCurrentProfile(userId)) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return false;
    }

    @GuardedBy({"mMethodMap"})
    private boolean calledWithValidTokenLocked(IBinder token) {
        if (token == null) {
            throw new InvalidParameterException("token must not be null.");
        } else if (token == this.mCurToken) {
            return true;
        } else {
            Slog.e(TAG, "Ignoring " + Debug.getCaller() + " due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
            return false;
        }
    }

    @GuardedBy({"mMethodMap"})
    private boolean bindCurrentInputMethodServiceLocked(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return false;
    }

    public List<InputMethodInfo> getInputMethodList(int userId) {
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
        }
        synchronized (this.mMethodMap) {
            int[] resolvedUserIds = InputMethodUtils.resolveUserId(userId, this.mSettings.getCurrentUserId(), null);
            if (resolvedUserIds.length != 1) {
                return Collections.emptyList();
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return getInputMethodListLocked(resolvedUserIds[0]);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodList(int userId) {
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
        }
        synchronized (this.mMethodMap) {
            int[] resolvedUserIds = InputMethodUtils.resolveUserId(userId, this.mSettings.getCurrentUserId(), null);
            if (resolvedUserIds.length != 1) {
                return Collections.emptyList();
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return getEnabledInputMethodListLocked(resolvedUserIds[0]);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    @GuardedBy({"mMethodMap"})
    private List<InputMethodInfo> getInputMethodListLocked(int userId) {
        ArrayList<InputMethodInfo> methodList;
        if (userId == this.mSettings.getCurrentUserId()) {
            methodList = new ArrayList<>(this.mMethodList);
        } else {
            ArrayMap<String, InputMethodInfo> methodMap = new ArrayMap<>();
            ArrayList<InputMethodInfo> methodList2 = new ArrayList<>();
            ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap = new ArrayMap<>();
            AdditionalSubtypeUtils.load(additionalSubtypeMap, userId);
            queryInputMethodServicesInternal(this.mContext, userId, additionalSubtypeMap, methodMap, methodList2);
            methodList = methodList2;
        }
        Iterator<InputMethodInfo> it = methodList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            InputMethodInfo inputMethodInfo = it.next();
            if (REMOTE_INPUT_METHOD_ID.equals(inputMethodInfo.getId())) {
                Slog.d(TAG, "hw remote inputmethod only available for windows cast, not show for user.");
                methodList.remove(inputMethodInfo);
                break;
            }
        }
        return methodList;
    }

    @GuardedBy({"mMethodMap"})
    private List<InputMethodInfo> getEnabledInputMethodListLocked(int userId) {
        if (userId == this.mSettings.getCurrentUserId()) {
            return this.mSettings.getEnabledInputMethodListLocked();
        }
        ArrayMap<String, InputMethodInfo> methodMap = new ArrayMap<>();
        ArrayList<InputMethodInfo> methodList = new ArrayList<>();
        ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap = new ArrayMap<>();
        AdditionalSubtypeUtils.load(additionalSubtypeMap, userId);
        queryInputMethodServicesInternal(this.mContext, userId, additionalSubtypeMap, methodMap, methodList);
        return new InputMethodUtils.InputMethodSettings(this.mContext.getResources(), this.mContext.getContentResolver(), methodMap, userId, true).getEnabledInputMethodListLocked();
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        int callingUserId = UserHandle.getCallingUserId();
        synchronized (this.mMethodMap) {
            int[] resolvedUserIds = InputMethodUtils.resolveUserId(callingUserId, this.mSettings.getCurrentUserId(), null);
            if (resolvedUserIds.length != 1) {
                return Collections.emptyList();
            }
            long ident = Binder.clearCallingIdentity();
            try {
                return getEnabledInputMethodSubtypeListLocked(imiId, allowsImplicitlySelectedSubtypes, resolvedUserIds[0]);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    @GuardedBy({"mMethodMap"})
    private List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(String imiId, boolean allowsImplicitlySelectedSubtypes, int userId) {
        InputMethodInfo imi;
        String str;
        if (userId == this.mSettings.getCurrentUserId()) {
            if (imiId != null || (str = this.mCurMethodId) == null) {
                imi = this.mMethodMap.get(imiId);
            } else {
                imi = this.mMethodMap.get(str);
            }
            if (imi == null) {
                return Collections.emptyList();
            }
            return this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, allowsImplicitlySelectedSubtypes);
        }
        ArrayMap<String, InputMethodInfo> methodMap = new ArrayMap<>();
        ArrayList<InputMethodInfo> methodList = new ArrayList<>();
        ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap = new ArrayMap<>();
        AdditionalSubtypeUtils.load(additionalSubtypeMap, userId);
        queryInputMethodServicesInternal(this.mContext, userId, additionalSubtypeMap, methodMap, methodList);
        InputMethodInfo imi2 = methodMap.get(imiId);
        if (imi2 == null) {
            return Collections.emptyList();
        }
        return new InputMethodUtils.InputMethodSettings(this.mContext.getResources(), this.mContext.getContentResolver(), methodMap, userId, true).getEnabledInputMethodSubtypeListLocked(this.mContext, imi2, allowsImplicitlySelectedSubtypes);
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int selfReportedDisplayId) {
        RemoteException e;
        int callerUid = Binder.getCallingUid();
        int callerPid = Binder.getCallingPid();
        synchronized (this.mMethodMap) {
            try {
                int numClients = this.mClients.size();
                for (int i = 0; i < numClients; i++) {
                    ClientState state = this.mClients.valueAt(i);
                    if (state.uid == callerUid && state.pid == callerPid) {
                        if (state.selfReportedDisplayId == selfReportedDisplayId) {
                            throw new SecurityException("uid=" + callerUid + "/pid=" + callerPid + "/displayId=" + selfReportedDisplayId + " is already registered.");
                        }
                    }
                }
                try {
                    ClientDeathRecipient deathRecipient = new ClientDeathRecipient(this, client);
                    try {
                        client.asBinder().linkToDeath(deathRecipient, 0);
                        this.mClients.put(client.asBinder(), new ClientState(client, inputContext, callerUid, callerPid, selfReportedDisplayId, deathRecipient));
                    } catch (RemoteException e2) {
                        throw new IllegalStateException(e2);
                    }
                } catch (Throwable th) {
                    e = th;
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeClient(IInputMethodClient client) {
        synchronized (this.mMethodMap) {
            ClientState cs = this.mClients.remove(client.asBinder());
            if (cs != null) {
                client.asBinder().unlinkToDeath(cs.clientDeathRecipient, 0);
                clearClientSessionLocked(cs);
                for (int i = this.mActivityViewDisplayIdToParentMap.size() - 1; i >= 0; i--) {
                    if (this.mActivityViewDisplayIdToParentMap.valueAt(i).mParentClient == cs) {
                        this.mActivityViewDisplayIdToParentMap.removeAt(i);
                    }
                }
                if (this.mCurClient == cs) {
                    if (this.mBoundToMethod) {
                        this.mBoundToMethod = false;
                        if (this.mCurMethod != null) {
                            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(1000, this.mCurMethod));
                        }
                    }
                    this.mCurClient = null;
                    this.mCurActivityViewToScreenMatrix = null;
                }
                if (this.mCurFocusedWindowClient == cs) {
                    this.mCurFocusedWindowClient = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void executeOrSendMessage(IInterface target, Message msg) {
        if (target.asBinder() instanceof Binder) {
            this.mCaller.sendMessage(msg);
            return;
        }
        handleMessage(msg);
        msg.recycle();
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentClientLocked(int unbindClientReason) {
        if (this.mCurClient != null) {
            if (this.mBoundToMethod) {
                this.mBoundToMethod = false;
                IInputMethod iInputMethod = this.mCurMethod;
                if (iInputMethod != null) {
                    executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageO(1000, iInputMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO((int) MSG_SET_ACTIVE, 0, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO((int) MSG_UNBIND_CLIENT, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = false;
            this.mCurClient = null;
            this.mCurActivityViewToScreenMatrix = null;
            hideInputMethodMenuLocked();
        }
    }

    private int getImeShowFlags() {
        if (this.mShowForced) {
            return 0 | 3;
        }
        if (this.mShowExplicitlyRequested) {
            return 0 | 1;
        }
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 0 | 2;
        }
        if (!this.mShowExplicitlyRequested) {
            return 0 | 1;
        }
        return 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r21v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    @GuardedBy({"mMethodMap"})
    public InputBindResult attachNewInputLocked(int startInputReason, boolean initial) {
        if (!this.mBoundToMethod) {
            IInputMethod iInputMethod = this.mCurMethod;
            executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageOO((int) MSG_BIND_INPUT, iInputMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        Binder startInputToken = new Binder();
        StartInputInfo info = new StartInputInfo(this.mSettings.getCurrentUserId(), this.mCurToken, this.mCurTokenDisplayId, this.mCurId, startInputReason, !initial ? 1 : 0, UserHandle.getUserId(this.mCurClient.uid), this.mCurClient.selfReportedDisplayId, this.mCurFocusedWindow, this.mCurAttribute, this.mCurFocusedWindowSoftInputMode, this.mCurSeq);
        this.mImeTargetWindowMap.put(startInputToken, this.mCurFocusedWindow);
        this.mStartInputHistory.addEntry(info);
        SessionState session = this.mCurClient.curSession;
        executeOrSendMessage(session.method, this.mCaller.obtainMessageIIOOOO(2000, this.mCurInputContextMissingMethods, !initial, startInputToken, session, this.mCurInputContext, this.mCurAttribute));
        InputChannel inputChannel = null;
        if (this.mShowRequested) {
            if (DEBUG_FLOW) {
                Slog.v(TAG, "Attach new input asks to show input");
            }
            showCurrentInputLocked(getAppShowFlags(), null);
        }
        IInputMethodSession iInputMethodSession = session.session;
        if (session.channel != null) {
            inputChannel = session.channel.dup();
        }
        return new InputBindResult(0, iInputMethodSession, inputChannel, this.mCurId, this.mCurSeq, this.mCurActivityViewToScreenMatrix);
    }

    private Matrix getActivityViewToScreenMatrixLocked(int clientDisplayId, int imeDisplayId) {
        if (clientDisplayId == imeDisplayId) {
            return null;
        }
        int displayId = clientDisplayId;
        Matrix matrix = null;
        while (true) {
            ActivityViewInfo info = this.mActivityViewDisplayIdToParentMap.get(displayId);
            if (info == null) {
                return null;
            }
            if (matrix == null) {
                matrix = new Matrix(info.mMatrix);
            } else {
                matrix.postConcat(info.mMatrix);
            }
            if (info.mParentClient.selfReportedDisplayId == imeDisplayId) {
                return matrix;
            }
            displayId = info.mParentClient.selfReportedDisplayId;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mMethodMap"})
    public InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int startInputFlags, int startInputReason) {
        int missingMethods2;
        String str = this.mCurMethodId;
        if (str == null) {
            return InputBindResult.NO_IME;
        }
        if (!this.mSystemReady) {
            return new InputBindResult(7, (IInputMethodSession) null, (InputChannel) null, str, this.mCurSeq, (Matrix) null);
        }
        if (!InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, cs.uid, attribute.packageName)) {
            Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + cs.uid + " package=" + attribute.packageName);
            return InputBindResult.INVALID_PACKAGE_NAME;
        } else if (!this.mWindowManagerInternal.isUidAllowedOnDisplay(cs.selfReportedDisplayId, cs.uid)) {
            return InputBindResult.INVALID_DISPLAY_ID;
        } else {
            int displayIdToShowIme = computeImeDisplayIdForTarget(cs.selfReportedDisplayId, this.mImeDisplayValidator);
            if (this.mCurClient != cs) {
                this.mCurClientInKeyguard = isKeyguardLocked();
                unbindCurrentClientLocked(1);
                if (this.mIsInteractive) {
                    executeOrSendMessage(cs.client, this.mCaller.obtainMessageIO((int) MSG_SET_ACTIVE, 1, cs));
                }
            }
            this.mCurSeq++;
            if (this.mCurSeq <= 0) {
                this.mCurSeq = 1;
            }
            this.mCurClient = cs;
            this.mCurInputContext = inputContext;
            this.mCurActivityViewToScreenMatrix = getActivityViewToScreenMatrixLocked(cs.selfReportedDisplayId, displayIdToShowIme);
            if (cs.selfReportedDisplayId == displayIdToShowIme || this.mCurActivityViewToScreenMatrix != null) {
                missingMethods2 = missingMethods;
            } else {
                missingMethods2 = missingMethods | 8;
            }
            this.mCurInputContextMissingMethods = missingMethods2;
            this.mCurAttribute = attribute;
            String str2 = this.mCurId;
            boolean z = false;
            if (str2 != null && str2.equals(this.mCurMethodId) && displayIdToShowIme == this.mCurTokenDisplayId) {
                if (cs.curSession != null) {
                    if ((startInputFlags & 8) != 0) {
                        z = true;
                    }
                    return attachNewInputLocked(startInputReason, z);
                } else if (this.mHaveConnection) {
                    if (this.mCurMethod != null) {
                        requestClientSessionLocked(cs);
                        return new InputBindResult(1, (IInputMethodSession) null, (InputChannel) null, this.mCurId, this.mCurSeq, (Matrix) null);
                    } else if (SystemClock.uptimeMillis() < this.mLastBindTime + 3000) {
                        return new InputBindResult(2, (IInputMethodSession) null, (InputChannel) null, this.mCurId, this.mCurSeq, (Matrix) null);
                    } else {
                        EventLog.writeEvent((int) EventLogTags.IMF_FORCE_RECONNECT_IME, this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 0);
                    }
                }
            }
            InputMethodInfo info = this.mMethodMap.get(this.mCurMethodId);
            if (info != null) {
                unbindCurrentMethodLocked();
                this.mCurIntent = new Intent("android.view.InputMethod");
                this.mCurIntent.setComponent(info.getComponent());
                this.mCurIntent.putExtra("android.intent.extra.client_label", 17040297);
                this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
                if (bindCurrentInputMethodServiceLocked(this.mCurIntent, this, IME_CONNECTION_BIND_FLAGS)) {
                    this.mLastBindTime = SystemClock.uptimeMillis();
                    this.mHaveConnection = true;
                    this.mCurId = info.getId();
                    this.mCurToken = new Binder();
                    this.mCurTokenDisplayId = displayIdToShowIme;
                    try {
                        this.mIWindowManager.addWindowToken(this.mCurToken, 2011, this.mCurTokenDisplayId);
                    } catch (RemoteException e) {
                    }
                    return new InputBindResult(2, (IInputMethodSession) null, (InputChannel) null, this.mCurId, this.mCurSeq, (Matrix) null);
                }
                this.mCurIntent = null;
                Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
                return InputBindResult.IME_NOT_CONNECTED;
            }
            throw new IllegalArgumentException("Unknown id: " + this.mCurMethodId);
        }
    }

    static int computeImeDisplayIdForTarget(int displayId, ImeDisplayValidator checker) {
        if (displayId == 0 || displayId == -1) {
            return 0;
        }
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            if (checker.displayCanShowIme(displayId)) {
                return displayId;
            }
            return 0;
        } else if (((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).isHardKeyboardAvailable() || HwPCUtils.mTouchDeviceID != -1 || HwPCUtils.enabledInPad()) {
            return displayId;
        } else {
            return 0;
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked();
                    return;
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Initiating attach with token: " + this.mCurToken);
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO((int) MSG_INITIALIZE_IME, this.mCurTokenDisplayId, this.mCurMethod, this.mCurToken));
                if (this.mCurClient != null) {
                    clearClientSessionLocked(this.mCurClient);
                    requestClientSessionLocked(this.mCurClient);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod == null || method == null || this.mCurMethod.asBinder() != method.asBinder() || this.mCurClient == null) {
                channel.dispose();
                return;
            }
            if (DEBUG_FLOW) {
                Slog.v(TAG, "IME session created");
            }
            clearClientSessionLocked(this.mCurClient);
            this.mCurClient.curSession = new SessionState(this.mCurClient, method, session, channel);
            InputBindResult res = attachNewInputLocked(9, true);
            if (res.method != null) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(3010, this.mCurClient.client, res));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentMethodLocked() {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = false;
        }
        IBinder iBinder = this.mCurToken;
        if (iBinder != null) {
            try {
                this.mIWindowManager.removeWindowToken(iBinder, this.mCurTokenDisplayId);
            } catch (RemoteException e) {
            }
            this.mImeWindowVis = 0;
            this.mBackDisposition = 0;
            updateSystemUiLocked(this.mImeWindowVis, this.mBackDisposition);
            this.mCurToken = null;
            this.mCurTokenDisplayId = -1;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    /* access modifiers changed from: package-private */
    public void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked();
        unbindCurrentClientLocked(unbindClientReason);
    }

    /* access modifiers changed from: package-private */
    public void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            IInputMethod iInputMethod = this.mCurMethod;
            executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageOOO((int) MSG_CREATE_SESSION, iInputMethod, channels[1], new MethodCallback(this, iInputMethod, channels[0])));
        }
    }

    /* access modifiers changed from: package-private */
    public void clearClientSessionLocked(ClientState cs) {
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = false;
    }

    private void finishSessionLocked(SessionState sessionState) {
        if (sessionState != null) {
            if (sessionState.session != null) {
                try {
                    sessionState.session.finishSession();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Session failed to close due to remote exception", e);
                    updateSystemUiLocked(0, this.mBackDisposition);
                }
                sessionState.session = null;
            }
            if (sessionState.channel != null) {
                sessionState.channel.dispose();
                sessionState.channel = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearCurMethodLocked() {
        if (this.mCurMethod != null) {
            int numClients = this.mClients.size();
            for (int i = 0; i < numClients; i++) {
                clearClientSessionLocked(this.mClients.valueAt(i));
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        StatusBarManagerService statusBarManagerService = this.mStatusBar;
        if (statusBarManagerService != null) {
            statusBarManagerService.setIconVisibility(this.mSlotIme, false);
        }
        this.mInFullscreenMode = false;
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = false;
                unbindCurrentClientLocked(3);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStatusIcon(IBinder token, String packageName, int iconId) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                long ident = Binder.clearCallingIdentity();
                if (iconId == 0) {
                    try {
                        if (this.mStatusBar != null) {
                            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                        }
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } else if (packageName != null) {
                    CharSequence contentDescription = null;
                    try {
                        contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                    } catch (RemoteException e) {
                    }
                    if (this.mStatusBar != null) {
                        this.mStatusBar.setIcon(this.mSlotIme, packageName, iconId, 0, contentDescription != null ? contentDescription.toString() : null);
                        this.mStatusBar.setIconVisibility(this.mSlotIme, true);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        KeyguardManager keyguardManager;
        if (!(this.mShowOngoingImeSwitcherForPhones && this.mSwitchingDialog == null)) {
            return false;
        }
        if ((this.mWindowManagerInternal.isKeyguardShowingAndNotOccluded() && (keyguardManager = this.mKeyguardManager) != null && keyguardManager.isKeyguardSecure()) || (visibility & 1) == 0 || (visibility & 4) != 0) {
            return false;
        }
        if (this.mWindowManagerInternal.isHardKeyboardAvailable()) {
            if (this.mHardKeyboardBehavior == 0) {
                return true;
            }
        } else if ((visibility & 2) == 0) {
            return false;
        }
        List<InputMethodInfo> imis = this.mSettings.getEnabledInputMethodListLocked();
        int N = imis.size();
        if (N > 2) {
            return true;
        }
        if (N < 1) {
            return false;
        }
        int nonAuxCount = 0;
        int auxCount = 0;
        InputMethodSubtype nonAuxSubtype = null;
        InputMethodSubtype auxSubtype = null;
        for (int i = 0; i < N; i++) {
            List<InputMethodSubtype> subtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imis.get(i), true);
            int subtypeCount = subtypes.size();
            if (subtypeCount == 0) {
                nonAuxCount++;
            } else {
                for (int j = 0; j < subtypeCount; j++) {
                    InputMethodSubtype subtype = subtypes.get(j);
                    if (!subtype.isAuxiliary()) {
                        nonAuxCount++;
                        nonAuxSubtype = subtype;
                    } else {
                        auxCount++;
                        auxSubtype = subtype;
                    }
                }
            }
        }
        if (nonAuxCount > 1 || auxCount > 1) {
            return true;
        }
        if (!(nonAuxCount == 1 && auxCount == 1)) {
            return false;
        }
        if (nonAuxSubtype == null || auxSubtype == null || ((!nonAuxSubtype.getLocale().equals(auxSubtype.getLocale()) && !auxSubtype.overridesImplicitlyEnabledSubtype() && !nonAuxSubtype.overridesImplicitlyEnabledSubtype()) || !nonAuxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER))) {
            return true;
        }
        return false;
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = this.mKeyguardManager;
        return keyguardManager != null && keyguardManager.isKeyguardLocked();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        boolean dismissImeOnBackKeyPressed;
        int topFocusedDisplayId = this.mWindowManagerInternal.getTopFocusedDisplayId();
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (this.mCurTokenDisplayId == topFocusedDisplayId || this.mCurTokenDisplayId == 0) {
                    this.mImeWindowVis = vis;
                    this.mBackDisposition = backDisposition;
                    updateSystemUiLocked(vis, backDisposition);
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        boolean z = false;
        if (backDisposition == 1) {
            dismissImeOnBackKeyPressed = false;
        } else if (backDisposition != 2) {
            dismissImeOnBackKeyPressed = (vis & 2) != 0;
        } else {
            dismissImeOnBackKeyPressed = true;
        }
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if ((vis & 2) != 0) {
            z = true;
        }
        windowManagerInternal.updateInputMethodWindowStatus(token, z, dismissImeOnBackKeyPressed);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportStartInput(IBinder token, IBinder startInputToken) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                IBinder targetWindow = this.mImeTargetWindowMap.get(startInputToken);
                if (!(targetWindow == null || this.mLastImeTargetWindow == targetWindow)) {
                    this.mWindowManagerInternal.updateInputMethodTargetWindow(token, targetWindow);
                }
                this.mLastImeTargetWindow = targetWindow;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSystemUiLocked(int vis, int backDisposition) {
        if (this.mCurToken != null) {
            long ident = Binder.clearCallingIdentity();
            if (vis != 0) {
                try {
                    if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
                        vis = 0;
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            boolean needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis);
            if (this.mStatusBar != null) {
                this.mStatusBar.setImeWindowStatus(this.mCurTokenDisplayId, this.mCurToken, vis, backDisposition, needsToShowImeSwitcher);
            }
            InputMethodInfo imi = this.mMethodMap.get(this.mCurMethodId);
            if (imi != null && needsToShowImeSwitcher) {
                CharSequence title = this.mRes.getText(17041185);
                this.mImeSwitcherNotification.setContentTitle(title).setContentText(InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, imi, this.mCurrentSubtype)).setContentIntent(this.mImeSwitchPendingIntent);
                try {
                    if (this.mNotificationManager != null && (!this.mIWindowManager.hasNavigationBar(0) || !this.mHwIMMSEx.isTriNavigationBar(this.mContext))) {
                        this.mNotificationManager.notifyAsUser(null, 8, this.mImeSwitcherNotification.build(), UserHandle.ALL);
                        this.mNotificationShown = true;
                    }
                } catch (RemoteException e) {
                }
            } else if (this.mNotificationShown && this.mNotificationManager != null) {
                this.mNotificationManager.cancelAsUser(null, 8, UserHandle.ALL);
                this.mNotificationShown = false;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateFromSettingsLocked(boolean enabledMayChange) {
        updateInputMethodsFromSettingsLocked(enabledMayChange);
        updateKeyboardFromSettingsLocked();
    }

    /* access modifiers changed from: package-private */
    public void updateInputMethodsFromSettingsLocked(boolean enabledMayChange) {
        if (enabledMayChange) {
            List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
            for (int i = 0; i < enabled.size(); i++) {
                InputMethodInfo imm = enabled.get(i);
                try {
                    ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(imm.getPackageName(), 32768, this.mSettings.getCurrentUserId());
                    if (ai != null && ai.enabledSetting == 4) {
                        this.mIPackageManager.setApplicationEnabledSetting(imm.getPackageName(), 0, 1, this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
                    }
                } catch (RemoteException e) {
                }
            }
        }
        String id = this.mSettings.getSelectedInputMethod();
        if (TextUtils.isEmpty(id) && chooseNewDefaultIMELocked()) {
            id = this.mSettings.getSelectedInputMethod();
        }
        if (!TextUtils.isEmpty(id)) {
            try {
                setInputMethodLocked(id, this.mSettings.getSelectedInputMethodSubtypeId(id));
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Unknown input method from prefs: " + id, e2);
                resetCurrentMethodAndClient(5);
            }
        } else {
            resetCurrentMethodAndClient(4);
        }
        this.mSwitchingController.resetCircularListLocked(this.mContext);
    }

    public void updateKeyboardFromSettingsLocked() {
        Switch hardKeySwitch;
        this.mShowImeWithHardKeyboard = this.mSettings.isShowImeWithHardKeyboardEnabled();
        AlertDialog alertDialog = this.mSwitchingDialog;
        if (alertDialog != null && this.mSwitchingDialogTitleView != null && alertDialog.isShowing() && (hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(16909008)) != null) {
            hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
        }
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodLocked(String id, int subtypeId) {
        InputMethodSubtype newSubtype;
        InputMethodInfo info = this.mMethodMap.get(id);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + id);
        } else if (id.equals(this.mCurMethodId)) {
            int subtypeCount = info.getSubtypeCount();
            if (subtypeCount > 0) {
                InputMethodSubtype oldSubtype = this.mCurrentSubtype;
                if (subtypeId < 0 || subtypeId >= subtypeCount) {
                    newSubtype = getCurrentInputMethodSubtypeLocked();
                } else {
                    newSubtype = info.getSubtypeAt(subtypeId);
                }
                if (newSubtype == null || oldSubtype == null) {
                    Slog.w(TAG, "Illegal subtype state: old subtype = " + oldSubtype + ", new subtype = " + newSubtype);
                } else if (newSubtype != oldSubtype) {
                    setSelectedInputMethodAndSubtypeLocked(info, subtypeId, true);
                    if (this.mCurMethod != null) {
                        try {
                            updateSystemUiLocked(this.mImeWindowVis, this.mBackDisposition);
                            this.mCurMethod.changeInputMethodSubtype(newSubtype);
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Failed to call changeInputMethodSubtype");
                        }
                    }
                }
            }
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                setSelectedInputMethodAndSubtypeLocked(info, subtypeId, false);
                this.mCurMethodId = id;
                if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady()) {
                    Intent intent = new Intent("android.intent.action.INPUT_METHOD_CHANGED");
                    intent.addFlags(536870912);
                    intent.putExtra("input_method_id", id);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                }
                unbindCurrentClientLocked(2);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        int uid = Binder.getCallingUid();
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) {
                    ClientState cs = this.mClients.get(client.asBinder());
                    if (cs == null) {
                        throw new IllegalArgumentException("unknown client " + client.asBinder());
                    } else if (!this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
                        Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be shown, requestedUid = " + uid);
                }
                boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return showCurrentInputLocked;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mMethodMap"})
    public boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mShowRequested = true;
        if (this.mAccessibilityRequestingNoSoftKeyboard) {
            return false;
        }
        if ((flags & 2) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForced = true;
        } else if ((flags & 1) == 0) {
            this.mShowExplicitlyRequested = true;
        }
        if (!this.mSystemReady) {
            return false;
        }
        if (!this.mIsInteractive && IS_TV) {
            return false;
        }
        if (this.mWindowManagerInternal.isPcFreeFormWinClient(this.mCurFocusedWindow)) {
            Slog.v(TAG, "needn't show IME when pc multi-window mode");
            return false;
        } else if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
            }
            this.mWindowManagerInternal.setImeHolder(this.mCurFocusedWindow);
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO((int) MSG_SHOW_SOFT_INPUT, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                bindCurrentInputMethodServiceLocked(this.mCurIntent, this.mVisibleConnection, IME_VISIBLE_BIND_FLAGS);
                this.mVisibleBound = true;
            }
            return true;
        } else if (!this.mHaveConnection || SystemClock.uptimeMillis() < this.mLastBindTime + 3000) {
            return false;
        } else {
            EventLog.writeEvent((int) EventLogTags.IMF_FORCE_RECONNECT_IME, this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 1);
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodServiceLocked(this.mCurIntent, this, IME_CONNECTION_BIND_FLAGS);
            return false;
        }
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        Binder.getCallingUid();
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) {
                    ClientState cs = this.mClients.get(client.asBinder());
                    if (cs == null) {
                        throw new IllegalArgumentException("unknown client " + client.asBinder());
                    } else if (!this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be hidden");
                }
                boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return hideCurrentInputLocked;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        boolean res;
        if ((flags & 1) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            return false;
        }
        if (this.mShowForced && (flags & 2) != 0) {
            return false;
        }
        boolean shouldHideSoftInput = true;
        if (this.mCurMethod == null || (!this.mInputShown && (this.mImeWindowVis & 1) == 0)) {
            shouldHideSoftInput = false;
        }
        if (shouldHideSoftInput) {
            IInputMethod iInputMethod = this.mCurMethod;
            executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageOO((int) MSG_HIDE_SOFT_INPUT, iInputMethod, resultReceiver));
            res = true;
        } else {
            res = false;
        }
        if (this.mHaveConnection && this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        this.mInputShown = false;
        this.mShowRequested = false;
        this.mShowExplicitlyRequested = false;
        this.mShowForced = false;
        return res;
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        int userId;
        ArrayMap<String, InputMethodInfo> arrayMap;
        Throwable th;
        InputBindResult result;
        if (windowToken == null) {
            Slog.e(TAG, "windowToken cannot be null.");
            return InputBindResult.NULL;
        }
        int callingUserId = UserHandle.getCallingUserId();
        if (attribute == null || attribute.targetInputMethodUser == null || attribute.targetInputMethodUser.getIdentifier() == callingUserId) {
            userId = callingUserId;
        } else {
            this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "Using EditorInfo.targetInputMethodUser requires INTERACT_ACROSS_USERS_FULL.");
            int userId2 = attribute.targetInputMethodUser.getIdentifier();
            if (!this.mUserManagerInternal.isUserRunning(userId2)) {
                Slog.e(TAG, "User #" + userId2 + " is not running.");
                return InputBindResult.INVALID_USER;
            }
            userId = userId2;
        }
        ArrayMap<String, InputMethodInfo> arrayMap2 = this.mMethodMap;
        synchronized (arrayMap2) {
            try {
                long ident = Binder.clearCallingIdentity();
                arrayMap = arrayMap2;
                try {
                    result = startInputOrWindowGainedFocusInternalLocked(startInputReason, client, windowToken, startInputFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion, userId);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
                try {
                    if (result != null) {
                        return result;
                    }
                    Slog.wtf(TAG, "InputBindResult is @NonNull. startInputReason=" + InputMethodDebug.startInputReasonToString(startInputReason) + " windowFlags=#" + Integer.toHexString(windowFlags) + " editorInfo=" + attribute);
                    return InputBindResult.NULL;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                arrayMap = arrayMap2;
                throw th;
            }
        }
    }

    private InputBindResult startInputOrWindowGainedFocusInternalLocked(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion, int userId) {
        int i;
        int windowDisplayId = this.mWindowManagerInternal.getDisplayIdForWindow(windowToken);
        ClientState cs = this.mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client " + client.asBinder());
        } else if (cs.selfReportedDisplayId != windowDisplayId) {
            Slog.e(TAG, "startInputOrWindowGainedFocusInternal: display ID mismatch. from client:" + cs.selfReportedDisplayId + " from window:" + windowDisplayId);
            return InputBindResult.DISPLAY_ID_MISMATCH;
        } else if (!this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
            return InputBindResult.NOT_IME_TARGET_WINDOW;
        } else {
            if (!this.mSettings.isCurrentProfile(userId)) {
                Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                Slog.w(TAG, "If you need to impersonate a foreground user/profile from a background user, use EditorInfo.targetInputMethodUser with INTERACT_ACROSS_USERS_FULL permission.");
                hideCurrentInputLocked(0, null);
                return InputBindResult.INVALID_USER;
            }
            if (InputMethodSystemProperty.PER_PROFILE_IME_ENABLED && userId != this.mSettings.getCurrentUserId() && !UserHandle.isClonedProfile(userId) && !this.mUserManagerInternal.isClonedProfile(userId)) {
                switchUserLocked(userId);
            }
            cs.shouldPreRenderIme = DebugFlags.FLAG_PRE_RENDER_IME_VIEWS.value() && !this.mIsLowRam;
            if (this.mCurFocusedWindow != windowToken) {
                this.mCurFocusedWindow = windowToken;
                this.mCurFocusedWindowSoftInputMode = softInputMode;
                this.mCurFocusedWindowClient = cs;
                boolean doAutoShow = (softInputMode & 240) == 16 || this.mRes.getConfiguration().isLayoutSizeAtLeast(3);
                boolean isTextEditor = (startInputFlags & 2) != 0;
                boolean didStart = false;
                InputBindResult res = null;
                int i2 = softInputMode & 15;
                if (i2 != 0) {
                    if (i2 != 1) {
                        if (i2 != 2) {
                            if (i2 == 3) {
                                if (DEBUG_FLOW) {
                                    Slog.v(TAG, "Window asks to hide input");
                                }
                                hideCurrentInputLocked(0, null);
                            } else if (i2 != 4) {
                                if (i2 == 5) {
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to always show input");
                                    }
                                    if (InputMethodUtils.isSoftInputModeStateVisibleAllowed(unverifiedTargetSdkVersion, startInputFlags)) {
                                        if (attribute != null) {
                                            i = 1;
                                            res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                                            didStart = true;
                                        } else {
                                            i = 1;
                                        }
                                        showCurrentInputLocked(i, null);
                                    } else {
                                        Slog.e(TAG, "SOFT_INPUT_STATE_ALWAYS_VISIBLE is ignored because there is no focused view that also returns true from View#onCheckIsTextEditor()");
                                    }
                                }
                            } else if ((softInputMode & 256) != 0) {
                                if (DEBUG_FLOW) {
                                    Slog.v(TAG, "Window asks to show input going forward");
                                }
                                if (InputMethodUtils.isSoftInputModeStateVisibleAllowed(unverifiedTargetSdkVersion, startInputFlags)) {
                                    if (attribute != null) {
                                        res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                                        didStart = true;
                                    }
                                    showCurrentInputLocked(1, null);
                                } else {
                                    Slog.e(TAG, "SOFT_INPUT_STATE_VISIBLE is ignored because there is no focused view that also returns true from View#onCheckIsTextEditor()");
                                }
                            }
                        } else if ((softInputMode & 256) != 0) {
                            if (DEBUG_FLOW) {
                                Slog.v(TAG, "Window asks to hide input going forward");
                            }
                            hideCurrentInputLocked(0, null);
                        }
                    }
                } else if (!isTextEditor || !doAutoShow) {
                    if (WindowManager.LayoutParams.mayUseInputMethod(windowFlags)) {
                        if (DEBUG_FLOW) {
                            Slog.v(TAG, "Unspecified window will hide input");
                        }
                        hideCurrentInputLocked(2, null);
                        if (cs.selfReportedDisplayId != this.mCurTokenDisplayId) {
                            unbindCurrentMethodLocked();
                        }
                    }
                } else if (isTextEditor && doAutoShow && (softInputMode & 256) != 0) {
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "Unspecified window will show input");
                    }
                    if (attribute != null) {
                        res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                        didStart = true;
                    }
                    showCurrentInputLocked(1, null);
                }
                if (didStart) {
                    return res;
                }
                if (attribute == null) {
                    return InputBindResult.NULL_EDITOR_INFO;
                }
                if (!DebugFlags.FLAG_OPTIMIZE_START_INPUT.value() || (startInputFlags & 2) != 0) {
                    return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                }
                return InputBindResult.NO_EDITOR;
            } else if (attribute != null) {
                return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
            } else {
                return new InputBindResult(3, (IInputMethodSession) null, (InputChannel) null, (String) null, -1, (Matrix) null);
            }
        }
    }

    private boolean canShowInputMethodPickerLocked(IInputMethodClient client) {
        int uid = Binder.getCallingUid();
        ClientState clientState = this.mCurFocusedWindowClient;
        if (clientState != null && client != null && clientState.client.asBinder() == client.asBinder()) {
            return true;
        }
        Intent intent = this.mCurIntent;
        if (intent == null || !InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, uid, intent.getComponent().getPackageName())) {
            return false;
        }
        return true;
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
        synchronized (this.mMethodMap) {
            if (calledFromValidUserLocked()) {
                if (!canShowInputMethodPickerLocked(client)) {
                    Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid " + Binder.getCallingUid() + ": " + client);
                    return;
                }
                Slog.d(TAG, Binder.getCallingPid() + ":request to show input method dialog");
                this.mHandler.sendMessage(this.mCaller.obtainMessageII(1, auxiliarySubtypeMode, this.mCurClient != null ? this.mCurClient.selfReportedDisplayId : 0));
            }
        }
    }

    public void showInputMethodPickerFromSystem(IInputMethodClient client, int auxiliarySubtypeMode, int displayId) {
        if (this.mContext.checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
            this.mHandler.sendMessage(this.mCaller.obtainMessageII(1, auxiliarySubtypeMode, displayId));
            return;
        }
        throw new SecurityException("showInputMethodPickerFromSystem requires WRITE_SECURE_SETTINGS permission");
    }

    public boolean isInputMethodPickerShownForTest() {
        synchronized (this.mMethodMap) {
            if (this.mSwitchingDialog == null) {
                return false;
            }
            return this.mSwitchingDialog.isShowing();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setInputMethod(IBinder token, String id) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                setInputMethodWithSubtypeIdLocked(token, id, -1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (subtype != null) {
                    setInputMethodWithSubtypeIdLocked(token, id, InputMethodUtils.getSubtypeIdFromHashCode(this.mMethodMap.get(id), subtype.hashCode()));
                } else {
                    setInputMethod(token, id);
                }
            }
        }
    }

    public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
        synchronized (this.mMethodMap) {
            if (calledFromValidUserLocked()) {
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(2, inputMethodId));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean switchToPreviousInputMethod(IBinder token) {
        InputMethodInfo lastImi;
        List<InputMethodInfo> enabled;
        String locale;
        InputMethodSubtype keyboardSubtype;
        int currentSubtypeHash;
        synchronized (this.mMethodMap) {
            try {
                if (!calledWithValidTokenLocked(token)) {
                    return false;
                }
                Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
                if (lastIme != null) {
                    lastImi = this.mMethodMap.get(lastIme.first);
                } else {
                    lastImi = null;
                }
                String targetLastImiId = null;
                int subtypeId = -1;
                if (!(lastIme == null || lastImi == null)) {
                    boolean imiIdIsSame = lastImi.getId().equals(this.mCurMethodId);
                    int lastSubtypeHash = Integer.parseInt((String) lastIme.second);
                    if (this.mCurrentSubtype == null) {
                        currentSubtypeHash = -1;
                    } else {
                        currentSubtypeHash = this.mCurrentSubtype.hashCode();
                    }
                    if (!imiIdIsSame || lastSubtypeHash != currentSubtypeHash) {
                        targetLastImiId = (String) lastIme.first;
                        subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, lastSubtypeHash);
                    }
                }
                if (TextUtils.isEmpty(targetLastImiId) && !InputMethodUtils.canAddToLastInputMethod(this.mCurrentSubtype) && (enabled = this.mSettings.getEnabledInputMethodListLocked()) != null) {
                    int N = enabled.size();
                    if (this.mCurrentSubtype == null) {
                        locale = this.mRes.getConfiguration().locale.toString();
                    } else {
                        locale = this.mCurrentSubtype.getLocale();
                    }
                    int i = 0;
                    while (true) {
                        if (i >= N) {
                            break;
                        }
                        InputMethodInfo imi = enabled.get(i);
                        if (imi.getSubtypeCount() > 0 && imi.isSystem() && (keyboardSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, InputMethodUtils.getSubtypes(imi), "keyboard", locale, true)) != null) {
                            targetLastImiId = imi.getId();
                            subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, keyboardSubtype.hashCode());
                            if (keyboardSubtype.getLocale().equals(locale)) {
                                break;
                            }
                        }
                        i++;
                    }
                }
                if (TextUtils.isEmpty(targetLastImiId)) {
                    return false;
                }
                setInputMethodWithSubtypeIdLocked(token, targetLastImiId, subtypeId);
                return true;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        synchronized (this.mMethodMap) {
            if (!calledWithValidTokenLocked(token)) {
                return false;
            }
            InputMethodSubtypeSwitchingController.ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(onlyCurrentIme, this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype);
            if (nextSubtype == null) {
                return false;
            }
            setInputMethodWithSubtypeIdLocked(token, nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        synchronized (this.mMethodMap) {
            if (!calledWithValidTokenLocked(token)) {
                return false;
            }
            if (this.mSwitchingController.getNextInputMethodLocked(false, this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype) == null) {
                return false;
            }
            return true;
        }
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return null;
            }
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme != null && !TextUtils.isEmpty((CharSequence) lastIme.first)) {
                if (!TextUtils.isEmpty((CharSequence) lastIme.second)) {
                    InputMethodInfo lastImi = this.mMethodMap.get(lastIme.first);
                    if (lastImi == null) {
                        return null;
                    }
                    try {
                        int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                        if (lastSubtypeId >= 0) {
                            if (lastSubtypeId < lastImi.getSubtypeCount()) {
                                return lastImi.getSubtypeAt(lastSubtypeId);
                            }
                        }
                        return null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        if (!TextUtils.isEmpty(imiId) && subtypes != null) {
            ArrayList<InputMethodSubtype> toBeAdded = new ArrayList<>();
            for (InputMethodSubtype subtype : subtypes) {
                if (!toBeAdded.contains(subtype)) {
                    toBeAdded.add(subtype);
                } else {
                    Slog.w(TAG, "Duplicated subtype definition found: " + subtype.getLocale() + ", " + subtype.getMode());
                }
            }
            synchronized (this.mMethodMap) {
                if (calledFromValidUserLocked()) {
                    if (this.mSystemReady) {
                        InputMethodInfo imi = this.mMethodMap.get(imiId);
                        if (imi != null) {
                            try {
                                String[] packageInfos = this.mIPackageManager.getPackagesForUid(Binder.getCallingUid());
                                if (packageInfos != null) {
                                    for (String str : packageInfos) {
                                        if (str.equals(imi.getPackageName())) {
                                            if (subtypes.length > 0) {
                                                this.mAdditionalSubtypeMap.put(imi.getId(), toBeAdded);
                                            } else {
                                                this.mAdditionalSubtypeMap.remove(imi.getId());
                                            }
                                            long ident = Binder.clearCallingIdentity();
                                            try {
                                                buildInputMethodListLocked(false);
                                                return;
                                            } finally {
                                                Binder.restoreCallingIdentity(ident);
                                            }
                                        }
                                    }
                                }
                            } catch (RemoteException e) {
                                Slog.e(TAG, "Failed to get package infos");
                            }
                        }
                    }
                }
            }
        }
    }

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight(this.mCurTokenDisplayId);
    }

    public void reportActivityView(IInputMethodClient parentClient, int childDisplayId, float[] matrixValues) {
        DisplayInfo displayInfo = this.mDisplayManagerInternal.getDisplayInfo(childDisplayId);
        if (displayInfo == null) {
            throw new IllegalArgumentException("Cannot find display for non-existent displayId: " + childDisplayId);
        } else if (Binder.getCallingUid() == displayInfo.ownerUid) {
            synchronized (this.mMethodMap) {
                ClientState cs = this.mClients.get(parentClient.asBinder());
                if (cs != null) {
                    if (matrixValues == null) {
                        ActivityViewInfo info = this.mActivityViewDisplayIdToParentMap.get(childDisplayId);
                        if (info != null) {
                            if (info.mParentClient == cs) {
                                this.mActivityViewDisplayIdToParentMap.remove(childDisplayId);
                                return;
                            }
                            throw new SecurityException("Only the owner client can clear ActivityViewGeometry for display #" + childDisplayId);
                        }
                        return;
                    }
                    ActivityViewInfo info2 = this.mActivityViewDisplayIdToParentMap.get(childDisplayId);
                    if (info2 != null) {
                        if (info2.mParentClient != cs) {
                            throw new InvalidParameterException("Display #" + childDisplayId + " is already registered by " + info2.mParentClient);
                        }
                    }
                    if (info2 == null) {
                        if (this.mWindowManagerInternal.isUidAllowedOnDisplay(childDisplayId, cs.uid)) {
                            info2 = new ActivityViewInfo(cs, new Matrix());
                            this.mActivityViewDisplayIdToParentMap.put(childDisplayId, info2);
                        } else {
                            throw new SecurityException(cs + " cannot access to display #" + childDisplayId);
                        }
                    }
                    info2.mMatrix.setValues(matrixValues);
                    if (this.mCurClient != null) {
                        if (this.mCurClient.curSession != null) {
                            Matrix matrix = null;
                            int displayId = this.mCurClient.selfReportedDisplayId;
                            boolean needToNotify = false;
                            while (true) {
                                needToNotify |= displayId == childDisplayId;
                                ActivityViewInfo next = this.mActivityViewDisplayIdToParentMap.get(displayId);
                                if (next == null) {
                                    break;
                                }
                                if (matrix == null) {
                                    matrix = new Matrix(next.mMatrix);
                                } else {
                                    matrix.postConcat(next.mMatrix);
                                }
                                if (next.mParentClient.selfReportedDisplayId != this.mCurTokenDisplayId) {
                                    displayId = info2.mParentClient.selfReportedDisplayId;
                                } else if (needToNotify) {
                                    float[] values = new float[9];
                                    matrix.getValues(values);
                                    try {
                                        this.mCurClient.client.updateActivityViewToScreenMatrix(this.mCurSeq, values);
                                    } catch (RemoteException e) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new SecurityException("The caller doesn't own the display.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyUserAction(IBinder token) {
        synchronized (this.mMethodMap) {
            if (this.mCurToken == token) {
                InputMethodInfo imi = this.mMethodMap.get(this.mCurMethodId);
                if (imi != null) {
                    this.mSwitchingController.onUserActionLocked(imi, this.mCurrentSubtype);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportPreRendered(IBinder token, EditorInfo info) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(3060, info, this.mCurClient));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyImeVisibility(IBinder token, boolean setVisible) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(3070, setVisible ? 1 : 0, this.mCurClient));
                }
            }
        }
    }

    private void setInputMethodWithSubtypeIdLocked(IBinder token, String id, int subtypeId) {
        if (token == null) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Using null token requires permission android.permission.WRITE_SECURE_SETTINGS");
            }
        } else if (this.mCurToken != token) {
            Slog.w(TAG, "Ignoring setInputMethod of uid " + Binder.getCallingUid() + " token: " + token);
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            setInputMethodLocked(id, subtypeId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideMySoftInput(IBinder token, int flags) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "hideMySoftInput, pid=" + Binder.getCallingPid() + ", token=" + token);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    hideCurrentInputLocked(flags, null);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMySoftInput(IBinder token, int flags) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    showCurrentInputLocked(flags, null);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setEnabledSessionInMainThread(SessionState session) {
        SessionState sessionState = this.mEnabledSession;
        if (sessionState != session) {
            if (!(sessionState == null || sessionState.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            this.mEnabledSession = session;
            SessionState sessionState2 = this.mEnabledSession;
            if (sessionState2 != null && sessionState2.session != null) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, true);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01dc, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x01f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01f3, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x01f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x01f5, code lost:
        r2.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x013c, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x013e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x013e, code lost:
        r2.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0168, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x013e;
     */
    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        boolean showAuxSubtypes;
        int i = msg.what;
        boolean setVisible = false;
        if (i == 1) {
            int displayId = msg.arg2;
            int i2 = msg.arg1;
            if (i2 == 0) {
                showAuxSubtypes = this.mInputShown;
            } else if (i2 == 1) {
                showAuxSubtypes = true;
            } else if (i2 != 2) {
                Slog.e(TAG, "Unknown subtype picker mode = " + msg.arg1);
                return false;
            } else {
                showAuxSubtypes = false;
            }
            showInputMethodMenu(showAuxSubtypes, displayId);
            return true;
        } else if (i == 2) {
            showInputMethodAndSubtypeEnabler((String) msg.obj);
            return true;
        } else if (i != 3) {
            switch (i) {
                case 1000:
                    try {
                        ((IInputMethod) msg.obj).unbindInput();
                    } catch (RemoteException e) {
                    }
                    return true;
                case MSG_BIND_INPUT /* 1010 */:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                    } catch (RemoteException e2) {
                    }
                    args.recycle();
                    return true;
                case MSG_SHOW_SOFT_INPUT /* 1020 */:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    try {
                        ((IInputMethod) args2.arg1).showSoftInput(msg.arg1, (ResultReceiver) args2.arg2);
                    } catch (RemoteException e3) {
                    }
                    args2.recycle();
                    return true;
                case MSG_HIDE_SOFT_INPUT /* 1030 */:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    try {
                        ((IInputMethod) args3.arg1).hideSoftInput(0, (ResultReceiver) args3.arg2);
                    } catch (RemoteException e4) {
                    }
                    args3.recycle();
                    return true;
                case MSG_HIDE_CURRENT_INPUT_METHOD /* 1035 */:
                    synchronized (this.mMethodMap) {
                        hideCurrentInputLocked(0, null);
                    }
                    return true;
                case MSG_INITIALIZE_IME /* 1040 */:
                    SomeArgs args4 = (SomeArgs) msg.obj;
                    try {
                        IBinder token = (IBinder) args4.arg2;
                        ((IInputMethod) args4.arg1).initializeInternal(token, msg.arg1, new InputMethodPrivilegedOperationsImpl(this, token));
                    } catch (RemoteException e5) {
                    }
                    args4.recycle();
                    return true;
                case MSG_CREATE_SESSION /* 1050 */:
                    SomeArgs args5 = (SomeArgs) msg.obj;
                    IInputMethod method = (IInputMethod) args5.arg1;
                    InputChannel channel = (InputChannel) args5.arg2;
                    try {
                        method.createSession(channel, (IInputSessionCallback) args5.arg3);
                        if (channel != null) {
                            break;
                        }
                    } catch (RemoteException e6) {
                        if (channel != null) {
                            break;
                        }
                    } catch (Throwable th) {
                        if (channel != null && Binder.isProxy(method)) {
                            channel.dispose();
                        }
                        throw th;
                    }
                    args5.recycle();
                    return true;
                case 2000:
                    int missingMethods = msg.arg1;
                    boolean restarting = msg.arg2 != 0;
                    SomeArgs args6 = (SomeArgs) msg.obj;
                    IBinder startInputToken = (IBinder) args6.arg1;
                    SessionState session = (SessionState) args6.arg2;
                    IInputContext inputContext = (IInputContext) args6.arg3;
                    EditorInfo editorInfo = (EditorInfo) args6.arg4;
                    try {
                        setEnabledSessionInMainThread(session);
                        session.method.startInput(startInputToken, inputContext, missingMethods, editorInfo, restarting, session.client.shouldPreRenderIme);
                    } catch (RemoteException e7) {
                    }
                    args6.recycle();
                    return true;
                case MSG_UNBIND_CLIENT /* 3000 */:
                    try {
                        ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                    } catch (RemoteException e8) {
                    }
                    return true;
                case 3010:
                    SomeArgs args7 = (SomeArgs) msg.obj;
                    IInputMethodClient client = (IInputMethodClient) args7.arg1;
                    InputBindResult res = (InputBindResult) args7.arg2;
                    try {
                        client.onBindMethod(res);
                        if (res.channel != null) {
                            break;
                        }
                    } catch (RemoteException e9) {
                        Slog.w(TAG, "Client died receiving input method " + args7.arg2);
                        if (res.channel != null) {
                            break;
                        }
                    } catch (Throwable th2) {
                        if (res.channel != null && Binder.isProxy(client)) {
                            res.channel.dispose();
                        }
                        throw th2;
                    }
                    args7.recycle();
                    return true;
                case MSG_SET_ACTIVE /* 3020 */:
                    try {
                        IInputMethodClient iInputMethodClient = ((ClientState) msg.obj).client;
                        boolean z = msg.arg1 != 0;
                        if (msg.arg2 != 0) {
                            setVisible = true;
                        }
                        iInputMethodClient.setActive(z, setVisible);
                    } catch (RemoteException e10) {
                        Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid " + ((ClientState) msg.obj).pid + " uid " + ((ClientState) msg.obj).uid);
                    }
                    return true;
                case MSG_SET_INTERACTIVE /* 3030 */:
                    if (msg.arg1 != 0) {
                        setVisible = true;
                    }
                    handleSetInteractive(setVisible);
                    return true;
                case MSG_REPORT_FULLSCREEN_MODE /* 3045 */:
                    if (msg.arg1 != 0) {
                        setVisible = true;
                    }
                    ClientState clientState = (ClientState) msg.obj;
                    try {
                        clientState.client.reportFullscreenMode(setVisible);
                    } catch (RemoteException e11) {
                        Slog.w(TAG, "Got RemoteException sending reportFullscreen(" + setVisible + ") notification to pid=" + clientState.pid + " uid=" + clientState.uid);
                    }
                    return true;
                case 3060:
                    SomeArgs args8 = (SomeArgs) msg.obj;
                    EditorInfo info = (EditorInfo) args8.arg1;
                    ClientState clientState2 = (ClientState) args8.arg2;
                    try {
                        clientState2.client.reportPreRendered(info);
                    } catch (RemoteException e12) {
                        Slog.w(TAG, "Got RemoteException sending reportPreRendered(" + info + ") notification to pid=" + clientState2.pid + " uid=" + clientState2.uid);
                    }
                    args8.recycle();
                    return true;
                case 3070:
                    if (msg.arg1 != 0) {
                        setVisible = true;
                    }
                    ClientState clientState3 = (ClientState) msg.obj;
                    try {
                        clientState3.client.applyImeVisibility(setVisible);
                    } catch (RemoteException e13) {
                        Slog.w(TAG, "Got RemoteException sending applyImeVisibility(" + setVisible + ") notification to pid=" + clientState3.pid + " uid=" + clientState3.uid);
                    }
                    return true;
                case MSG_HARD_KEYBOARD_SWITCH_CHANGED /* 4000 */:
                    HardKeyboardListener hardKeyboardListener = this.mHardKeyboardListener;
                    if (msg.arg1 == 1) {
                        setVisible = true;
                    }
                    hardKeyboardListener.handleHardKeyboardStatusChange(setVisible);
                    return true;
                case MSG_SYSTEM_UNLOCK_USER /* 5000 */:
                    onUnlockUser(msg.arg1);
                    return true;
                default:
                    return false;
            }
        } else {
            showConfigureInputMethods();
            return true;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        synchronized (this.mMethodMap) {
            this.mIsInteractive = interactive;
            int i = 0;
            updateSystemUiLocked(interactive ? this.mImeWindowVis : 0, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                IInputMethodClient iInputMethodClient = this.mCurClient.client;
                HandlerCaller handlerCaller = this.mCaller;
                int i2 = this.mIsInteractive ? 1 : 0;
                if (this.mInFullscreenMode) {
                    i = 1;
                }
                executeOrSendMessage(iInputMethodClient, handlerCaller.obtainMessageIIO((int) MSG_SET_ACTIVE, i2, i, this.mCurClient));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean chooseNewDefaultIMELocked() {
        InputMethodInfo imi = InputMethodUtils.getMostApplicableDefaultIME(this.mSettings.getEnabledInputMethodListLocked());
        if (imi != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "New default IME was selected: " + imi.getId());
            }
            resetSelectedInputMethodAndSubtypeLocked(imi.getId());
            return true;
        } else if (imi != null) {
            return false;
        } else {
            Slog.w(TAG, "NO default IME was selected: ");
            return false;
        }
    }

    private void queryInputMethodServicesInternal(Context context, int userId, ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap, ArrayMap<String, InputMethodInfo> methodMap, ArrayList<InputMethodInfo> methodList) {
        methodList.clear();
        methodMap.clear();
        List<ResolveInfo> services = context.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, userId);
        if (services.size() == 0) {
            Slog.e(TAG, "There is no input method available in the system");
        }
        methodList.ensureCapacity(services.size());
        methodMap.ensureCapacity(services.size());
        for (int i = 0; i < services.size(); i++) {
            ResolveInfo ri = services.get(i);
            ServiceInfo si = ri.serviceInfo;
            String imeId = InputMethodInfo.computeId(ri);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                Slog.w(TAG, "Skipping input method " + imeId + ": it does not require the permission android.permission.BIND_INPUT_METHOD");
            } else if (!shouldBuildInputMethodList(si.packageName)) {
                Slog.w(TAG, "buildInputMethodListLocked: Skipping IME " + si.packageName);
            } else {
                try {
                    InputMethodInfo imi = new InputMethodInfo(context, ri, additionalSubtypeMap.get(imeId));
                    if (!imi.isVrOnly()) {
                        methodList.add(imi);
                        methodMap.put(imi.getId(), imi);
                        if (ensureEnableSystemIME(imi.getId(), imi, context, this.mSettings.getCurrentUserId())) {
                            setInputMethodEnabledLocked(imi.getId(), true);
                        }
                    }
                } catch (Exception e) {
                    Slog.wtf(TAG, "Unable to load input method " + imeId, e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mMethodMap"})
    public void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        if (!this.mSystemReady) {
            Slog.e(TAG, "buildInputMethodListLocked is not allowed until system is ready");
            return;
        }
        this.mMethodMapUpdateCount++;
        this.mMyPackageMonitor.clearKnownImePackageNamesLocked();
        queryInputMethodServicesInternal(this.mContext, this.mSettings.getCurrentUserId(), this.mAdditionalSubtypeMap, this.mMethodMap, this.mMethodList);
        List<ResolveInfo> allInputMethodServices = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 512, this.mSettings.getCurrentUserId());
        int N = allInputMethodServices.size();
        for (int i = 0; i < N; i++) {
            ServiceInfo si = allInputMethodServices.get(i).serviceInfo;
            if ("android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                this.mMyPackageMonitor.addKnownImePackageNameLocked(si.packageName);
            }
        }
        boolean reenableMinimumNonAuxSystemImes = false;
        if (!resetDefaultEnabledIme) {
            boolean enabledImeFound = false;
            boolean enabledNonAuxImeFound = false;
            List<InputMethodInfo> enabledImes = this.mSettings.getEnabledInputMethodListLocked();
            int N2 = enabledImes.size();
            int i2 = 0;
            while (true) {
                if (i2 >= N2) {
                    break;
                }
                InputMethodInfo imi = enabledImes.get(i2);
                if (this.mMethodList.contains(imi)) {
                    enabledImeFound = true;
                    if (!imi.isAuxiliaryIme()) {
                        enabledNonAuxImeFound = true;
                        break;
                    }
                }
                i2++;
            }
            if (!enabledImeFound) {
                resetDefaultEnabledIme = true;
                resetSelectedInputMethodAndSubtypeLocked("");
            } else if (!enabledNonAuxImeFound) {
                reenableMinimumNonAuxSystemImes = true;
            }
        }
        if (resetDefaultEnabledIme || reenableMinimumNonAuxSystemImes) {
            ArrayList<InputMethodInfo> defaultEnabledIme = InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mMethodList, reenableMinimumNonAuxSystemImes);
            int N3 = defaultEnabledIme.size();
            for (int i3 = 0; i3 < N3; i3++) {
                setInputMethodEnabledLocked(defaultEnabledIme.get(i3).getId(), true);
            }
        }
        String defaultImiId = this.mSettings.getSelectedInputMethod();
        if (!TextUtils.isEmpty(defaultImiId)) {
            if (!this.mMethodMap.containsKey(defaultImiId)) {
                Slog.w(TAG, "Default IME is uninstalled. Choose new default IME.");
                if (chooseNewDefaultIMELocked()) {
                    updateInputMethodsFromSettingsLocked(true);
                }
            } else {
                setInputMethodEnabledLocked(defaultImiId, true);
            }
        }
        this.mSwitchingController.resetCircularListLocked(this.mContext);
    }

    private void showInputMethodAndSubtypeEnabler(String inputMethodId) {
        int userId;
        Intent intent = new Intent("android.settings.INPUT_METHOD_SUBTYPE_SETTINGS");
        intent.setFlags(337641472);
        if (!TextUtils.isEmpty(inputMethodId)) {
            intent.putExtra("input_method_id", inputMethodId);
        }
        synchronized (this.mMethodMap) {
            userId = this.mSettings.getCurrentUserId();
        }
        this.mContext.startActivityAsUser(intent, null, UserHandle.of(userId));
    }

    private void showConfigureInputMethods() {
        Intent intent = new Intent("android.settings.INPUT_METHOD_SETTINGS");
        intent.setFlags(337641472);
        this.mContext.startActivityAsUser(intent, null, UserHandle.CURRENT);
    }

    private boolean isScreenLocked() {
        KeyguardManager keyguardManager = this.mKeyguardManager;
        return keyguardManager != null && keyguardManager.isKeyguardLocked() && this.mKeyguardManager.isKeyguardSecure();
    }

    private void showInputMethodMenu(boolean showAuxSubtypes, int displayId) {
        Throwable th;
        int checkedItem;
        ContextThemeWrapper themeContext;
        Context dialogContext;
        int subtypeId;
        InputMethodSubtype currentSubtype;
        boolean isScreenLocked = isScreenLocked();
        String lastInputMethodId = this.mSettings.getSelectedInputMethod();
        int lastInputMethodSubtypeId = this.mSettings.getSelectedInputMethodSubtypeId(lastInputMethodId);
        synchronized (this.mMethodMap) {
            try {
                List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> imList = this.mSwitchingController.getSortedInputMethodAndSubtypeListLocked(showAuxSubtypes, isScreenLocked);
                if (imList.isEmpty()) {
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } else {
                    hideInputMethodMenuLocked();
                    if (lastInputMethodSubtypeId == -1 && (currentSubtype = getCurrentInputMethodSubtypeLocked()) != null) {
                        lastInputMethodSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(this.mMethodMap.get(this.mCurMethodId), currentSubtype.hashCode());
                    }
                    try {
                        int N = imList.size();
                        this.mIms = new InputMethodInfo[N];
                        this.mSubtypeIds = new int[N];
                        checkedItem = 0;
                        for (int i = 0; i < N; i++) {
                            InputMethodSubtypeSwitchingController.ImeSubtypeListItem item = imList.get(i);
                            this.mIms[i] = item.mImi;
                            this.mSubtypeIds[i] = item.mSubtypeId;
                            if (this.mIms[i].getId().equals(lastInputMethodId) && ((subtypeId = this.mSubtypeIds[i]) == -1 || ((lastInputMethodSubtypeId == -1 && subtypeId == 0) || subtypeId == lastInputMethodSubtypeId))) {
                                checkedItem = i;
                            }
                        }
                        int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
                        themeContext = new ContextThemeWrapper(this.mContext, themeID);
                        this.mDialogBuilder = new AlertDialog.Builder(themeContext, themeID);
                        this.mDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            /* class com.android.server.inputmethod.InputMethodManagerService.AnonymousClass3 */

                            @Override // android.content.DialogInterface.OnCancelListener
                            public void onCancel(DialogInterface dialog) {
                                InputMethodManagerService.this.hideInputMethodMenu();
                            }
                        });
                        dialogContext = this.mDialogBuilder.getContext();
                        TypedArray a = dialogContext.obtainStyledAttributes(null, R.styleable.DialogPreference, 16842845, 0);
                        Drawable dialogIcon = a.getDrawable(2);
                        a.recycle();
                        this.mDialogBuilder.setIcon(dialogIcon);
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                    try {
                        View tv = ((LayoutInflater) dialogContext.getSystemService(LayoutInflater.class)).inflate(34013191, (ViewGroup) null);
                        this.mDialogBuilder.setCustomTitle(tv);
                        this.mSwitchingDialogTitleView = tv;
                        View mSwitchSectionView = this.mSwitchingDialogTitleView.findViewById(34603134);
                        if (mSwitchSectionView == null) {
                            try {
                                Slog.e(TAG, "mSwitchSectionView is null");
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        } else {
                            try {
                                mSwitchSectionView.setVisibility(this.mWindowManagerInternal.isHardKeyboardAvailable() ? 0 : 8);
                                Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(34603135);
                                hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
                                hardKeySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    /* class com.android.server.inputmethod.InputMethodManagerService.AnonymousClass4 */

                                    @Override // android.widget.CompoundButton.OnCheckedChangeListener
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        InputMethodManagerService.this.mSettings.setShowImeWithHardKeyboard(isChecked);
                                        InputMethodManagerService.this.hideInputMethodMenu();
                                    }
                                });
                                final ImeSubtypeListAdapter adapter = new ImeSubtypeListAdapter(themeContext, 17367297, imList, checkedItem);
                                this.mDialogBuilder.setSingleChoiceItems(adapter, checkedItem, new DialogInterface.OnClickListener() {
                                    /* class com.android.server.inputmethod.InputMethodManagerService.AnonymousClass5 */

                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialog, int which) {
                                        synchronized (InputMethodManagerService.this.mMethodMap) {
                                            if (!(InputMethodManagerService.this.mIms == null || InputMethodManagerService.this.mIms.length <= which || InputMethodManagerService.this.mSubtypeIds == null)) {
                                                if (InputMethodManagerService.this.mSubtypeIds.length > which) {
                                                    InputMethodInfo im = InputMethodManagerService.this.mIms[which];
                                                    int subtypeId = InputMethodManagerService.this.mSubtypeIds[which];
                                                    adapter.mCheckedItem = which;
                                                    adapter.notifyDataSetChanged();
                                                    InputMethodManagerService.this.hideInputMethodMenu();
                                                    if (im != null) {
                                                        if (subtypeId < 0 || subtypeId >= im.getSubtypeCount()) {
                                                            subtypeId = -1;
                                                        }
                                                        InputMethodManagerService.this.setInputMethodLocked(im.getId(), subtypeId);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                                this.mSwitchingDialog = this.mDialogBuilder.create();
                                this.mSwitchingDialog.setCanceledOnTouchOutside(true);
                                Window w = this.mSwitchingDialog.getWindow();
                                WindowManager.LayoutParams attrs = w.getAttributes();
                                w.setType(2012);
                                attrs.token = this.mSwitchingDialogToken;
                                attrs.privateFlags |= 16;
                                attrs.setTitle("Select input method");
                                w.setAttributes(attrs);
                                View decView = w.getDecorView();
                                if (decView != null) {
                                    try {
                                        decView.setPadding(decView.getPaddingLeft(), decView.getPaddingTop(), decView.getPaddingRight(), (int) ((24.0f * this.mContext.getResources().getDisplayMetrics().density) + 0.5f));
                                    } catch (Throwable th5) {
                                        th = th5;
                                        throw th;
                                    }
                                }
                                updateSystemUiLocked(this.mImeWindowVis, this.mBackDisposition);
                                this.mSwitchingDialog.show();
                            } catch (Throwable th6) {
                                th = th6;
                                throw th;
                            }
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        throw th;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ImeSubtypeListAdapter extends ArrayAdapter<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> {
        public int mCheckedItem;
        private final LayoutInflater mInflater;
        private final List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> mItemsList;
        private final int mTextViewResourceId;

        public ImeSubtypeListAdapter(Context context, int textViewResourceId, List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> itemsList, int checkedItem) {
            super(context, textViewResourceId, itemsList);
            this.mTextViewResourceId = textViewResourceId;
            this.mItemsList = itemsList;
            this.mCheckedItem = checkedItem;
            this.mInflater = (LayoutInflater) context.getSystemService(LayoutInflater.class);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = this.mInflater.inflate(this.mTextViewResourceId, (ViewGroup) null);
            }
            if (position < 0 || position >= this.mItemsList.size()) {
                return view;
            }
            InputMethodSubtypeSwitchingController.ImeSubtypeListItem item = this.mItemsList.get(position);
            CharSequence imeName = item.mImeName;
            CharSequence subtypeName = item.mSubtypeName;
            TextView firstTextView = (TextView) view.findViewById(16908308);
            TextView secondTextView = (TextView) view.findViewById(16908309);
            boolean z = false;
            if (TextUtils.isEmpty(subtypeName)) {
                firstTextView.setText(imeName);
                secondTextView.setVisibility(8);
            } else {
                firstTextView.setText(subtypeName);
                secondTextView.setText(imeName);
                secondTextView.setVisibility(0);
            }
            RadioButton radioButton = (RadioButton) view.findViewById(16909307);
            if (position == this.mCheckedItem) {
                z = true;
            }
            radioButton.setChecked(z);
            return view;
        }
    }

    /* access modifiers changed from: package-private */
    public void hideInputMethodMenu() {
        synchronized (this.mMethodMap) {
            hideInputMethodMenuLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void hideInputMethodMenuLocked() {
        AlertDialog alertDialog = this.mSwitchingDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mSwitchingDialog = null;
            this.mSwitchingDialogTitleView = null;
        }
        updateSystemUiLocked(this.mImeWindowVis, this.mBackDisposition);
        this.mDialogBuilder = null;
        this.mIms = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        List<Pair<String, ArrayList<String>>> enabledInputMethodsList = this.mSettings.getEnabledInputMethodsAndSubtypeListLocked();
        if (enabled) {
            for (Pair<String, ArrayList<String>> pair : enabledInputMethodsList) {
                if (((String) pair.first).equals(id)) {
                    return true;
                }
            }
            this.mSettings.appendAndPutEnabledInputMethodLocked(id, false);
            return false;
        }
        if (!this.mSettings.buildAndPutEnabledInputMethodsStrRemovingIdLocked(new StringBuilder(), enabledInputMethodsList, id)) {
            return false;
        }
        if (id.equals(this.mSettings.getSelectedInputMethod()) && !chooseNewDefaultIMELocked()) {
            Slog.i(TAG, "Can't find new IME, unsetting the current input method.");
            resetSelectedInputMethodAndSubtypeLocked("");
        }
        return true;
    }

    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        this.mSettings.saveCurrentInputMethodAndSubtypeToHistory(this.mCurMethodId, this.mCurrentSubtype);
        if (imi == null || subtypeId < 0) {
            this.mSettings.putSelectedSubtype(-1);
            this.mCurrentSubtype = null;
        } else if (subtypeId < imi.getSubtypeCount()) {
            InputMethodSubtype subtype = imi.getSubtypeAt(subtypeId);
            this.mSettings.putSelectedSubtype(subtype.hashCode());
            this.mCurrentSubtype = subtype;
        } else {
            this.mSettings.putSelectedSubtype(-1);
            this.mCurrentSubtype = getCurrentInputMethodSubtypeLocked();
        }
        if (!setSubtypeOnly) {
            this.mSettings.putSelectedInputMethod(imi != null ? imi.getId() : "");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        String subtypeHashCode;
        InputMethodInfo imi = this.mMethodMap.get(newDefaultIme);
        int lastSubtypeId = -1;
        if (!(imi == null || TextUtils.isEmpty(newDefaultIme) || (subtypeHashCode = this.mSettings.getLastSubtypeForInputMethodLocked(newDefaultIme)) == null)) {
            try {
                lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, Integer.parseInt(subtypeHashCode));
            } catch (NumberFormatException e) {
                Slog.w(TAG, "HashCode for subtype looks broken: " + subtypeHashCode, e);
            }
        }
        setSelectedInputMethodAndSubtypeLocked(imi, lastSubtypeId, false);
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return null;
            }
            return getCurrentInputMethodSubtypeLocked();
        }
    }

    private InputMethodSubtype getCurrentInputMethodSubtypeLocked() {
        InputMethodSubtype inputMethodSubtype;
        if (this.mCurMethodId == null) {
            return null;
        }
        boolean subtypeIsSelected = this.mSettings.isSubtypeSelected();
        InputMethodInfo imi = this.mMethodMap.get(this.mCurMethodId);
        if (imi == null || imi.getSubtypeCount() == 0) {
            return null;
        }
        if (!subtypeIsSelected || (inputMethodSubtype = this.mCurrentSubtype) == null || !InputMethodUtils.isValidSubtypeId(imi, inputMethodSubtype.hashCode())) {
            int subtypeId = this.mSettings.getSelectedInputMethodSubtypeId(this.mCurMethodId);
            if (subtypeId == -1) {
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (explicitlyOrImplicitlyEnabledSubtypes.size() == 1) {
                    this.mCurrentSubtype = explicitlyOrImplicitlyEnabledSubtypes.get(0);
                } else if (explicitlyOrImplicitlyEnabledSubtypes.size() > 1) {
                    this.mCurrentSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, explicitlyOrImplicitlyEnabledSubtypes, "keyboard", null, true);
                    if (this.mCurrentSubtype == null) {
                        this.mCurrentSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, explicitlyOrImplicitlyEnabledSubtypes, null, null, true);
                    }
                }
            } else {
                this.mCurrentSubtype = InputMethodUtils.getSubtypes(imi).get(subtypeId);
            }
        }
        return this.mCurrentSubtype;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<InputMethodInfo> getInputMethodListAsUser(int userId) {
        List<InputMethodInfo> inputMethodListLocked;
        synchronized (this.mMethodMap) {
            inputMethodListLocked = getInputMethodListLocked(userId);
        }
        return inputMethodListLocked;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<InputMethodInfo> getEnabledInputMethodListAsUser(int userId) {
        List<InputMethodInfo> enabledInputMethodListLocked;
        synchronized (this.mMethodMap) {
            enabledInputMethodListLocked = getEnabledInputMethodListLocked(userId);
        }
        return enabledInputMethodListLocked;
    }

    private static final class LocalServiceImpl extends InputMethodManagerInternal {
        private final InputMethodManagerService mService;

        LocalServiceImpl(InputMethodManagerService service) {
            this.mService = service;
        }

        @Override // com.android.server.inputmethod.InputMethodManagerInternal
        public void setInteractive(boolean interactive) {
            this.mService.mHandler.obtainMessage(InputMethodManagerService.MSG_SET_INTERACTIVE, interactive ? 1 : 0, 0).sendToTarget();
        }

        @Override // com.android.server.inputmethod.InputMethodManagerInternal
        public void hideCurrentInputMethod() {
            Log.i(InputMethodManagerService.TAG, "hide inputmethod by local Service");
            this.mService.mHandler.removeMessages(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
            this.mService.mHandler.sendEmptyMessage(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
            HwInputMethodManager.hideSecureInputMethod();
        }

        @Override // com.android.server.inputmethod.InputMethodManagerInternal
        public List<InputMethodInfo> getInputMethodListAsUser(int userId) {
            return this.mService.getInputMethodListAsUser(userId);
        }

        @Override // com.android.server.inputmethod.InputMethodManagerInternal
        public List<InputMethodInfo> getEnabledInputMethodListAsUser(int userId) {
            return this.mService.getEnabledInputMethodListAsUser(userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IInputContentUriToken createInputContentUriToken(IBinder token, Uri contentUri, String packageName) {
        if (token == null) {
            throw new NullPointerException("token");
        } else if (packageName == null) {
            throw new NullPointerException("packageName");
        } else if (contentUri == null) {
            throw new NullPointerException("contentUri");
        } else if ("content".equals(contentUri.getScheme())) {
            synchronized (this.mMethodMap) {
                int uid = Binder.getCallingUid();
                if (this.mCurMethodId == null) {
                    return null;
                }
                if (this.mCurToken != token) {
                    Slog.e(TAG, "Ignoring createInputContentUriToken mCurToken=" + this.mCurToken + " token=" + token);
                    return null;
                } else if (!TextUtils.equals(this.mCurAttribute.packageName, packageName)) {
                    Slog.e(TAG, "Ignoring createInputContentUriToken mCurAttribute.packageName=" + this.mCurAttribute.packageName + " packageName=" + packageName);
                    return null;
                } else {
                    int imeUserId = UserHandle.getUserId(uid);
                    int appUserId = UserHandle.getUserId(this.mCurClient.uid);
                    return new InputContentUriTokenHandler(ContentProvider.getUriWithoutUserId(contentUri), uid, packageName, ContentProvider.getUserIdFromUri(contentUri, imeUserId), appUserId);
                }
            }
        } else {
            throw new InvalidParameterException("contentUri must have content scheme");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFullscreenMode(IBinder token, boolean fullscreen) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                    this.mInFullscreenMode = fullscreen;
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO((int) MSG_REPORT_FULLSCREEN_MODE, fullscreen ? 1 : 0, this.mCurClient));
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        ClientState client;
        ClientState focusedWindowClient;
        IInputMethod method;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            Printer p = new PrintWriterPrinter(pw);
            synchronized (this.mMethodMap) {
                p.println("Current Input Method Manager state:");
                int N = this.mMethodList.size();
                p.println("  Input Methods: mMethodMapUpdateCount=" + this.mMethodMapUpdateCount);
                for (int i = 0; i < N; i++) {
                    p.println("  InputMethod #" + i + ":");
                    this.mMethodList.get(i).dump(p, "    ");
                }
                p.println("  Clients:");
                int numClients = this.mClients.size();
                for (int i2 = 0; i2 < numClients; i2++) {
                    ClientState ci = this.mClients.valueAt(i2);
                    p.println("  Client " + ci + ":");
                    StringBuilder sb = new StringBuilder();
                    sb.append("    client=");
                    sb.append(ci.client);
                    p.println(sb.toString());
                    p.println("    inputContext=" + ci.inputContext);
                    p.println("    sessionRequested=" + ci.sessionRequested);
                    p.println("    curSession=" + ci.curSession);
                }
                p.println("  mCurMethodId=" + this.mCurMethodId);
                client = this.mCurClient;
                p.println("  mCurClient=" + client + " mCurSeq=" + this.mCurSeq);
                p.println("  mCurFocusedWindow=" + this.mCurFocusedWindow + " softInputMode=" + InputMethodDebug.softInputModeToString(this.mCurFocusedWindowSoftInputMode) + " client=" + this.mCurFocusedWindowClient);
                focusedWindowClient = this.mCurFocusedWindowClient;
                p.println("  mCurId=" + this.mCurId + " mHaveConnection=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod + " mVisibleBound=" + this.mVisibleBound);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  mCurToken=");
                sb2.append(this.mCurToken);
                p.println(sb2.toString());
                StringBuilder sb3 = new StringBuilder();
                sb3.append("  mCurTokenDisplayId=");
                sb3.append(this.mCurTokenDisplayId);
                p.println(sb3.toString());
                p.println("  mCurIntent=" + this.mCurIntent);
                method = this.mCurMethod;
                p.println("  mCurMethod=" + this.mCurMethod);
                p.println("  mEnabledSession=" + this.mEnabledSession);
                p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
                StringBuilder sb4 = new StringBuilder();
                sb4.append("  mInFullscreenMode=");
                sb4.append(this.mInFullscreenMode);
                p.println(sb4.toString());
                p.println("  mSystemReady=" + this.mSystemReady + " mInteractive=" + this.mIsInteractive);
                StringBuilder sb5 = new StringBuilder();
                sb5.append("  mSettingsObserver=");
                sb5.append(this.mSettingsObserver);
                p.println(sb5.toString());
                p.println("  mSwitchingController:");
                this.mSwitchingController.dump(p);
                p.println("  mSettings:");
                this.mSettings.dumpLocked(p, "    ");
                p.println("  mStartInputHistory:");
                this.mStartInputHistory.dump(pw, "   ");
            }
            p.println(" ");
            if (client != null) {
                pw.flush();
                try {
                    TransferPipe.dumpAsync(client.client.asBinder(), fd, args);
                } catch (RemoteException | IOException e) {
                    p.println("Failed to dump input method client: " + e);
                }
            } else {
                p.println("No input method client.");
            }
            if (!(focusedWindowClient == null || client == focusedWindowClient)) {
                p.println(" ");
                p.println("Warning: Current input method client doesn't match the last focused. window.");
                p.println("Dumping input method client in the last focused window just in case.");
                p.println(" ");
                pw.flush();
                try {
                    TransferPipe.dumpAsync(focusedWindowClient.client.asBinder(), fd, args);
                } catch (RemoteException | IOException e2) {
                    p.println("Failed to dump input method client in focused window: " + e2);
                }
            }
            p.println(" ");
            if (method != null) {
                pw.flush();
                try {
                    TransferPipe.dumpAsync(method.asBinder(), fd, args);
                } catch (RemoteException | IOException e3) {
                    p.println("Failed to dump input method service: " + e3);
                }
            } else {
                p.println("No input method service.");
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r11v0, resolved type: com.android.server.inputmethod.InputMethodManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == 2000) {
            new ShellCommandImpl(this).exec(this, in, out, err, args, callback, resultReceiver);
            return;
        }
        if (resultReceiver != null) {
            resultReceiver.send(-1, null);
        }
        String errorMsg = "InputMethodManagerService does not support shell commands from non-shell users. callingUid=" + callingUid + " args=" + Arrays.toString(args);
        if (Process.isCoreUid(callingUid)) {
            Slog.e(TAG, errorMsg);
            return;
        }
        throw new SecurityException(errorMsg);
    }

    /* access modifiers changed from: private */
    public static final class ShellCommandImpl extends ShellCommand {
        final InputMethodManagerService mService;

        ShellCommandImpl(InputMethodManagerService service) {
            this.mService = service;
        }

        public int onCommand(String cmd) {
            Arrays.asList("android.permission.DUMP", "android.permission.INTERACT_ACROSS_USERS_FULL", "android.permission.WRITE_SECURE_SETTINGS").forEach(new Consumer() {
                /* class com.android.server.inputmethod.$$Lambda$InputMethodManagerService$ShellCommandImpl$DbZq_GIUJWcuMsIpw_Jz5jVT2Y */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    InputMethodManagerService.ShellCommandImpl.this.lambda$onCommand$0$InputMethodManagerService$ShellCommandImpl((String) obj);
                }
            });
            long identity = Binder.clearCallingIdentity();
            try {
                return onCommandWithSystemIdentity(cmd);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public /* synthetic */ void lambda$onCommand$0$InputMethodManagerService$ShellCommandImpl(String permission) {
            this.mService.mContext.enforceCallingPermission(permission, null);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private int onCommandWithSystemIdentity(String cmd) {
            boolean z;
            if ("refresh_debug_properties".equals(cmd)) {
                return refreshDebugProperties();
            }
            if ("get-last-switch-user-id".equals(cmd)) {
                return this.mService.getLastSwitchUserId(this);
            }
            if (!"ime".equals(cmd)) {
                return handleDefaultCommands(cmd);
            }
            String imeCommand = getNextArg();
            if (imeCommand == null || "help".equals(imeCommand) || "-h".equals(imeCommand)) {
                onImeCommandHelp();
                return 0;
            }
            switch (imeCommand.hashCode()) {
                case -1298848381:
                    if (imeCommand.equals("enable")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 113762:
                    if (imeCommand.equals("set")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 3322014:
                    if (imeCommand.equals("list")) {
                        z = false;
                        break;
                    }
                    z = true;
                    break;
                case 108404047:
                    if (imeCommand.equals("reset")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case 1671308008:
                    if (imeCommand.equals("disable")) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            if (!z) {
                return this.mService.handleShellCommandListInputMethods(this);
            }
            if (z) {
                return this.mService.handleShellCommandEnableDisableInputMethod(this, true);
            }
            if (z) {
                return this.mService.handleShellCommandEnableDisableInputMethod(this, false);
            }
            if (z) {
                return this.mService.handleShellCommandSetInputMethod(this);
            }
            if (z) {
                return this.mService.handleShellCommandResetInputMethod(this);
            }
            getOutPrintWriter().println("Unknown command: " + imeCommand);
            return -1;
        }

        private int refreshDebugProperties() {
            DebugFlags.FLAG_OPTIMIZE_START_INPUT.refresh();
            DebugFlags.FLAG_PRE_RENDER_IME_VIEWS.refresh();
            return 0;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0031, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0034, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x002e, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x002f, code lost:
            if (r0 != null) goto L_0x0031;
         */
        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("InputMethodManagerService commands:");
            pw.println("  help");
            pw.println("    Prints this help text.");
            pw.println("  dump [options]");
            pw.println("    Synonym of dumpsys.");
            pw.println("  ime <command> [options]");
            pw.println("    Manipulate IMEs.  Run \"ime help\" for details.");
            $closeResource(null, pw);
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

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x00c3, code lost:
            throw r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x00bf, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x00c0, code lost:
            $closeResource(r0, r2);
         */
        private void onImeCommandHelp() {
            IndentingPrintWriter pw = new IndentingPrintWriter(getOutPrintWriter(), "  ", 100);
            pw.println("ime <command>:");
            pw.increaseIndent();
            pw.println("list [-a] [-s]");
            pw.increaseIndent();
            pw.println("prints all enabled input methods.");
            pw.increaseIndent();
            pw.println("-a: see all input methods");
            pw.println("-s: only a single summary line of each");
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println("enable [--user <USER_ID>] <ID>");
            pw.increaseIndent();
            pw.println("allows the given input method ID to be used.");
            pw.increaseIndent();
            pw.print("--user <USER_ID>: Specify which user to enable.");
            pw.println(" Assumes the current user if not specified.");
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println("disable [--user <USER_ID>] <ID>");
            pw.increaseIndent();
            pw.println("disallows the given input method ID to be used.");
            pw.increaseIndent();
            pw.print("--user <USER_ID>: Specify which user to disable.");
            pw.println(" Assumes the current user if not specified.");
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println("set [--user <USER_ID>] <ID>");
            pw.increaseIndent();
            pw.println("switches to the given input method ID.");
            pw.increaseIndent();
            pw.print("--user <USER_ID>: Specify which user to enable.");
            pw.println(" Assumes the current user if not specified.");
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.println("reset [--user <USER_ID>]");
            pw.increaseIndent();
            pw.println("reset currently selected/enabled IMEs to the default ones as if the device is initially booted with the current locale.");
            pw.increaseIndent();
            pw.print("--user <USER_ID>: Specify which user to reset.");
            pw.println(" Assumes the current user if not specified.");
            pw.decreaseIndent();
            pw.decreaseIndent();
            pw.decreaseIndent();
            $closeResource(null, pw);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getLastSwitchUserId(ShellCommand shellCommand) {
        synchronized (this.mMethodMap) {
            shellCommand.getOutPrintWriter().println(this.mLastSwitchUserId);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c3, code lost:
        if (r0.equals("-a") != false) goto L_0x00c7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00dc  */
    private int handleShellCommandListInputMethods(ShellCommand shellCommand) {
        int i;
        List<InputMethodInfo> methods;
        int userIdToBeResolved = -2;
        boolean brief = false;
        boolean all = false;
        while (true) {
            String nextOption = shellCommand.getNextOption();
            boolean z = false;
            i = 1;
            if (nextOption == null) {
                break;
            }
            int hashCode = nextOption.hashCode();
            if (hashCode != 1492) {
                if (hashCode != 1510) {
                    if (hashCode != 1512) {
                        if (hashCode == 1333469547 && nextOption.equals("--user")) {
                            z = true;
                            if (z) {
                                all = true;
                            } else if (z) {
                                brief = true;
                            } else if (z || z) {
                                userIdToBeResolved = UserHandle.parseUserArg(shellCommand.getNextArgRequired());
                            }
                        }
                    } else if (nextOption.equals("-u")) {
                        z = true;
                        if (z) {
                        }
                    }
                } else if (nextOption.equals("-s")) {
                    z = true;
                    if (z) {
                    }
                }
            }
            z = true;
            if (z) {
            }
        }
        synchronized (this.mMethodMap) {
            PrintWriter pr = shellCommand.getOutPrintWriter();
            int[] userIds = InputMethodUtils.resolveUserId(userIdToBeResolved, this.mSettings.getCurrentUserId(), shellCommand.getErrPrintWriter());
            int length = userIds.length;
            int i2 = 0;
            while (i2 < length) {
                int userId = userIds[i2];
                if (all) {
                    methods = getInputMethodListLocked(userId);
                } else {
                    methods = getEnabledInputMethodListLocked(userId);
                }
                if (userIds.length > i) {
                    pr.print("User #");
                    pr.print(userId);
                    pr.println(":");
                }
                for (InputMethodInfo info : methods) {
                    if (brief) {
                        pr.println(info.getId());
                    } else {
                        pr.print(info.getId());
                        pr.println(":");
                        Objects.requireNonNull(pr);
                        info.dump(new Printer(pr) {
                            /* class com.android.server.inputmethod.$$Lambda$Z2NtIIfW6UZqUgiVBM1fNETGPS8 */
                            private final /* synthetic */ PrintWriter f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // android.util.Printer
                            public final void println(String str) {
                                this.f$0.println(str);
                            }
                        }, "  ");
                    }
                }
                i2++;
                i = 1;
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleShellCommandEnableDisableInputMethod(ShellCommand shellCommand, boolean enabled) {
        int userIdToBeResolved = handleOptionsForCommandsThatOnlyHaveUserOption(shellCommand);
        String imeId = shellCommand.getNextArgRequired();
        PrintWriter out = shellCommand.getOutPrintWriter();
        PrintWriter error = shellCommand.getErrPrintWriter();
        synchronized (this.mMethodMap) {
            int[] userIds = InputMethodUtils.resolveUserId(userIdToBeResolved, this.mSettings.getCurrentUserId(), shellCommand.getErrPrintWriter());
            for (int userId : userIds) {
                if (userHasDebugPriv(userId, shellCommand)) {
                    handleShellCommandEnableDisableInputMethodInternalLocked(userId, imeId, enabled, out, error);
                }
            }
        }
        return 0;
    }

    private static int handleOptionsForCommandsThatOnlyHaveUserOption(ShellCommand shellCommand) {
        char c;
        do {
            String nextOption = shellCommand.getNextOption();
            if (nextOption != null) {
                c = 65535;
                int hashCode = nextOption.hashCode();
                if (hashCode != 1512) {
                    if (hashCode == 1333469547 && nextOption.equals("--user")) {
                        c = 1;
                    }
                } else if (nextOption.equals("-u")) {
                    c = 0;
                }
                if (c == 0) {
                    break;
                }
            } else {
                return -2;
            }
        } while (c != 1);
        return UserHandle.parseUserArg(shellCommand.getNextArgRequired());
    }

    private void handleShellCommandEnableDisableInputMethodInternalLocked(int userId, String imeId, boolean enabled, PrintWriter out, PrintWriter error) {
        PrintWriter printWriter;
        PrintWriter printWriter2;
        boolean previouslyEnabled;
        boolean failedToEnableUnknownIme = false;
        boolean previouslyEnabled2 = false;
        if (userId != this.mSettings.getCurrentUserId()) {
            ArrayMap<String, InputMethodInfo> methodMap = new ArrayMap<>();
            ArrayList<InputMethodInfo> methodList = new ArrayList<>();
            ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap = new ArrayMap<>();
            AdditionalSubtypeUtils.load(additionalSubtypeMap, userId);
            queryInputMethodServicesInternal(this.mContext, userId, additionalSubtypeMap, methodMap, methodList);
            printWriter = error;
            printWriter2 = out;
            InputMethodUtils.InputMethodSettings settings = new InputMethodUtils.InputMethodSettings(this.mContext.getResources(), this.mContext.getContentResolver(), methodMap, userId, false);
            if (!enabled) {
                previouslyEnabled = settings.buildAndPutEnabledInputMethodsStrRemovingIdLocked(new StringBuilder(), settings.getEnabledInputMethodsAndSubtypeListLocked(), imeId);
            } else if (!methodMap.containsKey(imeId)) {
                failedToEnableUnknownIme = true;
                previouslyEnabled = false;
            } else {
                Iterator<InputMethodInfo> it = settings.getEnabledInputMethodListLocked().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (TextUtils.equals(it.next().getId(), imeId)) {
                            previouslyEnabled2 = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!previouslyEnabled2) {
                    settings.appendAndPutEnabledInputMethodLocked(imeId, false);
                }
                previouslyEnabled = previouslyEnabled2;
            }
        } else if (!enabled || this.mMethodMap.containsKey(imeId)) {
            printWriter = error;
            printWriter2 = out;
            previouslyEnabled = setInputMethodEnabledLocked(imeId, enabled);
        } else {
            failedToEnableUnknownIme = true;
            printWriter = error;
            printWriter2 = out;
            previouslyEnabled = false;
        }
        if (failedToEnableUnknownIme) {
            printWriter.print("Unknown input method ");
            printWriter.print(imeId);
            printWriter.println(" cannot be enabled for user #" + userId);
            return;
        }
        printWriter2.print("Input method ");
        printWriter2.print(imeId);
        printWriter2.print(": ");
        printWriter2.print(enabled == previouslyEnabled ? "already " : "now ");
        printWriter2.print(enabled ? "enabled" : "disabled");
        printWriter2.print(" for user #");
        printWriter2.println(userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleShellCommandSetInputMethod(ShellCommand shellCommand) {
        int userId;
        int userIdToBeResolved = handleOptionsForCommandsThatOnlyHaveUserOption(shellCommand);
        String imeId = shellCommand.getNextArgRequired();
        PrintWriter out = shellCommand.getOutPrintWriter();
        PrintWriter error = shellCommand.getErrPrintWriter();
        synchronized (this.mMethodMap) {
            int[] userIds = InputMethodUtils.resolveUserId(userIdToBeResolved, this.mSettings.getCurrentUserId(), shellCommand.getErrPrintWriter());
            for (int userId2 : userIds) {
                if (userHasDebugPriv(userId2, shellCommand)) {
                    boolean failedToSelectUnknownIme = false;
                    if (userId2 != this.mSettings.getCurrentUserId()) {
                        ArrayMap<String, InputMethodInfo> methodMap = new ArrayMap<>();
                        ArrayList<InputMethodInfo> methodList = new ArrayList<>();
                        ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap = new ArrayMap<>();
                        AdditionalSubtypeUtils.load(additionalSubtypeMap, userId2);
                        userId = userId2;
                        queryInputMethodServicesInternal(this.mContext, userId2, additionalSubtypeMap, methodMap, methodList);
                        InputMethodUtils.InputMethodSettings settings = new InputMethodUtils.InputMethodSettings(this.mContext.getResources(), this.mContext.getContentResolver(), methodMap, userId, false);
                        if (methodMap.containsKey(imeId)) {
                            settings.putSelectedInputMethod(imeId);
                            settings.putSelectedSubtype(-1);
                        } else {
                            failedToSelectUnknownIme = true;
                        }
                    } else if (this.mMethodMap.containsKey(imeId)) {
                        setInputMethodLocked(imeId, -1);
                        userId = userId2;
                    } else {
                        failedToSelectUnknownIme = true;
                        userId = userId2;
                    }
                    if (failedToSelectUnknownIme) {
                        error.print("Unknown input method ");
                        error.print(imeId);
                        error.print(" cannot be selected for user #");
                        error.println(userId);
                    } else {
                        out.print("Input method ");
                        out.print(imeId);
                        out.print(" selected for user #");
                        out.println(userId);
                    }
                }
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleShellCommandResetInputMethod(ShellCommand shellCommand) {
        int userId;
        List<InputMethodInfo> nextEnabledImes;
        String nextIme;
        PrintWriter out = shellCommand.getOutPrintWriter();
        int userIdToBeResolved = handleOptionsForCommandsThatOnlyHaveUserOption(shellCommand);
        synchronized (this.mMethodMap) {
            int[] userIds = InputMethodUtils.resolveUserId(userIdToBeResolved, this.mSettings.getCurrentUserId(), shellCommand.getErrPrintWriter());
            for (int userId2 : userIds) {
                if (userHasDebugPriv(userId2, shellCommand)) {
                    if (userId2 == this.mSettings.getCurrentUserId()) {
                        hideCurrentInputLocked(0, null);
                        unbindCurrentMethodLocked();
                        resetSelectedInputMethodAndSubtypeLocked(null);
                        this.mSettings.putSelectedInputMethod(null);
                        this.mSettings.getEnabledInputMethodListLocked().forEach(new Consumer() {
                            /* class com.android.server.inputmethod.$$Lambda$InputMethodManagerService$SkFx0gCz5ltIh90rm1gl_NwDWM */

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                InputMethodManagerService.this.lambda$handleShellCommandResetInputMethod$1$InputMethodManagerService((InputMethodInfo) obj);
                            }
                        });
                        InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mMethodList).forEach(new Consumer() {
                            /* class com.android.server.inputmethod.$$Lambda$InputMethodManagerService$9NV9J24Jr9mwcbQnLu0hhjU */

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                InputMethodManagerService.this.lambda$handleShellCommandResetInputMethod$2$InputMethodManagerService((InputMethodInfo) obj);
                            }
                        });
                        updateInputMethodsFromSettingsLocked(true);
                        InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
                        nextIme = this.mSettings.getSelectedInputMethod();
                        nextEnabledImes = this.mSettings.getEnabledInputMethodListLocked();
                        userId = userId2;
                    } else {
                        ArrayMap<String, InputMethodInfo> methodMap = new ArrayMap<>();
                        ArrayList<InputMethodInfo> methodList = new ArrayList<>();
                        ArrayMap<String, List<InputMethodSubtype>> additionalSubtypeMap = new ArrayMap<>();
                        AdditionalSubtypeUtils.load(additionalSubtypeMap, userId2);
                        queryInputMethodServicesInternal(this.mContext, userId2, additionalSubtypeMap, methodMap, methodList);
                        userId = userId2;
                        InputMethodUtils.InputMethodSettings settings = new InputMethodUtils.InputMethodSettings(this.mContext.getResources(), this.mContext.getContentResolver(), methodMap, userId, false);
                        nextEnabledImes = InputMethodUtils.getDefaultEnabledImes(this.mContext, methodList);
                        String nextIme2 = InputMethodUtils.getMostApplicableDefaultIME(nextEnabledImes).getId();
                        settings.putEnabledInputMethodsStr("");
                        nextEnabledImes.forEach(new Consumer() {
                            /* class com.android.server.inputmethod.$$Lambda$InputMethodManagerService$cbEjGgC40X7HsuUviRQkKGegQKg */

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                InputMethodUtils.InputMethodSettings.this.appendAndPutEnabledInputMethodLocked(((InputMethodInfo) obj).getId(), false);
                            }
                        });
                        settings.putSelectedInputMethod(nextIme2);
                        settings.putSelectedSubtype(-1);
                        nextIme = nextIme2;
                    }
                    out.println("Reset current and enabled IMEs for user #" + userId);
                    out.println("  Selected: " + nextIme);
                    nextEnabledImes.forEach(new Consumer(out) {
                        /* class com.android.server.inputmethod.$$Lambda$InputMethodManagerService$yBUcRNgC_2SdMjBHdbSjb2l9Rw */
                        private final /* synthetic */ PrintWriter f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            InputMethodInfo inputMethodInfo = (InputMethodInfo) obj;
                            this.f$0.println("   Enabled: " + inputMethodInfo.getId());
                        }
                    });
                }
            }
        }
        return 0;
    }

    public /* synthetic */ void lambda$handleShellCommandResetInputMethod$1$InputMethodManagerService(InputMethodInfo imi) {
        setInputMethodEnabledLocked(imi.getId(), false);
    }

    public /* synthetic */ void lambda$handleShellCommandResetInputMethod$2$InputMethodManagerService(InputMethodInfo imi) {
        setInputMethodEnabledLocked(imi.getId(), true);
    }

    private boolean userHasDebugPriv(int userId, ShellCommand shellCommand) {
        if (!this.mUserManager.hasUserRestriction("no_debugging_features", UserHandle.of(userId))) {
            return true;
        }
        PrintWriter errPrintWriter = shellCommand.getErrPrintWriter();
        errPrintWriter.println("User #" + userId + " is restricted with DISALLOW_DEBUGGING_FEATURES.");
        return false;
    }

    /* access modifiers changed from: private */
    public static final class InputMethodPrivilegedOperationsImpl extends IInputMethodPrivilegedOperations.Stub {
        private final InputMethodManagerService mImms;
        private final IBinder mToken;

        InputMethodPrivilegedOperationsImpl(InputMethodManagerService imms, IBinder token) {
            this.mImms = imms;
            this.mToken = token;
        }

        public void setImeWindowStatus(int vis, int backDisposition) {
            this.mImms.setImeWindowStatus(this.mToken, vis, backDisposition);
        }

        public void reportStartInput(IBinder startInputToken) {
            this.mImms.reportStartInput(this.mToken, startInputToken);
        }

        public IInputContentUriToken createInputContentUriToken(Uri contentUri, String packageName) {
            return this.mImms.createInputContentUriToken(this.mToken, contentUri, packageName);
        }

        public void reportFullscreenMode(boolean fullscreen) {
            this.mImms.reportFullscreenMode(this.mToken, fullscreen);
        }

        public void setInputMethod(String id) {
            this.mImms.setInputMethod(this.mToken, id);
        }

        public void setInputMethodAndSubtype(String id, InputMethodSubtype subtype) {
            this.mImms.setInputMethodAndSubtype(this.mToken, id, subtype);
        }

        public void hideMySoftInput(int flags) {
            this.mImms.hideMySoftInput(this.mToken, flags);
        }

        public void showMySoftInput(int flags) {
            this.mImms.showMySoftInput(this.mToken, flags);
        }

        public void updateStatusIcon(String packageName, int iconId) {
            this.mImms.updateStatusIcon(this.mToken, packageName, iconId);
        }

        public boolean switchToPreviousInputMethod() {
            return this.mImms.switchToPreviousInputMethod(this.mToken);
        }

        public boolean switchToNextInputMethod(boolean onlyCurrentIme) {
            return this.mImms.switchToNextInputMethod(this.mToken, onlyCurrentIme);
        }

        public boolean shouldOfferSwitchingToNextInputMethod() {
            return this.mImms.shouldOfferSwitchingToNextInputMethod(this.mToken);
        }

        public void notifyUserAction() {
            this.mImms.notifyUserAction(this.mToken);
        }

        public void reportPreRendered(EditorInfo info) {
            this.mImms.reportPreRendered(this.mToken, info);
        }

        public void applyImeVisibility(boolean setVisible) {
            this.mImms.applyImeVisibility(this.mToken, setVisible);
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.inputmethod.InputMethodManagerService$HwInnerInputMethodManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0043  */
    public void setDefaultIme(String imeId) {
        boolean isSwitchEnable;
        int uid = Binder.getCallingUid();
        if (uid == 1000 || uid == 0 || IS_USE_VOICE) {
            synchronized (this.mMethodMap) {
                if (!this.mSettings.getIsWriteInputEnable()) {
                    if (!IS_USE_VOICE) {
                        isSwitchEnable = false;
                        Slog.d(TAG, "write input enable is " + isSwitchEnable);
                        if (isSwitchEnable) {
                            if (this.mPreDefaultImeId == null) {
                                this.mPreDefaultImeId = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "default_input_method", this.mLastSwitchUserId);
                            }
                            if (!TextUtils.isEmpty(imeId)) {
                                this.mCurChangedImeId = imeId;
                                setInputMethodLocked(this.mCurChangedImeId, this.mSettings.getSelectedInputMethodSubtypeId(this.mCurChangedImeId));
                            } else if (!TextUtils.isEmpty(this.mPreDefaultImeId)) {
                                setInputMethodLocked(this.mPreDefaultImeId, this.mSettings.getSelectedInputMethodSubtypeId(this.mPreDefaultImeId));
                                this.mCurChangedImeId = "";
                            }
                            Slog.d(TAG, "mCurChangedImeId = " + this.mCurChangedImeId + ";mPreDefaultImeId = " + this.mPreDefaultImeId);
                        }
                    }
                }
                isSwitchEnable = true;
                Slog.d(TAG, "write input enable is " + isSwitchEnable);
                if (isSwitchEnable) {
                }
            }
            return;
        }
        Slog.e(TAG, "process has no permssion to use");
    }

    public void setInputSource(boolean isFingerTouch) {
        int uid = Binder.getCallingUid();
        if (uid != 1000 && uid != 0) {
            throw new SecurityException("has no permssion to use");
        } else if (this.mCurMethod == null) {
        } else {
            if (!isFingerTouch) {
                Settings.Secure.putString(this.mContext.getContentResolver(), "input_source", "1");
            } else {
                Settings.Secure.putString(this.mContext.getContentResolver(), "input_source", "0");
            }
        }
    }

    public void restartInputMethodForMultiDisplay() {
        synchronized (this.mMethodMap) {
            Slog.d(TAG, "mFocusDisplayId = " + this.mFocusDisplayId + ", isSinkHasKeyboard = " + HwPCUtils.isSinkHasKeyboard());
            this.mFocusDisplayId = this.mWindowManagerInternal.getFocusedDisplayId();
            StringBuilder sb = new StringBuilder();
            sb.append("set mFocusDisplayId = ");
            sb.append(this.mFocusDisplayId);
            Slog.d(TAG, sb.toString());
            String lastImeId = this.mCurMethodId;
            if (this.mMethodMap.get(REMOTE_INPUT_METHOD_ID) == null || !HwPCUtils.isInWindowsCastMode() || HwPCUtils.getWindowsCastDisplayId() != this.mFocusDisplayId || !HwPCUtils.isSinkHasKeyboard()) {
                String defaultInputMethodId = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "default_input_method", this.mLastSwitchUserId);
                Slog.d(TAG, "mCurMethodId = " + this.mCurMethodId + ", relaunch ime to :" + defaultInputMethodId);
                this.mCurMethodId = defaultInputMethodId;
            } else {
                Slog.d(TAG, "mCurMethodId = " + this.mCurMethodId + ", relaunch to HiCastInputMethodId = " + REMOTE_INPUT_METHOD_ID);
                this.mCurMethodId = REMOTE_INPUT_METHOD_ID;
            }
            if (!this.mCurMethodId.equals(lastImeId)) {
                unbindCurrentClientLocked(2);
            }
        }
    }

    public InputBinding getCurInputBinding() {
        IInputContext iInputContext = this.mCurInputContext;
        if (iInputContext == null) {
            Log.e(TAG, "mCurInputContext is null");
            return null;
        } else if (this.mCurClient != null) {
            return new InputBinding(null, iInputContext.asBinder(), this.mCurClient.uid, this.mCurClient.pid);
        } else {
            Log.e(TAG, "mCurClient is null");
            return null;
        }
    }

    public EditorInfo getCurrentInputStyle() {
        if (this.mCurAttribute == null) {
            Log.e(TAG, "mCurAttribute is null");
        }
        return this.mCurAttribute;
    }

    @Override // com.android.server.imm.IHwInputMethodManagerInner
    public void changeInputMethod(String imeId) {
        setDefaultIme(imeId);
    }

    public class HwInnerInputMethodManagerService extends IHwInputMethodManager.Stub {
        InputMethodManagerService mIMMS;

        HwInnerInputMethodManagerService(InputMethodManagerService imms) {
            this.mIMMS = imms;
        }

        public void setDefaultIme(String imeId) {
            this.mIMMS.setDefaultIme(imeId);
        }

        public void setInputSource(boolean isFingerTouch) {
            this.mIMMS.setInputSource(isFingerTouch);
        }

        public void restartInputMethodForMultiDisplay() {
            this.mIMMS.restartInputMethodForMultiDisplay();
        }

        public void registerInputMethodListener(IHwInputMethodListener listener) {
            if (isCallingFromSystem() || checkCallingPermission(InputMethodManagerService.MULTI_DISPLAY_INPUT_METHOD_PERMISSION, "registerInputMethodListener")) {
                InputMethodManagerService.this.mHwIMMSEx.registerInputMethodListener(listener);
            }
        }

        public void unregisterInputMethodListener() {
            if (isCallingFromSystem() || checkCallingPermission(InputMethodManagerService.MULTI_DISPLAY_INPUT_METHOD_PERMISSION, "unregisterInputMethodListener")) {
                InputMethodManagerService.this.mHwIMMSEx.unregisterInputMethodListener();
            }
        }

        public void onStartInput() {
            InputMethodManagerService.this.mHwIMMSEx.onStartInput();
        }

        public void onFinishInput() {
            InputMethodManagerService.this.mHwIMMSEx.onFinishInput();
        }

        public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
            InputMethodManagerService.this.mHwIMMSEx.onUpdateCursorAnchorInfo(cursorAnchorInfo);
        }

        public void registerInputContentListener(IHwInputContentListener listener) {
            InputMethodManagerService.this.mHwIMMSEx.registerInputContentListener(listener);
        }

        public void unregisterInputContentListener() {
            InputMethodManagerService.this.mHwIMMSEx.unregisterInputContentListener();
        }

        public void onReceivedInputContent(String content) {
            if (isCallingFromSystem() || checkCallingPermission(InputMethodManagerService.MULTI_DISPLAY_INPUT_METHOD_PERMISSION, "onReceivedInputContent")) {
                InputMethodManagerService.this.mHwIMMSEx.onReceivedInputContent(content);
            }
        }

        private boolean isCallingFromSystem() {
            int uid = UserHandle.getAppId(Binder.getCallingUid());
            if (uid == 1000) {
                return true;
            }
            Slog.e(InputMethodManagerService.TAG, "Process Permission error! uid:" + uid);
            return false;
        }

        private boolean checkCallingPermission(String permission, String func) {
            if (InputMethodManagerService.this.mContext.checkCallingPermission(permission) == 0) {
                return true;
            }
            Slog.w(InputMethodManagerService.TAG, "Permission Denial: " + func + ", uid=" + Binder.getCallingUid() + " requires " + permission);
            return false;
        }

        public void onShowInputRequested() {
            InputMethodManagerService.this.mHwIMMSEx.onShowInputRequested();
        }

        public void onReceivedComposingText(String content) {
            if (isCallingFromSystem() || checkCallingPermission(InputMethodManagerService.MULTI_DISPLAY_INPUT_METHOD_PERMISSION, "onReceivedComposingText")) {
                InputMethodManagerService.this.mHwIMMSEx.onReceivedComposingText(content);
            }
        }

        public void onContentChanged(String text) {
            InputMethodManagerService.this.mHwIMMSEx.onContentChanged(text);
        }

        public InputBinding getCurInputBinding() {
            if (!isCallingFromSystem()) {
                return null;
            }
            return this.mIMMS.getCurInputBinding();
        }

        public EditorInfo getCurrentInputStyle() {
            if (!isCallingFromSystem()) {
                return null;
            }
            return this.mIMMS.getCurrentInputStyle();
        }

        public int sendEventData(int dataType, String dataStr) {
            return InputMethodManagerService.this.mHwIMMSEx.sendEventData(dataType, dataStr);
        }

        public void sendChangeInputMsg(int changeInputReason) {
            InputMethodManagerService.this.mHwIMMSEx.handleChangeInputMsg(changeInputReason);
        }
    }
}
