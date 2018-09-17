package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;

public class AndroidAshmemImageFactory extends AndroidImageFactory {

    private static class AndroidAshmemImage extends AndroidImage {
        private static final Options options = new Options();

        static {
            options.inPurgeable = true;
            options.inPreferredConfig = Config.RGB_565;
        }

        public AndroidAshmemImage(byte[] imageData, int imageOffset, int imageLength) {
            super(BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength, options));
        }
    }

    public AndroidAshmemImageFactory(Context context) {
        super(context);
    }

    public GoogleImage createImage(byte[] imageData, int imageOffset, int imageLength) {
        return new AndroidAshmemImage(imageData, imageOffset, imageLength);
    }
}
