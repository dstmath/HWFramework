package android.media;

import android.media.update.ApiLoader;
import android.media.update.MediaItem2Provider;
import android.os.Bundle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MediaItem2 {
    public static final int FLAG_BROWSABLE = 1;
    public static final int FLAG_PLAYABLE = 2;
    private final MediaItem2Provider mProvider;

    public static final class Builder {
        private final MediaItem2Provider.BuilderProvider mProvider;

        public Builder(int flags) {
            this.mProvider = ApiLoader.getProvider().createMediaItem2Builder(this, flags);
        }

        public Builder setMediaId(String mediaId) {
            return this.mProvider.setMediaId_impl(mediaId);
        }

        public Builder setMetadata(MediaMetadata2 metadata) {
            return this.mProvider.setMetadata_impl(metadata);
        }

        public Builder setDataSourceDesc(DataSourceDesc dataSourceDesc) {
            return this.mProvider.setDataSourceDesc_impl(dataSourceDesc);
        }

        public MediaItem2 build() {
            return this.mProvider.build_impl();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags {
    }

    public MediaItem2(MediaItem2Provider provider) {
        this.mProvider = provider;
    }

    public MediaItem2Provider getProvider() {
        return this.mProvider;
    }

    public Bundle toBundle() {
        return this.mProvider.toBundle_impl();
    }

    public static MediaItem2 fromBundle(Bundle bundle) {
        return ApiLoader.getProvider().fromBundle_MediaItem2(bundle);
    }

    public String toString() {
        return this.mProvider.toString_impl();
    }

    public int getFlags() {
        return this.mProvider.getFlags_impl();
    }

    public boolean isBrowsable() {
        return this.mProvider.isBrowsable_impl();
    }

    public boolean isPlayable() {
        return this.mProvider.isPlayable_impl();
    }

    public void setMetadata(MediaMetadata2 metadata) {
        this.mProvider.setMetadata_impl(metadata);
    }

    public MediaMetadata2 getMetadata() {
        return this.mProvider.getMetadata_impl();
    }

    public String getMediaId() {
        return this.mProvider.getMediaId_impl();
    }

    public DataSourceDesc getDataSourceDesc() {
        return this.mProvider.getDataSourceDesc_impl();
    }

    public boolean equals(Object obj) {
        return this.mProvider.equals_impl(obj);
    }
}
