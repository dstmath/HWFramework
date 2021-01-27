package ohos.media.camera.zidl;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.agp.graphics.SurfaceOps;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Scope;

public class StreamConfigAbility {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StreamConfigAbility.class);
    private final Map<Class<?>, List<Size>> classOutputSizesMap;
    private final Map<Integer, List<Size>> formatHighSizesMap;
    private final Map<Integer, List<Size>> formatOutputSizesMap;
    private final Map<Size, List<Scope<Integer>>> highSpeedVideSizesMap;
    private final StreamConfigurationMap streamConfigurationMap;

    public StreamConfigAbility(Map<Class<?>, List<Size>> map, Map<Integer, List<Size>> map2, Map<Size, List<Scope<Integer>>> map3, Map<Integer, List<Size>> map4, StreamConfigurationMap streamConfigurationMap2) {
        this.classOutputSizesMap = new HashMap(map);
        this.formatOutputSizesMap = new HashMap(map2);
        this.highSpeedVideSizesMap = new HashMap(map3);
        this.formatHighSizesMap = new HashMap(map4);
        this.streamConfigurationMap = streamConfigurationMap2;
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

    public List<Integer> getSupportedOutputFormats() {
        return Collections.unmodifiableList(new ArrayList(this.formatOutputSizesMap.keySet()));
    }

    public List<Scope<Integer>> getSupportedHighFrameRate(Size size) {
        if (this.highSpeedVideSizesMap.get(size) == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.highSpeedVideSizesMap.get(size));
    }

    public List<Size> getSupportedHighSizes(int i) {
        if (this.formatHighSizesMap.get(Integer.valueOf(i)) == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.formatHighSizesMap.get(Integer.valueOf(i)));
    }

    public long getMinCaptureDuration(int i, Size size) {
        HashMap hashMap = new HashMap(6);
        hashMap.put(0, 0);
        hashMap.put(1, 17);
        hashMap.put(2, 35);
        hashMap.put(3, 256);
        hashMap.put(4, 37);
        hashMap.put(5, 32);
        return this.streamConfigurationMap.getOutputMinFrameDuration(((Integer) hashMap.get(Integer.valueOf(i))).intValue(), new android.util.Size(size.width, size.height));
    }

    public <T> long getMinCaptureDuration(Class<T> cls, Size size) {
        HashMap hashMap = new HashMap(3);
        hashMap.put(SurfaceOps.class, SurfaceHolder.class);
        hashMap.put(ImageReceiver.class, ImageReader.class);
        hashMap.put(Recorder.class, MediaRecorder.class);
        if (hashMap.containsKey(cls)) {
            return this.streamConfigurationMap.getOutputMinFrameDuration((Class) hashMap.get(cls), new android.util.Size(size.width, size.height));
        }
        LOGGER.warn("Not supported class %{public}s", cls);
        return 0;
    }
}
