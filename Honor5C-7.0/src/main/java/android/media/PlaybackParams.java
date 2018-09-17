package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;

public final class PlaybackParams implements Parcelable {
    public static final int AUDIO_FALLBACK_MODE_DEFAULT = 0;
    public static final int AUDIO_FALLBACK_MODE_FAIL = 2;
    public static final int AUDIO_FALLBACK_MODE_MUTE = 1;
    public static final int AUDIO_STRETCH_MODE_DEFAULT = 0;
    public static final int AUDIO_STRETCH_MODE_VOICE = 1;
    public static final Creator<PlaybackParams> CREATOR = null;
    private static final int SET_AUDIO_FALLBACK_MODE = 4;
    private static final int SET_AUDIO_STRETCH_MODE = 8;
    private static final int SET_PITCH = 2;
    private static final int SET_SPEED = 1;
    private int mAudioFallbackMode;
    private int mAudioStretchMode;
    private float mPitch;
    private int mSet;
    private float mSpeed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.PlaybackParams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.PlaybackParams.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.PlaybackParams.<clinit>():void");
    }

    public PlaybackParams() {
        this.mSet = AUDIO_STRETCH_MODE_DEFAULT;
        this.mAudioFallbackMode = AUDIO_STRETCH_MODE_DEFAULT;
        this.mAudioStretchMode = AUDIO_STRETCH_MODE_DEFAULT;
        this.mPitch = Engine.DEFAULT_VOLUME;
        this.mSpeed = Engine.DEFAULT_VOLUME;
    }

    private PlaybackParams(Parcel in) {
        this.mSet = AUDIO_STRETCH_MODE_DEFAULT;
        this.mAudioFallbackMode = AUDIO_STRETCH_MODE_DEFAULT;
        this.mAudioStretchMode = AUDIO_STRETCH_MODE_DEFAULT;
        this.mPitch = Engine.DEFAULT_VOLUME;
        this.mSpeed = Engine.DEFAULT_VOLUME;
        this.mSet = in.readInt();
        this.mAudioFallbackMode = in.readInt();
        this.mAudioStretchMode = in.readInt();
        this.mPitch = in.readFloat();
        if (this.mPitch < 0.0f) {
            this.mPitch = 0.0f;
        }
        this.mSpeed = in.readFloat();
    }

    public PlaybackParams allowDefaults() {
        this.mSet |= 15;
        return this;
    }

    public PlaybackParams setAudioFallbackMode(int audioFallbackMode) {
        this.mAudioFallbackMode = audioFallbackMode;
        this.mSet |= SET_AUDIO_FALLBACK_MODE;
        return this;
    }

    public int getAudioFallbackMode() {
        if ((this.mSet & SET_AUDIO_FALLBACK_MODE) != 0) {
            return this.mAudioFallbackMode;
        }
        throw new IllegalStateException("audio fallback mode not set");
    }

    public PlaybackParams setAudioStretchMode(int audioStretchMode) {
        this.mAudioStretchMode = audioStretchMode;
        this.mSet |= SET_AUDIO_STRETCH_MODE;
        return this;
    }

    public int getAudioStretchMode() {
        if ((this.mSet & SET_AUDIO_STRETCH_MODE) != 0) {
            return this.mAudioStretchMode;
        }
        throw new IllegalStateException("audio stretch mode not set");
    }

    public PlaybackParams setPitch(float pitch) {
        if (pitch < 0.0f) {
            throw new IllegalArgumentException("pitch must not be negative");
        }
        this.mPitch = pitch;
        this.mSet |= SET_PITCH;
        return this;
    }

    public float getPitch() {
        if ((this.mSet & SET_PITCH) != 0) {
            return this.mPitch;
        }
        throw new IllegalStateException("pitch not set");
    }

    public PlaybackParams setSpeed(float speed) {
        this.mSpeed = speed;
        this.mSet |= SET_SPEED;
        return this;
    }

    public float getSpeed() {
        if ((this.mSet & SET_SPEED) != 0) {
            return this.mSpeed;
        }
        throw new IllegalStateException("speed not set");
    }

    public int describeContents() {
        return AUDIO_STRETCH_MODE_DEFAULT;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSet);
        dest.writeInt(this.mAudioFallbackMode);
        dest.writeInt(this.mAudioStretchMode);
        dest.writeFloat(this.mPitch);
        dest.writeFloat(this.mSpeed);
    }
}
