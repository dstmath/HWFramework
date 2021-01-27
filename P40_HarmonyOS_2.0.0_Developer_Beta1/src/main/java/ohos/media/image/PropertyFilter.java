package ohos.media.image;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ohos.media.image.common.Filter;
import ohos.media.image.exifadapter.ExifAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class PropertyFilter extends Filter {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(PropertyFilter.class);
    private ExifAdapter mExifAdapter;
    private HashMap<String, String> mProperties = new HashMap<>();

    public PropertyFilter setPropertyInt(String str, int i) {
        if (str == null) {
            LOGGER.error("invalid input key of setPropertyInt.", new Object[0]);
            return this;
        }
        this.mProperties.put(str, String.valueOf(i));
        return this;
    }

    public PropertyFilter setPropertyDouble(String str, double d) {
        if (str == null) {
            LOGGER.error("invalid input key of setPropertyDouble.", new Object[0]);
            return this;
        }
        this.mProperties.put(str, String.valueOf(d));
        return this;
    }

    public PropertyFilter setPropertyString(String str, String str2) {
        if (str == null) {
            LOGGER.error("invalid input key of setPropertyString.", new Object[0]);
            return this;
        }
        this.mProperties.put(str, str2);
        return this;
    }

    public PropertyFilter rollbackProperty(String str) {
        if (str == null || !this.mProperties.keySet().contains(str)) {
            LOGGER.error("invalid input key of rollbackProperty.", new Object[0]);
            return this;
        }
        this.mProperties.remove(str);
        return this;
    }

    @Override // ohos.media.image.common.Filter
    public PropertyFilter restore() {
        this.mProperties.clear();
        return this;
    }

    @Override // ohos.media.image.common.Filter
    public long applyToSource(ImageSource imageSource) throws IOException {
        if (imageSource == null || imageSource.isReleased()) {
            LOGGER.error("invalid input ImageSource status of applyToSource.", new Object[0]);
            return -1;
        }
        this.mExifAdapter = imageSource.getExifAdapterInstance();
        for (Map.Entry<String, String> entry : this.mProperties.entrySet()) {
            this.mExifAdapter.setImageProperty(entry.getKey(), entry.getValue());
        }
        this.mExifAdapter.saveAttributes();
        return imageSource.updateImageSource().getFileSize();
    }
}
