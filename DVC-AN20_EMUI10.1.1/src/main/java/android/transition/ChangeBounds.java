package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.animation.TypeConverter;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.BrowserContract;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;
import java.util.Map;

public class ChangeBounds extends Transition {
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static final Property<View, PointF> BOTTOM_RIGHT_ONLY_PROPERTY = new Property<View, PointF>(PointF.class, "bottomRight") {
        /* class android.transition.ChangeBounds.AnonymousClass4 */

        public void set(View view, PointF bottomRight) {
            view.setLeftTopRightBottom(view.getLeft(), view.getTop(), Math.round(bottomRight.x), Math.round(bottomRight.y));
        }

        public PointF get(View view) {
            return null;
        }
    };
    private static final Property<ViewBounds, PointF> BOTTOM_RIGHT_PROPERTY = new Property<ViewBounds, PointF>(PointF.class, "bottomRight") {
        /* class android.transition.ChangeBounds.AnonymousClass3 */

        public void set(ViewBounds viewBounds, PointF bottomRight) {
            viewBounds.setBottomRight(bottomRight);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    };
    private static final Property<Drawable, PointF> DRAWABLE_ORIGIN_PROPERTY = new Property<Drawable, PointF>(PointF.class, "boundsOrigin") {
        /* class android.transition.ChangeBounds.AnonymousClass1 */
        private Rect mBounds = new Rect();

        public void set(Drawable object, PointF value) {
            object.copyBounds(this.mBounds);
            this.mBounds.offsetTo(Math.round(value.x), Math.round(value.y));
            object.setBounds(this.mBounds);
        }

        public PointF get(Drawable object) {
            object.copyBounds(this.mBounds);
            return new PointF((float) this.mBounds.left, (float) this.mBounds.top);
        }
    };
    private static final String LOG_TAG = "ChangeBounds";
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static final Property<View, PointF> POSITION_PROPERTY = new Property<View, PointF>(PointF.class, BrowserContract.Bookmarks.POSITION) {
        /* class android.transition.ChangeBounds.AnonymousClass6 */

        public void set(View view, PointF topLeft) {
            int left = Math.round(topLeft.x);
            int top = Math.round(topLeft.y);
            view.setLeftTopRightBottom(left, top, view.getWidth() + left, view.getHeight() + top);
        }

        public PointF get(View view) {
            return null;
        }
    };
    private static final String PROPNAME_BOUNDS = "android:changeBounds:bounds";
    private static final String PROPNAME_CLIP = "android:changeBounds:clip";
    private static final String PROPNAME_PARENT = "android:changeBounds:parent";
    private static final String PROPNAME_WINDOW_X = "android:changeBounds:windowX";
    private static final String PROPNAME_WINDOW_Y = "android:changeBounds:windowY";
    private static final Property<View, PointF> TOP_LEFT_ONLY_PROPERTY = new Property<View, PointF>(PointF.class, "topLeft") {
        /* class android.transition.ChangeBounds.AnonymousClass5 */

        public void set(View view, PointF topLeft) {
            view.setLeftTopRightBottom(Math.round(topLeft.x), Math.round(topLeft.y), view.getRight(), view.getBottom());
        }

        public PointF get(View view) {
            return null;
        }
    };
    private static final Property<ViewBounds, PointF> TOP_LEFT_PROPERTY = new Property<ViewBounds, PointF>(PointF.class, "topLeft") {
        /* class android.transition.ChangeBounds.AnonymousClass2 */

        public void set(ViewBounds viewBounds, PointF topLeft) {
            viewBounds.setTopLeft(topLeft);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    };
    private static RectEvaluator sRectEvaluator = new RectEvaluator();
    private static final String[] sTransitionProperties = {PROPNAME_BOUNDS, PROPNAME_CLIP, PROPNAME_PARENT, PROPNAME_WINDOW_X, PROPNAME_WINDOW_Y};
    boolean mReparent;
    boolean mResizeClip;
    int[] tempLocation;

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

    @Override // android.transition.Transition
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public void setResizeClip(boolean resizeClip) {
        this.mResizeClip = resizeClip;
    }

    public boolean getResizeClip() {
        return this.mResizeClip;
    }

    @Deprecated
    public void setReparent(boolean reparent) {
        this.mReparent = reparent;
    }

    private void captureValues(TransitionValues values) {
        View view = values.view;
        if (view.isLaidOut() || view.getWidth() != 0 || view.getHeight() != 0) {
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
    }

    @Override // android.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override // android.transition.Transition
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private boolean parentMatches(View startParent, View endParent) {
        if (!this.mReparent) {
            return true;
        }
        boolean parentMatches = true;
        TransitionValues endValues = getMatchedTransitionValues(startParent, true);
        if (endValues == null) {
            if (startParent != endParent) {
                parentMatches = false;
            }
            return parentMatches;
        }
        if (endParent != endValues.view) {
            parentMatches = false;
        }
        return parentMatches;
    }

    /* JADX INFO: Multiple debug info for r11v2 int: [D('startBottom' int), D('startParentVals' java.util.Map<java.lang.String, java.lang.Object>)] */
    /* JADX INFO: Multiple debug info for r12v4 int: [D('endParentVals' java.util.Map<java.lang.String, java.lang.Object>), D('endBottom' int)] */
    /* JADX INFO: Multiple debug info for r13v3 int: [D('startParent' android.view.ViewGroup), D('startWidth' int)] */
    /* JADX INFO: Multiple debug info for r14v3 int: [D('endParent' android.view.ViewGroup), D('startHeight' int)] */
    /* JADX INFO: Multiple debug info for r7v4 int: [D('startBounds' android.graphics.Rect), D('endWidth' int)] */
    /* JADX INFO: Multiple debug info for r6v4 int: [D('endBounds' android.graphics.Rect), D('endHeight' int)] */
    /* JADX INFO: Multiple debug info for r11v5 'clipAnimator'  android.animation.ObjectAnimator: [D('startBottom' int), D('clipAnimator' android.animation.ObjectAnimator)] */
    /* JADX INFO: Multiple debug info for r6v10 android.animation.ObjectAnimator: [D('anim' android.animation.Animator), D('topLeftPath' android.graphics.Path)] */
    /* JADX INFO: Multiple debug info for r6v13 android.animation.ObjectAnimator: [D('bottomRight' android.graphics.Path), D('anim' android.animation.Animator)] */
    /* JADX INFO: Multiple debug info for r6v18 android.animation.ObjectAnimator: [D('anim' android.animation.Animator), D('topLeftPath' android.graphics.Path)] */
    @Override // android.transition.Transition
    public Animator createAnimator(final ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        final Rect endClip;
        Rect startClip;
        ChangeBounds changeBounds;
        final int endLeft;
        int startTop;
        ObjectAnimator positionAnimator;
        Rect startClip2;
        int i;
        Rect endClip2;
        ObjectAnimator positionAnimator2;
        ObjectAnimator clipAnimator;
        View view;
        if (startValues == null) {
            return null;
        }
        if (endValues == null) {
            return null;
        }
        Map<String, Object> startParentVals = startValues.values;
        Map<String, Object> endParentVals = endValues.values;
        ViewGroup startParent = (ViewGroup) startParentVals.get(PROPNAME_PARENT);
        ViewGroup endParent = (ViewGroup) endParentVals.get(PROPNAME_PARENT);
        if (startParent == null) {
            return null;
        }
        if (endParent == null) {
            return null;
        }
        final View view2 = endValues.view;
        if (parentMatches(startParent, endParent)) {
            Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
            Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
            int startLeft = startBounds.left;
            int endLeft2 = endBounds.left;
            int startTop2 = startBounds.top;
            final int endTop = endBounds.top;
            int startRight = startBounds.right;
            final int endRight = endBounds.right;
            int startBottom = startBounds.bottom;
            final int endBottom = endBounds.bottom;
            int startWidth = startRight - startLeft;
            int startHeight = startBottom - startTop2;
            int endWidth = endRight - endLeft2;
            int endHeight = endBottom - endTop;
            Rect startClip3 = (Rect) startValues.values.get(PROPNAME_CLIP);
            Rect endClip3 = (Rect) endValues.values.get(PROPNAME_CLIP);
            int numChanges = 0;
            if (!((startWidth == 0 || startHeight == 0) && (endWidth == 0 || endHeight == 0))) {
                if (!(startLeft == endLeft2 && startTop2 == endTop)) {
                    numChanges = 0 + 1;
                }
                if (!(startRight == endRight && startBottom == endBottom)) {
                    numChanges++;
                }
            }
            if ((startClip3 != null && !startClip3.equals(endClip3)) || (startClip3 == null && endClip3 != null)) {
                numChanges++;
            }
            if (numChanges <= 0) {
                return null;
            }
            if (view2.getParent() instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup) view2.getParent();
                startClip = startClip3;
                parent.suppressLayout(true);
                endClip = endClip3;
                changeBounds = this;
                changeBounds.addListener(new TransitionListenerAdapter() {
                    /* class android.transition.ChangeBounds.AnonymousClass7 */
                    boolean mCanceled = false;

                    @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                    public void onTransitionCancel(Transition transition) {
                        parent.suppressLayout(false);
                        this.mCanceled = true;
                    }

                    @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                    public void onTransitionEnd(Transition transition) {
                        if (!this.mCanceled) {
                            parent.suppressLayout(false);
                        }
                        transition.removeListener(this);
                    }

                    @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                    public void onTransitionPause(Transition transition) {
                        parent.suppressLayout(false);
                    }

                    @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                    public void onTransitionResume(Transition transition) {
                        parent.suppressLayout(true);
                    }
                });
            } else {
                startClip = startClip3;
                endClip = endClip3;
                changeBounds = this;
            }
            if (!changeBounds.mResizeClip) {
                view2.setLeftTopRightBottom(startLeft, startTop2, startRight, startBottom);
                if (numChanges != 2) {
                    if (startLeft != endLeft2) {
                        view = view2;
                    } else if (startTop2 != endTop) {
                        view = view2;
                    } else {
                        return ObjectAnimator.ofObject(view2, BOTTOM_RIGHT_ONLY_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                    }
                    return ObjectAnimator.ofObject(view, TOP_LEFT_ONLY_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startLeft, (float) startTop2, (float) endLeft2, (float) endTop));
                } else if (startWidth == endWidth && startHeight == endHeight) {
                    return ObjectAnimator.ofObject(view2, POSITION_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startLeft, (float) startTop2, (float) endLeft2, (float) endTop));
                } else {
                    final ViewBounds viewBounds = new ViewBounds(view2);
                    ObjectAnimator topLeftAnimator = ObjectAnimator.ofObject(viewBounds, TOP_LEFT_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startLeft, (float) startTop2, (float) endLeft2, (float) endTop));
                    ObjectAnimator bottomRightAnimator = ObjectAnimator.ofObject(viewBounds, BOTTOM_RIGHT_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(topLeftAnimator, bottomRightAnimator);
                    set.addListener(new AnimatorListenerAdapter() {
                        /* class android.transition.ChangeBounds.AnonymousClass8 */
                        private ViewBounds mViewBounds = viewBounds;
                    });
                    return set;
                }
            } else {
                view2.setLeftTopRightBottom(startLeft, startTop2, startLeft + Math.max(startWidth, endWidth), startTop2 + Math.max(startHeight, endHeight));
                if (startLeft == endLeft2 && startTop2 == endTop) {
                    endLeft = endLeft2;
                    startTop = startTop2;
                    positionAnimator = null;
                } else {
                    startTop = startTop2;
                    endLeft = endLeft2;
                    positionAnimator = ObjectAnimator.ofObject(view2, POSITION_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startLeft, (float) startTop2, (float) endLeft2, (float) endTop));
                }
                if (startClip == null) {
                    i = 0;
                    startClip2 = new Rect(0, 0, startWidth, startHeight);
                } else {
                    i = 0;
                    startClip2 = startClip;
                }
                if (endClip == null) {
                    endClip2 = new Rect(i, i, endWidth, endHeight);
                } else {
                    endClip2 = endClip;
                }
                if (!startClip2.equals(endClip2)) {
                    view2.setClipBounds(startClip2);
                    clipAnimator = ObjectAnimator.ofObject(view2, "clipBounds", sRectEvaluator, startClip2, endClip2);
                    positionAnimator2 = positionAnimator;
                    clipAnimator.addListener(new AnimatorListenerAdapter() {
                        /* class android.transition.ChangeBounds.AnonymousClass9 */
                        private boolean mIsCanceled;

                        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                        public void onAnimationCancel(Animator animation) {
                            this.mIsCanceled = true;
                        }

                        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                        public void onAnimationEnd(Animator animation) {
                            if (!this.mIsCanceled) {
                                view2.setClipBounds(endClip);
                                view2.setLeftTopRightBottom(endLeft, endTop, endRight, endBottom);
                            }
                        }
                    });
                } else {
                    positionAnimator2 = positionAnimator;
                    clipAnimator = null;
                }
                return TransitionUtils.mergeAnimators(positionAnimator2, clipAnimator);
            }
        } else {
            sceneRoot.getLocationInWindow(this.tempLocation);
            int startX = ((Integer) startValues.values.get(PROPNAME_WINDOW_X)).intValue() - this.tempLocation[0];
            int startY = ((Integer) startValues.values.get(PROPNAME_WINDOW_Y)).intValue() - this.tempLocation[1];
            int endX = ((Integer) endValues.values.get(PROPNAME_WINDOW_X)).intValue() - this.tempLocation[0];
            int endY = ((Integer) endValues.values.get(PROPNAME_WINDOW_Y)).intValue() - this.tempLocation[1];
            if (startX == endX && startY == endY) {
                return null;
            }
            int width = view2.getWidth();
            int height = view2.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            view2.draw(new Canvas(bitmap));
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(startX, startY, startX + width, startY + height);
            final float transitionAlpha = view2.getTransitionAlpha();
            view2.setTransitionAlpha(0.0f);
            sceneRoot.getOverlay().add(drawable);
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(drawable, PropertyValuesHolder.ofObject(DRAWABLE_ORIGIN_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) startX, (float) startY, (float) endX, (float) endY)));
            anim.addListener(new AnimatorListenerAdapter() {
                /* class android.transition.ChangeBounds.AnonymousClass10 */

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationEnd(Animator animation) {
                    sceneRoot.getOverlay().remove(drawable);
                    view2.setTransitionAlpha(transitionAlpha);
                }
            });
            return anim;
        }
    }

    /* access modifiers changed from: private */
    public static class ViewBounds {
        private int mBottom;
        private int mBottomRightCalls;
        private int mLeft;
        private int mRight;
        private int mTop;
        private int mTopLeftCalls;
        private View mView;

        public ViewBounds(View view) {
            this.mView = view;
        }

        public void setTopLeft(PointF topLeft) {
            this.mLeft = Math.round(topLeft.x);
            this.mTop = Math.round(topLeft.y);
            this.mTopLeftCalls++;
            if (this.mTopLeftCalls == this.mBottomRightCalls) {
                setLeftTopRightBottom();
            }
        }

        public void setBottomRight(PointF bottomRight) {
            this.mRight = Math.round(bottomRight.x);
            this.mBottom = Math.round(bottomRight.y);
            this.mBottomRightCalls++;
            if (this.mTopLeftCalls == this.mBottomRightCalls) {
                setLeftTopRightBottom();
            }
        }

        private void setLeftTopRightBottom() {
            this.mView.setLeftTopRightBottom(this.mLeft, this.mTop, this.mRight, this.mBottom);
            this.mTopLeftCalls = 0;
            this.mBottomRightCalls = 0;
        }
    }
}
