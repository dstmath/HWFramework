package android.media;

import android.media.AudioAttributes.Builder;
import android.media.IPlayer.Stub;
import android.media.PlayerBase.PlayerIdCard;
import android.os.IBinder.DeathRecipient;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.util.Log;
import java.io.PrintWriter;
import java.util.Objects;

public final class AudioPlaybackConfiguration implements Parcelable {
    public static final Creator<AudioPlaybackConfiguration> CREATOR = new Creator<AudioPlaybackConfiguration>() {
        public AudioPlaybackConfiguration createFromParcel(Parcel p) {
            return new AudioPlaybackConfiguration(p, null);
        }

        public AudioPlaybackConfiguration[] newArray(int size) {
            return new AudioPlaybackConfiguration[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final int PLAYER_PIID_INVALID = -1;
    public static final int PLAYER_STATE_IDLE = 1;
    public static final int PLAYER_STATE_PAUSED = 3;
    public static final int PLAYER_STATE_RELEASED = 0;
    public static final int PLAYER_STATE_STARTED = 2;
    public static final int PLAYER_STATE_STOPPED = 4;
    public static final int PLAYER_STATE_UNKNOWN = -1;
    public static final int PLAYER_TYPE_AAUDIO = 13;
    public static final int PLAYER_TYPE_EXTERNAL_PROXY = 15;
    public static final int PLAYER_TYPE_HW_SOURCE = 14;
    public static final int PLAYER_TYPE_JAM_AUDIOTRACK = 1;
    public static final int PLAYER_TYPE_JAM_MEDIAPLAYER = 2;
    public static final int PLAYER_TYPE_JAM_SOUNDPOOL = 3;
    public static final int PLAYER_TYPE_SLES_AUDIOPLAYER_BUFFERQUEUE = 11;
    public static final int PLAYER_TYPE_SLES_AUDIOPLAYER_URI_FD = 12;
    public static final int PLAYER_TYPE_UNKNOWN = -1;
    public static final int PLAYER_UPID_INVALID = -1;
    private static final String TAG = new String("AudioPlaybackConfiguration");
    public static PlayerDeathMonitor sPlayerDeathMonitor;
    private int mClientPid;
    private int mClientUid;
    private IPlayerShell mIPlayerShell;
    private String mPkgName;
    private AudioAttributes mPlayerAttr;
    private final int mPlayerIId;
    private int mPlayerState;
    private int mPlayerType;

    static final class IPlayerShell implements DeathRecipient {
        private IPlayer mIPlayer;
        final AudioPlaybackConfiguration mMonitor;

        IPlayerShell(AudioPlaybackConfiguration monitor, IPlayer iplayer) {
            this.mMonitor = monitor;
            this.mIPlayer = iplayer;
        }

        void monitorDeath() {
            try {
                this.mIPlayer.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                if (this.mMonitor != null) {
                    Log.w(AudioPlaybackConfiguration.TAG, "Could not link to client death for piid=" + this.mMonitor.mPlayerIId, e);
                } else {
                    Log.w(AudioPlaybackConfiguration.TAG, "Could not link to client death", e);
                }
            }
        }

        IPlayer getIPlayer() {
            return this.mIPlayer;
        }

        public void binderDied() {
            if (this.mMonitor != null) {
                this.mMonitor.playerDied();
            }
        }

        void release() {
            this.mIPlayer.asBinder().unlinkToDeath(this, 0);
        }
    }

    public interface PlayerDeathMonitor {
        void playerDeath(int i);
    }

    /* synthetic */ AudioPlaybackConfiguration(Parcel in, AudioPlaybackConfiguration -this1) {
        this(in);
    }

    private AudioPlaybackConfiguration(int piid) {
        this.mPlayerIId = piid;
        this.mIPlayerShell = null;
    }

    public AudioPlaybackConfiguration(PlayerIdCard pic, int piid, int uid, int pid) {
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
        if (this.mIPlayerShell != null) {
            this.mIPlayerShell.monitorDeath();
        }
    }

    public static AudioPlaybackConfiguration anonymizedCopy(AudioPlaybackConfiguration in) {
        AudioPlaybackConfiguration anonymCopy = new AudioPlaybackConfiguration(in.mPlayerIId);
        anonymCopy.mPlayerState = in.mPlayerState;
        anonymCopy.mPlayerAttr = new Builder().setUsage(in.mPlayerAttr.getUsage()).setContentType(in.mPlayerAttr.getContentType()).setFlags(in.mPlayerAttr.getFlags()).build();
        anonymCopy.mPlayerType = -1;
        anonymCopy.mClientUid = -1;
        anonymCopy.mClientPid = -1;
        anonymCopy.mIPlayerShell = null;
        return anonymCopy;
    }

    public AudioAttributes getAudioAttributes() {
        return this.mPlayerAttr;
    }

    public int getClientUid() {
        return this.mClientUid;
    }

    public int getClientPid() {
        return this.mClientPid;
    }

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

    public int getPlayerState() {
        return this.mPlayerState;
    }

    public int getPlayerInterfaceId() {
        return this.mPlayerIId;
    }

    public PlayerProxy getPlayerProxy() {
        return this.mIPlayerShell == null ? null : new PlayerProxy(this);
    }

    IPlayer getIPlayer() {
        return this.mIPlayerShell == null ? null : this.mIPlayerShell.getIPlayer();
    }

    public boolean handleAudioAttributesEvent(AudioAttributes attr) {
        boolean changed = attr.equals(this.mPlayerAttr) ^ 1;
        this.mPlayerAttr = attr;
        return changed;
    }

    public boolean handleStateEvent(int event) {
        boolean changed = this.mPlayerState != event;
        this.mPlayerState = event;
        if (event == 0 && this.mIPlayerShell != null) {
            this.mIPlayerShell.release();
        }
        return changed;
    }

    private void playerDied() {
        if (sPlayerDeathMonitor != null) {
            sPlayerDeathMonitor.playerDeath(this.mPlayerIId);
        }
    }

    public boolean isActive() {
        switch (this.mPlayerState) {
            case 2:
                return true;
            default:
                return false;
        }
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
        IInterface iInterface = null;
        dest.writeInt(this.mPlayerIId);
        dest.writeInt(this.mPlayerType);
        dest.writeInt(this.mClientUid);
        dest.writeInt(this.mClientPid);
        dest.writeInt(this.mPlayerState);
        this.mPlayerAttr.writeToParcel(dest, 0);
        if (this.mIPlayerShell != null) {
            iInterface = this.mIPlayerShell.getIPlayer();
        }
        dest.writeStrongInterface(iInterface);
    }

    private AudioPlaybackConfiguration(Parcel in) {
        this.mPlayerIId = in.readInt();
        this.mPlayerType = in.readInt();
        this.mClientUid = in.readInt();
        this.mClientPid = in.readInt();
        this.mPlayerState = in.readInt();
        this.mPlayerAttr = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(in);
        IPlayer p = Stub.asInterface(in.readStrongBinder());
        this.mIPlayerShell = p == null ? null : new IPlayerShell(null, p);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || ((o instanceof AudioPlaybackConfiguration) ^ 1) != 0) {
            return false;
        }
        AudioPlaybackConfiguration that = (AudioPlaybackConfiguration) o;
        if (this.mPlayerIId != that.mPlayerIId || this.mPlayerType != that.mPlayerType || this.mClientUid != that.mClientUid) {
            z = false;
        } else if (this.mClientPid != that.mClientPid) {
            z = false;
        }
        return z;
    }

    public static String toLogFriendlyPlayerType(int type) {
        switch (type) {
            case -1:
                return "unknown";
            case 1:
                return "android.media.AudioTrack";
            case 2:
                return "android.media.MediaPlayer";
            case 3:
                return "android.media.SoundPool";
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
                return "unknown player type - FIXME";
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
