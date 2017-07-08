package android.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class TransitionUtils {
    private static int MAX_IMAGE_SIZE;

    public static class MatrixEvaluator implements TypeEvaluator<Matrix> {
        float[] mTempEndValues;
        Matrix mTempMatrix;
        float[] mTempStartValues;

        public MatrixEvaluator() {
            this.mTempStartValues = new float[9];
            this.mTempEndValues = new float[9];
            this.mTempMatrix = new Matrix();
        }

        public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
            startValue.getValues(this.mTempStartValues);
            endValue.getValues(this.mTempEndValues);
            for (int i = 0; i < 9; i++) {
                this.mTempEndValues[i] = this.mTempStartValues[i] + (fraction * (this.mTempEndValues[i] - this.mTempStartValues[i]));
            }
            this.mTempMatrix.setValues(this.mTempEndValues);
            return this.mTempMatrix;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.TransitionUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.TransitionUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.transition.TransitionUtils.<clinit>():void");
    }

    static Animator mergeAnimators(Animator animator1, Animator animator2) {
        if (animator1 == null) {
            return animator2;
        }
        if (animator2 == null) {
            return animator1;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{animator1, animator2});
        return animatorSet;
    }

    public static Transition mergeTransitions(Transition... transitions) {
        int i;
        int count = 0;
        int nonNullIndex = -1;
        for (i = 0; i < transitions.length; i++) {
            if (transitions[i] != null) {
                count++;
                nonNullIndex = i;
            }
        }
        if (count == 0) {
            return null;
        }
        if (count == 1) {
            return transitions[nonNullIndex];
        }
        TransitionSet transitionSet = new TransitionSet();
        for (i = 0; i < transitions.length; i++) {
            if (transitions[i] != null) {
                transitionSet.addTransition(transitions[i]);
            }
        }
        return transitionSet;
    }

    public static View copyViewImage(ViewGroup sceneRoot, View view, View parent) {
        Matrix matrix = new Matrix();
        matrix.setTranslate((float) (-parent.getScrollX()), (float) (-parent.getScrollY()));
        view.transformMatrixToGlobal(matrix);
        sceneRoot.transformMatrixToLocal(matrix);
        RectF bounds = new RectF(0.0f, 0.0f, (float) view.getWidth(), (float) view.getHeight());
        matrix.mapRect(bounds);
        int left = Math.round(bounds.left);
        int top = Math.round(bounds.top);
        int right = Math.round(bounds.right);
        int bottom = Math.round(bounds.bottom);
        ImageView copy = new ImageView(view.getContext());
        copy.setScaleType(ScaleType.CENTER_CROP);
        Bitmap bitmap = createViewBitmap(view, matrix, bounds);
        if (bitmap != null) {
            copy.setImageBitmap(bitmap);
        }
        copy.measure(MeasureSpec.makeMeasureSpec(right - left, EditorInfo.IME_FLAG_NO_ENTER_ACTION), MeasureSpec.makeMeasureSpec(bottom - top, EditorInfo.IME_FLAG_NO_ENTER_ACTION));
        copy.layout(left, top, right, bottom);
        return copy;
    }

    public static Bitmap createDrawableBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        float scale = Math.min(LayoutParams.BRIGHTNESS_OVERRIDE_FULL, ((float) MAX_IMAGE_SIZE) / ((float) (width * height)));
        if ((drawable instanceof BitmapDrawable) && scale == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int bitmapWidth = (int) (((float) width) * scale);
        int bitmapHeight = (int) (((float) height) * scale);
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect existingBounds = drawable.getBounds();
        int left = existingBounds.left;
        int top = existingBounds.top;
        int right = existingBounds.right;
        int bottom = existingBounds.bottom;
        drawable.setBounds(0, 0, bitmapWidth, bitmapHeight);
        drawable.draw(canvas);
        drawable.setBounds(left, top, right, bottom);
        return bitmap;
    }

    public static Bitmap createViewBitmap(View view, Matrix matrix, RectF bounds) {
        int bitmapWidth = Math.round(bounds.width());
        int bitmapHeight = Math.round(bounds.height());
        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            return null;
        }
        float scale = Math.min(LayoutParams.BRIGHTNESS_OVERRIDE_FULL, ((float) MAX_IMAGE_SIZE) / ((float) (bitmapWidth * bitmapHeight)));
        bitmapWidth = (int) (((float) bitmapWidth) * scale);
        bitmapHeight = (int) (((float) bitmapHeight) * scale);
        matrix.postTranslate(-bounds.left, -bounds.top);
        matrix.postScale(scale, scale);
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.concat(matrix);
        view.draw(canvas);
        return bitmap;
    }
}
