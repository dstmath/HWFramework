package android.media;

import android.annotation.SystemApi;
import android.media.AudioAttributes;
import android.media.IPlayer;
import android.media.PlayerBase;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public final class AudioPlaybackConfiguration implements Parcelable {
    public static final Parcelable.Creator<AudioPlaybackConfiguration> CREATOR = new Parcelable.Creator<AudioPlaybackConfiguration>() {
        public AudioPlaybackConfiguration createFromParcel(Parcel p) {
            return new AudioPlaybackConfiguration(p);
        }

        public AudioPlaybackConfiguration[] newArray(int size) {
            return new AudioPlaybackConfiguration[size];
        }
    };
    private static final boolean DEBUG = true;
    public static final int PLAYER_PIID_INVALID = -1;
    public static final int PLAYER_PIID_UNASSIGNED = 0;
    @SystemApi
    public static final int PLAYER_STATE_IDLE = 1;
    @SystemApi
    public static final int PLAYER_STATE_PAUSED = 3;
    @SystemApi
    public static final int PLAYER_STATE_RELEASED = 0;
    @SystemApi
    public static final int PLAYER_STATE_STARTED = 2;
    @SystemApi
    public static final int PLAYER_STATE_STOPPED = 4;
    @SystemApi
    public static final int PLAYER_STATE_UNKNOWN = -1;
    public static final int PLAYER_TYPE_AAUDIO = 13;
    public static final int PLAYER_TYPE_EXTERNAL_PROXY = 15;
    public static final int PLAYER_TYPE_HW_SOURCE = 14;
    @SystemApi
    public static final int PLAYER_TYPE_JAM_AUDIOTRACK = 1;
    @SystemApi
    public static final int PLAYER_TYPE_JAM_MEDIAPLAYER = 2;
    @SystemApi
    public static final int PLAYER_TYPE_JAM_SOUNDPOOL = 3;
    @SystemApi
    public static final int PLAYER_TYPE_SLES_AUDIOPLAYER_BUFFERQUEUE = 11;
    @SystemApi
    public static final int PLAYER_TYPE_SLES_AUDIOPLAYER_URI_FD = 12;
    @SystemApi
    public static final int PLAYER_TYPE_UNKNOWN = -1;
    public static final int PLAYER_UPID_INVALID = -1;
    /* access modifiers changed from: private */
    public static final String TAG = new String("AudioPlaybackConfiguration");
    public static PlayerDeathMonitor sPlayerDeathMonitor;
    private int mClientPid;
    private int mClientUid;
    private IPlayerShell mIPlayerShell;
    private String mPkgName;
    private AudioAttributes mPlayerAttr;
    /* access modifiers changed from: private */
    public final int mPlayerIId;
    private int mPlayerState;
    private int mPlayerType;

    static final class IPlayerShell implements IBinder.DeathRecipient {
        private volatile IPlayer mIPlayer;
        final AudioPlaybackConfiguration mMonitor;

        IPlayerShell(AudioPlaybackConfiguration monitor, IPlayer iplayer) {
            this.mMonitor = monitor;
            this.mIPlayer = iplayer;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
            return;
         */
        public synchronized void monitorDeath() {
            if (this.mIPlayer != null) {
                try {
                    this.mIPlayer.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    if (this.mMonitor != null) {
                        String access$100 = AudioPlaybackConfiguration.TAG;
                        Log.w(access$100, "Could not link to client death for piid=" + this.mMonitor.mPlayerIId, e);
                    } else {
                        Log.w(AudioPlaybackConfiguration.TAG, "Could not link to client death", e);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public IPlayer getIPlayer() {
            return this.mIPlayer;
        }

        public void binderDied() {
            if (this.mMonitor != null) {
                String access$100 = AudioPlaybackConfiguration.TAG;
                Log.i(access$100, "IPlayerShell binderDied for piid=" + this.mMonitor.mPlayerIId);
                this.mMonitor.playerDied();
                return;
            }
            Log.i(AudioPlaybackConfiguration.TAG, "IPlayerShell binderDied");
        }

        /* access modifiers changed from: package-private */
        public synchronized void release() {
            if (this.mIPlayer != null) {
                this.mIPlayer.asBinder().unlinkToDeath(this, 0);
                this.mIPlayer = null;
                Binder.flushPendingCommands();
            }
        }
    }

    public interface PlayerDeathMonitor {
        void playerDeath(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayerState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayerType {
    }

    private AudioPlaybackConfiguration(int piid) {
        this.mPlayerIId = piid;
        this.mIPlayerShell = null;
    }

    public AudioPlaybackConfiguration(PlayerBase.PlayerIdCard pic, int piid, int uid, int pid) {
        String str = TAG;
        Log.d(str, "new: piid=" + piid + " iplayer=" + pic.mIPlayer);
        this.mPlayerIId = piid;
        this.mPlayerType = pic.mPlayerType;
        this.mClientUid = uid;
        this.mClientPid = pid;
        this.mPlayerState = 1;
        this.mPlayerAttr = pic.mAttributes;
        if (sPlayerDeathMonitor == null || pic.mIPlayer == null) {
            this.mIPlayerShell = null;
        } else {
            this.mIPlayerShell = new IPlayerShell(this, pic.mIPlayer);
        }
        this.mPkgName = pic.getPkgName();
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public void init() {
        synchronized (this) {
            if (this.mIPlayerShell != null) {
                this.mIPlayerShell.monitorDeath();
            }
        }
    }

    public static AudioPlaybackConfiguration anonymizedCopy(AudioPlaybackConfiguration in) {
        AudioPlaybackConfiguration anonymCopy = new AudioPlaybackConfiguration(in.mPlayerIId);
        anonymCopy.mPlayerState = in.mPlayerState;
        anonymCopy.mPlayerAttr = new AudioAttributes.Builder().setUsage(in.mPlayerAttr.getUsage()).setContentType(in.mPlayerAttr.getContentType()).setFlags(in.mPlayerAttr.getFlags()).build();
        anonymCopy.mPlayerType = -1;
        anonymCopy.mClientUid = -1;
        anonymCopy.mClientPid = -1;
        anonymCopy.mIPlayerShell = null;
        return anonymCopy;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mPlayerAttr;
    }

    @SystemApi
    public int getClientUid() {
        return this.mClientUid;
    }

    @SystemApi
    public int getClientPid() {
        return this.mClientPid;
    }

    @SystemApi
    public int getPlayerType() {
        switch (this.mPlayerType) {
            case 13:
            case 14:
            case 15:
                return -1;
            default:
                return this.mPlayerType;
        }
    }

    @SystemApi
    public int getPlayerState() {
        return this.mPlayerState;
    }

    @SystemApi
    public int getPlayerInterfaceId() {
        return this.mPlayerIId;
    }

    @SystemApi
    public PlayerProxy getPlayerProxy() {
        IPlayerShell ips;
        synchronized (this) {
            ips = this.mIPlayerShell;
        }
        if (ips == null) {
            return null;
        }
        return new PlayerProxy(this);
    }

    /* access modifiers changed from: package-private */
    public IPlayer getIPlayer() {
        IPlayerShell ips;
        synchronized (this) {
            ips = this.mIPlayerShell;
        }
        if (ips == null) {
            return null;
        }
        return ips.getIPlayer();
    }

    public boolean handleAudioAttributesEvent(AudioAttributes attr) {
        boolean changed = !attr.equals(this.mPlayerAttr);
        this.mPlayerAttr = attr;
        return changed;
    }

    public boolean handleStateEvent(int event) {
        boolean changed;
        synchronized (this) {
            changed = this.mPlayerState != event;
            this.mPlayerState = event;
            if (changed && event == 0 && this.mIPlayerShell != null) {
                this.mIPlayerShell.release();
                this.mIPlayerShell = null;
            }
        }
        return changed;
    }

    /* access modifiers changed from: private */
    public void playerDied() {
        if (sPlayerDeathMonitor != null) {
            sPlayerDeathMonitor.playerDeath(this.mPlayerIId);
        }
    }

    public boolean isActive() {
        if (this.mPlayerState != 2) {
            return false;
        }
        return true;
    }

    public void dump(PrintWriter pw) {
        pw.println("  " + toLogFriendlyString(this));
    }

    public static String toLogFriendlyString(AudioPlaybackConfiguration apc) {
        return new String("ID:" + apc.mPlayerIId + " -- type:" + toLogFriendlyPlayerType(apc.mPlayerType) + " -- u/pid:" + apc.mClientUid + "/" + apc.mClientPid + " -- state:" + toLogFriendlyPlayerState(apc.mPlayerState) + " -- attr:" + apc.mPlayerAttr);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mPlayerIId), Integer.valueOf(this.mPlayerType), Integer.valueOf(this.mClientUid), Integer.valueOf(this.mClientPid)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        IPlayerShell ips;
        dest.writeInt(this.mPlayerIId);
        dest.writeInt(this.mPlayerType);
        dest.writeInt(this.mClientUid);
        dest.writeInt(this.mClientPid);
        dest.writeInt(this.mPlayerState);
        this.mPlayerAttr.writeToParcel(dest, 0);
        synchronized (this) {
            ips = this.mIPlayerShell;
        }
        dest.writeStrongInterface(ips == null ? null : ips.getIPlayer());
    }

    private AudioPlaybackConfiguration(Parcel in) {
        this.mPlayerIId = in.readInt();
        this.mPlayerType = in.readInt();
        this.mClientUid = in.readInt();
        this.mClientPid = in.readInt();
        this.mPlayerState = in.readInt();
        this.mPlayerAttr = AudioAttributes.CREATOR.createFromParcel(in);
        IPlayer p = IPlayer.Stub.asInterface(in.readStrongBinder());
        this.mIPlayerShell = p != null ? new IPlayerShell(null, p) : null;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AudioPlaybackConfiguration)) {
            return false;
        }
        AudioPlaybackConfiguration that = (AudioPlaybackConfiguration) o;
        if (!(this.mPlayerIId == that.mPlayerIId && this.mPlayerType == that.mPlayerType && this.mClientUid == that.mClientUid && this.mClientPid == that.mClientPid)) {
            z = false;
        }
        return z;
    }

    public static String toLogFriendlyPlayerType(int type) {
        if (type == -1) {
            return "unknown";
        }
        switch (type) {
            case 1:
                return "android.media.AudioTrack";
            case 2:
                return "android.media.MediaPlayer";
            case 3:
                return "android.media.SoundPool";
            default:
                switch (type) {
                    case 11:
                        return "OpenSL ES AudioPlayer (Buffer Queue)";
                    case 12:
                        return "OpenSL ES AudioPlayer (URI/FD)";
                    case 13:
                        return "AAudio";
                    case 14:
                        return "hardware source";
                    case 15:
                        return "external proxy";
                    default:
                        return "unknown player type " + type + " - FIXME";
                }
        }
    }

    public static String toLogFriendlyPlayerState(int state) {
        switch (state) {
            case -1:
                return "unknown";
            case 0:
                return "released";
            case 1:
                return "idle";
            case 2:
                return "started";
            case 3:
                return "paused";
            case 4:
                return "stopped";
            default:
                return "unknown player state - FIXME";
        }
    }
}
