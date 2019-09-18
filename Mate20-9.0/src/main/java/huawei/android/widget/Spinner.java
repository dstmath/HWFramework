package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import huawei.android.widget.DecouplingUtil.ReflectUtil;

public class Spinner extends android.widget.Spinner {
    private static final String GOOGLE_SPINNER_CLASSNAME = "android.widget.AbsSpinner";
    private static final String TAG = "Spinner";
    private Class mSpinnerClass;

    public Spinner(Context context) {
        super(context);
        initClass();
        setClickEffice(context, 0);
    }

    public Spinner(Context context, int mode) {
        super(context, mode);
        initClass();
        setClickEffice(context, 0);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClass();
        setClickEffice(context, 0);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initClass();
        setClickEffice(context, defStyleAttr);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, 0);
        initClass();
        setClickEffice(context, defStyleAttr);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        initClass();
        setClickEffice(context, defStyleAttr);
    }

    private void setClickEffice(Context context, int defStyleAttr) {
        Drawable bg = getBackground();
        if (bg != null) {
            setBackground(new LayerDrawable(new Drawable[]{HwWidgetUtils.getHwAnimatedGradientDrawable(context, 16842881), bg}));
            return;
        }
        setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr));
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
        initClass();
    }

    private void initClass() {
        if (this.mSpinnerClass == null) {
            try {
                this.mSpinnerClass = Class.forName(GOOGLE_SPINNER_CLASSNAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mSpinnerClass not found");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Rect spinnerPadding = (Rect) ReflectUtil.getObject(this, "mSpinnerPadding", this.mSpinnerClass);
        if (isLayoutRtl() && spinnerPadding.left < spinnerPadding.right) {
            int temp = spinnerPadding.right;
            spinnerPadding.right = spinnerPadding.left;
            spinnerPadding.left = temp;
            ReflectUtil.setObject("mSpinnerPadding", this, spinnerPadding, this.mSpinnerClass);
            int temp2 = this.mPaddingRight;
            this.mPaddingRight = this.mPaddingLeft;
            this.mPaddingLeft = temp2;
        }
    }
}
