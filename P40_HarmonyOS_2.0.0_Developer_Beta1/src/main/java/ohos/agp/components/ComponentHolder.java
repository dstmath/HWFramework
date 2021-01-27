package ohos.agp.components;

import ohos.aafwk.utils.log.LogDomain;
import ohos.global.resource.solidxml.SolidXml;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ComponentHolder {
    private static final int DEFAULT_INDEX = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_ComponentHolder");
    private boolean isLoaded = false;
    private int mIndex = -1;
    private Component mLayout;
    private LayoutScatter mLayoutScatter;
    private ComponentContainer mParent;
    private int mResId;
    private SolidXml mSolidXml;

    public ComponentHolder(int i, ComponentContainer componentContainer, int i2, SolidXml solidXml, LayoutScatter layoutScatter) {
        this.mResId = i;
        this.mParent = componentContainer;
        this.mIndex = i2;
        this.mSolidXml = solidXml;
        this.mLayoutScatter = layoutScatter;
    }

    public Component show() {
        if (this.mLayoutScatter == null || this.mSolidXml == null || this.mParent == null) {
            return null;
        }
        HiLog.debug(TAG, "layout ComponentHolder", new Object[0]);
        if (this.isLoaded) {
            return this.mLayout;
        }
        this.isLoaded = true;
        if (this.mLayout == null) {
            this.mLayout = this.mLayoutScatter.parseSolidXml(this.mSolidXml, null, false);
        }
        HiLog.debug(TAG, "add ComponentHolder: index = %{public}d", new Object[]{Integer.valueOf(this.mIndex)});
        this.mParent.addComponent(this.mLayout, this.mIndex);
        return this.mLayout;
    }

    public void hide() {
        if (this.mLayoutScatter != null && this.mSolidXml != null && this.mParent != null) {
            HiLog.debug(TAG, "hide ComponentHolder", new Object[0]);
            if (this.isLoaded) {
                this.isLoaded = false;
                try {
                    HiLog.debug(TAG, "remove ComponentHolder: index = %{public}d", new Object[]{Integer.valueOf(this.mIndex)});
                    this.mParent.removeComponentAt(this.mIndex);
                } catch (IndexOutOfBoundsException unused) {
                    HiLog.error(TAG, "remove componentHolder failed.", new Object[0]);
                }
            }
        }
    }
}
