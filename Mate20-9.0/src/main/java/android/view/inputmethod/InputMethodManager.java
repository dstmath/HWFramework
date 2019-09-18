package android.view.inputmethod;

import android.common.HwFrameworkFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.util.Pools;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.autofill.AutofillManager;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputConnectionWrapper;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class InputMethodManager {
    private static final String[] BLACK_TABLE = {"com.baidu.input_huawei/.ImeService", "com.touchtype.swiftkey/com.touchtype.KeyboardService"};
    public static final int CONTROL_SHOW_INPUT = 65536;
    public static final int CONTROL_START_INITIAL = 256;
    public static final int CONTROL_WINDOW_FIRST = 4;
    public static final int CONTROL_WINDOW_IS_TEXT_EDITOR = 2;
    public static final int CONTROL_WINDOW_VIEW_HAS_FOCUS = 1;
    static final boolean DEBUG = false;
    public static final int DISPATCH_HANDLED = 1;
    public static final int DISPATCH_IN_PROGRESS = -1;
    public static final int DISPATCH_NOT_HANDLED = 0;
    public static final int HIDE_IMPLICIT_ONLY = 1;
    public static final int HIDE_NOT_ALWAYS = 2;
    static final long INPUT_METHOD_NOT_RESPONDING_TIMEOUT = 2500;
    private static final String IS_TABLET = SystemProperties.get("ro.build.characteristics", "");
    static final int MSG_BIND = 2;
    static final int MSG_DUMP = 1;
    static final int MSG_FLUSH_INPUT_EVENT = 7;
    static final int MSG_REPORT_FULLSCREEN_MODE = 10;
    static final int MSG_SEND_INPUT_EVENT = 5;
    static final int MSG_SET_ACTIVE = 4;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 9;
    static final int MSG_TIMEOUT_INPUT_EVENT = 6;
    static final int MSG_UNBIND = 3;
    private static final int NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER = -1;
    static final String PENDING_EVENT_COUNTER = "aq:imm";
    private static final int REQUEST_UPDATE_CURSOR_ANCHOR_INFO_NONE = 0;
    public static final int RESULT_HIDDEN = 3;
    public static final int RESULT_SHOWN = 2;
    public static final int RESULT_UNCHANGED_HIDDEN = 1;
    public static final int RESULT_UNCHANGED_SHOWN = 0;
    public static final int SHOW_FORCED = 2;
    public static final int SHOW_IMPLICIT = 1;
    public static final int SHOW_IM_PICKER_MODE_AUTO = 0;
    public static final int SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES = 2;
    public static final int SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES = 1;
    static final String TAG = "InputMethodManager";
    static InputMethodManager sInstance;
    boolean mActive;
    int mBindSequence;
    final IInputMethodClient.Stub mClient;
    CompletionInfo[] mCompletions;
    InputChannel mCurChannel;
    String mCurId;
    IInputMethodSession mCurMethod;
    View mCurRootView;
    ImeInputEventSender mCurSender;
    EditorInfo mCurrentTextBoxAttribute;
    private CursorAnchorInfo mCursorAnchorInfo;
    int mCursorCandEnd;
    int mCursorCandStart;
    Rect mCursorRect;
    int mCursorSelEnd;
    int mCursorSelStart;
    final InputConnection mDummyInputConnection;
    boolean mFullscreenMode;
    final H mH;
    final IInputContext mIInputContext;
    boolean mInTransition;
    private int mLastSentUserActionNotificationSequenceNumber;
    final Looper mMainLooper;
    View mNextServedView;
    /* access modifiers changed from: private */
    public int mNextUserActionNotificationSequenceNumber;
    final Pools.Pool<PendingEvent> mPendingEventPool;
    final SparseArray<PendingEvent> mPendingEvents;
    /* access modifiers changed from: private */
    public int mRequestUpdateCursorAnchorInfoMonitorMode;
    boolean mRestartOnNextWindowFocus;
    IHwSecImmHelper mSecImmHelper;
    boolean mServedConnecting;
    ControlledInputConnectionWrapper mServedInputConnectionWrapper;
    View mServedView;
    final IInputMethodManager mService;
    Rect mTmpCursorRect;

    private static class ControlledInputConnectionWrapper extends IInputConnectionWrapper {
        private final InputMethodManager mParentInputMethodManager;

        public ControlledInputConnectionWrapper(Looper mainLooper, InputConnection conn, InputMethodManager inputMethodManager) {
            super(mainLooper, conn);
            this.mParentInputMethodManager = inputMethodManager;
        }

        public boolean isActive() {
            return this.mParentInputMethodManager.mActive && !isFinished();
        }

        /* access modifiers changed from: package-private */
        public void deactivate() {
            if (!isFinished()) {
                closeConnection();
            }
        }

        /* access modifiers changed from: protected */
        public void onUserAction() {
            this.mParentInputMethodManager.notifyUserAction();
        }

        public String toString() {
            return "ControlledInputConnectionWrapper{connection=" + getInputConnection() + " finished=" + isFinished() + " mParentInputMethodManager.mActive=" + this.mParentInputMethodManager.mActive + "}";
        }
    }

    public interface FinishedInputEventCallback {
        void onFinishedInputEvent(Object obj, boolean z);
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper, null, true);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:116:0x0187, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:89:0x00ff, code lost:
            if (r1 == false) goto L_0x010b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x0101, code lost:
            r11.this$0.startInputInner(6, null, 0, 0, 0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:91:0x010b, code lost:
            return;
         */
        public void handleMessage(Message msg) {
            int reason;
            boolean z = true;
            boolean fullscreen = false;
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        InputMethodManager.this.doDump((FileDescriptor) args.arg1, (PrintWriter) args.arg2, (String[]) args.arg3);
                    } catch (RuntimeException e) {
                        ((PrintWriter) args.arg2).println("Exception: " + e);
                    }
                    synchronized (args.arg4) {
                        ((CountDownLatch) args.arg4).countDown();
                    }
                    args.recycle();
                    return;
                case 2:
                    InputBindResult res = (InputBindResult) msg.obj;
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence >= 0) {
                            if (InputMethodManager.this.mBindSequence == res.sequence) {
                                int unused = InputMethodManager.this.mRequestUpdateCursorAnchorInfoMonitorMode = 0;
                                InputMethodManager.this.setInputChannelLocked(res.channel);
                                InputMethodManager.this.mCurMethod = res.method;
                                InputMethodManager.this.mCurId = res.id;
                                InputMethodManager.this.mBindSequence = res.sequence;
                                InputMethodManager.this.startInputInner(5, null, 0, 0, 0);
                                return;
                            }
                        }
                        Log.w(InputMethodManager.TAG, "Ignoring onBind: cur seq=" + InputMethodManager.this.mBindSequence + ", given seq=" + res.sequence);
                        if (!(res.channel == null || res.channel == InputMethodManager.this.mCurChannel)) {
                            res.channel.dispose();
                            break;
                        }
                    }
                case 3:
                    int sequence = msg.arg1;
                    int i = msg.arg2;
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence == sequence) {
                            InputMethodManager.this.clearBindingLocked();
                            if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.isFocused()) {
                                InputMethodManager.this.mServedConnecting = true;
                            }
                            boolean startInput = InputMethodManager.this.mActive;
                            break;
                        } else {
                            return;
                        }
                    }
                case 4:
                    boolean active = msg.arg1 != 0;
                    if (msg.arg2 != 0) {
                        fullscreen = true;
                    }
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mActive = active;
                        InputMethodManager.this.mFullscreenMode = fullscreen;
                        if (!active) {
                            InputMethodManager.this.mRestartOnNextWindowFocus = true;
                            try {
                                InputMethodManager.this.mIInputContext.finishComposingText();
                            } catch (RemoteException e2) {
                            }
                        }
                        if (InputMethodManager.this.mServedView != null && InputMethodManager.canStartInput(InputMethodManager.this.mServedView) && InputMethodManager.this.checkFocusNoStartInput(InputMethodManager.this.mRestartOnNextWindowFocus)) {
                            if (active) {
                                reason = 7;
                            } else {
                                reason = 8;
                            }
                            InputMethodManager.this.startInputInner(reason, null, 0, 0, 0);
                        }
                    }
                    return;
                case 5:
                    InputMethodManager.this.sendInputEventAndReportResultOnMainLooper((PendingEvent) msg.obj);
                    return;
                case 6:
                    int seq = msg.arg1;
                    if (msg.obj instanceof PendingEvent) {
                        PendingEvent p = (PendingEvent) msg.obj;
                        seq = p.mEvent == null ? msg.arg1 : p.mEvent.getSequenceNumber();
                    }
                    InputMethodManager.this.finishedInputEvent(seq, false, true);
                    return;
                case 7:
                    InputMethodManager.this.finishedInputEvent(msg.arg1, false, false);
                    return;
                case 9:
                    synchronized (InputMethodManager.this.mH) {
                        int unused2 = InputMethodManager.this.mNextUserActionNotificationSequenceNumber = msg.arg1;
                    }
                    return;
                case 10:
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    boolean fullscreen2 = z;
                    InputConnection ic = null;
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mFullscreenMode = fullscreen2;
                        if (InputMethodManager.this.mServedInputConnectionWrapper != null) {
                            ic = InputMethodManager.this.mServedInputConnectionWrapper.getInputConnection();
                        }
                    }
                    if (ic != null) {
                        ic.reportFullscreenMode(fullscreen2);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private final class ImeInputEventSender extends InputEventSender {
        public ImeInputEventSender(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEventFinished(int seq, boolean handled) {
            InputMethodManager.this.finishedInputEvent(seq, handled, false);
        }
    }

    private final class PendingEvent implements Runnable {
        public FinishedInputEventCallback mCallback;
        public InputEvent mEvent;
        public boolean mHandled;
        public Handler mHandler;
        public String mInputMethodId;
        public Object mToken;

        private PendingEvent() {
        }

        public void recycle() {
            this.mEvent = null;
            this.mToken = null;
            this.mInputMethodId = null;
            this.mCallback = null;
            this.mHandler = null;
            this.mHandled = false;
        }

        public void run() {
            try {
                this.mCallback.onFinishedInputEvent(this.mToken, this.mHandled);
            } catch (IllegalArgumentException e) {
                Log.e(InputMethodManager.TAG, "Handle input Event in wrong state. Such as : parameter must be a descendant of this view. Ignore it!");
            }
            synchronized (InputMethodManager.this.mH) {
                InputMethodManager.this.recyclePendingEventLocked(this);
            }
        }
    }

    private static boolean isAutofillUIShowing(View servedView) {
        AutofillManager afm = (AutofillManager) servedView.getContext().getSystemService(AutofillManager.class);
        return afm != null && afm.isAutofillUiShowing();
    }

    /* access modifiers changed from: private */
    public static boolean canStartInput(View servedView) {
        return servedView.hasWindowFocus() || isAutofillUIShowing(servedView);
    }

    InputMethodManager(Looper looper) throws ServiceManager.ServiceNotFoundException {
        this(IInputMethodManager.Stub.asInterface(ServiceManager.getServiceOrThrow("input_method")), looper);
    }

    InputMethodManager(IInputMethodManager service, Looper looper) {
        this.mActive = false;
        this.mRestartOnNextWindowFocus = true;
        this.mTmpCursorRect = new Rect();
        this.mCursorRect = new Rect();
        this.mNextUserActionNotificationSequenceNumber = -1;
        this.mLastSentUserActionNotificationSequenceNumber = -1;
        this.mCursorAnchorInfo = null;
        this.mBindSequence = -1;
        this.mRequestUpdateCursorAnchorInfoMonitorMode = 0;
        this.mPendingEventPool = new Pools.SimplePool(20);
        this.mPendingEvents = new SparseArray<>(20);
        this.mClient = new IInputMethodClient.Stub() {
            /* access modifiers changed from: protected */
            public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
                CountDownLatch latch = new CountDownLatch(1);
                SomeArgs sargs = SomeArgs.obtain();
                sargs.arg1 = fd;
                sargs.arg2 = fout;
                sargs.arg3 = args;
                sargs.arg4 = latch;
                InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(1, sargs));
                try {
                    if (!latch.await(5, TimeUnit.SECONDS)) {
                        fout.println("Timeout waiting for dump");
                    }
                } catch (InterruptedException e) {
                    fout.println("Interrupted waiting for dump");
                }
            }

            public void setUsingInputMethod(boolean state) {
            }

            public void onBindMethod(InputBindResult res) {
                InputMethodManager.this.mH.obtainMessage(2, res).sendToTarget();
            }

            public void onUnbindMethod(int sequence, int unbindReason) {
                InputMethodManager.this.mH.obtainMessage(3, sequence, unbindReason).sendToTarget();
            }

            public void setActive(boolean active, boolean fullscreen) {
                InputMethodManager.this.mH.obtainMessage(4, active, fullscreen).sendToTarget();
            }

            public void setUserActionNotificationSequenceNumber(int sequenceNumber) {
                InputMethodManager.this.mH.obtainMessage(9, sequenceNumber, 0).sendToTarget();
            }

            public void reportFullscreenMode(boolean fullscreen) {
                InputMethodManager.this.mH.obtainMessage(10, fullscreen, 0).sendToTarget();
            }
        };
        this.mDummyInputConnection = new BaseInputConnection(this, false);
        this.mInTransition = false;
        this.mService = service;
        this.mMainLooper = looper;
        this.mH = new H(looper);
        this.mIInputContext = new ControlledInputConnectionWrapper(looper, this.mDummyInputConnection, this);
        this.mSecImmHelper = HwFrameworkFactory.getSecImmHelper(service);
    }

    public static InputMethodManager getInstance() {
        InputMethodManager inputMethodManager;
        synchronized (InputMethodManager.class) {
            if (sInstance == null) {
                try {
                    sInstance = new InputMethodManager(Looper.getMainLooper());
                } catch (ServiceManager.ServiceNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
            inputMethodManager = sInstance;
        }
        return inputMethodManager;
    }

    public static InputMethodManager peekInstance() {
        return sInstance;
    }

    public IInputMethodClient getClient() {
        return this.mClient;
    }

    public IInputContext getInputContext() {
        return this.mIInputContext;
    }

    public List<InputMethodInfo> getInputMethodList() {
        try {
            return this.mService.getInputMethodList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodInfo> getVrInputMethodList() {
        try {
            return this.mService.getVrInputMethodList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        try {
            return this.mService.getEnabledInputMethodList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
        try {
            return this.mService.getEnabledInputMethodSubtypeList(imi == null ? null : imi.getId(), allowsImplicitlySelectedSubtypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void showStatusIcon(IBinder imeToken, String packageName, int iconId) {
        showStatusIconInternal(imeToken, packageName, iconId);
    }

    public void showStatusIconInternal(IBinder imeToken, String packageName, int iconId) {
        try {
            this.mService.updateStatusIcon(imeToken, packageName, iconId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void hideStatusIcon(IBinder imeToken) {
        hideStatusIconInternal(imeToken);
    }

    public void hideStatusIconInternal(IBinder imeToken) {
        try {
            this.mService.updateStatusIcon(imeToken, null, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setImeWindowStatus(IBinder imeToken, IBinder startInputToken, int vis, int backDisposition) {
        try {
            this.mService.setImeWindowStatus(imeToken, startInputToken, vis, backDisposition);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        try {
            this.mService.registerSuggestionSpansForNotification(spans);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        try {
            this.mService.notifySuggestionPicked(span, originalString, index);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isFullscreenMode() {
        boolean z;
        synchronized (this.mH) {
            z = this.mFullscreenMode;
        }
        return z;
    }

    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        try {
            this.mService.reportFullscreenMode(token, fullscreen);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isActive(View view) {
        boolean z;
        checkFocus();
        synchronized (this.mH) {
            z = (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null;
        }
        return z;
    }

    public boolean isActive() {
        boolean z;
        checkFocus();
        synchronized (this.mH) {
            z = (this.mServedView == null || this.mCurrentTextBoxAttribute == null) ? false : true;
        }
        return z;
    }

    public boolean isAcceptingText() {
        checkFocus();
        return (this.mServedInputConnectionWrapper == null || this.mServedInputConnectionWrapper.getInputConnection() == null) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void clearBindingLocked() {
        clearConnectionLocked();
        setInputChannelLocked(null);
        this.mBindSequence = -1;
        this.mCurId = null;
        this.mCurMethod = null;
    }

    /* access modifiers changed from: package-private */
    public void setInputChannelLocked(InputChannel channel) {
        if (this.mCurChannel != channel) {
            if (this.mCurSender != null) {
                flushPendingEventsLocked();
                this.mCurSender.dispose();
                this.mCurSender = null;
            }
            if (this.mCurChannel != null) {
                this.mCurChannel.dispose();
            }
            this.mCurChannel = channel;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearConnectionLocked() {
        this.mCurrentTextBoxAttribute = null;
        if (this.mServedInputConnectionWrapper != null) {
            this.mServedInputConnectionWrapper.deactivate();
            this.mServedInputConnectionWrapper = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void finishInputLocked() {
        this.mNextServedView = null;
        if (this.mServedView != null) {
            if (this.mCurrentTextBoxAttribute != null) {
                try {
                    this.mService.finishInput(this.mClient);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            this.mServedView = null;
            this.mCompletions = null;
            this.mServedConnecting = false;
            clearConnectionLocked();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        return;
     */
    public void displayCompletions(View view, CompletionInfo[] completions) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                this.mCompletions = completions;
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.displayCompletions(this.mCompletions);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        return;
     */
    public void updateExtractedText(View view, int token, ExtractedText text) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.updateExtractedText(token, text);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public boolean showSoftInput(View view, int flags) {
        return showSoftInput(view, flags, null);
    }

    public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (this.mH) {
            Log.i(TAG, "showSoftInput");
            if (!(this.mServedView == null || view == null)) {
                Log.i(TAG, "mServedView =" + this.mServedView.getContext().getPackageName() + ";view =" + view.getContext().getPackageName() + ";flags =" + flags);
            }
            if (this.mServedView != view && (this.mServedView == null || !this.mServedView.checkInputConnectionProxy(view))) {
                Log.w(TAG, "The current service view is not the focus view");
                return false;
            } else if (isSecImmEnabled()) {
                boolean showSoftInput = this.mSecImmHelper.showSoftInput(view, flags, resultReceiver, this.mClient);
                return showSoftInput;
            } else {
                try {
                    boolean showSoftInput2 = this.mService.showSoftInput(this.mClient, flags, resultReceiver);
                    return showSoftInput2;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @Deprecated
    public void showSoftInputUnchecked(int flags, ResultReceiver resultReceiver) {
        try {
            Log.w(TAG, "showSoftInputUnchecked() is a hidden method, which will be removed soon. If you are using android.support.v7.widget.SearchView, please update to version 26.0 or newer version.");
            this.mService.showSoftInput(this.mClient, flags, resultReceiver);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
        return hideSoftInputFromWindow(windowToken, flags, null);
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags, ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != null) {
                if (this.mServedView.getWindowToken() == windowToken) {
                    if (isSecImmEnabled()) {
                        boolean hideSoftInputFromWindow = this.mSecImmHelper.hideSoftInputFromWindow(windowToken, flags, resultReceiver, this.mServedView, this.mClient);
                        return hideSoftInputFromWindow;
                    }
                    try {
                        boolean hideSoftInput = this.mService.hideSoftInput(this.mClient, flags, resultReceiver);
                        return hideSoftInput;
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001e, code lost:
        return;
     */
    public void toggleSoftInputFromWindow(IBinder windowToken, int showFlags, int hideFlags) {
        synchronized (this.mH) {
            if (this.mServedView != null) {
                if (this.mServedView.getWindowToken() == windowToken) {
                    if (this.mCurMethod != null) {
                        try {
                            this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
        }
    }

    public void toggleSoftInput(int showFlags, int hideFlags) {
        if (this.mCurMethod != null) {
            try {
                this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
            } catch (RemoteException e) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        return;
     */
    public void restartInput(View view) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                this.mServedConnecting = true;
                startInputInner(3, null, 0, 0, 0);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01cc, code lost:
        if (r2.result == 11) goto L_0x01cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x01cf, code lost:
        r1.mRestartOnNextWindowFocus = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x01d4, code lost:
        if (r1.mCurMethod == null) goto L_0x0248;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x01d8, code lost:
        if (r1.mCompletions == null) goto L_0x0248;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
        r1.mCurMethod.displayCompletions(r1.mCompletions);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002d, code lost:
        closeCurrentInput();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01e6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x01eb, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x01ee, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01ef, code lost:
        r5 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x01f1, code lost:
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x01f6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x01f7, code lost:
        r5 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x01f9, code lost:
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0030, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x01fe, code lost:
        r5 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0200, code lost:
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0204, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x0205, code lost:
        r5 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x0207, code lost:
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x020b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x020c, code lost:
        r5 = r24;
        r18 = r7;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:?, code lost:
        android.util.Log.e(TAG, "startInputInner() has exception -> " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0235, code lost:
        r5 = r24;
        r18 = r7;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x0241, code lost:
        android.util.Log.e(TAG, "Can not start input method, need check in Settings, a default language is needed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x0249, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x024a, code lost:
        r5 = r24;
        r18 = r7;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0256, code lost:
        android.util.Log.w(TAG, "IME died: " + r1.mCurId, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x026f, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x0271, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0039, code lost:
        if (r15.getLooper() == android.os.Looper.myLooper()) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x0272, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0273, code lost:
        r5 = r24;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x027e, code lost:
        r5 = r24;
        r3 = r25;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:?, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x028a, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x028b, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x028c, code lost:
        r15 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x028e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x028f, code lost:
        r5 = r24;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
        r15 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        r15.post(new android.view.inputmethod.$$Lambda$InputMethodManager$jNoqB3BbMToNjx3pSWwvtHoFfg(r1, r13));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:?, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x029c, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x029d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x02a6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0043, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        r12 = new android.view.inputmethod.EditorInfo();
        r12.packageName = r14.getContext().getOpPackageName();
        r12.fieldId = r14.getId();
        r10 = r14.onCreateInputConnection(r12);
        r9 = r1.mH;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0060, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0063, code lost:
        if (r1.mServedView != r14) goto L_0x027e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0067, code lost:
        if (r1.mServedConnecting != false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0069, code lost:
        r5 = r24;
        r3 = r25;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0078, code lost:
        if (r1.mCurrentTextBoxAttribute != null) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007a, code lost:
        r8 = r25 | 256;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0080, code lost:
        r8 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r1.mCurrentTextBoxAttribute = r12;
        r1.mServedConnecting = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x008a, code lost:
        if (r1.mServedInputConnectionWrapper == null) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r1.mServedInputConnectionWrapper.deactivate();
        r1.mServedInputConnectionWrapper = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0094, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0095, code lost:
        r5 = r24;
        r19 = r9;
        r20 = r10;
        r4 = r12;
        r17 = r15;
        r15 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a1, code lost:
        if (r10 == null) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a3, code lost:
        r1.mCursorSelStart = r12.initialSelStart;
        r1.mCursorSelEnd = r12.initialSelEnd;
        r1.mCursorCandStart = -1;
        r1.mCursorCandEnd = -1;
        r1.mCursorRect.setEmpty();
        r1.mCursorAnchorInfo = null;
        r2 = android.view.inputmethod.InputConnectionInspector.getMissingMethodFlags(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00bd, code lost:
        if ((r2 & 32) == 0) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00bf, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c1, code lost:
        r3 = r10.getHandler();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c7, code lost:
        if (r3 == null) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c9, code lost:
        r5 = r3.getLooper();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ce, code lost:
        r5 = r15.getLooper();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d5, code lost:
        r11 = r2;
        r7 = new android.view.inputmethod.InputMethodManager.ControlledInputConnectionWrapper(r5, r10, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00da, code lost:
        r11 = 0;
        r7 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r1.mServedInputConnectionWrapper = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r2 = r1.mService;
        r4 = r1.mClient;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ee, code lost:
        r17 = r15;
        r15 = r8;
        r19 = r9;
        r20 = r10;
        r1 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        r2 = r2.startInputOrWindowGainedFocus(r13, r4, r24, r8, r26, r27, r12, r7, r11, r14.getContext().getApplicationInfo().targetSdkVersion);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x010c, code lost:
        if (r2 != null) goto L_0x0193;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x010e, code lost:
        android.util.Log.wtf(TAG, "startInputOrWindowGainedFocus must not return null. startInputReason=" + com.android.internal.view.InputMethodClient.getStartInputReason(r23) + " editorInfo=" + r1 + " controlFlags=#" + java.lang.Integer.toHexString(r15));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x013e, code lost:
        if (r13 != 1) goto L_0x017e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0140, code lost:
        r4 = new java.lang.StringBuilder();
        r4.append("startInputOrWindowGainedFocus failed. Window focus may have already been lost. win=");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        r4.append(r24);
        r4.append(" view=");
        r4.append(dumpViewInfo(r14));
        android.util.Log.w(TAG, r4.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0165, code lost:
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x016a, code lost:
        if (r1.mActive == false) goto L_0x016e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x016c, code lost:
        if (r20 == null) goto L_0x0191;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x016e, code lost:
        r1.mRestartOnNextWindowFocus = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0172, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0175, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x017b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x017e, code lost:
        r5 = r24;
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0184, code lost:
        if (r13 != 3) goto L_0x0191;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0186, code lost:
        android.util.Log.w(TAG, "reStartinput failed, need refresh!");
        r1.mRestartOnNextWindowFocus = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0192, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0193, code lost:
        r5 = r24;
        r4 = r1;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x019a, code lost:
        if (r2.id == null) goto L_0x01b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x019c, code lost:
        r1.setInputChannelLocked(r2.channel);
        r1.mBindSequence = r2.sequence;
        r1.mCurMethod = r2.method;
        r1.mCurId = r2.id;
        r1.mNextUserActionNotificationSequenceNumber = r2.userActionNotificationSequenceNumber;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01b4, code lost:
        if (r2.channel == null) goto L_0x01c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        android.util.Log.w(TAG, "startInputReason = " + r13);
        r15 = r14.getHandler();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01ba, code lost:
        if (r2.channel == r1.mCurChannel) goto L_0x01c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01bc, code lost:
        r2.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01c3, code lost:
        if (r1.mCurMethod != null) goto L_0x01c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:?, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01c7, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002b, code lost:
        if (r15 != null) goto L_0x0031;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:64:0x0168, B:107:0x01da] */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x01e6 A[ExcHandler: NullPointerException (e java.lang.NullPointerException), Splitter:B:64:0x0168] */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x01e8 A[ExcHandler: IllegalArgumentException (e java.lang.IllegalArgumentException), Splitter:B:64:0x0168] */
    public boolean startInputInner(int startInputReason, IBinder windowGainingFocus, int controlFlags, int softInputMode, int windowFlags) {
        InputMethodManager inputMethodManager = this;
        int i = startInputReason;
        synchronized (inputMethodManager.mH) {
            try {
                View view = inputMethodManager.mServedView;
                if (view == null) {
                    return false;
                }
            } catch (Throwable th) {
                th = th;
                IBinder iBinder = windowGainingFocus;
                int i2 = controlFlags;
                while (true) {
                    throw th;
                }
            }
        }
    }

    public void windowDismissed(IBinder appWindowToken) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != null && this.mServedView.getWindowToken() == appWindowToken) {
                finishInputLocked();
            }
        }
    }

    public void focusIn(View view) {
        synchronized (this.mH) {
            focusInLocked(view);
        }
    }

    /* access modifiers changed from: package-private */
    public void focusInLocked(View view) {
        if ((view == null || !view.isTemporarilyDetached()) && this.mCurRootView == view.getRootView()) {
            this.mNextServedView = view;
            this.mInTransition = this.mNextServedView != this.mServedView;
            scheduleCheckFocusLocked(view);
        }
    }

    public void focusOut(View view) {
        synchronized (this.mH) {
            this.mInTransition = false;
            View view2 = this.mServedView;
        }
    }

    public void onViewDetachedFromWindow(View view) {
        synchronized (this.mH) {
            if (this.mServedView == view) {
                this.mNextServedView = null;
                scheduleCheckFocusLocked(view);
            }
        }
    }

    static void scheduleCheckFocusLocked(View view) {
        ViewRootImpl viewRootImpl = view.getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.dispatchCheckFocus();
        }
    }

    public void checkFocus() {
        if (checkFocusNoStartInput(false)) {
            startInputInner(4, null, 0, 0, 0);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0032, code lost:
        if (r1 == null) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0034, code lost:
        r1.finishComposingText();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0037, code lost:
        return true;
     */
    public boolean checkFocusNoStartInput(boolean forceNewFocus) {
        if (this.mServedView == this.mNextServedView && !forceNewFocus) {
            return false;
        }
        synchronized (this.mH) {
            if (this.mServedView == this.mNextServedView && !forceNewFocus) {
                return false;
            }
            if (this.mNextServedView == null) {
                finishInputLocked();
                closeCurrentInput();
                return false;
            }
            ControlledInputConnectionWrapper ic = this.mServedInputConnectionWrapper;
            this.mServedView = this.mNextServedView;
            this.mCurrentTextBoxAttribute = null;
            this.mCompletions = null;
            this.mServedConnecting = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeCurrentInput() {
        try {
            this.mService.hideSoftInput(this.mClient, 2, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001d, code lost:
        if (r23 == null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001f, code lost:
        r0 = 0 | 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0025, code lost:
        if (r23.onCheckIsTextEditor() == false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0027, code lost:
        r0 = r0 | 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0029, code lost:
        if (r25 == false) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002b, code lost:
        r0 = r0 | 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002d, code lost:
        r20 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        if (checkFocusNoStartInput(r8) == false) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        if (startInputInner(1, r22.getWindowToken(), r20, r24, r26) == false) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0047, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0048, code lost:
        r1 = r7.mH;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004a, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        android.util.Log.v(TAG, "Reporting focus gain, without startInput");
        r7.mService.startInputOrWindowGainedFocus(2, r7.mClient, r22.getWindowToken(), r20, r24, r26, null, null, 0, r22.getContext().getApplicationInfo().targetSdkVersion);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0078, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0079, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0080, code lost:
        throw r0.rethrowFromSystemServer();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0082, code lost:
        throw r0;
     */
    public void onPostWindowFocus(View rootView, View focusedView, int softInputMode, boolean first, int windowFlags) {
        boolean forceNewFocus;
        synchronized (this.mH) {
            try {
                if (this.mRestartOnNextWindowFocus) {
                    this.mRestartOnNextWindowFocus = false;
                    forceNewFocus = true;
                } else {
                    forceNewFocus = false;
                }
                try {
                    focusInLocked(focusedView != null ? focusedView : rootView);
                } catch (Throwable th) {
                    th = th;
                    boolean z = forceNewFocus;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public void onPreWindowFocus(View rootView, boolean hasWindowFocus) {
        synchronized (this.mH) {
            if (rootView == null) {
                try {
                    this.mCurRootView = null;
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (hasWindowFocus) {
                this.mCurRootView = rootView;
            } else if (rootView == this.mCurRootView) {
                this.mCurRootView = null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0061, code lost:
        return;
     */
    public void updateSelection(View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    if (!(this.mCursorSelStart == selStart && this.mCursorSelEnd == selEnd && this.mCursorCandStart == candidatesStart && this.mCursorCandEnd == candidatesEnd)) {
                        try {
                            int oldSelStart = this.mCursorSelStart;
                            int oldSelEnd = this.mCursorSelEnd;
                            this.mCursorSelStart = selStart;
                            this.mCursorSelEnd = selEnd;
                            this.mCursorCandStart = candidatesStart;
                            this.mCursorCandEnd = candidatesEnd;
                            this.mCurMethod.updateSelection(oldSelStart, oldSelEnd, selStart, selEnd, candidatesStart, candidatesEnd);
                        } catch (RemoteException e) {
                            Log.w(TAG, "IME died: " + this.mCurId, e);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004a, code lost:
        return;
     */
    public void viewClicked(View view) {
        boolean focusChanged = this.mServedView != this.mNextServedView;
        checkFocus();
        synchronized (this.mH) {
            if (!((this.mServedView != view && (this.mServedView == null || !this.mServedView.checkInputConnectionProxy(view))) || this.mCurrentTextBoxAttribute == null || this.mCurMethod == null)) {
                try {
                    this.mCurMethod.viewClicked(focusChanged);
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + this.mCurId, e);
                }
            }
        }
    }

    public boolean isSecImmEnabled() {
        if (this.mSecImmHelper == null) {
            return false;
        }
        return this.mSecImmHelper.isUseSecureIME();
    }

    public void resetInTransitionState() {
        this.mInTransition = false;
    }

    @Deprecated
    public boolean isWatchingCursor(View view) {
        return false;
    }

    public boolean isCursorAnchorInfoEnabled() {
        boolean z;
        synchronized (this.mH) {
            z = true;
            boolean isImmediate = (this.mRequestUpdateCursorAnchorInfoMonitorMode & 1) != 0;
            boolean isMonitoring = (this.mRequestUpdateCursorAnchorInfoMonitorMode & 2) != 0;
            if (!isImmediate) {
                if (!isMonitoring) {
                    z = false;
                }
            }
        }
        return z;
    }

    public void setUpdateCursorAnchorInfoMode(int flags) {
        synchronized (this.mH) {
            this.mRequestUpdateCursorAnchorInfoMonitorMode = flags;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0059, code lost:
        return;
     */
    @Deprecated
    public void updateCursor(View view, int left, int top, int right, int bottom) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    this.mTmpCursorRect.set(left, top, right, bottom);
                    if (!this.mCursorRect.equals(this.mTmpCursorRect)) {
                        try {
                            this.mCurMethod.updateCursor(this.mTmpCursorRect);
                            this.mCursorRect.set(this.mTmpCursorRect);
                        } catch (RemoteException e) {
                            Log.w(TAG, "IME died: " + this.mCurId, e);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0063, code lost:
        return;
     */
    public void updateCursorAnchorInfo(View view, CursorAnchorInfo cursorAnchorInfo) {
        if (view != null && cursorAnchorInfo != null) {
            checkFocus();
            synchronized (this.mH) {
                if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                    if (this.mCurMethod != null) {
                        boolean isImmediate = true;
                        if ((this.mRequestUpdateCursorAnchorInfoMonitorMode & 1) == 0) {
                            isImmediate = false;
                        }
                        if (isImmediate || !Objects.equals(this.mCursorAnchorInfo, cursorAnchorInfo)) {
                            try {
                                this.mCurMethod.updateCursorAnchorInfo(cursorAnchorInfo);
                                this.mCursorAnchorInfo = cursorAnchorInfo;
                                this.mRequestUpdateCursorAnchorInfoMonitorMode &= -2;
                            } catch (RemoteException e) {
                                Log.w(TAG, "IME died: " + this.mCurId, e);
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        return;
     */
    public void sendAppPrivateCommand(View view, String action, Bundle data) {
        checkFocus();
        synchronized (this.mH) {
            if (!((this.mServedView != view && (this.mServedView == null || !this.mServedView.checkInputConnectionProxy(view))) || this.mCurrentTextBoxAttribute == null || this.mCurMethod == null)) {
                try {
                    this.mCurMethod.appPrivateCommand(action, data);
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + this.mCurId, e);
                }
            }
        }
    }

    @Deprecated
    public void setInputMethod(IBinder token, String id) {
        setInputMethodInternal(token, id);
    }

    public void setInputMethodInternal(IBinder token, String id) {
        try {
            this.mService.setInputMethod(token, id);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        setInputMethodAndSubtypeInternal(token, id, subtype);
    }

    public void setInputMethodAndSubtypeInternal(IBinder token, String id, InputMethodSubtype subtype) {
        try {
            this.mService.setInputMethodAndSubtype(token, id, subtype);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void hideSoftInputFromInputMethod(IBinder token, int flags) {
        hideSoftInputFromInputMethodInternal(token, flags);
    }

    public void hideSoftInputFromInputMethodInternal(IBinder token, int flags) {
        try {
            this.mService.hideMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void showSoftInputFromInputMethod(IBinder token, int flags) {
        showSoftInputFromInputMethodInternal(token, flags);
    }

    public void showSoftInputFromInputMethodInternal(IBinder token, int flags) {
        try {
            this.mService.showMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int dispatchInputEvent(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
        synchronized (this.mH) {
            if (this.mCurMethod == null) {
                return 0;
            }
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (this.mCurId != null && keyEvent.getKeyCode() == 62 && keyEvent.getAction() == 0 && keyEvent.isShiftPressed() && keyEvent.getRepeatCount() == 0 && "tablet".equals(IS_TABLET)) {
                    for (String ime : BLACK_TABLE) {
                        if (ime.equals(this.mCurId)) {
                            showInputMethodPickerLocked();
                            return 1;
                        }
                    }
                }
                if (keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 63 && keyEvent.getRepeatCount() == 0) {
                    showInputMethodPickerLocked();
                    return 1;
                }
            }
            PendingEvent p = obtainPendingEventLocked(event, token, this.mCurId, callback, handler);
            if (this.mMainLooper.isCurrentThread()) {
                int sendInputEventOnMainLooperLocked = sendInputEventOnMainLooperLocked(p);
                return sendInputEventOnMainLooperLocked;
            }
            Message msg = this.mH.obtainMessage(5, p);
            msg.setAsynchronous(true);
            this.mH.sendMessage(msg);
            return -1;
        }
    }

    public void dispatchKeyEventFromInputMethod(View targetView, KeyEvent event) {
        ViewRootImpl viewRootImpl;
        synchronized (this.mH) {
            if (targetView != null) {
                try {
                    viewRootImpl = targetView.getViewRootImpl();
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                viewRootImpl = null;
            }
            if (viewRootImpl == null && this.mServedView != null) {
                viewRootImpl = this.mServedView.getViewRootImpl();
            }
            if (viewRootImpl != null) {
                viewRootImpl.dispatchKeyFromIme(event);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendInputEventAndReportResultOnMainLooper(PendingEvent p) {
        synchronized (this.mH) {
            int result = sendInputEventOnMainLooperLocked(p);
            if (result != -1) {
                boolean z = true;
                if (result != 1) {
                    z = false;
                }
                boolean handled = z;
                invokeFinishedInputEventCallback(p, handled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int sendInputEventOnMainLooperLocked(PendingEvent p) {
        if (this.mCurChannel != null) {
            if (this.mCurSender == null) {
                this.mCurSender = new ImeInputEventSender(this.mCurChannel, this.mH.getLooper());
            }
            InputEvent event = p.mEvent;
            int seq = event.getSequenceNumber();
            if (this.mCurSender.sendInputEvent(seq, event)) {
                this.mPendingEvents.put(seq, p);
                Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
                Message msg = this.mH.obtainMessage(6, seq, 0, p);
                msg.setAsynchronous(true);
                this.mH.sendMessageDelayed(msg, INPUT_METHOD_NOT_RESPONDING_TIMEOUT);
                return -1;
            }
            Log.w(TAG, "Unable to send input event to IME: " + this.mCurId + " dropping: " + event);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0049, code lost:
        invokeFinishedInputEventCallback(r2, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004d, code lost:
        return;
     */
    public void finishedInputEvent(int seq, boolean handled, boolean timeout) {
        synchronized (this.mH) {
            int index = this.mPendingEvents.indexOfKey(seq);
            if (index >= 0) {
                PendingEvent p = this.mPendingEvents.valueAt(index);
                this.mPendingEvents.removeAt(index);
                Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
                if (timeout) {
                    Log.w(TAG, "Timeout waiting for IME to handle input event after 2500 ms: " + p.mInputMethodId);
                } else {
                    this.mH.removeMessages(6, p);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeFinishedInputEventCallback(PendingEvent p, boolean handled) {
        p.mHandled = handled;
        if (p.mHandler.getLooper().isCurrentThread()) {
            p.run();
            return;
        }
        Message msg = Message.obtain(p.mHandler, (Runnable) p);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void flushPendingEventsLocked() {
        this.mH.removeMessages(7);
        int count = this.mPendingEvents.size();
        for (int i = 0; i < count; i++) {
            Message msg = this.mH.obtainMessage(7, this.mPendingEvents.keyAt(i), 0);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }
    }

    private PendingEvent obtainPendingEventLocked(InputEvent event, Object token, String inputMethodId, FinishedInputEventCallback callback, Handler handler) {
        PendingEvent p = this.mPendingEventPool.acquire();
        if (p == null) {
            p = new PendingEvent();
        }
        p.mEvent = event;
        p.mToken = token;
        p.mInputMethodId = inputMethodId;
        p.mCallback = callback;
        p.mHandler = handler;
        return p;
    }

    /* access modifiers changed from: private */
    public void recyclePendingEventLocked(PendingEvent p) {
        p.recycle();
        this.mPendingEventPool.release(p);
    }

    public void showInputMethodPicker() {
        synchronized (this.mH) {
            showInputMethodPickerLocked();
        }
    }

    public void showInputMethodPicker(boolean showAuxiliarySubtypes) {
        int mode;
        synchronized (this.mH) {
            if (showAuxiliarySubtypes) {
                mode = 1;
            } else {
                mode = 2;
            }
            try {
                this.mService.showInputMethodPickerFromClient(this.mClient, mode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void showInputMethodPickerLocked() {
        try {
            this.mService.showInputMethodPickerFromClient(this.mClient, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInputMethodPickerShown() {
        try {
            return this.mService.isInputMethodPickerShownForTest();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void showInputMethodAndSubtypeEnabler(String imiId) {
        synchronized (this.mH) {
            try {
                this.mService.showInputMethodAndSubtypeEnablerFromClient(this.mClient, imiId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        try {
            return this.mService.getCurrentInputMethodSubtype();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        boolean currentInputMethodSubtype;
        synchronized (this.mH) {
            try {
                currentInputMethodSubtype = this.mService.setCurrentInputMethodSubtype(subtype);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
        return currentInputMethodSubtype;
    }

    public void notifyUserAction() {
        synchronized (this.mH) {
            if (this.mLastSentUserActionNotificationSequenceNumber != this.mNextUserActionNotificationSequenceNumber) {
                try {
                    this.mService.notifyUserAction(this.mNextUserActionNotificationSequenceNumber);
                    this.mLastSentUserActionNotificationSequenceNumber = this.mNextUserActionNotificationSequenceNumber;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public Map<InputMethodInfo, List<InputMethodSubtype>> getShortcutInputMethodsAndSubtypes() {
        HashMap<InputMethodInfo, List<InputMethodSubtype>> ret;
        synchronized (this.mH) {
            ret = new HashMap<>();
            try {
                List<Object> info = this.mService.getShortcutInputMethodsAndSubtypes();
                ArrayList<InputMethodSubtype> subtypes = null;
                if (info != null && !info.isEmpty()) {
                    int N = info.size();
                    int i = 0;
                    while (true) {
                        if (i < N) {
                            Object o = info.get(i);
                            if (o instanceof InputMethodInfo) {
                                if (ret.containsKey(o)) {
                                    Log.e(TAG, "IMI list already contains the same InputMethod.");
                                    break;
                                }
                                subtypes = new ArrayList<>();
                                ret.put((InputMethodInfo) o, subtypes);
                            } else if (subtypes != null && (o instanceof InputMethodSubtype)) {
                                subtypes.add((InputMethodSubtype) o);
                            }
                            i++;
                        }
                    }
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return ret;
    }

    public int getInputMethodWindowVisibleHeight() {
        int inputMethodWindowVisibleHeight;
        synchronized (this.mH) {
            try {
                inputMethodWindowVisibleHeight = this.mService.getInputMethodWindowVisibleHeight();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
        return inputMethodWindowVisibleHeight;
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        synchronized (this.mH) {
            try {
                this.mService.clearLastInputMethodWindowForTransition(token);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Deprecated
    public boolean switchToLastInputMethod(IBinder imeToken) {
        return switchToPreviousInputMethodInternal(imeToken);
    }

    public boolean switchToPreviousInputMethodInternal(IBinder imeToken) {
        boolean switchToPreviousInputMethod;
        synchronized (this.mH) {
            try {
                switchToPreviousInputMethod = this.mService.switchToPreviousInputMethod(imeToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
        return switchToPreviousInputMethod;
    }

    @Deprecated
    public boolean switchToNextInputMethod(IBinder imeToken, boolean onlyCurrentIme) {
        return switchToNextInputMethodInternal(imeToken, onlyCurrentIme);
    }

    public boolean switchToNextInputMethodInternal(IBinder imeToken, boolean onlyCurrentIme) {
        boolean switchToNextInputMethod;
        synchronized (this.mH) {
            try {
                switchToNextInputMethod = this.mService.switchToNextInputMethod(imeToken, onlyCurrentIme);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
        return switchToNextInputMethod;
    }

    @Deprecated
    public boolean shouldOfferSwitchingToNextInputMethod(IBinder imeToken) {
        return shouldOfferSwitchingToNextInputMethodInternal(imeToken);
    }

    public boolean shouldOfferSwitchingToNextInputMethodInternal(IBinder imeToken) {
        boolean shouldOfferSwitchingToNextInputMethod;
        synchronized (this.mH) {
            try {
                shouldOfferSwitchingToNextInputMethod = this.mService.shouldOfferSwitchingToNextInputMethod(imeToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
        return shouldOfferSwitchingToNextInputMethod;
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        synchronized (this.mH) {
            try {
                this.mService.setAdditionalInputMethodSubtypes(imiId, subtypes);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        InputMethodSubtype lastInputMethodSubtype;
        synchronized (this.mH) {
            try {
                lastInputMethodSubtype = this.mService.getLastInputMethodSubtype();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
        return lastInputMethodSubtype;
    }

    public void exposeContent(IBinder token, InputContentInfo inputContentInfo, EditorInfo editorInfo) {
        Uri contentUri = inputContentInfo.getContentUri();
        try {
            IInputContentUriToken uriToken = this.mService.createInputContentUriToken(token, contentUri, editorInfo.packageName);
            if (uriToken != null) {
                inputContentInfo.setUriToken(uriToken);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "createInputContentAccessToken failed. contentUri=" + contentUri.toString() + " packageName=" + editorInfo.packageName, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void doDump(FileDescriptor fd, PrintWriter fout, String[] args) {
        Printer p = new PrintWriterPrinter(fout);
        p.println("Input method client state for " + this + SettingsStringUtil.DELIMITER);
        StringBuilder sb = new StringBuilder();
        sb.append("  mService=");
        sb.append(this.mService);
        p.println(sb.toString());
        p.println("  mMainLooper=" + this.mMainLooper);
        p.println("  mIInputContext=" + this.mIInputContext);
        p.println("  mActive=" + this.mActive + " mRestartOnNextWindowFocus=" + this.mRestartOnNextWindowFocus + " mBindSequence=" + this.mBindSequence + " mCurId=" + this.mCurId);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  mFullscreenMode=");
        sb2.append(this.mFullscreenMode);
        p.println(sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("  mCurMethod=");
        sb3.append(this.mCurMethod);
        p.println(sb3.toString());
        p.println("  mCurRootView=" + this.mCurRootView);
        p.println("  mServedView=" + this.mServedView);
        p.println("  mNextServedView=" + this.mNextServedView);
        p.println("  mServedConnecting=" + this.mServedConnecting);
        if (this.mCurrentTextBoxAttribute != null) {
            p.println("  mCurrentTextBoxAttribute:");
            this.mCurrentTextBoxAttribute.dump(p, "    ");
        } else {
            p.println("  mCurrentTextBoxAttribute: null");
        }
        p.println("  mServedInputConnectionWrapper=" + this.mServedInputConnectionWrapper);
        p.println("  mCompletions=" + Arrays.toString(this.mCompletions));
        p.println("  mCursorRect=" + this.mCursorRect);
        p.println("  mCursorSelStart=" + this.mCursorSelStart + " mCursorSelEnd=" + this.mCursorSelEnd + " mCursorCandStart=" + this.mCursorCandStart + " mCursorCandEnd=" + this.mCursorCandEnd);
        StringBuilder sb4 = new StringBuilder();
        sb4.append("  mNextUserActionNotificationSequenceNumber=");
        sb4.append(this.mNextUserActionNotificationSequenceNumber);
        sb4.append(" mLastSentUserActionNotificationSequenceNumber=");
        sb4.append(this.mLastSentUserActionNotificationSequenceNumber);
        p.println(sb4.toString());
    }

    private static String dumpViewInfo(View view) {
        if (view == null) {
            return "null";
        }
        return view + (",focus=" + view.hasFocus()) + (",windowFocus=" + view.hasWindowFocus()) + (",autofillUiShowing=" + isAutofillUIShowing(view)) + (",window=" + view.getWindowToken()) + (",temporaryDetach=" + view.isTemporarilyDetached());
    }
}
