package android.app.role;

import android.annotation.SystemApi;
import android.os.UserHandle;

@SystemApi
public interface OnRoleHoldersChangedListener {
    void onRoleHoldersChanged(String str, UserHandle userHandle);
}
