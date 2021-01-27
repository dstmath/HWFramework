package com.android.server.pm;

import android.content.ComponentName;

public class PreferredComponentEx {
    private PreferredComponent preferredComponent;

    public PreferredComponent getPreferredComponent() {
        return this.preferredComponent;
    }

    public void setPreferredComponent(PreferredComponent preferredComponent2) {
        this.preferredComponent = preferredComponent2;
    }

    public ComponentName getComponent() {
        return this.preferredComponent.mComponent;
    }

    public int getMatch() {
        return this.preferredComponent.mMatch;
    }

    public boolean getAlways() {
        return this.preferredComponent.mAlways;
    }

    public boolean sameSet(ComponentName[] comps) {
        return this.preferredComponent.sameSet(comps);
    }
}
