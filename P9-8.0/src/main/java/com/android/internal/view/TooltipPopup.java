package com.android.internal.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.rms.AppAssociate;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.widget.TextView;
import com.android.internal.R;

public class TooltipPopup {
    private static final String TAG = "TooltipPopup";
    private final View mContentView;
    private final Context mContext;
    private final LayoutParams mLayoutParams = new LayoutParams();
    private final TextView mMessageView;
    private final int[] mTmpAnchorPos = new int[2];
    private final int[] mTmpAppPos = new int[2];
    private final Rect mTmpDisplayFrame = new Rect();

    public TooltipPopup(Context context) {
        this.mContext = context;
        this.mContentView = LayoutInflater.from(this.mContext).inflate((int) R.layout.tooltip, null);
        this.mMessageView = (TextView) this.mContentView.findViewById(R.id.message);
        this.mLayoutParams.setTitle(this.mContext.getString(R.string.tooltip_popup_title));
        this.mLayoutParams.packageName = this.mContext.getOpPackageName();
        this.mLayoutParams.type = 1005;
        this.mLayoutParams.width = -2;
        this.mLayoutParams.height = -2;
        this.mLayoutParams.format = -3;
        this.mLayoutParams.windowAnimations = R.style.Animation_Tooltip;
        this.mLayoutParams.flags = 24;
    }

    public void show(View anchorView, int anchorX, int anchorY, boolean fromTouch, CharSequence tooltipText) {
        if (isShowing()) {
            hide();
        }
        this.mMessageView.setText(tooltipText);
        computePosition(anchorView, anchorX, anchorY, fromTouch, this.mLayoutParams);
        ((WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW)).addView(this.mContentView, this.mLayoutParams);
    }

    public void hide() {
        if (isShowing()) {
            ((WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW)).removeView(this.mContentView);
        }
    }

    public View getContentView() {
        return this.mContentView;
    }

    public boolean isShowing() {
        return this.mContentView.getParent() != null;
    }

    private void computePosition(View anchorView, int anchorX, int anchorY, boolean fromTouch, LayoutParams outParams) {
        int offsetX;
        int offsetBelow;
        int offsetAbove;
        int i;
        outParams.token = anchorView.getWindowToken();
        int tooltipPreciseAnchorThreshold = this.mContext.getResources().getDimensionPixelOffset(R.dimen.tooltip_precise_anchor_threshold);
        if (anchorView.getWidth() >= tooltipPreciseAnchorThreshold) {
            offsetX = anchorX;
        } else {
            offsetX = anchorView.getWidth() / 2;
        }
        if (anchorView.getHeight() >= tooltipPreciseAnchorThreshold) {
            int offsetExtra = this.mContext.getResources().getDimensionPixelOffset(R.dimen.tooltip_precise_anchor_extra_offset);
            offsetBelow = anchorY + offsetExtra;
            offsetAbove = anchorY - offsetExtra;
        } else {
            offsetBelow = anchorView.getHeight();
            offsetAbove = 0;
        }
        outParams.gravity = 49;
        Resources resources = this.mContext.getResources();
        if (fromTouch) {
            i = R.dimen.tooltip_y_offset_touch;
        } else {
            i = R.dimen.tooltip_y_offset_non_touch;
        }
        int tooltipOffset = resources.getDimensionPixelOffset(i);
        View appView = WindowManagerGlobal.getInstance().getWindowView(anchorView.getApplicationWindowToken());
        if (appView == null) {
            Slog.e(TAG, "Cannot find app view");
            return;
        }
        appView.getWindowVisibleDisplayFrame(this.mTmpDisplayFrame);
        appView.getLocationOnScreen(this.mTmpAppPos);
        anchorView.getLocationOnScreen(this.mTmpAnchorPos);
        int[] iArr = this.mTmpAnchorPos;
        iArr[0] = iArr[0] - this.mTmpAppPos[0];
        iArr = this.mTmpAnchorPos;
        iArr[1] = iArr[1] - this.mTmpAppPos[1];
        outParams.x = (this.mTmpAnchorPos[0] + offsetX) - (this.mTmpDisplayFrame.width() / 2);
        int spec = MeasureSpec.makeMeasureSpec(0, 0);
        this.mContentView.measure(spec, spec);
        int tooltipHeight = this.mContentView.getMeasuredHeight();
        int yAbove = ((this.mTmpAnchorPos[1] + offsetAbove) - tooltipOffset) - tooltipHeight;
        int yBelow = (this.mTmpAnchorPos[1] + offsetBelow) + tooltipOffset;
        if (fromTouch) {
            if (yAbove >= 0) {
                outParams.y = yAbove;
            } else {
                outParams.y = yBelow;
            }
        } else if (yBelow + tooltipHeight <= this.mTmpDisplayFrame.height()) {
            outParams.y = yBelow;
        } else {
            outParams.y = yAbove;
        }
    }
}
