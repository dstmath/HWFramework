package com.android.server.media;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.MediaMetadata;
import android.media.MediaMetadata.Builder;
import android.media.Rating;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionController;
import android.media.session.ISessionController.Stub;
import android.media.session.ISessionControllerCallback;
import android.media.session.MediaSession;
import android.media.session.ParcelableVolumeInfo;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.LocalServices;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MediaSessionRecord implements DeathRecipient {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int OPTIMISTIC_VOLUME_TIMEOUT = 1000;
    private static final String PACKAGE_NAME_HUAWEIMUSIC = "com.android.mediacenter";
    private static final String TAG = "MediaSessionRecord";
    private static final int UID_NOT_SET = -1;
    private AudioAttributes mAudioAttrs;
    private AudioManager mAudioManager;
    private AudioManagerInternal mAudioManagerInternal;
    private String mCallingPackage;
    private int mCallingUid = -1;
    private final Runnable mClearOptimisticVolumeRunnable = new Runnable() {
        public void run() {
            boolean needUpdate = MediaSessionRecord.this.mOptimisticVolume != MediaSessionRecord.this.mCurrentVolume;
            MediaSessionRecord.this.mOptimisticVolume = -1;
            if (needUpdate) {
                MediaSessionRecord.this.pushVolumeUpdate();
            }
        }
    };
    private final ControllerStub mController;
    private final ArrayList<ISessionControllerCallbackHolder> mControllerCallbackHolders = new ArrayList();
    private int mCurrentVolume = 0;
    private boolean mDestroyed = false;
    private Bundle mExtras;
    private long mFlags;
    private final MessageHandler mHandler;
    private boolean mIsActive = false;
    private PendingIntent mLaunchIntent;
    private final Object mLock = new Object();
    private int mMaxVolume = 0;
    private PendingIntent mMediaButtonReceiver;
    private MediaMetadata mMetadata;
    private int mOptimisticVolume = -1;
    final int mOwnerPid;
    final int mOwnerUid;
    final String mPackageName;
    private PlaybackState mPlaybackState;
    private ParceledListSlice mQueue;
    private CharSequence mQueueTitle;
    private int mRatingType;
    private final MediaSessionService mService;
    private final SessionStub mSession;
    private final SessionCb mSessionCb;
    private final String mTag;
    private final int mUserId;
    private int mVolumeControlType = 2;
    private int mVolumeType = 1;

    class ControllerStub extends Stub {
        ControllerStub() {
        }

        public void sendCommand(String command, Bundle args, ResultReceiver cb) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.sendCommand(command, args, cb);
        }

        public boolean sendMediaButton(KeyEvent mediaButtonIntent) {
            MediaSessionRecord.this.updateCallingPackage();
            return MediaSessionRecord.this.mSessionCb.sendMediaButton(mediaButtonIntent, 0, null);
        }

        /* JADX WARNING: Missing block: B:17:0x0053, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void registerCallbackListener(ISessionControllerCallback cb) {
            synchronized (MediaSessionRecord.this.mLock) {
                if (MediaSessionRecord.this.mDestroyed) {
                    try {
                        cb.onSessionDestroyed();
                    } catch (Exception e) {
                    }
                } else if (MediaSessionRecord.this.getControllerHolderIndexForCb(cb) < 0) {
                    MediaSessionRecord.this.mControllerCallbackHolders.add(new ISessionControllerCallbackHolder(cb, Binder.getCallingUid()));
                    if (MediaSessionRecord.DEBUG) {
                        Log.d(MediaSessionRecord.TAG, "registering controller callback " + cb);
                    }
                }
            }
        }

        public void unregisterCallbackListener(ISessionControllerCallback cb) throws RemoteException {
            synchronized (MediaSessionRecord.this.mLock) {
                int index = MediaSessionRecord.this.getControllerHolderIndexForCb(cb);
                if (index != -1) {
                    MediaSessionRecord.this.mControllerCallbackHolders.remove(index);
                }
                if (MediaSessionRecord.DEBUG) {
                    Log.d(MediaSessionRecord.TAG, "unregistering callback " + cb + ". index=" + index);
                }
            }
        }

        public String getPackageName() {
            return MediaSessionRecord.this.mPackageName;
        }

        public String getTag() {
            return MediaSessionRecord.this.mTag;
        }

        public PendingIntent getLaunchPendingIntent() {
            return MediaSessionRecord.this.mLaunchIntent;
        }

        public long getFlags() {
            return MediaSessionRecord.this.mFlags;
        }

        public ParcelableVolumeInfo getVolumeAttributes() {
            synchronized (MediaSessionRecord.this.mLock) {
                if (MediaSessionRecord.this.mVolumeType == 2) {
                    ParcelableVolumeInfo parcelableVolumeInfo = new ParcelableVolumeInfo(MediaSessionRecord.this.mVolumeType, MediaSessionRecord.this.mAudioAttrs, MediaSessionRecord.this.mVolumeControlType, MediaSessionRecord.this.mMaxVolume, MediaSessionRecord.this.mOptimisticVolume != -1 ? MediaSessionRecord.this.mOptimisticVolume : MediaSessionRecord.this.mCurrentVolume);
                    return parcelableVolumeInfo;
                }
                int volumeType = MediaSessionRecord.this.mVolumeType;
                AudioAttributes attributes = MediaSessionRecord.this.mAudioAttrs;
                int stream = AudioAttributes.toLegacyStreamType(attributes);
                return new ParcelableVolumeInfo(volumeType, attributes, 2, MediaSessionRecord.this.mAudioManager.getStreamMaxVolume(stream), MediaSessionRecord.this.mAudioManager.getStreamVolume(stream));
            }
        }

        public void adjustVolume(int direction, int flags, String packageName) {
            MediaSessionRecord.this.updateCallingPackage();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.adjustVolume(Integer.MIN_VALUE, direction, flags, packageName, uid, false);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVolumeTo(int value, int flags, String packageName) {
            MediaSessionRecord.this.updateCallingPackage();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.setVolumeTo(value, flags, packageName, uid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void prepare() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.prepare();
        }

        public void prepareFromMediaId(String mediaId, Bundle extras) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.prepareFromMediaId(mediaId, extras);
        }

        public void prepareFromSearch(String query, Bundle extras) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.prepareFromSearch(query, extras);
        }

        public void prepareFromUri(Uri uri, Bundle extras) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.prepareFromUri(uri, extras);
        }

        public void play() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.play();
        }

        public void playFromMediaId(String mediaId, Bundle extras) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.playFromMediaId(mediaId, extras);
        }

        public void playFromSearch(String query, Bundle extras) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.playFromSearch(query, extras);
        }

        public void playFromUri(Uri uri, Bundle extras) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.playFromUri(uri, extras);
        }

        public void skipToQueueItem(long id) {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.skipToTrack(id);
        }

        public void pause() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.pause();
        }

        public void stop() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.stop();
        }

        public void next() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.next();
        }

        public void previous() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.previous();
        }

        public void fastForward() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.fastForward();
        }

        public void rewind() throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.rewind();
        }

        public void seekTo(long pos) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.seekTo(pos);
        }

        public void rate(Rating rating) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.rate(rating);
        }

        public void sendCustomAction(String action, Bundle args) throws RemoteException {
            MediaSessionRecord.this.updateCallingPackage();
            MediaSessionRecord.this.mSessionCb.sendCustomAction(action, args);
        }

        public MediaMetadata getMetadata() {
            MediaMetadata -get15;
            synchronized (MediaSessionRecord.this.mLock) {
                -get15 = MediaSessionRecord.this.mMetadata;
            }
            return -get15;
        }

        public PlaybackState getPlaybackState() {
            return MediaSessionRecord.this.getStateWithUpdatedPosition();
        }

        public ParceledListSlice getQueue() {
            ParceledListSlice -get18;
            synchronized (MediaSessionRecord.this.mLock) {
                -get18 = MediaSessionRecord.this.mQueue;
            }
            return -get18;
        }

        public CharSequence getQueueTitle() {
            return MediaSessionRecord.this.mQueueTitle;
        }

        public Bundle getExtras() {
            Bundle -get9;
            synchronized (MediaSessionRecord.this.mLock) {
                -get9 = MediaSessionRecord.this.mExtras;
            }
            return -get9;
        }

        public int getRatingType() {
            return MediaSessionRecord.this.mRatingType;
        }

        public boolean isTransportControlEnabled() {
            return MediaSessionRecord.this.isTransportControlEnabled();
        }
    }

    private class ISessionControllerCallbackHolder {
        private final ISessionControllerCallback mCallback;
        private final String mPackageName;

        ISessionControllerCallbackHolder(ISessionControllerCallback callback, int uid) {
            this.mCallback = callback;
            this.mPackageName = MediaSessionRecord.this.getPackageName(uid);
        }
    }

    private class MessageHandler extends Handler {
        private static final int MSG_DESTROYED = 9;
        private static final int MSG_SEND_EVENT = 6;
        private static final int MSG_UPDATE_EXTRAS = 5;
        private static final int MSG_UPDATE_METADATA = 1;
        private static final int MSG_UPDATE_PLAYBACK_STATE = 2;
        private static final int MSG_UPDATE_QUEUE = 3;
        private static final int MSG_UPDATE_QUEUE_TITLE = 4;
        private static final int MSG_UPDATE_SESSION_STATE = 7;
        private static final int MSG_UPDATE_VOLUME = 8;

        public MessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MediaSessionRecord.this.pushMetadataUpdate();
                    return;
                case 2:
                    MediaSessionRecord.this.pushPlaybackStateUpdate();
                    return;
                case 3:
                    MediaSessionRecord.this.pushQueueUpdate();
                    return;
                case 4:
                    MediaSessionRecord.this.pushQueueTitleUpdate();
                    return;
                case 5:
                    MediaSessionRecord.this.pushExtrasUpdate();
                    return;
                case 6:
                    MediaSessionRecord.this.pushEvent((String) msg.obj, msg.getData());
                    return;
                case 8:
                    MediaSessionRecord.this.pushVolumeUpdate();
                    return;
                case 9:
                    MediaSessionRecord.this.pushSessionDestroyed();
                    return;
                default:
                    return;
            }
        }

        public void post(int what) {
            post(what, null);
        }

        public void post(int what, Object obj) {
            obtainMessage(what, obj).sendToTarget();
        }

        public void post(int what, Object obj, Bundle data) {
            Message msg = obtainMessage(what, obj);
            msg.setData(data);
            msg.sendToTarget();
        }
    }

    class SessionCb {
        private final ISessionCallback mCb;

        public SessionCb(ISessionCallback cb) {
            this.mCb = cb;
        }

        public boolean sendMediaButton(KeyEvent keyEvent, int sequenceId, ResultReceiver cb) {
            Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
            mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
            try {
                this.mCb.onMediaButton(mediaButtonIntent, sequenceId, cb);
                return true;
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in sendMediaRequest.", e);
                return false;
            }
        }

        public void sendCommand(String command, Bundle args, ResultReceiver cb) {
            try {
                this.mCb.onCommand(command, args, cb);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in sendCommand.", e);
            }
        }

        public void sendCustomAction(String action, Bundle args) {
            try {
                this.mCb.onCustomAction(action, args);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in sendCustomAction.", e);
            }
        }

        public void prepare() {
            try {
                this.mCb.onPrepare();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepare.", e);
            }
        }

        public void prepareFromMediaId(String mediaId, Bundle extras) {
            try {
                this.mCb.onPrepareFromMediaId(mediaId, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepareFromMediaId.", e);
            }
        }

        public void prepareFromSearch(String query, Bundle extras) {
            try {
                this.mCb.onPrepareFromSearch(query, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepareFromSearch.", e);
            }
        }

        public void prepareFromUri(Uri uri, Bundle extras) {
            try {
                this.mCb.onPrepareFromUri(uri, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepareFromUri.", e);
            }
        }

        public void play() {
            try {
                this.mCb.onPlay();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in play.", e);
            }
        }

        public void playFromMediaId(String mediaId, Bundle extras) {
            try {
                this.mCb.onPlayFromMediaId(mediaId, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in playFromMediaId.", e);
            }
        }

        public void playFromSearch(String query, Bundle extras) {
            try {
                this.mCb.onPlayFromSearch(query, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in playFromSearch.", e);
            }
        }

        public void playFromUri(Uri uri, Bundle extras) {
            try {
                this.mCb.onPlayFromUri(uri, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in playFromUri.", e);
            }
        }

        public void skipToTrack(long id) {
            try {
                this.mCb.onSkipToTrack(id);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in skipToTrack", e);
            }
        }

        public void pause() {
            try {
                this.mCb.onPause();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in pause.", e);
            }
        }

        public void stop() {
            try {
                this.mCb.onStop();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in stop.", e);
            }
        }

        public void next() {
            try {
                this.mCb.onNext();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in next.", e);
            }
        }

        public void previous() {
            try {
                this.mCb.onPrevious();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in previous.", e);
            }
        }

        public void fastForward() {
            try {
                this.mCb.onFastForward();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in fastForward.", e);
            }
        }

        public void rewind() {
            try {
                this.mCb.onRewind();
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in rewind.", e);
            }
        }

        public void seekTo(long pos) {
            try {
                this.mCb.onSeekTo(pos);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in seekTo.", e);
            }
        }

        public void rate(Rating rating) {
            try {
                this.mCb.onRate(rating);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in rate.", e);
            }
        }

        public void adjustVolume(int direction) {
            try {
                this.mCb.onAdjustVolume(direction);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in adjustVolume.", e);
            }
        }

        public void setVolumeTo(int value) {
            try {
                this.mCb.onSetVolumeTo(value);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in setVolumeTo.", e);
            }
        }
    }

    private final class SessionStub extends ISession.Stub {
        /* synthetic */ SessionStub(MediaSessionRecord this$0, SessionStub -this1) {
            this();
        }

        private SessionStub() {
        }

        public void destroy() {
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.destroySession(MediaSessionRecord.this);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void sendEvent(String event, Bundle data) {
            Bundle bundle = null;
            MessageHandler -get11 = MediaSessionRecord.this.mHandler;
            if (data != null) {
                bundle = new Bundle(data);
            }
            -get11.post(6, event, bundle);
        }

        public ISessionController getController() {
            return MediaSessionRecord.this.mController;
        }

        public void setActive(boolean active) {
            MediaSessionRecord.this.mIsActive = active;
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.updateSession(MediaSessionRecord.this);
                MediaSessionRecord.this.mHandler.post(7);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setFlags(int flags) {
            if ((65536 & flags) != 0) {
                MediaSessionRecord.this.mService.enforcePhoneStatePermission(getCallingPid(), getCallingUid());
                MediaSessionRecord.this.mFlags = (long) flags;
                MediaSessionRecord.this.mService.updateSession(MediaSessionRecord.this);
            }
            MediaSessionRecord.this.mFlags = (long) flags;
            MediaSessionRecord.this.mHandler.post(7);
        }

        public void setMediaButtonReceiver(PendingIntent pi) {
            MediaSessionRecord.this.mMediaButtonReceiver = pi;
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.onMediaButtonReceiverChanged(MediaSessionRecord.this);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setLaunchPendingIntent(PendingIntent pi) {
            MediaSessionRecord.this.mLaunchIntent = pi;
        }

        public void setMetadata(MediaMetadata metadata) {
            synchronized (MediaSessionRecord.this.mLock) {
                MediaMetadata temp = metadata == null ? null : new Builder(metadata).build();
                if (temp != null) {
                    temp.size();
                }
                MediaSessionRecord.this.mMetadata = temp;
            }
            MediaSessionRecord.this.mHandler.post(1);
        }

        public void setPlaybackState(PlaybackState state) {
            int oldState = MediaSessionRecord.this.mPlaybackState == null ? 0 : MediaSessionRecord.this.mPlaybackState.getState();
            int newState = state == null ? 0 : state.getState();
            synchronized (MediaSessionRecord.this.mLock) {
                MediaSessionRecord.this.mPlaybackState = state;
            }
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.onSessionPlaystateChanged(MediaSessionRecord.this, oldState, newState);
                MediaSessionRecord.this.mHandler.post(2);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setQueue(ParceledListSlice queue) {
            synchronized (MediaSessionRecord.this.mLock) {
                MediaSessionRecord.this.mQueue = queue;
            }
            MediaSessionRecord.this.mHandler.post(3);
        }

        public void setQueueTitle(CharSequence title) {
            MediaSessionRecord.this.mQueueTitle = title;
            MediaSessionRecord.this.mHandler.post(4);
        }

        public void setExtras(Bundle extras) {
            Bundle bundle = null;
            synchronized (MediaSessionRecord.this.mLock) {
                MediaSessionRecord mediaSessionRecord = MediaSessionRecord.this;
                if (extras != null) {
                    bundle = new Bundle(extras);
                }
                mediaSessionRecord.mExtras = bundle;
            }
            MediaSessionRecord.this.mHandler.post(5);
        }

        public void setRatingType(int type) {
            MediaSessionRecord.this.mRatingType = type;
        }

        public void setCurrentVolume(int volume) {
            MediaSessionRecord.this.mCurrentVolume = volume;
            MediaSessionRecord.this.mHandler.post(8);
        }

        public void setPlaybackToLocal(AudioAttributes attributes) {
            boolean typeChanged;
            synchronized (MediaSessionRecord.this.mLock) {
                typeChanged = MediaSessionRecord.this.mVolumeType == 2;
                MediaSessionRecord.this.mVolumeType = 1;
                if (attributes != null) {
                    MediaSessionRecord.this.mAudioAttrs = attributes;
                } else {
                    Log.e(MediaSessionRecord.TAG, "Received null audio attributes, using existing attributes");
                }
            }
            if (typeChanged) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaSessionRecord.this.mService.onSessionPlaybackTypeChanged(MediaSessionRecord.this);
                    MediaSessionRecord.this.mHandler.post(8);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        public void setPlaybackToRemote(int control, int max) {
            boolean typeChanged;
            synchronized (MediaSessionRecord.this.mLock) {
                typeChanged = MediaSessionRecord.this.mVolumeType == 1;
                MediaSessionRecord.this.mVolumeType = 2;
                MediaSessionRecord.this.mVolumeControlType = control;
                MediaSessionRecord.this.mMaxVolume = max;
            }
            if (typeChanged) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaSessionRecord.this.mService.onSessionPlaybackTypeChanged(MediaSessionRecord.this);
                    MediaSessionRecord.this.mHandler.post(8);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        public String getCallingPackage() {
            return MediaSessionRecord.this.mCallingPackage;
        }
    }

    public MediaSessionRecord(int ownerPid, int ownerUid, int userId, String ownerPackageName, ISessionCallback cb, String tag, MediaSessionService service, Looper handlerLooper) {
        this.mOwnerPid = ownerPid;
        this.mOwnerUid = ownerUid;
        this.mUserId = userId;
        this.mPackageName = ownerPackageName;
        this.mTag = tag;
        this.mController = new ControllerStub();
        this.mSession = new SessionStub(this, null);
        this.mSessionCb = new SessionCb(cb);
        this.mService = service;
        this.mHandler = new MessageHandler(handlerLooper);
        this.mAudioManager = (AudioManager) service.getContext().getSystemService("audio");
        this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        this.mAudioAttrs = new AudioAttributes.Builder().setUsage(1).build();
    }

    public ISession getSessionBinder() {
        return this.mSession;
    }

    public ISessionController getControllerBinder() {
        return this.mController;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getTag() {
        return this.mTag;
    }

    public PendingIntent getMediaButtonReceiver() {
        return this.mMediaButtonReceiver;
    }

    public long getFlags() {
        return this.mFlags;
    }

    public boolean hasFlag(int flag) {
        return (this.mFlags & ((long) flag)) != 0;
    }

    public int getUid() {
        return this.mOwnerUid;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public boolean isSystemPriority() {
        return (this.mFlags & 65536) != 0;
    }

    public void adjustVolume(int suggestedStream, int direction, int flags, String packageName, int uid, boolean useSuggested) {
        int previousFlagPlaySound = flags & 4;
        if (isPlaybackActive() || hasFlag(65536)) {
            flags &= -5;
        }
        if (direction == -1 || direction == 1 || direction == 0 || direction == -100 || direction == 100 || direction == 101) {
            if (this.mVolumeType == 1) {
                postAdjustLocalVolume(suggestedStream, AudioAttributes.toLegacyStreamType(this.mAudioAttrs), direction, flags, packageName, uid, useSuggested, previousFlagPlaySound);
            } else if (this.mVolumeControlType != 0) {
                if (direction == 101 || direction == -100 || direction == 100) {
                    Log.w(TAG, "Muting remote playback is not supported");
                    return;
                }
                this.mSessionCb.adjustVolume(direction);
                int volumeBefore = this.mOptimisticVolume < 0 ? this.mCurrentVolume : this.mOptimisticVolume;
                this.mOptimisticVolume = volumeBefore + direction;
                this.mOptimisticVolume = Math.max(0, Math.min(this.mOptimisticVolume, this.mMaxVolume));
                this.mHandler.removeCallbacks(this.mClearOptimisticVolumeRunnable);
                this.mHandler.postDelayed(this.mClearOptimisticVolumeRunnable, 1000);
                if (volumeBefore != this.mOptimisticVolume) {
                    pushVolumeUpdate();
                }
                this.mService.notifyRemoteVolumeChanged(flags, this);
                if (DEBUG) {
                    Log.d(TAG, "Adjusted optimistic volume to " + this.mOptimisticVolume + " max is " + this.mMaxVolume);
                }
            } else {
                return;
            }
            return;
        }
        Log.e(TAG, "adjustVolume param is not supported, direction is " + direction);
    }

    public void setVolumeTo(int value, int flags, String packageName, int uid) {
        if (this.mVolumeType == 1) {
            this.mAudioManagerInternal.setStreamVolumeForUid(AudioAttributes.toLegacyStreamType(this.mAudioAttrs), value, flags, packageName, uid);
        } else if (this.mVolumeControlType == 2) {
            value = Math.max(0, Math.min(value, this.mMaxVolume));
            this.mSessionCb.setVolumeTo(value);
            int volumeBefore = this.mOptimisticVolume < 0 ? this.mCurrentVolume : this.mOptimisticVolume;
            this.mOptimisticVolume = Math.max(0, Math.min(value, this.mMaxVolume));
            this.mHandler.removeCallbacks(this.mClearOptimisticVolumeRunnable);
            this.mHandler.postDelayed(this.mClearOptimisticVolumeRunnable, 1000);
            if (volumeBefore != this.mOptimisticVolume) {
                pushVolumeUpdate();
            }
            this.mService.notifyRemoteVolumeChanged(flags, this);
            if (DEBUG) {
                Log.d(TAG, "Set optimistic volume to " + this.mOptimisticVolume + " max is " + this.mMaxVolume);
            }
        }
    }

    public boolean isActive() {
        return this.mIsActive ? this.mDestroyed ^ 1 : false;
    }

    public PlaybackState getPlaybackState() {
        return this.mPlaybackState;
    }

    public boolean isPlaybackActive() {
        return MediaSession.isActiveState(this.mPlaybackState == null ? 0 : this.mPlaybackState.getState());
    }

    public int getPlaybackType() {
        return this.mVolumeType;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAudioAttrs;
    }

    public int getVolumeControl() {
        return this.mVolumeControlType;
    }

    public int getMaxVolume() {
        return this.mMaxVolume;
    }

    public int getCurrentVolume() {
        return this.mCurrentVolume;
    }

    public int getOptimisticVolume() {
        return this.mOptimisticVolume;
    }

    public boolean isTransportControlEnabled() {
        return hasFlag(2);
    }

    public void binderDied() {
        this.mService.sessionDied(this);
    }

    public void onDestroy() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            this.mDestroyed = true;
            this.mHandler.post(9);
        }
    }

    public ISessionCallback getCallback() {
        return this.mSessionCb.mCb;
    }

    public void sendMediaButton(KeyEvent ke, int sequenceId, ResultReceiver cb, int uid, String packageName) {
        updateCallingPackage(uid, packageName);
        this.mSessionCb.sendMediaButton(ke, sequenceId, cb);
    }

    public void dump(PrintWriter pw, String prefix) {
        String str = null;
        pw.println(prefix + this.mTag + " " + this);
        String indent = prefix + "  ";
        pw.println(indent + "ownerPid=" + this.mOwnerPid + ", ownerUid=" + this.mOwnerUid + ", userId=" + this.mUserId);
        pw.println(indent + "package=" + this.mPackageName);
        pw.println(indent + "launchIntent=" + this.mLaunchIntent);
        pw.println(indent + "mediaButtonReceiver=" + this.mMediaButtonReceiver);
        pw.println(indent + "active=" + this.mIsActive);
        pw.println(indent + "flags=" + this.mFlags);
        pw.println(indent + "rating type=" + this.mRatingType);
        pw.println(indent + "controllers: " + this.mControllerCallbackHolders.size());
        StringBuilder append = new StringBuilder().append(indent).append("state=");
        if (this.mPlaybackState != null) {
            str = this.mPlaybackState.toString();
        }
        pw.println(append.append(str).toString());
        pw.println(indent + "audioAttrs=" + this.mAudioAttrs);
        pw.println(indent + "volumeType=" + this.mVolumeType + ", controlType=" + this.mVolumeControlType + ", max=" + this.mMaxVolume + ", current=" + this.mCurrentVolume);
        pw.println(indent + "metadata:" + getShortMetadataString());
        pw.println(indent + "queueTitle=" + this.mQueueTitle + ", size=" + (this.mQueue == null ? 0 : this.mQueue.getList().size()));
    }

    public String toString() {
        return this.mPackageName + "/" + this.mTag + " (userId=" + this.mUserId + ")";
    }

    private void postAdjustLocalVolume(int suggestedStream, int stream, int direction, int flags, String packageName, int uid, boolean useSuggested, int previousFlagPlaySound) {
        final boolean z = useSuggested;
        final int i = stream;
        final int i2 = direction;
        final int i3 = flags;
        final String str = packageName;
        final int i4 = uid;
        final int i5 = suggestedStream;
        final int i6 = previousFlagPlaySound;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    if (!z) {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustStreamVolumeForUid(i, i2, i3, str, i4);
                    } else if (AudioSystem.isStreamActive(i, 0)) {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustSuggestedStreamVolumeForUid(i, i2, i3, str, i4);
                    } else if (MediaSessionRecord.this.mPackageName == null || !MediaSessionRecord.this.mPackageName.equals(MediaSessionRecord.PACKAGE_NAME_HUAWEIMUSIC)) {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustSuggestedStreamVolumeForUid(Integer.MIN_VALUE, i2, i6 | i3, str, i4);
                    } else {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustSuggestedStreamVolumeForUid(i5, i2, i3, str, i4);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(MediaSessionRecord.TAG, "IllegalArgument when postAdjustLocalVolume, suggestedStream=" + i5 + ", stream=" + i + ", packageName=" + str);
                }
            }
        });
    }

    private String getShortMetadataString() {
        return "size=" + (this.mMetadata == null ? 0 : this.mMetadata.size()) + ", description=" + (this.mMetadata == null ? null : this.mMetadata.getDescription());
    }

    private void logCallbackException(String msg, ISessionControllerCallbackHolder holder, Exception e) {
        Log.v(TAG, msg + ", this=" + this + ", callback package=" + holder.mPackageName + ", exception=" + e);
    }

    private void pushPlaybackStateUpdate() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onPlaybackStateChanged(this.mPlaybackState);
                } catch (DeadObjectException e) {
                    this.mControllerCallbackHolders.remove(i);
                    logCallbackException("Removed dead callback in pushPlaybackStateUpdate", holder, e);
                } catch (RemoteException e2) {
                    logCallbackException("unexpected exception in pushPlaybackStateUpdate", holder, e2);
                }
            }
        }
    }

    private void pushMetadataUpdate() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onMetadataChanged(this.mMetadata);
                } catch (DeadObjectException e) {
                    logCallbackException("Removing dead callback in pushMetadataUpdate", holder, e);
                    this.mControllerCallbackHolders.remove(i);
                } catch (RemoteException e2) {
                    logCallbackException("unexpected exception in pushMetadataUpdate", holder, e2);
                }
            }
        }
    }

    private void pushQueueUpdate() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onQueueChanged(this.mQueue);
                } catch (DeadObjectException e) {
                    this.mControllerCallbackHolders.remove(i);
                    logCallbackException("Removed dead callback in pushQueueUpdate", holder, e);
                } catch (RemoteException e2) {
                    logCallbackException("unexpected exception in pushQueueUpdate", holder, e2);
                }
            }
        }
    }

    private void pushQueueTitleUpdate() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onQueueTitleChanged(this.mQueueTitle);
                } catch (DeadObjectException e) {
                    this.mControllerCallbackHolders.remove(i);
                    logCallbackException("Removed dead callback in pushQueueTitleUpdate", holder, e);
                } catch (RemoteException e2) {
                    logCallbackException("unexpected exception in pushQueueTitleUpdate", holder, e2);
                }
            }
        }
    }

    private void pushExtrasUpdate() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onExtrasChanged(this.mExtras);
                } catch (DeadObjectException e) {
                    this.mControllerCallbackHolders.remove(i);
                    logCallbackException("Removed dead callback in pushExtrasUpdate", holder, e);
                } catch (RemoteException e2) {
                    logCallbackException("unexpected exception in pushExtrasUpdate", holder, e2);
                }
            }
        }
    }

    private void pushVolumeUpdate() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            ParcelableVolumeInfo info = this.mController.getVolumeAttributes();
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onVolumeInfoChanged(info);
                } catch (DeadObjectException e) {
                    logCallbackException("Removing dead callback in pushVolumeUpdate", holder, e);
                } catch (RemoteException e2) {
                    logCallbackException("Unexpected exception in pushVolumeUpdate", holder, e2);
                }
            }
        }
    }

    private void pushEvent(String event, Bundle data) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                return;
            }
            for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                try {
                    holder.mCallback.onEvent(event, data);
                } catch (DeadObjectException e) {
                    logCallbackException("Removing dead callback in pushEvent", holder, e);
                    this.mControllerCallbackHolders.remove(i);
                } catch (RemoteException e2) {
                    logCallbackException("unexpected exception in pushEvent", holder, e2);
                }
            }
        }
    }

    private void pushSessionDestroyed() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = (ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onSessionDestroyed();
                    } catch (DeadObjectException e) {
                        logCallbackException("Removing dead callback in pushEvent", holder, e);
                        this.mControllerCallbackHolders.remove(i);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushEvent", holder, e2);
                    }
                }
                this.mControllerCallbackHolders.clear();
                return;
            }
        }
    }

    private PlaybackState getStateWithUpdatedPosition() {
        PlaybackState state;
        long duration = -1;
        synchronized (this.mLock) {
            state = this.mPlaybackState;
            if (this.mMetadata != null && this.mMetadata.containsKey("android.media.metadata.DURATION")) {
                duration = this.mMetadata.getLong("android.media.metadata.DURATION");
            }
        }
        PlaybackState result = null;
        if (state != null && (state.getState() == 3 || state.getState() == 4 || state.getState() == 5)) {
            long updateTime = state.getLastPositionUpdateTime();
            long currentTime = SystemClock.elapsedRealtime();
            if (updateTime > 0) {
                long position = ((long) (state.getPlaybackSpeed() * ((float) (currentTime - updateTime)))) + state.getPosition();
                if (duration >= 0 && position > duration) {
                    position = duration;
                } else if (position < 0) {
                    position = 0;
                }
                PlaybackState.Builder builder = new PlaybackState.Builder(state);
                builder.setState(state.getState(), position, state.getPlaybackSpeed(), currentTime);
                result = builder.build();
            }
        }
        if (result == null) {
            return state;
        }
        return result;
    }

    private int getControllerHolderIndexForCb(ISessionControllerCallback cb) {
        IBinder binder = cb.asBinder();
        for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
            if (binder.equals(((ISessionControllerCallbackHolder) this.mControllerCallbackHolders.get(i)).mCallback.asBinder())) {
                return i;
            }
        }
        return -1;
    }

    private void updateCallingPackage() {
        updateCallingPackage(-1, null);
    }

    private void updateCallingPackage(int uid, String packageName) {
        if (uid == -1) {
            uid = Binder.getCallingUid();
        }
        synchronized (this.mLock) {
            if (this.mCallingUid == -1 || this.mCallingUid != uid) {
                this.mCallingUid = uid;
                if (packageName == null) {
                    packageName = getPackageName(uid);
                }
                this.mCallingPackage = packageName;
            }
        }
    }

    private String getPackageName(int uid) {
        Context context = this.mService.getContext();
        if (context == null) {
            return null;
        }
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        if (packages == null || packages.length <= 0) {
            return null;
        }
        return packages[0];
    }
}
