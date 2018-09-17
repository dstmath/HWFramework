package com.android.server;

import android.annotation.IntDef;
import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
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
import android.hardware.input.InputManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.ArrayMap;
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
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.OnHardKeyboardStatusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManagerInternal;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSubtype.InputMethodSubtypeBuilder;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController.ImeSubtypeListItem;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputBindResult;
import com.android.internal.view.InputMethodClient;
import com.android.server.HwServiceFactory.IHwInputMethodManagerService;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.statusbar.StatusBarManagerService;
import com.huawei.android.inputmethod.IHwInputMethodManager.Stub;
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
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class InputMethodManagerService extends AbsInputMethodManagerService implements IHwInputMethodManagerInner, ServiceConnection, Callback {
    private static final String BROADCAST_ACTION_ENABLE_KEYGUARD = "com.huawei.action.SKIP_RESTREICT_INPUT";
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    static final boolean DEBUG_RESTORE = false;
    private static final String DEVELOPER_CHANNEL = "DEVELOPER";
    private static final int IME_CONNECTION_BIND_FLAGS = 1619001349;
    private static final int IME_VISIBLE_BIND_FLAGS = 201326593;
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
    static final int MSG_SWITCH_IME = 3050;
    static final int MSG_SYSTEM_UNLOCK_USER = 5000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    static final String TAG = "InputMethodManagerService";
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    static final long TIME_TO_RECONNECT = 3000;
    boolean bFlag = false;
    private boolean mAccessibilityRequestingNoSoftKeyboard;
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition = 0;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients = new HashMap();
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
    private String mCurInputId;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurUserActionNotificationSequenceNumber = 0;
    private InputMethodSubtype mCurrentSubtype;
    private Builder mDialogBuilder;
    HashMap<String, Boolean> mEnabledFileMap = new HashMap();
    SessionState mEnabledSession;
    private InputMethodFileManager mFileManager;
    final Handler mHandler;
    private final int mHardKeyboardBehavior;
    private final HardKeyboardListener mHardKeyboardListener;
    final boolean mHasFeature;
    boolean mHaveConnection;
    IHwInputMethodManagerServiceEx mHwIMMSEx = null;
    HwInnerInputMethodManagerService mHwInnerService = new HwInnerInputMethodManagerService(this);
    private final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    private PendingIntent mImeSwitchPendingIntent;
    private Notification.Builder mImeSwitcherNotification;
    int mImeWindowVis;
    private InputMethodInfo[] mIms;
    boolean mInFullscreenMode;
    boolean mInputShown;
    boolean mIsDiffIME;
    boolean mIsInteractive = true;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    private String mLastIME = null;
    boolean mLastInputShown;
    private LocaleList mLastSystemLocales;
    boolean mLastUnBindInputMethodInPCMode = false;
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList();
    final HashMap<String, InputMethodInfo> mMethodMap = new HashMap();
    @GuardedBy("mMethodMap")
    private int mMethodMapUpdateCount = 0;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    final InputBindResult mNoBinding = new InputBindResult(null, null, null, -1, -1);
    private NotificationManager mNotificationManager;
    private boolean mNotificationShown;
    final Resources mRes;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans = new LruCache(20);
    final InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes = new HashMap();
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    private boolean mShowImeWithHardKeyboard;
    private boolean mShowOngoingImeSwitcherForPhones;
    boolean mShowRequested;
    private final String mSlotIme;
    @GuardedBy("mMethodMap")
    private final StartInputHistory mStartInputHistory = new StartInputHistory();
    @GuardedBy("mMethodMap")
    private final WeakHashMap<IBinder, StartInputInfo> mStartInputMap = new WeakHashMap();
    private StatusBarManagerService mStatusBar;
    private int[] mSubtypeIds;
    private Toast mSubtypeSwitchedByShortCutToast;
    private final InputMethodSubtypeSwitchingController mSwitchingController;
    private AlertDialog mSwitchingDialog;
    private View mSwitchingDialogTitleView;
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

    @IntDef({0, 1})
    @Retention(RetentionPolicy.SOURCE)
    private @interface HardKeyboardBehavior {
        public static final int WIRED_AFFORDANCE = 1;
        public static final int WIRELESS_AFFORDANCE = 0;
    }

    private class HardKeyboardListener implements OnHardKeyboardStatusChangeListener {
        /* synthetic */ HardKeyboardListener(InputMethodManagerService this$0, HardKeyboardListener -this1) {
            this();
        }

        private HardKeyboardListener() {
        }

        public void onHardKeyboardStatusChange(boolean available) {
            InputMethodManagerService.this.mHandler.sendMessage(InputMethodManagerService.this.mHandler.obtainMessage(InputMethodManagerService.MSG_HARD_KEYBOARD_SWITCH_CHANGED, Integer.valueOf(available ? 1 : 0)));
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

    public class HwInnerInputMethodManagerService extends Stub {
        HwInnerInputMethodManagerService(InputMethodManagerService imms) {
        }

        public void setDefaultIme(String imeId) {
            InputMethodManagerService.this.mHwIMMSEx.setDefaultIme(imeId);
        }

        public void setKeyguardEnable() {
            InputMethodManagerService.this.mHwIMMSEx.setKeyguardEnable();
        }
    }

    private static class ImeSubtypeListAdapter extends ArrayAdapter<ImeSubtypeListItem> {
        public int mCheckedItem;
        private final LayoutInflater mInflater;
        private final List<ImeSubtypeListItem> mItemsList;
        private final int mTextColorPri;
        private final int mTextColorSec;
        private final int mTextViewResourceId;

        public ImeSubtypeListAdapter(Context context, int textViewResourceId, List<ImeSubtypeListItem> itemsList, int checkedItem) {
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
            View view;
            boolean z = false;
            if (convertView != null) {
                view = convertView;
            } else {
                view = this.mInflater.inflate(this.mTextViewResourceId, null);
            }
            if (position < 0 || position >= this.mItemsList.size()) {
                return view;
            }
            ImeSubtypeListItem item = (ImeSubtypeListItem) this.mItemsList.get(position);
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
            if (TextUtils.isEmpty(subtypeName)) {
                firstTextView.setText(imeName);
                secondTextView.setVisibility(8);
            } else {
                secondTextView.setText(subtypeName);
                firstTextView.setText(imeName);
                secondTextView.setVisibility(0);
            }
            RadioButton radioButton = (RadioButton) view.findViewById(16909198);
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
                if ("android.os.action.SETTING_RESTORED".equals(action)) {
                    if ("enabled_input_methods".equals(intent.getStringExtra("setting_name"))) {
                        InputMethodManagerService.restoreEnabledInputMethods(InputMethodManagerService.this.mContext, intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"));
                    }
                } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                    synchronized (InputMethodManagerService.this.mMethodMap) {
                        InputMethodManagerService.this.mSettings.putSelectedSubtype(-1);
                    }
                    InputMethodManagerService.this.onActionLocaleChanged();
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
        private final HashMap<String, List<InputMethodSubtype>> mAdditionalSubtypesMap = new HashMap();
        private final HashMap<String, InputMethodInfo> mMethodMap;

        public InputMethodFileManager(HashMap<String, InputMethodInfo> methodMap, int userId) {
            if (methodMap == null) {
                throw new NullPointerException("methodMap is null");
            }
            File systemDir;
            this.mMethodMap = methodMap;
            if (userId == 0) {
                systemDir = new File(Environment.getDataDirectory(), SYSTEM_PATH);
            } else {
                systemDir = Environment.getUserSystemDirectory(userId);
            }
            File inputMethodDir = new File(systemDir, INPUT_METHOD_PATH);
            if (!(inputMethodDir.exists() || (inputMethodDir.mkdirs() ^ 1) == 0)) {
                Slog.w(InputMethodManagerService.TAG, "Couldn't create dir.: " + inputMethodDir.getAbsolutePath());
            }
            File subtypeFile = new File(inputMethodDir, ADDITIONAL_SUBTYPES_FILE_NAME);
            this.mAdditionalInputMethodSubtypeFile = new AtomicFile(subtypeFile);
            if (subtypeFile.exists()) {
                readAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile);
            } else {
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, methodMap);
            }
        }

        private void deleteAllInputMethodSubtypes(String imiId) {
            synchronized (this.mMethodMap) {
                this.mAdditionalSubtypesMap.remove(imiId);
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, this.mMethodMap);
            }
        }

        public void addInputMethodSubtypes(InputMethodInfo imi, InputMethodSubtype[] additionalSubtypes) {
            synchronized (this.mMethodMap) {
                ArrayList<InputMethodSubtype> subtypes = new ArrayList();
                for (InputMethodSubtype subtype : additionalSubtypes) {
                    if (subtypes.contains(subtype)) {
                        Slog.w(InputMethodManagerService.TAG, "Duplicated subtype definition found: " + subtype.getLocale() + ", " + subtype.getMode());
                    } else {
                        subtypes.add(subtype);
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
            FileOutputStream fos = null;
            try {
                fos = subtypesFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, NODE_SUBTYPES);
                for (String imiId : allSubtypes.keySet()) {
                    if (!isSetMethodMap || (methodMap.containsKey(imiId) ^ 1) == 0) {
                        out.startTag(null, NODE_IMI);
                        out.attribute(null, ATTR_ID, imiId);
                        List<InputMethodSubtype> subtypesList = (List) allSubtypes.get(imiId);
                        int N = subtypesList.size();
                        for (int i = 0; i < N; i++) {
                            InputMethodSubtype subtype = (InputMethodSubtype) subtypesList.get(i);
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
                if (fos != null) {
                    subtypesFile.failWrite(fos);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A:{Splitter: B:23:0x0064, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A:{Splitter: B:23:0x0064, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:25:0x0065, code:
            r6 = move-exception;
     */
        /* JADX WARNING: Missing block: B:26:0x0066, code:
            android.util.Slog.w(com.android.server.InputMethodManagerService.TAG, "Error reading subtypes", r6);
     */
        /* JADX WARNING: Missing block: B:27:0x0073, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void readAdditionalInputMethodSubtypes(HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile) {
            Throwable th;
            FileInputStream fis;
            Throwable th2;
            if (allSubtypes != null && subtypesFile != null) {
                allSubtypes.clear();
                th = null;
                fis = null;
                try {
                    fis = subtypesFile.openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis, StandardCharsets.UTF_8.name());
                    int eventType = parser.getEventType();
                    do {
                        eventType = parser.next();
                        if (eventType == 2) {
                            break;
                        }
                    } while (eventType != 1);
                    if (NODE_SUBTYPES.equals(parser.getName())) {
                        int depth = parser.getDepth();
                        Object currentImiId = null;
                        ArrayList<InputMethodSubtype> tempSubtypesArray = null;
                        while (true) {
                            eventType = parser.next();
                            if ((eventType != 3 || parser.getDepth() > depth) && eventType != 1) {
                                if (eventType == 2) {
                                    String nodeName = parser.getName();
                                    if (NODE_IMI.equals(nodeName)) {
                                        currentImiId = parser.getAttributeValue(null, ATTR_ID);
                                        if (TextUtils.isEmpty(currentImiId)) {
                                            Slog.w(InputMethodManagerService.TAG, "Invalid imi id found in subtypes.xml");
                                        } else {
                                            tempSubtypesArray = new ArrayList();
                                            allSubtypes.put(currentImiId, tempSubtypesArray);
                                        }
                                    } else if (NODE_SUBTYPE.equals(nodeName)) {
                                        if (TextUtils.isEmpty(currentImiId) || tempSubtypesArray == null) {
                                            Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + currentImiId);
                                        } else {
                                            int icon = Integer.parseInt(parser.getAttributeValue(null, ATTR_ICON));
                                            int label = Integer.parseInt(parser.getAttributeValue(null, ATTR_LABEL));
                                            String imeSubtypeLocale = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LOCALE);
                                            String languageTag = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG);
                                            String imeSubtypeMode = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_MODE);
                                            String imeSubtypeExtraValue = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_EXTRA_VALUE);
                                            boolean isAuxiliary = "1".equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_AUXILIARY)));
                                            InputMethodSubtypeBuilder builder = new InputMethodSubtypeBuilder().setSubtypeNameResId(label).setSubtypeIconResId(icon).setSubtypeLocale(imeSubtypeLocale).setLanguageTag(languageTag).setSubtypeMode(imeSubtypeMode).setSubtypeExtraValue(imeSubtypeExtraValue).setIsAuxiliary(isAuxiliary).setIsAsciiCapable("1".equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_ASCII_CAPABLE))));
                                            String subtypeIdString = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_ID);
                                            if (subtypeIdString != null) {
                                                builder.setSubtypeId(Integer.parseInt(subtypeIdString));
                                            }
                                            tempSubtypesArray.add(builder.build());
                                        }
                                    }
                                }
                            }
                        }
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        if (th != null) {
                            throw th;
                        }
                        return;
                    }
                    throw new XmlPullParserException("Xml doesn't start with subtypes");
                } catch (Throwable th4) {
                    Throwable th5 = th4;
                    th4 = th2;
                    th2 = th5;
                }
            } else {
                return;
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable th6) {
                    if (th4 == null) {
                        th4 = th6;
                    } else if (th4 != th6) {
                        th4.addSuppressed(th6);
                    }
                }
            }
            if (th4 != null) {
                try {
                    throw th4;
                } catch (Exception e) {
                }
            } else {
                throw th2;
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private InputMethodManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            IHwInputMethodManagerService iinputmethodMS = HwServiceFactory.getHwInputMethodManagerService();
            if (iinputmethodMS != null) {
                this.mService = iinputmethodMS.getInstance(context);
            } else {
                this.mService = new InputMethodManagerService(context);
            }
        }

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
            int i;
            Handler handler = this.mHandler;
            Handler handler2 = this.mHandler;
            if (interactive) {
                i = 1;
            } else {
                i = 0;
            }
            handler.sendMessage(handler2.obtainMessage(InputMethodManagerService.MSG_SET_INTERACTIVE, i, 0));
        }

        public void switchInputMethod(boolean forwardDirection) {
            int i;
            Handler handler = this.mHandler;
            Handler handler2 = this.mHandler;
            if (forwardDirection) {
                i = 1;
            } else {
                i = 0;
            }
            handler.sendMessage(handler2.obtainMessage(3050, i, 0));
        }

        public void hideCurrentInputMethod() {
            this.mHandler.removeMessages(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
            this.mHandler.sendEmptyMessage(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
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
        private final ArrayList<String> mChangedPackages = new ArrayList();
        private boolean mImePackageAppeared = false;
        @GuardedBy("mMethodMap")
        private final ArraySet<String> mKnownImePackageNames = new ArraySet();

        MyPackageMonitor() {
        }

        @GuardedBy("mMethodMap")
        void clearKnownImePackageNamesLocked() {
            this.mKnownImePackageNames.clear();
        }

        @GuardedBy("mMethodMap")
        final void addKnownImePackageNameLocked(String packageName) {
            this.mKnownImePackageNames.add(packageName);
        }

        @GuardedBy("mMethodMap")
        private boolean isChangingPackagesOfCurrentUserLocked() {
            return getChangingUserId() == InputMethodManagerService.this.mSettings.getCurrentUserId();
        }

        /* JADX WARNING: Missing block: B:26:0x0055, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (isChangingPackagesOfCurrentUserLocked()) {
                    String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                    int N = InputMethodManagerService.this.mMethodList.size();
                    if (curInputMethodId != null) {
                        for (int i = 0; i < N; i++) {
                            InputMethodInfo imi = (InputMethodInfo) InputMethodManagerService.this.mMethodList.get(i);
                            if (imi.getId().equals(curInputMethodId)) {
                                int length = packages.length;
                                int i2 = 0;
                                while (i2 < length) {
                                    if (!imi.getPackageName().equals(packages[i2])) {
                                        i2++;
                                    } else if (doit) {
                                        return true;
                                    } else {
                                        return true;
                                    }
                                }
                                continue;
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
        }

        public void onBeginPackageChanges() {
            clearPackageChangeState();
        }

        public void onPackageAppeared(String packageName, int reason) {
            if (!(this.mImePackageAppeared || InputMethodManagerService.this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod").setPackage(packageName), 512, getChangingUserId()).isEmpty())) {
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

        private boolean shouldRebuildInputMethodListLocked() {
            if (this.mImePackageAppeared) {
                return true;
            }
            int N = this.mChangedPackages.size();
            for (int i = 0; i < N; i++) {
                if (this.mKnownImePackageNames.contains((String) this.mChangedPackages.get(i))) {
                    return true;
                }
            }
            return false;
        }

        /* JADX WARNING: Missing block: B:50:0x0111, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void onFinishPackageChangesInternal() {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (!isChangingPackagesOfCurrentUserLocked()) {
                } else if (shouldRebuildInputMethodListLocked()) {
                    int change;
                    InputMethodInfo inputMethodInfo = null;
                    String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                    int N = InputMethodManagerService.this.mMethodList.size();
                    if (curInputMethodId != null) {
                        for (int i = 0; i < N; i++) {
                            InputMethodInfo imi = (InputMethodInfo) InputMethodManagerService.this.mMethodList.get(i);
                            String imiId = imi.getId();
                            if (imiId.equals(curInputMethodId)) {
                                inputMethodInfo = imi;
                            }
                            change = isPackageDisappearing(imi.getPackageName());
                            if (isPackageModified(imi.getPackageName())) {
                                InputMethodManagerService.this.mFileManager.deleteAllInputMethodSubtypes(imiId);
                            }
                            if (change == 2 || change == 3) {
                                Slog.i(InputMethodManagerService.TAG, "Input method uninstalled, disabling: " + imi.getComponent());
                                InputMethodManagerService.this.setInputMethodEnabledLocked(imi.getId(), false);
                            }
                        }
                    }
                    InputMethodManagerService.this.buildInputMethodListLocked(false);
                    boolean changed = false;
                    if (inputMethodInfo != null) {
                        change = isPackageDisappearing(inputMethodInfo.getPackageName());
                        if (change == 2 || change == 3) {
                            ServiceInfo si = null;
                            try {
                                si = InputMethodManagerService.this.mIPackageManager.getServiceInfo(inputMethodInfo.getComponent(), 0, InputMethodManagerService.this.mSettings.getCurrentUserId());
                            } catch (RemoteException e) {
                            }
                            if (si == null) {
                                Slog.i(InputMethodManagerService.TAG, "Current input method removed: " + curInputMethodId);
                                InputMethodManagerService.this.updateSystemUiLocked(InputMethodManagerService.this.mCurToken, 0, InputMethodManagerService.this.mBackDisposition);
                                if (!InputMethodManagerService.this.chooseNewDefaultIMELocked()) {
                                    changed = true;
                                    inputMethodInfo = null;
                                    Slog.i(InputMethodManagerService.TAG, "Unsetting current input method");
                                    InputMethodManagerService.this.resetSelectedInputMethodAndSubtypeLocked("");
                                }
                            }
                        }
                    }
                    if (inputMethodInfo == null) {
                        changed = InputMethodManagerService.this.chooseNewDefaultIMELocked();
                    } else if (!changed) {
                        if (isPackageModified(inputMethodInfo.getPackageName())) {
                            changed = true;
                        }
                    }
                    if (changed) {
                        InputMethodManagerService.this.updateFromSettingsLocked(false);
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
                resolver.registerContentObserver(Secure.getUriFor("default_input_method"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("enabled_input_methods"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("selected_input_method_subtype"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("show_ime_with_hard_keyboard"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("accessibility_soft_keyboard_mode"), false, this, userId);
                this.mRegistered = true;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean z = true;
            Uri showImeUri = Secure.getUriFor("show_ime_with_hard_keyboard");
            Uri accessibilityRequestingNoImeUri = Secure.getUriFor("accessibility_soft_keyboard_mode");
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (showImeUri.equals(uri)) {
                    InputMethodManagerService.this.updateKeyboardFromSettingsLocked();
                } else if (accessibilityRequestingNoImeUri.equals(uri)) {
                    InputMethodManagerService inputMethodManagerService = InputMethodManagerService.this;
                    if (Secure.getIntForUser(InputMethodManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, this.mUserId) != 1) {
                        z = false;
                    }
                    inputMethodManagerService.mAccessibilityRequestingNoSoftKeyboard = z;
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
                        this.mLastEnabled = "";
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

            void set(StartInputInfo original) {
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

        /* synthetic */ StartInputHistory(StartInputHistory -this0) {
            this();
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

        void addEntry(StartInputInfo info) {
            int index = this.mNextIndex;
            if (this.mEntries[index] == null) {
                this.mEntries[index] = new Entry(info);
            } else {
                this.mEntries[index].set(info);
            }
            this.mNextIndex = (this.mNextIndex + 1) % this.mEntries.length;
        }

        void dump(PrintWriter pw, String prefix) {
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            for (int i = 0; i < this.mEntries.length; i++) {
                Entry entry = this.mEntries[(this.mNextIndex + i) % this.mEntries.length];
                if (entry != null) {
                    pw.print(prefix);
                    pw.println("StartInput #" + entry.mSequenceNumber + ":");
                    pw.print(prefix);
                    pw.println(" time=" + dataFormat.format(new Date(entry.mWallTime)) + " (timestamp=" + entry.mTimestamp + ")" + " reason=" + InputMethodClient.getStartInputReason(entry.mStartInputReason) + " restarting=" + entry.mRestarting);
                    pw.print(prefix);
                    pw.println(" imeToken=" + entry.mImeTokenString + " [" + entry.mImeId + "]");
                    pw.print(prefix);
                    pw.println(" targetWin=" + entry.mTargetWindowString + " [" + entry.mEditorInfo.packageName + "]" + " clientBindSeq=" + entry.mClientBindSequenceNumber);
                    pw.print(prefix);
                    pw.println(" softInputMode=" + InputMethodClient.softInputModeToString(entry.mTargetWindowSoftInputMode));
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

    void onActionLocaleChanged() {
        synchronized (this.mMethodMap) {
            LocaleList possibleNewLocale = this.mRes.getConfiguration().getLocales();
            if (possibleNewLocale == null || !possibleNewLocale.equals(this.mLastSystemLocales)) {
                buildInputMethodListLocked(true);
                resetDefaultImeLocked(this.mContext);
                updateFromSettingsLocked(true);
                this.mLastSystemLocales = possibleNewLocale;
                return;
            }
        }
    }

    static void restoreEnabledInputMethods(Context context, String prevValue, String newValue) {
        ArrayMap<String, ArraySet<String>> prevMap = InputMethodUtils.parseInputMethodsAndSubtypesString(prevValue);
        for (Entry<String, ArraySet<String>> entry : InputMethodUtils.parseInputMethodsAndSubtypesString(newValue).entrySet()) {
            String imeId = (String) entry.getKey();
            ArraySet<String> prevSubtypes = (ArraySet) prevMap.get(imeId);
            if (prevSubtypes == null) {
                prevSubtypes = new ArraySet(2);
                prevMap.put(imeId, prevSubtypes);
            }
            prevSubtypes.addAll((ArraySet) entry.getValue());
        }
        Secure.putString(context.getContentResolver(), "enabled_input_methods", InputMethodUtils.buildInputMethodsAndSubtypesString(prevMap));
    }

    /* JADX WARNING: Missing block: B:12:0x0023, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onUnlockUser(int userId) {
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId != currentUserId) {
                return;
            }
            this.mSettings.switchCurrentUser(currentUserId, this.mSystemReady ^ 1);
            if (this.mSystemReady) {
                buildInputMethodListLocked(false);
                updateInputMethodsFromSettingsLocked(true);
            }
        }
    }

    void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public void setWriteInputEnable(boolean isWriteInput) {
        setPanWriteInputEnable(isWriteInput);
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
        this.mCaller = new HandlerCaller(context, null, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                InputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mHardKeyboardListener = new HardKeyboardListener(this, null);
        this.mHasFeature = context.getPackageManager().hasSystemFeature("android.software.input_methods");
        this.mSlotIme = this.mContext.getString(17041061);
        this.mHardKeyboardBehavior = this.mContext.getResources().getInteger(17694785);
        Bundle extras = new Bundle();
        extras.putBoolean("android.allowDuringSetup", true);
        this.mImeSwitcherNotification = new Notification.Builder(this.mContext, DEVELOPER_CHANNEL).setSmallIcon(33751156).setWhen(0).setOngoing(true).addExtras(extras).setCategory("sys");
        Intent intent = new Intent("android.settings.SHOW_INPUT_METHOD_PICKER");
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
        this.mSettings = new InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, this.mMethodList, userId, this.mSystemReady ^ 1);
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, userId);
        this.mSwitchingController = InputMethodSubtypeSwitchingController.createInstanceLocked(this.mSettings, context);
        createFlagIfNecessary(userId);
    }

    private void resetDefaultImeLocked(Context context) {
    }

    private void switchUserLocked(int newUserId) {
        createFlagIfNecessary(newUserId);
        this.mSettingsObserver.registerContentObserverLocked(newUserId);
        this.mSettings.switchCurrentUser(newUserId, this.mSystemReady ? this.mUserManager.isUserUnlockingOrUnlocked(newUserId) ^ 1 : true);
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

    void updateCurrentProfileIds() {
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
                this.mSettings.switchCurrentUser(currentUserId, this.mUserManager.isUserUnlockingOrUnlocked(currentUserId) ^ 1);
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                this.mShowOngoingImeSwitcherForPhones = this.mRes.getBoolean(17957081);
                if (this.mShowOngoingImeSwitcherForPhones) {
                    this.mWindowManagerInternal.setOnHardKeyboardStatusChangeListener(this.mHardKeyboardListener);
                }
                this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
                this.mSettingsObserver.registerContentObserverLocked(currentUserId);
                IntentFilter broadcastFilter = new IntentFilter();
                broadcastFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                broadcastFilter.addAction("android.intent.action.USER_ADDED");
                broadcastFilter.addAction("android.intent.action.USER_REMOVED");
                broadcastFilter.addAction("android.os.action.SETTING_RESTORED");
                broadcastFilter.addAction("android.intent.action.LOCALE_CHANGED");
                this.mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
                String[] imePkgName = new String[]{"com.baidu.input_huawei", "com.touchtype.swiftkey", "com.swiftkey.swiftkeyconfigurator"};
                for (String defaultImeEnable : imePkgName) {
                    setDefaultImeEnable(defaultImeEnable);
                }
                buildInputMethodListLocked(true);
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
        return;
    }

    /* JADX WARNING: Missing block: B:4:0x0016, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        Slog.e(TAG, "Ignoring " + Debug.getCaller() + " due to an invalid token." + " uid:" + Binder.getCallingUid() + " token:" + token);
        return false;
    }

    private boolean bindCurrentInputMethodService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return false;
    }

    public List<InputMethodInfo> getInputMethodList() {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        List arrayList;
        synchronized (this.mMethodMap) {
            arrayList = new ArrayList(this.mMethodList);
        }
        return arrayList;
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        List enabledInputMethodListLocked;
        synchronized (this.mMethodMap) {
            enabledInputMethodListLocked = this.mSettings.getEnabledInputMethodListLocked();
        }
        return enabledInputMethodListLocked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0020  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo imi;
            if (imiId == null) {
                if (this.mCurMethodId != null) {
                    imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                    List<InputMethodSubtype> emptyList;
                    if (imi != null) {
                        emptyList = Collections.emptyList();
                        return emptyList;
                    }
                    emptyList = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, allowsImplicitlySelectedSubtypes);
                    return emptyList;
                }
            }
            imi = (InputMethodInfo) this.mMethodMap.get(imiId);
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
                ClientState cs = (ClientState) this.mClients.remove(client.asBinder());
                if (cs != null) {
                    clearClientSessionLocked(cs);
                    if (this.mCurClient == cs) {
                        this.mCurClient = null;
                    }
                    if (this.mCurFocusedWindowClient == cs) {
                        this.mCurFocusedWindowClient = null;
                    }
                }
            }
        }
    }

    void executeOrSendMessage(IInterface target, Message msg) {
        if (target.asBinder() instanceof Binder) {
            this.mCaller.sendMessage(msg);
            return;
        }
        handleMessage(msg);
        msg.recycle();
    }

    void unbindCurrentClientLocked(int unbindClientReason) {
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
            return 3;
        }
        if (this.mShowExplicitlyRequested) {
            return 1;
        }
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 2;
        }
        if (this.mShowExplicitlyRequested) {
            return 0;
        }
        return 1;
    }

    InputBindResult attachNewInputLocked(int startInputReason, boolean initial) {
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_BIND_INPUT, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        Binder startInputToken = new Binder();
        StartInputInfo info = new StartInputInfo(this.mCurToken, this.mCurId, startInputReason, initial ^ 1, this.mCurFocusedWindow, this.mCurAttribute, this.mCurFocusedWindowSoftInputMode, this.mCurSeq);
        this.mStartInputMap.put(startInputToken, info);
        this.mStartInputHistory.addEntry(info);
        SessionState session = this.mCurClient.curSession;
        executeOrSendMessage(session.method, this.mCaller.obtainMessageIIOOOO(2000, this.mCurInputContextMissingMethods, initial ? 0 : 1, startInputToken, session, this.mCurInputContext, this.mCurAttribute));
        if (this.mShowRequested) {
            if (DEBUG_FLOW) {
                Slog.v(TAG, "Attach new input asks to show input");
            }
            showCurrentInputLocked(getAppShowFlags(), null);
        }
        return new InputBindResult(session.session, session.channel != null ? session.channel.dup() : null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    InputBindResult startInputLocked(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        ClientState cs = (ClientState) this.mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client " + client.asBinder());
        } else if (attribute == null) {
            Slog.w(TAG, "Ignoring startInput with null EditorInfo. uid=" + cs.uid + " pid=" + cs.pid);
            return null;
        } else {
            try {
                if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                    Slog.w(TAG, "Starting input on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                    return null;
                }
            } catch (RemoteException e) {
            }
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
        }
    }

    InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int startInputReason) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, cs.uid, attribute.packageName)) {
            if (this.mCurClient != cs) {
                this.mCurClientInKeyguard = isKeyguardLocked();
                unbindCurrentClientLocked(1);
                if (this.mIsInteractive) {
                    executeOrSendMessage(cs.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, cs));
                }
            }
            this.mCurSeq++;
            if (this.mCurSeq <= 0) {
                this.mCurSeq = 1;
            }
            this.mCurClient = cs;
            this.mCurInputContext = inputContext;
            this.mCurInputContextMissingMethods = missingMethods;
            this.mCurAttribute = attribute;
            if (this.mCurId != null && this.mCurId.equals(this.mCurMethodId)) {
                if (cs.curSession != null) {
                    if ((65536 & controlFlags) != 0) {
                        this.mShowRequested = true;
                    }
                    return attachNewInputLocked(startInputReason, (controlFlags & 256) != 0);
                } else if (this.mHaveConnection) {
                    if (this.mCurMethod != null) {
                        requestClientSessionLocked(cs);
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else if (SystemClock.uptimeMillis() < this.mLastBindTime + TIME_TO_RECONNECT) {
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else {
                        EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), Integer.valueOf(0)});
                    }
                }
            }
            try {
                return startInputInnerLocked();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Unexpected exception", e);
                return null;
            }
        }
        Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + cs.uid + " package=" + attribute.packageName);
        return this.mNoBinding;
    }

    InputBindResult startInputInnerLocked() {
        if (this.mCurMethodId == null || (this.mMethodMap.containsKey(this.mCurMethodId) ^ 1) != 0) {
            return this.mNoBinding;
        }
        if (this.mSystemReady) {
            InputMethodInfo info = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (info == null) {
                Slog.w(TAG, "info == null id: " + this.mCurMethodId);
                return this.mNoBinding;
            }
            unbindCurrentMethodLocked(true);
            this.mCurIntent = new Intent("android.view.InputMethod");
            this.mCurIntent.setComponent(info.getComponent());
            this.mCurIntent.putExtra("android.intent.extra.client_label", 17040156);
            this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
            if (bindCurrentInputMethodService(this.mCurIntent, this, IME_CONNECTION_BIND_FLAGS)) {
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mHaveConnection = true;
                this.mCurId = info.getId();
                this.mCurToken = new Binder();
                try {
                    this.mIWindowManager.addWindowToken(this.mCurToken, 2011, 0);
                } catch (RemoteException e) {
                }
                return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            }
            this.mCurIntent = null;
            Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
            return null;
        }
        return new InputBindResult(null, null, this.mCurMethodId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    protected InputBindResult startInput(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (!calledFromValidUser()) {
            return null;
        }
        InputBindResult startInputLocked;
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                startInputLocked = startInputLocked(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return startInputLocked;
    }

    public void finishInput(IInputMethodClient client) {
    }

    /* JADX WARNING: Missing block: B:20:0x006c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    /* JADX WARNING: Missing block: B:17:0x0055, code:
            return;
     */
    /* JADX WARNING: Missing block: B:19:0x0057, code:
            r9.dispose();
     */
    /* JADX WARNING: Missing block: B:20:0x005a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod == null || method == null || this.mCurMethod.asBinder() != method.asBinder() || this.mCurClient == null) {
            } else {
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

    void unbindCurrentMethodLocked(boolean savePosition) {
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

    void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(false);
        unbindCurrentClientLocked(unbindClientReason);
    }

    void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[1], new MethodCallback(this, this.mCurMethod, channels[0])));
        }
    }

    void clearClientSessionLocked(ClientState cs) {
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

    void clearCurMethodLocked() {
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
                if (HwPCUtils.enabledInPad() && (HwPCUtils.isPcCastModeInServer() ^ 1) != 0 && this.mLastUnBindInputMethodInPCMode) {
                    unbindCurrentMethodLocked(false);
                    this.mLastUnBindInputMethodInPCMode = false;
                }
            }
        }
    }

    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        String str = null;
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
                    }
                } else if (packageName != null) {
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
                return;
            }
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        if (!this.mShowOngoingImeSwitcherForPhones) {
            return false;
        }
        if (this.mSwitchingDialog != null) {
            return false;
        }
        if (isScreenLocked()) {
            return false;
        }
        if ((visibility & 1) == 0) {
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
        InputMethodSubtype inputMethodSubtype = null;
        for (int i = 0; i < N; i++) {
            List<InputMethodSubtype> subtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, (InputMethodInfo) imis.get(i), true);
            int subtypeCount = subtypes.size();
            if (subtypeCount == 0) {
                nonAuxCount++;
            } else {
                for (int j = 0; j < subtypeCount; j++) {
                    InputMethodSubtype subtype = (InputMethodSubtype) subtypes.get(j);
                    if (subtype.isAuxiliary()) {
                        auxCount++;
                    } else {
                        nonAuxCount++;
                    }
                    nonAuxSubtype = subtype;
                }
            }
        }
        if (nonAuxCount > 1 || auxCount > 1) {
            return true;
        }
        if (nonAuxCount != 1 || auxCount != 1) {
            return false;
        }
        if (nonAuxSubtype == null || inputMethodSubtype == null || ((!nonAuxSubtype.getLocale().equals(inputMethodSubtype.getLocale()) && !inputMethodSubtype.overridesImplicitlyEnabledSubtype() && !nonAuxSubtype.overridesImplicitlyEnabledSubtype()) || !nonAuxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER))) {
            return true;
        }
        return false;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null ? this.mKeyguardManager.isKeyguardLocked() : false;
    }

    public void setImeWindowStatus(IBinder token, IBinder startInputToken, int vis, int backDisposition) {
        IBinder iBinder = null;
        boolean z = false;
        if (calledWithValidToken(token)) {
            StartInputInfo info;
            boolean dismissImeOnBackKeyPressed;
            synchronized (this.mMethodMap) {
                info = (StartInputInfo) this.mStartInputMap.get(startInputToken);
                this.mImeWindowVis = vis;
                this.mBackDisposition = backDisposition;
                updateSystemUiLocked(token, vis, backDisposition);
            }
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
            if (info != null) {
                iBinder = info.mTargetWindow;
            }
            windowManagerInternal.updateInputMethodWindowStatus(token, z, dismissImeOnBackKeyPressed, iBinder);
        }
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    private void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            long ident = Binder.clearCallingIdentity();
            if (vis != 0) {
                try {
                    if (isKeyguardLocked() && (this.mCurClientInKeyguard ^ 1) != 0) {
                        vis = 0;
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            boolean needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis);
            if (this.mStatusBar != null) {
                this.mStatusBar.setImeWindowStatus(token, vis, backDisposition, needsToShowImeSwitcher);
            }
            InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (imi != null && needsToShowImeSwitcher) {
                this.mImeSwitcherNotification.setContentTitle(this.mRes.getText(17040945)).setContentText(InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, imi, this.mCurrentSubtype)).setContentIntent(this.mImeSwitchPendingIntent);
                try {
                    boolean isEnableNavBar = System.getIntForUser(this.mContext.getContentResolver(), "enable_navbar", getNaviBarEnabledDefValue(), -2) != 0;
                    Slog.i(TAG, "--- show notification config: mIWindowManager.hasNavigationBar() =  " + this.mIWindowManager.hasNavigationBar() + " ,isEnableNavBar = " + isEnableNavBar);
                    if (!(this.mNotificationManager == null || (this.mIWindowManager.hasNavigationBar() && (isEnableNavBar ^ 1) == 0))) {
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

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                for (SuggestionSpan ss : spans) {
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x001f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo targetImi = (InputMethodInfo) this.mSecureSuggestionSpans.get(span);
            if (targetImi != null) {
                String[] suggestions = span.getSuggestions();
                if (index < 0 || index >= suggestions.length) {
                } else {
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
                    }
                }
            } else {
                return false;
            }
        }
    }

    void updateFromSettingsLocked(boolean enabledMayChange) {
        updateInputMethodsFromSettingsLocked(enabledMayChange);
        updateKeyboardFromSettingsLocked();
    }

    void updateInputMethodsFromSettingsLocked(boolean enabledMayChange) {
        if (enabledMayChange) {
            List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
            for (int i = 0; i < enabled.size(); i++) {
                InputMethodInfo imm = (InputMethodInfo) enabled.get(i);
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
        if (TextUtils.isEmpty(id)) {
            resetCurrentMethodAndClient(4);
        } else {
            try {
                setInputMethodLocked(id, this.mSettings.getSelectedInputMethodSubtypeId(id));
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Unknown input method from prefs: " + id, e2);
                resetCurrentMethodAndClient(5);
            }
            this.mShortcutInputMethodsAndSubtypes.clear();
        }
        this.mSwitchingController.resetCircularListLocked(this.mContext);
    }

    public void updateKeyboardFromSettingsLocked() {
        this.mShowImeWithHardKeyboard = this.mSettings.isShowImeWithHardKeyboardEnabled();
        if (this.mSwitchingDialog != null && this.mSwitchingDialogTitleView != null && this.mSwitchingDialog.isShowing()) {
            Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(16908931);
            if (hardKeySwitch != null) {
                hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
            }
        }
    }

    private void notifyInputMethodSubtypeChanged(int userId, InputMethodInfo inputMethodInfo, InputMethodSubtype subtype) {
        InputManagerInternal inputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        if (inputManagerInternal != null) {
            inputManagerInternal.onInputMethodSubtypeChanged(userId, inputMethodInfo, subtype);
        }
    }

    void setInputMethodLocked(String id, int subtypeId) {
        InputMethodInfo info = (InputMethodInfo) this.mMethodMap.get(id);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + id);
        } else if (id.equals(this.mCurMethodId)) {
            this.mIsDiffIME = false;
            int subtypeCount = info.getSubtypeCount();
            if (subtypeCount > 0) {
                InputMethodSubtype newSubtype;
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
                            return;
                        }
                    }
                    notifyInputMethodSubtypeChanged(this.mSettings.getCurrentUserId(), info, newSubtype);
                }
            }
        } else {
            this.mIsDiffIME = true;
            this.mLastIME = this.mCurMethodId;
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
                notifyInputMethodSubtypeChanged(this.mSettings.getCurrentUserId(), info, getCurrentInputMethodSubtypeLocked());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0057, code:
            if (r9.mCurClient.client.asBinder() == r10.asBinder()) goto L_0x0059;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                }
                try {
                    if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                        Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                    }
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                    }
                    boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                    Binder.restoreCallingIdentity(ident);
                    return showCurrentInputLocked;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return false;
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
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
            if (this.mHaveConnection && (this.mVisibleBound ^ 1) != 0) {
                bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, IME_VISIBLE_BIND_FLAGS);
                this.mVisibleBound = true;
            }
            res = true;
        } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + TIME_TO_RECONNECT) {
            EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), Integer.valueOf(1)});
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodService(this.mCurIntent, this, IME_CONNECTION_BIND_FLAGS);
        }
        return res;
    }

    /* JADX WARNING: Missing block: B:20:0x005b, code:
            if (r10.mCurClient.client.asBinder() == r11.asBinder()) goto L_0x005d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                }
                try {
                    if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                        Slog.w(TAG, "Ignoring hideSoftInput of uid " + uid + ": " + client);
                    }
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "Client requesting input be hidden, pid=" + pid);
                    }
                    boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                    Binder.restoreCallingIdentity(ident);
                    return hideCurrentInputLocked;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return false;
    }

    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
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
            boolean res;
            boolean shouldHideSoftInput = this.mCurMethod != null ? !this.mInputShown ? (this.mImeWindowVis & 1) != 0 : true : false;
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

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (windowToken != null) {
            return windowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
        }
        return startInput(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
    }

    protected InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult res = null;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                ClientState cs = (ClientState) this.mClients.get(client.asBinder());
                if (cs == null) {
                    throw new IllegalArgumentException("unknown client " + client.asBinder());
                }
                try {
                    if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                        Slog.w(TAG, "Focus gain on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                    }
                } catch (RemoteException e) {
                }
                if (!calledFromValidUser) {
                    Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                    Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                    hideCurrentInputLocked(0, null);
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } else if (this.mCurFocusedWindow != windowToken) {
                    this.mCurFocusedWindow = windowToken;
                    this.mCurFocusedWindowSoftInputMode = softInputMode;
                    this.mCurFocusedWindowClient = cs;
                    int doAutoShow;
                    if ((softInputMode & 240) != 16) {
                        doAutoShow = this.mRes.getConfiguration().isLayoutSizeAtLeast(3);
                    } else {
                        doAutoShow = 1;
                    }
                    boolean isTextEditor = (controlFlags & 2) != 0;
                    boolean didStart = false;
                    switch (softInputMode & 15) {
                        case 0:
                            if (!isTextEditor || (doAutoShow ^ 1) != 0) {
                                if (LayoutParams.mayUseInputMethod(windowFlags)) {
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Unspecified window will hide input");
                                    }
                                    hideCurrentInputLocked(2, null);
                                    break;
                                }
                            } else if (!(!isTextEditor || doAutoShow == 0 || (softInputMode & 256) == 0)) {
                                if (DEBUG_FLOW) {
                                    Slog.v(TAG, "Unspecified window will show input");
                                }
                                if (attribute != null) {
                                    res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
                                    didStart = true;
                                }
                                showCurrentInputLocked(1, null);
                                break;
                            }
                            break;
                        case 2:
                            if ((softInputMode & 256) != 0) {
                                if (DEBUG_FLOW) {
                                    Slog.v(TAG, "Window asks to hide input going forward");
                                }
                                hideCurrentInputLocked(0, null);
                                break;
                            }
                            break;
                        case 3:
                            if (DEBUG_FLOW) {
                                Slog.v(TAG, "Window asks to hide input");
                            }
                            hideCurrentInputLocked(0, null);
                            break;
                        case 4:
                            if ((softInputMode & 256) != 0) {
                                if (DEBUG_FLOW) {
                                    Slog.v(TAG, "Window asks to show input going forward");
                                }
                                if (attribute != null) {
                                    res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
                                    didStart = true;
                                }
                                showCurrentInputLocked(1, null);
                                break;
                            }
                            break;
                        case 5:
                            if (DEBUG_FLOW) {
                                Slog.v(TAG, "Window asks to always show input");
                            }
                            if (attribute != null) {
                                res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
                                didStart = true;
                            }
                            showCurrentInputLocked(1, null);
                            break;
                    }
                    if (!(didStart || attribute == null)) {
                        res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
                    }
                    Binder.restoreCallingIdentity(ident);
                    return res;
                } else if (attribute != null) {
                    InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
                    Binder.restoreCallingIdentity(ident);
                    return startInputUncheckedLocked;
                } else {
                    Binder.restoreCallingIdentity(ident);
                    return null;
                }
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0053, code:
            if (r4.mCurClient.client.asBinder() != r5.asBinder()) goto L_0x0010;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                }
                Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid " + Binder.getCallingUid() + ": " + client);
                this.mHandler.sendMessage(this.mCaller.obtainMessageI(1, auxiliarySubtypeMode));
            }
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
                    setInputMethodWithSubtypeIdLocked(token, id, InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(id), subtype.hashCode()));
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

    public boolean switchToLastInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo lastImi;
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme != null) {
                lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
            } else {
                lastImi = null;
            }
            String targetLastImiId = null;
            int subtypeId = -1;
            if (!(lastIme == null || lastImi == null)) {
                boolean imiIdIsSame = lastImi.getId().equals(this.mCurMethodId);
                int lastSubtypeHash = Integer.parseInt((String) lastIme.second);
                int currentSubtypeHash;
                if (this.mCurrentSubtype == null) {
                    currentSubtypeHash = -1;
                } else {
                    currentSubtypeHash = this.mCurrentSubtype.hashCode();
                }
                if (!(imiIdIsSame && lastSubtypeHash == currentSubtypeHash)) {
                    targetLastImiId = lastIme.first;
                    subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, lastSubtypeHash);
                }
            }
            if (TextUtils.isEmpty(targetLastImiId) && (InputMethodUtils.canAddToLastInputMethod(this.mCurrentSubtype) ^ 1) != 0) {
                List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
                if (enabled != null) {
                    String locale;
                    int N = enabled.size();
                    if (this.mCurrentSubtype == null) {
                        locale = this.mRes.getConfiguration().locale.toString();
                    } else {
                        locale = this.mCurrentSubtype.getLocale();
                    }
                    for (int i = 0; i < N; i++) {
                        InputMethodInfo imi = (InputMethodInfo) enabled.get(i);
                        if (imi.getSubtypeCount() > 0 && InputMethodUtils.isSystemIme(imi)) {
                            InputMethodSubtype keyboardSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, InputMethodUtils.getSubtypes(imi), "keyboard", locale, true);
                            if (keyboardSubtype != null) {
                                CharSequence targetLastImiId2 = imi.getId();
                                subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, keyboardSubtype.hashCode());
                                if (keyboardSubtype.getLocale().equals(locale)) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(targetLastImiId)) {
                return false;
            }
            setInputMethodWithSubtypeIdLocked(token, targetLastImiId, subtypeId);
            return true;
        }
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (calledWithValidToken(token)) {
                ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(onlyCurrentIme, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true);
                if (nextSubtype == null) {
                    return false;
                }
                setInputMethodWithSubtypeIdLocked(token, nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
                return true;
            }
            return false;
        }
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (!calledWithValidToken(token)) {
                return false;
            } else if (this.mSwitchingController.getNextInputMethodLocked(false, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true) == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0028, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:25:0x004c, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null || TextUtils.isEmpty((CharSequence) lastIme.first) || TextUtils.isEmpty((CharSequence) lastIme.second)) {
            } else {
                InputMethodInfo lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
                if (lastImi == null) {
                    return null;
                }
                try {
                    int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                    if (lastSubtypeId < 0 || lastSubtypeId >= lastImi.getSubtypeCount()) {
                    } else {
                        InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                        return subtypeAt;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:49:0x006b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        if (calledFromValidUser() && !TextUtils.isEmpty(imiId) && subtypes != null) {
            synchronized (this.mMethodMap) {
                if (this.mSystemReady) {
                    InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(imiId);
                    if (imi == null) {
                        return;
                    }
                    long ident;
                    try {
                        String[] packageInfos = this.mIPackageManager.getPackagesForUid(Binder.getCallingUid());
                        if (packageInfos != null) {
                            for (String equals : packageInfos) {
                                if (equals.equals(imi.getPackageName())) {
                                    this.mFileManager.addInputMethodSubtypes(imi, subtypes);
                                    ident = Binder.clearCallingIdentity();
                                    buildInputMethodListLocked(false);
                                    Binder.restoreCallingIdentity(ident);
                                    return;
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Failed to get package infos");
                        return;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
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
                    return;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyUserAction(int sequenceNumber) {
        synchronized (this.mMethodMap) {
            if (this.mCurUserActionNotificationSequenceNumber != sequenceNumber) {
                return;
            }
            InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (imi != null) {
                this.mSwitchingController.onUserActionLocked(imi, this.mCurrentSubtype);
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
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
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
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }
        }
    }

    void setEnabledSessionInMainThread(SessionState session) {
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

    public boolean handleMessage(Message msg) {
        SomeArgs args;
        ClientState clientState;
        switch (msg.what) {
            case 1:
                boolean showAuxSubtypes;
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
                showInputMethodMenu(showAuxSubtypes);
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
                args = msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                }
                args.recycle();
                return true;
            case MSG_SHOW_SOFT_INPUT /*1020*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).showSoftInput(msg.arg1, (ResultReceiver) args.arg2);
                } catch (RemoteException e3) {
                }
                args.recycle();
                return true;
            case MSG_HIDE_SOFT_INPUT /*1030*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).hideSoftInput(0, (ResultReceiver) args.arg2);
                } catch (RemoteException e4) {
                }
                args.recycle();
                return true;
            case MSG_HIDE_CURRENT_INPUT_METHOD /*1035*/:
                synchronized (this.mMethodMap) {
                    hideCurrentInputLocked(0, null);
                }
                return true;
            case MSG_ATTACH_TOKEN /*1040*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).attachToken((IBinder) args.arg2);
                } catch (RemoteException e5) {
                }
                args.recycle();
                return true;
            case MSG_CREATE_SESSION /*1050*/:
                args = (SomeArgs) msg.obj;
                IInputMethod method = args.arg1;
                InputChannel channel = args.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback) args.arg3);
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                } catch (RemoteException e6) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                } catch (Throwable th) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case 2000:
                int missingMethods = msg.arg1;
                boolean restarting = msg.arg2 != 0;
                args = (SomeArgs) msg.obj;
                IBinder startInputToken = args.arg1;
                SessionState session = args.arg2;
                IInputContext inputContext = args.arg3;
                EditorInfo editorInfo = args.arg4;
                try {
                    setEnabledSessionInMainThread(session);
                    session.method.startInput(startInputToken, inputContext, missingMethods, editorInfo, restarting);
                } catch (RemoteException e7) {
                }
                args.recycle();
                return true;
            case MSG_UNBIND_CLIENT /*3000*/:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e8) {
                }
                return true;
            case 3010:
                args = (SomeArgs) msg.obj;
                IInputMethodClient client = args.arg1;
                InputBindResult res = args.arg2;
                try {
                    client.onBindMethod(res);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (RemoteException e9) {
                    Slog.w(TAG, "Client died receiving input method " + args.arg2);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (Throwable th2) {
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case MSG_SET_ACTIVE /*3020*/:
                try {
                    ((ClientState) msg.obj).client.setActive(msg.arg1 != 0, msg.arg2 != 0);
                } catch (RemoteException e10) {
                    Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid " + ((ClientState) msg.obj).pid + " uid " + ((ClientState) msg.obj).uid);
                }
                return true;
            case MSG_SET_INTERACTIVE /*3030*/:
                handleSetInteractive(msg.arg1 != 0);
                return true;
            case 3040:
                int sequenceNumber = msg.arg1;
                clientState = msg.obj;
                try {
                    clientState.client.setUserActionNotificationSequenceNumber(sequenceNumber);
                } catch (RemoteException e11) {
                    Slog.w(TAG, "Got RemoteException sending setUserActionNotificationSequenceNumber(" + sequenceNumber + ") notification to pid " + clientState.pid + " uid " + clientState.uid);
                }
                return true;
            case MSG_REPORT_FULLSCREEN_MODE /*3045*/:
                boolean fullscreen = msg.arg1 != 0;
                clientState = (ClientState) msg.obj;
                try {
                    clientState.client.reportFullscreenMode(fullscreen);
                } catch (RemoteException e12) {
                    Slog.w(TAG, "Got RemoteException sending reportFullscreen(" + fullscreen + ") notification to pid=" + clientState.pid + " uid=" + clientState.uid);
                }
                return true;
            case 3050:
                handleSwitchInputMethod(msg.arg1 != 0);
                return true;
            case MSG_HARD_KEYBOARD_SWITCH_CHANGED /*4000*/:
                this.mHardKeyboardListener.handleHardKeyboardStatusChange(msg.arg1 == 1);
                return true;
            case MSG_SYSTEM_UNLOCK_USER /*5000*/:
                onUnlockUser(msg.arg1);
                return true;
            default:
                return false;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        int i = 1;
        synchronized (this.mMethodMap) {
            this.mIsInteractive = interactive;
            updateSystemUiLocked(this.mCurToken, interactive ? this.mImeWindowVis : 0, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                int i2;
                IInterface iInterface = this.mCurClient.client;
                HandlerCaller handlerCaller = this.mCaller;
                if (this.mIsInteractive) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                if (!this.mInFullscreenMode) {
                    i = 0;
                }
                executeOrSendMessage(iInterface, handlerCaller.obtainMessageIIO(MSG_SET_ACTIVE, i2, i, this.mCurClient));
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0054, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleSwitchInputMethod(boolean forwardDirection) {
        synchronized (this.mMethodMap) {
            ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(false, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, forwardDirection);
            if (nextSubtype == null) {
                return;
            }
            setInputMethodLocked(nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
            InputMethodInfo newInputMethodInfo = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (newInputMethodInfo == null) {
                return;
            }
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

    private boolean chooseNewDefaultIMELocked() {
        if (this.mLastIME == null || !this.mMethodMap.containsKey(this.mLastIME)) {
            InputMethodInfo imi = InputMethodUtils.getMostApplicableDefaultIME(this.mSettings.getEnabledInputMethodListLocked());
            if (imi == null) {
                return false;
            }
            this.mLastIME = imi.getId();
            resetSelectedInputMethodAndSubtypeLocked(imi.getId());
            return true;
        }
        resetSelectedInputMethodAndSubtypeLocked(this.mLastIME);
        return true;
    }

    void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        if (this.mSystemReady) {
            int i;
            ServiceInfo si;
            this.mMethodList.clear();
            this.mMethodMap.clear();
            this.mMethodMapUpdateCount++;
            this.mMyPackageMonitor.clearKnownImePackageNamesLocked();
            PackageManager pm = this.mContext.getPackageManager();
            List<ResolveInfo> services = pm.queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, this.mSettings.getCurrentUserId());
            HashMap<String, List<InputMethodSubtype>> additionalSubtypeMap = this.mFileManager.getAllAdditionalInputMethodSubtypes();
            for (i = 0; i < services.size(); i++) {
                ResolveInfo ri = (ResolveInfo) services.get(i);
                si = ri.serviceInfo;
                String imeId = InputMethodInfo.computeId(ri);
                if ("android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                    if (shouldBuildInputMethodList(si.packageName)) {
                        try {
                            InputMethodInfo inputMethodInfo = new InputMethodInfo(this.mContext, ri, (List) additionalSubtypeMap.get(imeId));
                            this.mMethodList.add(inputMethodInfo);
                            String id = inputMethodInfo.getId();
                            this.mMethodMap.put(id, inputMethodInfo);
                            ensureEnableSystemIME(id, inputMethodInfo, this.mContext, this.mSettings.getCurrentUserId());
                        } catch (Exception e) {
                            Slog.wtf(TAG, "Unable to load input method " + imeId, e);
                        }
                    } else {
                        Slog.w(TAG, "buildInputMethodListLocked: Skipping IME " + si.packageName);
                    }
                } else {
                    Slog.w(TAG, "Skipping input method " + imeId + ": it does not require the permission " + "android.permission.BIND_INPUT_METHOD");
                }
            }
            HwPCUtils.setInputMethodList(new ArrayList(this.mMethodList));
            updateSecureIMEStatus();
            List<ResolveInfo> allInputMethodServices = pm.queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 512, this.mSettings.getCurrentUserId());
            int N = allInputMethodServices.size();
            for (i = 0; i < N; i++) {
                si = ((ResolveInfo) allInputMethodServices.get(i)).serviceInfo;
                if ("android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                    this.mMyPackageMonitor.addKnownImePackageNameLocked(si.packageName);
                }
            }
            if (!resetDefaultEnabledIme) {
                boolean enabledImeFound = false;
                List<InputMethodInfo> enabledImes = this.mSettings.getEnabledInputMethodListLocked();
                N = enabledImes.size();
                for (i = 0; i < N; i++) {
                    if (this.mMethodList.contains((InputMethodInfo) enabledImes.get(i))) {
                        enabledImeFound = true;
                        break;
                    }
                }
                if (!enabledImeFound) {
                    resetDefaultEnabledIme = true;
                    resetSelectedInputMethodAndSubtypeLocked("");
                }
            }
            if (resetDefaultEnabledIme) {
                ArrayList<InputMethodInfo> defaultEnabledIme = InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mMethodList);
                N = defaultEnabledIme.size();
                for (i = 0; i < N; i++) {
                    setInputMethodEnabledLocked(((InputMethodInfo) defaultEnabledIme.get(i)).getId(), true);
                }
            }
            String defaultImiId = this.mSettings.getSelectedInputMethod();
            if (!TextUtils.isEmpty(defaultImiId)) {
                if (this.mMethodMap.containsKey(defaultImiId)) {
                    setInputMethodEnabledLocked(defaultImiId, true);
                } else {
                    Slog.w(TAG, "Default IME is uninstalled. Choose new default IME.");
                    if (chooseNewDefaultIMELocked()) {
                        updateInputMethodsFromSettingsLocked(true);
                    }
                }
            }
            this.mSwitchingController.resetCircularListLocked(this.mContext);
            return;
        }
        Slog.e(TAG, "buildInputMethodListLocked is not allowed until system is ready");
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
        if (this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardLocked()) {
            return false;
        }
        return this.mKeyguardManager.isKeyguardSecure();
    }

    /* JADX WARNING: Missing block: B:8:0x0044, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showInputMethodMenu(boolean showAuxSubtypes) {
        Context context = this.mContext;
        boolean isScreenLocked = isScreenLocked();
        String lastInputMethodId = this.mSettings.getSelectedInputMethod();
        int lastInputMethodSubtypeId = this.mSettings.getSelectedInputMethodSubtypeId(lastInputMethodId);
        synchronized (this.mMethodMap) {
            HashMap<InputMethodInfo, List<InputMethodSubtype>> immis = this.mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(this.mContext);
            if (immis == null || immis.size() == 0) {
            } else {
                hideInputMethodMenuLocked();
                List<ImeSubtypeListItem> imList = this.mSwitchingController.getSortedInputMethodAndSubtypeListLocked(showAuxSubtypes, isScreenLocked);
                if (lastInputMethodSubtypeId == -1) {
                    InputMethodSubtype currentSubtype = getCurrentInputMethodSubtypeLocked();
                    if (currentSubtype != null) {
                        lastInputMethodSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), currentSubtype.hashCode());
                    }
                }
                int N = imList.size();
                this.mIms = new InputMethodInfo[N];
                this.mSubtypeIds = new int[N];
                int checkedItem = -1;
                for (int i = 0; i < N; i++) {
                    ImeSubtypeListItem item = (ImeSubtypeListItem) imList.get(i);
                    this.mIms[i] = item.mImi;
                    this.mSubtypeIds[i] = item.mSubtypeId;
                    if (this.mIms[i].getId().equals(lastInputMethodId)) {
                        int subtypeId = this.mSubtypeIds[i];
                        if (subtypeId == -1 || ((lastInputMethodSubtypeId == -1 && subtypeId == 0) || subtypeId == lastInputMethodSubtypeId)) {
                            checkedItem = i;
                        }
                    }
                }
                int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
                Context contextThemeWrapper = new ContextThemeWrapper(context, themeID);
                this.mDialogBuilder = new Builder(contextThemeWrapper, themeID);
                this.mDialogBuilder.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        InputMethodManagerService.this.hideInputMethodMenu();
                    }
                });
                Context dialogContext = this.mDialogBuilder.getContext();
                TypedArray a = dialogContext.obtainStyledAttributes(null, R.styleable.DialogPreference, 16842845, 0);
                Drawable dialogIcon = a.getDrawable(2);
                a.recycle();
                this.mDialogBuilder.setIcon(dialogIcon);
                View tv = ((LayoutInflater) dialogContext.getSystemService(LayoutInflater.class)).inflate(34013191, null);
                this.mDialogBuilder.setCustomTitle(tv);
                this.mSwitchingDialogTitleView = tv;
                View mSwitchSectionView = this.mSwitchingDialogTitleView.findViewById(34603134);
                if (mSwitchSectionView == null) {
                    Slog.e(TAG, "mSwitchSectionView is null");
                    return;
                }
                mSwitchSectionView.setVisibility(this.mWindowManagerInternal.isHardKeyboardAvailable() ? 0 : 8);
                Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(34603135);
                hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
                hardKeySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        InputMethodManagerService.this.mSettings.setShowImeWithHardKeyboard(isChecked);
                        InputMethodManagerService.this.hideInputMethodMenu();
                    }
                });
                final ImeSubtypeListAdapter adapter = new ImeSubtypeListAdapter(contextThemeWrapper, 17367274, imList, checkedItem);
                this.mDialogBuilder.setSingleChoiceItems(adapter, checkedItem, new OnClickListener() {
                    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return;
     */
                    /* JADX WARNING: Missing block: B:25:0x00a8, code:
            return;
     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized (InputMethodManagerService.this.mMethodMap) {
                            if (InputMethodManagerService.this.mIms != null && InputMethodManagerService.this.mIms.length > which) {
                                if (InputMethodManagerService.this.mSubtypeIds != null && InputMethodManagerService.this.mSubtypeIds.length > which) {
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
                                        InputMethodManagerService.this.mCurInputId = im.getId();
                                        InputMethodManagerService.this.setInputMethodLocked(im.getId(), subtypeId);
                                    }
                                }
                            }
                        }
                    }
                });
                if (!isScreenLocked) {
                    this.mDialogBuilder.setPositiveButton(33685727, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            InputMethodManagerService.this.showConfigureInputMethods();
                        }
                    });
                }
                this.mSwitchingDialog = this.mDialogBuilder.create();
                this.mSwitchingDialog.setCanceledOnTouchOutside(true);
                Window w = this.mSwitchingDialog.getWindow();
                LayoutParams attrs = w.getAttributes();
                w.setType(2012);
                attrs.token = this.mSwitchingDialogToken;
                attrs.privateFlags |= 16;
                attrs.setTitle("Select input method");
                w.setAttributes(attrs);
                updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                this.mSwitchingDialog.show();
            }
        }
    }

    void hideInputMethodMenu() {
        synchronized (this.mMethodMap) {
            hideInputMethodMenuLocked();
        }
    }

    void hideInputMethodMenuLocked() {
        if (this.mSwitchingDialog != null) {
            this.mSwitchingDialog.dismiss();
            this.mSwitchingDialog = null;
        }
        updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
        this.mDialogBuilder = null;
        this.mIms = null;
    }

    public boolean setInputMethodEnabled(String id, boolean enabled) {
        if (!calledFromValidUser()) {
            return false;
        }
        boolean inputMethodEnabledLocked;
        synchronized (this.mMethodMap) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                inputMethodEnabledLocked = setInputMethodEnabledLocked(id, enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return inputMethodEnabledLocked;
    }

    boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        if (((InputMethodInfo) this.mMethodMap.get(id)) == null) {
            throw new IllegalArgumentException("Unknown id: " + this.mCurMethodId);
        }
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
        if (id.equals(this.mSettings.getSelectedInputMethod()) && (chooseNewDefaultIMELocked() ^ 1) != 0) {
            Slog.i(TAG, "Can't find new IME, unsetting the current input method.");
            resetSelectedInputMethodAndSubtypeLocked("");
        }
        return true;
    }

    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        if (imi == null || !isSecureIME(imi.getPackageName())) {
            this.mSettings.saveCurrentInputMethodAndSubtypeToHistory(this.mCurMethodId, this.mCurrentSubtype);
            this.mCurUserActionNotificationSequenceNumber = Math.max(this.mCurUserActionNotificationSequenceNumber + 1, 1);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(3040, this.mCurUserActionNotificationSequenceNumber, this.mCurClient));
            }
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
            return;
        }
        Slog.d(TAG, "setSelectedInputMethodAndSubtypeLocked: Skipping SecureIME");
    }

    private void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(newDefaultIme);
        int lastSubtypeId = -1;
        if (!(imi == null || (TextUtils.isEmpty(newDefaultIme) ^ 1) == 0)) {
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
        Object mostApplicableIMI = null;
        Object mostApplicableSubtype = null;
        boolean foundInSystemIME = false;
        for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
            String imiId = imi.getId();
            if (!foundInSystemIME || (imiId.equals(this.mCurMethodId) ^ 1) == 0) {
                ArrayList<InputMethodSubtype> subtypesForSearch;
                InputMethodSubtype subtype = null;
                List<InputMethodSubtype> enabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, mode, this.mCurrentSubtype.getLocale(), false);
                }
                if (subtype == null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, mode, null, true);
                }
                ArrayList<InputMethodSubtype> overridingImplicitlyEnabledSubtypes = InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, mode);
                if (overridingImplicitlyEnabledSubtypes.isEmpty()) {
                    subtypesForSearch = InputMethodUtils.getSubtypes(imi);
                } else {
                    subtypesForSearch = overridingImplicitlyEnabledSubtypes;
                }
                if (subtype == null && this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, mode, this.mCurrentSubtype.getLocale(), false);
                }
                if (subtype == null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, mode, null, true);
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
            return new Pair(mostApplicableIMI, mostApplicableSubtype);
        }
        return null;
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        InputMethodSubtype currentInputMethodSubtypeLocked;
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
        InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
        if (imi == null || imi.getSubtypeCount() == 0) {
            return null;
        }
        if (!(subtypeIsSelected && this.mCurrentSubtype != null && (InputMethodUtils.isValidSubtypeId(imi, this.mCurrentSubtype.hashCode()) ^ 1) == 0)) {
            int subtypeId = this.mSettings.getSelectedInputMethodSubtypeId(this.mCurMethodId);
            if (subtypeId == -1) {
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (explicitlyOrImplicitlyEnabledSubtypes.size() == 1) {
                    this.mCurrentSubtype = (InputMethodSubtype) explicitlyOrImplicitlyEnabledSubtypes.get(0);
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

    /* JADX WARNING: Missing block: B:9:0x0024, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList();
            if (this.mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
            } else {
                for (InputMethodInfo imi : this.mShortcutInputMethodsAndSubtypes.keySet()) {
                    ret.add(imi);
                    for (InputMethodSubtype subtype : (ArrayList) this.mShortcutInputMethodsAndSubtypes.get(imi)) {
                        ret.add(subtype);
                    }
                }
                return ret;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x002f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (subtype != null) {
                if (this.mCurMethodId != null) {
                    int subtypeId = InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), subtype.hashCode());
                    if (subtypeId != -1) {
                        setInputMethodLocked(this.mCurMethodId, subtypeId);
                        return true;
                    }
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
        } else {
            if ("content".equals(contentUri.getScheme())) {
                synchronized (this.mMethodMap) {
                    int uid = Binder.getCallingUid();
                    if (this.mCurMethodId == null) {
                        return null;
                    } else if (this.mCurToken != token) {
                        Slog.e(TAG, "Ignoring createInputContentUriToken mCurToken=" + this.mCurToken + " token=" + token);
                        return null;
                    } else if (TextUtils.equals(this.mCurAttribute.packageName, packageName)) {
                        int imeUserId = UserHandle.getUserId(uid);
                        int appUserId = UserHandle.getUserId(this.mCurClient.uid);
                        InputContentUriTokenHandler inputContentUriTokenHandler = new InputContentUriTokenHandler(ContentProvider.getUriWithoutUserId(contentUri), uid, packageName, ContentProvider.getUserIdFromUri(contentUri, imeUserId), appUserId);
                        return inputContentUriTokenHandler;
                    } else {
                        Slog.e(TAG, "Ignoring createInputContentUriToken mCurAttribute.packageName=" + this.mCurAttribute.packageName + " packageName=" + packageName);
                        return null;
                    }
                }
            }
            throw new InvalidParameterException("contentUri must have content scheme");
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0033, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!calledWithValidToken(token)) {
                } else if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                    this.mInFullscreenMode = fullscreen;
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_REPORT_FULLSCREEN_MODE, fullscreen ? 1 : 0, this.mCurClient));
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0370 A:{Splitter: B:23:0x0326, ExcHandler: java.io.IOException (r5_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0390 A:{Splitter: B:28:0x0350, ExcHandler: java.io.IOException (r5_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x03a9 A:{Splitter: B:33:0x0366, ExcHandler: java.io.IOException (r5_2 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:36:0x0370, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:37:0x0371, code:
            r10.println("Failed to dump input method client: " + r5);
     */
    /* JADX WARNING: Missing block: B:39:0x0390, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:40:0x0391, code:
            r10.println("Failed to dump input method client in focused window: " + r5);
     */
    /* JADX WARNING: Missing block: B:41:0x03a9, code:
            r5 = move-exception;
     */
    /* JADX WARNING: Missing block: B:42:0x03aa, code:
            r10.println("Failed to dump input method service: " + r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            ClientState client;
            ClientState focusedWindowClient;
            IInputMethod method;
            Printer p = new PrintWriterPrinter(pw);
            synchronized (this.mMethodMap) {
                p.println("Current Input Method Manager state:");
                int N = this.mMethodList.size();
                p.println("  Input Methods: mMethodMapUpdateCount=" + this.mMethodMapUpdateCount);
                for (int i = 0; i < N; i++) {
                    InputMethodInfo info = (InputMethodInfo) this.mMethodList.get(i);
                    p.println("  InputMethod #" + i + ":");
                    info.dump(p, "    ");
                }
                p.println("  Clients:");
                for (ClientState ci : this.mClients.values()) {
                    p.println("  Client " + ci + ":");
                    p.println("    client=" + ci.client);
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
                p.println("  mCurToken=" + this.mCurToken);
                p.println("  mCurIntent=" + this.mCurIntent);
                method = this.mCurMethod;
                p.println("  mCurMethod=" + this.mCurMethod);
                p.println("  mEnabledSession=" + this.mEnabledSession);
                p.println("  mImeWindowVis=" + imeWindowStatusToString(this.mImeWindowVis));
                p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
                p.println("  mInFullscreenMode=" + this.mInFullscreenMode);
                p.println("  mCurUserActionNotificationSequenceNumber=" + this.mCurUserActionNotificationSequenceNumber);
                p.println("  mSystemReady=" + this.mSystemReady + " mInteractive=" + this.mIsInteractive);
                p.println("  mSettingsObserver=" + this.mSettingsObserver);
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
                } catch (Exception e) {
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
                } catch (Exception e2) {
                }
            }
            p.println(" ");
            if (method != null) {
                pw.flush();
                try {
                    TransferPipe.dumpAsync(method.asBinder(), fd, args);
                } catch (Exception e3) {
                }
            } else {
                p.println("No input method service.");
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
            Slog.w(TAG, "Unexpected exception");
        }
    }

    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public void setInputMethodLockedByInner(String imeId) {
        int uid = Binder.getCallingUid();
        if (uid == 1000 || uid == 0) {
            synchronized (this.mMethodMap) {
                if (this.mSettings.getIsWriteInputEnable()) {
                    if (TextUtils.isEmpty(imeId)) {
                        if (TextUtils.isEmpty(this.mCurInputId)) {
                            this.mCurInputId = this.mSettings.getSelectedInputMethod();
                        }
                        setInputMethodLocked(this.mCurInputId, this.mSettings.getSelectedInputMethodSubtypeId(this.mCurInputId));
                    } else {
                        setInputMethodLocked(imeId, this.mSettings.getSelectedInputMethodSubtypeId(imeId));
                    }
                }
            }
            return;
        }
        throw new SecurityException("has no permssion to use");
    }

    public void sendEnableKeyguardBroadcastByInner() {
        if (this.mKeyguardManager.inKeyguardRestrictedInputMode()) {
            Intent intent = new Intent();
            intent.setPackage(SYSTEMUI_PACKAGE_NAME);
            intent.setAction(BROADCAST_ACTION_ENABLE_KEYGUARD);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
        }
    }
}
