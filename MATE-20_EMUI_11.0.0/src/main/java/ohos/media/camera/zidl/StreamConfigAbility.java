package ohos.media.camera.zidl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class StreamConfigAbility {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StreamConfigAbility.class);
    private final Map<Class<?>, List<Size>> classOutputSizesMap;
    private final Map<Integer, List<Size>> formatOutputSizesMap;

    public StreamConfigAbility(Map<Class<?>, List<Size>> map, Map<Integer, List<Size>> map2) {
        this.classOutputSizesMap = new HashMap(map);
        this.formatOutputSizesMap = new HashMap(map2);
    }

    public List<Size> getOutputSizes(int i) {
        if (isFormatSupported(i)) {
            return Collections.unmodifiableList(this.formatOutputSizesMap.get(Integer.valueOf(i)));
        }
        LOGGER.warn("Not supported format %{public}d", Integer.valueOf(i));
        return Collections.emptyList();
    }

    public <T> List<Size> getOutputSizes(Class<T> cls) {
        if (this.classOutputSizesMap.containsKey(cls)) {
            return Collections.unmodifiableList(this.classOutputSizesMap.get(cls));
        }
        LOGGER.warn("Not supported class %{public}s", cls);
        return Collections.emptyList();
    }

    public <T> boolean isSurfaceClassSupported(Class<T> cls) {
        return this.classOutputSizesMap.containsKey(cls);
    }

    public boolean isFormatSupported(int i) {
        return this.formatOutputSizesMap.containsKey(Integer.valueOf(i));
    }

    public int[] getOutputSizesFormats() {
        return this.formatOutputSizesMap.keySet().stream().mapToInt($$Lambda$StreamConfigAbility$0tccY7fBcmEQDW3JFr9NNHAuIk.INSTANCE).toArray();
    }
}
