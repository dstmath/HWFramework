package android.vrsystem;

import android.content.Context;

public interface IVRSystemServiceManager {
    public static final String VR_MANAGER = "vr_system";

    void acceptInCall(Context context);

    void endInCall(Context context);

    String getContactName(Context context, String str);

    boolean isVRApplication(Context context, String str);

    boolean isVRMode();

    void registerVRListener(Context context, IVRListener iVRListener);

    void unregisterVRListener(Context context, IVRListener iVRListener);
}
