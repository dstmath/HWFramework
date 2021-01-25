package huawei.com.android.internal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class HwFragmentMenuItemView extends TextView {
    public static final float ALPHA_DISABLE = 0.3f;
    public static final float ALPHA_NORMAL = 1.0f;
    public static final float ALPHA_PRESSED = 0.5f;
    private static final float EXTRA_ICON_SIZE = 0.5f;
    private static final int MAX_ICON_SIZE = 32;
    private static final int MENU_TEXT_SIZE = 9;
    private float mDensity;
    private int mMaxIconSize = 0;

    public HwFragmentMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initIconSize();
    }

    public HwFragmentMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initIconSize();
    }

    public HwFragmentMenuItemView(Context context) {
        super(context);
        initIconSize();
    }

    private void initIconSize() {
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mMaxIconSize = (int) ((this.mDensity * 32.0f) + 0.5f);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        setActivated(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        if (!isActivated()) {
            setAlpha(0.3f);
        } else if (isPressed() || isFocused()) {
            setAlpha(0.5f);
        } else {
            setAlpha(1.0f);
        }
        super.onDraw(canvas);
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(getText());
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!hasText()) {
            setPadding(getPaddingLeft(), (int) (this.mDensity * 9.0f), getPaddingRight(), getPaddingBottom());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
    }

    public void setIcon(Drawable icon) {
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            int i = this.mMaxIconSize;
            if (width > i) {
                float scale = ((float) i) / ((float) width);
                width = this.mMaxIconSize;
                height = (int) (((float) height) * scale);
            }
            int i2 = this.mMaxIconSize;
            if (height > i2) {
                float scale2 = ((float) i2) / ((float) height);
                height = this.mMaxIconSize;
                width = (int) (((float) width) * scale2);
            }
            icon.setBounds(0, 0, width, height);
            setCompoundDrawables(null, icon, null, null);
        }
    }

    public void setIcon(int resId) {
        if (resId != 0) {
            setIcon(getContext().getResources().getDrawable(resId));
        }
    }

    public Drawable getIcon() {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables != null) {
            return drawables[1];
        }
        return null;
    }
}
