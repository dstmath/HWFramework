package ohos.media.camera.zidl;

import ohos.media.camera.exception.ConnectException;

public interface ICameraService {
    ICamera createCamera(String str, ICameraCallback iCameraCallback, String str2) throws ConnectException;

    CameraAbilityNative getCameraAbility(String str) throws ConnectException;

    void initialize() throws ConnectException;
}
