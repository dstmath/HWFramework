package com.huawei.server.face;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.biometric.BiometricServiceReceiverListenerEx;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.facerecognition.FaceManager;
import com.huawei.facerecognition.FaceRecognizeManager;
import com.huawei.server.fingerprint.FingerprintView;
import com.huawei.server.fingerprint.HintText;
import com.huawei.utils.HwPartResourceUtils;

public class FaceViewController {
    private static final float APP_DESCRIPTION_TEXT_SIZE = 39.0f;
    private static final float APP_DESCRIPTION_TOP_MARGIN = 6.0f;
    private static final float APP_SUBTITLE_TEXT_SIZE = 39.0f;
    private static final float APP_SUBTITLE_TOP_MARGIN = 8.0f;
    private static final String APP_SYSTEMUI_PKG_NAME = "com.android.systemui";
    private static final float APP_TITE_TEXT_SIZE = 30.0f;
    private static final float APP_TITLE_TEXT_IOP_MARGIN = 60.0f;
    private static final float APP_TITLE_TEXT_SIZE = 90.0f;
    private static final float APP_VIEW_TEXT_SIZE = 13.0f;
    private static final String BIOMETRIC_FACE_SUPPORT = "hw_biometirc_facedetect";
    private static final int BUTTOM_STROKE_WITH = 5;
    private static final float CANCEL_BUTTOM_TEXT_SIZE = 45.0f;
    private static final float CONFIRM_BUTTOM_TEXT_SIZE = 45.0f;
    private static final float DP_TO_PX_PARAMS = 0.5f;
    private static final int END_DISTANCE = 4;
    private static final int FACE_2D_LOWER = 2;
    private static final int FACE_2D_UPPER = 6;
    private static final int FACE_3D = SystemPropertiesEx.getInt("ro.config.support_face_mode", 1);
    public static final int FACE_ACQUIRED_VENDOR_BASE = 100;
    private static final int FACE_AUTH_FLAG = 7;
    private static final long FACE_DETECTED_DELAY = 500;
    private static final int FACE_DETECTING = 0;
    private static final int FACE_DETECT_SUCCESS = 1;
    private static final int FACE_INIT_FAIL = -1;
    private static final int FACE_INIT_FAIL_UPDATE_DELAY = 200;
    private static final float FACE_MESSAGE_TEXT_SIZE = 39.0f;
    private static final String FACE_RECOGNIZE_UNLOCK = "face_bind_with_lock";
    private static final float FACE_TITLE_TEXT_SIZE = 45.0f;
    private static final float FACE_TITLE_TOP_MARGIN = 50.0f;
    private static final float FINGERPRINT_HINT_TEXT_SIZE = 45.0f;
    private static final long FINGER_ICON_REMOVE_DELAY = 10;
    private static final int MIDDLE_BUTTOM_WIDTD = 480;
    public static final int MSG_FACE_DETECT_SUCCESS = 2;
    public static final int MSG_FACE_VIEW_INIT = 3;
    public static final int MSG_REMOVE_VIEW = 1;
    private static final int SCREEN_SINGLE_BUTTOM_DIV = 2;
    private static final int START_DISTANCE = 16;
    private static final int STATE_FALSE = 0;
    private static final String TAG = "FaceViewController";
    private static final int TOKEN_LENGTH = 6;
    private static final long VIEW_REMOVE_DELAY = 400;
    private static final long VIEW_RESUME_DELAY = 350;
    private static FaceViewController sInstance;
    private FaceRecognizeManager.FaceRecognizeCallback faceCallback = new FaceRecognizeManager.FaceRecognizeCallback() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass9 */

        public void onCallbackEvent(int reqId, int type, int code, int errorCode) {
            if (reqId != FaceViewController.this.mOptId) {
                Log.w(FaceViewController.TAG, "bioface reqId is wrong");
            } else if (type == 2) {
                if (code == 1) {
                    FaceViewController.this.handleCallbackResult(errorCode);
                } else if (code == 3) {
                    FaceViewController.this.handleCallbackAcquire(errorCode);
                } else {
                    Log.d(FaceViewController.TAG, "bioface Authentication code = " + code);
                }
                if (code == 1 && FaceViewController.this.mFaceRecognizeManager.release() != 0) {
                    Log.w(FaceViewController.TAG, "bioface Authentication release failed.");
                }
            }
        }
    };
    private boolean isBiomericDetecting = false;
    private boolean isFaceEnable = false;
    private boolean isInit = false;
    private boolean isNewFingerView = false;
    private TextView mAppDescriptionTextView;
    private RelativeLayout mAppRelativeLayout;
    private TextView mAppSubTitleTextView;
    private RelativeLayout mAppTitleAndSummaryView;
    private TextView mAppTitleTextView;
    private CancellationSignal mCancel;
    private Button mCancelButton;
    private RelativeLayout mCancelHotspotView;
    private TextView mCancelText;
    private Button mConfirmButton;
    private Drawable mConfirmButtonDrawable;
    private String mConfirmDes;
    private Context mContext;
    private FaceAnimation mFaceAnimation;
    private ContentObserver mFaceConfigContentObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass10 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isChange) {
            FaceViewController.this.updateConfiguration();
        }
    };
    private View mFaceDetectButtomView;
    private View mFaceDetectView;
    private FaceManager mFaceManager;
    private TextView mFaceMessage;
    private FaceRecognizeManager mFaceRecognizeManager;
    private TextView mFaceTitle;
    private RelativeLayout mFacebuttonPanelView;
    private ImageView mFacedetectIcon;
    private FingerprintView mFingerView;
    private ImageView mFingerprintView;
    private Handler mHandler = null;
    private HintText mHintView;
    private final Runnable mMakeSureSuccessHintViewRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceViewController.this.mContext != null) {
                FaceViewController.this.mFaceMessage.setText(FaceViewController.this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_success")));
                FaceViewController.this.mConfirmButton.setVisibility(4);
                FaceViewController.this.mCancelButton.setVisibility(4);
                FaceViewController.this.mHandler.postDelayed(FaceViewController.this.mRemoveViewRunnable, FaceViewController.VIEW_RESUME_DELAY);
                if (FaceViewController.this.mReceiver != null) {
                    FaceViewController.this.mReceiver.onUserVerificationResult(0, (byte[]) null, new byte[6]);
                    Log.i(FaceViewController.TAG, "bioface begin mMakeSureSuccessHintViewRunnable");
                }
                FaceViewController.this.mHandler.sendMessageDelayed(FaceViewController.this.mHandler.obtainMessage(1), FaceViewController.VIEW_REMOVE_DELAY);
            }
        }
    };
    private final Runnable mMakeSureUsePasswordRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceViewController.this.mContext != null) {
                FaceViewController.this.mFingerView.removeView(FaceViewController.this.mFaceDetectButtomView);
                FaceViewController.this.mFingerView.removeView(FaceViewController.this.mFaceDetectView);
                FaceViewController.this.mHandler.sendEmptyMessage(1);
                if (FaceViewController.this.mReceiver != null) {
                    FaceViewController.this.mReceiver.onErrorFingerprintServiceReceiver(0, 5, FaceDetectManager.getInstance().getCookie());
                    Log.i(FaceViewController.TAG, "bioface mMakeSureUsePasswordRunnable");
                }
            }
        }
    };
    private int mOptId;
    private Bundle mPkgAttributes;
    private BiometricServiceReceiverListenerEx mReceiver;
    private final Runnable mRemoveViewRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass7 */

        @Override // java.lang.Runnable
        public void run() {
            FaceViewController.this.clearFaceViewInfo();
        }
    };
    private RelativeLayout mTitleAndSummaryView;
    private final Runnable mUpdateDetectingHintViewRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass6 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceViewController.this.mContext != null) {
                FaceViewController faceViewController = FaceViewController.this;
                faceViewController.playFaceRecognition(faceViewController.mFacedetectIcon, FaceViewController.this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_recognizing")));
                FaceViewController.this.updateButtomTextInformation(0);
                FaceViewController.this.mFingerprintView.setVisibility(0);
                FaceViewController.this.mHintView.setVisibility(0);
                Log.i(FaceViewController.TAG, "bioface begin mUpdateDetectingHintViewRunnable");
            }
        }
    };
    private final Runnable mUpdateFaceHintWhenInitFail = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass8 */

        @Override // java.lang.Runnable
        public void run() {
            FaceViewController.this.updateForbiddenView(-1);
        }
    };
    private final Runnable mUpdateFailHintViewRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceViewController.this.mContext != null) {
                FaceViewController faceViewController = FaceViewController.this;
                faceViewController.playFaceDetectError(faceViewController.mFacedetectIcon, FaceViewController.this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_recognizing_fail")));
                FaceViewController.this.updateButtomTextInformation(0);
                Log.i(FaceViewController.TAG, "bioface begin mUpdateFailHintViewRunnable");
            }
        }
    };
    private final Runnable mUpdateForbiddenViewRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass5 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceViewController.this.mContext != null) {
                FaceViewController faceViewController = FaceViewController.this;
                faceViewController.playFaceDetectError(faceViewController.mFacedetectIcon, FaceViewController.this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_recognizing_mismatch")));
                FaceViewController.this.updateButtomTextInformation(0);
                Log.i(FaceViewController.TAG, "bioface begin mUpdateForbiddenViewRunnable");
            }
        }
    };
    private final Runnable mUpdateSuccessHintViewRunnable = new Runnable() {
        /* class com.huawei.server.face.FaceViewController.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (FaceViewController.this.mContext != null) {
                if (FaceViewController.this.mConfirmButton != null) {
                    FaceViewController.this.updateButtomTextInformation(1);
                    FaceViewController.this.updateButtomColor();
                }
                if (!FaceViewController.this.isNewFingerView) {
                    FaceViewController.this.mCancelHotspotView.setVisibility(4);
                    FaceViewController.this.mUsePasswordHotspotView.setVisibility(4);
                }
                FaceViewController.this.mAppTitleAndSummaryView.setVisibility(4);
                FaceViewController.this.mFingerprintView.setVisibility(4);
                FaceViewController.this.mHintView.setVisibility(4);
                FaceViewController.this.mHandler.sendMessageDelayed(FaceViewController.this.mHandler.obtainMessage(2), FaceViewController.FINGER_ICON_REMOVE_DELAY);
                Log.w(FaceViewController.TAG, "bioface mUpdateSuccessHintViewRunnable");
            }
        }
    };
    private RelativeLayout mUsePasswordHotspotView;

    private FaceViewController() {
    }

    public static boolean isFaceThreeDimensional() {
        return false;
    }

    public void initFaveViewController(Context context, Handler handle) {
        if (isFaceThreeDimensional() && !this.isInit) {
            this.mContext = context;
            this.mHandler = handle;
            if (ActivityManagerEx.getCurrentUser() == 0) {
                this.mFaceRecognizeManager = new FaceRecognizeManager(context, this.faceCallback);
                this.mOptId = 0;
                listenCfgChange();
                this.isInit = true;
                Log.i(TAG, "bioface initFaveViewController");
            }
        }
    }

    public static synchronized FaceViewController getInstance() {
        FaceViewController faceViewController;
        synchronized (FaceViewController.class) {
            synchronized (FaceViewController.class) {
                if (sInstance == null) {
                    sInstance = new FaceViewController();
                }
                faceViewController = sInstance;
            }
            return faceViewController;
        }
        return faceViewController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCallbackResult(int errorCode) {
        Handler handler;
        if (this.mContext != null && (handler = this.mHandler) != null) {
            if (errorCode == 0) {
                Log.i(TAG, "bioface onAuthenticationSucceeded");
                playFaceDetectCompleted(this.mFacedetectIcon, this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_success_confirm")));
                this.mHandler.postDelayed(this.mUpdateSuccessHintViewRunnable, FACE_DETECTED_DELAY);
            } else if (errorCode == 3) {
                Log.i(TAG, "bioface onAuthenticationFailed");
                this.mHandler.post(this.mUpdateFailHintViewRunnable);
                if (this.mReceiver != null) {
                    Log.w(TAG, "bioface mReceiver COMPARE_FAIL");
                    this.mReceiver.onUserVerificationResult(FaceDetectManager.getInstance().getCookie(), (byte[]) null, (byte[]) null);
                }
            } else if (errorCode == 8) {
                updateForbiddenView(errorCode);
                Log.w(TAG, "bioface updateForbiddenView");
            } else {
                handler.post(this.mUpdateFailHintViewRunnable);
                if (this.mReceiver != null) {
                    Log.i(TAG, "bioface onAuthenticationError errorCode=" + errorCode);
                    this.mReceiver.onErrorFingerprintServiceReceiver(0, 3, FaceDetectManager.getInstance().getCookie());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCallbackAcquire(int errorCode) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(this.mUpdateDetectingHintViewRunnable);
            if (this.mReceiver != null) {
                Log.d(TAG, "bioface onAuthenticationHelp errorCode=" + errorCode);
                this.mReceiver.onAcquiredFingerprintServiceReceiver(0, errorCode + 100, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateButtomColor() {
        Context context = this.mContext;
        if (context != null) {
            this.mConfirmButton.getBackground().setColorFilter(Color.parseColor(context.getResources().getString(HwPartResourceUtils.getResourceId("hw_facedetct_makesure_background"))), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void resumeButtom() {
        Button button = this.mConfirmButton;
        if (button != null) {
            button.setBackgroundDrawable(this.mConfirmButtonDrawable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForbiddenView(int errorCode) {
        Handler handler;
        if (this.mContext != null && (handler = this.mHandler) != null) {
            handler.post(this.mUpdateForbiddenViewRunnable);
            if (this.mReceiver != null) {
                Log.w(TAG, "bioface onAuthenticationFailed face forbidden");
                this.mReceiver.onUserVerificationResult(FaceDetectManager.getInstance().getCookie(), (byte[]) null, (byte[]) null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfiguration() {
        Context context = this.mContext;
        if (context != null) {
            if (SettingsEx.Secure.getIntForUser(context.getContentResolver(), FACE_RECOGNIZE_UNLOCK, 0, 0) != 0) {
                Log.w(TAG, "updateConfiguration isFaceEnable true");
                this.isFaceEnable = true;
                return;
            }
            Log.w(TAG, "updateConfiguration isFaceEnable false");
            this.isFaceEnable = false;
        }
    }

    private void listenCfgChange() {
        Context context = this.mContext;
        if (context != null) {
            FingerprintSupportEx.registerContentObserver(context.getContentResolver(), Settings.Secure.getUriFor(FACE_RECOGNIZE_UNLOCK), false, this.mFaceConfigContentObserver, 0);
            updateConfiguration();
        }
    }

    public boolean isFaceSupportBiometric() {
        if (!isFaceThreeDimensional()) {
            return false;
        }
        if (!this.isFaceEnable) {
            Log.w(TAG, "isFaceSupportBiometric support face : false");
            return false;
        }
        Log.w(TAG, "isFaceSupportBiometric: true");
        return true;
    }

    public boolean updateExtraElement(FingerprintView fingerView, boolean isFingerView, BiometricServiceReceiverListenerEx receiver, int currentRotation) {
        if (!isFaceThreeDimensional() || this.mHandler == null) {
            return false;
        }
        this.isNewFingerView = isFingerView;
        Log.w(TAG, "bioface updateExtraElement begin");
        if (!judgeRotation(currentRotation)) {
            return false;
        }
        if (fingerView == null) {
            Log.w(TAG, "bioface fingerView is null");
            return false;
        }
        this.mReceiver = receiver;
        Log.w(TAG, "bioface mReceiver:" + this.mReceiver);
        this.mFingerView = fingerView;
        if (!updateTextAndSetButtomRes()) {
            Log.w(TAG, "bioface getTextAndButtomRes false");
            return false;
        }
        updateCustomInformation(this.mFingerView);
        setButtomListener();
        setTextListener();
        setTextAndButtomVisible();
        this.mHandler.sendEmptyMessage(3);
        Log.w(TAG, "bioface updateExtraElement end");
        return true;
    }

    private int dip2px(float dpValue, float scale) {
        return (int) ((dpValue * scale) + DP_TO_PX_PARAMS);
    }

    private int addAppTitleTextView(RelativeLayout appRelativeLayout, float scale) {
        Context context = this.mContext;
        if (context == null || appRelativeLayout == null) {
            return 0;
        }
        this.mAppTitleTextView = new TextView(context);
        this.mAppTitleTextView.setText(this.mPkgAttributes.getString(FaceConstants.KEY_TITLE));
        this.mAppTitleTextView.setTextSize(0, (float) dip2px(APP_TITE_TEXT_SIZE, scale));
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(-2, -2);
        titleParams.addRule(10);
        titleParams.addRule(14);
        int topMargin = dip2px(APP_TITLE_TEXT_IOP_MARGIN, scale);
        titleParams.topMargin = topMargin;
        this.mAppTitleTextView.setGravity(17);
        this.mAppTitleTextView.setTextColor(-1);
        this.mAppTitleTextView.setVisibility(0);
        this.mAppRelativeLayout.addView(this.mAppTitleTextView, titleParams);
        return topMargin;
    }

    private int addAppSubTitleTextView(RelativeLayout appRelativeLayout, float scale, int topMargin) {
        Context context = this.mContext;
        if (context == null || appRelativeLayout == null) {
            return 0;
        }
        this.mAppSubTitleTextView = new TextView(context);
        this.mAppSubTitleTextView.setText(this.mPkgAttributes.getString(FaceConstants.KEY_SUBTITLE));
        this.mAppSubTitleTextView.setTextSize(0, (float) dip2px(APP_VIEW_TEXT_SIZE, scale));
        RelativeLayout.LayoutParams subTitleParams = new RelativeLayout.LayoutParams(-2, -2);
        subTitleParams.addRule(2, HwPartResourceUtils.getResourceId("face_title_and_summary"));
        subTitleParams.addRule(14);
        int textSize = 0;
        TextView textView = this.mAppTitleTextView;
        if (textView != null) {
            textSize = (int) textView.getTextSize();
        }
        int subTitleTopMargin = dip2px(APP_SUBTITLE_TOP_MARGIN, scale) + topMargin + textSize;
        subTitleParams.topMargin = subTitleTopMargin;
        this.mAppSubTitleTextView.setGravity(17);
        this.mAppSubTitleTextView.setTextColor(-1);
        this.mAppSubTitleTextView.setVisibility(0);
        this.mAppRelativeLayout.addView(this.mAppSubTitleTextView, subTitleParams);
        return subTitleTopMargin;
    }

    private int addAppDescriptionTextView(RelativeLayout appRelativeLayout, float scale, int topMargin) {
        Context context = this.mContext;
        if (context == null || appRelativeLayout == null) {
            return 0;
        }
        this.mAppDescriptionTextView = new TextView(context);
        this.mAppDescriptionTextView.setText(this.mPkgAttributes.getString(FaceConstants.KEY_DESCRIPTION));
        this.mAppDescriptionTextView.setTextSize(0, (float) dip2px(APP_VIEW_TEXT_SIZE, scale));
        RelativeLayout.LayoutParams descriptionParams = new RelativeLayout.LayoutParams(-2, -2);
        descriptionParams.addRule(2, HwPartResourceUtils.getResourceId("face_title_and_summary"));
        descriptionParams.addRule(14);
        int textSize = 0;
        TextView textView = this.mAppSubTitleTextView;
        if (textView != null) {
            textSize = (int) textView.getTextSize();
        } else {
            TextView textView2 = this.mAppTitleTextView;
            if (textView2 != null) {
                textSize = (int) textView2.getTextSize();
            }
        }
        int descriptionTopMargin = dip2px(APP_DESCRIPTION_TOP_MARGIN, scale) + topMargin + textSize;
        descriptionParams.topMargin = descriptionTopMargin;
        this.mAppDescriptionTextView.setGravity(17);
        this.mAppDescriptionTextView.setTextColor(-1);
        this.mAppRelativeLayout.addView(this.mAppDescriptionTextView, descriptionParams);
        return descriptionTopMargin;
    }

    private int addAppCustomInfomation(float scale) {
        Bundle bundle = this.mPkgAttributes;
        if (bundle == null || this.mAppRelativeLayout == null) {
            return 0;
        }
        int topMargin = 0;
        if (bundle.getString(FaceConstants.KEY_TITLE) != null) {
            topMargin = addAppTitleTextView(this.mAppRelativeLayout, scale);
        }
        if (this.mPkgAttributes.getString(FaceConstants.KEY_SUBTITLE) != null) {
            topMargin = addAppSubTitleTextView(this.mAppRelativeLayout, scale, topMargin);
        }
        if (this.mPkgAttributes.getString(FaceConstants.KEY_DESCRIPTION) != null) {
            return addAppDescriptionTextView(this.mAppRelativeLayout, scale, topMargin);
        }
        return topMargin;
    }

    private void updateCustomInformation(FingerprintView fingerView) {
        if (this.mContext != null) {
            setTextViewSize();
            if (this.mPkgAttributes != null) {
                View view = this.mAppRelativeLayout;
                if (!(view == null || fingerView == null)) {
                    fingerView.removeView(view);
                    this.mAppRelativeLayout = null;
                    this.mAppSubTitleTextView = null;
                    this.mAppSubTitleTextView = null;
                    this.mAppDescriptionTextView = null;
                    this.mCancelText = null;
                }
                View cancelText = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("app_back_fingerprint_cancel"));
                if (cancelText instanceof TextView) {
                    this.mCancelText = (TextView) cancelText;
                }
                this.mAppRelativeLayout = new RelativeLayout(this.mContext);
                RelativeLayout.LayoutParams appParams = new RelativeLayout.LayoutParams(-1, -2);
                appParams.addRule(2, HwPartResourceUtils.getResourceId("face_title_and_summary"));
                float scale = this.mContext.getResources().getDisplayMetrics().density;
                int topMargin = addAppCustomInfomation(scale);
                updateButtomTextInformation(0);
                if (topMargin > 0) {
                    RelativeLayout.LayoutParams faceTitleAndSummaryParams = new RelativeLayout.LayoutParams(-2, -2);
                    faceTitleAndSummaryParams.addRule(2, HwPartResourceUtils.getResourceId("app_lock_finger"));
                    faceTitleAndSummaryParams.addRule(14);
                    faceTitleAndSummaryParams.topMargin = topMargin + dip2px(FACE_TITLE_TOP_MARGIN, scale);
                    this.mTitleAndSummaryView.setLayoutParams(faceTitleAndSummaryParams);
                }
                fingerView.addView(this.mAppRelativeLayout, appParams);
            }
        }
    }

    private void updateButtomToMiddle(Button button) {
        if (this.isNewFingerView) {
            View fingerbuttomView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("fingerbuttomview"));
            if (fingerbuttomView != null) {
                fingerbuttomView.setVisibility(8);
            }
        } else {
            View facebuttomView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("facebuttomview"));
            if (facebuttomView != null) {
                facebuttomView.setVisibility(8);
            }
        }
        if (button != null) {
            setButtonWith(button);
            button.setVisibility(0);
        }
    }

    private void setButtonWith(Button button) {
        if (button == null) {
            Log.d(TAG, "bioface setButtonWith button is null");
            return;
        }
        int sreenWith = getScreenWith();
        if (sreenWith > 0) {
            button.getLayoutParams().width = sreenWith / 2;
        } else {
            button.getLayoutParams().width = MIDDLE_BUTTOM_WIDTD;
        }
    }

    private int getScreenWith() {
        Context context = this.mContext;
        if (context == null) {
            return 0;
        }
        Object windowObject = context.getSystemService("window");
        if (!(windowObject instanceof WindowManager)) {
            return 0;
        }
        WindowManager windowManager = (WindowManager) windowObject;
        DisplayMetrics dm = new DisplayMetrics();
        if (windowManager == null || windowManager.getDefaultDisplay() == null) {
            return 0;
        }
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        Log.d(TAG, "bioface getScreenWith width=" + width);
        return width;
    }

    private void setConfirmButtonParams() {
        ViewGroup.LayoutParams params = this.mConfirmButton.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) params).removeRule(1);
            ((RelativeLayout.LayoutParams) params).addRule(17, HwPartResourceUtils.getResourceId("fingerbuttomview"));
            ((RelativeLayout.LayoutParams) params).setMarginStart(START_DISTANCE);
            ((RelativeLayout.LayoutParams) params).setMarginEnd(4);
            this.mConfirmButton.setLayoutParams(params);
        }
    }

    private void setCancelBuutonParams() {
        ViewGroup.LayoutParams params = this.mCancelButton.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) params).removeRule(1);
            ((RelativeLayout.LayoutParams) params).addRule(START_DISTANCE, HwPartResourceUtils.getResourceId("fingerbuttomview"));
            ((RelativeLayout.LayoutParams) params).setMarginStart(START_DISTANCE);
            ((RelativeLayout.LayoutParams) params).setMarginEnd(4);
            this.mCancelButton.setLayoutParams(params);
        }
    }

    private void updateButtomToOriginalLayout(Button cancelButtom, Button confirmButtom) {
        Button button;
        if (this.isNewFingerView) {
            View fingerbuttomView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("fingerbuttomview"));
            if (fingerbuttomView != null) {
                fingerbuttomView.setVisibility(0);
            }
            if (this.mCancelButton != null && (button = this.mConfirmButton) != null) {
                setButtonWith(button);
                setButtonWith(this.mCancelButton);
                setConfirmButtonParams();
                setCancelBuutonParams();
                this.mCancelButton.setVisibility(0);
                this.mConfirmButton.setVisibility(0);
                return;
            }
            return;
        }
        View facebuttomView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("facebuttomview"));
        if (facebuttomView != null) {
            facebuttomView.setVisibility(0);
        }
        if (cancelButtom != null) {
            cancelButtom.setVisibility(0);
        }
        if (confirmButtom != null) {
            confirmButtom.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateButtomTextInformation(int faceDetectFlag) {
        Bundle bundle;
        if (this.mContext != null && (bundle = this.mPkgAttributes) != null) {
            if (bundle.getString(FaceConstants.KEY_POSITIVE_TEXT) != null && this.mPkgAttributes.getString(FaceConstants.KEY_NEGATIVE_TEXT) != null) {
                Button button = this.mConfirmButton;
                if (button != null) {
                    button.setVisibility(0);
                    this.mConfirmButton.setText(this.mPkgAttributes.getString(FaceConstants.KEY_POSITIVE_TEXT));
                }
                Button button2 = this.mCancelButton;
                if (button2 != null) {
                    button2.setVisibility(0);
                    this.mCancelButton.setText(this.mPkgAttributes.getString(FaceConstants.KEY_NEGATIVE_TEXT));
                }
            } else if (this.mPkgAttributes.getString(FaceConstants.KEY_NEGATIVE_TEXT) != null) {
                Button button3 = this.mConfirmButton;
                if (button3 != null) {
                    button3.setVisibility(4);
                }
                Button button4 = this.mCancelButton;
                if (button4 != null) {
                    button4.setText(this.mPkgAttributes.getString(FaceConstants.KEY_NEGATIVE_TEXT));
                }
                View fingerbuttomView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("fingerbuttomview"));
                if (fingerbuttomView != null) {
                    fingerbuttomView.setVisibility(0);
                }
                updateButtomToMiddle(this.mCancelButton);
            } else if (this.mPkgAttributes.getString(FaceConstants.KEY_POSITIVE_TEXT) != null) {
                updateButtomToMiddle(this.mConfirmButton);
                Button button5 = this.mConfirmButton;
                if (button5 != null) {
                    button5.setText(this.mPkgAttributes.getString(FaceConstants.KEY_POSITIVE_TEXT));
                }
                Button button6 = this.mCancelButton;
                if (button6 != null) {
                    button6.setVisibility(4);
                }
            } else {
                Button button7 = this.mConfirmButton;
                if (button7 != null && faceDetectFlag == 0) {
                    button7.setVisibility(0);
                    this.mConfirmButton.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_usepassword")));
                }
                Button button8 = this.mCancelButton;
                if (button8 != null && faceDetectFlag == 0) {
                    button8.setVisibility(0);
                    this.mCancelButton.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_cancel")));
                }
            }
            if (faceDetectFlag == 1) {
                Button button9 = this.mConfirmButton;
                if (button9 != null) {
                    button9.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_makesure")));
                }
                updateButtomToOriginalLayout(this.mCancelButton, this.mConfirmButton);
            }
        }
    }

    private void setTextViewSize() {
        TextView textView = this.mFaceTitle;
        if (textView != null) {
            textView.setTextSize(0, 45.0f);
        }
        TextView textView2 = this.mFaceMessage;
        if (textView2 != null) {
            textView2.setTextSize(0, 39.0f);
        }
        HintText hintText = this.mHintView;
        if (hintText != null) {
            hintText.setTextSize(0, 45.0f);
        }
        Button button = this.mCancelButton;
        if (button != null) {
            button.setTextSize(0, 45.0f);
        }
        Button button2 = this.mConfirmButton;
        if (button2 != null) {
            button2.setTextSize(0, 45.0f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearFaceViewInfo() {
        RelativeLayout relativeLayout;
        RelativeLayout relativeLayout2;
        if (!this.isNewFingerView && (relativeLayout2 = this.mCancelHotspotView) != null) {
            relativeLayout2.setVisibility(0);
        }
        if (!this.isNewFingerView && (relativeLayout = this.mUsePasswordHotspotView) != null) {
            relativeLayout.setVisibility(0);
        }
        RelativeLayout relativeLayout3 = this.mAppTitleAndSummaryView;
        if (relativeLayout3 != null) {
            relativeLayout3.setVisibility(0);
        }
        ImageView imageView = this.mFingerprintView;
        if (imageView != null) {
            imageView.setVisibility(0);
        }
        HintText hintText = this.mHintView;
        if (hintText != null) {
            hintText.setVisibility(0);
        }
        removeFaceView();
    }

    public boolean isFaceDetectFromBiometricPrompt(Bundle bundle, String pkgName) {
        if (!isFaceSupportBiometric()) {
            return false;
        }
        if (bundle == null) {
            Log.w(TAG, "bioface isFaceDetectFromBiometricPrompt clearFaceViewInfo");
            clearFaceViewInfo();
            return false;
        }
        this.mPkgAttributes = bundle;
        this.isBiomericDetecting = true;
        Log.d(TAG, "bioface isFaceDetectFromBiometricPrompt true");
        return true;
    }

    private boolean judgeRotation(int currentRotation) {
        if (currentRotation == 0 || currentRotation == 2) {
            return true;
        }
        Log.w(TAG, "bioface judgeRotation false");
        return false;
    }

    private void updateHotspotView() {
        if (!this.isNewFingerView) {
            View cacelHotspotView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("cancel_hotspot"));
            if (cacelHotspotView != null && (cacelHotspotView instanceof RelativeLayout)) {
                this.mCancelHotspotView = (RelativeLayout) cacelHotspotView;
            }
            View usePasswordHotspoView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("use_password_hotspot"));
            if (usePasswordHotspoView != null && (usePasswordHotspoView instanceof RelativeLayout)) {
                this.mUsePasswordHotspotView = (RelativeLayout) usePasswordHotspoView;
            }
        }
    }

    private void updateTitleAndSummaryView() {
        View appTitleAndSummaryView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("app_title_and_summary"));
        if (appTitleAndSummaryView != null && (appTitleAndSummaryView instanceof RelativeLayout)) {
            this.mAppTitleAndSummaryView = (RelativeLayout) appTitleAndSummaryView;
        }
        this.mFingerprintView = (ImageView) this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("fingerprintView"));
        View faceTitle = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("face_title"));
        if (faceTitle != null && (faceTitle instanceof TextView)) {
            this.mFaceTitle = (TextView) faceTitle;
        }
    }

    private void updateTitleView() {
        View faceMessage = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("face_message"));
        if (faceMessage != null && (faceMessage instanceof TextView)) {
            this.mFaceMessage = (TextView) faceMessage;
        }
        this.mFacedetectIcon = (ImageView) this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("face_icon"));
        View titleAndSummaryView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("face_title_and_summary"));
        if (titleAndSummaryView != null && (titleAndSummaryView instanceof RelativeLayout)) {
            this.mTitleAndSummaryView = (RelativeLayout) titleAndSummaryView;
        }
    }

    private boolean updateTitleAndFaceView() {
        if (this.mFingerView == null) {
            return false;
        }
        updateHotspotView();
        updateTitleAndSummaryView();
        updateTitleView();
        if (this.mTitleAndSummaryView == null) {
            Log.w(TAG, "bioface mTitleAndSummaryView is null");
            return false;
        }
        this.mHintView = (HintText) this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("fingerprint_hint"));
        View facebuttonPanelView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("facebuttonPanel"));
        if (facebuttonPanelView != null && (facebuttonPanelView instanceof RelativeLayout)) {
            this.mFacebuttonPanelView = (RelativeLayout) facebuttonPanelView;
        }
        return judgeTitleAndFaceView();
    }

    private boolean judgeTitleAndFaceView() {
        if (!this.isNewFingerView) {
            if (this.mCancelHotspotView == null || this.mUsePasswordHotspotView == null) {
                Log.w(TAG, "bioface mCancelHotspotView or mUsePasswordHotspotView is null");
                return false;
            } else if (this.mFacebuttonPanelView == null) {
                Log.w(TAG, "bioface mFacebuttonPanelView is null");
                return false;
            }
        }
        if (this.mAppTitleAndSummaryView == null || this.mFaceMessage == null) {
            Log.w(TAG, "bioface mAppTitleAndSummaryView or mFaceMessage is null");
            return false;
        } else if (this.mFingerprintView == null || this.mFaceTitle == null) {
            Log.w(TAG, "bioface mFingerprintView or mFaceTitle or mFaceMessage is null");
            return false;
        } else if (this.mFacedetectIcon == null) {
            Log.w(TAG, "bioface mFacedetectIcon is null");
            return false;
        } else if (this.mHintView != null) {
            return true;
        } else {
            Log.w(TAG, "bioface mHintView is null");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playFaceRecognition(ImageView faceDetectIcon, String faceMessage) {
        if (this.mContext != null) {
            this.mFaceAnimation.faceDetecting(faceMessage);
        }
    }

    private void playAnimate(ImageView faceDetectIcon, Drawable drawable) {
        if (drawable instanceof Animatable) {
            faceDetectIcon.setImageDrawable(drawable);
            Drawable playdrawable = faceDetectIcon.getDrawable();
            if (playdrawable instanceof Animatable) {
                ((Animatable) playdrawable).start();
                Log.w(TAG, "bioface playAnimaten start");
            }
        }
    }

    private void playFaceAnimate(ImageView faceDetectIcon, Drawable drawable) {
        if (faceDetectIcon == null) {
            Log.w(TAG, "bioface playAnimaten faceDetectIcon is null");
        } else if (drawable == null) {
            Log.w(TAG, "bioface playAnimaten drawable is null");
        } else {
            Drawable frontDrawable = faceDetectIcon.getDrawable();
            if (!(frontDrawable instanceof Animatable) || !((Animatable) frontDrawable).isRunning()) {
                playAnimate(faceDetectIcon, drawable);
            } else {
                Log.w(TAG, "bioface playAnimaten isRunning");
            }
        }
    }

    private void playFaceDetectCompleted(ImageView faceDetectIcon, String faceMessage) {
        if (this.mContext != null && faceDetectIcon != null) {
            Log.w(TAG, "bioface play playFaceDetectCompleted");
            this.mFaceAnimation.faceDetectedSuccess(faceMessage);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playFaceDetectError(ImageView faceDetectIcon, String faceMessage) {
        if (this.mContext != null && faceDetectIcon != null) {
            Log.w(TAG, "bioface play playFaceDetectError");
            this.mFaceAnimation.faceDetectedFailed(faceMessage);
        }
    }

    private boolean updateTextAndSetButtomRes() {
        Button button;
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        this.mConfirmDes = context.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_makesure"));
        if (this.mConfirmDes == null) {
            Log.w(TAG, "bioface mRecognizingFailDes or mConfirmDes is null");
            return false;
        } else if (!updateTitleAndFaceView()) {
            Log.w(TAG, "bioface updateTitleAndFaceView is false");
            return false;
        } else {
            if (this.isNewFingerView) {
                View cancelView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("l2_back_fingerprint_cancel"));
                if (cancelView instanceof Button) {
                    this.mCancelButton = (Button) cancelView;
                }
                View confirmView = this.mFingerView.findViewById(HwPartResourceUtils.getResourceId("l2_back_fingerprint_usepassword"));
                if (confirmView instanceof Button) {
                    this.mConfirmButton = (Button) confirmView;
                }
            } else {
                this.mCancelButton = (Button) this.mFacebuttonPanelView.findViewById(HwPartResourceUtils.getResourceId("facebuttoncancel"));
                this.mConfirmButton = (Button) this.mFacebuttonPanelView.findViewById(HwPartResourceUtils.getResourceId("facebuttonmakesure"));
            }
            if (this.mCancelButton == null || (button = this.mConfirmButton) == null) {
                Log.w(TAG, "bioface mConfirmButton or mConfirmButton is null");
                return false;
            }
            this.mConfirmButtonDrawable = button.getBackground();
            this.mCancelButton.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_cancel")));
            this.mConfirmButton.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_usepassword")));
            this.mCancelButton.setTextColor(-1);
            this.mConfirmButton.setTextColor(-1);
            this.mHintView.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_press_finger_detect")));
            this.mFaceTitle.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_finger_face_pay")));
            this.mFaceMessage.setText(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_recognizing")));
            return true;
        }
    }

    private void setTextAndButtomVisible() {
        this.mTitleAndSummaryView.setVisibility(0);
        this.mAppTitleAndSummaryView.setVisibility(8);
        if (!this.isNewFingerView) {
            this.mFacebuttonPanelView.setVisibility(0);
            this.mCancelHotspotView.setVisibility(4);
            this.mUsePasswordHotspotView.setVisibility(4);
            TextView textView = this.mCancelText;
            if (textView != null) {
                textView.setVisibility(8);
            }
        }
        ImageView imageView = this.mFingerprintView;
        if (imageView != null) {
            imageView.setVisibility(0);
        }
        HintText hintText = this.mHintView;
        if (hintText != null) {
            hintText.setVisibility(0);
        }
        this.mFaceTitle.setVisibility(0);
        this.mFaceMessage.setVisibility(0);
        this.mFacedetectIcon.setVisibility(0);
    }

    private void setButtomListener() {
        this.mCancelButton.setOnClickListener(new View.OnClickListener() {
            /* class com.huawei.server.face.FaceViewController.AnonymousClass11 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (FaceViewController.this.mContext != null) {
                    FaceViewController.this.mHandler.post(FaceViewController.this.mRemoveViewRunnable);
                    FaceViewController.this.mFingerView.removeView(FaceViewController.this.mFaceDetectButtomView);
                    FaceViewController.this.mFingerView.removeView(FaceViewController.this.mFaceDetectView);
                    FaceViewController.this.mHandler.sendMessage(FaceViewController.this.mHandler.obtainMessage(1));
                    if (FaceViewController.this.mReceiver != null) {
                        Log.i(FaceViewController.TAG, "bioface mCancelButton");
                        FaceViewController.this.mReceiver.onErrorFingerprintServiceReceiver(0, 5, FaceDetectManager.getInstance().getCookie());
                    }
                    FaceViewController.this.cancelFaceAuth();
                    FaceViewController.this.isBiomericDetecting = false;
                }
            }
        });
        this.mConfirmButton.setOnClickListener(new View.OnClickListener() {
            /* class com.huawei.server.face.FaceViewController.AnonymousClass12 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (FaceViewController.this.mConfirmDes == null || !FaceViewController.this.mConfirmDes.equals(FaceViewController.this.mConfirmButton.getText().toString())) {
                    FaceViewController.this.mHandler.post(FaceViewController.this.mMakeSureUsePasswordRunnable);
                    FaceViewController.this.mFaceRecognizeManager.cancelAuthenticate(FaceViewController.this.mOptId);
                } else {
                    Log.w(FaceViewController.TAG, "bioface mConfirmButton mMakeSureSuccessHintViewRunnable");
                    FaceViewController.this.mHandler.post(FaceViewController.this.mMakeSureSuccessHintViewRunnable);
                }
                FaceViewController.this.isBiomericDetecting = false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendFaceDetectTry() {
        TextView textView;
        if (this.mContext == null || (textView = this.mFaceMessage) == null || textView.getText() == null) {
            Log.w(TAG, "bioface sendFaceDetectTry mFaceMessage null");
            return;
        }
        String faceMessage = this.mFaceMessage.getText().toString();
        if (faceMessage.equals(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_recognizing_mismatch")))) {
            Log.w(TAG, "bioface sendFaceDetectTry Recognizing Forbidden");
        } else if (faceMessage.equals(this.mContext.getString(HwPartResourceUtils.getResourceId("emui_biometric_face_detect_success_confirm")))) {
            Log.w(TAG, "bioface sendFaceDetectTry detect success");
        } else {
            this.mHandler.removeCallbacks(this.mUpdateDetectingHintViewRunnable);
            this.mHandler.post(this.mUpdateDetectingHintViewRunnable);
            faceServiceDetectAuth();
        }
    }

    private void setTextListener() {
        this.mFacedetectIcon.setOnClickListener(new View.OnClickListener() {
            /* class com.huawei.server.face.FaceViewController.AnonymousClass13 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                FaceViewController.this.sendFaceDetectTry();
            }
        });
        this.mFaceMessage.setOnClickListener(new View.OnClickListener() {
            /* class com.huawei.server.face.FaceViewController.AnonymousClass14 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                FaceViewController.this.sendFaceDetectTry();
            }
        });
    }

    private boolean init() {
        try {
            if (this.mContext == null) {
                return false;
            }
            this.mFaceRecognizeManager = new FaceRecognizeManager(this.mContext, this.faceCallback);
            int ret = this.mFaceRecognizeManager.init();
            Log.w(TAG, "bioface init mFaceRecognizeManager result : " + ret);
            if (ret == 0) {
                return true;
            }
            this.mHandler.postDelayed(this.mUpdateFaceHintWhenInitFail, 200);
            return false;
        } catch (SecurityException e) {
            Log.e(TAG, "bioface init fail", e);
            return false;
        }
    }

    private void auth() {
        try {
            this.mOptId++;
            this.mFaceRecognizeManager.authenticate(this.mOptId, 0, (Surface) null);
            Log.w(TAG, "bioface DoAuth finish with seq");
        } catch (SecurityException e) {
            Log.e(TAG, "bioface authenticate fail", e);
        }
    }

    public void faceServiceDetectAuth() {
        if (isFaceThreeDimensional() && isFaceSupportBiometric() && init()) {
            this.mFaceAnimation = new FaceAnimation(this.mContext, this.mFacedetectIcon, this.mHandler, this.mFaceMessage);
            auth();
        }
    }

    public void cancelFaceAuth() {
        if (isFaceThreeDimensional() && isFaceSupportBiometric() && this.isBiomericDetecting && this.mFaceRecognizeManager != null) {
            Log.i(TAG, "bioface cancelFaceAuth");
            this.mFaceRecognizeManager.cancelAuthenticate(this.mOptId);
        }
    }

    public boolean isBiomericDetecting() {
        return this.isBiomericDetecting;
    }

    private void removeFaceView() {
        FingerprintView fingerprintView;
        Button button;
        RelativeLayout relativeLayout;
        TextView textView = this.mFaceTitle;
        if (textView != null) {
            textView.setVisibility(8);
        }
        RelativeLayout relativeLayout2 = this.mTitleAndSummaryView;
        if (relativeLayout2 != null) {
            relativeLayout2.setVisibility(8);
        }
        if (!this.isNewFingerView && (relativeLayout = this.mFacebuttonPanelView) != null) {
            relativeLayout.setVisibility(8);
        }
        ImageView imageView = this.mFacedetectIcon;
        if (imageView != null) {
            imageView.setVisibility(8);
        }
        TextView textView2 = this.mFaceMessage;
        if (textView2 != null) {
            textView2.setVisibility(8);
        }
        Button button2 = this.mConfirmButton;
        if (button2 != null) {
            button2.setVisibility(8);
        }
        if (!this.isNewFingerView && (button = this.mCancelButton) != null) {
            button.setVisibility(8);
        }
        RelativeLayout relativeLayout3 = this.mAppRelativeLayout;
        if (!(relativeLayout3 == null || (fingerprintView = this.mFingerView) == null)) {
            fingerprintView.removeView(relativeLayout3);
            this.mAppRelativeLayout = null;
            this.mAppSubTitleTextView = null;
            this.mAppSubTitleTextView = null;
            this.mAppDescriptionTextView = null;
        }
        resumeButtom();
    }
}
