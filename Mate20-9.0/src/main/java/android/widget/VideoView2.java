package android.widget;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.DataSourceDesc;
import android.media.MediaItem2;
import android.media.MediaMetadata2;
import android.media.SessionToken2;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.media.update.ApiLoader;
import android.media.update.VideoView2Provider;
import android.media.update.ViewGroupHelper;
import android.media.update.ViewGroupProvider;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class VideoView2 extends ViewGroupHelper<VideoView2Provider> {
    public static final int VIEW_TYPE_SURFACEVIEW = 1;
    public static final int VIEW_TYPE_TEXTUREVIEW = 2;

    public interface OnCustomActionListener {
        void onCustomAction(String str, Bundle bundle);
    }

    public interface OnFullScreenRequestListener {
        void onFullScreenRequest(View view, boolean z);
    }

    @VisibleForTesting
    public interface OnViewTypeChangedListener {
        void onViewTypeChanged(View view, int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewType {
    }

    public VideoView2(Context context) {
        this(context, null);
    }

    public VideoView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView2(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VideoView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(new ViewGroupHelper.ProviderCreator(defStyleAttr, defStyleRes) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final ViewGroupProvider createProvider(ViewGroupHelper viewGroupHelper, ViewGroupProvider viewGroupProvider, ViewGroupProvider viewGroupProvider2) {
                return ApiLoader.getProvider().createVideoView2((VideoView2) viewGroupHelper, viewGroupProvider, viewGroupProvider2, AttributeSet.this, this.f$1, this.f$2);
            }
        }, context, attrs, defStyleAttr, defStyleRes);
        this.mProvider.initialize(attrs, defStyleAttr, defStyleRes);
    }

    public void setMediaControlView2(MediaControlView2 mediaControlView, long intervalMs) {
        this.mProvider.setMediaControlView2_impl(mediaControlView, intervalMs);
    }

    public MediaControlView2 getMediaControlView2() {
        return this.mProvider.getMediaControlView2_impl();
    }

    public void setMediaMetadata(MediaMetadata2 metadata) {
        this.mProvider.setMediaMetadata_impl(metadata);
    }

    public MediaMetadata2 getMediaMetadata() {
        return this.mProvider.getMediaMetadata_impl();
    }

    public MediaController getMediaController() {
        return this.mProvider.getMediaController_impl();
    }

    public SessionToken2 getMediaSessionToken() {
        return this.mProvider.getMediaSessionToken_impl();
    }

    public void setSubtitleEnabled(boolean enable) {
        this.mProvider.setSubtitleEnabled_impl(enable);
    }

    public boolean isSubtitleEnabled() {
        return this.mProvider.isSubtitleEnabled_impl();
    }

    public void setSpeed(float speed) {
        this.mProvider.setSpeed_impl(speed);
    }

    public void setAudioFocusRequest(int focusGain) {
        this.mProvider.setAudioFocusRequest_impl(focusGain);
    }

    public void setAudioAttributes(AudioAttributes attributes) {
        this.mProvider.setAudioAttributes_impl(attributes);
    }

    public void setVideoPath(String path) {
        this.mProvider.setVideoPath_impl(path);
    }

    public void setVideoUri(Uri uri) {
        this.mProvider.setVideoUri_impl(uri);
    }

    public void setVideoUri(Uri uri, Map<String, String> headers) {
        this.mProvider.setVideoUri_impl(uri, headers);
    }

    public void setMediaItem(MediaItem2 mediaItem) {
        this.mProvider.setMediaItem_impl(mediaItem);
    }

    public void setDataSource(DataSourceDesc dataSource) {
        this.mProvider.setDataSource_impl(dataSource);
    }

    public void setViewType(int viewType) {
        this.mProvider.setViewType_impl(viewType);
    }

    public int getViewType() {
        return this.mProvider.getViewType_impl();
    }

    public void setCustomActions(List<PlaybackState.CustomAction> actionList, Executor executor, OnCustomActionListener listener) {
        this.mProvider.setCustomActions_impl(actionList, executor, listener);
    }

    @VisibleForTesting
    public void setOnViewTypeChangedListener(OnViewTypeChangedListener l) {
        this.mProvider.setOnViewTypeChangedListener_impl(l);
    }

    public void setFullScreenRequestListener(OnFullScreenRequestListener l) {
        this.mProvider.setFullScreenRequestListener_impl(l);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mProvider.onLayout_impl(changed, l, t, r, b);
    }
}
