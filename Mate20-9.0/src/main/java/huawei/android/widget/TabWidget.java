package huawei.android.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import huawei.com.android.internal.widget.HwWidgetUtils;

public class TabWidget extends android.widget.TabWidget {
    private Context mContext;

    public TabWidget(Context context) {
        super(context);
        initHwTabWidget(context);
    }

    public TabWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHwTabWidget(context);
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initHwTabWidget(context);
    }

    private void initHwTabWidget(Context context) {
        this.mContext = context;
        setDividerDrawable(null);
        if (HwWidgetUtils.isActionbarBackgroundThemed(context)) {
            setBackgroundDrawable(this.mContext.getResources().getDrawable(33751393));
        } else if (!HwWidgetFactory.isHwDarkTheme(context)) {
            int color = HwWidgetFactory.getPrimaryColor(context);
            if (Color.alpha(color) != 0) {
                setBackgroundDrawable(new ColorDrawable(color));
            }
        }
    }
}
