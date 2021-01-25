package com.huawei.server.magicwin;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.HwMwUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.HwMagicContainer;
import com.android.server.wm.HwMagicWinManager;
import com.android.server.wm.HwMultiWindowSplitUI;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.IWallpaperManagerCallbackEx;
import com.huawei.android.app.WallpaperManagerExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.LayoutParamsExt;
import com.huawei.android.widget.ToastEx;
import com.huawei.libcore.io.IoUtilsEx;
import com.huawei.utils.HwPartResourceUtils;
import java.io.FileDescriptor;
import java.util.Locale;

public class HwMagicWindowUIController {
    private static final int ANIMATION_DELAY = 500;
    private static final float CIRCLE_ALPHA_END = 0.0f;
    private static final float CIRCLE_ALPHA_MIDDLE = 1.0f;
    private static final float CIRCLE_ALPHA_START = 0.5f;
    private static final int CIRCLE_ALPHA_TIME = 600;
    private static final float CIRCLE_SCALE_END = 1.5f;
    private static final float CIRCLE_SCALE_MIDDLE = 0.5f;
    private static final float CIRCLE_SCALE_START = 1.0f;
    private static final int CIRCLE_SCALE_TIME = 600;
    private static final float CIRCLE_TRANS_END = -26.0f;
    private static final float CIRCLE_TRANS_START = -14.0f;
    private static final int CIRCLE_TRANS_TIME = 1000;
    private static final int CONTENT_POSITION_ERROR = 0;
    private static final String CONTENT_RELPACE_LEFT_CHARACTER = "<a>";
    private static final String CONTENT_RELPACE_RIGHT_CHARACTER = "</a>";
    private static final float DIP2PX_REF = 0.5f;
    private static final float DRAG_BAR_ALPHA_END = 1.0f;
    private static final float DRAG_BAR_ALPHA_START = 0.0f;
    public static final int DURATION_ADD_SPLIT_BAR = 200;
    private static final int IS_REMINDER = 1;
    private static final int KEYWORD_DEFAULT_LENGTH = 3;
    private static final int KEYWORD_POSITION_ERROR = -1;
    private static final String KEY_NO_MORE_REMINDER = "key_no_more_reminder";
    private static final int MSG_DISMISS_DIALOG = 4;
    public static final int MSG_FORCE_UPDATE_SPLIT_BAR = 5;
    public static final int MSG_SET_WALLPAPER = 1;
    private static final int MSG_SHOW_DIALOG = 2;
    private static final int MSG_UPDATE_BG_COLOR = 6;
    public static final int MSG_UPDATE_DRAG_VIEW_VISIBLE = 3;
    public static final int MSG_UPDATE_WALLPAPER_VISIBILITY = 0;
    private static final int NOT_REMINDER = 0;
    private static final int PAGE_ANIMATION_TIME = 600;
    private static final float PAGE_LEFT_TRANS_END = -54.9f;
    private static final float PAGE_LEFT_TRANS_START = 0.0f;
    private static final float PAGE_RIGHT_ALPHA_END = 1.0f;
    private static final float PAGE_RIGHT_ALPHA_START = 0.0f;
    private static final float PAGE_RIGHT_SCALE_END = 1.0f;
    private static final float PAGE_RIGHT_SCALE_START = 0.4f;
    private static final String SETTINGS_INTNET_ACTION = "android.settings.MAGICWINDOW_SETTINGS";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String TAG = "HWMW_HwMagicWindowUIController";
    private static final int TAH_DELAY_ANIMATION = 1;
    private static final int THEME_EMUI_DIALOG_ALERT = 33947691;
    private ActivityTaskManagerServiceEx mAtmsEx = null;
    private Bitmap mBmpGauss = null;
    private boolean mCheckBoxStatus = false;
    private ImageView mCircleIv = null;
    private ClickableSpan mClickableSpan = new ClickableSpan() {
        /* class com.huawei.server.magicwin.HwMagicWindowUIController.AnonymousClass4 */

        @Override // android.text.style.ClickableSpan
        public void onClick(View view) {
            if (HwMagicWindowUIController.this.mDialog != null) {
                HwMagicWindowUIController.this.mDialog.dismiss();
            }
            Intent intent = new Intent(HwMagicWindowUIController.SETTINGS_INTNET_ACTION);
            intent.setPackage(HwMagicWindowUIController.SETTINGS_PACKAGE_NAME);
            intent.addFlags(268435456);
            try {
                ContextEx.startActivityAsUser(HwMagicWindowUIController.this.mContext, intent, UserHandleEx.CURRENT);
            } catch (ActivityNotFoundException ex) {
                SlogEx.v(HwMagicWindowUIController.TAG, "startActivity failed! message : " + ex.getMessage());
            }
        }

        @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
        public void updateDrawState(TextPaint ds) {
        }
    };
    private Context mContext = null;
    private AlertDialog mDialog;
    private View mDragBarView = null;
    private HwMagicWinManager mMwManager = null;
    private SparseArray<HwMagicWindowUI> mMwUIs = null;
    private String mPackageName = null;
    private ImageView mPageLeftIv = null;
    private ImageView mPageRightIv = null;
    private Handler mUIHandler = new Handler(ActivityThreadEx.currentActivityThread().getLooper()) {
        /* class com.huawei.server.magicwin.HwMagicWindowUIController.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwMagicWindowUIController.this.handleUpdateMwBackground(msg);
                    return;
                case 1:
                    HwMagicWindowUIController.this.handleSetMwBackground(msg);
                    return;
                case 2:
                    if (msg.obj instanceof String) {
                        HwMagicWindowUIController.this.showDialog((String) msg.obj);
                        return;
                    }
                    return;
                case 3:
                    HwMagicWindowUI mwUi = HwMagicWindowUIController.this.getMwUi(msg.arg2);
                    if (mwUi != null) {
                        mwUi.updateDragViewVisibility();
                        return;
                    }
                    return;
                case 4:
                    if (HwMagicWindowUIController.this.mDialog != null && HwMagicWindowUIController.this.mDialog.isShowing()) {
                        HwMagicWindowUIController.this.mDialog.dismiss();
                    }
                    HwMagicWindowUIController.this.mDialog = null;
                    return;
                case 5:
                    HwMagicWindowUIController.this.handleForceUpdateSplitBar(msg);
                    return;
                case 6:
                    HwMagicWindowUI mwUi2 = HwMagicWindowUIController.this.getMwUi(msg.arg2);
                    if (mwUi2 != null) {
                        mwUi2.setBgColor();
                        return;
                    }
                    return;
                default:
                    SlogEx.e(HwMagicWindowUIController.TAG, "msg.what error : " + msg.what);
                    return;
            }
        }
    };
    private IWallpaperManagerCallbackEx mWallpaperCallback = null;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateMwBackground(Message msg) {
        HwMagicWindowUI mwUi = getMwUi(msg.arg2);
        if (mwUi != null) {
            mwUi.changeMagicWindowWallpaper(msg.arg1 | 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetMwBackground(Message msg) {
        if (msg.obj instanceof Boolean) {
            boolean isMiddle = ((Boolean) msg.obj).booleanValue();
            HwMagicWindowUI mwUi = getMwUi(msg.arg2);
            if (mwUi != null) {
                mwUi.changeMagicWindowWallpaper(isMiddle ? 2 : 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForceUpdateSplitBar(Message msg) {
        if (msg.obj instanceof Boolean) {
            boolean isVisible = ((Boolean) msg.obj).booleanValue();
            HwMagicWindowUI mwUi = getMwUi(msg.arg2);
            if (mwUi != null) {
                mwUi.forceUpdateSplitBar(isVisible);
            }
        }
    }

    public HwMagicWindowUIController(HwMagicWinManager manager, Context context, ActivityTaskManagerServiceEx atmsEx) {
        this.mContext = context;
        this.mMwManager = manager;
        this.mAtmsEx = atmsEx;
        this.mWallpaperCallback = new WallpaperCallback();
        initMwUI(context);
    }

    private void initMwUI(Context context) {
        this.mMwUIs = new SparseArray<>();
    }

    public void onContainerRelease(int displayId) {
        this.mMwUIs.remove(displayId);
    }

    public HwMagicWindowUI createMwUi(int displayId, HwMagicContainer container) {
        HwMagicWindowUI mwUi = this.mMwUIs.get(displayId);
        if (mwUi == null) {
            if (displayId != 0) {
                mwUi = initRemoteMwUI(displayId, container);
            } else {
                mwUi = initLocalMwUI(container);
            }
            this.mMwUIs.put(displayId, mwUi);
            SlogEx.i(TAG, "Create a new mw ui with display id = " + displayId);
        }
        return mwUi;
    }

    private HwMagicWindowUI initLocalMwUI(HwMagicContainer container) {
        HwMagicWindowUI mwUI4default = new HwMagicWindowUI(this.mContext, container, this.mMwManager, this);
        mwUI4default.setHwMultiWindowSplitUI(HwMultiWindowSplitUI.getInstance(this.mContext, this.mAtmsEx, 0));
        mwUI4default.registerGestureHomeAnimatorObserver();
        return mwUI4default;
    }

    private HwMagicWindowUI initRemoteMwUI(int displayId, HwMagicContainer container) {
        if (displayId <= 0) {
            return new HwMagicWindowUI(container, this.mMwManager, this);
        }
        Context context = createVirtualContext(displayId);
        HwMagicWindowUI mwUi = new HwMagicWindowUI(context, container, this.mMwManager, this);
        mwUi.setHwMultiWindowSplitUI(HwMultiWindowSplitUI.getInstance(context, this.mAtmsEx, displayId));
        return mwUi;
    }

    private Context createVirtualContext(int displayId) {
        return this.mContext.createDisplayContext(((DisplayManager) this.mContext.getSystemService("display")).getDisplay(displayId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HwMagicWindowUI getMwUi(int displayId) {
        HwMagicWindowUI mwUi = this.mMwUIs.get(displayId);
        if (HwMwUtils.MAGICWIN_LOG_SWITCH && mwUi == null) {
            SlogEx.i(TAG, "No MW UI available now with display id = " + displayId);
        }
        return mwUi;
    }

    public Bitmap getBmpGauss() {
        return this.mBmpGauss;
    }

    public Handler getUiHandler() {
        return this.mUIHandler;
    }

    public Bitmap getWallpaperScreenShot(int displayId) {
        SlogEx.i(TAG, "getWallpaperScreenShot did=" + displayId);
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            return mwUi.getWallpaperScreenShot();
        }
        return null;
    }

    public void updateMwWallpaperVisibility(boolean isVisible, int displayId, boolean isDisableAnim) {
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            SlogEx.i(TAG, "update MW Wallpaper Visibility:did=" + displayId + ", iv=" + isVisible);
            mwUi.updateMwWallpaperVisibility(isVisible, displayId, isDisableAnim);
        }
    }

    public void updateMwWallpaperVisibilityIfNeed(boolean isVisible, int displayId) {
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            mwUi.updateMwWallpaperVisibilityIfNeed(isVisible, displayId);
        }
    }

    public void hideMwWallpaperInNeed(int displayId) {
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            mwUi.hideMwWallpaperInNeed();
        }
    }

    public void onUserSwitch() {
        for (int i = 0; i < this.mMwUIs.size(); i++) {
            this.mMwUIs.valueAt(i).onUserSwitch();
        }
    }

    public void changeWallpaper(boolean isMiddle, int displayId) {
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            mwUi.changeWallpaper(isMiddle);
        }
    }

    public void updateSplitBarVisibility(boolean isVisible, int displayId) {
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            mwUi.updateSplitBarVisibility(isVisible);
        }
    }

    public void updateSplitBarVisibility(boolean isVisible, boolean isNeedDelayed, int displayId) {
        HwMagicWindowUI mwUi = getMwUi(displayId);
        if (mwUi != null) {
            mwUi.updateSplitBarVisibility(isVisible, isNeedDelayed);
        }
    }

    public void setNeedUpdateWallpaperSize(boolean isUpdateWallPaper) {
        for (int i = 0; i < this.mMwUIs.size(); i++) {
            this.mMwUIs.valueAt(i).setIsNeedUpdateWallPaperSize(isUpdateWallPaper);
        }
    }

    public void updateBgColor(int displayId) {
        Message msg = this.mUIHandler.obtainMessage(6);
        msg.arg2 = displayId;
        this.mUIHandler.removeMessages(6);
        this.mUIHandler.sendMessage(msg);
    }

    public void whetherShowDialog(String packageName) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && !container.isFoldableDevice()) {
            boolean isNeedShowForPackage = true;
            boolean hasReminder = SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), KEY_NO_MORE_REMINDER, 0, ActivityManagerEx.getCurrentUser()) == 1;
            AlertDialog alertDialog = this.mDialog;
            boolean isDialogShowing = alertDialog != null && alertDialog.isShowing();
            if (container.getConfig().getDialogShownForApp(packageName) || !container.getHwMagicWinEnabled(packageName)) {
                isNeedShowForPackage = false;
            }
            if (!hasReminder && !isDialogShowing && isNeedShowForPackage) {
                Message msg = this.mUIHandler.obtainMessage();
                msg.what = 2;
                msg.obj = packageName;
                this.mUIHandler.sendMessage(msg);
            }
        }
    }

    private void initDialog(AlertDialog.Builder builder, ScrollView magicWinTipsViewRoot) {
        final HwMagicContainer container = this.mMwManager.getLocalContainer();
        this.mDialog = builder.setCancelable(false).setView(magicWinTipsViewRoot).setPositiveButton(HwPartResourceUtils.getResourceId("magic_window_confirm"), new DialogInterface.OnClickListener() {
            /* class com.huawei.server.magicwin.HwMagicWindowUIController.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (HwMagicWindowUIController.this.mCheckBoxStatus) {
                    SettingsEx.System.putIntForUser(HwMagicWindowUIController.this.mContext.getContentResolver(), HwMagicWindowUIController.KEY_NO_MORE_REMINDER, 1, ActivityManagerEx.getCurrentUser());
                }
                container.getConfig().setDialogShownForApp(HwMagicWindowUIController.this.mPackageName, true);
            }
        }).create();
    }

    private ScrollView inflateMagicWindowTipsLayout(AlertDialog.Builder buider) {
        View inflateView = LayoutInflater.from(buider.getContext()).inflate(HwPartResourceUtils.getResourceId("magic_window_tips"), (ViewGroup) null);
        if (inflateView == null || !(inflateView instanceof ScrollView)) {
            return null;
        }
        return (ScrollView) inflateView;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDialog(String packageName) {
        AlertDialog.Builder builder;
        ScrollView magicWinTipsViewRoot;
        this.mPackageName = packageName;
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container.getAppSupportMode(this.mPackageName) != 0) {
            AlertDialog alertDialog = this.mDialog;
            if ((alertDialog == null || !alertDialog.isShowing()) && (magicWinTipsViewRoot = inflateMagicWindowTipsLayout((builder = new AlertDialog.Builder(this.mContext, THEME_EMUI_DIALOG_ALERT)))) != null) {
                this.mCircleIv = (ImageView) magicWinTipsViewRoot.findViewById(HwPartResourceUtils.getResourceId("tips_circle"));
                this.mDragBarView = magicWinTipsViewRoot.findViewById(HwPartResourceUtils.getResourceId("tips_dragBar"));
                this.mPageLeftIv = (ImageView) magicWinTipsViewRoot.findViewById(HwPartResourceUtils.getResourceId("tips_pageLeft"));
                this.mPageRightIv = (ImageView) magicWinTipsViewRoot.findViewById(HwPartResourceUtils.getResourceId("tips_pageRight"));
                TextView goTv = (TextView) magicWinTipsViewRoot.findViewById(HwPartResourceUtils.getResourceId("tips_go_settings_magicwin"));
                CheckBox cb = (CheckBox) magicWinTipsViewRoot.findViewById(HwPartResourceUtils.getResourceId("tips_cb"));
                initDialog(builder, magicWinTipsViewRoot);
                if (this.mCircleIv != null && this.mDragBarView != null && this.mPageLeftIv != null && this.mPageRightIv != null && goTv != null && cb != null && this.mDialog != null) {
                    goTv.setHighlightColor(this.mContext.getResources().getColor(17170445));
                    setClickableSpanForTextView(goTv, getKeywords(goTv.getText().toString()), this.mClickableSpan, this.mContext);
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        /* class com.huawei.server.magicwin.HwMagicWindowUIController.AnonymousClass3 */

                        @Override // android.widget.CompoundButton.OnCheckedChangeListener
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            HwMagicWindowUIController.this.mCheckBoxStatus = isChecked;
                        }
                    });
                    LayoutParamsExt.orPrivateFlags(this.mDialog.getWindow().getAttributes(), 16);
                    this.mDialog.getWindow().setType(2003);
                    this.mDialog.getWindow().getAttributes().setTitle("MagicWindowGuideDialog");
                    this.mCheckBoxStatus = true;
                    this.mDialog.show();
                    startDialogAnimation(this.mCircleIv, this.mPageLeftIv, this.mPageRightIv, this.mDragBarView);
                }
            }
        } else if (!container.getConfig().getDialogShownForApp(this.mPackageName)) {
            Toast toast = Toast.makeText(this.mContext, HwPartResourceUtils.getResourceId("magic_window_toast"), 0);
            LayoutParamsExt.orPrivateFlags(ToastEx.getWindowParams(toast), 16);
            toast.show();
            container.getConfig().setDialogShownForApp(this.mPackageName, true);
        }
    }

    public void dismissDialog() {
        this.mUIHandler.sendMessage(this.mUIHandler.obtainMessage(4));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0093: APUT  
      (r7v1 float[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: float : 0x0092: CAST (r3v3 float) = (float) (r3v2 int))
     */
    private void startDialogAnimation(ImageView circleIv, ImageView pageLeftIv, ImageView pageRightIv, View dragBarIv) {
        ObjectAnimator circleIvTranslateX = ObjectAnimator.ofFloat(circleIv, "translationX", (float) dip2px(Math.abs((float) CIRCLE_TRANS_START)), 0.0f);
        ObjectAnimator circleIvTranslateY = ObjectAnimator.ofFloat(circleIv, "translationY", (float) dip2px(CIRCLE_TRANS_START), (float) dip2px(CIRCLE_TRANS_END));
        ObjectAnimator circleIvScaleX = ObjectAnimator.ofFloat(circleIv, "scaleX", 1.0f, 0.5f, CIRCLE_SCALE_END);
        ObjectAnimator circleIvScaleY = ObjectAnimator.ofFloat(circleIv, "scaleY", 1.0f, 0.5f, CIRCLE_SCALE_END);
        circleIvTranslateX.setDuration(1000L);
        circleIvTranslateY.setDuration(1000L);
        circleIvScaleX.setDuration(600L);
        circleIvScaleY.setDuration(600L);
        ObjectAnimator circleIvAlpha = ObjectAnimator.ofFloat(circleIv, "alpha", 0.5f, 1.0f, HwMagicWinAnimation.INVALID_THRESHOLD);
        circleIvAlpha.setDuration(600L);
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
        float[] fArr = new float[2];
        fArr[0] = 0.0f;
        fArr[1] = (float) (isRtl ? -dip2px(PAGE_LEFT_TRANS_END) : dip2px(PAGE_LEFT_TRANS_END));
        ObjectAnimator pageLeftIvTranslate = ObjectAnimator.ofFloat(pageLeftIv, "translationX", fArr);
        pageLeftIvTranslate.setDuration(600L);
        ObjectAnimator pageRightIvAlpha = ObjectAnimator.ofFloat(pageRightIv, "alpha", HwMagicWinAnimation.INVALID_THRESHOLD, 1.0f);
        ObjectAnimator pageRightIvScaleX = ObjectAnimator.ofFloat(pageRightIv, "scaleX", PAGE_RIGHT_SCALE_START, 1.0f);
        ObjectAnimator pageRightIvScaleY = ObjectAnimator.ofFloat(pageRightIv, "scaleY", PAGE_RIGHT_SCALE_START, 1.0f);
        pageRightIvAlpha.setDuration(600L);
        pageRightIvScaleX.setDuration(600L);
        pageRightIvScaleY.setDuration(600L);
        ObjectAnimator barIvAlpha = ObjectAnimator.ofFloat(dragBarIv, "alpha", HwMagicWinAnimation.INVALID_THRESHOLD, 1.0f);
        barIvAlpha.setDuration(600L);
        AnimatorSet animSetBar = new AnimatorSet();
        animSetBar.play(barIvAlpha);
        AnimatorSet animSetPage = new AnimatorSet();
        animSetPage.play(pageLeftIvTranslate).with(pageRightIvAlpha).with(pageRightIvScaleX).with(pageRightIvScaleY).before(animSetBar);
        AnimatorSet animSetCircle = new AnimatorSet();
        animSetCircle.play(circleIvScaleX).with(circleIvScaleY).with(circleIvAlpha).after(circleIvTranslateX).after(circleIvTranslateY).before(animSetPage).after(500);
        animSetCircle.start();
    }

    private int dip2px(float dpValue) {
        return (int) ((dpValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private String getKeywords(String information) {
        int keywordStartPosition = information.indexOf(CONTENT_RELPACE_LEFT_CHARACTER);
        int keywordEndPosition = information.indexOf(CONTENT_RELPACE_RIGHT_CHARACTER);
        if (keywordStartPosition == -1 || keywordEndPosition == -1 || keywordEndPosition < keywordStartPosition) {
            return "";
        }
        return information.substring(keywordStartPosition + 3, keywordEndPosition);
    }

    private void setClickableSpanForTextView(TextView tv, String linkStr, ClickableSpan clickableSpan, Context context) {
        if (tv != null && !TextUtils.isEmpty(linkStr)) {
            String content = tv.getText().toString();
            Locale defaultLocale = Locale.getDefault();
            if (!content.toLowerCase(defaultLocale).contains(linkStr.toLowerCase(defaultLocale))) {
                content = content + " " + linkStr;
            }
            String content2 = content.replaceAll(CONTENT_RELPACE_LEFT_CHARACTER, "").replaceAll(CONTENT_RELPACE_RIGHT_CHARACTER, "");
            int start = content2.toLowerCase(defaultLocale).lastIndexOf(linkStr.toLowerCase(defaultLocale));
            int end = linkStr.length() + start;
            if (start >= 0 && start < end && end <= content2.length()) {
                SpannableStringBuilder sp = new SpannableStringBuilder(content2);
                sp.setSpan(clickableSpan, start, end, 33);
                sp.setSpan(new ForegroundColorSpan(this.mContext.getResources().getColor(33882525)), start, end, 34);
                tv.setText(sp);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public void initWallpaperGaussBmp() {
        FileDescriptor fileDescriptor;
        SlogEx.i(TAG, "Init MW Wallpaper Gauss Bmp");
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.mContext);
        if (wallpaperManager == null || WallpaperManagerExt.isIWallpaperManagerNull(wallpaperManager)) {
            SlogEx.e(TAG, "wallpaperManager is a null object or wallpaperManager.getIWallpaperManager() = null ");
            return;
        }
        try {
            ParcelFileDescriptor fd = WallpaperManagerExt.getBlurWallpaper(wallpaperManager, this.mWallpaperCallback);
            if (fd == null) {
                SlogEx.e(TAG, "getBlurWallpaper(), fd = null");
                return;
            }
            try {
                this.mBmpGauss = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new BitmapFactory.Options());
                SlogEx.i(TAG, "MW Wallpaper Gauss Bmp initialized");
                fileDescriptor = fd.getFileDescriptor();
            } catch (OutOfMemoryError e) {
                SlogEx.w(TAG, "Can't decode file\n" + Log.getStackTraceString(e));
                fileDescriptor = fd.getFileDescriptor();
            } catch (Throwable th) {
                IoUtilsEx.closeQuietly(fd.getFileDescriptor());
                throw th;
            }
            IoUtilsEx.closeQuietly(fileDescriptor);
        } catch (RemoteException re) {
            SlogEx.w(TAG, "Can't getWallpaper\n" + Log.getStackTraceString(re));
        }
    }

    private class WallpaperCallback extends IWallpaperManagerCallbackEx {
        private WallpaperCallback() {
        }

        public void onWallpaperChanged() throws RemoteException {
        }

        public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) throws RemoteException {
        }

        public void onBlurWallpaperChanged() throws RemoteException {
            if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                SlogEx.d(HwMagicWindowUIController.TAG, "onBlurWallpaperChanged()");
            }
            HwMagicWindowUIController.this.mMwManager.getHandler().sendEmptyMessage(1);
        }
    }
}
