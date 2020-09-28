package android.camera;

import android.util.ArrayMap;

public interface IHwCameraUtil {
    int filterVirtualCamera(ArrayMap<String, Integer> arrayMap, int i);

    boolean isIllegalAccessAuxCamera(int i, String str);

    boolean needHideAuxCamera(int i);

    boolean notifySurfaceFlingerCameraStatus(boolean z, boolean z2);
}
