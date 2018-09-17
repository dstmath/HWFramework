package android_maps_conflict_avoidance.com.google.image.compression.jpeg;

public class JpegHeaderParams {
    private int headerLength;
    private int height;
    private int quality;
    private int qualityAlgorithm;
    private int variant;
    private int width;

    public JpegHeaderParams(int variant, int width, int height, int quality, int qualityAlgorithm, int headerLength) {
        if (qualityAlgorithm == 0 || qualityAlgorithm == 1) {
            this.variant = variant;
            this.width = width;
            this.height = height;
            this.quality = quality;
            this.qualityAlgorithm = qualityAlgorithm;
            this.headerLength = headerLength;
            return;
        }
        throw new IllegalArgumentException("qualityAlgorithm = " + qualityAlgorithm);
    }

    public int getVariant() {
        return this.variant;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getQuality() {
        return this.quality;
    }

    public int getQualityAlgorithm() {
        return this.qualityAlgorithm;
    }
}
