package android.content.pm;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.R;
import com.android.internal.util.UserIcons;
import java.util.List;

public class CrossProfileApps {
    private final Context mContext;
    private final Resources mResources;
    private final ICrossProfileApps mService;
    private final UserManager mUserManager;

    public CrossProfileApps(Context context, ICrossProfileApps service) {
        this.mContext = context;
        this.mService = service;
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mResources = context.getResources();
    }

    public void startMainActivity(ComponentName component, UserHandle targetUser) {
        try {
            this.mService.startActivityAsUser(this.mContext.getIApplicationThread(), this.mContext.getPackageName(), component, targetUser.getIdentifier(), true);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void startActivity(ComponentName component, UserHandle targetUser) {
        try {
            this.mService.startActivityAsUser(this.mContext.getIApplicationThread(), this.mContext.getPackageName(), component, targetUser.getIdentifier(), false);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public List<UserHandle> getTargetUserProfiles() {
        try {
            return this.mService.getTargetUserProfiles(this.mContext.getPackageName());
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public CharSequence getProfileSwitchingLabel(UserHandle userHandle) {
        int stringRes;
        verifyCanAccessUser(userHandle);
        if (this.mUserManager.isManagedProfile(userHandle.getIdentifier())) {
            stringRes = R.string.managed_profile_label;
        } else {
            stringRes = R.string.user_owner_label;
        }
        return this.mResources.getString(stringRes);
    }

    public Drawable getProfileSwitchingIconDrawable(UserHandle userHandle) {
        verifyCanAccessUser(userHandle);
        if (this.mUserManager.isManagedProfile(userHandle.getIdentifier())) {
            return this.mResources.getDrawable(R.drawable.ic_corp_badge, null);
        }
        return UserIcons.getDefaultUserIcon(this.mResources, 0, true);
    }

    private void verifyCanAccessUser(UserHandle userHandle) {
        if (!getTargetUserProfiles().contains(userHandle)) {
            throw new SecurityException("Not allowed to access " + userHandle);
        }
    }
}
