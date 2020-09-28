package huawei.com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.widget.NotificationActionListLayout;
import com.huawei.uikit.effect.BuildConfig;
import java.text.BreakIterator;
import java.util.Locale;

@RemoteViews.RemoteView
public class HwNotificationActionListLayout extends NotificationActionListLayout {
    private static final int END_DRAWABLE = 2;
    private static final int MULTIPLE_LINE = 2;
    private static final int SINGLE_LINE = 1;
    private static final String TAG = "HwNotiActionListLayout";
    private boolean mHasCompoundDrawable = false;
    private int mLeftRightPadding = getResources().getDimensionPixelSize(34472196);

    public HwNotificationActionListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        HwNotificationActionListLayout.super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        boolean isMultipleLines = false;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                TextView button = (TextView) child;
                if (button.getText().length() > 0) {
                    boolean isMultipleLinesTemp = isMultipleLines(button);
                    if (!isMultipleLines && isMultipleLinesTemp) {
                        isMultipleLines = true;
                    }
                    this.mHasCompoundDrawable = hasCompoundDrawables(button);
                }
            }
        }
        if (isMultipleLines) {
            updateGravity(childCount);
        }
    }

    private void updateGravity(int childCount) {
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                TextView button = (TextView) child;
                if (button.getText().length() > 0) {
                    int i2 = this.mLeftRightPadding;
                    button.setPadding(i2, 0, i2, 0);
                    if (!this.mHasCompoundDrawable) {
                        button.setGravity(19);
                    }
                }
            }
        }
    }

    private boolean hasCompoundDrawables(TextView textView) {
        Drawable[] drawables;
        if (textView == null) {
            Log.w(TAG, "hasCompoundDrawables, textview is null");
            return false;
        } else if (this.mHasCompoundDrawable) {
            return true;
        } else {
            for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
                if (drawable != null) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isMultipleLines(TextView textView) {
        if (textView == null) {
            Log.w(TAG, "isMultipleLines, textview is null");
            return false;
        }
        int availableWidth = ((textView.getMeasuredWidth() - textView.getPaddingLeft()) - textView.getPaddingRight()) - calculateCompoundDrawableWidth(textView);
        String textString = textView.getText().toString();
        float buttonTextWidth = Layout.getDesiredWidth(textString.toUpperCase(Locale.ROOT), 0, textString.length(), textView.getPaint());
        String firstWord = getFirstWord(textString);
        float firstWordWidth = Layout.getDesiredWidth(firstWord.toUpperCase(Locale.ROOT), 0, firstWord.length(), textView.getPaint());
        float multipleLineTextSize = (float) textView.getContext().getResources().getDimensionPixelSize(34472688);
        if (firstWordWidth > ((float) availableWidth) || ((float) availableWidth) >= buttonTextWidth) {
            textView.setSingleLine(true);
            textView.setAllCaps(true);
            return false;
        }
        textView.setSingleLine(false);
        textView.setAllCaps(true);
        textView.setMaxLines(2);
        textView.setTextSize(0, multipleLineTextSize);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return true;
    }

    private int calculateCompoundDrawableWidth(TextView textView) {
        if (textView == null) {
            Log.w(TAG, "calculateCompoundDrawableWidth, textview is null");
            return 0;
        }
        int compoundDrawableWidth = 0;
        Drawable[] drawables = textView.getCompoundDrawablesRelative();
        if (drawables[0] != null) {
            compoundDrawableWidth = 0 + drawables[0].getBounds().width() + textView.getCompoundDrawablePadding();
        }
        if (drawables[2] != null) {
            return compoundDrawableWidth + drawables[2].getBounds().width() + textView.getCompoundDrawablePadding();
        }
        return compoundDrawableWidth;
    }

    private String getFirstWord(String rawText) {
        if (TextUtils.isEmpty(rawText)) {
            Log.w(TAG, "getFirstWord, input text is empty");
            return BuildConfig.FLAVOR;
        }
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(rawText);
        return rawText.substring(boundary.first(), boundary.next());
    }
}
