package com.android.server.media;

import android.content.Context;
import android.media.AudioPlaybackConfiguration;
import android.media.IAudioService;
import android.media.IPlaybackConfigDispatcher.Stub;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.IntArray;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AudioPlaybackMonitor extends Stub {
    private static boolean DEBUG = MediaSessionService.DEBUG;
    private static String TAG = "AudioPlaybackMonitor";
    private Set<Integer> mActiveAudioPlaybackClientUids = new HashSet();
    private Set<Integer> mActiveAudioPlaybackPlayerInterfaceIds = new HashSet();
    private final Context mContext;
    private final OnAudioPlaybackStartedListener mListener;
    private final Object mLock = new Object();
    private final IntArray mSortedAudioPlaybackClientUids = new IntArray();

    interface OnAudioPlaybackStartedListener {
        void onAudioPlaybackStarted(int i);
    }

    AudioPlaybackMonitor(Context context, IAudioService audioService, OnAudioPlaybackStartedListener listener) {
        this.mContext = context;
        this.mListener = listener;
        try {
            audioService.registerPlaybackCallback(this);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failed to register playback callback", e);
        }
    }

    public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs) {
        long token = Binder.clearCallingIdentity();
        try {
            Set<Integer> newActiveAudioPlaybackPlayerInterfaceIds = new HashSet();
            List<Integer> newActiveAudioPlaybackClientUids = new ArrayList();
            synchronized (this.mLock) {
                this.mActiveAudioPlaybackClientUids.clear();
                for (AudioPlaybackConfiguration config : configs) {
                    if (config.isActive() && config.getPlayerType() != 3) {
                        this.mActiveAudioPlaybackClientUids.add(Integer.valueOf(config.getClientUid()));
                        newActiveAudioPlaybackPlayerInterfaceIds.add(Integer.valueOf(config.getPlayerInterfaceId()));
                        if (!this.mActiveAudioPlaybackPlayerInterfaceIds.contains(Integer.valueOf(config.getPlayerInterfaceId()))) {
                            if (DEBUG) {
                                Log.d(TAG, "Found a new active media playback. " + AudioPlaybackConfiguration.toLogFriendlyString(config));
                            }
                            newActiveAudioPlaybackClientUids.add(Integer.valueOf(config.getClientUid()));
                            int index = this.mSortedAudioPlaybackClientUids.indexOf(config.getClientUid());
                            if (index != 0) {
                                if (index > 0) {
                                    this.mSortedAudioPlaybackClientUids.remove(index);
                                }
                                this.mSortedAudioPlaybackClientUids.add(0, config.getClientUid());
                            }
                        }
                    }
                }
                this.mActiveAudioPlaybackPlayerInterfaceIds.clear();
                this.mActiveAudioPlaybackPlayerInterfaceIds = newActiveAudioPlaybackPlayerInterfaceIds;
            }
            for (Integer intValue : newActiveAudioPlaybackClientUids) {
                this.mListener.onAudioPlaybackStarted(intValue.intValue());
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public IntArray getSortedAudioPlaybackClientUids() {
        IntArray sortedAudioPlaybackClientUids = new IntArray();
        synchronized (this.mLock) {
            sortedAudioPlaybackClientUids.addAll(this.mSortedAudioPlaybackClientUids);
        }
        return sortedAudioPlaybackClientUids;
    }

    public boolean isPlaybackActive(int uid) {
        boolean contains;
        synchronized (this.mLock) {
            contains = this.mActiveAudioPlaybackClientUids.contains(Integer.valueOf(uid));
        }
        return contains;
    }

    public void cleanUpAudioPlaybackUids(int mediaButtonSessionUid) {
        synchronized (this.mLock) {
            int userId = UserHandle.getUserId(mediaButtonSessionUid);
            int i = this.mSortedAudioPlaybackClientUids.size() - 1;
            while (i >= 0 && this.mSortedAudioPlaybackClientUids.get(i) != mediaButtonSessionUid) {
                if (userId == UserHandle.getUserId(this.mSortedAudioPlaybackClientUids.get(i))) {
                    this.mSortedAudioPlaybackClientUids.remove(i);
                }
                i--;
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.println(prefix + "Audio playback (lastly played comes first)");
            String indent = prefix + "  ";
            for (int i = 0; i < this.mSortedAudioPlaybackClientUids.size(); i++) {
                int uid = this.mSortedAudioPlaybackClientUids.get(i);
                pw.print(indent + "uid=" + uid + " packages=");
                String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
                if (packages != null && packages.length > 0) {
                    for (String str : packages) {
                        pw.print(str + " ");
                    }
                }
                pw.println();
            }
        }
    }
}
