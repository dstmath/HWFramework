package android.text.style;

import android.graphics.MaskFilter;
import android.text.TextPaint;

public class MaskFilterSpan extends CharacterStyle implements UpdateAppearance {
    private MaskFilter mFilter;

    public MaskFilterSpan(MaskFilter filter) {
        this.mFilter = filter;
    }

    public MaskFilter getMaskFilter() {
        return this.mFilter;
    }

    public void updateDrawState(TextPaint ds) {
        ds.setMaskFilter(this.mFilter);
    }
}
