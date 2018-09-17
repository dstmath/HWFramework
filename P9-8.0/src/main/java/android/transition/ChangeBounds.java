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
import android.provider.BrowserContract.Bookmarks;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;
import java.util.Map;

public class ChangeBounds extends Transition {
    private static final Property<View, PointF> BOTTOM_RIGHT_ONLY_PROPERTY = new Property<View, PointF>(PointF.class, "bottomRight") {
        public void set(View view, PointF bottomRight) {
            view.setLeftTopRightBottom(view.getLeft(), view.getTop(), Math.round(bottomRight.x), Math.round(bottomRight.y));
        }

        public PointF get(View view) {
            return null;
        }
    };
    private static final Property<ViewBounds, PointF> BOTTOM_RIGHT_PROPERTY = new Property<ViewBounds, PointF>(PointF.class, "bottomRight") {
        public void set(ViewBounds viewBounds, PointF bottomRight) {
            viewBounds.setBottomRight(bottomRight);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    };
    private static final Property<Drawable, PointF> DRAWABLE_ORIGIN_PROPERTY = new Property<Drawable, PointF>(PointF.class, "boundsOrigin") {
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
    private static final Property<View, PointF> POSITION_PROPERTY = new Property<View, PointF>(PointF.class, Bookmarks.POSITION) {
        public void set(View view, PointF topLeft) {
            int left = Math.round(topLeft.x);
            int top = Math.round(topLeft.y);
            view.setLeftTopRightBottom(left, top, left + view.getWidth(), top + view.getHeight());
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
        public void set(View view, PointF topLeft) {
            view.setLeftTopRightBottom(Math.round(topLeft.x), Math.round(topLeft.y), view.getRight(), view.getBottom());
        }

        public PointF get(View view) {
            return null;
        }
    };
    private static final Property<ViewBounds, PointF> TOP_LEFT_PROPERTY = new Property<ViewBounds, PointF>(PointF.class, "topLeft") {
        public void set(ViewBounds viewBounds, PointF topLeft) {
            viewBounds.setTopLeft(topLeft);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    };
    private static RectEvaluator sRectEvaluator = new RectEvaluator();
    private static final String[] sTransitionProperties = new String[]{PROPNAME_BOUNDS, PROPNAME_CLIP, PROPNAME_PARENT, PROPNAME_WINDOW_X, PROPNAME_WINDOW_Y};
    boolean mReparent;
    boolean mResizeClip;
    int[] tempLocation;

    private static class ViewBounds {
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
        View endParent = (ViewGroup) endValues.values.get(PROPNAME_PARENT);
        if (startParent == null || endParent == null) {
            return null;
        }
        final View view = endValues.view;
        if (parentMatches(startParent, endParent)) {
            Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
            Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
            int startLeft = startBounds.left;
            final int endLeft = endBounds.left;
            int startTop = startBounds.top;
            final int endTop = endBounds.top;
            int startRight = startBounds.right;
            final int endRight = endBounds.right;
            int startBottom = startBounds.bottom;
            final int endBottom = endBounds.bottom;
            int startWidth = startRight - startLeft;
            int startHeight = startBottom - startTop;
            int endWidth = endRight - endLeft;
            int endHeight = endBottom - endTop;
            Rect startClip = (Rect) startValues.values.get(PROPNAME_CLIP);
            final Rect endClip = (Rect) endValues.values.get(PROPNAME_CLIP);
            int numChanges = 0;
            if (!((startWidth == 0 || startHeight == 0) && (endWidth == 0 || endHeight == 0))) {
                if (!(startLeft == endLeft && startTop == endTop)) {
                    numChanges = 1;
                }
                if (!(startRight == endRight && startBottom == endBottom)) {
                    numChanges++;
                }
            }
            if (!(startClip == null || (startClip.equals(endClip) ^ 1) == 0) || (startClip == null && endClip != null)) {
                numChanges++;
            }
            if (numChanges > 0) {
                Animator anim;
                if (this.mResizeClip) {
                    Rect rect;
                    Rect endClip2;
                    int i = startLeft;
                    int i2 = startTop;
                    view.setLeftTopRightBottom(i, i2, startLeft + Math.max(startWidth, endWidth), startTop + Math.max(startHeight, endHeight));
                    Animator positionAnimator = null;
                    if (!(startLeft == endLeft && startTop == endTop)) {
                        positionAnimator = ObjectAnimator.ofObject(view, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                    }
                    Rect finalClip = endClip;
                    if (startClip == null) {
                        rect = new Rect(0, 0, startWidth, startHeight);
                    }
                    if (endClip == null) {
                        rect = new Rect(0, 0, endWidth, endHeight);
                    } else {
                        endClip2 = endClip;
                    }
                    Animator clipAnimator = null;
                    if (!startClip.equals(endClip2)) {
                        view.setClipBounds(startClip);
                        clipAnimator = ObjectAnimator.ofObject(view, "clipBounds", sRectEvaluator, new Object[]{startClip, endClip2});
                        clipAnimator.addListener(new AnimatorListenerAdapter() {
                            private boolean mIsCanceled;

                            public void onAnimationCancel(Animator animation) {
                                this.mIsCanceled = true;
                            }

                            public void onAnimationEnd(Animator animation) {
                                if (!this.mIsCanceled) {
                                    view.setClipBounds(endClip);
                                    view.setLeftTopRightBottom(endLeft, endTop, endRight, endBottom);
                                }
                            }
                        });
                    }
                    anim = TransitionUtils.mergeAnimators(positionAnimator, clipAnimator);
                    endClip = endClip2;
                } else {
                    view.setLeftTopRightBottom(startLeft, startTop, startRight, startBottom);
                    if (numChanges == 2) {
                        if (startWidth == endWidth && startHeight == endHeight) {
                            anim = ObjectAnimator.ofObject(view, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                        } else {
                            ViewBounds viewBounds = new ViewBounds(view);
                            viewBounds = viewBounds;
                            ObjectAnimator topLeftAnimator = ObjectAnimator.ofObject(viewBounds, TOP_LEFT_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                            viewBounds = viewBounds;
                            ObjectAnimator bottomRightAnimator = ObjectAnimator.ofObject(viewBounds, BOTTOM_RIGHT_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                            Animator set = new AnimatorSet();
                            set.playTogether(new Animator[]{topLeftAnimator, bottomRightAnimator});
                            anim = set;
                            final ViewBounds viewBounds2 = viewBounds;
                            set.addListener(new AnimatorListenerAdapter() {
                                private ViewBounds mViewBounds = viewBounds2;
                            });
                        }
                    } else if (startLeft == endLeft && startTop == endTop) {
                        anim = ObjectAnimator.ofObject(view, BOTTOM_RIGHT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                    } else {
                        anim = ObjectAnimator.ofObject(view, TOP_LEFT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                    }
                }
                if (view.getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) view.getParent();
                    parent.suppressLayout(true);
                    final ViewGroup viewGroup = parent;
                    addListener(new TransitionListenerAdapter() {
                        boolean mCanceled = false;

                        public void onTransitionCancel(Transition transition) {
                            viewGroup.suppressLayout(false);
                            this.mCanceled = true;
                        }

                        public void onTransitionEnd(Transition transition) {
                            if (!this.mCanceled) {
                                viewGroup.suppressLayout(false);
                            }
                            transition.removeListener(this);
                        }

                        public void onTransitionPause(Transition transition) {
                            viewGroup.suppressLayout(false);
                        }

                        public void onTransitionResume(Transition transition) {
                            viewGroup.suppressLayout(true);
                        }
                    });
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
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(startX, startY, startX + width, startY + height);
            final float transitionAlpha = view.getTransitionAlpha();
            view.setTransitionAlpha(0.0f);
            sceneRoot.getOverlay().add(drawable);
            PropertyValuesHolder origin = PropertyValuesHolder.ofObject(DRAWABLE_ORIGIN_PROPERTY, null, getPathMotion().getPath((float) startX, (float) startY, (float) endX, (float) endY));
            ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(drawable, new PropertyValuesHolder[]{origin});
            final ViewGroup viewGroup2 = sceneRoot;
            final View view2 = view;
            anim2.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    viewGroup2.getOverlay().remove(drawable);
                    view2.setTransitionAlpha(transitionAlpha);
                }
            });
            return anim2;
        }
        return null;
    }
}
