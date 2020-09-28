package android.view.inputmethod;

import android.Manifest;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.text.style.SuggestionSpan;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pools;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.Display;
import android.view.ImeInsetsSourceConsumer;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.autofill.AutofillManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.inputmethod.InputMethodPrivilegedOperationsRegistry;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputConnectionWrapper;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class InputMethodManager {
    public static final int CONTROL_SHOW_INPUT = 65536;
    static final boolean DEBUG = false;
    public static final int DISPATCH_HANDLED = 1;
    public static final int DISPATCH_IN_PROGRESS = -1;
    public static final int DISPATCH_NOT_HANDLED = 0;
    public static final int HIDE_IMPLICIT_ONLY = 1;
    public static final int HIDE_NOT_ALWAYS = 2;
    static final long INPUT_METHOD_NOT_RESPONDING_TIMEOUT = 2500;
    static final int MSG_APPLY_IME_VISIBILITY = 20;
    static final int MSG_BIND = 2;
    static final int MSG_DUMP = 1;
    static final int MSG_FLUSH_INPUT_EVENT = 7;
    static final int MSG_REPORT_FULLSCREEN_MODE = 10;
    static final int MSG_REPORT_PRE_RENDERED = 15;
    static final int MSG_SEND_INPUT_EVENT = 5;
    static final int MSG_SET_ACTIVE = 4;
    static final int MSG_TIMEOUT_INPUT_EVENT = 6;
    static final int MSG_UNBIND = 3;
    static final int MSG_UPDATE_ACTIVITY_VIEW_TO_SCREEN_MATRIX = 30;
    private static final int NOT_A_SUBTYPE_ID = -1;
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
    private static final String SUBTYPE_MODE_VOICE = "voice";
    static final String TAG = "InputMethodManager";
    @UnsupportedAppUsage
    @GuardedBy({"sLock"})
    @Deprecated
    static InputMethodManager sInstance;
    @GuardedBy({"sLock"})
    private static final SparseArray<InputMethodManager> sInstanceMap = new SparseArray<>();
    private static final Object sLock = new Object();
    boolean mActive = false;
    private Matrix mActivityViewToScreenMatrix = null;
    int mBindSequence = -1;
    final IInputMethodClient.Stub mClient = new IInputMethodClient.Stub() {
        /* class android.view.inputmethod.InputMethodManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
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

        @Override // com.android.internal.view.IInputMethodClient
        public void onBindMethod(InputBindResult res) {
            InputMethodManager.this.mH.obtainMessage(2, res).sendToTarget();
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void onUnbindMethod(int sequence, int unbindReason) {
            InputMethodManager.this.mH.obtainMessage(3, sequence, unbindReason).sendToTarget();
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void setActive(boolean active, boolean fullscreen) {
            InputMethodManager.this.mH.obtainMessage(4, active ? 1 : 0, fullscreen ? 1 : 0).sendToTarget();
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void reportFullscreenMode(boolean fullscreen) {
            InputMethodManager.this.mH.obtainMessage(10, fullscreen ? 1 : 0, 0).sendToTarget();
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void reportPreRendered(EditorInfo info) {
            InputMethodManager.this.mH.obtainMessage(15, 0, 0, info).sendToTarget();
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void applyImeVisibility(boolean setVisible) {
            InputMethodManager.this.mH.obtainMessage(20, setVisible ? 1 : 0, 0).sendToTarget();
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void updateActivityViewToScreenMatrix(int bindSequence, float[] matrixValues) {
            InputMethodManager.this.mH.obtainMessage(30, bindSequence, 0, matrixValues).sendToTarget();
        }
    };
    CompletionInfo[] mCompletions;
    InputChannel mCurChannel;
    @UnsupportedAppUsage
    String mCurId;
    @UnsupportedAppUsage
    IInputMethodSession mCurMethod;
    @UnsupportedAppUsage
    View mCurRootView;
    ImeInputEventSender mCurSender;
    EditorInfo mCurrentTextBoxAttribute;
    private CursorAnchorInfo mCursorAnchorInfo = null;
    int mCursorCandEnd;
    int mCursorCandStart;
    @UnsupportedAppUsage
    Rect mCursorRect = new Rect();
    int mCursorSelEnd;
    int mCursorSelStart;
    private final int mDisplayId;
    final InputConnection mDummyInputConnection = new BaseInputConnection(this, false);
    boolean mFullscreenMode;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    final H mH;
    final IInputContext mIInputContext;
    private ImeInsetsSourceConsumer mImeInsetsConsumer;
    final Looper mMainLooper;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    View mNextServedView;
    final Pools.Pool<PendingEvent> mPendingEventPool = new Pools.SimplePool(20);
    final SparseArray<PendingEvent> mPendingEvents = new SparseArray<>(20);
    private int mRequestUpdateCursorAnchorInfoMonitorMode = 0;
    boolean mRestartOnNextWindowFocus = true;
    boolean mServedConnecting;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    ControlledInputConnectionWrapper mServedInputConnectionWrapper;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    View mServedView;
    @UnsupportedAppUsage
    final IInputMethodManager mService;
    @UnsupportedAppUsage
    Rect mTmpCursorRect = new Rect();

    public interface FinishedInputEventCallback {
        void onFinishedInputEvent(Object obj, boolean z);
    }

    public static void ensureDefaultInstanceForDefaultDisplayIfNecessary() {
        forContextInternal(0, Looper.getMainLooper());
    }

    private static boolean isAutofillUIShowing(View servedView) {
        AutofillManager afm = (AutofillManager) servedView.getContext().getSystemService(AutofillManager.class);
        return afm != null && afm.isAutofillUiShowing();
    }

    private InputMethodManager getFallbackInputMethodManagerIfNecessary(View view) {
        ViewRootImpl viewRootImpl;
        int viewRootDisplayId;
        if (view == null || (viewRootImpl = view.getViewRootImpl()) == null || (viewRootDisplayId = viewRootImpl.getDisplayId()) == this.mDisplayId) {
            return null;
        }
        InputMethodManager fallbackImm = (InputMethodManager) viewRootImpl.mContext.getSystemService(InputMethodManager.class);
        if (fallbackImm == null) {
            Log.e(TAG, "b/117267690: Failed to get non-null fallback IMM. view=" + view);
            return null;
        } else if (fallbackImm.mDisplayId != viewRootDisplayId) {
            Log.e(TAG, "b/117267690: Failed to get fallback IMM with expected displayId=" + viewRootDisplayId + " actual IMM#displayId=" + fallbackImm.mDisplayId + " view=" + view);
            return null;
        } else {
            Log.w(TAG, "b/117267690: Display ID mismatch found. ViewRootImpl displayId=" + viewRootDisplayId + " InputMethodManager displayId=" + this.mDisplayId + ". Use the right InputMethodManager instance to avoid performance overhead.", new Throwable());
            return fallbackImm;
        }
    }

    /* access modifiers changed from: private */
    public static boolean canStartInput(View servedView) {
        return servedView.hasWindowFocus() || isAutofillUIShowing(servedView);
    }

    /* access modifiers changed from: package-private */
    public class H extends Handler {
        H(Looper looper) {
            super(looper, null, true);
        }

        /* JADX INFO: Multiple debug info for r0v13 int: [D('active' boolean), D('sequence' int)] */
        /* JADX WARNING: Code restructure failed: missing block: B:184:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:185:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x00bd, code lost:
            if (r2 == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x00bf, code lost:
            r11.this$0.startInputInner(6, null, 0, 0, 0);
         */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int reason;
            int i = msg.what;
            boolean isMonitoring = true;
            boolean fullscreen = false;
            if (i == 10) {
                if (msg.arg1 == 0) {
                    isMonitoring = false;
                }
                InputConnection ic = null;
                synchronized (InputMethodManager.this.mH) {
                    InputMethodManager.this.mFullscreenMode = isMonitoring;
                    if (InputMethodManager.this.mServedInputConnectionWrapper != null) {
                        ic = InputMethodManager.this.mServedInputConnectionWrapper.getInputConnection();
                    }
                }
                if (ic != null) {
                    ic.reportFullscreenMode(isMonitoring);
                }
            } else if (i == 15) {
                synchronized (InputMethodManager.this.mH) {
                    if (InputMethodManager.this.mImeInsetsConsumer != null) {
                        InputMethodManager.this.mImeInsetsConsumer.onPreRendered((EditorInfo) msg.obj);
                    }
                }
            } else if (i == 20) {
                synchronized (InputMethodManager.this.mH) {
                    if (InputMethodManager.this.mImeInsetsConsumer != null) {
                        ImeInsetsSourceConsumer imeInsetsSourceConsumer = InputMethodManager.this.mImeInsetsConsumer;
                        if (msg.arg1 == 0) {
                            isMonitoring = false;
                        }
                        imeInsetsSourceConsumer.applyImeVisibility(isMonitoring);
                    }
                }
            } else if (i != 30) {
                switch (i) {
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
                                    InputMethodManager.this.mRequestUpdateCursorAnchorInfoMonitorMode = 0;
                                    InputMethodManager.this.setInputChannelLocked(res.channel);
                                    InputMethodManager.this.mCurMethod = res.method;
                                    InputMethodManager.this.mCurId = res.id;
                                    InputMethodManager.this.mBindSequence = res.sequence;
                                    InputMethodManager.this.mActivityViewToScreenMatrix = res.getActivityViewToScreenMatrix();
                                    InputMethodManager.this.startInputInner(5, null, 0, 0, 0);
                                    return;
                                }
                            }
                            Log.w(InputMethodManager.TAG, "Ignoring onBind: cur seq=" + InputMethodManager.this.mBindSequence + ", given seq=" + res.sequence);
                            if (!(res.channel == null || res.channel == InputMethodManager.this.mCurChannel)) {
                                res.channel.dispose();
                            }
                            return;
                        }
                    case 3:
                        int sequence = msg.arg1;
                        int i2 = msg.arg2;
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
                        InputMethodManager.this.finishedInputEvent(msg.arg1, false, true);
                        return;
                    case 7:
                        InputMethodManager.this.finishedInputEvent(msg.arg1, false, false);
                        return;
                    default:
                        return;
                }
            } else {
                float[] matrixValues = (float[]) msg.obj;
                int bindSequence = msg.arg1;
                synchronized (InputMethodManager.this.mH) {
                    if (InputMethodManager.this.mBindSequence == bindSequence) {
                        if (matrixValues == null) {
                            InputMethodManager.this.mActivityViewToScreenMatrix = null;
                            return;
                        }
                        float[] currentValues = new float[9];
                        InputMethodManager.this.mActivityViewToScreenMatrix.getValues(currentValues);
                        if (!Arrays.equals(currentValues, matrixValues)) {
                            InputMethodManager.this.mActivityViewToScreenMatrix.setValues(matrixValues);
                            if (!(InputMethodManager.this.mCursorAnchorInfo == null || InputMethodManager.this.mCurMethod == null)) {
                                if (InputMethodManager.this.mServedInputConnectionWrapper != null) {
                                    if ((InputMethodManager.this.mRequestUpdateCursorAnchorInfoMonitorMode & 2) == 0) {
                                        isMonitoring = false;
                                    }
                                    if (isMonitoring) {
                                        try {
                                            InputMethodManager.this.mCurMethod.updateCursorAnchorInfo(CursorAnchorInfo.createForAdditionalParentMatrix(InputMethodManager.this.mCursorAnchorInfo, InputMethodManager.this.mActivityViewToScreenMatrix));
                                        } catch (RemoteException e3) {
                                            Log.w(InputMethodManager.TAG, "IME died: " + InputMethodManager.this.mCurId, e3);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ControlledInputConnectionWrapper extends IInputConnectionWrapper {
        private final InputMethodManager mParentInputMethodManager;

        public ControlledInputConnectionWrapper(Looper mainLooper, InputConnection conn, InputMethodManager inputMethodManager) {
            super(mainLooper, conn);
            this.mParentInputMethodManager = inputMethodManager;
        }

        @Override // com.android.internal.view.IInputConnectionWrapper
        public boolean isActive() {
            return this.mParentInputMethodManager.mActive && !isFinished();
        }

        /* access modifiers changed from: package-private */
        public void deactivate() {
            if (!isFinished()) {
                closeConnection();
            }
        }

        public String toString() {
            return "ControlledInputConnectionWrapper{connection=" + getInputConnection() + " finished=" + isFinished() + " mParentInputMethodManager.mActive=" + this.mParentInputMethodManager.mActive + "}";
        }
    }

    static void tearDownEditMode() {
        if (isInEditMode()) {
            synchronized (sLock) {
                sInstance = null;
            }
            return;
        }
        throw new UnsupportedOperationException("This method must be called only from layoutlib");
    }

    private static boolean isInEditMode() {
        return false;
    }

    private static InputMethodManager createInstance(int displayId, Looper looper) {
        if (isInEditMode()) {
            return createStubInstance(displayId, looper);
        }
        return createRealInstance(displayId, looper);
    }

    private static InputMethodManager createRealInstance(int displayId, Looper looper) {
        try {
            IInputMethodManager service = IInputMethodManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.INPUT_METHOD_SERVICE));
            InputMethodManager imm = new InputMethodManager(service, displayId, looper);
            long identity = Binder.clearCallingIdentity();
            try {
                service.addClient(imm.mClient, imm.mIInputContext, displayId);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
            return imm;
        } catch (ServiceManager.ServiceNotFoundException e2) {
            throw new IllegalStateException(e2);
        }
    }

    private static InputMethodManager createStubInstance(int displayId, Looper looper) {
        return new InputMethodManager((IInputMethodManager) Proxy.newProxyInstance(IInputMethodManager.class.getClassLoader(), new Class[]{IInputMethodManager.class}, $$Lambda$InputMethodManager$iDWn3IGSUFqIcs8Py42UhfrshxI.INSTANCE), displayId, looper);
    }

    static /* synthetic */ Object lambda$createStubInstance$0(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        if (returnType == Boolean.TYPE) {
            return false;
        }
        if (returnType == Integer.TYPE) {
            return 0;
        }
        if (returnType == Long.TYPE) {
            return 0L;
        }
        if (returnType == Short.TYPE || returnType == Character.TYPE || returnType == Byte.TYPE) {
            return 0;
        }
        if (returnType == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (returnType == Double.TYPE) {
            return Double.valueOf(0.0d);
        }
        return null;
    }

    private InputMethodManager(IInputMethodManager service, int displayId, Looper looper) {
        this.mService = service;
        this.mMainLooper = looper;
        this.mH = new H(looper);
        this.mDisplayId = displayId;
        this.mIInputContext = new ControlledInputConnectionWrapper(looper, this.mDummyInputConnection, this);
    }

    public static InputMethodManager forContext(Context context) {
        int pcDisplayId;
        int displayId = context.getDisplayId();
        if (HwPCUtils.enabledInPad() && HwPCUtils.PKG_PHONE_SYSTEMUI.equals(context.getPackageName()) && displayId == 0 && HwPCUtils.isPcCastMode()) {
            try {
                pcDisplayId = Integer.parseInt(SystemProperties.get("hw.pc.casting.displayid", "-1"));
            } catch (NumberFormatException e) {
                pcDisplayId = -1;
                Log.e(TAG, "forContext has exception: " + e.getMessage());
            }
            if (pcDisplayId > 0) {
                displayId = pcDisplayId;
                try {
                    Display targetDisplay = ((DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE)).getDisplay(displayId);
                    if (targetDisplay == null) {
                        Log.e(TAG, "forContext targetDisplay is null");
                    } else {
                        context = context.createDisplayContext(targetDisplay);
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "forContext: " + e2.getMessage());
                }
            }
        }
        return forContextInternal(displayId, displayId == 0 ? Looper.getMainLooper() : context.getMainLooper());
    }

    private static InputMethodManager forContextInternal(int displayId, Looper looper) {
        boolean isDefaultDisplay = displayId == 0;
        synchronized (sLock) {
            InputMethodManager instance = sInstanceMap.get(displayId);
            if (instance != null) {
                return instance;
            }
            InputMethodManager instance2 = createInstance(displayId, looper);
            if (sInstance == null && isDefaultDisplay) {
                sInstance = instance2;
            }
            sInstanceMap.put(displayId, instance2);
            return instance2;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public static InputMethodManager getInstance() {
        Log.w(TAG, "InputMethodManager.getInstance() is deprecated because it cannot be compatible with multi-display. Use context.getSystemService(InputMethodManager.class) instead.", new Throwable());
        ensureDefaultInstanceForDefaultDisplayIfNecessary();
        return peekInstance();
    }

    @UnsupportedAppUsage
    @Deprecated
    public static InputMethodManager peekInstance() {
        InputMethodManager inputMethodManager;
        Log.w(TAG, "InputMethodManager.peekInstance() is deprecated because it cannot be compatible with multi-display. Use context.getSystemService(InputMethodManager.class) instead.", new Throwable());
        synchronized (sLock) {
            inputMethodManager = sInstance;
        }
        return inputMethodManager;
    }

    @UnsupportedAppUsage
    public IInputMethodClient getClient() {
        return this.mClient;
    }

    @UnsupportedAppUsage
    public IInputContext getInputContext() {
        return this.mIInputContext;
    }

    public List<InputMethodInfo> getInputMethodList() {
        try {
            return this.mService.getInputMethodList(UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodInfo> getInputMethodListAsUser(int userId) {
        try {
            return this.mService.getInputMethodList(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        try {
            return this.mService.getEnabledInputMethodList(UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodListAsUser(int userId) {
        try {
            return this.mService.getEnabledInputMethodList(userId);
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
        InputMethodPrivilegedOperationsRegistry.get(imeToken).updateStatusIcon(packageName, iconId);
    }

    @Deprecated
    public void hideStatusIcon(IBinder imeToken) {
        InputMethodPrivilegedOperationsRegistry.get(imeToken).updateStatusIcon(null, 0);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        Log.w(TAG, "registerSuggestionSpansForNotification() is deprecated.  Does nothing.");
    }

    @UnsupportedAppUsage
    @Deprecated
    public void notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        Log.w(TAG, "notifySuggestionPicked() is deprecated.  Does nothing.");
    }

    public boolean isFullscreenMode() {
        boolean z;
        synchronized (this.mH) {
            z = this.mFullscreenMode;
        }
        return z;
    }

    public boolean isActive(View view) {
        boolean z;
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            return fallbackImm.isActive(view);
        }
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
        ControlledInputConnectionWrapper controlledInputConnectionWrapper = this.mServedInputConnectionWrapper;
        return (controlledInputConnectionWrapper == null || controlledInputConnectionWrapper.getInputConnection() == null) ? false : true;
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
            InputChannel inputChannel = this.mCurChannel;
            if (inputChannel != null) {
                inputChannel.dispose();
            }
            this.mCurChannel = channel;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearConnectionLocked() {
        this.mCurrentTextBoxAttribute = null;
        ControlledInputConnectionWrapper controlledInputConnectionWrapper = this.mServedInputConnectionWrapper;
        if (controlledInputConnectionWrapper != null) {
            controlledInputConnectionWrapper.deactivate();
            this.mServedInputConnectionWrapper = null;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void finishInputLocked() {
        this.mNextServedView = null;
        this.mActivityViewToScreenMatrix = null;
        if (this.mServedView != null) {
            this.mServedView = null;
            this.mCompletions = null;
            this.mServedConnecting = false;
            clearConnectionLocked();
        }
    }

    public void displayCompletions(View view, CompletionInfo[] completions) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.displayCompletions(view, completions);
            return;
        }
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

    public void updateExtractedText(View view, int token, ExtractedText text) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.updateExtractedText(view, token, text);
            return;
        }
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
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            return fallbackImm.showSoftInput(view, flags);
        }
        return showSoftInput(view, flags, null);
    }

    public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            return fallbackImm.showSoftInput(view, flags, resultReceiver);
        }
        checkFocus();
        synchronized (this.mH) {
            Log.i(TAG, "showSoftInput");
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                try {
                    return this.mService.showSoftInput(this.mClient, flags, resultReceiver);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } else {
                Log.w(TAG, "The current service view is not the focus view");
                return false;
            }
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768499)
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
            if (this.mServedView == null || this.mServedView.getWindowToken() != windowToken) {
                return false;
            }
            try {
                return this.mService.hideSoftInput(this.mClient, flags, resultReceiver);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

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
        IInputMethodSession iInputMethodSession = this.mCurMethod;
        if (iInputMethodSession != null) {
            try {
                iInputMethodSession.toggleSoftInput(showFlags, hideFlags);
            } catch (RemoteException e) {
            }
        }
    }

    public void restartInput(View view) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.restartInput(view);
            return;
        }
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                this.mServedConnecting = true;
                startInputInner(3, null, 0, 0, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01fc, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01fd, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x01fe, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0205, code lost:
        monitor-exit(r22);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0206, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0207, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        if (r2 != null) goto L_0x0020;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        android.util.Log.e(android.view.inputmethod.InputMethodManager.TAG, "ABORT input: ServedView must be attached to a Window");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r3 = r28 | 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        if (r0.onCheckIsTextEditor() == false) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        r3 = r3 | 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        r15 = r2;
        r16 = r0.getViewRootImpl().mWindowAttributes.softInputMode;
        r17 = r0.getViewRootImpl().mWindowAttributes.flags;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
        r15 = r27;
        r3 = r28;
        r16 = r29;
        r17 = r30;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0048, code lost:
        android.util.Log.w(android.view.inputmethod.InputMethodManager.TAG, "startInputReason = " + r26);
        r12 = r0.getHandler();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0063, code lost:
        if (r12 != null) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0065, code lost:
        closeCurrentInput();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0068, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0071, code lost:
        if (r12.getLooper() == android.os.Looper.myLooper()) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0073, code lost:
        r12.post(new android.view.inputmethod.$$Lambda$InputMethodManager$dfnCauFoZCfHfXs1QavrkwWDf0(r25, r26));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007b, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007c, code lost:
        r2 = new android.view.inputmethod.EditorInfo();
        r2.packageName = r0.getContext().getOpPackageName();
        r2.fieldId = r0.getId();
        r10 = r0.onCreateInputConnection(r2);
        r9 = r25.mH;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0098, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009b, code lost:
        if (r25.mServedView != r0) goto L_0x01f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009f, code lost:
        if (r25.mServedConnecting != false) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a1, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ac, code lost:
        if (r25.mCurrentTextBoxAttribute != null) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ae, code lost:
        r18 = r3 | 8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b3, code lost:
        r18 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r25.mCurrentTextBoxAttribute = r2;
        maybeCallServedViewChangedLocked(r2);
        r25.mServedConnecting = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00bf, code lost:
        if (r25.mServedInputConnectionWrapper == null) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r25.mServedInputConnectionWrapper.deactivate();
        r25.mServedInputConnectionWrapper = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ca, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d5, code lost:
        if (r10 == null) goto L_0x010e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d7, code lost:
        r25.mCursorSelStart = r2.initialSelStart;
        r25.mCursorSelEnd = r2.initialSelEnd;
        r25.mCursorCandStart = -1;
        r25.mCursorCandEnd = -1;
        r25.mCursorRect.setEmpty();
        r25.mCursorAnchorInfo = null;
        r2 = android.view.inputmethod.InputConnectionInspector.getMissingMethodFlags(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00f1, code lost:
        if ((r2 & 32) == 0) goto L_0x00f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f3, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f5, code lost:
        r3 = r10.getHandler();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00fb, code lost:
        if (r3 == null) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00fd, code lost:
        r5 = r3.getLooper();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0102, code lost:
        r5 = r12.getLooper();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0109, code lost:
        r19 = r2;
        r8 = new android.view.inputmethod.InputMethodManager.ControlledInputConnectionWrapper(r5, r10, r25);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x010e, code lost:
        r8 = null;
        r19 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0113, code lost:
        r25.mServedInputConnectionWrapper = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0124, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r2 = r25.mService.startInputOrWindowGainedFocus(r26, r25.mClient, r15, r18, r16, r17, r2, r8, r19, r0.getContext().getApplicationInfo().targetSdkVersion);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0144, code lost:
        if (r2 != null) goto L_0x0177;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0146, code lost:
        android.util.Log.wtf(android.view.inputmethod.InputMethodManager.TAG, "startInputOrWindowGainedFocus must not return null. startInputReason=" + com.android.internal.inputmethod.InputMethodDebug.startInputReasonToString(r26) + " editorInfo=" + r2 + " startInputFlags=" + com.android.internal.inputmethod.InputMethodDebug.startInputFlagsToString(r18));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        monitor-exit(r22);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0176, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0177, code lost:
        r25.mActivityViewToScreenMatrix = r2.getActivityViewToScreenMatrix();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x017f, code lost:
        if (r2.id == null) goto L_0x0193;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0181, code lost:
        setInputChannelLocked(r2.channel);
        r25.mBindSequence = r2.sequence;
        r25.mCurMethod = r2.method;
        r25.mCurId = r2.id;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0195, code lost:
        if (r2.channel == null) goto L_0x01a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x019b, code lost:
        if (r2.channel == r25.mCurChannel) goto L_0x01a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x019d, code lost:
        r2.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01a6, code lost:
        if (r2.result == 11) goto L_0x01a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01a9, code lost:
        r25.mRestartOnNextWindowFocus = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01ae, code lost:
        if (r25.mCurMethod == null) goto L_0x01e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01b2, code lost:
        if (r25.mCompletions == null) goto L_0x01e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:?, code lost:
        r25.mCurMethod.displayCompletions(r25.mCompletions);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01be, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01c0, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01c1, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        if (r27 != null) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01ca, code lost:
        android.util.Log.w(android.view.inputmethod.InputMethodManager.TAG, "IME died: " + r25.mCurId, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01e2, code lost:
        monitor-exit(r22);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01e3, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01e5, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01e9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x01ea, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01f4, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        monitor-exit(r22);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        r2 = r0.getWindowToken();
     */
    public boolean startInputInner(int startInputReason, IBinder windowGainingFocus, int startInputFlags, int softInputMode, int windowFlags) {
        synchronized (this.mH) {
            View view = this.mServedView;
            if (view == null) {
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$startInputInner$1$InputMethodManager(int startInputReason) {
        startInputInner(startInputReason, null, 0, 0, 0);
    }

    @UnsupportedAppUsage
    public void windowDismissed(IBinder appWindowToken) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != null && this.mServedView.getWindowToken() == appWindowToken) {
                finishInputLocked();
            }
            if (this.mCurRootView != null && this.mCurRootView.getWindowToken() == appWindowToken) {
                this.mCurRootView = null;
            }
        }
    }

    @UnsupportedAppUsage
    public void focusIn(View view) {
        synchronized (this.mH) {
            focusInLocked(view);
        }
    }

    /* access modifiers changed from: package-private */
    public void focusInLocked(View view) {
        if ((view == null || !view.isTemporarilyDetached()) && this.mCurRootView == view.getRootView()) {
            this.mNextServedView = view;
            scheduleCheckFocusLocked(view);
        }
    }

    @UnsupportedAppUsage
    public void focusOut(View view) {
        synchronized (this.mH) {
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

    @UnsupportedAppUsage
    public void checkFocus() {
        if (checkFocusNoStartInput(false)) {
            startInputInner(4, null, 0, 0, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        if (r1 == null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003f, code lost:
        r1.finishComposingText();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
        return true;
     */
    private boolean checkFocusNoStartInput(boolean forceNewFocus) {
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
            if (!this.mServedView.onCheckIsTextEditor()) {
                maybeCallServedViewChangedLocked(null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
        if (r25 == false) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002b, code lost:
        r20 = r0 | 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0030, code lost:
        r20 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0036, code lost:
        if (checkFocusNoStartInput(r8) == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0049, code lost:
        if (startInputInner(1, r22.getWindowToken(), r20, r24, r26) == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004c, code lost:
        r1 = r21.mH;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004e, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r21.mService.startInputOrWindowGainedFocus(2, r21.mClient, r22.getWindowToken(), r20, r24, r26, null, null, 0, r22.getContext().getApplicationInfo().targetSdkVersion);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0075, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0076, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0078, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007d, code lost:
        throw r0.rethrowFromSystemServer();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007f, code lost:
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
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    @UnsupportedAppUsage
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

    public void registerImeConsumer(ImeInsetsSourceConsumer imeInsetsConsumer) {
        if (imeInsetsConsumer != null) {
            synchronized (this.mH) {
                this.mImeInsetsConsumer = imeInsetsConsumer;
            }
            return;
        }
        throw new IllegalStateException("ImeInsetsSourceConsumer cannot be null.");
    }

    public void unregisterImeConsumer(ImeInsetsSourceConsumer imeInsetsConsumer) {
        if (imeInsetsConsumer != null) {
            synchronized (this.mH) {
                if (this.mImeInsetsConsumer == imeInsetsConsumer) {
                    this.mImeInsetsConsumer = null;
                }
            }
            return;
        }
        throw new IllegalStateException("ImeInsetsSourceConsumer cannot be null.");
    }

    public boolean requestImeShow(ResultReceiver resultReceiver) {
        synchronized (this.mH) {
            if (this.mServedView == null) {
                return false;
            }
            showSoftInput(this.mServedView, 0, resultReceiver);
            return true;
        }
    }

    public void notifyImeHidden() {
        synchronized (this.mH) {
            try {
                if (this.mCurMethod != null) {
                    this.mCurMethod.notifyImeHidden();
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void updateSelection(View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.updateSelection(view, selStart, selEnd, candidatesStart, candidatesEnd);
            return;
        }
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

    @Deprecated
    public void viewClicked(View view) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.viewClicked(view);
            return;
        }
        boolean focusChanged = this.mServedView != this.mNextServedView;
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
                try {
                    this.mCurMethod.viewClicked(focusChanged);
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + this.mCurId, e);
                }
            }
        }
    }

    @Deprecated
    public boolean isWatchingCursor(View view) {
        return false;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void setUpdateCursorAnchorInfoMode(int flags) {
        synchronized (this.mH) {
            this.mRequestUpdateCursorAnchorInfoMonitorMode = flags;
        }
    }

    @Deprecated
    public void updateCursor(View view, int left, int top, int right, int bottom) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.updateCursor(view, left, top, right, bottom);
            return;
        }
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

    public void updateCursorAnchorInfo(View view, CursorAnchorInfo cursorAnchorInfo) {
        if (view != null && cursorAnchorInfo != null) {
            InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
            if (fallbackImm != null) {
                fallbackImm.updateCursorAnchorInfo(view, cursorAnchorInfo);
                return;
            }
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
                                if (this.mActivityViewToScreenMatrix != null) {
                                    this.mCurMethod.updateCursorAnchorInfo(CursorAnchorInfo.createForAdditionalParentMatrix(cursorAnchorInfo, this.mActivityViewToScreenMatrix));
                                } else {
                                    this.mCurMethod.updateCursorAnchorInfo(cursorAnchorInfo);
                                }
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

    public void sendAppPrivateCommand(View view, String action, Bundle data) {
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(view);
        if (fallbackImm != null) {
            fallbackImm.sendAppPrivateCommand(view, action, data);
            return;
        }
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
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
        if (token != null) {
            InputMethodPrivilegedOperationsRegistry.get(token).setInputMethod(id);
        } else if (id != null) {
            if (Process.myUid() == 1000) {
                Log.w(TAG, "System process should not be calling setInputMethod() because almost always it is a bug under multi-user / multi-profile environment. Consider interacting with InputMethodManagerService directly via LocalServices.");
                return;
            }
            Context fallbackContext = ActivityThread.currentApplication();
            if (fallbackContext != null && fallbackContext.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == 0) {
                List<InputMethodInfo> imis = getEnabledInputMethodList();
                int numImis = imis.size();
                boolean found = false;
                int i = 0;
                while (true) {
                    if (i >= numImis) {
                        break;
                    } else if (id.equals(imis.get(i).getId())) {
                        found = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!found) {
                    Log.e(TAG, "Ignoring setInputMethod(null, " + id + ") because the specified id not found in enabled IMEs.");
                    return;
                }
                Log.w(TAG, "The undocumented behavior that setInputMethod() accepts null token when the caller has WRITE_SECURE_SETTINGS is deprecated. This behavior may be completely removed in a future version.  Update secure settings directly instead.");
                ContentResolver resolver = fallbackContext.getContentResolver();
                Settings.Secure.putInt(resolver, Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE, -1);
                Settings.Secure.putString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD, id);
            }
        }
    }

    @Deprecated
    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        if (token == null) {
            Log.e(TAG, "setInputMethodAndSubtype() does not accept null token on Android Q and later.");
        } else {
            InputMethodPrivilegedOperationsRegistry.get(token).setInputMethodAndSubtype(id, subtype);
        }
    }

    @Deprecated
    public void hideSoftInputFromInputMethod(IBinder token, int flags) {
        InputMethodPrivilegedOperationsRegistry.get(token).hideMySoftInput(flags);
    }

    @Deprecated
    public void showSoftInputFromInputMethod(IBinder token, int flags) {
        InputMethodPrivilegedOperationsRegistry.get(token).showMySoftInput(flags);
    }

    public int dispatchInputEvent(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
        synchronized (this.mH) {
            if (this.mCurMethod == null) {
                return 0;
            }
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 63 && keyEvent.getRepeatCount() == 0) {
                    showInputMethodPickerLocked();
                    return 1;
                }
            }
            PendingEvent p = obtainPendingEventLocked(event, token, this.mCurId, callback, handler);
            if (this.mMainLooper.isCurrentThread()) {
                return sendInputEventOnMainLooperLocked(p);
            }
            Message msg = this.mH.obtainMessage(5, p);
            msg.setAsynchronous(true);
            this.mH.sendMessage(msg);
            return -1;
        }
    }

    public void dispatchKeyEventFromInputMethod(View targetView, KeyEvent event) {
        ViewRootImpl viewRootImpl;
        InputMethodManager fallbackImm = getFallbackInputMethodManagerIfNecessary(targetView);
        if (fallbackImm != null) {
            fallbackImm.dispatchKeyEventFromInputMethod(targetView, event);
            return;
        }
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
                boolean handled = true;
                if (result != 1) {
                    handled = false;
                }
                invokeFinishedInputEventCallback(p, handled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int sendInputEventOnMainLooperLocked(PendingEvent p) {
        InputChannel inputChannel = this.mCurChannel;
        if (inputChannel != null) {
            if (this.mCurSender == null) {
                this.mCurSender = new ImeInputEventSender(inputChannel, this.mH.getLooper());
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
                invokeFinishedInputEventCallback(p, handled);
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
        Message msg = Message.obtain(p.mHandler, p);
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
    /* access modifiers changed from: public */
    private void recyclePendingEventLocked(PendingEvent p) {
        p.recycle();
        this.mPendingEventPool.release(p);
    }

    public void showInputMethodPicker() {
        synchronized (this.mH) {
            showInputMethodPickerLocked();
        }
    }

    public void showInputMethodPickerFromSystem(boolean showAuxiliarySubtypes, int displayId) {
        int mode;
        if (showAuxiliarySubtypes) {
            mode = 1;
        } else {
            mode = 2;
        }
        try {
            this.mService.showInputMethodPickerFromSystem(this.mClient, mode, displayId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
        try {
            this.mService.showInputMethodAndSubtypeEnablerFromClient(this.mClient, imiId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        try {
            return this.mService.getCurrentInputMethodSubtype();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        Context fallbackContext;
        if (Process.myUid() == 1000) {
            Log.w(TAG, "System process should not call setCurrentInputMethodSubtype() because almost always it is a bug under multi-user / multi-profile environment. Consider directly interacting with InputMethodManagerService via LocalServices.");
            return false;
        } else if (subtype == null || (fallbackContext = ActivityThread.currentApplication()) == null || fallbackContext.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != 0) {
            return false;
        } else {
            ContentResolver contentResolver = fallbackContext.getContentResolver();
            String imeId = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD);
            if (ComponentName.unflattenFromString(imeId) == null) {
                return false;
            }
            try {
                List<InputMethodSubtype> enabledSubtypes = this.mService.getEnabledInputMethodSubtypeList(imeId, true);
                int numSubtypes = enabledSubtypes.size();
                for (int i = 0; i < numSubtypes; i++) {
                    InputMethodSubtype enabledSubtype = enabledSubtypes.get(i);
                    if (enabledSubtype.equals(subtype)) {
                        Settings.Secure.putInt(contentResolver, Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE, enabledSubtype.hashCode());
                        return true;
                    }
                }
                return false;
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 114740982)
    @Deprecated
    public void notifyUserAction() {
        Log.w(TAG, "notifyUserAction() is a hidden method, which is now just a stub method that does nothing.  Leave comments in b.android.com/114740982 if your  application still depends on the previous behavior of this method.");
    }

    public Map<InputMethodInfo, List<InputMethodSubtype>> getShortcutInputMethodsAndSubtypes() {
        List<InputMethodInfo> enabledImes = getEnabledInputMethodList();
        enabledImes.sort(Comparator.comparingInt($$Lambda$InputMethodManager$pvWYFFVbHzZCDCCTiZVM09Xls4w.INSTANCE));
        int numEnabledImes = enabledImes.size();
        for (int imiIndex = 0; imiIndex < numEnabledImes; imiIndex++) {
            InputMethodInfo imi = enabledImes.get(imiIndex);
            int subtypeCount = getEnabledInputMethodSubtypeList(imi, true).size();
            for (int subtypeIndex = 0; subtypeIndex < subtypeCount; subtypeIndex++) {
                InputMethodSubtype subtype = imi.getSubtypeAt(subtypeIndex);
                if (SUBTYPE_MODE_VOICE.equals(subtype.getMode())) {
                    return Collections.singletonMap(imi, Collections.singletonList(subtype));
                }
            }
        }
        return Collections.emptyMap();
    }

    static /* synthetic */ int lambda$getShortcutInputMethodsAndSubtypes$2(InputMethodInfo imi) {
        return !imi.isSystem();
    }

    @UnsupportedAppUsage
    public int getInputMethodWindowVisibleHeight() {
        try {
            return this.mService.getInputMethodWindowVisibleHeight();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportActivityView(int childDisplayId, Matrix matrix) {
        float[] matrixValues;
        if (matrix == null) {
            matrixValues = null;
        } else {
            try {
                matrixValues = new float[9];
                matrix.getValues(matrixValues);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        this.mService.reportActivityView(this.mClient, childDisplayId, matrixValues);
    }

    @Deprecated
    public boolean switchToLastInputMethod(IBinder imeToken) {
        return InputMethodPrivilegedOperationsRegistry.get(imeToken).switchToPreviousInputMethod();
    }

    @Deprecated
    public boolean switchToNextInputMethod(IBinder imeToken, boolean onlyCurrentIme) {
        return InputMethodPrivilegedOperationsRegistry.get(imeToken).switchToNextInputMethod(onlyCurrentIme);
    }

    @Deprecated
    public boolean shouldOfferSwitchingToNextInputMethod(IBinder imeToken) {
        return InputMethodPrivilegedOperationsRegistry.get(imeToken).shouldOfferSwitchingToNextInputMethod();
    }

    @Deprecated
    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        try {
            this.mService.setAdditionalInputMethodSubtypes(imiId, subtypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        try {
            return this.mService.getLastInputMethodSubtype();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void maybeCallServedViewChangedLocked(EditorInfo tba) {
        ImeInsetsSourceConsumer imeInsetsSourceConsumer = this.mImeInsetsConsumer;
        if (imeInsetsSourceConsumer != null) {
            imeInsetsSourceConsumer.onServedEditorChanged(tba);
        }
    }

    public int getDisplayId() {
        return this.mDisplayId;
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
    }

    /* access modifiers changed from: private */
    public final class ImeInputEventSender extends InputEventSender {
        public ImeInputEventSender(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override // android.view.InputEventSender
        public void onInputEventFinished(int seq, boolean handled) {
            InputMethodManager.this.finishedInputEvent(seq, handled, false);
        }
    }

    /* access modifiers changed from: private */
    public final class PendingEvent implements Runnable {
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
            this.mCallback.onFinishedInputEvent(this.mToken, this.mHandled);
            synchronized (InputMethodManager.this.mH) {
                InputMethodManager.this.recyclePendingEventLocked(this);
            }
        }
    }

    private static String dumpViewInfo(View view) {
        if (view == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(view);
        sb.append(",focus=" + view.hasFocus());
        sb.append(",windowFocus=" + view.hasWindowFocus());
        sb.append(",autofillUiShowing=" + isAutofillUIShowing(view));
        sb.append(",window=" + view.getWindowToken());
        sb.append(",displayId=" + view.getContext().getDisplayId());
        sb.append(",temporaryDetach=" + view.isTemporarilyDetached());
        return sb.toString();
    }
}
