package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.content.Context;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.common.graphics.ImageFactory;
import android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidImage.AutoScale;
import java.util.Map;

public class AndroidImageFactory implements ImageFactory {
    private final Context context;
    private Map<String, Integer> stringIdMap;

    public AndroidImageFactory(Context context) {
        this.context = context;
    }

    public GoogleImage createImage(byte[] imageData, int imageOffset, int imageLength) {
        return new AndroidImage(imageData, imageOffset, imageLength);
    }

    public GoogleImage createUnscaledImage(String name) {
        Map map = this.stringIdMap;
        return new AndroidImage(this.context, this.stringIdMap, name, AutoScale.AUTO_SCALE_DISABLED);
    }

    public GoogleImage createImage(int width, int height) {
        return new AndroidImage(width, height);
    }

    public GoogleImage createImage(int width, int height, boolean processAlpha) {
        return new AndroidImage(width, height, processAlpha);
    }

    public void setStringIdMap(Map<String, Integer> stringIdMap) {
        this.stringIdMap = stringIdMap;
    }
}
