package ohos.media.camera.mode.action;

import java.io.File;
import java.util.List;
import ohos.agp.graphics.Surface;
import ohos.media.camera.mode.impl.ActionDataCallbackImpl;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;
import ohos.media.image.common.Size;

public interface CaptureAction extends Action {
    void capture(File file, ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i, List<Surface> list);

    void capture(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i, List<Surface> list);

    void captureBurst(File file, ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i, List<Surface> list);

    void captureBurst(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i, List<Surface> list);

    void createSurface(Size size, int i, int i2);

    void destroySurface();

    Surface getRawSurface();

    void releaseResources();

    void setCaptureTemplateType(int i);

    void stop();
}
