package ohos.media.camera.mode.function;

import ohos.media.camera.mode.ActionStateCallback;

public interface PreCapture {
    public static final int PRECAPTURE_NORMAL_TYPE = 0;
    public static final int PRECAPTURE_SUPERNIGHT_TYPE = 2;
    public static final int PRECAPTURE_VIDEO_TYPE = 1;

    void active();

    void capture(Promise promise);

    void deactive();

    void setActionStateCallback(ActionStateCallback actionStateCallback);

    void setRotation(int i);

    void stop();
}
