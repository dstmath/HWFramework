package android.support.v4.media;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.IMediaController2;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaSession2;
import android.text.TextUtils;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class MediaController2Stub extends IMediaController2.Stub {
    private static final boolean DEBUG = true;
    private static final String TAG = "MediaController2Stub";
    private final WeakReference<MediaController2ImplBase> mController;

    MediaController2Stub(MediaController2ImplBase controller) {
        this.mController = new WeakReference<>(controller);
    }

    public void onCurrentMediaItemChanged(Bundle item) {
        try {
            getController().notifyCurrentMediaItemChanged(MediaItem2.fromBundle(item));
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onPlayerStateChanged(long eventTimeMs, long positionMs, int state) {
        try {
            getController().notifyPlayerStateChanges(eventTimeMs, positionMs, state);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onPlaybackSpeedChanged(long eventTimeMs, long positionMs, float speed) {
        try {
            getController().notifyPlaybackSpeedChanges(eventTimeMs, positionMs, speed);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onBufferingStateChanged(Bundle itemBundle, int state, long bufferedPositionMs) {
        try {
            getController().notifyBufferingStateChanged(MediaItem2.fromBundle(itemBundle), state, bufferedPositionMs);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onPlaylistChanged(List<Bundle> playlistBundle, Bundle metadataBundle) {
        try {
            MediaController2ImplBase controller = getController();
            if (playlistBundle == null) {
                Log.w(TAG, "onPlaylistChanged(): Ignoring null playlist from " + controller);
                return;
            }
            List<MediaItem2> playlist = new ArrayList<>();
            for (Bundle bundle : playlistBundle) {
                MediaItem2 item = MediaItem2.fromBundle(bundle);
                if (item == null) {
                    Log.w(TAG, "onPlaylistChanged(): Ignoring null item in playlist");
                } else {
                    playlist.add(item);
                }
            }
            controller.notifyPlaylistChanges(playlist, MediaMetadata2.fromBundle(metadataBundle));
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onPlaylistMetadataChanged(Bundle metadataBundle) throws RuntimeException {
        try {
            getController().notifyPlaylistMetadataChanges(MediaMetadata2.fromBundle(metadataBundle));
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onRepeatModeChanged(int repeatMode) {
        try {
            getController().notifyRepeatModeChanges(repeatMode);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onShuffleModeChanged(int shuffleMode) {
        try {
            getController().notifyShuffleModeChanges(shuffleMode);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onPlaybackInfoChanged(Bundle playbackInfo) throws RuntimeException {
        Log.d(TAG, "onPlaybackInfoChanged");
        try {
            MediaController2ImplBase controller = getController();
            MediaController2.PlaybackInfo info = MediaController2.PlaybackInfo.fromBundle(playbackInfo);
            if (info == null) {
                Log.w(TAG, "onPlaybackInfoChanged(): Ignoring null playbackInfo");
            } else {
                controller.notifyPlaybackInfoChanges(info);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onSeekCompleted(long eventTimeMs, long positionMs, long seekPositionMs) {
        try {
            getController().notifySeekCompleted(eventTimeMs, positionMs, seekPositionMs);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onError(int errorCode, Bundle extras) {
        try {
            getController().notifyError(errorCode, extras);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onRoutesInfoChanged(List<Bundle> routes) {
        try {
            getController().notifyRoutesInfoChanged(routes);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onConnected(IMediaSession2 sessionBinder, Bundle commandGroup, int playerState, Bundle currentItem, long positionEventTimeMs, long positionMs, float playbackSpeed, long bufferedPositionMs, Bundle playbackInfo, int shuffleMode, int repeatMode, List<Bundle> itemBundleList, PendingIntent sessionActivity) {
        List<Bundle> list = itemBundleList;
        MediaController2ImplBase controller = (MediaController2ImplBase) this.mController.get();
        if (controller == null) {
            Log.d(TAG, "onConnected after MediaController2.close()");
            return;
        }
        List<MediaItem2> itemList = null;
        if (list != null) {
            itemList = new ArrayList<>();
            for (int i = 0; i < itemBundleList.size(); i++) {
                MediaItem2 item = MediaItem2.fromBundle(list.get(i));
                if (item != null) {
                    itemList.add(item);
                }
            }
        }
        List<MediaItem2> itemList2 = itemList;
        controller.onConnectedNotLocked(sessionBinder, SessionCommandGroup2.fromBundle(commandGroup), playerState, MediaItem2.fromBundle(currentItem), positionEventTimeMs, positionMs, playbackSpeed, bufferedPositionMs, MediaController2.PlaybackInfo.fromBundle(playbackInfo), repeatMode, shuffleMode, itemList2, sessionActivity);
    }

    public void onDisconnected() {
        MediaController2ImplBase controller = (MediaController2ImplBase) this.mController.get();
        if (controller == null) {
            Log.d(TAG, "onDisconnected after MediaController2.close()");
        } else {
            controller.getInstance().close();
        }
    }

    public void onCustomLayoutChanged(List<Bundle> commandButtonlist) {
        if (commandButtonlist == null) {
            Log.w(TAG, "onCustomLayoutChanged(): Ignoring null commandButtonlist");
            return;
        }
        try {
            MediaController2ImplBase controller = getController();
            if (controller != null) {
                List<MediaSession2.CommandButton> layout = new ArrayList<>();
                for (int i = 0; i < commandButtonlist.size(); i++) {
                    MediaSession2.CommandButton button = MediaSession2.CommandButton.fromBundle(commandButtonlist.get(i));
                    if (button != null) {
                        layout.add(button);
                    }
                }
                controller.onCustomLayoutChanged(layout);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onAllowedCommandsChanged(Bundle commandsBundle) {
        try {
            MediaController2ImplBase controller = getController();
            if (controller != null) {
                SessionCommandGroup2 commands = SessionCommandGroup2.fromBundle(commandsBundle);
                if (commands == null) {
                    Log.w(TAG, "onAllowedCommandsChanged(): Ignoring null commands");
                } else {
                    controller.onAllowedCommandsChanged(commands);
                }
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onCustomCommand(Bundle commandBundle, Bundle args, ResultReceiver receiver) {
        try {
            MediaController2ImplBase controller = getController();
            SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
            if (command == null) {
                Log.w(TAG, "onCustomCommand(): Ignoring null command");
            } else {
                controller.onCustomCommand(command, args, receiver);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onGetLibraryRootDone(Bundle rootHints, String rootMediaId, Bundle rootExtra) throws RuntimeException {
        try {
            MediaBrowser2 browser = getBrowser();
            if (browser != null) {
                Executor callbackExecutor = browser.getCallbackExecutor();
                final MediaBrowser2 mediaBrowser2 = browser;
                final Bundle bundle = rootHints;
                final String str = rootMediaId;
                final Bundle bundle2 = rootExtra;
                AnonymousClass1 r1 = new Runnable() {
                    public void run() {
                        mediaBrowser2.getCallback().onGetLibraryRootDone(mediaBrowser2, bundle, str, bundle2);
                    }
                };
                callbackExecutor.execute(r1);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onGetItemDone(final String mediaId, final Bundle itemBundle) throws RuntimeException {
        if (mediaId == null) {
            Log.w(TAG, "onGetItemDone(): Ignoring null mediaId");
            return;
        }
        try {
            final MediaBrowser2 browser = getBrowser();
            if (browser != null) {
                browser.getCallbackExecutor().execute(new Runnable() {
                    public void run() {
                        browser.getCallback().onGetItemDone(browser, mediaId, MediaItem2.fromBundle(itemBundle));
                    }
                });
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onGetChildrenDone(String parentId, int page, int pageSize, List<Bundle> itemBundleList, Bundle extras) throws RuntimeException {
        if (parentId == null) {
            Log.w(TAG, "onGetChildrenDone(): Ignoring null parentId");
            return;
        }
        try {
            MediaBrowser2 browser = getBrowser();
            if (browser != null) {
                Executor callbackExecutor = browser.getCallbackExecutor();
                final MediaBrowser2 mediaBrowser2 = browser;
                final String str = parentId;
                final int i = page;
                final int i2 = pageSize;
                final List<Bundle> list = itemBundleList;
                final Bundle bundle = extras;
                AnonymousClass3 r1 = new Runnable() {
                    public void run() {
                        mediaBrowser2.getCallback().onGetChildrenDone(mediaBrowser2, str, i, i2, MediaUtils2.convertBundleListToMediaItem2List(list), bundle);
                    }
                };
                callbackExecutor.execute(r1);
            }
        } catch (IllegalStateException e) {
            IllegalStateException illegalStateException = e;
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onSearchResultChanged(String query, int itemCount, Bundle extras) throws RuntimeException {
        if (TextUtils.isEmpty(query)) {
            Log.w(TAG, "onSearchResultChanged(): Ignoring empty query");
            return;
        }
        try {
            MediaBrowser2 browser = getBrowser();
            if (browser != null) {
                Executor callbackExecutor = browser.getCallbackExecutor();
                final MediaBrowser2 mediaBrowser2 = browser;
                final String str = query;
                final int i = itemCount;
                final Bundle bundle = extras;
                AnonymousClass4 r1 = new Runnable() {
                    public void run() {
                        mediaBrowser2.getCallback().onSearchResultChanged(mediaBrowser2, str, i, bundle);
                    }
                };
                callbackExecutor.execute(r1);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onGetSearchResultDone(String query, int page, int pageSize, List<Bundle> itemBundleList, Bundle extras) throws RuntimeException {
        if (TextUtils.isEmpty(query)) {
            Log.w(TAG, "onGetSearchResultDone(): Ignoring empty query");
            return;
        }
        try {
            MediaBrowser2 browser = getBrowser();
            if (browser != null) {
                Executor callbackExecutor = browser.getCallbackExecutor();
                final MediaBrowser2 mediaBrowser2 = browser;
                final String str = query;
                final int i = page;
                final int i2 = pageSize;
                final List<Bundle> list = itemBundleList;
                final Bundle bundle = extras;
                AnonymousClass5 r1 = new Runnable() {
                    public void run() {
                        mediaBrowser2.getCallback().onGetSearchResultDone(mediaBrowser2, str, i, i2, MediaUtils2.convertBundleListToMediaItem2List(list), bundle);
                    }
                };
                callbackExecutor.execute(r1);
            }
        } catch (IllegalStateException e) {
            IllegalStateException illegalStateException = e;
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void onChildrenChanged(String parentId, int itemCount, Bundle extras) {
        if (parentId == null) {
            Log.w(TAG, "onChildrenChanged(): Ignoring null parentId");
            return;
        }
        try {
            MediaBrowser2 browser = getBrowser();
            if (browser != null) {
                Executor callbackExecutor = browser.getCallbackExecutor();
                final MediaBrowser2 mediaBrowser2 = browser;
                final String str = parentId;
                final int i = itemCount;
                final Bundle bundle = extras;
                AnonymousClass6 r1 = new Runnable() {
                    public void run() {
                        mediaBrowser2.getCallback().onChildrenChanged(mediaBrowser2, str, i, bundle);
                    }
                };
                callbackExecutor.execute(r1);
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Don't fail silently here. Highly likely a bug");
        }
    }

    public void destroy() {
        this.mController.clear();
    }

    private MediaController2ImplBase getController() throws IllegalStateException {
        MediaController2ImplBase controller = (MediaController2ImplBase) this.mController.get();
        if (controller != null) {
            return controller;
        }
        throw new IllegalStateException("Controller is released");
    }

    private MediaBrowser2 getBrowser() throws IllegalStateException {
        MediaController2ImplBase controller = getController();
        if (controller.getInstance() instanceof MediaBrowser2) {
            return (MediaBrowser2) controller.getInstance();
        }
        return null;
    }
}
