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
        /* class android.media.AudioPlaybackConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioPlaybackConfiguration createFromParcel(Parcel p) {
            return new AudioPlaybackConfiguration(p);
        }

        @Override // android.os.Parcelable.Creator
        public AudioPlaybackConfiguration[] newArray(int size) {
            return new AudioPlaybackConfiguration[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final int PLAYER_PIID_INVALID = -1;
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
    private static final String TAG = new String("AudioPlaybackConfiguration");
    public static PlayerDeathMonitor sPlayerDeathMonitor;
    private int mClientPid;
    private int mClientUid;
    private int mDevice;
    private IPlayerShell mIPlayerShell;
    private String mPkgName;
    private AudioAttributes mPlayerAttr;
    private final int mPlayerIId;
    private int mPlayerState;
    private int mPlayerType;

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
        this.mDevice = getDevice();
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public int getDevice() {
        return this.mDevice;
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
        AudioAttributes.Builder flags = new AudioAttributes.Builder().setUsage(in.mPlayerAttr.getUsage()).setContentType(in.mPlayerAttr.getContentType()).setFlags(in.mPlayerAttr.getFlags());
        int i = 1;
        if (in.mPlayerAttr.getAllowedCapturePolicy() != 1) {
            i = 3;
        }
        anonymCopy.mPlayerAttr = flags.setAllowedCapturePolicy(i).build();
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
        int i = this.mPlayerType;
        switch (i) {
            case 13:
            case 14:
            case 15:
                return -1;
            default:
                return i;
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

    public int getAudioSessionId() {
        try {
            return this.mIPlayerShell.getIPlayer().getAudioSessionId();
        } catch (RemoteException e) {
            Log.e(TAG, "getAudioSessionId failed: catch RemoteException!");
            return -2;
        }
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

    public void setDevice(int device) {
        this.mDevice = device;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playerDied() {
        PlayerDeathMonitor playerDeathMonitor = sPlayerDeathMonitor;
        if (playerDeathMonitor != null) {
            playerDeathMonitor.playerDeath(this.mPlayerIId);
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
        return Objects.hash(Integer.valueOf(this.mPlayerIId), Integer.valueOf(this.mPlayerType), Integer.valueOf(this.mClientUid), Integer.valueOf(this.mClientPid));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AudioPlaybackConfiguration)) {
            return false;
        }
        AudioPlaybackConfiguration that = (AudioPlaybackConfiguration) o;
        if (this.mPlayerIId == that.mPlayerIId && this.mPlayerType == that.mPlayerType && this.mClientUid == that.mClientUid && this.mClientPid == that.mClientPid) {
            return true;
        }
        return false;
    }

    static final class IPlayerShell implements IBinder.DeathRecipient {
        private volatile IPlayer mIPlayer;
        final AudioPlaybackConfiguration mMonitor;

        IPlayerShell(AudioPlaybackConfiguration monitor, IPlayer iplayer) {
            this.mMonitor = monitor;
            this.mIPlayer = iplayer;
        }

        /* access modifiers changed from: package-private */
        public synchronized void monitorDeath() {
            if (this.mIPlayer != null) {
                try {
                    this.mIPlayer.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    if (this.mMonitor != null) {
                        String str = AudioPlaybackConfiguration.TAG;
                        Log.w(str, "Could not link to client death for piid=" + this.mMonitor.mPlayerIId, e);
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

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            AudioPlaybackConfiguration audioPlaybackConfiguration = this.mMonitor;
            if (audioPlaybackConfiguration != null) {
                audioPlaybackConfiguration.playerDied();
            }
            this.mIPlayer.asBinder().unlinkToDeath(this, 0);
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

    public static String toLogFriendlyPlayerType(int type) {
        if (type == -1) {
            return "unknown";
        }
        if (type == 1) {
            return "android.media.AudioTrack";
        }
        if (type == 2) {
            return "android.media.MediaPlayer";
        }
        if (type == 3) {
            return "android.media.SoundPool";
        }
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

    public static String toLogFriendlyPlayerState(int state) {
        if (state == -1) {
            return "unknown";
        }
        if (state == 0) {
            return "released";
        }
        if (state == 1) {
            return "idle";
        }
        if (state == 2) {
            return "started";
        }
        if (state == 3) {
            return "paused";
        }
        if (state != 4) {
            return "unknown player state - FIXME";
        }
        return "stopped";
    }
}
