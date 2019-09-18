package android.drm;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class DrmInfo {
    private final HashMap<String, Object> mAttributes = new HashMap<>();
    private byte[] mData;
    private final int mInfoType;
    private final String mMimeType;

    public DrmInfo(int infoType, byte[] data, String mimeType) {
        this.mInfoType = infoType;
        this.mMimeType = mimeType;
        this.mData = data;
        if (!isValid()) {
            throw new IllegalArgumentException("infoType: " + infoType + ",mimeType: " + mimeType + ",data: " + Arrays.toString(data));
        }
    }

    public DrmInfo(int infoType, String path, String mimeType) {
        this.mInfoType = infoType;
        this.mMimeType = mimeType;
        try {
            this.mData = DrmUtils.readBytes(path);
        } catch (IOException e) {
            this.mData = null;
        }
        if (!isValid()) {
            String str = "infoType: " + infoType + ",mimeType: " + mimeType + ",data: " + Arrays.toString(this.mData);
            throw new IllegalArgumentException();
        }
    }

    public void put(String key, Object value) {
        this.mAttributes.put(key, value);
    }

    public Object get(String key) {
        return this.mAttributes.get(key);
    }

    public Iterator<String> keyIterator() {
        return this.mAttributes.keySet().iterator();
    }

    public Iterator<Object> iterator() {
        return this.mAttributes.values().iterator();
    }

    public byte[] getData() {
        return this.mData;
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public int getInfoType() {
        return this.mInfoType;
    }

    /* access modifiers changed from: package-private */
    public boolean isValid() {
        return this.mMimeType != null && !this.mMimeType.equals("") && this.mData != null && this.mData.length > 0 && DrmInfoRequest.isValidType(this.mInfoType);
    }
}
