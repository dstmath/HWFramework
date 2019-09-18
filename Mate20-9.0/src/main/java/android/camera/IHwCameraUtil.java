package android.camera;

public interface IHwCameraUtil {
    boolean isIllegalAccessAuxCamera(int i, String str);

    boolean needHideAuxCamera(int i);

    boolean notifySurfaceFlingerCameraStatus(boolean z, boolean z2);
}
