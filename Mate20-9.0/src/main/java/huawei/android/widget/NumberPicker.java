package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioAttributes;
import android.media.SoundPool;
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
import dalvik.system.DexClassLoader;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.utils.EmuiUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class NumberPicker extends android.widget.NumberPicker {
    private static final int DEFAULT_NORMAL_TEXT_COLOR = -15132391;
    private static final int DEFAULT_SELECTE_TEXT_COLOR = -16744961;
    private static final String GOOGLE_NP_CLASSNAME = "android.widget.NumberPicker";
    private static final String HW_CHINESE_MEDIUM_TYPEFACE = "HwChinese-medium";
    private static final float SOUND_LEFT_VOLUME = 1.0f;
    private static final int SOUND_LOOP = 0;
    private static final int SOUND_PRIORITY = 0;
    private static final float SOUND_RATE = 1.0f;
    private static final float SOUND_RIGHT_VOLUME = 1.0f;
    private static final String TAG = "NumberPicker";
    private static String apkPath = "/system/framework/immersion.jar";
    private static String dexOutputDir = "/data/data/com.immersion/";
    private static DexClassLoader mClassLoader = null;
    private int FLING_BACKWARD;
    private int FLING_FOWARD;
    private int FLING_STOP;
    private boolean isVibrateImplemented;
    private Context mContext_Vibrate;
    private final Typeface mDefaultTypeface;
    private final int mEdgeOffset;
    private final int mEdgeOffsetTop;
    private List<android.widget.NumberPicker> mFireList;
    private int mFlingDirection;
    /* access modifiers changed from: private */
    public Class mGNumberPickerClass;
    private int mGradientHeight;
    private final Typeface mHwChineseMediumTypeface;
    private final int mInternalOffsetAbove;
    private final int mInternalOffsetBelow;
    private boolean mIsDarkHwTheme;
    private boolean mIsLongPress;
    private boolean mIsSupportVibrator;
    private int mNormalTextColor;
    private float mNormalTextSize;
    private int mSelectorOffset;
    private int mSelectorTextColor;
    private float mSelectorTextSize;
    private int mSmallTextColor;
    private int mSoundId;
    /* access modifiers changed from: private */
    public boolean mSoundLoadFinished;
    private SoundPool mSoundPool;
    private VibratorEx mVibratorEx;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, context.getResources().getIdentifier("numberPickerStyle", "attr", "android"));
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
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
        this.mFireList = new ArrayList();
        this.isVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
        this.FLING_FOWARD = 0;
        this.FLING_BACKWARD = 1;
        this.FLING_STOP = 2;
        this.mFlingDirection = this.FLING_STOP;
        this.mIsLongPress = false;
        this.mSoundPool = null;
        this.mSoundId = 0;
        this.mSoundLoadFinished = false;
        this.mVibratorEx = new VibratorEx();
        this.mIsSupportVibrator = false;
        initClass();
        this.mIsSupportVibrator = this.mVibratorEx.isSupportHwVibrator("haptic.control.time_scroll");
        Log.d(TAG, "Support HwVibrator type HW_VIBRATOR_TPYE_CONTROL_TIME_SCROLL: " + this.mIsSupportVibrator);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                ReflectUtil.callMethod(this, "hideSoftInput", null, null, NumberPicker.this.mGNumberPickerClass);
                EditText inputText = (EditText) ReflectUtil.getObject(this, "mInputText", NumberPicker.this.mGNumberPickerClass);
                if (inputText != null) {
                    inputText.clearFocus();
                    Class<?>[] changeValueByOneArgsClass = {Boolean.TYPE};
                    if (v.getId() == 16908995) {
                        0[0] = true;
                    } else {
                        0[0] = false;
                    }
                    ReflectUtil.callMethod(this, "changeValueByOne", changeValueByOneArgsClass, null, NumberPicker.this.mGNumberPickerClass);
                    NumberPicker.this.setLongPressState(false);
                }
            }
        };
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                NumberPicker.this.setLongPressState(true);
                ReflectUtil.callMethod(this, "hideSoftInput", null, null, NumberPicker.this.mGNumberPickerClass);
                EditText inputText = (EditText) ReflectUtil.getObject(this, "mInputText", NumberPicker.this.mGNumberPickerClass);
                if (inputText != null) {
                    inputText.clearFocus();
                    Class<?>[] postChangeCurrentByOneFromLongPressArgsClass = {Boolean.TYPE, Long.TYPE};
                    if (v.getId() == 16908995) {
                        0[0] = true;
                    } else {
                        0[0] = false;
                    }
                    0[1] = 0;
                    ReflectUtil.callMethod(this, "postChangeCurrentByOneFromLongPress", postChangeCurrentByOneFromLongPressArgsClass, null, NumberPicker.this.mGNumberPickerClass);
                }
                return true;
            }
        };
        boolean hasSelectorWheel = ((Boolean) ReflectUtil.getObject(this, "mHasSelectorWheel", this.mGNumberPickerClass)).booleanValue();
        ImageButton incrementButton = (ImageButton) ReflectUtil.getObject(this, "mIncrementButton", this.mGNumberPickerClass);
        ImageButton decrementButton = (ImageButton) ReflectUtil.getObject(this, "mDecrementButton", this.mGNumberPickerClass);
        if (!(incrementButton == null || decrementButton == null || hasSelectorWheel)) {
            incrementButton.setOnClickListener(onClickListener);
            incrementButton.setOnLongClickListener(onLongClickListener);
            decrementButton.setOnClickListener(onClickListener);
            decrementButton.setOnLongClickListener(onLongClickListener);
        }
        initialNumberPicker(context, attrs);
        getSelectorWheelPaint().setColor(this.mNormalTextColor);
        Context context2 = context;
        this.mContext_Vibrate = context2;
        this.mSelectorWheelItemCount = 5;
        setSelectMiddleItemIdex(this.mSelectorWheelItemCount / 2);
        setSelectorIndices(new int[this.mSelectorWheelItemCount]);
        this.mDefaultTypeface = Typeface.create(null, 0);
        this.mHwChineseMediumTypeface = Typeface.create(HW_CHINESE_MEDIUM_TYPEFACE, 0);
        Resources res = context2.getResources();
        this.mEdgeOffset = res.getDimensionPixelSize(34472050);
        this.mEdgeOffsetTop = res.getDimensionPixelSize(34472477);
        this.mInternalOffsetAbove = res.getDimensionPixelSize(34472479);
        this.mInternalOffsetBelow = res.getDimensionPixelSize(34472480);
        getInputText().setTypeface(this.mHwChineseMediumTypeface);
        getInputText().setTextColor(this.mSelectorTextColor);
        try {
            mClassLoader = new DexClassLoader(apkPath, dexOutputDir, null, ClassLoader.getSystemClassLoader());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "fail get mClassLoader");
        }
    }

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
    public void setSelectorColor(int i, int currentOffset, int initOffset, int index, int height, Paint paint) {
        int offset = currentOffset + ((i - index) * height);
        if (offset <= initOffset - this.mSelectorOffset || offset >= this.mSelectorOffset + initOffset) {
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
    public float adjustYPosition(int i, float y) {
        float ret = y;
        if (i == getSelectorMiddleItemIdex()) {
            return ret - (this.mSelectorTextSize - this.mNormalTextSize);
        }
        return ret;
    }

    public void addFireList(android.widget.NumberPicker np) {
        this.mFireList.add(np);
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
        this.mGradientHeight = res.getDimensionPixelSize(34472478);
        this.mIsDarkHwTheme = HwWidgetFactory.isHwDarkTheme(context);
        if (this.mIsDarkHwTheme) {
            this.mSelectorTextColor = EmuiUtils.getAttrColor(context, 16842806, DEFAULT_NORMAL_TEXT_COLOR);
        } else {
            this.mSelectorTextColor = EmuiUtils.getAttrColor(context, 16843829, DEFAULT_SELECTE_TEXT_COLOR);
        }
        this.mSmallTextColor = EmuiUtils.getAttrColor(context, 16842806, DEFAULT_NORMAL_TEXT_COLOR);
        this.mNormalTextColor = EmuiUtils.getAttrColor(context, 16842806, DEFAULT_NORMAL_TEXT_COLOR);
    }

    private void handleFireList() {
        getInputText().setTextSize(0, this.mSelectorTextSize);
        int size = this.mFireList.size();
        for (int i = 0; i < size; i++) {
            android.widget.NumberPicker np = this.mFireList.get(i);
            np.getInputText().setVisibility(0);
            np.invalidate();
        }
    }

    private int getAlphaGradient(int initOffset, int offset, int color) {
        float rate = 1.0f - (((float) Math.abs(initOffset - offset)) / ((float) this.mSelectorOffset));
        if (rate < 0.4f) {
            rate = 0.4f;
        }
        return (16777215 & color) | (((int) (((float) Color.alpha(color)) * rate)) << 24);
    }

    /* access modifiers changed from: protected */
    public void playIvtEffect() {
        if (this.isVibrateImplemented && 1 == Settings.System.getInt(this.mContext_Vibrate.getContentResolver(), "touch_vibrate_mode", 1)) {
            if (this.mContext_Vibrate.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
                Log.e(TAG, "playIvtEffect Method requires android.Manifest.permission.VIBRATE permission");
                return;
            }
            try {
                Class<?> mClazz_vibetonzImpl = mClassLoader.loadClass("com.immersion.VibetonzImpl");
                Object object_vibetonzImpl = mClazz_vibetonzImpl.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                mClazz_vibetonzImpl.getMethod("playIvtEffect", new Class[]{String.class}).invoke(object_vibetonzImpl, new Object[]{"NUMBERPICKER_ITEMSCROLL"});
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "ClassNotFoundException in reflect playIvtEffect in set object");
            } catch (NoSuchMethodException e2) {
                Log.e(TAG, "no field in reflect playIvtEffect in set object");
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (IllegalArgumentException e4) {
                Log.e(TAG, "IllegalArgumentException in reflect playIvtEffect in set object");
            } catch (InvocationTargetException e5) {
                Log.e(TAG, "InvocationTargetException in reflect playIvtEffect in set object");
            } catch (RuntimeException e6) {
                Log.e(TAG, "RuntimeException in reflect playIvtEffect in set object");
            } catch (Exception e7) {
                Log.e(TAG, "Exception in reflect playIvtEffect in set object");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setLongPressState(boolean state) {
        this.mIsLongPress = state;
    }

    /* access modifiers changed from: protected */
    public boolean needToPlayIvtEffectWhenScrolling(int scrollByY) {
        int mScrollState = ((Integer) ReflectUtil.getObject(this, "mScrollState", this.mGNumberPickerClass)).intValue();
        if (this.mIsLongPress || mScrollState != 1 || Math.abs(scrollByY) <= 10) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void playIvtEffectWhenFling(int previous, int current) {
        int mScrollState = ((Integer) ReflectUtil.getObject(this, "mScrollState", this.mGNumberPickerClass)).intValue();
        if (!this.mIsLongPress && mScrollState == 2) {
            if (this.mFlingDirection == this.FLING_FOWARD) {
                if (current > previous) {
                    playIvtEffect();
                } else {
                    this.mFlingDirection = this.FLING_STOP;
                }
            } else if (this.mFlingDirection != this.FLING_BACKWARD) {
            } else {
                if (current < previous) {
                    playIvtEffect();
                } else {
                    this.mFlingDirection = this.FLING_STOP;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setFlingDirection(int velocityY) {
        if (velocityY > 0) {
            this.mFlingDirection = this.FLING_BACKWARD;
        } else {
            this.mFlingDirection = this.FLING_FOWARD;
        }
    }

    /* access modifiers changed from: protected */
    public float adjustYCoordinate(int i, float y) {
        int[] selectorIndices = getSelectorIndices();
        if (i == 0) {
            return y + ((float) this.mEdgeOffsetTop);
        }
        if (selectorIndices.length - 1 == i) {
            return y - ((float) this.mEdgeOffset);
        }
        if (getSelectorMiddleItemIdex() - 1 == i) {
            return y - ((float) this.mInternalOffsetAbove);
        }
        if (getSelectorMiddleItemIdex() + 1 == i) {
            return y + ((float) this.mInternalOffsetBelow);
        }
        return y;
    }

    private void initClass() {
        if (this.mGNumberPickerClass == null) {
            try {
                this.mGNumberPickerClass = Class.forName(GOOGLE_NP_CLASSNAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mGNumberPickerClass not found");
            }
        }
    }

    public void setFormatter(NumberPicker.Formatter formatter) {
        super.setFormatter(formatter);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initSoundPool(this.mContext_Vibrate);
    }

    /* access modifiers changed from: protected */
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
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    boolean unused = NumberPicker.this.mSoundLoadFinished = true;
                }
            }
        });
        ResLoader resLoader = ResLoader.getInstance();
        Resources res = resLoader.getResources(context);
        int resId = resLoader.getIdentifier(context, "raw", "time_picker");
        if (resId != 0) {
            try {
                this.mSoundId = this.mSoundPool.load(res.openRawResourceFd(resId), 1);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "Resource not found");
            }
        } else {
            Log.e(TAG, "Can't find resource id for time_picker.");
        }
    }

    private void releaseSoundPool() {
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
            this.mSoundId = 0;
            this.mSoundLoadFinished = false;
        }
    }

    /* access modifiers changed from: protected */
    public void playSound() {
        if (this.mIsSupportVibrator) {
            this.mVibratorEx.setHwVibrator("haptic.control.time_scroll");
        }
        if (this.mSoundPool == null || this.mSoundId == 0 || !this.mSoundLoadFinished) {
            Log.w(TAG, "SoundPool is not initialized properly!");
        } else {
            this.mSoundPool.play(this.mSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }
}
