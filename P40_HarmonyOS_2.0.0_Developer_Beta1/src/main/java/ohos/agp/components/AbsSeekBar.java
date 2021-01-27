package ohos.agp.components;

import java.util.Objects;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.app.Context;

public abstract class AbsSeekBar extends ProgressBar {
    private Formatter mFormatter;
    private Element mThumbElement;

    public interface Formatter {
        String format(int i);
    }

    private native void nativeSetTextFormatter(long j, Formatter formatter);

    private native void nativeSetThumbDrawable(long j, long j2);

    public AbsSeekBar(Context context) {
        this(context, null);
    }

    public AbsSeekBar(Context context, AttrSet attrSet) {
        this(context, attrSet, "AbsSeekBarDefaultStyle");
    }

    public AbsSeekBar(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    public void setThumbElement(Element element) {
        this.mThumbElement = element;
        nativeSetThumbDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getThumbElement() {
        return this.mThumbElement;
    }

    public void setAbsSeekBarFormatter(Formatter formatter) {
        if (!Objects.equals(formatter, this.mFormatter)) {
            this.mFormatter = formatter;
            nativeSetTextFormatter(this.mNativeViewPtr, formatter);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getAbsSeekBarAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        applyStyleImplementation(style);
    }

    private void applyStyleImplementation(Style style) {
        String[] strArr = {"thumb_element"};
        for (String str : strArr) {
            if (style.hasProperty(str)) {
                char c = 65535;
                if (str.hashCode() == 788152723 && str.equals("thumb_element")) {
                    c = 0;
                }
                if (c == 0) {
                    setThumbElement(style.getPropertyValue(str).asElement());
                }
            }
        }
    }
}
