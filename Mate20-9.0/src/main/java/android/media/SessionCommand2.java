package android.media;

import android.media.update.ApiLoader;
import android.media.update.MediaSession2Provider;
import android.os.Bundle;

public final class SessionCommand2 {
    public static final int COMMAND_CODE_ADJUST_VOLUME = 11;
    public static final int COMMAND_CODE_CUSTOM = 0;
    public static final int COMMAND_CODE_LIBRARY_GET_CHILDREN = 29;
    public static final int COMMAND_CODE_LIBRARY_GET_ITEM = 30;
    public static final int COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT = 31;
    public static final int COMMAND_CODE_LIBRARY_GET_SEARCH_RESULT = 32;
    public static final int COMMAND_CODE_LIBRARY_SEARCH = 33;
    public static final int COMMAND_CODE_LIBRARY_SUBSCRIBE = 34;
    public static final int COMMAND_CODE_LIBRARY_UNSUBSCRIBE = 35;
    public static final int COMMAND_CODE_PLAYBACK_PAUSE = 2;
    public static final int COMMAND_CODE_PLAYBACK_PLAY = 1;
    public static final int COMMAND_CODE_PLAYBACK_PREPARE = 6;
    public static final int COMMAND_CODE_PLAYBACK_SEEK_TO = 9;
    public static final int COMMAND_CODE_PLAYBACK_STOP = 3;
    public static final int COMMAND_CODE_PLAYLIST_ADD_ITEM = 15;
    public static final int COMMAND_CODE_PLAYLIST_GET_LIST = 18;
    public static final int COMMAND_CODE_PLAYLIST_GET_LIST_METADATA = 20;
    public static final int COMMAND_CODE_PLAYLIST_REMOVE_ITEM = 16;
    public static final int COMMAND_CODE_PLAYLIST_REPLACE_ITEM = 17;
    public static final int COMMAND_CODE_PLAYLIST_SET_LIST = 19;
    public static final int COMMAND_CODE_PLAYLIST_SET_LIST_METADATA = 21;
    public static final int COMMAND_CODE_PLAYLIST_SET_REPEAT_MODE = 14;
    public static final int COMMAND_CODE_PLAYLIST_SET_SHUFFLE_MODE = 13;
    public static final int COMMAND_CODE_PLAYLIST_SKIP_NEXT_ITEM = 4;
    public static final int COMMAND_CODE_PLAYLIST_SKIP_PREV_ITEM = 5;
    public static final int COMMAND_CODE_PLAYLIST_SKIP_TO_PLAYLIST_ITEM = 12;
    public static final int COMMAND_CODE_SESSION_FAST_FORWARD = 7;
    public static final int COMMAND_CODE_SESSION_PLAY_FROM_MEDIA_ID = 22;
    public static final int COMMAND_CODE_SESSION_PLAY_FROM_SEARCH = 24;
    public static final int COMMAND_CODE_SESSION_PLAY_FROM_URI = 23;
    public static final int COMMAND_CODE_SESSION_PREPARE_FROM_MEDIA_ID = 25;
    public static final int COMMAND_CODE_SESSION_PREPARE_FROM_SEARCH = 27;
    public static final int COMMAND_CODE_SESSION_PREPARE_FROM_URI = 26;
    public static final int COMMAND_CODE_SESSION_REWIND = 8;
    public static final int COMMAND_CODE_SESSION_SET_RATING = 28;
    public static final int COMMAND_CODE_SET_VOLUME = 10;
    private final MediaSession2Provider.CommandProvider mProvider;

    public SessionCommand2(int commandCode) {
        this.mProvider = ApiLoader.getProvider().createMediaSession2Command(this, commandCode, null, null);
    }

    public SessionCommand2(String action, Bundle extras) {
        if (action != null) {
            this.mProvider = ApiLoader.getProvider().createMediaSession2Command(this, 0, action, extras);
            return;
        }
        throw new IllegalArgumentException("action shouldn't be null");
    }

    public MediaSession2Provider.CommandProvider getProvider() {
        return this.mProvider;
    }

    public int getCommandCode() {
        return this.mProvider.getCommandCode_impl();
    }

    public String getCustomCommand() {
        return this.mProvider.getCustomCommand_impl();
    }

    public Bundle getExtras() {
        return this.mProvider.getExtras_impl();
    }

    public Bundle toBundle() {
        return this.mProvider.toBundle_impl();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SessionCommand2)) {
            return false;
        }
        return this.mProvider.equals_impl(((SessionCommand2) obj).mProvider);
    }

    public int hashCode() {
        return this.mProvider.hashCode_impl();
    }

    public static SessionCommand2 fromBundle(Bundle command) {
        return ApiLoader.getProvider().fromBundle_MediaSession2Command(command);
    }
}
