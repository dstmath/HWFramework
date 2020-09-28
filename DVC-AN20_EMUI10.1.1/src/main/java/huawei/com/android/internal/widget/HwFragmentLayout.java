package huawei.com.android.internal.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.FreezeScreenScene;
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
import huawei.com.android.internal.widget.HwFragmentFrameLayout;
import java.math.BigDecimal;

public class HwFragmentLayout extends RelativeLayout implements View.OnTouchListener, HwFragmentFrameLayout.HwFragmentFrameLayoutCallback {
    private static final int ANIMATE_DURATION = 100;
    private static final int ANIMATION_END_Z_TRANS = -1;
    private static final int BLUR_LAYER_BACKGROUND_COLOR = -16777216;
    private static final int COLOR_SPLIT_LINE_CLICKED = -16744961;
    private static final int COLOR_SPLIT_LINE_DEFAULT = 419430400;
    private static final int DEFAULT_SELECTED_CONTAINER = -1;
    private static final double DEVICE_SIZE_55 = 5.5d;
    private static final double DEVICE_SIZE_80 = 8.0d;
    public static final float DISPLAY_RATE_FIFTY_PERCENT = 0.5f;
    public static final float DISPLAY_RATE_FORTY_PERCENT = 0.4f;
    private static final int DISPLAY_RATE_SCALE = 2;
    public static final float DISPLAY_RATE_SIXTY_PERCENT = 0.6f;
    private static final int EXTRA_ANIMATION_DURATION = 50;
    private static final float EXTRA_DP_VALUE = 0.5f;
    private static final int INCH_POWER_SIZE = 2;
    private static final boolean IS_DEBUG = false;
    private static final int LEFT_CONTENT_ID = 655361;
    private static final int MAX_LOCATIONS = 2;
    private static final int MIN_DISTANCE_MOVED = 15;
    private static final int MSG_REFRESH_FRAGMENT_LAYOUT = 11;
    private static final int MSG_SEND_DELAYED = 300;
    private static final int MSG_SET_TOUCH_DELEGATE = 10;
    private static final int RIGHT_CONTENT_ID = 655362;
    private static final int SPLIT_BTN_ID = 655364;
    private static final int SPLIT_LINE_ID = 655363;
    private static final int SPLIT_WIDTH_DIV = 2;
    private static final String TAG = "FragmentLayout";
    private static final int TOUCH_DELEGATE_VALUE = 25;
    private static final int WIDTH_LIMIT_LAND = 592;
    private static final int WIDTH_LIMIT_PORT = 533;
    private static final int WIDTH_SPLIT_LINE_CLICKED = 3;
    private static double sDeviceSize = 0.0d;
    private int mAppWidth;
    private FrameLayout.LayoutParams mBlurLayerParams;
    private boolean mCanMove;
    private Context mContext;
    private Display mDisplay;
    private float mDisplayRate;
    private AlphaAnimation mFadeInAnimation;
    private AlphaAnimation mFadeOutAnimation;
    private HwFragmentLayoutCallback mFragmentLayoutCallback;
    private Handler mHandler;
    private IWindowManager mInterfaceWindowManager;
    private boolean mIsAnimationDisplayed;
    private boolean mIsClicked;
    private boolean mIsMovingX;
    private boolean mIsMovingY;
    private double mLandSeparateSize;
    private RelativeLayout.LayoutParams mLayoutParams;
    private ImageView mLeftBlurLayer;
    private HwFragmentFrameLayout mLeftContent;
    private Animation mLeftIn;
    private Animation mLeftOut;
    private RelativeLayout.LayoutParams mLeftParams;
    private final int[] mLocations;
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
    private int mWidthSplitLineDefault;
    private WindowManager mWindowManager;

    public interface HwFragmentLayoutCallback {
        void setDisplayRate(float f);
    }

    public HwFragmentLayout(Context context) {
        super(context);
        this.mSplitMode = 0;
        this.mCanMove = false;
        this.mSelectedContainer = -1;
        this.mLocations = new int[2];
        this.mIsAnimationDisplayed = false;
        this.mHandler = new Handler() {
            /* class huawei.com.android.internal.widget.HwFragmentLayout.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 10) {
                    HwFragmentLayout.this.setSplitLineTouchDelegate();
                } else if (i == 11) {
                    HwFragmentLayout.this.refreshFragmentLayout();
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
        this.mLocations = new int[2];
        this.mIsAnimationDisplayed = false;
        this.mHandler = new Handler() {
            /* class huawei.com.android.internal.widget.HwFragmentLayout.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 10) {
                    HwFragmentLayout.this.setSplitLineTouchDelegate();
                } else if (i == 11) {
                    HwFragmentLayout.this.refreshFragmentLayout();
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
        this.mLocations = new int[2];
        this.mIsAnimationDisplayed = false;
        this.mHandler = new Handler() {
            /* class huawei.com.android.internal.widget.HwFragmentLayout.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 10) {
                    HwFragmentLayout.this.setSplitLineTouchDelegate();
                } else if (i == 11) {
                    HwFragmentLayout.this.refreshFragmentLayout();
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
        this.mLocations = new int[2];
        this.mIsAnimationDisplayed = false;
        this.mHandler = new Handler() {
            /* class huawei.com.android.internal.widget.HwFragmentLayout.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 10) {
                    HwFragmentLayout.this.setSplitLineTouchDelegate();
                } else if (i == 11) {
                    HwFragmentLayout.this.refreshFragmentLayout();
                }
            }
        };
        init(context, displayRate, canMove);
    }

    private void init(Context context, float displayRate, boolean canMove) {
        init(context);
        setDisplayRate(displayRate);
        setCanMove(canMove);
        iniAnimation(context);
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
        this.mWidthSplitLineDefault = this.mContext.getResources().getDimensionPixelSize(34472186);
        if (this.mContext.getSystemService(FreezeScreenScene.WINDOW_PARAM) instanceof WindowManager) {
            this.mWindowManager = (WindowManager) this.mContext.getSystemService(FreezeScreenScene.WINDOW_PARAM);
            this.mDisplay = this.mWindowManager.getDefaultDisplay();
            this.mMetrics = new DisplayMetrics();
            this.mDisplay.getMetrics(this.mMetrics);
        }
        this.mLayoutParams = new RelativeLayout.LayoutParams(-1, -1);
        setLayoutParams(this.mLayoutParams);
        initFragment();
        initSplitImageView();
        this.mBlurLayerParams = new FrameLayout.LayoutParams(-1, -1);
        this.mLeftBlurLayer = new ImageView(this.mContext);
        this.mLeftBlurLayer.setBackgroundColor(BLUR_LAYER_BACKGROUND_COLOR);
        this.mLeftContent.addView(this.mLeftBlurLayer, this.mBlurLayerParams);
        this.mLeftBlurLayer.setVisibility(4);
        this.mRightBlurLayer = new ImageView(this.mContext);
        this.mRightBlurLayer.setBackgroundColor(BLUR_LAYER_BACKGROUND_COLOR);
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

    private void iniAnimation(Context context) {
        if (this.mRightIn == null) {
            this.mRightIn = AnimationUtils.loadAnimation(context, 34209792);
            this.mRightIn.setAnimationListener(new Animation.AnimationListener() {
                /* class huawei.com.android.internal.widget.HwFragmentLayout.AnonymousClass2 */

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
                /* class huawei.com.android.internal.widget.HwFragmentLayout.AnonymousClass3 */

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
    }

    private void initFragment() {
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
    }

    private void initSplitImageView() {
        this.mSplitLine = new ImageView(this.mContext);
        this.mSplitLine.setId(SPLIT_LINE_ID);
        this.mSplitLine.setBackgroundColor(COLOR_SPLIT_LINE_DEFAULT);
        this.mSplitLineParams = new RelativeLayout.LayoutParams(this.mWidthSplitLineDefault, -1);
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
    }

    /* access modifiers changed from: private */
    public class FadeAnimationListener implements Animation.AnimationListener {
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
        if (this.mLeftContent.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
            if (this.mLeftParams.width != leftWidth) {
                RelativeLayout.LayoutParams layoutParams = this.mLeftParams;
                layoutParams.width = leftWidth;
                this.mLeftContent.setLayoutParams(layoutParams);
                if (this.mRightContent.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                    this.mRightParams = (RelativeLayout.LayoutParams) this.mRightContent.getLayoutParams();
                    this.mRightParams.addRule(17, LEFT_CONTENT_ID);
                    this.mRightParams.setMarginStart(this.mWidthSplitLineDefault);
                    this.mRightContent.setLayoutParams(this.mRightParams);
                }
                if (this.mSplitLine.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                    this.mSplitLineParams = (RelativeLayout.LayoutParams) this.mSplitLine.getLayoutParams();
                    RelativeLayout.LayoutParams layoutParams2 = this.mSplitLineParams;
                    layoutParams2.width = this.mWidthSplitLineDefault;
                    layoutParams2.addRule(17, LEFT_CONTENT_ID);
                    this.mSplitLine.setLayoutParams(this.mSplitLineParams);
                }
                if (this.mCanMove) {
                    this.mHandler.removeMessages(10);
                    this.mHandler.sendEmptyMessageDelayed(10, 300);
                }
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
                    refreshLeftDisplayAnimation();
                }
                getLeftLayout().setVisibility(0);
                getRightLayout().setVisibility(8);
                setLeftContent();
                return;
            }
            if (getLeftLayout().getVisibility() == 0 && getRightLayout().getVisibility() == 8) {
                refreshRightDisplayAnimation();
            }
            getLeftLayout().setVisibility(8);
            getRightLayout().setVisibility(0);
            setRightContent();
        } else if (columnNumber == 2) {
            getLeftLayout().setVisibility(0);
            getLeftLayout().setAlpha(1.0f);
            getRightLayout().setVisibility(0);
            getRightLayout().setAlpha(1.0f);
            getSplitLine().setVisibility(0);
            setDisplayRate(this.mDisplayRate);
        }
    }

    private void refreshLeftDisplayAnimation() {
        if (this.mIsAnimationDisplayed) {
            getLeftBlurLayer().startAnimation(this.mFadeInAnimation);
            getLeftLayout().startAnimation(this.mLeftIn);
            getRightLayout().startAnimation(this.mRightOut);
        }
    }

    private void refreshRightDisplayAnimation() {
        if (this.mIsAnimationDisplayed) {
            getLeftBlurLayer().startAnimation(this.mFadeOutAnimation);
            getLeftLayout().startAnimation(this.mLeftOut);
            getRightLayout().startAnimation(this.mRightIn);
        }
    }

    private void setLeftContent() {
        if (this.mLeftContent.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
            if (this.mLeftParams.width != -1) {
                RelativeLayout.LayoutParams layoutParams = this.mLeftParams;
                layoutParams.width = -1;
                this.mLeftContent.setLayoutParams(layoutParams);
            }
        }
    }

    private void setRightContent() {
        if (this.mRightContent.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            this.mRightParams = (RelativeLayout.LayoutParams) this.mRightContent.getLayoutParams();
            if (this.mRightParams.width != -1 || this.mRightParams.getMarginStart() != 0) {
                this.mRightParams.setMarginStart(0);
                RelativeLayout.LayoutParams layoutParams = this.mRightParams;
                layoutParams.width = -1;
                this.mRightContent.setLayoutParams(layoutParams);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void displayAnimation() {
        this.mIsAnimationDisplayed = true;
    }

    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction() & 255;
        if (action == 0) {
            onTouchForActionDown(event);
        } else if (action != 2 || !this.mIsClicked) {
            return true;
        } else {
            onTouchForActionMove(event);
        }
        return true;
    }

    private void onTouchForActionDown(MotionEvent event) {
        this.mStartX = (int) event.getRawX();
        this.mStartY = (int) event.getRawY();
        getLocationOnScreen(this.mLocations);
        this.mSplitLine.setPressed(true);
        this.mSplitLine.setBackgroundColor(COLOR_SPLIT_LINE_CLICKED);
        if (this.mSplitLine.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            this.mSplitLineParams = (RelativeLayout.LayoutParams) this.mSplitLine.getLayoutParams();
            this.mSplitLineParams.width = dip2px(this.mContext, 3.0f);
            RelativeLayout.LayoutParams layoutParams = this.mSplitLineParams;
            layoutParams.setMarginStart((-(layoutParams.width - this.mWidthSplitLineDefault)) / 2);
            this.mSplitLine.setLayoutParams(this.mSplitLineParams);
        }
        setSplitBtnParams(this.mStartY);
        this.mSplitBtn.setVisibility(0);
        this.mIsMovingX = false;
        this.mIsMovingY = false;
        this.mIsClicked = true;
    }

    private void onTouchForActionMove(MotionEvent event) {
        int valueX = (int) event.getRawX();
        int valueY = (int) event.getRawY();
        boolean isExceededSlopY = Math.abs(valueY - this.mStartY) > 15;
        if (!this.mIsMovingY && isExceededSlopY) {
            this.mIsMovingY = true;
        }
        if (this.mIsMovingY) {
            setSplitBtnParams(valueY);
        }
        boolean isExceededSlopX = Math.abs(valueX - this.mStartX) > 15;
        if (!this.mIsMovingX && isExceededSlopX) {
            this.mIsMovingX = true;
        }
        if (this.mIsMovingX) {
            setLeftLayoutParams(valueX - this.mLocations[0]);
        }
    }

    private void setSplitBtnParams(int locationY) {
        if (this.mSplitBtn.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            this.mSplitBtnParams = (RelativeLayout.LayoutParams) this.mSplitBtn.getLayoutParams();
            this.mSplitBtnParams.setMarginStart((-(this.mSplitBtnWidth - this.mWidthSplitLineDefault)) / 2);
            int[] iArr = this.mLocations;
            int i = this.mSplitBtnHeight;
            if ((locationY - iArr[1]) - (i / 2) < 0) {
                this.mSplitBtnParams.topMargin = 0;
            } else if ((locationY - iArr[1]) + (i / 2) > getHeight()) {
                this.mSplitBtnParams.topMargin = getHeight() - this.mSplitBtnHeight;
            } else {
                this.mSplitBtnParams.topMargin = (locationY - this.mLocations[1]) - (this.mSplitBtnHeight / 2);
            }
            this.mSplitBtn.setLayoutParams(this.mSplitBtnParams);
        }
    }

    private void setLeftLayoutParams(int widthParams) {
        int width = widthParams;
        float displayRate = this.mDisplayRate;
        if (getLayoutDirection() == 1) {
            width = this.mAppWidth - width;
        }
        if (this.mLeftContent.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            this.mLeftParams = (RelativeLayout.LayoutParams) this.mLeftContent.getLayoutParams();
            int i = this.mAppWidth;
            if (((float) width) < ((float) i) * 0.4f) {
                this.mLeftParams.width = (int) (((float) i) * 0.4f);
                this.mDisplayRate = 0.4f;
            } else if (((float) width) > ((float) i) * 0.6f) {
                this.mLeftParams.width = (int) (((float) i) * 0.6f);
                this.mDisplayRate = 0.6f;
            } else {
                this.mLeftParams.width = width;
                this.mDisplayRate = new BigDecimal((double) (((float) width) / ((float) i))).setScale(2, 4).floatValue();
            }
            this.mLeftContent.setLayoutParams(this.mLeftParams);
        }
        HwFragmentLayoutCallback hwFragmentLayoutCallback = this.mFragmentLayoutCallback;
        if (hwFragmentLayoutCallback != null) {
            float f = this.mDisplayRate;
            if (displayRate != f) {
                hwFragmentLayoutCallback.setDisplayRate(f);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        this.mIsClicked = false;
        if (getRightLayout().animate() != null) {
            getRightLayout().animate().cancel();
        }
        if (getLeftLayout().animate() != null) {
            getLeftLayout().animate().cancel();
        }
        if (getLeftBlurLayer().animate() != null) {
            getLeftBlurLayer().animate().cancel();
        }
        if (width != oldWidth) {
            this.mAppWidth = width;
            this.mHandler.removeMessages(11);
            this.mHandler.sendEmptyMessage(11);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction() & 255;
        if (action == 1 || action == 3) {
            dispatchTouchEventForCancel();
        }
        return super.dispatchTouchEvent(event);
    }

    private void dispatchTouchEventForCancel() {
        if (this.mSplitLine.isPressed()) {
            this.mIsClicked = false;
            this.mSplitLine.setPressed(false);
            this.mSplitLine.setBackgroundColor(COLOR_SPLIT_LINE_DEFAULT);
            if (getSplitLine().getVisibility() == 0) {
                if (this.mSplitLine.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                    this.mSplitLineParams = (RelativeLayout.LayoutParams) this.mSplitLine.getLayoutParams();
                    RelativeLayout.LayoutParams layoutParams = this.mSplitLineParams;
                    layoutParams.width = this.mWidthSplitLineDefault;
                    layoutParams.addRule(17, LEFT_CONTENT_ID);
                    this.mSplitLineParams.setMarginStart(0);
                    getSplitLine().setLayoutParams(this.mSplitLineParams);
                }
                this.mHandler.removeMessages(10);
                this.mHandler.sendEmptyMessageDelayed(10, 300);
            }
            if (getSplitBtn().getVisibility() != 8) {
                getSplitBtn().setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class huawei.com.android.internal.widget.HwFragmentLayout.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private float mRate;
        private int mSelected;

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

        public int getSelected() {
            return this.mSelected;
        }

        public void setSelected(int selected) {
            this.mSelected = selected;
        }

        public float getRate() {
            return this.mRate;
        }

        public void setRate(float rate) {
            this.mRate = rate;
        }
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.setSelected(getSelectedContainer());
        if (this.mCanMove) {
            savedState.setRate(this.mDisplayRate);
        }
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setSelectedContainer(savedState.getSelected());
        if (this.mCanMove) {
            this.mDisplayRate = savedState.getRate();
        }
    }

    public void setSeparateDeviceSize(double landSeparateSize, double portSeparateSize) {
        if (landSeparateSize > 0.0d) {
            this.mLandSeparateSize = landSeparateSize;
        }
        if (portSeparateSize > 0.0d) {
            this.mPortSeparateSize = portSeparateSize;
        }
    }

    /* access modifiers changed from: protected */
    public int calculateColumnsNumber() {
        return calculateColumnsNumber(this.mContext.getResources().getConfiguration().orientation, this.mAppWidth);
    }

    /* access modifiers changed from: protected */
    public int calculateColumnsNumber(int orientation, int appWidth) {
        double sizeInch = calculateDeviceSize();
        double landSeparateSize = this.mLandSeparateSize;
        if (landSeparateSize <= 0.0d) {
            landSeparateSize = 5.5d;
        }
        double portSeparateSize = this.mPortSeparateSize;
        if (portSeparateSize <= 0.0d) {
            portSeparateSize = 8.0d;
        }
        int i = this.mSplitMode;
        if (i == 1) {
            return 1;
        }
        if (i == 3) {
            return 2;
        }
        if (i == 2) {
            if (orientation == 2) {
                return columnsNumberByAppWidth(appWidth, WIDTH_LIMIT_LAND);
            }
            return 1;
        } else if (sizeInch < landSeparateSize && Math.abs(sizeInch - landSeparateSize) > 0.0d) {
            return 1;
        } else {
            if (sizeInch - portSeparateSize > 0.0d) {
                if (orientation == 2) {
                    return columnsNumberByAppWidth(appWidth, WIDTH_LIMIT_LAND);
                }
                return columnsNumberByAppWidth(appWidth, WIDTH_LIMIT_PORT);
            } else if (orientation == 2) {
                return columnsNumberByAppWidth(appWidth, WIDTH_LIMIT_LAND);
            } else {
                return 1;
            }
        }
    }

    private int columnsNumberByAppWidth(int appWidth, int widthLimit) {
        if (appWidth >= dip2px(this.mContext, (float) widthLimit)) {
            return 2;
        }
        return 1;
    }

    private double calculateDeviceSize() {
        double d = sDeviceSize;
        if (d > 0.0d) {
            return d;
        }
        IWindowManager windowManager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        if (windowManager != null) {
            Point point = new Point();
            try {
                windowManager.getInitialDisplaySize(0, point);
                sDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / metrics.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / metrics.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                return sDeviceSize;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while calculate device size");
                this.mInterfaceWindowManager = null;
            }
        }
        sDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) metrics.widthPixels) / metrics.xdpi), 2.0d) + Math.pow((double) (((float) metrics.heightPixels) / metrics.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return sDeviceSize;
    }

    private IWindowManager getWindowManager() {
        if (this.mInterfaceWindowManager == null) {
            this.mInterfaceWindowManager = IWindowManager.Stub.asInterface(ServiceManager.checkService(FreezeScreenScene.WINDOW_PARAM));
        }
        return this.mInterfaceWindowManager;
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
    public void setSelectContainerByTouch(boolean isEnabled) {
        HwFragmentFrameLayout hwFragmentFrameLayout = this.mLeftContent;
        if (hwFragmentFrameLayout != null) {
            hwFragmentFrameLayout.setSelectContainerByTouch(isEnabled);
        }
        HwFragmentFrameLayout hwFragmentFrameLayout2 = this.mRightContent;
        if (hwFragmentFrameLayout2 != null) {
            hwFragmentFrameLayout2.setSelectContainerByTouch(isEnabled);
        }
    }

    @Override // huawei.com.android.internal.widget.HwFragmentFrameLayout.HwFragmentFrameLayoutCallback
    public void setSelectedFrameLayout(int id) {
        if (id == getLeftContentID()) {
            setSelectedContainer(0);
        } else if (id == getRightContentID()) {
            setSelectedContainer(1);
        }
    }
}
