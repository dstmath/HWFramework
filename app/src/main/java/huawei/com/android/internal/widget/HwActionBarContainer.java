package huawei.com.android.internal.widget;

import android.content.Context;
import android.cover.CoverManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.internal.widget.ActionBarContainer;
import com.android.internal.widget.ActionBarView;

public class HwActionBarContainer extends ActionBarContainer {
    private int mColor;
    private boolean mDisplayNoSplitLine;
    private boolean mIsStack;

    public HwActionBarContainer(Context context) {
        this(context, null);
    }

    public HwActionBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColors(context, false);
    }

    private boolean hasTabs() {
        View tabContainer = getTabContainer();
        if (tabContainer == null || tabContainer.getVisibility() == 8) {
            return false;
        }
        return true;
    }

    private void setBackgroundColors(Context context, boolean forceRefresh) {
        if (getId() == 16909290 && !HwWidgetUtils.isActionbarBackgroundThemed(context) && !HwWidgetFactory.isHwDarkTheme(context)) {
            int color = HwWidgetFactory.getPrimaryColor(context);
            ActionBarView abv = (ActionBarView) getActionBarView();
            boolean isStack = hasTabs();
            boolean force = forceRefresh;
            if (this.mIsStack != isStack) {
                this.mIsStack = isStack;
                force = true;
            }
            if (abv != null && Color.alpha(color) != 0) {
                if (this.mColor != color || r2) {
                    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{new ColorDrawable(color)});
                    if (needSplitLine(color) && !this.mDisplayNoSplitLine) {
                        Drawable splitDrawable = context.getDrawable(33751366);
                        if (splitDrawable != null) {
                            layerDrawable.addLayer(splitDrawable);
                        } else {
                            Log.w("HwActionBarContainer", "splitDrawable is null");
                        }
                    }
                    if (isStack) {
                        setStackedBackgroundInner(layerDrawable);
                    } else {
                        setPrimaryBackgroundInner(layerDrawable);
                    }
                    this.mColor = color;
                }
            }
        }
    }

    private boolean needSplitLine(int color) {
        if (color == -197380 || color == CoverManager.DEFAULT_COLOR) {
            return true;
        }
        return false;
    }

    private void setPrimaryBackgroundInner(Drawable d) {
        if (!this.mForcedPrimaryBackground) {
            setPrimaryBackground(d);
        }
    }

    private void setStackedBackgroundInner(Drawable d) {
        if (!this.mForcedStackedBackground) {
            setStackedBackground(d);
        }
    }

    public Drawable getBackgroundDrawable() {
        if (this.mIsStack) {
            return getStackedBackground();
        }
        return getPrimaryBackground();
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!HwWidgetUtils.isActionbarBackgroundThemed(getContext())) {
            setBackgroundColors(getContext(), false);
        } else if (getId() == 16909290) {
            setBackgroundDrawable(getContext().getResources().getDrawable(33751070));
        }
    }

    public void setDisplayNoSplitLine(boolean displayNoSplitLine) {
        this.mDisplayNoSplitLine = displayNoSplitLine;
        if (this.mDisplayNoSplitLine) {
            setBackgroundColors(getContext(), true);
        }
    }

    public void setSplitViewLocation(int start, int end) {
    }
}
