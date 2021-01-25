package ohos.miscservices.inputmethod.adapter;

import android.graphics.Matrix;
import android.view.inputmethod.CursorAnchorInfo;
import java.util.Optional;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class CursorContextInfoAdapter {
    private static final int BOTTOM_INDEX = 3;
    private static final int BOUND_NUM = 4;
    private static final int MATRIX_VALUE_LEN = 9;
    private static final int RIGHT_INDEX = 2;
    private static final int TOP_INDEX = 1;

    private CursorContextInfoAdapter() {
    }

    public static Optional<CursorAnchorInfo> convertToCursorAnchorInfo(float f, float f2, float[] fArr) {
        if (fArr == null || fArr.length != 9) {
            return Optional.empty();
        }
        CursorAnchorInfo.Builder builder = new CursorAnchorInfo.Builder();
        builder.setInsertionMarkerLocation(f, ConstantValue.MIN_ZOOM_VALUE, ConstantValue.MIN_ZOOM_VALUE, f2, 0);
        Matrix matrix = new Matrix();
        matrix.setValues(fArr);
        builder.setMatrix(matrix);
        return Optional.of(builder.build());
    }
}
