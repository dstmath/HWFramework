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
import android.graphics.Canvas;
import android.graphics.Path;
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
    private static final Property<View, PointF> POSITION_PROPERTY = new Property<View, PointF>(PointF.class, BrowserContract.Bookmarks.POSITION) {
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
    private static final String[] sTransitionProperties = {PROPNAME_BOUNDS, PROPNAME_CLIP, PROPNAME_PARENT, PROPNAME_WINDOW_X, PROPNAME_WINDOW_Y};
    boolean mReparent = false;
    boolean mResizeClip = false;
    int[] tempLocation = new int[2];

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
    }

    public ChangeBounds(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        int endBottom;
        Rect endClip;
        ChangeBounds changeBounds;
        Animator anim;
        int startLeft;
        int startTop;
        int endLeft;
        Rect startClip;
        int i;
        Rect endClip2;
        ObjectAnimator positionAnimator;
        ObjectAnimator clipAnimator;
        View view;
        TransitionValues transitionValues = startValues;
        TransitionValues transitionValues2 = endValues;
        if (transitionValues == null || transitionValues2 == null) {
            TransitionValues transitionValues3 = transitionValues2;
            return null;
        }
        Map<String, Object> startParentVals = transitionValues.values;
        Map<String, Object> endParentVals = transitionValues2.values;
        ViewGroup startParent = (ViewGroup) startParentVals.get(PROPNAME_PARENT);
        ViewGroup endParent = (ViewGroup) endParentVals.get(PROPNAME_PARENT);
        if (startParent == null) {
            Map<String, Object> map = endParentVals;
            ViewGroup viewGroup = startParent;
            ViewGroup viewGroup2 = endParent;
            TransitionValues transitionValues4 = transitionValues2;
        } else if (endParent == null) {
            Map<String, Object> map2 = startParentVals;
            Map<String, Object> map3 = endParentVals;
            ViewGroup viewGroup3 = startParent;
            ViewGroup viewGroup4 = endParent;
            TransitionValues transitionValues5 = transitionValues2;
        } else {
            View view2 = transitionValues2.view;
            if (parentMatches(startParent, endParent)) {
                Rect startBounds = (Rect) transitionValues.values.get(PROPNAME_BOUNDS);
                Rect endBounds = (Rect) transitionValues2.values.get(PROPNAME_BOUNDS);
                int startLeft2 = startBounds.left;
                int endLeft2 = endBounds.left;
                int startTop2 = startBounds.top;
                int endTop = endBounds.top;
                int startRight = startBounds.right;
                int endRight = endBounds.right;
                Map<String, Object> map4 = startParentVals;
                int startBottom = startBounds.bottom;
                Map<String, Object> map5 = endParentVals;
                int endBottom2 = endBounds.bottom;
                ViewGroup viewGroup5 = startParent;
                int startWidth = startRight - startLeft2;
                ViewGroup viewGroup6 = endParent;
                int startHeight = startBottom - startTop2;
                Rect rect = startBounds;
                int endWidth = endRight - endLeft2;
                Rect rect2 = endBounds;
                int endHeight = endBottom2 - endTop;
                Rect startClip2 = (Rect) transitionValues.values.get(PROPNAME_CLIP);
                Rect endClip3 = (Rect) transitionValues2.values.get(PROPNAME_CLIP);
                int numChanges = 0;
                if (!((startWidth == 0 || startHeight == 0) && (endWidth == 0 || endHeight == 0))) {
                    if (!(startLeft2 == endLeft2 && startTop2 == endTop)) {
                        numChanges = 0 + 1;
                    }
                    if (!(startRight == endRight && startBottom == endBottom2)) {
                        numChanges++;
                    }
                }
                if ((startClip2 != null && !startClip2.equals(endClip3)) || (startClip2 == null && endClip3 != null)) {
                    numChanges++;
                }
                if (numChanges > 0) {
                    Rect startClip3 = startClip2;
                    if (view2.getParent() instanceof ViewGroup) {
                        final ViewGroup parent = (ViewGroup) view2.getParent();
                        endClip = endClip3;
                        parent.suppressLayout(true);
                        endBottom = endBottom2;
                        changeBounds = this;
                        changeBounds.addListener(new TransitionListenerAdapter() {
                            boolean mCanceled = false;

                            public void onTransitionCancel(Transition transition) {
                                parent.suppressLayout(false);
                                this.mCanceled = true;
                            }

                            public void onTransitionEnd(Transition transition) {
                                if (!this.mCanceled) {
                                    parent.suppressLayout(false);
                                }
                                transition.removeListener(this);
                            }

                            public void onTransitionPause(Transition transition) {
                                parent.suppressLayout(false);
                            }

                            public void onTransitionResume(Transition transition) {
                                parent.suppressLayout(true);
                            }
                        });
                    } else {
                        endClip = endClip3;
                        endBottom = endBottom2;
                        changeBounds = this;
                    }
                    if (!changeBounds.mResizeClip) {
                        view2.setLeftTopRightBottom(startLeft2, startTop2, startRight, startBottom);
                        if (numChanges != 2) {
                            int i2 = endWidth;
                            int i3 = numChanges;
                            int i4 = startWidth;
                            int endWidth2 = startHeight;
                            View view3 = view2;
                            int endBottom3 = endBottom;
                            if (startLeft2 != endLeft2) {
                                view = view3;
                            } else if (startTop2 != endTop) {
                                view = view3;
                            } else {
                                anim = ObjectAnimator.ofObject(view3, BOTTOM_RIGHT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom3));
                                int startHeight2 = endWidth2;
                                int endWidth3 = i2;
                                int startWidth2 = i4;
                            }
                            anim = ObjectAnimator.ofObject(view, TOP_LEFT_ONLY_PROPERTY, null, getPathMotion().getPath((float) startLeft2, (float) startTop2, (float) endLeft2, (float) endTop));
                            int startHeight22 = endWidth2;
                            int endWidth32 = i2;
                            int startWidth22 = i4;
                        } else if (startWidth == endWidth && startHeight == endHeight) {
                            int i5 = numChanges;
                            int i6 = endHeight;
                            anim = ObjectAnimator.ofObject(view2, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft2, (float) startTop2, (float) endLeft2, (float) endTop));
                            int i7 = startRight;
                            int i8 = endLeft2;
                            int i9 = endTop;
                            int i10 = startTop2;
                            int i11 = endRight;
                            int i12 = startBottom;
                            int i13 = startWidth;
                            View view4 = view2;
                            int startWidth3 = endBottom;
                            int i14 = startHeight;
                            int endBottom4 = startLeft2;
                            int startHeight3 = endWidth;
                        } else {
                            int i15 = numChanges;
                            final ViewBounds viewBounds = new ViewBounds(view2);
                            int endWidth4 = endWidth;
                            Path topLeftPath = getPathMotion().getPath((float) startLeft2, (float) startTop2, (float) endLeft2, (float) endTop);
                            ObjectAnimator topLeftAnimator = ObjectAnimator.ofObject(viewBounds, TOP_LEFT_PROPERTY, null, topLeftPath);
                            Path path = topLeftPath;
                            ObjectAnimator bottomRightAnimator = ObjectAnimator.ofObject(viewBounds, BOTTOM_RIGHT_PROPERTY, null, getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                            AnimatorSet set = new AnimatorSet();
                            set.playTogether(new Animator[]{topLeftAnimator, bottomRightAnimator});
                            set.addListener(new AnimatorListenerAdapter() {
                                private ViewBounds mViewBounds = viewBounds;
                            });
                            int i16 = startRight;
                            int i17 = endLeft2;
                            int i18 = endTop;
                            int i19 = startTop2;
                            int i20 = endRight;
                            int i21 = startLeft2;
                            int i22 = startBottom;
                            anim = set;
                            int i23 = startHeight;
                            int startHeight4 = endWidth4;
                            int i24 = startWidth;
                            View view5 = view2;
                        }
                    } else {
                        int i25 = endWidth;
                        int i26 = numChanges;
                        int startWidth4 = startWidth;
                        int i27 = startHeight;
                        View view6 = view2;
                        int endBottom5 = endBottom;
                        int startWidth5 = startWidth4;
                        int maxWidth = Math.max(startWidth5, endWidth);
                        int startRight2 = startRight;
                        int endRight2 = endRight;
                        view6.setLeftTopRightBottom(startLeft2, startTop2, startLeft2 + maxWidth, startTop2 + Math.max(startHeight, endHeight));
                        ObjectAnimator positionAnimator2 = null;
                        if (startLeft2 == endLeft2 && startTop2 == endTop) {
                            endLeft = endLeft2;
                            startTop = startTop2;
                            startLeft = startLeft2;
                        } else {
                            startLeft = startLeft2;
                            startTop = startTop2;
                            endLeft = endLeft2;
                            positionAnimator2 = ObjectAnimator.ofObject(view6, POSITION_PROPERTY, null, getPathMotion().getPath((float) startLeft2, (float) startTop2, (float) endLeft2, (float) endTop));
                        }
                        ObjectAnimator positionAnimator3 = positionAnimator2;
                        int i28 = startTop;
                        final Rect finalClip = endClip;
                        if (startClip3 == null) {
                            i = 0;
                            startClip = new Rect(0, 0, startWidth5, startHeight);
                        } else {
                            i = 0;
                            startClip = startClip3;
                        }
                        if (endClip == null) {
                            endClip2 = new Rect(i, i, endWidth, endHeight);
                        } else {
                            endClip2 = endClip;
                        }
                        if (!startClip.equals(endClip2)) {
                            view6.setClipBounds(startClip);
                            int endTop2 = endTop;
                            ObjectAnimator positionAnimator4 = positionAnimator3;
                            ObjectAnimator clipAnimator2 = ObjectAnimator.ofObject(view6, "clipBounds", sRectEvaluator, new Object[]{startClip, endClip2});
                            int i29 = startRight2;
                            Rect rect3 = endClip2;
                            int i30 = startWidth5;
                            AnonymousClass9 r9 = r0;
                            final View view7 = view6;
                            Rect rect4 = startClip;
                            final int i31 = endLeft;
                            int i32 = maxWidth;
                            int i33 = startBottom;
                            int i34 = startLeft;
                            positionAnimator = positionAnimator4;
                            clipAnimator = clipAnimator2;
                            final int i35 = endTop2;
                            int i36 = endHeight;
                            final int endHeight2 = endRight2;
                            int i37 = endWidth;
                            final int endWidth5 = endBottom5;
                            AnonymousClass9 r0 = new AnimatorListenerAdapter() {
                                private boolean mIsCanceled;

                                public void onAnimationCancel(Animator animation) {
                                    this.mIsCanceled = true;
                                }

                                public void onAnimationEnd(Animator animation) {
                                    if (!this.mIsCanceled) {
                                        view7.setClipBounds(finalClip);
                                        view7.setLeftTopRightBottom(i31, i35, endHeight2, endWidth5);
                                    }
                                }
                            };
                            clipAnimator.addListener(r9);
                        } else {
                            Rect rect5 = endClip2;
                            int i38 = endTop;
                            Rect rect6 = startClip;
                            int i39 = endHeight;
                            int i40 = endWidth;
                            int i41 = startWidth5;
                            int i42 = maxWidth;
                            int i43 = startBottom;
                            int i44 = startRight2;
                            int i45 = endRight2;
                            int i46 = startLeft;
                            int i47 = endLeft;
                            positionAnimator = positionAnimator3;
                            clipAnimator = null;
                        }
                        anim = TransitionUtils.mergeAnimators(positionAnimator, clipAnimator);
                    }
                    return anim;
                }
                ViewGroup viewGroup7 = sceneRoot;
                TransitionValues transitionValues6 = startValues;
                TransitionValues transitionValues7 = endValues;
            } else {
                Map<String, Object> map6 = endParentVals;
                ViewGroup viewGroup8 = startParent;
                ViewGroup viewGroup9 = endParent;
                View view8 = view2;
                ViewGroup viewGroup10 = sceneRoot;
                viewGroup10.getLocationInWindow(this.tempLocation);
                TransitionValues transitionValues8 = startValues;
                int startX = ((Integer) transitionValues8.values.get(PROPNAME_WINDOW_X)).intValue() - this.tempLocation[0];
                int startY = ((Integer) transitionValues8.values.get(PROPNAME_WINDOW_Y)).intValue() - this.tempLocation[1];
                TransitionValues transitionValues9 = endValues;
                int endX = ((Integer) transitionValues9.values.get(PROPNAME_WINDOW_X)).intValue() - this.tempLocation[0];
                int endY = ((Integer) transitionValues9.values.get(PROPNAME_WINDOW_Y)).intValue() - this.tempLocation[1];
                if (!(startX == endX && startY == endY)) {
                    int width = view8.getWidth();
                    int height = view8.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    view8.draw(canvas);
                    BitmapDrawable drawable = new BitmapDrawable(bitmap);
                    drawable.setBounds(startX, startY, startX + width, startY + height);
                    float transitionAlpha = view8.getTransitionAlpha();
                    view8.setTransitionAlpha(0.0f);
                    sceneRoot.getOverlay().add(drawable);
                    Bitmap bitmap2 = bitmap;
                    int height2 = height;
                    Path topLeftPath2 = getPathMotion().getPath((float) startX, (float) startY, (float) endX, (float) endY);
                    ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(drawable, new PropertyValuesHolder[]{PropertyValuesHolder.ofObject(DRAWABLE_ORIGIN_PROPERTY, null, topLeftPath2)});
                    BitmapDrawable drawable2 = drawable;
                    final ViewGroup viewGroup11 = viewGroup10;
                    AnonymousClass10 r6 = r0;
                    Canvas canvas2 = canvas;
                    final BitmapDrawable bitmapDrawable = drawable2;
                    ObjectAnimator anim3 = anim2;
                    Bitmap bitmap3 = bitmap2;
                    final View view9 = view8;
                    Path path2 = topLeftPath2;
                    int i48 = height2;
                    final float f = transitionAlpha;
                    AnonymousClass10 r02 = new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            viewGroup11.getOverlay().remove(bitmapDrawable);
                            view9.setTransitionAlpha(f);
                        }
                    };
                    anim3.addListener(r6);
                    return anim3;
                }
            }
            return null;
        }
        return null;
    }
}
