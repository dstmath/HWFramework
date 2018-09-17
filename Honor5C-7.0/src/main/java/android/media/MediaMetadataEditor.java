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
    protected static final SparseIntArray METADATA_KEYS_TYPE = null;
    protected static final int METADATA_TYPE_BITMAP = 2;
    protected static final int METADATA_TYPE_INVALID = -1;
    protected static final int METADATA_TYPE_LONG = 0;
    protected static final int METADATA_TYPE_RATING = 3;
    protected static final int METADATA_TYPE_STRING = 1;
    public static final int RATING_KEY_BY_OTHERS = 101;
    public static final int RATING_KEY_BY_USER = 268435457;
    private static final String TAG = "MediaMetadataEditor";
    protected boolean mApplied;
    protected boolean mArtworkChanged;
    protected long mEditableKeys;
    protected Bitmap mEditorArtwork;
    protected Bundle mEditorMetadata;
    protected Builder mMetadataBuilder;
    protected boolean mMetadataChanged;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaMetadataEditor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaMetadataEditor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaMetadataEditor.<clinit>():void");
    }

    public abstract void apply();

    protected MediaMetadataEditor() {
        this.mMetadataChanged = false;
        this.mApplied = false;
        this.mArtworkChanged = false;
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

    public synchronized void addEditableKey(int key) {
        if (this.mApplied) {
            Log.e(TAG, "Can't change editable keys of a previously applied MetadataEditor");
            return;
        }
        if (key == RATING_KEY_BY_USER) {
            this.mEditableKeys |= (long) (KEY_EDITABLE_MASK & key);
            this.mMetadataChanged = true;
        } else {
            Log.e(TAG, "Metadata key " + key + " cannot be edited");
        }
    }

    public synchronized void removeEditableKeys() {
        if (this.mApplied) {
            Log.e(TAG, "Can't remove all editable keys of a previously applied MetadataEditor");
            return;
        }
        if (this.mEditableKeys != 0) {
            this.mEditableKeys = 0;
            this.mMetadataChanged = true;
        }
    }

    public synchronized int[] getEditableKeys() {
        if (this.mEditableKeys != 268435457) {
            return null;
        }
        int[] keys = new int[METADATA_TYPE_STRING];
        keys[METADATA_TYPE_LONG] = RATING_KEY_BY_USER;
        return keys;
    }

    public synchronized MediaMetadataEditor putString(int key, String value) throws IllegalArgumentException {
        if (this.mApplied) {
            Log.e(TAG, "Can't edit a previously applied MediaMetadataEditor");
            return this;
        } else if (METADATA_KEYS_TYPE.get(key, METADATA_TYPE_INVALID) != METADATA_TYPE_STRING) {
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
        } else if (METADATA_KEYS_TYPE.get(key, METADATA_TYPE_INVALID) != 0) {
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
        }
        if (key != BITMAP_KEY_ARTWORK) {
            throw new IllegalArgumentException("Invalid type 'Bitmap' for key " + key);
        }
        this.mEditorArtwork = bitmap;
        this.mArtworkChanged = true;
        return this;
    }

    public synchronized MediaMetadataEditor putObject(int key, Object value) throws IllegalArgumentException {
        if (this.mApplied) {
            Log.e(TAG, "Can't edit a previously applied MediaMetadataEditor");
            return this;
        }
        switch (METADATA_KEYS_TYPE.get(key, METADATA_TYPE_INVALID)) {
            case METADATA_TYPE_LONG /*0*/:
                if (value instanceof Long) {
                    return putLong(key, ((Long) value).longValue());
                }
                throw new IllegalArgumentException("Not a non-null Long for key " + key);
            case METADATA_TYPE_STRING /*1*/:
                if (value == null || (value instanceof String)) {
                    return putString(key, (String) value);
                }
                throw new IllegalArgumentException("Not a String for key " + key);
            case METADATA_TYPE_BITMAP /*2*/:
                if (value != null) {
                    if (!(value instanceof Bitmap)) {
                        throw new IllegalArgumentException("Not a Bitmap for key " + key);
                    }
                    break;
                }
                return putBitmap(key, (Bitmap) value);
            case METADATA_TYPE_RATING /*3*/:
                this.mEditorMetadata.putParcelable(String.valueOf(key), (Parcelable) value);
                this.mMetadataChanged = true;
                return this;
            default:
                throw new IllegalArgumentException("Invalid key " + key);
        }
    }

    public synchronized long getLong(int key, long defaultValue) throws IllegalArgumentException {
        if (METADATA_KEYS_TYPE.get(key, METADATA_TYPE_INVALID) != 0) {
            throw new IllegalArgumentException("Invalid type 'long' for key " + key);
        }
        return this.mEditorMetadata.getLong(String.valueOf(key), defaultValue);
    }

    public synchronized String getString(int key, String defaultValue) throws IllegalArgumentException {
        if (METADATA_KEYS_TYPE.get(key, METADATA_TYPE_INVALID) != METADATA_TYPE_STRING) {
            throw new IllegalArgumentException("Invalid type 'String' for key " + key);
        }
        return this.mEditorMetadata.getString(String.valueOf(key), defaultValue);
    }

    public synchronized Bitmap getBitmap(int key, Bitmap defaultValue) throws IllegalArgumentException {
        if (key != BITMAP_KEY_ARTWORK) {
            throw new IllegalArgumentException("Invalid type 'Bitmap' for key " + key);
        } else if (this.mEditorArtwork != null) {
            defaultValue = this.mEditorArtwork;
        }
        return defaultValue;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Object getObject(int key, Object defaultValue) throws IllegalArgumentException {
        switch (METADATA_KEYS_TYPE.get(key, METADATA_TYPE_INVALID)) {
            case METADATA_TYPE_LONG /*0*/:
                if (!this.mEditorMetadata.containsKey(String.valueOf(key))) {
                    return defaultValue;
                }
                return Long.valueOf(this.mEditorMetadata.getLong(String.valueOf(key)));
            case METADATA_TYPE_STRING /*1*/:
                if (!this.mEditorMetadata.containsKey(String.valueOf(key))) {
                    return defaultValue;
                }
                return this.mEditorMetadata.getString(String.valueOf(key));
            case METADATA_TYPE_BITMAP /*2*/:
                if (key == BITMAP_KEY_ARTWORK) {
                    if (this.mEditorArtwork != null) {
                        defaultValue = this.mEditorArtwork;
                    }
                    return defaultValue;
                }
                break;
            case METADATA_TYPE_RATING /*3*/:
                if (!this.mEditorMetadata.containsKey(String.valueOf(key))) {
                    return defaultValue;
                }
                return this.mEditorMetadata.getParcelable(String.valueOf(key));
        }
    }
}
