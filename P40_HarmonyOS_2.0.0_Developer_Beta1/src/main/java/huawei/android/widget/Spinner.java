package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import androidhwext.R;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.columnsystem.HwColumnSystem;

public class Spinner extends android.widget.Spinner {
    private static final float DEFAULT_COLUMN_DENSITY = -1.0f;
    private static final int DEFAULT_COLUMN_SIZE = -1;
    private static final String GOOGLE_SPINNER_CLASSNAME = "android.widget.AbsSpinner";
    private static final String TAG = "Spinner";
    private float mColumnDensity = DEFAULT_COLUMN_DENSITY;
    private int mColumnHeight = -1;
    private int mColumnWidth = -1;
    private HwColumnSystem mHwColumnSystem;
    private boolean mIsColumnEnabled = true;
    private Class mSpinnerClass;

    public Spinner(Context context) {
        super(context);
        initClass();
        setClickEffice(context, 0);
        initColumnEnabled(context, null);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    public Spinner(Context context, int mode) {
        super(context, mode);
        initClass();
        setClickEffice(context, 0);
        initColumnEnabled(context, null);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClass();
        setClickEffice(context, 0);
        initColumnEnabled(context, attrs);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initClass();
        setClickEffice(context, defStyleAttr);
        initColumnEnabled(context, attrs);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, 0);
        initClass();
        setClickEffice(context, defStyleAttr);
        initColumnEnabled(context, attrs);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        initClass();
        setClickEffice(context, defStyleAttr);
        initColumnEnabled(context, attrs);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
        initClass();
        initColumnEnabled(context, attrs);
        this.mHwColumnSystem = new HwColumnSystem(getPopupContext());
    }

    private void setClickEffice(Context context, int defStyleAttr) {
        Drawable background = getBackground();
        if (background != null) {
            setBackground(new LayerDrawable(new Drawable[]{HwWidgetUtils.getHwAnimatedGradientDrawable(context, 16842881), background}));
        } else {
            setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr));
        }
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

    private void initColumnEnabled(Context context, AttributeSet attrs) {
        this.mIsColumnEnabled = false;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwSpinner);
        if (typedArray != null) {
            this.mIsColumnEnabled = typedArray.getBoolean(0, false);
            typedArray.recycle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Spinner, android.widget.AbsSpinner, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Object obj = ReflectUtil.getObject(this, "mSpinnerPadding", this.mSpinnerClass);
        if (obj instanceof Rect) {
            Rect spinnerPadding = (Rect) obj;
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

    public void setColumnEnabled(boolean isEnabled) {
        this.mIsColumnEnabled = isEnabled;
        requestLayout();
    }

    public boolean isColumnEnabled() {
        return this.mIsColumnEnabled;
    }

    public void configureColumn(int columnWidth, int columnHeight, float columnDensity) {
        this.mColumnWidth = columnWidth;
        this.mColumnHeight = columnHeight;
        this.mColumnDensity = columnDensity;
        if (isColumnEnabled()) {
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    public int limitColumnWidth(int measuredWidth) {
        this.mHwColumnSystem.setColumnType(10);
        if (this.mColumnWidth <= 0 || this.mColumnHeight <= 0 || this.mColumnDensity <= 0.0f) {
            this.mHwColumnSystem.updateConfigation(getPopupContext());
        } else {
            this.mHwColumnSystem.updateConfigation(getPopupContext(), this.mColumnWidth, this.mColumnHeight, this.mColumnDensity);
        }
        int maxColumnWidth = this.mHwColumnSystem.getMaxColumnWidth();
        int minColumnWidth = this.mHwColumnSystem.getMinColumnWidth();
        if (measuredWidth > maxColumnWidth) {
            return maxColumnWidth;
        }
        return measuredWidth < minColumnWidth ? minColumnWidth : measuredWidth;
    }
}
