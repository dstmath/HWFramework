package android.telephony.ims;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class ImsStreamMediaProfile implements Parcelable {
    public static final int AUDIO_QUALITY_AMR = 1;
    public static final int AUDIO_QUALITY_AMR_WB = 2;
    public static final int AUDIO_QUALITY_EVRC = 4;
    public static final int AUDIO_QUALITY_EVRC_B = 5;
    public static final int AUDIO_QUALITY_EVRC_NW = 7;
    public static final int AUDIO_QUALITY_EVRC_WB = 6;
    public static final int AUDIO_QUALITY_EVS_FB = 20;
    public static final int AUDIO_QUALITY_EVS_NB = 17;
    public static final int AUDIO_QUALITY_EVS_SWB = 19;
    public static final int AUDIO_QUALITY_EVS_WB = 18;
    public static final int AUDIO_QUALITY_G711A = 13;
    public static final int AUDIO_QUALITY_G711AB = 15;
    public static final int AUDIO_QUALITY_G711U = 11;
    public static final int AUDIO_QUALITY_G722 = 14;
    public static final int AUDIO_QUALITY_G723 = 12;
    public static final int AUDIO_QUALITY_G729 = 16;
    public static final int AUDIO_QUALITY_GSM_EFR = 8;
    public static final int AUDIO_QUALITY_GSM_FR = 9;
    public static final int AUDIO_QUALITY_GSM_HR = 10;
    public static final int AUDIO_QUALITY_NONE = 0;
    public static final int AUDIO_QUALITY_QCELP13K = 3;
    public static final Parcelable.Creator<ImsStreamMediaProfile> CREATOR = new Parcelable.Creator<ImsStreamMediaProfile>() {
        /* class android.telephony.ims.ImsStreamMediaProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImsStreamMediaProfile createFromParcel(Parcel in) {
            return new ImsStreamMediaProfile(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImsStreamMediaProfile[] newArray(int size) {
            return new ImsStreamMediaProfile[size];
        }
    };
    public static final int DIRECTION_INACTIVE = 0;
    public static final int DIRECTION_INVALID = -1;
    public static final int DIRECTION_RECEIVE = 1;
    public static final int DIRECTION_SEND = 2;
    public static final int DIRECTION_SEND_RECEIVE = 3;
    public static final int RTT_MODE_DISABLED = 0;
    public static final int RTT_MODE_FULL = 1;
    private static final String TAG = "ImsStreamMediaProfile";
    public static final int VIDEO_QUALITY_NONE = 0;
    public static final int VIDEO_QUALITY_QCIF = 1;
    public static final int VIDEO_QUALITY_QVGA_LANDSCAPE = 2;
    public static final int VIDEO_QUALITY_QVGA_PORTRAIT = 4;
    public static final int VIDEO_QUALITY_VGA_LANDSCAPE = 8;
    public static final int VIDEO_QUALITY_VGA_PORTRAIT = 16;
    @UnsupportedAppUsage
    public int mAudioDirection;
    @UnsupportedAppUsage
    public int mAudioQuality;
    public boolean mIsReceivingRttAudio;
    public int mRttMode;
    @UnsupportedAppUsage
    public int mVideoDirection;
    public int mVideoQuality;

    public ImsStreamMediaProfile(Parcel in) {
        this.mIsReceivingRttAudio = false;
        readFromParcel(in);
    }

    public ImsStreamMediaProfile(int audioQuality, int audioDirection, int videoQuality, int videoDirection, int rttMode) {
        this.mIsReceivingRttAudio = false;
        this.mAudioQuality = audioQuality;
        this.mAudioDirection = audioDirection;
        this.mVideoQuality = videoQuality;
        this.mVideoDirection = videoDirection;
        this.mRttMode = rttMode;
    }

    @UnsupportedAppUsage
    public ImsStreamMediaProfile() {
        this.mIsReceivingRttAudio = false;
        this.mAudioQuality = 0;
        this.mAudioDirection = 3;
        this.mVideoQuality = 0;
        this.mVideoDirection = -1;
        this.mRttMode = 0;
    }

    public ImsStreamMediaProfile(int audioQuality, int audioDirection, int videoQuality, int videoDirection) {
        this.mIsReceivingRttAudio = false;
        this.mAudioQuality = audioQuality;
        this.mAudioDirection = audioDirection;
        this.mVideoQuality = videoQuality;
        this.mVideoDirection = videoDirection;
    }

    public ImsStreamMediaProfile(int rttMode) {
        this.mIsReceivingRttAudio = false;
        this.mRttMode = rttMode;
    }

    public void copyFrom(ImsStreamMediaProfile profile) {
        this.mAudioQuality = profile.mAudioQuality;
        this.mAudioDirection = profile.mAudioDirection;
        this.mVideoQuality = profile.mVideoQuality;
        this.mVideoDirection = profile.mVideoDirection;
        this.mRttMode = profile.mRttMode;
    }

    public String toString() {
        return "{ audioQuality=" + this.mAudioQuality + ", audioDirection=" + this.mAudioDirection + ", videoQuality=" + this.mVideoQuality + ", videoDirection=" + this.mVideoDirection + ", rttMode=" + this.mRttMode + ", hasRttAudioSpeech=" + this.mIsReceivingRttAudio + " }";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mAudioQuality);
        out.writeInt(this.mAudioDirection);
        out.writeInt(this.mVideoQuality);
        out.writeInt(this.mVideoDirection);
        out.writeInt(this.mRttMode);
        out.writeBoolean(this.mIsReceivingRttAudio);
    }

    private void readFromParcel(Parcel in) {
        this.mAudioQuality = in.readInt();
        this.mAudioDirection = in.readInt();
        this.mVideoQuality = in.readInt();
        this.mVideoDirection = in.readInt();
        this.mRttMode = in.readInt();
        this.mIsReceivingRttAudio = in.readBoolean();
    }

    public boolean isRttCall() {
        return this.mRttMode == 1;
    }

    public void setRttMode(int rttMode) {
        this.mRttMode = rttMode;
    }

    public void setReceivingRttAudio(boolean audioOn) {
        this.mIsReceivingRttAudio = audioOn;
    }

    public int getAudioQuality() {
        return this.mAudioQuality;
    }

    public int getAudioDirection() {
        return this.mAudioDirection;
    }

    public int getVideoQuality() {
        return this.mVideoQuality;
    }

    public int getVideoDirection() {
        return this.mVideoDirection;
    }

    public int getRttMode() {
        return this.mRttMode;
    }

    public boolean isReceivingRttAudio() {
        return this.mIsReceivingRttAudio;
    }
}
