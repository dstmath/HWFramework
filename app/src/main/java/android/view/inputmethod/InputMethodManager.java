package android.view.inputmethod;

import android.common.HwFrameworkFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.Trace;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewRootImpl;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputConnectionWrapper;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodClient.Stub;
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
    static final int MSG_BIND = 2;
    static final int MSG_DUMP = 1;
    static final int MSG_FLUSH_INPUT_EVENT = 7;
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
    final Stub mClient;
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
    boolean mHasBeenInactive;
    final IInputContext mIInputContext;
    boolean mInTransition;
    private int mLastSentUserActionNotificationSequenceNumber;
    View mLastSrvView;
    final Looper mMainLooper;
    View mNextServedView;
    private int mNextUserActionNotificationSequenceNumber;
    final Pool<PendingEvent> mPendingEventPool;
    final SparseArray<PendingEvent> mPendingEvents;
    private int mRequestUpdateCursorAnchorInfoMonitorMode;
    IHwSecImmHelper mSecImmHelper;
    boolean mServedConnecting;
    ControlledInputConnectionWrapper mServedInputConnectionWrapper;
    View mServedView;
    final IInputMethodManager mService;
    Rect mTmpCursorRect;

    public interface FinishedInputEventCallback {
        void onFinishedInputEvent(Object obj, boolean z);
    }

    /* renamed from: android.view.inputmethod.InputMethodManager.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ int val$startInputReason;

        AnonymousClass2(int val$startInputReason) {
            this.val$startInputReason = val$startInputReason;
        }

        public void run() {
            InputMethodManager.this.startInputInner(this.val$startInputReason, null, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO);
        }
    }

    private static class ControlledInputConnectionWrapper extends IInputConnectionWrapper {
        private final InputMethodManager mParentInputMethodManager;

        public ControlledInputConnectionWrapper(Looper mainLooper, InputConnection conn, InputMethodManager inputMethodManager) {
            super(mainLooper, conn);
            this.mParentInputMethodManager = inputMethodManager;
        }

        public boolean isActive() {
            return (!this.mParentInputMethodManager.mActive || isFinished()) ? InputMethodManager.DEBUG : true;
        }

        void deactivate() {
            if (!isFinished()) {
                closeConnection();
            }
        }

        protected void onUserAction() {
            this.mParentInputMethodManager.notifyUserAction();
        }

        protected void onReportFullscreenMode(boolean enabled, boolean calledInBackground) {
            this.mParentInputMethodManager.onReportFullscreenMode(enabled, calledInBackground, getInputMethodId());
        }

        public String toString() {
            return "ControlledInputConnectionWrapper{connection=" + getInputConnection() + " finished=" + isFinished() + " mParentInputMethodManager.mActive=" + this.mParentInputMethodManager.mActive + " mInputMethodId=" + getInputMethodId() + "}";
        }
    }

    class H extends Handler {
        H(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int reason;
            switch (msg.what) {
                case InputMethodManager.SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES /*1*/:
                    SomeArgs args = msg.obj;
                    try {
                        InputMethodManager.this.doDump((FileDescriptor) args.arg1, (PrintWriter) args.arg2, (String[]) args.arg3);
                    } catch (RuntimeException e) {
                        ((PrintWriter) args.arg2).println("Exception: " + e);
                    }
                    synchronized (args.arg4) {
                        ((CountDownLatch) args.arg4).countDown();
                        break;
                    }
                    args.recycle();
                case InputMethodManager.SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES /*2*/:
                    InputBindResult res = msg.obj;
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence < 0 || InputMethodManager.this.mBindSequence != res.sequence) {
                            Log.w(InputMethodManager.TAG, "Ignoring onBind: cur seq=" + InputMethodManager.this.mBindSequence + ", given seq=" + res.sequence);
                            if (!(res.channel == null || res.channel == InputMethodManager.this.mCurChannel)) {
                                res.channel.dispose();
                            }
                            return;
                        }
                        InputMethodManager.this.mRequestUpdateCursorAnchorInfoMonitorMode = InputMethodManager.SHOW_IM_PICKER_MODE_AUTO;
                        InputMethodManager.this.setInputChannelLocked(res.channel);
                        InputMethodManager.this.mCurMethod = res.method;
                        InputMethodManager.this.mCurId = res.id;
                        InputMethodManager.this.mBindSequence = res.sequence;
                        InputMethodManager.this.startInputInner(InputMethodManager.MSG_SEND_INPUT_EVENT, null, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO);
                    }
                case InputMethodManager.RESULT_HIDDEN /*3*/:
                    int sequence = msg.arg1;
                    reason = msg.arg2;
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence != sequence) {
                            return;
                        }
                        InputMethodManager.this.clearBindingLocked();
                        if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.isFocused()) {
                            InputMethodManager.this.mServedConnecting = true;
                        }
                        boolean startInput = InputMethodManager.this.mActive;
                        if (startInput) {
                            InputMethodManager.this.startInputInner(InputMethodManager.MSG_TIMEOUT_INPUT_EVENT, null, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO);
                        }
                    }
                case InputMethodManager.MSG_SET_ACTIVE /*4*/:
                    boolean active = msg.arg1 != 0 ? true : InputMethodManager.DEBUG;
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mActive = active;
                        InputMethodManager.this.mFullscreenMode = InputMethodManager.DEBUG;
                        if (!active) {
                            InputMethodManager.this.mHasBeenInactive = true;
                            try {
                                InputMethodManager.this.mIInputContext.finishComposingText();
                            } catch (RemoteException e2) {
                            }
                        }
                        if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.hasWindowFocus() && InputMethodManager.this.checkFocusNoStartInput(InputMethodManager.this.mHasBeenInactive)) {
                            if (active) {
                                reason = InputMethodManager.MSG_FLUSH_INPUT_EVENT;
                            } else {
                                reason = 8;
                            }
                            InputMethodManager.this.startInputInner(reason, null, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO);
                        }
                        break;
                    }
                case InputMethodManager.MSG_SEND_INPUT_EVENT /*5*/:
                    InputMethodManager.this.sendInputEventAndReportResultOnMainLooper((PendingEvent) msg.obj);
                case InputMethodManager.MSG_TIMEOUT_INPUT_EVENT /*6*/:
                    int seq = msg.arg1;
                    if (msg.obj instanceof PendingEvent) {
                        PendingEvent p = msg.obj;
                        seq = p.mEvent == null ? msg.arg1 : p.mEvent.getSequenceNumber();
                    }
                    InputMethodManager.this.finishedInputEvent(seq, InputMethodManager.DEBUG, true);
                case InputMethodManager.MSG_FLUSH_INPUT_EVENT /*7*/:
                    InputMethodManager.this.finishedInputEvent(msg.arg1, InputMethodManager.DEBUG, InputMethodManager.DEBUG);
                case InputMethodManager.MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER /*9*/:
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mNextUserActionNotificationSequenceNumber = msg.arg1;
                        break;
                    }
                    break;
            }
        }
    }

    private final class ImeInputEventSender extends InputEventSender {
        public ImeInputEventSender(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEventFinished(int seq, boolean handled) {
            InputMethodManager.this.finishedInputEvent(seq, handled, InputMethodManager.DEBUG);
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
            this.mHandled = InputMethodManager.DEBUG;
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

    InputMethodManager(IInputMethodManager service, Looper looper) {
        this.mActive = DEBUG;
        this.mHasBeenInactive = true;
        this.mTmpCursorRect = new Rect();
        this.mCursorRect = new Rect();
        this.mNextUserActionNotificationSequenceNumber = NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
        this.mLastSentUserActionNotificationSequenceNumber = NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
        this.mCursorAnchorInfo = null;
        this.mBindSequence = NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
        this.mRequestUpdateCursorAnchorInfoMonitorMode = SHOW_IM_PICKER_MODE_AUTO;
        this.mPendingEventPool = new SimplePool(20);
        this.mPendingEvents = new SparseArray(20);
        this.mClient = new Stub() {
            protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
                CountDownLatch latch = new CountDownLatch(InputMethodManager.SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES);
                SomeArgs sargs = SomeArgs.obtain();
                sargs.arg1 = fd;
                sargs.arg2 = fout;
                sargs.arg3 = args;
                sargs.arg4 = latch;
                InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(InputMethodManager.SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES, sargs));
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
                InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(InputMethodManager.SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES, res));
            }

            public void onUnbindMethod(int sequence, int unbindReason) {
                InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(InputMethodManager.RESULT_HIDDEN, sequence, unbindReason));
            }

            public void setActive(boolean active) {
                int i;
                H h = InputMethodManager.this.mH;
                H h2 = InputMethodManager.this.mH;
                if (active) {
                    i = InputMethodManager.SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES;
                } else {
                    i = InputMethodManager.SHOW_IM_PICKER_MODE_AUTO;
                }
                h.sendMessage(h2.obtainMessage(InputMethodManager.MSG_SET_ACTIVE, i, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO));
            }

            public void setUserActionNotificationSequenceNumber(int sequenceNumber) {
                InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(InputMethodManager.MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER, sequenceNumber, InputMethodManager.SHOW_IM_PICKER_MODE_AUTO));
            }
        };
        this.mDummyInputConnection = new BaseInputConnection(this, (boolean) DEBUG);
        this.mLastSrvView = null;
        this.mInTransition = DEBUG;
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
                sInstance = new InputMethodManager(IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method")), Looper.getMainLooper());
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

    public List<InputMethodInfo> getEnabledInputMethodList() {
        try {
            return this.mService.getEnabledInputMethodList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
        String str = null;
        try {
            IInputMethodManager iInputMethodManager = this.mService;
            if (imi != null) {
                str = imi.getId();
            }
            return iInputMethodManager.getEnabledInputMethodSubtypeList(str, allowsImplicitlySelectedSubtypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void showStatusIcon(IBinder imeToken, String packageName, int iconId) {
        try {
            this.mService.updateStatusIcon(imeToken, packageName, iconId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void hideStatusIcon(IBinder imeToken) {
        try {
            this.mService.updateStatusIcon(imeToken, null, SHOW_IM_PICKER_MODE_AUTO);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setImeWindowStatus(IBinder imeToken, int vis, int backDisposition) {
        try {
            this.mService.setImeWindowStatus(imeToken, vis, backDisposition);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReportFullscreenMode(boolean fullScreen, boolean calledInBackground, String inputMethodId) {
        synchronized (this.mH) {
            if (calledInBackground) {
            }
            this.mFullscreenMode = fullScreen;
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

    public boolean isActive(View view) {
        boolean z = DEBUG;
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                z = true;
            }
        }
        return z;
    }

    public boolean isActive() {
        boolean z = DEBUG;
        checkFocus();
        synchronized (this.mH) {
            if (!(this.mServedView == null || this.mCurrentTextBoxAttribute == null)) {
                z = true;
            }
        }
        return z;
    }

    public boolean isAcceptingText() {
        checkFocus();
        if (this.mServedInputConnectionWrapper == null || this.mServedInputConnectionWrapper.getInputConnection() == null) {
            return DEBUG;
        }
        return true;
    }

    void clearBindingLocked() {
        clearConnectionLocked();
        setInputChannelLocked(null);
        this.mBindSequence = NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
        this.mCurId = null;
        this.mCurMethod = null;
    }

    void setInputChannelLocked(InputChannel channel) {
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

    void clearConnectionLocked() {
        this.mCurrentTextBoxAttribute = null;
        if (this.mServedInputConnectionWrapper != null) {
            this.mServedInputConnectionWrapper.deactivate();
            this.mServedInputConnectionWrapper = null;
        }
    }

    void finishInputLocked() {
        this.mNextServedView = null;
        this.mLastSrvView = null;
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
            this.mServedConnecting = DEBUG;
            clearConnectionLocked();
        }
    }

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
                return;
            }
        }
    }

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
                return;
            }
        }
    }

    public boolean showSoftInput(View view, int flags) {
        return showSoftInput(view, flags, null);
    }

    public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != view && (this.mServedView == null || !this.mServedView.checkInputConnectionProxy(view))) {
                return DEBUG;
            } else if (isSecImmEnabled()) {
                r1 = this.mSecImmHelper.showSoftInput(view, flags, resultReceiver, this.mClient);
                return r1;
            } else {
                try {
                    r1 = this.mService.showSoftInput(this.mClient, flags, resultReceiver);
                    return r1;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void showSoftInputUnchecked(int flags, ResultReceiver resultReceiver) {
        try {
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
            if (this.mServedView == null || this.mServedView.getWindowToken() != windowToken) {
                return DEBUG;
            } else if (isSecImmEnabled()) {
                r0 = this.mSecImmHelper.hideSoftInputFromWindow(windowToken, flags, resultReceiver, this.mServedView, this.mClient);
                return r0;
            } else {
                try {
                    r0 = this.mService.hideSoftInput(this.mClient, flags, resultReceiver);
                    return r0;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void toggleSoftInputFromWindow(IBinder windowToken, int showFlags, int hideFlags) {
        synchronized (this.mH) {
            if (this.mServedView == null || this.mServedView.getWindowToken() != windowToken) {
                return;
            }
            if (this.mCurMethod != null) {
                try {
                    this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
                } catch (RemoteException e) {
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

    public void restartInput(View view) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                this.mServedConnecting = true;
                startInputInner(RESULT_HIDDEN, null, SHOW_IM_PICKER_MODE_AUTO, SHOW_IM_PICKER_MODE_AUTO, SHOW_IM_PICKER_MODE_AUTO);
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean startInputInner(int startInputReason, IBinder windowGainingFocus, int controlFlags, int softInputMode, int windowFlags) {
        synchronized (this.mH) {
            View view = this.mServedView;
            if (view == null) {
                return DEBUG;
            }
            Handler vh = view.getHandler();
            if (vh == null) {
                closeCurrentInput();
                return DEBUG;
            } else if (vh.getLooper() != Looper.myLooper()) {
                vh.post(new AnonymousClass2(startInputReason));
                return DEBUG;
            } else {
                EditorInfo tba = new EditorInfo();
                tba.packageName = view.getContext().getOpPackageName();
                tba.fieldId = view.getId();
                InputConnection ic = view.onCreateInputConnection(tba);
                synchronized (this.mH) {
                    if (this.mServedView == view && this.mServedConnecting) {
                        int missingMethodFlags;
                        IInputContext controlledInputConnectionWrapper;
                        if (this.mCurrentTextBoxAttribute == null) {
                            controlFlags |= CONTROL_START_INITIAL;
                        }
                        this.mCurrentTextBoxAttribute = tba;
                        this.mServedConnecting = DEBUG;
                        if (this.mServedInputConnectionWrapper != null) {
                            this.mServedInputConnectionWrapper.deactivate();
                            this.mServedInputConnectionWrapper = null;
                        }
                        if (ic != null) {
                            Handler handler;
                            this.mCursorSelStart = tba.initialSelStart;
                            this.mCursorSelEnd = tba.initialSelEnd;
                            this.mCursorCandStart = NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
                            this.mCursorCandEnd = NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
                            this.mCursorRect.setEmpty();
                            this.mCursorAnchorInfo = null;
                            missingMethodFlags = InputConnectionInspector.getMissingMethodFlags(ic);
                            if ((missingMethodFlags & 32) != 0) {
                                handler = null;
                            } else {
                                handler = ic.getHandler();
                            }
                            controlledInputConnectionWrapper = new ControlledInputConnectionWrapper(handler != null ? handler.getLooper() : vh.getLooper(), ic, this);
                        } else {
                            controlledInputConnectionWrapper = null;
                            missingMethodFlags = SHOW_IM_PICKER_MODE_AUTO;
                        }
                        this.mServedInputConnectionWrapper = controlledInputConnectionWrapper;
                        try {
                            InputBindResult res = this.mService.startInputOrWindowGainedFocus(startInputReason, this.mClient, windowGainingFocus, controlFlags, softInputMode, windowFlags, tba, controlledInputConnectionWrapper, missingMethodFlags);
                            if (res != null) {
                                if (res.id != null) {
                                    setInputChannelLocked(res.channel);
                                    this.mBindSequence = res.sequence;
                                    this.mCurMethod = res.method;
                                    this.mCurId = res.id;
                                    this.mNextUserActionNotificationSequenceNumber = res.userActionNotificationSequenceNumber;
                                    if (this.mServedInputConnectionWrapper != null) {
                                        this.mServedInputConnectionWrapper.setInputMethodId(this.mCurId);
                                    }
                                } else {
                                    if (!(res.channel == null || res.channel == this.mCurChannel)) {
                                        res.channel.dispose();
                                    }
                                    if (this.mCurMethod == null) {
                                        return true;
                                    }
                                }
                            } else if (startInputReason == SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES) {
                                if (!this.mActive) {
                                    this.mHasBeenInactive = true;
                                }
                            }
                            if (!(this.mCurMethod == null || this.mCompletions == null)) {
                                this.mCurMethod.displayCompletions(this.mCompletions);
                            }
                        } catch (RemoteException e) {
                        } catch (IllegalArgumentException e2) {
                            Log.e(TAG, "Can not start input method, need check in Settings, a default language is needed!");
                        } catch (NullPointerException ex) {
                            Log.e(TAG, "startInputInner() has exception -> " + ex.getMessage());
                        }
                        return true;
                    }
                    return DEBUG;
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

    void focusInLocked(View view) {
        if ((view == null || !view.isTemporarilyDetached()) && this.mCurRootView == view.getRootView()) {
            this.mNextServedView = view;
            this.mInTransition = this.mNextServedView != this.mServedView ? true : DEBUG;
            this.mLastSrvView = this.mServedView;
            scheduleCheckFocusLocked(view);
        }
    }

    public void focusOut(View view) {
        synchronized (this.mH) {
            this.mLastSrvView = null;
            this.mInTransition = DEBUG;
            if (this.mServedView != view) {
            }
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
        if (checkFocusNoStartInput(DEBUG)) {
            startInputInner(MSG_SET_ACTIVE, null, SHOW_IM_PICKER_MODE_AUTO, SHOW_IM_PICKER_MODE_AUTO, SHOW_IM_PICKER_MODE_AUTO);
        }
    }

    private boolean checkFocusNoStartInput(boolean forceNewFocus) {
        if (this.mServedView == this.mNextServedView && !forceNewFocus) {
            return DEBUG;
        }
        synchronized (this.mH) {
            if (this.mServedView == this.mNextServedView && !forceNewFocus) {
                return DEBUG;
            } else if (this.mNextServedView == null) {
                finishInputLocked();
                closeCurrentInput();
                return DEBUG;
            } else {
                ControlledInputConnectionWrapper ic = this.mServedInputConnectionWrapper;
                this.mServedView = this.mNextServedView;
                this.mCurrentTextBoxAttribute = null;
                this.mCompletions = null;
                this.mServedConnecting = true;
                if (ic != null) {
                    ic.finishComposingText();
                }
                return true;
            }
        }
    }

    void closeCurrentInput() {
        try {
            this.mService.hideSoftInput(this.mClient, SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void onPostWindowFocus(View rootView, View focusedView, int softInputMode, boolean first, int windowFlags) {
        boolean forceNewFocus = DEBUG;
        synchronized (this.mH) {
            View view;
            if (this.mHasBeenInactive) {
                this.mHasBeenInactive = DEBUG;
                forceNewFocus = true;
            }
            if (focusedView != null) {
                view = focusedView;
            } else {
                view = rootView;
            }
            focusInLocked(view);
        }
        int controlFlags = SHOW_IM_PICKER_MODE_AUTO;
        if (focusedView != null) {
            controlFlags = SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES;
            if (focusedView.onCheckIsTextEditor()) {
                controlFlags = SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES | SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES;
            }
        }
        if (first) {
            controlFlags |= MSG_SET_ACTIVE;
        }
        if (!checkFocusNoStartInput(forceNewFocus) || !startInputInner(SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES, rootView.getWindowToken(), controlFlags, softInputMode, windowFlags)) {
            synchronized (this.mH) {
                try {
                    this.mService.startInputOrWindowGainedFocus(SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES, this.mClient, rootView.getWindowToken(), controlFlags, softInputMode, windowFlags, null, null, SHOW_IM_PICKER_MODE_AUTO);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void onPreWindowFocus(View rootView, boolean hasWindowFocus) {
        synchronized (this.mH) {
            if (rootView == null) {
                this.mCurRootView = null;
            }
            if (hasWindowFocus) {
                this.mCurRootView = rootView;
            } else if (rootView == this.mCurRootView) {
                this.mCurRootView = null;
            }
        }
    }

    public void updateSelection(View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    if (this.mCursorSelStart == selStart && this.mCursorSelEnd == selEnd) {
                        if (this.mCursorCandStart == candidatesStart) {
                            if (this.mCursorCandEnd != candidatesEnd) {
                            }
                            return;
                        }
                    }
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
                    return;
                }
            }
        }
    }

    public void viewClicked(View view) {
        boolean focusChanged = this.mServedView != this.mNextServedView ? true : DEBUG;
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.viewClicked(focusChanged);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                    return;
                }
            }
        }
    }

    public boolean isSecImmEnabled() {
        if (this.mSecImmHelper == null) {
            return DEBUG;
        }
        return this.mSecImmHelper.isUseSecureIME();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isViewInTransition(View view) {
        boolean ret = this.mInTransition ? (view == this.mServedView || view == this.mNextServedView) ? true : view == this.mLastSrvView ? true : DEBUG : DEBUG;
        if (!ret || this.mServedView == null || this.mNextServedView == null || this.mLastSrvView == null || !this.mServedView.onCheckIsTextEditor() || !this.mNextServedView.onCheckIsTextEditor() || !this.mLastSrvView.onCheckIsTextEditor() || view == this.mServedView || !isSecImmEnabled()) {
            return DEBUG;
        }
        boolean isPwdType = this.mSecImmHelper.isPasswordInputType(this.mServedView);
        ret = ret ? isPwdType == this.mSecImmHelper.isPasswordInputType(this.mNextServedView) ? isPwdType != this.mSecImmHelper.isPasswordInputType(this.mLastSrvView) ? true : DEBUG : true : DEBUG;
        if (ret) {
            Log.i(TAG, "isViewInTransition is true !");
        }
        return ret;
    }

    public void resetInTransitionState() {
        this.mInTransition = DEBUG;
        this.mLastSrvView = null;
    }

    @Deprecated
    public boolean isWatchingCursor(View view) {
        return DEBUG;
    }

    public boolean isCursorAnchorInfoEnabled() {
        boolean z;
        synchronized (this.mH) {
            boolean isImmediate = (this.mRequestUpdateCursorAnchorInfoMonitorMode & SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES) != 0 ? true : DEBUG;
            z = (this.mRequestUpdateCursorAnchorInfoMonitorMode & SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES) != 0 ? true : DEBUG;
            if (isImmediate) {
                z = true;
            }
        }
        return z;
    }

    public void setUpdateCursorAnchorInfoMode(int flags) {
        synchronized (this.mH) {
            this.mRequestUpdateCursorAnchorInfoMonitorMode = flags;
        }
    }

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
                    return;
                }
            }
        }
    }

    public void updateCursorAnchorInfo(View view, CursorAnchorInfo cursorAnchorInfo) {
        boolean isImmediate = DEBUG;
        if (view != null && cursorAnchorInfo != null) {
            checkFocus();
            synchronized (this.mH) {
                if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                    if (this.mCurMethod != null) {
                        if ((this.mRequestUpdateCursorAnchorInfoMonitorMode & SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES) != 0) {
                            isImmediate = true;
                        }
                        if (isImmediate || !Objects.equals(this.mCursorAnchorInfo, cursorAnchorInfo)) {
                            try {
                                this.mCurMethod.updateCursorAnchorInfo(cursorAnchorInfo);
                                this.mCursorAnchorInfo = cursorAnchorInfo;
                                this.mRequestUpdateCursorAnchorInfoMonitorMode &= -2;
                            } catch (RemoteException e) {
                                Log.w(TAG, "IME died: " + this.mCurId, e);
                            }
                            return;
                        }
                        return;
                    }
                }
            }
        }
    }

    public void sendAppPrivateCommand(View view, String action, Bundle data) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null) {
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.appPrivateCommand(action, data);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                    return;
                }
            }
        }
    }

    public void setInputMethod(IBinder token, String id) {
        try {
            this.mService.setInputMethod(token, id);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        try {
            this.mService.setInputMethodAndSubtype(token, id, subtype);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void hideSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            this.mService.hideMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void showSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            this.mService.showMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int dispatchInputEvent(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
        synchronized (this.mH) {
            if (this.mCurMethod != null) {
                if (event instanceof KeyEvent) {
                    KeyEvent keyEvent = (KeyEvent) event;
                    if (keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 63 && keyEvent.getRepeatCount() == 0) {
                        showInputMethodPickerLocked();
                        return SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES;
                    }
                }
                PendingEvent p = obtainPendingEventLocked(event, token, this.mCurId, callback, handler);
                if (this.mMainLooper.isCurrentThread()) {
                    int sendInputEventOnMainLooperLocked = sendInputEventOnMainLooperLocked(p);
                    return sendInputEventOnMainLooperLocked;
                }
                Message msg = this.mH.obtainMessage(MSG_SEND_INPUT_EVENT, p);
                msg.setAsynchronous(true);
                this.mH.sendMessage(msg);
                return NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
            }
            return SHOW_IM_PICKER_MODE_AUTO;
        }
    }

    public void dispatchKeyEventFromInputMethod(View targetView, KeyEvent event) {
        synchronized (this.mH) {
            ViewRootImpl viewRootImpl = targetView != null ? targetView.getViewRootImpl() : null;
            if (viewRootImpl == null && this.mServedView != null) {
                viewRootImpl = this.mServedView.getViewRootImpl();
            }
            if (viewRootImpl != null) {
                viewRootImpl.dispatchKeyFromIme(event);
            }
        }
    }

    void sendInputEventAndReportResultOnMainLooper(PendingEvent p) {
        synchronized (this.mH) {
            int result = sendInputEventOnMainLooperLocked(p);
            if (result == NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER) {
                return;
            }
            boolean handled = result == SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES ? true : DEBUG;
            invokeFinishedInputEventCallback(p, handled);
        }
    }

    int sendInputEventOnMainLooperLocked(PendingEvent p) {
        if (this.mCurChannel != null) {
            if (this.mCurSender == null) {
                this.mCurSender = new ImeInputEventSender(this.mCurChannel, this.mH.getLooper());
            }
            InputEvent event = p.mEvent;
            int seq = event.getSequenceNumber();
            if (this.mCurSender.sendInputEvent(seq, event)) {
                this.mPendingEvents.put(seq, p);
                Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
                Message msg = this.mH.obtainMessage(MSG_TIMEOUT_INPUT_EVENT, p);
                msg.setAsynchronous(true);
                this.mH.sendMessageDelayed(msg, INPUT_METHOD_NOT_RESPONDING_TIMEOUT);
                return NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER;
            }
            Log.w(TAG, "Unable to send input event to IME: " + this.mCurId + " dropping: " + event);
        }
        return SHOW_IM_PICKER_MODE_AUTO;
    }

    void finishedInputEvent(int seq, boolean handled, boolean timeout) {
        synchronized (this.mH) {
            int index = this.mPendingEvents.indexOfKey(seq);
            if (index < 0) {
                return;
            }
            PendingEvent p = (PendingEvent) this.mPendingEvents.valueAt(index);
            this.mPendingEvents.removeAt(index);
            Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
            if (timeout) {
                Log.w(TAG, "Timeout waiting for IME to handle input event after 2500 ms: " + p.mInputMethodId);
            } else {
                this.mH.removeMessages(MSG_TIMEOUT_INPUT_EVENT, p);
            }
            invokeFinishedInputEventCallback(p, handled);
        }
    }

    void invokeFinishedInputEventCallback(PendingEvent p, boolean handled) {
        p.mHandled = handled;
        if (p.mHandler.getLooper().isCurrentThread()) {
            p.run();
            return;
        }
        Message msg = Message.obtain(p.mHandler, p);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void flushPendingEventsLocked() {
        this.mH.removeMessages(MSG_FLUSH_INPUT_EVENT);
        int count = this.mPendingEvents.size();
        for (int i = SHOW_IM_PICKER_MODE_AUTO; i < count; i += SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES) {
            Message msg = this.mH.obtainMessage(MSG_FLUSH_INPUT_EVENT, this.mPendingEvents.keyAt(i), SHOW_IM_PICKER_MODE_AUTO);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }
    }

    private PendingEvent obtainPendingEventLocked(InputEvent event, Object token, String inputMethodId, FinishedInputEventCallback callback, Handler handler) {
        PendingEvent p = (PendingEvent) this.mPendingEventPool.acquire();
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

    private void recyclePendingEventLocked(PendingEvent p) {
        p.recycle();
        this.mPendingEventPool.release(p);
    }

    public void showInputMethodPicker() {
        synchronized (this.mH) {
            showInputMethodPickerLocked();
        }
    }

    public void showInputMethodPicker(boolean showAuxiliarySubtypes) {
        synchronized (this.mH) {
            int mode;
            if (showAuxiliarySubtypes) {
                mode = SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES;
            } else {
                mode = SHOW_IM_PICKER_MODE_EXCLUDE_AUXILIARY_SUBTYPES;
            }
            try {
                this.mService.showInputMethodPickerFromClient(this.mClient, mode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private void showInputMethodPickerLocked() {
        try {
            this.mService.showInputMethodPickerFromClient(this.mClient, SHOW_IM_PICKER_MODE_AUTO);
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
            }
        }
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        InputMethodSubtype currentInputMethodSubtype;
        synchronized (this.mH) {
            try {
                currentInputMethodSubtype = this.mService.getCurrentInputMethodSubtype();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return currentInputMethodSubtype;
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        boolean currentInputMethodSubtype;
        synchronized (this.mH) {
            try {
                currentInputMethodSubtype = this.mService.setCurrentInputMethodSubtype(subtype);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return currentInputMethodSubtype;
    }

    public void notifyUserAction() {
        synchronized (this.mH) {
            if (this.mLastSentUserActionNotificationSequenceNumber == this.mNextUserActionNotificationSequenceNumber) {
                return;
            }
            try {
                this.mService.notifyUserAction(this.mNextUserActionNotificationSequenceNumber);
                this.mLastSentUserActionNotificationSequenceNumber = this.mNextUserActionNotificationSequenceNumber;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public Map<InputMethodInfo, List<InputMethodSubtype>> getShortcutInputMethodsAndSubtypes() {
        HashMap<InputMethodInfo, List<InputMethodSubtype>> ret;
        synchronized (this.mH) {
            ret = new HashMap();
            try {
                List<Object> info = this.mService.getShortcutInputMethodsAndSubtypes();
                ArrayList subtypes = null;
                if (!(info == null || info.isEmpty())) {
                    int N = info.size();
                    for (int i = SHOW_IM_PICKER_MODE_AUTO; i < N; i += SHOW_IM_PICKER_MODE_INCLUDE_AUXILIARY_SUBTYPES) {
                        Object o = info.get(i);
                        if (o instanceof InputMethodInfo) {
                            if (ret.containsKey(o)) {
                                Log.e(TAG, "IMI list already contains the same InputMethod.");
                                break;
                            }
                            subtypes = new ArrayList();
                            ret.put((InputMethodInfo) o, subtypes);
                        } else if (subtypes != null && (o instanceof InputMethodSubtype)) {
                            subtypes.add((InputMethodSubtype) o);
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
            }
        }
    }

    public boolean switchToLastInputMethod(IBinder imeToken) {
        boolean switchToLastInputMethod;
        synchronized (this.mH) {
            try {
                switchToLastInputMethod = this.mService.switchToLastInputMethod(imeToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return switchToLastInputMethod;
    }

    public boolean switchToNextInputMethod(IBinder imeToken, boolean onlyCurrentIme) {
        boolean switchToNextInputMethod;
        synchronized (this.mH) {
            try {
                switchToNextInputMethod = this.mService.switchToNextInputMethod(imeToken, onlyCurrentIme);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return switchToNextInputMethod;
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder imeToken) {
        boolean shouldOfferSwitchingToNextInputMethod;
        synchronized (this.mH) {
            try {
                shouldOfferSwitchingToNextInputMethod = this.mService.shouldOfferSwitchingToNextInputMethod(imeToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
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
            }
        }
        return lastInputMethodSubtype;
    }

    void doDump(FileDescriptor fd, PrintWriter fout, String[] args) {
        Printer p = new PrintWriterPrinter(fout);
        p.println("Input method client state for " + this + ":");
        p.println("  mService=" + this.mService);
        p.println("  mMainLooper=" + this.mMainLooper);
        p.println("  mIInputContext=" + this.mIInputContext);
        p.println("  mActive=" + this.mActive + " mHasBeenInactive=" + this.mHasBeenInactive + " mBindSequence=" + this.mBindSequence + " mCurId=" + this.mCurId);
        p.println("  mCurMethod=" + this.mCurMethod);
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
        p.println("  mNextUserActionNotificationSequenceNumber=" + this.mNextUserActionNotificationSequenceNumber + " mLastSentUserActionNotificationSequenceNumber=" + this.mLastSentUserActionNotificationSequenceNumber);
    }

    private static String dumpViewInfo(View view) {
        if (view == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(view);
        sb.append(",focus=").append(view.hasFocus());
        sb.append(",windowFocus=").append(view.hasWindowFocus());
        sb.append(",window=").append(view.getWindowToken());
        sb.append(",temporaryDetach=").append(view.isTemporarilyDetached());
        return sb.toString();
    }
}
