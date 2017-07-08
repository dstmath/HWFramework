package android.location;

public abstract class GnssStatusCallback {
    public void onStarted() {
    }

    public void onStopped() {
    }

    public void onFirstFix(int ttffMillis) {
    }

    public void onSatelliteStatusChanged(GnssStatus status) {
    }
}
