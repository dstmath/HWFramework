package ohos.media.camera.mode.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.params.ParameterKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CaptureParameters {
    private static final int INIT_PARAMETER_NUMBER = 30;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CaptureParameters.class);
    private final List<CaptureParameter<?>> mParameters = new ArrayList(30);
    private int stageId = 0;

    public CaptureParameters() {
    }

    public CaptureParameters(int i) {
        this.stageId = i;
    }

    public void addParameter(CaptureParameter<?> captureParameter) {
        if (captureParameter == null) {
            LOGGER.error("addParameter: null param!", new Object[0]);
            return;
        }
        LOGGER.debug("addParameter: %{public}s = %{public}s", captureParameter.getKey().getName(), captureParameter.getValue());
        this.mParameters.removeIf(new Predicate() {
            /* class ohos.media.camera.mode.tags.$$Lambda$CaptureParameters$hInt_Rx1rHPjrpSBTb4qiIkWMg */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((CaptureParameter) obj).getKey().equals(CaptureParameter.this.getKey());
            }
        });
        this.mParameters.add(captureParameter);
    }

    public <T> void addParameter(ParameterKey.Key<T> key, T t) {
        addParameter(new CaptureParameter<>(key, t));
    }

    public void addParameters(CaptureParameters captureParameters) {
        if (captureParameters == null) {
            LOGGER.error("CaptureParameters: params is null", new Object[0]);
        } else {
            this.mParameters.addAll(captureParameters.mParameters);
        }
    }

    public List<CaptureParameter<?>> getParameters() {
        return this.mParameters;
    }

    public void applyToBuilder(FrameConfig.Builder builder) {
        for (CaptureParameter<?> captureParameter : this.mParameters) {
            captureParameter.applyToBuilder(builder);
        }
    }

    public void clearParameters() {
        this.mParameters.clear();
    }
}
