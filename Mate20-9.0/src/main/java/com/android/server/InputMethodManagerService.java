package com.android.server;

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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
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
import android.provider.Settings;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManagerInternal;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputBindResult;
import com.android.internal.view.InputMethodClient;
import com.android.server.HwServiceFactory;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.pm.DumpState;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.inputmethod.IHwInputMethodManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class InputMethodManagerService extends AbsInputMethodManagerService implements IHwInputMethodManagerInner, ServiceConnection, Handler.Callback {
    private static final String ACTION_INPUT_METHOD_PICKER = "huawei.settings.ACTION_SHOW_INPUT_METHOD_PICKER";
    private static final String ACTION_SHOW_INPUT_METHOD_PICKER = "com.android.server.InputMethodManagerService.SHOW_INPUT_METHOD_PICKER";
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    static final boolean DEBUG_RESTORE = false;
    private static final String DEVELOPER_CHANNEL = "DEVELOPER";
    private static final int FORCE_HIDESOFT_FLAG = 1000;
    private static final int IME_CONNECTION_BIND_FLAGS = 1082130437;
    private static final int IME_VISIBLE_BIND_FLAGS = 738197505;
    private static final boolean LAUNCHER_FORCE_HIDE_SOFT = SystemProperties.getBoolean("ro.feature.launcher.force_hidesoft", false);
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HARD_KEYBOARD_SWITCH_CHANGED = 4000;
    static final int MSG_HIDE_CURRENT_INPUT_METHOD = 1035;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_REPORT_FULLSCREEN_MODE = 3045;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 3040;
    static final int MSG_SHOW_IM_CONFIG = 3;
    static final int MSG_SHOW_IM_SUBTYPE_ENABLER = 2;
    static final int MSG_SHOW_IM_SUBTYPE_PICKER = 1;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_START_VR_INPUT = 2010;
    static final int MSG_SWITCH_IME = 3050;
    static final int MSG_SYSTEM_UNLOCK_USER = 5000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    static final String TAG = "InputMethodManagerService";
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    static final long TIME_TO_RECONNECT = 3000;
    boolean bFlag = false;
    /* access modifiers changed from: private */
    public boolean mAccessibilityRequestingNoSoftKeyboard;
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition = 0;
    private boolean mBindInstantServiceAllowed = false;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients = new HashMap<>();
    final Context mContext;
    EditorInfo mCurAttribute;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    int mCurFocusedWindowSoftInputMode;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    /* access modifiers changed from: private */
    public String mCurInputId;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurUserActionNotificationSequenceNumber = 0;
    private InputMethodSubtype mCurrentSubtype;
    private AlertDialog.Builder mDialogBuilder;
    HashMap<String, Boolean> mEnabledFileMap = new HashMap<>();
    SessionState mEnabledSession;
    /* access modifiers changed from: private */
    public InputMethodFileManager mFileManager;
    final Handler mHandler;
    private final int mHardKeyboardBehavior;
    private final HardKeyboardListener mHardKeyboardListener;
    final boolean mHasFeature;
    boolean mHaveConnection;
    IHwInputMethodManagerServiceEx mHwIMMSEx = null;
    HwInnerInputMethodManagerService mHwInnerService = new HwInnerInputMethodManagerService(this);
    /* access modifiers changed from: private */
    public final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    private PendingIntent mImeSwitchPendingIntent;
    private Notification.Builder mImeSwitcherNotification;
    int mImeWindowVis;
    /* access modifiers changed from: private */
    public InputMethodInfo[] mIms;
    boolean mInFullscreenMode;
    boolean mInputShown;
    boolean mIsDiffIME;
    boolean mIsInteractive = true;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    boolean mLastInputShown;
    private LocaleList mLastSystemLocales;
    boolean mLastUnBindInputMethodInPCMode = false;
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList<>();
    final HashMap<String, InputMethodInfo> mMethodMap = new HashMap<>();
    @GuardedBy("mMethodMap")
    private int mMethodMapUpdateCount = 0;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private NotificationManager mNotificationManager;
    private boolean mNotificationShown;
    final Resources mRes;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans = new LruCache<>(20);
    final InputMethodUtils.InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes = new HashMap<>();
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    private boolean mShowImeWithHardKeyboard;
    private boolean mShowOngoingImeSwitcherForPhones;
    boolean mShowRequested;
    private final String mSlotIme;
    @GuardedBy("mMethodMap")
    private final StartInputHistory mStartInputHistory = new StartInputHistory();
    @GuardedBy("mMethodMap")
    private final WeakHashMap<IBinder, StartInputInfo> mStartInputMap = new WeakHashMap<>();
    private StatusBarManagerService mStatusBar;
    /* access modifiers changed from: private */
    public int[] mSubtypeIds;
    private Toast mSubtypeSwitchedByShortCutToast;
    private final InputMethodSubtypeSwitchingController mSwitchingController;
    /* access modifiers changed from: private */
    public AlertDialog mSwitchingDialog;
    /* access modifiers changed from: private */
    public View mSwitchingDialogTitleView;
    private IBinder mSwitchingDialogToken = new Binder();
    boolean mSystemReady;
    private final UserManager mUserManager;
    boolean mVisibleBound = false;
    final ServiceConnection mVisibleConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        public void onVrStateChanged(boolean enabled) {
            if (!enabled) {
                InputMethodManagerService.this.restoreNonVrImeFromSettingsNoCheck();
            }
        }
    };
    final WindowManagerInternal mWindowManagerInternal;

    static final class ClientState {
        final InputBinding binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        final IInputMethodClient client;
        SessionState curSession;
        final IInputContext inputContext;
        final int pid;
        boolean sessionRequested;
        final int uid;

        public String toString() {
            return "ClientState{" + Integer.toHexString(System.identityHashCode(this)) + " uid " + this.uid + " pid " + this.pid + "}";
        }

        ClientState(IInputMethodClient _client, IInputContext _inputContext, int _uid, int _pid) {
            this.client = _client;
            this.inputContext = _inputContext;
            this.uid = _uid;
            this.pid = _pid;
        }
    }

    private static final class DebugFlag {
        private static final Object LOCK = new Object();
        private final boolean mDefaultValue;
        private final String mKey;
        @GuardedBy("LOCK")
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

    private static final class DebugFlags {
        static final DebugFlag FLAG_OPTIMIZE_START_INPUT = new DebugFlag("debug.optimize_startinput", false);

        private DebugFlags() {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface HardKeyboardBehavior {
        public static final int WIRED_AFFORDANCE = 1;
        public static final int WIRELESS_AFFORDANCE = 0;
    }

    private class HardKeyboardListener implements WindowManagerInternal.OnHardKeyboardStatusChangeListener {
        private HardKeyboardListener() {
        }

        public void onHardKeyboardStatusChange(boolean available) {
            InputMethodManagerService.this.mHandler.sendMessage(InputMethodManagerService.this.mHandler.obtainMessage(InputMethodManagerService.MSG_HARD_KEYBOARD_SWITCH_CHANGED, Integer.valueOf(available)));
        }

        public void handleHardKeyboardStatusChange(boolean available) {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (!(InputMethodManagerService.this.mSwitchingDialog == null || InputMethodManagerService.this.mSwitchingDialogTitleView == null || !InputMethodManagerService.this.mSwitchingDialog.isShowing())) {
                    View switchSectionView = InputMethodManagerService.this.mSwitchingDialogTitleView.findViewById(34603134);
                    if (switchSectionView != null) {
                        switchSectionView.setVisibility(available ? 0 : 8);
                    }
                }
            }
        }
    }

    public class HwInnerInputMethodManagerService extends IHwInputMethodManager.Stub {
        HwInnerInputMethodManagerService(InputMethodManagerService imms) {
        }

        public void setDefaultIme(String imeId) {
            InputMethodManagerService.this.mHwIMMSEx.setDefaultIme(imeId);
        }

        public void setInputSource(boolean isFingerTouch) {
            InputMethodManagerService.this.mHwIMMSEx.setInputSource(isFingerTouch);
        }
    }

    private static class ImeSubtypeListAdapter extends ArrayAdapter<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> {
        public int mCheckedItem;
        private final LayoutInflater mInflater;
        private final List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> mItemsList;
        private final int mTextColorPri;
        private final int mTextColorSec;
        private final int mTextViewResourceId;

        public ImeSubtypeListAdapter(Context context, int textViewResourceId, List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> itemsList, int checkedItem) {
            super(context, textViewResourceId, itemsList);
            this.mTextViewResourceId = textViewResourceId;
            this.mItemsList = itemsList;
            this.mCheckedItem = checkedItem;
            this.mInflater = (LayoutInflater) context.getSystemService(LayoutInflater.class);
            TypedArray array = context.obtainStyledAttributes(null, R.styleable.Theme, 0, 0);
            this.mTextColorPri = array.getColor(6, 0);
            this.mTextColorSec = array.getColor(8, 0);
            array.recycle();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView : this.mInflater.inflate(this.mTextViewResourceId, null);
            if (position < 0 || position >= this.mItemsList.size()) {
                return view;
            }
            InputMethodSubtypeSwitchingController.ImeSubtypeListItem item = this.mItemsList.get(position);
            CharSequence imeName = item.mImeName;
            CharSequence subtypeName = item.mSubtypeName;
            TextView firstTextView = (TextView) view.findViewById(16908308);
            TextView secondTextView = (TextView) view.findViewById(16908309);
            if (this.mTextColorPri != 0) {
                firstTextView.setTextColor(this.mTextColorPri);
            }
            if (this.mTextColorSec != 0) {
                secondTextView.setTextColor(this.mTextColorSec);
            }
            boolean z = false;
            if (TextUtils.isEmpty(subtypeName)) {
                firstTextView.setText(imeName);
                secondTextView.setVisibility(8);
            } else {
                secondTextView.setText(subtypeName);
                firstTextView.setText(imeName);
                secondTextView.setVisibility(0);
            }
            RadioButton radioButton = (RadioButton) view.findViewById(16909237);
            if (position == this.mCheckedItem) {
                z = true;
            }
            radioButton.setChecked(z);
            return view;
        }
    }

    class ImmsBroadcastReceiver extends BroadcastReceiver {
        ImmsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                InputMethodManagerService.this.hideInputMethodMenu();
            } else if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                InputMethodManagerService.this.updateCurrentProfileIds();
            } else {
                if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                    synchronized (InputMethodManagerService.this.mMethodMap) {
                        InputMethodManagerService.this.mSettings.putSelectedSubtype(-1);
                    }
                    InputMethodManagerService.this.onActionLocaleChanged();
                } else if (InputMethodManagerService.ACTION_SHOW_INPUT_METHOD_PICKER.equals(action)) {
                    InputMethodManagerService.this.mHandler.obtainMessage(1, 1, 0).sendToTarget();
                } else {
                    Slog.w(InputMethodManagerService.TAG, "Unexpected intent " + intent);
                }
            }
        }
    }

    private static class InputMethodFileManager {
        private static final String ADDITIONAL_SUBTYPES_FILE_NAME = "subtypes.xml";
        private static final String ATTR_ICON = "icon";
        private static final String ATTR_ID = "id";
        private static final String ATTR_IME_SUBTYPE_EXTRA_VALUE = "imeSubtypeExtraValue";
        private static final String ATTR_IME_SUBTYPE_ID = "subtypeId";
        private static final String ATTR_IME_SUBTYPE_LANGUAGE_TAG = "languageTag";
        private static final String ATTR_IME_SUBTYPE_LOCALE = "imeSubtypeLocale";
        private static final String ATTR_IME_SUBTYPE_MODE = "imeSubtypeMode";
        private static final String ATTR_IS_ASCII_CAPABLE = "isAsciiCapable";
        private static final String ATTR_IS_AUXILIARY = "isAuxiliary";
        private static final String ATTR_LABEL = "label";
        private static final String INPUT_METHOD_PATH = "inputmethod";
        private static final String NODE_IMI = "imi";
        private static final String NODE_SUBTYPE = "subtype";
        private static final String NODE_SUBTYPES = "subtypes";
        private static final String SYSTEM_PATH = "system";
        private final AtomicFile mAdditionalInputMethodSubtypeFile;
        private final HashMap<String, List<InputMethodSubtype>> mAdditionalSubtypesMap = new HashMap<>();
        private final HashMap<String, InputMethodInfo> mMethodMap;

        public InputMethodFileManager(HashMap<String, InputMethodInfo> methodMap, int userId) {
            File systemDir;
            if (methodMap != null) {
                this.mMethodMap = methodMap;
                if (userId == 0) {
                    systemDir = new File(Environment.getDataDirectory(), SYSTEM_PATH);
                } else {
                    systemDir = Environment.getUserSystemDirectory(userId);
                }
                File inputMethodDir = new File(systemDir, INPUT_METHOD_PATH);
                if (!inputMethodDir.exists() && !inputMethodDir.mkdirs()) {
                    Slog.w(InputMethodManagerService.TAG, "Couldn't create dir.: " + inputMethodDir.getAbsolutePath());
                }
                File subtypeFile = new File(inputMethodDir, ADDITIONAL_SUBTYPES_FILE_NAME);
                this.mAdditionalInputMethodSubtypeFile = new AtomicFile(subtypeFile, "input-subtypes");
                if (!subtypeFile.exists()) {
                    writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, methodMap);
                } else {
                    readAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile);
                }
            } else {
                throw new NullPointerException("methodMap is null");
            }
        }

        /* access modifiers changed from: private */
        public void deleteAllInputMethodSubtypes(String imiId) {
            synchronized (this.mMethodMap) {
                this.mAdditionalSubtypesMap.remove(imiId);
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, this.mMethodMap);
            }
        }

        public void addInputMethodSubtypes(InputMethodInfo imi, InputMethodSubtype[] additionalSubtypes) {
            synchronized (this.mMethodMap) {
                ArrayList<InputMethodSubtype> subtypes = new ArrayList<>();
                for (InputMethodSubtype subtype : additionalSubtypes) {
                    if (!subtypes.contains(subtype)) {
                        subtypes.add(subtype);
                    } else {
                        Slog.w(InputMethodManagerService.TAG, "Duplicated subtype definition found: " + subtype.getLocale() + ", " + subtype.getMode());
                    }
                }
                this.mAdditionalSubtypesMap.put(imi.getId(), subtypes);
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, this.mMethodMap);
            }
        }

        public HashMap<String, List<InputMethodSubtype>> getAllAdditionalInputMethodSubtypes() {
            HashMap<String, List<InputMethodSubtype>> hashMap;
            synchronized (this.mMethodMap) {
                hashMap = this.mAdditionalSubtypesMap;
            }
            return hashMap;
        }

        private static void writeAdditionalInputMethodSubtypes(HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile, HashMap<String, InputMethodInfo> methodMap) {
            boolean isSetMethodMap = methodMap != null && methodMap.size() > 0;
            try {
                FileOutputStream fos = subtypesFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, NODE_SUBTYPES);
                for (String imiId : allSubtypes.keySet()) {
                    if (!isSetMethodMap || methodMap.containsKey(imiId)) {
                        out.startTag(null, NODE_IMI);
                        out.attribute(null, ATTR_ID, imiId);
                        List<InputMethodSubtype> subtypesList = allSubtypes.get(imiId);
                        int N = subtypesList.size();
                        for (int i = 0; i < N; i++) {
                            InputMethodSubtype subtype = subtypesList.get(i);
                            out.startTag(null, NODE_SUBTYPE);
                            if (subtype.hasSubtypeId()) {
                                out.attribute(null, ATTR_IME_SUBTYPE_ID, String.valueOf(subtype.getSubtypeId()));
                            }
                            out.attribute(null, ATTR_ICON, String.valueOf(subtype.getIconResId()));
                            out.attribute(null, ATTR_LABEL, String.valueOf(subtype.getNameResId()));
                            out.attribute(null, ATTR_IME_SUBTYPE_LOCALE, subtype.getLocale());
                            out.attribute(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG, subtype.getLanguageTag());
                            out.attribute(null, ATTR_IME_SUBTYPE_MODE, subtype.getMode());
                            out.attribute(null, ATTR_IME_SUBTYPE_EXTRA_VALUE, subtype.getExtraValue());
                            out.attribute(null, ATTR_IS_AUXILIARY, String.valueOf(subtype.isAuxiliary() ? 1 : 0));
                            out.attribute(null, ATTR_IS_ASCII_CAPABLE, String.valueOf(subtype.isAsciiCapable() ? 1 : 0));
                            out.endTag(null, NODE_SUBTYPE);
                        }
                        out.endTag(null, NODE_IMI);
                    } else {
                        Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + imiId);
                    }
                }
                out.endTag(null, NODE_SUBTYPES);
                out.endDocument();
                subtypesFile.finishWrite(fos);
            } catch (IOException e) {
                Slog.w(InputMethodManagerService.TAG, "Error writing subtypes", e);
                if (0 != 0) {
                    subtypesFile.failWrite(null);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:60:0x017e, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x017f, code lost:
            r1 = r0;
            r4 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x0182, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x0183, code lost:
            r4 = r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
            throw r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x0187, code lost:
            if (r3 != null) goto L_0x0189;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x0189, code lost:
            if (r4 != null) goto L_0x018b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x0195, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x0198, code lost:
            throw r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0199, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x019a, code lost:
            android.util.Slog.w(com.android.server.InputMethodManagerService.TAG, "Error reading subtypes", r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x01a1, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:62:0x0182 A[ExcHandler: Throwable (r0v3 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:7:0x0011] */
        /* JADX WARNING: Removed duplicated region for block: B:78:0x0199 A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (r0v0 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:4:0x000b] */
        private static void readAdditionalInputMethodSubtypes(HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile) {
            Throwable th;
            int type;
            int i;
            int i2;
            int depth;
            String firstNodeName;
            int depth2;
            String firstNodeName2;
            HashMap<String, List<InputMethodSubtype>> hashMap = allSubtypes;
            if (hashMap != null && subtypesFile != null) {
                allSubtypes.clear();
                try {
                    FileInputStream fis = subtypesFile.openRead();
                    String str = null;
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(fis, StandardCharsets.UTF_8.name());
                        int eventType = parser.getEventType();
                        while (true) {
                            int next = parser.next();
                            type = next;
                            i = 1;
                            i2 = 2;
                            if (next == 2 || type == 1) {
                                String firstNodeName3 = parser.getName();
                            }
                        }
                        String firstNodeName32 = parser.getName();
                        if (NODE_SUBTYPES.equals(firstNodeName32)) {
                            int depth3 = parser.getDepth();
                            String currentImiId = null;
                            int i3 = type;
                            ArrayList<InputMethodSubtype> tempSubtypesArray = null;
                            while (true) {
                                int next2 = parser.next();
                                int type2 = next2;
                                if (next2 == 3) {
                                    if (parser.getDepth() <= depth3) {
                                        break;
                                    }
                                }
                                if (type2 == i) {
                                    break;
                                }
                                if (type2 != i2) {
                                    firstNodeName = firstNodeName32;
                                    depth = depth3;
                                } else {
                                    String nodeName = parser.getName();
                                    if (NODE_IMI.equals(nodeName)) {
                                        currentImiId = parser.getAttributeValue(str, ATTR_ID);
                                        if (TextUtils.isEmpty(currentImiId)) {
                                            Slog.w(InputMethodManagerService.TAG, "Invalid imi id found in subtypes.xml");
                                        } else {
                                            tempSubtypesArray = new ArrayList<>();
                                            hashMap.put(currentImiId, tempSubtypesArray);
                                            firstNodeName2 = firstNodeName32;
                                            depth2 = depth3;
                                        }
                                    } else if (NODE_SUBTYPE.equals(nodeName)) {
                                        if (TextUtils.isEmpty(currentImiId)) {
                                            firstNodeName = firstNodeName32;
                                            depth = depth3;
                                        } else if (tempSubtypesArray == null) {
                                            firstNodeName = firstNodeName32;
                                            depth = depth3;
                                        } else {
                                            int icon = Integer.parseInt(parser.getAttributeValue(str, ATTR_ICON));
                                            int label = Integer.parseInt(parser.getAttributeValue(str, ATTR_LABEL));
                                            String imeSubtypeLocale = parser.getAttributeValue(str, ATTR_IME_SUBTYPE_LOCALE);
                                            String languageTag = parser.getAttributeValue(str, ATTR_IME_SUBTYPE_LANGUAGE_TAG);
                                            String imeSubtypeMode = parser.getAttributeValue(str, ATTR_IME_SUBTYPE_MODE);
                                            String imeSubtypeExtraValue = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_EXTRA_VALUE);
                                            firstNodeName2 = firstNodeName32;
                                            boolean isAuxiliary = "1".equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_AUXILIARY)));
                                            depth2 = depth3;
                                            InputMethodSubtype.InputMethodSubtypeBuilder builder = new InputMethodSubtype.InputMethodSubtypeBuilder().setSubtypeNameResId(label).setSubtypeIconResId(icon).setSubtypeLocale(imeSubtypeLocale).setLanguageTag(languageTag).setSubtypeMode(imeSubtypeMode).setSubtypeExtraValue(imeSubtypeExtraValue).setIsAuxiliary(isAuxiliary).setIsAsciiCapable("1".equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_ASCII_CAPABLE))));
                                            boolean z = isAuxiliary;
                                            String subtypeIdString = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_ID);
                                            if (subtypeIdString != null) {
                                                builder.setSubtypeId(Integer.parseInt(subtypeIdString));
                                            }
                                            tempSubtypesArray.add(builder.build());
                                        }
                                        Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + currentImiId);
                                    } else {
                                        firstNodeName2 = firstNodeName32;
                                        depth2 = depth3;
                                    }
                                    firstNodeName32 = firstNodeName2;
                                    depth3 = depth2;
                                    hashMap = allSubtypes;
                                    str = null;
                                    i = 1;
                                    i2 = 2;
                                }
                                firstNodeName32 = firstNodeName;
                                depth3 = depth;
                                hashMap = allSubtypes;
                                str = null;
                                i = 1;
                                i2 = 2;
                            }
                            if (fis != null) {
                                fis.close();
                            }
                            return;
                        }
                        String str2 = firstNodeName32;
                        throw new XmlPullParserException("Xml doesn't start with subtypes");
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                    }
                } catch (IOException | NumberFormatException | XmlPullParserException e) {
                } catch (Throwable th4) {
                    th.addSuppressed(th4);
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

        /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.InputMethodManagerService, android.os.IBinder] */
        public void onStart() {
            LocalServices.addService(InputMethodManagerInternal.class, new LocalServiceImpl(this.mService.mHandler));
            publishBinderService("input_method", this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemRunning((StatusBarManagerService) ServiceManager.getService("statusbar"));
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.mHandler.sendMessage(this.mService.mHandler.obtainMessage(InputMethodManagerService.MSG_SYSTEM_UNLOCK_USER, userHandle, 0));
        }
    }

    private static final class LocalServiceImpl implements InputMethodManagerInternal {
        private final Handler mHandler;

        LocalServiceImpl(Handler handler) {
            this.mHandler = handler;
        }

        public void setInteractive(boolean interactive) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(InputMethodManagerService.MSG_SET_INTERACTIVE, interactive, 0));
        }

        public void switchInputMethod(boolean forwardDirection) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3050, forwardDirection, 0));
        }

        public void hideCurrentInputMethod() {
            this.mHandler.removeMessages(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
            this.mHandler.sendEmptyMessage(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
        }

        public void startVrInputMethodNoCheck(ComponentName componentName) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(InputMethodManagerService.MSG_START_VR_INPUT, componentName));
        }
    }

    private static final class MethodCallback extends IInputSessionCallback.Stub {
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

    final class MyPackageMonitor extends PackageMonitor {
        private final ArrayList<String> mChangedPackages = new ArrayList<>();
        private boolean mImePackageAppeared = false;
        @GuardedBy("mMethodMap")
        private final ArraySet<String> mKnownImePackageNames = new ArraySet<>();

        MyPackageMonitor() {
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mMethodMap")
        public void clearKnownImePackageNamesLocked() {
            this.mKnownImePackageNames.clear();
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mMethodMap")
        public final void addKnownImePackageNameLocked(String packageName) {
            this.mKnownImePackageNames.add(packageName);
        }

        @GuardedBy("mMethodMap")
        private boolean isChangingPackagesOfCurrentUserLocked() {
            return getChangingUserId() == InputMethodManagerService.this.mSettings.getCurrentUserId();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0055, code lost:
            return false;
         */
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
            }
        }

        public void onBeginPackageChanges() {
            clearPackageChangeState();
        }

        public void onPackageAppeared(String packageName, int reason) {
            if (!this.mImePackageAppeared && !InputMethodManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod").setPackage(packageName), InputMethodManagerService.this.getComponentMatchingFlags(512), getChangingUserId()).isEmpty()) {
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

        @GuardedBy("mMethodMap")
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

        /* JADX WARNING: Code restructure failed: missing block: B:51:0x0114, code lost:
            return;
         */
        private void onFinishPackageChangesInternal() {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (isChangingPackagesOfCurrentUserLocked()) {
                    if (shouldRebuildInputMethodListLocked()) {
                        InputMethodInfo curIm = null;
                        String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                        int N = InputMethodManagerService.this.mMethodList.size();
                        if (curInputMethodId != null) {
                            InputMethodInfo curIm2 = null;
                            for (int i = 0; i < N; i++) {
                                InputMethodInfo imi = InputMethodManagerService.this.mMethodList.get(i);
                                String imiId = imi.getId();
                                if (imiId.equals(curInputMethodId)) {
                                    curIm2 = imi;
                                }
                                int change = isPackageDisappearing(imi.getPackageName());
                                if (isPackageModified(imi.getPackageName())) {
                                    InputMethodManagerService.this.mFileManager.deleteAllInputMethodSubtypes(imiId);
                                }
                                if (change == 2 || change == 3) {
                                    Slog.i(InputMethodManagerService.TAG, "Input method uninstalled, disabling: " + imi.getComponent());
                                    InputMethodManagerService.this.setInputMethodEnabledLocked(imi.getId(), false);
                                }
                            }
                            curIm = curIm2;
                        }
                        InputMethodManagerService.this.buildInputMethodListLocked(false);
                        boolean changed = false;
                        if (curIm != null) {
                            int change2 = isPackageDisappearing(curIm.getPackageName());
                            if (change2 == 2 || change2 == 3) {
                                ServiceInfo si = null;
                                try {
                                    si = InputMethodManagerService.this.mIPackageManager.getServiceInfo(curIm.getComponent(), 0, InputMethodManagerService.this.mSettings.getCurrentUserId());
                                } catch (RemoteException e) {
                                }
                                if (si == null) {
                                    Slog.i(InputMethodManagerService.TAG, "Current input method removed: " + curInputMethodId);
                                    InputMethodManagerService.this.updateSystemUiLocked(InputMethodManagerService.this.mCurToken, 0, InputMethodManagerService.this.mBackDisposition);
                                    if (!InputMethodManagerService.this.chooseNewDefaultIMELocked()) {
                                        changed = true;
                                        curIm = null;
                                        Slog.i(InputMethodManagerService.TAG, "Unsetting current input method");
                                        InputMethodManagerService.this.resetSelectedInputMethodAndSubtypeLocked(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                    }
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

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            onSomePackagesChanged();
            if (components != null) {
                for (String name : components) {
                    if (packageName.equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static class SessionState {
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

    class SettingsObserver extends ContentObserver {
        String mLastEnabled = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
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
                    this.mLastEnabled = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
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

        public void onChange(boolean selfChange, Uri uri) {
            Uri showImeUri = Settings.Secure.getUriFor("show_ime_with_hard_keyboard");
            Uri accessibilityRequestingNoImeUri = Settings.Secure.getUriFor("accessibility_soft_keyboard_mode");
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (showImeUri.equals(uri)) {
                    InputMethodManagerService.this.updateKeyboardFromSettingsLocked();
                } else if (accessibilityRequestingNoImeUri.equals(uri)) {
                    boolean unused = InputMethodManagerService.this.mAccessibilityRequestingNoSoftKeyboard = Settings.Secure.getIntForUser(InputMethodManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, this.mUserId) == 1;
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
                    if (this.mLastEnabled == null) {
                        this.mLastEnabled = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    }
                    if (!this.mLastEnabled.equals(newEnabled)) {
                        this.mLastEnabled = newEnabled;
                        enabledChanged = true;
                    }
                    InputMethodManagerService.this.updateInputMethodsFromSettingsLocked(enabledChanged);
                }
            }
        }

        public String toString() {
            return "SettingsObserver{mUserId=" + this.mUserId + " mRegistered=" + this.mRegistered + " mLastEnabled=" + this.mLastEnabled + "}";
        }
    }

    private static final class ShellCommandImpl extends ShellCommand {
        final InputMethodManagerService mService;

        ShellCommandImpl(InputMethodManagerService service) {
            this.mService = service;
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        public int onCommand(String cmd) {
            char c;
            if ("refresh_debug_properties".equals(cmd)) {
                return refreshDebugProperties();
            }
            if ("set-bind-instant-service-allowed".equals(cmd)) {
                return setBindInstantServiceAllowed();
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
                        c = 1;
                        break;
                    }
                case 113762:
                    if (imeCommand.equals("set")) {
                        c = 3;
                        break;
                    }
                case 3322014:
                    if (imeCommand.equals("list")) {
                        c = 0;
                        break;
                    }
                case 108404047:
                    if (imeCommand.equals("reset")) {
                        c = 4;
                        break;
                    }
                case 1671308008:
                    if (imeCommand.equals("disable")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    return this.mService.handleShellCommandListInputMethods(this);
                case 1:
                    return this.mService.handleShellCommandEnableDisableInputMethod(this, true);
                case 2:
                    return this.mService.handleShellCommandEnableDisableInputMethod(this, false);
                case 3:
                    return this.mService.handleShellCommandSetInputMethod(this);
                case 4:
                    return this.mService.handleShellCommandResetInputMethod(this);
                default:
                    getOutPrintWriter().println("Unknown command: " + imeCommand);
                    return -1;
            }
        }

        private int setBindInstantServiceAllowed() {
            return this.mService.handleSetBindInstantServiceAllowed(this);
        }

        private int refreshDebugProperties() {
            DebugFlags.FLAG_OPTIMIZE_START_INPUT.refresh();
            return 0;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x003e, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0041, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0038, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x003c, code lost:
            if (r0 != null) goto L_0x003e;
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
            pw.println("  set-bind-instant-service-allowed true|false ");
            pw.println("    Set whether binding to services provided by instant apps is allowed.");
            if (pw != null) {
                $closeResource(null, pw);
            }
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

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x008b, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0084, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0088, code lost:
            $closeResource(r1, r0);
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
            pw.println("enable <ID>");
            pw.increaseIndent();
            pw.println("allows the given input method ID to be used.");
            pw.decreaseIndent();
            pw.println("disable <ID>");
            pw.increaseIndent();
            pw.println("disallows the given input method ID to be used.");
            pw.decreaseIndent();
            pw.println("set <ID>");
            pw.increaseIndent();
            pw.println("switches to the given input method ID.");
            pw.decreaseIndent();
            pw.println("reset");
            pw.increaseIndent();
            pw.println("reset currently selected/enabled IMEs to the default ones as if the device is initially booted with the current locale.");
            pw.decreaseIndent();
            pw.decreaseIndent();
            $closeResource(null, pw);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface ShellCommandResult {
        public static final int FAILURE = -1;
        public static final int SUCCESS = 0;
    }

    private static final class StartInputHistory {
        private static final int ENTRY_SIZE_FOR_HIGH_RAM_DEVICE = 16;
        private static final int ENTRY_SIZE_FOR_LOW_RAM_DEVICE = 5;
        private final Entry[] mEntries;
        private int mNextIndex;

        private static final class Entry {
            int mClientBindSequenceNumber;
            EditorInfo mEditorInfo;
            String mImeId;
            String mImeTokenString;
            boolean mRestarting;
            int mSequenceNumber;
            int mStartInputReason;
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
                this.mImeTokenString = String.valueOf(original.mImeToken);
                this.mImeId = original.mImeId;
                this.mStartInputReason = original.mStartInputReason;
                this.mRestarting = original.mRestarting;
                this.mTargetWindowString = String.valueOf(original.mTargetWindow);
                this.mEditorInfo = original.mEditorInfo;
                this.mTargetWindowSoftInputMode = original.mTargetWindowSoftInputMode;
                this.mClientBindSequenceNumber = original.mClientBindSequenceNumber;
            }
        }

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

        /* access modifiers changed from: package-private */
        public void addEntry(StartInputInfo info) {
            int index = this.mNextIndex;
            if (this.mEntries[index] == null) {
                this.mEntries[index] = new Entry(info);
            } else {
                this.mEntries[index].set(info);
            }
            this.mNextIndex = (this.mNextIndex + 1) % this.mEntries.length;
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw, String prefix) {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            for (int i = 0; i < this.mEntries.length; i++) {
                Entry entry = this.mEntries[(this.mNextIndex + i) % this.mEntries.length];
                if (entry != null) {
                    pw.print(prefix);
                    pw.println("StartInput #" + entry.mSequenceNumber + ":");
                    pw.print(prefix);
                    pw.println(" time=" + dataFormat.format(new Date(entry.mWallTime)) + " (timestamp=" + entry.mTimestamp + ") reason=" + InputMethodClient.getStartInputReason(entry.mStartInputReason) + " restarting=" + entry.mRestarting);
                    pw.print(prefix);
                    StringBuilder sb = new StringBuilder();
                    sb.append(" imeToken=");
                    sb.append(entry.mImeTokenString);
                    sb.append(" [");
                    sb.append(entry.mImeId);
                    sb.append("]");
                    pw.println(sb.toString());
                    pw.print(prefix);
                    pw.println(" targetWin=" + entry.mTargetWindowString + " [" + entry.mEditorInfo.packageName + "] clientBindSeq=" + entry.mClientBindSequenceNumber);
                    pw.print(prefix);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(" softInputMode=");
                    sb2.append(InputMethodClient.softInputModeToString(entry.mTargetWindowSoftInputMode));
                    pw.println(sb2.toString());
                    pw.print(prefix);
                    pw.println(" inputType=0x" + Integer.toHexString(entry.mEditorInfo.inputType) + " imeOptions=0x" + Integer.toHexString(entry.mEditorInfo.imeOptions) + " fieldId=0x" + Integer.toHexString(entry.mEditorInfo.fieldId) + " fieldName=" + entry.mEditorInfo.fieldName + " actionId=" + entry.mEditorInfo.actionId + " actionLabel=" + entry.mEditorInfo.actionLabel);
                }
            }
        }
    }

    private static class StartInputInfo {
        private static final AtomicInteger sSequenceNumber = new AtomicInteger(0);
        final int mClientBindSequenceNumber;
        final EditorInfo mEditorInfo;
        final String mImeId;
        final IBinder mImeToken;
        final boolean mRestarting;
        final int mSequenceNumber = sSequenceNumber.getAndIncrement();
        final int mStartInputReason;
        final IBinder mTargetWindow;
        final int mTargetWindowSoftInputMode;
        final long mTimestamp = SystemClock.uptimeMillis();
        final long mWallTime = System.currentTimeMillis();

        StartInputInfo(IBinder imeToken, String imeId, int startInputReason, boolean restarting, IBinder targetWindow, EditorInfo editorInfo, int targetWindowSoftInputMode, int clientBindSequenceNumber) {
            this.mImeToken = imeToken;
            this.mImeId = imeId;
            this.mStartInputReason = startInputReason;
            this.mRestarting = restarting;
            this.mTargetWindow = targetWindow;
            this.mEditorInfo = editorInfo;
            this.mTargetWindowSoftInputMode = targetWindowSoftInputMode;
            this.mClientBindSequenceNumber = clientBindSequenceNumber;
        }
    }

    /* access modifiers changed from: private */
    public void restoreNonVrImeFromSettingsNoCheck() {
        synchronized (this.mMethodMap) {
            String lastInputId = this.mSettings.getSelectedInputMethod();
            setInputMethodLocked(lastInputId, this.mSettings.getSelectedInputMethodSubtypeId(lastInputId));
        }
    }

    private void startVrInputMethodNoCheck(ComponentName component) {
        if (component == null) {
            restoreNonVrImeFromSettingsNoCheck();
            return;
        }
        synchronized (this.mMethodMap) {
            String packageName = component.getPackageName();
            Iterator<InputMethodInfo> it = this.mMethodList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                InputMethodInfo info = it.next();
                if (TextUtils.equals(info.getPackageName(), packageName) && info.isVrOnly()) {
                    setInputMethodEnabledLocked(info.getId(), true);
                    setInputMethodLocked(info.getId(), -1);
                    break;
                }
            }
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
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        return;
     */
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
        Context context2 = context;
        this.mHwIMMSEx = HwServiceExFactory.getHwInputMethodManagerServiceEx(this, context);
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mContext = context2;
        this.mRes = context.getResources();
        this.mHandler = new Handler(this);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mCaller = new HandlerCaller(context2, null, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                InputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mHardKeyboardListener = new HardKeyboardListener();
        this.mHasFeature = context.getPackageManager().hasSystemFeature("android.software.input_methods");
        this.mSlotIme = this.mContext.getString(17041189);
        this.mHardKeyboardBehavior = this.mContext.getResources().getInteger(17694786);
        Bundle extras = new Bundle();
        extras.putBoolean("android.allowDuringSetup", true);
        this.mImeSwitcherNotification = new Notification.Builder(this.mContext, DEVELOPER_CHANNEL).setSmallIcon(33751156).setWhen(0).setOngoing(true).addExtras(extras).setCategory("sys");
        Intent intent = new Intent(ACTION_INPUT_METHOD_PICKER).setPackage(this.mContext.getPackageName());
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.inputmethod.InputMethodDialogReceiver"));
        this.mImeSwitchPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
        this.mShowOngoingImeSwitcherForPhones = false;
        this.mNotificationShown = false;
        int userId = 0;
        try {
            userId = ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        InputMethodUtils.InputMethodSettings inputMethodSettings = new InputMethodUtils.InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, this.mMethodList, userId, !this.mSystemReady);
        this.mSettings = inputMethodSettings;
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, userId);
        this.mSwitchingController = InputMethodSubtypeSwitchingController.createInstanceLocked(this.mSettings, context2);
        IVrManager vrManager = ServiceManager.getService("vrmanager");
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to register VR mode state listener.");
            }
        }
        createFlagIfNecessary(userId);
    }

    private void resetDefaultImeLocked(Context context) {
    }

    @GuardedBy("mMethodMap")
    private void switchUserLocked(int newUserId) {
        createFlagIfNecessary(newUserId);
        this.mSettingsObserver.registerContentObserverLocked(newUserId);
        this.mSettings.switchCurrentUser(newUserId, !this.mSystemReady || !this.mUserManager.isUserUnlockingOrUnlocked(newUserId));
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, newUserId);
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
            try {
                startInputInnerLocked();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Unexpected exception", e);
            }
        }
        if (initialUserSwitch) {
            InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), newUserId, this.mContext.getBasePackageName());
        }
        switchUserExtra(newUserId);
    }

    /* access modifiers changed from: package-private */
    public void updateCurrentProfileIds() {
        this.mSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(this.mSettings.getCurrentUserId()));
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
        synchronized (this.mMethodMap) {
            if (!this.mSystemReady) {
                this.mSystemReady = true;
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
                int currentUserId = this.mSettings.getCurrentUserId();
                this.mSettings.switchCurrentUser(currentUserId, !this.mUserManager.isUserUnlockingOrUnlocked(currentUserId));
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                this.mStatusBar = statusBar;
                boolean z = false;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                this.mShowOngoingImeSwitcherForPhones = this.mRes.getBoolean(17957108);
                if (this.mShowOngoingImeSwitcherForPhones) {
                    this.mWindowManagerInternal.setOnHardKeyboardStatusChangeListener(this.mHardKeyboardListener);
                }
                this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
                this.mSettingsObserver.registerContentObserverLocked(currentUserId);
                IntentFilter broadcastFilter = new IntentFilter();
                broadcastFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                broadcastFilter.addAction("android.intent.action.USER_ADDED");
                broadcastFilter.addAction("android.intent.action.USER_REMOVED");
                broadcastFilter.addAction("android.intent.action.LOCALE_CHANGED");
                broadcastFilter.addAction(ACTION_SHOW_INPUT_METHOD_PICKER);
                this.mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
                String[] imePkgName = {"com.baidu.input_huawei", "com.touchtype.swiftkey", "com.swiftkey.swiftkeyconfigurator"};
                for (String defaultImeEnable : imePkgName) {
                    setDefaultImeEnable(defaultImeEnable);
                }
                if (!(!TextUtils.isEmpty(this.mSettings.getSelectedInputMethod()))) {
                    z = true;
                }
                buildInputMethodListLocked(z);
                resetDefaultImeLocked(this.mContext);
                updateFromSettingsLocked(true);
                InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), currentUserId, this.mContext.getBasePackageName());
                try {
                    startInputInnerLocked();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Unexpected exception", e);
                }
            }
        }
    }

    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == 1000 || this.mSettings.isCurrentProfile(userId) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return false;
    }

    private boolean calledWithValidToken(IBinder token) {
        if (token == null && Binder.getCallingPid() == Process.myPid()) {
            return false;
        }
        if (token != null && token == this.mCurToken) {
            return true;
        }
        Slog.e(TAG, "Ignoring " + Debug.getCaller() + " due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
        return false;
    }

    @GuardedBy("mMethodMap")
    private boolean bindCurrentInputMethodServiceLocked(Intent service, ServiceConnection conn, int flags) {
        if (service == null || conn == null) {
            Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
            return false;
        }
        if (this.mBindInstantServiceAllowed) {
            flags |= DumpState.DUMP_CHANGES;
        }
        return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
    }

    public List<InputMethodInfo> getInputMethodList() {
        return getInputMethodList(false);
    }

    public List<InputMethodInfo> getVrInputMethodList() {
        return getInputMethodList(true);
    }

    private List<InputMethodInfo> getInputMethodList(boolean isVrOnly) {
        ArrayList<InputMethodInfo> methodList;
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            methodList = new ArrayList<>();
            Iterator<InputMethodInfo> it = this.mMethodList.iterator();
            while (it.hasNext()) {
                InputMethodInfo info = it.next();
                if (info.isVrOnly() == isVrOnly) {
                    methodList.add(info);
                }
            }
        }
        return methodList;
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        ArrayList enabledInputMethodListLocked;
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            enabledInputMethodListLocked = this.mSettings.getEnabledInputMethodListLocked();
        }
        return enabledInputMethodListLocked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x002b A[Catch:{ all -> 0x001f }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0031 A[Catch:{ all -> 0x001f }] */
    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        InputMethodInfo imi;
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            if (imiId == null) {
                try {
                    if (this.mCurMethodId != null) {
                        imi = this.mMethodMap.get(this.mCurMethodId);
                        if (imi != null) {
                            List<InputMethodSubtype> emptyList = Collections.emptyList();
                            return emptyList;
                        }
                        List<InputMethodSubtype> enabledInputMethodSubtypeListLocked = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, allowsImplicitlySelectedSubtypes);
                        return enabledInputMethodSubtypeListLocked;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            imi = this.mMethodMap.get(imiId);
            if (imi != null) {
            }
        }
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int uid, int pid) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                this.mClients.put(client.asBinder(), new ClientState(client, inputContext, uid, pid));
            }
        }
    }

    public void removeClient(IInputMethodClient client) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                ClientState cs = this.mClients.remove(client.asBinder());
                if (cs != null) {
                    clearClientSessionLocked(cs);
                    if (this.mCurClient == cs) {
                        if (this.mBoundToMethod) {
                            this.mBoundToMethod = false;
                            if (this.mCurMethod != null) {
                                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(1000, this.mCurMethod));
                            }
                        }
                        this.mCurClient = null;
                    }
                    if (this.mCurFocusedWindowClient == cs) {
                        this.mCurFocusedWindowClient = null;
                    }
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
                if (this.mCurMethod != null) {
                    executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(1000, this.mCurMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_SET_ACTIVE, 0, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_UNBIND_CLIENT, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = false;
            this.mCurClient = null;
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

    /* access modifiers changed from: package-private */
    @GuardedBy("mMethodMap")
    public InputBindResult attachNewInputLocked(int startInputReason, boolean initial) {
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_BIND_INPUT, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        Binder startInputToken = new Binder();
        StartInputInfo startInputInfo = new StartInputInfo(this.mCurToken, this.mCurId, startInputReason, !initial, this.mCurFocusedWindow, this.mCurAttribute, this.mCurFocusedWindowSoftInputMode, this.mCurSeq);
        StartInputInfo info = startInputInfo;
        this.mStartInputMap.put(startInputToken, info);
        this.mStartInputHistory.addEntry(info);
        SessionState session = this.mCurClient.curSession;
        IInputMethod iInputMethod = session.method;
        HandlerCaller handlerCaller = this.mCaller;
        int i = this.mCurInputContextMissingMethods;
        IInputContext iInputContext = this.mCurInputContext;
        EditorInfo editorInfo = this.mCurAttribute;
        executeOrSendMessage(iInputMethod, handlerCaller.obtainMessageIIOOOO(2000, i, initial ^ true ? 1 : 0, startInputToken, session, iInputContext, editorInfo));
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
        InputChannel inputChannel2 = inputChannel;
        InputBindResult inputBindResult = new InputBindResult(0, iInputMethodSession, inputChannel2, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
        return inputBindResult;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mMethodMap")
    public InputBindResult startInputLocked(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return InputBindResult.NO_IME;
        }
        ClientState cs = this.mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client " + client.asBinder());
        } else if (attribute == null) {
            Slog.w(TAG, "Ignoring startInput with null EditorInfo. uid=" + cs.uid + " pid=" + cs.pid);
            return InputBindResult.NULL_EDITOR_INFO;
        } else {
            try {
                if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                    Slog.w(TAG, "Starting input on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                    return InputBindResult.NOT_IME_TARGET_WINDOW;
                }
            } catch (RemoteException e) {
            }
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mMethodMap")
    public InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int startInputReason) {
        ClientState clientState = cs;
        EditorInfo editorInfo = attribute;
        int i = controlFlags;
        if (this.mCurMethodId == null) {
            return InputBindResult.NO_IME;
        }
        if (!InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, clientState.uid, editorInfo.packageName)) {
            Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + clientState.uid + " package=" + editorInfo.packageName);
            return InputBindResult.INVALID_PACKAGE_NAME;
        }
        if (this.mCurClient != clientState) {
            this.mCurClientInKeyguard = isKeyguardLocked();
            unbindCurrentClientLocked(1);
            if (this.mIsInteractive) {
                executeOrSendMessage(clientState.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, clientState));
            }
        }
        this.mCurSeq++;
        if (this.mCurSeq <= 0) {
            this.mCurSeq = 1;
        }
        this.mCurClient = clientState;
        this.mCurInputContext = inputContext;
        this.mCurInputContextMissingMethods = missingMethods;
        this.mCurAttribute = editorInfo;
        if (this.mCurId == null || !this.mCurId.equals(this.mCurMethodId)) {
            int i2 = startInputReason;
        } else {
            boolean z = false;
            if (clientState.curSession != null) {
                if ((65536 & i) != 0) {
                    this.mShowRequested = true;
                }
                if ((i & 256) != 0) {
                    z = true;
                }
                return attachNewInputLocked(startInputReason, z);
            }
            int i3 = startInputReason;
            if (this.mHaveConnection) {
                if (this.mCurMethod != null) {
                    requestClientSessionLocked(cs);
                    InputBindResult inputBindResult = new InputBindResult(1, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    return inputBindResult;
                } else if (SystemClock.uptimeMillis() < this.mLastBindTime + TIME_TO_RECONNECT) {
                    InputBindResult inputBindResult2 = new InputBindResult(2, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    return inputBindResult2;
                } else {
                    EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 0});
                }
            }
        }
        try {
            return startInputInnerLocked();
        } catch (RuntimeException e) {
            RuntimeException runtimeException = e;
            Slog.w(TAG, "Unexpected exception", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public InputBindResult startInputInnerLocked() {
        if (this.mCurMethodId == null || !this.mMethodMap.containsKey(this.mCurMethodId)) {
            return InputBindResult.NO_IME;
        }
        if (!this.mSystemReady) {
            InputBindResult inputBindResult = new InputBindResult(7, null, null, this.mCurMethodId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            return inputBindResult;
        }
        InputMethodInfo info = this.mMethodMap.get(this.mCurMethodId);
        if (info == null) {
            Slog.w(TAG, "info == null id: " + this.mCurMethodId);
            return InputBindResult.NO_IME;
        }
        unbindCurrentMethodLocked(true);
        this.mCurIntent = new Intent("android.view.InputMethod");
        this.mCurIntent.setComponent(info.getComponent());
        this.mCurIntent.putExtra("android.intent.extra.client_label", 17040222);
        this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
        if (bindCurrentInputMethodServiceLocked(this.mCurIntent, this, IME_CONNECTION_BIND_FLAGS)) {
            this.mLastBindTime = SystemClock.uptimeMillis();
            this.mHaveConnection = true;
            this.mCurId = info.getId();
            this.mCurToken = new Binder();
            try {
                this.mIWindowManager.addWindowToken(this.mCurToken, 2011, 0);
            } catch (RemoteException e) {
            }
            InputBindResult inputBindResult2 = new InputBindResult(2, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            return inputBindResult2;
        }
        this.mCurIntent = null;
        Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
        return InputBindResult.IME_NOT_CONNECTED;
    }

    /* access modifiers changed from: protected */
    public InputBindResult startInput(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        InputBindResult startInputLocked;
        if (!calledFromValidUser()) {
            return InputBindResult.INVALID_USER;
        }
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                startInputLocked = startInputLocked(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return startInputLocked;
    }

    public void finishInput(IInputMethodClient client) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        return;
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(false);
                    return;
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Initiating attach with token: " + this.mCurToken);
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_ATTACH_TOKEN, this.mCurMethod, this.mCurToken));
                if (this.mCurClient != null) {
                    clearClientSessionLocked(this.mCurClient);
                    requestClientSessionLocked(this.mCurClient);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0053, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        r9.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
        return;
     */
    public void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod != null && method != null && this.mCurMethod.asBinder() == method.asBinder() && this.mCurClient != null) {
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
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentMethodLocked(boolean savePosition) {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = false;
        }
        if (this.mCurToken != null) {
            try {
                if ((this.mImeWindowVis & 1) != 0 && savePosition) {
                    this.mWindowManagerInternal.saveLastInputMethodWindowForTransition();
                }
                this.mIWindowManager.removeWindowToken(this.mCurToken, 0);
            } catch (RemoteException e) {
            }
            this.mCurToken = null;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    /* access modifiers changed from: package-private */
    public void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(false);
        unbindCurrentClientLocked(unbindClientReason);
    }

    /* access modifiers changed from: package-private */
    public void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[1], new MethodCallback(this, this.mCurMethod, channels[0])));
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
                    updateSystemUiLocked(this.mCurToken, 0, this.mBackDisposition);
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
            for (ClientState cs : this.mClients.values()) {
                clearClientSessionLocked(cs);
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        if (this.mStatusBar != null) {
            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
        }
        this.mInFullscreenMode = false;
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = false;
                unbindCurrentClientLocked(3);
                if (HwPCUtils.isPcCastModeInServer()) {
                    unbindCurrentMethodLocked(false);
                    this.mLastUnBindInputMethodInPCMode = true;
                }
                if (HwPCUtils.enabledInPad() && !HwPCUtils.isPcCastModeInServer() && this.mLastUnBindInputMethodInPCMode) {
                    unbindCurrentMethodLocked(false);
                    this.mLastUnBindInputMethodInPCMode = false;
                }
            }
        }
    }

    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        synchronized (this.mMethodMap) {
            if (calledWithValidToken(token)) {
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
                    String str = null;
                    CharSequence contentDescription = null;
                    try {
                        contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                    } catch (RemoteException e) {
                    }
                    if (this.mStatusBar != null) {
                        StatusBarManagerService statusBarManagerService = this.mStatusBar;
                        String str2 = this.mSlotIme;
                        if (contentDescription != null) {
                            str = contentDescription.toString();
                        }
                        statusBarManagerService.setIcon(str2, packageName, iconId, 0, str);
                        this.mStatusBar.setIconVisibility(this.mSlotIme, true);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        if (!this.mShowOngoingImeSwitcherForPhones || this.mSwitchingDialog != null) {
            return false;
        }
        if ((this.mWindowManagerInternal.isKeyguardShowingAndNotOccluded() && this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardSecure()) || (visibility & 1) == 0) {
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
        InputMethodSubtype nonAuxSubtype = null;
        InputMethodSubtype auxSubtype = null;
        int nonAuxCount = 0;
        int nonAuxCount2 = 0;
        for (int i = 0; i < N; i++) {
            List<InputMethodSubtype> subtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imis.get(i), true);
            int subtypeCount = subtypes.size();
            if (subtypeCount == 0) {
                nonAuxCount2++;
            } else {
                InputMethodSubtype auxSubtype2 = nonAuxSubtype;
                InputMethodSubtype nonAuxSubtype2 = auxSubtype;
                int auxCount = nonAuxCount;
                int nonAuxCount3 = nonAuxCount2;
                for (int j = 0; j < subtypeCount; j++) {
                    InputMethodSubtype subtype = subtypes.get(j);
                    if (!subtype.isAuxiliary()) {
                        nonAuxCount3++;
                        nonAuxSubtype2 = subtype;
                    } else {
                        auxCount++;
                        auxSubtype2 = subtype;
                    }
                }
                nonAuxCount2 = nonAuxCount3;
                nonAuxCount = auxCount;
                auxSubtype = nonAuxSubtype2;
                nonAuxSubtype = auxSubtype2;
            }
        }
        if (nonAuxCount2 > 1 || nonAuxCount > 1) {
            return true;
        }
        if (nonAuxCount2 != 1 || nonAuxCount != 1) {
            return false;
        }
        if (auxSubtype == null || nonAuxSubtype == null || ((!auxSubtype.getLocale().equals(nonAuxSubtype.getLocale()) && !nonAuxSubtype.overridesImplicitlyEnabledSubtype() && !auxSubtype.overridesImplicitlyEnabledSubtype()) || !auxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER))) {
            return true;
        }
        return false;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked();
    }

    public void setImeWindowStatus(IBinder token, IBinder startInputToken, int vis, int backDisposition) {
        StartInputInfo info;
        boolean dismissImeOnBackKeyPressed;
        if (calledWithValidToken(token)) {
            synchronized (this.mMethodMap) {
                info = this.mStartInputMap.get(startInputToken);
                this.mImeWindowVis = vis;
                this.mBackDisposition = backDisposition;
                updateSystemUiLocked(token, vis, backDisposition);
            }
            boolean z = false;
            switch (backDisposition) {
                case 1:
                    dismissImeOnBackKeyPressed = false;
                    break;
                case 2:
                    dismissImeOnBackKeyPressed = true;
                    break;
                default:
                    if ((vis & 2) == 0) {
                        dismissImeOnBackKeyPressed = false;
                        break;
                    } else {
                        dismissImeOnBackKeyPressed = true;
                        break;
                    }
            }
            WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
            if ((vis & 2) != 0) {
                z = true;
            }
            windowManagerInternal.updateInputMethodWindowStatus(token, z, dismissImeOnBackKeyPressed, info != null ? info.mTargetWindow : null);
        }
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0030 A[SYNTHETIC, Splitter:B:17:0x0030] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0045  */
    public void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        int vis2;
        boolean needsToShowImeSwitcher;
        InputMethodInfo imi;
        if (calledWithValidToken(token)) {
            long ident = Binder.clearCallingIdentity();
            if (vis != 0) {
                try {
                    if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
                        vis2 = 0;
                        needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis2);
                        if (this.mStatusBar == null) {
                            try {
                                try {
                                    this.mStatusBar.setImeWindowStatus(token, vis2, backDisposition, needsToShowImeSwitcher);
                                } catch (Throwable th) {
                                    th = th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                IBinder iBinder = token;
                                int i = backDisposition;
                                Binder.restoreCallingIdentity(ident);
                                throw th;
                            }
                        } else {
                            IBinder iBinder2 = token;
                            int i2 = backDisposition;
                        }
                        try {
                            imi = this.mMethodMap.get(this.mCurMethodId);
                            boolean isGestureNavigation = false;
                            if (imi != null || !needsToShowImeSwitcher) {
                                boolean z = needsToShowImeSwitcher;
                                try {
                                    if (this.mNotificationShown && this.mNotificationManager != null) {
                                        this.mNotificationManager.cancelAsUser(null, 8, UserHandle.ALL);
                                        this.mNotificationShown = false;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    Binder.restoreCallingIdentity(ident);
                                    throw th;
                                }
                            } else {
                                try {
                                    boolean isEnableNavBar = Settings.System.getIntForUser(this.mContext.getContentResolver(), "enable_navbar", getNaviBarEnabledDefValue(), -2) != 0;
                                    if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "secure_gesture_navigation", 0, -2) != 0) {
                                        isGestureNavigation = true;
                                    }
                                    Slog.i(TAG, "--- show notification config: mIWindowManager.hasNavigationBar() =  " + this.mIWindowManager.hasNavigationBar() + " ,isEnableNavBar = " + isEnableNavBar + " ,isGestureNavigation = " + isGestureNavigation);
                                    if (this.mNotificationManager != null) {
                                        if (this.mIWindowManager.hasNavigationBar() && isEnableNavBar) {
                                            if (!isGestureNavigation) {
                                                int i3 = vis2;
                                                boolean z2 = needsToShowImeSwitcher;
                                            }
                                        }
                                        this.mImeSwitcherNotification.setContentTitle(this.mRes.getText(17041062)).setContentText(InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, imi, this.mCurrentSubtype)).setContentIntent(this.mImeSwitchPendingIntent);
                                        int i4 = vis2;
                                        boolean z3 = needsToShowImeSwitcher;
                                        try {
                                            this.mNotificationManager.notifyAsUser(null, 8, this.mImeSwitcherNotification.build(), UserHandle.ALL);
                                            this.mNotificationShown = true;
                                        } catch (RemoteException e) {
                                        }
                                    } else {
                                        boolean z4 = needsToShowImeSwitcher;
                                    }
                                } catch (RemoteException e2) {
                                    int i5 = vis2;
                                    boolean z5 = needsToShowImeSwitcher;
                                }
                            }
                            Binder.restoreCallingIdentity(ident);
                        } catch (Throwable th4) {
                            th = th4;
                            int i6 = vis2;
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    IBinder iBinder3 = token;
                    int i7 = vis;
                    int i8 = backDisposition;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            vis2 = vis;
            try {
                needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis2);
                if (this.mStatusBar == null) {
                }
                imi = this.mMethodMap.get(this.mCurMethodId);
                boolean isGestureNavigation2 = false;
                if (imi != null) {
                }
                boolean z6 = needsToShowImeSwitcher;
                this.mNotificationManager.cancelAsUser(null, 8, UserHandle.ALL);
                this.mNotificationShown = false;
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th6) {
                th = th6;
                IBinder iBinder4 = token;
                int i9 = backDisposition;
                int i62 = vis2;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = this.mMethodMap.get(this.mCurMethodId);
                for (SuggestionSpan ss : spans) {
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0061, code lost:
        return false;
     */
    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo targetImi = this.mSecureSuggestionSpans.get(span);
            if (targetImi == null) {
                return false;
            }
            String[] suggestions = span.getSuggestions();
            if (index >= 0) {
                if (index < suggestions.length) {
                    String className = span.getNotificationTargetClassName();
                    Intent intent = new Intent();
                    intent.setClassName(targetImi.getPackageName(), className);
                    intent.setAction("android.text.style.SUGGESTION_PICKED");
                    intent.putExtra("before", originalString);
                    intent.putExtra("after", suggestions[index]);
                    intent.putExtra("hashcode", span.hashCode());
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                        Binder.restoreCallingIdentity(ident);
                        return true;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                }
            }
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
            this.mShortcutInputMethodsAndSubtypes.clear();
        } else {
            resetCurrentMethodAndClient(4);
        }
        this.mSwitchingController.resetCircularListLocked(this.mContext);
    }

    public void updateKeyboardFromSettingsLocked() {
        this.mShowImeWithHardKeyboard = this.mSettings.isShowImeWithHardKeyboardEnabled();
        if (this.mSwitchingDialog != null && this.mSwitchingDialogTitleView != null && this.mSwitchingDialog.isShowing()) {
            Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(16908954);
            if (hardKeySwitch != null) {
                hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodLocked(String id, int subtypeId) {
        InputMethodSubtype newSubtype;
        InputMethodInfo info = this.mMethodMap.get(id);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + id);
        } else if (id.equals(this.mCurMethodId)) {
            this.mIsDiffIME = false;
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
                    return;
                }
                if (newSubtype != oldSubtype) {
                    setSelectedInputMethodAndSubtypeLocked(info, subtypeId, true);
                    if (this.mCurMethod != null) {
                        try {
                            updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                            this.mCurMethod.changeInputMethodSubtype(newSubtype);
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Failed to call changeInputMethodSubtype");
                        }
                    }
                }
            }
        } else {
            this.mIsDiffIME = true;
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
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                }
                boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return showCurrentInputLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mLastInputShown = false;
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
        boolean res = false;
        if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
            }
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(MSG_SHOW_SOFT_INPUT, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                bindCurrentInputMethodServiceLocked(this.mCurIntent, this.mVisibleConnection, IME_VISIBLE_BIND_FLAGS);
                this.mVisibleBound = true;
            }
            res = true;
        } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + TIME_TO_RECONNECT) {
            EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 1});
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodServiceLocked(this.mCurIntent, this, IME_CONNECTION_BIND_FLAGS);
        }
        return res;
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) {
                    if (!LAUNCHER_FORCE_HIDE_SOFT || flags != 1000) {
                        try {
                            if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                                Slog.w(TAG, "Ignoring hideSoftInput of uid " + uid + ": " + client);
                                Binder.restoreCallingIdentity(ident);
                                return false;
                            }
                        } catch (RemoteException e) {
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    } else {
                        Slog.i(TAG, "force hideSoftInput pid " + pid);
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be hidden, pid=" + pid);
                }
                boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return hideCurrentInputLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        boolean res;
        if (this.mLastInputShown && this.mIsDiffIME) {
            Slog.i(TAG, "cancel hide ");
            this.mLastInputShown = false;
            return false;
        } else if ((flags & 1) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            return false;
        } else {
            if (this.mShowForced && (flags & 2) != 0) {
                return false;
            }
            boolean shouldHideSoftInput = true;
            if (this.mCurMethod == null || (!this.mInputShown && (this.mImeWindowVis & 1) == 0)) {
                shouldHideSoftInput = false;
            }
            if (shouldHideSoftInput) {
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_HIDE_SOFT_INPUT, this.mCurMethod, resultReceiver));
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
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        InputBindResult result;
        if (windowToken != null) {
            result = windowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
        } else {
            result = startInput(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
        }
        if (result == null) {
            Slog.wtf(TAG, "InputBindResult is @NonNull. startInputReason=" + InputMethodClient.getStartInputReason(startInputReason) + " windowFlags=#" + Integer.toHexString(windowFlags) + " editorInfo=" + attribute);
            return InputBindResult.NULL;
        }
        EditorInfo editorInfo = attribute;
        return result;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01ad A[Catch:{ all -> 0x0149, all -> 0x01a8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01c6 A[Catch:{ all -> 0x0149, all -> 0x01a8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01cb A[Catch:{ all -> 0x0149, all -> 0x01a8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x021e A[Catch:{ all -> 0x0149, all -> 0x01a8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f6  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0108 A[SYNTHETIC, Splitter:B:62:0x0108] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0156 A[Catch:{ all -> 0x0149, all -> 0x01a8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0193 A[Catch:{ all -> 0x0149, all -> 0x01a8 }] */
    public InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        long ident;
        HashMap<String, InputMethodInfo> hashMap;
        long ident2;
        boolean z;
        boolean didStart;
        long ident3;
        ResultReceiver resultReceiver;
        long ident4;
        int i;
        IBinder iBinder = windowToken;
        int i2 = controlFlags;
        int i3 = softInputMode;
        int i4 = unverifiedTargetSdkVersion;
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult res = null;
        long ident5 = Binder.clearCallingIdentity();
        try {
            HashMap<String, InputMethodInfo> hashMap2 = this.mMethodMap;
            synchronized (hashMap2) {
                try {
                    ClientState cs = this.mClients.get(client.asBinder());
                    if (cs != null) {
                        try {
                            if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                                Slog.w(TAG, "Focus gain on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                                InputBindResult inputBindResult = InputBindResult.NOT_IME_TARGET_WINDOW;
                                try {
                                    Binder.restoreCallingIdentity(ident5);
                                    return inputBindResult;
                                } catch (Throwable th) {
                                    th = th;
                                    hashMap = hashMap2;
                                    ident = ident5;
                                    int i5 = i4;
                                    boolean z2 = calledFromValidUser;
                                    try {
                                        throw th;
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                        }
                        if (!calledFromValidUser) {
                            Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                            Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                            hideCurrentInputLocked(0, null);
                            InputBindResult inputBindResult2 = InputBindResult.INVALID_USER;
                            Binder.restoreCallingIdentity(ident5);
                            return inputBindResult2;
                        } else if (this.mCurFocusedWindow != iBinder) {
                            hashMap = hashMap2;
                            ident2 = ident5;
                            boolean z3 = calledFromValidUser;
                            ClientState cs2 = cs;
                            try {
                                this.mCurFocusedWindow = iBinder;
                                this.mCurFocusedWindowSoftInputMode = i3;
                                this.mCurFocusedWindowClient = cs2;
                                if ((i3 & 240) != 16) {
                                    if (!this.mRes.getConfiguration().isLayoutSizeAtLeast(3)) {
                                        z = false;
                                        boolean doAutoShow = z;
                                        boolean isTextEditor = (i2 & 2) == 0;
                                        didStart = false;
                                        switch (i3 & 15) {
                                            case 0:
                                                ident3 = ident2;
                                                int i6 = unverifiedTargetSdkVersion;
                                                if (isTextEditor) {
                                                    if (doAutoShow) {
                                                        if (isTextEditor && doAutoShow && (i3 & 256) != 0) {
                                                            if (DEBUG_FLOW) {
                                                                Slog.v(TAG, "Unspecified window will show input");
                                                            }
                                                            if (attribute != null) {
                                                                resultReceiver = null;
                                                                res = startInputUncheckedLocked(cs2, inputContext, missingMethods, attribute, i2, startInputReason);
                                                                didStart = true;
                                                            } else {
                                                                resultReceiver = null;
                                                            }
                                                            showCurrentInputLocked(1, resultReceiver);
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (WindowManager.LayoutParams.mayUseInputMethod(windowFlags)) {
                                                    if (DEBUG_FLOW) {
                                                        Slog.v(TAG, "Unspecified window will hide input");
                                                    }
                                                    hideCurrentInputLocked(2, null);
                                                    break;
                                                }
                                                break;
                                            case 1:
                                                ident3 = ident2;
                                                int i7 = unverifiedTargetSdkVersion;
                                                break;
                                            case 2:
                                                ident3 = ident2;
                                                int i8 = unverifiedTargetSdkVersion;
                                                if ((i3 & 256) != 0) {
                                                    if (DEBUG_FLOW) {
                                                        Slog.v(TAG, "Window asks to hide input going forward");
                                                    }
                                                    hideCurrentInputLocked(0, null);
                                                    break;
                                                }
                                                break;
                                            case 3:
                                                ident3 = ident2;
                                                int i9 = unverifiedTargetSdkVersion;
                                                if (DEBUG_FLOW) {
                                                    Slog.v(TAG, "Window asks to hide input");
                                                }
                                                hideCurrentInputLocked(0, null);
                                                break;
                                            case 4:
                                                ident3 = ident2;
                                                int i10 = unverifiedTargetSdkVersion;
                                                if ((i3 & 256) != 0) {
                                                    if (DEBUG_FLOW) {
                                                        Slog.v(TAG, "Window asks to show input going forward");
                                                    }
                                                    if (!InputMethodUtils.isSoftInputModeStateVisibleAllowed(i10, i2)) {
                                                        Slog.e(TAG, "SOFT_INPUT_STATE_VISIBLE is ignored because there is no focused view that also returns true from View#onCheckIsTextEditor()");
                                                        break;
                                                    } else {
                                                        if (attribute != null) {
                                                            didStart = true;
                                                            res = startInputUncheckedLocked(cs2, inputContext, missingMethods, attribute, i2, startInputReason);
                                                        }
                                                        showCurrentInputLocked(1, null);
                                                        break;
                                                    }
                                                }
                                                break;
                                            case 5:
                                                try {
                                                    if (DEBUG_FLOW) {
                                                        Slog.v(TAG, "Window asks to always show input");
                                                    }
                                                    ident4 = ident2;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    long j = ident2;
                                                    int i11 = unverifiedTargetSdkVersion;
                                                    ident = j;
                                                    throw th;
                                                }
                                                try {
                                                    if (!InputMethodUtils.isSoftInputModeStateVisibleAllowed(unverifiedTargetSdkVersion, i2)) {
                                                        ident3 = ident4;
                                                        Slog.e(TAG, "SOFT_INPUT_STATE_ALWAYS_VISIBLE is ignored because there is no focused view that also returns true from View#onCheckIsTextEditor()");
                                                        break;
                                                    } else {
                                                        if (attribute != null) {
                                                            ident3 = ident4;
                                                            i = 1;
                                                            didStart = true;
                                                            res = startInputUncheckedLocked(cs2, inputContext, missingMethods, attribute, i2, startInputReason);
                                                        } else {
                                                            ident3 = ident4;
                                                            i = 1;
                                                        }
                                                        showCurrentInputLocked(i, null);
                                                        break;
                                                    }
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    ident = ident3;
                                                    throw th;
                                                }
                                            default:
                                                ident3 = ident2;
                                                int i12 = unverifiedTargetSdkVersion;
                                                break;
                                        }
                                        if (!didStart) {
                                            if (attribute != null) {
                                                if (DebugFlags.FLAG_OPTIMIZE_START_INPUT.value()) {
                                                    if ((i2 & 2) == 0) {
                                                        res = InputBindResult.NO_EDITOR;
                                                    }
                                                }
                                                res = startInputUncheckedLocked(cs2, inputContext, missingMethods, attribute, i2, startInputReason);
                                            } else {
                                                res = InputBindResult.NULL_EDITOR_INFO;
                                            }
                                        }
                                        Binder.restoreCallingIdentity(ident3);
                                        return res;
                                    }
                                }
                                z = true;
                                boolean doAutoShow2 = z;
                                boolean isTextEditor2 = (i2 & 2) == 0;
                                didStart = false;
                                switch (i3 & 15) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                    case 4:
                                        break;
                                    case 5:
                                        break;
                                }
                                if (!didStart) {
                                }
                                try {
                                    Binder.restoreCallingIdentity(ident3);
                                    return res;
                                } catch (Throwable th5) {
                                    th = th5;
                                    ident = ident3;
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                ident = ident2;
                                int i13 = unverifiedTargetSdkVersion;
                                throw th;
                            }
                        } else if (attribute != null) {
                            boolean z4 = calledFromValidUser;
                            ClientState clientState = cs;
                            hashMap = hashMap2;
                            ident2 = ident5;
                            try {
                                InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, i2, startInputReason);
                                Binder.restoreCallingIdentity(ident2);
                                return startInputUncheckedLocked;
                            } catch (Throwable th7) {
                                th = th7;
                                ident = ident2;
                                int i14 = unverifiedTargetSdkVersion;
                                throw th;
                            }
                        } else {
                            hashMap = hashMap2;
                            ident2 = ident5;
                            boolean z5 = calledFromValidUser;
                            ClientState clientState2 = cs;
                            InputBindResult inputBindResult3 = new InputBindResult(3, null, null, null, -1, -1);
                            Binder.restoreCallingIdentity(ident2);
                            return inputBindResult3;
                        }
                    } else {
                        hashMap = hashMap2;
                        ident = ident5;
                        int i15 = i4;
                        boolean z6 = calledFromValidUser;
                        ClientState clientState3 = cs;
                        throw new IllegalArgumentException("unknown client " + client.asBinder());
                    }
                } catch (Throwable th8) {
                    th = th8;
                    throw th;
                }
            }
        } catch (Throwable th9) {
            th = th9;
            ident = ident5;
            int i16 = i4;
            boolean z7 = calledFromValidUser;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    private boolean canShowInputMethodPickerLocked(IInputMethodClient client) {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) == 1000) {
            return true;
        }
        if (this.mCurFocusedWindowClient != null && client != null && this.mCurFocusedWindowClient.client.asBinder() == client.asBinder()) {
            return true;
        }
        if ((this.mCurIntent == null || !InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, uid, this.mCurIntent.getComponent().getPackageName())) && this.mContext.checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            return false;
        }
        return true;
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!canShowInputMethodPickerLocked(client)) {
                    Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid " + Binder.getCallingUid() + ": " + client);
                    return;
                }
                Slog.d(TAG, Binder.getCallingPid() + ":request to show input method dialog");
                this.mHandler.sendMessage(this.mCaller.obtainMessageII(1, auxiliarySubtypeMode, Binder.getCallingPid()));
            }
        }
    }

    public boolean isInputMethodPickerShownForTest() {
        synchronized (this.mMethodMap) {
            if (this.mSwitchingDialog == null) {
                return false;
            }
            boolean isShowing = this.mSwitchingDialog.isShowing();
            return isShowing;
        }
    }

    public void setInputMethod(IBinder token, String id) {
        if (calledFromValidUser()) {
            setInputMethodWithSubtypeId(token, id, -1);
        }
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (subtype != null) {
                    try {
                        setInputMethodWithSubtypeIdLocked(token, id, InputMethodUtils.getSubtypeIdFromHashCode(this.mMethodMap.get(id), subtype.hashCode()));
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    setInputMethod(token, id);
                }
            }
        }
    }

    public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(2, inputMethodId));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00d7 A[Catch:{ all -> 0x00db, all -> 0x00e0 }] */
    public boolean switchToPreviousInputMethod(IBinder token) {
        InputMethodInfo lastImi;
        int subtypeId;
        String targetLastImiId;
        String locale;
        int currentSubtypeHash;
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            try {
                Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
                if (lastIme != null) {
                    lastImi = this.mMethodMap.get(lastIme.first);
                } else {
                    lastImi = null;
                }
                String targetLastImiId2 = null;
                int subtypeId2 = -1;
                if (!(lastIme == null || lastImi == null)) {
                    boolean imiIdIsSame = lastImi.getId().equals(this.mCurMethodId);
                    int lastSubtypeHash = Integer.parseInt((String) lastIme.second);
                    if (this.mCurrentSubtype == null) {
                        currentSubtypeHash = -1;
                    } else {
                        currentSubtypeHash = this.mCurrentSubtype.hashCode();
                    }
                    if (!imiIdIsSame || lastSubtypeHash != currentSubtypeHash) {
                        targetLastImiId2 = (String) lastIme.first;
                        subtypeId2 = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, lastSubtypeHash);
                    }
                }
                if (TextUtils.isEmpty(targetLastImiId2) && !InputMethodUtils.canAddToLastInputMethod(this.mCurrentSubtype)) {
                    List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
                    if (enabled != null) {
                        int N = enabled.size();
                        if (this.mCurrentSubtype == null) {
                            locale = this.mRes.getConfiguration().locale.toString();
                        } else {
                            locale = this.mCurrentSubtype.getLocale();
                        }
                        subtypeId = subtypeId2;
                        targetLastImiId = targetLastImiId2;
                        int i = 0;
                        while (true) {
                            if (i >= N) {
                                break;
                            }
                            InputMethodInfo imi = enabled.get(i);
                            if (imi.getSubtypeCount() > 0 && InputMethodUtils.isSystemIme(imi)) {
                                InputMethodSubtype keyboardSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, InputMethodUtils.getSubtypes(imi), "keyboard", locale, true);
                                if (keyboardSubtype != null) {
                                    targetLastImiId = imi.getId();
                                    subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, keyboardSubtype.hashCode());
                                    if (keyboardSubtype.getLocale().equals(locale)) {
                                        break;
                                    }
                                } else {
                                    continue;
                                }
                            }
                            i++;
                        }
                        if (TextUtils.isEmpty(targetLastImiId)) {
                            setInputMethodWithSubtypeIdLocked(token, targetLastImiId, subtypeId);
                            return true;
                        }
                        IBinder iBinder = token;
                        return false;
                    }
                }
                subtypeId = subtypeId2;
                targetLastImiId = targetLastImiId2;
                if (TextUtils.isEmpty(targetLastImiId)) {
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (!calledWithValidToken(token)) {
                return false;
            }
            InputMethodSubtypeSwitchingController.ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(onlyCurrentIme, this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true);
            if (nextSubtype == null) {
                return false;
            }
            setInputMethodWithSubtypeIdLocked(token, nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
            return true;
        }
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (!calledWithValidToken(token)) {
                return false;
            }
            if (this.mSwitchingController.getNextInputMethodLocked(false, this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true) == null) {
                return false;
            }
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0053, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0058, code lost:
        return null;
     */
    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
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
                                InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                                return subtypeAt;
                            }
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005f, code lost:
        return;
     */
    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        if (calledFromValidUser() && !TextUtils.isEmpty(imiId) && subtypes != null) {
            synchronized (this.mMethodMap) {
                if (this.mSystemReady) {
                    InputMethodInfo imi = this.mMethodMap.get(imiId);
                    if (imi != null) {
                        try {
                            String[] packageInfos = this.mIPackageManager.getPackagesForUid(Binder.getCallingUid());
                            if (packageInfos != null) {
                                int packageNum = packageInfos.length;
                                int i = 0;
                                while (i < packageNum) {
                                    if (packageInfos[i].equals(imi.getPackageName())) {
                                        this.mFileManager.addInputMethodSubtypes(imi, subtypes);
                                        long ident = Binder.clearCallingIdentity();
                                        try {
                                            buildInputMethodListLocked(false);
                                            return;
                                        } finally {
                                            Binder.restoreCallingIdentity(ident);
                                        }
                                    } else {
                                        i++;
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

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight();
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    this.mWindowManagerInternal.clearLastInputMethodWindowForTransition();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        return;
     */
    public void notifyUserAction(int sequenceNumber) {
        synchronized (this.mMethodMap) {
            if (this.mCurUserActionNotificationSequenceNumber == sequenceNumber) {
                InputMethodInfo imi = this.mMethodMap.get(this.mCurMethodId);
                if (imi != null) {
                    this.mSwitchingController.onUserActionLocked(imi, this.mCurrentSubtype);
                }
            }
        }
    }

    private void setInputMethodWithSubtypeId(IBinder token, String id, int subtypeId) {
        synchronized (this.mMethodMap) {
            setInputMethodWithSubtypeIdLocked(token, id, subtypeId);
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

    public void hideMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
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
    }

    public void showMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        showCurrentInputLocked(flags, null);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setEnabledSessionInMainThread(SessionState session) {
        if (this.mEnabledSession != session) {
            if (!(this.mEnabledSession == null || this.mEnabledSession.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            this.mEnabledSession = session;
            if (this.mEnabledSession != null && this.mEnabledSession.session != null) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, true);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:103:0x01b7, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x01b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x01b9, code lost:
        r3.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00fd, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ff, code lost:
        r3.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0128, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01a0, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x01b9;
     */
    public boolean handleMessage(Message msg) {
        boolean showAuxSubtypes;
        boolean fullscreen = false;
        switch (msg.what) {
            case 1:
                switch (msg.arg1) {
                    case 0:
                        showAuxSubtypes = this.mInputShown;
                        break;
                    case 1:
                        showAuxSubtypes = true;
                        break;
                    case 2:
                        showAuxSubtypes = false;
                        break;
                    default:
                        Slog.e(TAG, "Unknown subtype picker mode = " + msg.arg1);
                        return false;
                }
                showInputMethodMenu(showAuxSubtypes, msg.arg2);
                return true;
            case 2:
                showInputMethodAndSubtypeEnabler((String) msg.obj);
                return true;
            case 3:
                showConfigureInputMethods();
                return true;
            case 1000:
                try {
                    ((IInputMethod) msg.obj).unbindInput();
                } catch (RemoteException e) {
                }
                return true;
            case MSG_BIND_INPUT /*1010*/:
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                }
                args.recycle();
                return true;
            case MSG_SHOW_SOFT_INPUT /*1020*/:
                SomeArgs args2 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args2.arg1).showSoftInput(msg.arg1, (ResultReceiver) args2.arg2);
                } catch (RemoteException e3) {
                }
                args2.recycle();
                return true;
            case MSG_HIDE_SOFT_INPUT /*1030*/:
                SomeArgs args3 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args3.arg1).hideSoftInput(0, (ResultReceiver) args3.arg2);
                } catch (RemoteException e4) {
                }
                args3.recycle();
                return true;
            case MSG_HIDE_CURRENT_INPUT_METHOD /*1035*/:
                synchronized (this.mMethodMap) {
                    hideCurrentInputLocked(0, null);
                }
                return true;
            case MSG_ATTACH_TOKEN /*1040*/:
                SomeArgs args4 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args4.arg1).attachToken((IBinder) args4.arg2);
                } catch (RemoteException e5) {
                }
                args4.recycle();
                return true;
            case MSG_CREATE_SESSION /*1050*/:
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
                    session.method.startInput(startInputToken, inputContext, missingMethods, editorInfo, restarting);
                } catch (RemoteException e7) {
                }
                args6.recycle();
                return true;
            case MSG_START_VR_INPUT /*2010*/:
                startVrInputMethodNoCheck((ComponentName) msg.obj);
                return true;
            case MSG_UNBIND_CLIENT /*3000*/:
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
            case MSG_SET_ACTIVE /*3020*/:
                try {
                    IInputMethodClient iInputMethodClient = ((ClientState) msg.obj).client;
                    boolean z = msg.arg1 != 0;
                    if (msg.arg2 != 0) {
                        fullscreen = true;
                    }
                    iInputMethodClient.setActive(z, fullscreen);
                } catch (RemoteException e10) {
                    Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid " + ((ClientState) msg.obj).pid + " uid " + ((ClientState) msg.obj).uid);
                }
                return true;
            case MSG_SET_INTERACTIVE /*3030*/:
                if (msg.arg1 != 0) {
                    fullscreen = true;
                }
                handleSetInteractive(fullscreen);
                return true;
            case 3040:
                try {
                    ((ClientState) msg.obj).client.setUserActionNotificationSequenceNumber(msg.arg1);
                } catch (RemoteException e11) {
                    Slog.w(TAG, "Got RemoteException sending setUserActionNotificationSequenceNumber(" + sequenceNumber + ") notification to pid " + clientState.pid + " uid " + clientState.uid);
                }
                return true;
            case MSG_REPORT_FULLSCREEN_MODE /*3045*/:
                if (msg.arg1 != 0) {
                    fullscreen = true;
                }
                try {
                    ((ClientState) msg.obj).client.reportFullscreenMode(fullscreen);
                } catch (RemoteException e12) {
                    Slog.w(TAG, "Got RemoteException sending reportFullscreen(" + fullscreen + ") notification to pid=" + clientState.pid + " uid=" + clientState.uid);
                }
                return true;
            case 3050:
                if (msg.arg1 != 0) {
                    fullscreen = true;
                }
                handleSwitchInputMethod(fullscreen);
                return true;
            case MSG_HARD_KEYBOARD_SWITCH_CHANGED /*4000*/:
                HardKeyboardListener hardKeyboardListener = this.mHardKeyboardListener;
                if (msg.arg1 == 1) {
                    fullscreen = true;
                }
                hardKeyboardListener.handleHardKeyboardStatusChange(fullscreen);
                return true;
            case MSG_SYSTEM_UNLOCK_USER /*5000*/:
                onUnlockUser(msg.arg1);
                return true;
            default:
                return false;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        synchronized (this.mMethodMap) {
            this.mIsInteractive = interactive;
            updateSystemUiLocked(this.mCurToken, interactive ? this.mImeWindowVis : 0, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, this.mInFullscreenMode ? 1 : 0, this.mCurClient));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0059, code lost:
        return;
     */
    private void handleSwitchInputMethod(boolean forwardDirection) {
        synchronized (this.mMethodMap) {
            InputMethodSubtypeSwitchingController.ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(false, this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, forwardDirection);
            if (nextSubtype != null) {
                setInputMethodLocked(nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
                InputMethodInfo newInputMethodInfo = this.mMethodMap.get(this.mCurMethodId);
                if (newInputMethodInfo != null) {
                    CharSequence toastText = InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, newInputMethodInfo, this.mCurrentSubtype);
                    if (!TextUtils.isEmpty(toastText)) {
                        if (this.mSubtypeSwitchedByShortCutToast == null) {
                            this.mSubtypeSwitchedByShortCutToast = Toast.makeText(this.mContext, toastText, 0);
                        } else {
                            this.mSubtypeSwitchedByShortCutToast.setText(toastText);
                        }
                        this.mSubtypeSwitchedByShortCutToast.show();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean chooseNewDefaultIMELocked() {
        InputMethodInfo imi = InputMethodUtils.getMostApplicableDefaultIME(this.mSettings.getEnabledInputMethodListLocked());
        if (imi != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "New default IME was selected: " + imi.getId());
            }
            resetSelectedInputMethodAndSubtypeLocked(imi.getId());
            return true;
        }
        if (imi == null) {
            Slog.w(TAG, "NO default IME was selected: ");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int getComponentMatchingFlags(int baseFlags) {
        synchronized (this.mMethodMap) {
            if (this.mBindInstantServiceAllowed) {
                baseFlags |= DumpState.DUMP_VOLUMES;
            }
        }
        return baseFlags;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mMethodMap")
    public void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        if (!this.mSystemReady) {
            Slog.e(TAG, "buildInputMethodListLocked is not allowed until system is ready");
            return;
        }
        this.mMethodList.clear();
        this.mMethodMap.clear();
        this.mMethodMapUpdateCount++;
        this.mMyPackageMonitor.clearKnownImePackageNamesLocked();
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServicesAsUser(new Intent("android.view.InputMethod"), getComponentMatchingFlags(32896), this.mSettings.getCurrentUserId());
        if (services.size() == 0) {
            Slog.e(TAG, "There is no input method available in the system");
        }
        HashMap<String, List<InputMethodSubtype>> additionalSubtypeMap = this.mFileManager.getAllAdditionalInputMethodSubtypes();
        for (int i = 0; i < services.size(); i++) {
            ResolveInfo ri = services.get(i);
            ServiceInfo si = ri.serviceInfo;
            String imeId = InputMethodInfo.computeId(ri);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                Slog.w(TAG, "Skipping input method " + imeId + ": it does not require the permission " + "android.permission.BIND_INPUT_METHOD");
            } else if (!shouldBuildInputMethodList(si.packageName)) {
                Slog.w(TAG, "buildInputMethodListLocked: Skipping IME " + si.packageName);
            } else {
                try {
                    InputMethodInfo p = new InputMethodInfo(this.mContext, ri, additionalSubtypeMap.get(imeId));
                    this.mMethodList.add(p);
                    String id = p.getId();
                    this.mMethodMap.put(id, p);
                    ensureEnableSystemIME(id, p, this.mContext, this.mSettings.getCurrentUserId());
                } catch (Exception e) {
                    Slog.wtf(TAG, "Unable to load input method " + imeId, e);
                }
            }
        }
        HwPCUtils.setInputMethodList(new ArrayList(this.mMethodList));
        updateSecureIMEStatus();
        List<ResolveInfo> allInputMethodServices = pm.queryIntentServicesAsUser(new Intent("android.view.InputMethod"), getComponentMatchingFlags(512), this.mSettings.getCurrentUserId());
        int N = allInputMethodServices.size();
        for (int i2 = 0; i2 < N; i2++) {
            ServiceInfo si2 = allInputMethodServices.get(i2).serviceInfo;
            if ("android.permission.BIND_INPUT_METHOD".equals(si2.permission)) {
                this.mMyPackageMonitor.addKnownImePackageNameLocked(si2.packageName);
            }
        }
        boolean reenableMinimumNonAuxSystemImes = false;
        if (!resetDefaultEnabledIme) {
            boolean enabledNonAuxImeFound = false;
            List<InputMethodInfo> enabledImes = this.mSettings.getEnabledInputMethodListLocked();
            int N2 = enabledImes.size();
            boolean enabledImeFound = false;
            int i3 = 0;
            while (true) {
                if (i3 >= N2) {
                    break;
                }
                InputMethodInfo imi = enabledImes.get(i3);
                if (this.mMethodList.contains(imi)) {
                    enabledImeFound = true;
                    if (!imi.isAuxiliaryIme()) {
                        enabledNonAuxImeFound = true;
                        break;
                    }
                }
                i3++;
            }
            if (!enabledImeFound) {
                resetDefaultEnabledIme = true;
                resetSelectedInputMethodAndSubtypeLocked(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            } else if (!enabledNonAuxImeFound) {
                reenableMinimumNonAuxSystemImes = true;
            }
        }
        if (resetDefaultEnabledIme || reenableMinimumNonAuxSystemImes) {
            ArrayList<InputMethodInfo> defaultEnabledIme = InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mMethodList, reenableMinimumNonAuxSystemImes);
            int N3 = defaultEnabledIme.size();
            for (int i4 = 0; i4 < N3; i4++) {
                setInputMethodEnabledLocked(defaultEnabledIme.get(i4).getId(), true);
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

    /* access modifiers changed from: private */
    public void showConfigureInputMethods() {
        Intent intent = new Intent("android.settings.INPUT_METHOD_SETTINGS");
        intent.setFlags(337641472);
        this.mContext.startActivityAsUser(intent, null, UserHandle.CURRENT);
    }

    private boolean isScreenLocked() {
        return this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked() && this.mKeyguardManager.isKeyguardSecure();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0204, code lost:
        return;
     */
    private void showInputMethodMenu(boolean showAuxSubtypes, int pID) {
        List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> imList;
        int checkedItem;
        ContextThemeWrapper themeContext;
        Context dialogContext;
        int i;
        Context context = this.mContext;
        boolean isScreenLocked = isScreenLocked();
        String lastInputMethodId = this.mSettings.getSelectedInputMethod();
        int lastInputMethodSubtypeId = this.mSettings.getSelectedInputMethodSubtypeId(lastInputMethodId);
        synchronized (this.mMethodMap) {
            try {
                HashMap<InputMethodInfo, List<InputMethodSubtype>> immis = this.mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(this.mContext);
                if (immis == null) {
                    boolean z = showAuxSubtypes;
                    HashMap<InputMethodInfo, List<InputMethodSubtype>> hashMap = immis;
                    Context context2 = context;
                    boolean z2 = isScreenLocked;
                    String str = lastInputMethodId;
                } else if (immis.size() == 0) {
                    boolean z3 = showAuxSubtypes;
                    HashMap<InputMethodInfo, List<InputMethodSubtype>> hashMap2 = immis;
                    Context context3 = context;
                    boolean z4 = isScreenLocked;
                    String str2 = lastInputMethodId;
                } else {
                    hideInputMethodMenuLocked();
                    try {
                        imList = this.mSwitchingController.getSortedInputMethodAndSubtypeListLocked(showAuxSubtypes, isScreenLocked);
                        if (lastInputMethodSubtypeId == -1) {
                            try {
                                InputMethodSubtype currentSubtype = getCurrentInputMethodSubtypeLocked();
                                if (currentSubtype != null) {
                                    lastInputMethodSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(this.mMethodMap.get(this.mCurMethodId), currentSubtype.hashCode());
                                }
                            } catch (Throwable th) {
                                th = th;
                                Context context4 = context;
                                throw th;
                            }
                        }
                        int N = imList.size();
                        this.mIms = new InputMethodInfo[N];
                        this.mSubtypeIds = new int[N];
                        checkedItem = -1;
                        for (int i2 = 0; i2 < N; i2++) {
                            InputMethodSubtypeSwitchingController.ImeSubtypeListItem item = imList.get(i2);
                            this.mIms[i2] = item.mImi;
                            this.mSubtypeIds[i2] = item.mSubtypeId;
                            if (this.mIms[i2].getId().equals(lastInputMethodId)) {
                                int subtypeId = this.mSubtypeIds[i2];
                                if (subtypeId == -1 || ((lastInputMethodSubtypeId == -1 && subtypeId == 0) || subtypeId == lastInputMethodSubtypeId)) {
                                    checkedItem = i2;
                                }
                            }
                        }
                        int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
                        themeContext = new ContextThemeWrapper(context, themeID);
                        this.mDialogBuilder = new AlertDialog.Builder(themeContext, themeID);
                        this.mDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                InputMethodManagerService.this.hideInputMethodMenu();
                            }
                        });
                        dialogContext = this.mDialogBuilder.getContext();
                        HashMap<InputMethodInfo, List<InputMethodSubtype>> hashMap3 = immis;
                        Context context5 = context;
                    } catch (Throwable th2) {
                        th = th2;
                        Context context6 = context;
                        boolean z5 = isScreenLocked;
                        String str3 = lastInputMethodId;
                        throw th;
                    }
                    try {
                        TypedArray a = dialogContext.obtainStyledAttributes(null, R.styleable.DialogPreference, 16842845, 0);
                        Drawable dialogIcon = a.getDrawable(2);
                        a.recycle();
                        this.mDialogBuilder.setIcon(dialogIcon);
                        LayoutInflater inflater = (LayoutInflater) dialogContext.getSystemService(LayoutInflater.class);
                        TypedArray typedArray = a;
                        View tv = inflater.inflate(34013191, null);
                        this.mDialogBuilder.setCustomTitle(tv);
                        this.mSwitchingDialogTitleView = tv;
                        View view = tv;
                        View mSwitchSectionView = this.mSwitchingDialogTitleView.findViewById(34603134);
                        if (mSwitchSectionView == null) {
                            LayoutInflater layoutInflater = inflater;
                            try {
                                Slog.e(TAG, "mSwitchSectionView is null");
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } else {
                            LayoutInflater layoutInflater2 = inflater;
                            if (this.mWindowManagerInternal.isHardKeyboardAvailable()) {
                                i = 0;
                            } else {
                                i = 8;
                            }
                            mSwitchSectionView.setVisibility(i);
                            Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(34603135);
                            hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
                            hardKeySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    InputMethodManagerService.this.mSettings.setShowImeWithHardKeyboard(isChecked);
                                    InputMethodManagerService.this.hideInputMethodMenu();
                                }
                            });
                            View view2 = mSwitchSectionView;
                            final ImeSubtypeListAdapter adapter = new ImeSubtypeListAdapter(themeContext, 17367285, imList, checkedItem);
                            Switch switchR = hardKeySwitch;
                            this.mDialogBuilder.setSingleChoiceItems(adapter, checkedItem, new DialogInterface.OnClickListener() {
                                /* JADX WARNING: Code restructure failed: missing block: B:23:0x00a1, code lost:
                                    return;
                                 */
                                /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a3, code lost:
                                    return;
                                 */
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
                                                    InputMethodManagerService.this.mLastInputShown = InputMethodManagerService.this.mInputShown;
                                                    if (im.getId() != null) {
                                                        Slog.i(InputMethodManagerService.TAG, "ime choosed, issame: " + im.getId().equals(InputMethodManagerService.this.mCurMethodId) + ",lastInputShown: " + InputMethodManagerService.this.mLastInputShown);
                                                    }
                                                    String unused = InputMethodManagerService.this.mCurInputId = im.getId();
                                                    InputMethodManagerService.this.setInputMethodLocked(im.getId(), subtypeId);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            String packageName = getPackageName(pID);
                            ImeSubtypeListAdapter imeSubtypeListAdapter = adapter;
                            StringBuilder sb = new StringBuilder();
                            String str4 = lastInputMethodId;
                            try {
                                sb.append("showInputMethodMenu packageName: ");
                                String packageName2 = packageName;
                                sb.append(packageName2);
                                Slog.i(TAG, sb.toString());
                                if (!isScreenLocked) {
                                    if (packageName2 != null) {
                                        try {
                                            if (packageName2.equals("com.android.settings")) {
                                                boolean z6 = isScreenLocked;
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            boolean z7 = isScreenLocked;
                                            throw th;
                                        }
                                    }
                                    boolean z8 = isScreenLocked;
                                    this.mDialogBuilder.setPositiveButton(33685727, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            InputMethodManagerService.this.showConfigureInputMethods();
                                        }
                                    });
                                }
                                this.mSwitchingDialog = this.mDialogBuilder.create();
                                this.mSwitchingDialog.setCanceledOnTouchOutside(true);
                                Window w = this.mSwitchingDialog.getWindow();
                                WindowManager.LayoutParams attrs = w.getAttributes();
                                w.setType(2012);
                                attrs.token = this.mSwitchingDialogToken;
                                attrs.privateFlags |= 16;
                                attrs.setTitle("Select input method");
                                w.setAttributes(attrs);
                                Window window = w;
                                WindowManager.LayoutParams layoutParams = attrs;
                                updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                                this.mSwitchingDialog.show();
                            } catch (Throwable th5) {
                                th = th5;
                                throw th;
                            }
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        boolean z9 = isScreenLocked;
                        String str5 = lastInputMethodId;
                        throw th;
                    }
                }
            } catch (Throwable th7) {
                th = th7;
                boolean z10 = showAuxSubtypes;
                Context context62 = context;
                boolean z52 = isScreenLocked;
                String str32 = lastInputMethodId;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getPackageName(int pID) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void hideInputMethodMenu() {
        synchronized (this.mMethodMap) {
            hideInputMethodMenuLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void hideInputMethodMenuLocked() {
        if (this.mSwitchingDialog != null) {
            this.mSwitchingDialog.dismiss();
            this.mSwitchingDialog = null;
            this.mSwitchingDialogTitleView = null;
        }
        updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
        this.mDialogBuilder = null;
        this.mIms = null;
    }

    /* access modifiers changed from: package-private */
    public boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        if (this.mMethodMap.get(id) != null) {
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
                resetSelectedInputMethodAndSubtypeLocked(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
            return true;
        }
        throw new IllegalArgumentException("Unknown id: " + this.mCurMethodId);
    }

    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        if (imi == null || !isSecureIME(imi.getPackageName())) {
            boolean isVrInput = imi != null && imi.isVrOnly();
            if (!isVrInput) {
                this.mSettings.saveCurrentInputMethodAndSubtypeToHistory(this.mCurMethodId, this.mCurrentSubtype);
            }
            this.mCurUserActionNotificationSequenceNumber = Math.max(this.mCurUserActionNotificationSequenceNumber + 1, 1);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(3040, this.mCurUserActionNotificationSequenceNumber, this.mCurClient));
            }
            if (!isVrInput) {
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
                    this.mSettings.putSelectedInputMethod(imi != null ? imi.getId() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
                return;
            }
            return;
        }
        Slog.d(TAG, "setSelectedInputMethodAndSubtypeLocked: Skipping SecureIME");
    }

    /* access modifiers changed from: private */
    public void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        InputMethodInfo imi = this.mMethodMap.get(newDefaultIme);
        int lastSubtypeId = -1;
        if (imi != null && !TextUtils.isEmpty(newDefaultIme)) {
            String subtypeHashCode = this.mSettings.getLastSubtypeForInputMethodLocked(newDefaultIme);
            if (subtypeHashCode != null) {
                try {
                    lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, Integer.parseInt(subtypeHashCode));
                } catch (NumberFormatException e) {
                    Slog.w(TAG, "HashCode for subtype looks broken: " + subtypeHashCode, e);
                }
            }
        }
        setSelectedInputMethodAndSubtypeLocked(imi, lastSubtypeId, false);
    }

    private Pair<InputMethodInfo, InputMethodSubtype> findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        ArrayList<InputMethodSubtype> subtypesForSearch;
        String str = mode;
        InputMethodInfo mostApplicableIMI = null;
        InputMethodSubtype mostApplicableSubtype = null;
        boolean foundInSystemIME = false;
        Iterator<InputMethodInfo> it = this.mSettings.getEnabledInputMethodListLocked().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            InputMethodInfo imi = it.next();
            String imiId = imi.getId();
            if (!foundInSystemIME || imiId.equals(this.mCurMethodId)) {
                InputMethodSubtype subtype = null;
                List<InputMethodSubtype> enabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, str, this.mCurrentSubtype.getLocale(), false);
                }
                if (subtype == null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, str, null, true);
                }
                ArrayList<InputMethodSubtype> overridingImplicitlyEnabledSubtypes = InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, str);
                if (overridingImplicitlyEnabledSubtypes.isEmpty()) {
                    subtypesForSearch = InputMethodUtils.getSubtypes(imi);
                } else {
                    subtypesForSearch = overridingImplicitlyEnabledSubtypes;
                }
                if (subtype == null && this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, str, this.mCurrentSubtype.getLocale(), false);
                }
                if (subtype == null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, str, null, true);
                }
                if (subtype == null) {
                    continue;
                } else if (imiId.equals(this.mCurMethodId)) {
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    break;
                } else if (!foundInSystemIME) {
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    if ((imi.getServiceInfo().applicationInfo.flags & 1) != 0) {
                        foundInSystemIME = true;
                    }
                }
            }
        }
        if (mostApplicableIMI != null) {
            return new Pair<>(mostApplicableIMI, mostApplicableSubtype);
        }
        return null;
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        InputMethodSubtype currentInputMethodSubtypeLocked;
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            currentInputMethodSubtypeLocked = getCurrentInputMethodSubtypeLocked();
        }
        return currentInputMethodSubtypeLocked;
    }

    private InputMethodSubtype getCurrentInputMethodSubtypeLocked() {
        if (this.mCurMethodId == null) {
            return null;
        }
        boolean subtypeIsSelected = this.mSettings.isSubtypeSelected();
        InputMethodInfo imi = this.mMethodMap.get(this.mCurMethodId);
        if (imi == null || imi.getSubtypeCount() == 0) {
            return null;
        }
        if (!subtypeIsSelected || this.mCurrentSubtype == null || !InputMethodUtils.isValidSubtypeId(imi, this.mCurrentSubtype.hashCode())) {
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
                this.mCurrentSubtype = (InputMethodSubtype) InputMethodUtils.getSubtypes(imi).get(subtypeId);
            }
        }
        return this.mCurrentSubtype;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0024, code lost:
        return r1;
     */
    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList<>();
            if (this.mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
            } else {
                for (InputMethodInfo imi : this.mShortcutInputMethodsAndSubtypes.keySet()) {
                    ret.add(imi);
                    Iterator it = this.mShortcutInputMethodsAndSubtypes.get(imi).iterator();
                    while (it.hasNext()) {
                        ret.add((InputMethodSubtype) it.next());
                    }
                }
                return ret;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        return false;
     */
    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (subtype != null) {
                try {
                    if (this.mCurMethodId != null) {
                        int subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(this.mMethodMap.get(this.mCurMethodId), subtype.hashCode());
                        if (subtypeId != -1) {
                            setInputMethodLocked(this.mCurMethodId, subtypeId);
                            return true;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    private static String imeWindowStatusToString(int imeWindowVis) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if ((imeWindowVis & 1) != 0) {
            sb.append("Active");
            first = false;
        }
        if ((imeWindowVis & 2) != 0) {
            if (!first) {
                sb.append("|");
            }
            sb.append("Visible");
        }
        return sb.toString();
    }

    public IInputContentUriToken createInputContentUriToken(IBinder token, Uri contentUri, String packageName) {
        if (!calledFromValidUser()) {
            return null;
        }
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
                    InputContentUriTokenHandler inputContentUriTokenHandler = new InputContentUriTokenHandler(ContentProvider.getUriWithoutUserId(contentUri), uid, packageName, ContentProvider.getUserIdFromUri(contentUri, imeUserId), appUserId);
                    return inputContentUriTokenHandler;
                }
            }
        } else {
            throw new InvalidParameterException("contentUri must have content scheme");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        return;
     */
    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                        this.mInFullscreenMode = fullscreen;
                        executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_REPORT_FULLSCREEN_MODE, fullscreen, this.mCurClient));
                    }
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
                p.println("  Input Methods: mMethodMapUpdateCount=" + this.mMethodMapUpdateCount + " mBindInstantServiceAllowed=" + this.mBindInstantServiceAllowed);
                for (int i = 0; i < N; i++) {
                    p.println("  InputMethod #" + i + ":");
                    this.mMethodList.get(i).dump(p, "    ");
                }
                p.println("  Clients:");
                for (ClientState ci : this.mClients.values()) {
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
                p.println("  mCurFocusedWindow=" + this.mCurFocusedWindow + " softInputMode=" + InputMethodClient.softInputModeToString(this.mCurFocusedWindowSoftInputMode) + " client=" + this.mCurFocusedWindowClient);
                focusedWindowClient = this.mCurFocusedWindowClient;
                p.println("  mCurId=" + this.mCurId + " mHaveConnect=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  mCurToken=");
                sb2.append(this.mCurToken);
                p.println(sb2.toString());
                p.println("  mCurIntent=" + this.mCurIntent);
                method = this.mCurMethod;
                p.println("  mCurMethod=" + this.mCurMethod);
                p.println("  mEnabledSession=" + this.mEnabledSession);
                p.println("  mImeWindowVis=" + imeWindowStatusToString(this.mImeWindowVis));
                p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("  mInFullscreenMode=");
                sb3.append(this.mInFullscreenMode);
                p.println(sb3.toString());
                StringBuilder sb4 = new StringBuilder();
                sb4.append("  mCurUserActionNotificationSequenceNumber=");
                sb4.append(this.mCurUserActionNotificationSequenceNumber);
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

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        new ShellCommandImpl(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: private */
    public int handleSetBindInstantServiceAllowed(ShellCommand shellCommand) {
        String allowedString = shellCommand.getNextArgRequired();
        if (allowedString == null) {
            shellCommand.getErrPrintWriter().println("Error: no true/false specified");
            return -1;
        }
        boolean allowed = Boolean.parseBoolean(allowedString);
        synchronized (this.mMethodMap) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_BIND_INSTANT_SERVICE") != 0) {
                shellCommand.getErrPrintWriter().print("Caller must have MANAGE_BIND_INSTANT_SERVICE permission");
                return -1;
            } else if (this.mBindInstantServiceAllowed == allowed) {
                return 0;
            } else {
                this.mBindInstantServiceAllowed = allowed;
                long ident = Binder.clearCallingIdentity();
                try {
                    resetSelectedInputMethodAndSubtypeLocked(null);
                    this.mSettings.putSelectedInputMethod(null);
                    buildInputMethodListLocked(false);
                    updateInputMethodsFromSettingsLocked(true);
                    return 0;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public int handleShellCommandListInputMethods(ShellCommand shellCommand) {
        boolean all = false;
        boolean brief = false;
        while (true) {
            String nextOption = shellCommand.getNextOption();
            if (nextOption != null) {
                char c = 65535;
                int hashCode = nextOption.hashCode();
                if (hashCode != 1492) {
                    if (hashCode == 1510 && nextOption.equals("-s")) {
                        c = 1;
                    }
                } else if (nextOption.equals("-a")) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        all = true;
                        break;
                    case 1:
                        brief = true;
                        break;
                }
            } else {
                List<InputMethodInfo> methods = all ? getInputMethodList() : getEnabledInputMethodList();
                PrintWriter pr = shellCommand.getOutPrintWriter();
                Printer printer = new Printer(pr) {
                    private final /* synthetic */ PrintWriter f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void println(String str) {
                        this.f$0.println(str);
                    }
                };
                int N = methods.size();
                for (int i = 0; i < N; i++) {
                    if (brief) {
                        pr.println(methods.get(i).getId());
                    } else {
                        pr.print(methods.get(i).getId());
                        pr.println(":");
                        methods.get(i).dump(printer, "  ");
                    }
                }
                return 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public int handleShellCommandEnableDisableInputMethod(ShellCommand shellCommand, boolean enabled) {
        boolean previouslyEnabled;
        if (!calledFromValidUser()) {
            shellCommand.getErrPrintWriter().print("Must be called from the foreground user or with INTERACT_ACROSS_USERS_FULL");
            return -1;
        }
        String id = shellCommand.getNextArgRequired();
        synchronized (this.mMethodMap) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
                long ident = Binder.clearCallingIdentity();
                try {
                    previouslyEnabled = setInputMethodEnabledLocked(id, enabled);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                shellCommand.getErrPrintWriter().print("Caller must have WRITE_SECURE_SETTINGS permission");
                throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
            }
        }
        PrintWriter pr = shellCommand.getOutPrintWriter();
        pr.print("Input method ");
        pr.print(id);
        pr.print(": ");
        pr.print(enabled == previouslyEnabled ? "already " : "now ");
        pr.println(enabled ? "enabled" : "disabled");
        return 0;
    }

    /* access modifiers changed from: private */
    public int handleShellCommandSetInputMethod(ShellCommand shellCommand) {
        String id = shellCommand.getNextArgRequired();
        setInputMethod(null, id);
        PrintWriter pr = shellCommand.getOutPrintWriter();
        pr.print("Input method ");
        pr.print(id);
        pr.println("  selected");
        return 0;
    }

    /* access modifiers changed from: private */
    public int handleShellCommandResetInputMethod(ShellCommand shellCommand) {
        String nextIme;
        List<InputMethodInfo> nextEnabledImes;
        if (!calledFromValidUser()) {
            shellCommand.getErrPrintWriter().print("Must be called from the foreground user or with INTERACT_ACROSS_USERS_FULL");
            return -1;
        }
        synchronized (this.mMethodMap) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mMethodMap) {
                        hideCurrentInputLocked(0, null);
                        unbindCurrentMethodLocked(false);
                        resetSelectedInputMethodAndSubtypeLocked(null);
                        this.mSettings.putSelectedInputMethod(null);
                        ArrayList<InputMethodInfo> enabledImes = this.mSettings.getEnabledInputMethodListLocked();
                        int N = enabledImes.size();
                        for (int i = 0; i < N; i++) {
                            setInputMethodEnabledLocked(enabledImes.get(i).getId(), false);
                        }
                        ArrayList<InputMethodInfo> defaultEnabledIme = InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mMethodList);
                        int N2 = defaultEnabledIme.size();
                        for (int i2 = 0; i2 < N2; i2++) {
                            setInputMethodEnabledLocked(defaultEnabledIme.get(i2).getId(), true);
                        }
                        updateInputMethodsFromSettingsLocked(true);
                        InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
                        nextIme = this.mSettings.getSelectedInputMethod();
                        nextEnabledImes = getEnabledInputMethodList();
                    }
                    Binder.restoreCallingIdentity(ident);
                    PrintWriter pr = shellCommand.getOutPrintWriter();
                    pr.println("Reset current and enabled IMEs");
                    pr.println("Newly selected IME:");
                    pr.print("  ");
                    pr.println(nextIme);
                    pr.println("Newly enabled IMEs:");
                    int N3 = nextEnabledImes.size();
                    for (int i3 = 0; i3 < N3; i3++) {
                        pr.print("  ");
                        pr.println(nextEnabledImes.get(i3).getId());
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } else {
                shellCommand.getErrPrintWriter().print("Caller must have WRITE_SECURE_SETTINGS permission");
                throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
            }
        }
        return 0;
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

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.InputMethodManagerService$HwInnerInputMethodManagerService, android.os.IBinder] */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public void setInputMethodLockedByInner(String imeId) {
        int uid = Binder.getCallingUid();
        if (uid == 1000 || uid == 0) {
            synchronized (this.mMethodMap) {
                if (this.mSettings.getIsWriteInputEnable()) {
                    if (!TextUtils.isEmpty(imeId)) {
                        setInputMethodLocked(imeId, this.mSettings.getSelectedInputMethodSubtypeId(imeId));
                    } else {
                        if (TextUtils.isEmpty(this.mCurInputId)) {
                            this.mCurInputId = this.mSettings.getSelectedInputMethod();
                        }
                        setInputMethodLocked(this.mCurInputId, this.mSettings.getSelectedInputMethodSubtypeId(this.mCurInputId));
                    }
                }
            }
            return;
        }
        throw new SecurityException("has no permssion to use");
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
}
