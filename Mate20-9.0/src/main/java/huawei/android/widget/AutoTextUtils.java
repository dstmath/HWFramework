package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoTextUtils {
    public static void autoText(int autoSizeMaxTextSize, int autoSizeMinTextSize, int autoSizeStepGranularity, int unit, int parentWidth, int parentHeight, TextView textView) {
        int parentWidth2;
        int parentHeight2;
        Resources r;
        int i = unit;
        TextView textView2 = textView;
        int maxWidth = textView.getMaxWidth();
        int maxHeight = textView.getMaxHeight();
        if (maxWidth != -1) {
            parentWidth2 = parentWidth;
            if (maxWidth < parentWidth2) {
                parentWidth2 = maxWidth;
            }
        } else {
            parentWidth2 = parentWidth;
        }
        if (maxHeight != -1) {
            parentHeight2 = parentHeight;
            if (maxHeight < parentHeight2) {
                parentHeight2 = maxHeight;
            }
        } else {
            parentHeight2 = parentHeight;
        }
        int viewWidth = (parentWidth2 - textView.getTotalPaddingLeft()) - textView.getTotalPaddingRight();
        if (viewWidth >= 0) {
            TextPaint textPaint = new TextPaint();
            textPaint.set(textView.getPaint());
            Context c = textView.getContext();
            if (c == null) {
                r = Resources.getSystem();
            } else {
                r = c.getResources();
            }
            float currentSize = TypedValue.applyDimension(i, (float) autoSizeMaxTextSize, r.getDisplayMetrics());
            float minSize = TypedValue.applyDimension(i, (float) autoSizeMinTextSize, r.getDisplayMetrics());
            int i2 = maxWidth;
            float sizeStep = TypedValue.applyDimension(i, (float) autoSizeStepGranularity, r.getDisplayMetrics());
            if (minSize <= 0.0f || sizeStep <= 0.0f) {
            } else {
                CharSequence text = textView.getText();
                textPaint.setTextSize(currentSize);
                float textWidth = textPaint.measureText(text.toString());
                while (true) {
                    int maxHeight2 = maxHeight;
                    if (textWidth <= ((float) viewWidth) || currentSize <= minSize) {
                        textView2.setTextSize(0, currentSize);
                        measureHeight(parentHeight2, parentWidth2, textView2);
                    } else {
                        currentSize -= sizeStep;
                        textPaint.setTextSize(currentSize);
                        textWidth = textPaint.measureText(text.toString());
                        maxHeight = maxHeight2;
                    }
                }
                textView2.setTextSize(0, currentSize);
                measureHeight(parentHeight2, parentWidth2, textView2);
            }
        }
    }

    private static void measureHeight(int parentHeight, int parentWidth, TextView textView) {
        int maxLines = textView.getMaxLines();
        if (maxLines > 1) {
            int availedHeight = (parentHeight - textView.getExtendedPaddingBottom()) - textView.getExtendedPaddingTop();
            if (availedHeight >= 0) {
                StaticLayout staticLayout = new StaticLayout(textView.getText(), textView.getPaint(), (parentWidth - textView.getTotalPaddingLeft()) - textView.getTotalPaddingRight(), Layout.Alignment.ALIGN_NORMAL, textView.getLineSpacingMultiplier(), textView.getLineSpacingExtra(), false);
                int lineCount = staticLayout.getLineCount();
                if (staticLayout.getHeight() > availedHeight && lineCount > 1 && lineCount <= maxLines + 1) {
                    textView.setMaxLines(lineCount - 1);
                }
            }
        }
    }
}
