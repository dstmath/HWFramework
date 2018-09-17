package android.net.rtp;

import java.util.Arrays;

public class AudioCodec {
    public static final AudioCodec AMR = new AudioCodec(97, "AMR/8000", null);
    public static final AudioCodec GSM = new AudioCodec(3, "GSM/8000", null);
    public static final AudioCodec GSM_EFR = new AudioCodec(96, "GSM-EFR/8000", null);
    public static final AudioCodec PCMA = new AudioCodec(8, "PCMA/8000", null);
    public static final AudioCodec PCMU = new AudioCodec(0, "PCMU/8000", null);
    private static final AudioCodec[] sCodecs = new AudioCodec[]{GSM_EFR, AMR, GSM, PCMU, PCMA};
    public final String fmtp;
    public final String rtpmap;
    public final int type;

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
