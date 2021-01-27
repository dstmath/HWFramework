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
import android.media.Rating;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionController;
import android.media.session.ISessionControllerCallback;
import android.media.session.MediaController;
import android.media.session.MediaSession;
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
import java.util.List;

public class MediaSessionRecord implements IBinder.DeathRecipient {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int OPTIMISTIC_VOLUME_TIMEOUT = 1000;
    private static final String TAG = "MediaSessionRecord";
    private AudioAttributes mAudioAttrs;
    private AudioManager mAudioManager;
    private AudioManagerInternal mAudioManagerInternal;
    private final Runnable mClearOptimisticVolumeRunnable = new Runnable() {
        /* class com.android.server.media.MediaSessionRecord.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            boolean needUpdate = MediaSessionRecord.this.mOptimisticVolume != MediaSessionRecord.this.mCurrentVolume;
            MediaSessionRecord.this.mOptimisticVolume = -1;
            if (needUpdate) {
                MediaSessionRecord.this.pushVolumeUpdate();
            }
        }
    };
    private final Context mContext;
    private final ControllerStub mController;
    private final ArrayList<ISessionControllerCallbackHolder> mControllerCallbackHolders = new ArrayList<>();
    private int mCurrentVolume = 0;
    private boolean mDestroyed = false;
    private long mDuration = -1;
    private Bundle mExtras;
    private long mFlags;
    private final MessageHandler mHandler;
    private boolean mIsActive = false;
    private PendingIntent mLaunchIntent;
    private final Object mLock = new Object();
    private int mMaxVolume = 0;
    private PendingIntent mMediaButtonReceiver;
    private MediaMetadata mMetadata;
    private String mMetadataDescription;
    private int mOptimisticVolume = -1;
    final int mOwnerPid;
    final int mOwnerUid;
    final String mPackageName;
    private PlaybackState mPlaybackState;
    private List<MediaSession.QueueItem> mQueue;
    private CharSequence mQueueTitle;
    private int mRatingType;
    private final MediaSessionService mService;
    private final SessionStub mSession;
    private final SessionCb mSessionCb;
    private final Bundle mSessionInfo;
    private final MediaSession.Token mSessionToken;
    private final String mTag;
    private final int mUserId;
    private int mVolumeControlType = 2;
    private int mVolumeType = 1;

    public MediaSessionRecord(int ownerPid, int ownerUid, int userId, String ownerPackageName, ISessionCallback cb, String tag, Bundle sessionInfo, MediaSessionService service, Looper handlerLooper) {
        this.mOwnerPid = ownerPid;
        this.mOwnerUid = ownerUid;
        this.mUserId = userId;
        this.mPackageName = ownerPackageName;
        this.mTag = tag;
        this.mSessionInfo = sessionInfo;
        this.mController = new ControllerStub();
        this.mSessionToken = new MediaSession.Token(this.mController);
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

    public MediaSession.Token getSessionToken() {
        return this.mSessionToken;
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

    public void adjustVolume(String packageName, String opPackageName, int pid, int uid, ISessionControllerCallback caller, boolean asSystemService, int direction, int flags, boolean useSuggested) {
        int flags2;
        int previousFlagPlaySound = flags & 4;
        if (isPlaybackActive() || hasFlag(65536)) {
            flags2 = flags & -5;
        } else {
            flags2 = flags;
        }
        if (this.mVolumeType == 1) {
            postAdjustLocalVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttrs), direction, flags2, opPackageName, pid, uid, asSystemService, useSuggested, previousFlagPlaySound);
        } else if (this.mVolumeControlType != 0) {
            if (direction != 101 && direction != -100) {
                if (direction != 100) {
                    if (DEBUG) {
                        Log.w(TAG, "adjusting volume, pkg=" + packageName + ", asSystemService=" + asSystemService + ", dir=" + direction);
                    }
                    this.mSessionCb.adjustVolume(packageName, pid, uid, caller, asSystemService, direction);
                    int volumeBefore = this.mOptimisticVolume;
                    if (volumeBefore < 0) {
                        volumeBefore = this.mCurrentVolume;
                    }
                    this.mOptimisticVolume = volumeBefore + direction;
                    this.mOptimisticVolume = Math.max(0, Math.min(this.mOptimisticVolume, this.mMaxVolume));
                    this.mHandler.removeCallbacks(this.mClearOptimisticVolumeRunnable);
                    this.mHandler.postDelayed(this.mClearOptimisticVolumeRunnable, 1000);
                    if (volumeBefore != this.mOptimisticVolume) {
                        pushVolumeUpdate();
                    }
                    this.mService.notifyRemoteVolumeChanged(flags2, this);
                    if (DEBUG) {
                        Log.i(TAG, "Adjusted optimistic volume to " + this.mOptimisticVolume + " max is " + this.mMaxVolume);
                        return;
                    }
                    return;
                }
            }
            Log.w(TAG, "Muting remote playback is not supported");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVolumeTo(String packageName, final String opPackageName, int pid, final int uid, ISessionControllerCallback caller, final int value, final int flags) {
        if (this.mVolumeType == 1) {
            final int stream = AudioAttributes.toLegacyStreamType(this.mAudioAttrs);
            this.mHandler.post(new Runnable() {
                /* class com.android.server.media.MediaSessionRecord.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        MediaSessionRecord.this.mAudioManagerInternal.setStreamVolumeForUid(stream, value, flags, opPackageName, uid);
                    } catch (IllegalArgumentException | SecurityException e) {
                        Log.e(MediaSessionRecord.TAG, "Cannot set volume: stream=" + stream + ", value=" + value + ", flags=" + flags, e);
                    }
                }
            });
        } else if (this.mVolumeControlType == 2) {
            int value2 = Math.max(0, Math.min(value, this.mMaxVolume));
            this.mSessionCb.setVolumeTo(packageName, pid, uid, caller, value2);
            int volumeBefore = this.mOptimisticVolume;
            if (volumeBefore < 0) {
                volumeBefore = this.mCurrentVolume;
            }
            this.mOptimisticVolume = Math.max(0, Math.min(value2, this.mMaxVolume));
            this.mHandler.removeCallbacks(this.mClearOptimisticVolumeRunnable);
            this.mHandler.postDelayed(this.mClearOptimisticVolumeRunnable, 1000);
            if (volumeBefore != this.mOptimisticVolume) {
                pushVolumeUpdate();
            }
            this.mService.notifyRemoteVolumeChanged(flags, this);
            if (DEBUG) {
                Log.i(TAG, "Set optimistic volume to " + this.mOptimisticVolume + " max is " + this.mMaxVolume);
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
        PlaybackState playbackState = this.mPlaybackState;
        return MediaSession.isActiveState(playbackState == null ? 0 : playbackState.getState());
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

    @Override // android.os.IBinder.DeathRecipient
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

    public boolean sendMediaButton(String packageName, int pid, int uid, boolean asSystemService, KeyEvent ke, int sequenceId, ResultReceiver cb) {
        return this.mSessionCb.sendMediaButton(packageName, pid, uid, asSystemService, ke, sequenceId, cb);
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
        PlaybackState playbackState = this.mPlaybackState;
        sb3.append(playbackState == null ? null : playbackState.toString());
        pw.println(sb3.toString());
        pw.println(indent + "audioAttrs=" + this.mAudioAttrs);
        pw.println(indent + "volumeType=" + this.mVolumeType + ", controlType=" + this.mVolumeControlType + ", max=" + this.mMaxVolume + ", current=" + this.mCurrentVolume);
        StringBuilder sb4 = new StringBuilder();
        sb4.append(indent);
        sb4.append("metadata: ");
        sb4.append(this.mMetadataDescription);
        pw.println(sb4.toString());
        StringBuilder sb5 = new StringBuilder();
        sb5.append(indent);
        sb5.append("queueTitle=");
        sb5.append((Object) this.mQueueTitle);
        sb5.append(", size=");
        List<MediaSession.QueueItem> list = this.mQueue;
        sb5.append(list == null ? 0 : list.size());
        pw.println(sb5.toString());
    }

    @Override // java.lang.Object
    public String toString() {
        return this.mPackageName + SliceClientPermissions.SliceAuthority.DELIMITER + this.mTag + " (userId=" + this.mUserId + ")";
    }

    private void postAdjustLocalVolume(final int stream, final int direction, final int flags, String callingOpPackageName, int callingPid, int callingUid, boolean asSystemService, final boolean useSuggested, final int previousFlagPlaySound) {
        final int uid;
        final String opPackageName;
        if (DEBUG) {
            Log.w(TAG, "adjusting local volume, stream=" + stream + ", dir=" + direction + ", asSystemService=" + asSystemService + ", useSuggested=" + useSuggested);
        }
        if (asSystemService) {
            opPackageName = this.mContext.getOpPackageName();
            uid = 1000;
        } else {
            opPackageName = callingOpPackageName;
            uid = callingUid;
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.media.MediaSessionRecord.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (!useSuggested) {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustStreamVolumeForUid(stream, direction, flags, opPackageName, uid);
                    } else if (AudioSystem.isStreamActive(stream, 0)) {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustSuggestedStreamVolumeForUid(stream, direction, flags, opPackageName, uid);
                    } else {
                        MediaSessionRecord.this.mAudioManagerInternal.adjustSuggestedStreamVolumeForUid(Integer.MIN_VALUE, direction, previousFlagPlaySound | flags, opPackageName, uid);
                    }
                } catch (IllegalArgumentException | SecurityException e) {
                    Log.e(MediaSessionRecord.TAG, "Cannot adjust volume: direction=" + direction + ", stream=" + stream + ", flags=" + flags + ", opPackageName=" + opPackageName + ", uid=" + uid + ", useSuggested=" + useSuggested + ", previousFlagPlaySound=" + previousFlagPlaySound, e);
                }
            }
        });
    }

    private void logCallbackException(String msg, ISessionControllerCallbackHolder holder, Exception e) {
        Log.i(TAG, msg + ", this=" + this + ", callback package=" + holder.mPackageName + ", exception=" + e);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushPlaybackStateUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onPlaybackStateChanged(this.mPlaybackState);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushPlaybackStateUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushPlaybackStateUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushMetadataUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onMetadataChanged(this.mMetadata);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushMetadataUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushMetadataUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushQueueUpdate() {
        ParceledListSlice parceledListSlice;
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        ISessionControllerCallback iSessionControllerCallback = holder.mCallback;
                        if (this.mQueue == null) {
                            parceledListSlice = null;
                        } else {
                            parceledListSlice = new ParceledListSlice(this.mQueue);
                        }
                        iSessionControllerCallback.onQueueChanged(parceledListSlice);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushQueueUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushQueueUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushQueueTitleUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onQueueTitleChanged(this.mQueueTitle);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushQueueTitleUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushQueueTitleUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushExtrasUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onExtrasChanged(this.mExtras);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushExtrasUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushExtrasUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushVolumeUpdate() {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                MediaController.PlaybackInfo info = getVolumeAttributes();
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onVolumeInfoChanged(info);
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushVolumeUpdate", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushVolumeUpdate", holder, e2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushEvent(String event, Bundle data) {
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
    /* access modifiers changed from: public */
    private void pushSessionDestroyed() {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
                    ISessionControllerCallbackHolder holder = this.mControllerCallbackHolders.get(i);
                    try {
                        holder.mCallback.onSessionDestroyed();
                    } catch (DeadObjectException e) {
                        this.mControllerCallbackHolders.remove(i);
                        logCallbackException("Removing dead callback in pushSessionDestroyed", holder, e);
                    } catch (RemoteException e2) {
                        logCallbackException("unexpected exception in pushSessionDestroyed", holder, e2);
                    }
                }
                this.mControllerCallbackHolders.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PlaybackState getStateWithUpdatedPosition() {
        PlaybackState state;
        long duration;
        long position;
        synchronized (this.mLock) {
            state = this.mPlaybackState;
            duration = this.mDuration;
        }
        PlaybackState result = null;
        if (state != null && (state.getState() == 3 || state.getState() == 4 || state.getState() == 5)) {
            long updateTime = state.getLastPositionUpdateTime();
            long currentTime = SystemClock.elapsedRealtime();
            if (updateTime > 0) {
                long position2 = ((long) (state.getPlaybackSpeed() * ((float) (currentTime - updateTime)))) + state.getPosition();
                if (duration >= 0 && position2 > duration) {
                    position = duration;
                } else if (position2 < 0) {
                    position = 0;
                } else {
                    position = position2;
                }
                PlaybackState.Builder builder = new PlaybackState.Builder(state);
                builder.setState(state.getState(), position, state.getPlaybackSpeed(), currentTime);
                result = builder.build();
            }
        }
        return result == null ? state : result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getControllerHolderIndexForCb(ISessionControllerCallback cb) {
        IBinder binder = cb.asBinder();
        for (int i = this.mControllerCallbackHolders.size() - 1; i >= 0; i--) {
            if (binder.equals(this.mControllerCallbackHolders.get(i).mCallback.asBinder())) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MediaController.PlaybackInfo getVolumeAttributes() {
        synchronized (this.mLock) {
            if (this.mVolumeType == 2) {
                return new MediaController.PlaybackInfo(this.mVolumeType, this.mVolumeControlType, this.mMaxVolume, this.mOptimisticVolume != -1 ? this.mOptimisticVolume : this.mCurrentVolume, this.mAudioAttrs);
            }
            int volumeType = this.mVolumeType;
            AudioAttributes attributes = this.mAudioAttrs;
            int stream = AudioAttributes.toLegacyStreamType(attributes);
            return new MediaController.PlaybackInfo(volumeType, 2, this.mAudioManager.getStreamMaxVolume(stream), this.mAudioManager.getStreamVolume(stream), attributes);
        }
    }

    /* access modifiers changed from: private */
    public final class SessionStub extends ISession.Stub {
        private SessionStub() {
        }

        public void destroySession() throws RemoteException {
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.destroySession(MediaSessionRecord.this);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void sendEvent(String event, Bundle data) throws RemoteException {
            MediaSessionRecord.this.mHandler.post(6, event, data == null ? null : new Bundle(data));
        }

        public ISessionController getController() throws RemoteException {
            return MediaSessionRecord.this.mController;
        }

        /* JADX INFO: finally extract failed */
        public void setActive(boolean active) throws RemoteException {
            MediaSessionRecord.this.mIsActive = active;
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

        public void setFlags(int flags) throws RemoteException {
            if ((flags & 65536) != 0) {
                MediaSessionRecord.this.mService.enforcePhoneStatePermission(Binder.getCallingPid(), Binder.getCallingUid());
            }
            MediaSessionRecord.this.mFlags = (long) flags;
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

        public void setMediaButtonReceiver(PendingIntent pi) throws RemoteException {
            MediaSessionRecord.this.mMediaButtonReceiver = pi;
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.mService.onMediaButtonReceiverChanged(MediaSessionRecord.this);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setLaunchPendingIntent(PendingIntent pi) throws RemoteException {
            MediaSessionRecord.this.mLaunchIntent = pi;
        }

        public void setMetadata(MediaMetadata metadata, long duration, String metadataDescription) throws RemoteException {
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
                MediaSessionRecord.this.mMetadata = temp;
                MediaSessionRecord.this.mDuration = duration;
                MediaSessionRecord.this.mMetadataDescription = metadataDescription;
            }
            MediaSessionRecord.this.mHandler.post(1);
        }

        /* JADX INFO: finally extract failed */
        public void setPlaybackState(PlaybackState state) throws RemoteException {
            int newState = 0;
            int oldState = MediaSessionRecord.this.mPlaybackState == null ? 0 : MediaSessionRecord.this.mPlaybackState.getState();
            if (state != null) {
                newState = state.getState();
            }
            synchronized (MediaSessionRecord.this.mLock) {
                MediaSessionRecord.this.mPlaybackState = state;
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

        public void setQueue(ParceledListSlice queue) throws RemoteException {
            synchronized (MediaSessionRecord.this.mLock) {
                MediaSessionRecord.this.mQueue = queue == null ? null : queue.getList();
            }
            MediaSessionRecord.this.mHandler.post(3);
        }

        public void setQueueTitle(CharSequence title) throws RemoteException {
            MediaSessionRecord.this.mQueueTitle = title;
            MediaSessionRecord.this.mHandler.post(4);
        }

        public void setExtras(Bundle extras) throws RemoteException {
            synchronized (MediaSessionRecord.this.mLock) {
                MediaSessionRecord.this.mExtras = extras == null ? null : new Bundle(extras);
            }
            MediaSessionRecord.this.mHandler.post(5);
        }

        public void setRatingType(int type) throws RemoteException {
            MediaSessionRecord.this.mRatingType = type;
        }

        public void setCurrentVolume(int volume) throws RemoteException {
            MediaSessionRecord.this.mCurrentVolume = volume;
            MediaSessionRecord.this.mHandler.post(8);
        }

        /* JADX INFO: finally extract failed */
        public void setPlaybackToLocal(AudioAttributes attributes) throws RemoteException {
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
                    Binder.restoreCallingIdentity(token);
                    MediaSessionRecord.this.mHandler.post(8);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
        }

        /* JADX INFO: finally extract failed */
        public void setPlaybackToRemote(int control, int max) throws RemoteException {
            boolean typeChanged;
            synchronized (MediaSessionRecord.this.mLock) {
                typeChanged = true;
                if (MediaSessionRecord.this.mVolumeType != 1) {
                    typeChanged = false;
                }
                MediaSessionRecord.this.mVolumeType = 2;
                MediaSessionRecord.this.mVolumeControlType = control;
                MediaSessionRecord.this.mMaxVolume = max;
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

    /* access modifiers changed from: package-private */
    public class SessionCb {
        private final ISessionCallback mCb;

        SessionCb(ISessionCallback cb) {
            this.mCb = cb;
        }

        public boolean sendMediaButton(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, int sequenceId, ResultReceiver cb) {
            if (asSystemService) {
                try {
                    this.mCb.onMediaButton(MediaSessionRecord.this.mContext.getPackageName(), Process.myPid(), 1000, createMediaButtonIntent(keyEvent), sequenceId, cb);
                    return true;
                } catch (RemoteException e) {
                    Slog.e(MediaSessionRecord.TAG, "Remote failure in sendMediaRequest.", e);
                    return false;
                }
            } else {
                this.mCb.onMediaButton(packageName, pid, uid, createMediaButtonIntent(keyEvent), sequenceId, cb);
                return true;
            }
        }

        public boolean sendMediaButton(String packageName, int pid, int uid, ISessionControllerCallback caller, boolean asSystemService, KeyEvent keyEvent) {
            if (asSystemService) {
                try {
                    this.mCb.onMediaButton(MediaSessionRecord.this.mContext.getPackageName(), Process.myPid(), 1000, createMediaButtonIntent(keyEvent), 0, (ResultReceiver) null);
                    return true;
                } catch (RemoteException e) {
                    Slog.e(MediaSessionRecord.TAG, "Remote failure in sendMediaRequest.", e);
                    return false;
                }
            } else {
                this.mCb.onMediaButtonFromController(packageName, pid, uid, caller, createMediaButtonIntent(keyEvent));
                return true;
            }
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

        public void setPlaybackSpeed(String packageName, int pid, int uid, ISessionControllerCallback caller, float speed) {
            try {
                this.mCb.onSetPlaybackSpeed(packageName, pid, uid, caller, speed);
            } catch (RemoteException e) {
                Slog.e(MediaSessionRecord.TAG, "Remote failure in setPlaybackSpeed.", e);
            }
        }

        public void adjustVolume(String packageName, int pid, int uid, ISessionControllerCallback caller, boolean asSystemService, int direction) {
            if (asSystemService) {
                try {
                    this.mCb.onAdjustVolume(MediaSessionRecord.this.mContext.getPackageName(), Process.myPid(), 1000, (ISessionControllerCallback) null, direction);
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

    /* access modifiers changed from: package-private */
    public class ControllerStub extends ISessionController.Stub {
        ControllerStub() {
        }

        public void sendCommand(String packageName, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) {
            MediaSessionRecord.this.mSessionCb.sendCommand(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, command, args, cb);
        }

        public boolean sendMediaButton(String packageName, ISessionControllerCallback cb, KeyEvent keyEvent) {
            return MediaSessionRecord.this.mSessionCb.sendMediaButton(packageName, Binder.getCallingPid(), Binder.getCallingUid(), cb, false, keyEvent);
        }

        public void registerCallback(String packageName, ISessionControllerCallback cb) {
            synchronized (MediaSessionRecord.this.mLock) {
                if (MediaSessionRecord.this.mDestroyed) {
                    try {
                        cb.onSessionDestroyed();
                    } catch (Exception e) {
                    }
                    return;
                }
                if (MediaSessionRecord.this.getControllerHolderIndexForCb(cb) < 0) {
                    MediaSessionRecord.this.mControllerCallbackHolders.add(new ISessionControllerCallbackHolder(cb, packageName, Binder.getCallingUid()));
                    if (MediaSessionRecord.DEBUG) {
                        Log.i(MediaSessionRecord.TAG, "registering controller callback " + cb + " from controller" + packageName);
                    }
                }
            }
        }

        public void unregisterCallback(ISessionControllerCallback cb) {
            synchronized (MediaSessionRecord.this.mLock) {
                int index = MediaSessionRecord.this.getControllerHolderIndexForCb(cb);
                if (index != -1) {
                    MediaSessionRecord.this.mControllerCallbackHolders.remove(index);
                }
                if (MediaSessionRecord.DEBUG) {
                    Log.i(MediaSessionRecord.TAG, "unregistering callback " + cb.asBinder());
                }
            }
        }

        public String getPackageName() {
            return MediaSessionRecord.this.mPackageName;
        }

        public String getTag() {
            return MediaSessionRecord.this.mTag;
        }

        public Bundle getSessionInfo() {
            return MediaSessionRecord.this.mSessionInfo;
        }

        public PendingIntent getLaunchPendingIntent() {
            return MediaSessionRecord.this.mLaunchIntent;
        }

        public long getFlags() {
            return MediaSessionRecord.this.mFlags;
        }

        public MediaController.PlaybackInfo getVolumeAttributes() {
            return MediaSessionRecord.this.getVolumeAttributes();
        }

        public void adjustVolume(String packageName, String opPackageName, ISessionControllerCallback caller, int direction, int flags) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.adjustVolume(packageName, opPackageName, pid, uid, caller, false, direction, flags, false);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVolumeTo(String packageName, String opPackageName, ISessionControllerCallback caller, int value, int flags) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionRecord.this.setVolumeTo(packageName, opPackageName, pid, uid, caller, value, flags);
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

        public void setPlaybackSpeed(String packageName, ISessionControllerCallback caller, float speed) {
            MediaSessionRecord.this.mSessionCb.setPlaybackSpeed(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, speed);
        }

        public void sendCustomAction(String packageName, ISessionControllerCallback caller, String action, Bundle args) {
            MediaSessionRecord.this.mSessionCb.sendCustomAction(packageName, Binder.getCallingPid(), Binder.getCallingUid(), caller, action, args);
        }

        public MediaMetadata getMetadata() {
            MediaMetadata mediaMetadata;
            synchronized (MediaSessionRecord.this.mLock) {
                mediaMetadata = MediaSessionRecord.this.mMetadata;
            }
            return mediaMetadata;
        }

        public PlaybackState getPlaybackState() {
            return MediaSessionRecord.this.getStateWithUpdatedPosition();
        }

        public ParceledListSlice getQueue() {
            ParceledListSlice parceledListSlice;
            synchronized (MediaSessionRecord.this.mLock) {
                parceledListSlice = MediaSessionRecord.this.mQueue == null ? null : new ParceledListSlice(MediaSessionRecord.this.mQueue);
            }
            return parceledListSlice;
        }

        public CharSequence getQueueTitle() {
            return MediaSessionRecord.this.mQueueTitle;
        }

        public Bundle getExtras() {
            Bundle bundle;
            synchronized (MediaSessionRecord.this.mLock) {
                bundle = MediaSessionRecord.this.mExtras;
            }
            return bundle;
        }

        public int getRatingType() {
            return MediaSessionRecord.this.mRatingType;
        }
    }

    /* access modifiers changed from: private */
    public class ISessionControllerCallbackHolder {
        private final ISessionControllerCallback mCallback;
        private final String mPackageName;
        private final int mUid;

        ISessionControllerCallbackHolder(ISessionControllerCallback callback, String packageName, int uid) {
            this.mCallback = callback;
            this.mPackageName = packageName;
            this.mUid = uid;
        }
    }

    /* access modifiers changed from: private */
    public class MessageHandler extends Handler {
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

        @Override // android.os.Handler
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
                case 7:
                default:
                    return;
                case 8:
                    MediaSessionRecord.this.pushVolumeUpdate();
                    return;
                case 9:
                    MediaSessionRecord.this.pushSessionDestroyed();
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
}
