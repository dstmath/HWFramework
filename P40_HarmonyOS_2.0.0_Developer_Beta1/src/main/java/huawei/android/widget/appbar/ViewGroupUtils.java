package huawei.android.widget.appbar;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class ViewGroupUtils {
    private static final float HALF_RATE = 0.5f;
    private static final ThreadLocal<Matrix> SMATRIX = new ThreadLocal<>();
    private static final ThreadLocal<RectF> SRECTF = new ThreadLocal<>();

    private ViewGroupUtils() {
    }

    static void offsetDescendantRect(ViewGroup parent, View descendant, Rect rect) {
        Matrix matrix = SMATRIX.get();
        if (matrix == null) {
            matrix = new Matrix();
            SMATRIX.set(matrix);
        } else {
            matrix.reset();
        }
        offsetDescendantMatrix(parent, descendant, matrix);
        RectF rectF = SRECTF.get();
        if (rectF == null) {
            rectF = new RectF();
            SRECTF.set(rectF);
        }
        rectF.set(rect);
        matrix.mapRect(rectF);
        rect.set((int) (rectF.left + HALF_RATE), (int) (rectF.top + HALF_RATE), (int) (rectF.right + HALF_RATE), (int) (rectF.bottom + HALF_RATE));
    }

    static void getDescendantRect(ViewGroup parent, View descendant, Rect out) {
        out.set(0, 0, descendant.getWidth(), descendant.getHeight());
        offsetDescendantRect(parent, descendant, out);
    }

    private static void offsetDescendantMatrix(ViewParent target, View view, Matrix matrix) {
        ViewParent parent = view.getParent();
        if ((parent instanceof View) && parent != target) {
            View viewParent = (View) parent;
            offsetDescendantMatrix(target, viewParent, matrix);
            matrix.preTranslate((float) (-viewParent.getScrollX()), (float) (-viewParent.getScrollY()));
        }
        matrix.preTranslate((float) view.getLeft(), (float) view.getTop());
        if (!view.getMatrix().isIdentity()) {
            matrix.preConcat(view.getMatrix());
        }
    }
}
