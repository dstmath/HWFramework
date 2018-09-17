package android.media;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.media.MediaMetadata.Builder;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.MediaSessionLegacyHelper;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.rms.HwSysResource;
import android.util.Log;

@Deprecated
public class RemoteControlClient {
    private static final boolean DEBUG = false;
    public static final int DEFAULT_PLAYBACK_VOLUME = 15;
    public static final int DEFAULT_PLAYBACK_VOLUME_HANDLING = 1;
    public static final int FLAGS_KEY_MEDIA_NONE = 0;
    public static final int FLAG_INFORMATION_REQUEST_ALBUM_ART = 8;
    public static final int FLAG_INFORMATION_REQUEST_KEY_MEDIA = 2;
    public static final int FLAG_INFORMATION_REQUEST_METADATA = 1;
    public static final int FLAG_INFORMATION_REQUEST_PLAYSTATE = 4;
    public static final int FLAG_KEY_MEDIA_FAST_FORWARD = 64;
    public static final int FLAG_KEY_MEDIA_NEXT = 128;
    public static final int FLAG_KEY_MEDIA_PAUSE = 16;
    public static final int FLAG_KEY_MEDIA_PLAY = 4;
    public static final int FLAG_KEY_MEDIA_PLAY_PAUSE = 8;
    public static final int FLAG_KEY_MEDIA_POSITION_UPDATE = 256;
    public static final int FLAG_KEY_MEDIA_PREVIOUS = 1;
    public static final int FLAG_KEY_MEDIA_RATING = 512;
    public static final int FLAG_KEY_MEDIA_REWIND = 2;
    public static final int FLAG_KEY_MEDIA_STOP = 32;
    public static int MEDIA_POSITION_READABLE = 0;
    public static int MEDIA_POSITION_WRITABLE = 0;
    public static final int PLAYBACKINFO_INVALID_VALUE = Integer.MIN_VALUE;
    public static final int PLAYBACKINFO_PLAYBACK_TYPE = 1;
    public static final int PLAYBACKINFO_USES_STREAM = 5;
    public static final int PLAYBACKINFO_VOLUME = 2;
    public static final int PLAYBACKINFO_VOLUME_HANDLING = 4;
    public static final int PLAYBACKINFO_VOLUME_MAX = 3;
    public static final long PLAYBACK_POSITION_ALWAYS_UNKNOWN = -9216204211029966080L;
    public static final long PLAYBACK_POSITION_INVALID = -1;
    public static final float PLAYBACK_SPEED_1X = 1.0f;
    public static final int PLAYBACK_TYPE_LOCAL = 0;
    private static final int PLAYBACK_TYPE_MAX = 1;
    private static final int PLAYBACK_TYPE_MIN = 0;
    public static final int PLAYBACK_TYPE_REMOTE = 1;
    public static final int PLAYBACK_VOLUME_FIXED = 0;
    public static final int PLAYBACK_VOLUME_VARIABLE = 1;
    public static final int PLAYSTATE_BUFFERING = 8;
    public static final int PLAYSTATE_ERROR = 9;
    public static final int PLAYSTATE_FAST_FORWARDING = 4;
    public static final int PLAYSTATE_NONE = 0;
    public static final int PLAYSTATE_PAUSED = 2;
    public static final int PLAYSTATE_PLAYING = 3;
    public static final int PLAYSTATE_REWINDING = 5;
    public static final int PLAYSTATE_SKIPPING_BACKWARDS = 7;
    public static final int PLAYSTATE_SKIPPING_FORWARDS = 6;
    public static final int PLAYSTATE_STOPPED = 1;
    private static final long POSITION_DRIFT_MAX_MS = 500;
    private static final long POSITION_REFRESH_PERIOD_MIN_MS = 2000;
    private static final long POSITION_REFRESH_PERIOD_PLAYING_MS = 15000;
    public static final int RCSE_ID_UNREGISTERED = -1;
    private static final String TAG = "RemoteControlClient";
    private final Object mCacheLock;
    private int mCurrentClientGenId;
    private MediaMetadata mMediaMetadata;
    private Bundle mMetadata;
    private OnMetadataUpdateListener mMetadataUpdateListener;
    private boolean mNeedsPositionSync;
    private Bitmap mOriginalArtwork;
    private long mPlaybackPositionMs;
    private float mPlaybackSpeed;
    private int mPlaybackState;
    private long mPlaybackStateChangeTimeMs;
    private OnGetPlaybackPositionListener mPositionProvider;
    private OnPlaybackPositionUpdateListener mPositionUpdateListener;
    private final PendingIntent mRcMediaIntent;
    private MediaSession mSession;
    private PlaybackState mSessionPlaybackState;
    private int mTransportControlFlags;
    private Callback mTransportListener;

    @Deprecated
    public class MetadataEditor extends MediaMetadataEditor {
        public static final int BITMAP_KEY_ARTWORK = 100;
        public static final int METADATA_KEY_ARTWORK = 100;

        private MetadataEditor() {
        }

        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }

        public synchronized MetadataEditor putString(int key, String value) throws IllegalArgumentException {
            super.putString(key, value);
            if (this.mMetadataBuilder != null) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putText(metadataKey, value);
                }
            }
            return this;
        }

        public synchronized MetadataEditor putLong(int key, long value) throws IllegalArgumentException {
            super.putLong(key, value);
            if (this.mMetadataBuilder != null) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putLong(metadataKey, value);
                }
            }
            return this;
        }

        public synchronized MetadataEditor putBitmap(int key, Bitmap bitmap) throws IllegalArgumentException {
            super.putBitmap(key, bitmap);
            if (this.mMetadataBuilder != null) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putBitmap(metadataKey, bitmap);
                }
            }
            return this;
        }

        public synchronized MetadataEditor putObject(int key, Object object) throws IllegalArgumentException {
            super.putObject(key, object);
            if (this.mMetadataBuilder != null && (key == MediaMetadataEditor.RATING_KEY_BY_USER || key == HwSysResource.MAINSERVICES)) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putRating(metadataKey, (Rating) object);
                }
            }
            return this;
        }

        public synchronized void clear() {
            super.clear();
        }

        public synchronized void apply() {
            if (this.mApplied) {
                Log.e(RemoteControlClient.TAG, "Can't apply a previously applied MetadataEditor");
                return;
            }
            synchronized (RemoteControlClient.this.mCacheLock) {
                RemoteControlClient.this.mMetadata = new Bundle(this.mEditorMetadata);
                RemoteControlClient.this.mMetadata.putLong(String.valueOf(MediaMetadataEditor.KEY_EDITABLE_MASK), this.mEditableKeys);
                if (!(RemoteControlClient.this.mOriginalArtwork == null || RemoteControlClient.this.mOriginalArtwork.equals(this.mEditorArtwork))) {
                    RemoteControlClient.this.mOriginalArtwork.recycle();
                }
                RemoteControlClient.this.mOriginalArtwork = this.mEditorArtwork;
                this.mEditorArtwork = null;
                if (!(RemoteControlClient.this.mSession == null || this.mMetadataBuilder == null)) {
                    RemoteControlClient.this.mMediaMetadata = this.mMetadataBuilder.build();
                    RemoteControlClient.this.mSession.setMetadata(RemoteControlClient.this.mMediaMetadata);
                }
                this.mApplied = true;
            }
        }
    }

    public interface OnGetPlaybackPositionListener {
        long onGetPlaybackPosition();
    }

    public interface OnMetadataUpdateListener {
        void onMetadataUpdate(int i, Object obj);
    }

    public interface OnPlaybackPositionUpdateListener {
        void onPlaybackPositionUpdate(long j);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.RemoteControlClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.RemoteControlClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteControlClient.<clinit>():void");
    }

    public RemoteControlClient(PendingIntent mediaButtonIntent) {
        this.mCacheLock = new Object();
        this.mPlaybackState = PLAYSTATE_NONE;
        this.mPlaybackStateChangeTimeMs = 0;
        this.mPlaybackPositionMs = PLAYBACK_POSITION_INVALID;
        this.mPlaybackSpeed = PLAYBACK_SPEED_1X;
        this.mTransportControlFlags = PLAYSTATE_NONE;
        this.mMetadata = new Bundle();
        this.mCurrentClientGenId = RCSE_ID_UNREGISTERED;
        this.mNeedsPositionSync = DEBUG;
        this.mSessionPlaybackState = null;
        this.mTransportListener = new Callback() {
            public void onSeekTo(long pos) {
                RemoteControlClient.this.onSeekTo(RemoteControlClient.this.mCurrentClientGenId, pos);
            }

            public void onSetRating(Rating rating) {
                if ((RemoteControlClient.this.mTransportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_RATING) != 0) {
                    RemoteControlClient.this.onUpdateMetadata(RemoteControlClient.this.mCurrentClientGenId, MediaMetadataEditor.RATING_KEY_BY_USER, rating);
                }
            }
        };
        this.mRcMediaIntent = mediaButtonIntent;
    }

    public RemoteControlClient(PendingIntent mediaButtonIntent, Looper looper) {
        this.mCacheLock = new Object();
        this.mPlaybackState = PLAYSTATE_NONE;
        this.mPlaybackStateChangeTimeMs = 0;
        this.mPlaybackPositionMs = PLAYBACK_POSITION_INVALID;
        this.mPlaybackSpeed = PLAYBACK_SPEED_1X;
        this.mTransportControlFlags = PLAYSTATE_NONE;
        this.mMetadata = new Bundle();
        this.mCurrentClientGenId = RCSE_ID_UNREGISTERED;
        this.mNeedsPositionSync = DEBUG;
        this.mSessionPlaybackState = null;
        this.mTransportListener = new Callback() {
            public void onSeekTo(long pos) {
                RemoteControlClient.this.onSeekTo(RemoteControlClient.this.mCurrentClientGenId, pos);
            }

            public void onSetRating(Rating rating) {
                if ((RemoteControlClient.this.mTransportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_RATING) != 0) {
                    RemoteControlClient.this.onUpdateMetadata(RemoteControlClient.this.mCurrentClientGenId, MediaMetadataEditor.RATING_KEY_BY_USER, rating);
                }
            }
        };
        this.mRcMediaIntent = mediaButtonIntent;
    }

    public void registerWithSession(MediaSessionLegacyHelper helper) {
        helper.addRccListener(this.mRcMediaIntent, this.mTransportListener);
        this.mSession = helper.getSession(this.mRcMediaIntent);
        setTransportControlFlags(this.mTransportControlFlags);
    }

    public void unregisterWithSession(MediaSessionLegacyHelper helper) {
        helper.removeRccListener(this.mRcMediaIntent);
        this.mSession = null;
    }

    public MediaSession getMediaSession() {
        return this.mSession;
    }

    public MetadataEditor editMetadata(boolean startEmpty) {
        MetadataEditor editor = new MetadataEditor();
        if (startEmpty) {
            editor.mEditorMetadata = new Bundle();
            editor.mEditorArtwork = null;
            editor.mMetadataChanged = true;
            editor.mArtworkChanged = true;
            editor.mEditableKeys = 0;
        } else {
            editor.mEditorMetadata = new Bundle(this.mMetadata);
            editor.mEditorArtwork = this.mOriginalArtwork;
            editor.mMetadataChanged = DEBUG;
            editor.mArtworkChanged = DEBUG;
        }
        if (startEmpty || this.mMediaMetadata == null) {
            editor.mMetadataBuilder = new Builder();
        } else {
            editor.mMetadataBuilder = new Builder(this.mMediaMetadata);
        }
        return editor;
    }

    public void setPlaybackState(int state) {
        setPlaybackStateInt(state, PLAYBACK_POSITION_ALWAYS_UNKNOWN, PLAYBACK_SPEED_1X, DEBUG);
    }

    public void setPlaybackState(int state, long timeInMs, float playbackSpeed) {
        setPlaybackStateInt(state, timeInMs, playbackSpeed, true);
    }

    private void setPlaybackStateInt(int state, long timeInMs, float playbackSpeed, boolean hasPosition) {
        synchronized (this.mCacheLock) {
            if (this.mPlaybackState == state && this.mPlaybackPositionMs == timeInMs) {
                if (this.mPlaybackSpeed != playbackSpeed) {
                }
            }
            this.mPlaybackState = state;
            if (!hasPosition) {
                this.mPlaybackPositionMs = PLAYBACK_POSITION_ALWAYS_UNKNOWN;
            } else if (timeInMs < 0) {
                this.mPlaybackPositionMs = PLAYBACK_POSITION_INVALID;
            } else {
                this.mPlaybackPositionMs = timeInMs;
            }
            this.mPlaybackSpeed = playbackSpeed;
            this.mPlaybackStateChangeTimeMs = SystemClock.elapsedRealtime();
            if (this.mSession != null) {
                long position;
                int pbState = PlaybackState.getStateFromRccState(state);
                if (hasPosition) {
                    position = this.mPlaybackPositionMs;
                } else {
                    position = PLAYBACK_POSITION_INVALID;
                }
                PlaybackState.Builder bob = new PlaybackState.Builder(this.mSessionPlaybackState);
                bob.setState(pbState, position, playbackSpeed, SystemClock.elapsedRealtime());
                bob.setErrorMessage(null);
                this.mSessionPlaybackState = bob.build();
                this.mSession.setPlaybackState(this.mSessionPlaybackState);
            }
        }
    }

    public void setTransportControlFlags(int transportControlFlags) {
        synchronized (this.mCacheLock) {
            this.mTransportControlFlags = transportControlFlags;
            if (this.mSession != null) {
                PlaybackState.Builder bob = new PlaybackState.Builder(this.mSessionPlaybackState);
                bob.setActions(PlaybackState.getActionsFromRccControlFlags(transportControlFlags));
                this.mSessionPlaybackState = bob.build();
                this.mSession.setPlaybackState(this.mSessionPlaybackState);
            }
        }
    }

    public void setMetadataUpdateListener(OnMetadataUpdateListener l) {
        synchronized (this.mCacheLock) {
            this.mMetadataUpdateListener = l;
        }
    }

    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener l) {
        synchronized (this.mCacheLock) {
            this.mPositionUpdateListener = l;
        }
    }

    public void setOnGetPlaybackPositionListener(OnGetPlaybackPositionListener l) {
        synchronized (this.mCacheLock) {
            this.mPositionProvider = l;
        }
    }

    public PendingIntent getRcMediaIntent() {
        return this.mRcMediaIntent;
    }

    private void onSeekTo(int generationId, long timeMs) {
        synchronized (this.mCacheLock) {
            if (this.mCurrentClientGenId == generationId && this.mPositionUpdateListener != null) {
                this.mPositionUpdateListener.onPlaybackPositionUpdate(timeMs);
            }
        }
    }

    private void onUpdateMetadata(int generationId, int key, Object value) {
        synchronized (this.mCacheLock) {
            if (this.mCurrentClientGenId == generationId && this.mMetadataUpdateListener != null) {
                this.mMetadataUpdateListener.onMetadataUpdate(key, value);
            }
        }
    }

    static boolean playbackPositionShouldMove(int playstate) {
        switch (playstate) {
            case PLAYSTATE_STOPPED /*1*/:
            case PLAYSTATE_PAUSED /*2*/:
            case PLAYSTATE_SKIPPING_FORWARDS /*6*/:
            case PLAYSTATE_SKIPPING_BACKWARDS /*7*/:
            case PLAYSTATE_BUFFERING /*8*/:
            case PLAYSTATE_ERROR /*9*/:
                return DEBUG;
            default:
                return true;
        }
    }

    private static long getCheckPeriodFromSpeed(float speed) {
        if (Math.abs(speed) <= PLAYBACK_SPEED_1X) {
            return POSITION_REFRESH_PERIOD_PLAYING_MS;
        }
        return Math.max((long) (15000.0f / Math.abs(speed)), POSITION_REFRESH_PERIOD_MIN_MS);
    }
}
