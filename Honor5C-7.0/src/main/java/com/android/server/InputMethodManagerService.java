package com.android.server;

import android.annotation.IntDef;
import android.app.ActivityManagerNative;
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
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController.ImeSubtypeListItem;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.IInputSessionCallback.Stub;
import com.android.internal.view.InputBindResult;
import com.android.server.HwServiceFactory.IHwInputMethodManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class InputMethodManagerService extends AbsInputMethodManagerService implements ServiceConnection, Callback {
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = false;
    static final boolean DEBUG_RESTORE = false;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HARD_KEYBOARD_SWITCH_CHANGED = 4000;
    static final int MSG_HIDE_CURRENT_INPUT_METHOD = 1035;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_RESTART_INPUT = 2010;
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
    static final String TAG = "InputMethodManagerService";
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    static final long TIME_TO_RECONNECT = 3000;
    boolean bFlag;
    private boolean mAccessibilityRequestingNoSoftKeyboard;
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients;
    final Context mContext;
    EditorInfo mCurAttribute;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurUserActionNotificationSequenceNumber;
    private InputMethodSubtype mCurrentSubtype;
    private Builder mDialogBuilder;
    HashMap<String, Boolean> mEnabledFileMap;
    SessionState mEnabledSession;
    private InputMethodFileManager mFileManager;
    final Handler mHandler;
    private final int mHardKeyboardBehavior;
    private final HardKeyboardListener mHardKeyboardListener;
    final boolean mHasFeature;
    boolean mHaveConnection;
    private final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    private final boolean mImeSelectedOnBoot;
    private PendingIntent mImeSwitchPendingIntent;
    private Notification.Builder mImeSwitcherNotification;
    int mImeWindowVis;
    private InputMethodInfo[] mIms;
    boolean mInputShown;
    boolean mIsDiffIME;
    boolean mIsInteractive;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    boolean mLastInputShown;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList;
    final HashMap<String, InputMethodInfo> mMethodMap;
    private final MyPackageMonitor mMyPackageMonitor;
    final InputBindResult mNoBinding;
    private NotificationManager mNotificationManager;
    private boolean mNotificationShown;
    final Resources mRes;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans;
    final InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    private boolean mShowImeWithHardKeyboard;
    private boolean mShowOngoingImeSwitcherForPhones;
    boolean mShowRequested;
    private final String mSlotIme;
    private StatusBarManagerService mStatusBar;
    private int[] mSubtypeIds;
    private Toast mSubtypeSwitchedByShortCutToast;
    private final InputMethodSubtypeSwitchingController mSwitchingController;
    private AlertDialog mSwitchingDialog;
    private View mSwitchingDialogTitleView;
    boolean mSystemReady;
    private final UserManager mUserManager;
    boolean mVisibleBound;
    final ServiceConnection mVisibleConnection;
    final WindowManagerInternal mWindowManagerInternal;

    /* renamed from: com.android.server.InputMethodManagerService.6 */
    class AnonymousClass6 implements OnClickListener {
        final /* synthetic */ ImeSubtypeListAdapter val$adapter;

        AnonymousClass6(ImeSubtypeListAdapter val$adapter) {
            this.val$adapter = val$adapter;
        }

        public void onClick(DialogInterface dialog, int which) {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (InputMethodManagerService.this.mIms != null && InputMethodManagerService.this.mIms.length > which) {
                    if (InputMethodManagerService.this.mSubtypeIds != null && InputMethodManagerService.this.mSubtypeIds.length > which) {
                        InputMethodInfo im = InputMethodManagerService.this.mIms[which];
                        int subtypeId = InputMethodManagerService.this.mSubtypeIds[which];
                        this.val$adapter.mCheckedItem = which;
                        this.val$adapter.notifyDataSetChanged();
                        InputMethodManagerService.this.hideInputMethodMenu();
                        if (im != null) {
                            if (subtypeId < 0 || subtypeId >= im.getSubtypeCount()) {
                                subtypeId = InputMethodManagerService.NOT_A_SUBTYPE_ID;
                            }
                            InputMethodManagerService.this.mLastInputShown = InputMethodManagerService.this.mInputShown;
                            if (im.getId() != null) {
                                Slog.i(InputMethodManagerService.TAG, "ime choosed, issame: " + im.getId().equals(InputMethodManagerService.this.mCurMethodId) + ",lastInputShown: " + InputMethodManagerService.this.mLastInputShown);
                            }
                            InputMethodManagerService.this.setInputMethodLocked(im.getId(), subtypeId);
                        }
                        return;
                    }
                }
            }
        }
    }

    static final class ClientState {
        final InputBinding binding;
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
            this.binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        }
    }

    @IntDef({0, 1})
    @Retention(RetentionPolicy.SOURCE)
    private @interface HardKeyboardBehavior {
        public static final int WIRED_AFFORDANCE = 1;
        public static final int WIRELESS_AFFORDANCE = 0;
    }

    private class HardKeyboardListener implements OnHardKeyboardStatusChangeListener {
        private HardKeyboardListener() {
        }

        public void onHardKeyboardStatusChange(boolean available) {
            InputMethodManagerService.this.mHandler.sendMessage(InputMethodManagerService.this.mHandler.obtainMessage(InputMethodManagerService.MSG_HARD_KEYBOARD_SWITCH_CHANGED, Integer.valueOf(available ? InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER : 0)));
        }

        public void handleHardKeyboardStatusChange(boolean available) {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (!(InputMethodManagerService.this.mSwitchingDialog == null || InputMethodManagerService.this.mSwitchingDialogTitleView == null || !InputMethodManagerService.this.mSwitchingDialog.isShowing())) {
                    View switchSectionView = InputMethodManagerService.this.mSwitchingDialogTitleView.findViewById(34603136);
                    if (switchSectionView != null) {
                        switchSectionView.setVisibility(available ? 0 : 8);
                    }
                }
            }
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
            boolean z = InputMethodManagerService.DEBUG_RESTORE;
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
            RadioButton radioButton = (RadioButton) view.findViewById(16909186);
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
        private final HashMap<String, List<InputMethodSubtype>> mAdditionalSubtypesMap;
        private final HashMap<String, InputMethodInfo> mMethodMap;

        public InputMethodFileManager(HashMap<String, InputMethodInfo> methodMap, int userId) {
            this.mAdditionalSubtypesMap = new HashMap();
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
            if (!(inputMethodDir.exists() || inputMethodDir.mkdirs())) {
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
                int N = additionalSubtypes.length;
                for (int i = 0; i < N; i += InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
                    InputMethodSubtype subtype = additionalSubtypes[i];
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
            boolean isSetMethodMap = (methodMap == null || methodMap.size() <= 0) ? InputMethodManagerService.DEBUG_RESTORE : true;
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = subtypesFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, NODE_SUBTYPES);
                for (String imiId : allSubtypes.keySet()) {
                    if (!isSetMethodMap || methodMap.containsKey(imiId)) {
                        out.startTag(null, NODE_IMI);
                        out.attribute(null, ATTR_ID, imiId);
                        List<InputMethodSubtype> subtypesList = (List) allSubtypes.get(imiId);
                        int N = subtypesList.size();
                        for (int i = 0; i < N; i += InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
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
                            out.attribute(null, ATTR_IS_AUXILIARY, String.valueOf(subtype.isAuxiliary() ? InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER : 0));
                            out.attribute(null, ATTR_IS_ASCII_CAPABLE, String.valueOf(subtype.isAsciiCapable() ? InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER : 0));
                            out.endTag(null, NODE_SUBTYPE);
                        }
                        out.endTag(null, NODE_IMI);
                    } else {
                        Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + imiId);
                    }
                }
                out.endTag(null, NODE_SUBTYPES);
                out.endDocument();
                subtypesFile.finishWrite(fileOutputStream);
            } catch (IOException e) {
                Slog.w(InputMethodManagerService.TAG, "Error writing subtypes", e);
                if (fileOutputStream != null) {
                    subtypesFile.failWrite(fileOutputStream);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void readAdditionalInputMethodSubtypes(HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile) {
            Throwable th;
            if (allSubtypes != null && subtypesFile != null) {
                allSubtypes.clear();
                Throwable th2 = null;
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = subtypesFile.openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
                    int eventType = parser.getEventType();
                    do {
                        eventType = parser.next();
                        if (eventType == InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_ENABLER) {
                            break;
                        }
                    } while (eventType != InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER);
                    String firstNodeName = parser.getName();
                    if (NODE_SUBTYPES.equals(firstNodeName)) {
                        int depth = parser.getDepth();
                        Object currentImiId = null;
                        ArrayList<InputMethodSubtype> arrayList = null;
                        while (true) {
                            eventType = parser.next();
                            if ((eventType != InputMethodManagerService.MSG_SHOW_IM_CONFIG || parser.getDepth() > depth) && eventType != InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
                                if (eventType == InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_ENABLER) {
                                    String nodeName = parser.getName();
                                    if (NODE_IMI.equals(nodeName)) {
                                        currentImiId = parser.getAttributeValue(null, ATTR_ID);
                                        if (TextUtils.isEmpty(currentImiId)) {
                                            Slog.w(InputMethodManagerService.TAG, "Invalid imi id found in subtypes.xml");
                                        } else {
                                            arrayList = new ArrayList();
                                            allSubtypes.put(currentImiId, arrayList);
                                        }
                                    } else {
                                        if (NODE_SUBTYPE.equals(nodeName)) {
                                            if (TextUtils.isEmpty(currentImiId) || arrayList == null) {
                                                Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + currentImiId);
                                            } else {
                                                int icon = Integer.parseInt(parser.getAttributeValue(null, ATTR_ICON));
                                                int label = Integer.parseInt(parser.getAttributeValue(null, ATTR_LABEL));
                                                String imeSubtypeLocale = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LOCALE);
                                                String languageTag = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG);
                                                String imeSubtypeMode = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_MODE);
                                                String imeSubtypeExtraValue = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_EXTRA_VALUE);
                                                boolean isAuxiliary = "1".equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_AUXILIARY)));
                                                boolean isAsciiCapable = "1".equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_ASCII_CAPABLE)));
                                                InputMethodSubtypeBuilder builder = new InputMethodSubtypeBuilder().setSubtypeNameResId(label).setSubtypeIconResId(icon).setSubtypeLocale(imeSubtypeLocale).setLanguageTag(languageTag).setSubtypeMode(imeSubtypeMode).setSubtypeExtraValue(imeSubtypeExtraValue).setIsAuxiliary(isAuxiliary).setIsAsciiCapable(isAsciiCapable);
                                                String subtypeIdString = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_ID);
                                                if (subtypeIdString != null) {
                                                    builder.setSubtypeId(Integer.parseInt(subtypeIdString));
                                                }
                                                arrayList.add(builder.build());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 != null) {
                            throw th2;
                        }
                        return;
                    }
                    throw new XmlPullParserException("Xml doesn't start with subtypes");
                } catch (Throwable th22) {
                    Throwable th4 = th22;
                    th22 = th;
                    th = th4;
                }
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
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
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
                i = InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER;
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
                i = InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER;
            } else {
                i = 0;
            }
            handler.sendMessage(handler2.obtainMessage(InputMethodManagerService.MSG_SWITCH_IME, i, 0));
        }

        public void hideCurrentInputMethod() {
            this.mHandler.removeMessages(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
            this.mHandler.sendEmptyMessage(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
        }
    }

    private static final class MethodCallback extends Stub {
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

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        private boolean isChangingPackagesOfCurrentUser() {
            return getChangingUserId() == InputMethodManagerService.this.mSettings.getCurrentUserId() ? true : InputMethodManagerService.DEBUG_RESTORE;
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            if (!isChangingPackagesOfCurrentUser()) {
                return InputMethodManagerService.DEBUG_RESTORE;
            }
            synchronized (InputMethodManagerService.this.mMethodMap) {
                String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                int N = InputMethodManagerService.this.mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i = 0; i < N; i += InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
                        InputMethodInfo imi = (InputMethodInfo) InputMethodManagerService.this.mMethodList.get(i);
                        if (imi.getId().equals(curInputMethodId)) {
                            int length = packages.length;
                            int i2 = 0;
                            while (i2 < length) {
                                if (!imi.getPackageName().equals(packages[i2])) {
                                    i2 += InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER;
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
                return InputMethodManagerService.DEBUG_RESTORE;
            }
        }

        public void onSomePackagesChanged() {
            if (isChangingPackagesOfCurrentUser()) {
                synchronized (InputMethodManagerService.this.mMethodMap) {
                    int change;
                    InputMethodInfo inputMethodInfo = null;
                    String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                    int N = InputMethodManagerService.this.mMethodList.size();
                    if (curInputMethodId != null) {
                        for (int i = 0; i < N; i += InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
                            InputMethodInfo imi = (InputMethodInfo) InputMethodManagerService.this.mMethodList.get(i);
                            String imiId = imi.getId();
                            if (imiId.equals(curInputMethodId)) {
                                inputMethodInfo = imi;
                            }
                            change = isPackageDisappearing(imi.getPackageName());
                            if (isPackageModified(imi.getPackageName())) {
                                InputMethodManagerService.this.mFileManager.deleteAllInputMethodSubtypes(imiId);
                            }
                            if (change == InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_ENABLER || change == InputMethodManagerService.MSG_SHOW_IM_CONFIG) {
                                Slog.i(InputMethodManagerService.TAG, "Input method uninstalled, disabling: " + imi.getComponent());
                                InputMethodManagerService.this.setInputMethodEnabledLocked(imi.getId(), InputMethodManagerService.DEBUG_RESTORE);
                            }
                        }
                    }
                    InputMethodManagerService.this.buildInputMethodListLocked(InputMethodManagerService.DEBUG_RESTORE);
                    boolean changed = InputMethodManagerService.DEBUG_RESTORE;
                    if (inputMethodInfo != null) {
                        change = isPackageDisappearing(inputMethodInfo.getPackageName());
                        if (change == InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_ENABLER || change == InputMethodManagerService.MSG_SHOW_IM_CONFIG) {
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
                        InputMethodManagerService.this.updateFromSettingsLocked(InputMethodManagerService.DEBUG_RESTORE);
                    }
                }
            }
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            onSomePackagesChanged();
            if (components != null) {
                int length = components.length;
                for (int i = 0; i < length; i += InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
                    if (packageName.equals(components[i])) {
                        return true;
                    }
                }
            }
            return InputMethodManagerService.DEBUG_RESTORE;
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
        String mLastEnabled;
        boolean mRegistered;
        int mUserId;

        SettingsObserver(Handler handler) {
            super(handler);
            this.mRegistered = InputMethodManagerService.DEBUG_RESTORE;
            this.mLastEnabled = "";
        }

        public void registerContentObserverLocked(int userId) {
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = InputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    InputMethodManagerService.this.mContext.getContentResolver().unregisterContentObserver(this);
                    this.mRegistered = InputMethodManagerService.DEBUG_RESTORE;
                }
                if (this.mUserId != userId) {
                    this.mLastEnabled = "";
                    this.mUserId = userId;
                }
                resolver.registerContentObserver(Secure.getUriFor("default_input_method"), InputMethodManagerService.DEBUG_RESTORE, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("enabled_input_methods"), InputMethodManagerService.DEBUG_RESTORE, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("selected_input_method_subtype"), InputMethodManagerService.DEBUG_RESTORE, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("show_ime_with_hard_keyboard"), InputMethodManagerService.DEBUG_RESTORE, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("accessibility_soft_keyboard_mode"), InputMethodManagerService.DEBUG_RESTORE, this, userId);
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
                    if (Secure.getIntForUser(InputMethodManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, this.mUserId) != InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER) {
                        z = InputMethodManagerService.DEBUG_RESTORE;
                    }
                    inputMethodManagerService.mAccessibilityRequestingNoSoftKeyboard = z;
                    if (InputMethodManagerService.this.mAccessibilityRequestingNoSoftKeyboard) {
                        boolean showRequested = InputMethodManagerService.this.mShowRequested;
                        InputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        InputMethodManagerService.this.mShowRequested = showRequested;
                    } else if (InputMethodManagerService.this.mShowRequested) {
                        InputMethodManagerService.this.showCurrentInputLocked(InputMethodManagerService.MSG_SHOW_IM_SUBTYPE_PICKER, null);
                    }
                } else {
                    boolean enabledChanged = InputMethodManagerService.DEBUG_RESTORE;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.InputMethodManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.InputMethodManagerService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.InputMethodManagerService.<clinit>():void");
    }

    static void restoreEnabledInputMethods(Context context, String prevValue, String newValue) {
        ArrayMap<String, ArraySet<String>> prevMap = InputMethodUtils.parseInputMethodsAndSubtypesString(prevValue);
        for (Entry<String, ArraySet<String>> entry : InputMethodUtils.parseInputMethodsAndSubtypesString(newValue).entrySet()) {
            String imeId = (String) entry.getKey();
            ArraySet<String> prevSubtypes = (ArraySet) prevMap.get(imeId);
            if (prevSubtypes == null) {
                prevSubtypes = new ArraySet(MSG_SHOW_IM_SUBTYPE_ENABLER);
                prevMap.put(imeId, prevSubtypes);
            }
            prevSubtypes.addAll((ArraySet) entry.getValue());
        }
        Secure.putString(context.getContentResolver(), "enabled_input_methods", InputMethodUtils.buildInputMethodsAndSubtypesString(prevMap));
    }

    void onUnlockUser(int userId) {
        boolean z = DEBUG_RESTORE;
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId != currentUserId) {
                return;
            }
            InputMethodSettings inputMethodSettings = this.mSettings;
            if (!this.mSystemReady) {
                z = true;
            }
            inputMethodSettings.switchCurrentUser(currentUserId, z);
            buildInputMethodListLocked(DEBUG_RESTORE);
            updateInputMethodsFromSettingsLocked(true);
        }
    }

    void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public InputMethodManagerService(Context context) {
        this.mNoBinding = new InputBindResult(null, null, null, NOT_A_SUBTYPE_ID, NOT_A_SUBTYPE_ID);
        this.mMethodList = new ArrayList();
        this.mMethodMap = new HashMap();
        this.mSecureSuggestionSpans = new LruCache(SECURE_SUGGESTION_SPANS_MAX_SIZE);
        this.mEnabledFileMap = new HashMap();
        this.mVisibleConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
        this.mVisibleBound = DEBUG_RESTORE;
        this.mClients = new HashMap();
        this.mShortcutInputMethodsAndSubtypes = new HashMap();
        this.mIsInteractive = true;
        this.mCurUserActionNotificationSequenceNumber = 0;
        this.mBackDisposition = 0;
        this.mMyPackageMonitor = new MyPackageMonitor();
        this.bFlag = DEBUG_RESTORE;
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
        this.mHardKeyboardListener = new HardKeyboardListener();
        this.mHasFeature = context.getPackageManager().hasSystemFeature("android.software.input_methods");
        this.mSlotIme = this.mContext.getString(17039389);
        this.mHardKeyboardBehavior = this.mContext.getResources().getInteger(17694932);
        Bundle extras = new Bundle();
        extras.putBoolean("android.allowDuringSetup", true);
        this.mImeSwitcherNotification = new Notification.Builder(this.mContext).setSmallIcon(33751156).setWhen(0).setOngoing(true).addExtras(extras).setCategory("sys");
        this.mImeSwitchPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent("android.settings.SHOW_INPUT_METHOD_PICKER"), 0);
        this.mShowOngoingImeSwitcherForPhones = DEBUG_RESTORE;
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        broadcastFilter.addAction("android.intent.action.USER_ADDED");
        broadcastFilter.addAction("android.intent.action.USER_REMOVED");
        broadcastFilter.addAction("android.os.action.SETTING_RESTORED");
        this.mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
        this.mNotificationShown = DEBUG_RESTORE;
        int userId = 0;
        try {
            userId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        this.mSettings = new InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, this.mMethodList, userId, this.mSystemReady ? DEBUG_RESTORE : true);
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, userId);
        synchronized (this.mMethodMap) {
            this.mSwitchingController = InputMethodSubtypeSwitchingController.createInstanceLocked(this.mSettings, context);
        }
        this.mImeSelectedOnBoot = TextUtils.isEmpty(this.mSettings.getSelectedInputMethod()) ? DEBUG_RESTORE : true;
        createFlagIfNecessary(userId);
        synchronized (this.mMethodMap) {
            buildInputMethodListLocked(this.mImeSelectedOnBoot ? DEBUG_RESTORE : true);
        }
        this.mSettings.enableAllIMEsIfThereIsNoEnabledIME();
        if (!this.mImeSelectedOnBoot) {
            Slog.w(TAG, "No IME selected. Choose the most applicable IME.");
            synchronized (this.mMethodMap) {
                resetDefaultImeLocked(context);
            }
        }
        synchronized (this.mMethodMap) {
            this.mSettingsObserver.registerContentObserverLocked(userId);
            updateFromSettingsLocked(true);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (InputMethodManagerService.this.mMethodMap) {
                    InputMethodManagerService.this.mSettings.putSelectedSubtype(InputMethodManagerService.NOT_A_SUBTYPE_ID);
                    InputMethodManagerService.this.resetStateIfCurrentLocaleChangedLocked();
                }
            }
        }, filter);
    }

    private void resetDefaultImeLocked(Context context) {
    }

    private void resetAllInternalStateLocked(boolean updateOnlyWhenLocaleChanged, boolean resetDefaultEnabledIme) {
        if (this.mSystemReady) {
            LocaleList newLocales = this.mRes.getConfiguration().getLocales();
            if (!(updateOnlyWhenLocaleChanged && (newLocales == null || newLocales.equals(this.mLastSystemLocales)))) {
                if (!updateOnlyWhenLocaleChanged) {
                    hideCurrentInputLocked(0, null);
                    resetCurrentMethodAndClient(6);
                }
                buildInputMethodListLocked(resetDefaultEnabledIme);
                if (updateOnlyWhenLocaleChanged) {
                    resetDefaultImeLocked(this.mContext);
                } else if (TextUtils.isEmpty(this.mSettings.getSelectedInputMethod())) {
                    resetDefaultImeLocked(this.mContext);
                }
                updateFromSettingsLocked(true);
                this.mLastSystemLocales = newLocales;
                if (!updateOnlyWhenLocaleChanged) {
                    try {
                        startInputInnerLocked();
                    } catch (RuntimeException e) {
                        Slog.w(TAG, "Unexpected exception", e);
                    }
                }
            }
        }
    }

    private void resetStateIfCurrentLocaleChangedLocked() {
        resetAllInternalStateLocked(true, true);
    }

    private void switchUserLocked(int newUserId) {
        createFlagIfNecessary(newUserId);
        this.mSettingsObserver.registerContentObserverLocked(newUserId);
        boolean useCopyOnWriteSettings = (this.mSystemReady && this.mUserManager.isUserUnlockingOrUnlocked(newUserId)) ? DEBUG_RESTORE : true;
        this.mSettings.switchCurrentUser(newUserId, useCopyOnWriteSettings);
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, newUserId);
        boolean initialUserSwitch = TextUtils.isEmpty(this.mSettings.getSelectedInputMethod());
        resetAllInternalStateLocked(DEBUG_RESTORE, initialUserSwitch);
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
                boolean z;
                this.mSystemReady = true;
                int currentUserId = this.mSettings.getCurrentUserId();
                InputMethodSettings inputMethodSettings = this.mSettings;
                if (this.mUserManager.isUserUnlockingOrUnlocked(currentUserId)) {
                    z = DEBUG_RESTORE;
                } else {
                    z = true;
                }
                inputMethodSettings.switchCurrentUser(currentUserId, z);
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, DEBUG_RESTORE);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                this.mShowOngoingImeSwitcherForPhones = this.mRes.getBoolean(17956870);
                if (this.mShowOngoingImeSwitcherForPhones) {
                    this.mWindowManagerInternal.setOnHardKeyboardStatusChangeListener(this.mHardKeyboardListener);
                }
                buildInputMethodListLocked(this.mImeSelectedOnBoot ? DEBUG_RESTORE : true);
                if (!this.mImeSelectedOnBoot) {
                    Slog.w(TAG, "Reset the default IME as \"Resource\" is ready here.");
                    resetStateIfCurrentLocaleChangedLocked();
                    InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
                }
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
                try {
                    startInputInnerLocked();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Unexpected exception", e);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == MSG_UNBIND_INPUT || this.mSettings.isCurrentProfile(userId) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return DEBUG_RESTORE;
    }

    private boolean calledWithValidToken(IBinder token) {
        if (token == null || this.mCurToken != token) {
            return DEBUG_RESTORE;
        }
        return true;
    }

    private boolean bindCurrentInputMethodService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return DEBUG_RESTORE;
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

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo imi;
            List<InputMethodSubtype> emptyList;
            if (imiId == null) {
                if (this.mCurMethodId != null) {
                    imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
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
                emptyList = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, allowsImplicitlySelectedSubtypes);
                return emptyList;
            }
            emptyList = Collections.emptyList();
            return emptyList;
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
                this.mBoundToMethod = DEBUG_RESTORE;
                if (this.mCurMethod != null) {
                    executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(MSG_UNBIND_INPUT, this.mCurMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_UNBIND_CLIENT, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = DEBUG_RESTORE;
            this.mCurClient = null;
            hideInputMethodMenuLocked();
        }
    }

    private int getImeShowFlags() {
        if (this.mShowForced) {
            return MSG_SHOW_IM_CONFIG;
        }
        if (this.mShowExplicitlyRequested) {
            return MSG_SHOW_IM_SUBTYPE_PICKER;
        }
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return MSG_SHOW_IM_SUBTYPE_ENABLER;
        }
        if (this.mShowExplicitlyRequested) {
            return 0;
        }
        return MSG_SHOW_IM_SUBTYPE_PICKER;
    }

    InputBindResult attachNewInputLocked(boolean initial) {
        InputChannel inputChannel = null;
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_BIND_INPUT, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        SessionState session = this.mCurClient.curSession;
        if (initial) {
            executeOrSendMessage(session.method, this.mCaller.obtainMessageIOOO(MSG_START_INPUT, this.mCurInputContextMissingMethods, session, this.mCurInputContext, this.mCurAttribute));
        } else {
            executeOrSendMessage(session.method, this.mCaller.obtainMessageIOOO(MSG_RESTART_INPUT, this.mCurInputContextMissingMethods, session, this.mCurInputContext, this.mCurAttribute));
        }
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
        return new InputBindResult(iInputMethodSession, inputChannel, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
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
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
        }
    }

    InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, cs.uid, attribute.packageName)) {
            if (this.mCurClient != cs) {
                this.mCurClientInKeyguard = isKeyguardLocked();
                unbindCurrentClientLocked(MSG_SHOW_IM_SUBTYPE_PICKER);
                if (this.mIsInteractive) {
                    executeOrSendMessage(cs.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive ? MSG_SHOW_IM_SUBTYPE_PICKER : 0, cs));
                }
            }
            this.mCurSeq += MSG_SHOW_IM_SUBTYPE_PICKER;
            if (this.mCurSeq <= 0) {
                this.mCurSeq = MSG_SHOW_IM_SUBTYPE_PICKER;
            }
            this.mCurClient = cs;
            this.mCurInputContext = inputContext;
            this.mCurInputContextMissingMethods = missingMethods;
            this.mCurAttribute = attribute;
            if (this.mCurId != null && this.mCurId.equals(this.mCurMethodId)) {
                if (cs.curSession != null) {
                    if ((DumpState.DUMP_INSTALLS & controlFlags) != 0) {
                        this.mShowRequested = true;
                    }
                    return attachNewInputLocked((controlFlags & DumpState.DUMP_SHARED_USERS) != 0 ? true : DEBUG_RESTORE);
                } else if (this.mHaveConnection) {
                    if (this.mCurMethod != null) {
                        requestClientSessionLocked(cs);
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else if (SystemClock.uptimeMillis() < this.mLastBindTime + TIME_TO_RECONNECT) {
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else {
                        Object[] objArr = new Object[MSG_SHOW_IM_CONFIG];
                        objArr[0] = this.mCurMethodId;
                        objArr[MSG_SHOW_IM_SUBTYPE_PICKER] = Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime);
                        objArr[MSG_SHOW_IM_SUBTYPE_ENABLER] = Integer.valueOf(0);
                        EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, objArr);
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
        if (this.mCurMethodId == null || !this.mMethodMap.containsKey(this.mCurMethodId)) {
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
            this.mCurIntent.putExtra("android.intent.extra.client_label", 17040471);
            this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
            if (bindCurrentInputMethodService(this.mCurIntent, this, 1610612741)) {
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mHaveConnection = true;
                this.mCurId = info.getId();
                this.mCurToken = new Binder();
                try {
                    Slog.v(TAG, "Adding window token: " + this.mCurToken);
                    this.mIWindowManager.addWindowToken(this.mCurToken, 2011);
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

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(DEBUG_RESTORE);
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

    void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
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
            InputBindResult res = attachNewInputLocked(true);
            if (res.method != null) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(MSG_BIND_CLIENT, this.mCurClient.client, res));
            }
        }
    }

    void unbindCurrentMethodLocked(boolean savePosition) {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = DEBUG_RESTORE;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = DEBUG_RESTORE;
        }
        if (this.mCurToken != null) {
            try {
                if ((this.mImeWindowVis & MSG_SHOW_IM_SUBTYPE_PICKER) != 0 && savePosition) {
                    this.mWindowManagerInternal.saveLastInputMethodWindowForTransition();
                }
                this.mIWindowManager.removeWindowToken(this.mCurToken);
            } catch (RemoteException e) {
            }
            this.mCurToken = null;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(DEBUG_RESTORE);
        unbindCurrentClientLocked(unbindClientReason);
    }

    void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[MSG_SHOW_IM_SUBTYPE_PICKER], new MethodCallback(this, this.mCurMethod, channels[0])));
        }
    }

    void clearClientSessionLocked(ClientState cs) {
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = DEBUG_RESTORE;
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
            this.mStatusBar.setIconVisibility(this.mSlotIme, DEBUG_RESTORE);
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = DEBUG_RESTORE;
                if (this.mCurClient != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_UNBIND_CLIENT, MSG_SHOW_IM_CONFIG, this.mCurSeq, this.mCurClient.client));
                }
            }
        }
    }

    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (iconId == 0) {
                        if (this.mStatusBar != null) {
                            this.mStatusBar.setIconVisibility(this.mSlotIme, DEBUG_RESTORE);
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
                    return;
                }
                Slog.e(TAG, "Ignoring updateStatusIcon due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        if (!this.mShowOngoingImeSwitcherForPhones) {
            return DEBUG_RESTORE;
        }
        if (this.mSwitchingDialog != null) {
            return DEBUG_RESTORE;
        }
        if (isScreenLocked()) {
            return DEBUG_RESTORE;
        }
        if ((visibility & MSG_SHOW_IM_SUBTYPE_PICKER) == 0) {
            return DEBUG_RESTORE;
        }
        if (this.mWindowManagerInternal.isHardKeyboardAvailable()) {
            if (this.mHardKeyboardBehavior == 0) {
                return true;
            }
        } else if ((visibility & MSG_SHOW_IM_SUBTYPE_ENABLER) == 0) {
            return DEBUG_RESTORE;
        }
        List<InputMethodInfo> imis = this.mSettings.getEnabledInputMethodListLocked();
        int N = imis.size();
        if (N > MSG_SHOW_IM_SUBTYPE_ENABLER) {
            return true;
        }
        if (N < MSG_SHOW_IM_SUBTYPE_PICKER) {
            return DEBUG_RESTORE;
        }
        int nonAuxCount = 0;
        int auxCount = 0;
        InputMethodSubtype nonAuxSubtype = null;
        InputMethodSubtype auxSubtype = null;
        for (int i = 0; i < N; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
            List<InputMethodSubtype> subtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, (InputMethodInfo) imis.get(i), true);
            int subtypeCount = subtypes.size();
            if (subtypeCount == 0) {
                nonAuxCount += MSG_SHOW_IM_SUBTYPE_PICKER;
            } else {
                for (int j = 0; j < subtypeCount; j += MSG_SHOW_IM_SUBTYPE_PICKER) {
                    InputMethodSubtype subtype = (InputMethodSubtype) subtypes.get(j);
                    if (subtype.isAuxiliary()) {
                        auxCount += MSG_SHOW_IM_SUBTYPE_PICKER;
                        auxSubtype = subtype;
                    } else {
                        nonAuxCount += MSG_SHOW_IM_SUBTYPE_PICKER;
                        nonAuxSubtype = subtype;
                    }
                }
            }
        }
        if (nonAuxCount > MSG_SHOW_IM_SUBTYPE_PICKER || auxCount > MSG_SHOW_IM_SUBTYPE_PICKER) {
            return true;
        }
        if (nonAuxCount != MSG_SHOW_IM_SUBTYPE_PICKER || auxCount != MSG_SHOW_IM_SUBTYPE_PICKER) {
            return DEBUG_RESTORE;
        }
        if (nonAuxSubtype == null || r2 == null || ((!nonAuxSubtype.getLocale().equals(r2.getLocale()) && !r2.overridesImplicitlyEnabledSubtype() && !nonAuxSubtype.overridesImplicitlyEnabledSubtype()) || !nonAuxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER))) {
            return true;
        }
        return DEBUG_RESTORE;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null ? this.mKeyguardManager.isKeyguardLocked() : DEBUG_RESTORE;
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            synchronized (this.mMethodMap) {
                this.mImeWindowVis = vis;
                this.mBackDisposition = backDisposition;
                updateSystemUiLocked(token, vis, backDisposition);
            }
            return;
        }
        Slog.e(TAG, "Ignoring setImeWindowStatus due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
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
                    if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
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
                this.mImeSwitcherNotification.setContentTitle(this.mRes.getText(17040399)).setContentText(InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, imi, this.mCurrentSubtype)).setContentIntent(this.mImeSwitchPendingIntent);
                try {
                    boolean isEnableNavBar = System.getIntForUser(this.mContext.getContentResolver(), "enable_navbar", getNaviBarEnabledDefValue(), -2) != 0 ? true : DEBUG_RESTORE;
                    if (!(this.mNotificationManager == null || (this.mIWindowManager.hasNavigationBar() && isEnableNavBar))) {
                        this.mNotificationManager.notifyAsUser(null, 17040399, this.mImeSwitcherNotification.build(), UserHandle.ALL);
                        this.mNotificationShown = true;
                    }
                } catch (RemoteException e) {
                }
            } else if (this.mNotificationShown && this.mNotificationManager != null) {
                this.mNotificationManager.cancelAsUser(null, 17040399, UserHandle.ALL);
                this.mNotificationShown = DEBUG_RESTORE;
            }
            Binder.restoreCallingIdentity(ident);
            return;
        }
        Slog.e(TAG, "Ignoring updateSystemUiLocked due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                for (int i = 0; i < spans.length; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
                    SuggestionSpan ss = spans[i];
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo targetImi = (InputMethodInfo) this.mSecureSuggestionSpans.get(span);
            if (targetImi != null) {
                String[] suggestions = span.getSuggestions();
                if (index < 0 || index >= suggestions.length) {
                    return DEBUG_RESTORE;
                }
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
            } else {
                return DEBUG_RESTORE;
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
            for (int i = 0; i < enabled.size(); i += MSG_SHOW_IM_SUBTYPE_PICKER) {
                InputMethodInfo imm = (InputMethodInfo) enabled.get(i);
                try {
                    ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(imm.getPackageName(), DumpState.DUMP_VERSION, this.mSettings.getCurrentUserId());
                    if (ai != null && ai.enabledSetting == 4) {
                        this.mIPackageManager.setApplicationEnabledSetting(imm.getPackageName(), 0, MSG_SHOW_IM_SUBTYPE_PICKER, this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
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
            Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(16909185);
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
            this.mIsDiffIME = DEBUG_RESTORE;
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
            long ident = Binder.clearCallingIdentity();
            try {
                setSelectedInputMethodAndSubtypeLocked(info, subtypeId, DEBUG_RESTORE);
                this.mCurMethodId = id;
                if (ActivityManagerNative.isSystemReady()) {
                    Intent intent = new Intent("android.intent.action.INPUT_METHOD_CHANGED");
                    intent.addFlags(536870912);
                    intent.putExtra("input_method_id", id);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                }
                unbindCurrentClientLocked(MSG_SHOW_IM_SUBTYPE_ENABLER);
                notifyInputMethodSubtypeChanged(this.mSettings.getCurrentUserId(), info, getCurrentInputMethodSubtypeLocked());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
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
                        return DEBUG_RESTORE;
                    }
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                    }
                    boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                    Binder.restoreCallingIdentity(ident);
                    return showCurrentInputLocked;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    return DEBUG_RESTORE;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mLastInputShown = DEBUG_RESTORE;
        this.mShowRequested = true;
        if (this.mAccessibilityRequestingNoSoftKeyboard) {
            return DEBUG_RESTORE;
        }
        if ((flags & MSG_SHOW_IM_SUBTYPE_ENABLER) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForced = true;
        } else if ((flags & MSG_SHOW_IM_SUBTYPE_PICKER) == 0) {
            this.mShowExplicitlyRequested = true;
        }
        if (!this.mSystemReady) {
            return DEBUG_RESTORE;
        }
        boolean res = DEBUG_RESTORE;
        if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
            }
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(MSG_SHOW_SOFT_INPUT, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, 201326593);
                this.mVisibleBound = true;
            }
            res = true;
        } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + TIME_TO_RECONNECT) {
            Object[] objArr = new Object[MSG_SHOW_IM_CONFIG];
            objArr[0] = this.mCurMethodId;
            objArr[MSG_SHOW_IM_SUBTYPE_PICKER] = Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime);
            objArr[MSG_SHOW_IM_SUBTYPE_ENABLER] = Integer.valueOf(MSG_SHOW_IM_SUBTYPE_PICKER);
            EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, objArr);
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodService(this.mCurIntent, this, 1073741825);
        }
        return res;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        boolean z = DEBUG_RESTORE;
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
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
                        return z;
                    }
                    if (DEBUG_FLOW) {
                        z = "Client requesting input be hidden, pid=";
                        Slog.v(TAG, z + pid);
                    }
                    boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                    Binder.restoreCallingIdentity(ident);
                    return hideCurrentInputLocked;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    return DEBUG_RESTORE;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        boolean shouldHideSoftInput = true;
        if (this.mLastInputShown && this.mIsDiffIME) {
            Slog.i(TAG, "cancel hide ");
            this.mLastInputShown = DEBUG_RESTORE;
            return DEBUG_RESTORE;
        } else if ((flags & MSG_SHOW_IM_SUBTYPE_PICKER) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            return DEBUG_RESTORE;
        } else {
            if (this.mShowForced && (flags & MSG_SHOW_IM_SUBTYPE_ENABLER) != 0) {
                return DEBUG_RESTORE;
            }
            boolean res;
            if (this.mCurMethod == null) {
                shouldHideSoftInput = DEBUG_RESTORE;
            } else if (!this.mInputShown && (this.mImeWindowVis & MSG_SHOW_IM_SUBTYPE_PICKER) == 0) {
                shouldHideSoftInput = DEBUG_RESTORE;
            }
            if (shouldHideSoftInput) {
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_HIDE_SOFT_INPUT, this.mCurMethod, resultReceiver));
                res = true;
            } else {
                res = DEBUG_RESTORE;
            }
            if (this.mHaveConnection && this.mVisibleBound) {
                this.mContext.unbindService(this.mVisibleConnection);
                this.mVisibleBound = DEBUG_RESTORE;
            }
            this.mInputShown = DEBUG_RESTORE;
            this.mShowRequested = DEBUG_RESTORE;
            this.mShowExplicitlyRequested = DEBUG_RESTORE;
            this.mShowForced = DEBUG_RESTORE;
            return res;
        }
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (windowToken != null) {
            return windowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
        }
        return startInput(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult inputBindResult = null;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                ClientState cs = (ClientState) this.mClients.get(client.asBinder());
                if (cs != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                            Slog.w(TAG, "Focus gain on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                            return null;
                        }
                    } catch (RemoteException e) {
                    }
                    if (calledFromValidUser) {
                        if (this.mCurFocusedWindow != windowToken) {
                            this.mCurFocusedWindow = windowToken;
                            this.mCurFocusedWindowClient = cs;
                            boolean isLayoutSizeAtLeast;
                            if ((softInputMode & 240) != 16) {
                                isLayoutSizeAtLeast = this.mRes.getConfiguration().isLayoutSizeAtLeast(MSG_SHOW_IM_CONFIG);
                            } else {
                                isLayoutSizeAtLeast = true;
                            }
                            boolean isTextEditor = (controlFlags & MSG_SHOW_IM_SUBTYPE_ENABLER) != 0 ? true : DEBUG_RESTORE;
                            boolean didStart = DEBUG_RESTORE;
                            switch (softInputMode & 15) {
                                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                                    if (!isTextEditor || !r10) {
                                        if (LayoutParams.mayUseInputMethod(windowFlags)) {
                                            if (DEBUG_FLOW) {
                                                Slog.v(TAG, "Unspecified window will hide input");
                                            }
                                            hideCurrentInputLocked(MSG_SHOW_IM_SUBTYPE_ENABLER, null);
                                            break;
                                        }
                                    } else if (isTextEditor && r10 && (softInputMode & DumpState.DUMP_SHARED_USERS) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Unspecified window will show input");
                                        }
                                        if (attribute != null) {
                                            inputBindResult = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                            didStart = true;
                                        }
                                        showCurrentInputLocked(MSG_SHOW_IM_SUBTYPE_PICKER, null);
                                        break;
                                    }
                                    break;
                                case MSG_SHOW_IM_SUBTYPE_ENABLER /*2*/:
                                    if ((softInputMode & DumpState.DUMP_SHARED_USERS) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to hide input going forward");
                                        }
                                        hideCurrentInputLocked(0, null);
                                        break;
                                    }
                                    break;
                                case MSG_SHOW_IM_CONFIG /*3*/:
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to hide input");
                                    }
                                    hideCurrentInputLocked(0, null);
                                    break;
                                case H.DO_TRAVERSAL /*4*/:
                                    if ((softInputMode & DumpState.DUMP_SHARED_USERS) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to show input going forward");
                                        }
                                        if (attribute != null) {
                                            inputBindResult = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                            didStart = true;
                                        }
                                        showCurrentInputLocked(MSG_SHOW_IM_SUBTYPE_PICKER, null);
                                        break;
                                    }
                                    break;
                                case H.ADD_STARTING /*5*/:
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to always show input");
                                    }
                                    if (attribute != null) {
                                        inputBindResult = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                        didStart = true;
                                    }
                                    showCurrentInputLocked(MSG_SHOW_IM_SUBTYPE_PICKER, null);
                                    break;
                            }
                        }
                        Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client + " attribute=" + attribute + ", token = " + windowToken);
                        if (attribute != null) {
                            InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                            Binder.restoreCallingIdentity(ident);
                            return startInputUncheckedLocked;
                        }
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                    Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                    Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                    hideCurrentInputLocked(0, null);
                    Binder.restoreCallingIdentity(ident);
                    return null;
                }
                throw new IllegalArgumentException("unknown client " + client.asBinder());
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                    if (this.mCurClient.client.asBinder() != client.asBinder()) {
                    }
                    this.mHandler.sendMessage(this.mCaller.obtainMessageI(MSG_SHOW_IM_SUBTYPE_PICKER, auxiliarySubtypeMode));
                }
                Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid " + Binder.getCallingUid() + ": " + client);
                this.mHandler.sendMessage(this.mCaller.obtainMessageI(MSG_SHOW_IM_SUBTYPE_PICKER, auxiliarySubtypeMode));
            }
        }
    }

    public void setInputMethod(IBinder token, String id) {
        if (calledFromValidUser()) {
            setInputMethodWithSubtypeId(token, id, NOT_A_SUBTYPE_ID);
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
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(MSG_SHOW_IM_SUBTYPE_ENABLER, inputMethodId));
            }
        }
    }

    public boolean switchToLastInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo inputMethodInfo;
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme != null) {
                inputMethodInfo = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
            } else {
                inputMethodInfo = null;
            }
            String str = null;
            int subtypeId = NOT_A_SUBTYPE_ID;
            if (!(lastIme == null || inputMethodInfo == null)) {
                boolean imiIdIsSame = inputMethodInfo.getId().equals(this.mCurMethodId);
                int lastSubtypeHash = Integer.parseInt((String) lastIme.second);
                int currentSubtypeHash;
                if (this.mCurrentSubtype == null) {
                    currentSubtypeHash = NOT_A_SUBTYPE_ID;
                } else {
                    currentSubtypeHash = this.mCurrentSubtype.hashCode();
                }
                if (!(imiIdIsSame && lastSubtypeHash == currentSubtypeHash)) {
                    str = lastIme.first;
                    subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(inputMethodInfo, lastSubtypeHash);
                }
            }
            if (TextUtils.isEmpty(str)) {
                if (!InputMethodUtils.canAddToLastInputMethod(this.mCurrentSubtype)) {
                    List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
                    if (enabled != null) {
                        String locale;
                        int N = enabled.size();
                        if (this.mCurrentSubtype == null) {
                            locale = this.mRes.getConfiguration().locale.toString();
                        } else {
                            locale = this.mCurrentSubtype.getLocale();
                        }
                        for (int i = 0; i < N; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
                            InputMethodInfo imi = (InputMethodInfo) enabled.get(i);
                            if (imi.getSubtypeCount() > 0 && InputMethodUtils.isSystemIme(imi)) {
                                InputMethodSubtype keyboardSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, InputMethodUtils.getSubtypes(imi), "keyboard", locale, true);
                                if (keyboardSubtype != null) {
                                    str = imi.getId();
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
            }
            if (TextUtils.isEmpty(str)) {
                return DEBUG_RESTORE;
            }
            setInputMethodWithSubtypeIdLocked(token, str, subtypeId);
            return true;
        }
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        synchronized (this.mMethodMap) {
            if (calledWithValidToken(token)) {
                ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(onlyCurrentIme, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true);
                if (nextSubtype == null) {
                    return DEBUG_RESTORE;
                }
                setInputMethodWithSubtypeIdLocked(token, nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
                return true;
            }
            Slog.e(TAG, "Ignoring switchToNextInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
            return DEBUG_RESTORE;
        }
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        synchronized (this.mMethodMap) {
            if (!calledWithValidToken(token)) {
                Slog.e(TAG, "Ignoring shouldOfferSwitchingToNextInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                return DEBUG_RESTORE;
            } else if (this.mSwitchingController.getNextInputMethodLocked(DEBUG_RESTORE, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true) == null) {
                return DEBUG_RESTORE;
            } else {
                return true;
            }
        }
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null || TextUtils.isEmpty((CharSequence) lastIme.first) || TextUtils.isEmpty((CharSequence) lastIme.second)) {
                return null;
            }
            InputMethodInfo lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
            if (lastImi == null) {
                return null;
            }
            try {
                int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                if (lastSubtypeId < 0 || lastSubtypeId >= lastImi.getSubtypeCount()) {
                    return null;
                }
                InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                return subtypeAt;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        long ident;
        if (calledFromValidUser() && !TextUtils.isEmpty(imiId) && subtypes != null) {
            synchronized (this.mMethodMap) {
                InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(imiId);
                if (imi == null) {
                    return;
                }
                try {
                    String[] packageInfos = this.mIPackageManager.getPackagesForUid(Binder.getCallingUid());
                    if (packageInfos != null) {
                        int packageNum = packageInfos.length;
                        for (int i = 0; i < packageNum; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
                            if (packageInfos[i].equals(imi.getPackageName())) {
                                this.mFileManager.addInputMethodSubtypes(imi, subtypes);
                                ident = Binder.clearCallingIdentity();
                                buildInputMethodListLocked(DEBUG_RESTORE);
                                Binder.restoreCallingIdentity(ident);
                                return;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to get package infos");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight();
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        if (calledFromValidUser()) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mMethodMap) {
                    if (calledWithValidToken(token)) {
                        this.mWindowManagerInternal.clearLastInputMethodWindowForTransition();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    Slog.e(TAG, "Ignoring clearLastInputMethodWindowForTransition due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

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
                } else {
                    Slog.e(TAG, "Ignoring hideInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
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
                } else {
                    Slog.e(TAG, "Ignoring showMySoftInput due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            }
        }
    }

    void setEnabledSessionInMainThread(SessionState session) {
        if (this.mEnabledSession != session) {
            if (!(this.mEnabledSession == null || this.mEnabledSession.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, DEBUG_RESTORE);
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
        int missingMethods;
        SessionState session;
        switch (msg.what) {
            case MSG_SHOW_IM_SUBTYPE_PICKER /*1*/:
                boolean z;
                switch (msg.arg1) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                        z = this.mInputShown;
                        break;
                    case MSG_SHOW_IM_SUBTYPE_PICKER /*1*/:
                        z = true;
                        break;
                    case MSG_SHOW_IM_SUBTYPE_ENABLER /*2*/:
                        z = DEBUG_RESTORE;
                        break;
                    default:
                        Slog.e(TAG, "Unknown subtype picker mode = " + msg.arg1);
                        return DEBUG_RESTORE;
                }
                showInputMethodMenu(z);
                return true;
            case MSG_SHOW_IM_SUBTYPE_ENABLER /*2*/:
                showInputMethodAndSubtypeEnabler((String) msg.obj);
                return true;
            case MSG_SHOW_IM_CONFIG /*3*/:
                showConfigureInputMethods();
                return true;
            case MSG_UNBIND_INPUT /*1000*/:
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
                    break;
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
            case MSG_START_INPUT /*2000*/:
                missingMethods = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    session = args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.startInput((IInputContext) args.arg2, missingMethods, (EditorInfo) args.arg3);
                } catch (RemoteException e7) {
                }
                args.recycle();
                return true;
            case MSG_RESTART_INPUT /*2010*/:
                missingMethods = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    session = (SessionState) args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.restartInput((IInputContext) args.arg2, missingMethods, (EditorInfo) args.arg3);
                } catch (RemoteException e8) {
                }
                args.recycle();
                return true;
            case MSG_UNBIND_CLIENT /*3000*/:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e9) {
                }
                return true;
            case MSG_BIND_CLIENT /*3010*/:
                args = (SomeArgs) msg.obj;
                IInputMethodClient client = args.arg1;
                InputBindResult res = args.arg2;
                try {
                    client.onBindMethod(res);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (RemoteException e10) {
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
                    ((ClientState) msg.obj).client.setActive(msg.arg1 != 0 ? true : DEBUG_RESTORE);
                } catch (RemoteException e11) {
                    Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid " + ((ClientState) msg.obj).pid + " uid " + ((ClientState) msg.obj).uid);
                }
                return true;
            case MSG_SET_INTERACTIVE /*3030*/:
                handleSetInteractive(msg.arg1 != 0 ? true : DEBUG_RESTORE);
                return true;
            case MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER /*3040*/:
                int sequenceNumber = msg.arg1;
                ClientState clientState = msg.obj;
                try {
                    clientState.client.setUserActionNotificationSequenceNumber(sequenceNumber);
                } catch (RemoteException e12) {
                    Slog.w(TAG, "Got RemoteException sending setUserActionNotificationSequenceNumber(" + sequenceNumber + ") notification to pid " + clientState.pid + " uid " + clientState.uid);
                }
                return true;
            case MSG_SWITCH_IME /*3050*/:
                handleSwitchInputMethod(msg.arg1 != 0 ? true : DEBUG_RESTORE);
                return true;
            case MSG_HARD_KEYBOARD_SWITCH_CHANGED /*4000*/:
                this.mHardKeyboardListener.handleHardKeyboardStatusChange(msg.arg1 == MSG_SHOW_IM_SUBTYPE_PICKER ? true : DEBUG_RESTORE);
                return true;
            case MSG_SYSTEM_UNLOCK_USER /*5000*/:
                onUnlockUser(msg.arg1);
                return true;
            default:
                return DEBUG_RESTORE;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        int i = 0;
        synchronized (this.mMethodMap) {
            int i2;
            this.mIsInteractive = interactive;
            IBinder iBinder = this.mCurToken;
            if (interactive) {
                i2 = this.mImeWindowVis;
            } else {
                i2 = 0;
            }
            updateSystemUiLocked(iBinder, i2, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                IInterface iInterface = this.mCurClient.client;
                HandlerCaller handlerCaller = this.mCaller;
                if (this.mIsInteractive) {
                    i = MSG_SHOW_IM_SUBTYPE_PICKER;
                }
                executeOrSendMessage(iInterface, handlerCaller.obtainMessageIO(MSG_SET_ACTIVE, i, this.mCurClient));
            }
        }
    }

    private void handleSwitchInputMethod(boolean forwardDirection) {
        synchronized (this.mMethodMap) {
            ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(DEBUG_RESTORE, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, forwardDirection);
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
        InputMethodInfo imi = InputMethodUtils.getMostApplicableDefaultIME(this.mSettings.getEnabledInputMethodListLocked());
        if (imi == null) {
            return DEBUG_RESTORE;
        }
        resetSelectedInputMethodAndSubtypeLocked(imi.getId());
        return true;
    }

    void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        int i;
        int N;
        this.mMethodList.clear();
        this.mMethodMap.clear();
        List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, this.mSettings.getCurrentUserId());
        HashMap<String, List<InputMethodSubtype>> additionalSubtypes = this.mFileManager.getAllAdditionalInputMethodSubtypes();
        for (i = 0; i < services.size(); i += MSG_SHOW_IM_SUBTYPE_PICKER) {
            ResolveInfo ri = (ResolveInfo) services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if ("android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                if (shouldBuildInputMethodList(si.packageName)) {
                    try {
                        InputMethodInfo p = new InputMethodInfo(this.mContext, ri, additionalSubtypes);
                        this.mMethodList.add(p);
                        String id = p.getId();
                        this.mMethodMap.put(id, p);
                        ensureEnableSystemIME(id, p, this.mContext, this.mSettings.getCurrentUserId());
                    } catch (Exception e) {
                        Slog.wtf(TAG, "Unable to load input method " + compName, e);
                    }
                } else {
                    Slog.w(TAG, "buildInputMethodListLocked: Skipping IME " + si.packageName);
                }
            } else {
                Slog.w(TAG, "Skipping input method " + compName + ": it does not require the permission " + "android.permission.BIND_INPUT_METHOD");
            }
        }
        updateSecureIMEStatus();
        if (!resetDefaultEnabledIme) {
            boolean enabledImeFound = DEBUG_RESTORE;
            List<InputMethodInfo> enabledImes = this.mSettings.getEnabledInputMethodListLocked();
            N = enabledImes.size();
            for (i = 0; i < N; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
                InputMethodInfo imi = (InputMethodInfo) enabledImes.get(i);
                if (this.mMethodList.contains(imi)) {
                    enabledImeFound = true;
                    break;
                }
            }
            if (!enabledImeFound) {
                Slog.i(TAG, "All the enabled IMEs are gone. Reset default enabled IMEs.");
                resetDefaultEnabledIme = true;
                resetSelectedInputMethodAndSubtypeLocked("");
            }
        }
        if (resetDefaultEnabledIme) {
            ArrayList<InputMethodInfo> defaultEnabledIme = InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mSystemReady, this.mMethodList);
            N = defaultEnabledIme.size();
            for (i = 0; i < N; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
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
            return DEBUG_RESTORE;
        }
        return this.mKeyguardManager.isKeyguardSecure();
    }

    private void showInputMethodMenu(boolean showAuxSubtypes) {
        Context context = this.mContext;
        boolean isScreenLocked = isScreenLocked();
        String lastInputMethodId = this.mSettings.getSelectedInputMethod();
        int lastInputMethodSubtypeId = this.mSettings.getSelectedInputMethodSubtypeId(lastInputMethodId);
        synchronized (this.mMethodMap) {
            HashMap<InputMethodInfo, List<InputMethodSubtype>> immis = this.mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(this.mContext);
            if (immis == null || immis.size() == 0) {
                return;
            }
            hideInputMethodMenuLocked();
            List<ImeSubtypeListItem> imList = this.mSwitchingController.getSortedInputMethodAndSubtypeListLocked(showAuxSubtypes, isScreenLocked);
            if (lastInputMethodSubtypeId == NOT_A_SUBTYPE_ID) {
                InputMethodSubtype currentSubtype = getCurrentInputMethodSubtypeLocked();
                if (currentSubtype != null) {
                    lastInputMethodSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), currentSubtype.hashCode());
                }
            }
            int N = imList.size();
            this.mIms = new InputMethodInfo[N];
            this.mSubtypeIds = new int[N];
            int checkedItem = NOT_A_SUBTYPE_ID;
            for (int i = 0; i < N; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
                ImeSubtypeListItem item = (ImeSubtypeListItem) imList.get(i);
                this.mIms[i] = item.mImi;
                this.mSubtypeIds[i] = item.mSubtypeId;
                if (this.mIms[i].getId().equals(lastInputMethodId)) {
                    int subtypeId = this.mSubtypeIds[i];
                    if (!(subtypeId == NOT_A_SUBTYPE_ID || (lastInputMethodSubtypeId == NOT_A_SUBTYPE_ID && subtypeId == 0))) {
                        if (subtypeId == lastInputMethodSubtypeId) {
                        }
                    }
                    checkedItem = i;
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
            Drawable dialogIcon = a.getDrawable(MSG_SHOW_IM_SUBTYPE_ENABLER);
            a.recycle();
            this.mDialogBuilder.setIcon(dialogIcon);
            View tv = ((LayoutInflater) dialogContext.getSystemService(LayoutInflater.class)).inflate(34013191, null);
            this.mDialogBuilder.setCustomTitle(tv);
            this.mSwitchingDialogTitleView = tv;
            View mSwitchSectionView = this.mSwitchingDialogTitleView.findViewById(34603136);
            if (mSwitchSectionView == null) {
                Slog.e(TAG, "mSwitchSectionView is null");
                return;
            }
            mSwitchSectionView.setVisibility(this.mWindowManagerInternal.isHardKeyboardAvailable() ? 0 : 8);
            Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(34603137);
            hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
            hardKeySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    InputMethodManagerService.this.mSettings.setShowImeWithHardKeyboard(isChecked);
                    InputMethodManagerService.this.hideInputMethodMenu();
                }
            });
            ImeSubtypeListAdapter adapter = new ImeSubtypeListAdapter(contextThemeWrapper, 17367270, imList, checkedItem);
            this.mDialogBuilder.setSingleChoiceItems(adapter, checkedItem, new AnonymousClass6(adapter));
            if (!isScreenLocked) {
                this.mDialogBuilder.setPositiveButton(33685720, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        InputMethodManagerService.this.showConfigureInputMethods();
                    }
                });
            }
            this.mSwitchingDialog = this.mDialogBuilder.create();
            this.mSwitchingDialog.setCanceledOnTouchOutside(true);
            this.mSwitchingDialog.getWindow().setType(2012);
            LayoutParams attributes = this.mSwitchingDialog.getWindow().getAttributes();
            attributes.privateFlags |= 16;
            this.mSwitchingDialog.getWindow().getAttributes().setTitle("Select input method");
            updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
            this.mSwitchingDialog.show();
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
            return DEBUG_RESTORE;
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
            this.mSettings.appendAndPutEnabledInputMethodLocked(id, DEBUG_RESTORE);
            return DEBUG_RESTORE;
        }
        if (!this.mSettings.buildAndPutEnabledInputMethodsStrRemovingIdLocked(new StringBuilder(), enabledInputMethodsList, id)) {
            return DEBUG_RESTORE;
        }
        if (id.equals(this.mSettings.getSelectedInputMethod()) && !chooseNewDefaultIMELocked()) {
            Slog.i(TAG, "Can't find new IME, unsetting the current input method.");
            resetSelectedInputMethodAndSubtypeLocked("");
        }
        return true;
    }

    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        if (imi == null || !isSecureIME(imi.getPackageName())) {
            this.mSettings.saveCurrentInputMethodAndSubtypeToHistory(this.mCurMethodId, this.mCurrentSubtype);
            this.mCurUserActionNotificationSequenceNumber = Math.max(this.mCurUserActionNotificationSequenceNumber + MSG_SHOW_IM_SUBTYPE_PICKER, MSG_SHOW_IM_SUBTYPE_PICKER);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER, this.mCurUserActionNotificationSequenceNumber, this.mCurClient));
            }
            if (imi == null || subtypeId < 0) {
                this.mSettings.putSelectedSubtype(NOT_A_SUBTYPE_ID);
                this.mCurrentSubtype = null;
            } else if (subtypeId < imi.getSubtypeCount()) {
                InputMethodSubtype subtype = imi.getSubtypeAt(subtypeId);
                this.mSettings.putSelectedSubtype(subtype.hashCode());
                this.mCurrentSubtype = subtype;
            } else {
                this.mSettings.putSelectedSubtype(NOT_A_SUBTYPE_ID);
                this.mCurrentSubtype = getCurrentInputMethodSubtypeLocked();
            }
            if (!setSubtypeOnly) {
                String id;
                InputMethodSettings inputMethodSettings = this.mSettings;
                if (imi != null) {
                    id = imi.getId();
                } else {
                    id = "";
                }
                inputMethodSettings.putSelectedInputMethod(id);
            }
            return;
        }
        Slog.d(TAG, "setSelectedInputMethodAndSubtypeLocked: Skipping SecureIME");
    }

    private void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(newDefaultIme);
        int lastSubtypeId = NOT_A_SUBTYPE_ID;
        if (!(imi == null || TextUtils.isEmpty(newDefaultIme))) {
            String subtypeHashCode = this.mSettings.getLastSubtypeForInputMethodLocked(newDefaultIme);
            if (subtypeHashCode != null) {
                try {
                    lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, Integer.parseInt(subtypeHashCode));
                } catch (NumberFormatException e) {
                    Slog.w(TAG, "HashCode for subtype looks broken: " + subtypeHashCode, e);
                }
            }
        }
        setSelectedInputMethodAndSubtypeLocked(imi, lastSubtypeId, DEBUG_RESTORE);
    }

    private Pair<InputMethodInfo, InputMethodSubtype> findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        List<InputMethodInfo> imis = this.mSettings.getEnabledInputMethodListLocked();
        Object mostApplicableIMI = null;
        Object mostApplicableSubtype = null;
        boolean foundInSystemIME = DEBUG_RESTORE;
        for (InputMethodInfo imi : imis) {
            String imiId = imi.getId();
            if (!foundInSystemIME || imiId.equals(this.mCurMethodId)) {
                ArrayList<InputMethodSubtype> subtypesForSearch;
                InputMethodSubtype subtype = null;
                List<InputMethodSubtype> enabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, mode, this.mCurrentSubtype.getLocale(), DEBUG_RESTORE);
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
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, mode, this.mCurrentSubtype.getLocale(), DEBUG_RESTORE);
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
                    if ((imi.getServiceInfo().applicationInfo.flags & MSG_SHOW_IM_SUBTYPE_PICKER) != 0) {
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
        if (!subtypeIsSelected || this.mCurrentSubtype == null || !InputMethodUtils.isValidSubtypeId(imi, this.mCurrentSubtype.hashCode())) {
            int subtypeId = this.mSettings.getSelectedInputMethodSubtypeId(this.mCurMethodId);
            if (subtypeId == NOT_A_SUBTYPE_ID) {
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (explicitlyOrImplicitlyEnabledSubtypes.size() == MSG_SHOW_IM_SUBTYPE_PICKER) {
                    this.mCurrentSubtype = (InputMethodSubtype) explicitlyOrImplicitlyEnabledSubtypes.get(0);
                } else if (explicitlyOrImplicitlyEnabledSubtypes.size() > MSG_SHOW_IM_SUBTYPE_PICKER) {
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

    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList();
            if (this.mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
                return ret;
            }
            for (InputMethodInfo imi : this.mShortcutInputMethodsAndSubtypes.keySet()) {
                ret.add(imi);
                for (InputMethodSubtype subtype : (ArrayList) this.mShortcutInputMethodsAndSubtypes.get(imi)) {
                    ret.add(subtype);
                }
            }
            return ret;
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        synchronized (this.mMethodMap) {
            if (subtype != null) {
                if (this.mCurMethodId != null) {
                    int subtypeId = InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), subtype.hashCode());
                    if (subtypeId != NOT_A_SUBTYPE_ID) {
                        setInputMethodLocked(this.mCurMethodId, subtypeId);
                        return true;
                    }
                }
            }
            return DEBUG_RESTORE;
        }
    }

    private static String imeWindowStatusToString(int imeWindowVis) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if ((imeWindowVis & MSG_SHOW_IM_SUBTYPE_PICKER) != 0) {
            sb.append("Active");
            first = DEBUG_RESTORE;
        }
        if ((imeWindowVis & MSG_SHOW_IM_SUBTYPE_ENABLER) != 0) {
            if (!first) {
                sb.append("|");
            }
            sb.append("Visible");
        }
        return sb.toString();
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump InputMethodManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        Printer p = new PrintWriterPrinter(pw);
        synchronized (this.mMethodMap) {
            p.println("Current Input Method Manager state:");
            int N = this.mMethodList.size();
            p.println("  Input Methods:");
            for (int i = 0; i < N; i += MSG_SHOW_IM_SUBTYPE_PICKER) {
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
            ClientState client = this.mCurClient;
            p.println("  mCurClient=" + client + " mCurSeq=" + this.mCurSeq);
            p.println("  mCurFocusedWindow=" + this.mCurFocusedWindow);
            ClientState focusedWindowClient = this.mCurFocusedWindowClient;
            p.println("  mCurFocusedWindowClient=" + focusedWindowClient);
            p.println("  mCurId=" + this.mCurId + " mHaveConnect=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod);
            p.println("  mCurToken=" + this.mCurToken);
            p.println("  mCurIntent=" + this.mCurIntent);
            IInputMethod method = this.mCurMethod;
            p.println("  mCurMethod=" + this.mCurMethod);
            p.println("  mEnabledSession=" + this.mEnabledSession);
            p.println("  mImeWindowVis=" + imeWindowStatusToString(this.mImeWindowVis));
            p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
            p.println("  mCurUserActionNotificationSequenceNumber=" + this.mCurUserActionNotificationSequenceNumber);
            p.println("  mSystemReady=" + this.mSystemReady + " mInteractive=" + this.mIsInteractive);
            p.println("  mSettingsObserver=" + this.mSettingsObserver);
            p.println("  mSwitchingController:");
            this.mSwitchingController.dump(p);
            p.println("  mSettings:");
            this.mSettings.dumpLocked(p, "    ");
        }
        p.println(" ");
        if (client != null) {
            pw.flush();
            try {
                client.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                p.println("Input method client dead: " + e);
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
                focusedWindowClient.client.asBinder().dump(fd, args);
            } catch (RemoteException e2) {
                p.println("Input method client in focused window dead: " + e2);
            }
        }
        p.println(" ");
        if (method != null) {
            pw.flush();
            try {
                method.asBinder().dump(fd, args);
            } catch (RemoteException e22) {
                p.println("Input method service dead: " + e22);
            }
        } else {
            p.println("No input method service.");
        }
    }
}
