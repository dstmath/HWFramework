package ohos.bundle;

import java.io.IOException;
import ohos.agp.components.element.Element;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.global.resource.NotExistException;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class LauncherAbilityInfo {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218108160, "LauncherService");
    private AbilityInfo abilityInfo;
    private Context context;
    private ElementName elementName;
    private Element icon;
    private String label;
    private LauncherUtil launcherUtil;
    private ResourceManager resourceManager;
    private int userId;

    public LauncherAbilityInfo(Context context2, AbilityInfo abilityInfo2, int i) {
        this.context = context2;
        if (abilityInfo2 != null) {
            this.abilityInfo = new AbilityInfo(abilityInfo2);
            this.elementName = new ElementName(abilityInfo2.getDeviceId(), abilityInfo2.getBundleName(), abilityInfo2.getClassName());
        }
        this.userId = i;
    }

    public ApplicationInfo getApplicationInfo() {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            return null;
        }
        return abilityInfo2.getApplicationInfo();
    }

    public ElementName getElementName() {
        return this.elementName;
    }

    public int getUserId() {
        return this.userId;
    }

    public String getName() {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            return "";
        }
        return abilityInfo2.getClassName();
    }

    public String getLabel() {
        String str = this.label;
        if (str == null || str.isEmpty()) {
            this.label = getLabelInner();
        }
        return this.label;
    }

    public Element getIcon() {
        if (this.icon == null) {
            this.icon = getIconInner();
        }
        return this.icon;
    }

    private ResourceManager getResourceManager() {
        if (this.resourceManager == null) {
            if (this.launcherUtil == null) {
                this.launcherUtil = new LauncherUtil(this.context);
            }
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null) {
                this.resourceManager = this.launcherUtil.getResourceManager(abilityInfo2.getBundleName());
            }
        }
        return this.resourceManager;
    }

    private String getLabelInner() {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            AppLog.e("getLabel abilityInfo is null", new Object[0]);
            return "";
        }
        int labelId = abilityInfo2.getLabelId();
        if (labelId <= 0) {
            AppLog.i("getLabelId from abilityInfo failed", new Object[0]);
            this.label = this.abilityInfo.getLabel();
            return this.label;
        }
        this.resourceManager = getResourceManager();
        ResourceManager resourceManager2 = this.resourceManager;
        if (resourceManager2 == null) {
            AppLog.e("getLabel error due to get resourceManager failed", new Object[0]);
            return "";
        }
        try {
            this.label = resourceManager2.getElement(labelId).getString();
        } catch (NotExistException e) {
            AppLog.e("getResource NotExistException happen: %{public}s", e.getMessage());
        } catch (IOException e2) {
            AppLog.e("getResource IOException happen: %{public}s", e2.getMessage());
        } catch (WrongTypeException e3) {
            AppLog.e("getResource WrongTypeException happen: %{public}s", e3.getMessage());
        }
        return this.label;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0073  */
    private Element getIconInner() {
        Resource resource;
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            AppLog.e("getIconInner abilityInfo is null", new Object[0]);
            return null;
        }
        int iconId = abilityInfo2.getIconId();
        if (iconId <= 0) {
            ApplicationInfo applicationInfo = getApplicationInfo();
            if (applicationInfo != null) {
                iconId = applicationInfo.getIconId();
            }
            if (iconId <= 0) {
                AppLog.e(TAG, "getIconId from abilityInfo and applicationInfo failed", new Object[0]);
                return null;
            }
        }
        this.resourceManager = getResourceManager();
        ResourceManager resourceManager2 = this.resourceManager;
        if (resourceManager2 == null) {
            AppLog.e(TAG, "remoteResourceManager is null", new Object[0]);
            return null;
        }
        try {
            resource = resourceManager2.getResource(iconId);
        } catch (NotExistException e) {
            AppLog.e(TAG, "getResource NotExistException happen: %{public}s", e.getMessage());
        } catch (IOException e2) {
            AppLog.e(TAG, "getResource IOException happen: %{public}s", e2.getMessage());
        }
        if (resource != null) {
            AppLog.e(TAG, "getIconInner get resource is null", new Object[0]);
            return null;
        }
        if (this.launcherUtil == null) {
            this.launcherUtil = new LauncherUtil(this.context);
        }
        Element createPixelMapDrawable = this.launcherUtil.createPixelMapDrawable(resource);
        if (createPixelMapDrawable == null) {
            AppLog.e(TAG, "drawable created is null", new Object[0]);
        }
        this.launcherUtil.closeStream(resource);
        return createPixelMapDrawable;
        resource = null;
        if (resource != null) {
        }
    }

    public long getInstallTime() {
        Context context2 = this.context;
        if (context2 == null) {
            AppLog.e("getInstallTime error: context is null.", new Object[0]);
            return 0;
        } else if (this.abilityInfo == null) {
            AppLog.e("getInstallTime error:  ability info is null.", new Object[0]);
            return 0;
        } else {
            IBundleManager bundleManager = context2.getBundleManager();
            if (bundleManager == null) {
                AppLog.e("getInstallTime error:  bundle manager is null.", new Object[0]);
                return 0;
            }
            try {
                BundleInfo bundleInfo = bundleManager.getBundleInfo(this.abilityInfo.getBundleName(), 0);
                if (bundleInfo != null) {
                    return bundleInfo.getInstallTime();
                }
            } catch (RemoteException unused) {
                AppLog.e("getInstallTime server error.", new Object[0]);
            }
            AppLog.e("getInstallTime error: no matched bundle.", new Object[0]);
            return 0;
        }
    }
}
