package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.Entity;

public class NodeComponent implements Component {
    private boolean mIsEnabled;
    private boolean mIsExported;
    private String mName;
    private Entity mParent;

    public String getName() {
        return this.mName;
    }

    public void setName(String str) {
        this.mName = str;
    }

    public Entity getParent() {
        return this.mParent;
    }

    public void setParent(Entity entity) {
        this.mParent = entity;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public void setEnabled(boolean z) {
        this.mIsEnabled = z;
    }

    public boolean isExported() {
        return this.mIsExported;
    }

    public void setExported(boolean z) {
        this.mIsExported = z;
    }
}
