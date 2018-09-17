package huawei.com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.android.internal.widget.NotificationActionListLayout;
import java.text.BreakIterator;

@RemoteView
public class HwNotificationActionListLayout extends NotificationActionListLayout {
    private static final int MULTIPLE_LINE = 2;
    private static final int SINGLE_LINE = 1;
    private static final String TAG = "HwNotiActionListLayout";
    private boolean mHasCompoundDrawble = false;
    private int mLeftRightPadding = getResources().getDimensionPixelSize(34472196);

    public HwNotificationActionListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
                    this.mHasCompoundDrawble = hasCompoundDrawbles(button);
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
                    button.setPadding(this.mLeftRightPadding, 0, this.mLeftRightPadding, 0);
                    if (!this.mHasCompoundDrawble) {
                        button.setGravity(19);
                    }
                }
            }
        }
    }

    private boolean hasCompoundDrawbles(TextView textView) {
        if (textView == null) {
            Log.w(TAG, "hasCompoundDrawbles, textview is null");
            return false;
        } else if (this.mHasCompoundDrawble) {
            return true;
        } else {
            Drawable[] drawables = textView.getCompoundDrawablesRelative();
            for (Drawable drawable : drawables) {
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
        float buttonTextWidth = Layout.getDesiredWidth(textString.toUpperCase(), 0, textString.length(), textView.getPaint());
        String firstWord = getFirstWord(textString);
        float mutipleLineTextSize = (float) textView.getContext().getResources().getDimensionPixelSize(34472119);
        if (Layout.getDesiredWidth(firstWord.toUpperCase(), 0, firstWord.length(), textView.getPaint()) > ((float) availableWidth) || ((float) availableWidth) >= buttonTextWidth) {
            textView.setSingleLine(true);
            textView.setAllCaps(true);
            return false;
        }
        textView.setSingleLine(false);
        textView.setAllCaps(true);
        textView.setMaxLines(2);
        textView.setTextSize(0, mutipleLineTextSize);
        textView.setEllipsize(TruncateAt.END);
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
            compoundDrawableWidth = (drawables[0].getBounds().width() + textView.getCompoundDrawablePadding()) + 0;
        }
        if (drawables[2] != null) {
            compoundDrawableWidth += drawables[2].getBounds().width() + textView.getCompoundDrawablePadding();
        }
        return compoundDrawableWidth;
    }

    private String getFirstWord(String rawText) {
        if (TextUtils.isEmpty(rawText)) {
            Log.w(TAG, "getFirstWord, input text is empty");
            return "";
        }
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(rawText);
        return rawText.substring(boundary.first(), boundary.next());
    }
}
