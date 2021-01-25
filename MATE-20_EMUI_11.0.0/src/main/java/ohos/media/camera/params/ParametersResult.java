package ohos.media.camera.params;

import java.util.List;
import ohos.media.camera.params.ResultKey;

public interface ParametersResult {

    public @interface State {
        public static final int ERROR_UNKNOWN = -1;
        public static final int PARAMETERS_RESULT = 1;
    }

    List<ResultKey.Key<?>> getAvailableResultKeys();

    <T> T getResultValue(ResultKey.Key<T> key);

    @State
    int getState();
}
