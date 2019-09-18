package android.speech.tts;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface SynthesisCallback {

    @Retention(RetentionPolicy.SOURCE)
    public @interface SupportedAudioFormat {
    }

    int audioAvailable(byte[] bArr, int i, int i2);

    int done();

    void error();

    void error(int i);

    int getMaxBufferSize();

    boolean hasFinished();

    boolean hasStarted();

    int start(int i, int i2, int i3);

    void rangeStart(int markerInFrames, int start, int end) {
    }
}
