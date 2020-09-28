package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoTextUtils {
    private static final int INVALIDE_VALUE = -1;

    public static void autoText(int maxTextSize, int minTextSize, int stepGranularity, int unit, int parentWidth, int parentHeight, TextView textView) {
        if (textView != null) {
            int maxWidth = textView.getMaxWidth();
            int maxHeight = textView.getMaxHeight();
            int newParentWidth = parentWidth;
            if (maxWidth != -1) {
                if (maxWidth < parentWidth) {
                    newParentWidth = maxWidth;
                }
            }
            int newParentHeight = parentHeight;
            if (maxHeight != -1) {
                if (maxHeight < parentHeight) {
                    newParentHeight = maxHeight;
                }
            }
            int viewWidth = (newParentWidth - textView.getTotalPaddingLeft()) - textView.getTotalPaddingRight();
            if (viewWidth >= 0) {
                TextPaint textPaint = new TextPaint();
                textPaint.set(textView.getPaint());
                Context context = textView.getContext();
                Resources resources = context == null ? Resources.getSystem() : context.getResources();
                float currentSize = TypedValue.applyDimension(unit, (float) maxTextSize, resources.getDisplayMetrics());
                float minSize = TypedValue.applyDimension(unit, (float) minTextSize, resources.getDisplayMetrics());
                float sizeStep = TypedValue.applyDimension(unit, (float) stepGranularity, resources.getDisplayMetrics());
                if (minSize > 0.0f && sizeStep > 0.0f) {
                    CharSequence text = textView.getText();
                    textPaint.setTextSize(currentSize);
                    for (float textWidth = textPaint.measureText(text.toString()); textWidth > ((float) viewWidth) && currentSize > minSize; textWidth = textPaint.measureText(text.toString())) {
                        currentSize -= sizeStep;
                        textPaint.setTextSize(currentSize);
                    }
                    textView.setTextSize(0, currentSize);
                    measureHeight(newParentHeight, newParentWidth, textView);
                }
            }
        }
    }

    private static void measureHeight(int parentHeight, int parentWidth, TextView textView) {
        int availedHeight;
        int maxLines = textView.getMaxLines();
        if (maxLines > 1 && (availedHeight = (parentHeight - textView.getExtendedPaddingBottom()) - textView.getExtendedPaddingTop()) >= 0) {
            StaticLayout staticLayout = new StaticLayout(textView.getText(), textView.getPaint(), (parentWidth - textView.getTotalPaddingLeft()) - textView.getTotalPaddingRight(), Layout.Alignment.ALIGN_NORMAL, textView.getLineSpacingMultiplier(), textView.getLineSpacingExtra(), false);
            int lineCount = staticLayout.getLineCount();
            if (staticLayout.getHeight() > availedHeight && lineCount > 1 && lineCount <= maxLines + 1) {
                textView.setMaxLines(lineCount - 1);
            }
        }
    }
}
