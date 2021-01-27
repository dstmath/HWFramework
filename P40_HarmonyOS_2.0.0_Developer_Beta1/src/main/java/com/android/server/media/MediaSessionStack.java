package com.android.server.media;

import android.media.session.MediaSession;
import android.os.Debug;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseArray;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class MediaSessionStack {
    private static final int[] ALWAYS_PRIORITY_STATES = {4, 5, 9, 10};
    private static final boolean DEBUG = true;
    private static final String PRIORITY_PKG = "com.huawei.camera";
    private static final String TAG = "MediaSessionStack";
    private static final int[] TRANSITION_PRIORITY_STATES = {6, 8, 3};
    private final AudioPlayerStateMonitor mAudioPlayerStateMonitor;
    private final SparseArray<ArrayList<MediaSessionRecord>> mCachedActiveLists = new SparseArray<>();
    private MediaSessionRecord mCachedVolumeDefault;
    private MediaSessionRecord mMediaButtonSession;
    private final OnMediaButtonSessionChangedListener mOnMediaButtonSessionChangedListener;
    private final List<MediaSessionRecord> mSessions = new ArrayList();

    /* access modifiers changed from: package-private */
    public interface OnMediaButtonSessionChangedListener {
        void onMediaButtonSessionChanged(MediaSessionRecord mediaSessionRecord, MediaSessionRecord mediaSessionRecord2);
    }

    MediaSessionStack(AudioPlayerStateMonitor monitor, OnMediaButtonSessionChangedListener listener) {
        this.mAudioPlayerStateMonitor = monitor;
        this.mOnMediaButtonSessionChangedListener = listener;
    }

    public void addSession(MediaSessionRecord record) {
        this.mSessions.add(record);
        clearCache(record.getUserId());
        updateMediaButtonSessionIfNeeded();
    }

    public void removeSession(MediaSessionRecord record) {
        this.mSessions.remove(record);
        if (this.mMediaButtonSession == record) {
            updateMediaButtonSession(null);
        }
        clearCache(record.getUserId());
    }

    public boolean contains(MediaSessionRecord record) {
        return this.mSessions.contains(record);
    }

    public MediaSessionRecord getMediaSessionRecord(MediaSession.Token sessionToken) {
        for (MediaSessionRecord record : this.mSessions) {
            if (Objects.equals(record.getControllerBinder(), sessionToken.getBinder())) {
                return record;
            }
        }
        return null;
    }

    public void onPlaystateChanged(MediaSessionRecord record, int oldState, int newState) {
        MediaSessionRecord newMediaButtonSession;
        if (shouldUpdatePriority(oldState, newState)) {
            this.mSessions.remove(record);
            this.mSessions.add(0, record);
            clearCache(record.getUserId());
        } else if (!MediaSession.isActiveState(newState)) {
            this.mCachedVolumeDefault = null;
        }
        MediaSessionRecord mediaSessionRecord = this.mMediaButtonSession;
        if (mediaSessionRecord != null && mediaSessionRecord.getUid() == record.getUid() && (newMediaButtonSession = findMediaButtonSession(this.mMediaButtonSession.getUid())) != this.mMediaButtonSession) {
            updateMediaButtonSession(newMediaButtonSession);
        }
    }

    public void onSessionStateChange(MediaSessionRecord record) {
        if (isSystemSession(record)) {
            if (record.isActive()) {
                updateMediaButtonSession(record);
            } else {
                updateMediaButtonSessionIfNeeded();
            }
        }
        clearCache(record.getUserId());
    }

    public void updateMediaButtonSessionIfNeeded() {
        Log.i(TAG, "updateMediaButtonSessionIfNeeded, callers=" + Debug.getCallers(2));
        MediaSessionRecord mediaButtonSession = getSystemActiveSession();
        if (mediaButtonSession != null) {
            updateMediaButtonSession(mediaButtonSession);
            return;
        }
        IntArray audioPlaybackUids = this.mAudioPlayerStateMonitor.getSortedAudioPlaybackClientUids();
        for (int i = 0; i < audioPlaybackUids.size(); i++) {
            MediaSessionRecord mediaButtonSession2 = findMediaButtonSession(audioPlaybackUids.get(i));
            if (mediaButtonSession2 != null) {
                this.mAudioPlayerStateMonitor.cleanUpAudioPlaybackUids(mediaButtonSession2.getUid());
                if (this.mMediaButtonSession != mediaButtonSession2) {
                    updateMediaButtonSession(mediaButtonSession2);
                    return;
                }
                return;
            }
        }
    }

    private MediaSessionRecord findMediaButtonSession(int uid) {
        MediaSessionRecord mediaButtonSession = null;
        for (MediaSessionRecord session : this.mSessions) {
            if (uid == session.getUid()) {
                if (session.getPlaybackState() != null && session.isPlaybackActive() == this.mAudioPlayerStateMonitor.isPlaybackActive(session.getUid())) {
                    return session;
                }
                if (mediaButtonSession == null) {
                    mediaButtonSession = session;
                }
            }
        }
        return mediaButtonSession;
    }

    public ArrayList<MediaSessionRecord> getActiveSessions(int userId) {
        ArrayList<MediaSessionRecord> cachedActiveList = this.mCachedActiveLists.get(userId);
        if (cachedActiveList != null) {
            return cachedActiveList;
        }
        ArrayList<MediaSessionRecord> cachedActiveList2 = getPriorityList(true, userId);
        this.mCachedActiveLists.put(userId, cachedActiveList2);
        return cachedActiveList2;
    }

    public MediaSessionRecord getMediaButtonSession() {
        MediaSessionRecord mediaSessionRecord = this.mMediaButtonSession;
        if (mediaSessionRecord == null || !isSystemSession(mediaSessionRecord)) {
            return this.mMediaButtonSession;
        }
        if (this.mMediaButtonSession.isActive()) {
            return this.mMediaButtonSession;
        }
        return null;
    }

    private void updateMediaButtonSession(MediaSessionRecord newMediaButtonSession) {
        MediaSessionRecord oldMediaButtonSession = this.mMediaButtonSession;
        this.mMediaButtonSession = newMediaButtonSession;
        this.mOnMediaButtonSessionChangedListener.onMediaButtonSessionChanged(oldMediaButtonSession, newMediaButtonSession);
    }

    public MediaSessionRecord getDefaultVolumeSession() {
        MediaSessionRecord mediaSessionRecord = this.mCachedVolumeDefault;
        if (mediaSessionRecord != null) {
            return mediaSessionRecord;
        }
        ArrayList<MediaSessionRecord> records = getPriorityList(true, -1);
        int size = records.size();
        for (int i = 0; i < size; i++) {
            MediaSessionRecord record = records.get(i);
            if (record.isPlaybackActive()) {
                this.mCachedVolumeDefault = record;
                return record;
            }
        }
        return null;
    }

    public MediaSessionRecord getDefaultRemoteSession(int userId) {
        ArrayList<MediaSessionRecord> records = getPriorityList(true, userId);
        int size = records.size();
        for (int i = 0; i < size; i++) {
            MediaSessionRecord record = records.get(i);
            if (record.getPlaybackType() == 2) {
                return record;
            }
        }
        return null;
    }

    public void dump(PrintWriter pw, String prefix) {
        ArrayList<MediaSessionRecord> sortedSessions = getPriorityList(false, -1);
        int count = sortedSessions.size();
        pw.println(prefix + "Media button session is " + this.mMediaButtonSession);
        pw.println(prefix + "Sessions Stack - have " + count + " sessions:");
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("  ");
        String indent = sb.toString();
        for (int i = 0; i < count; i++) {
            sortedSessions.get(i).dump(pw, indent);
            pw.println();
        }
    }

    /* JADX INFO: Multiple debug info for r6v3 int: [D('lastActiveIndex' int), D('lastPlaybackActiveIndex' int)] */
    public ArrayList<MediaSessionRecord> getPriorityList(boolean activeOnly, int userId) {
        ArrayList<MediaSessionRecord> result = new ArrayList<>();
        int lastPlaybackActiveIndex = 0;
        int lastActiveIndex = 0;
        int size = this.mSessions.size();
        for (int i = 0; i < size; i++) {
            MediaSessionRecord session = this.mSessions.get(i);
            if (userId == -1 || userId == session.getUserId()) {
                if (!session.isActive()) {
                    if (!activeOnly) {
                        result.add(session);
                    }
                } else if (session.isPlaybackActive()) {
                    result.add(lastPlaybackActiveIndex, session);
                    lastActiveIndex++;
                    lastPlaybackActiveIndex++;
                } else {
                    result.add(lastActiveIndex, session);
                    lastActiveIndex++;
                }
            }
        }
        return result;
    }

    private boolean shouldUpdatePriority(int oldState, int newState) {
        if (containsState(newState, ALWAYS_PRIORITY_STATES)) {
            return true;
        }
        if (containsState(oldState, TRANSITION_PRIORITY_STATES) || !containsState(newState, TRANSITION_PRIORITY_STATES)) {
            return false;
        }
        return true;
    }

    private boolean containsState(int state, int[] states) {
        for (int i : states) {
            if (i == state) {
                return true;
            }
        }
        return false;
    }

    private void clearCache(int userId) {
        this.mCachedVolumeDefault = null;
        this.mCachedActiveLists.remove(userId);
        this.mCachedActiveLists.remove(-1);
    }

    private boolean isSystemSession(MediaSessionRecord mediaSession) {
        if (mediaSession == null || mediaSession.mPackageName == null || !mediaSession.mPackageName.equals(PRIORITY_PKG)) {
            return false;
        }
        return true;
    }

    private MediaSessionRecord getSystemActiveSession() {
        for (MediaSessionRecord mediaSession : this.mSessions) {
            if (isSystemSession(mediaSession) && mediaSession.isActive()) {
                return mediaSession;
            }
        }
        return null;
    }
}
