package com.android.server.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioSystem;
import android.media.IPlaybackConfigDispatcher;
import android.media.PlayerBase;
import android.media.VolumeShaper;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.server.audio.AudioEventLogger;
import com.android.server.slice.SliceClientPermissions;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class PlaybackActivityMonitor implements AudioPlaybackConfiguration.PlayerDeathMonitor, PlayerFocusEnforcer {
    private static final boolean DEBUG = true;
    /* access modifiers changed from: private */
    public static final VolumeShaper.Configuration DUCK_ID = new VolumeShaper.Configuration(1);
    /* access modifiers changed from: private */
    public static final VolumeShaper.Configuration DUCK_VSHAPE = new VolumeShaper.Configuration.Builder().setId(1).setCurve(new float[]{0.0f, 1.0f}, new float[]{1.0f, 0.2f}).setOptionFlags(2).setDuration((long) MediaFocusControl.getFocusRampTimeMs(3, new AudioAttributes.Builder().setUsage(5).build())).build();
    private static final int FLAGS_FOR_SILENCE_OVERRIDE = 192;
    /* access modifiers changed from: private */
    public static final VolumeShaper.Operation PLAY_CREATE_IF_NEEDED = new VolumeShaper.Operation.Builder(VolumeShaper.Operation.PLAY).createIfNeeded().build();
    /* access modifiers changed from: private */
    public static final VolumeShaper.Operation PLAY_SKIP_RAMP = new VolumeShaper.Operation.Builder(PLAY_CREATE_IF_NEEDED).setXOffset(1.0f).build();
    public static final String TAG = "AudioService.PlaybackActivityMonitor";
    private static final int[] UNDUCKABLE_PLAYER_TYPES = {13, 3};
    private static final int VOLUME_SHAPER_SYSTEM_DUCK_ID = 1;
    /* access modifiers changed from: private */
    public static final AudioEventLogger sEventLogger = new AudioEventLogger(100, "playback activity as reported through PlayerBase");
    private final ArrayList<Integer> mBannedUids = new ArrayList<>();
    private final ArrayList<PlayMonitorClient> mClients = new ArrayList<>();
    private final Context mContext;
    private final DuckingManager mDuckingManager = new DuckingManager();
    private boolean mHasPublicClients = false;
    private final int mMaxAlarmVolume;
    private MediaFocusControl mMfc;
    private final ArrayList<Integer> mMutedPlayers = new ArrayList<>();
    private final Object mPlayerLock = new Object();
    private final HashMap<Integer, AudioPlaybackConfiguration> mPlayers = new HashMap<>();
    private int mPrivilegedAlarmActiveCount = 0;
    private int mSavedAlarmVolume = -1;

    private static final class AudioAttrEvent extends AudioEventLogger.Event {
        private final AudioAttributes mPlayerAttr;
        private final int mPlayerIId;

        AudioAttrEvent(int piid, AudioAttributes attr) {
            this.mPlayerIId = piid;
            this.mPlayerAttr = attr;
        }

        public String eventToString() {
            return new String("player piid:" + this.mPlayerIId + " new AudioAttributes:" + this.mPlayerAttr);
        }
    }

    private static final class DuckEvent extends AudioEventLogger.Event {
        private final int mClientPid;
        private final int mClientUid;
        private final int mPlayerIId;
        private final boolean mSkipRamp;

        DuckEvent(AudioPlaybackConfiguration apc, boolean skipRamp) {
            this.mPlayerIId = apc.getPlayerInterfaceId();
            this.mSkipRamp = skipRamp;
            this.mClientUid = apc.getClientUid();
            this.mClientPid = apc.getClientPid();
        }

        public String eventToString() {
            return "ducking player piid:" + this.mPlayerIId + " uid/pid:" + this.mClientUid + SliceClientPermissions.SliceAuthority.DELIMITER + this.mClientPid + " skip ramp:" + this.mSkipRamp;
        }
    }

    private static final class DuckingManager {
        private final HashMap<Integer, DuckedApp> mDuckers;

        private static final class DuckedApp {
            private final ArrayList<Integer> mDuckedPlayers = new ArrayList<>();
            private final int mUid;

            DuckedApp(int uid) {
                this.mUid = uid;
            }

            /* access modifiers changed from: package-private */
            public void dump(PrintWriter pw) {
                pw.print("\t uid:" + this.mUid + " piids:");
                Iterator<Integer> it = this.mDuckedPlayers.iterator();
                while (it.hasNext()) {
                    int piid = it.next().intValue();
                    pw.print(" " + piid);
                }
                pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }

            /* access modifiers changed from: package-private */
            public void addDuck(AudioPlaybackConfiguration apc, boolean skipRamp) {
                int piid = new Integer(apc.getPlayerInterfaceId()).intValue();
                if (this.mDuckedPlayers.contains(Integer.valueOf(piid))) {
                    Log.v(PlaybackActivityMonitor.TAG, "player piid:" + piid + " already ducked");
                    return;
                }
                try {
                    PlaybackActivityMonitor.sEventLogger.log(new DuckEvent(apc, skipRamp).printLog(PlaybackActivityMonitor.TAG));
                    apc.getPlayerProxy().applyVolumeShaper(PlaybackActivityMonitor.DUCK_VSHAPE, skipRamp ? PlaybackActivityMonitor.PLAY_SKIP_RAMP : PlaybackActivityMonitor.PLAY_CREATE_IF_NEEDED);
                    this.mDuckedPlayers.add(Integer.valueOf(piid));
                } catch (Exception e) {
                    Log.e(PlaybackActivityMonitor.TAG, "Error ducking player piid:" + piid + " uid:" + this.mUid, e);
                }
            }

            /* access modifiers changed from: package-private */
            public void removeUnduckAll(HashMap<Integer, AudioPlaybackConfiguration> players) {
                Iterator<Integer> it = this.mDuckedPlayers.iterator();
                while (it.hasNext()) {
                    int piid = it.next().intValue();
                    AudioPlaybackConfiguration apc = players.get(Integer.valueOf(piid));
                    if (apc != null) {
                        try {
                            AudioEventLogger access$100 = PlaybackActivityMonitor.sEventLogger;
                            access$100.log(new AudioEventLogger.StringEvent("unducking piid:" + piid).printLog(PlaybackActivityMonitor.TAG));
                            apc.getPlayerProxy().applyVolumeShaper(PlaybackActivityMonitor.DUCK_ID, VolumeShaper.Operation.REVERSE);
                        } catch (Exception e) {
                            Log.e(PlaybackActivityMonitor.TAG, "Error unducking player piid:" + piid + " uid:" + this.mUid, e);
                        }
                    } else {
                        Log.v(PlaybackActivityMonitor.TAG, "Error unducking player piid:" + piid + ", player not found for uid " + this.mUid);
                    }
                }
                this.mDuckedPlayers.clear();
            }

            /* access modifiers changed from: package-private */
            public void removeReleased(AudioPlaybackConfiguration apc) {
                this.mDuckedPlayers.remove(new Integer(apc.getPlayerInterfaceId()));
            }
        }

        private DuckingManager() {
            this.mDuckers = new HashMap<>();
        }

        /* access modifiers changed from: package-private */
        public synchronized void duckUid(int uid, ArrayList<AudioPlaybackConfiguration> apcsToDuck) {
            Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: duckUid() uid:" + uid);
            if (!this.mDuckers.containsKey(Integer.valueOf(uid))) {
                this.mDuckers.put(Integer.valueOf(uid), new DuckedApp(uid));
            }
            DuckedApp da = this.mDuckers.get(Integer.valueOf(uid));
            Iterator<AudioPlaybackConfiguration> it = apcsToDuck.iterator();
            while (it.hasNext()) {
                da.addDuck(it.next(), false);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void unduckUid(int uid, HashMap<Integer, AudioPlaybackConfiguration> players) {
            Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: unduckUid() uid:" + uid);
            DuckedApp da = this.mDuckers.remove(Integer.valueOf(uid));
            if (da != null) {
                da.removeUnduckAll(players);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void checkDuck(AudioPlaybackConfiguration apc) {
            Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: checkDuck() player piid:" + apc.getPlayerInterfaceId() + " uid:" + apc.getClientUid());
            DuckedApp da = this.mDuckers.get(Integer.valueOf(apc.getClientUid()));
            if (da != null) {
                da.addDuck(apc, true);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void dump(PrintWriter pw) {
            for (DuckedApp da : this.mDuckers.values()) {
                da.dump(pw);
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void removeReleased(AudioPlaybackConfiguration apc) {
            int uid = apc.getClientUid();
            Log.v(PlaybackActivityMonitor.TAG, "DuckingManager: removedReleased() player piid: " + apc.getPlayerInterfaceId() + " uid:" + uid);
            DuckedApp da = this.mDuckers.get(Integer.valueOf(uid));
            if (da != null) {
                da.removeReleased(apc);
            }
        }
    }

    private static final class NewPlayerEvent extends AudioEventLogger.Event {
        private final int mClientPid;
        private final int mClientUid;
        private final AudioAttributes mPlayerAttr;
        private final int mPlayerIId;
        private final int mPlayerType;

        NewPlayerEvent(AudioPlaybackConfiguration apc) {
            this.mPlayerIId = apc.getPlayerInterfaceId();
            this.mPlayerType = apc.getPlayerType();
            this.mClientUid = apc.getClientUid();
            this.mClientPid = apc.getClientPid();
            this.mPlayerAttr = apc.getAudioAttributes();
        }

        public String eventToString() {
            return new String("new player piid:" + this.mPlayerIId + " uid/pid:" + this.mClientUid + SliceClientPermissions.SliceAuthority.DELIMITER + this.mClientPid + " type:" + AudioPlaybackConfiguration.toLogFriendlyPlayerType(this.mPlayerType) + " attr:" + this.mPlayerAttr);
        }
    }

    private static final class PlayMonitorClient implements IBinder.DeathRecipient {
        static final int MAX_ERRORS = 5;
        static PlaybackActivityMonitor sListenerDeathMonitor;
        final IPlaybackConfigDispatcher mDispatcherCb;
        int mErrorCount = 0;
        final boolean mIsPrivileged;

        PlayMonitorClient(IPlaybackConfigDispatcher pcdb, boolean isPrivileged) {
            this.mDispatcherCb = pcdb;
            this.mIsPrivileged = isPrivileged;
        }

        public void binderDied() {
            Log.w(PlaybackActivityMonitor.TAG, "client died");
            sListenerDeathMonitor.unregisterPlaybackCallback(this.mDispatcherCb);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(PlaybackActivityMonitor.TAG, "Could not link to client death", e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    private static final class PlayerEvent extends AudioEventLogger.Event {
        final int mPlayerIId;
        final int mState;

        PlayerEvent(int piid, int state) {
            this.mPlayerIId = piid;
            this.mState = state;
        }

        public String eventToString() {
            return "player piid:" + this.mPlayerIId + " state:" + AudioPlaybackConfiguration.toLogFriendlyPlayerState(this.mState);
        }
    }

    private static final class PlayerOpPlayAudioEvent extends AudioEventLogger.Event {
        final boolean mHasOp;
        final int mPlayerIId;
        final int mUid;

        PlayerOpPlayAudioEvent(int piid, boolean hasOp, int uid) {
            this.mPlayerIId = piid;
            this.mHasOp = hasOp;
            this.mUid = uid;
        }

        public String eventToString() {
            return "player piid:" + this.mPlayerIId + " has OP_PLAY_AUDIO:" + this.mHasOp + " in uid:" + this.mUid;
        }
    }

    PlaybackActivityMonitor(Context context, int maxAlarmVolume) {
        this.mContext = context;
        this.mMaxAlarmVolume = maxAlarmVolume;
        PlayMonitorClient.sListenerDeathMonitor = this;
        AudioPlaybackConfiguration.sPlayerDeathMonitor = this;
    }

    public void disableAudioForUid(boolean disable, int uid) {
        synchronized (this.mPlayerLock) {
            int index = this.mBannedUids.indexOf(new Integer(uid));
            if (index >= 0) {
                if (!disable) {
                    AudioEventLogger audioEventLogger = sEventLogger;
                    audioEventLogger.log(new AudioEventLogger.StringEvent("unbanning uid:" + uid));
                    this.mBannedUids.remove(index);
                }
            } else if (disable) {
                for (AudioPlaybackConfiguration apc : this.mPlayers.values()) {
                    checkBanPlayer(apc, uid);
                }
                AudioEventLogger audioEventLogger2 = sEventLogger;
                audioEventLogger2.log(new AudioEventLogger.StringEvent("banning uid:" + uid));
                this.mBannedUids.add(new Integer(uid));
            }
        }
    }

    private boolean checkBanPlayer(AudioPlaybackConfiguration apc, int uid) {
        boolean toBan = apc.getClientUid() == uid;
        if (toBan) {
            int piid = apc.getPlayerInterfaceId();
            try {
                Log.v(TAG, "banning player " + piid + " uid:" + uid);
                apc.getPlayerProxy().pause();
            } catch (Exception e) {
                Log.e(TAG, "error banning player " + piid + " uid:" + uid, e);
            }
        }
        return toBan;
    }

    public int trackPlayer(PlayerBase.PlayerIdCard pic) {
        int newPiid = AudioSystem.newAudioPlayerId();
        Log.v(TAG, "trackPlayer() new piid=" + newPiid);
        AudioPlaybackConfiguration apc = new AudioPlaybackConfiguration(pic, newPiid, Binder.getCallingUid(), Binder.getCallingPid());
        apc.init();
        sEventLogger.log(new NewPlayerEvent(apc));
        synchronized (this.mPlayerLock) {
            this.mPlayers.put(Integer.valueOf(newPiid), apc);
        }
        return newPiid;
    }

    public void playerAttributes(int piid, AudioAttributes attr, int binderUid) {
        boolean change;
        boolean change2;
        synchronized (this.mPlayerLock) {
            AudioPlaybackConfiguration apc = this.mPlayers.get(new Integer(piid));
            if (checkConfigurationCaller(piid, apc, binderUid)) {
                sEventLogger.log(new AudioAttrEvent(piid, attr));
                change = apc.handleAudioAttributesEvent(attr);
            } else {
                Log.e(TAG, "Error updating audio attributes");
                change = false;
            }
            change2 = change;
        }
        if (change2) {
            dispatchPlaybackChange(false);
        }
    }

    private void checkVolumeForPrivilegedAlarm(AudioPlaybackConfiguration apc, int event) {
        if ((event != 2 && apc.getPlayerState() != 2) || (apc.getAudioAttributes().getAllFlags() & FLAGS_FOR_SILENCE_OVERRIDE) != FLAGS_FOR_SILENCE_OVERRIDE || apc.getAudioAttributes().getUsage() != 4 || this.mContext.checkPermission("android.permission.MODIFY_PHONE_STATE", apc.getClientPid(), apc.getClientUid()) != 0) {
            return;
        }
        if (event == 2 && apc.getPlayerState() != 2) {
            int i = this.mPrivilegedAlarmActiveCount;
            this.mPrivilegedAlarmActiveCount = i + 1;
            if (i == 0) {
                this.mSavedAlarmVolume = AudioSystem.getStreamVolumeIndex(4, 2);
                AudioSystem.setStreamVolumeIndex(4, this.mMaxAlarmVolume, 2);
            }
        } else if (event != 2 && apc.getPlayerState() == 2) {
            int i2 = this.mPrivilegedAlarmActiveCount - 1;
            this.mPrivilegedAlarmActiveCount = i2;
            if (i2 == 0 && AudioSystem.getStreamVolumeIndex(4, 2) == this.mMaxAlarmVolume) {
                AudioSystem.setStreamVolumeIndex(4, this.mSavedAlarmVolume, 2);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00e1, code lost:
        if (r3 == false) goto L_0x00ea;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e3, code lost:
        if (r11 != 0) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00e5, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00e7, code lost:
        dispatchPlaybackChange(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ea, code lost:
        return;
     */
    public void playerEvent(int piid, int event, int binderUid) {
        boolean change;
        boolean z = false;
        Log.v(TAG, String.format("playerEvent(piid=%d, event=%d)", new Object[]{Integer.valueOf(piid), Integer.valueOf(event)}));
        synchronized (this.mPlayerLock) {
            AudioPlaybackConfiguration apc = this.mPlayers.get(new Integer(piid));
            if (apc == null) {
                Log.v(TAG, "apc is null");
                return;
            }
            sEventLogger.log(new PlayerEvent(piid, event));
            if (event == 2) {
                Iterator<Integer> it = this.mBannedUids.iterator();
                while (it.hasNext()) {
                    if (checkBanPlayer(apc, it.next().intValue())) {
                        sEventLogger.log(new AudioEventLogger.StringEvent("not starting piid:" + piid + " ,is banned"));
                        Log.v(TAG, "not starting piid");
                        return;
                    }
                }
            }
            if (apc.getPlayerType() == 3) {
                Log.v(TAG, "SoundPool");
                return;
            }
            if (checkConfigurationCaller(piid, apc, binderUid)) {
                checkVolumeForPrivilegedAlarm(apc, event);
                change = apc.handleStateEvent(event);
                Log.v(TAG, "change: " + change);
            } else {
                Log.e(TAG, "Error handling event " + event);
                change = false;
            }
            if (change && event == 2) {
                this.mDuckingManager.checkDuck(apc);
            }
        }
    }

    public void playerHasOpPlayAudio(int piid, boolean hasOpPlayAudio, int binderUid) {
        sEventLogger.log(new PlayerOpPlayAudioEvent(piid, hasOpPlayAudio, binderUid));
    }

    public void releasePlayer(int piid, int binderUid) {
        Log.v(TAG, "releasePlayer() for piid=" + piid);
        boolean change = false;
        synchronized (this.mPlayerLock) {
            AudioPlaybackConfiguration apc = this.mPlayers.get(new Integer(piid));
            if (checkConfigurationCaller(piid, apc, binderUid)) {
                AudioEventLogger audioEventLogger = sEventLogger;
                audioEventLogger.log(new AudioEventLogger.StringEvent("releasing player piid:" + piid));
                this.mPlayers.remove(new Integer(piid));
                this.mDuckingManager.removeReleased(apc);
                checkVolumeForPrivilegedAlarm(apc, 0);
                change = apc.handleStateEvent(0);
                LogPower.push(163, String.valueOf(Binder.getCallingPid()), String.valueOf(piid), String.valueOf(Binder.getCallingUid()));
            }
        }
        if (change) {
            dispatchPlaybackChange(true);
        }
    }

    public void playerDeath(int piid) {
        releasePlayer(piid, 0);
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("\nPlaybackActivityMonitor dump time: " + DateFormat.getTimeInstance().format(new Date()));
        synchronized (this.mPlayerLock) {
            pw.println("\n  playback listeners:");
            synchronized (this.mClients) {
                Iterator<PlayMonitorClient> it = this.mClients.iterator();
                while (it.hasNext()) {
                    PlayMonitorClient pmc = it.next();
                    StringBuilder sb = new StringBuilder();
                    sb.append(" ");
                    sb.append(pmc.mIsPrivileged ? "(S)" : "(P)");
                    sb.append(pmc.toString());
                    pw.print(sb.toString());
                }
            }
            pw.println("\n");
            pw.println("\n  players:");
            List<Integer> piidIntList = new ArrayList<>(this.mPlayers.keySet());
            Collections.sort(piidIntList);
            for (Integer piidInt : piidIntList) {
                AudioPlaybackConfiguration apc = this.mPlayers.get(piidInt);
                if (apc != null) {
                    apc.dump(pw);
                }
            }
            pw.println("\n  ducked players piids:");
            this.mDuckingManager.dump(pw);
            pw.print("\n  muted player piids:");
            Iterator<Integer> it2 = this.mMutedPlayers.iterator();
            while (it2.hasNext()) {
                int piid = it2.next().intValue();
                pw.print(" " + piid);
            }
            pw.println();
            pw.print("\n  banned uids:");
            Iterator<Integer> it3 = this.mBannedUids.iterator();
            while (it3.hasNext()) {
                int uid = it3.next().intValue();
                pw.print(" " + uid);
            }
            pw.println("\n");
            sEventLogger.dump(pw);
        }
    }

    private static boolean checkConfigurationCaller(int piid, AudioPlaybackConfiguration apc, int binderUid) {
        if (apc == null) {
            return false;
        }
        if (binderUid == 0 || apc.getClientUid() == binderUid) {
            return true;
        }
        Log.e(TAG, "Forbidden operation from uid " + binderUid + " for player " + piid);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0039, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0040, code lost:
        if (r9.mPlayers.isEmpty() == false) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0042, code lost:
        android.util.Log.v(TAG, "mPlayers is empty");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004a, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        r0 = new java.util.ArrayList<>(r9.mPlayers.values());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0057, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        r2 = r9.mClients;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005a, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0061, code lost:
        if (r9.mClients.isEmpty() == false) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0063, code lost:
        android.util.Log.v(TAG, "mClients is empty");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006b, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006f, code lost:
        if (r9.mHasPublicClients == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0071, code lost:
        r1 = anonymizeForPublicConsumption(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0076, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0077, code lost:
        r3 = r9.mClients.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0081, code lost:
        if (r3.hasNext() == false) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0083, code lost:
        r4 = r3.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008c, code lost:
        if (r4.mErrorCount >= 5) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0090, code lost:
        if (r4.mIsPrivileged == false) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0092, code lost:
        android.util.Log.v(TAG, "configsSystem");
        r4.mDispatcherCb.dispatchPlaybackConfigChange(r0, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x009f, code lost:
        android.util.Log.v(TAG, "configsPublic");
        r4.mDispatcherCb.dispatchPlaybackConfigChange(r1, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00ad, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r4.mErrorCount++;
        android.util.Log.e(TAG, "Error (" + r4.mErrorCount + ") trying to dispatch playback config change to " + r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d5, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d6, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        android.util.Log.v(TAG, "dispatchPlaybackChange to " + r9.mClients.size() + " clients");
        r1 = r9.mPlayerLock;
     */
    private void dispatchPlaybackChange(boolean iplayerReleased) {
        synchronized (this.mClients) {
            if (this.mClients.isEmpty()) {
                Log.v(TAG, "mClients is empty");
            }
        }
    }

    private ArrayList<AudioPlaybackConfiguration> anonymizeForPublicConsumption(List<AudioPlaybackConfiguration> sysConfigs) {
        ArrayList<AudioPlaybackConfiguration> publicConfigs = new ArrayList<>();
        for (AudioPlaybackConfiguration config : sysConfigs) {
            if (config.isActive()) {
                publicConfigs.add(AudioPlaybackConfiguration.anonymizedCopy(config));
            }
        }
        return publicConfigs;
    }

    public boolean duckPlayers(FocusRequester winner, FocusRequester loser, boolean forceDuck) {
        Log.v(TAG, String.format("duckPlayers: uids winner=%d loser=%d", new Object[]{Integer.valueOf(winner.getClientUid()), Integer.valueOf(loser.getClientUid())}));
        synchronized (this.mPlayerLock) {
            if (this.mPlayers.isEmpty()) {
                return true;
            }
            ArrayList<AudioPlaybackConfiguration> apcsToDuck = new ArrayList<>();
            for (AudioPlaybackConfiguration apc : this.mPlayers.values()) {
                if (!winner.hasSameUid(apc.getClientUid()) && loser.hasSameUid(apc.getClientUid()) && apc.getPlayerState() == 2) {
                    if (!forceDuck && apc.getAudioAttributes().getContentType() == 1) {
                        Log.v(TAG, "not ducking player " + apc.getPlayerInterfaceId() + " uid:" + apc.getClientUid() + " pid:" + apc.getClientPid() + " - SPEECH");
                        return false;
                    } else if (ArrayUtils.contains(UNDUCKABLE_PLAYER_TYPES, apc.getPlayerType())) {
                        Log.v(TAG, "not ducking player " + apc.getPlayerInterfaceId() + " uid:" + apc.getClientUid() + " pid:" + apc.getClientPid() + " due to type:" + AudioPlaybackConfiguration.toLogFriendlyPlayerType(apc.getPlayerType()));
                        return false;
                    } else {
                        apcsToDuck.add(apc);
                    }
                }
            }
            this.mDuckingManager.duckUid(loser.getClientUid(), apcsToDuck);
            return true;
        }
    }

    public void unduckPlayers(FocusRequester winner) {
        Log.v(TAG, "unduckPlayers: uids winner=" + winner.getClientUid());
        synchronized (this.mPlayerLock) {
            this.mDuckingManager.unduckUid(winner.getClientUid(), this.mPlayers);
        }
    }

    public void mutePlayersForCall(int[] usagesToMute) {
        String log = new String("mutePlayersForCall: usages=");
        String log2 = log;
        for (int usage : usagesToMute) {
            log2 = log2 + " " + usage;
        }
        Log.v(TAG, log2);
        synchronized (this.mPlayerLock) {
            for (Integer piid : this.mPlayers.keySet()) {
                AudioPlaybackConfiguration apc = this.mPlayers.get(piid);
                if (apc != null) {
                    int playerUsage = apc.getAudioAttributes().getUsage();
                    boolean mute = false;
                    int length = usagesToMute.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        } else if (playerUsage == usagesToMute[i]) {
                            mute = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    boolean isInExternalDisplay = false;
                    if (!(this.mMfc == null || apc.getPkgName() == null)) {
                        isInExternalDisplay = this.mMfc.isPkgInExternalDisplay(apc.getPkgName());
                        Log.v(TAG, "isInExternalDisplay = " + isInExternalDisplay);
                    }
                    if (mute && !isInExternalDisplay) {
                        try {
                            sEventLogger.log(new AudioEventLogger.StringEvent("call: muting piid:" + piid + " uid:" + apc.getClientUid()).printLog(TAG));
                            apc.getPlayerProxy().setVolume(0.0f);
                            this.mMutedPlayers.add(new Integer(piid.intValue()));
                        } catch (Exception e) {
                            Log.e(TAG, "call: error muting player " + piid, e);
                        }
                    }
                }
            }
        }
    }

    public void unmutePlayersForCall() {
        Log.v(TAG, "unmutePlayersForCall()");
        synchronized (this.mPlayerLock) {
            if (!this.mMutedPlayers.isEmpty()) {
                Iterator<Integer> it = this.mMutedPlayers.iterator();
                while (it.hasNext()) {
                    int piid = it.next().intValue();
                    AudioPlaybackConfiguration apc = this.mPlayers.get(Integer.valueOf(piid));
                    if (apc != null) {
                        try {
                            AudioEventLogger audioEventLogger = sEventLogger;
                            audioEventLogger.log(new AudioEventLogger.StringEvent("call: unmuting piid:" + piid).printLog(TAG));
                            apc.getPlayerProxy().setVolume(1.0f);
                        } catch (Exception e) {
                            Log.e(TAG, "call: error unmuting player " + piid + " uid:" + apc.getClientUid(), e);
                        }
                    }
                }
                this.mMutedPlayers.clear();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerPlaybackCallback(IPlaybackConfigDispatcher pcdb, boolean isPrivileged) {
        Log.v(TAG, "registerPlaybackCallback");
        if (pcdb != null) {
            synchronized (this.mClients) {
                PlayMonitorClient pmc = new PlayMonitorClient(pcdb, isPrivileged);
                if (pmc.init()) {
                    if (!isPrivileged) {
                        this.mHasPublicClients = true;
                    }
                    this.mClients.add(pmc);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterPlaybackCallback(IPlaybackConfigDispatcher pcdb) {
        Log.v(TAG, "unregisterPlaybackCallback");
        if (pcdb != null) {
            synchronized (this.mClients) {
                Iterator<PlayMonitorClient> clientIterator = this.mClients.iterator();
                boolean hasPublicClients = false;
                while (clientIterator.hasNext()) {
                    PlayMonitorClient pmc = clientIterator.next();
                    if (pcdb.equals(pmc.mDispatcherCb)) {
                        pmc.release();
                        clientIterator.remove();
                    } else if (!pmc.mIsPrivileged) {
                        hasPublicClients = true;
                    }
                }
                this.mHasPublicClients = hasPublicClients;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<AudioPlaybackConfiguration> getActivePlaybackConfigurations(boolean isPrivileged) {
        List<AudioPlaybackConfiguration> configsPublic;
        synchronized (this.mPlayers) {
            if (isPrivileged) {
                try {
                    ArrayList arrayList = new ArrayList(this.mPlayers.values());
                    return arrayList;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                synchronized (this.mPlayerLock) {
                    configsPublic = anonymizeForPublicConsumption(new ArrayList(this.mPlayers.values()));
                }
                return configsPublic;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setMediaFocusControl(MediaFocusControl mfc) {
        this.mMfc = mfc;
    }
}
