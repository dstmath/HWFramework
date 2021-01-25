package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class HwDevicePolicyManagerInnerEx extends DevicePolicyManagerService implements IHwDevicePolicyManagerInner {
    private static HwDevicePolicyManagerInnerEx sInstance;

    public HwDevicePolicyManagerInnerEx(Context context) {
        super(context);
        this.mHwDevicePolicyManagerService = HwDevicePolicyFactory.loadFactory().getHuaweiDevicePolicyManagerService(context, this);
    }

    public static synchronized HwDevicePolicyManagerInnerEx getDefault(Context context) {
        HwDevicePolicyManagerInnerEx hwDevicePolicyManagerInnerEx;
        synchronized (HwDevicePolicyManagerInnerEx.class) {
            if (sInstance == null) {
                sInstance = new HwDevicePolicyManagerInnerEx(context);
            }
            hwDevicePolicyManagerInnerEx = sInstance;
        }
        return hwDevicePolicyManagerInnerEx;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerInner
    public void saveSettingsLockedInner(int userHandle) {
        super.saveSettingsLocked(userHandle);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerInner
    public Object getLockObjectInner() {
        return super.getLockObject();
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerInner
    public ActiveAdminEx getActiveAdminUncheckedLockedInner(ComponentName who, int userHandle) {
        DevicePolicyManagerService.ActiveAdmin admin = super.getActiveAdminUncheckedLocked(who, userHandle);
        if (admin != null) {
            return new ActiveAdminEx(admin);
        }
        return null;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerInner
    public ActiveAdminEx getActiveAdminForCallerLockedInner(ComponentName who, int reqPolicy) throws SecurityException {
        DevicePolicyManagerService.ActiveAdmin admin = super.getActiveAdminForCallerLocked(who, reqPolicy);
        if (admin != null) {
            return new ActiveAdminEx(admin);
        }
        return null;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerInner
    public DevicePolicyDataEx getUserDataInner(int userHandle) {
        return new DevicePolicyDataEx(super.getUserData(userHandle));
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerInner
    public void enforceFullCrossUsersPermissionInner(int userHandle) {
        super.enforceFullCrossUsersPermission(userHandle);
    }

    public class DevicePolicyDataEx {
        private DevicePolicyManagerService.DevicePolicyData policyData;

        public DevicePolicyDataEx() {
        }

        public DevicePolicyDataEx(DevicePolicyManagerService.DevicePolicyData policy) {
            this.policyData = policy;
        }

        public ArrayList<ActiveAdminEx> getActiveAdminExList() {
            ArrayList<ActiveAdminEx> adminList = new ArrayList<>();
            Iterator<DevicePolicyManagerService.ActiveAdmin> it = this.policyData.mAdminList.iterator();
            while (it.hasNext()) {
                adminList.add(new ActiveAdminEx(it.next()));
            }
            return adminList;
        }
    }

    public class ActiveAdminEx {
        private DevicePolicyManagerService.ActiveAdmin activeAdmin;

        public ActiveAdminEx() {
        }

        public ActiveAdminEx(DevicePolicyManagerService.ActiveAdmin admin) {
            this.activeAdmin = admin;
        }

        public DevicePolicyManagerService.ActiveAdmin getActiveAdmin() {
            return this.activeAdmin;
        }

        public int getUid() {
            return this.activeAdmin.getUid();
        }

        public HwActiveAdmin getHwActiveAdmin() {
            return this.activeAdmin.mHwActiveAdmin;
        }

        public void setHwActiveAdmin(HwActiveAdmin admin) {
            this.activeAdmin.mHwActiveAdmin = admin;
        }

        public Set<String> getAccountTypesWithManagementDisabled() {
            return this.activeAdmin.accountTypesWithManagementDisabled;
        }

        public boolean equals(Object obj) {
            if (obj instanceof ActiveAdminEx) {
                return Objects.equals(this.activeAdmin, ((ActiveAdminEx) obj).getActiveAdmin());
            }
            return false;
        }

        public int hashCode() {
            return this.activeAdmin.hashCode();
        }
    }
}
