package android.net.rtp;

import java.util.Arrays;

public class AudioCodec {
    public static final AudioCodec AMR = new AudioCodec(97, "AMR/8000", null);
    public static final AudioCodec GSM = new AudioCodec(3, "GSM/8000", null);
    public static final AudioCodec GSM_EFR = new AudioCodec(96, "GSM-EFR/8000", null);
    public static final AudioCodec PCMA = new AudioCodec(8, "PCMA/8000", null);
    public static final AudioCodec PCMU = new AudioCodec(0, "PCMU/8000", null);
    private static final AudioCodec[] sCodecs = {GSM_EFR, AMR, GSM, PCMU, PCMA};
    public final String fmtp;
    public final String rtpmap;
    public final int type;

    private AudioCodec(int type2, String rtpmap2, String fmtp2) {
        this.type = type2;
        this.rtpmap = rtpmap2;
        this.fmtp = fmtp2;
    }

    public static AudioCodec[] getCodecs() {
        AudioCodec[] audioCodecArr = sCodecs;
        return (AudioCodec[]) Arrays.copyOf(audioCodecArr, audioCodecArr.length);
    }

    public static AudioCodec getCodec(int type2, String rtpmap2, String fmtp2) {
        if (type2 < 0 || type2 > 127) {
            return null;
        }
        AudioCodec hint = null;
        int i = 0;
        if (rtpmap2 != null) {
            String clue = rtpmap2.trim().toUpperCase();
            AudioCodec[] audioCodecArr = sCodecs;
            int length = audioCodecArr.length;
            while (true) {
                if (i >= length) {
                    break;
                }
                AudioCodec codec = audioCodecArr[i];
                if (clue.startsWith(codec.rtpmap)) {
                    String channels = clue.substring(codec.rtpmap.length());
                    if (channels.length() == 0 || channels.equals("/1")) {
                        hint = codec;
                    }
                } else {
                    i++;
                }
            }
        } else if (type2 < 96) {
            AudioCodec[] audioCodecArr2 = sCodecs;
            int length2 = audioCodecArr2.length;
            while (true) {
                if (i >= length2) {
                    break;
                }
                AudioCodec codec2 = audioCodecArr2[i];
                if (type2 == codec2.type) {
                    hint = codec2;
                    rtpmap2 = codec2.rtpmap;
                    break;
                }
                i++;
            }
        }
        if (hint == null) {
            return null;
        }
        if (hint == AMR && fmtp2 != null) {
            String clue2 = fmtp2.toLowerCase();
            if (clue2.contains("crc=1") || clue2.contains("robust-sorting=1") || clue2.contains("interleaving=")) {
                return null;
            }
        }
        return new AudioCodec(type2, rtpmap2, fmtp2);
    }
}
