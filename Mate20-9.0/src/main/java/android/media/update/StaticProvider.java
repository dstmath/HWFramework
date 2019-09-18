package android.media.update;

import android.app.Notification;
import android.content.Context;
import android.media.MediaBrowser2;
import android.media.MediaController2;
import android.media.MediaItem2;
import android.media.MediaLibraryService2;
import android.media.MediaMetadata2;
import android.media.MediaPlaylistAgent;
import android.media.MediaSession2;
import android.media.MediaSessionService2;
import android.media.Rating2;
import android.media.SessionCommand2;
import android.media.SessionCommandGroup2;
import android.media.SessionToken2;
import android.media.VolumeProvider2;
import android.media.update.MediaItem2Provider;
import android.media.update.MediaLibraryService2Provider;
import android.media.update.MediaMetadata2Provider;
import android.media.update.MediaSession2Provider;
import android.media.update.MediaSessionService2Provider;
import android.os.Bundle;
import android.os.IInterface;
import android.util.AttributeSet;
import android.widget.MediaControlView2;
import android.widget.VideoView2;
import java.util.concurrent.Executor;

public interface StaticProvider {
    MediaBrowser2Provider createMediaBrowser2(Context context, MediaBrowser2 mediaBrowser2, SessionToken2 sessionToken2, Executor executor, MediaBrowser2.BrowserCallback browserCallback);

    MediaControlView2Provider createMediaControlView2(MediaControlView2 mediaControlView2, ViewGroupProvider viewGroupProvider, ViewGroupProvider viewGroupProvider2, AttributeSet attributeSet, int i, int i2);

    MediaController2Provider createMediaController2(Context context, MediaController2 mediaController2, SessionToken2 sessionToken2, Executor executor, MediaController2.ControllerCallback controllerCallback);

    MediaItem2Provider.BuilderProvider createMediaItem2Builder(MediaItem2.Builder builder, int i);

    MediaSessionService2Provider createMediaLibraryService2(MediaLibraryService2 mediaLibraryService2);

    MediaSession2Provider.BuilderBaseProvider<MediaLibraryService2.MediaLibrarySession, MediaLibraryService2.MediaLibrarySession.MediaLibrarySessionCallback> createMediaLibraryService2Builder(MediaLibraryService2 mediaLibraryService2, MediaLibraryService2.MediaLibrarySession.Builder builder, Executor executor, MediaLibraryService2.MediaLibrarySession.MediaLibrarySessionCallback mediaLibrarySessionCallback);

    MediaLibraryService2Provider.LibraryRootProvider createMediaLibraryService2LibraryRoot(MediaLibraryService2.LibraryRoot libraryRoot, String str, Bundle bundle);

    MediaMetadata2Provider.BuilderProvider createMediaMetadata2Builder(MediaMetadata2.Builder builder);

    MediaMetadata2Provider.BuilderProvider createMediaMetadata2Builder(MediaMetadata2.Builder builder, MediaMetadata2 mediaMetadata2);

    MediaPlaylistAgentProvider createMediaPlaylistAgent(MediaPlaylistAgent mediaPlaylistAgent);

    MediaSession2Provider.BuilderBaseProvider<MediaSession2, MediaSession2.SessionCallback> createMediaSession2Builder(Context context, MediaSession2.Builder builder);

    MediaSession2Provider.CommandProvider createMediaSession2Command(SessionCommand2 sessionCommand2, int i, String str, Bundle bundle);

    MediaSession2Provider.CommandButtonProvider.BuilderProvider createMediaSession2CommandButtonBuilder(MediaSession2.CommandButton.Builder builder);

    MediaSession2Provider.CommandGroupProvider createMediaSession2CommandGroup(SessionCommandGroup2 sessionCommandGroup2, SessionCommandGroup2 sessionCommandGroup22);

    MediaSession2Provider.ControllerInfoProvider createMediaSession2ControllerInfo(Context context, MediaSession2.ControllerInfo controllerInfo, int i, int i2, String str, IInterface iInterface);

    MediaSessionService2Provider createMediaSessionService2(MediaSessionService2 mediaSessionService2);

    MediaSessionService2Provider.MediaNotificationProvider createMediaSessionService2MediaNotification(MediaSessionService2.MediaNotification mediaNotification, int i, Notification notification);

    SessionToken2Provider createSessionToken2(Context context, SessionToken2 sessionToken2, String str, String str2, int i);

    VideoView2Provider createVideoView2(VideoView2 videoView2, ViewGroupProvider viewGroupProvider, ViewGroupProvider viewGroupProvider2, AttributeSet attributeSet, int i, int i2);

    VolumeProvider2Provider createVolumeProvider2(VolumeProvider2 volumeProvider2, int i, int i2, int i3);

    MediaItem2 fromBundle_MediaItem2(Bundle bundle);

    MediaMetadata2 fromBundle_MediaMetadata2(Bundle bundle);

    SessionCommand2 fromBundle_MediaSession2Command(Bundle bundle);

    SessionCommandGroup2 fromBundle_MediaSession2CommandGroup(Bundle bundle);

    Rating2 fromBundle_Rating2(Bundle bundle);

    SessionToken2 fromBundle_SessionToken2(Bundle bundle);

    Rating2 newHeartRating_Rating2(boolean z);

    Rating2 newPercentageRating_Rating2(float f);

    Rating2 newStarRating_Rating2(int i, float f);

    Rating2 newThumbRating_Rating2(boolean z);

    Rating2 newUnratedRating_Rating2(int i);
}
