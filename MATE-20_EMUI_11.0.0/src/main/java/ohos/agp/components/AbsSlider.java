package ohos.agp.components;

import ohos.agp.components.AbsSeekBar;
import ohos.agp.components.element.Element;
import ohos.app.Context;

public abstract class AbsSlider extends AbsSeekBar {

    public interface Formatter extends AbsSeekBar.Formatter {
    }

    public AbsSlider(Context context) {
        super(context, null);
    }

    public AbsSlider(Context context, AttrSet attrSet) {
        super(context, attrSet, "AbsSliderDefaultStyle");
    }

    public AbsSlider(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    @Override // ohos.agp.components.AbsSeekBar
    public void setThumbElement(Element element) {
        super.setThumbElement(element);
    }

    @Override // ohos.agp.components.AbsSeekBar
    public Element getThumbElement() {
        return super.getThumbElement();
    }

    public void setFormatter(Formatter formatter) {
        super.setFormatter((AbsSeekBar.Formatter) formatter);
    }
}
