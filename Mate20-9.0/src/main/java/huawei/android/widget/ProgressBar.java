package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.HwLoadingDrawableImpl;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import androidhwext.R;
import java.util.ArrayList;

@RemoteViews.RemoteView
public class ProgressBar extends android.widget.ProgressBar {
    private static final String ATTRIBUTE_INDETERMINATE_DRAWABLE = "indeterminateDrawable";
    private static final String ATTRIBUTE_PROGRESS_DRAWABLE = "progressDrawable";
    private static final int DEFAULT_FILLED_COLOR = -10066330;
    private static final boolean GRADENT_DEFAUIT = false;
    private static final int MAX_ALPHA = 255;
    private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final int PROGRESS_BAR_NONE_STYLE = 0;
    private static final int SECONDARY_PROGRESS_ALPHA = 76;
    private final boolean mGradientColorEnable;

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842871);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Theme, defStyleAttr, defStyleRes);
        this.mGradientColorEnable = a.getBoolean(53, GRADENT_DEFAUIT);
        a.recycle();
        if (attrs != null) {
            if (isIndeterminate()) {
                onInitIndeterminateDrawable(attrs, defStyleAttr, defStyleRes);
            } else {
                onInitProgressDrawable(attrs);
            }
        }
    }

    private boolean isProgressGradientColorEnable(AttributeSet attrs) {
        int style = attrs.getStyleAttribute();
        String attributeValue = attrs.getAttributeValue(NAMESPACE_ANDROID, ATTRIBUTE_PROGRESS_DRAWABLE);
        boolean z = GRADENT_DEFAUIT;
        boolean result = (attributeValue == null) && this.mGradientColorEnable;
        if (style == 0 || style == 16842872) {
            return result;
        }
        if ((style == 33947950 || style == 33948031) && result) {
            z = true;
        }
        return z;
    }

    private boolean isIndeterminateGradientColorEnable(AttributeSet attrs) {
        int style = attrs.getStyleAttribute();
        String attributeValue = attrs.getAttributeValue(NAMESPACE_ANDROID, ATTRIBUTE_INDETERMINATE_DRAWABLE);
        boolean result = GRADENT_DEFAUIT;
        boolean result2 = (attributeValue == null) && this.mGradientColorEnable;
        if (style == 0) {
            return result2;
        }
        if (style < 16842871 || style > 16843401) {
            if (style >= 33947949 && style <= 33948035 && style != 33947950 && style != 33948031 && result2) {
                result = true;
            }
        } else if (style != 16842872 && result2) {
            result = true;
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void onInitProgressDrawable(AttributeSet attrs) {
        if (isProgressGradientColorEnable(attrs)) {
            Drawable progressDrawable = getContext().getDrawable(33751823);
            if (progressDrawable != null && (progressDrawable instanceof LayerDrawable)) {
                Drawable secondaryProgress = ((LayerDrawable) progressDrawable).findDrawableByLayerId(16908303);
                if (secondaryProgress != null) {
                    secondaryProgress.setAlpha(SECONDARY_PROGRESS_ALPHA);
                }
            }
            setProgressDrawable(progressDrawable);
        }
    }

    /* access modifiers changed from: protected */
    public void onInitIndeterminateDrawable(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Theme, defStyleAttr, defStyleRes);
        ArrayList<Integer> colorList = new ArrayList<>();
        if (a.hasValue(50)) {
            colorList.add(Integer.valueOf(a.getColor(50, DEFAULT_FILLED_COLOR)));
        }
        if (a.hasValue(51)) {
            colorList.add(Integer.valueOf(a.getColor(51, DEFAULT_FILLED_COLOR)));
        }
        if (a.hasValue(52)) {
            colorList.add(Integer.valueOf(a.getColor(52, DEFAULT_FILLED_COLOR)));
        }
        a.recycle();
        if (isIndeterminateGradientColorEnable(attrs) && colorList.size() >= 2) {
            int[] colors = new int[colorList.size()];
            int size = colorList.size();
            for (int i = 0; i < size; i++) {
                colors[i] = colorList.get(i).intValue();
            }
            initIndeterminateStyle(colors);
        }
    }

    /* access modifiers changed from: protected */
    public void initIndeterminateStyle(int... color) {
        int minWidth = getMinimumWidth();
        int minHeight = getMinimumHeight();
        setIndeterminateDrawable(new HwLoadingDrawableImpl(getResources(), minWidth > minHeight ? minHeight : minWidth, color));
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onInitProgressDrawable(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }
}
