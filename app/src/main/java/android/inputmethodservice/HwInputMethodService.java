package android.inputmethodservice;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.FreezeScreenScene;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import huawei.com.android.internal.widget.HwFragmentContainer;

public class HwInputMethodService implements IHwInputMethodService {
    private static final String CONFIG_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    private static final boolean DEBUG = false;
    private static final float FLOAT_IME_WIDTH_WIEGHT = 0.8f;
    private static final int IME_DOCK_HEIGHT = 54;
    private static final int IME_DOCK_INITIAL_POSITION = 100;
    private static final int IME_DOCK_MIN_X_POSITION = 0;
    private static final int IME_DOCK_MIN_Y_POSITION = 100;
    private static final String TAG = "HwInputMethodService";
    private static HwInputMethodService mHwInputMethodService;
    private ContentObserver mContentObserver;
    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private Handler mHandler;
    private Dialog mImeDockDialog;
    private final OnTouchListener mImeDockTouchListener;
    private float mImeDockWidthLandscpaeFactor;
    private float mImeDockWidthPortraitFactor;
    private boolean mIsPortrait;
    private ConfigHandleBroadcastReceiver mReciever;
    private boolean mVisible;
    private WindowManager mWindowManage;

    private class ConfigHandleBroadcastReceiver extends BroadcastReceiver {
        private ConfigHandleBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
        }
    }

    private HwInputMethodService(Context context) {
        this.mImeDockWidthPortraitFactor = 0.0f;
        this.mImeDockWidthLandscpaeFactor = 0.0f;
        this.mDisplayMetrics = new DisplayMetrics();
        this.mIsPortrait = true;
        this.mVisible = DEBUG;
        this.mImeDockTouchListener = new OnTouchListener() {
            private float imeDockLastX;
            private float imeDockLastY;
            private boolean imeDockStartTracking;

            {
                this.imeDockStartTracking = HwInputMethodService.DEBUG;
            }

            public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                if (HwInputMethodService.this.mImeDockDialog == null || HwInputMethodService.this.mImeDockDialog.getWindow() == null) {
                    return HwInputMethodService.DEBUG;
                }
                LayoutParams imeDockLP = HwInputMethodService.this.mImeDockDialog.getWindow().getAttributes();
                switch (paramMotionEvent.getAction() & PduHeaders.STORE_STATUS_ERROR_END) {
                    case HwInputMethodService.IME_DOCK_MIN_X_POSITION /*0*/:
                        this.imeDockStartTracking = true;
                        this.imeDockLastX = paramMotionEvent.getRawX();
                        this.imeDockLastY = paramMotionEvent.getRawY();
                        break;
                    case HwFragmentContainer.TRANSITION_FADE /*1*/:
                        this.imeDockStartTracking = HwInputMethodService.DEBUG;
                        break;
                    case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                        if (this.imeDockStartTracking) {
                            int xDrift = (int) (paramMotionEvent.getRawX() - this.imeDockLastX);
                            int yDrift = (int) (paramMotionEvent.getRawY() - this.imeDockLastY);
                            int maxX = HwInputMethodService.this.mDisplayMetrics.widthPixels;
                            int maxY = HwInputMethodService.this.mDisplayMetrics.heightPixels;
                            imeDockLP.y += yDrift;
                            if (imeDockLP.y > maxY) {
                                imeDockLP.y = maxY;
                            } else if (imeDockLP.y < HwInputMethodService.IME_DOCK_MIN_Y_POSITION) {
                                imeDockLP.y = HwInputMethodService.IME_DOCK_MIN_Y_POSITION;
                            }
                            imeDockLP.x += xDrift;
                            if (imeDockLP.x <= maxX) {
                                if (imeDockLP.x <= maxX - imeDockLP.width) {
                                    if (imeDockLP.x < 0) {
                                        imeDockLP.x = HwInputMethodService.IME_DOCK_MIN_X_POSITION;
                                        break;
                                    }
                                }
                                imeDockLP.x = maxX - imeDockLP.width;
                                break;
                            }
                            imeDockLP.x = maxX;
                            break;
                        }
                        break;
                }
                this.imeDockLastX = paramMotionEvent.getRawX();
                this.imeDockLastY = paramMotionEvent.getRawY();
                LayoutParams imeDockLPF = imeDockLP;
                HwInputMethodService.this.mImeDockDialog.getWindow().setAttributes(imeDockLP);
                return true;
            }
        };
        this.mContext = context;
        this.mHandler = new Handler(this.mContext.getMainLooper());
        reloadDisplayMetric();
    }

    public static synchronized HwInputMethodService getInstance(Context context) {
        HwInputMethodService hwInputMethodService;
        synchronized (HwInputMethodService.class) {
            if (mHwInputMethodService == null) {
                mHwInputMethodService = new HwInputMethodService(context);
            }
            hwInputMethodService = mHwInputMethodService;
        }
        return hwInputMethodService;
    }

    public boolean updateImeDockVisibility(boolean visibility) {
        if (this.mImeDockDialog == null) {
            this.mVisible = visibility;
        } else {
            this.mVisible = visibility;
        }
        return DEBUG;
    }

    public boolean updateImeDockConfiguration(boolean visibility) {
        return DEBUG;
    }

    public boolean handleImeDockDestroy() {
        if (this.mImeDockDialog != null) {
            this.mImeDockDialog.dismiss();
            this.mImeDockDialog = null;
            if (this.mContentObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
            }
            if (this.mReciever != null) {
                this.mContext.unregisterReceiver(this.mReciever);
            }
            this.mContentObserver = null;
            this.mReciever = null;
        }
        return DEBUG;
    }

    private void reloadDisplayMetric() {
        boolean z;
        if (this.mWindowManage == null) {
            this.mWindowManage = (WindowManager) this.mContext.getSystemService(FreezeScreenScene.WINDOW_PARAM);
        }
        this.mWindowManage.getDefaultDisplay().getMetrics(this.mDisplayMetrics);
        if (this.mContext.getResources().getConfiguration().orientation == 1) {
            z = true;
        } else {
            z = DEBUG;
        }
        this.mIsPortrait = z;
    }

    public float getImeDockWidthFactor() {
        if (this.mIsPortrait) {
            return this.mImeDockWidthPortraitFactor;
        }
        return this.mImeDockWidthLandscpaeFactor;
    }

    public void updateImeDockWidth() {
        if (this.mImeDockDialog.getWindow() != null) {
            getImeDockWidthFactor();
            LayoutParams imeDockLP = this.mImeDockDialog.getWindow().getAttributes();
            if (this.mIsPortrait) {
                imeDockLP.width = (int) (this.mImeDockWidthPortraitFactor * ((float) this.mDisplayMetrics.widthPixels));
            } else {
                imeDockLP.width = (int) (this.mImeDockWidthLandscpaeFactor * ((float) this.mDisplayMetrics.widthPixels));
            }
            imeDockLP.height = IME_DOCK_HEIGHT;
            this.mImeDockDialog.getWindow().setAttributes(imeDockLP);
        }
    }

    public void updateImeDockPosition(boolean reset) {
        int i = IME_DOCK_MIN_X_POSITION;
        if (this.mImeDockDialog.getWindow() != null) {
            LayoutParams imeDockLP = this.mImeDockDialog.getWindow().getAttributes();
            if (reset) {
                switch (getSplitFocusPosition()) {
                    case IME_DOCK_MIN_X_POSITION /*0*/:
                        if (!this.mIsPortrait) {
                            i = this.mDisplayMetrics.widthPixels / 2;
                        }
                        imeDockLP.x = i;
                        if (this.mIsPortrait) {
                            i = this.mDisplayMetrics.heightPixels / 2;
                        } else {
                            i = IME_DOCK_MIN_Y_POSITION;
                        }
                        imeDockLP.y = i;
                        break;
                    case HwFragmentContainer.TRANSITION_FADE /*1*/:
                        imeDockLP.x = IME_DOCK_MIN_X_POSITION;
                        imeDockLP.y = IME_DOCK_MIN_Y_POSITION;
                        break;
                }
            }
            int k = this.mDisplayMetrics.heightPixels;
            int m = this.mDisplayMetrics.widthPixels;
            int previousX = imeDockLP.x;
            imeDockLP.x = imeDockLP.y;
            imeDockLP.y = previousX;
            if (imeDockLP.y > k) {
                imeDockLP.y = k;
            } else if (imeDockLP.y < IME_DOCK_MIN_Y_POSITION) {
                imeDockLP.y = IME_DOCK_MIN_Y_POSITION;
            }
            if (imeDockLP.x > m) {
                imeDockLP.x = m;
            } else if (imeDockLP.x > m - imeDockLP.width) {
                imeDockLP.x = m - imeDockLP.width;
            } else if (imeDockLP.x < 0) {
                imeDockLP.x = IME_DOCK_MIN_X_POSITION;
            }
            this.mImeDockDialog.getWindow().setAttributes(imeDockLP);
        }
    }

    private int getSplitFocusPosition() {
        return IME_DOCK_MIN_X_POSITION;
    }
}
