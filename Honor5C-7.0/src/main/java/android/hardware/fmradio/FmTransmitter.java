package android.hardware.fmradio;

public class FmTransmitter extends FmTransceiver {
    public static final int RDS_GRPS_TX_PAUSE = 0;
    public static final int RDS_GRPS_TX_RESUME = 1;
    public static final int RDS_GRPS_TX_STOP = 2;

    public class FmPSFeatures {
        public int maxPSCharacters;
        public int maxPSStringRepeatCount;
    }

    public interface TransmitterCallbacks {
        void onRDSGroupsAvailable();

        void onRDSGroupsComplete();

        void onTuneFrequencyChange(int i);
    }

    public FmTransmitter() {
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public FmTransmitter(String path, FmRxEvCallbacksAdaptor callbacks) {
        acquire(path);
        registerTransmitClient(callbacks);
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public boolean enable(FmConfig configSettings) {
        boolean status = super.enable(configSettings, RDS_GRPS_TX_STOP);
        return true;
    }

    public boolean disable() {
        return super.disable();
    }

    public boolean reset() {
        return unregisterTransmitClient();
    }

    public FmPSFeatures getPSFeatures() {
        FmPSFeatures psFeatures = new FmPSFeatures();
        psFeatures.maxPSCharacters = RDS_GRPS_TX_PAUSE;
        psFeatures.maxPSStringRepeatCount = RDS_GRPS_TX_PAUSE;
        return psFeatures;
    }

    public boolean setPSInfo(String[] psStr, int pty, long repeatCount) {
        return false;
    }

    public boolean stopPSInfo() {
        return false;
    }

    public boolean setRTInfo(String rtStr) {
        return false;
    }

    public boolean stopRTInfo() {
        return false;
    }

    public boolean setPSRTProgramType(int pty) {
        return false;
    }

    public int getRdsGroupBufSize() {
        return RDS_GRPS_TX_PAUSE;
    }

    public int transmitRdsGroups(byte[] rdsGroups, long numGroupsToTransmit) {
        return -1;
    }

    public boolean transmitRdsGroupControl(int ctrlCmd) {
        return false;
    }

    boolean isAntennaAvailable() {
        return true;
    }
}
