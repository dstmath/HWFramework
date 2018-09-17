package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Transition.TransitionListenerAdapter;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;
import java.util.Map;

public class ChangeBounds extends Transition {
    private static final Property<View, PointF> BOTTOM_RIGHT_ONLY_PROPERTY = null;
    private static final Property<ViewBounds, PointF> BOTTOM_RIGHT_PROPERTY = null;
    private static final Property<Drawable, PointF> DRAWABLE_ORIGIN_PROPERTY = null;
    private static final String LOG_TAG = "ChangeBounds";
    private static final Property<View, PointF> POSITION_PROPERTY = null;
    private static final String PROPNAME_BOUNDS = "android:changeBounds:bounds";
    private static final String PROPNAME_CLIP = "android:changeBounds:clip";
    private static final String PROPNAME_PARENT = "android:changeBounds:parent";
    private static final String PROPNAME_WINDOW_X = "android:changeBounds:windowX";
    private static final String PROPNAME_WINDOW_Y = "android:changeBounds:windowY";
    private static final Property<View, PointF> TOP_LEFT_ONLY_PROPERTY = null;
    private static final Property<ViewBounds, PointF> TOP_LEFT_PROPERTY = null;
    private static RectEvaluator sRectEvaluator;
    private static final String[] sTransitionProperties = null;
    boolean mReparent;
    boolean mResizeClip;
    int[] tempLocation;

    /* renamed from: android.transition.ChangeBounds.10 */
    class AnonymousClass10 extends AnimatorListenerAdapter {
        final /* synthetic */ BitmapDrawable val$drawable;
        final /* synthetic */ ViewGroup val$sceneRoot;
        final /* synthetic */ float val$transitionAlpha;
        final /* synthetic */ View val$view;

        AnonymousClass10(ViewGroup val$sceneRoot, BitmapDrawable val$drawable, View val$view, float val$transitionAlpha) {
            this.val$sceneRoot = val$sceneRoot;
            this.val$drawable = val$drawable;
            this.val$view = val$view;
            this.val$transitionAlpha = val$transitionAlpha;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$sceneRoot.getOverlay().remove(this.val$drawable);
            this.val$view.setTransitionAlpha(this.val$transitionAlpha);
        }
    }

    /* renamed from: android.transition.ChangeBounds.1 */
    static class AnonymousClass1 extends Property<Drawable, PointF> {
        private Rect mBounds;

        AnonymousClass1(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
            this.mBounds = new Rect();
        }

        public void set(Drawable object, PointF value) {
            object.copyBounds(this.mBounds);
            this.mBounds.offsetTo(Math.round(value.x), Math.round(value.y));
            object.setBounds(this.mBounds);
        }

        public PointF get(Drawable object) {
            object.copyBounds(this.mBounds);
            return new PointF((float) this.mBounds.left, (float) this.mBounds.top);
        }
    }

    /* renamed from: android.transition.ChangeBounds.2 */
    static class AnonymousClass2 extends Property<ViewBounds, PointF> {
        AnonymousClass2(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void set(ViewBounds viewBounds, PointF topLeft) {
            viewBounds.setTopLeft(topLeft);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds.3 */
    static class AnonymousClass3 extends Property<ViewBounds, PointF> {
        AnonymousClass3(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void set(ViewBounds viewBounds, PointF bottomRight) {
            viewBounds.setBottomRight(bottomRight);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds.4 */
    static class AnonymousClass4 extends Property<View, PointF> {
        AnonymousClass4(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void set(View view, PointF bottomRight) {
            view.setLeftTopRightBottom(view.getLeft(), view.getTop(), Math.round(bottomRight.x), Math.round(bottomRight.y));
        }

        public PointF get(View view) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds.5 */
    static class AnonymousClass5 extends Property<View, PointF> {
        AnonymousClass5(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void set(View view, PointF topLeft) {
            view.setLeftTopRightBottom(Math.round(topLeft.x), Math.round(topLeft.y), view.getRight(), view.getBottom());
        }

        public PointF get(View view) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds.6 */
    static class AnonymousClass6 extends Property<View, PointF> {
        AnonymousClass6(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public void set(View view, PointF topLeft) {
            int left = Math.round(topLeft.x);
            int top = Math.round(topLeft.y);
            view.setLeftTopRightBottom(left, top, left + view.getWidth(), top + view.getHeight());
        }

        public PointF get(View view) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds.7 */
    class AnonymousClass7 extends AnimatorListenerAdapter {
        private ViewBounds mViewBounds;
        final /* synthetic */ ViewBounds val$viewBounds;

        AnonymousClass7(ViewBounds val$viewBounds) {
            this.val$viewBounds = val$viewBounds;
            this.mViewBounds = this.val$viewBounds;
        }
    }

    /* renamed from: android.transition.ChangeBounds.8 */
    class AnonymousClass8 extends AnimatorListenerAdapter {
        private boolean mIsCanceled;
        final /* synthetic */ int val$endBottom;
        final /* synthetic */ int val$endLeft;
        final /* synthetic */ int val$endRight;
        final /* synthetic */ int val$endTop;
        final /* synthetic */ Rect val$finalClip;
        final /* synthetic */ View val$view;

        AnonymousClass8(View val$view, Rect val$finalClip, int val$endLeft, int val$endTop, int val$endRight, int val$endBottom) {
            this.val$view = val$view;
            this.val$finalClip = val$finalClip;
            this.val$endLeft = val$endLeft;
            this.val$endTop = val$endTop;
            this.val$endRight = val$endRight;
            this.val$endBottom = val$endBottom;
        }

        public void onAnimationCancel(Animator animation) {
            this.mIsCanceled = true;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mIsCanceled) {
                this.val$view.setClipBounds(this.val$finalClip);
                this.val$view.setLeftTopRightBottom(this.val$endLeft, this.val$endTop, this.val$endRight, this.val$endBottom);
            }
        }
    }

    /* renamed from: android.transition.ChangeBounds.9 */
    class AnonymousClass9 extends TransitionListenerAdapter {
        boolean mCanceled;
        final /* synthetic */ ViewGroup val$parent;

        AnonymousClass9(ViewGroup val$parent) {
            this.val$parent = val$parent;
            this.mCanceled = false;
        }

        public void onTransitionCancel(Transition transition) {
            this.val$parent.suppressLayout(false);
            this.mCanceled = true;
        }

        public void onTransitionEnd(Transition transition) {
            if (!this.mCanceled) {
                this.val$parent.suppressLayout(false);
            }
        }

        public void onTransitionPause(Transition transition) {
            this.val$parent.suppressLayout(false);
        }

        public void onTransitionResume(Transition transition) {
            this.val$parent.suppressLayout(true);
        }
    }

    private static class ViewBounds {
        private int mBottom;
        private boolean mIsBottomRightSet;
        private boolean mIsTopLeftSet;
        private int mLeft;
        private int mRight;
        private int mTop;
        private View mView;

        public ViewBounds(View view) {
            this.mView = view;
        }

        public void setTopLeft(PointF topLeft) {
            this.mLeft = Math.round(topLeft.x);
            this.mTop = Math.round(topLeft.y);
            this.mIsTopLeftSet = true;
            if (this.mIsBottomRightSet) {
                setLeftTopRightBottom();
            }
        }

        public void setBottomRight(PointF bottomRight) {
            this.mRight = Math.round(bottomRight.x);
            this.mBottom = Math.round(bottomRight.y);
            this.mIsBottomRightSet = true;
            if (this.mIsTopLeftSet) {
                setLeftTopRightBottom();
            }
        }

        private void setLeftTopRightBottom() {
            this.mView.setLeftTopRightBottom(this.mLeft, this.mTop, this.mRight, this.mBottom);
            this.mIsTopLeftSet = false;
            this.mIsBottomRightSet = false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.ChangeBounds.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.ChangeBounds.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.<clinit>():void");
    }

    public ChangeBounds() {
        this.tempLocation = new int[2];
        this.mResizeClip = false;
        this.mReparent = false;
    }

    public ChangeBounds(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.tempLocation = new int[2];
        this.mResizeClip = false;
        this.mReparent = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeBounds);
        boolean resizeClip = a.getBoolean(0, false);
        a.recycle();
        setResizeClip(resizeClip);
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public void setResizeClip(boolean resizeClip) {
        this.mResizeClip = resizeClip;
    }

    public boolean getResizeClip() {
        return this.mResizeClip;
    }

    public void setReparent(boolean reparent) {
        this.mReparent = reparent;
    }

    private void captureValues(TransitionValues values) {
        View view = values.view;
        if (!view.isLaidOut() && view.getWidth() == 0) {
            if (view.getHeight() == 0) {
                return;
            }
        }
        values.values.put(PROPNAME_BOUNDS, new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
        values.values.put(PROPNAME_PARENT, values.view.getParent());
        if (this.mReparent) {
            values.view.getLocationInWindow(this.tempLocation);
            values.values.put(PROPNAME_WINDOW_X, Integer.valueOf(this.tempLocation[0]));
            values.values.put(PROPNAME_WINDOW_Y, Integer.valueOf(this.tempLocation[1]));
        }
        if (this.mResizeClip) {
            values.values.put(PROPNAME_CLIP, view.getClipBounds());
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private boolean parentMatches(View startParent, View endParent) {
        if (!this.mReparent) {
            return true;
        }
        TransitionValues endValues = getMatchedTransitionValues(startParent, true);
        return endValues == null ? startParent == endParent : endParent == endValues.view;
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        Map<String, Object> startParentVals = startValues.values;
        View startParent = (ViewGroup) startParentVals.get(PROPNAME_PARENT);
        Map<String, Object> map = endValues.values;
        View endParent = (ViewGroup) endParentVals.get(PROPNAME_PARENT);
        if (startParent == null || endParent == null) {
            return null;
        }
        View view = endValues.view;
        if (parentMatches(startParent, endParent)) {
            ViewBounds viewBounds;
            ObjectAnimator topLeftAnimator;
            ObjectAnimator bottomRightAnimator;
            Animator set;
            Animator anim;
            int i;
            int i2;
            Animator positionAnimator;
            Rect finalClip;
            Rect rect;
            Rect endClip;
            Animator animator;
            ViewGroup parent;
            Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
            Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
            int startLeft = startBounds.left;
            int endLeft = endBounds.left;
            int startTop = startBounds.top;
            int endTop = endBounds.top;
            int startRight = startBounds.right;
            int endRight = endBounds.right;
            int startBottom = startBounds.bottom;
            int endBottom = endBounds.bottom;
            int startWidth = startRight - startLeft;
            int startHeight = startBottom - startTop;
            int endWidth = endRight - endLeft;
            int endHeight = endBottom - endTop;
            Rect startClip = (Rect) startValues.values.get(PROPNAME_CLIP);
            Rect endClip2 = (Rect) endValues.values.get(PROPNAME_CLIP);
            int numChanges = 0;
            if (startWidth == 0 || startHeight == 0) {
                if (!(endWidth == 0 || endHeight == 0)) {
                }
                if (!(startClip == null || startClip.equals(endClip2)) || (startClip == null && endClip2 != null)) {
                    numChanges++;
                }
                if (numChanges > 0) {
                    if (this.mResizeClip) {
                        view.setLeftTopRightBottom(startLeft, startTop, startRight, startBottom);
                        if (numChanges != 2) {
                            if (startWidth == endWidth || startHeight != endHeight) {
                                viewBounds = new ViewBounds(view);
                                viewBounds = viewBounds;
                                topLeftAnimator = ObjectAnimator.ofObject(viewBounds, TOP_LEFT_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                                viewBounds = viewBounds;
                                bottomRightAnimator = ObjectAnimator.ofObject(viewBounds, BOTTOM_RIGHT_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                                set = new AnimatorSet();
                                set.playTogether(new Animator[]{topLeftAnimator, bottomRightAnimator});
                                anim = set;
                                set.addListener(new AnonymousClass7(viewBounds));
                            } else {
                                anim = ObjectAnimator.ofObject(view, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                            }
                        } else if (startLeft == endLeft || startTop != endTop) {
                            anim = ObjectAnimator.ofObject(view, TOP_LEFT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                        } else {
                            anim = ObjectAnimator.ofObject(view, BOTTOM_RIGHT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                        }
                    } else {
                        i = startLeft;
                        i2 = startTop;
                        view.setLeftTopRightBottom(i, i2, startLeft + Math.max(startWidth, endWidth), startTop + Math.max(startHeight, endHeight));
                        positionAnimator = null;
                        if (!(startLeft == endLeft && startTop == endTop)) {
                            positionAnimator = ObjectAnimator.ofObject(view, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                        }
                        finalClip = endClip2;
                        if (startClip == null) {
                            rect = new Rect(0, 0, startWidth, startHeight);
                        }
                        if (endClip2 != null) {
                            rect = new Rect(0, 0, endWidth, endHeight);
                        } else {
                            endClip = endClip2;
                        }
                        animator = null;
                        if (!startClip.equals(endClip)) {
                            view.setClipBounds(startClip);
                            animator = ObjectAnimator.ofObject(view, "clipBounds", sRectEvaluator, new Object[]{startClip, endClip});
                            animator.addListener(new AnonymousClass8(view, endClip2, endLeft, endTop, endRight, endBottom));
                        }
                        anim = TransitionUtils.mergeAnimators(positionAnimator, animator);
                        endClip2 = endClip;
                    }
                    if (view.getParent() instanceof ViewGroup) {
                        parent = (ViewGroup) view.getParent();
                        parent.suppressLayout(true);
                        addListener(new AnonymousClass9(parent));
                    }
                    return anim;
                }
            }
            if (!(startLeft == endLeft && startTop == endTop)) {
                numChanges = 1;
            }
            if (!(startRight == endRight && startBottom == endBottom)) {
                numChanges++;
            }
            numChanges++;
            if (numChanges > 0) {
                if (this.mResizeClip) {
                    i = startLeft;
                    i2 = startTop;
                    view.setLeftTopRightBottom(i, i2, startLeft + Math.max(startWidth, endWidth), startTop + Math.max(startHeight, endHeight));
                    positionAnimator = null;
                    positionAnimator = ObjectAnimator.ofObject(view, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                    finalClip = endClip2;
                    if (startClip == null) {
                        rect = new Rect(0, 0, startWidth, startHeight);
                    }
                    if (endClip2 != null) {
                        endClip = endClip2;
                    } else {
                        rect = new Rect(0, 0, endWidth, endHeight);
                    }
                    animator = null;
                    if (startClip.equals(endClip)) {
                        view.setClipBounds(startClip);
                        animator = ObjectAnimator.ofObject(view, "clipBounds", sRectEvaluator, new Object[]{startClip, endClip});
                        animator.addListener(new AnonymousClass8(view, endClip2, endLeft, endTop, endRight, endBottom));
                    }
                    anim = TransitionUtils.mergeAnimators(positionAnimator, animator);
                    endClip2 = endClip;
                } else {
                    view.setLeftTopRightBottom(startLeft, startTop, startRight, startBottom);
                    if (numChanges != 2) {
                        if (startLeft == endLeft) {
                        }
                        anim = ObjectAnimator.ofObject(view, TOP_LEFT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                    } else {
                        if (startWidth == endWidth) {
                        }
                        viewBounds = new ViewBounds(view);
                        viewBounds = viewBounds;
                        topLeftAnimator = ObjectAnimator.ofObject(viewBounds, TOP_LEFT_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                        viewBounds = viewBounds;
                        bottomRightAnimator = ObjectAnimator.ofObject(viewBounds, BOTTOM_RIGHT_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                        set = new AnimatorSet();
                        set.playTogether(new Animator[]{topLeftAnimator, bottomRightAnimator});
                        anim = set;
                        set.addListener(new AnonymousClass7(viewBounds));
                    }
                }
                if (view.getParent() instanceof ViewGroup) {
                    parent = (ViewGroup) view.getParent();
                    parent.suppressLayout(true);
                    addListener(new AnonymousClass9(parent));
                }
                return anim;
            }
        }
        sceneRoot.getLocationInWindow(this.tempLocation);
        int startX = ((Integer) startValues.values.get(PROPNAME_WINDOW_X)).intValue() - this.tempLocation[0];
        int startY = ((Integer) startValues.values.get(PROPNAME_WINDOW_Y)).intValue() - this.tempLocation[1];
        int endX = ((Integer) endValues.values.get(PROPNAME_WINDOW_X)).intValue() - this.tempLocation[0];
        int endY = ((Integer) endValues.values.get(PROPNAME_WINDOW_Y)).intValue() - this.tempLocation[1];
        if (!(startX == endX && startY == endY)) {
            int width = view.getWidth();
            int height = view.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            view.draw(new Canvas(bitmap));
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(startX, startY, startX + width, startY + height);
            float transitionAlpha = view.getTransitionAlpha();
            view.setTransitionAlpha(0.0f);
            sceneRoot.getOverlay().add(drawable);
            PropertyValuesHolder origin = PropertyValuesHolder.ofObject(DRAWABLE_ORIGIN_PROPERTY, null, getPathMotion().getPath((float) startX, (float) startY, (float) endX, (float) endY));
            ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(drawable, new PropertyValuesHolder[]{origin});
            anim2.addListener(new AnonymousClass10(sceneRoot, drawable, view, transitionAlpha));
            return anim2;
        }
        return null;
    }
}
