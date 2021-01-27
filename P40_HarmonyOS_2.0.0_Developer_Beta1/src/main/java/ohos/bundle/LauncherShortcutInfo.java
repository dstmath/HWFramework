package ohos.bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ohos.agp.components.element.Element;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.global.resource.NotExistException;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;

public class LauncherShortcutInfo {
    private Context context;
    private Element icon;
    private String label;
    private LauncherUtil launcherUtil;
    private ResourceManager resourceManager;
    private ShortcutInfo shortcutInfo;

    public LauncherShortcutInfo(Context context2, ShortcutInfo shortcutInfo2) {
        this.context = context2;
        if (shortcutInfo2 != null) {
            this.shortcutInfo = new ShortcutInfo(shortcutInfo2);
        }
    }

    public String getShortcutId() {
        ShortcutInfo shortcutInfo2 = this.shortcutInfo;
        if (shortcutInfo2 == null) {
            return "";
        }
        return shortcutInfo2.getId();
    }

    public String getBundleName() {
        ShortcutInfo shortcutInfo2 = this.shortcutInfo;
        if (shortcutInfo2 == null) {
            return "";
        }
        return shortcutInfo2.getBundleName();
    }

    public String getLabel() {
        String str = this.label;
        if (str == null || str.isEmpty()) {
            this.label = getLabelInner();
        }
        return this.label;
    }

    public List<ShortcutIntent> getIntents() {
        ShortcutInfo shortcutInfo2 = this.shortcutInfo;
        if (shortcutInfo2 == null) {
            return new ArrayList();
        }
        return shortcutInfo2.getIntents();
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
            ShortcutInfo shortcutInfo2 = this.shortcutInfo;
            if (shortcutInfo2 != null) {
                this.resourceManager = this.launcherUtil.getResourceManager(shortcutInfo2.getBundleName());
            }
        }
        return this.resourceManager;
    }

    private String getLabelInner() {
        ShortcutInfo shortcutInfo2 = this.shortcutInfo;
        if (shortcutInfo2 == null) {
            AppLog.e("getLabel shortcutInfo is null", new Object[0]);
            return "";
        }
        int shortcutLabelId = shortcutInfo2.getShortcutLabelId();
        if (shortcutLabelId <= 0) {
            AppLog.i("getLabelId from shortcutInfo failed", new Object[0]);
            this.label = this.shortcutInfo.getLabel();
            return this.label;
        }
        this.resourceManager = getResourceManager();
        ResourceManager resourceManager2 = this.resourceManager;
        if (resourceManager2 == null) {
            AppLog.e("getLabel error due to get resourceManager failed", new Object[0]);
            return "";
        }
        try {
            this.label = resourceManager2.getElement(shortcutLabelId).getString();
        } catch (NotExistException e) {
            AppLog.e("getResource NotExistException happen: %{public}s", e.getMessage());
        } catch (IOException e2) {
            AppLog.e("getResource IOException happen: %{public}s", e2.getMessage());
        } catch (WrongTypeException e3) {
            AppLog.e("getResource WrongTypeException happen: %{public}s", e3.getMessage());
        }
        return this.label;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005d  */
    private Element getIconInner() {
        Resource resource;
        ShortcutInfo shortcutInfo2 = this.shortcutInfo;
        if (shortcutInfo2 == null) {
            AppLog.e("getIconInner shortcutInfo is null", new Object[0]);
            return null;
        }
        int shortcutIconId = shortcutInfo2.getShortcutIconId();
        if (shortcutIconId <= 0) {
            AppLog.e("getIconId from shortcutInfo and applicationInfo failed", new Object[0]);
            return null;
        }
        this.resourceManager = getResourceManager();
        ResourceManager resourceManager2 = this.resourceManager;
        if (resourceManager2 == null) {
            AppLog.e("remoteResourceManager is null", new Object[0]);
            return null;
        }
        try {
            resource = resourceManager2.getResource(shortcutIconId);
        } catch (NotExistException e) {
            AppLog.e("getResource NotExistException happen: %{public}s", e.getMessage());
        } catch (IOException e2) {
            AppLog.e("getResource IOException happen: %{public}s", e2.getMessage());
        }
        if (resource != null) {
            AppLog.e("getIconInner get resource is null", new Object[0]);
            return null;
        }
        if (this.launcherUtil == null) {
            this.launcherUtil = new LauncherUtil(this.context);
        }
        Element createPixelMapDrawable = this.launcherUtil.createPixelMapDrawable(resource);
        if (createPixelMapDrawable == null) {
            AppLog.e("drawable created is null", new Object[0]);
        }
        this.launcherUtil.closeStream(resource);
        return createPixelMapDrawable;
        resource = null;
        if (resource != null) {
        }
    }
}
