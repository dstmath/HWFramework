package android.text.style;

import android.text.TextPaint;
import android.view.View;

public abstract class ClickableSpan extends CharacterStyle implements UpdateAppearance {
    public abstract void onClick(View view);

    public void updateDrawState(TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(true);
    }
}
