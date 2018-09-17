package android_maps_conflict_avoidance.com.google.image.compression.jpeg;

public class GenerateJpegHeader {
    private static final byte[] JPEG_STANDARD_HEADER = null;
    private static int JPEG_STANDARD_HEADER_CHROMINANCE_QUANT_OFFSET;
    private static int JPEG_STANDARD_HEADER_LUMINANCE_QUANT_OFFSET;
    private static int JPEG_STANDARD_HEADER_Y_X_OFFSET;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.image.compression.jpeg.GenerateJpegHeader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.image.compression.jpeg.GenerateJpegHeader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.image.compression.jpeg.GenerateJpegHeader.<clinit>():void");
    }

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
