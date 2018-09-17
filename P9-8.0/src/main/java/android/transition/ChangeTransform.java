package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatArrayEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.GhostView;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

public class ChangeTransform extends Transition {
    private static final Property<PathAnimatorMatrix, float[]> NON_TRANSLATIONS_PROPERTY = new Property<PathAnimatorMatrix, float[]>(float[].class, "nonTranslations") {
        public float[] get(PathAnimatorMatrix object) {
            return null;
        }

        public void set(PathAnimatorMatrix object, float[] value) {
            object.setValues(value);
        }
    };
    private static final String PROPNAME_INTERMEDIATE_MATRIX = "android:changeTransform:intermediateMatrix";
    private static final String PROPNAME_INTERMEDIATE_PARENT_MATRIX = "android:changeTransform:intermediateParentMatrix";
    private static final String PROPNAME_MATRIX = "android:changeTransform:matrix";
    private static final String PROPNAME_PARENT = "android:changeTransform:parent";
    private static final String PROPNAME_PARENT_MATRIX = "android:changeTransform:parentMatrix";
    private static final String PROPNAME_TRANSFORMS = "android:changeTransform:transforms";
    private static final String TAG = "ChangeTransform";
    private static final Property<PathAnimatorMatrix, PointF> TRANSLATIONS_PROPERTY = new Property<PathAnimatorMatrix, PointF>(PointF.class, "translations") {
        public PointF get(PathAnimatorMatrix object) {
            return null;
        }

        public void set(PathAnimatorMatrix object, PointF value) {
            object.setTranslation(value);
        }
    };
    private static final String[] sTransitionProperties = new String[]{PROPNAME_MATRIX, PROPNAME_TRANSFORMS, PROPNAME_PARENT_MATRIX};
    private boolean mReparent = true;
    private Matrix mTempMatrix = new Matrix();
    private boolean mUseOverlay = true;

    private static class GhostListener extends TransitionListenerAdapter {
        private GhostView mGhostView;
        private View mStartView;
        private View mView;

        public GhostListener(View view, View startView, GhostView ghostView) {
            this.mView = view;
            this.mStartView = startView;
            this.mGhostView = ghostView;
        }

        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
            GhostView.removeGhost(this.mView);
            this.mView.setTagInternal(R.id.transitionTransform, null);
            this.mView.setTagInternal(R.id.parentMatrix, null);
            this.mStartView.setTransitionAlpha(1.0f);
        }

        public void onTransitionPause(Transition transition) {
            this.mGhostView.setVisibility(4);
        }

        public void onTransitionResume(Transition transition) {
            this.mGhostView.setVisibility(0);
        }
    }

    private static class PathAnimatorMatrix {
        private final Matrix mMatrix = new Matrix();
        private float mTranslationX;
        private float mTranslationY;
        private final float[] mValues;
        private final View mView;

        public PathAnimatorMatrix(View view, float[] values) {
            this.mView = view;
            this.mValues = (float[]) values.clone();
            this.mTranslationX = this.mValues[2];
            this.mTranslationY = this.mValues[5];
            setAnimationMatrix();
        }

        public void setValues(float[] values) {
            System.arraycopy(values, 0, this.mValues, 0, values.length);
            setAnimationMatrix();
        }

        public void setTranslation(PointF translation) {
            this.mTranslationX = translation.x;
            this.mTranslationY = translation.y;
            setAnimationMatrix();
        }

        private void setAnimationMatrix() {
            this.mValues[2] = this.mTranslationX;
            this.mValues[5] = this.mTranslationY;
            this.mMatrix.setValues(this.mValues);
            this.mView.setAnimationMatrix(this.mMatrix);
        }

        public Matrix getMatrix() {
            return this.mMatrix;
        }
    }

    private static class Transforms {
        public final float rotationX;
        public final float rotationY;
        public final float rotationZ;
        public final float scaleX;
        public final float scaleY;
        public final float translationX;
        public final float translationY;
        public final float translationZ;

        public Transforms(View view) {
            this.translationX = view.getTranslationX();
            this.translationY = view.getTranslationY();
            this.translationZ = view.getTranslationZ();
            this.scaleX = view.getScaleX();
            this.scaleY = view.getScaleY();
            this.rotationX = view.getRotationX();
            this.rotationY = view.getRotationY();
            this.rotationZ = view.getRotation();
        }

        public void restore(View view) {
            ChangeTransform.setTransforms(view, this.translationX, this.translationY, this.translationZ, this.scaleX, this.scaleY, this.rotationX, this.rotationY, this.rotationZ);
        }

        public boolean equals(Object that) {
            boolean z = false;
            if (!(that instanceof Transforms)) {
                return false;
            }
            Transforms thatTransform = (Transforms) that;
            if (thatTransform.translationX == this.translationX && thatTransform.translationY == this.translationY && thatTransform.translationZ == this.translationZ && thatTransform.scaleX == this.scaleX && thatTransform.scaleY == this.scaleY && thatTransform.rotationX == this.rotationX && thatTransform.rotationY == this.rotationY && thatTransform.rotationZ == this.rotationZ) {
                z = true;
            }
            return z;
        }
    }

    public ChangeTransform(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeTransform);
        this.mUseOverlay = a.getBoolean(1, true);
        this.mReparent = a.getBoolean(0, true);
        a.recycle();
    }

    public boolean getReparentWithOverlay() {
        return this.mUseOverlay;
    }

    public void setReparentWithOverlay(boolean reparentWithOverlay) {
        this.mUseOverlay = reparentWithOverlay;
    }

    public boolean getReparent() {
        return this.mReparent;
    }

    public void setReparent(boolean reparent) {
        this.mReparent = reparent;
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        if (view.getVisibility() != 8) {
            Object matrix;
            transitionValues.values.put(PROPNAME_PARENT, view.getParent());
            transitionValues.values.put(PROPNAME_TRANSFORMS, new Transforms(view));
            Matrix matrix2 = view.getMatrix();
            if (matrix2 == null || matrix2.isIdentity()) {
                matrix = null;
            } else {
                matrix2 = new Matrix(matrix2);
            }
            transitionValues.values.put(PROPNAME_MATRIX, matrix);
            if (this.mReparent) {
                Matrix parentMatrix = new Matrix();
                ViewGroup parent = (ViewGroup) view.getParent();
                parent.transformMatrixToGlobal(parentMatrix);
                parentMatrix.preTranslate((float) (-parent.getScrollX()), (float) (-parent.getScrollY()));
                transitionValues.values.put(PROPNAME_PARENT_MATRIX, parentMatrix);
                transitionValues.values.put(PROPNAME_INTERMEDIATE_MATRIX, view.getTag(R.id.transitionTransform));
                transitionValues.values.put(PROPNAME_INTERMEDIATE_PARENT_MATRIX, view.getTag(R.id.parentMatrix));
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null || (startValues.values.containsKey(PROPNAME_PARENT) ^ 1) != 0 || (endValues.values.containsKey(PROPNAME_PARENT) ^ 1) != 0) {
            return null;
        }
        boolean handleParentChange = this.mReparent ? parentsMatch((ViewGroup) startValues.values.get(PROPNAME_PARENT), (ViewGroup) endValues.values.get(PROPNAME_PARENT)) ^ 1 : false;
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_INTERMEDIATE_MATRIX);
        if (startMatrix != null) {
            startValues.values.put(PROPNAME_MATRIX, startMatrix);
        }
        Matrix startParentMatrix = (Matrix) startValues.values.get(PROPNAME_INTERMEDIATE_PARENT_MATRIX);
        if (startParentMatrix != null) {
            startValues.values.put(PROPNAME_PARENT_MATRIX, startParentMatrix);
        }
        if (handleParentChange) {
            setMatricesForParent(startValues, endValues);
        }
        ObjectAnimator transformAnimator = createTransformAnimator(startValues, endValues, handleParentChange);
        if (handleParentChange && transformAnimator != null && this.mUseOverlay) {
            createGhostView(sceneRoot, startValues, endValues);
        }
        return transformAnimator;
    }

    private ObjectAnimator createTransformAnimator(TransitionValues startValues, TransitionValues endValues, boolean handleParentChange) {
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        Matrix endMatrix = (Matrix) endValues.values.get(PROPNAME_MATRIX);
        if (startMatrix == null) {
            startMatrix = Matrix.IDENTITY_MATRIX;
        }
        if (endMatrix == null) {
            endMatrix = Matrix.IDENTITY_MATRIX;
        }
        if (startMatrix.equals(endMatrix)) {
            return null;
        }
        final Transforms transforms = (Transforms) endValues.values.get(PROPNAME_TRANSFORMS);
        final View view = endValues.view;
        setIdentityTransforms(view);
        startMatrixValues = new float[9];
        startMatrix.getValues(startMatrixValues);
        float[] endMatrixValues = new float[9];
        endMatrix.getValues(endMatrixValues);
        final PathAnimatorMatrix pathAnimatorMatrix = new PathAnimatorMatrix(view, startMatrixValues);
        PropertyValuesHolder valuesProperty = PropertyValuesHolder.ofObject(NON_TRANSLATIONS_PROPERTY, new FloatArrayEvaluator(new float[9]), new float[][]{startMatrixValues, endMatrixValues});
        PropertyValuesHolder translationProperty = PropertyValuesHolder.ofObject(TRANSLATIONS_PROPERTY, null, getPathMotion().getPath(startMatrixValues[2], startMatrixValues[5], endMatrixValues[2], endMatrixValues[5]));
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(pathAnimatorMatrix, new PropertyValuesHolder[]{valuesProperty, translationProperty});
        final Matrix finalEndMatrix = endMatrix;
        final boolean z = handleParentChange;
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            private boolean mIsCanceled;
            private Matrix mTempMatrix = new Matrix();

            public void onAnimationCancel(Animator animation) {
                this.mIsCanceled = true;
            }

            public void onAnimationEnd(Animator animation) {
                if (!this.mIsCanceled) {
                    if (z && ChangeTransform.this.mUseOverlay) {
                        setCurrentMatrix(finalEndMatrix);
                    } else {
                        view.setTagInternal(R.id.transitionTransform, null);
                        view.setTagInternal(R.id.parentMatrix, null);
                    }
                }
                view.setAnimationMatrix(null);
                transforms.restore(view);
            }

            public void onAnimationPause(Animator animation) {
                setCurrentMatrix(pathAnimatorMatrix.getMatrix());
            }

            public void onAnimationResume(Animator animation) {
                ChangeTransform.setIdentityTransforms(view);
            }

            private void setCurrentMatrix(Matrix currentMatrix) {
                this.mTempMatrix.set(currentMatrix);
                view.setTagInternal(R.id.transitionTransform, this.mTempMatrix);
                transforms.restore(view);
            }
        };
        animator.addListener(listener);
        animator.addPauseListener(listener);
        return animator;
    }

    private boolean parentsMatch(ViewGroup startParent, ViewGroup endParent) {
        if (!isValidTarget(startParent) || (isValidTarget(endParent) ^ 1) != 0) {
            return startParent == endParent;
        } else {
            TransitionValues endValues = getMatchedTransitionValues(startParent, true);
            if (endValues != null) {
                return endParent == endValues.view;
            } else {
                return false;
            }
        }
    }

    private void createGhostView(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        View view = endValues.view;
        Matrix localEndMatrix = new Matrix((Matrix) endValues.values.get(PROPNAME_PARENT_MATRIX));
        sceneRoot.transformMatrixToLocal(localEndMatrix);
        GhostView ghostView = GhostView.addGhost(view, sceneRoot, localEndMatrix);
        Transition outerTransition = this;
        while (outerTransition.mParent != null) {
            outerTransition = outerTransition.mParent;
        }
        outerTransition.addListener(new GhostListener(view, startValues.view, ghostView));
        if (startValues.view != endValues.view) {
            startValues.view.setTransitionAlpha(0.0f);
        }
        view.setTransitionAlpha(1.0f);
    }

    private void setMatricesForParent(TransitionValues startValues, TransitionValues endValues) {
        Matrix endParentMatrix = (Matrix) endValues.values.get(PROPNAME_PARENT_MATRIX);
        endValues.view.setTagInternal(R.id.parentMatrix, endParentMatrix);
        Matrix toLocal = this.mTempMatrix;
        toLocal.reset();
        endParentMatrix.invert(toLocal);
        Matrix startLocal = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        if (startLocal == null) {
            startLocal = new Matrix();
            startValues.values.put(PROPNAME_MATRIX, startLocal);
        }
        startLocal.postConcat((Matrix) startValues.values.get(PROPNAME_PARENT_MATRIX));
        startLocal.postConcat(toLocal);
    }

    private static void setIdentityTransforms(View view) {
        setTransforms(view, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
    }

    private static void setTransforms(View view, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float rotationX, float rotationY, float rotationZ) {
        view.setTranslationX(translationX);
        view.setTranslationY(translationY);
        view.setTranslationZ(translationZ);
        view.setScaleX(scaleX);
        view.setScaleY(scaleY);
        view.setRotationX(rotationX);
        view.setRotationY(rotationY);
        view.setRotation(rotationZ);
    }
}
