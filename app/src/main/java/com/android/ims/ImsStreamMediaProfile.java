package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImsStreamMediaProfile implements Parcelable {
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
    public static final Creator<ImsStreamMediaProfile> CREATOR = null;
    public static final int DIRECTION_INACTIVE = 0;
    public static final int DIRECTION_INVALID = -1;
    public static final int DIRECTION_RECEIVE = 1;
    public static final int DIRECTION_SEND = 2;
    public static final int DIRECTION_SEND_RECEIVE = 3;
    private static final String TAG = "ImsStreamMediaProfile";
    public static final int VIDEO_QUALITY_NONE = 0;
    public static final int VIDEO_QUALITY_QCIF = 1;
    public static final int VIDEO_QUALITY_QVGA_LANDSCAPE = 2;
    public static final int VIDEO_QUALITY_QVGA_PORTRAIT = 4;
    public static final int VIDEO_QUALITY_VGA_LANDSCAPE = 8;
    public static final int VIDEO_QUALITY_VGA_PORTRAIT = 16;
    public int mAudioDirection;
    public int mAudioQuality;
    public int mVideoDirection;
    public int mVideoQuality;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsStreamMediaProfile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsStreamMediaProfile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsStreamMediaProfile.<clinit>():void");
    }

    public ImsStreamMediaProfile(Parcel in) {
        readFromParcel(in);
    }

    public ImsStreamMediaProfile() {
        this.mAudioQuality = VIDEO_QUALITY_NONE;
        this.mAudioDirection = DIRECTION_SEND_RECEIVE;
        this.mVideoQuality = VIDEO_QUALITY_NONE;
        this.mVideoDirection = DIRECTION_INVALID;
    }

    public ImsStreamMediaProfile(int audioQuality, int audioDirection, int videoQuality, int videoDirection) {
        this.mAudioQuality = audioQuality;
        this.mAudioDirection = audioDirection;
        this.mVideoQuality = videoQuality;
        this.mVideoDirection = videoDirection;
    }

    public void copyFrom(ImsStreamMediaProfile profile) {
        this.mAudioQuality = profile.mAudioQuality;
        this.mAudioDirection = profile.mAudioDirection;
        this.mVideoQuality = profile.mVideoQuality;
        this.mVideoDirection = profile.mVideoDirection;
    }

    public String toString() {
        return "{ audioQuality=" + this.mAudioQuality + ", audioDirection=" + this.mAudioDirection + ", videoQuality=" + this.mVideoQuality + ", videoDirection=" + this.mVideoDirection + " }";
    }

    public int describeContents() {
        return VIDEO_QUALITY_NONE;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mAudioQuality);
        out.writeInt(this.mAudioDirection);
        out.writeInt(this.mVideoQuality);
        out.writeInt(this.mVideoDirection);
    }

    private void readFromParcel(Parcel in) {
        this.mAudioQuality = in.readInt();
        this.mAudioDirection = in.readInt();
        this.mVideoQuality = in.readInt();
        this.mVideoDirection = in.readInt();
    }
}
