package huawei.android.widget;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.FreezeScreenScene;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.view.ButtonWindowTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.huawei.hsm.permission.StubController;
import java.util.List;

public class HwChangeButtonWindow extends LinearLayout {
    public static final String ENABLE_NAVBAR = "enable_navbar";
    public static final boolean IS_FRONT_NAV = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int LAMD_MARGIN_START = 1;
    private static final float LAYOUT_WIDTH = 24.0f;
    private static final int LINEAR_LAYOUT_BACKGROUND = -16777216;
    private static String PACKGE_NAME = "package_name";
    private static final int PORTRAIT_MARGIN_START = 8;
    private static final float SIZE = 40.0f;
    private static final String TAG = "HwChangeButtonWindow";
    private static final int TEXT_COLOR = -15884293;
    private static final float TEXT_SIZE = 15.0f;
    private static final String TITLE = "SetFullScreenWindow";
    private RelativeLayout clickLayout;
    private boolean isAddView;
    private boolean isHide;
    private ActivityManager mActivityManager;
    private Activity mContext;
    private View mLayout;
    private LinearLayout mLayoutLinear;
    private LayoutParams mLayoutParams;
    private OnClickListener mListener = new OnClickListener() {
        public void onClick(View v) {
            HwChangeButtonWindow.this.toastInLockTaskMode();
            Intent intent = new Intent();
            intent.putExtra(HwChangeButtonWindow.PACKGE_NAME, HwChangeButtonWindow.this.mContext.getPackageName());
            intent.setAction("com.huawei.intent.action.HwChangeButton");
            intent.addFlags(268435456);
            long identity = Binder.clearCallingIdentity();
            try {
                HwChangeButtonWindow.this.mContext.getApplicationContext().startActivity(intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    };
    private boolean mNavBarEnable = false;
    private ButtonWindowTextView mTextView;
    private Toast mToast;

    public HwChangeButtonWindow(Activity a) {
        super(a);
        this.mContext = a;
        inflateView(null);
        this.mLayoutParams = new LayoutParams(-1, -2, 99, 134612776, -3);
        LayoutParams layoutParams = this.mLayoutParams;
        layoutParams.hwFlags |= StubController.PERMISSION_CALL_FORWARD;
        this.mLayoutParams.gravity = 8388659;
        this.mLayoutParams.setTitle(TITLE);
    }

    private void updateLayoutParams(Rect appBounds) {
        int size = (dip2px(this.mContext, SIZE) + getNavigationBarHeight()) + 1;
        int showInCenter = IS_FRONT_NAV ? (System.getInt(this.mContext.getContentResolver(), "enable_navbar", 0) != 0) ^ 1 : 0;
        RelativeLayout.LayoutParams layoutParams = null;
        if (this.clickLayout != null) {
            layoutParams = (RelativeLayout.LayoutParams) this.clickLayout.getLayoutParams();
        }
        if (appBounds.width() < appBounds.height()) {
            this.mLayoutParams.width = -1;
            this.mLayoutParams.height = size;
            this.mLayoutParams.x = 0;
            this.mLayoutParams.y = appBounds.height();
            if (!(layoutParams == null || (showInCenter ^ 1) == 0)) {
                layoutParams.addRule(10);
            }
        } else {
            this.mLayoutParams.width = size;
            this.mLayoutParams.height = -1;
            this.mLayoutParams.x = appBounds.width();
            this.mLayoutParams.y = 0;
            if (!(layoutParams == null || (showInCenter ^ 1) == 0)) {
                layoutParams.addRule(9);
            }
        }
        if (layoutParams != null && showInCenter != 0) {
            layoutParams.addRule(13);
        }
    }

    public void addView(WindowManager wm, Rect appBounds) {
        updateLayoutParams(appBounds);
        wm.addView(this.mLayout, this.mLayoutParams);
        this.isAddView = true;
    }

    public void removeView(WindowManager wm) {
        wm.removeViewImmediate(this.mLayout);
        this.isAddView = false;
    }

    private int getNavigationBarHeight() {
        try {
            return this.mContext.getApplicationContext().getResources().getDimensionPixelSize(17105141);
        } catch (Exception e) {
            return 0;
        }
    }

    private void inflateView(Rect appBounds) {
        LinearLayout.LayoutParams textLayoutParams;
        this.mLayout = LayoutInflater.from(this.mContext).inflate(34013285, null);
        Configuration config = this.mContext.getResources().getConfiguration();
        Configuration applicationconfig = this.mContext.getApplicationContext().getResources().getConfiguration();
        if (config.orientation != applicationconfig.orientation) {
            config = applicationconfig;
            this.mLayout = LayoutInflater.from(this.mContext.getApplicationContext()).inflate(34013285, null);
        }
        this.mLayoutLinear = (LinearLayout) this.mLayout.findViewById(34603199);
        boolean verticalscape = appBounds != null ? appBounds.width() < appBounds.height() : config.orientation == 1;
        if (verticalscape) {
            try {
                this.mTextView = new ButtonWindowTextView(this.mContext, 1);
            } catch (Exception e) {
                this.mTextView = new ButtonWindowTextView(this.mContext.getApplicationContext(), 1);
            }
            this.mTextView.setText(33685950);
            textLayoutParams = new LinearLayout.LayoutParams(-2, -2);
            textLayoutParams.setMarginStart(dip2px(this.mContext, 8.0f));
        } else {
            try {
                this.mTextView = new ButtonWindowTextView(this.mContext, 2);
            } catch (Exception e2) {
                this.mTextView = new ButtonWindowTextView(this.mContext.getApplicationContext(), 2);
            }
            textLayoutParams = new LinearLayout.LayoutParams(dip2px(this.mContext, LAYOUT_WIDTH), -2);
            textLayoutParams.bottomMargin = dip2px(this.mContext, 8.0f);
            textLayoutParams.leftMargin = dip2px(this.mContext, 1.0f);
            this.mLayoutLinear.setGravity(17);
            this.mTextView.setGravity(49);
        }
        if (this.mTextView != null) {
            this.mTextView.setBackgroundColor(-16777216);
            this.mTextView.setTextColor(TEXT_COLOR);
            this.mTextView.setTextSize(1, TEXT_SIZE);
            this.mTextView.setText(getResourcesByLocale(this.mContext, 33685950));
            if (verticalscape) {
                this.mLayoutLinear.addView(this.mTextView, textLayoutParams);
            } else {
                this.mLayoutLinear.addView(this.mTextView, 0, textLayoutParams);
            }
        }
        this.mLayout.setBackgroundColor(-16777216);
        this.clickLayout = (RelativeLayout) this.mLayout.findViewById(34603042);
        if (this.clickLayout != null) {
            this.clickLayout.setBackgroundColor(-16777216);
            this.clickLayout.setOnClickListener(this.mListener);
        }
    }

    public void setViewHide(boolean isHide) {
        this.mLayout.setVisibility(isHide ? 4 : 0);
        this.isHide = isHide;
    }

    public boolean isHide() {
        return this.isHide;
    }

    public boolean isAddView() {
        return this.isAddView;
    }

    private int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public boolean isToOtherApp() {
        List<RunningTaskInfo> infos = ((ActivityManager) this.mContext.getSystemService(FreezeScreenScene.ACTIVITY_PARAM)).getRunningTasks(1);
        if (infos == null) {
            return false;
        }
        return this.mContext.getPackageName().equals(((RunningTaskInfo) infos.get(0)).topActivity.getPackageName()) ^ 1;
    }

    public void updateView(WindowManager wm, Rect appBounds) {
        removeView(wm);
        inflateView(appBounds);
        addView(wm, appBounds);
    }

    private String getResourcesByLocale(Context context, int id) {
        Configuration config = getResources().getConfiguration();
        if (VERSION.SDK_INT >= 24) {
            config.setLocale(LocaleList.getDefault().get(0));
            context = context.createConfigurationContext(config);
        }
        return context.getString(id);
    }

    private void toastInLockTaskMode() {
        if (this.mToast != null) {
            this.mToast.cancel();
        }
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService(FreezeScreenScene.ACTIVITY_PARAM);
        }
        if (this.mActivityManager.getLockTaskModeState() != 0) {
            this.mToast = Toast.makeText(this.mContext, 17040266, 0);
            this.mToast.show();
        }
    }

    public void setNavBarState(boolean currentState) {
        this.mNavBarEnable = currentState;
    }

    public boolean getNavBarState() {
        return this.mNavBarEnable;
    }
}
