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
    private int mMode;
    private long mNative;
    private final Map<AudioStream, Long> mStreams;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.rtp.AudioGroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.rtp.AudioGroup.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.rtp.AudioGroup.<clinit>():void");
    }

    private native long nativeAdd(int i, int i2, String str, int i3, String str2, int i4, String str3);

    private native void nativeRemove(long j);

    private native void nativeSendDtmf(int i);

    private native void nativeSetMode(int i);

    public AudioGroup() {
        this.mMode = 0;
        this.mStreams = new HashMap();
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
        if (mode < 0 || mode > MODE_LAST) {
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
                Object[] objArr = new Object[MODE_LAST];
                objArr[0] = Integer.valueOf(codec.type);
                objArr[MODE_MUTED] = codec.rtpmap;
                objArr[MODE_NORMAL] = codec.fmtp;
                this.mStreams.put(stream, Long.valueOf(nativeAdd(stream.getMode(), stream.getSocket(), stream.getRemoteAddress().getHostAddress(), stream.getRemotePort(), String.format(Locale.US, "%d %s %s", objArr), stream.getDtmfType(), ActivityThread.currentOpPackageName())));
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
        AudioStream[] streams = getStreams();
        int length = streams.length;
        for (int i = 0; i < length; i += MODE_MUTED) {
            streams[i].join(null);
        }
    }

    protected void finalize() throws Throwable {
        nativeRemove(0);
        super.finalize();
    }
}
