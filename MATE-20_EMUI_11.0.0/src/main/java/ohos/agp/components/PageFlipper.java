package ohos.agp.components;

import ohos.app.Context;

public class PageFlipper extends ViewFlipper {
    public PageFlipper(Context context) {
        super(context);
    }

    public PageFlipper(Context context, AttrSet attrSet) {
        super(context, attrSet);
    }

    public PageFlipper(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    @Override // ohos.agp.components.ViewFlipper
    public int getFlipInterval() {
        return super.getFlipInterval();
    }

    @Override // ohos.agp.components.ViewFlipper
    public void setFlipInterval(int i) {
        super.setFlipInterval(i);
    }

    @Override // ohos.agp.components.ViewFlipper
    public void startFlipping() {
        super.startFlipping();
    }

    @Override // ohos.agp.components.ViewFlipper
    public void stopFlipping() {
        super.stopFlipping();
    }

    @Override // ohos.agp.components.ViewFlipper
    public boolean isFlipping() {
        return super.isFlipping();
    }
}
