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

public final class HwMagnificationGestureHandler extends MagnificationGestureHandler {
    private static final String MAGN_CHECKBOX_ISCHECKED = "magn_checkbox_ischecked";
    private static final String TAG = "HwMagnificationGestureHandler";
    private Builder mBuilder;
    private CheckBox mCheckBox;
    private Context mContext;
    private AlertDialog mDlg;
    private boolean mIsFirstShow;
    private ContextThemeWrapper mThemeContext;
    private WindowManagerInternal mWindowManagerService;

    /* renamed from: com.android.server.accessibility.HwMagnificationGestureHandler.1 */
    class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ Context val$co;

        AnonymousClass1(Context val$co) {
            this.val$co = val$co;
        }

        public void onClick(DialogInterface dialog, int which) {
            Secure.putIntForUser(this.val$co.getContentResolver(), "accessibility_display_magnification_enabled", 0, ActivityManager.getCurrentUser());
        }
    }

    public /* bridge */ /* synthetic */ void clearEvents(int inputSource) {
        super.clearEvents(inputSource);
    }

    public /* bridge */ /* synthetic */ void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);
    }

    public /* bridge */ /* synthetic */ void onDestroy() {
        super.onDestroy();
    }

    public /* bridge */ /* synthetic */ void onKeyEvent(KeyEvent event, int policyFlags) {
        super.onKeyEvent(event, policyFlags);
    }

    public /* bridge */ /* synthetic */ void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        super.onMotionEvent(event, rawEvent, policyFlags);
    }

    public /* bridge */ /* synthetic */ void setNext(EventStreamTransformation next) {
        super.setNext(next);
    }

    public HwMagnificationGestureHandler(Context context, AccessibilityManagerService service, boolean detectControlGestures) {
        super(context, service, detectControlGestures);
        this.mThemeContext = null;
        this.mBuilder = null;
        this.mDlg = null;
        this.mContext = null;
        this.mCheckBox = null;
        this.mIsFirstShow = true;
        this.mContext = context;
    }

    public boolean showMagnDialog(Context context) {
        this.mContext = context;
        if (this.mWindowManagerService == null) {
            this.mWindowManagerService = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        try {
            if (this.mIsFirstShow) {
                Secure.putIntForUser(this.mContext.getContentResolver(), MAGN_CHECKBOX_ISCHECKED, 0, ActivityManager.getCurrentUser());
                this.mIsFirstShow = false;
            }
            if (Secure.getIntForUser(context.getContentResolver(), MAGN_CHECKBOX_ISCHECKED, 0, ActivityManager.getCurrentUser()) == 1 || this.mWindowManagerService.isKeyguardLocked()) {
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
            this.mCheckBox = (CheckBox) view.findViewById(34603139);
            this.mBuilder.setView(view);
            this.mBuilder.setTitle(33685745);
            this.mBuilder.setMessage(33685746);
            this.mBuilder.setNegativeButton(33685748, new AnonymousClass1(context));
            this.mBuilder.setPositiveButton(33685749, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (HwMagnificationGestureHandler.this.mCheckBox.isChecked() && HwMagnificationGestureHandler.this.mContext != null) {
                        Secure.putIntForUser(HwMagnificationGestureHandler.this.mContext.getContentResolver(), HwMagnificationGestureHandler.MAGN_CHECKBOX_ISCHECKED, 1, ActivityManager.getCurrentUser());
                    }
                    HwMagnificationGestureHandler.this.scaleAndMagnifiedRegionCenter();
                }
            });
            this.mBuilder.setCancelable(true);
            this.mDlg = this.mBuilder.create();
            this.mDlg.getWindow().setType(2003);
            LayoutParams attributes = this.mDlg.getWindow().getAttributes();
            attributes.privateFlags |= 16;
            this.mDlg.show();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
