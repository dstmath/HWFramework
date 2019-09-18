package android.media;

import android.graphics.Bitmap;
import android.media.MediaDescription;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.Set;

public final class MediaMetadata implements Parcelable {
    public static final Parcelable.Creator<MediaMetadata> CREATOR = new Parcelable.Creator<MediaMetadata>() {
        public MediaMetadata createFromParcel(Parcel in) {
            return new MediaMetadata(in);
        }

        public MediaMetadata[] newArray(int size) {
            return new MediaMetadata[size];
        }
    };
    private static final SparseArray<String> EDITOR_KEY_MAPPING = new SparseArray<>();
    /* access modifiers changed from: private */
    public static final ArrayMap<String, Integer> METADATA_KEYS_TYPE = new ArrayMap<>();
    public static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";
    public static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    public static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";
    public static final String METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI";
    public static final String METADATA_KEY_ART = "android.media.metadata.ART";
    public static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";
    public static final String METADATA_KEY_ART_URI = "android.media.metadata.ART_URI";
    public static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";
    public static final String METADATA_KEY_BT_FOLDER_TYPE = "android.media.metadata.BT_FOLDER_TYPE";
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
    public static final String METADATA_KEY_MEDIA_URI = "android.media.metadata.MEDIA_URI";
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
    private static final String[] PREFERRED_BITMAP_ORDER = {"android.media.metadata.DISPLAY_ICON", "android.media.metadata.ART", "android.media.metadata.ALBUM_ART"};
    private static final String[] PREFERRED_DESCRIPTION_ORDER = {"android.media.metadata.TITLE", "android.media.metadata.ARTIST", "android.media.metadata.ALBUM", "android.media.metadata.ALBUM_ARTIST", "android.media.metadata.WRITER", "android.media.metadata.AUTHOR", "android.media.metadata.COMPOSER"};
    private static final String[] PREFERRED_URI_ORDER = {"android.media.metadata.DISPLAY_ICON_URI", "android.media.metadata.ART_URI", "android.media.metadata.ALBUM_ART_URI"};
    private static final String TAG = "MediaMetadata";
    /* access modifiers changed from: private */
    public final Bundle mBundle;
    private MediaDescription mDescription;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BitmapKey {
    }

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
                Object value = this.mBundle.get(key);
                if (value != null && (value instanceof Bitmap)) {
                    Bitmap bmp = (Bitmap) value;
                    if (bmp.getHeight() > maxBitmapSize || bmp.getWidth() > maxBitmapSize) {
                        putBitmap(key, scaleBitmap(bmp, maxBitmapSize));
                    }
                }
            }
        }

        public Builder putText(String key, CharSequence value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 1) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a CharSequence");
        }

        public Builder putString(String key, String value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 1) {
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
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 3) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Rating");
        }

        public Builder putBitmap(String key, Bitmap value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 2) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Bitmap");
        }

        public MediaMetadata build() {
            return new MediaMetadata(this.mBundle);
        }

        private Bitmap scaleBitmap(Bitmap bmp, int maxSize) {
            float maxSizeF = (float) maxSize;
            float scale = Math.min(maxSizeF / ((float) bmp.getWidth()), maxSizeF / ((float) bmp.getHeight()));
            return Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth()) * scale), (int) (((float) bmp.getHeight()) * scale), true);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LongKey {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RatingKey {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TextKey {
    }

    static {
        METADATA_KEYS_TYPE.put("android.media.metadata.TITLE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.ARTIST", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DURATION", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.AUTHOR", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.WRITER", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.COMPOSER", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.COMPILATION", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DATE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.YEAR", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.GENRE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.TRACK_NUMBER", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.NUM_TRACKS", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISC_NUMBER", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM_ARTIST", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.ART", 2);
        METADATA_KEYS_TYPE.put("android.media.metadata.ART_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM_ART", 2);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM_ART_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.USER_RATING", 3);
        METADATA_KEYS_TYPE.put("android.media.metadata.RATING", 3);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_TITLE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_SUBTITLE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_DESCRIPTION", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_ICON", 2);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_ICON_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.BT_FOLDER_TYPE", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.MEDIA_ID", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.MEDIA_URI", 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_LYRIC, 1);
        EDITOR_KEY_MAPPING.put(100, "android.media.metadata.ART");
        EDITOR_KEY_MAPPING.put(101, "android.media.metadata.RATING");
        EDITOR_KEY_MAPPING.put(MediaMetadataEditor.RATING_KEY_BY_USER, "android.media.metadata.USER_RATING");
        EDITOR_KEY_MAPPING.put(1, "android.media.metadata.ALBUM");
        EDITOR_KEY_MAPPING.put(13, "android.media.metadata.ALBUM_ARTIST");
        EDITOR_KEY_MAPPING.put(2, "android.media.metadata.ARTIST");
        EDITOR_KEY_MAPPING.put(3, "android.media.metadata.AUTHOR");
        EDITOR_KEY_MAPPING.put(0, "android.media.metadata.TRACK_NUMBER");
        EDITOR_KEY_MAPPING.put(4, "android.media.metadata.COMPOSER");
        EDITOR_KEY_MAPPING.put(15, "android.media.metadata.COMPILATION");
        EDITOR_KEY_MAPPING.put(5, "android.media.metadata.DATE");
        EDITOR_KEY_MAPPING.put(14, "android.media.metadata.DISC_NUMBER");
        EDITOR_KEY_MAPPING.put(9, "android.media.metadata.DURATION");
        EDITOR_KEY_MAPPING.put(6, "android.media.metadata.GENRE");
        EDITOR_KEY_MAPPING.put(10, "android.media.metadata.NUM_TRACKS");
        EDITOR_KEY_MAPPING.put(7, "android.media.metadata.TITLE");
        EDITOR_KEY_MAPPING.put(11, "android.media.metadata.WRITER");
        EDITOR_KEY_MAPPING.put(8, "android.media.metadata.YEAR");
        EDITOR_KEY_MAPPING.put(1000, METADATA_KEY_LYRIC);
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
        try {
            return (Rating) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Rating.", e);
            return null;
        }
    }

    public Bitmap getBitmap(String key) {
        try {
            return (Bitmap) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
            return null;
        }
    }

    public int describeContents() {
        return 0;
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
        String mediaId = getString("android.media.metadata.MEDIA_ID");
        CharSequence[] text = new CharSequence[3];
        Bitmap icon = null;
        Uri iconUri = null;
        CharSequence displayText = getText("android.media.metadata.DISPLAY_TITLE");
        if (!TextUtils.isEmpty(displayText)) {
            text[0] = displayText;
            text[1] = getText("android.media.metadata.DISPLAY_SUBTITLE");
            text[2] = getText("android.media.metadata.DISPLAY_DESCRIPTION");
        } else {
            int textIndex = 0;
            int textIndex2 = 0;
            while (textIndex < text.length && textIndex2 < PREFERRED_DESCRIPTION_ORDER.length) {
                int keyIndex = textIndex2 + 1;
                CharSequence next = getText(PREFERRED_DESCRIPTION_ORDER[textIndex2]);
                if (!TextUtils.isEmpty(next)) {
                    text[textIndex] = next;
                    textIndex++;
                }
                textIndex2 = keyIndex;
            }
        }
        int i = 0;
        while (true) {
            if (i >= PREFERRED_BITMAP_ORDER.length) {
                break;
            }
            Bitmap next2 = getBitmap(PREFERRED_BITMAP_ORDER[i]);
            if (next2 != null) {
                icon = next2;
                break;
            }
            i++;
        }
        int i2 = 0;
        while (true) {
            if (i2 >= PREFERRED_URI_ORDER.length) {
                break;
            }
            String next3 = getString(PREFERRED_URI_ORDER[i2]);
            if (!TextUtils.isEmpty(next3)) {
                iconUri = Uri.parse(next3);
                break;
            }
            i2++;
        }
        Uri mediaUri = null;
        String mediaUriStr = getString("android.media.metadata.MEDIA_URI");
        if (!TextUtils.isEmpty(mediaUriStr)) {
            mediaUri = Uri.parse(mediaUriStr);
        }
        MediaDescription.Builder bob = new MediaDescription.Builder();
        bob.setMediaId(mediaId);
        bob.setTitle(text[0]);
        bob.setSubtitle(text[1]);
        bob.setDescription(text[2]);
        bob.setIconBitmap(icon);
        bob.setIconUri(iconUri);
        bob.setMediaUri(mediaUri);
        if (this.mBundle.containsKey("android.media.metadata.BT_FOLDER_TYPE")) {
            Bundle bundle = new Bundle();
            bundle.putLong(MediaDescription.EXTRA_BT_FOLDER_TYPE, getLong("android.media.metadata.BT_FOLDER_TYPE"));
            bob.setExtras(bundle);
        }
        this.mDescription = bob.build();
        return this.mDescription;
    }

    public static String getKeyFromMetadataEditorKey(int editorKey) {
        return EDITOR_KEY_MAPPING.get(editorKey, null);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MediaMetadata)) {
            return false;
        }
        MediaMetadata m = (MediaMetadata) o;
        for (int i = 0; i < METADATA_KEYS_TYPE.size(); i++) {
            String key = METADATA_KEYS_TYPE.keyAt(i);
            switch (METADATA_KEYS_TYPE.valueAt(i).intValue()) {
                case 0:
                    if (getLong(key) == m.getLong(key)) {
                        break;
                    } else {
                        return false;
                    }
                case 1:
                    if (Objects.equals(getString(key), m.getString(key))) {
                        break;
                    } else {
                        return false;
                    }
            }
        }
        return true;
    }

    public int hashCode() {
        int hashCode;
        int hashCode2 = 17;
        for (int i = 0; i < METADATA_KEYS_TYPE.size(); i++) {
            String key = METADATA_KEYS_TYPE.keyAt(i);
            switch (METADATA_KEYS_TYPE.valueAt(i).intValue()) {
                case 0:
                    hashCode = (31 * hashCode2) + Long.hashCode(getLong(key));
                    break;
                case 1:
                    hashCode = (31 * hashCode2) + Objects.hash(new Object[]{getString(key)});
                    break;
            }
            hashCode2 = hashCode;
        }
        return hashCode2;
    }
}
