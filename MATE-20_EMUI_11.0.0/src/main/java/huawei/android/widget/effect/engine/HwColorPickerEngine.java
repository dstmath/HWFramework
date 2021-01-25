package huawei.android.widget.effect.engine;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import huawei.android.hwcolorpicker.HwColorPicker;

public class HwColorPickerEngine {
    private static final int DEFAULT_COLOR = 0;
    private static final String TAG = "HwColorPickerEngine";
    private static boolean sIsEnable = true;

    public static int getColor(View view, HwColorPicker.ClientType clientType, HwColorPicker.ResultType resultType) {
        if (view == null || clientType == null || resultType == null) {
            Log.w(TAG, "bitmap and clientType and resultType cannot be null");
            return 0;
        } else if (!isEnable()) {
            return 0;
        } else {
            try {
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                HwColorPicker.PickedColor pickedColor = HwColorPicker.processBitmap(bitmap, clientType);
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (pickedColor == null) {
                    return 0;
                }
                return pickedColor.get(resultType);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "An exception has occurred : " + e.getMessage());
                return 0;
            }
        }
    }

    public static int getColor(Bitmap bitmap, HwColorPicker.ClientType clientType, HwColorPicker.ResultType resultType) {
        HwColorPicker.PickedColor pickedColor;
        if (bitmap == null || clientType == null || resultType == null) {
            Log.w(TAG, "bitmap and clientType and resultType cannot be null");
            return 0;
        } else if (isEnable() && (pickedColor = HwColorPicker.processBitmap(bitmap, clientType)) != null) {
            return pickedColor.get(resultType);
        } else {
            return 0;
        }
    }

    public static boolean isEnable() {
        return HwColorPicker.isEnable() && sIsEnable;
    }

    public static void setEnable(boolean isEnable) {
        sIsEnable = isEnable;
    }
}
