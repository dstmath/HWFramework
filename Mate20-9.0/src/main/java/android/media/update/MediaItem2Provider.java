package android.media.update;

import android.media.DataSourceDesc;
import android.media.MediaItem2;
import android.media.MediaMetadata2;
import android.os.Bundle;

public interface MediaItem2Provider {

    public interface BuilderProvider {
        MediaItem2 build_impl();

        MediaItem2.Builder setDataSourceDesc_impl(DataSourceDesc dataSourceDesc);

        MediaItem2.Builder setMediaId_impl(String str);

        MediaItem2.Builder setMetadata_impl(MediaMetadata2 mediaMetadata2);
    }

    boolean equals_impl(Object obj);

    DataSourceDesc getDataSourceDesc_impl();

    int getFlags_impl();

    String getMediaId_impl();

    MediaMetadata2 getMetadata_impl();

    boolean isBrowsable_impl();

    boolean isPlayable_impl();

    void setMetadata_impl(MediaMetadata2 mediaMetadata2);

    Bundle toBundle_impl();

    String toString_impl();
}
