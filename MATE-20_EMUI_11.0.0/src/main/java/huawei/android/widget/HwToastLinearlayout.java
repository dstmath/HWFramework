package huawei.android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import huawei.android.widget.columnsystem.HwColumnSystem;

@RemoteViews.RemoteView
public class HwToastLinearlayout extends LinearLayout {
    private static final int DEVICE_TYPE_PHONE = 1;
    private static final int DEVICE_TYPE_WATCH = 8;
    private int mDeviceType;
    private LinearLayout mToastLinearLayout;
    private TextView mToastTextView;

    public HwToastLinearlayout(Context context) {
        this(context, null);
    }

    public HwToastLinearlayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwToastLinearlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwToastLinearlayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDeviceType = 1;
        Object object = context.getSystemService("layout_inflater");
        if (object instanceof LayoutInflater) {
            ((LayoutInflater) object).inflate(34013279, (ViewGroup) this, true);
            this.mToastLinearLayout = (LinearLayout) findViewById(34603199);
            this.mToastTextView = (TextView) findViewById(16908299);
            this.mDeviceType = context.getResources().getInteger(34275378);
            setHwToastWidthColumnLimits();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setHwToastWidthColumnLimits();
    }

    private void setHwToastWidthColumnLimits() {
        if (this.mDeviceType != 8) {
            if (this.mToastLinearLayout == null || this.mToastTextView == null) {
                Log.e("HwToastLinearlayout", "setHwToastWidthColumnLimits init fail!");
                return;
            }
            HwColumnSystem hwColumnSystem = new HwColumnSystem(getContext());
            hwColumnSystem.setColumnType(5);
            int maxWidthToast = hwColumnSystem.getMaxColumnWidth();
            int minWidthToast = hwColumnSystem.getMinColumnWidth();
            Drawable toastBackground = this.mToastLinearLayout.getBackground();
            Rect padding = new Rect();
            int maxWidthToastTextview = maxWidthToast;
            if (toastBackground.getPadding(padding)) {
                maxWidthToastTextview = maxWidthToast - (padding.left + padding.right);
            }
            if (maxWidthToast > 0 && minWidthToast > 0 && maxWidthToastTextview > 0) {
                this.mToastTextView.setMaxWidth(maxWidthToastTextview);
                this.mToastLinearLayout.setMinimumWidth(minWidthToast);
            }
        }
    }
}
