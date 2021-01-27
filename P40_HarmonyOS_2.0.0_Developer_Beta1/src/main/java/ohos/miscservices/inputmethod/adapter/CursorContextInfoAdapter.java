package ohos.miscservices.inputmethod.adapter;

import android.graphics.Matrix;
import android.view.inputmethod.CursorAnchorInfo;
import java.util.Optional;

public class CursorContextInfoAdapter {
    private static final int MATRIX_VALUE_LEN = 9;

    private CursorContextInfoAdapter() {
    }

    public static Optional<CursorAnchorInfo> convertToCursorAnchorInfo(float f, float f2, float f3, float[] fArr) {
        if (fArr == null || fArr.length != 9) {
            return Optional.empty();
        }
        CursorAnchorInfo.Builder builder = new CursorAnchorInfo.Builder();
        builder.setInsertionMarkerLocation(f, f3, 0.0f, f2, 0);
        Matrix matrix = new Matrix();
        matrix.setValues(fArr);
        builder.setMatrix(matrix);
        return Optional.of(builder.build());
    }
}
