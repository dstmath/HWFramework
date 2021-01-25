package android.telephony.mbms;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface GroupCallCallback {
    public static final int SIGNAL_STRENGTH_UNAVAILABLE = -1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface GroupCallError {
    }

    default void onError(int errorCode, String message) {
    }

    default void onGroupCallStateChanged(int state, int reason) {
    }

    default void onBroadcastSignalStrengthUpdated(int signalStrength) {
    }
}
