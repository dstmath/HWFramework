package android.media;

import android.graphics.Bitmap;
import android.media.MediaMetadata.Builder;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseIntArray;

@Deprecated
public abstract class MediaMetadataEditor {
    public static final int BITMAP_KEY_ARTWORK = 100;
    public static final int KEY_EDITABLE_MASK = 536870911;
    protected static final SparseIntArray METADATA_KEYS_TYPE = new SparseIntArray(18);
    protected static final int METADATA_TYPE_BITMAP = 2;
    protected static final int METADATA_TYPE_INVALID = -1;
    protected static final int METADATA_TYPE_LONG = 0;
    protected static final int METADATA_TYPE_RATING = 3;
    protected static final int METADATA_TYPE_STRING = 1;
    public static final int RATING_KEY_BY_OTHERS = 101;
    public static final int RATING_KEY_BY_USER = 268435457;
    private static final String TAG = "MediaMetadataEditor";
    protected boolean mApplied = false;
    protected boolean mArtworkChanged = false;
    protected long mEditableKeys;
    protected Bitmap mEditorArtwork;
    protected Bundle mEditorMetadata;
    protected Builder mMetadataBuilder;
    protected boolean mMetadataChanged = false;

    public abstract void apply();

    protected MediaMetadataEditor() {
    }

    public synchronized void clear() {
        if (this.mApplied) {
            Log.e(TAG, "Can't clear a previously applied MediaMetadataEditor");
            return;
        }
        this.mEditorMetadata.clear();
        this.mEditorArtwork = null;
        this.mMetadataBuilder = new Builder();
    }

    /* JADX WARNING: Missing block: B:12:0x0023, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void addEditableKey(int key) {
        if (this.mApplied) {
            Log.e(TAG, "Can't change editable keys of a previously applied MetadataEditor");
        } else if (key == RATING_KEY_BY_USER) {
            this.mEditableKeys |= (long) (KEY_EDITABLE_MASK & key);
            this.mMetadataChanged = true;
        } else {
            Log.e(TAG, "Metadata key " + key + " cannot be edited");
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void removeEditableKeys() {
        if (this.mApplied) {
            Log.e(TAG, "Can't remove all editable keys of a previously applied MetadataEditor");
        } else if (this.mEditableKeys != 0) {
            this.mEditableKeys = 0;
            this.mMetadataChanged = true;
        }
    }

    public synchronized int[] getEditableKeys() {
        if (this.mEditableKeys != 268435457) {
            return null;
        }
        return new int[]{RATING_KEY_BY_USER};
    }

    public synchronized MediaMetadataEditor putString(int key, String value) throws IllegalArgumentException {
        if (this.mApplied) {
            Log.e(TAG, "Can't edit a previously applied MediaMetadataEditor");
            return this;
        } else if (METADATA_KEYS_TYPE.get(key, -1) != 1) {
            throw new IllegalArgumentException("Invalid type 'String' for key " + key);
        } else {
            this.mEditorMetadata.putString(String.valueOf(key), value);
            this.mMetadataChanged = true;
            return this;
        }
    }

    public synchronized MediaMetadataEditor putLong(int key, long value) throws IllegalArgumentException {
        if (this.mApplied) {
            Log.e(TAG, "Can't edit a previously applied MediaMetadataEditor");
            return this;
        } else if (METADATA_KEYS_TYPE.get(key, -1) != 0) {
            throw new IllegalArgumentException("Invalid type 'long' for key " + key);
        } else {
            this.mEditorMetadata.putLong(String.valueOf(key), value);
            this.mMetadataChanged = true;
            return this;
        }
    }

    public synchronized MediaMetadataEditor putBitmap(int key, Bitmap bitmap) throws IllegalArgumentException {
        if (this.mApplied) {
            Log.e(TAG, "Can't edit a previously applied MediaMetadataEditor");
            return this;
        } else if (key != 100) {
            throw new IllegalArgumentException("Invalid type 'Bitmap' for key " + key);
        } else {
            this.mEditorArtwork = bitmap;
            this.mArtworkChanged = true;
            return this;
        }
    }

    public synchronized MediaMetadataEditor putObject(int key, Object value) throws IllegalArgumentException {
        if (this.mApplied) {
            Log.e(TAG, "Can't edit a previously applied MediaMetadataEditor");
            return this;
        }
        switch (METADATA_KEYS_TYPE.get(key, -1)) {
            case 0:
                if (value instanceof Long) {
                    return putLong(key, ((Long) value).longValue());
                }
                throw new IllegalArgumentException("Not a non-null Long for key " + key);
            case 1:
                if (value == null || (value instanceof String)) {
                    return putString(key, (String) value);
                }
                throw new IllegalArgumentException("Not a String for key " + key);
            case 2:
                if (value != null) {
                    if (!(value instanceof Bitmap)) {
                        throw new IllegalArgumentException("Not a Bitmap for key " + key);
                    }
                }
                return putBitmap(key, (Bitmap) value);
            case 3:
                this.mEditorMetadata.putParcelable(String.valueOf(key), (Parcelable) value);
                this.mMetadataChanged = true;
                return this;
            default:
                throw new IllegalArgumentException("Invalid key " + key);
        }
    }

    public synchronized long getLong(int key, long defaultValue) throws IllegalArgumentException {
        if (METADATA_KEYS_TYPE.get(key, -1) != 0) {
            throw new IllegalArgumentException("Invalid type 'long' for key " + key);
        }
        return this.mEditorMetadata.getLong(String.valueOf(key), defaultValue);
    }

    public synchronized String getString(int key, String defaultValue) throws IllegalArgumentException {
        if (METADATA_KEYS_TYPE.get(key, -1) != 1) {
            throw new IllegalArgumentException("Invalid type 'String' for key " + key);
        }
        return this.mEditorMetadata.getString(String.valueOf(key), defaultValue);
    }

    public synchronized Bitmap getBitmap(int key, Bitmap defaultValue) throws IllegalArgumentException {
        if (key != 100) {
            throw new IllegalArgumentException("Invalid type 'Bitmap' for key " + key);
        } else if (this.mEditorArtwork != null) {
            defaultValue = this.mEditorArtwork;
        }
        return defaultValue;
    }

    /* JADX WARNING: Missing block: B:40:0x0085, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Object getObject(int key, Object defaultValue) throws IllegalArgumentException {
        switch (METADATA_KEYS_TYPE.get(key, -1)) {
            case 0:
                if (!this.mEditorMetadata.containsKey(String.valueOf(key))) {
                    return defaultValue;
                }
                return Long.valueOf(this.mEditorMetadata.getLong(String.valueOf(key)));
            case 1:
                if (!this.mEditorMetadata.containsKey(String.valueOf(key))) {
                    return defaultValue;
                }
                return this.mEditorMetadata.getString(String.valueOf(key));
            case 2:
                if (key == 100) {
                    if (this.mEditorArtwork != null) {
                        defaultValue = this.mEditorArtwork;
                        break;
                    }
                }
                break;
            case 3:
                if (!this.mEditorMetadata.containsKey(String.valueOf(key))) {
                    return defaultValue;
                }
                return this.mEditorMetadata.getParcelable(String.valueOf(key));
        }
        throw new IllegalArgumentException("Invalid key " + key);
    }

    static {
        METADATA_KEYS_TYPE.put(1000, 1);
        METADATA_KEYS_TYPE.put(0, 0);
        METADATA_KEYS_TYPE.put(14, 0);
        METADATA_KEYS_TYPE.put(9, 0);
        METADATA_KEYS_TYPE.put(8, 0);
        METADATA_KEYS_TYPE.put(1, 1);
        METADATA_KEYS_TYPE.put(13, 1);
        METADATA_KEYS_TYPE.put(7, 1);
        METADATA_KEYS_TYPE.put(2, 1);
        METADATA_KEYS_TYPE.put(3, 1);
        METADATA_KEYS_TYPE.put(15, 1);
        METADATA_KEYS_TYPE.put(4, 1);
        METADATA_KEYS_TYPE.put(5, 1);
        METADATA_KEYS_TYPE.put(6, 1);
        METADATA_KEYS_TYPE.put(11, 1);
        METADATA_KEYS_TYPE.put(100, 2);
        METADATA_KEYS_TYPE.put(101, 3);
        METADATA_KEYS_TYPE.put(RATING_KEY_BY_USER, 3);
    }
}
