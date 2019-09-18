package com.android.server.media;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionController;
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
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.LocalServices;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MediaSessionRecord implements IBinder.DeathRecipient {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int OPTIMISTIC_VOLUME_TIMEOUT = 1000;
    private static final String PACKAGE_NAME_HUAWEIMUSIC = "com.android.mediacenter";
    private static final String TAG = "MediaSessionRecord";
    /* access modifiers changed from: private */
    public AudioAttributes mAudioAttrs;
    /* access modifiers changed from: private */
    public AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public AudioManagerInternal mAudioManagerInternal;
    private final Runnable mClearOptimisticVolumeRunnable = new Runnable() {
        public void run() {
            boolean needUpdate = MediaSessionRecord.this.mOptimisticVolume != MediaSessionRecord.this.mCurrentVolume;
            int unused = MediaSessionRecord.this.mOptimisticVolume = -1;
            if (needUpdate) {
                MediaSessionRecord.this.pushVolumeUpdate();
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final ControllerStub mController;
    /* access modifiers changed from: private */
    public final ArrayList<ISessionControllerCallbackHolder> mControllerCallbackHolders = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mCurrentVolume = 0;
    /* access modifiers changed from: private */
    public boolean mDestroyed = false;
    /* access modifiers changed from: private */
    public Bundle mExtras;
    /* access modifiers changed from: private */
    public long mFlags;
    /* access modifiers changed from: private */
    public final MessageHandler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsActive = false;
    /* access modifiers changed from: private */
    public PendingIntent mLaunchIntent;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mMaxVolume = 0;
    /* access modifiers changed from: private */
    public PendingIntent mMediaButtonReceiver;
    /* access modifiers changed from: private */
    public MediaMetadata mMetadata;
    /* access modifiers changed from: private */
    public int mOptimisticVolume = -1;
    final int mOwnerPid;
    final int mOwnerUid;
    final String mPackageName;
    /* access modifiers changed from: private */
    public PlaybackState mPlaybackState;
    /* access modifiers changed from: private */
    public ParceledListSlice mQueue;
    /* access modifiers changed from: private */
    public CharSequence mQueueTitle;
    /* access modifiers changed from: private */
    public int mRatingType;
    /* access modifiers changed from: private */
    public final MediaSessionService mService;
    private final SessionStub mSession;
    /* access modifiers changed from: private */
    public final SessionCb mSessionCb;
    /* access modifiers changed from: private */
    public final String mTag;
    private final int mUserId;
    /* access modifiers changed from: private */
    public int mVolumeControlType = 2;
    /* access modifiers changed from: private */
    public int mVolumeType = 1;

    class ControllerStub extends ISessionController.Stub {
        ControllerStub() {
        }

        public void sendCommand(String packageName, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) {
            MediaSessionRecord.this.mSessionCb.sendCommand(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, command, args, cb);
        }

        public boolean sendMediaButton(String packageName, ISessionControllerCallback cb, boolean asSystemService, KeyEvent keyEvent) {
            return MediaSessionRecord.this.mSessionCb.sendMediaButton(packageName, Binder.getCallingPid(), Binder.getCallingUid(), cb, asSystemService, keyEvent);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
            return;
         */
        public void registerCallbackListener(String packageName, ISessionControllerCallback cb) {
            synchronized (MediaSessionRecord.this.mLock) {
                if (MediaSessionRecord.this.mDestroyed) {
                    try {
                        cb.onSessionDestroyed();
                    } catch (Exception e) {
                    }
                } else if (MediaSessionRecord.this.getControllerHolderIndexForCb(cb) < 0) {
                    MediaSessionRecord.this.mControllerCallbackHolders.add(new ISessionControllerCallbackHolder(cb, packageName, Binder.getCallingUid()));
                    if (MediaSessionRecord.DEBUG) {
                        Log.d(MediaSessionRecord.TAG, "registering controller callback " + cb + " from controller" + packageName);
                    }
                }
            }
        }

        public void unregisterCallbackListener(ISessionControllerCallback cb) {
            synchronized (MediaSessionRecord.this.mLock) {
                int index = MediaSessionRecord.this.getControllerHolderIndexForCb(cb);
                if (index != -1) {
                    MediaSessionRecord.this.mControllerCallbackHolders.remove(index);
                }
                if (MediaSessionRecord.DEBUG) {
                    Log.d(MediaSessionRecord.TAG, "unregistering callback " + cb.asBinder());
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
                ParcelableVolumeInfo parcelableVolumeInfo2 = new ParcelableVolumeInfo(volumeType, attributes, 2, MediaSessionRecord.this.mAudioManager.getStreamMaxVolume(stream), MediaSessionRecord.this.mAudioManager.getStreamVolume(stream));
                return parcelableVolumeInfo2;
            }
        }

        public void adjustVolume(String packageName, ISessionControllerCallback caller, boolean asSystemService, int direction, int flags) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.adjustVolume(packageName, pid, uid, caller, asSystemService, direction, flags, false);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVolumeTo(String packageName, ISessionControllerCallback caller, int value, int flags) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.setVolumeTo(packageName, pid, uid, caller, value, flags);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void prepare(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.prepare(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void prepareFromMediaId(String packageName, ISessionControllerCallback caller, String mediaId, Bundle extras) {
            MediaSessionRecord.this.mSessionCb.prepareFromMediaId(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, mediaId, extras);
        }

        public void prepareFromSearch(String packageName, ISessionControllerCallback caller, String query, Bundle extras) {
            MediaSessionRecord.this.mSessionCb.prepareFromSearch(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, query, extras);
        }

        public void prepareFromUri(String packageName, ISessionControllerCallback caller, Uri uri, Bundle extras) {
            MediaSessionRecord.this.mSessionCb.prepareFromUri(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, uri, extras);
        }

        public void play(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.play(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void playFromMediaId(String packageName, ISessionControllerCallback caller, String mediaId, Bundle extras) {
            MediaSessionRecord.this.mSessionCb.playFromMediaId(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, mediaId, extras);
        }

        public void playFromSearch(String packageName, ISessionControllerCallback caller, String query, Bundle extras) {
            MediaSessionRecord.this.mSessionCb.playFromSearch(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, query, extras);
        }

        public void playFromUri(String packageName, ISessionControllerCallback caller, Uri uri, Bundle extras) {
            MediaSessionRecord.this.mSessionCb.playFromUri(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, uri, extras);
        }

        public void skipToQueueItem(String packageName, ISessionControllerCallback caller, long id) {
            MediaSessionRecord.this.mSessionCb.skipToTrack(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, id);
        }

        public void pause(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.pause(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void stop(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.stop(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void next(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.next(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void previous(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.previous(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void fastForward(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.fastForward(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void rewind(String packageName, ISessionControllerCallback caller) {
            MediaSessionRecord.this.mSessionCb.rewind(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller);
        }

        public void seekTo(String packageName, ISessionControllerCallback caller, long pos) {
            MediaSessionRecord.this.mSessionCb.seekTo(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, pos);
        }

        public void rate(String packageName, ISessionControllerCallback caller, Rating rating) {
            MediaSessionRecord.this.mSessionCb.rate(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, rating);
        }

        public void sendCustomAction(String packageName, ISessionControllerCallback caller, String action, Bundle args) {
            MediaSessionRecord.this.mSessionCb.sendCustomAction(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, action, args);
        }

        public MediaMetadata getMetadata() {
            MediaMetadata access$1600;
            synchronized (MediaSessionRecord.this.mLock) {
                access$1600 = MediaSessionRecord.this.mMetadata;
            }
            return access$1600;
        }

        public PlaybackState getPlaybackState() {
            return MediaSessionRecord.this.getStateWithUpdatedPosition();
        }

        public ParceledListSlice getQueue() {
            ParceledListSlice access$1800;
            synchronized (MediaSessionRecord.this.mLock) {
                access$1800 = MediaSessionRecord.this.mQueue;
            }
            return access$1800;
        }

        public CharSequence getQueueTitle() {
            return MediaSessionRecord.this.mQueueTitle;
        }

        public Bundle getExtras() {
            Bundle access$2000;
            synchronized (MediaSessionRecord.this.mLock) {
                access$2000 = MediaSessionRecord.this.mExtras;
            }
            return access$2000;
        }

        public int getRatingType() {
            return MediaSessionRecord.this.mRatingType;
        }

        public boolean isTransportControlEnabled() {
            return MediaSessionRecord.this.isTransportControlEnabled();
        }
    }

    private class ISessionControllerCallbackHolder {
        /* access modifiers changed from: private */
        public final ISessionControllerCallback mCallback;
        /* access modifiers changed from: private */
        public final String mPackageName;
        private final int mUid;

        ISessionControllerCallbackHolder(ISessionControllerCallback callback, String packageName, int uid) {
            this.mCallback = callback;
            this.mPackageName = packageName;
            this.mUid = uid;
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
        /* access modifiers changed from: private */
        public final ISessionCallback mCb;

        public SessionCb(ISessionCallback cb) {
            this.mCb = cb;
        }

        public boolean sendMediaButton(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, int sequenceId, ResultReceiver cb) {
            if (asSystemService) {
                try {
                    this.mCb.onMediaButton(MediaSessionRecord.this.mContext.getPackageName(), Process.myPid(), 1000, createMediaButtonIntent(keyEvent), sequenceId, cb);
                } catch (RemoteException e) {
                    Slog.e(MediaSessionRecord.TAG, "Remote failure in sendMediaRequest.", e);
                    return false;
                }
            } else {
                this.mCb.onMediaButton(packageName, pid, uid, createMediaButtonIntent(keyEvent), sequenceId, cb);
            }
            return true;
        }

        public boolean sendMediaButton(String packageName, int pid, int uid, ISessionControllerCallback caller, boolean asSystemService, KeyEvent keyEvent) {
            if (asSystemService) {
                try {
                    this.mCb.onMediaButton(MediaSessionRecord.this.mContext.getPackageName(), Process.myPid(), 1000, createMediaButtonIntent(keyEvent), 0, null);
                } catch (RemoteException e) {
                    Slog.e(MediaSessionRecord.TAG, "Remote failure in sendMediaRequest.", e);
                    return false;
                }
            } else {
                this.mCb.onMediaButtonFromController(packageName, pid, uid, caller, createMediaButtonIntent(keyEvent));
            }
            return true;
        }

        public void sendCommand(String packageName, int pid, int uid, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) {
            try {
                this.mCb.onCommand(packageName, pid, uid, caller, command, args, cb);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in sendCommand.", e);
            }
        }

        public void sendCustomAction(String packageName, int pid, int uid, ISessionControllerCallback caller, String action, Bundle args) {
            try {
                this.mCb.onCustomAction(packageName, pid, uid, caller, action, args);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in sendCustomAction.", e);
            }
        }

        public void prepare(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onPrepare(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepare.", e);
            }
        }

        public void prepareFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) {
            try {
                this.mCb.onPrepareFromMediaId(packageName, pid, uid, caller, mediaId, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepareFromMediaId.", e);
            }
        }

        public void prepareFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) {
            try {
                this.mCb.onPrepareFromSearch(packageName, pid, uid, caller, query, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepareFromSearch.", e);
            }
        }

        public void prepareFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) {
            try {
                this.mCb.onPrepareFromUri(packageName, pid, uid, caller, uri, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in prepareFromUri.", e);
            }
        }

        public void play(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onPlay(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in play.", e);
            }
        }

        public void playFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) {
            try {
                this.mCb.onPlayFromMediaId(packageName, pid, uid, caller, mediaId, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in playFromMediaId.", e);
            }
        }

        public void playFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) {
            try {
                this.mCb.onPlayFromSearch(packageName, pid, uid, caller, query, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in playFromSearch.", e);
            }
        }

        public void playFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) {
            try {
                this.mCb.onPlayFromUri(packageName, pid, uid, caller, uri, extras);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in playFromUri.", e);
            }
        }

        public void skipToTrack(String packageName, int pid, int uid, ISessionControllerCallback caller, long id) {
            try {
                this.mCb.onSkipToTrack(packageName, pid, uid, caller, id);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in skipToTrack", e);
            }
        }

        public void pause(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onPause(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in pause.", e);
            }
        }

        public void stop(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onStop(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in stop.", e);
            }
        }

        public void next(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onNext(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in next.", e);
            }
        }

        public void previous(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onPrevious(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in previous.", e);
            }
        }

        public void fastForward(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onFastForward(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in fastForward.", e);
            }
        }

        public void rewind(String packageName, int pid, int uid, ISessionControllerCallback caller) {
            try {
                this.mCb.onRewind(packageName, pid, uid, caller);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in rewind.", e);
            }
        }

        public void seekTo(String packageName, int pid, int uid, ISessionControllerCallback caller, long pos) {
            try {
                this.mCb.onSeekTo(packageName, pid, uid, caller, pos);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in seekTo.", e);
            }
        }

        public void rate(String packageName, int pid, int uid, ISessionControllerCallback caller, Rating rating) {
            try {
                this.mCb.onRate(packageName, pid, uid, caller, rating);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in rate.", e);
            }
        }

        public void adjustVolume(String packageName, int pid, int uid, ISessionControllerCallback caller, boolean asSystemService, int direction) {
            if (asSystemService) {
                try {
                    this.mCb.onAdjustVolume(MediaSessionRecord.this.mContext.getPackageName(), Process.myPid(), 1000, null, direction);
                } catch (RemoteException e) {
                    Slog.e(MediaSessionRecord.TAG, "Remote failure in adjustVolume.", e);
                }
            } else {
                this.mCb.onAdjustVolume(packageName, pid, uid, caller, direction);
            }
        }

        public void setVolumeTo(String packageName, int pid, int uid, ISessionControllerCallback caller, int value) {
            try {
                this.mCb.onSetVolumeTo(packageName, pid, uid, caller, value);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in setVolumeTo.", e);
            }
        }

        private Intent createMediaButtonIntent(KeyEvent keyEvent) {
            Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
            mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
            return mediaButtonIntent;
        }
    }

    private final class SessionStub extends ISession.Stub {
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
            MediaSessionRecord.this.mHandler.post(6, event, data == null ? null : new Bundle(data));
        }

        public ISessionController getController() {
            return MediaSessionRecord.this.mController;
        }

        /* JADX INFO: finally extract failed */
        public void setActive(boolean active) {
            boolean unused = MediaSessionRecord.this.mIsActive = active;
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.updateSession(MediaSessionRecord.this);
                Binder.restoreCallingIdentity(token);
                MediaSessionRecord.this.mHandler.post(7);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setFlags(int flags) {
            if ((flags & 65536) != 0) {
                MediaSessionRecord.this.mService.enforcePhoneStatePermission(getCallingPid(), getCallingUid());
                long unused = MediaSessionRecord.this.mFlags = (long) flags;
                MediaSessionRecord.this.mService.updateSession(MediaSessionRecord.this);
            }
            long unused2 = MediaSessionRecord.this.mFlags = (long) flags;
            if ((65536 & flags) != 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaSessionRecord.this.mService.setGlobalPrioritySession(MediaSessionRecord.this);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            MediaSessionRecord.this.mHandler.post(7);
        }

        public void setMediaButtonReceiver(PendingIntent pi) {
            PendingIntent unused = MediaSessionRecord.this.mMediaButtonReceiver = pi;
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.onMediaButtonReceiverChanged(MediaSessionRecord.this);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setLaunchPendingIntent(PendingIntent pi) {
            PendingIntent unused = MediaSessionRecord.this.mLaunchIntent = pi;
        }

        public void setMetadata(MediaMetadata metadata) {
            MediaMetadata temp;
            synchronized (MediaSessionRecord.this.mLock) {
                if (metadata == null) {
                    temp = null;
                } else {
                    temp = new MediaMetadata.Builder(metadata).build();
                }
                if (temp != null) {
                    temp.size();
                }
                MediaMetadata unused = MediaSessionRecord.this.mMetadata = temp;
            }
            MediaSessionRecord.this.mHandler.post(1);
        }

        /* JADX INFO: finally extract failed */
        public void setPlaybackState(PlaybackState state) {
            int newState = 0;
            int oldState = MediaSessionRecord.this.mPlaybackState == null ? 0 : MediaSessionRecord.this.mPlaybackState.getState();
            if (state != null) {
                newState = state.getState();
            }
            synchronized (MediaSessionRecord.this.mLock) {
                PlaybackState unused = MediaSessionRecord.this.mPlaybackState = state;
            }
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.onSessionPlaystateChanged(MediaSessionRecord.this, oldState, newState);
                Binder.restoreCallingIdentity(token);
                MediaSessionRecord.this.mHandler.post(2);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setQueue(ParceledListSlice queue) {
            synchronized (MediaSessionRecord.this.mLock) {
                ParceledListSlice unused = MediaSessionRecord.this.mQueue = queue;
            }
            MediaSessionRecord.this.mHandler.post(3);
        }

        public void setQueueTitle(CharSequence title) {
            CharSequence unused = MediaSessionRecord.this.mQueueTitle = title;
            MediaSessionRecord.this.mHandler.post(4);
        }

        public void setExtras(Bundle extras) {
            synchronized (MediaSessionRecord.this.mLock) {
                Bundle unused = MediaSessionRecord.this.mExtras = extras == null ? null : new Bundle(extras);
            }
            MediaSessionRecord.this.mHandler.post(5);
        }

        public void setRatingType(int type) {
            int unused = MediaSessionRecord.this.mRatingType = type;
        }

        public void setCurrentVolume(int volume) {
            int unused = MediaSessionRecord.this.mCurrentVolume = volume;
            MediaSessionRecord.this.mHandler.post(8);
        }

        /* JADX INFO: finally extract failed */
        public void setPlaybackToLocal(AudioAttributes attributes) {
            boolean typeChanged;
            synchronized (MediaSessionRecord.this.mLock) {
                typeChanged = MediaSessionRecord.this.mVolumeType == 2;
                int unused = MediaSessionRecord.this.mVolumeType = 1;
                if (attributes != null) {
                    AudioAttributes unused2 = MediaSessionRecord.this.mAudioAttrs = attributes;
                } else {
                    Log.e(MediaSessionRecord.TAG, "Received null audio attributes, using existing attributes");
                }
            }
            if (typeChanged) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaSessionRecord.this.mService.onSessionPlaybackTypeChanged(MediaSessionRecord.this);
                    Binder.restoreCallingIdentity(token);
                    MediaSessionRecord.this.mHandler.post(8);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
        }

        /* JADX INFO: finally extract failed */
        public void setPlaybackToRemote(int control, int max) {
            boolean typeChanged;
            synchronized (MediaSessionRecord.this.mLock) {
                boolean z = true;
                if (MediaSessionRecord.this.mVolumeType != 1) {
                    z = false;
                }
                typeChanged = z;
                int unused = MediaSessionRecord.this.mVolumeType = 2;
                int unused2 = MediaSessionRecord.this.mVolumeControlType = control;
                int unused3 = MediaSessionRecord.this.mMaxVolume = max;
            }
            if (typeChanged) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaSessionRecord.this.mService.onSessionPlaybackTypeChanged(MediaSessionRecord.this);
                    Binder.restoreCallingIdentity(token);
                    MediaSessionRecord.this.mHandler.post(8);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
        }
    }

    public MediaSessionRecord(int ownerPid, int ownerUid, int userId, String ownerPackageName, ISessionCallback cb, String tag, MediaSessionService service, Looper handlerLooper) {
        this.mOwnerPid = ownerPid;
        this.mOwnerUid = ownerUid;
        this.mUserId = userId;
        this.mPackageName = ownerPackageName;
        this.mTag = tag;
        this.mController = new ControllerStub();
        this.mSession = new SessionStub();
        this.mSessionCb = new SessionCb(cb);
        this.mService = service;
        this.mContext = this.mService.getContext();
        this.mHandler = new MessageHandler(handlerLooper);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
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

    public void adjustVolume(String packageName, int pid, int uid, ISessionControllerCallback caller, boolean asSystemService, int direction, int flags, boolean useSuggested) {
        int flags2;
        int i = direction;
        int previousFlagPlaySound = flags & 4;
        if (isPlaybackActive() || hasFlag(65536)) {
            flags2 = flags & -5;
        } else {
            flags2 = flags;
        }
        if (i == -1 || i == 1 || i == 0 || i == -100 || i == 100 || i == 101) {
            if (this.mVolumeType == 1) {
                int stream = AudioAttributes.toLegacyStreamType(this.mAudioAttrs);
                postAdjustLocalVolume(stream, stream, i, flags2, packageName, uid, useSuggested, previousFlagPlaySound);
            } else if (this.mVolumeControlType != 0) {
                if (i == 101 || i == -100 || i == 100) {
                    Log.w(TAG, "Muting remote playback is not supported");
                    return;
                }
                this.mSessionCb.adjustVolume(packageName, pid, uid, caller, asSystemService, i);
                int volumeBefore = this.mOptimisticVolume < 0 ? this.mCurrentVolume : this.mOptimisticVolume;
                this.mOptimisticVolume = volumeBefore + i;
                this.mOptimisticVolume = Math.max(0, Math.min(this.mOptimisticVolume, this.mMaxVolume));
                this.mHandler.removeCallbacks(this.mClearOptimisticVolumeRunnable);
                this.mHandler.postDelayed(this.mClearOptimisticVolumeRunnable, 1000);
                if (volumeBefore != this.mOptimisticVolume) {
                    pushVolumeUpdate();
                }
                this.mService.notifyRemoteVolumeChanged(flags2, this);
                if (DEBUG) {
                    Log.d(TAG, "Adjusted optimistic volume to " + this.mOptimisticVolume + " max is " + this.mMaxVolume);
                }
            } else {
                return;
            }
            return;
        }
        Log.e(TAG, "adjustVolume param is not supported, direction is " + i);
    }

    /* access modifiers changed from: private */
    public void setVolumeTo(String packageName, int pid, int uid, ISessionControllerCallback caller, int value, int flags) {
        if (this.mVolumeType == 1) {
            this.mAudioManagerInternal.setStreamVolumeForUid(AudioAttributes.toLegacyStreamType(this.mAudioAttrs), value, flags, packageName, uid);
        } else if (this.mVolumeControlType == 2) {
            int value2 = Math.max(0, Math.min(value, this.mMaxVolume));
            this.mSessionCb.setVolumeTo(packageName, pid, uid, caller, value2);
            int volumeBefore = this.mOptimisticVolume < 0 ? this.mCurrentVolume : this.mOptimisticVolume;
            this.mOptimisticVolume = Math.max(0, Math.min(value2, this.mMaxVolume));
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
        return this.mIsActive && !this.mDestroyed;
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
            if (!this.mDestroyed) {
                this.mDestroyed = true;
                this.mHandler.post(9);
            }
        }
    }

    public ISessionCallback getCallback() {
        return this.mSessionCb.mCb;
    }

    public void sendMediaButton(String packageName, int pid, int uid, boolean asSystemService, KeyEvent ke, int sequenceId, ResultReceiver cb) {
        this.mSessionCb.sendMediaButton(packageName, pid, uid, asSystemService, ke, sequenceId, cb);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + this.mTag + " " + this);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("  ");
        String indent = sb.toString();
        pw.println(indent + "ownerPid=" + this.mOwnerPid + ", ownerUid=" + this.mOwnerUid + ", userId=" + this.mUserId);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(indent);
        sb2.append("package=");
        sb2.append(this.mPackageName);
        pw.println(sb2.toString());
        pw.println(indent + "launchIntent=" + this.mLaunchIntent);
        pw.println(indent + "mediaButtonReceiver=" + this.mMediaButtonReceiver);
        pw.println(indent + "active=" + this.mIsActive);
        pw.println(indent + "flags=" + this.mFlags);
        pw.println(indent + "rating type=" + this.mRatingType);
        pw.println(indent + "controllers: " + this.mControllerCallbackHolders.size());
        StringBuilder sb3 = new StringBuilder();
        sb3.append(indent);
        sb3.append("state=");
        sb3.append(this.mPlaybackState == null ? null : this.mPlaybackState.toString());
        pw.println(sb3.toString());
        pw.println(indent + "audioAttrs=" + this.mAudioAttrs);
        pw.println(indent + "volumeType=" + this.mVolumeType + ", controlType=" + this.mVolumeControlType + ", max=" + this.mMaxVolume + ", current=" + this.mCurrentVolume);
        StringBuilder sb4 = new StringBuilder();
        sb4.append(indent);
        sb4.append("metadata:");
        sb4.append(getShortMetadataString());
        pw.println(sb4.toString());
        StringBuilder sb5 = new StringBuilder();
        sb5.append(indent);
        sb5.append("queueTitle=");
        sb5.append(this.mQueueTitle);
        sb5.append(", size=");
        sb5.append(this.mQueue == null ? 0 : this.mQueue.getList().size());
        pw.println(sb5.toString());
    }

    public String toString() {
        return this.mPackageName + SliceClientPermissions.SliceAuthority.DELIMITER + this.mTag + " (userId=" + this.mUserId + ")";
    }

    private void postAdjustLocalVolume(int suggestedStream, int stream, int direction, int flags, String packageName, int uid, boolean useSuggested, int previousFlagPlaySound) {
        MessageHandler messageHandler = this.mHandler;
        final boolean z = useSuggested;
        final int i = stream;
        final int i2 = direction;
        final int i3 = flags;
        final String str = packageName;
        final int i4 = uid;
        final int i5 = suggestedStream;
        final int i6 = previousFlagPlaySound;
        AnonymousClass1 r0 = new Runnable() {
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
                    Log.e(MediaSessionRecord.TAG, "Cannot adjust volume: direction=" + i2 + ", stream=" + i + ", flags=" + i3 + ", packageName=" + str + ", uid=" + i4 + ", useSuggested=" + z + ", previousFlagPlaySound=" + i6 + ", suggestedStream=" + i5, e);
                }
            }
        };
        messageHandler.post(r0);
    }

    private String getShortMetadataString() {
        MediaDescription description;
        int fields = this.mMetadata == null ? 0 : this.mMetadata.size();
        if (this.mMetadata == null) {
            description = null;
        } else {
            description = this.mMetadata.getDescription();
        }
        return "size=" + fields + ", description=" + description;
    }

    private void logCallbackException(String msg, ISessionControllerCallbackHolder holder, Exception e) {
        Log.v(TAG, msg + ", this=" + this + ", callback package=" + holder.mPackageName + ", exception=" + e);
    }

    /* access modifiers changed from: private */
    public void pushPlaybackStateUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
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
    }

    /* access modifiers changed from: private */
    public void pushMetadataUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
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
    }

    /* access modifiers changed from: private */
    public void pushQueueUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
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
    }

    /* access modifiers changed from: private */
    public void pushQueueTitleUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
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
    }

    /* access modifiers changed from: private */
    public void pushExtrasUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
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
    }

    /* access modifiers changed from: private */
    public void pushVolumeUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                ParcelableVolumeInfo info = this.mController.getVolumeAttributes();
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onVolumeInfoChanged(info);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushVolumeUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("Unexpected exception in pushVolumeUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void pushEvent(String event, Bundle data) {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onEvent(event, data);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushEvent", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushEvent", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void pushSessionDestroyed() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
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
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0092  */
    public PlaybackState getStateWithUpdatedPosition() {
        PlaybackState state;
        PlaybackState state2;
        PlaybackState state3;
        long position;
        long position2;
        long duration = -1;
        synchronized (this.mLock) {
            state = this.mPlaybackState;
            if (this.mMetadata != null && this.mMetadata.containsKey("android.media.metadata.DURATION")) {
                duration = this.mMetadata.getLong("android.media.metadata.DURATION");
            }
        }
        if (state != null) {
            if (state.getState() == 3 || state.getState() == 4 || state.getState() == 5) {
                long updateTime = state.getLastPositionUpdateTime();
                long currentTime = SystemClock.elapsedRealtime();
                if (updateTime > 0) {
                    long position3 = ((long) (state.getPlaybackSpeed() * ((float) (currentTime - updateTime)))) + state.getPosition();
                    if (duration >= 0 && position3 > duration) {
                        position2 = duration;
                    } else if (position3 < 0) {
                        position2 = 0;
                    } else {
                        position = position3;
                        PlaybackState.Builder builder = new PlaybackState.Builder(state);
                        state2 = state;
                        builder.setState(state.getState(), position, state.getPlaybackSpeed(), currentTime);
                        state3 = builder.build();
                        return state3 != null ? state2 : state3;
                    }
                    position = position2;
                    PlaybackState.Builder builder2 = new PlaybackState.Builder(state);
                    state2 = state;
                    builder2.setState(state.getState(), position, state.getPlaybackSpeed(), currentTime);
                    state3 = builder2.build();
                    if (state3 != null) {
                    }
                }
            } else {
                state2 = state;
                state3 = null;
                if (state3 != null) {
                }
            }
        }
        state2 = state;
        state3 = null;
        if (state3 != null) {
        }
    }

    /* access modifiers changed from: private */
    public int getControllerHolderIndexForCb(ISessionControllerCallback cb) {
        IBinder binder = cb.asBinder();
        for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
            if (binder.equals(this.mControllerCallbackHolders.get(i).mCallback.asBinder())) {
                return i;
            }
        }
        return -1;
    }
}
