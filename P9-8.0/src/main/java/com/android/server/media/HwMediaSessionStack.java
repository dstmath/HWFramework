package com.android.server.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.hsm.MediaTransactWrapper;
import android.media.AudioSystem;
import android.util.Log;
import java.util.List;
import java.util.Set;

public class HwMediaSessionStack extends MediaSessionStack {
    private static final int STREAM_FM = 10;
    private static final String TAG = "HwMediaSessionStack";
    private final Context mContext;

    public HwMediaSessionStack(Context context) {
        super(null, null);
        this.mContext = context;
    }

    public MediaSessionRecord getDefaultMediaButtonSession(int userId, boolean includeNotPlaying) {
        this.mIsSupportMediaKey = true;
        if (this.mGlobalPrioritySession != null && this.mGlobalPrioritySession.isActive()) {
            return this.mGlobalPrioritySession;
        }
        boolean isPid = false;
        int pid = -1;
        Set result = null;
        if (AudioSystem.isStreamActive(3, 0)) {
            result = MediaTransactWrapper.playingMusicUidSet();
            if (result != null && result.isEmpty()) {
                pid = Integer.parseInt(AudioSystem.getParameters("active_music_pid"));
                if (pid != -1) {
                    isPid = true;
                }
            }
        }
        MediaSessionRecord activeSession;
        if (isPid) {
            activeSession = findRecordByPid(pid, userId, 1, true);
            if (activeSession != null) {
                this.mCachedButtonReceiver = activeSession;
                this.mLastInterestingRecord = activeSession;
            } else {
                this.mIsSupportMediaKey = false;
            }
        } else if (result == null || (result.isEmpty() ^ 1) == 0) {
            activeSession = findRecordByTopActivity(userId, 1, true);
            if (activeSession != null) {
                this.mCachedButtonReceiver = activeSession;
            }
        } else {
            activeSession = findRecordByUids(result, userId, 1, true);
            if (activeSession != null) {
                this.mCachedButtonReceiver = activeSession;
                this.mLastInterestingRecord = activeSession;
            } else {
                this.mIsSupportMediaKey = false;
            }
        }
        if (this.mCachedButtonReceiver != null) {
            return this.mCachedButtonReceiver;
        }
        return super.getMediaButtonSession();
    }

    private MediaSessionRecord findRecordByTopActivity(int userId, int withFlags, boolean active) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                Log.e(TAG, " Failure to get topActivity PackageName tasks null");
                return null;
            }
            ComponentName topActivity = ((RunningTaskInfo) tasks.get(0)).topActivity;
            if (topActivity == null || topActivity.getPackageName() == null) {
                Log.e(TAG, " Failure to get topActivity PackageName topActivity null");
                return null;
            }
            Log.d(TAG, "isTOPActivity topActivity.getPackageName()" + topActivity.getPackageName());
            int size = this.mSessions.size();
            for (int i = 0; i < size; i++) {
                MediaSessionRecord session = (MediaSessionRecord) this.mSessions.get(i);
                if (!byPassRecord(session, userId, withFlags, active)) {
                    if (session.isSystemPriority() && session.isActive()) {
                        return session;
                    }
                    if (session.mPackageName.equals(topActivity.getPackageName())) {
                        Log.e(TAG, "mPackageName: " + session.mPackageName);
                        return session;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, " Failure to get topActivity PackageName " + e);
            return null;
        }
    }

    private MediaSessionRecord findRecordByPid(int pid, int userId, int withFlags, boolean active) {
        int size = this.mSessions.size();
        for (int i = 0; i < size; i++) {
            MediaSessionRecord session = (MediaSessionRecord) this.mSessions.get(i);
            if (!byPassRecord(session, userId, withFlags, active) && ((session.isSystemPriority() && session.isActive()) || session.mOwnerPid == pid)) {
                return session;
            }
        }
        return null;
    }

    private MediaSessionRecord findRecordByUids(Set<Integer> uids, int userId, int withFlags, boolean active) {
        int size = this.mSessions.size();
        for (int i = 0; i < size; i++) {
            MediaSessionRecord session = (MediaSessionRecord) this.mSessions.get(i);
            if (!byPassRecord(session, userId, withFlags, active)) {
                if (session.isSystemPriority() && session.isActive()) {
                    return session;
                }
                for (Integer uid : uids) {
                    if (session.mOwnerUid == uid.intValue()) {
                        return session;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private boolean byPassRecord(MediaSessionRecord session, int userId, int withFlags, boolean active) {
        if ((userId == -1 || userId == session.getUserId()) && (session.getFlags() & ((long) withFlags)) == ((long) withFlags) && session.isActive()) {
            return false;
        }
        return true;
    }
}
