package ohos.media.audio;

public class AudioInterrupt {
    public static final int INTERRUPT_HINT_DUCK = 4;
    public static final int INTERRUPT_HINT_NONE = 0;
    public static final int INTERRUPT_HINT_PAUSE = 2;
    public static final int INTERRUPT_HINT_RESUME = 1;
    public static final int INTERRUPT_HINT_STOP = 3;
    public static final int INTERRUPT_HINT_UNDUCK = 5;
    public static final int INTERRUPT_TYPE_BEGIN = 1;
    public static final int INTERRUPT_TYPE_END = 2;
    private AudioStreamInfo audioStreamInfo;
    private InterruptListener interruptListener;
    private boolean pauseWhenDucked = false;

    public interface InterruptListener {
        void onInterrupt(int i, int i2);
    }

    public void setInterruptListener(InterruptListener interruptListener2) {
        this.interruptListener = interruptListener2;
    }

    public void setStreamInfo(AudioStreamInfo audioStreamInfo2) {
        this.audioStreamInfo = audioStreamInfo2;
    }

    public InterruptListener getInterruptListener() {
        return this.interruptListener;
    }

    public AudioStreamInfo getStreamInfo() {
        return this.audioStreamInfo;
    }

    public boolean isPauseWhenDucked() {
        return this.pauseWhenDucked;
    }

    public void setPauseWhenDucked(boolean z) {
        this.pauseWhenDucked = z;
    }
}
