package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.Entity;

public class NodeComponent implements Component {
    private boolean mIsEnabled;
    private boolean mIsExported;
    private String mName;
    private Entity mParent;

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Entity getParent() {
        return this.mParent;
    }

    public void setParent(Entity parent) {
        this.mParent = parent;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
    }

    public boolean isExported() {
        return this.mIsExported;
    }

    public void setExported(boolean isExported) {
        this.mIsExported = isExported;
    }
}
