package ohos.agp.components;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.Attr;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.element.Element;
import ohos.agp.render.Canvas;
import ohos.agp.styles.Style;
import ohos.agp.styles.Value;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.agp.utils.Color;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.MimeData;
import ohos.agp.utils.Point;
import ohos.agp.utils.Rect;
import ohos.app.Context;
import ohos.global.configuration.Configuration;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.SpeechEvent;
import ohos.multimodalinput.event.TouchEvent;

public class Component {
    public static final float DEFAULT_SCALE = 1.0f;
    public static final int DRAG_DOWN = 2;
    public static final int DRAG_HORIZONTAL = 2;
    public static final int DRAG_HORIZONTAL_VERTICAL = 1;
    public static final int DRAG_LEFT = 3;
    public static final int DRAG_RIGHT = 4;
    public static final int DRAG_UP = 1;
    public static final int DRAG_VERTICAL = 3;
    public static final int FOCUSABLE = 1;
    public static final int FOCUSABLE_AUTO = 16;
    public static final int FOCUS_NEXT = 4;
    public static final int FOCUS_PREVIOUS = 5;
    public static final int FOCUS_SIDE_BOTTOM = 3;
    public static final int FOCUS_SIDE_LEFT = 0;
    public static final int FOCUS_SIDE_RIGHT = 2;
    public static final int FOCUS_SIDE_TOP = 1;
    public static final int GONE = 8;
    public static final int HORIZONTAL = 0;
    public static final int INHERITED_MODE = 0;
    public static final int INVISIBLE = 4;
    public static final int NOT_FOCUSABLE = 0;
    private static final int NOT_UI_THREAD = 1;
    public static final int NO_ID = -1;
    public static final int OVAL_MODE = 2;
    private static final int PAIR_MODE = 2;
    protected static final int POSITION_X_INDEX = 0;
    protected static final int POSITION_Y_INDEX = 1;
    public static final int RECT_MODE = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_VIEW");
    public static final int VERTICAL = 1;
    public static final int VISIBLE = 0;
    private static Map<String, BiConsumer<Component, Value>> sStyleMethodMap = new HashMap<String, BiConsumer<Component, Value>>() {
        /* class ohos.agp.components.Component.AnonymousClass1 */

        {
            put(ViewAttrsConstants.WIDTH, $$Lambda$Component$1$hAMy_VgFb9BKb4HQMePz8Kgk_c.INSTANCE);
            put(ViewAttrsConstants.HEIGHT, $$Lambda$Component$1$NtlcPYlUC6ZxjDb934DFgdEqF98.INSTANCE);
            put(ViewAttrsConstants.BACKGROUND_ELEMENT, $$Lambda$Component$1$8ZzEEmhAcUEr40uXuxsrxA5Bb1Y.INSTANCE);
            put(ViewAttrsConstants.FOREGROUND_ELEMENT, $$Lambda$Component$1$pPMKm7uGIO71bKx97TDmZ642s.INSTANCE);
        }
    };
    private AttachStateChangedListener mAttachStateChangedListener;
    protected ViewAttrsConstants mAttrsConstants;
    protected Element mBackgroundElement;
    protected Canvas mCanvasForTaskOverContent;
    protected Canvas mCanvasForTaskUnderContent;
    private ClickedListener mClickedListener;
    private ComponentStateChangedListener mComponentStateChangedListener;
    private ComponentTreeObserver mComponentTreeObserver;
    protected Context mContext;
    private MimeData mDraggableViewClipData;
    private DraggedListener mDraggedListener;
    protected DrawTask mDrawTaskOverContent;
    protected DrawTask mDrawTaskUnderContent;
    private FocusChangedListener mFocusChangedListener;
    protected Element mForegroundElement;
    protected KeyEventListener mKeyEventListener;
    protected ComponentContainer.LayoutConfig mLayoutConfig;
    private LayoutRefreshedListener mLayoutRefreshedListener;
    private LongClickedListener mLongClickedListener;
    protected long mNativeViewPtr;
    protected ArrayList<AvailabilityObserver> mObserverList;
    private final ArrayList<AttachStateChangedListener> mOnAttachStateChangeListeners;
    private OnDragListener mOnDragListener;
    protected ComponentParent mParent;
    protected float[] mPosition;
    protected RotationEventListener mRotationEventListener;
    private ScaledListener mScaledListener;
    private ScrolledListener mScrolledListener;
    private Component mShadowComponent;
    private SpeechEventListener mSpeechEventListener;
    private Object mTag;
    private TouchEventListener mTouchEventListener;

    public interface AttachStateChangedListener {
        void onComponentAttachedToWindow(Component component);

        void onComponentDetachedFromWindow(Component component);
    }

    public interface AvailabilityObserver {
        void onComponentRemoved(Component component);
    }

    public interface ClickedListener {
        void onClick(Component component);
    }

    public interface ComponentStateChangedListener {
        void onComponentStateChanged(Component component, int i);
    }

    public interface DraggedListener {
        void onDragCancel(Component component, DragInfo dragInfo);

        void onDragDown(Component component, DragInfo dragInfo);

        void onDragEnd(Component component, DragInfo dragInfo);

        default boolean onDragPreAccept(Component component, int i) {
            return true;
        }

        void onDragStart(Component component, DragInfo dragInfo);

        void onDragUpdate(Component component, DragInfo dragInfo);
    }

    public interface DrawTask {
        public static final int BETWEEN_BACKGROUND_AND_CONTENT = 1;
        public static final int BETWEEN_CONTENT_AND_FOREGROUND = 2;

        void onDraw(Component component, Canvas canvas);
    }

    public interface FocusChangedListener {
        void onFocusChange(Component component, boolean z);
    }

    public interface KeyEventListener {
        boolean onKeyEvent(Component component, KeyEvent keyEvent);
    }

    public enum LayoutDirection {
        LTR,
        RTL,
        INHERIT,
        LOCALE
    }

    public interface LayoutRefreshedListener {
        void onRefreshed(Component component);
    }

    public interface LongClickedListener {
        void onLongClick(Component component);
    }

    public static class MeasureSpec {
        public static final int AT_MOST = Integer.MIN_VALUE;
        public static final int EXACTLY = 1073741824;
        private static final int MODE_MASK = -1073741824;
        private static final int MODE_SHIFT = 30;
        public static final int UNSPECIFIED = 0;

        public static int getMode(int i) {
            return i & MODE_MASK;
        }

        public static int getSize(int i) {
            return i & 1073741823;
        }

        public static int makeMeasureSpec(int i, int i2) {
            return (i & 1073741823) | (i2 & MODE_MASK);
        }
    }

    public interface OnDragListener {
        boolean onDrag(Component component, DragEvent dragEvent);
    }

    public interface RotationEventListener {
        boolean onRotationEvent(Component component, RotationEvent rotationEvent);
    }

    public interface ScaledListener {
        void onScaleEnd(Component component, ScaleInfo scaleInfo);

        void onScaleStart(Component component, ScaleInfo scaleInfo);

        void onScaleUpdate(Component component, ScaleInfo scaleInfo);
    }

    public interface ScrolledListener {
        void onScrolled(Component component, int i, int i2, int i3, int i4);
    }

    public interface SpeechEventListener {
        boolean onSpeechEvent(Component component, SpeechEvent speechEvent);
    }

    public interface TouchEventListener {
        boolean onTouchEvent(Component component, TouchEvent touchEvent);
    }

    private native void nativeAddDrawTaskOverContent(long j, DrawTask drawTask, long j2);

    private native void nativeAddDrawTaskUnderContent(long j, DrawTask drawTask, long j2);

    private native void nativeAddOnAttachStateChangeCallback(long j, AttachStateChangedListener attachStateChangedListener);

    private native void nativeApplyStyle(long j, long j2);

    private native void nativeClearFocus(long j);

    private native void nativeCreateVoiceEvent(long j, String str, int i, boolean z);

    private native Component nativeFindFocus(long j);

    private native Component nativeFindNextFocus(long j, int i);

    private native boolean nativeFindRequestNextFocus(long j, int i);

    private native float nativeGetAlpha(long j);

    private native String nativeGetBarrierfreeDescription(long j);

    private native int nativeGetBottom(long j);

    private native float[] nativeGetCenterZoomFactor(long j);

    private native boolean nativeGetCentralScrollMode(long j);

    private native String nativeGetContentDescription(long j);

    private native boolean nativeGetContentEnable(long j);

    private native float nativeGetContentPositionX(long j);

    private native float nativeGetContentPositionY(long j);

    private native int nativeGetFocusable(long j);

    private native int nativeGetForegroundGravity(long j);

    private native int nativeGetHeight(long j);

    private native int nativeGetId(long j);

    private native int nativeGetLayoutDirection(long j);

    private native int nativeGetLayoutDirectionResolved(long j);

    private native int nativeGetLeft(long j);

    private native boolean nativeGetLocalVisibleRect(long j, Rect rect);

    private native void nativeGetLocationOnScreen(long j, int[] iArr);

    private native int nativeGetMinHeight(long j);

    private native int nativeGetMinWidth(long j);

    private native int nativeGetMode(long j);

    private native String nativeGetName(long j);

    private native int nativeGetPaddingBottom(long j);

    private native int nativeGetPaddingEnd(long j);

    private native int nativeGetPaddingLeft(long j);

    private native int nativeGetPaddingRight(long j);

    private native int nativeGetPaddingStart(long j);

    private native int nativeGetPaddingTop(long j);

    private native float nativeGetPivotX(long j);

    private native float nativeGetPivotY(long j);

    private native int nativeGetRight(long j);

    private native float nativeGetRotation(long j);

    private native float nativeGetRotationSensitivity(long j);

    private native float nativeGetRotationX(long j);

    private native float nativeGetRotationY(long j);

    private native float nativeGetScaleX(long j);

    private native float nativeGetScaleY(long j);

    private native int nativeGetScrollX(long j);

    private native int nativeGetScrollY(long j);

    private native int nativeGetScrollbarBackgroundColor(long j);

    private native int nativeGetScrollbarColor(long j);

    private native int nativeGetScrollbarFadingDelay(long j);

    private native int nativeGetScrollbarFadingDuration(long j);

    private native float nativeGetScrollbarStartAngle(long j);

    private native float nativeGetScrollbarSweepAngle(long j);

    private native int nativeGetScrollbarThickness(long j);

    private native int nativeGetTop(long j);

    private native float nativeGetTranslationX(long j);

    private native float nativeGetTranslationY(long j);

    private native int nativeGetUserNextFocus(long j, int i);

    private native long nativeGetViewHandle();

    private native int nativeGetVisibility(long j);

    private native int nativeGetWidth(long j);

    private native boolean nativeHasFocus(long j);

    private native int nativeInvalidate(long j);

    private native boolean nativeIsAttachedToWindow(long j);

    private native boolean nativeIsClickable(long j);

    private native boolean nativeIsEnabled(long j);

    private native boolean nativeIsFocusable(long j);

    private native boolean nativeIsFocusableInTouchMode(long j);

    private native boolean nativeIsFocused(long j);

    private native boolean nativeIsHorizontalScrollBarEnabled(long j);

    private native boolean nativeIsLayoutRtl(long j);

    private native boolean nativeIsLongClickable(long j);

    private native boolean nativeIsPressed(long j);

    private native boolean nativeIsScrollbarFadingEnabled(long j);

    private native boolean nativeIsScrollbarOverlapEnabled(long j);

    private native boolean nativeIsSelected(long j);

    private native boolean nativeIsShown(long j);

    private native boolean nativeIsVerticalScrollBarEnabled(long j);

    private native void nativeObjectBind(long j);

    private native boolean nativePerformClick(long j);

    private native boolean nativePerformLongClick(long j);

    private native boolean nativePerformNewDrag(long j);

    private native boolean nativePerformScale(long j);

    private native void nativeRemoveOnAttachStateChangeCallback(long j, int i);

    private native boolean nativeRequestFocus(long j);

    private native void nativeRequestLayout(long j);

    private native void nativeScrollTo(long j, int i, int i2);

    private native void nativeSetAlpha(long j, float f);

    private native void nativeSetBackground(long j, long j2);

    private native void nativeSetBadgeInfo(long j, String[] strArr, String[] strArr2);

    private native void nativeSetBarrierfreeDescription(long j, String str);

    private native void nativeSetBottom(long j, int i);

    private native void nativeSetCenterZoomFactor(long j, float f, float f2);

    private native void nativeSetCentralScrollMode(long j, boolean z);

    private native void nativeSetClickable(long j, boolean z);

    private native void nativeSetClipEnabled(long j, boolean z);

    private native void nativeSetContentDescription(long j, String str);

    private native void nativeSetContentEnable(long j, boolean z);

    private native void nativeSetContentPosition(long j, float f, float f2);

    private native void nativeSetEnabled(long j, boolean z);

    private native void nativeSetFocusable(long j, int i);

    private native void nativeSetFocusableInTouchMode(long j, boolean z);

    private native void nativeSetForeground(long j, long j2);

    private native void nativeSetForegroundGravity(long j, int i);

    private native void nativeSetHorizontalScrollBarEnabled(long j, boolean z);

    private native void nativeSetId(long j, int i);

    private native void nativeSetLayoutDirection(long j, int i);

    private native void nativeSetLeft(long j, int i);

    private native void nativeSetLongClickable(long j, boolean z);

    private native void nativeSetMinHeight(long j, int i);

    private native void nativeSetMinWidth(long j, int i);

    private native void nativeSetMode(long j, int i);

    private native void nativeSetName(long j, String str);

    private native void nativeSetOnClickCallback(long j, ClickedListener clickedListener);

    private native void nativeSetOnDragCallback(long j, int i, DraggedListener draggedListener);

    private native void nativeSetOnFocusChangedCallback(long j, FocusChangedListener focusChangedListener);

    private native void nativeSetOnKeyCallback(long j, KeyEventListener keyEventListener);

    private native void nativeSetOnLayoutRefreshListener(long j, LayoutRefreshedListener layoutRefreshedListener);

    private native void nativeSetOnLongClickCallback(long j, LongClickedListener longClickedListener);

    private native void nativeSetOnRotationCallback(long j, RotationEventListener rotationEventListener);

    private native void nativeSetOnScaleCallback(long j, ScaledListener scaledListener);

    private native void nativeSetOnScrollChangeCallback(long j, ScrolledListener scrolledListener);

    private native void nativeSetOnTouchEventCallback(long j, TouchEventListener touchEventListener);

    private native void nativeSetOnViewStateChangedListener(long j, ComponentStateChangedListener componentStateChangedListener);

    private native void nativeSetOnVoiceEventCallback(long j, SpeechEventListener speechEventListener);

    private native void nativeSetPadding(long j, int i, int i2, int i3, int i4);

    private native void nativeSetPaddingRelative(long j, int i, int i2, int i3, int i4);

    private native void nativeSetPivotX(long j, float f);

    private native void nativeSetPivotY(long j, float f);

    private native void nativeSetPressed(long j, boolean z);

    private native void nativeSetRight(long j, int i);

    private native void nativeSetRotation(long j, float f);

    private native void nativeSetRotationSensitivity(long j, float f);

    private native void nativeSetRotationX(long j, float f);

    private native void nativeSetRotationY(long j, float f);

    private native void nativeSetScaleX(long j, float f);

    private native void nativeSetScaleY(long j, float f);

    private native void nativeSetScrollbarBackgroundColor(long j, int i);

    private native void nativeSetScrollbarColor(long j, int i);

    private native void nativeSetScrollbarFadingDelay(long j, int i);

    private native void nativeSetScrollbarFadingDuration(long j, int i);

    private native void nativeSetScrollbarFadingEnabled(long j, boolean z);

    private native void nativeSetScrollbarOverlapEnabled(long j, boolean z);

    private native void nativeSetScrollbarStartAngle(long j, float f);

    private native void nativeSetScrollbarSweepAngle(long j, float f);

    private native void nativeSetScrollbarThickness(long j, int i);

    private native void nativeSetSelected(long j, boolean z);

    private native void nativeSetSynonyms(long j, String[] strArr);

    private native void nativeSetTop(long j, int i);

    private native void nativeSetTranslationX(long j, float f);

    private native void nativeSetTranslationY(long j, float f);

    private native void nativeSetUserNextFocus(long j, int i, int i2);

    private native void nativeSetVerticalScrollBarEnabled(long j, boolean z);

    private native void nativeSetVisibility(long j, int i);

    private native boolean nativeStartDragAndDrop(long j, long j2);

    private native void nativeSubscribeVoiceEvent(long j);

    private native void nativeUnsubscribeVoiceEvents(long j);

    /* access modifiers changed from: protected */
    public void onRtlPropertiesChanged(LayoutDirection layoutDirection) {
    }

    public void release() {
    }

    static {
        System.loadLibrary("agp.z");
    }

    /* access modifiers changed from: protected */
    public static class ComponentCleaner implements MemoryCleaner {
        protected long mNativePtr;

        private native void nativeViewRelease(long j);

        public ComponentCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeViewRelease(j);
                this.mNativePtr = 0;
            }
        }
    }

    public Component(Context context) {
        this(context, null);
    }

    public Component(Context context, AttrSet attrSet) {
        this(context, attrSet, (String) null);
    }

    public Component(Context context, AttrSet attrSet, String str) {
        this(context, attrSet, 0);
    }

    public Component(Context context, AttrSet attrSet, int i) {
        this.mNativeViewPtr = 0;
        this.mParent = null;
        this.mBackgroundElement = null;
        this.mForegroundElement = null;
        this.mObserverList = new ArrayList<>();
        this.mLayoutConfig = null;
        this.mDrawTaskUnderContent = null;
        this.mCanvasForTaskUnderContent = null;
        this.mDrawTaskOverContent = null;
        this.mCanvasForTaskOverContent = null;
        this.mContext = null;
        this.mKeyEventListener = null;
        this.mRotationEventListener = null;
        this.mComponentTreeObserver = null;
        this.mTag = null;
        this.mClickedListener = null;
        this.mScaledListener = null;
        this.mOnDragListener = null;
        this.mDraggedListener = null;
        this.mFocusChangedListener = null;
        this.mLongClickedListener = null;
        this.mTouchEventListener = null;
        this.mSpeechEventListener = null;
        this.mScrolledListener = null;
        this.mComponentStateChangedListener = null;
        this.mLayoutRefreshedListener = null;
        this.mDraggableViewClipData = null;
        this.mShadowComponent = null;
        this.mOnAttachStateChangeListeners = new ArrayList<>();
        createNativePtr();
        registerCleaner();
        this.mContext = context;
        long j = this.mNativeViewPtr;
        if (j != 0) {
            nativeObjectBind(j);
        }
        if (context == null) {
            HiLog.info(TAG, "Context is null", new Object[0]);
        }
        applyStyle(convertAttrToStyle(AttrHelper.mergeStyle(context, attrSet, i)));
    }

    /* access modifiers changed from: protected */
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ViewAttrsConstants();
        }
        for (int i = 0; i < attrSet.getLength(); i++) {
            attrSet.getAttr(i).ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$Component$yk3OoPuikXHgLWZhm7YQqaNZcU */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Component.this.lambda$convertAttrToStyle$0$Component((Attr) obj);
                }
            });
        }
        return Style.fromAttrSet(attrSet);
    }

    public /* synthetic */ void lambda$convertAttrToStyle$0$Component(Attr attr) {
        if (attr.getType() == Attr.AttrType.NONE) {
            attr.setType(this.mAttrsConstants.getType(attr.getName()));
            attr.setContext(this.mContext);
        }
    }

    public final boolean startDragAndDrop(MimeData mimeData, DragFeedbackProvider dragFeedbackProvider) {
        this.mShadowComponent = dragFeedbackProvider.getComponent();
        Component component = this.mShadowComponent;
        long nativeViewPtr = component != null ? component.getNativeViewPtr() : 0;
        getRoot().mDraggableViewClipData = mimeData;
        return nativeStartDragAndDrop(getNativeViewPtr(), nativeViewPtr);
    }

    private Component getRoot() {
        ComponentParent componentParent = getComponentParent();
        Component component = componentParent instanceof Component ? (Component) componentParent : null;
        while (true) {
            this = component;
            if (this == null) {
                return this;
            }
            ComponentParent componentParent2 = this.getComponentParent();
            if (!(componentParent2 instanceof Component)) {
                return this;
            }
            component = (Component) componentParent2;
        }
    }

    /* access modifiers changed from: protected */
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetViewHandle();
        }
    }

    /* access modifiers changed from: protected */
    public void registerCleaner() {
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new ComponentCleaner(this.mNativeViewPtr), this.mNativeViewPtr);
    }

    public final ComponentParent getComponentParent() {
        return this.mParent;
    }

    /* access modifiers changed from: package-private */
    public void assignParent(ComponentParent componentParent) {
        if (equals(componentParent)) {
            throw new IllegalArgumentException("Not allowed to specify itself as parent");
        } else if (componentParent == null) {
            this.mParent = null;
        } else if (this.mParent == null) {
            this.mParent = componentParent;
        } else {
            throw new IllegalArgumentException("view " + this + " being added, but it already has a parent");
        }
    }

    public void addDrawTask(DrawTask drawTask) {
        addDrawTask(drawTask, 2);
    }

    public void addDrawTask(DrawTask drawTask, int i) {
        HiLog.debug(TAG, "addDrawTask", new Object[0]);
        if (i == 1) {
            this.mDrawTaskUnderContent = drawTask;
            if (this.mCanvasForTaskUnderContent == null) {
                this.mCanvasForTaskUnderContent = new Canvas();
            }
            nativeAddDrawTaskUnderContent(this.mNativeViewPtr, this.mDrawTaskUnderContent, this.mCanvasForTaskUnderContent.getNativePtr());
        } else if (i != 2) {
            HiLog.error(TAG, "addDrawTask fail! Invalid number of layers.", new Object[0]);
        } else {
            this.mDrawTaskOverContent = drawTask;
            if (this.mCanvasForTaskOverContent == null) {
                this.mCanvasForTaskOverContent = new Canvas();
            }
            nativeAddDrawTaskOverContent(this.mNativeViewPtr, this.mDrawTaskOverContent, this.mCanvasForTaskOverContent.getNativePtr());
        }
    }

    public void invalidate() {
        HiLog.debug(TAG, "invalidate", new Object[0]);
        if (nativeInvalidate(this.mNativeViewPtr) == 1) {
            throw new UnsupportedOperationException("view " + this + " unsupported because is not in UI thread.");
        }
    }

    public void setContentEnable(boolean z) {
        HiLog.debug(TAG, "setContentEnable", new Object[0]);
        nativeSetContentEnable(this.mNativeViewPtr, z);
    }

    public boolean getContentEnable() {
        HiLog.debug(TAG, "getContentEnable", new Object[0]);
        return nativeGetContentEnable(this.mNativeViewPtr);
    }

    public AnimatorProperty createAnimatorProperty() {
        return new AnimatorProperty(this);
    }

    public long getNativeViewPtr() {
        return this.mNativeViewPtr;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContentPosition(float f, float f2) {
        nativeSetContentPosition(this.mNativeViewPtr, f, f2);
    }

    public float getContentPositionX() {
        return nativeGetContentPositionX(this.mNativeViewPtr);
    }

    public float getContentPositionY() {
        return nativeGetContentPositionY(this.mNativeViewPtr);
    }

    public float[] getContentPosition() {
        return new float[]{getContentPositionX(), getContentPositionY()};
    }

    public int[] getLocationOnScreen() {
        int[] iArr = new int[2];
        nativeGetLocationOnScreen(this.mNativeViewPtr, iArr);
        return iArr;
    }

    public void setWidth(int i) {
        ComponentContainer.LayoutConfig layoutConfig = this.mLayoutConfig;
        boolean z = true;
        if (layoutConfig == null) {
            this.mLayoutConfig = new ComponentContainer.LayoutConfig();
            this.mLayoutConfig.width = i;
        } else if (layoutConfig.width != i) {
            this.mLayoutConfig.width = i;
        } else {
            z = false;
        }
        if (z) {
            setLayoutConfig(this.mLayoutConfig);
        }
    }

    public int getWidth() {
        return nativeGetWidth(this.mNativeViewPtr);
    }

    public void setHeight(int i) {
        ComponentContainer.LayoutConfig layoutConfig = this.mLayoutConfig;
        boolean z = true;
        if (layoutConfig == null) {
            this.mLayoutConfig = new ComponentContainer.LayoutConfig();
            this.mLayoutConfig.height = i;
        } else if (layoutConfig.height != i) {
            this.mLayoutConfig.height = i;
        } else {
            z = false;
        }
        if (z) {
            setLayoutConfig(this.mLayoutConfig);
        }
    }

    public void setRotationSensitivity(float f) {
        nativeSetRotationSensitivity(this.mNativeViewPtr, f);
    }

    public float getRotationSensitivity() {
        return nativeGetRotationSensitivity(this.mNativeViewPtr);
    }

    public int getHeight() {
        return nativeGetHeight(this.mNativeViewPtr);
    }

    public int getLeft() {
        return nativeGetLeft(this.mNativeViewPtr);
    }

    public void setLeft(int i) {
        nativeSetLeft(this.mNativeViewPtr, i);
    }

    public int getRight() {
        return nativeGetRight(this.mNativeViewPtr);
    }

    public void setRight(int i) {
        nativeSetRight(this.mNativeViewPtr, i);
    }

    public int getTop() {
        return nativeGetTop(this.mNativeViewPtr);
    }

    public void setTop(int i) {
        nativeSetTop(this.mNativeViewPtr, i);
    }

    public int getBottom() {
        return nativeGetBottom(this.mNativeViewPtr);
    }

    public void setBottom(int i) {
        nativeSetBottom(this.mNativeViewPtr, i);
    }

    public void setForegroundGravity(int i) {
        nativeSetForegroundGravity(this.mNativeViewPtr, i);
    }

    public int getForegroundGravity() {
        return nativeGetForegroundGravity(this.mNativeViewPtr);
    }

    public void setLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (layoutConfig != null) {
            try {
                Object clone = layoutConfig.clone();
                ComponentContainer.LayoutConfig layoutConfig2 = null;
                if (clone instanceof ComponentContainer.LayoutConfig) {
                    layoutConfig2 = (ComponentContainer.LayoutConfig) clone;
                }
                if (this.mParent != null) {
                    layoutConfig2 = this.mParent.verifyLayoutConfig(layoutConfig2);
                }
                this.mLayoutConfig = layoutConfig2;
            } catch (CloneNotSupportedException unused) {
                ComponentParent componentParent = this.mParent;
                if (componentParent != null) {
                    layoutConfig = componentParent.verifyLayoutConfig(layoutConfig);
                }
                this.mLayoutConfig = layoutConfig;
            }
            this.mLayoutConfig.applyToComponent(this);
            return;
        }
        throw new NullPointerException("Layout parameters cannot be null for " + this);
    }

    public ComponentContainer.LayoutConfig getLayoutConfig() {
        return this.mLayoutConfig;
    }

    public void setMinimumHeight(int i) {
        nativeSetMinHeight(this.mNativeViewPtr, i);
    }

    public int getMinimumHeight() {
        return nativeGetMinHeight(this.mNativeViewPtr);
    }

    public void setMinimumWidth(int i) {
        nativeSetMinWidth(this.mNativeViewPtr, i);
    }

    public int getMinimumWidth() {
        return nativeGetMinWidth(this.mNativeViewPtr);
    }

    public int getScrollX() {
        return nativeGetScrollX(this.mNativeViewPtr);
    }

    public int getScrollY() {
        return nativeGetScrollY(this.mNativeViewPtr);
    }

    public void setVisibility(int i) {
        nativeSetVisibility(this.mNativeViewPtr, i);
    }

    public int getVisibility() {
        return nativeGetVisibility(this.mNativeViewPtr);
    }

    public void addAvailabilityObserver(AvailabilityObserver availabilityObserver) {
        if (availabilityObserver != null) {
            this.mObserverList.add(availabilityObserver);
        }
    }

    public void removeAvailabilityObserver(AvailabilityObserver availabilityObserver) {
        if (availabilityObserver != null) {
            this.mObserverList.remove(availabilityObserver);
        }
    }

    public void setAccessibilityDescription(String str) {
        nativeSetBarrierfreeDescription(this.mNativeViewPtr, str);
    }

    public String getAccessibilityDescription() {
        return nativeGetBarrierfreeDescription(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    public void notifyAllForRemove() {
        Iterator<AvailabilityObserver> it = this.mObserverList.iterator();
        while (it.hasNext()) {
            it.next().onComponentRemoved(this);
        }
    }

    public boolean isAttachedToWindow() {
        return nativeIsAttachedToWindow(this.mNativeViewPtr);
    }

    public boolean isShown() {
        return nativeIsShown(this.mNativeViewPtr);
    }

    public void setComponentStateChangedListener(ComponentStateChangedListener componentStateChangedListener) {
        this.mComponentStateChangedListener = componentStateChangedListener;
        nativeSetOnViewStateChangedListener(this.mNativeViewPtr, this.mComponentStateChangedListener);
    }

    public void setClickedListener(ClickedListener clickedListener) {
        this.mClickedListener = clickedListener;
        nativeSetOnClickCallback(this.mNativeViewPtr, this.mClickedListener);
    }

    public void setScaledListener(ScaledListener scaledListener) {
        this.mScaledListener = scaledListener;
        nativeSetOnScaleCallback(this.mNativeViewPtr, this.mScaledListener);
    }

    @Deprecated
    public void setOnDragListener(OnDragListener onDragListener) {
        this.mOnDragListener = onDragListener;
    }

    @Deprecated
    public void setDraggedListener(DraggedListener draggedListener) {
        this.mDraggedListener = draggedListener;
        nativeSetOnDragCallback(this.mNativeViewPtr, 1, this.mDraggedListener);
    }

    public void setDraggedListener(int i, DraggedListener draggedListener) {
        this.mDraggedListener = draggedListener;
        nativeSetOnDragCallback(this.mNativeViewPtr, i, this.mDraggedListener);
    }

    private boolean onDragFromNative(Component component, int i, float f, float f2) {
        return onDrag(component, DragEvent.obtain(i, f, f2, this.mDraggableViewClipData));
    }

    @Override // ohos.agp.components.ComponentParent
    public boolean onDrag(Component component, DragEvent dragEvent) {
        boolean z = false;
        if (dragEvent == null) {
            return false;
        }
        if (dragEvent.getAction() == 4 && component == this) {
            this.mShadowComponent = null;
        }
        if (dragEvent.isBroadcast()) {
            OnDragListener onDragListener = this.mOnDragListener;
            if (onDragListener != null) {
                return onDragListener.onDrag(this, dragEvent);
            }
            return false;
        } else if (component == null) {
            return false;
        } else {
            OnDragListener onDragListener2 = component.mOnDragListener;
            if (onDragListener2 != null) {
                z = onDragListener2.onDrag(component, dragEvent);
            }
            if (z || dragEvent.getAction() != 3 || component.getComponentParent() == null) {
                return z;
            }
            ComponentParent componentParent = component.getComponentParent();
            return componentParent instanceof Component ? onDrag((Component) componentParent, dragEvent) : z;
        }
    }

    public void setFocusChangedListener(FocusChangedListener focusChangedListener) {
        this.mFocusChangedListener = focusChangedListener;
        nativeSetOnFocusChangedCallback(this.mNativeViewPtr, this.mFocusChangedListener);
    }

    public void setKeyEventListener(KeyEventListener keyEventListener) {
        this.mKeyEventListener = keyEventListener;
        nativeSetOnKeyCallback(this.mNativeViewPtr, this.mKeyEventListener);
    }

    public void setRotationEventListener(RotationEventListener rotationEventListener) {
        this.mRotationEventListener = rotationEventListener;
        nativeSetOnRotationCallback(this.mNativeViewPtr, this.mRotationEventListener);
    }

    public void setLongClickedListener(LongClickedListener longClickedListener) {
        this.mLongClickedListener = longClickedListener;
        nativeSetOnLongClickCallback(this.mNativeViewPtr, this.mLongClickedListener);
    }

    public void setTouchEventListener(TouchEventListener touchEventListener) {
        this.mTouchEventListener = touchEventListener;
        nativeSetOnTouchEventCallback(this.mNativeViewPtr, this.mTouchEventListener);
    }

    public void setLayoutRefreshedListener(LayoutRefreshedListener layoutRefreshedListener) {
        this.mLayoutRefreshedListener = layoutRefreshedListener;
        nativeSetOnLayoutRefreshListener(this.mNativeViewPtr, this.mLayoutRefreshedListener);
    }

    public void setScrolledListener(ScrolledListener scrolledListener) {
        this.mScrolledListener = scrolledListener;
        nativeSetOnScrollChangeCallback(this.mNativeViewPtr, this.mScrolledListener);
    }

    public boolean performClick() {
        return nativePerformClick(this.mNativeViewPtr);
    }

    public boolean performScale() {
        return nativePerformScale(this.mNativeViewPtr);
    }

    public boolean performDrag() {
        return nativePerformNewDrag(this.mNativeViewPtr);
    }

    public boolean callOnClick() {
        ClickedListener clickedListener = this.mClickedListener;
        if (clickedListener != null) {
            clickedListener.onClick(this);
            return true;
        }
        HiLog.error(TAG, "callOnClick fail, need to setClickedListener.", new Object[0]);
        return false;
    }

    public boolean performLongClick() {
        return nativePerformLongClick(this.mNativeViewPtr);
    }

    public void setClickable(boolean z) {
        nativeSetClickable(this.mNativeViewPtr, z);
    }

    public boolean isClickable() {
        return nativeIsClickable(this.mNativeViewPtr);
    }

    public void setEnabled(boolean z) {
        nativeSetEnabled(this.mNativeViewPtr, z);
    }

    public void setClipEnabled(boolean z) {
        nativeSetClipEnabled(this.mNativeViewPtr, z);
    }

    public boolean isEnabled() {
        return nativeIsEnabled(this.mNativeViewPtr);
    }

    public void setFocusable(int i) {
        nativeSetFocusable(this.mNativeViewPtr, i);
    }

    public void setFocusableInTouchMode(boolean z) {
        nativeSetFocusableInTouchMode(this.mNativeViewPtr, z);
    }

    public int getFocusable() {
        return nativeGetFocusable(this.mNativeViewPtr);
    }

    public void setId(int i) {
        nativeSetId(this.mNativeViewPtr, i);
    }

    public int getId() {
        return nativeGetId(this.mNativeViewPtr);
    }

    public void setName(String str) {
        nativeSetName(this.mNativeViewPtr, str);
    }

    public String getName() {
        return nativeGetName(this.mNativeViewPtr);
    }

    public void setLongClickable(boolean z) {
        nativeSetLongClickable(this.mNativeViewPtr, z);
    }

    public boolean isLongClickable() {
        return nativeIsLongClickable(this.mNativeViewPtr);
    }

    public void setPadding(int i, int i2, int i3, int i4) {
        nativeSetPadding(this.mNativeViewPtr, i, i2, i3, i4);
    }

    public void setPaddingRelative(int i, int i2, int i3, int i4) {
        nativeSetPaddingRelative(this.mNativeViewPtr, i, i2, i3, i4);
    }

    public int getPaddingBottom() {
        return nativeGetPaddingBottom(this.mNativeViewPtr);
    }

    public int getPaddingEnd() {
        return nativeGetPaddingEnd(this.mNativeViewPtr);
    }

    public int getPaddingLeft() {
        return nativeGetPaddingLeft(this.mNativeViewPtr);
    }

    public int getPaddingRight() {
        return nativeGetPaddingRight(this.mNativeViewPtr);
    }

    public int getPaddingStart() {
        return nativeGetPaddingStart(this.mNativeViewPtr);
    }

    public int getPaddingTop() {
        return nativeGetPaddingTop(this.mNativeViewPtr);
    }

    public void setPressed(boolean z) {
        nativeSetPressed(this.mNativeViewPtr, z);
    }

    public boolean isPressed() {
        return nativeIsPressed(this.mNativeViewPtr);
    }

    public void setSelected(boolean z) {
        nativeSetSelected(this.mNativeViewPtr, z);
    }

    public boolean isSelected() {
        return nativeIsSelected(this.mNativeViewPtr);
    }

    public boolean hasFocus() {
        return nativeHasFocus(this.mNativeViewPtr);
    }

    public boolean isFocusable() {
        return nativeIsFocusable(this.mNativeViewPtr);
    }

    public boolean isFocusableInTouchMode() {
        return nativeIsFocusableInTouchMode(this.mNativeViewPtr);
    }

    public boolean isFocused() {
        return nativeIsFocused(this.mNativeViewPtr);
    }

    public boolean requestFocus() {
        return nativeRequestFocus(this.mNativeViewPtr);
    }

    public void clearFocus() {
        nativeClearFocus(this.mNativeViewPtr);
    }

    public void requestLayout() {
        nativeRequestLayout(this.mNativeViewPtr);
    }

    public void addAttachStateChangedListener(AttachStateChangedListener attachStateChangedListener) {
        if (this.mAttachStateChangedListener == null) {
            this.mAttachStateChangedListener = new AttachStateChangedListener() {
                /* class ohos.agp.components.Component.AnonymousClass2 */

                @Override // ohos.agp.components.Component.AttachStateChangedListener
                public void onComponentAttachedToWindow(Component component) {
                    Iterator it = Component.this.mOnAttachStateChangeListeners.iterator();
                    while (it.hasNext()) {
                        ((AttachStateChangedListener) it.next()).onComponentAttachedToWindow(component);
                    }
                }

                @Override // ohos.agp.components.Component.AttachStateChangedListener
                public void onComponentDetachedFromWindow(Component component) {
                    Iterator it = Component.this.mOnAttachStateChangeListeners.iterator();
                    while (it.hasNext()) {
                        ((AttachStateChangedListener) it.next()).onComponentDetachedFromWindow(component);
                    }
                }
            };
            nativeAddOnAttachStateChangeCallback(this.mNativeViewPtr, this.mAttachStateChangedListener);
        }
        this.mOnAttachStateChangeListeners.add(attachStateChangedListener);
    }

    public void removeAttachStateChangedListener(AttachStateChangedListener attachStateChangedListener) {
        this.mOnAttachStateChangeListeners.remove(attachStateChangedListener);
        if (this.mOnAttachStateChangeListeners.isEmpty()) {
            nativeRemoveOnAttachStateChangeCallback(this.mNativeViewPtr, 0);
            this.mAttachStateChangedListener = null;
        }
    }

    public void setBackground(Element element) {
        this.mBackgroundElement = element;
        nativeSetBackground(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public void setForeground(Element element) {
        this.mForegroundElement = element;
        nativeSetForeground(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getBackgroundElement() {
        return this.mBackgroundElement;
    }

    public Element getForegroundElement() {
        return this.mForegroundElement;
    }

    public void setRotation(float f) {
        nativeSetRotation(this.mNativeViewPtr, f);
    }

    public void setRotationX(float f) {
        nativeSetRotationX(this.mNativeViewPtr, f);
    }

    public void setRotationY(float f) {
        nativeSetRotationY(this.mNativeViewPtr, f);
    }

    public void setPivotX(float f) {
        nativeSetPivotX(this.mNativeViewPtr, f);
    }

    public void setPivotY(float f) {
        nativeSetPivotY(this.mNativeViewPtr, f);
    }

    public void setScaleX(float f) {
        nativeSetScaleX(this.mNativeViewPtr, f);
    }

    public void setScaleY(float f) {
        nativeSetScaleY(this.mNativeViewPtr, f);
    }

    public void setTranslationX(float f) {
        nativeSetTranslationX(this.mNativeViewPtr, f);
    }

    public void setTranslationY(float f) {
        nativeSetTranslationY(this.mNativeViewPtr, f);
    }

    public float getRotation() {
        return nativeGetRotation(this.mNativeViewPtr);
    }

    public float getRotationX() {
        return nativeGetRotationX(this.mNativeViewPtr);
    }

    public float getRotationY() {
        return nativeGetRotationY(this.mNativeViewPtr);
    }

    public float getPivotX() {
        return nativeGetPivotX(this.mNativeViewPtr);
    }

    public float getPivotY() {
        return nativeGetPivotY(this.mNativeViewPtr);
    }

    public float getScaleX() {
        return nativeGetScaleX(this.mNativeViewPtr);
    }

    public float getScaleY() {
        return nativeGetScaleY(this.mNativeViewPtr);
    }

    public float getTranslationX() {
        return nativeGetTranslationX(this.mNativeViewPtr);
    }

    public float getTranslationY() {
        return nativeGetTranslationY(this.mNativeViewPtr);
    }

    public ResourceManager getResourceManager() {
        Context context = this.mContext;
        if (context == null) {
            return null;
        }
        return context.getResourceManager();
    }

    public ComponentTreeObserver getComponentTreeObserver() {
        if (this.mComponentTreeObserver == null) {
            this.mComponentTreeObserver = new ComponentTreeObserver(this.mNativeViewPtr);
        }
        return this.mComponentTreeObserver;
    }

    public void scrollTo(int i, int i2) {
        nativeScrollTo(this.mNativeViewPtr, i, i2);
    }

    public boolean getLocalVisibleRect(Rect rect) {
        if (rect == null) {
            return false;
        }
        return nativeGetLocalVisibleRect(this.mNativeViewPtr, rect);
    }

    public void setAlpha(float f) {
        nativeSetAlpha(this.mNativeViewPtr, f);
    }

    public float getAlpha() {
        return nativeGetAlpha(this.mNativeViewPtr);
    }

    public void setContentDescription(CharSequence charSequence) {
        nativeSetContentDescription(this.mNativeViewPtr, charSequence.toString());
    }

    public CharSequence getContentDescription() {
        return nativeGetContentDescription(this.mNativeViewPtr);
    }

    public Component findComponentById(int i) {
        if (getId() == i) {
            return this;
        }
        return null;
    }

    public Component findFocus() {
        return nativeFindFocus(this.mNativeViewPtr);
    }

    public Component findNextFocus(int i) {
        if (i < 0 || i > 5) {
            return null;
        }
        return nativeFindNextFocus(this.mNativeViewPtr, i);
    }

    public boolean findRequestNextFocus(int i) {
        if (i < 0 || i > 5) {
            return false;
        }
        return nativeFindRequestNextFocus(this.mNativeViewPtr, i);
    }

    public void setUserNextFocus(int i, int i2) {
        if (i >= 0 && i <= 5) {
            nativeSetUserNextFocus(this.mNativeViewPtr, i, i2);
        }
    }

    public int getUserNextFocus(int i) {
        if (i < 0 || i > 5) {
            return -1;
        }
        return nativeGetUserNextFocus(this.mNativeViewPtr, i);
    }

    public void subscribeVoiceEvents(VoiceEvent voiceEvent) {
        if (voiceEvent == null || this.mNativeViewPtr == 0) {
            HiLog.error(TAG, "subscribeVoiceEvents VoiceEvent or viewPtr is null.", new Object[0]);
        } else if (createVoiceEvent(voiceEvent.getSpeech(), voiceEvent.getScene(), true)) {
            setEventSynonyms(voiceEvent.getSynonyms());
            setEventBadges(voiceEvent.getBadge());
            HiLog.debug(TAG, "VoiceEvent sendDataToView", new Object[0]);
            nativeSubscribeVoiceEvent(this.mNativeViewPtr);
        }
    }

    public void unsubscribeVoiceEvents() {
        nativeUnsubscribeVoiceEvents(this.mNativeViewPtr);
    }

    public void setSpeechEventListener(SpeechEventListener speechEventListener) {
        this.mSpeechEventListener = speechEventListener;
        nativeSetOnVoiceEventCallback(this.mNativeViewPtr, this.mSpeechEventListener);
    }

    public void setTag(Object obj) {
        this.mTag = obj;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setHorizontalScrollBarEnabled(boolean z) {
        nativeSetHorizontalScrollBarEnabled(this.mNativeViewPtr, z);
    }

    public boolean isHorizontalScrollBarEnabled() {
        return nativeIsHorizontalScrollBarEnabled(this.mNativeViewPtr);
    }

    public void setVerticalScrollBarEnabled(boolean z) {
        nativeSetVerticalScrollBarEnabled(this.mNativeViewPtr, z);
    }

    public boolean isVerticalScrollBarEnabled() {
        return nativeIsVerticalScrollBarEnabled(this.mNativeViewPtr);
    }

    public void setScrollbarFadingEnabled(boolean z) {
        nativeSetScrollbarFadingEnabled(this.mNativeViewPtr, z);
    }

    public boolean isScrollbarFadingEnabled() {
        return nativeIsScrollbarFadingEnabled(this.mNativeViewPtr);
    }

    public void setScrollbarFadingDelay(int i) {
        nativeSetScrollbarFadingDelay(this.mNativeViewPtr, i);
    }

    public int getScrollbarFadingDelay() {
        return nativeGetScrollbarFadingDelay(this.mNativeViewPtr);
    }

    public void setScrollbarFadingDuration(int i) {
        nativeSetScrollbarFadingDuration(this.mNativeViewPtr, i);
    }

    public int getScrollbarFadingDuration() {
        return nativeGetScrollbarFadingDuration(this.mNativeViewPtr);
    }

    public void setScrollbarColor(Color color) {
        nativeSetScrollbarColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getScrollbarColor() {
        return new Color(nativeGetScrollbarColor(this.mNativeViewPtr));
    }

    public void setScrollbarBackgroundColor(Color color) {
        nativeSetScrollbarBackgroundColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getScrollbarBackgroundColor() {
        return new Color(nativeGetScrollbarBackgroundColor(this.mNativeViewPtr));
    }

    public void setScrollbarThickness(int i) {
        nativeSetScrollbarThickness(this.mNativeViewPtr, i);
    }

    public int getScrollbarThickness() {
        return nativeGetScrollbarThickness(this.mNativeViewPtr);
    }

    public void setScrollbarStartAngle(float f) {
        nativeSetScrollbarStartAngle(this.mNativeViewPtr, f);
    }

    public float getScrollbarStartAngle() {
        return nativeGetScrollbarStartAngle(this.mNativeViewPtr);
    }

    public void setScrollbarSweepAngle(float f) {
        nativeSetScrollbarSweepAngle(this.mNativeViewPtr, f);
    }

    public float getScrollbarSweepAngle() {
        return nativeGetScrollbarSweepAngle(this.mNativeViewPtr);
    }

    public void setScrollbarOverlapEnabled(boolean z) {
        nativeSetScrollbarOverlapEnabled(this.mNativeViewPtr, z);
    }

    public boolean isScrollbarOverlapEnabled() {
        return nativeIsScrollbarOverlapEnabled(this.mNativeViewPtr);
    }

    public boolean isLayoutRtl() {
        return nativeIsLayoutRtl(this.mNativeViewPtr);
    }

    public void applyStyle(Style style) {
        for (Map.Entry<String, BiConsumer<Component, Value>> entry : sStyleMethodMap.entrySet()) {
            if (style.hasProperty(entry.getKey())) {
                Optional.ofNullable(sStyleMethodMap.get(entry.getKey())).ifPresent(new Consumer(style, entry) {
                    /* class ohos.agp.components.$$Lambda$Component$Zp_33GdIPLZscgd_yWbMUGuAWo */
                    private final /* synthetic */ Style f$1;
                    private final /* synthetic */ Map.Entry f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Component.this.lambda$applyStyle$1$Component(this.f$1, this.f$2, (BiConsumer) obj);
                    }
                });
            }
        }
        nativeApplyStyle(this.mNativeViewPtr, style.getNativeStylePtr());
    }

    public /* synthetic */ void lambda$applyStyle$1$Component(Style style, Map.Entry entry, BiConsumer biConsumer) {
        biConsumer.accept(this, style.getPropertyValue((String) entry.getKey()));
    }

    public void setCentralScrollMode(boolean z) {
        nativeSetCentralScrollMode(this.mNativeViewPtr, z);
    }

    public boolean getCentralScrollMode() {
        return nativeGetCentralScrollMode(this.mNativeViewPtr);
    }

    public void setMode(int i) {
        nativeSetMode(this.mNativeViewPtr, i);
    }

    public int getMode() {
        return nativeGetMode(this.mNativeViewPtr);
    }

    public void setCenterZoomFactor(float f, float f2) {
        nativeSetCenterZoomFactor(this.mNativeViewPtr, f, f2);
    }

    public float[] getCenterZoomFactor() {
        return nativeGetCenterZoomFactor(this.mNativeViewPtr);
    }

    public void dispatchConfigurationChanged(Configuration configuration) {
        HiLog.debug(TAG, "dispatchConfigurationChanged enter", new Object[0]);
        onConfigurationChanged(configuration);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        HiLog.debug(TAG, "onConfigurationChanged enter", new Object[0]);
    }

    public void setLayoutDirection(LayoutDirection layoutDirection) {
        nativeSetLayoutDirection(this.mNativeViewPtr, layoutDirection.ordinal());
    }

    public LayoutDirection getLayoutDirection() {
        int nativeGetLayoutDirection = nativeGetLayoutDirection(this.mNativeViewPtr);
        if (nativeGetLayoutDirection == 0) {
            return LayoutDirection.LTR;
        }
        if (nativeGetLayoutDirection == 1) {
            return LayoutDirection.RTL;
        }
        if (nativeGetLayoutDirection != 2) {
            return LayoutDirection.LOCALE;
        }
        return LayoutDirection.INHERIT;
    }

    public LayoutDirection getLayoutDirectionResolved() {
        return nativeGetLayoutDirectionResolved(this.mNativeViewPtr) == LayoutDirection.LTR.ordinal() ? LayoutDirection.LTR : LayoutDirection.RTL;
    }

    private void resolveLayoutParamsFromNative(int i) {
        ComponentContainer.LayoutConfig layoutConfig = this.mLayoutConfig;
        if (layoutConfig != null) {
            layoutConfig.resolveLayoutDirection(i != LayoutDirection.LTR.ordinal() ? LayoutDirection.RTL : LayoutDirection.LTR);
        }
    }

    private void onRtlPropertiesChangedFromNative(int i) {
        onRtlPropertiesChanged(i == LayoutDirection.LTR.ordinal() ? LayoutDirection.LTR : LayoutDirection.RTL);
    }

    private boolean isLayoutRtlFromContext() {
        ResourceManager resourceManager;
        Configuration configuration;
        Context context = this.mContext;
        if (context == null || (resourceManager = context.getResourceManager()) == null || (configuration = resourceManager.getConfiguration()) == null) {
            return false;
        }
        return configuration.isLayoutRTL;
    }

    private boolean createVoiceEvent(String str, int i, boolean z) {
        if (str == null || this.mNativeViewPtr == 0) {
            return false;
        }
        HiLog.debug(TAG, "createVoiceEvent enter", new Object[0]);
        nativeCreateVoiceEvent(this.mNativeViewPtr, str, i, z);
        return true;
    }

    private void setEventSynonyms(List<String> list) {
        if (list != null && this.mNativeViewPtr != 0 && list.size() != 0) {
            HiLog.debug(TAG, "createVoiceEvent", new Object[0]);
            nativeSetSynonyms(this.mNativeViewPtr, (String[]) list.toArray(new String[list.size()]));
        }
    }

    private void setEventBadges(List<String[]> list) {
        if (!(list == null || this.mNativeViewPtr == 0 || list.size() == 0)) {
            int size = list.size();
            String[] strArr = new String[size];
            String[] strArr2 = new String[size];
            for (int i = 0; i < size; i++) {
                String[] strArr3 = list.get(i);
                strArr[i] = strArr3[0];
                strArr2[i] = strArr3[1];
            }
            HiLog.debug(TAG, "setEventBadges", new Object[0]);
            nativeSetBadgeInfo(this.mNativeViewPtr, strArr, strArr2);
        }
    }

    public static class DragFeedbackProvider {
        private WeakReference<Component> mComponentWeakReference = null;

        public DragFeedbackProvider(Component component) {
            if (component != null) {
                this.mComponentWeakReference = new WeakReference<>(component);
            }
        }

        public void onProvideShadowMetrics(Point point, Point point2) {
            Component component = getComponent();
            if (component != null) {
                component.setWidth((int) point.position[0]);
                component.setHeight((int) point.position[1]);
                component.setLeft((int) point2.position[0]);
                component.setTop((int) point2.position[1]);
                return;
            }
            HiLog.error(Component.TAG, "fail! Asked for drag thumb metrics but no view", new Object[0]);
        }

        public final Component getComponent() {
            WeakReference<Component> weakReference = this.mComponentWeakReference;
            if (weakReference == null) {
                return null;
            }
            return weakReference.get();
        }
    }

    public static class VoiceEvent {
        private boolean isBadge;
        private List<String[]> mBadges;
        private int mScene;
        private String mSpeech;
        private List<String> mSynonyms;

        public VoiceEvent(String str, int i, boolean z) {
            this.mSpeech = str;
            this.mScene = i;
            this.isBadge = z;
        }

        public VoiceEvent(String str) {
            this(str, 0, false);
        }

        public void addSynonyms(String str) {
            if (this.mSynonyms == null) {
                this.mSynonyms = new ArrayList();
            }
            this.mSynonyms.add(str);
        }

        public void setScene(int i) {
            this.mScene = i;
        }

        public void setBadge(boolean z) {
            this.isBadge = z;
        }

        public void addBadges(String str, String str2) {
            if (str != null && str2 != null) {
                String[] strArr = {str, str2};
                if (this.mBadges == null) {
                    this.mBadges = new ArrayList();
                }
                this.mBadges.add(strArr);
            }
        }

        public String getSpeech() {
            return this.mSpeech;
        }

        public List<String> getSynonyms() {
            return this.mSynonyms;
        }

        public List<String[]> getBadge() {
            return this.mBadges;
        }

        public int getScene() {
            return this.mScene;
        }

        public void sendDataToComponent(Component component) {
            HiLog.debug(Component.TAG, "VoiceEvent sendDataToView", new Object[0]);
            component.subscribeVoiceEvents(this);
        }
    }
}
