package android.net.apf;

public class ApfCapabilities {
    public final int apfPacketFormat;
    public final int apfVersionSupported;
    public final int maximumApfProgramSize;

    public ApfCapabilities(int apfVersionSupported2, int maximumApfProgramSize2, int apfPacketFormat2) {
        this.apfVersionSupported = apfVersionSupported2;
        this.maximumApfProgramSize = maximumApfProgramSize2;
        this.apfPacketFormat = apfPacketFormat2;
    }

    public String toString() {
        return String.format("%s{version: %d, maxSize: %d, format: %d}", new Object[]{getClass().getSimpleName(), Integer.valueOf(this.apfVersionSupported), Integer.valueOf(this.maximumApfProgramSize), Integer.valueOf(this.apfPacketFormat)});
    }

    public boolean hasDataAccess() {
        return this.apfVersionSupported >= 4;
    }
}
