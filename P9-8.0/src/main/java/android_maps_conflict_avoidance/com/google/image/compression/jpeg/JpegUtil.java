package android_maps_conflict_avoidance.com.google.image.compression.jpeg;

import com.google.android.maps.MapView.LayoutParams;

public class JpegUtil {
    private static final byte[][] JPEG_QUANT_TABLES;
    private static final int[] imageIoScaleFactor = new int[]{-1, 1677721601, 838860801, 559240577, 419430401, 335544321, 279620289, 239674513, 209715201, 186413505, 167772161, 152520145, 139810145, 129055513, 119837257, 111848105, 104857601, 98689505, 93206753, 88301137, 83886081, 79891505, 76260073, 72944417, 69905073, 67108865, 64527757, 62137837, 59918629, 57852473, 55924053, 54120053, 52428801, 50840049, 49344753, 47934905, 46603377, 45343829, 44150569, 43018505, 41943041, 40920041, 39945753, 39016781, 38130037, 37282705, 36472209, 35696205, 34952537, 34239217, 33554433, 32883345, 32212257, 31541169, 30870077, 30198989, 29527901, 28856813, 28185725, 27514637, 26843545, 26172457, 25501369, 24830281, 24159193, 23488105, 22817013, 22145925, 21474837, 20803749, 20132661, 19461573, 18790481, 18119393, 17448305, 16777217, 16106129, 15435041, 14763953, 14092861, 13421773, 12750685, 12079597, 11408509, 10737421, 10066329, 9395241, 8724153, 8053065, 7381977, 6710889, 6039797, 5368709, 4697621, 4026533, 3355445, 2684357, 2013265, 1342177, 671089, 1};

    static {
        r0 = new byte[2][];
        r0[0] = new byte[]{(byte) 16, (byte) 11, (byte) 12, (byte) 14, (byte) 12, (byte) 10, (byte) 16, (byte) 14, (byte) 13, (byte) 14, (byte) 18, (byte) 17, (byte) 16, (byte) 19, (byte) 24, (byte) 40, (byte) 26, (byte) 24, (byte) 22, (byte) 22, (byte) 24, (byte) 49, (byte) 35, (byte) 37, (byte) 29, (byte) 40, (byte) 58, (byte) 51, (byte) 61, (byte) 60, (byte) 57, (byte) 51, (byte) 56, (byte) 55, (byte) 64, (byte) 72, (byte) 92, (byte) 78, (byte) 64, (byte) 68, (byte) 87, (byte) 69, (byte) 55, (byte) 56, (byte) 80, (byte) 109, (byte) 81, (byte) 87, (byte) 95, (byte) 98, (byte) 103, (byte) 104, (byte) 103, (byte) 62, (byte) 77, (byte) 113, (byte) 121, (byte) 112, (byte) 100, (byte) 120, (byte) 92, (byte) 101, (byte) 103, (byte) 99};
        r0[1] = new byte[]{(byte) 17, (byte) 18, (byte) 18, (byte) 24, (byte) 21, (byte) 24, (byte) 47, (byte) 26, (byte) 26, (byte) 47, (byte) 99, (byte) 66, (byte) 56, (byte) 66, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99, (byte) 99};
        JPEG_QUANT_TABLES = r0;
    }

    private JpegUtil() {
    }

    public static byte getScaledQuantizationFactor(int q, int quality, int qualityAlgorithm) {
        int val;
        switch (qualityAlgorithm) {
            case LayoutParams.MODE_MAP /*0*/:
                if (q != 99 || quality != 36) {
                    val = (int) ((((((long) q) * ((long) imageIoScaleFactor[quality])) / 16777216) + 1) / 2);
                    break;
                }
                val = 138;
                break;
                break;
            case 1:
                int iscale;
                if (quality >= 50) {
                    iscale = Math.max(200 - (quality * 2), 0);
                } else {
                    iscale = Math.min(5000 / quality, 5000);
                }
                val = ((q * iscale) + 50) / 100;
                break;
            default:
                throw new IllegalArgumentException("qualityAlgorithm");
        }
        if (val < 1) {
            val = 1;
        } else if (val > 255) {
            val = 255;
        }
        return (byte) val;
    }

    public static synchronized byte[] getQuantTable(int quantType, int quality, int qualityAlgorithm) {
        byte[] qtable;
        synchronized (JpegUtil.class) {
            int index = ((quantType * 154) + (qualityAlgorithm * 77)) + (quality - 24);
            qtable = new byte[64];
            byte[] rawTable = JPEG_QUANT_TABLES[quantType];
            for (int j = 0; j < 64; j++) {
                qtable[j] = (byte) getScaledQuantizationFactor(rawTable[j] & 255, quality, qualityAlgorithm);
            }
        }
        return qtable;
    }

    static void prependStandardHeader(byte[] src, int soff, int len, byte[] dst, int doff, JpegHeaderParams params) {
        int variant = params.getVariant();
        int width = params.getWidth();
        int height = params.getHeight();
        int quality = params.getQuality();
        int qualityAlgorithm = params.getQualityAlgorithm();
        if (variant == 0) {
            System.arraycopy(src, soff, dst, doff + GenerateJpegHeader.getHeaderLength(variant), len);
            GenerateJpegHeader.generate(dst, doff, variant, width, height, quality, qualityAlgorithm);
            return;
        }
        throw new IllegalArgumentException("variant");
    }

    public static byte[] uncompactJpeg(byte[] compactJpegData, int off, int len) {
        if (compactJpegData[off] == (byte) -1 && compactJpegData[off + 1] == (byte) -40) {
            Object data = new byte[len];
            System.arraycopy(compactJpegData, off, data, 0, len);
            return data;
        } else if (compactJpegData[off] == (byte) 67 && compactJpegData[off + 1] == (byte) 74 && compactJpegData[off + 2] == (byte) 80 && compactJpegData[off + 3] == (byte) 71) {
            int variant = compactJpegData[off + 4] & 255;
            int width = ((compactJpegData[off + 5] & 255) << 8) | (compactJpegData[off + 6] & 255);
            int height = ((compactJpegData[off + 7] & 255) << 8) | (compactJpegData[off + 8] & 255);
            int quality = compactJpegData[off + 9] & 255;
            int qualityAlgorithm = compactJpegData[off + 10] & 255;
            try {
                int hlen = GenerateJpegHeader.getHeaderLength(variant);
                byte[] jpegData = new byte[((hlen + len) - 11)];
                prependStandardHeader(compactJpegData, off + 11, len - 11, jpegData, 0, new JpegHeaderParams(variant, width, height, quality, qualityAlgorithm, hlen));
                return jpegData;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown variant " + variant);
            }
        } else {
            throw new IllegalArgumentException("Input is not in compact JPEG format");
        }
    }

    public static byte[] uncompactJpeg(byte[] compactJpegData) {
        return uncompactJpeg(compactJpegData, 0, compactJpegData.length);
    }
}
