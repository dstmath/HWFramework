package android.net.rtp;

import android.app.ActivityThread;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AudioGroup {
    public static final int MODE_ECHO_SUPPRESSION = 3;
    private static final int MODE_LAST = 3;
    public static final int MODE_MUTED = 1;
    public static final int MODE_NORMAL = 2;
    public static final int MODE_ON_HOLD = 0;
    private int mMode = 0;
    private long mNative;
    private final Map<AudioStream, Long> mStreams = new HashMap();

    private native long nativeAdd(int i, int i2, String str, int i3, String str2, int i4, String str3);

    private native void nativeRemove(long j);

    private native void nativeSendDtmf(int i);

    private native void nativeSetMode(int i);

    static {
        System.loadLibrary("rtp_jni");
    }

    public AudioStream[] getStreams() {
        AudioStream[] audioStreamArr;
        synchronized (this) {
            audioStreamArr = (AudioStream[]) this.mStreams.keySet().toArray(new AudioStream[this.mStreams.size()]);
        }
        return audioStreamArr;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMode(int mode) {
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Invalid mode");
        }
        synchronized (this) {
            nativeSetMode(mode);
            this.mMode = mode;
        }
    }

    synchronized void add(AudioStream stream) {
        if (!this.mStreams.containsKey(stream)) {
            try {
                AudioCodec codec = stream.getCodec();
                this.mStreams.put(stream, Long.valueOf(nativeAdd(stream.getMode(), stream.getSocket(), stream.getRemoteAddress().getHostAddress(), stream.getRemotePort(), String.format(Locale.US, "%d %s %s", new Object[]{Integer.valueOf(codec.type), codec.rtpmap, codec.fmtp}), stream.getDtmfType(), ActivityThread.currentOpPackageName())));
            } catch (NullPointerException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    synchronized void remove(AudioStream stream) {
        Long id = (Long) this.mStreams.remove(stream);
        if (id != null) {
            nativeRemove(id.longValue());
        }
    }

    public void sendDtmf(int event) {
        if (event < 0 || event > 15) {
            throw new IllegalArgumentException("Invalid event");
        }
        synchronized (this) {
            nativeSendDtmf(event);
        }
    }

    public void clear() {
        for (AudioStream stream : getStreams()) {
            stream.join(null);
        }
    }

    protected void finalize() throws Throwable {
        nativeRemove(0);
        super.finalize();
    }
}
