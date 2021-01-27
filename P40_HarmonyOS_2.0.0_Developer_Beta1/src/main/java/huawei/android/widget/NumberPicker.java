package huawei.android.widget;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Process;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import com.huawei.android.os.VibratorEx;
import com.huawei.android.view.ViewEx;
import dalvik.system.DexClassLoader;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.utils.EmuiUtils;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class NumberPicker extends android.widget.NumberPicker {
    private static final float ALPHA_GRADIENT_BOUND = 0.4f;
    private static final String APK_PATH = "/system/framework/immersion.jar";
    private static final int COLOR_OPACITY_MASK = 16777215;
    private static final int DEFAULT_NORMAL_TEXT_COLOR = -15132391;
    private static final int DEFAULT_SELECTE_TEXT_COLOR = -16744961;
    private static final String DEX_OUTPUT_DIR = "/data/data/com.immersion/";
    private static final int FIRST_LIST_DEFAULT_SIZE = 10;
    private static final int FLING_BACKWARD = 1;
    private static final int FLING_FOWARD = 0;
    private static final int FLING_STATE_INDEX = 3;
    private static final int FLING_STOP = 2;
    private static final String GOOGLE_NP_CLASSNAME = "android.widget.NumberPicker";
    private static final int HALF_DIVIDER = 2;
    private static final String HW_CHINESE_MEDIUM_TYPEFACE = "HwChinese-medium";
    private static final String HW_VIBRATOR_SLIDE_TYPE1 = "haptic.slide.type1";
    private static final int MAX_VELOCITY_INDEX = 0;
    private static final int REALTIME_FLINGVELOCITY_INDEX = 2;
    private static final int REALTIME_VELOCITY_INDEX = 1;
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 5;
    private static final float SOUND_LEFT_VOLUME = 1.0f;
    private static final int SOUND_LOOP = 0;
    private static final int SOUND_PRIORITY = 0;
    private static final float SOUND_RATE = 1.0f;
    private static final float SOUND_RIGHT_VOLUME = 1.0f;
    private static final String TAG = "NumberPicker";
    private static final int TEXT_SIZE_DIVIDER = 3;
    private static final int TRANSPARENCY_LEFT_SHIFT = 24;
    private static final float VELOCITY_STAGE_1 = 0.1f;
    private static final float VELOCITY_STAGE_2 = 0.25f;
    private static final float VELOCITY_STAGE_3 = 0.55f;
    private static final float VELOCITY_STAGE_4 = 0.8f;
    private static final int VIBRATION_EFFECT_Y_BOUND = 10;
    private static DexClassLoader sClassLoader = null;
    private Context mContextVibrate;
    private final Typeface mDefaultTypeface;
    private int mEdgeOffset;
    private int mEdgeOffsetTop;
    private List<android.widget.NumberPicker> mFireList;
    private int mFlingDirection;
    private int mFlingState;
    private Class mGoogleNumberPickerClass;
    private int mGradientHeight;
    private final Typeface mHwChineseMediumTypeface;
    private int mInternalOffsetAbove;
    private int mInternalOffsetBelow;
    private boolean mIsDarkHwTheme;
    private boolean mIsLongPress;
    private boolean mIsSoundLoadFinished;
    private boolean mIsSupportVibrator;
    private boolean mIsSupportVibratorOld;
    private boolean mIsVibrateImplemented;
    private int mMaximumFlingVelocity;
    private int mNormalTextColor;
    private float mNormalTextSize;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private int mRealTimeFlingVelocity;
    private int mRealTimeVelocity;
    private int mSelectorOffset;
    private int mSelectorTextColor;
    private float mSelectorTextSize;
    private int mSmallTextColor;
    private int mSoundId;
    private SoundPool mSoundPool;
    private float mVelocityStage1;
    private float mVelocityStage2;
    private float mVelocityStage3;
    private float mVelocityStage4;
    private VibratorEx mVibratorEx;
    private ViewEx mViewEx;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, context.getResources().getIdentifier("numberPickerStyle", "attr", "android"));
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSelectorOffset = 0;
        this.mSelectorTextSize = 0.0f;
        this.mNormalTextSize = 0.0f;
        this.mSelectorTextColor = 0;
        this.mGradientHeight = 0;
        this.mSmallTextColor = 0;
        this.mNormalTextColor = 0;
        this.mIsDarkHwTheme = false;
        this.mFireList = new ArrayList(10);
        this.mIsVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
        this.mFlingDirection = 2;
        this.mIsLongPress = false;
        this.mSoundPool = null;
        this.mSoundId = 0;
        this.mIsSoundLoadFinished = false;
        this.mVibratorEx = new VibratorEx();
        this.mViewEx = new ViewEx();
        this.mIsSupportVibratorOld = false;
        this.mIsSupportVibrator = false;
        initClass();
        List<Integer> flingInfoList = getCurrentStatus();
        if (flingInfoList.size() > 0) {
            this.mMaximumFlingVelocity = flingInfoList.get(0).intValue();
        }
        int i = this.mMaximumFlingVelocity;
        this.mVelocityStage1 = ((float) i) * VELOCITY_STAGE_1;
        this.mVelocityStage2 = ((float) i) * VELOCITY_STAGE_2;
        this.mVelocityStage3 = ((float) i) * VELOCITY_STAGE_3;
        this.mVelocityStage4 = ((float) i) * VELOCITY_STAGE_4;
        this.mIsSupportVibratorOld = this.mVibratorEx.isSupportHwVibrator("haptic.control.time_scroll");
        this.mIsSupportVibrator = this.mVibratorEx.isSupportHwVibrator(HW_VIBRATOR_SLIDE_TYPE1);
        initIncrementAndDecrementButton(createOnClickListener(), createOnLongClickListener());
        initialNumberPicker(context, attrs);
        getSelectorWheelPaint().setColor(this.mNormalTextColor);
        this.mContextVibrate = context;
        this.mSelectorWheelItemCount = 5;
        setSelectMiddleItemIdex(this.mSelectorWheelItemCount / 2);
        setSelectorIndices(new int[this.mSelectorWheelItemCount]);
        this.mDefaultTypeface = Typeface.create((String) null, 0);
        this.mHwChineseMediumTypeface = Typeface.create(HW_CHINESE_MEDIUM_TYPEFACE, 0);
        Resources res = context.getResources();
        this.mEdgeOffset = res.getDimensionPixelSize(34472050);
        this.mEdgeOffsetTop = res.getDimensionPixelSize(34472804);
        this.mInternalOffsetAbove = res.getDimensionPixelSize(34472806);
        this.mInternalOffsetBelow = res.getDimensionPixelSize(34472807);
        getInputText().setTypeface(this.mHwChineseMediumTypeface);
        getInputText().setTextColor(this.mSelectorTextColor);
        try {
            sClassLoader = new DexClassLoader(APK_PATH, DEX_OUTPUT_DIR, null, ClassLoader.getSystemClassLoader());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "fail get sClassLoader");
        }
    }

    private void initIncrementAndDecrementButton(View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        Object hasSelectorWheelTemp = ReflectUtil.getObject(this, "mHasSelectorWheel", this.mGoogleNumberPickerClass);
        if (hasSelectorWheelTemp instanceof Boolean) {
            boolean hasSelectorWheel = ((Boolean) hasSelectorWheelTemp).booleanValue();
            Object incrementButtonTemp = ReflectUtil.getObject(this, "mIncrementButton", this.mGoogleNumberPickerClass);
            Object decrementButtonTemp = ReflectUtil.getObject(this, "mDecrementButton", this.mGoogleNumberPickerClass);
            if ((incrementButtonTemp instanceof ImageButton) && (decrementButtonTemp instanceof ImageButton) && !hasSelectorWheel) {
                ImageButton incrementButton = (ImageButton) incrementButtonTemp;
                incrementButton.setOnClickListener(onClickListener);
                incrementButton.setOnLongClickListener(onLongClickListener);
                ImageButton decrementButton = (ImageButton) decrementButtonTemp;
                decrementButton.setOnClickListener(onClickListener);
                decrementButton.setOnLongClickListener(onLongClickListener);
            }
        }
    }

    private View.OnClickListener createOnClickListener() {
        return new View.OnClickListener() {
            /* class huawei.android.widget.NumberPicker.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (view != null) {
                    ReflectUtil.callMethod(this, "hideSoftInput", null, null, NumberPicker.this.mGoogleNumberPickerClass);
                    Object inputTextTemp = ReflectUtil.getObject(this, "mInputText", NumberPicker.this.mGoogleNumberPickerClass);
                    if (inputTextTemp instanceof EditText) {
                        ((EditText) inputTextTemp).clearFocus();
                        Class<?>[] changeValueByOneArgsClasses = {Boolean.TYPE};
                        Object[] changeValueByOneObjects = {false};
                        if (view.getId() == 16909054) {
                            changeValueByOneObjects[0] = true;
                        } else {
                            changeValueByOneObjects[0] = false;
                        }
                        ReflectUtil.callMethod(this, "changeValueByOne", changeValueByOneArgsClasses, changeValueByOneObjects, NumberPicker.this.mGoogleNumberPickerClass);
                        NumberPicker.this.setLongPressState(false);
                    }
                }
            }
        };
    }

    private View.OnLongClickListener createOnLongClickListener() {
        return new View.OnLongClickListener() {
            /* class huawei.android.widget.NumberPicker.AnonymousClass2 */

            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (view == null) {
                    return false;
                }
                NumberPicker.this.setLongPressState(true);
                ReflectUtil.callMethod(this, "hideSoftInput", null, null, NumberPicker.this.mGoogleNumberPickerClass);
                Object inputTextTemp = ReflectUtil.getObject(this, "mInputText", NumberPicker.this.mGoogleNumberPickerClass);
                if (inputTextTemp instanceof EditText) {
                    ((EditText) inputTextTemp).clearFocus();
                    Class<?>[] postChangeCurrentByOneFromLongPressArgsClasses = {Boolean.TYPE, Long.TYPE};
                    Object[] postChangeCurrentByOneFromLongPressObjects = {false, 0};
                    if (view.getId() == 16909054) {
                        postChangeCurrentByOneFromLongPressObjects[0] = true;
                    } else {
                        postChangeCurrentByOneFromLongPressObjects[0] = false;
                    }
                    ReflectUtil.callMethod(this, "postChangeCurrentByOneFromLongPress", postChangeCurrentByOneFromLongPressArgsClasses, postChangeCurrentByOneFromLongPressObjects, NumberPicker.this.mGoogleNumberPickerClass);
                }
                return true;
            }
        };
    }

    @Override // android.widget.NumberPicker, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!getHasSelectorWheel() || !isEnabled()) {
            return false;
        }
        if (event.getActionMasked() == 0) {
            handleFireList();
        }
        return super.onInterceptTouchEvent(event);
    }

    /* access modifiers changed from: protected */
    public void initializeFadingEdgesEx() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(this.mGradientHeight);
    }

    /* access modifiers changed from: protected */
    public int getNormalTextColor(int color) {
        return this.mNormalTextColor;
    }

    /* access modifiers changed from: protected */
    public void setSelectorColor(int selectorIndecesIndex, int currentOffset, int initOffset, int index, int height, Paint paint) {
        int offset = currentOffset + ((selectorIndecesIndex - index) * height);
        int i = this.mSelectorOffset;
        if (offset <= initOffset - i || offset >= i + initOffset) {
            paint.setTextSize(this.mNormalTextSize);
            paint.setColor(this.mSmallTextColor);
            paint.setTypeface(this.mDefaultTypeface);
            return;
        }
        paint.setTextSize(this.mSelectorTextSize);
        paint.setColor(this.mSelectorTextColor);
        paint.setTypeface(this.mHwChineseMediumTypeface);
    }

    /* access modifiers changed from: protected */
    public float adjustYPosition(int index, float offsetY) {
        if (index == getSelectorMiddleItemIdex()) {
            return offsetY - (this.mSelectorTextSize - this.mNormalTextSize);
        }
        return offsetY;
    }

    public void addFireList(android.widget.NumberPicker numberPicker) {
        this.mFireList.add(numberPicker);
    }

    /* access modifiers changed from: protected */
    public int initializeSelectorElementHeight(int textSize, int selectorTextGapHeight) {
        return ((textSize * 5) / 3) + selectorTextGapHeight;
    }

    private void initialNumberPicker(Context context, AttributeSet attrs) {
        Resources res = context.getResources();
        this.mSelectorOffset = res.getDimensionPixelSize(34472047);
        this.mSelectorTextSize = (float) res.getDimensionPixelSize(34472048);
        this.mNormalTextSize = (float) res.getDimensionPixelSize(34472049);
        this.mGradientHeight = res.getDimensionPixelSize(34472805);
        this.mIsDarkHwTheme = HwWidgetFactory.isHwDarkTheme(context);
        if (this.mIsDarkHwTheme) {
            this.mSelectorTextColor = EmuiUtils.getAttrColor(context, 16842806, DEFAULT_NORMAL_TEXT_COLOR);
        } else {
            this.mSelectorTextColor = EmuiUtils.getAttrColor(context, 33620227, DEFAULT_SELECTE_TEXT_COLOR);
        }
        this.mSmallTextColor = EmuiUtils.getAttrColor(context, 16842806, DEFAULT_NORMAL_TEXT_COLOR);
        this.mNormalTextColor = EmuiUtils.getAttrColor(context, 16842806, DEFAULT_NORMAL_TEXT_COLOR);
    }

    private void handleFireList() {
        getInputText().setTextSize(0, this.mSelectorTextSize);
        int size = this.mFireList.size();
        for (int i = 0; i < size; i++) {
            android.widget.NumberPicker numberPicker = this.mFireList.get(i);
            numberPicker.getInputText().setVisibility(0);
            numberPicker.invalidate();
        }
    }

    private int getAlphaGradient(int initOffset, int offset, int color) {
        float rate = 0.0f;
        if (this.mSelectorOffset != 0) {
            rate = 1.0f - (((float) Math.abs(initOffset - offset)) / ((float) this.mSelectorOffset));
        }
        if (rate < ALPHA_GRADIENT_BOUND) {
            rate = ALPHA_GRADIENT_BOUND;
        }
        return (COLOR_OPACITY_MASK & color) | (((int) (((float) Color.alpha(color)) * rate)) << TRANSPARENCY_LEFT_SHIFT);
    }

    /* access modifiers changed from: protected */
    public void playIvtEffect() {
        if (this.mIsVibrateImplemented && Settings.System.getInt(this.mContextVibrate.getContentResolver(), "touch_vibrate_mode", 1) == 1) {
            if (Binder.getCallingPid() == Process.myPid() || this.mContextVibrate.checkCallingPermission("android.permission.VIBRATE") == 0) {
                try {
                    Class<?> clazzVibetonzImpl = sClassLoader.loadClass("com.immersion.VibetonzImpl");
                    clazzVibetonzImpl.getMethod("playIvtEffect", String.class).invoke(clazzVibetonzImpl.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]), "NUMBERPICKER_ITEMSCROLL");
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ClassNotFoundException in reflect playIvtEffect in set object");
                } catch (NoSuchMethodException e2) {
                    Log.e(TAG, "no field in reflect playIvtEffect in set object");
                } catch (IllegalAccessException e3) {
                    Log.e(TAG, "IllegalAccessException in reflect playIvtEffect in set object");
                } catch (IllegalArgumentException e4) {
                    Log.e(TAG, "IllegalArgumentException in reflect playIvtEffect in set object");
                } catch (InvocationTargetException e5) {
                    Log.e(TAG, "InvocationTargetException in reflect playIvtEffect in set object");
                } catch (Exception e6) {
                    Log.e(TAG, "Exception in reflect playIvtEffect in set object");
                }
            } else {
                Log.e(TAG, "playIvtEffect Method requires android.Manifest.permission.VIBRATE permission");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLongPressState(boolean isLongPress) {
        this.mIsLongPress = isLongPress;
    }

    private List<Integer> getCurrentStatus() {
        return super.getFlingState();
    }

    /* access modifiers changed from: protected */
    public boolean needToPlayIvtEffectWhenScrolling(int scrollByY) {
        int scrollState = ((Integer) ReflectUtil.getObject(this, "mScrollState", this.mGoogleNumberPickerClass)).intValue();
        if (this.mIsLongPress || scrollState != 1 || Math.abs(scrollByY) <= 10) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void playIvtEffectWhenFling(int previous, int current) {
        Object scrollStateTemp = ReflectUtil.getObject(this, "mScrollState", this.mGoogleNumberPickerClass);
        if (scrollStateTemp instanceof Integer) {
            int scrollState = ((Integer) scrollStateTemp).intValue();
            if (!this.mIsLongPress && scrollState == 2) {
                int i = this.mFlingDirection;
                if (i == 0) {
                    if (current > previous) {
                        playIvtEffect();
                    } else {
                        this.mFlingDirection = 2;
                    }
                } else if (i != 1) {
                } else {
                    if (current < previous) {
                        playIvtEffect();
                    } else {
                        this.mFlingDirection = 2;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setFlingDirection(int velocityY) {
        if (velocityY > 0) {
            this.mFlingDirection = 1;
        } else {
            this.mFlingDirection = 0;
        }
    }

    /* access modifiers changed from: protected */
    public float adjustYCoordinate(int index, float offsetY) {
        int[] selectorIndices = getSelectorIndices();
        if (index == 0) {
            return ((float) this.mEdgeOffsetTop) + offsetY;
        }
        if (selectorIndices.length - 1 == index) {
            return offsetY - ((float) this.mEdgeOffset);
        }
        if (getSelectorMiddleItemIdex() - 1 == index) {
            return offsetY - ((float) this.mInternalOffsetAbove);
        }
        if (getSelectorMiddleItemIdex() + 1 == index) {
            return ((float) this.mInternalOffsetBelow) + offsetY;
        }
        return offsetY;
    }

    private void initClass() {
        if (this.mGoogleNumberPickerClass == null) {
            try {
                this.mGoogleNumberPickerClass = Class.forName(GOOGLE_NP_CLASSNAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mGoogleNumberPickerClass not found");
            }
        }
    }

    @Override // android.widget.NumberPicker
    public void setFormatter(NumberPicker.Formatter formatter) {
        super.setFormatter(formatter);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initSoundPool(this.mContextVibrate);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.NumberPicker, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseSoundPool();
    }

    private void initSoundPool(Context context) {
        AudioAttributes.Builder audioAttributesBuilder = new AudioAttributes.Builder();
        audioAttributesBuilder.setUsage(13);
        AudioAttributes attributes = audioAttributesBuilder.build();
        SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
        soundPoolBuilder.setAudioAttributes(attributes);
        this.mSoundPool = soundPoolBuilder.build();
        this.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            /* class huawei.android.widget.NumberPicker.AnonymousClass3 */

            @Override // android.media.SoundPool.OnLoadCompleteListener
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    NumberPicker.this.mIsSoundLoadFinished = true;
                }
            }
        });
        ResLoader resLoader = ResLoader.getInstance();
        Resources res = resLoader.getResources(context);
        int resId = resLoader.getIdentifier(context, "raw", "time_picker");
        if (resId != 0) {
            try {
                AssetFileDescriptor assetFileDescriptor = res.openRawResourceFd(resId);
                if (assetFileDescriptor != null) {
                    this.mSoundId = this.mSoundPool.load(assetFileDescriptor, 1);
                    try {
                        assetFileDescriptor.close();
                    } catch (IOException e) {
                        Log.e(TAG, "AssetFileDescriptor close error");
                    }
                }
            } catch (Resources.NotFoundException e2) {
                Log.w(TAG, "Resource not found");
            }
        } else {
            Log.e(TAG, "Can't find resource id for time_picker.");
        }
    }

    private void releaseSoundPool() {
        SoundPool soundPool = this.mSoundPool;
        if (soundPool != null) {
            soundPool.release();
            this.mSoundPool = null;
            this.mSoundId = 0;
            this.mIsSoundLoadFinished = false;
        }
    }

    /* access modifiers changed from: protected */
    public void playSound() {
        int i;
        if (this.mIsSupportVibratorOld && !this.mIsSupportVibrator) {
            this.mVibratorEx.setHwVibrator("haptic.control.time_scroll");
        }
        if (this.mIsSupportVibrator) {
            changeCurrent();
        }
        SoundPool soundPool = this.mSoundPool;
        if (soundPool == null || (i = this.mSoundId) == 0 || !this.mIsSoundLoadFinished) {
            Log.w(TAG, "SoundPool is not initialized properly!");
        } else {
            soundPool.play(i, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void changeCurrent() {
        List<Integer> flingInfoList = getCurrentStatus();
        if (flingInfoList.size() > 3) {
            this.mFlingState = flingInfoList.get(3).intValue();
            this.mRealTimeFlingVelocity = flingInfoList.get(2).intValue();
            this.mRealTimeVelocity = flingInfoList.get(1).intValue();
        }
        if (isHapticFeedbackEnabled()) {
            if (this.mFlingState == 0) {
                scrollVibratorSelector(this.mRealTimeVelocity);
            }
            if (this.mFlingState == 1) {
                flingVibratorSelector(this.mRealTimeFlingVelocity);
            }
        }
    }

    private void scrollVibratorSelector(int realTimeVelocity) {
        int currentVelocity = Math.abs(realTimeVelocity);
        float f = this.mVelocityStage1;
        if (((float) currentVelocity) < f) {
            ViewEx.performHwHapticFeedback(this, 5, 0);
        } else if (((float) currentVelocity) >= f && ((float) currentVelocity) < this.mVelocityStage2) {
            ViewEx.performHwHapticFeedback(this, 6, 0);
        } else if (((float) currentVelocity) >= this.mVelocityStage2 && ((float) currentVelocity) < this.mVelocityStage3) {
            ViewEx.performHwHapticFeedback(this, 7, 0);
        } else if (((float) currentVelocity) >= this.mVelocityStage3 && ((float) currentVelocity) < this.mVelocityStage4) {
            ViewEx.performHwHapticFeedback(this, 8, 0);
        } else if (((float) currentVelocity) >= this.mVelocityStage4) {
            ViewEx.performHwHapticFeedback(this, 9, 0);
        }
    }

    private void flingVibratorSelector(int realTimeFlingVelocity) {
        float f = this.mVelocityStage1;
        if (((float) realTimeFlingVelocity) < f) {
            ViewEx.performHwHapticFeedback(this, 5, 0);
        } else if (((float) realTimeFlingVelocity) >= f && ((float) realTimeFlingVelocity) < this.mVelocityStage2) {
            ViewEx.performHwHapticFeedback(this, 6, 0);
        } else if (((float) realTimeFlingVelocity) >= this.mVelocityStage2 && ((float) realTimeFlingVelocity) < this.mVelocityStage3) {
            ViewEx.performHwHapticFeedback(this, 7, 0);
        } else if (((float) realTimeFlingVelocity) >= this.mVelocityStage3 && ((float) realTimeFlingVelocity) < this.mVelocityStage4) {
            ViewEx.performHwHapticFeedback(this, 8, 0);
        } else if (((float) realTimeFlingVelocity) >= this.mVelocityStage4) {
            ViewEx.performHwHapticFeedback(this, 9, 0);
        }
    }
}
