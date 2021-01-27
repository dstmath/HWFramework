package ohos.media.camera.params.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.media.camera.params.ParametersResult;
import ohos.media.camera.params.ResultKey;

public class ParametersResultImpl implements ParametersResult {
    private final Map<ResultKey.Key<?>, Object> parameterResults;
    private final int state;

    public ParametersResultImpl(@ParametersResult.State int i, Map<ResultKey.Key<?>, Object> map) {
        this.state = i;
        this.parameterResults = map;
    }

    @Override // ohos.media.camera.params.ParametersResult
    @ParametersResult.State
    public int getState() {
        return this.state;
    }

    @Override // ohos.media.camera.params.ParametersResult
    public List<ResultKey.Key<?>> getAvailableResultKeys() {
        return new ArrayList(this.parameterResults.keySet());
    }

    @Override // ohos.media.camera.params.ParametersResult
    public <T> T getResultValue(ResultKey.Key<T> key) {
        return (T) this.parameterResults.get(key);
    }
}
