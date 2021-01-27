package android.inputmethodservice;

import android.R;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.Region;
import android.inputmethodservice.AbstractInputMethodService;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.IInputMethodPrivilegedOperations;
import com.android.internal.inputmethod.InputMethodPrivilegedOperations;
import com.android.internal.inputmethod.InputMethodPrivilegedOperationsRegistry;
import com.android.internal.telephony.PhoneConstants;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.inputmethod.HwInputContentListenerEx;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class InputMethodService extends AbstractInputMethodService {
    public static final int BACK_DISPOSITION_ADJUST_NOTHING = 3;
    public static final int BACK_DISPOSITION_DEFAULT = 0;
    private static final int BACK_DISPOSITION_MAX = 3;
    private static final int BACK_DISPOSITION_MIN = 0;
    @Deprecated
    public static final int BACK_DISPOSITION_WILL_DISMISS = 2;
    @Deprecated
    public static final int BACK_DISPOSITION_WILL_NOT_DISMISS = 1;
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    public static final int IME_ACTIVE = 1;
    public static final int IME_INVISIBLE = 4;
    public static final int IME_VISIBLE = 2;
    private static final String INPUT_METHOD_EMUI_THEME = "androidhwext:style/Theme.Emui.InputMethod";
    private static final boolean IS_TV;
    static final int MOVEMENT_DOWN = -1;
    static final int MOVEMENT_UP = -2;
    public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
    static final String TAG = "InputMethodService";
    private static final String THP_COMMAND = "THP_InformTypingStatus";
    private static final String THP_SWITCH = "hw_sc.inputmethod.support_touch_host_processing";
    private String SWIFTKEY_PACKAGE_NAME = "com.touchtype.swiftkey";
    final View.OnClickListener mActionClickListener = new View.OnClickListener() {
        /* class android.inputmethodservice.$$Lambda$InputMethodService$wp8DeVGx_WDOPw4F6an7QbwVxf0 */

        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            InputMethodService.this.lambda$new$1$InputMethodService(view);
        }
    };
    int mBackDisposition;
    boolean mCanPreRender;
    FrameLayout mCandidatesFrame;
    boolean mCandidatesViewStarted;
    int mCandidatesVisibility;
    CompletionInfo[] mCurCompletions;
    boolean mDecorViewVisible;
    boolean mDecorViewWasVisible;
    ViewGroup mExtractAccessories;
    View mExtractAction;
    @UnsupportedAppUsage
    ExtractEditText mExtractEditText;
    FrameLayout mExtractFrame;
    @UnsupportedAppUsage
    View mExtractView;
    boolean mExtractViewHidden;
    ExtractedText mExtractedText;
    int mExtractedToken;
    boolean mFullscreenApplied;
    ViewGroup mFullscreenArea;
    private Handler mHandler = new Handler();
    private boolean mHasComposingRegion = false;
    InputMethodManager mImm;
    boolean mInShowWindow;
    LayoutInflater mInflater;
    boolean mInitialized;
    InputBinding mInputBinding;
    InputConnection mInputConnection;
    private HwInputContentListenerEx mInputContentListener = new HwInputContentListenerEx() {
        /* class android.inputmethodservice.InputMethodService.AnonymousClass1 */

        @Override // com.huawei.android.inputmethod.HwInputContentListenerEx
        public void onReceivedInputContent(String content) {
            StringBuilder sb = new StringBuilder();
            sb.append("onReceivedInputContent, length = ");
            sb.append(content == null ? 0 : content.length());
            Log.d(InputMethodService.TAG, sb.toString());
            InputMethodService.this.mHandler.post(new Runnable() {
                /* class android.inputmethodservice.InputMethodService.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    InputMethodService.this.mShowInputFlags = 0;
                    InputMethodService.this.mShowInputRequested = false;
                    InputMethodService.this.doHideWindow();
                }
            });
            if (InputMethodService.this.getCurrentInputConnection() != null && !TextUtils.isEmpty(content)) {
                if (InputMethodService.this.isNeedShowImeOnWindowsCastMode() && HwPCUtils.getHwPCManager() != null) {
                    try {
                        HwPCUtils.getHwPCManager().updateFocusDisplayToWindowsCast();
                    } catch (RemoteException e) {
                        Log.e(InputMethodService.TAG, "onReceivedInputContent remote exception.");
                    }
                }
                if (HwPCUtils.getHwPCManager() != null && InputMethodService.this.SWIFTKEY_PACKAGE_NAME.equals(InputMethodService.this.getPackageName())) {
                    try {
                        if (!HwPCUtils.getHwPCManager().isPadAssistantMode()) {
                            InputMethodService.this.getCurrentInputConnection().finishComposingText();
                            InputMethodService.this.getCurrentInputConnection().commitText(content, 1);
                            return;
                        }
                        if (!InputMethodService.this.mHasComposingRegion) {
                            InputMethodService.this.getCurrentInputConnection().finishComposingText();
                        }
                        InputMethodService.this.getCurrentInputConnection().commitText(content, 1);
                        InputMethodService.this.mHasComposingRegion = false;
                        return;
                    } catch (RemoteException e2) {
                        Log.e(InputMethodService.TAG, "onReceivedInputContent remote exception.");
                    }
                }
                InputMethodService.this.getCurrentInputConnection().commitText(content, 1);
            }
        }

        @Override // com.huawei.android.inputmethod.HwInputContentListenerEx
        public void onReceivedComposingText(String content) {
            StringBuilder sb = new StringBuilder();
            sb.append("onReceivedComposingText, length = ");
            sb.append(content == null ? 0 : content.length());
            Log.d(InputMethodService.TAG, sb.toString());
            InputMethodService.this.mHandler.post(new Runnable() {
                /* class android.inputmethodservice.InputMethodService.AnonymousClass1.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    InputMethodService.this.mShowInputFlags = 0;
                    InputMethodService.this.mShowInputRequested = false;
                    InputMethodService.this.doHideWindow();
                }
            });
            if (InputMethodService.this.getCurrentInputConnection() != null && content != null) {
                if (InputMethodService.this.isNeedShowImeOnWindowsCastMode() && HwPCUtils.getHwPCManager() != null) {
                    try {
                        HwPCUtils.getHwPCManager().updateFocusDisplayToWindowsCast();
                    } catch (RemoteException e) {
                        Log.e(InputMethodService.TAG, "onReceivedComposingText remote exception.");
                    }
                }
                InputMethodService.this.getCurrentInputConnection().setComposingText(content, 1);
                InputMethodService.this.mHasComposingRegion = true;
            }
        }
    };
    EditorInfo mInputEditorInfo;
    FrameLayout mInputFrame;
    boolean mInputStarted;
    View mInputView;
    boolean mInputViewStarted;
    final ViewTreeObserver.OnComputeInternalInsetsListener mInsetsComputer = new ViewTreeObserver.OnComputeInternalInsetsListener() {
        /* class android.inputmethodservice.$$Lambda$InputMethodService$8T9TmAUIN7vW9eU6kTg8309_d4E */

        @Override // android.view.ViewTreeObserver.OnComputeInternalInsetsListener
        public final void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
            InputMethodService.this.lambda$new$0$InputMethodService(internalInsetsInfo);
        }
    };
    boolean mIsFullscreen;
    boolean mIsInputViewShown;
    boolean mIsPreRendered;
    boolean mLastShowInputRequested;
    private Object mLock = new Object();
    @GuardedBy({"mLock"})
    private boolean mNotifyUserActionSent;
    private InputMethodPrivilegedOperations mPrivOps = new InputMethodPrivilegedOperations();
    @UnsupportedAppUsage
    View mRootView;
    @UnsupportedAppUsage
    private SettingsObserver mSettingsObserver;
    int mShowInputFlags;
    boolean mShowInputRequested;
    InputConnection mStartedInputConnection;
    int mStatusIcon;
    private String mTextContentCache;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    int mTheme = 0;
    TypedArray mThemeAttrs;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    final Insets mTmpInsets = new Insets();
    final int[] mTmpLocation = new int[2];
    IBinder mToken;
    boolean mViewsCreated;
    SoftInputWindow mWindow;
    IWindowManager mWindowManager;
    boolean mWindowVisible;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BackDispositionMode {
    }

    public static final class Insets {
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        public static final int TOUCHABLE_INSETS_REGION = 3;
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;
        public int contentTopInsets;
        public int touchableInsets;
        public final Region touchableRegion = new Region();
        public int visibleTopInsets;
    }

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.remoteinput.debugtv", false) || "tv".equals(SystemProperties.get("ro.build.characteristics", PhoneConstants.APN_TYPE_DEFAULT))) {
            z = true;
        }
        IS_TV = z;
    }

    public /* synthetic */ void lambda$new$0$InputMethodService(ViewTreeObserver.InternalInsetsInfo info) {
        if (isExtractViewShown()) {
            View decor = getWindow().getWindow().getDecorView();
            Rect rect = info.contentInsets;
            Rect rect2 = info.visibleInsets;
            int height = decor.getHeight();
            rect2.top = height;
            rect.top = height;
            info.touchableRegion.setEmpty();
            info.setTouchableInsets(0);
            return;
        }
        onComputeInsets(this.mTmpInsets);
        info.contentInsets.top = this.mTmpInsets.contentTopInsets;
        info.visibleInsets.top = this.mTmpInsets.visibleTopInsets;
        info.touchableRegion.set(this.mTmpInsets.touchableRegion);
        info.setTouchableInsets(this.mTmpInsets.touchableInsets);
    }

    public /* synthetic */ void lambda$new$1$InputMethodService(View v) {
        EditorInfo ei = getCurrentInputEditorInfo();
        InputConnection ic = getCurrentInputConnection();
        if (ei != null && ic != null) {
            if (ei.actionId != 0) {
                ic.performEditorAction(ei.actionId);
            } else if ((ei.imeOptions & 255) != 1) {
                ic.performEditorAction(ei.imeOptions & 255);
            }
        }
    }

    public class InputMethodImpl extends AbstractInputMethodService.AbstractInputMethodImpl {
        public InputMethodImpl() {
            super();
        }

        @Override // android.view.inputmethod.InputMethod
        public final void initializeInternal(IBinder token, int displayId, IInputMethodPrivilegedOperations privilegedOperations) {
            if (InputMethodPrivilegedOperationsRegistry.isRegistered(token)) {
                Log.w(InputMethodService.TAG, "The token has already registered, ignore this initialization.");
                return;
            }
            InputMethodService.this.mPrivOps.set(privilegedOperations);
            InputMethodPrivilegedOperationsRegistry.put(token, InputMethodService.this.mPrivOps);
            updateInputMethodDisplay(displayId);
            attachToken(token);
        }

        @Override // android.view.inputmethod.InputMethod
        public void attachToken(IBinder token) {
            if (InputMethodService.this.mToken == null) {
                InputMethodService inputMethodService = InputMethodService.this;
                inputMethodService.mToken = token;
                inputMethodService.mWindow.setToken(token);
                return;
            }
            throw new IllegalStateException("attachToken() must be called at most once. token=" + token);
        }

        @Override // android.view.inputmethod.InputMethod
        public void updateInputMethodDisplay(int displayId) {
            if (displayId != 0) {
                InputMethodService.this.updateDisplay(displayId);
            }
            if (HwPCUtils.isPcCastMode()) {
                HwPCUtils.setPCDisplayID(displayId);
            }
        }

        @Override // android.view.inputmethod.InputMethod
        public void bindInput(InputBinding binding) {
            InputMethodService inputMethodService = InputMethodService.this;
            inputMethodService.mInputBinding = binding;
            inputMethodService.mInputConnection = binding.getConnection();
            InputMethodService.this.reportFullscreenMode();
            InputMethodService.this.initialize();
            InputMethodService.this.onBindInput();
        }

        @Override // android.view.inputmethod.InputMethod
        public void unbindInput() {
            InputMethodService.this.onUnbindInput();
            InputMethodService inputMethodService = InputMethodService.this;
            inputMethodService.mInputBinding = null;
            inputMethodService.mInputConnection = null;
        }

        @Override // android.view.inputmethod.InputMethod
        public void startInput(InputConnection ic, EditorInfo attribute) {
            InputMethodService.this.doStartInput(ic, attribute, false);
        }

        @Override // android.view.inputmethod.InputMethod
        public void restartInput(InputConnection ic, EditorInfo attribute) {
            InputMethodService.this.doStartInput(ic, attribute, true);
        }

        @Override // android.view.inputmethod.InputMethod
        public final void dispatchStartInputWithToken(InputConnection inputConnection, EditorInfo editorInfo, boolean restarting, IBinder startInputToken, boolean shouldPreRenderIme) {
            InputMethodService.this.mPrivOps.reportStartInput(startInputToken);
            InputMethodService.this.mCanPreRender = shouldPreRenderIme;
            if (restarting) {
                restartInput(inputConnection, editorInfo);
            } else {
                startInput(inputConnection, editorInfo);
            }
        }

        @Override // android.view.inputmethod.InputMethod
        public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
            LogPower.push(118);
            int i = 1;
            boolean wasVisible = InputMethodService.this.mIsPreRendered ? InputMethodService.this.mDecorViewVisible && InputMethodService.this.mWindowVisible : InputMethodService.this.isInputViewShown();
            if (InputMethodService.this.mIsPreRendered) {
                InputMethodService inputMethodService = InputMethodService.this;
                inputMethodService.setImeWindowStatus(5, inputMethodService.mBackDisposition);
                InputMethodService.this.applyVisibilityInInsetsConsumer(false);
                InputMethodService.this.onPreRenderedWindowVisibilityChanged(false);
            } else {
                InputMethodService inputMethodService2 = InputMethodService.this;
                inputMethodService2.mShowInputFlags = 0;
                inputMethodService2.mShowInputRequested = false;
                inputMethodService2.doHideWindow();
            }
            boolean visibilityChanged = (InputMethodService.this.mIsPreRendered ? InputMethodService.this.mDecorViewVisible && InputMethodService.this.mWindowVisible : InputMethodService.this.isInputViewShown()) != wasVisible;
            if (resultReceiver != null) {
                if (visibilityChanged) {
                    i = 3;
                } else if (wasVisible) {
                    i = 0;
                }
                resultReceiver.send(i, null);
            }
        }

        @Override // android.view.inputmethod.InputMethod
        public void showSoftInput(int flags, ResultReceiver resultReceiver) {
            if (InputMethodService.DEBUG_FLOW) {
                Log.v(InputMethodService.TAG, "showSoftInput()");
            }
            int i = 0;
            boolean wasVisible = InputMethodService.this.mIsPreRendered ? InputMethodService.this.mDecorViewVisible && InputMethodService.this.mWindowVisible : InputMethodService.this.isInputViewShown();
            if (InputMethodService.this.dispatchOnShowInputRequested(flags, false)) {
                if (InputMethodService.this.mIsPreRendered) {
                    InputMethodService.this.applyVisibilityInInsetsConsumer(true);
                    InputMethodService.this.onPreRenderedWindowVisibilityChanged(true);
                } else {
                    InputMethodService.this.showWindow(true);
                }
            }
            InputMethodService inputMethodService = InputMethodService.this;
            inputMethodService.setImeWindowStatus(inputMethodService.mapToImeWindowStatus(), InputMethodService.this.mBackDisposition);
            boolean visibilityChanged = (InputMethodService.this.mIsPreRendered ? InputMethodService.this.mDecorViewVisible && InputMethodService.this.mWindowVisible : InputMethodService.this.isInputViewShown()) != wasVisible;
            if (resultReceiver != null) {
                if (visibilityChanged) {
                    i = 2;
                } else if (!wasVisible) {
                    i = 1;
                }
                resultReceiver.send(i, null);
            }
        }

        @Override // android.view.inputmethod.InputMethod
        public void changeInputMethodSubtype(InputMethodSubtype subtype) {
            InputMethodService.this.dispatchOnCurrentInputMethodSubtypeChanged(subtype);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyImeHidden() {
        setImeWindowStatus(5, this.mBackDisposition);
        onPreRenderedWindowVisibilityChanged(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setImeWindowStatus(int visibilityFlags, int backDisposition) {
        this.mPrivOps.setImeWindowStatus(visibilityFlags, backDisposition);
    }

    public class InputMethodSessionImpl extends AbstractInputMethodService.AbstractInputMethodSessionImpl {
        public InputMethodSessionImpl() {
            super();
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void finishInput() {
            if (isEnabled()) {
                InputMethodService.this.doFinishInput();
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void displayCompletions(CompletionInfo[] completions) {
            if (isEnabled()) {
                InputMethodService inputMethodService = InputMethodService.this;
                inputMethodService.mCurCompletions = completions;
                inputMethodService.onDisplayCompletions(completions);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void updateExtractedText(int token, ExtractedText text) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateExtractedText(token, text);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void viewClicked(boolean focusChanged) {
            if (isEnabled()) {
                InputMethodService.this.onViewClicked(focusChanged);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void updateCursor(Rect newCursor) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateCursor(newCursor);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void appPrivateCommand(String action, Bundle data) {
            if (isEnabled()) {
                InputMethodService.this.onAppPrivateCommand(action, data);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void toggleSoftInput(int showFlags, int hideFlags) {
            InputMethodService.this.onToggleSoftInput(showFlags, hideFlags);
        }

        @Override // android.view.inputmethod.InputMethodSession
        public void updateCursorAnchorInfo(CursorAnchorInfo info) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateCursorAnchorInfo(info);
                HwInputMethodManager.onUpdateCursorAnchorInfo(info);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession
        public final void notifyImeHidden() {
            InputMethodService.this.notifyImeHidden();
        }
    }

    /* access modifiers changed from: private */
    public static final class SettingsObserver extends ContentObserver {
        private final InputMethodService mService;
        private int mShowImeWithHardKeyboard = 0;

        @Retention(RetentionPolicy.SOURCE)
        private @interface ShowImeWithHardKeyboardType {
            public static final int FALSE = 1;
            public static final int TRUE = 2;
            public static final int UNKNOWN = 0;
        }

        private SettingsObserver(InputMethodService service) {
            super(new Handler(service.getMainLooper()));
            this.mService = service;
        }

        public static SettingsObserver createAndRegister(InputMethodService service) {
            SettingsObserver observer = new SettingsObserver(service);
            service.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD), false, observer);
            service.getContentResolver().registerContentObserver(Settings.Global.getUriFor("navigationbar_is_min"), true, observer);
            return observer;
        }

        /* access modifiers changed from: package-private */
        public void unregister() {
            this.mService.getContentResolver().unregisterContentObserver(this);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @UnsupportedAppUsage
        private boolean shouldShowImeWithHardKeyboard() {
            if (this.mShowImeWithHardKeyboard == 0) {
                this.mShowImeWithHardKeyboard = Settings.Secure.getInt(this.mService.getContentResolver(), Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD, 0) != 0 ? 2 : 1;
            }
            int i = this.mShowImeWithHardKeyboard;
            if (i == 1) {
                return false;
            }
            if (i == 2) {
                return true;
            }
            Log.e(InputMethodService.TAG, "Unexpected mShowImeWithHardKeyboard=" + this.mShowImeWithHardKeyboard);
            return false;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Uri showImeWithHardKeyboardUri = Settings.Secure.getUriFor(Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD);
            Uri navigationBarIsMinUri = Settings.Global.getUriFor("navigationbar_is_min");
            if (showImeWithHardKeyboardUri.equals(uri)) {
                this.mShowImeWithHardKeyboard = Settings.Secure.getInt(this.mService.getContentResolver(), Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD, 0) != 0 ? 2 : 1;
                this.mService.resetStateForNewConfiguration();
            }
            if (navigationBarIsMinUri.equals(uri) && this.mService.mInputStarted && this.mService.isLandScapeMultiWindowMode()) {
                InputMethodService inputMethodService = this.mService;
                inputMethodService.doStartInput(inputMethodService.getCurrentInputConnection(), this.mService.getCurrentInputEditorInfo(), false);
            }
        }

        public String toString() {
            return "SettingsObserver{mShowImeWithHardKeyboard=" + this.mShowImeWithHardKeyboard + "}";
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void setTheme(int theme) {
        if (this.mWindow == null) {
            this.mTheme = theme;
            return;
        }
        throw new IllegalStateException("Must be called before onCreate()");
    }

    @Deprecated
    public boolean enableHardwareAcceleration() {
        if (this.mWindow == null) {
            return ActivityManager.isHighEndGfx();
        }
        throw new IllegalStateException("Must be called before onCreate()");
    }

    @Override // android.app.Service
    public void onCreate() {
        if (this.mTheme == 0) {
            this.mTheme = getResources().getIdentifier(INPUT_METHOD_EMUI_THEME, null, null);
        }
        this.mTheme = Resources.selectSystemTheme(this.mTheme, getApplicationInfo().targetSdkVersion, 16973908, 16973951, 16974142, 16974142);
        super.setTheme(this.mTheme);
        super.onCreate();
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.mSettingsObserver = SettingsObserver.createAndRegister(this);
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mWindow = new SoftInputWindow(this, "InputMethod", this.mTheme, null, null, this.mDispatcherState, 2011, 80, false);
        this.mWindow.getWindow().setFlags(Integer.MIN_VALUE, Integer.MIN_VALUE);
        initViews();
        this.mWindow.getWindow().setLayout(-1, -2);
        this.mWindowManager = WindowManagerGlobal.getWindowManagerService();
    }

    public void onInitializeInterface() {
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            onInitializeInterface();
        }
    }

    /* access modifiers changed from: package-private */
    public void initViews() {
        this.mInitialized = false;
        this.mViewsCreated = false;
        this.mShowInputRequested = false;
        this.mShowInputFlags = 0;
        this.mThemeAttrs = obtainStyledAttributes(R.styleable.InputMethodService);
        this.mRootView = this.mInflater.inflate(com.android.internal.R.layout.input_method, (ViewGroup) null);
        this.mWindow.setContentView(this.mRootView);
        this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        this.mRootView.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsComputer);
        if (Settings.Global.getInt(getContentResolver(), Settings.Global.FANCY_IME_ANIMATIONS, 0) != 0) {
            this.mWindow.getWindow().setWindowAnimations(com.android.internal.R.style.Animation_InputMethodFancy);
        }
        this.mFullscreenArea = (ViewGroup) this.mRootView.findViewById(com.android.internal.R.id.fullscreenArea);
        this.mExtractViewHidden = false;
        this.mExtractFrame = (FrameLayout) this.mRootView.findViewById(16908316);
        this.mExtractView = null;
        this.mExtractEditText = null;
        this.mExtractAccessories = null;
        this.mExtractAction = null;
        this.mFullscreenApplied = false;
        this.mCandidatesFrame = (FrameLayout) this.mRootView.findViewById(16908317);
        this.mInputFrame = (FrameLayout) this.mRootView.findViewById(16908318);
        this.mInputView = null;
        this.mIsInputViewShown = false;
        this.mExtractFrame.setVisibility(8);
        this.mCandidatesVisibility = getCandidatesHiddenVisibility();
        this.mCandidatesFrame.setVisibility(this.mCandidatesVisibility);
        this.mInputFrame.setVisibility(8);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        doFinishInput();
        this.mHandler.removeCallbacksAndMessages(null);
        this.mHandler = null;
        this.mWindow.dismissForDestroyIfNecessary();
        SettingsObserver settingsObserver = this.mSettingsObserver;
        if (settingsObserver != null) {
            settingsObserver.unregister();
            this.mSettingsObserver = null;
        }
        IBinder iBinder = this.mToken;
        if (iBinder != null) {
            InputMethodPrivilegedOperationsRegistry.remove(iBinder);
        }
    }

    @Override // android.app.Service, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetStateForNewConfiguration();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetStateForNewConfiguration() {
        boolean visible = this.mDecorViewVisible;
        int showFlags = this.mShowInputFlags;
        boolean showingInput = this.mShowInputRequested;
        CompletionInfo[] completions = this.mCurCompletions;
        initViews();
        int i = 0;
        this.mInputViewStarted = false;
        this.mCandidatesViewStarted = false;
        if (this.mInputStarted) {
            doStartInput(getCurrentInputConnection(), getCurrentInputEditorInfo(), true);
        }
        if (visible) {
            if (showingInput) {
                if (dispatchOnShowInputRequested(showFlags, true)) {
                    showWindow(true);
                    if (completions != null) {
                        this.mCurCompletions = completions;
                        onDisplayCompletions(completions);
                    }
                } else {
                    doHideWindow();
                }
            } else if (this.mCandidatesVisibility == 0) {
                showWindow(false);
            } else {
                doHideWindow();
            }
            if (onEvaluateInputViewShown()) {
                i = 2;
            }
            setImeWindowStatus(i | 1, this.mBackDisposition);
        }
    }

    @Override // android.inputmethodservice.AbstractInputMethodService
    public AbstractInputMethodService.AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethodImpl();
    }

    @Override // android.inputmethodservice.AbstractInputMethodService
    public AbstractInputMethodService.AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface() {
        return new InputMethodSessionImpl();
    }

    public LayoutInflater getLayoutInflater() {
        return this.mInflater;
    }

    public Dialog getWindow() {
        return this.mWindow;
    }

    public void setBackDisposition(int disposition) {
        if (disposition != this.mBackDisposition) {
            if (disposition > 3 || disposition < 0) {
                Log.e(TAG, "Invalid back disposition value (" + disposition + ") specified.");
                return;
            }
            this.mBackDisposition = disposition;
            setImeWindowStatus(mapToImeWindowStatus(), this.mBackDisposition);
        }
    }

    public int getBackDisposition() {
        return this.mBackDisposition;
    }

    public int getMaxWidth() {
        return ((WindowManager) getSystemService("window")).getDefaultDisplay().getWidth();
    }

    public InputBinding getCurrentInputBinding() {
        return this.mInputBinding;
    }

    public InputConnection getCurrentInputConnection() {
        InputConnection ic = this.mStartedInputConnection;
        if (ic != null) {
            return ic;
        }
        return this.mInputConnection;
    }

    public final boolean switchToPreviousInputMethod() {
        return this.mPrivOps.switchToPreviousInputMethod();
    }

    public final boolean switchToNextInputMethod(boolean onlyCurrentIme) {
        return this.mPrivOps.switchToNextInputMethod(onlyCurrentIme);
    }

    public final boolean shouldOfferSwitchingToNextInputMethod() {
        return this.mPrivOps.shouldOfferSwitchingToNextInputMethod();
    }

    public boolean getCurrentInputStarted() {
        return this.mInputStarted;
    }

    public EditorInfo getCurrentInputEditorInfo() {
        return this.mInputEditorInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFullscreenMode() {
        this.mPrivOps.reportFullscreenMode(this.mIsFullscreen);
    }

    public void updateFullscreenMode() {
        View v;
        View extractView;
        boolean isFullscreen = this.mShowInputRequested && onEvaluateFullscreenMode() && !isNeedHideFullscreenArea();
        boolean changed = this.mLastShowInputRequested != this.mShowInputRequested;
        if (this.mIsFullscreen != isFullscreen || !this.mFullscreenApplied) {
            changed = true;
            this.mIsFullscreen = isFullscreen;
            reportFullscreenMode();
            this.mFullscreenApplied = true;
            initialize();
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mFullscreenArea.getLayoutParams();
            if (isFullscreen) {
                this.mFullscreenArea.setBackgroundDrawable(this.mThemeAttrs.getDrawable(0));
                lp.height = 0;
                lp.weight = 1.0f;
            } else {
                this.mFullscreenArea.setBackgroundDrawable(null);
                lp.height = -2;
                lp.weight = 0.0f;
            }
            ((ViewGroup) this.mFullscreenArea.getParent()).updateViewLayout(this.mFullscreenArea, lp);
            if (isFullscreen) {
                if (this.mExtractView == null && (v = onCreateExtractTextView()) != null) {
                    setExtractView(v);
                }
                startExtractingText(false);
            }
            updateExtractFrameVisibility();
        } else if (isFullscreen && isLandScapeMultiWindowMode()) {
            if (this.mExtractView == null && (extractView = onCreateExtractTextView()) != null) {
                setExtractView(extractView);
            }
            startExtractingText(false);
        }
        if (changed) {
            onConfigureWindow(this.mWindow.getWindow(), isFullscreen, true ^ this.mShowInputRequested);
            this.mLastShowInputRequested = this.mShowInputRequested;
        }
    }

    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        int i = this.mWindow.getWindow().getAttributes().height;
        int newHeight = isFullscreen ? -1 : -2;
        boolean z = this.mIsInputViewShown;
        this.mWindow.getWindow().setLayout(-1, newHeight);
    }

    public boolean isFullscreenMode() {
        return this.mIsFullscreen;
    }

    public boolean onEvaluateFullscreenMode() {
        if (getResources().getConfiguration().orientation != 2) {
            return false;
        }
        EditorInfo editorInfo = this.mInputEditorInfo;
        if (editorInfo == null || (editorInfo.imeOptions & 33554432) == 0) {
            return true;
        }
        return false;
    }

    public void setExtractViewShown(boolean shown) {
        if (this.mExtractViewHidden == shown) {
            this.mExtractViewHidden = !shown;
            updateExtractFrameVisibility();
        }
    }

    public boolean isExtractViewShown() {
        return this.mIsFullscreen && !this.mExtractViewHidden;
    }

    /* access modifiers changed from: package-private */
    public void updateExtractFrameVisibility() {
        int vis;
        if (isFullscreenMode()) {
            vis = this.mExtractViewHidden ? 4 : 0;
            this.mExtractFrame.setVisibility(vis);
        } else {
            vis = 0;
            this.mExtractFrame.setVisibility(8);
        }
        int i = 1;
        updateCandidatesVisibility(this.mCandidatesVisibility == 0);
        if (this.mDecorViewWasVisible && this.mFullscreenArea.getVisibility() != vis) {
            TypedArray typedArray = this.mThemeAttrs;
            if (vis != 0) {
                i = 2;
            }
            int animRes = typedArray.getResourceId(i, 0);
            if (animRes != 0) {
                this.mFullscreenArea.startAnimation(AnimationUtils.loadAnimation(this, animRes));
            }
        }
        this.mFullscreenArea.setVisibility(vis);
    }

    public void onComputeInsets(Insets outInsets) {
        int[] loc = this.mTmpLocation;
        if (this.mInputFrame.getVisibility() == 0) {
            this.mInputFrame.getLocationInWindow(loc);
        } else {
            loc[1] = getWindow().getWindow().getDecorView().getHeight();
        }
        if (isFullscreenMode()) {
            outInsets.contentTopInsets = getWindow().getWindow().getDecorView().getHeight();
        } else {
            outInsets.contentTopInsets = loc[1];
        }
        if (this.mCandidatesFrame.getVisibility() == 0) {
            this.mCandidatesFrame.getLocationInWindow(loc);
        }
        outInsets.visibleTopInsets = loc[1];
        outInsets.touchableInsets = 2;
        outInsets.touchableRegion.setEmpty();
    }

    public void updateInputViewShown() {
        int i = 0;
        boolean isShown = this.mShowInputRequested && onEvaluateInputViewShown();
        if (this.mIsInputViewShown != isShown && this.mDecorViewVisible) {
            this.mIsInputViewShown = isShown;
            FrameLayout frameLayout = this.mInputFrame;
            if (!isShown) {
                i = 8;
            }
            frameLayout.setVisibility(i);
            if (this.mInputView == null) {
                initialize();
                View v = onCreateInputView();
                if (v != null) {
                    setInputView(v);
                }
            }
        }
    }

    public boolean isShowInputRequested() {
        return this.mShowInputRequested;
    }

    public boolean isInputViewShown() {
        if (this.mCanPreRender) {
            return this.mWindowVisible;
        }
        return this.mIsInputViewShown && this.mDecorViewVisible;
    }

    public boolean onEvaluateInputViewShown() {
        if (this.mSettingsObserver == null) {
            Log.w(TAG, "onEvaluateInputViewShown: mSettingsObserver must not be null here.");
            return false;
        } else if (!isNeedShowImeOnPCDisplay()) {
            Log.w(TAG, "do not need show ime on PCDisplay now");
            return false;
        } else if (!isNeedShowImeOnWindowsCastMode()) {
            return false;
        } else {
            if (this.mSettingsObserver.shouldShowImeWithHardKeyboard()) {
                return true;
            }
            Configuration config = getResources().getConfiguration();
            if (config.keyboard == 1 || config.hardKeyboardHidden == 2) {
                return true;
            }
            return false;
        }
    }

    private boolean isNeedShowImeOnPCDisplay() {
        if (!HwPCUtils.isValidExtDisplayId(HwPCUtils.getPCDisplayID()) || HwPCUtils.enabledInPad() || HwPCUtils.mTouchDeviceID != -1 || HwPCUtils.isHiCarCastModeForClient()) {
            return true;
        }
        return false;
    }

    private boolean isNeedHideFullscreenArea() {
        if (!HwPCUtils.isValidExtDisplayId(HwPCUtils.getPCDisplayID()) || HwPCUtils.enabledInPad() || HwPCUtils.mTouchDeviceID == -1) {
            return false;
        }
        return true;
    }

    public void setCandidatesViewShown(boolean shown) {
        updateCandidatesVisibility(shown);
        if (!this.mShowInputRequested && this.mDecorViewVisible != shown) {
            if (shown) {
                showWindow(false);
            } else {
                doHideWindow();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateCandidatesVisibility(boolean shown) {
        int vis = shown ? 0 : getCandidatesHiddenVisibility();
        if (this.mCandidatesVisibility != vis) {
            this.mCandidatesFrame.setVisibility(vis);
            this.mCandidatesVisibility = vis;
        }
    }

    public int getCandidatesHiddenVisibility() {
        return isExtractViewShown() ? 8 : 4;
    }

    public void showStatusIcon(int iconResId) {
        this.mStatusIcon = iconResId;
        this.mPrivOps.updateStatusIcon(getPackageName(), iconResId);
        if (HwPCUtils.isPcCastMode()) {
            HwPCUtils.showImeStatusIcon(iconResId, getPackageName());
        }
    }

    public void hideStatusIcon() {
        this.mStatusIcon = 0;
        this.mPrivOps.updateStatusIcon(null, 0);
        if (HwPCUtils.isPcCastMode()) {
            HwPCUtils.hideImeStatusIcon(getPackageName());
        }
    }

    public void switchInputMethod(String id) {
        this.mPrivOps.setInputMethod(id);
    }

    public final void switchInputMethod(String id, InputMethodSubtype subtype) {
        this.mPrivOps.setInputMethodAndSubtype(id, subtype);
    }

    public void setExtractView(View view) {
        this.mExtractFrame.removeAllViews();
        this.mExtractFrame.addView(view, new FrameLayout.LayoutParams(-1, -1));
        this.mExtractView = view;
        if (view != null) {
            this.mExtractEditText = (ExtractEditText) view.findViewById(16908325);
            this.mExtractEditText.setIME(this);
            this.mExtractAction = view.findViewById(com.android.internal.R.id.inputExtractAction);
            if (this.mExtractAction != null) {
                this.mExtractAccessories = (ViewGroup) view.findViewById(com.android.internal.R.id.inputExtractAccessories);
            }
            startExtractingText(false);
            return;
        }
        this.mExtractEditText = null;
        this.mExtractAccessories = null;
        this.mExtractAction = null;
    }

    public void setCandidatesView(View view) {
        this.mCandidatesFrame.removeAllViews();
        this.mCandidatesFrame.addView(view, new FrameLayout.LayoutParams(-1, -2));
    }

    public void setInputView(View view) {
        this.mInputFrame.removeAllViews();
        this.mInputFrame.addView(view, new FrameLayout.LayoutParams(-1, -2));
        this.mInputView = view;
    }

    public View onCreateExtractTextView() {
        return this.mInflater.inflate(com.android.internal.R.layout.input_method_extract_view, (ViewGroup) null);
    }

    public View onCreateCandidatesView() {
        return null;
    }

    public View onCreateInputView() {
        return null;
    }

    public void onStartInputView(EditorInfo info, boolean restarting) {
    }

    public void onFinishInputView(boolean finishingInput) {
        InputConnection ic;
        if (!finishingInput && (ic = getCurrentInputConnection()) != null) {
            ic.finishComposingText();
        }
    }

    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
    }

    public void onFinishCandidatesView(boolean finishingInput) {
        InputConnection ic;
        if (!finishingInput && (ic = getCurrentInputConnection()) != null) {
            ic.finishComposingText();
        }
    }

    public boolean onShowInputRequested(int flags, boolean configChange) {
        if (!onEvaluateInputViewShown()) {
            return false;
        }
        if ((flags & 1) == 0) {
            if (!configChange && onEvaluateFullscreenMode()) {
                return false;
            }
            if (!isNeedShowImeOnPCDisplay()) {
                Log.w(TAG, "the input method in PCDisplay now");
                return false;
            } else if (!this.mSettingsObserver.shouldShowImeWithHardKeyboard() && getResources().getConfiguration().keyboard != 1) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean dispatchOnShowInputRequested(int flags, boolean configChange) {
        onStartInputForMultiDisplay();
        onStartInputForTv();
        onShowInputRequestedForMultiDisplay();
        boolean result = isNeedShowImeOnWindowsCastMode() && onShowInputRequested(flags, configChange);
        if (result) {
            this.mShowInputFlags = flags;
        } else {
            this.mShowInputFlags = 0;
        }
        return result;
    }

    public void showWindow(boolean showInput) {
        if (DEBUG_FLOW) {
            Log.v(TAG, "Showing window: showInput=" + showInput + " mShowInputRequested=" + this.mShowInputRequested + " mViewsCreated=" + this.mViewsCreated + " mDecorViewVisible=" + this.mDecorViewVisible + " mWindowVisible=" + this.mWindowVisible + " mInputStarted=" + this.mInputStarted + " mShowInputFlags=" + this.mShowInputFlags);
        }
        if (SystemProperties.getBoolean(THP_SWITCH, false)) {
            try {
                HwInputManager.runHwTHPCommand(THP_COMMAND, "1");
            } catch (SecurityException e) {
                Log.w(TAG, "showWindow runHwTHPCommand error occured: the package has no permission");
            }
        }
        if (this.mInShowWindow) {
            Log.w(TAG, "Re-entrance in to showWindow");
            return;
        }
        HwInputMethodManager.raiseIawarePriority();
        this.mDecorViewWasVisible = this.mDecorViewVisible;
        this.mInShowWindow = true;
        boolean isPreRenderedAndInvisible = this.mIsPreRendered && !this.mWindowVisible;
        int previousImeWindowStatus = (this.mDecorViewVisible ? 1 : 0) | (isInputViewShown() ? isPreRenderedAndInvisible ? 4 : 2 : 0);
        startViews(prepareWindow(showInput));
        int nextImeWindowStatus = mapToImeWindowStatus();
        if (previousImeWindowStatus != nextImeWindowStatus) {
            setImeWindowStatus(nextImeWindowStatus, this.mBackDisposition);
        }
        onWindowShown();
        this.mIsPreRendered = this.mCanPreRender;
        if (this.mIsPreRendered) {
            onPreRenderedWindowVisibilityChanged(true);
        } else {
            this.mWindowVisible = true;
        }
        if ((previousImeWindowStatus & 1) == 0) {
            this.mWindow.show();
        }
        maybeNotifyPreRendered();
        this.mDecorViewWasVisible = true;
        this.mInShowWindow = false;
    }

    private void maybeNotifyPreRendered() {
        if (this.mCanPreRender && this.mIsPreRendered) {
            this.mPrivOps.reportPreRendered(getCurrentInputEditorInfo());
        }
    }

    private boolean prepareWindow(boolean showInput) {
        boolean doShowInput = false;
        this.mDecorViewVisible = true;
        if (!this.mShowInputRequested && this.mInputStarted && showInput) {
            doShowInput = true;
            this.mShowInputRequested = true;
        }
        initialize();
        updateFullscreenMode();
        updateInputViewShown();
        if (!this.mViewsCreated) {
            this.mViewsCreated = true;
            initialize();
            View v = onCreateCandidatesView();
            if (v != null) {
                setCandidatesView(v);
            }
        }
        return doShowInput;
    }

    private void startViews(boolean doShowInput) {
        if (this.mShowInputRequested) {
            if (!this.mInputViewStarted) {
                this.mInputViewStarted = true;
                onStartInputView(this.mInputEditorInfo, false);
            }
        } else if (!this.mCandidatesViewStarted) {
            this.mCandidatesViewStarted = true;
            onStartCandidatesView(this.mInputEditorInfo, false);
        }
        if (doShowInput) {
            startExtractingText(false);
        }
        LogPower.push(117);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPreRenderedWindowVisibilityChanged(boolean setVisible) {
        this.mWindowVisible = setVisible;
        this.mShowInputFlags = setVisible ? this.mShowInputFlags : 0;
        this.mShowInputRequested = setVisible;
        this.mDecorViewVisible = setVisible;
        if (setVisible) {
            onWindowShown();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyVisibilityInInsetsConsumer(boolean setVisible) {
        if (this.mIsPreRendered) {
            this.mPrivOps.applyImeVisibility(setVisible);
        }
    }

    private void finishViews(boolean finishingInput) {
        if (this.mInputViewStarted) {
            onFinishInputView(finishingInput);
        } else if (this.mCandidatesViewStarted) {
            onFinishCandidatesView(finishingInput);
        }
        this.mInputViewStarted = false;
        this.mCandidatesViewStarted = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doHideWindow() {
        setImeWindowStatus(0, this.mBackDisposition);
        hideWindow();
    }

    public void hideWindow() {
        if (SystemProperties.getBoolean(THP_SWITCH, false)) {
            try {
                HwInputManager.runHwTHPCommand(THP_COMMAND, WifiEnterpriseConfig.ENGINE_DISABLE);
            } catch (SecurityException e) {
                Log.w(TAG, "hideWindow runHwTHPCommand error occured: the package has no permission");
            }
        }
        HwInputMethodManager.recoveryIawarePriority();
        this.mIsPreRendered = false;
        this.mWindowVisible = false;
        finishViews(false);
        if (this.mDecorViewVisible) {
            this.mWindow.hide();
            this.mDecorViewVisible = false;
            onWindowHidden();
            this.mDecorViewWasVisible = false;
        }
        updateFullscreenMode();
    }

    public void onWindowShown() {
    }

    public void onWindowHidden() {
    }

    public void onBindInput() {
    }

    public void onUnbindInput() {
    }

    public void onStartInput(EditorInfo attribute, boolean restarting) {
    }

    /* access modifiers changed from: package-private */
    public void doFinishInput() {
        finishViews(true);
        if (this.mInputStarted) {
            onFinishInput();
            onFinishInputForMultiDisplay();
            onFinishInputForTv();
        }
        this.mInputStarted = false;
        this.mStartedInputConnection = null;
        this.mCurCompletions = null;
    }

    /* access modifiers changed from: package-private */
    public void doStartInput(InputConnection ic, EditorInfo attribute, boolean restarting) {
        if (!restarting) {
            doFinishInput();
        }
        this.mInputStarted = true;
        this.mStartedInputConnection = ic;
        this.mInputEditorInfo = attribute;
        initialize();
        onStartInput(attribute, restarting);
        onStartInputForMultiDisplay();
        boolean z = this.mDecorViewVisible;
        if (z) {
            if (this.mShowInputRequested) {
                this.mInputViewStarted = true;
                onStartInputView(this.mInputEditorInfo, restarting);
                startExtractingText(true);
            } else if (this.mCandidatesVisibility == 0) {
                this.mCandidatesViewStarted = true;
                onStartCandidatesView(this.mInputEditorInfo, restarting);
            }
        } else if (!this.mCanPreRender || this.mInputEditorInfo == null || this.mStartedInputConnection == null) {
            this.mIsPreRendered = false;
        } else if (this.mInShowWindow) {
            Log.w(TAG, "Re-entrance in to showWindow");
        } else {
            this.mDecorViewWasVisible = z;
            this.mInShowWindow = true;
            startViews(prepareWindow(true));
            this.mIsPreRendered = true;
            onPreRenderedWindowVisibilityChanged(false);
            this.mWindow.show();
            maybeNotifyPreRendered();
            this.mDecorViewWasVisible = true;
            this.mInShowWindow = false;
        }
    }

    private void onStartInputForMultiDisplay() {
        if (HwPCUtils.isInWindowsCastMode()) {
            HwInputMethodManager.onStartInput();
            startInputForMultiDisplay();
        }
    }

    private void onFinishInputForMultiDisplay() {
        if (HwPCUtils.isInWindowsCastMode()) {
            HwInputMethodManager.unregisterInputContentListener();
            HwInputMethodManager.onFinishInput();
            if (getCurrentInputConnection() != null) {
                boolean ret = getCurrentInputConnection().requestCursorUpdates(0);
                Log.i(TAG, "onFinishInput requestCursorUpdates, ret:" + ret);
                return;
            }
            Log.w(TAG, "onFinishInput, null input connection");
        }
    }

    private void onShowInputRequestedForMultiDisplay() {
        if (HwPCUtils.isInWindowsCastMode()) {
            HwInputMethodManager.onShowInputRequested();
            startInputForMultiDisplay();
        }
    }

    private void startInputForMultiDisplay() {
        HwInputMethodManager.registerInputContentListener(this.mInputContentListener);
        if (getCurrentInputConnection() == null || this.mImm.isCursorAnchorInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("onStartInput, null input connection:");
            sb.append(getCurrentInputConnection() == null);
            Log.w(TAG, sb.toString());
            return;
        }
        boolean ret = getCurrentInputConnection().requestCursorUpdates(3);
        Log.i(TAG, "onStartInput requestCursorUpdates, ret:" + ret);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedShowImeOnWindowsCastMode() {
        if (HwPCUtils.getHwPCManager() != null) {
            try {
                return !HwPCUtils.getHwPCManager().isFocusedOnWindowsCastDisplay();
            } catch (RemoteException e) {
                Log.e(TAG, "getFocusedDisplayId remote exception.");
            }
        }
        return true;
    }

    private void onContentChanged() {
        onContentChangedForTV();
    }

    private boolean isTvDevice() {
        return IS_TV;
    }

    private void onContentChangedForTV() {
        InputConnection ic;
        ExtractedText tmepExtractedText;
        String tempTextContent;
        if (isTvDevice() && (ic = getCurrentInputConnection()) != null && (tmepExtractedText = ic.getExtractedText(new ExtractedTextRequest(), 1)) != null && (tempTextContent = tmepExtractedText.text.toString()) != null && !tempTextContent.equals(this.mTextContentCache)) {
            this.mTextContentCache = tempTextContent;
            HwInputMethodManager.onContentChanged(this.mTextContentCache);
        }
    }

    private void onStartInputForTv() {
        if (isTvDevice()) {
            HwInputMethodManager.onStartInput();
        }
    }

    private void onFinishInputForTv() {
        if (isTvDevice()) {
            HwInputMethodManager.onFinishInput();
        }
    }

    public void onFinishInput() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.finishComposingText();
        }
    }

    public void onDisplayCompletions(CompletionInfo[] completions) {
    }

    public void onUpdateExtractedText(int token, ExtractedText text) {
        ExtractEditText extractEditText;
        if (this.mExtractedToken == token && text != null && (extractEditText = this.mExtractEditText) != null) {
            this.mExtractedText = text;
            extractEditText.setExtractedText(text);
        }
    }

    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        ExtractedText extractedText;
        ExtractEditText eet = this.mExtractEditText;
        if (eet != null && isFullscreenMode() && (extractedText = this.mExtractedText) != null) {
            int off = extractedText.startOffset;
            eet.startInternalChanges();
            int newSelStart2 = newSelStart - off;
            int newSelEnd2 = newSelEnd - off;
            int len = eet.getText().length();
            if (newSelStart2 < 0) {
                newSelStart2 = 0;
            } else if (newSelStart2 > len) {
                newSelStart2 = len;
            }
            if (newSelEnd2 < 0) {
                newSelEnd2 = 0;
            } else if (newSelEnd2 > len) {
                newSelEnd2 = len;
            }
            eet.setSelection(newSelStart2, newSelEnd2);
            eet.finishInternalChanges();
        }
    }

    @Deprecated
    public void onViewClicked(boolean focusChanged) {
    }

    @Deprecated
    public void onUpdateCursor(Rect newCursor) {
    }

    public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
    }

    public void requestHideSelf(int flags) {
        this.mPrivOps.hideMySoftInput(flags);
    }

    public final void requestShowSelf(int flags) {
        this.mPrivOps.showMySoftInput(flags);
    }

    private boolean handleBack(boolean doIt) {
        if (this.mShowInputRequested) {
            if (doIt) {
                requestHideSelf(0);
            }
            return true;
        } else if (!this.mDecorViewVisible) {
            return false;
        } else {
            if (this.mCandidatesVisibility == 0) {
                if (doIt) {
                    setCandidatesViewShown(false);
                }
            } else if (doIt) {
                doHideWindow();
            }
            return true;
        }
    }

    private ExtractEditText getExtractEditTextIfVisible() {
        if (!isExtractViewShown() || !isInputViewShown()) {
            return null;
        }
        return this.mExtractEditText;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() != 4) {
            return doMovementKey(keyCode, event, -1);
        }
        ExtractEditText eet = getExtractEditTextIfVisible();
        if (eet != null && eet.handleBackInTextActionModeIfNeeded(event)) {
            return true;
        }
        if (!handleBack(false)) {
            return false;
        }
        event.startTracking();
        return true;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return doMovementKey(keyCode, event, count);
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == 4) {
            ExtractEditText eet = getExtractEditTextIfVisible();
            if (eet != null && eet.handleBackInTextActionModeIfNeeded(event)) {
                return true;
            }
            if (event.isTracking() && !event.isCanceled()) {
                return handleBack(true);
            }
        }
        if (keyCode == 66 && event.isCtrlPressed() && getCurrentInputConnection() != null && HwPCUtils.isInWindowsCastMode()) {
            getCurrentInputConnection().performEditorAction(4);
        }
        return doMovementKey(keyCode, event, -2);
    }

    @Override // android.inputmethodservice.AbstractInputMethodService
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    @Override // android.inputmethodservice.AbstractInputMethodService
    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    public void onAppPrivateCommand(String action, Bundle data) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onToggleSoftInput(int showFlags, int hideFlags) {
        if (isInputViewShown()) {
            requestHideSelf(hideFlags);
        } else {
            requestShowSelf(showFlags);
        }
    }

    /* access modifiers changed from: package-private */
    public void reportExtractedMovement(int keyCode, int count) {
        int dx = 0;
        int dy = 0;
        switch (keyCode) {
            case 19:
                dy = -count;
                break;
            case 20:
                dy = count;
                break;
            case 21:
                dx = -count;
                break;
            case 22:
                dx = count;
                break;
        }
        onExtractedCursorMovement(dx, dy);
    }

    /* access modifiers changed from: package-private */
    public boolean doMovementKey(int keyCode, KeyEvent event, int count) {
        ExtractEditText eet = getExtractEditTextIfVisible();
        if (eet != null) {
            MovementMethod movement = eet.getMovementMethod();
            Layout layout = eet.getLayout();
            if (!(movement == null || layout == null)) {
                if (count == -1) {
                    if (movement.onKeyDown(eet, eet.getText(), keyCode, event)) {
                        reportExtractedMovement(keyCode, 1);
                        return true;
                    }
                } else if (count == -2) {
                    if (movement.onKeyUp(eet, eet.getText(), keyCode, event)) {
                        return true;
                    }
                } else if (movement.onKeyOther(eet, eet.getText(), event)) {
                    reportExtractedMovement(keyCode, count);
                } else {
                    KeyEvent down = KeyEvent.changeAction(event, 0);
                    if (movement.onKeyDown(eet, eet.getText(), keyCode, down)) {
                        KeyEvent up = KeyEvent.changeAction(event, 1);
                        movement.onKeyUp(eet, eet.getText(), keyCode, up);
                        while (true) {
                            count--;
                            if (count <= 0) {
                                break;
                            }
                            movement.onKeyDown(eet, eet.getText(), keyCode, down);
                            movement.onKeyUp(eet, eet.getText(), keyCode, up);
                        }
                        reportExtractedMovement(keyCode, count);
                    }
                }
            }
            switch (keyCode) {
                case 19:
                case 20:
                case 21:
                case 22:
                    return true;
            }
        }
        return false;
    }

    public void sendDownUpKeyEvents(int keyEventCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            long eventTime = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(eventTime, eventTime, 0, keyEventCode, 0, 0, -1, 0, 6));
            ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(), 1, keyEventCode, 0, 0, -1, 0, 6));
        }
    }

    public boolean sendDefaultEditorAction(boolean fromEnterKey) {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null) {
            return false;
        }
        if ((fromEnterKey && (ei.imeOptions & 1073741824) != 0) || (ei.imeOptions & 255) == 1) {
            return false;
        }
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.performEditorAction(ei.imeOptions & 255);
        }
        return true;
    }

    public void sendKeyChar(char charCode) {
        if (charCode != '\n') {
            if (charCode < '0' || charCode > '9') {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(String.valueOf(charCode), 1);
                    return;
                }
                return;
            }
            sendDownUpKeyEvents((charCode - '0') + 7);
        } else if (!sendDefaultEditorAction(true)) {
            sendDownUpKeyEvents(66);
        }
    }

    public void onExtractedSelectionChanged(int start, int end) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.setSelection(start, end);
        }
    }

    @UnsupportedAppUsage
    public void onExtractedDeleteText(int start, int end) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.finishComposingText();
            conn.setSelection(start, start);
            conn.deleteSurroundingText(0, end - start);
        }
    }

    @UnsupportedAppUsage
    public void onExtractedReplaceText(int start, int end, CharSequence text) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.setComposingRegion(start, end);
            conn.commitText(text, 1);
        }
    }

    @UnsupportedAppUsage
    public void onExtractedSetSpan(Object span, int start, int end, int flags) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null && conn.setSelection(start, end)) {
            CharSequence text = conn.getSelectedText(1);
            if (text instanceof Spannable) {
                ((Spannable) text).setSpan(span, 0, text.length(), flags);
                conn.setComposingRegion(start, end);
                conn.commitText(text, 1);
            }
        }
    }

    public void onExtractedTextClicked() {
        ExtractEditText extractEditText = this.mExtractEditText;
        if (extractEditText != null && extractEditText.hasVerticalScrollBar()) {
            setCandidatesViewShown(false);
        }
    }

    public void onExtractedCursorMovement(int dx, int dy) {
        ExtractEditText extractEditText = this.mExtractEditText;
        if (extractEditText != null && dy != 0 && extractEditText.hasVerticalScrollBar()) {
            setCandidatesViewShown(false);
        }
    }

    public boolean onExtractTextContextMenuItem(int id) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return true;
        }
        ic.performContextMenuAction(id);
        return true;
    }

    public CharSequence getTextForImeAction(int imeOptions) {
        switch (imeOptions & 255) {
            case 1:
                return null;
            case 2:
                return getText(com.android.internal.R.string.ime_action_go);
            case 3:
                return getText(com.android.internal.R.string.ime_action_search);
            case 4:
                return getText(com.android.internal.R.string.ime_action_send);
            case 5:
                return getText(com.android.internal.R.string.ime_action_next);
            case 6:
                return getText(com.android.internal.R.string.ime_action_done);
            case 7:
                return getText(com.android.internal.R.string.ime_action_previous);
            default:
                return getText(com.android.internal.R.string.ime_action_default);
        }
    }

    private int getIconForImeAction(int imeOptions) {
        switch (imeOptions & 255) {
            case 2:
                return com.android.internal.R.drawable.ic_input_extract_action_go;
            case 3:
                return com.android.internal.R.drawable.ic_input_extract_action_search;
            case 4:
                return com.android.internal.R.drawable.ic_input_extract_action_send;
            case 5:
                return com.android.internal.R.drawable.ic_input_extract_action_next;
            case 6:
                return com.android.internal.R.drawable.ic_input_extract_action_done;
            case 7:
                return com.android.internal.R.drawable.ic_input_extract_action_previous;
            default:
                return com.android.internal.R.drawable.ic_input_extract_action_return;
        }
    }

    public void onUpdateExtractingVisibility(EditorInfo ei) {
        if (ei.inputType == 0 || (ei.imeOptions & 268435456) != 0) {
            setExtractViewShown(false);
        } else {
            setExtractViewShown(true);
        }
    }

    public void onUpdateExtractingViews(EditorInfo ei) {
        if (isExtractViewShown() && this.mExtractAccessories != null) {
            boolean hasAction = true;
            if (ei.actionLabel == null && ((ei.imeOptions & 255) == 1 || (ei.imeOptions & 536870912) != 0 || ei.inputType == 0)) {
                hasAction = false;
            }
            if (hasAction) {
                this.mExtractAccessories.setVisibility(0);
                View view = this.mExtractAction;
                if (view != null) {
                    if (view instanceof ImageButton) {
                        ((ImageButton) view).setImageResource(getIconForImeAction(ei.imeOptions));
                        if (ei.actionLabel != null) {
                            this.mExtractAction.setContentDescription(ei.actionLabel);
                        } else {
                            this.mExtractAction.setContentDescription(getTextForImeAction(ei.imeOptions));
                        }
                    } else if (ei.actionLabel != null) {
                        ((TextView) this.mExtractAction).setText(ei.actionLabel);
                    } else {
                        ((TextView) this.mExtractAction).setText(getTextForImeAction(ei.imeOptions));
                    }
                    this.mExtractAction.setOnClickListener(this.mActionClickListener);
                    return;
                }
                return;
            }
            this.mExtractAccessories.setVisibility(8);
            View view2 = this.mExtractAction;
            if (view2 != null) {
                view2.setOnClickListener(null);
            }
        }
    }

    public void onExtractingInputChanged(EditorInfo ei) {
        if (ei.inputType == 0) {
            requestHideSelf(2);
        }
    }

    /* access modifiers changed from: package-private */
    public void startExtractingText(boolean inputChanged) {
        ExtractedText extractedText;
        ExtractEditText eet = this.mExtractEditText;
        if (eet != null && getCurrentInputStarted() && isFullscreenMode()) {
            this.mExtractedToken++;
            ExtractedTextRequest req = new ExtractedTextRequest();
            req.token = this.mExtractedToken;
            req.flags = 1;
            req.hintMaxLines = 10;
            req.hintMaxChars = 10000;
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) {
                extractedText = null;
            } else {
                extractedText = ic.getExtractedText(req, 1);
            }
            this.mExtractedText = extractedText;
            if (this.mExtractedText == null || ic == null) {
                Log.e(TAG, "Unexpected null in startExtractingText : mExtractedText = " + this.mExtractedText + ", input connection = " + ic);
            }
            EditorInfo ei = getCurrentInputEditorInfo();
            try {
                eet.startInternalChanges();
                onUpdateExtractingVisibility(ei);
                onUpdateExtractingViews(ei);
                int inputType = ei.inputType;
                if ((inputType & 15) == 1 && (262144 & inputType) != 0) {
                    inputType |= 131072;
                }
                eet.setInputType(inputType);
                eet.setHint(ei.hintText);
                if (this.mExtractedText != null) {
                    eet.setEnabled(true);
                    eet.setExtractedText(this.mExtractedText);
                } else {
                    eet.setEnabled(false);
                    eet.setText("");
                }
                if (inputChanged) {
                    onExtractingInputChanged(ei);
                }
            } finally {
                eet.finishInternalChanges();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
        synchronized (this.mLock) {
            this.mNotifyUserActionSent = false;
        }
        onCurrentInputMethodSubtypeChanged(newSubtype);
    }

    /* access modifiers changed from: protected */
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
    }

    @Deprecated
    public int getInputMethodWindowRecommendedHeight() {
        Log.w(TAG, "getInputMethodWindowRecommendedHeight() is deprecated and now always returns 0. Do not use this method.");
        return 0;
    }

    @Override // android.inputmethodservice.AbstractInputMethodService
    public final void exposeContent(InputContentInfo inputContentInfo, InputConnection inputConnection) {
        if (inputConnection != null && getCurrentInputConnection() == inputConnection) {
            exposeContentInternal(inputContentInfo, getCurrentInputEditorInfo());
        }
    }

    public boolean isLandScapeMultiWindowMode() {
        Configuration config = getResources().getConfiguration();
        int dockSide = -1;
        try {
            dockSide = this.mWindowManager.getDockedStackSide();
        } catch (Exception e) {
            Log.w(TAG, "Failed to get dock side: " + e.getMessage());
        }
        return config.orientation == 2 && dockSide != -1;
    }

    @Override // android.inputmethodservice.AbstractInputMethodService
    public final void notifyUserActionIfNecessary() {
        onContentChanged();
        synchronized (this.mLock) {
            if (!this.mNotifyUserActionSent) {
                this.mPrivOps.notifyUserAction();
                this.mNotifyUserActionSent = true;
            }
        }
    }

    private void exposeContentInternal(InputContentInfo inputContentInfo, EditorInfo editorInfo) {
        Uri contentUri = inputContentInfo.getContentUri();
        IInputContentUriToken uriToken = this.mPrivOps.createInputContentUriToken(contentUri, editorInfo.packageName);
        if (uriToken == null) {
            Log.e(TAG, "createInputContentAccessToken failed. contentUri=" + contentUri.toString() + " packageName=" + editorInfo.packageName);
            return;
        }
        inputContentInfo.setUriToken(uriToken);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int mapToImeWindowStatus() {
        int i = 2;
        if (!isInputViewShown()) {
            i = 0;
        } else if (this.mCanPreRender && !this.mWindowVisible) {
            i = 4;
        }
        return 1 | i;
    }

    /* access modifiers changed from: protected */
    @Override // android.inputmethodservice.AbstractInputMethodService, android.app.Service
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        Printer p = new PrintWriterPrinter(fout);
        p.println("Input method service state for " + this + SettingsStringUtil.DELIMITER);
        StringBuilder sb = new StringBuilder();
        sb.append("  mViewsCreated=");
        sb.append(this.mViewsCreated);
        p.println(sb.toString());
        p.println("  mDecorViewVisible=" + this.mDecorViewVisible + " mDecorViewWasVisible=" + this.mDecorViewWasVisible + " mWindowVisible=" + this.mWindowVisible + " mInShowWindow=" + this.mInShowWindow);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  Configuration=");
        sb2.append(getResources().getConfiguration());
        p.println(sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("  mToken=");
        sb3.append(this.mToken);
        p.println(sb3.toString());
        p.println("  mInputBinding=" + this.mInputBinding);
        p.println("  mInputConnection=" + this.mInputConnection);
        p.println("  mStartedInputConnection=" + this.mStartedInputConnection);
        p.println("  mInputStarted=" + this.mInputStarted + " mInputViewStarted=" + this.mInputViewStarted + " mCandidatesViewStarted=" + this.mCandidatesViewStarted);
        if (this.mInputEditorInfo != null) {
            p.println("  mInputEditorInfo:");
            this.mInputEditorInfo.dump(p, "    ");
        } else {
            p.println("  mInputEditorInfo: null");
        }
        p.println("  mShowInputRequested=" + this.mShowInputRequested + " mLastShowInputRequested=" + this.mLastShowInputRequested + " mCanPreRender=" + this.mCanPreRender + " mIsPreRendered=" + this.mIsPreRendered + " mShowInputFlags=0x" + Integer.toHexString(this.mShowInputFlags));
        StringBuilder sb4 = new StringBuilder();
        sb4.append("  mCandidatesVisibility=");
        sb4.append(this.mCandidatesVisibility);
        sb4.append(" mFullscreenApplied=");
        sb4.append(this.mFullscreenApplied);
        sb4.append(" mIsFullscreen=");
        sb4.append(this.mIsFullscreen);
        sb4.append(" mExtractViewHidden=");
        sb4.append(this.mExtractViewHidden);
        p.println(sb4.toString());
        if (this.mExtractedText != null) {
            p.println("  mExtractedText:");
            p.println("    text=" + this.mExtractedText.text.length() + " chars startOffset=" + this.mExtractedText.startOffset);
            p.println("    selectionStart=" + this.mExtractedText.selectionStart + " selectionEnd=" + this.mExtractedText.selectionEnd + " flags=0x" + Integer.toHexString(this.mExtractedText.flags));
        } else {
            p.println("  mExtractedText: null");
        }
        p.println("  mExtractedToken=" + this.mExtractedToken);
        p.println("  mIsInputViewShown=" + this.mIsInputViewShown + " mStatusIcon=" + this.mStatusIcon);
        p.println("Last computed insets:");
        p.println("  contentTopInsets=" + this.mTmpInsets.contentTopInsets + " visibleTopInsets=" + this.mTmpInsets.visibleTopInsets + " touchableInsets=" + this.mTmpInsets.touchableInsets + " touchableRegion=" + this.mTmpInsets.touchableRegion);
        StringBuilder sb5 = new StringBuilder();
        sb5.append(" mSettingsObserver=");
        sb5.append(this.mSettingsObserver);
        p.println(sb5.toString());
    }
}
