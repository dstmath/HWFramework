package android.inputmethodservice;

import android.R;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothAvrcp;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.Region;
import android.inputmethodservice.AbstractInputMethodService.AbstractInputMethodImpl;
import android.inputmethodservice.AbstractInputMethodService.AbstractInputMethodSessionImpl;
import android.net.ProxyInfo;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.DocumentsContract.Root;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.rms.AppAssociate;
import android.rms.HwSysResource;
import android.rms.iaware.Events;
import android.security.keymaster.KeymasterDefs;
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class InputMethodService extends AbstractInputMethodService {
    public static final int BACK_DISPOSITION_DEFAULT = 0;
    public static final int BACK_DISPOSITION_WILL_DISMISS = 2;
    public static final int BACK_DISPOSITION_WILL_NOT_DISMISS = 1;
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = false;
    public static final int IME_ACTIVE = 1;
    public static final int IME_VISIBLE = 2;
    static final int MOVEMENT_DOWN = -1;
    static final int MOVEMENT_UP = -2;
    static final String TAG = "InputMethodService";
    final OnClickListener mActionClickListener;
    int mBackDisposition;
    FrameLayout mCandidatesFrame;
    boolean mCandidatesViewStarted;
    int mCandidatesVisibility;
    CompletionInfo[] mCurCompletions;
    ViewGroup mExtractAccessories;
    View mExtractAction;
    ExtractEditText mExtractEditText;
    FrameLayout mExtractFrame;
    View mExtractView;
    boolean mExtractViewHidden;
    ExtractedText mExtractedText;
    int mExtractedToken;
    boolean mFullscreenApplied;
    ViewGroup mFullscreenArea;
    boolean mHardwareAccelerated;
    InputMethodManager mImm;
    boolean mInShowWindow;
    LayoutInflater mInflater;
    boolean mInitialized;
    InputBinding mInputBinding;
    InputConnection mInputConnection;
    EditorInfo mInputEditorInfo;
    FrameLayout mInputFrame;
    boolean mInputStarted;
    View mInputView;
    boolean mInputViewStarted;
    final OnComputeInternalInsetsListener mInsetsComputer;
    boolean mIsFullscreen;
    boolean mIsInputViewShown;
    boolean mLastShowInputRequested;
    View mRootView;
    private SettingsObserver mSettingsObserver;
    boolean mShouldClearInsetOfPreviousIme;
    int mShowInputFlags;
    boolean mShowInputRequested;
    InputConnection mStartedInputConnection;
    int mStatusIcon;
    int mTheme;
    TypedArray mThemeAttrs;
    final Insets mTmpInsets;
    final int[] mTmpLocation;
    IBinder mToken;
    SoftInputWindow mWindow;
    boolean mWindowAdded;
    boolean mWindowCreated;
    boolean mWindowVisible;
    boolean mWindowWasVisible;

    public class InputMethodImpl extends AbstractInputMethodImpl {
        public InputMethodImpl() {
            super();
        }

        public void attachToken(IBinder token) {
            if (InputMethodService.this.mToken == null) {
                InputMethodService.this.mToken = token;
                InputMethodService.this.mWindow.setToken(token);
            }
        }

        public void bindInput(InputBinding binding) {
            InputMethodService.this.mInputBinding = binding;
            InputMethodService.this.mInputConnection = binding.getConnection();
            InputConnection ic = InputMethodService.this.getCurrentInputConnection();
            if (ic != null) {
                ic.reportFullscreenMode(InputMethodService.this.mIsFullscreen);
            }
            InputMethodService.this.initialize();
            InputMethodService.this.onBindInput();
        }

        public void unbindInput() {
            InputMethodService.this.onUnbindInput();
            InputMethodService.this.mInputBinding = null;
            InputMethodService.this.mInputConnection = null;
        }

        public void startInput(InputConnection ic, EditorInfo attribute) {
            InputMethodService.this.doStartInput(ic, attribute, InputMethodService.DEBUG_FLOW);
        }

        public void restartInput(InputConnection ic, EditorInfo attribute) {
            InputMethodService.this.doStartInput(ic, attribute, true);
        }

        public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
            int i = InputMethodService.BACK_DISPOSITION_DEFAULT;
            boolean wasVis = InputMethodService.this.isInputViewShown();
            LogPower.push(BluetoothAssignedNumbers.CREATIVE_TECHNOLOGY);
            InputMethodService.this.mShowInputFlags = InputMethodService.BACK_DISPOSITION_DEFAULT;
            InputMethodService.this.mShowInputRequested = InputMethodService.DEBUG_FLOW;
            InputMethodService.this.doHideWindow();
            InputMethodService.this.clearInsetOfPreviousIme();
            if (resultReceiver != null) {
                if (wasVis != InputMethodService.this.isInputViewShown()) {
                    i = 3;
                } else if (!wasVis) {
                    i = InputMethodService.IME_ACTIVE;
                }
                resultReceiver.send(i, null);
            }
        }

        public void showSoftInput(int flags, ResultReceiver resultReceiver) {
            int i = InputMethodService.IME_VISIBLE;
            if (InputMethodService.DEBUG_FLOW) {
                Log.v(InputMethodService.TAG, "showSoftInput()");
            }
            boolean wasVis = InputMethodService.this.isInputViewShown();
            if (InputMethodService.this.dispatchOnShowInputRequested(flags, InputMethodService.DEBUG_FLOW)) {
                try {
                    InputMethodService.this.showWindow(true);
                } catch (BadTokenException e) {
                }
            }
            InputMethodService.this.clearInsetOfPreviousIme();
            InputMethodService.this.mImm.setImeWindowStatus(InputMethodService.this.mToken, (InputMethodService.this.isInputViewShown() ? InputMethodService.IME_VISIBLE : InputMethodService.BACK_DISPOSITION_DEFAULT) | InputMethodService.IME_ACTIVE, InputMethodService.this.mBackDisposition);
            if (resultReceiver != null) {
                if (wasVis == InputMethodService.this.isInputViewShown()) {
                    i = wasVis ? InputMethodService.BACK_DISPOSITION_DEFAULT : InputMethodService.IME_ACTIVE;
                }
                resultReceiver.send(i, null);
            }
        }

        public void changeInputMethodSubtype(InputMethodSubtype subtype) {
            InputMethodService.this.onCurrentInputMethodSubtypeChanged(subtype);
        }
    }

    public class InputMethodSessionImpl extends AbstractInputMethodSessionImpl {
        public InputMethodSessionImpl() {
            super();
        }

        public void finishInput() {
            if (isEnabled()) {
                InputMethodService.this.doFinishInput();
            }
        }

        public void displayCompletions(CompletionInfo[] completions) {
            if (isEnabled()) {
                InputMethodService.this.mCurCompletions = completions;
                InputMethodService.this.onDisplayCompletions(completions);
            }
        }

        public void updateExtractedText(int token, ExtractedText text) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateExtractedText(token, text);
            }
        }

        public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
            }
        }

        public void viewClicked(boolean focusChanged) {
            if (isEnabled()) {
                InputMethodService.this.onViewClicked(focusChanged);
            }
        }

        public void updateCursor(Rect newCursor) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateCursor(newCursor);
            }
        }

        public void appPrivateCommand(String action, Bundle data) {
            if (isEnabled()) {
                InputMethodService.this.onAppPrivateCommand(action, data);
            }
        }

        public void toggleSoftInput(int showFlags, int hideFlags) {
            InputMethodService.this.onToggleSoftInput(showFlags, hideFlags);
        }

        public void updateCursorAnchorInfo(CursorAnchorInfo info) {
            if (isEnabled()) {
                InputMethodService.this.onUpdateCursorAnchorInfo(info);
            }
        }
    }

    public static final class Insets {
        public static final int TOUCHABLE_INSETS_CONTENT = 1;
        public static final int TOUCHABLE_INSETS_FRAME = 0;
        public static final int TOUCHABLE_INSETS_REGION = 3;
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;
        public int contentTopInsets;
        public int touchableInsets;
        public final Region touchableRegion;
        public int visibleTopInsets;

        public Insets() {
            this.touchableRegion = new Region();
        }
    }

    private static final class SettingsObserver extends ContentObserver {
        private final InputMethodService mService;
        private int mShowImeWithHardKeyboard;

        private SettingsObserver(InputMethodService service) {
            super(new Handler(service.getMainLooper()));
            this.mShowImeWithHardKeyboard = InputMethodService.BACK_DISPOSITION_DEFAULT;
            this.mService = service;
        }

        public static SettingsObserver createAndRegister(InputMethodService service) {
            SettingsObserver observer = new SettingsObserver(service);
            service.getContentResolver().registerContentObserver(Secure.getUriFor(Secure.SHOW_IME_WITH_HARD_KEYBOARD), InputMethodService.DEBUG_FLOW, observer);
            return observer;
        }

        void unregister() {
            this.mService.getContentResolver().unregisterContentObserver(this);
        }

        private boolean shouldShowImeWithHardKeyboard() {
            if (this.mShowImeWithHardKeyboard == 0) {
                this.mShowImeWithHardKeyboard = Secure.getInt(this.mService.getContentResolver(), Secure.SHOW_IME_WITH_HARD_KEYBOARD, InputMethodService.BACK_DISPOSITION_DEFAULT) != 0 ? InputMethodService.IME_VISIBLE : InputMethodService.IME_ACTIVE;
            }
            switch (this.mShowImeWithHardKeyboard) {
                case InputMethodService.IME_ACTIVE /*1*/:
                    return InputMethodService.DEBUG_FLOW;
                case InputMethodService.IME_VISIBLE /*2*/:
                    return true;
                default:
                    Log.e(InputMethodService.TAG, "Unexpected mShowImeWithHardKeyboard=" + this.mShowImeWithHardKeyboard);
                    return InputMethodService.DEBUG_FLOW;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (Secure.getUriFor(Secure.SHOW_IME_WITH_HARD_KEYBOARD).equals(uri)) {
                this.mShowImeWithHardKeyboard = Secure.getInt(this.mService.getContentResolver(), Secure.SHOW_IME_WITH_HARD_KEYBOARD, InputMethodService.BACK_DISPOSITION_DEFAULT) != 0 ? InputMethodService.IME_VISIBLE : InputMethodService.IME_ACTIVE;
                this.mService.resetStateForNewConfiguration();
            }
        }

        public String toString() {
            return "SettingsObserver{mShowImeWithHardKeyboard=" + this.mShowImeWithHardKeyboard + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.inputmethodservice.InputMethodService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.inputmethodservice.InputMethodService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.inputmethodservice.InputMethodService.<clinit>():void");
    }

    public InputMethodService() {
        this.mTheme = BACK_DISPOSITION_DEFAULT;
        this.mHardwareAccelerated = DEBUG_FLOW;
        this.mTmpInsets = new Insets();
        this.mTmpLocation = new int[IME_VISIBLE];
        this.mInsetsComputer = new OnComputeInternalInsetsListener() {
            public void onComputeInternalInsets(InternalInsetsInfo info) {
                if (InputMethodService.this.isExtractViewShown()) {
                    View decor = InputMethodService.this.getWindow().getWindow().getDecorView();
                    Rect rect = info.contentInsets;
                    int height = decor.getHeight();
                    info.visibleInsets.top = height;
                    rect.top = height;
                    info.touchableRegion.setEmpty();
                    info.setTouchableInsets(InputMethodService.BACK_DISPOSITION_DEFAULT);
                    return;
                }
                InputMethodService.this.onComputeInsets(InputMethodService.this.mTmpInsets);
                info.contentInsets.top = InputMethodService.this.mTmpInsets.contentTopInsets;
                info.visibleInsets.top = InputMethodService.this.mTmpInsets.visibleTopInsets;
                info.touchableRegion.set(InputMethodService.this.mTmpInsets.touchableRegion);
                info.setTouchableInsets(InputMethodService.this.mTmpInsets.touchableInsets);
            }
        };
        this.mActionClickListener = new OnClickListener() {
            public void onClick(View v) {
                EditorInfo ei = InputMethodService.this.getCurrentInputEditorInfo();
                InputConnection ic = InputMethodService.this.getCurrentInputConnection();
                if (ei != null && ic != null) {
                    if (ei.actionId != 0) {
                        ic.performEditorAction(ei.actionId);
                    } else if ((ei.imeOptions & Process.PROC_TERM_MASK) != InputMethodService.IME_ACTIVE) {
                        ic.performEditorAction(ei.imeOptions & Process.PROC_TERM_MASK);
                    }
                }
            }
        };
    }

    public void setTheme(int theme) {
        if (this.mWindow != null) {
            throw new IllegalStateException("Must be called before onCreate()");
        }
        this.mTheme = theme;
    }

    public boolean enableHardwareAcceleration() {
        if (this.mWindow != null) {
            throw new IllegalStateException("Must be called before onCreate()");
        } else if (!ActivityManager.isHighEndGfx()) {
            return DEBUG_FLOW;
        } else {
            this.mHardwareAccelerated = true;
            return true;
        }
    }

    public void onCreate() {
        this.mTheme = Resources.selectSystemTheme(getResources().getIdentifier("androidhwext:style/Theme.Emui.InputMethod", null, null), getApplicationInfo().targetSdkVersion, R.style.Theme_InputMethod, R.style.Theme_Holo_InputMethod, R.style.Theme_DeviceDefault_InputMethod, R.style.Theme_DeviceDefault_InputMethod);
        super.setTheme(this.mTheme);
        super.onCreate();
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.mSettingsObserver = SettingsObserver.createAndRegister(this);
        this.mShouldClearInsetOfPreviousIme = this.mImm.getInputMethodWindowVisibleHeight() > 0 ? true : DEBUG_FLOW;
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mWindow = new SoftInputWindow(this, "InputMethod", this.mTheme, null, null, this.mDispatcherState, 2011, 80, DEBUG_FLOW);
        if (this.mHardwareAccelerated) {
            this.mWindow.getWindow().addFlags(StrictMode.PENALTY_DEATH_ON_NETWORK);
        }
        initViews();
        this.mWindow.getWindow().setLayout(MOVEMENT_DOWN, MOVEMENT_UP);
    }

    public void onInitializeInterface() {
    }

    void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            onInitializeInterface();
        }
    }

    void initViews() {
        this.mInitialized = DEBUG_FLOW;
        this.mWindowCreated = DEBUG_FLOW;
        this.mShowInputRequested = DEBUG_FLOW;
        this.mShowInputFlags = BACK_DISPOSITION_DEFAULT;
        this.mThemeAttrs = obtainStyledAttributes(R.styleable.InputMethodService);
        this.mRootView = this.mInflater.inflate(17367149, null);
        this.mRootView.setSystemUiVisibility(GLES20.GL_SRC_COLOR);
        this.mWindow.setContentView(this.mRootView);
        this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        this.mRootView.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsComputer);
        if (Global.getInt(getContentResolver(), Global.FANCY_IME_ANIMATIONS, BACK_DISPOSITION_DEFAULT) != 0) {
            this.mWindow.getWindow().setWindowAnimations(16974575);
        }
        this.mFullscreenArea = (ViewGroup) this.mRootView.findViewById(16909181);
        this.mExtractViewHidden = DEBUG_FLOW;
        this.mExtractFrame = (FrameLayout) this.mRootView.findViewById(R.id.extractArea);
        this.mExtractView = null;
        this.mExtractEditText = null;
        this.mExtractAccessories = null;
        this.mExtractAction = null;
        this.mFullscreenApplied = DEBUG_FLOW;
        this.mCandidatesFrame = (FrameLayout) this.mRootView.findViewById(R.id.candidatesArea);
        this.mInputFrame = (FrameLayout) this.mRootView.findViewById(R.id.inputArea);
        this.mInputView = null;
        this.mIsInputViewShown = DEBUG_FLOW;
        this.mExtractFrame.setVisibility(8);
        this.mCandidatesVisibility = getCandidatesHiddenVisibility();
        this.mCandidatesFrame.setVisibility(this.mCandidatesVisibility);
        this.mInputFrame.setVisibility(8);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mRootView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mInsetsComputer);
        doFinishInput();
        if (this.mWindowAdded) {
            try {
                this.mWindow.getWindow().setWindowAnimations(BACK_DISPOSITION_DEFAULT);
                this.mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "IME win has removed:", ex);
            }
        }
        if (this.mSettingsObserver != null) {
            this.mSettingsObserver.unregister();
            this.mSettingsObserver = null;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetStateForNewConfiguration();
    }

    private void resetStateForNewConfiguration() {
        int i = BACK_DISPOSITION_DEFAULT;
        boolean visible = this.mWindowVisible;
        int showFlags = this.mShowInputFlags;
        boolean showingInput = this.mShowInputRequested;
        CompletionInfo[] completions = this.mCurCompletions;
        initViews();
        this.mInputViewStarted = DEBUG_FLOW;
        this.mCandidatesViewStarted = DEBUG_FLOW;
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
                showWindow(DEBUG_FLOW);
            } else {
                doHideWindow();
            }
            boolean showing = onEvaluateInputViewShown();
            InputMethodManager inputMethodManager = this.mImm;
            IBinder iBinder = this.mToken;
            if (showing) {
                i = IME_VISIBLE;
            }
            inputMethodManager.setImeWindowStatus(iBinder, i | IME_ACTIVE, this.mBackDisposition);
        }
    }

    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethodImpl();
    }

    public AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface() {
        return new InputMethodSessionImpl();
    }

    public LayoutInflater getLayoutInflater() {
        return this.mInflater;
    }

    public Dialog getWindow() {
        return this.mWindow;
    }

    public void setBackDisposition(int disposition) {
        this.mBackDisposition = disposition;
    }

    public int getBackDisposition() {
        return this.mBackDisposition;
    }

    public int getMaxWidth() {
        return ((WindowManager) getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay().getWidth();
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

    public boolean getCurrentInputStarted() {
        return this.mInputStarted;
    }

    public EditorInfo getCurrentInputEditorInfo() {
        return this.mInputEditorInfo;
    }

    public void updateFullscreenMode() {
        boolean onEvaluateFullscreenMode = this.mShowInputRequested ? onEvaluateFullscreenMode() : DEBUG_FLOW;
        boolean changed = this.mLastShowInputRequested != this.mShowInputRequested ? true : DEBUG_FLOW;
        if (!(this.mIsFullscreen == onEvaluateFullscreenMode && this.mFullscreenApplied)) {
            changed = true;
            this.mIsFullscreen = onEvaluateFullscreenMode;
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.reportFullscreenMode(onEvaluateFullscreenMode);
            }
            this.mFullscreenApplied = true;
            initialize();
            LayoutParams lp = (LayoutParams) this.mFullscreenArea.getLayoutParams();
            if (onEvaluateFullscreenMode) {
                this.mFullscreenArea.setBackgroundDrawable(this.mThemeAttrs.getDrawable(BACK_DISPOSITION_DEFAULT));
                lp.height = BACK_DISPOSITION_DEFAULT;
                lp.weight = Engine.DEFAULT_VOLUME;
            } else {
                this.mFullscreenArea.setBackgroundDrawable(null);
                lp.height = MOVEMENT_UP;
                lp.weight = 0.0f;
            }
            ((ViewGroup) this.mFullscreenArea.getParent()).updateViewLayout(this.mFullscreenArea, lp);
            if (onEvaluateFullscreenMode) {
                if (this.mExtractView == null) {
                    View v = onCreateExtractTextView();
                    if (v != null) {
                        setExtractView(v);
                    }
                }
                startExtractingText(DEBUG_FLOW);
            }
            updateExtractFrameVisibility();
        }
        if (changed) {
            boolean z;
            Window window = this.mWindow.getWindow();
            if (this.mShowInputRequested) {
                z = DEBUG_FLOW;
            } else {
                z = true;
            }
            onConfigureWindow(window, onEvaluateFullscreenMode, z);
            this.mLastShowInputRequested = this.mShowInputRequested;
        }
    }

    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        int currentHeight = this.mWindow.getWindow().getAttributes().height;
        int newHeight = isFullscreen ? MOVEMENT_DOWN : MOVEMENT_UP;
        if (this.mIsInputViewShown && currentHeight != newHeight) {
            Log.w(TAG, "Window size has been changed. This may cause jankiness of resizing window: " + currentHeight + " -> " + newHeight);
        }
        this.mWindow.getWindow().setLayout(MOVEMENT_DOWN, newHeight);
    }

    public boolean isFullscreenMode() {
        return this.mIsFullscreen;
    }

    public boolean onEvaluateFullscreenMode() {
        if (getResources().getConfiguration().orientation != IME_VISIBLE) {
            return DEBUG_FLOW;
        }
        if (this.mInputEditorInfo == null || (this.mInputEditorInfo.imeOptions & StrictMode.PENALTY_DEATH_ON_CLEARTEXT_NETWORK) == 0) {
            return true;
        }
        return DEBUG_FLOW;
    }

    public void setExtractViewShown(boolean shown) {
        if (this.mExtractViewHidden == shown) {
            this.mExtractViewHidden = shown ? DEBUG_FLOW : true;
            updateExtractFrameVisibility();
        }
    }

    public boolean isExtractViewShown() {
        return (!this.mIsFullscreen || this.mExtractViewHidden) ? DEBUG_FLOW : true;
    }

    void updateExtractFrameVisibility() {
        int vis;
        boolean z;
        int i = IME_ACTIVE;
        if (isFullscreenMode()) {
            vis = this.mExtractViewHidden ? 4 : BACK_DISPOSITION_DEFAULT;
            this.mExtractFrame.setVisibility(vis);
        } else {
            vis = BACK_DISPOSITION_DEFAULT;
            this.mExtractFrame.setVisibility(8);
        }
        if (this.mCandidatesVisibility == 0) {
            z = true;
        } else {
            z = DEBUG_FLOW;
        }
        updateCandidatesVisibility(z);
        if (this.mWindowWasVisible && this.mFullscreenArea.getVisibility() != vis) {
            TypedArray typedArray = this.mThemeAttrs;
            if (vis != 0) {
                i = IME_VISIBLE;
            }
            int animRes = typedArray.getResourceId(i, BACK_DISPOSITION_DEFAULT);
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
            loc[IME_ACTIVE] = getWindow().getWindow().getDecorView().getHeight();
        }
        if (isFullscreenMode()) {
            outInsets.contentTopInsets = getWindow().getWindow().getDecorView().getHeight();
        } else {
            outInsets.contentTopInsets = loc[IME_ACTIVE];
        }
        if (this.mCandidatesFrame.getVisibility() == 0) {
            this.mCandidatesFrame.getLocationInWindow(loc);
        }
        outInsets.visibleTopInsets = loc[IME_ACTIVE];
        outInsets.touchableInsets = IME_VISIBLE;
        outInsets.touchableRegion.setEmpty();
    }

    public void updateInputViewShown() {
        boolean onEvaluateInputViewShown = this.mShowInputRequested ? onEvaluateInputViewShown() : DEBUG_FLOW;
        if (this.mIsInputViewShown != onEvaluateInputViewShown && this.mWindowVisible) {
            this.mIsInputViewShown = onEvaluateInputViewShown;
            this.mInputFrame.setVisibility(onEvaluateInputViewShown ? BACK_DISPOSITION_DEFAULT : 8);
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
        return this.mIsInputViewShown ? this.mWindowVisible : DEBUG_FLOW;
    }

    public boolean onEvaluateInputViewShown() {
        boolean z = true;
        if (this.mSettingsObserver == null) {
            Log.w(TAG, "onEvaluateInputViewShown: mSettingsObserver must not be null here.");
            return DEBUG_FLOW;
        } else if (this.mSettingsObserver.shouldShowImeWithHardKeyboard()) {
            return true;
        } else {
            Configuration config = getResources().getConfiguration();
            if (!(config.keyboard == IME_ACTIVE || config.hardKeyboardHidden == IME_VISIBLE)) {
                z = DEBUG_FLOW;
            }
            return z;
        }
    }

    public void setCandidatesViewShown(boolean shown) {
        updateCandidatesVisibility(shown);
        if (!this.mShowInputRequested && this.mWindowVisible != shown) {
            if (shown) {
                showWindow(DEBUG_FLOW);
            } else {
                doHideWindow();
            }
        }
    }

    void updateCandidatesVisibility(boolean shown) {
        int vis = shown ? BACK_DISPOSITION_DEFAULT : getCandidatesHiddenVisibility();
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
        this.mImm.showStatusIcon(this.mToken, getPackageName(), iconResId);
    }

    public void hideStatusIcon() {
        this.mStatusIcon = BACK_DISPOSITION_DEFAULT;
        this.mImm.hideStatusIcon(this.mToken);
    }

    public void switchInputMethod(String id) {
        this.mImm.setInputMethod(this.mToken, id);
    }

    public void setExtractView(View view) {
        this.mExtractFrame.removeAllViews();
        this.mExtractFrame.addView(view, new FrameLayout.LayoutParams(MOVEMENT_DOWN, MOVEMENT_DOWN));
        this.mExtractView = view;
        if (view != null) {
            this.mExtractEditText = (ExtractEditText) view.findViewById(R.id.inputExtractEditText);
            this.mExtractEditText.setIME(this);
            this.mExtractAction = view.findViewById(16909183);
            if (this.mExtractAction != null) {
                this.mExtractAccessories = (ViewGroup) view.findViewById(16909182);
            }
            startExtractingText(DEBUG_FLOW);
            return;
        }
        this.mExtractEditText = null;
        this.mExtractAccessories = null;
        this.mExtractAction = null;
    }

    public void setCandidatesView(View view) {
        this.mCandidatesFrame.removeAllViews();
        this.mCandidatesFrame.addView(view, new FrameLayout.LayoutParams(MOVEMENT_DOWN, MOVEMENT_UP));
    }

    public void setInputView(View view) {
        this.mInputFrame.removeAllViews();
        this.mInputFrame.addView(view, new FrameLayout.LayoutParams(MOVEMENT_DOWN, MOVEMENT_UP));
        this.mInputView = view;
    }

    public View onCreateExtractTextView() {
        return this.mInflater.inflate(17367150, null);
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
        if (!finishingInput) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
    }

    public void onFinishCandidatesView(boolean finishingInput) {
        if (!finishingInput) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    public boolean onShowInputRequested(int flags, boolean configChange) {
        if (!onEvaluateInputViewShown()) {
            return DEBUG_FLOW;
        }
        if ((flags & IME_ACTIVE) == 0) {
            if (configChange || !onEvaluateFullscreenMode()) {
                return (this.mSettingsObserver.shouldShowImeWithHardKeyboard() || getResources().getConfiguration().keyboard == IME_ACTIVE) ? true : DEBUG_FLOW;
            } else {
                return DEBUG_FLOW;
            }
        }
    }

    private boolean dispatchOnShowInputRequested(int flags, boolean configChange) {
        boolean result = onShowInputRequested(flags, configChange);
        if (result) {
            this.mShowInputFlags = flags;
        } else {
            this.mShowInputFlags = BACK_DISPOSITION_DEFAULT;
        }
        return result;
    }

    public void showWindow(boolean showInput) {
        if (DEBUG_FLOW) {
            Log.v(TAG, "Showing window: showInput=" + showInput + " mShowInputRequested=" + this.mShowInputRequested + " mWindowAdded=" + this.mWindowAdded + " mWindowCreated=" + this.mWindowCreated + " mWindowVisible=" + this.mWindowVisible + " mInputStarted=" + this.mInputStarted + " mShowInputFlags=" + this.mShowInputFlags);
        }
        if (this.mInShowWindow) {
            Log.w(TAG, "Re-entrance in to showWindow");
            return;
        }
        try {
            this.mWindowWasVisible = this.mWindowVisible;
            this.mInShowWindow = true;
            showWindowInner(showInput);
            this.mWindowWasVisible = true;
            this.mInShowWindow = DEBUG_FLOW;
        } catch (BadTokenException e) {
            this.mWindowVisible = DEBUG_FLOW;
            this.mWindowAdded = DEBUG_FLOW;
            throw e;
        } catch (Throwable th) {
            this.mWindowWasVisible = true;
            this.mInShowWindow = DEBUG_FLOW;
        }
    }

    void showWindowInner(boolean showInput) {
        int i;
        int i2;
        int i3 = IME_VISIBLE;
        boolean doShowInput = DEBUG_FLOW;
        if (this.mWindowVisible) {
            i = IME_ACTIVE;
        } else {
            i = BACK_DISPOSITION_DEFAULT;
        }
        if (isInputViewShown()) {
            i2 = IME_VISIBLE;
        } else {
            i2 = BACK_DISPOSITION_DEFAULT;
        }
        int previousImeWindowStatus = i | i2;
        this.mWindowVisible = true;
        if (!this.mShowInputRequested && this.mInputStarted && showInput) {
            doShowInput = true;
            this.mShowInputRequested = true;
        }
        initialize();
        updateFullscreenMode();
        updateInputViewShown();
        if (!(this.mWindowAdded && this.mWindowCreated)) {
            this.mWindowAdded = true;
            this.mWindowCreated = true;
            initialize();
            View v = onCreateCandidatesView();
            if (v != null) {
                setCandidatesView(v);
            }
        }
        if (this.mShowInputRequested) {
            if (!this.mInputViewStarted) {
                this.mInputViewStarted = true;
                onStartInputView(this.mInputEditorInfo, DEBUG_FLOW);
            }
        } else if (!this.mCandidatesViewStarted) {
            this.mCandidatesViewStarted = true;
            onStartCandidatesView(this.mInputEditorInfo, DEBUG_FLOW);
        }
        if (doShowInput) {
            startExtractingText(DEBUG_FLOW);
        }
        if (!isInputViewShown()) {
            i3 = BACK_DISPOSITION_DEFAULT;
        }
        int nextImeWindowStatus = i3 | IME_ACTIVE;
        if (previousImeWindowStatus != nextImeWindowStatus) {
            this.mImm.setImeWindowStatus(this.mToken, nextImeWindowStatus, this.mBackDisposition);
        }
        if ((previousImeWindowStatus & IME_ACTIVE) == 0) {
            if (DEBUG_FLOW) {
                Log.v(TAG, "showWindow: showing!");
            }
            LogPower.push(BluetoothAvrcp.PASSTHROUGH_ID_F5);
            onWindowShown();
            this.mWindow.show();
            this.mShouldClearInsetOfPreviousIme = DEBUG_FLOW;
        }
    }

    private void finishViews() {
        if (this.mInputViewStarted) {
            onFinishInputView(DEBUG_FLOW);
        } else if (this.mCandidatesViewStarted) {
            onFinishCandidatesView(DEBUG_FLOW);
        }
        this.mInputViewStarted = DEBUG_FLOW;
        this.mCandidatesViewStarted = DEBUG_FLOW;
    }

    private void doHideWindow() {
        this.mImm.setImeWindowStatus(this.mToken, BACK_DISPOSITION_DEFAULT, this.mBackDisposition);
        hideWindow();
    }

    public void hideWindow() {
        finishViews();
        if (this.mWindowVisible) {
            this.mWindow.hide();
            this.mWindowVisible = DEBUG_FLOW;
            onWindowHidden();
            this.mWindowWasVisible = DEBUG_FLOW;
        }
        updateFullscreenMode();
    }

    public void onWindowShown() {
    }

    public void onWindowHidden() {
    }

    private void clearInsetOfPreviousIme() {
        if (this.mShouldClearInsetOfPreviousIme) {
            this.mImm.clearLastInputMethodWindowForTransition(this.mToken);
            this.mShouldClearInsetOfPreviousIme = DEBUG_FLOW;
        }
    }

    public void onBindInput() {
    }

    public void onUnbindInput() {
    }

    public void onStartInput(EditorInfo attribute, boolean restarting) {
    }

    void doFinishInput() {
        if (this.mInputViewStarted) {
            onFinishInputView(true);
        } else if (this.mCandidatesViewStarted) {
            onFinishCandidatesView(true);
        }
        this.mInputViewStarted = DEBUG_FLOW;
        this.mCandidatesViewStarted = DEBUG_FLOW;
        if (this.mInputStarted) {
            onFinishInput();
        }
        this.mInputStarted = DEBUG_FLOW;
        this.mStartedInputConnection = null;
        this.mCurCompletions = null;
    }

    void doStartInput(InputConnection ic, EditorInfo attribute, boolean restarting) {
        if (!restarting) {
            doFinishInput();
        }
        this.mInputStarted = true;
        this.mStartedInputConnection = ic;
        this.mInputEditorInfo = attribute;
        initialize();
        onStartInput(attribute, restarting);
        if (!this.mWindowVisible) {
            return;
        }
        if (this.mShowInputRequested) {
            this.mInputViewStarted = true;
            onStartInputView(this.mInputEditorInfo, restarting);
            startExtractingText(true);
        } else if (this.mCandidatesVisibility == 0) {
            this.mCandidatesViewStarted = true;
            onStartCandidatesView(this.mInputEditorInfo, restarting);
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
        if (!(this.mExtractedToken != token || text == null || this.mExtractEditText == null)) {
            this.mExtractedText = text;
            this.mExtractEditText.setExtractedText(text);
        }
    }

    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        ExtractEditText eet = this.mExtractEditText;
        if (eet != null && isFullscreenMode() && this.mExtractedText != null) {
            int off = this.mExtractedText.startOffset;
            eet.startInternalChanges();
            newSelStart -= off;
            newSelEnd -= off;
            int len = eet.getText().length();
            if (newSelStart < 0) {
                newSelStart = BACK_DISPOSITION_DEFAULT;
            } else if (newSelStart > len) {
                newSelStart = len;
            }
            if (newSelEnd < 0) {
                newSelEnd = BACK_DISPOSITION_DEFAULT;
            } else if (newSelEnd > len) {
                newSelEnd = len;
            }
            eet.setSelection(newSelStart, newSelEnd);
            eet.finishInternalChanges();
        }
    }

    public void onViewClicked(boolean focusChanged) {
    }

    @Deprecated
    public void onUpdateCursor(Rect newCursor) {
    }

    public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
    }

    public void requestHideSelf(int flags) {
        this.mImm.hideSoftInputFromInputMethod(this.mToken, flags);
    }

    private void requestShowSelf(int flags) {
        this.mImm.showSoftInputFromInputMethod(this.mToken, flags);
    }

    private boolean handleBack(boolean doIt) {
        if (this.mShowInputRequested) {
            if (doIt) {
                requestHideSelf(BACK_DISPOSITION_DEFAULT);
            }
            return true;
        } else if (!this.mWindowVisible) {
            return DEBUG_FLOW;
        } else {
            if (this.mCandidatesVisibility == 0) {
                if (doIt) {
                    setCandidatesViewShown(DEBUG_FLOW);
                }
            } else if (doIt) {
                doHideWindow();
            }
            return true;
        }
    }

    private ExtractEditText getExtractEditTextIfVisible() {
        if (isExtractViewShown() && isInputViewShown()) {
            return this.mExtractEditText;
        }
        return null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() != 4) {
            return doMovementKey(keyCode, event, MOVEMENT_DOWN);
        }
        ExtractEditText eet = getExtractEditTextIfVisible();
        if (eet != null && eet.handleBackInTextActionModeIfNeeded(event)) {
            return true;
        }
        if (!handleBack(DEBUG_FLOW)) {
            return DEBUG_FLOW;
        }
        event.startTracking();
        return true;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return DEBUG_FLOW;
    }

    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return doMovementKey(keyCode, event, count);
    }

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
        return doMovementKey(keyCode, event, MOVEMENT_UP);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return DEBUG_FLOW;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return DEBUG_FLOW;
    }

    public void onAppPrivateCommand(String action, Bundle data) {
    }

    private void onToggleSoftInput(int showFlags, int hideFlags) {
        if (isInputViewShown()) {
            requestHideSelf(hideFlags);
        } else {
            requestShowSelf(showFlags);
        }
    }

    void reportExtractedMovement(int keyCode, int count) {
        int dx = BACK_DISPOSITION_DEFAULT;
        int dy = BACK_DISPOSITION_DEFAULT;
        switch (keyCode) {
            case HwSysResource.APP /*19*/:
                dy = -count;
                break;
            case HwSysResource.MEMORY /*20*/:
                dy = count;
                break;
            case HwSysResource.CPU /*21*/:
                dx = -count;
                break;
            case HwSysResource.IO /*22*/:
                dx = count;
                break;
        }
        onExtractedCursorMovement(dx, dy);
    }

    boolean doMovementKey(int keyCode, KeyEvent event, int count) {
        ExtractEditText eet = getExtractEditTextIfVisible();
        if (eet != null) {
            MovementMethod movement = eet.getMovementMethod();
            Layout layout = eet.getLayout();
            if (!(movement == null || layout == null)) {
                if (count == MOVEMENT_DOWN) {
                    if (movement.onKeyDown(eet, eet.getText(), keyCode, event)) {
                        reportExtractedMovement(keyCode, IME_ACTIVE);
                        return true;
                    }
                } else if (count == MOVEMENT_UP) {
                    if (movement.onKeyUp(eet, eet.getText(), keyCode, event)) {
                        return true;
                    }
                } else if (movement.onKeyOther(eet, eet.getText(), event)) {
                    reportExtractedMovement(keyCode, count);
                } else {
                    KeyEvent down = KeyEvent.changeAction(event, BACK_DISPOSITION_DEFAULT);
                    if (movement.onKeyDown(eet, eet.getText(), keyCode, down)) {
                        KeyEvent up = KeyEvent.changeAction(event, IME_ACTIVE);
                        movement.onKeyUp(eet, eet.getText(), keyCode, up);
                        while (true) {
                            count += MOVEMENT_DOWN;
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
                case HwSysResource.APP /*19*/:
                case HwSysResource.MEMORY /*20*/:
                case HwSysResource.CPU /*21*/:
                case HwSysResource.IO /*22*/:
                    return true;
            }
        }
        return DEBUG_FLOW;
    }

    public void sendDownUpKeyEvents(int keyEventCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            long eventTime = SystemClock.uptimeMillis();
            ic.sendKeyEvent(new KeyEvent(eventTime, eventTime, BACK_DISPOSITION_DEFAULT, keyEventCode, BACK_DISPOSITION_DEFAULT, BACK_DISPOSITION_DEFAULT, MOVEMENT_DOWN, BACK_DISPOSITION_DEFAULT, 6));
            ic.sendKeyEvent(new KeyEvent(eventTime, SystemClock.uptimeMillis(), IME_ACTIVE, keyEventCode, BACK_DISPOSITION_DEFAULT, BACK_DISPOSITION_DEFAULT, MOVEMENT_DOWN, BACK_DISPOSITION_DEFAULT, 6));
        }
    }

    public boolean sendDefaultEditorAction(boolean fromEnterKey) {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null || ((fromEnterKey && (ei.imeOptions & KeymasterDefs.KM_UINT_REP) != 0) || (ei.imeOptions & Process.PROC_TERM_MASK) == IME_ACTIVE)) {
            return DEBUG_FLOW;
        }
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.performEditorAction(ei.imeOptions & Process.PROC_TERM_MASK);
        }
        return true;
    }

    public void sendKeyChar(char charCode) {
        switch (charCode) {
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                if (!sendDefaultEditorAction(true)) {
                    sendDownUpKeyEvents(66);
                }
            default:
                if (charCode < '0' || charCode > '9') {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ic.commitText(String.valueOf(charCode), IME_ACTIVE);
                        return;
                    }
                    return;
                }
                sendDownUpKeyEvents((charCode - 48) + 7);
        }
    }

    public void onExtractedSelectionChanged(int start, int end) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.setSelection(start, end);
        }
    }

    public void onExtractedDeleteText(int start, int end) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.finishComposingText();
            conn.setSelection(start, start);
            conn.deleteSurroundingText(BACK_DISPOSITION_DEFAULT, end - start);
        }
    }

    public void onExtractedReplaceText(int start, int end, CharSequence text) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null) {
            conn.setComposingRegion(start, end);
            conn.commitText(text, IME_ACTIVE);
        }
    }

    public void onExtractedSetSpan(Object span, int start, int end, int flags) {
        InputConnection conn = getCurrentInputConnection();
        if (conn != null && conn.setSelection(start, end)) {
            CharSequence text = conn.getSelectedText(IME_ACTIVE);
            if (text instanceof Spannable) {
                ((Spannable) text).setSpan(span, BACK_DISPOSITION_DEFAULT, text.length(), flags);
                conn.setComposingRegion(start, end);
                conn.commitText(text, IME_ACTIVE);
            }
        }
    }

    public void onExtractedTextClicked() {
        if (this.mExtractEditText != null && this.mExtractEditText.hasVerticalScrollBar()) {
            setCandidatesViewShown(DEBUG_FLOW);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onExtractedCursorMovement(int dx, int dy) {
        if (!(this.mExtractEditText == null || dy == 0 || !this.mExtractEditText.hasVerticalScrollBar())) {
            setCandidatesViewShown(DEBUG_FLOW);
        }
    }

    public boolean onExtractTextContextMenuItem(int id) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.performContextMenuAction(id);
        }
        return true;
    }

    public CharSequence getTextForImeAction(int imeOptions) {
        switch (imeOptions & Process.PROC_TERM_MASK) {
            case IME_ACTIVE /*1*/:
                return null;
            case IME_VISIBLE /*2*/:
                return getText(17040453);
            case Engine.DEFAULT_STREAM /*3*/:
                return getText(17040454);
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return getText(17040455);
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return getText(17040456);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                return getText(17040457);
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                return getText(17040458);
            default:
                return getText(17040459);
        }
    }

    private int getIconForImeAction(int imeOptions) {
        switch (imeOptions & Process.PROC_TERM_MASK) {
            case IME_VISIBLE /*2*/:
                return 17303497;
            case Engine.DEFAULT_STREAM /*3*/:
                return 17303501;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return 17303502;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return 17303498;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                return 17303496;
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                return 17303499;
            default:
                return 17303500;
        }
    }

    public void onUpdateExtractingVisibility(EditorInfo ei) {
        if (ei.inputType == 0 || (ei.imeOptions & KeymasterDefs.KM_ENUM) != 0) {
            setExtractViewShown(DEBUG_FLOW);
        } else {
            setExtractViewShown(true);
        }
    }

    public void onUpdateExtractingViews(EditorInfo ei) {
        boolean z = true;
        if (isExtractViewShown() && this.mExtractAccessories != null) {
            boolean hasAction;
            if (ei.actionLabel != null) {
                hasAction = true;
            } else if ((ei.imeOptions & Process.PROC_TERM_MASK) == IME_ACTIVE || (ei.imeOptions & KeymasterDefs.KM_ENUM_REP) != 0) {
                hasAction = DEBUG_FLOW;
            } else {
                if (ei.inputType == 0) {
                    z = BACK_DISPOSITION_DEFAULT;
                }
                hasAction = z;
            }
            if (hasAction) {
                this.mExtractAccessories.setVisibility(BACK_DISPOSITION_DEFAULT);
                if (this.mExtractAction != null) {
                    if (this.mExtractAction instanceof ImageButton) {
                        ((ImageButton) this.mExtractAction).setImageResource(getIconForImeAction(ei.imeOptions));
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
                }
            } else {
                this.mExtractAccessories.setVisibility(8);
                if (this.mExtractAction != null) {
                    this.mExtractAction.setOnClickListener(null);
                }
            }
        }
    }

    public void onExtractingInputChanged(EditorInfo ei) {
        if (ei.inputType == 0) {
            requestHideSelf(IME_VISIBLE);
        }
    }

    void startExtractingText(boolean inputChanged) {
        ExtractedText extractedText = null;
        ExtractEditText eet = this.mExtractEditText;
        if (eet != null && getCurrentInputStarted() && isFullscreenMode()) {
            this.mExtractedToken += IME_ACTIVE;
            ExtractedTextRequest req = new ExtractedTextRequest();
            req.token = this.mExtractedToken;
            req.flags = IME_ACTIVE;
            req.hintMaxLines = 10;
            req.hintMaxChars = Events.EVENT_FLAG_START;
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                extractedText = ic.getExtractedText(req, IME_ACTIVE);
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
                if ((inputType & 15) == IME_ACTIVE && (Root.FLAG_HAS_SETTINGS & inputType) != 0) {
                    inputType |= Root.FLAG_ADVANCED;
                }
                eet.setInputType(inputType);
                eet.setHint(ei.hintText);
                if (this.mExtractedText != null) {
                    eet.setEnabled(true);
                    eet.setExtractedText(this.mExtractedText);
                } else {
                    eet.setEnabled(DEBUG_FLOW);
                    eet.setText(ProxyInfo.LOCAL_EXCL_LIST);
                }
                eet.finishInternalChanges();
                if (inputChanged) {
                    onExtractingInputChanged(ei);
                }
            } catch (Throwable th) {
                eet.finishInternalChanges();
            }
        }
    }

    protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
    }

    public int getInputMethodWindowRecommendedHeight() {
        return this.mImm.getInputMethodWindowVisibleHeight();
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        Printer p = new PrintWriterPrinter(fout);
        p.println("Input method service state for " + this + ":");
        p.println("  mWindowCreated=" + this.mWindowCreated + " mWindowAdded=" + this.mWindowAdded);
        p.println("  mWindowVisible=" + this.mWindowVisible + " mWindowWasVisible=" + this.mWindowWasVisible + " mInShowWindow=" + this.mInShowWindow);
        p.println("  Configuration=" + getResources().getConfiguration());
        p.println("  mToken=" + this.mToken);
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
        p.println("  mShowInputRequested=" + this.mShowInputRequested + " mLastShowInputRequested=" + this.mLastShowInputRequested + " mShowInputFlags=0x" + Integer.toHexString(this.mShowInputFlags));
        p.println("  mCandidatesVisibility=" + this.mCandidatesVisibility + " mFullscreenApplied=" + this.mFullscreenApplied + " mIsFullscreen=" + this.mIsFullscreen + " mExtractViewHidden=" + this.mExtractViewHidden);
        if (this.mExtractedText != null) {
            p.println("  mExtractedText:");
            p.println("    text=" + this.mExtractedText.text.length() + " chars" + " startOffset=" + this.mExtractedText.startOffset);
            p.println("    selectionStart=" + this.mExtractedText.selectionStart + " selectionEnd=" + this.mExtractedText.selectionEnd + " flags=0x" + Integer.toHexString(this.mExtractedText.flags));
        } else {
            p.println("  mExtractedText: null");
        }
        p.println("  mExtractedToken=" + this.mExtractedToken);
        p.println("  mIsInputViewShown=" + this.mIsInputViewShown + " mStatusIcon=" + this.mStatusIcon);
        p.println("Last computed insets:");
        p.println("  contentTopInsets=" + this.mTmpInsets.contentTopInsets + " visibleTopInsets=" + this.mTmpInsets.visibleTopInsets + " touchableInsets=" + this.mTmpInsets.touchableInsets + " touchableRegion=" + this.mTmpInsets.touchableRegion);
        p.println(" mShouldClearInsetOfPreviousIme=" + this.mShouldClearInsetOfPreviousIme);
        p.println(" mSettingsObserver=" + this.mSettingsObserver);
    }
}
