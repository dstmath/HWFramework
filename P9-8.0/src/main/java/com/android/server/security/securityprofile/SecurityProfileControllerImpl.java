package com.android.server.security.securityprofile;

import android.os.Binder;
import com.android.server.LocalServices;

public class SecurityProfileControllerImpl implements ISecurityProfileController {
    public boolean shouldPreventMediaProjection(int uid) {
        boolean ret = false;
        SecurityProfileInternal mSecurityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (mSecurityProfileInternal != null) {
            long token = Binder.clearCallingIdentity();
            try {
                ret = mSecurityProfileInternal.shouldPreventMediaProjection(uid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return ret;
    }
}
