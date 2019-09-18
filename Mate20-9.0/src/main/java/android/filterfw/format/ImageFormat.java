package android.filterfw.format;

import android.filterfw.core.MutableFrameFormat;
import android.graphics.Bitmap;

public class ImageFormat {
    public static final int COLORSPACE_GRAY = 1;
    public static final String COLORSPACE_KEY = "colorspace";
    public static final int COLORSPACE_RGB = 2;
    public static final int COLORSPACE_RGBA = 3;
    public static final int COLORSPACE_YUV = 4;

    public static MutableFrameFormat create(int width, int height, int colorspace, int bytesPerSample, int target) {
        MutableFrameFormat result = new MutableFrameFormat(2, target);
        result.setDimensions(width, height);
        result.setBytesPerSample(bytesPerSample);
        result.setMetaValue(COLORSPACE_KEY, Integer.valueOf(colorspace));
        if (target == 1) {
            result.setObjectClass(Bitmap.class);
        }
        return result;
    }

    public static MutableFrameFormat create(int width, int height, int colorspace, int target) {
        return create(width, height, colorspace, bytesPerSampleForColorspace(colorspace), target);
    }

    public static MutableFrameFormat create(int colorspace, int target) {
        return create(0, 0, colorspace, bytesPerSampleForColorspace(colorspace), target);
    }

    public static MutableFrameFormat create(int colorspace) {
        return create(0, 0, colorspace, bytesPerSampleForColorspace(colorspace), 0);
    }

    public static int bytesPerSampleForColorspace(int colorspace) {
        switch (colorspace) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 3;
            default:
                throw new RuntimeException("Unknown colorspace id " + colorspace + "!");
        }
    }
}
