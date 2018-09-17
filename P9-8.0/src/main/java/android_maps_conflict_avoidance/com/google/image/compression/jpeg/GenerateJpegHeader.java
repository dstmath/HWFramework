package android_maps_conflict_avoidance.com.google.image.compression.jpeg;

public class GenerateJpegHeader {
    private static final byte[] JPEG_STANDARD_HEADER = new byte[]{(byte) -1, (byte) -40, (byte) -1, (byte) -32, (byte) 0, (byte) 16, (byte) 74, (byte) 70, (byte) 73, (byte) 70, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) -1, (byte) -37, (byte) 0, (byte) 67, (byte) 0, (byte) 8, (byte) 6, (byte) 6, (byte) 7, (byte) 6, (byte) 5, (byte) 8, (byte) 7, (byte) 7, (byte) 7, (byte) 9, (byte) 9, (byte) 8, (byte) 10, (byte) 12, (byte) 20, (byte) 13, (byte) 12, (byte) 11, (byte) 11, (byte) 12, (byte) 25, (byte) 18, (byte) 19, (byte) 15, (byte) 20, (byte) 29, (byte) 26, (byte) 31, (byte) 30, (byte) 29, (byte) 26, (byte) 28, (byte) 28, (byte) 32, (byte) 36, (byte) 46, (byte) 39, (byte) 32, (byte) 34, (byte) 44, (byte) 35, (byte) 28, (byte) 28, (byte) 40, (byte) 55, (byte) 41, (byte) 44, (byte) 48, (byte) 49, (byte) 52, (byte) 52, (byte) 52, (byte) 31, (byte) 39, (byte) 57, (byte) 61, (byte) 56, (byte) 50, (byte) 60, (byte) 46, (byte) 51, (byte) 52, (byte) 50, (byte) -1, (byte) -37, (byte) 0, (byte) 67, (byte) 1, (byte) 9, (byte) 9, (byte) 9, (byte) 12, (byte) 11, (byte) 12, (byte) 24, (byte) 13, (byte) 13, (byte) 24, (byte) 50, (byte) 33, (byte) 28, (byte) 33, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) -1, (byte) -64, (byte) 0, (byte) 17, (byte) 8, (byte) 0, (byte) 64, (byte) 0, (byte) 64, (byte) 3, (byte) 1, (byte) 34, (byte) 0, (byte) 2, (byte) 17, (byte) 1, (byte) 3, (byte) 17, (byte) 1, (byte) -1, (byte) -60, (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 1, (byte) 5, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) -1, (byte) -60, (byte) 0, (byte) -75, (byte) 16, (byte) 0, (byte) 2, (byte) 1, (byte) 3, (byte) 3, (byte) 2, (byte) 4, (byte) 3, (byte) 5, (byte) 5, (byte) 4, (byte) 4, (byte) 0, (byte) 0, (byte) 1, (byte) 125, (byte) 1, (byte) 2, (byte) 3, (byte) 0, (byte) 4, (byte) 17, (byte) 5, (byte) 18, (byte) 33, (byte) 49, (byte) 65, (byte) 6, (byte) 19, (byte) 81, (byte) 97, (byte) 7, (byte) 34, (byte) 113, (byte) 20, (byte) 50, (byte) -127, (byte) -111, (byte) -95, (byte) 8, (byte) 35, (byte) 66, (byte) -79, (byte) -63, (byte) 21, (byte) 82, (byte) -47, (byte) -16, (byte) 36, (byte) 51, (byte) 98, (byte) 114, (byte) -126, (byte) 9, (byte) 10, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) -125, (byte) -124, (byte) -123, (byte) -122, (byte) -121, (byte) -120, (byte) -119, (byte) -118, (byte) -110, (byte) -109, (byte) -108, (byte) -107, (byte) -106, (byte) -105, (byte) -104, (byte) -103, (byte) -102, (byte) -94, (byte) -93, (byte) -92, (byte) -91, (byte) -90, (byte) -89, (byte) -88, (byte) -87, (byte) -86, (byte) -78, (byte) -77, (byte) -76, (byte) -75, (byte) -74, (byte) -73, (byte) -72, (byte) -71, (byte) -70, (byte) -62, (byte) -61, (byte) -60, (byte) -59, (byte) -58, (byte) -57, (byte) -56, (byte) -55, (byte) -54, (byte) -46, (byte) -45, (byte) -44, (byte) -43, (byte) -42, (byte) -41, (byte) -40, (byte) -39, (byte) -38, (byte) -31, (byte) -30, (byte) -29, (byte) -28, (byte) -27, (byte) -26, (byte) -25, (byte) -24, (byte) -23, (byte) -22, (byte) -15, (byte) -14, (byte) -13, (byte) -12, (byte) -11, (byte) -10, (byte) -9, (byte) -8, (byte) -7, (byte) -6, (byte) -1, (byte) -60, (byte) 0, (byte) 31, (byte) 1, (byte) 0, (byte) 3, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) -1, (byte) -60, (byte) 0, (byte) -75, (byte) 17, (byte) 0, (byte) 2, (byte) 1, (byte) 2, (byte) 4, (byte) 4, (byte) 3, (byte) 4, (byte) 7, (byte) 5, (byte) 4, (byte) 4, (byte) 0, (byte) 1, (byte) 2, (byte) 119, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 17, (byte) 4, (byte) 5, (byte) 33, (byte) 49, (byte) 6, (byte) 18, (byte) 65, (byte) 81, (byte) 7, (byte) 97, (byte) 113, (byte) 19, (byte) 34, (byte) 50, (byte) -127, (byte) 8, (byte) 20, (byte) 66, (byte) -111, (byte) -95, (byte) -79, (byte) -63, (byte) 9, (byte) 35, (byte) 51, (byte) 82, (byte) -16, (byte) 21, (byte) 98, (byte) 114, (byte) -47, (byte) 10, (byte) 22, (byte) 36, (byte) 52, (byte) -31, (byte) 37, (byte) -15, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) -126, (byte) -125, (byte) -124, (byte) -123, (byte) -122, (byte) -121, (byte) -120, (byte) -119, (byte) -118, (byte) -110, (byte) -109, (byte) -108, (byte) -107, (byte) -106, (byte) -105, (byte) -104, (byte) -103, (byte) -102, (byte) -94, (byte) -93, (byte) -92, (byte) -91, (byte) -90, (byte) -89, (byte) -88, (byte) -87, (byte) -86, (byte) -78, (byte) -77, (byte) -76, (byte) -75, (byte) -74, (byte) -73, (byte) -72, (byte) -71, (byte) -70, (byte) -62, (byte) -61, (byte) -60, (byte) -59, (byte) -58, (byte) -57, (byte) -56, (byte) -55, (byte) -54, (byte) -46, (byte) -45, (byte) -44, (byte) -43, (byte) -42, (byte) -41, (byte) -40, (byte) -39, (byte) -38, (byte) -30, (byte) -29, (byte) -28, (byte) -27, (byte) -26, (byte) -25, (byte) -24, (byte) -23, (byte) -22, (byte) -14, (byte) -13, (byte) -12, (byte) -11, (byte) -10, (byte) -9, (byte) -8, (byte) -7, (byte) -6, (byte) -1, (byte) -38, (byte) 0, (byte) 12, (byte) 3, (byte) 1, (byte) 0, (byte) 2, (byte) 17, (byte) 3, (byte) 17, (byte) 0, (byte) 63, (byte) 0};
    private static int JPEG_STANDARD_HEADER_CHROMINANCE_QUANT_OFFSET = 94;
    private static int JPEG_STANDARD_HEADER_LUMINANCE_QUANT_OFFSET = 25;
    private static int JPEG_STANDARD_HEADER_Y_X_OFFSET = 163;

    private GenerateJpegHeader() {
    }

    public static int getHeaderLength(int variant) {
        if (variant == 0) {
            return JPEG_STANDARD_HEADER.length;
        }
        throw new IllegalArgumentException("Unknown variant " + variant);
    }

    private static void copyQuantTable(byte[] dest, int off, int quantType, int quality, int qualityAlgorithm) {
        byte[] qtable = JpegUtil.getQuantTable(quantType, quality, qualityAlgorithm);
        System.arraycopy(qtable, 0, dest, off, qtable.length);
    }

    public static int generate(byte[] dest, int off, int variant, int width, int height, int quality, int qualityAlgorithm) {
        if (variant != 0) {
            throw new IllegalArgumentException("variant");
        } else if (quality < 24 || quality > 100) {
            throw new IllegalArgumentException("quality");
        } else if (qualityAlgorithm == 0 || qualityAlgorithm == 1) {
            int len = JPEG_STANDARD_HEADER.length;
            if (off + len <= dest.length) {
                System.arraycopy(JPEG_STANDARD_HEADER, 0, dest, off, len);
                int yxOffset = off + JPEG_STANDARD_HEADER_Y_X_OFFSET;
                dest[yxOffset] = (byte) ((byte) (width >> 8));
                dest[yxOffset + 1] = (byte) ((byte) (width & 255));
                dest[yxOffset + 2] = (byte) ((byte) (height >> 8));
                dest[yxOffset + 3] = (byte) ((byte) (height & 255));
                if (quality != 75) {
                    int cOff = off + JPEG_STANDARD_HEADER_CHROMINANCE_QUANT_OFFSET;
                    copyQuantTable(dest, off + JPEG_STANDARD_HEADER_LUMINANCE_QUANT_OFFSET, 0, quality, qualityAlgorithm);
                    copyQuantTable(dest, cOff, 1, quality, qualityAlgorithm);
                }
                return len;
            }
            throw new ArrayIndexOutOfBoundsException("dest");
        } else {
            throw new IllegalArgumentException("qualityAlgorithm: " + qualityAlgorithm);
        }
    }
}
