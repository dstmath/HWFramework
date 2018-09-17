package com.android.server.accessibility;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings.Secure;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import com.android.server.LocalServices;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;

public final class HwMagnificationGestureHandler extends MagnificationGestureHandler {
    private static final String MAGN_CHECKBOX_ISCHECKED = "magn_checkbox_ischecked";
    private static final String TAG = "HwMagnificationGestureHandler";
    private Builder mBuilder = null;
    private CheckBox mCheckBox = null;
    private Context mContext = null;
    private AlertDialog mDlg = null;
    private ContextThemeWrapper mThemeContext = null;
    private WindowManagerInternal mWindowManagerService;

    public /* bridge */ /* synthetic */ void clearEvents(int i) {
        super.clearEvents(i);
    }

    public /* bridge */ /* synthetic */ void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onAccessibilityEvent(accessibilityEvent);
    }

    public /* bridge */ /* synthetic */ void onDestroy() {
        super.onDestroy();
    }

    public /* bridge */ /* synthetic */ void onKeyEvent(KeyEvent keyEvent, int i) {
        super.onKeyEvent(keyEvent, i);
    }

    public /* bridge */ /* synthetic */ void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i) {
        super.onMotionEvent(motionEvent, motionEvent2, i);
    }

    public /* bridge */ /* synthetic */ void setNext(EventStreamTransformation eventStreamTransformation) {
        super.setNext(eventStreamTransformation);
    }

    public HwMagnificationGestureHandler(Context context, AccessibilityManagerService service, boolean detectControlGestures, boolean triggerable) {
        super(context, service, detectControlGestures, triggerable);
        this.mContext = context;
    }

    public boolean showMagnDialog(final Context context) {
        this.mContext = context;
        if (this.mWindowManagerService == null) {
            this.mWindowManagerService = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        try {
            if (Secure.getIntForUser(context.getContentResolver(), MAGN_CHECKBOX_ISCHECKED, 0, ActivityManager.getCurrentUser()) == 1 || (this.mWindowManagerService.isKeyguardLocked() ^ 1) == 0) {
                return false;
            }
            if (this.mDlg != null && this.mDlg.isShowing()) {
                return true;
            }
            int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            this.mThemeContext = new ContextThemeWrapper(context, themeID);
            this.mBuilder = new Builder(this.mThemeContext, themeID);
            View view = LayoutInflater.from(this.mBuilder.getContext()).inflate(34013231, null);
            Context co = context;
            this.mCheckBox = (CheckBox) view.findViewById(34603136);
            this.mBuilder.setView(view);
            this.mBuilder.setTitle(33685752);
            this.mBuilder.setMessage(33685753);
            this.mBuilder.setNegativeButton(33685755, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Secure.putIntForUser(context.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManager.getCurrentUser());
                }
            });
            this.mBuilder.setPositiveButton(33685756, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (HwMagnificationGestureHandler.this.mCheckBox.isChecked() && HwMagnificationGestureHandler.this.mContext != null) {
                        Secure.putIntForUser(HwMagnificationGestureHandler.this.mContext.getContentResolver(), HwMagnificationGestureHandler.MAGN_CHECKBOX_ISCHECKED, 1, ActivityManager.getCurrentUser());
                    }
                    HwMagnificationGestureHandler.this.scaleAndMagnifiedRegionCenter();
                }
            });
            this.mBuilder.setCancelable(true);
            this.mDlg = this.mBuilder.create();
            this.mDlg.getWindow().setType(DeviceStatusConstant.MSDP_DEVICE_STATUS_MOVEMENT);
            LayoutParams attributes = this.mDlg.getWindow().getAttributes();
            attributes.privateFlags |= 16;
            this.mDlg.show();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
