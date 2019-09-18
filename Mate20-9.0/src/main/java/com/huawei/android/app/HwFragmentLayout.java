package com.huawei.android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.huawei.android.app.HwFragmentFrameLayout;
import java.math.BigDecimal;

public class HwFragmentLayout extends RelativeLayout implements View.OnTouchListener, HwFragmentFrameLayout.HwFragmentFrameLayoutCallback {
    private static final int ANIMATE_DURATION = 100;
    private static final int BLUR_LAYER_BACKGROUND_COLOR = -16777216;
    private static final int COLOR_SPLIT_LINE_CLICKED = -16744961;
    private static final int COLOR_SPLIT_LINE_DEFAULT = 419430400;
    private static final boolean DEBUG = false;
    private static final double DEVICE_SIZE_55 = 5.5d;
    private static final double DEVICE_SIZE_80 = 8.0d;
    public static final float DISPLAY_RATE_FIFTY_PERCENT = 0.5f;
    public static final float DISPLAY_RATE_FORTY_PERCENT = 0.4f;
    public static final float DISPLAY_RATE_SIXTY_PERCENT = 0.6f;
    private static final int LEFT_CONTENT_ID = 655361;
    private static final int MIN_DISTANCE_MOVED = 15;
    private static final int MSG_REFRESH_FRAMENT_LAYOUT = 11;
    private static final int MSG_SEND_DELAYED = 300;
    private static final int MSG_SET_TOUCH_DELEGATE = 10;
    private static final int RIGHT_CONTENT_ID = 655362;
    private static final int SPLIT_BTN_ID = 655364;
    private static final int SPLIT_LINE_ID = 655363;
    private static final String TAG = "FragmentLayout";
    private static final int TOUCH_DELEGATE_VALUE = 25;
    private static final int WIDTH_LIMIT_LAND = 592;
    private static final int WIDTH_LIMIT_PORT = 533;
    private static final int WIDTH_SPLIT_LINE_CLICKED = 3;
    private static final int WIDTH_SPLIT_LINE_DEFAULT = 2;
    private static double mDeviceSize = 0.0d;
    private int mAppWidth;
    private FrameLayout.LayoutParams mBlurLayerParams;
    private boolean mCanMove;
    private boolean mClicked;
    private Context mContext;
    private Display mDisplay;
    private boolean mDisplayAnimation;
    private float mDisplayRate;
    private AlphaAnimation mFadeInAnimation;
    private AlphaAnimation mFadeOutAnimation;
    private HwFragmentLayoutCallback mFragmentLayoutCallback;
    private Handler mHandler;
    private IWindowManager mIWindowManager;
    private double mLandSeparateSize;
    private RelativeLayout.LayoutParams mLayoutParams;
    private ImageView mLeftBlurLayer;
    private HwFragmentFrameLayout mLeftContent;
    private Animation mLeftIn;
    private Animation mLeftOut;
    private RelativeLayout.LayoutParams mLeftParams;
    private final int[] mLocation;
    private DisplayMetrics mMetrics;
    private double mPortSeparateSize;
    private ImageView mRightBlurLayer;
    private HwFragmentFrameLayout mRightContent;
    private Animation mRightIn;
    private Animation mRightOut;
    private RelativeLayout.LayoutParams mRightParams;
    private int mSelectedContainer;
    private ImageView mSplitBtn;
    private int mSplitBtnHeight;
    private RelativeLayout.LayoutParams mSplitBtnParams;
    private int mSplitBtnWidth;
    private ImageView mSplitLine;
    private RelativeLayout.LayoutParams mSplitLineParams;
    private int mSplitMode;
    private int mStartX;
    private int mStartY;
    private WindowManager mWindowManager;
    private boolean mXMoving;
    private boolean mYMoving;

    private class FadeAnimationListener implements Animation.AnimationListener {
        private FadeAnimationListener() {
        }

        public void onAnimationStart(Animation animation) {
            HwFragmentLayout.this.getLeftBlurLayer().setLayerType(2, null);
            HwFragmentLayout.this.getLeftBlurLayer().setVisibility(0);
            HwFragmentLayout.this.getLeftBlurLayer().setTranslationZ(1.0f);
        }

        public void onAnimationEnd(Animation animation) {
            HwFragmentLayout.this.getLeftBlurLayer().setLayerType(0, null);
            HwFragmentLayout.this.getLeftBlurLayer().setVisibility(8);
            HwFragmentLayout.this.getLeftBlurLayer().setTranslationZ(-1.0f);
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    public interface HwFragmentLayoutCallback {
        void setDisplayRate(float f);
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        float mRate;
        int mSelected;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            this.mSelected = source.readInt();
            this.mRate = source.readFloat();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mSelected);
            dest.writeFloat(this.mRate);
        }
    }

    public HwFragmentLayout(Context context) {
        super(context);
        this.mSplitMode = 0;
        this.mCanMove = false;
        this.mSelectedContainer = -1;
        this.mLocation = new int[2];
        this.mDisplayAnimation = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        HwFragmentLayout.this.setSplitLineTouchDelegate();
                        return;
                    case 11:
                        HwFragmentLayout.this.refreshFragmentLayout();
                        return;
                    default:
                        return;
                }
            }
        };
        init(context, 0.4f, false);
    }

    public HwFragmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSplitMode = 0;
        this.mCanMove = false;
        this.mSelectedContainer = -1;
        this.mLocation = new int[2];
        this.mDisplayAnimation = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        HwFragmentLayout.this.setSplitLineTouchDelegate();
                        return;
                    case 11:
                        HwFragmentLayout.this.refreshFragmentLayout();
                        return;
                    default:
                        return;
                }
            }
        };
        init(context, 0.4f, false);
    }

    public HwFragmentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mSplitMode = 0;
        this.mCanMove = false;
        this.mSelectedContainer = -1;
        this.mLocation = new int[2];
        this.mDisplayAnimation = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        HwFragmentLayout.this.setSplitLineTouchDelegate();
                        return;
                    case 11:
                        HwFragmentLayout.this.refreshFragmentLayout();
                        return;
                    default:
                        return;
                }
            }
        };
        init(context, 0.4f, false);
    }

    public HwFragmentLayout(Context context, float displayRate) {
        this(context, displayRate, false);
    }

    public HwFragmentLayout(Context context, float displayRate, boolean canMove) {
        super(context);
        this.mSplitMode = 0;
        this.mCanMove = false;
        this.mSelectedContainer = -1;
        this.mLocation = new int[2];
        this.mDisplayAnimation = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        HwFragmentLayout.this.setSplitLineTouchDelegate();
                        return;
                    case 11:
                        HwFragmentLayout.this.refreshFragmentLayout();
                        return;
                    default:
                        return;
                }
            }
        };
        init(context, displayRate, canMove);
    }

    private void init(Context context, float displayRate, boolean canMove) {
        init(context);
        setDisplayRate(displayRate);
        setCanMove(canMove);
        if (this.mRightIn == null) {
            this.mRightIn = AnimationUtils.loadAnimation(context, 34209792);
            this.mRightIn.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    HwFragmentLayout.this.getRightLayout().setBackgroundColor(-197380);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    HwFragmentLayout.this.getRightLayout().setBackground(null);
                }
            });
        }
        if (this.mLeftOut == null) {
            this.mLeftOut = AnimationUtils.loadAnimation(context, 34209793);
            this.mLeftOut.setDuration(250);
        }
        if (this.mLeftIn == null) {
            this.mLeftIn = AnimationUtils.loadAnimation(context, 34209794);
        }
        if (this.mRightOut == null) {
            this.mRightOut = AnimationUtils.loadAnimation(context, 34209795);
            this.mRightOut.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    HwFragmentLayout.this.getRightLayout().setBackgroundColor(-197380);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    HwFragmentLayout.this.getRightLayout().setBackground(null);
                }
            });
        }
        if (this.mFadeInAnimation == null) {
            this.mFadeInAnimation = new AlphaAnimation(0.4f, 0.0f);
            this.mFadeInAnimation.setInterpolator(HwFragmentContainer.INTERPOLATOR_20_90);
            this.mFadeInAnimation.setDuration(300);
            this.mFadeInAnimation.setAnimationListener(new FadeAnimationListener());
        }
        if (this.mFadeOutAnimation == null) {
            this.mFadeOutAnimation = new AlphaAnimation(0.0f, 0.4f);
            this.mFadeOutAnimation.setInterpolator(HwFragmentContainer.INTERPOLATOR_20_90);
            this.mFadeOutAnimation.setDuration(250);
            this.mFadeOutAnimation.setAnimationListener(new FadeAnimationListener());
        }
    }

    private void init(Context context) {
        this.mContext = context;
        this.mAppWidth = dip2px(context, (float) this.mContext.getResources().getConfiguration().screenWidthDp);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mMetrics = new DisplayMetrics();
        this.mDisplay.getMetrics(this.mMetrics);
        this.mLayoutParams = new RelativeLayout.LayoutParams(-1, -1);
        setLayoutParams(this.mLayoutParams);
        this.mLeftContent = new HwFragmentFrameLayout(this.mContext);
        this.mLeftContent.setId(LEFT_CONTENT_ID);
        this.mLeftParams = new RelativeLayout.LayoutParams(-2, -1);
        this.mLeftParams.addRule(20);
        addView(this.mLeftContent, this.mLeftParams);
        this.mRightContent = new HwFragmentFrameLayout(this.mContext);
        this.mRightContent.setId(RIGHT_CONTENT_ID);
        this.mRightParams = new RelativeLayout.LayoutParams(-2, -1);
        this.mRightParams.addRule(21);
        this.mRightParams.addRule(17, LEFT_CONTENT_ID);
        addView(this.mRightContent, this.mRightParams);
        this.mSplitLine = new ImageView(this.mContext);
        this.mSplitLine.setId(SPLIT_LINE_ID);
        this.mSplitLine.setBackgroundColor(COLOR_SPLIT_LINE_DEFAULT);
        this.mSplitLineParams = new RelativeLayout.LayoutParams(2, -1);
        this.mSplitLineParams.addRule(17, LEFT_CONTENT_ID);
        this.mSplitLine.setLayoutParams(this.mSplitLineParams);
        addView(this.mSplitLine, this.mSplitLineParams);
        this.mSplitBtn = new ImageView(this.mContext);
        this.mSplitBtn.setId(SPLIT_BTN_ID);
        this.mSplitBtn.setImageResource(33751581);
        this.mSplitBtnParams = new RelativeLayout.LayoutParams(-2, -2);
        this.mSplitBtnParams.addRule(17, LEFT_CONTENT_ID);
        this.mSplitBtn.setLayoutParams(this.mSplitBtnParams);
        this.mSplitBtn.setVisibility(8);
        addView(this.mSplitBtn, this.mSplitBtnParams);
        this.mBlurLayerParams = new FrameLayout.LayoutParams(-1, -1);
        this.mLeftBlurLayer = new ImageView(this.mContext);
        this.mLeftBlurLayer.setBackgroundColor(-16777216);
        this.mLeftContent.addView(this.mLeftBlurLayer, this.mBlurLayerParams);
        this.mLeftBlurLayer.setVisibility(4);
        this.mRightBlurLayer = new ImageView(this.mContext);
        this.mRightBlurLayer.setBackgroundColor(-16777216);
        this.mRightContent.addView(this.mRightBlurLayer, this.mBlurLayerParams);
        this.mRightBlurLayer.setVisibility(8);
        this.mLeftContent.setFragmentFrameLayoutCallback(this);
        this.mRightContent.setFragmentFrameLayoutCallback(this);
        Bitmap splitBtnBitmap = getBitmapFor(33751581);
        if (splitBtnBitmap != null) {
            this.mSplitBtnWidth = splitBtnBitmap.getScaledWidth(this.mMetrics);
            this.mSplitBtnHeight = splitBtnBitmap.getScaledHeight(this.mMetrics);
            splitBtnBitmap.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public int getLeftContentID() {
        return LEFT_CONTENT_ID;
    }

    /* access modifiers changed from: protected */
    public int getRightContentID() {
        return RIGHT_CONTENT_ID;
    }

    /* access modifiers changed from: protected */
    public View getFragmentLayout() {
        return this;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getLeftLayout() {
        return this.mLeftContent;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getRightLayout() {
        return this.mRightContent;
    }

    /* access modifiers changed from: protected */
    public ImageView getSplitLine() {
        return this.mSplitLine;
    }

    /* access modifiers changed from: protected */
    public ImageView getSplitBtn() {
        return this.mSplitBtn;
    }

    /* access modifiers changed from: protected */
    public ImageView getLeftBlurLayer() {
        return this.mLeftBlurLayer;
    }

    /* access modifiers changed from: protected */
    public ImageView getRightBlurLayer() {
        return this.mRightBlurLayer;
    }

    public void setSplitMode(int splitMode) {
        this.mSplitMode = splitMode;
    }

    /* access modifiers changed from: protected */
    public void setDisplayRate(float displayRate) {
        this.mDisplayRate = displayRate;
        int leftWidth = (int) (((float) this.mAppWidth) * displayRate);
        this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
        if (this.mLeftParams.width != leftWidth) {
            this.mLeftParams.width = leftWidth;
            this.mLeftContent.setLayoutParams(this.mLeftParams);
            this.mRightParams = (RelativeLayout.LayoutParams) this.mRightContent.getLayoutParams();
            this.mRightParams.addRule(17, LEFT_CONTENT_ID);
            this.mRightParams.setMarginStart(2);
            this.mRightContent.setLayoutParams(this.mRightParams);
            this.mSplitLineParams = (RelativeLayout.LayoutParams) this.mSplitLine.getLayoutParams();
            this.mSplitLineParams.width = 2;
            this.mSplitLineParams.addRule(17, LEFT_CONTENT_ID);
            this.mSplitLine.setLayoutParams(this.mSplitLineParams);
            if (this.mCanMove) {
                this.mHandler.removeMessages(10);
                this.mHandler.sendEmptyMessageDelayed(10, 300);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setCanMove(boolean canMove) {
        this.mCanMove = canMove;
        if (this.mCanMove) {
            this.mSplitLine.setOnTouchListener(this);
            this.mHandler.removeMessages(10);
            this.mHandler.sendEmptyMessageDelayed(10, 300);
        }
    }

    /* access modifiers changed from: protected */
    public void setSplitLineTouchDelegate() {
        if (getSplitLine().getVisibility() != 8) {
            Rect bounds = new Rect();
            this.mSplitLine.getHitRect(bounds);
            bounds.left -= 25;
            bounds.right += 25;
            setTouchDelegate(new TouchDelegate(bounds, this.mSplitLine));
        }
    }

    /* access modifiers changed from: protected */
    public int getSelectedContainer() {
        return this.mSelectedContainer;
    }

    /* access modifiers changed from: protected */
    public void setSelectedContainer(int selectedContainer) {
        this.mSelectedContainer = selectedContainer;
    }

    /* access modifiers changed from: protected */
    public void refreshFragmentLayout() {
        int columnNumber = calculateColumnsNumber();
        if (columnNumber == 1) {
            getSplitLine().setVisibility(8);
            getSplitBtn().setVisibility(8);
            if (getSelectedContainer() == 0 || getSelectedContainer() == -1) {
                if (getLeftLayout().getVisibility() == 8 && getRightLayout().getVisibility() == 0) {
                    if (this.mDisplayAnimation) {
                        getLeftBlurLayer().startAnimation(this.mFadeInAnimation);
                        getLeftLayout().startAnimation(this.mLeftIn);
                        getRightLayout().startAnimation(this.mRightOut);
                    }
                    getRightLayout().setVisibility(8);
                    getLeftLayout().setVisibility(0);
                    this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
                    if (this.mLeftParams.width != -1) {
                        this.mLeftParams.width = -1;
                        this.mLeftContent.setLayoutParams(this.mLeftParams);
                        return;
                    }
                    return;
                }
                getLeftLayout().setVisibility(0);
                getRightLayout().setVisibility(8);
                this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
                if (this.mLeftParams.width != -1) {
                    this.mLeftParams.width = -1;
                    this.mLeftContent.setLayoutParams(this.mLeftParams);
                }
            } else if (getLeftLayout().getVisibility() == 0 && getRightLayout().getVisibility() == 8) {
                if (this.mDisplayAnimation) {
                    getLeftBlurLayer().startAnimation(this.mFadeOutAnimation);
                    getLeftLayout().startAnimation(this.mLeftOut);
                    getRightLayout().startAnimation(this.mRightIn);
                }
                getLeftLayout().setVisibility(8);
                getRightLayout().setVisibility(0);
                this.mRightParams = (RelativeLayout.LayoutParams) this.mRightContent.getLayoutParams();
                if (this.mRightParams.width != -1 || this.mRightParams.getMarginStart() != 0) {
                    this.mRightParams.setMarginStart(0);
                    this.mRightParams.width = -1;
                    this.mRightContent.setLayoutParams(this.mRightParams);
                }
            } else {
                getLeftLayout().setVisibility(8);
                getRightLayout().setVisibility(0);
                this.mRightParams = (RelativeLayout.LayoutParams) this.mRightContent.getLayoutParams();
                if (this.mRightParams.width != -1 || this.mRightParams.getMarginStart() != 0) {
                    this.mRightParams.setMarginStart(0);
                    this.mRightParams.width = -1;
                    this.mRightContent.setLayoutParams(this.mRightParams);
                }
            }
        } else if (columnNumber == 2) {
            getLeftLayout().setVisibility(0);
            getLeftLayout().setAlpha(1.0f);
            getRightLayout().setVisibility(0);
            getRightLayout().setAlpha(1.0f);
            getSplitLine().setVisibility(0);
            setDisplayRate(this.mDisplayRate);
        }
    }

    /* access modifiers changed from: protected */
    public void displayAnimation() {
        this.mDisplayAnimation = true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & 255;
        if (action == 0) {
            this.mStartX = (int) event.getRawX();
            this.mStartY = (int) event.getRawY();
            getLocationOnScreen(this.mLocation);
            this.mSplitLine.setPressed(true);
            this.mSplitLine.setBackgroundColor(COLOR_SPLIT_LINE_CLICKED);
            this.mSplitLineParams = (RelativeLayout.LayoutParams) this.mSplitLine.getLayoutParams();
            this.mSplitLineParams.width = dip2px(this.mContext, 3.0f);
            this.mSplitLineParams.setMarginStart((-(this.mSplitLineParams.width - 2)) / 2);
            this.mSplitLine.setLayoutParams(this.mSplitLineParams);
            setSplitBtnParams(this.mStartY);
            this.mSplitBtn.setVisibility(0);
            this.mXMoving = false;
            this.mYMoving = false;
            this.mClicked = true;
        } else if (action != 2 || !this.mClicked) {
            return true;
        } else {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            boolean exceededYSlop = Math.abs(y - this.mStartY) > 15;
            if (!this.mYMoving && exceededYSlop) {
                this.mYMoving = true;
            }
            if (this.mYMoving) {
                setSplitBtnParams(y);
            }
            boolean exceededXSlop = Math.abs(x - this.mStartX) > 15;
            if (!this.mXMoving && exceededXSlop) {
                this.mXMoving = true;
            }
            if (this.mXMoving) {
                setLeftLayoutParams(x - this.mLocation[0]);
            }
        }
        return true;
    }

    private void setSplitBtnParams(int y) {
        this.mSplitBtnParams = (RelativeLayout.LayoutParams) this.mSplitBtn.getLayoutParams();
        this.mSplitBtnParams.setMarginStart((-(this.mSplitBtnWidth - 2)) / 2);
        if ((y - this.mLocation[1]) - (this.mSplitBtnHeight / 2) < 0) {
            this.mSplitBtnParams.topMargin = 0;
        } else if ((y - this.mLocation[1]) + (this.mSplitBtnHeight / 2) > getHeight()) {
            this.mSplitBtnParams.topMargin = getHeight() - this.mSplitBtnHeight;
        } else {
            this.mSplitBtnParams.topMargin = (y - this.mLocation[1]) - (this.mSplitBtnHeight / 2);
        }
        this.mSplitBtn.setLayoutParams(this.mSplitBtnParams);
    }

    private void setLeftLayoutParams(int x) {
        this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
        float dispayRate = this.mDisplayRate;
        if (1 == getLayoutDirection()) {
            x = this.mAppWidth - x;
        }
        if (((float) x) < ((float) this.mAppWidth) * 0.4f) {
            this.mLeftParams.width = (int) (((float) this.mAppWidth) * 0.4f);
            this.mDisplayRate = 0.4f;
        } else if (((float) x) > ((float) this.mAppWidth) * 0.6f) {
            this.mLeftParams.width = (int) (((float) this.mAppWidth) * 0.6f);
            this.mDisplayRate = 0.6f;
        } else {
            this.mLeftParams.width = x;
            this.mDisplayRate = new BigDecimal((double) (((float) x) / ((float) this.mAppWidth))).setScale(2, 4).floatValue();
        }
        this.mLeftContent.setLayoutParams(this.mLeftParams);
        if (this.mFragmentLayoutCallback != null && dispayRate != this.mDisplayRate) {
            this.mFragmentLayoutCallback.setDisplayRate(this.mDisplayRate);
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mClicked = false;
        if (getRightLayout().animate() != null) {
            getRightLayout().animate().cancel();
        }
        if (getLeftLayout().animate() != null) {
            getLeftLayout().animate().cancel();
        }
        if (getLeftBlurLayer().animate() != null) {
            getLeftBlurLayer().animate().cancel();
        }
        if (w != oldw) {
            this.mAppWidth = w;
            this.mHandler.removeMessages(11);
            this.mHandler.sendEmptyMessage(11);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction() & 255;
        if ((action == 1 || action == 3) && this.mSplitLine.isPressed()) {
            this.mClicked = false;
            this.mSplitLine.setPressed(false);
            this.mSplitLine.setBackgroundColor(COLOR_SPLIT_LINE_DEFAULT);
            if (getSplitLine().getVisibility() == 0) {
                this.mSplitLineParams = (RelativeLayout.LayoutParams) this.mSplitLine.getLayoutParams();
                this.mSplitLineParams.width = 2;
                this.mSplitLineParams.addRule(17, LEFT_CONTENT_ID);
                this.mSplitLineParams.setMarginStart(0);
                getSplitLine().setLayoutParams(this.mSplitLineParams);
                this.mHandler.removeMessages(10);
                this.mHandler.sendEmptyMessageDelayed(10, 300);
            }
            if (getSplitBtn().getVisibility() != 8) {
                getSplitBtn().setVisibility(8);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mSelected = getSelectedContainer();
        if (this.mCanMove) {
            savedState.mRate = this.mDisplayRate;
        }
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setSelectedContainer(savedState.mSelected);
        if (this.mCanMove) {
            this.mDisplayRate = savedState.mRate;
        }
    }

    public void setSeparateDeviceSize(double landSeparateSize, double portSeparteSize) {
        if (landSeparateSize > 0.0d) {
            this.mLandSeparateSize = landSeparateSize;
        }
        if (portSeparteSize > 0.0d) {
            this.mPortSeparateSize = portSeparteSize;
        }
    }

    /* access modifiers changed from: protected */
    public int calculateColumnsNumber() {
        return calculateColumnsNumber(this.mContext.getResources().getConfiguration().orientation, this.mAppWidth);
    }

    /* access modifiers changed from: protected */
    public int calculateColumnsNumber(int orientation, int appWidth) {
        int i = orientation;
        int i2 = appWidth;
        double sizeInch = calculateDeviceSize();
        double landSeparateSize = this.mLandSeparateSize > 0.0d ? this.mLandSeparateSize : DEVICE_SIZE_55;
        double portSeparateSize = this.mPortSeparateSize > 0.0d ? this.mPortSeparateSize : DEVICE_SIZE_80;
        if (this.mSplitMode == 1) {
            return 1;
        }
        if (this.mSplitMode == 3) {
            return 2;
        }
        if (this.mSplitMode == 2) {
            if (i != 2 || i2 < dip2px(this.mContext, 592.0f)) {
                return 1;
            }
            return 2;
        } else if (sizeInch < landSeparateSize && Math.abs(sizeInch - landSeparateSize) > 0.0d) {
            return 1;
        } else {
            if (sizeInch - portSeparateSize > 0.0d) {
                if (i == 2) {
                    if (i2 >= dip2px(this.mContext, 592.0f)) {
                        return 2;
                    }
                    return 1;
                } else if (i2 >= dip2px(this.mContext, 533.0f)) {
                    return 2;
                } else {
                    return 1;
                }
            } else if (i != 2 || i2 < dip2px(this.mContext, 592.0f)) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    private double calculateDeviceSize() {
        if (mDeviceSize > 0.0d) {
            return mDeviceSize;
        }
        IWindowManager iwm = getIWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(dm);
        if (iwm != null) {
            Point point = new Point();
            try {
                iwm.getInitialDisplaySize(0, point);
                mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / dm.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                return mDeviceSize;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while calculate device size", e);
                this.mIWindowManager = null;
            }
        }
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return mDeviceSize;
    }

    private IWindowManager getIWindowManager() {
        if (this.mIWindowManager == null) {
            this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.checkService("window"));
        }
        return this.mIWindowManager;
    }

    private static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(this.mContext.getResources(), resId);
    }

    public void setFragmentLayoutCallback(HwFragmentLayoutCallback callback) {
        this.mFragmentLayoutCallback = callback;
    }

    /* access modifiers changed from: protected */
    public void setSelectContainerByTouch(boolean enabled) {
        if (this.mLeftContent != null) {
            this.mLeftContent.setSelectContainerByTouch(enabled);
        }
        if (this.mRightContent != null) {
            this.mRightContent.setSelectContainerByTouch(enabled);
        }
    }

    public void setSelectedFrameLayout(int id) {
        if (getLeftContentID() == id) {
            setSelectedContainer(0);
        } else if (getRightContentID() == id) {
            setSelectedContainer(1);
        }
    }
}
