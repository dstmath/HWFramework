package ohos.agp.components;

import ohos.agp.components.AbsSeekBar;
import ohos.agp.components.element.Element;
import ohos.app.Context;

public abstract class AbsSlider extends AbsSeekBar {
    private Formatter mFormatter;

    public interface Formatter {
        String format(int i);
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
        this.mFormatter = formatter;
        super.setAbsSeekBarFormatter(new AbsSeekBar.Formatter() {
            /* class ohos.agp.components.AbsSlider.AnonymousClass1 */

            @Override // ohos.agp.components.AbsSeekBar.Formatter
            public String format(int i) {
                return AbsSlider.this.mFormatter != null ? AbsSlider.this.mFormatter.format(i) : "";
            }
        });
    }

    public Formatter getFormatter() {
        return this.mFormatter;
    }
}
