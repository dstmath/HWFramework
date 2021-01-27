package android.app.timedetector;

import android.app.timedetector.ITimeDetectorService;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;

public final class TimeDetector {
    private static final boolean DEBUG = false;
    private static final String TAG = "timedetector.TimeDetector";
    private final ITimeDetectorService mITimeDetectorService = ITimeDetectorService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.TIME_DETECTOR_SERVICE));

    public void suggestTime(TimeSignal timeSignal) {
        try {
            this.mITimeDetectorService.suggestTime(timeSignal);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
