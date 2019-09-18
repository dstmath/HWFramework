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
        public void set(ImageView object, Matrix value) {
            object.animateTransform(value);
        }

        public Matrix get(ImageView object) {
            return null;
        }
    };
    private static TypeEvaluator<Matrix> NULL_MATRIX_EVALUATOR = new TypeEvaluator<Matrix>() {
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
        Matrix matrix;
        Matrix matrix2;
        TransitionValues transitionValues2 = transitionValues;
        View view = transitionValues2.view;
        if ((view instanceof ImageView) && view.getVisibility() == 0) {
            ImageView imageView = (ImageView) view;
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                Map<String, Object> values = transitionValues2.values;
                Rect bounds = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                values.put(PROPNAME_BOUNDS, bounds);
                if (imageView.getScaleType() == ImageView.ScaleType.FIT_XY) {
                    Matrix matrix3 = imageView.getImageMatrix();
                    if (!matrix3.isIdentity()) {
                        matrix = new Matrix(matrix3);
                    } else {
                        int drawableWidth = drawable.getIntrinsicWidth();
                        int drawableHeight = drawable.getIntrinsicHeight();
                        if (drawableWidth <= 0 || drawableHeight <= 0) {
                            matrix2 = null;
                        } else {
                            float scaleX = ((float) bounds.width()) / ((float) drawableWidth);
                            float scaleY = ((float) bounds.height()) / ((float) drawableHeight);
                            matrix2 = new Matrix();
                            matrix2.setScale(scaleX, scaleY);
                        }
                        matrix = matrix2;
                    }
                } else {
                    matrix = new Matrix(imageView.getImageMatrix());
                }
                values.put(PROPNAME_MATRIX, matrix);
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        ObjectAnimator animator;
        if (startValues == null || endValues == null) {
            return null;
        }
        Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
        if (startBounds == null || endBounds == null) {
            return null;
        }
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        Matrix endMatrix = (Matrix) endValues.values.get(PROPNAME_MATRIX);
        boolean matricesEqual = (startMatrix == null && endMatrix == null) || (startMatrix != null && startMatrix.equals(endMatrix));
        if (startBounds.equals(endBounds) && matricesEqual) {
            return null;
        }
        ImageView imageView = (ImageView) endValues.view;
        Drawable drawable = imageView.getDrawable();
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth == 0 || drawableHeight == 0) {
            animator = createNullAnimator(imageView);
        } else {
            if (startMatrix == null) {
                startMatrix = Matrix.IDENTITY_MATRIX;
            }
            if (endMatrix == null) {
                endMatrix = Matrix.IDENTITY_MATRIX;
            }
            ANIMATED_TRANSFORM_PROPERTY.set(imageView, startMatrix);
            animator = createMatrixAnimator(imageView, startMatrix, endMatrix);
        }
        return animator;
    }

    private ObjectAnimator createNullAnimator(ImageView imageView) {
        return ObjectAnimator.ofObject(imageView, ANIMATED_TRANSFORM_PROPERTY, NULL_MATRIX_EVALUATOR, new Matrix[]{null, null});
    }

    private ObjectAnimator createMatrixAnimator(ImageView imageView, Matrix startMatrix, Matrix endMatrix) {
        return ObjectAnimator.ofObject(imageView, ANIMATED_TRANSFORM_PROPERTY, new TransitionUtils.MatrixEvaluator(), new Matrix[]{startMatrix, endMatrix});
    }
}
