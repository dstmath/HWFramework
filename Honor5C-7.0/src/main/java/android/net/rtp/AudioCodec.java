package android.net.rtp;

import java.util.Arrays;

public class AudioCodec {
    public static final AudioCodec AMR = null;
    public static final AudioCodec GSM = null;
    public static final AudioCodec GSM_EFR = null;
    public static final AudioCodec PCMA = null;
    public static final AudioCodec PCMU = null;
    private static final AudioCodec[] sCodecs = null;
    public final String fmtp;
    public final String rtpmap;
    public final int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.rtp.AudioCodec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.rtp.AudioCodec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.rtp.AudioCodec.<clinit>():void");
    }

    private AudioCodec(int type, String rtpmap, String fmtp) {
        this.type = type;
        this.rtpmap = rtpmap;
        this.fmtp = fmtp;
    }

    public static AudioCodec[] getCodecs() {
        return (AudioCodec[]) Arrays.copyOf(sCodecs, sCodecs.length);
    }

    public static AudioCodec getCodec(int type, String rtpmap, String fmtp) {
        int i = 0;
        if (type < 0 || type > 127) {
            return null;
        }
        String clue;
        AudioCodec hint = null;
        AudioCodec[] audioCodecArr;
        int length;
        AudioCodec codec;
        if (rtpmap != null) {
            clue = rtpmap.trim().toUpperCase();
            audioCodecArr = sCodecs;
            length = audioCodecArr.length;
            while (i < length) {
                codec = audioCodecArr[i];
                if (clue.startsWith(codec.rtpmap)) {
                    String channels = clue.substring(codec.rtpmap.length());
                    if (channels.length() == 0 || channels.equals("/1")) {
                        hint = codec;
                    }
                } else {
                    i++;
                }
            }
        } else if (type < 96) {
            audioCodecArr = sCodecs;
            length = audioCodecArr.length;
            while (i < length) {
                codec = audioCodecArr[i];
                if (type == codec.type) {
                    hint = codec;
                    rtpmap = codec.rtpmap;
                    break;
                }
                i++;
            }
        }
        if (hint == null) {
            return null;
        }
        if (hint == AMR && fmtp != null) {
            clue = fmtp.toLowerCase();
            if (clue.contains("crc=1") || clue.contains("robust-sorting=1") || clue.contains("interleaving=")) {
                return null;
            }
        }
        return new AudioCodec(type, rtpmap, fmtp);
    }
}
