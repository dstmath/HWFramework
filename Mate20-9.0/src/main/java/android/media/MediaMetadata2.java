package android.media;

import android.graphics.Bitmap;
import android.media.update.ApiLoader;
import android.media.update.MediaMetadata2Provider;
import android.os.Bundle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public final class MediaMetadata2 {
    public static final long BT_FOLDER_TYPE_ALBUMS = 2;
    public static final long BT_FOLDER_TYPE_ARTISTS = 3;
    public static final long BT_FOLDER_TYPE_GENRES = 4;
    public static final long BT_FOLDER_TYPE_MIXED = 0;
    public static final long BT_FOLDER_TYPE_PLAYLISTS = 5;
    public static final long BT_FOLDER_TYPE_TITLES = 1;
    public static final long BT_FOLDER_TYPE_YEARS = 6;
    public static final String METADATA_KEY_ADVERTISEMENT = "android.media.metadata.ADVERTISEMENT";
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
    public static final String METADATA_KEY_DOWNLOAD_STATUS = "android.media.metadata.DOWNLOAD_STATUS";
    public static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";
    public static final String METADATA_KEY_EXTRAS = "android.media.metadata.EXTRAS";
    public static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";
    public static final String METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID";
    public static final String METADATA_KEY_MEDIA_URI = "android.media.metadata.MEDIA_URI";
    public static final String METADATA_KEY_NUM_TRACKS = "android.media.metadata.NUM_TRACKS";
    public static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
    public static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";
    public static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";
    public static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
    public static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";
    public static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";
    public static final long STATUS_DOWNLOADED = 2;
    public static final long STATUS_DOWNLOADING = 1;
    public static final long STATUS_NOT_DOWNLOADED = 0;
    private final MediaMetadata2Provider mProvider;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BitmapKey {
    }

    public static final class Builder {
        private final MediaMetadata2Provider.BuilderProvider mProvider;

        public Builder() {
            this.mProvider = ApiLoader.getProvider().createMediaMetadata2Builder(this);
        }

        public Builder(MediaMetadata2 source) {
            this.mProvider = ApiLoader.getProvider().createMediaMetadata2Builder(this, source);
        }

        public Builder(MediaMetadata2Provider.BuilderProvider provider) {
            this.mProvider = provider;
        }

        public Builder putText(String key, CharSequence value) {
            return this.mProvider.putText_impl(key, value);
        }

        public Builder putString(String key, String value) {
            return this.mProvider.putString_impl(key, value);
        }

        public Builder putLong(String key, long value) {
            return this.mProvider.putLong_impl(key, value);
        }

        public Builder putRating(String key, Rating2 value) {
            return this.mProvider.putRating_impl(key, value);
        }

        public Builder putBitmap(String key, Bitmap value) {
            return this.mProvider.putBitmap_impl(key, value);
        }

        public Builder putFloat(String key, float value) {
            return this.mProvider.putFloat_impl(key, value);
        }

        public Builder setExtras(Bundle extras) {
            return this.mProvider.setExtras_impl(extras);
        }

        public MediaMetadata2 build() {
            return this.mProvider.build_impl();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FloatKey {
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

    public MediaMetadata2(MediaMetadata2Provider provider) {
        this.mProvider = provider;
    }

    public boolean containsKey(String key) {
        return this.mProvider.containsKey_impl(key);
    }

    public CharSequence getText(String key) {
        return this.mProvider.getText_impl(key);
    }

    public String getMediaId() {
        return this.mProvider.getMediaId_impl();
    }

    public String getString(String key) {
        return this.mProvider.getString_impl(key);
    }

    public long getLong(String key) {
        return this.mProvider.getLong_impl(key);
    }

    public Rating2 getRating(String key) {
        return this.mProvider.getRating_impl(key);
    }

    public Bitmap getBitmap(String key) {
        return this.mProvider.getBitmap_impl(key);
    }

    public float getFloat(String key) {
        return this.mProvider.getFloat_impl(key);
    }

    public Bundle getExtras() {
        return this.mProvider.getExtras_impl();
    }

    public int size() {
        return this.mProvider.size_impl();
    }

    public Set<String> keySet() {
        return this.mProvider.keySet_impl();
    }

    public Bundle toBundle() {
        return this.mProvider.toBundle_impl();
    }

    public static MediaMetadata2 fromBundle(Bundle bundle) {
        return ApiLoader.getProvider().fromBundle_MediaMetadata2(bundle);
    }
}
