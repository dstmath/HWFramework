package android.net.apf;

public class ApfCapabilities {
    public final int apfPacketFormat;
    public final int apfVersionSupported;
    public final int maximumApfProgramSize;

    public ApfCapabilities(int apfVersionSupported, int maximumApfProgramSize, int apfPacketFormat) {
        this.apfVersionSupported = apfVersionSupported;
        this.maximumApfProgramSize = maximumApfProgramSize;
        this.apfPacketFormat = apfPacketFormat;
    }

    public String toString() {
        return String.format("%s{version: %d, maxSize: %d, format: %d}", new Object[]{getClass().getSimpleName(), Integer.valueOf(this.apfVersionSupported), Integer.valueOf(this.maximumApfProgramSize), Integer.valueOf(this.apfPacketFormat)});
    }
}
