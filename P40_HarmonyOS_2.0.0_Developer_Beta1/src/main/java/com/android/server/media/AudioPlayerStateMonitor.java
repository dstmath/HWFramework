package com.android.server.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* access modifiers changed from: package-private */
public class AudioPlayerStateMonitor {
    private static boolean DEBUG = true;
    private static String TAG = "AudioPlayerStateMonitor";
    private static AudioPlayerStateMonitor sInstance;
    @GuardedBy({"mLock"})
    final Set<Integer> mActiveAudioUids = new ArraySet();
    @GuardedBy({"mLock"})
    private final Map<OnAudioPlayerActiveStateChangedListener, MessageHandler> mListenerMap = new ArrayMap();
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    ArrayMap<Integer, AudioPlaybackConfiguration> mPrevActiveAudioPlaybackConfigs = new ArrayMap<>();
    @GuardedBy({"mLock"})
    final IntArray mSortedAudioPlaybackClientUids = new IntArray();

    /* access modifiers changed from: package-private */
    public interface OnAudioPlayerActiveStateChangedListener {
        void onAudioPlayerActiveStateChanged(AudioPlaybackConfiguration audioPlaybackConfiguration, boolean z);
    }

    /* access modifiers changed from: private */
    public static final class MessageHandler extends Handler {
        private static final int MSG_AUDIO_PLAYER_ACTIVE_STATE_CHANGED = 1;
        private final OnAudioPlayerActiveStateChangedListener mListener;

        MessageHandler(Looper looper, OnAudioPlayerActiveStateChangedListener listener) {
            super(looper);
            this.mListener = listener;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what == 1) {
                OnAudioPlayerActiveStateChangedListener onAudioPlayerActiveStateChangedListener = this.mListener;
                AudioPlaybackConfiguration audioPlaybackConfiguration = (AudioPlaybackConfiguration) msg.obj;
                if (msg.arg1 == 0) {
                    z = false;
                }
                onAudioPlayerActiveStateChangedListener.onAudioPlayerActiveStateChanged(audioPlaybackConfiguration, z);
            }
        }

        /* access modifiers changed from: package-private */
        public void sendAudioPlayerActiveStateChangedMessage(AudioPlaybackConfiguration config, boolean isRemoved) {
            obtainMessage(1, isRemoved ? 1 : 0, 0, config).sendToTarget();
        }
    }

    static AudioPlayerStateMonitor getInstance(Context context) {
        AudioPlayerStateMonitor audioPlayerStateMonitor;
        synchronized (AudioPlayerStateMonitor.class) {
            if (sInstance == null) {
                sInstance = new AudioPlayerStateMonitor(context);
            }
            audioPlayerStateMonitor = sInstance;
        }
        return audioPlayerStateMonitor;
    }

    private AudioPlayerStateMonitor(Context context) {
        ((AudioManager) context.getSystemService("audio")).registerAudioPlaybackCallback(new AudioManagerPlaybackListener(), null);
    }

    public void registerListener(OnAudioPlayerActiveStateChangedListener listener, Handler handler) {
        synchronized (this.mLock) {
            this.mListenerMap.put(listener, new MessageHandler(handler == null ? Looper.myLooper() : handler.getLooper(), listener));
        }
    }

    public void unregisterListener(OnAudioPlayerActiveStateChangedListener listener) {
        synchronized (this.mLock) {
            this.mListenerMap.remove(listener);
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
            contains = this.mActiveAudioUids.contains(Integer.valueOf(uid));
        }
        return contains;
    }

    public void cleanUpAudioPlaybackUids(int mediaButtonSessionUid) {
        synchronized (this.mLock) {
            int userId = UserHandle.getUserId(mediaButtonSessionUid);
            int i = this.mSortedAudioPlaybackClientUids.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (this.mSortedAudioPlaybackClientUids.get(i) == mediaButtonSessionUid) {
                    break;
                } else {
                    int uid = this.mSortedAudioPlaybackClientUids.get(i);
                    if (userId == UserHandle.getUserId(uid) && !isPlaybackActive(uid)) {
                        this.mSortedAudioPlaybackClientUids.remove(i);
                    }
                    i--;
                }
            }
        }
    }

    public void dump(Context context, PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.println(prefix + "Audio playback (lastly played comes first)");
            String indent = prefix + "  ";
            for (int i = 0; i < this.mSortedAudioPlaybackClientUids.size(); i++) {
                int uid = this.mSortedAudioPlaybackClientUids.get(i);
                pw.print(indent + "uid=" + uid + " packages=");
                String[] packages = context.getPackageManager().getPackagesForUid(uid);
                if (packages != null && packages.length > 0) {
                    for (int j = 0; j < packages.length; j++) {
                        pw.print(packages[j] + " ");
                    }
                }
                pw.println();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void sendAudioPlayerActiveStateChangedMessageLocked(AudioPlaybackConfiguration config, boolean isRemoved) {
        for (MessageHandler messageHandler : this.mListenerMap.values()) {
            messageHandler.sendAudioPlayerActiveStateChangedMessage(config, isRemoved);
        }
    }

    private class AudioManagerPlaybackListener extends AudioManager.AudioPlaybackCallback {
        private AudioManagerPlaybackListener() {
        }

        @Override // android.media.AudioManager.AudioPlaybackCallback
        public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
            synchronized (AudioPlayerStateMonitor.this.mLock) {
                AudioPlayerStateMonitor.this.mActiveAudioUids.clear();
                ArrayMap<Integer, AudioPlaybackConfiguration> activeAudioPlaybackConfigs = new ArrayMap<>();
                for (AudioPlaybackConfiguration config : configs) {
                    if (config.isActive()) {
                        AudioPlayerStateMonitor.this.mActiveAudioUids.add(Integer.valueOf(config.getClientUid()));
                        activeAudioPlaybackConfigs.put(Integer.valueOf(config.getPlayerInterfaceId()), config);
                    }
                }
                for (int i = 0; i < activeAudioPlaybackConfigs.size(); i++) {
                    AudioPlaybackConfiguration config2 = activeAudioPlaybackConfigs.valueAt(i);
                    int uid = config2.getClientUid();
                    if (!AudioPlayerStateMonitor.this.mPrevActiveAudioPlaybackConfigs.containsKey(Integer.valueOf(config2.getPlayerInterfaceId()))) {
                        if (AudioPlayerStateMonitor.DEBUG) {
                            Log.i(AudioPlayerStateMonitor.TAG, "Found a new active media playback. " + AudioPlaybackConfiguration.toLogFriendlyString(config2));
                        }
                        int index = AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.indexOf(uid);
                        if (index != 0) {
                            if (index > 0) {
                                AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.remove(index);
                            }
                            AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.add(0, uid);
                        }
                    }
                }
                if (AudioPlayerStateMonitor.this.mActiveAudioUids.size() > 0 && !AudioPlayerStateMonitor.this.mActiveAudioUids.contains(Integer.valueOf(AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.get(0)))) {
                    int firstActiveUid = -1;
                    int firatActiveUidIndex = -1;
                    int i2 = 1;
                    while (true) {
                        if (i2 >= AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.size()) {
                            break;
                        }
                        int uid2 = AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.get(i2);
                        if (AudioPlayerStateMonitor.this.mActiveAudioUids.contains(Integer.valueOf(uid2))) {
                            firatActiveUidIndex = i2;
                            firstActiveUid = uid2;
                            break;
                        }
                        i2++;
                    }
                    for (int i3 = firatActiveUidIndex; i3 > 0; i3--) {
                        AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.set(i3, AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.get(i3 - 1));
                    }
                    AudioPlayerStateMonitor.this.mSortedAudioPlaybackClientUids.set(0, firstActiveUid);
                }
                for (AudioPlaybackConfiguration config3 : configs) {
                    if ((AudioPlayerStateMonitor.this.mPrevActiveAudioPlaybackConfigs.remove(Integer.valueOf(config3.getPlayerInterfaceId())) != null) != config3.isActive()) {
                        AudioPlayerStateMonitor.this.sendAudioPlayerActiveStateChangedMessageLocked(config3, false);
                    }
                }
                for (AudioPlaybackConfiguration config4 : AudioPlayerStateMonitor.this.mPrevActiveAudioPlaybackConfigs.values()) {
                    AudioPlayerStateMonitor.this.sendAudioPlayerActiveStateChangedMessageLocked(config4, true);
                }
                AudioPlayerStateMonitor.this.mPrevActiveAudioPlaybackConfigs = activeAudioPlaybackConfigs;
            }
        }
    }
}
