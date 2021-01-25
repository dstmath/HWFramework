package ohos.agp.window.wmc;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewRootImpl;
import android.view.ViewRootImplEx;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputContentInfo;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.accessibility.BarrierFreeInnerClient;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.RootContainerView;
import ohos.agp.components.Text;
import ohos.agp.utils.Rect;
import ohos.agp.vsync.VsyncScheduler;
import ohos.agp.window.aspbshell.AGPContainerView;
import ohos.agp.window.aspbshell.AGPWindowInternal;
import ohos.agp.window.aspbshell.TextInputConnection;
import ohos.agp.window.wmc.AGPEngineAdapter;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.bluetooth.BluetoothDeviceClass;
import ohos.bundle.AbilityInfo;
import ohos.global.resource.ResourceManagerInner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.miscservices.inputmethod.InputMethodController;
import ohos.miscservices.inputmethod.adapter.CompletionInfoAdapter;
import ohos.miscservices.inputmethod.adapter.ContextMenuActionIdAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextRequestAdapter;
import ohos.miscservices.inputmethod.adapter.InputContentInfoAdapter;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public class AGPWindow {
    private static final long GET_INPUTHEIGHT_DELAY = 200;
    private static final int IM_HIDDEN_HEIGHT = 0;
    private static final int INVALID_VISIBLILITY = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPWindow");
    private static final int WINDOW_ANIMATION_NONE = -1;
    private static final float WINDOW_SURFACE_ALPHA = 0.0f;
    private static final int WINDOW_WIDTH_OFFSET = 0;
    private static Context mBarrierfreeContext;
    protected Rect boundRect;
    private boolean hasSetPreContentLayout;
    private boolean isTransparent;
    private WeakReference<Text> lastInputFocus;
    private AGPEngineAdapter mAGPEngine;
    private long mAGPMultiModel;
    private AGPWindowInternal mAGPWindowInternal;
    protected android.content.Context mAndroidContext;
    protected WindowManager.LayoutParams mAndroidParam;
    protected Window mAndroidWindow;
    private Color mBackgroundColor;
    protected Context mContext;
    private IAGPEngineAdapter mEngine;
    private int mEngineMode;
    protected int mFlag;
    private RootContainerView mRootContainerView;
    protected AGPContainerView mSurfaceView;
    private TextInputConnection.ITextViewListener mTextViewListener;
    private ComponentContainer mViewGroup;
    private int mWindowAnimations;
    protected int mWindowFlag;
    protected boolean movable;
    protected Move move;
    private int preInputHeight;
    private boolean swipeDismissEnabled;
    private SwipeManager swipeManager;
    private boolean viewGroupResizedFlag;

    public static class LayoutParams {
        public static final int FIRST_SUB_WINDOW = 1000;
        public static final int FIRST_SYSTEM_WINDOW = 2000;
        public static final int INVALID_WINDOW_TYPE = -1;
        public static final int START_REMOTE_INPUT_FLAG = 1;
        public static final int TYPE_APPLICATION = 2;
        public static final int TYPE_APPLICATION_MEDIA = 1001;
        public static final int TYPE_APPLICATION_OVERLAY = 2038;
        public static final int TYPE_APPLICATION_PANEL = 1000;
        public static final int TYPE_DREAM = 2023;
        public static final int TYPE_KEYGUARD = 2004;
        public static final int TYPE_NAVIGATION_BAR = 2019;
        public static final int TYPE_STATUS_BAR = 2000;
        public static final int TYPE_TOAST = 2005;
        public static final int TYPE_VOICE_INTERACTION = 2031;
        public float alpha = 1.0f;
        public float dimAmount = 1.0f;
        public int flags;
        public int gravity;
        public int height;
        public float screenBrightness = -1.0f;
        public String title;
        public IBinder token;
        public int type;
        public int width;
        public int windowAnimations = -1;
        public int x;
        public int y;
    }

    public interface OnDismissListener {
        void onDismissed();
    }

    public interface OnSwipeChangedListener {
        void onSwipeCancelled();

        void onSwipeProgressChanged(float f, float f2);
    }

    private void invalidate() {
    }

    private native void nativeDestroyMultiModel(long j);

    private native long nativeInitMultimodal();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean nativeSetWindowtoken(long j, IBinder iBinder);

    public void load() {
    }

    public void setContentLayout(String str) {
    }

    public AGPWindow() {
        this.isTransparent = false;
        this.preInputHeight = 0;
        this.viewGroupResizedFlag = false;
        this.hasSetPreContentLayout = false;
        this.mWindowAnimations = -1;
        this.swipeDismissEnabled = false;
    }

    public AGPWindow(android.content.Context context) {
        this.isTransparent = false;
        this.preInputHeight = 0;
        this.viewGroupResizedFlag = false;
        this.hasSetPreContentLayout = false;
        this.mWindowAnimations = -1;
        this.swipeDismissEnabled = false;
        if (context != null) {
            this.mFlag = 10;
            createSurfaceView(context);
            this.mAGPWindowInternal = new AGPWindowInternal(context);
            this.mAndroidContext = context;
            this.mAGPWindowInternal.setContentView(this.mSurfaceView);
            this.mRootContainerView = new RootContainerView(this.mContext);
            return;
        }
        throw new AGPWindowManager.BadWindowException("AGPWindow: android Context is null");
    }

    public AGPWindow(Context context) {
        this(context, 1);
    }

    public AGPWindow(Context context, int i) {
        this.isTransparent = false;
        this.preInputHeight = 0;
        this.viewGroupResizedFlag = false;
        this.hasSetPreContentLayout = false;
        this.mWindowAnimations = -1;
        this.swipeDismissEnabled = false;
        if (context != null) {
            this.mFlag = i;
            this.mContext = context;
            this.mRootContainerView = new RootContainerView(this.mContext);
            Object hostContext = context.getHostContext();
            if (hostContext instanceof android.content.Context) {
                this.mAndroidContext = (android.content.Context) hostContext;
                createSurfaceView(this.mAndroidContext);
                if (context.getAbilityInfo() == null || context.getAbilityInfo().getType() != AbilityInfo.AbilityType.PAGE) {
                    HiLog.debug(LABEL, "AGPWindow the context type is not page", new Object[0]);
                    this.mAndroidWindow = null;
                } else {
                    if (HiLog.isDebuggable()) {
                        HiLog.debug(LABEL, "AGPWindow the context type is page", new Object[0]);
                    }
                    this.mAndroidWindow = ((Activity) this.mAndroidContext).getWindow();
                    initWindow();
                }
                if (this.mFlag == 1) {
                    if (HiLog.isDebuggable()) {
                        HiLog.debug(LABEL, "AGPWindow mSurfaceView is set as content view", new Object[0]);
                    }
                    android.content.Context context2 = this.mAndroidContext;
                    if (context2 instanceof Activity) {
                        ((Activity) context2).setContentView(this.mSurfaceView);
                    } else {
                        HiLog.error(LABEL, "AGPWindow can not get activity", new Object[0]);
                    }
                }
            } else {
                HiLog.error(LABEL, "AGPWindow context.getHostContext() is not android content instance", new Object[0]);
            }
        } else {
            HiLog.error(LABEL, "agpWindow context is null", new Object[0]);
            throw new AGPWindowManager.BadWindowException("AGPWindow: context is null");
        }
    }

    private void initWindow() {
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.error(LABEL, "initWindow mAndroidWindow is null.", new Object[0]);
        } else {
            window.addFlags(Integer.MIN_VALUE);
        }
    }

    private void createSurfaceView(android.content.Context context) {
        if (context == null) {
            HiLog.error(LABEL, "createSurfaceView failed, AGPWindow androidContext is null", new Object[0]);
            return;
        }
        this.mSurfaceView = new AGPContainerView(context);
        this.mSurfaceView.setZOrderMediaOverlay(true);
        this.mSurfaceView.setSurfaceListener(new SurfaceViewListener());
    }

    public long setEngine(int i, IAGPEngineAdapter iAGPEngineAdapter) {
        this.mEngineMode = i;
        this.mEngine = iAGPEngineAdapter;
        if (i == 3) {
            this.mAGPMultiModel = nativeInitMultimodal();
        } else {
            this.mAGPMultiModel = 0;
        }
        return this.mAGPMultiModel;
    }

    private void createEngineAdapter(int i) {
        HiLog.debug(LABEL, "createEngineAdapter", new Object[0]);
        int i2 = this.mEngineMode;
        if (i2 == 2) {
            create3DEngineAdapter(i);
        } else if (i2 == 3) {
            HiLog.debug(LABEL, "Here Create ACE engine.", new Object[0]);
            if (this.mTextViewListener == null) {
                HiLog.error(LABEL, "ACE text view listener is null, input method events will not be processed.", new Object[0]);
            }
        } else {
            create2DEngineAdapter(i);
        }
    }

    private void create2DEngineAdapter(int i) {
        this.mTextViewListener = new TextViewListener();
        this.mSurfaceView.setInputChannelListener(this.mTextViewListener);
        HiLog.debug(LABEL, "Create 2D engine adapter.", new Object[0]);
        this.mAGPEngine = new AGPEngineAdapter(this.mContext, i);
        this.mAGPMultiModel = nativeInitMultimodal();
        AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
        this.mEngine = aGPEngineAdapter;
        aGPEngineAdapter.setInputListener(new AGPEngineAdapter.IAGPInputListener() {
            /* class ohos.agp.window.wmc.AGPWindow.AnonymousClass1 */

            @Override // ohos.agp.window.wmc.AGPEngineAdapter.IAGPInputListener
            public void onInputStart() {
                HiLog.debug(AGPWindow.LABEL, "onInputStart", new Object[0]);
                AGPWindow.this.startInput();
            }

            @Override // ohos.agp.window.wmc.AGPEngineAdapter.IAGPInputListener
            public void onInputStop() {
                HiLog.debug(AGPWindow.LABEL, "onInputStop", new Object[0]);
                AGPWindow.this.stopInput();
            }
        });
    }

    private void create3DEngineAdapter(int i) {
        HiLog.debug(LABEL, "Create 3D engine adapter.", new Object[0]);
        VsyncScheduler.getInstance().requestVsync(new VsyncScheduler.FrameCallback() {
            /* class ohos.agp.window.wmc.AGPWindow.AnonymousClass2 */

            @Override // ohos.agp.vsync.VsyncScheduler.FrameCallback
            public void doFrame(long j) {
                HiLog.debug(AGPWindow.LABEL, "VsyncScheduler doFrame time=%{public}l.", new Object[]{Long.valueOf(j)});
                if (j > 0 && AGPWindow.this.mEngine != null) {
                    AGPWindow.this.mEngine.processVSync(j);
                }
            }
        });
    }

    public void notifyBarrierFree() {
        if (this.mFlag != 5) {
            Context context = mBarrierfreeContext;
            if (!(context == this.mContext || context == null)) {
                HiLog.debug(LABEL, "BF:unRegister ability", new Object[0]);
                BarrierFreeInnerClient.unRegisterBarrierFreeAbility(mBarrierfreeContext);
            }
            if (this.mContext != null) {
                HiLog.debug(LABEL, "BF:Register ability", new Object[0]);
                BarrierFreeInnerClient.registerBarrierFreeAbility(this.mContext, 0);
            }
            setBarrierfreeContext(this.mContext);
        }
    }

    public void setMovable(boolean z) {
        this.movable = z;
    }

    public boolean isMovable() {
        return this.movable;
    }

    public Rect getBoundRect() {
        return this.boundRect;
    }

    public void setBoundRect(Rect rect) {
        this.boundRect = rect;
    }

    public void setTransparent(boolean z) {
        if (this.isTransparent != z) {
            if (z) {
                this.mSurfaceView.getHolder().setFormat(-3);
            }
            AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
            if (aGPEngineAdapter != null) {
                aGPEngineAdapter.setTransparent(z);
            } else {
                HiLog.error(LABEL, "setTransparent not find mAGPEngine.", new Object[0]);
            }
            this.isTransparent = z;
        }
    }

    public boolean dispatchTouchEventFromAndroid(MotionEvent motionEvent) {
        if (this.mAGPEngine == null || motionEvent == null) {
            HiLog.error(LABEL, "dispatchManipulationEvent event or mAGPEngine be null.", new Object[0]);
            return false;
        }
        motionEvent.offsetLocation(0.0f, (float) (-getWindowOffsetY()));
        Optional createEvent = MultimodalEventFactory.createEvent(motionEvent);
        if (createEvent.isPresent()) {
            TouchEvent touchEvent = (MultimodalEvent) createEvent.get();
            if (touchEvent instanceof TouchEvent) {
                return this.mAGPEngine.processTouchEvent(touchEvent);
            }
        }
        return false;
    }

    public boolean dispatchManipulationEvent(MultimodalEvent multimodalEvent) {
        HiLog.debug(LABEL, "dispatchManipulationEvent event.", new Object[0]);
        if (multimodalEvent instanceof KeyEvent) {
            return dispatchKeyboardEvent((KeyEvent) multimodalEvent);
        }
        if (multimodalEvent instanceof TouchEvent) {
            return dispatchTouchEvent((TouchEvent) multimodalEvent);
        }
        return false;
    }

    public boolean dispatchKeyboardEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            HiLog.error(LABEL, "dispatchKeyboardEvent event is null.", new Object[0]);
            return false;
        }
        HiLog.debug(LABEL, "Window dispatchKeyboardEvent event called, code =%{public}d", new Object[]{Integer.valueOf(keyEvent.getKeyCode())});
        IAGPEngineAdapter iAGPEngineAdapter = this.mEngine;
        if (iAGPEngineAdapter != null) {
            return iAGPEngineAdapter.processKeyEvent(keyEvent);
        }
        HiLog.error(LABEL, "dispatchKeyboardEvent not find mEngine.", new Object[0]);
        return false;
    }

    public boolean dispatchTouchEvent(TouchEvent touchEvent) {
        if (touchEvent == null) {
            HiLog.error(LABEL, "dispatchTouchEvent event is null.", new Object[0]);
            return false;
        }
        int action = touchEvent.getAction();
        if (HiLog.isDebuggable()) {
            HiLog.debug(LABEL, "Window dispatchTouchEvent called, touch type =%{public}d", new Object[]{Integer.valueOf(action)});
        }
        if (this.swipeDismissEnabled && processSwipeDismiss(touchEvent)) {
            HiLog.debug(LABEL, "Window process swipe dismiss process finish, event continue.", new Object[0]);
        }
        adjustLayout(action);
        touchEvent.setScreenOffset(0.0f, (float) (-getWindowOffsetY()));
        handleMovable(touchEvent);
        IAGPEngineAdapter iAGPEngineAdapter = this.mEngine;
        if (iAGPEngineAdapter != null) {
            return iAGPEngineAdapter.processTouchEvent(touchEvent);
        }
        HiLog.error(LABEL, "dispatchTouchEvent not find mEngine.", new Object[0]);
        return false;
    }

    public boolean setSwipeToDismiss(boolean z) {
        HiLog.debug(LABEL, "Window set swipe to dismiss begin, isEnabled =%{public}s", new Object[]{String.valueOf(z)});
        if (!z) {
            this.swipeDismissEnabled = false;
            SwipeManager swipeManager2 = this.swipeManager;
            if (swipeManager2 != null) {
                swipeManager2.resetMembers();
                this.swipeManager = null;
            }
            return true;
        } else if (this.mAndroidContext != null) {
            this.swipeDismissEnabled = true;
            this.swipeManager = new SwipeManager();
            this.swipeManager.setOnDismissListener(new OnDismissListener() {
                /* class ohos.agp.window.wmc.AGPWindow.AnonymousClass3 */

                @Override // ohos.agp.window.wmc.AGPWindow.OnDismissListener
                public void onDismissed() {
                    if (AGPWindow.this.mContext != null && AGPWindow.this.mContext.getUITaskDispatcher() != null) {
                        AGPWindow.this.mContext.getUITaskDispatcher().delayDispatch(new Runnable() {
                            /* class ohos.agp.window.wmc.AGPWindow.AnonymousClass3.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                HiLog.debug(AGPWindow.LABEL, "Swipe dismiss terminate ability start.", new Object[0]);
                                AGPWindow.this.mContext.terminateAbility();
                            }
                        }, 0);
                    }
                }
            });
            this.mRootContainerView.setSlideRecognizerMode(2);
            this.mRootContainerView.setOnSlideListener(new RootContainerView.OnSlideListener() {
                /* class ohos.agp.window.wmc.AGPWindow.AnonymousClass4 */

                @Override // ohos.agp.components.RootContainerView.OnSlideListener
                public void onSlideStart(RootContainerView rootContainerView) {
                    if (AGPWindow.this.swipeManager != null) {
                        AGPWindow.this.swipeManager.setCanScroll(false);
                        HiLog.debug(AGPWindow.LABEL, "Window Swipe dismiss onSlideStart called, canScroll is false.", new Object[0]);
                    }
                }
            });
            return true;
        } else {
            HiLog.debug(LABEL, "Window set swipe to dismiss failed because context invalid.", new Object[0]);
            return false;
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        SwipeManager swipeManager2 = this.swipeManager;
        if (swipeManager2 != null) {
            swipeManager2.setOnDismissListener(onDismissListener);
        }
    }

    public void setOnSwipeChangedListener(OnSwipeChangedListener onSwipeChangedListener) {
        SwipeManager swipeManager2 = this.swipeManager;
        if (swipeManager2 != null) {
            swipeManager2.setOnSwipeChangedListener(onSwipeChangedListener);
        }
    }

    public void setWindowOnSwipe() {
        SwipeManager swipeManager2 = this.swipeManager;
        if (swipeManager2 != null) {
            swipeManager2.setOnSwipeChangedListener(new SwipeChangedListener());
        }
    }

    public boolean dispatchMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            HiLog.error(LABEL, "dispatchMouseEvent event is null.", new Object[0]);
            return false;
        }
        HiLog.debug(LABEL, "dispatchMouseEvent event start, event type =%{public}d", new Object[]{Integer.valueOf(mouseEvent.getAction())});
        mouseEvent.setCursorOffset(0.0f, (float) (-getWindowOffsetY()));
        HiLog.debug(LABEL, "dispatchMouseEvent event end, event y =%{public}d", new Object[]{Float.valueOf(mouseEvent.getCursor().getY())});
        IAGPEngineAdapter iAGPEngineAdapter = this.mEngine;
        if (iAGPEngineAdapter != null) {
            return iAGPEngineAdapter.processMouseEvent(mouseEvent);
        }
        HiLog.error(LABEL, "dispatchMouseEvent not find mEngine.", new Object[0]);
        return false;
    }

    public boolean dispatchRotationEvent(RotationEvent rotationEvent) {
        if (rotationEvent == null) {
            HiLog.error(LABEL, "dispatchRotationEvent event is null.", new Object[0]);
            return false;
        }
        HiLog.debug(LABEL, "dispatchRotationEvent event start, event value =%{public}d", new Object[]{Float.valueOf(rotationEvent.getRotationValue())});
        IAGPEngineAdapter iAGPEngineAdapter = this.mEngine;
        if (iAGPEngineAdapter != null) {
            return iAGPEngineAdapter.processRotationEvent(rotationEvent);
        }
        HiLog.error(LABEL, "dispatchRotationEvent not find mEngine.", new Object[0]);
        return false;
    }

    public void show() {
        int i = this.mEngineMode;
        if (i == 2 || i == 3) {
            createEngineAdapter(this.mFlag);
        }
    }

    public void destroy() {
        AGPContainerView aGPContainerView = this.mSurfaceView;
        if (aGPContainerView != null) {
            aGPContainerView.destroy();
            this.mSurfaceView = null;
        }
        IAGPEngineAdapter iAGPEngineAdapter = this.mEngine;
        if (iAGPEngineAdapter != null) {
            iAGPEngineAdapter.processDestroy();
        } else {
            HiLog.error(LABEL, "destroy not find mEngine.", new Object[0]);
        }
        long j = this.mAGPMultiModel;
        if (j != 0) {
            nativeDestroyMultiModel(j);
        }
        this.mContext = null;
        this.mAndroidContext = null;
        this.mTextViewListener = null;
    }

    public void setAttributes(LayoutParams layoutParams) {
        Window window = this.mAndroidWindow;
        if (window == null || layoutParams == null || this.mAndroidParam == null) {
            HiLog.error(LABEL, "setAttributes mAndroidWindow or param be null.", new Object[0]);
            return;
        }
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = AGPWindowManager.getAndroidGravity(layoutParams.gravity);
        attributes.x = layoutParams.x;
        attributes.y = layoutParams.y;
        if (layoutParams.width != this.mSurfaceView.getActualWidth()) {
            attributes.width = layoutParams.width;
        }
        if (layoutParams.height != this.mSurfaceView.getActualHeight()) {
            attributes.height = layoutParams.height;
        }
        attributes.alpha = layoutParams.alpha;
        attributes.dimAmount = layoutParams.dimAmount;
        attributes.screenBrightness = layoutParams.screenBrightness;
        attributes.flags = layoutParams.flags;
        if (layoutParams.title == null) {
            HiLog.error(LABEL, "setAttributes title can not be null.", new Object[0]);
        } else if (this.mAndroidParam.getTitle().toString().equals(layoutParams.title)) {
            HiLog.debug(LABEL, "setAttributes title not change.", new Object[0]);
        } else {
            this.mAndroidParam.setTitle(layoutParams.title);
        }
        if (layoutParams.windowAnimations != -1) {
            try {
                attributes.windowAnimations = ResourceManagerInner.getAResId(layoutParams.windowAnimations, Class.forName(this.mAndroidContext.getPackageName() + ".ResourceTable", false, this.mAndroidContext.getClassLoader()), this.mAndroidContext);
                this.mWindowAnimations = layoutParams.windowAnimations;
                HiLog.debug(LABEL, "setAttributes for window animation finished, resource id: %{public}d.", new Object[]{Integer.valueOf(layoutParams.windowAnimations)});
            } catch (ClassNotFoundException unused) {
                HiLog.error(LABEL, "Theme class for window animation not found, resource id: %{public}d.", new Object[]{Integer.valueOf(layoutParams.windowAnimations)});
            }
        }
        this.mAndroidWindow.setAttributes(attributes);
        setWindowOffset();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWindowOffset() {
        AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
        if (aGPEngineAdapter != null) {
            aGPEngineAdapter.setWindowOffset(getWindowOffsetX(), getWindowOffsetY());
        }
    }

    public void updateAttributes(LayoutParams layoutParams) {
        WindowManager.LayoutParams layoutParams2 = this.mAndroidParam;
        if (layoutParams2 == null || layoutParams == null) {
            HiLog.error(LABEL, "updateAttributes mAndroidParam or param be null.", new Object[0]);
            return;
        }
        layoutParams2.gravity = AGPWindowManager.getAndroidGravity(layoutParams.gravity);
        this.mAndroidParam.type = layoutParams.type;
        this.mAndroidParam.x = layoutParams.x;
        this.mAndroidParam.y = layoutParams.y;
        this.mAndroidParam.width = layoutParams.width;
        this.mAndroidParam.height = layoutParams.height;
        this.mAndroidParam.alpha = layoutParams.alpha;
        this.mAndroidParam.dimAmount = layoutParams.dimAmount;
        this.mAndroidParam.screenBrightness = layoutParams.screenBrightness;
        this.mAndroidParam.flags = layoutParams.flags;
    }

    public final LayoutParams getAttributes() {
        LayoutParams layoutParams = new LayoutParams();
        int i = this.mFlag;
        if (i == 1 || i == 10) {
            if (!(this.mAndroidContext instanceof Activity)) {
                HiLog.error(LABEL, "AGPWindow getAttributes failed due to mAndroidContext is not acitivity", new Object[0]);
            }
            Window window = ((Activity) this.mAndroidContext).getWindow();
            if (window == null) {
                HiLog.error(LABEL, "AGPWindow getAttributes failed due to getWindow return null", new Object[0]);
                return null;
            }
            this.mAndroidParam = window.getAttributes();
        }
        WindowManager.LayoutParams layoutParams2 = this.mAndroidParam;
        if (layoutParams2 != null) {
            if (this.mFlag == 6) {
                layoutParams.token = layoutParams2.token;
            }
            layoutParams.type = this.mAndroidParam.type;
            layoutParams.x = this.mAndroidParam.x;
            layoutParams.y = this.mAndroidParam.y;
            layoutParams.width = this.mAndroidParam.width;
            layoutParams.height = this.mAndroidParam.height;
            layoutParams.alpha = this.mAndroidParam.alpha;
            layoutParams.dimAmount = this.mAndroidParam.dimAmount;
            layoutParams.screenBrightness = this.mAndroidParam.screenBrightness;
            layoutParams.gravity = AGPWindowManager.getZidaneTextAlignment(this.mAndroidParam.gravity);
            layoutParams.flags = this.mAndroidParam.flags;
            int i2 = this.mWindowAnimations;
            if (i2 != -1) {
                layoutParams.windowAnimations = i2;
            }
            if (layoutParams.width == -1 || layoutParams.width == -2) {
                layoutParams.width = this.mSurfaceView.getActualWidth();
            }
            if (layoutParams.height == -1 || layoutParams.height == -2) {
                layoutParams.height = this.mSurfaceView.getActualHeight();
            }
            layoutParams.title = this.mAndroidParam.getTitle().toString();
            return layoutParams;
        }
        HiLog.error(LABEL, "AGPWindow getAttributes failed due to mAndroidParam is null", new Object[0]);
        return null;
    }

    public Optional<Component> findComponentById(int i) {
        ComponentContainer componentContainer = this.mViewGroup;
        if (componentContainer != null) {
            return Optional.ofNullable(componentContainer.findComponentById(i));
        }
        HiLog.debug(LABEL, "Window find component empty.", new Object[0]);
        return Optional.empty();
    }

    public void setContentLayout(int i) {
        Context context = this.mContext;
        if (context != null) {
            Component parse = LayoutScatter.getInstance(context).parse(i, null, false);
            if (parse instanceof ComponentContainer) {
                setContentLayout((ComponentContainer) parse);
            }
        }
    }

    public void setContentLayout(ComponentContainer componentContainer) {
        this.mViewGroup = componentContainer;
        if (this.mAGPEngine == null) {
            this.mEngineMode = 1;
            createEngineAdapter(this.mFlag);
        }
        AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
        if (aGPEngineAdapter != null) {
            if (this.hasSetPreContentLayout) {
                aGPEngineAdapter.setContentLayout(componentContainer);
            } else {
                this.mRootContainerView.removeAllComponents();
                this.mRootContainerView.addComponent(componentContainer);
                this.mAGPEngine.setContentLayout(this.mRootContainerView);
            }
            if (this.isTransparent) {
                this.mAGPEngine.setTransparent(true);
            }
        } else {
            HiLog.error(LABEL, "setContentLayout not find mEngine.", new Object[0]);
        }
        invalidate();
        notifyBarrierFree();
    }

    public void setPreContentLayout(ComponentContainer componentContainer, int i, int i2) {
        this.mViewGroup = componentContainer;
        if (this.mAGPEngine == null) {
            this.mEngineMode = 1;
            createEngineAdapter(this.mFlag);
        }
        AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
        if (aGPEngineAdapter != null) {
            aGPEngineAdapter.setPreContentLayout(componentContainer, i, i2);
        } else {
            HiLog.error(LABEL, "setPreContentLayout not find mEngine.", new Object[0]);
        }
        this.hasSetPreContentLayout = true;
    }

    public ComponentContainer getContainerLayout() {
        return this.mViewGroup;
    }

    public void setBackgroundColor(int i, int i2, int i3) {
        this.mBackgroundColor = new Color(i, i2, i3);
        AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
        if (aGPEngineAdapter != null) {
            aGPEngineAdapter.setBackgroundColor(i, i2, i3);
        } else {
            HiLog.error(LABEL, "setBackgroundColor not find mEngine.", new Object[0]);
        }
        invalidate();
    }

    public Color getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public void setBackground(String str) {
        if (this.mAndroidWindow == null) {
            HiLog.error(LABEL, "AGPWindow setBackground failed due to Window is null.", new Object[0]);
        } else if (new File(str).exists()) {
            AGPEngineAdapter aGPEngineAdapter = this.mAGPEngine;
            if (aGPEngineAdapter != null) {
                aGPEngineAdapter.setTransparent(true);
            } else {
                HiLog.error(LABEL, "setBackground not find mAGPEngine.", new Object[0]);
            }
            AGPContainerView aGPContainerView = this.mSurfaceView;
            if (aGPContainerView != null) {
                aGPContainerView.setZOrderOnTop(true);
                this.mSurfaceView.getHolder().setFormat(-2);
                this.mSurfaceView.setAlpha(0.0f);
            }
            Bitmap decodeFile = BitmapFactory.decodeFile(str);
            if (decodeFile == null) {
                HiLog.error(LABEL, "The drawablePath bitmap cannot be decoded and returned null.", new Object[0]);
                return;
            }
            this.mAndroidWindow.setBackgroundDrawable(new BitmapDrawable(decodeFile));
            HiLog.debug(LABEL, "AGPWindow setBackground success.", new Object[0]);
        }
    }

    public void setNavigationBarColor(int i) {
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.error(LABEL, "setNavigationBarColor failed due to Window is null", new Object[0]);
        } else {
            window.setNavigationBarColor(i);
        }
    }

    public void setStatusBarColor(int i) {
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.error(LABEL, "setStatusBarColor failed due to Window is null", new Object[0]);
        } else {
            window.setStatusBarColor(i);
        }
    }

    public void setPadding(int i, int i2, int i3, int i4) {
        if (i < 0 || i2 < 0 || i3 < 0 || i4 < 0) {
            HiLog.error(LABEL, "setPadding failed due to input < 0", new Object[0]);
            return;
        }
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.error(LABEL, "setPadding failed due to Window is null", new Object[0]);
            return;
        }
        View decorView = window.getDecorView();
        if (decorView == null) {
            HiLog.error(LABEL, "setPadding failed due to decorView is null", new Object[0]);
        } else {
            decorView.setPadding(i, i2, i3, i4);
        }
    }

    public void setStatusBarVisibility(int i) {
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.error(LABEL, "setStatusBarVisibility failed due to Window is null", new Object[0]);
            return;
        }
        View decorView = window.getDecorView();
        if (decorView == null) {
            HiLog.error(LABEL, "setPadding failed due to decorView is null", new Object[0]);
        } else {
            decorView.setSystemUiVisibility(i);
        }
    }

    public int getStatusBarVisibility() {
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.error(LABEL, "getStatusBarVisibility failed due to Window is null", new Object[0]);
            return -1;
        }
        View decorView = window.getDecorView();
        if (decorView != null) {
            return decorView.getSystemUiVisibility();
        }
        HiLog.error(LABEL, "setPadding failed due to decorView is null", new Object[0]);
        return -1;
    }

    public AGPContainerView getSurfaceView() {
        return this.mSurfaceView;
    }

    public android.content.Context getContext() {
        return this.mAndroidContext;
    }

    public Context getHarmonyContext() {
        return this.mContext;
    }

    public int getWindowFlag() {
        return this.mWindowFlag;
    }

    public void setWindowFlag(int i) {
        this.mWindowFlag = i;
    }

    public TextInputConnection.ITextViewListener getTextViewListener() {
        return this.mTextViewListener;
    }

    public void setTextViewListener(TextViewListenerWrapper textViewListenerWrapper) {
        this.mTextViewListener = textViewListenerWrapper;
        AGPContainerView aGPContainerView = this.mSurfaceView;
        if (aGPContainerView != null) {
            aGPContainerView.setInputChannelListener(textViewListenerWrapper);
        } else {
            HiLog.error(LABEL, "AGP container view is empty, make sure to initialize surface view.", new Object[0]);
        }
        HiLog.debug(LABEL, "Set ACE text view listener successfully.", new Object[0]);
    }

    public void startInput() {
        ComponentContainer componentContainer;
        InputMethodController instance = InputMethodController.getInstance();
        if (instance == null || (componentContainer = this.mViewGroup) == null) {
            HiLog.error(LABEL, "StartInput failed because InputMethodController or viewgroup invalid.", new Object[0]);
            return;
        }
        Component findFocus = componentContainer.findFocus();
        if (findFocus instanceof Text) {
            Text text = (Text) findFocus;
            WeakReference<Text> weakReference = this.lastInputFocus;
            if (weakReference == null || !text.equals(weakReference.get())) {
                this.lastInputFocus = new WeakReference<>(text);
                instance.startInput(1, true);
                HiLog.debug(LABEL, "StartInput InputMethodController successful, focus view changed.", new Object[0]);
                return;
            }
            instance.startInput(1, false);
            HiLog.debug(LABEL, "StartInput InputMethodController successful, focus view not changed.", new Object[0]);
            return;
        }
        HiLog.error(LABEL, "StartInput InputMethodController failed because focus text view invalid.", new Object[0]);
    }

    public void stopInput() {
        InputMethodController instance = InputMethodController.getInstance();
        if (instance == null) {
            HiLog.error(LABEL, "InputMethodController mIMController is null", new Object[0]);
        } else if (!instance.stopInput(1)) {
            HiLog.debug(LABEL, "stopInput InputMethodController failed", new Object[0]);
        }
    }

    public void setAVController(Object obj) {
        Window window;
        if (obj == null || (window = this.mAndroidWindow) == null || !(obj instanceof MediaController)) {
            HiLog.error(LABEL, "Set media controller failed because type error.", new Object[0]);
            return;
        }
        window.setMediaController((MediaController) obj);
        HiLog.debug(LABEL, "Set media controller successful.", new Object[0]);
    }

    public Optional<Object> getAVController() {
        if (this.mAndroidWindow != null) {
            HiLog.debug(LABEL, "Get media controller from window.", new Object[0]);
            return Optional.ofNullable(this.mAndroidWindow.getMediaController());
        }
        HiLog.debug(LABEL, "Get media controller empty because window empty.", new Object[0]);
        return Optional.empty();
    }

    public void setVolumeControlStream(int i) {
        Window window = this.mAndroidWindow;
        if (window != null) {
            window.setVolumeControlStream(i);
            HiLog.debug(LABEL, "Set the audio stream successful.", new Object[0]);
            return;
        }
        HiLog.error(LABEL, "Set the audio stream failed because window does not exist.", new Object[0]);
    }

    public int getVolumeControlStream() {
        if (this.mAndroidWindow != null) {
            HiLog.debug(LABEL, "Get the audio stream from window.", new Object[0]);
            return this.mAndroidWindow.getVolumeControlStream();
        }
        HiLog.debug(LABEL, "Get the audio stream failed empty because window empty.", new Object[0]);
        return Integer.MIN_VALUE;
    }

    public boolean hasWindowFocus() {
        View decorView;
        Window window = this.mAndroidWindow;
        if (window != null && (decorView = window.getDecorView()) != null) {
            return decorView.hasWindowFocus();
        }
        HiLog.debug(LABEL, "Check window focus failed because window empty or abnormal.", new Object[0]);
        return false;
    }

    public Optional<Component> getWindowFocus() {
        ComponentContainer componentContainer = this.mViewGroup;
        if (componentContainer != null) {
            return Optional.ofNullable(componentContainer.findFocus());
        }
        HiLog.debug(LABEL, "Window does not have a focus.", new Object[0]);
        return Optional.empty();
    }

    public void setIsAmbientMode(boolean z) {
        Window window = this.mAndroidWindow;
        if (window == null) {
            HiLog.debug(LABEL, "Window is invalid, cannot set ambient mode.", new Object[0]);
            return;
        }
        View peekDecorView = window.peekDecorView();
        if (peekDecorView != null) {
            ViewRootImpl viewRootImpl = peekDecorView.getViewRootImpl();
            if (viewRootImpl != null) {
                ViewRootImplEx.setIsAmbientMode(viewRootImpl, z);
                HiLog.debug(LABEL, "Window set ambient mode success, now mode is: %{public}s", new Object[]{String.valueOf(z)});
                return;
            }
            HiLog.debug(LABEL, "Window set ambient mode: %{public}s failed due to viewRoot is invalid.", new Object[]{String.valueOf(z)});
            return;
        }
        HiLog.debug(LABEL, "Window set ambient mode: %{public}s failed due to decorView is invalid.", new Object[]{String.valueOf(z)});
    }

    /* access modifiers changed from: protected */
    public static class Move {
        protected float lastX;
        protected float lastY;
        protected int[] location = new int[2];
        protected float nowX;
        protected float nowY;
        protected float tranX;
        protected float tranY;

        protected Move() {
        }
    }

    public static class Color {
        private int mBlue;
        private int mGreen;
        private int mRed;

        Color(int i, int i2, int i3) {
            this.mRed = i;
            this.mGreen = i2;
            this.mBlue = i3;
        }

        public int getBlue() {
            return this.mBlue;
        }

        public int getGreen() {
            return this.mGreen;
        }

        public int getRed() {
            return this.mRed;
        }
    }

    public class TextViewListener implements TextInputConnection.ITextViewListener {
        public TextViewListener() {
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean commitText(CharSequence charSequence, int i) {
            HiLog.debug(AGPWindow.LABEL, "AGPWindow TextViewListener commitText was called", new Object[0]);
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean insertText = ((Text) findFocus).getInputDataChannel().insertText(charSequence.toString());
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call insertText of input data channel success.", new Object[0]);
                    return insertText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean commitContent(InputContentInfo inputContentInfo, int i, Bundle bundle) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean insertRichContent = ((Text) findFocus).getInputDataChannel().insertRichContent(InputContentInfoAdapter.convertToRichContent(inputContentInfo));
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call insertRichContent of input data channel success.", new Object[0]);
                    return insertRichContent;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean deleteSurroundingText(int i, int i2) {
            if (AGPWindow.this.mViewGroup == null) {
                return false;
            }
            Component findFocus = AGPWindow.this.mViewGroup.findFocus();
            if (!(findFocus instanceof Text)) {
                return false;
            }
            InputDataChannel inputDataChannel = ((Text) findFocus).getInputDataChannel();
            if (!inputDataChannel.deleteForward(i2) || !inputDataChannel.deleteBackward(i)) {
                return false;
            }
            HiLog.debug(AGPWindow.LABEL, "AGPWindow call delete events of input data channel success.", new Object[0]);
            return true;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public CharSequence getTextBeforeCursor(int i, int i2) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    String forward = ((Text) findFocus).getInputDataChannel().getForward(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call getForward of input data channel success.", new Object[0]);
                    return forward;
                }
            }
            return "";
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public CharSequence getTextAfterCursor(int i, int i2) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    String backward = ((Text) findFocus).getInputDataChannel().getBackward(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call getBackward of input data channel success.", new Object[0]);
                    return backward;
                }
            }
            return "";
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean sendKeyEvent(android.view.KeyEvent keyEvent) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    Text text = (Text) findFocus;
                    Optional createEvent = MultimodalEventFactory.createEvent(keyEvent);
                    if (!createEvent.isPresent()) {
                        HiLog.debug(AGPWindow.LABEL, "AGPWindow createEvent failed!", new Object[0]);
                        return false;
                    }
                    KeyEvent keyEvent2 = (MultimodalEvent) createEvent.get();
                    if (!(keyEvent2 instanceof KeyEvent)) {
                        HiLog.debug(AGPWindow.LABEL, "AGPWindow createEvent is not KeyEvent", new Object[0]);
                        return false;
                    }
                    boolean sendKeyEvent = text.getInputDataChannel().sendKeyEvent(keyEvent2);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call sendKeyEvent of input data channel success.", new Object[0]);
                    return sendKeyEvent;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean setComposingRegion(int i, int i2) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean markText = ((Text) findFocus).getInputDataChannel().markText(i, i2);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call markText of input data channel success.", new Object[0]);
                    return markText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean finishComposingText() {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean unmarkText = ((Text) findFocus).getInputDataChannel().unmarkText();
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call unmarkText of input data channel success.", new Object[0]);
                    return unmarkText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean setComposingText(CharSequence charSequence, int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean replaceMarkedText = ((Text) findFocus).getInputDataChannel().replaceMarkedText(charSequence.toString());
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call replaceMarkedText of input data channel success.", new Object[0]);
                    return replaceMarkedText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public ExtractedText getExtractedText(ExtractedTextRequest extractedTextRequest, int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    ExtractedText convertToExtractedText = ExtractedTextAdapter.convertToExtractedText(((Text) findFocus).getInputDataChannel().subscribeEditingText(ExtractedTextRequestAdapter.convertToEditingCapability(extractedTextRequest)));
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call subscribeEditingText of input data channel success.", new Object[0]);
                    return convertToExtractedText;
                }
            }
            return null;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean performEditorAction(int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean sendKeyFunction = ((Text) findFocus).getInputDataChannel().sendKeyFunction(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call sendKeyFunction of input data channel success.", new Object[0]);
                    return sendKeyFunction;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean setSelection(int i, int i2) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean selectText = ((Text) findFocus).getInputDataChannel().selectText(i, i2);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call selectText of input data channel success.", new Object[0]);
                    return selectText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public void closeConnection() {
            if (AGPWindow.this.mViewGroup != null) {
                try {
                    Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                    if (findFocus instanceof Text) {
                        ((Text) findFocus).getInputDataChannel().close();
                        HiLog.debug(AGPWindow.LABEL, "AGPWindow call close of input data channel success.", new Object[0]);
                    }
                } catch (IllegalStateException unused) {
                    HiLog.error(AGPWindow.LABEL, "AGPWindow call close input data channel but view group has been released.", new Object[0]);
                }
            }
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public void updateEditorInfo(EditorInfo editorInfo) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    Text text = (Text) findFocus;
                    editorInfo.inputType = text.getTextInputType();
                    editorInfo.imeOptions = text.getInputMethodOption();
                }
            }
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public int getCursorCapsMode(int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    int autoCapitalizeMode = ((Text) findFocus).getInputDataChannel().getAutoCapitalizeMode(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call get getAutoCapitalizeMode of input data channel success.", new Object[0]);
                    return autoCapitalizeMode;
                }
            }
            return 0;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean beginBatchEdit() {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean lock = ((Text) findFocus).getInputDataChannel().lock();
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call lock of input data channel success.", new Object[0]);
                    return lock;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean endBatchEdit() {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean unlock = ((Text) findFocus).getInputDataChannel().unlock();
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call unlock of input data channel success.", new Object[0]);
                    return unlock;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public CharSequence getSelectedText(int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    String selectedText = ((Text) findFocus).getInputDataChannel().getSelectedText(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call getSelectedText of input data channel success.", new Object[0]);
                    return selectedText;
                }
            }
            return "";
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean clearMetaKeyStates(int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean clearNoncharacterKeyState = ((Text) findFocus).getInputDataChannel().clearNoncharacterKeyState(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call clearNoncharacterKeyState of input data channel success.", new Object[0]);
                    return clearNoncharacterKeyState;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean commitCompletion(CompletionInfo completionInfo) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean recommendText = ((Text) findFocus).getInputDataChannel().recommendText(CompletionInfoAdapter.convertToRecommendationInfo(completionInfo));
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call recommendText of input data channel success.", new Object[0]);
                    return recommendText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            if (!(AGPWindow.this.mViewGroup == null || correctionInfo == null)) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean reviseText = ((Text) findFocus).getInputDataChannel().reviseText(correctionInfo.getOffset(), correctionInfo.getOldText().toString(), correctionInfo.getNewText().toString());
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call reviseText of input data channel success.", new Object[0]);
                    return reviseText;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean performContextMenuAction(int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean sendMenuFunction = ((Text) findFocus).getInputDataChannel().sendMenuFunction(ContextMenuActionIdAdapter.convertToEditMenuActionId(i));
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call sendMenuFunction of input data channel success.", new Object[0]);
                    return sendMenuFunction;
                }
            }
            return false;
        }

        @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
        public boolean requestCursorUpdates(int i) {
            if (AGPWindow.this.mViewGroup != null) {
                Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                if (findFocus instanceof Text) {
                    boolean subscribeCaretContext = ((Text) findFocus).getInputDataChannel().subscribeCaretContext(i);
                    HiLog.debug(AGPWindow.LABEL, "AGPWindow call subscribeCaretContext of input data channel success.", new Object[0]);
                    return subscribeCaretContext;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class SurfaceViewListener implements AGPContainerView.ISurfaceViewListener {
        private SurfaceViewListener() {
        }

        @Override // ohos.agp.window.aspbshell.AGPContainerView.ISurfaceViewListener
        public void onSurfaceCreated(Surface surface) {
            HiLog.debug(AGPWindow.LABEL, "onSurfaceCreated", new Object[0]);
            if (AGPWindow.this.mEngine != null) {
                AGPWindow.this.mEngine.processSurfaceCreated(surface);
            } else {
                HiLog.error(AGPWindow.LABEL, "onSurfaceCreated not find mEngine.", new Object[0]);
            }
        }

        @Override // ohos.agp.window.aspbshell.AGPContainerView.ISurfaceViewListener
        public void onSurfaceChanged(Surface surface, int i, int i2, int i3) {
            HiLog.debug(AGPWindow.LABEL, "onSurfaceChanged", new Object[0]);
            if (AGPWindow.this.mEngine != null) {
                AGPWindow.this.mEngine.processSurfaceChanged(surface, i, i2, i3);
                if (AGPWindow.this.swipeDismissEnabled && AGPWindow.this.swipeManager != null && AGPWindow.this.swipeManager.windowWidth == 0) {
                    HiLog.debug(AGPWindow.LABEL, "Surface change calls swipe manager to init.", new Object[0]);
                    AGPWindow.this.swipeManager.init(AGPWindow.this.mAndroidContext);
                }
            } else {
                HiLog.error(AGPWindow.LABEL, "onSurfaceChanged not find mEngine.", new Object[0]);
            }
            AGPWindow.this.setWindowOffset();
            if (AGPWindow.this.mAGPMultiModel != 0) {
                HiLog.debug(AGPWindow.LABEL, "try to get windowtoken", new Object[0]);
                IBinder windowToken = AGPWindow.this.mSurfaceView.getWindowToken();
                if (windowToken == null) {
                    HiLog.error(AGPWindow.LABEL, "token is null", new Object[0]);
                    return;
                }
                AGPWindow aGPWindow = AGPWindow.this;
                if (!aGPWindow.nativeSetWindowtoken(aGPWindow.mAGPMultiModel, windowToken)) {
                    HiLog.error(AGPWindow.LABEL, "failed to set windowtoken for multimodel", new Object[0]);
                } else if (AGPWindow.this.mAGPEngine != null) {
                    AGPWindow.this.mAGPEngine.setMultiModel(AGPWindow.this.mAGPMultiModel);
                }
            }
        }

        @Override // ohos.agp.window.aspbshell.AGPContainerView.ISurfaceViewListener
        public void onSurfaceDestroyed(Surface surface) {
            HiLog.debug(AGPWindow.LABEL, "onSurfaceDestroyed", new Object[0]);
            if (AGPWindow.this.mEngine != null) {
                AGPWindow.this.mEngine.processSurfaceDestroy(surface);
            } else {
                HiLog.error(AGPWindow.LABEL, "onSurfaceDestroyed not find mEngine.", new Object[0]);
            }
        }

        @Override // ohos.agp.window.aspbshell.AGPContainerView.ISurfaceViewListener
        public void onConfigurationChanged(Configuration configuration) {
            HiLog.debug(AGPWindow.LABEL, "onConfigurationChanged", new Object[0]);
            if (AGPWindow.this.mEngine != null) {
                AGPWindow.this.mEngine.processConfigurationChanged(configuration);
            } else {
                HiLog.error(AGPWindow.LABEL, "ConfigurationChanged not find mEngine..", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SwipeManager {
        private static final float MAX_DISTANCE_THRESHOLD = 0.33f;
        private static final int MILLISECOND_UNIT = 1000;
        private static final float MIN_DISTANCE_THRESHOLD = 0.1f;
        private static final int SLOP_RATIO = 2;
        private int activeTouchId;
        private boolean activityTranslucencyConverted;
        private boolean blockGesture;
        private boolean canScroll;
        private boolean discardSwiping;
        private OnDismissListener dismissListener;
        private boolean dismissed;
        private long downTime;
        private float downX;
        private float downY;
        private float lastX;
        private int minFlingVelocity;
        private int slop;
        private final SwipeAnimator swipeAnimator;
        private OnSwipeChangedListener swipeChangedListener;
        private boolean swiping;
        private VelocityTracker velocityTracker;
        private int windowWidth;

        private float progressOfAlpha(float f) {
            return 1.0f - ((f * f) * f);
        }

        private SwipeManager() {
            this.blockGesture = false;
            this.canScroll = true;
            this.swipeAnimator = new SwipeAnimator();
        }

        public void setCanScroll(boolean z) {
            this.canScroll = z;
        }

        public boolean verifySwiping(TouchEvent touchEvent) {
            checkGesture(touchEvent);
            if (this.blockGesture) {
                return true;
            }
            switch (touchEvent.getAction()) {
                case 1:
                    resetMembers();
                    this.downTime = touchEvent.getStartTime();
                    this.downX = touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX();
                    this.downY = touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY();
                    this.activeTouchId = touchEvent.getPointerId(touchEvent.getIndex());
                    this.velocityTracker = VelocityTracker.obtain();
                    MotionEvent obtain = MotionEvent.obtain(touchEvent.getStartTime(), touchEvent.getOccurredTime(), 0, this.downX, this.downY, 0);
                    this.velocityTracker.addMovement(obtain);
                    obtain.recycle();
                    break;
                case 3:
                    if (this.velocityTracker != null && !this.discardSwiping) {
                        if (this.activeTouchId < touchEvent.getPointerCount()) {
                            updateSwiping(touchEvent);
                            break;
                        } else {
                            this.discardSwiping = true;
                            break;
                        }
                    }
                case 4:
                    this.activeTouchId = touchEvent.getPointerId(touchEvent.getIndex());
                    break;
                case 5:
                    if (touchEvent.getPointerId(touchEvent.getIndex()) == this.activeTouchId) {
                        this.activeTouchId = touchEvent.getPointerId(touchEvent.getIndex() == 0 ? 1 : 0);
                        break;
                    }
                    break;
            }
            if (this.discardSwiping || !this.swiping) {
                return false;
            }
            return true;
        }

        public void executeSwiping(TouchEvent touchEvent) {
            checkGesture(touchEvent);
            if (!this.blockGesture) {
                int action = touchEvent.getAction();
                if (action == 2) {
                    updateDismiss(touchEvent);
                    if (this.dismissed) {
                        HiLog.debug(AGPWindow.LABEL, "Action PRIMARY_POINT_UP, executeSwiping animate dismiss started.", new Object[0]);
                        this.swipeAnimator.animateDismiss(touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() - this.downX);
                    } else if (!this.swiping || this.lastX == -2.14748365E9f) {
                        HiLog.debug(AGPWindow.LABEL, "Action PRIMARY_POINT_UP, but neither animate dismiss nor recovery.", new Object[0]);
                    } else {
                        HiLog.debug(AGPWindow.LABEL, "Action PRIMARY_POINT_UP, executeSwiping animate recovery started.", new Object[0]);
                        this.swipeAnimator.animateRecovery(touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() - this.downX);
                    }
                    resetMembers();
                } else if (action == 3) {
                    MotionEvent obtain = MotionEvent.obtain(this.downTime, touchEvent.getOccurredTime(), 2, touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX(), touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY(), 0);
                    this.velocityTracker.addMovement(obtain);
                    obtain.recycle();
                    this.lastX = touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX();
                    updateSwiping(touchEvent);
                    if (this.swiping) {
                        setProgress(this.lastX - this.downX);
                    }
                } else if (action == 6) {
                    cancel();
                    resetMembers();
                }
            }
        }

        public void setOnDismissListener(OnDismissListener onDismissListener) {
            this.dismissListener = onDismissListener;
        }

        public void setOnSwipeChangedListener(OnSwipeChangedListener onSwipeChangedListener) {
            this.swipeChangedListener = onSwipeChangedListener;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void init(android.content.Context context) {
            ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
            this.slop = viewConfiguration.getScaledTouchSlop();
            HiLog.debug(AGPWindow.LABEL, "Swipe manager init slop: %{public}d.", new Object[]{Integer.valueOf(this.slop)});
            this.minFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
            if (AGPWindow.this.mViewGroup != null) {
                this.windowWidth = AGPWindow.this.mViewGroup.getWidth();
                HiLog.debug(AGPWindow.LABEL, "Swipe manager init windowWidth: %{public}d.", new Object[]{Integer.valueOf(this.windowWidth)});
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetMembers() {
            VelocityTracker velocityTracker2 = this.velocityTracker;
            if (velocityTracker2 != null) {
                velocityTracker2.recycle();
            }
            this.velocityTracker = null;
            this.downTime = 0;
            this.downX = 0.0f;
            this.lastX = -2.14748365E9f;
            this.downY = 0.0f;
            this.swiping = false;
            this.dismissed = false;
            this.discardSwiping = false;
            this.canScroll = true;
        }

        private void updateSwiping(TouchEvent touchEvent) {
            if (!this.canScroll) {
                boolean z = this.swiping;
                if (!z) {
                    float x = touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() - this.downX;
                    float y = touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getY() - this.downY;
                    float f = (x * x) + (y * y);
                    int i = this.slop;
                    if (f > ((float) (i * i))) {
                        this.swiping = x > ((float) (i * 2)) && Math.abs(y) < Math.abs(x);
                    } else {
                        this.swiping = false;
                    }
                }
                if (this.swiping && !z && !AGPWindow.this.isTransparent && (AGPWindow.this.mAndroidContext instanceof Activity)) {
                    try {
                        Object invoke = Activity.class.getMethod("convertToTranslucent", Class.forName("android.app.Activity$TranslucentConversionListener"), ActivityOptions.class).invoke((Activity) AGPWindow.this.mAndroidContext, null, null);
                        if (invoke instanceof Boolean) {
                            this.activityTranslucencyConverted = ((Boolean) invoke).booleanValue();
                        }
                        HiLog.debug(AGPWindow.LABEL, "Update swiping window convertToTranslucent done: %{public}s.", new Object[]{String.valueOf(this.activityTranslucencyConverted)});
                    } catch (InvocationTargetException unused) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert to translucent failed, target invoke failed.", new Object[0]);
                    } catch (NoSuchMethodException unused2) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert to translucent failed, method not found.", new Object[0]);
                    } catch (IllegalAccessException unused3) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert to translucent failed, access illegal.", new Object[0]);
                    } catch (ClassNotFoundException unused4) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert to translucent failed, class not found.", new Object[0]);
                    }
                }
            }
        }

        private void updateDismiss(TouchEvent touchEvent) {
            if (!this.canScroll) {
                float x = touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX();
                this.velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = this.velocityTracker.getXVelocity();
                if (this.lastX == -2.14748365E9f && touchEvent.getOccurredTime() != this.downTime) {
                    xVelocity = x / (((float) (touchEvent.getOccurredTime() - this.downTime)) / 1000.0f);
                }
                if (!this.dismissed) {
                    float f = 0.0f;
                    int i = this.minFlingVelocity;
                    if (i != 0) {
                        f = ((float) this.windowWidth) * Math.max(Math.min(((-0.23000002f * xVelocity) / ((float) i)) + MAX_DISTANCE_THRESHOLD, (float) MAX_DISTANCE_THRESHOLD), (float) MIN_DISTANCE_THRESHOLD);
                    }
                    if ((x > f && touchEvent.getPointerScreenPosition(touchEvent.getIndex()).getX() >= this.lastX) || xVelocity >= ((float) this.minFlingVelocity)) {
                        this.dismissed = true;
                    }
                }
                if (this.dismissed && this.swiping && xVelocity < ((float) (-this.minFlingVelocity))) {
                    this.dismissed = false;
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancel() {
            if (!AGPWindow.this.isTransparent && (AGPWindow.this.mAndroidContext instanceof Activity)) {
                Activity activity = (Activity) AGPWindow.this.mAndroidContext;
                if (this.activityTranslucencyConverted) {
                    try {
                        Activity.class.getMethod("convertFromTranslucent", new Class[0]).invoke(activity, new Object[0]);
                        this.activityTranslucencyConverted = false;
                        HiLog.debug(AGPWindow.LABEL, "Cancel window swiping convertFromTranslucent done.", new Object[0]);
                    } catch (NoSuchMethodException unused) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert from translucent failed, method not found.", new Object[0]);
                    } catch (IllegalAccessException unused2) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert from translucent failed, access illegal.", new Object[0]);
                    } catch (InvocationTargetException unused3) {
                        HiLog.error(AGPWindow.LABEL, "Activity convert from translucent failed, target invoke failed.", new Object[0]);
                    }
                }
            }
            OnSwipeChangedListener onSwipeChangedListener = this.swipeChangedListener;
            if (onSwipeChangedListener != null) {
                onSwipeChangedListener.onSwipeCancelled();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dismiss() {
            HiLog.debug(AGPWindow.LABEL, "Swipe manager dismiss called.", new Object[0]);
            OnDismissListener onDismissListener = this.dismissListener;
            if (onDismissListener != null) {
                onDismissListener.onDismissed();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setProgress(float f) {
            int i;
            OnSwipeChangedListener onSwipeChangedListener = this.swipeChangedListener;
            if (onSwipeChangedListener != null && f >= 0.0f && (i = this.windowWidth) > 0) {
                onSwipeChangedListener.onSwipeProgressChanged(progressOfAlpha(f / ((float) i)), f);
            }
        }

        private void checkGesture(TouchEvent touchEvent) {
            if (touchEvent.getAction() == 1) {
                this.blockGesture = this.swipeAnimator.isAnimating();
            }
        }

        /* access modifiers changed from: private */
        public class SwipeAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
            private static final float INTERPOLATOR_FACTOR = 1.5f;
            private static final long SWIPE_DURATION = 250;
            private final TimeInterpolator SWIPE_INTERPOLATOR = new DecelerateInterpolator(INTERPOLATOR_FACTOR);
            private boolean dismissComplete = false;
            private final ValueAnimator swipeAnimator = new ValueAnimator();
            private boolean wasCanceled = false;

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            SwipeAnimator() {
                this.swipeAnimator.addUpdateListener(this);
                this.swipeAnimator.addListener(this);
            }

            /* access modifiers changed from: package-private */
            public void animateDismiss(float f) {
                if (SwipeManager.this.windowWidth > 0) {
                    animate(f / ((float) SwipeManager.this.windowWidth), 1.0f, SWIPE_DURATION, this.SWIPE_INTERPOLATOR, true);
                }
            }

            /* access modifiers changed from: package-private */
            public void animateRecovery(float f) {
                if (SwipeManager.this.windowWidth > 0) {
                    animate(f / ((float) SwipeManager.this.windowWidth), 0.0f, SWIPE_DURATION, this.SWIPE_INTERPOLATOR, false);
                }
            }

            /* access modifiers changed from: package-private */
            public boolean isAnimating() {
                return this.swipeAnimator.isStarted();
            }

            private void animate(float f, float f2, long j, TimeInterpolator timeInterpolator, boolean z) {
                this.swipeAnimator.cancel();
                this.dismissComplete = z;
                this.swipeAnimator.setFloatValues(f, f2);
                this.swipeAnimator.setDuration(j);
                this.swipeAnimator.setInterpolator(timeInterpolator);
                this.swipeAnimator.start();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                this.wasCanceled = false;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                HiLog.debug(AGPWindow.LABEL, "SwipeAnimator on animation end called.", new Object[0]);
                if (this.wasCanceled) {
                    return;
                }
                if (this.dismissComplete) {
                    SwipeManager.this.dismiss();
                } else {
                    SwipeManager.this.cancel();
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.wasCanceled = true;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (valueAnimator.getAnimatedValue() instanceof Float) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    SwipeManager swipeManager = SwipeManager.this;
                    swipeManager.setProgress(floatValue * ((float) swipeManager.windowWidth));
                }
            }
        }
    }

    private class SwipeChangedListener implements OnSwipeChangedListener {
        private SwipeChangedListener() {
        }

        @Override // ohos.agp.window.wmc.AGPWindow.OnSwipeChangedListener
        public void onSwipeProgressChanged(float f, float f2) {
            if (AGPWindow.this.mAndroidWindow != null) {
                WindowManager.LayoutParams attributes = AGPWindow.this.mAndroidWindow.getAttributes();
                attributes.x = (int) f2;
                attributes.alpha = f;
                AGPWindow.this.mAndroidWindow.setAttributes(attributes);
                AGPWindow.this.mAndroidWindow.setFlags(attributes.x == 0 ? 1024 : 512, BluetoothDeviceClass.MajorClass.IMAGING);
            }
        }

        @Override // ohos.agp.window.wmc.AGPWindow.OnSwipeChangedListener
        public void onSwipeCancelled() {
            if (AGPWindow.this.mAndroidWindow != null) {
                WindowManager.LayoutParams attributes = AGPWindow.this.mAndroidWindow.getAttributes();
                if (attributes.x != 0 || attributes.alpha != 1.0f) {
                    attributes.x = 0;
                    attributes.alpha = 1.0f;
                    AGPWindow.this.mAndroidWindow.setAttributes(attributes);
                    AGPWindow.this.mAndroidWindow.setFlags(1024, BluetoothDeviceClass.MajorClass.IMAGING);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getWindowOffsetX() {
        Window window;
        View findViewById;
        android.content.Context context = this.mAndroidContext;
        if (context == null || !(context instanceof Activity) || (window = ((Activity) context).getWindow()) == null || (findViewById = window.findViewById(16908290)) == null) {
            return 0;
        }
        return findViewById.getLeft();
    }

    /* access modifiers changed from: protected */
    public int getWindowOffsetY() {
        Window window;
        View findViewById;
        android.content.Context context = this.mAndroidContext;
        if (context == null || !(context instanceof Activity) || (window = ((Activity) context).getWindow()) == null || (findViewById = window.findViewById(16908290)) == null) {
            return 0;
        }
        return findViewById.getTop();
    }

    public boolean dispatchTouchEventFromDialog(TouchEvent touchEvent) {
        HiLog.debug(LABEL, "dispatchTouchEventFromDialog TouchEvent.", new Object[0]);
        IAGPEngineAdapter iAGPEngineAdapter = this.mEngine;
        if (iAGPEngineAdapter != null && touchEvent != null) {
            return iAGPEngineAdapter.processTouchEvent(touchEvent);
        }
        HiLog.error(LABEL, "dispatchTouchEventFromDialog not find mEngine.", new Object[0]);
        return false;
    }

    private void handleMovable(TouchEvent touchEvent) {
        if (touchEvent == null) {
            HiLog.error(LABEL, "handleMovable event is null.", new Object[0]);
        } else if (this.movable) {
            if (this.move == null) {
                this.move = new Move();
            }
            int action = touchEvent.getAction();
            if (action == 1) {
                this.move.lastX = touchEvent.getPointerScreenPosition(0).getX();
                this.move.lastY = touchEvent.getPointerScreenPosition(0).getY();
            } else if (action == 3) {
                this.move.nowX = touchEvent.getPointerScreenPosition(0).getX();
                this.move.nowY = touchEvent.getPointerScreenPosition(0).getY();
                Move move2 = this.move;
                move2.tranX = move2.nowX - this.move.lastX;
                Move move3 = this.move;
                move3.tranY = move3.nowY - this.move.lastY;
                WindowManager.LayoutParams layoutParams = this.mAndroidParam;
                if (layoutParams == null) {
                    HiLog.error(LABEL, "handleMovable mAndroidParam is null", new Object[0]);
                    return;
                }
                if (this.boundRect != null) {
                    this.mAndroidWindow.getDecorView().getLocationOnScreen(this.move.location);
                    if (((float) this.boundRect.left) <= ((float) this.move.location[0]) + this.move.tranX && ((float) this.boundRect.right) >= ((float) this.move.location[0]) + this.move.tranX + ((float) this.mAndroidParam.width) && ((float) this.boundRect.top) <= ((float) this.move.location[1]) + this.move.tranY && ((float) this.boundRect.bottom) >= ((float) this.move.location[1]) + this.move.tranY + ((float) this.mAndroidParam.height)) {
                        WindowManager.LayoutParams layoutParams2 = this.mAndroidParam;
                        layoutParams2.x = (int) (((float) layoutParams2.x) + this.move.tranX);
                        WindowManager.LayoutParams layoutParams3 = this.mAndroidParam;
                        layoutParams3.y = (int) (((float) layoutParams3.y) + this.move.tranY);
                        this.mAndroidWindow.setAttributes(this.mAndroidParam);
                    }
                } else {
                    layoutParams.x = (int) (((float) layoutParams.x) + this.move.tranX);
                    WindowManager.LayoutParams layoutParams4 = this.mAndroidParam;
                    layoutParams4.y = (int) (((float) layoutParams4.y) + this.move.tranY);
                    this.mAndroidWindow.setAttributes(this.mAndroidParam);
                }
                Move move4 = this.move;
                move4.lastX = move4.nowX;
                Move move5 = this.move;
                move5.lastY = move5.nowY;
            }
        }
    }

    private void adjustLayout(int i) {
        Context context = this.mContext;
        if (context != null && context.getUITaskDispatcher() != null && i == 2) {
            this.mContext.getUITaskDispatcher().delayDispatch(new Runnable() {
                /* class ohos.agp.window.wmc.AGPWindow.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    if (AGPWindow.this.mViewGroup != null && AGPWindow.this.mSurfaceView != null) {
                        Component findFocus = AGPWindow.this.mViewGroup.findFocus();
                        if (findFocus instanceof Text) {
                            Text text = (Text) findFocus;
                            if (text.isAdjustInputPanel()) {
                                int inputMethodDisplayHeight = InputMethodController.getInstance().getInputMethodDisplayHeight();
                                int actualHeight = AGPWindow.this.mSurfaceView.getActualHeight() - text.getLocationOnScreen()[1];
                                HiLog.debug(AGPWindow.LABEL, "Focus top=%{public}d", new Object[]{Integer.valueOf(text.getLocationOnScreen()[1])});
                                if (actualHeight < inputMethodDisplayHeight) {
                                    AGPWindow.this.mViewGroup.setHeight(AGPWindow.this.mViewGroup.getHeight() - inputMethodDisplayHeight);
                                    HiLog.debug(AGPWindow.LABEL, "ViewGroup reduces, height=%{public}d", new Object[]{Integer.valueOf(AGPWindow.this.mViewGroup.getHeight())});
                                    AGPWindow.this.viewGroupResizedFlag = true;
                                    AGPWindow.this.preInputHeight = inputMethodDisplayHeight;
                                } else if (AGPWindow.this.preInputHeight > 0 && inputMethodDisplayHeight == 0 && AGPWindow.this.viewGroupResizedFlag) {
                                    AGPWindow.this.mViewGroup.setHeight(AGPWindow.this.mViewGroup.getHeight() + AGPWindow.this.preInputHeight);
                                    HiLog.debug(AGPWindow.LABEL, "ViewGroup enlarged, height=%{public}d", new Object[]{Integer.valueOf(AGPWindow.this.mViewGroup.getHeight())});
                                    AGPWindow.this.preInputHeight = 0;
                                    AGPWindow.this.viewGroupResizedFlag = false;
                                }
                            }
                        }
                    }
                }
            }, GET_INPUTHEIGHT_DELAY);
        }
    }

    private boolean processSwipeDismiss(TouchEvent touchEvent) {
        SwipeManager swipeManager2 = this.swipeManager;
        if (swipeManager2 == null || !swipeManager2.verifySwiping(touchEvent)) {
            return false;
        }
        this.swipeManager.executeSwiping(touchEvent);
        return true;
    }

    private static void setBarrierfreeContext(Context context) {
        mBarrierfreeContext = context;
    }
}
