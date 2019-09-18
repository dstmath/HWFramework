package com.android.server.media;

import android.content.Context;
import android.media.AudioPlaybackConfiguration;
import android.media.IAudioService;
import android.media.IPlaybackConfigDispatcher;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AudioPlayerStateMonitor extends IPlaybackConfigDispatcher.Stub {
    private static boolean DEBUG = MediaSessionService.DEBUG;
    private static String TAG = "AudioPlayerStateMonitor";
    private static AudioPlayerStateMonitor sInstance = new AudioPlayerStateMonitor();
    @GuardedBy("mLock")
    private final Set<Integer> mActiveAudioUids = new ArraySet();
    @GuardedBy("mLock")
    private final Map<OnAudioPlayerActiveStateChangedListener, MessageHandler> mListenerMap = new ArrayMap();
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private ArrayMap<Integer, AudioPlaybackConfiguration> mPrevActiveAudioPlaybackConfigs = new ArrayMap<>();
    @GuardedBy("mLock")
    private boolean mRegisteredToAudioService;
    @GuardedBy("mLock")
    private final IntArray mSortedAudioPlaybackClientUids = new IntArray();

    private static final class MessageHandler extends Handler {
        private static final int MSG_AUDIO_PLAYER_ACTIVE_STATE_CHANGED = 1;
        private final OnAudioPlayerActiveStateChangedListener mListener;

        MessageHandler(Looper looper, OnAudioPlayerActiveStateChangedListener listener) {
            super(looper);
            this.mListener = listener;
        }

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
            obtainMessage(1, isRemoved, 0, config).sendToTarget();
        }
    }

    interface OnAudioPlayerActiveStateChangedListener {
        void onAudioPlayerActiveStateChanged(AudioPlaybackConfiguration audioPlaybackConfiguration, boolean z);
    }

    static AudioPlayerStateMonitor getInstance() {
        return sInstance;
    }

    private AudioPlayerStateMonitor() {
    }

    public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs, boolean flush) {
        if (flush) {
            Binder.flushPendingCommands();
        }
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                this.mActiveAudioUids.clear();
                ArrayMap<Integer, AudioPlaybackConfiguration> activeAudioPlaybackConfigs = new ArrayMap<>();
                for (AudioPlaybackConfiguration config : configs) {
                    if (config.isActive()) {
                        this.mActiveAudioUids.add(Integer.valueOf(config.getClientUid()));
                        activeAudioPlaybackConfigs.put(Integer.valueOf(config.getPlayerInterfaceId()), config);
                    }
                }
                for (int i = 0; i < activeAudioPlaybackConfigs.size(); i++) {
                    AudioPlaybackConfiguration config2 = activeAudioPlaybackConfigs.valueAt(i);
                    int uid = config2.getClientUid();
                    if (!this.mPrevActiveAudioPlaybackConfigs.containsKey(Integer.valueOf(config2.getPlayerInterfaceId()))) {
                        if (DEBUG) {
                            Log.d(TAG, "Found a new active media playback. " + AudioPlaybackConfiguration.toLogFriendlyString(config2));
                        }
                        int index = this.mSortedAudioPlaybackClientUids.indexOf(uid);
                        if (index != 0) {
                            if (index > 0) {
                                this.mSortedAudioPlaybackClientUids.remove(index);
                            }
                            this.mSortedAudioPlaybackClientUids.add(0, uid);
                        }
                    }
                }
                Iterator<AudioPlaybackConfiguration> it = configs.iterator();
                while (true) {
                    boolean wasActive = true;
                    if (!it.hasNext()) {
                        break;
                    }
                    AudioPlaybackConfiguration config3 = it.next();
                    if (this.mPrevActiveAudioPlaybackConfigs.remove(Integer.valueOf(config3.getPlayerInterfaceId())) == null) {
                        wasActive = false;
                    }
                    if (wasActive != config3.isActive()) {
                        sendAudioPlayerActiveStateChangedMessageLocked(config3, false);
                    }
                }
                for (AudioPlaybackConfiguration config4 : this.mPrevActiveAudioPlaybackConfigs.values()) {
                    sendAudioPlayerActiveStateChangedMessageLocked(config4, true);
                }
                this.mPrevActiveAudioPlaybackConfigs = activeAudioPlaybackConfigs;
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
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
        int uid;
        synchronized (this.mLock) {
            pw.println(prefix + "Audio playback (lastly played comes first)");
            String indent = prefix + "  ";
            int i = 0;
            while (i < this.mSortedAudioPlaybackClientUids.size()) {
                pw.print(indent + "uid=" + uid + " packages=");
                String[] packages = context.getPackageManager().getPackagesForUid(uid);
                if (packages != null && packages.length > 0) {
                    for (int j = 0; j < packages.length; j++) {
                        pw.print(packages[j] + " ");
                    }
                }
                pw.println();
                i++;
            }
        }
    }

    public void registerSelfIntoAudioServiceIfNeeded(IAudioService audioService) {
        synchronized (this.mLock) {
            try {
                if (!this.mRegisteredToAudioService) {
                    audioService.registerPlaybackCallback(this);
                    this.mRegisteredToAudioService = true;
                }
            } catch (RemoteException e) {
                Log.wtf(TAG, "Failed to register playback callback", e);
                this.mRegisteredToAudioService = false;
            }
        }
    }

    @GuardedBy("mLock")
    private void sendAudioPlayerActiveStateChangedMessageLocked(AudioPlaybackConfiguration config, boolean isRemoved) {
        for (MessageHandler messageHandler : this.mListenerMap.values()) {
            messageHandler.sendAudioPlayerActiveStateChangedMessage(config, isRemoved);
        }
    }
}
