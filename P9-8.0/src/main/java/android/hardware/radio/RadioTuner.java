package android.hardware.radio;

import android.hardware.radio.RadioManager.BandConfig;
import android.hardware.radio.RadioManager.ProgramInfo;
import java.util.List;

public abstract class RadioTuner {
    public static final int DIRECTION_DOWN = 1;
    public static final int DIRECTION_UP = 0;
    public static final int ERROR_CANCELLED = 2;
    public static final int ERROR_CONFIG = 4;
    public static final int ERROR_HARDWARE_FAILURE = 0;
    public static final int ERROR_SCAN_TIMEOUT = 3;
    public static final int ERROR_SERVER_DIED = 1;

    public static abstract class Callback {
        public void onError(int status) {
        }

        public void onConfigurationChanged(BandConfig config) {
        }

        public void onProgramInfoChanged(ProgramInfo info) {
        }

        public void onMetadataChanged(RadioMetadata metadata) {
        }

        public void onTrafficAnnouncement(boolean active) {
        }

        public void onEmergencyAnnouncement(boolean active) {
        }

        public void onAntennaState(boolean connected) {
        }

        public void onControlChanged(boolean control) {
        }
    }

    public abstract int cancel();

    public abstract void close();

    public abstract int getConfiguration(BandConfig[] bandConfigArr);

    public abstract boolean getMute();

    public abstract int getProgramInformation(ProgramInfo[] programInfoArr);

    public abstract List<ProgramInfo> getProgramList(String str);

    public abstract boolean hasControl();

    public abstract boolean isAntennaConnected();

    public abstract int scan(int i, boolean z);

    public abstract int setConfiguration(BandConfig bandConfig);

    public abstract int setMute(boolean z);

    public abstract int step(int i, boolean z);

    public abstract int tune(int i, int i2);
}
