package android.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.util.Set;

public final class MediaMetadata implements Parcelable {
    public static final Creator<MediaMetadata> CREATOR = null;
    private static final SparseArray<String> EDITOR_KEY_MAPPING = null;
    private static final ArrayMap<String, Integer> METADATA_KEYS_TYPE = null;
    public static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";
    public static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    public static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";
    public static final String METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI";
    public static final String METADATA_KEY_ART = "android.media.metadata.ART";
    public static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";
    public static final String METADATA_KEY_ART_URI = "android.media.metadata.ART_URI";
    public static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";
    public static final String METADATA_KEY_COMPILATION = "android.media.metadata.COMPILATION";
    public static final String METADATA_KEY_COMPOSER = "android.media.metadata.COMPOSER";
    public static final String METADATA_KEY_DATE = "android.media.metadata.DATE";
    public static final String METADATA_KEY_DISC_NUMBER = "android.media.metadata.DISC_NUMBER";
    public static final String METADATA_KEY_DISPLAY_DESCRIPTION = "android.media.metadata.DISPLAY_DESCRIPTION";
    public static final String METADATA_KEY_DISPLAY_ICON = "android.media.metadata.DISPLAY_ICON";
    public static final String METADATA_KEY_DISPLAY_ICON_URI = "android.media.metadata.DISPLAY_ICON_URI";
    public static final String METADATA_KEY_DISPLAY_SUBTITLE = "android.media.metadata.DISPLAY_SUBTITLE";
    public static final String METADATA_KEY_DISPLAY_TITLE = "android.media.metadata.DISPLAY_TITLE";
    public static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";
    public static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";
    public static final String METADATA_KEY_LYRIC = "android.media.metadata.LYRIC";
    public static final String METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID";
    public static final String METADATA_KEY_NUM_TRACKS = "android.media.metadata.NUM_TRACKS";
    public static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
    public static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";
    public static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";
    public static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
    public static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";
    public static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";
    private static final int METADATA_TYPE_BITMAP = 2;
    private static final int METADATA_TYPE_INVALID = -1;
    private static final int METADATA_TYPE_LONG = 0;
    private static final int METADATA_TYPE_RATING = 3;
    private static final int METADATA_TYPE_TEXT = 1;
    private static final String[] PREFERRED_BITMAP_ORDER = null;
    private static final String[] PREFERRED_DESCRIPTION_ORDER = null;
    private static final String[] PREFERRED_URI_ORDER = null;
    private static final String TAG = "MediaMetadata";
    private final Bundle mBundle;
    private MediaDescription mDescription;

    public static final class Builder {
        private final Bundle mBundle;

        public Builder() {
            this.mBundle = new Bundle();
        }

        public Builder(MediaMetadata source) {
            this.mBundle = new Bundle(source.mBundle);
        }

        public Builder(MediaMetadata source, int maxBitmapSize) {
            this(source);
            for (String key : this.mBundle.keySet()) {
                Bitmap value = this.mBundle.get(key);
                if (value != null && (value instanceof Bitmap)) {
                    Bitmap bmp = value;
                    if (bmp.getHeight() > maxBitmapSize || bmp.getWidth() > maxBitmapSize) {
                        putBitmap(key, scaleBitmap(bmp, maxBitmapSize));
                    }
                }
            }
        }

        public Builder putText(String key, CharSequence value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadata.METADATA_TYPE_TEXT) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a CharSequence");
        }

        public Builder putString(String key, String value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadata.METADATA_TYPE_TEXT) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a String");
        }

        public Builder putLong(String key, long value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 0) {
                this.mBundle.putLong(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a long");
        }

        public Builder putRating(String key, Rating value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadata.METADATA_TYPE_RATING) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Rating");
        }

        public Builder putBitmap(String key, Bitmap value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadata.METADATA_TYPE_BITMAP) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Bitmap");
        }

        public MediaMetadata build() {
            return new MediaMetadata(null);
        }

        private Bitmap scaleBitmap(Bitmap bmp, int maxSize) {
            float maxSizeF = (float) maxSize;
            float scale = Math.min(maxSizeF / ((float) bmp.getWidth()), maxSizeF / ((float) bmp.getHeight()));
            return Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth()) * scale), (int) (((float) bmp.getHeight()) * scale), true);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaMetadata.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaMetadata.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaMetadata.<clinit>():void");
    }

    private MediaMetadata(Bundle bundle) {
        this.mBundle = new Bundle(bundle);
    }

    private MediaMetadata(Parcel in) {
        this.mBundle = Bundle.setDefusable(in.readBundle(), true);
    }

    public boolean containsKey(String key) {
        return this.mBundle.containsKey(key);
    }

    public CharSequence getText(String key) {
        return this.mBundle.getCharSequence(key);
    }

    public String getString(String key) {
        CharSequence text = getText(key);
        if (text != null) {
            return text.toString();
        }
        return null;
    }

    public long getLong(String key) {
        return this.mBundle.getLong(key, 0);
    }

    public Rating getRating(String key) {
        Rating rating = null;
        try {
            return (Rating) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Rating.", e);
            return rating;
        }
    }

    public Bitmap getBitmap(String key) {
        Bitmap bmp = null;
        try {
            return (Bitmap) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
            return bmp;
        }
    }

    public int describeContents() {
        return METADATA_TYPE_LONG;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mBundle);
    }

    public int size() {
        return this.mBundle.size();
    }

    public Set<String> keySet() {
        return this.mBundle.keySet();
    }

    public MediaDescription getDescription() {
        if (this.mDescription != null) {
            return this.mDescription;
        }
        int i;
        String mediaId = getString(METADATA_KEY_MEDIA_ID);
        CharSequence[] text = new CharSequence[METADATA_TYPE_RATING];
        Bitmap icon = null;
        Uri iconUri = null;
        CharSequence displayText = getText(METADATA_KEY_DISPLAY_TITLE);
        if (TextUtils.isEmpty(displayText)) {
            int textIndex = METADATA_TYPE_LONG;
            int keyIndex = METADATA_TYPE_LONG;
            while (textIndex < text.length && keyIndex < PREFERRED_DESCRIPTION_ORDER.length) {
                int keyIndex2 = keyIndex + METADATA_TYPE_TEXT;
                CharSequence next = getText(PREFERRED_DESCRIPTION_ORDER[keyIndex]);
                if (!TextUtils.isEmpty(next)) {
                    int textIndex2 = textIndex + METADATA_TYPE_TEXT;
                    text[textIndex] = next;
                    textIndex = textIndex2;
                }
                keyIndex = keyIndex2;
            }
        } else {
            text[METADATA_TYPE_LONG] = displayText;
            text[METADATA_TYPE_TEXT] = getText(METADATA_KEY_DISPLAY_SUBTITLE);
            text[METADATA_TYPE_BITMAP] = getText(METADATA_KEY_DISPLAY_DESCRIPTION);
        }
        for (i = METADATA_TYPE_LONG; i < PREFERRED_BITMAP_ORDER.length; i += METADATA_TYPE_TEXT) {
            Bitmap next2 = getBitmap(PREFERRED_BITMAP_ORDER[i]);
            if (next2 != null) {
                icon = next2;
                break;
            }
        }
        for (i = METADATA_TYPE_LONG; i < PREFERRED_URI_ORDER.length; i += METADATA_TYPE_TEXT) {
            String next3 = getString(PREFERRED_URI_ORDER[i]);
            if (!TextUtils.isEmpty(next3)) {
                iconUri = Uri.parse(next3);
                break;
            }
        }
        android.media.MediaDescription.Builder bob = new android.media.MediaDescription.Builder();
        bob.setMediaId(mediaId);
        bob.setTitle(text[METADATA_TYPE_LONG]);
        bob.setSubtitle(text[METADATA_TYPE_TEXT]);
        bob.setDescription(text[METADATA_TYPE_BITMAP]);
        bob.setIconBitmap(icon);
        bob.setIconUri(iconUri);
        this.mDescription = bob.build();
        return this.mDescription;
    }

    public static String getKeyFromMetadataEditorKey(int editorKey) {
        return (String) EDITOR_KEY_MAPPING.get(editorKey, null);
    }
}
