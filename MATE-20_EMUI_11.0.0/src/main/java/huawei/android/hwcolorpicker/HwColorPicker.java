package huawei.android.hwcolorpicker;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.util.Log;
import java.lang.ref.WeakReference;

public class HwColorPicker {
    private static final int CLIENT_TYPE_SHIFT_FACTOR = 24;
    private static final float CURVE_LINEAR_COEF = 5644.5f;
    private static final float CURVE_POWER_COEF = -0.54f;
    private static final PickedColor DEFAULT_COLOR = new PickedColor();
    private static final int DEFAULT_H = 25;
    private static final int DEFAULT_W = 25;
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    public static final int MASK_CLIENT_TYPE = -16777216;
    public static final int MASK_RESULT_INDEX = 65535;
    public static final int MASK_RESULT_STATE = 16711680;
    private static final int RESULT_STATE_SHIFT_FACTOR = 16;
    private static final String TAG = "HwColorPicker";
    private static boolean sIsLibColorPickerExist;

    public interface AsyncProcessCallBack {
        void onColorPicked(PickedColor pickedColor);
    }

    private static native int[] processPixels(int[] iArr, int i, int i2);

    static {
        sIsLibColorPickerExist = true;
        try {
            System.loadLibrary("colorpicker");
        } catch (UnsatisfiedLinkError e) {
            sIsLibColorPickerExist = false;
            Log.e(TAG, "libcolorpicker.so couldn't be found.");
        }
    }

    public enum StateType {
        UnDefined(-1),
        OK(0),
        Error(1),
        None(2),
        Common(3),
        Single(4),
        Gray(5);
        
        private int mIndex;

        private StateType(int index) {
            this.mIndex = index;
        }
    }

    public enum ResultType {
        Domain(ClientType.Default, 0),
        DomainDark(ClientType.Default, 1),
        DomainDarkB(ClientType.Default, 2),
        DomainDarkC(ClientType.Default, 3),
        DomainLight(ClientType.Default, 4),
        DomainLightB(ClientType.Default, 5),
        DomainLightC(ClientType.Default, 6),
        Second(ClientType.Default, 7),
        SecondDark(ClientType.Default, 8),
        SecondLight(ClientType.Default, 9),
        Widget(ClientType.Default, 10),
        Shadow(ClientType.Default, 11),
        OriginRgb1(ClientType.Extend, 0),
        OriginRgb2(ClientType.Extend, 1),
        OriginRgb3(ClientType.Extend, 2),
        OriginNum1(ClientType.Extend, 3),
        OriginNum2(ClientType.Extend, 4),
        OriginNum3(ClientType.Extend, 5),
        MergedRgb1(ClientType.Extend, 6),
        MergedRgb2(ClientType.Extend, 7),
        MergedRgb3(ClientType.Extend, 8),
        MergedNum1(ClientType.Extend, 9),
        MergedNum2(ClientType.Extend, 10),
        MergedNum3(ClientType.Extend, 11),
        Music_Domain(ClientType.Music, 0),
        Music_Light(ClientType.Music, 1),
        Music_Widget(ClientType.Music, 2),
        Music_Title(ClientType.Music, 3);
        
        private ClientType mClientType;
        private int mFlag;
        private int mIndex;

        private ResultType(ClientType clientType, int index) {
            this.mClientType = clientType;
            this.mIndex = index & 65535;
            this.mFlag = (65535 & index) | ((clientType.mIndex << 24) & HwColorPicker.MASK_CLIENT_TYPE);
        }

        public static int getRequestedColorNum(int clientType) {
            int num = 0;
            for (ResultType type : values()) {
                if (clientType == type.mClientType.mIndex) {
                    num++;
                }
            }
            return num;
        }

        @Override // java.lang.Enum, java.lang.Object
        public String toString() {
            return name() + "[" + this.mClientType + ", " + this.mIndex + ", " + String.format("0x%08x", Integer.valueOf(this.mFlag)) + "]";
        }
    }

    public enum ClientType {
        Default(0),
        Extend(1),
        Debug(1),
        Music(2);
        
        private int mIndex;

        private ClientType(int index) {
            this.mIndex = index;
        }
    }

    public static boolean isColorPickerEnable() {
        if (!sIsLibColorPickerExist) {
            Log.w(TAG, "lib colorPicker is not exist!");
        }
        return sIsLibColorPickerExist;
    }

    public static boolean isDeviceSupport() {
        return !IS_EMUI_LITE;
    }

    public static boolean isEnable() {
        return isColorPickerEnable();
    }

    public static PickedColor processBitmap(Bitmap bitmap) {
        return processBitmap(bitmap, ClientType.Default);
    }

    public static PickedColor processBitmap(Bitmap bitmap, Rect rect) {
        return processBitmap(bitmap, rect, ClientType.Default);
    }

    public static PickedColor processBitmap(Bitmap bitmap, ClientType clientType) {
        if (!isEnable()) {
            return DEFAULT_COLOR;
        }
        int[] pixels = getPixelsFromBitmap(bitmap);
        if (pixels != null) {
            return processPixels(pixels, clientType);
        }
        Log.e(TAG, "getPixelsFromBitmap(" + bitmap + ", " + clientType + "), return null!");
        return DEFAULT_COLOR;
    }

    public static PickedColor processBitmap(Bitmap bitmap, Rect rect, ClientType clientType) {
        if (!isEnable()) {
            return DEFAULT_COLOR;
        }
        int[] pixels = getPixelsFromBitmap(bitmap, rect);
        if (pixels != null) {
            return processPixels(pixels, clientType);
        }
        Log.e(TAG, "getPixelsFromBitmap(" + bitmap + ", " + rect + ", " + clientType + "), return null!");
        return DEFAULT_COLOR;
    }

    public static void processBitmapAsync(Bitmap bitmap, AsyncProcessCallBack callback) {
        if (isEnable()) {
            processBitmapAsync(bitmap, null, ClientType.Default, callback);
        } else if (callback != null) {
            callback.onColorPicked(DEFAULT_COLOR);
        }
    }

    public static void processBitmapAsync(Bitmap bitmap, Rect rect, AsyncProcessCallBack callback) {
        if (isEnable()) {
            processBitmapAsync(bitmap, rect, ClientType.Default, callback);
        } else if (callback != null) {
            callback.onColorPicked(DEFAULT_COLOR);
        }
    }

    public static void processBitmapAsync(Bitmap bitmap, ClientType clientType, AsyncProcessCallBack callback) {
        if (isEnable()) {
            processBitmapAsync(bitmap, null, clientType, callback);
        } else if (callback != null) {
            callback.onColorPicked(DEFAULT_COLOR);
        }
    }

    public static void processBitmapAsync(Bitmap bitmap, Rect rect, ClientType clientType, AsyncProcessCallBack callback) {
        if (isEnable()) {
            new ColorPickingTask(callback).execute(new WeakReference(new TaskParams(bitmap, rect, clientType)));
        } else if (callback != null) {
            callback.onColorPicked(DEFAULT_COLOR);
        }
    }

    public static class PickedColor {
        private static final int UNDEFINED_CLIENT_TYPE = -1;
        private static final int UNDEFINED_RESULT_STATE = -1;
        private static final int WIDGET_DEFAULT_COLOR = -855638017;
        private int mClientType;
        private int[] mColorPickeds;
        private int mResultState;

        private PickedColor() {
            this.mClientType = -1;
            this.mResultState = -1;
        }

        private PickedColor(int[] colorResults) {
            this.mClientType = -1;
            this.mResultState = -1;
            if (colorResults == null || colorResults.length <= 0) {
                throw new RuntimeException("Illegal result, colorResults is null or Empty!");
            }
            int flag = colorResults[0];
            this.mClientType = (-16777216 & flag) >> 24;
            this.mResultState = (16711680 & flag) >> 16;
            int requestedNum = ResultType.getRequestedColorNum(this.mClientType);
            if (colorResults.length == requestedNum + 1) {
                this.mColorPickeds = new int[requestedNum];
                System.arraycopy(colorResults, 1, this.mColorPickeds, 0, requestedNum);
                return;
            }
            Log.e(HwColorPicker.TAG, "colorResults's length : " + colorResults.length + ", requestedNum : " + requestedNum + ", mClientType : " + this.mClientType + ", mResultState : " + this.mResultState);
            throw new RuntimeException("Illegal result, colorResults's length must be (requestedNum + 1)!");
        }

        public int getState() {
            return this.mResultState;
        }

        @Deprecated
        public int getDomainColor() {
            return get(ResultType.Domain);
        }

        @Deprecated
        public int getWidgetColor() {
            if (!HwColorPicker.isEnable()) {
                return WIDGET_DEFAULT_COLOR;
            }
            return get(ResultType.Widget);
        }

        @Deprecated
        public int getShadowColor() {
            return get(ResultType.Shadow);
        }

        public int get(ResultType resultType) {
            int[] iArr = this.mColorPickeds;
            if (iArr == null || iArr.length <= 0) {
                return 0;
            }
            int index = resultType.mIndex & 65535;
            int[] iArr2 = this.mColorPickeds;
            if (index >= iArr2.length) {
                return 0;
            }
            return iArr2[index];
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("PickedColor{");
            stringBuffer.append(this.mClientType);
            stringBuffer.append(", ");
            stringBuffer.append(this.mResultState);
            stringBuffer.append(", [");
            int[] iArr = this.mColorPickeds;
            if (iArr != null) {
                int length = iArr.length;
                for (int i = 0; i < length; i++) {
                    stringBuffer.append(String.format("0x%08x", Integer.valueOf(this.mColorPickeds[i])));
                    if (i != length - 1) {
                        stringBuffer.append(", ");
                    }
                }
            }
            stringBuffer.append("]}");
            return stringBuffer.toString();
        }
    }

    /* access modifiers changed from: private */
    public static class TaskParams {
        Bitmap mBitmap;
        ClientType mClientType = ClientType.Default;
        Rect mRect;

        TaskParams(Bitmap bitmap, Rect rect, ClientType clientType) {
            this.mBitmap = bitmap;
            this.mRect = rect;
            this.mClientType = clientType;
        }
    }

    /* access modifiers changed from: private */
    public static class ColorPickingTask extends AsyncTask<WeakReference<TaskParams>, Integer, PickedColor> {
        AsyncProcessCallBack mCallBack;

        private ColorPickingTask(AsyncProcessCallBack callBack) {
            this.mCallBack = callBack;
        }

        /* access modifiers changed from: protected */
        @SafeVarargs
        public final PickedColor doInBackground(WeakReference<TaskParams>... weakReferences) {
            TaskParams taskParams = weakReferences[0].get();
            if (taskParams == null) {
                return HwColorPicker.DEFAULT_COLOR;
            }
            if (taskParams.mRect == null) {
                return HwColorPicker.processBitmap(taskParams.mBitmap, taskParams.mClientType);
            }
            return HwColorPicker.processBitmap(taskParams.mBitmap, taskParams.mRect, taskParams.mClientType);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(PickedColor pickedColor) {
            AsyncProcessCallBack asyncProcessCallBack = this.mCallBack;
            if (asyncProcessCallBack != null) {
                asyncProcessCallBack.onColorPicked(pickedColor);
            }
        }
    }

    private static int[] getPixelsFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            return getPixelsFormFixedSizeBitmap(Bitmap.createScaledBitmap(bitmap, 25, 25, false));
        }
        Log.e(TAG, "bitmap is null, can't be processed!");
        return null;
    }

    private static int[] getPixelsFromBitmap(Bitmap bitmap, Rect rect) {
        if (bitmap == null || rect == null) {
            Log.e(TAG, "bitmap is null or rect is null, can't be processed!");
            return null;
        }
        Rect realRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        if (!realRect.intersect(rect)) {
            Log.e(TAG, "rect and bitmap's rect has not intersection, can't be processed!");
            return null;
        }
        int left = realRect.left;
        int top = realRect.top;
        int width = realRect.right - realRect.left;
        int height = realRect.bottom - realRect.top;
        if (isSmallSizeRect(width, height)) {
            return createSubBitmapAndScale(bitmap, left, top, width, height);
        }
        return scaleAndCreateSubBitmap(bitmap, left, top, width, height);
    }

    private static boolean isSmallSizeRect(int width, int height) {
        return Float.compare(((float) (Math.pow((double) width, -0.5400000214576721d) * 5644.5d)) - ((float) height), 0.0f) > 0;
    }

    private static int[] createSubBitmapAndScale(Bitmap bitmap, int firstX, int firstY, int width, int height) {
        return getPixelsFormFixedSizeBitmap(Bitmap.createScaledBitmap(Bitmap.createBitmap(bitmap, firstX, firstY, width, height), 25, 25, false));
    }

    private static int[] scaleAndCreateSubBitmap(Bitmap bitmap, int scaleX, int scaleY, int width, int height) {
        if (width == 0 || height == 0) {
            return new int[0];
        }
        int scaledW = Math.round(((float) (bitmap.getWidth() * 25)) / ((float) width));
        int scaledH = Math.round(((float) (bitmap.getHeight() * 25)) / ((float) height));
        return getPixelsFormFixedSizeBitmap(Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, false), Math.round(((float) (scaleX * 25)) / ((float) width)), Math.round(((float) (scaleY * 25)) / ((float) height)), 25, 25));
    }

    private static int[] getPixelsFormFixedSizeBitmap(Bitmap bitmap) {
        int[] pixels = new int[625];
        bitmap.getPixels(pixels, 0, 25, 0, 0, 25, 25);
        return pixels;
    }

    private static PickedColor processPixels(int[] pixels) {
        if (!isEnable()) {
            return DEFAULT_COLOR;
        }
        return new PickedColor(processPixels(pixels, pixels.length, ClientType.Default.mIndex));
    }

    private static PickedColor processPixels(int[] pixels, ClientType clientType) {
        if (!isEnable()) {
            return DEFAULT_COLOR;
        }
        return new PickedColor(processPixels(pixels, pixels.length, clientType != null ? clientType.mIndex : ClientType.Default.mIndex));
    }
}
