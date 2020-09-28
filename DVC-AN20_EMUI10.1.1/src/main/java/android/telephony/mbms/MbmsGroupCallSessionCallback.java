package android.telephony.mbms;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface MbmsGroupCallSessionCallback {

    @Retention(RetentionPolicy.SOURCE)
    public @interface GroupCallError {
    }

    default void onError(int errorCode, String message) {
    }

    default void onAvailableSaisUpdated(List<Integer> list, List<List<Integer>> list2) {
    }

    default void onServiceInterfaceAvailable(String interfaceName, int index) {
    }

    default void onMiddlewareReady() {
    }
}
