package android.permission;

import android.annotation.SystemApi;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import com.android.internal.annotations.Immutable;
import com.android.server.SystemConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SystemApi
public final class PermissionManager {
    public static final ArrayList<SplitPermissionInfo> SPLIT_PERMISSIONS = SystemConfig.getInstance().getSplitPermissions();
    private final Context mContext;
    private final IPackageManager mPackageManager;

    public PermissionManager(Context context, IPackageManager packageManager) {
        this.mContext = context;
        this.mPackageManager = packageManager;
    }

    @SystemApi
    public int getRuntimePermissionsVersion() {
        try {
            return this.mPackageManager.getRuntimePermissionsVersion(this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setRuntimePermissionsVersion(int version) {
        try {
            this.mPackageManager.setRuntimePermissionsVersion(version, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<SplitPermissionInfo> getSplitPermissions() {
        return SPLIT_PERMISSIONS;
    }

    @Immutable
    public static final class SplitPermissionInfo {
        private final List<String> mNewPerms;
        private final String mSplitPerm;
        private final int mTargetSdk;

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SplitPermissionInfo that = (SplitPermissionInfo) o;
            if (this.mTargetSdk != that.mTargetSdk || !this.mSplitPerm.equals(that.mSplitPerm) || !this.mNewPerms.equals(that.mNewPerms)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mSplitPerm, this.mNewPerms, Integer.valueOf(this.mTargetSdk));
        }

        public String getSplitPermission() {
            return this.mSplitPerm;
        }

        public List<String> getNewPermissions() {
            return this.mNewPerms;
        }

        public int getTargetSdk() {
            return this.mTargetSdk;
        }

        public SplitPermissionInfo(String splitPerm, List<String> newPerms, int targetSdk) {
            this.mSplitPerm = splitPerm;
            this.mNewPerms = newPerms;
            this.mTargetSdk = targetSdk;
        }
    }
}
