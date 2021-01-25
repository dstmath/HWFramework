package android.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.transition.TransitionUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.Map;

public class ChangeImageTransform extends Transition {
    private static Property<ImageView, Matrix> ANIMATED_TRANSFORM_PROPERTY = new Property<ImageView, Matrix>(Matrix.class, "animatedTransform") {
        /* class android.transition.ChangeImageTransform.AnonymousClass2 */

        public void set(ImageView object, Matrix value) {
            object.animateTransform(value);
        }

        public Matrix get(ImageView object) {
            return null;
        }
    };
    private static TypeEvaluator<Matrix> NULL_MATRIX_EVALUATOR = new TypeEvaluator<Matrix>() {
        /* class android.transition.ChangeImageTransform.AnonymousClass1 */

        public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
            return null;
        }
    };
    private static final String PROPNAME_BOUNDS = "android:changeImageTransform:bounds";
    private static final String PROPNAME_MATRIX = "android:changeImageTransform:matrix";
    private static final String TAG = "ChangeImageTransform";
    private static final String[] sTransitionProperties = {PROPNAME_MATRIX, PROPNAME_BOUNDS};

    public ChangeImageTransform() {
    }

    public ChangeImageTransform(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void captureValues(TransitionValues transitionValues) {
        ImageView imageView;
        Drawable drawable;
        Matrix matrix;
        View view = transitionValues.view;
        if ((view instanceof ImageView) && view.getVisibility() == 0 && (drawable = (imageView = (ImageView) view).getDrawable()) != null) {
            Map<String, Object> values = transitionValues.values;
            Rect bounds = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            values.put(PROPNAME_BOUNDS, bounds);
            ImageView.ScaleType scaleType = imageView.getScaleType();
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            if (scaleType != ImageView.ScaleType.FIT_XY || drawableWidth <= 0 || drawableHeight <= 0) {
                matrix = new Matrix(imageView.getImageMatrix());
            } else {
                float scaleX = ((float) bounds.width()) / ((float) drawableWidth);
                float scaleY = ((float) bounds.height()) / ((float) drawableHeight);
                matrix = new Matrix();
                matrix.setScale(scaleX, scaleY);
            }
            values.put(PROPNAME_MATRIX, matrix);
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

    @Override // android.transition.Transition
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    @Override // android.transition.Transition
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        Matrix endMatrix = (Matrix) endValues.values.get(PROPNAME_MATRIX);
        if (startBounds == null || endBounds == null || startMatrix == null || endMatrix == null) {
            return null;
        }
        if (startBounds.equals(endBounds) && startMatrix.equals(endMatrix)) {
            return null;
        }
        ImageView imageView = (ImageView) endValues.view;
        Drawable drawable = imageView.getDrawable();
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth <= 0 || drawableHeight <= 0) {
            return createNullAnimator(imageView);
        }
        ANIMATED_TRANSFORM_PROPERTY.set(imageView, startMatrix);
        return createMatrixAnimator(imageView, startMatrix, endMatrix);
    }

    private ObjectAnimator createNullAnimator(ImageView imageView) {
        return ObjectAnimator.ofObject(imageView, (Property<ImageView, V>) ANIMATED_TRANSFORM_PROPERTY, (TypeEvaluator) NULL_MATRIX_EVALUATOR, (Object[]) new Matrix[]{Matrix.IDENTITY_MATRIX, Matrix.IDENTITY_MATRIX});
    }

    private ObjectAnimator createMatrixAnimator(ImageView imageView, Matrix startMatrix, Matrix endMatrix) {
        return ObjectAnimator.ofObject(imageView, (Property<ImageView, V>) ANIMATED_TRANSFORM_PROPERTY, (TypeEvaluator) new TransitionUtils.MatrixEvaluator(), (Object[]) new Matrix[]{startMatrix, endMatrix});
    }
}
