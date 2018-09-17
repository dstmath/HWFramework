package android.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.IntArray;
import android.util.Log;
import android.util.MathUtils;
import android.util.Property;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.widget.ExploreByTouchHelper;
import com.huawei.hwperformance.HwPerformance;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Locale;

public class RadialTimePickerView extends View {
    private static final int AM = 0;
    private static final int ANIM_DURATION_NORMAL = 500;
    private static final int ANIM_DURATION_TOUCH = 60;
    private static final float[] COS_30 = null;
    private static final int DEGREES_FOR_ONE_HOUR = 30;
    private static final int DEGREES_FOR_ONE_MINUTE = 6;
    public static final int HOURS = 0;
    private static final int HOURS_INNER = 2;
    private static final int HOURS_IN_CIRCLE = 12;
    private static final int[] HOURS_NUMBERS = null;
    private static final int[] HOURS_NUMBERS_24 = null;
    public static final int MINUTES = 1;
    private static final int MINUTES_IN_CIRCLE = 60;
    private static final int[] MINUTES_NUMBERS = null;
    private static final int MISSING_COLOR = -65281;
    private static final int NUM_POSITIONS = 12;
    private static final int PM = 1;
    private static final int SELECTOR_CIRCLE = 0;
    private static final int SELECTOR_DOT = 1;
    private static final int SELECTOR_LINE = 2;
    private static final float[] SIN_30 = null;
    private static final int[] SNAP_PREFER_30S_MAP = null;
    private static final String TAG = "RadialTimePickerView";
    private final FloatProperty<RadialTimePickerView> HOURS_TO_MINUTES;
    private int mAmOrPm;
    private int mCenterDotRadius;
    boolean mChangedDuringTouch;
    private int mCircleRadius;
    private float mDisabledAlpha;
    private int mHalfwayDist;
    private final String[] mHours12Texts;
    private float mHoursToMinutes;
    private ObjectAnimator mHoursToMinutesAnimator;
    private final String[] mInnerHours24Texts;
    private String[] mInnerTextHours;
    private final float[] mInnerTextX;
    private final float[] mInnerTextY;
    private boolean mInputEnabled;
    private boolean mIs24HourMode;
    private boolean mIsOnInnerCircle;
    private OnValueSelectedListener mListener;
    private int mMaxDistForOuterNumber;
    private int mMinDistForInnerNumber;
    private String[] mMinutesText;
    private final String[] mMinutesTexts;
    private final String[] mOuterHours24Texts;
    private String[] mOuterTextHours;
    private final float[][] mOuterTextX;
    private final float[][] mOuterTextY;
    private final Paint[] mPaint;
    private final Paint mPaintBackground;
    private final Paint mPaintCenter;
    private final Paint[] mPaintSelector;
    private final int[] mSelectionDegrees;
    private int mSelectorColor;
    private int mSelectorDotColor;
    private int mSelectorDotRadius;
    private final Path mSelectorPath;
    private int mSelectorRadius;
    private int mSelectorStroke;
    private boolean mShowHours;
    private final ColorStateList[] mTextColor;
    private final int[] mTextInset;
    private final int[] mTextSize;
    private final RadialPickerTouchHelper mTouchHelper;
    private final Typeface mTypeface;
    private int mXCenter;
    private int mYCenter;

    /* renamed from: android.widget.RadialTimePickerView.1 */
    class AnonymousClass1 extends FloatProperty<RadialTimePickerView> {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public Float get(RadialTimePickerView radialTimePickerView) {
            return Float.valueOf(radialTimePickerView.mHoursToMinutes);
        }

        public void setValue(RadialTimePickerView object, float value) {
            object.mHoursToMinutes = value;
            object.invalidate();
        }
    }

    public interface OnValueSelectedListener {
        void onValueSelected(int i, int i2, boolean z);
    }

    private class RadialPickerTouchHelper extends ExploreByTouchHelper {
        private final int MASK_TYPE;
        private final int MASK_VALUE;
        private final int MINUTE_INCREMENT;
        private final int SHIFT_TYPE;
        private final int SHIFT_VALUE;
        private final int TYPE_HOUR;
        private final int TYPE_MINUTE;
        private final Rect mTempRect;

        public RadialPickerTouchHelper() {
            super(RadialTimePickerView.this);
            this.mTempRect = new Rect();
            this.TYPE_HOUR = RadialTimePickerView.SELECTOR_DOT;
            this.TYPE_MINUTE = RadialTimePickerView.SELECTOR_LINE;
            this.SHIFT_TYPE = RadialTimePickerView.SELECTOR_CIRCLE;
            this.MASK_TYPE = 15;
            this.SHIFT_VALUE = 8;
            this.MASK_VALUE = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            this.MINUTE_INCREMENT = 5;
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
        }

        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            }
            switch (action) {
                case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
                    adjustPicker(RadialTimePickerView.SELECTOR_DOT);
                    return true;
                case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
                    adjustPicker(-1);
                    return true;
                default:
                    return false;
            }
        }

        private void adjustPicker(int step) {
            int stepSize;
            int initialStep;
            int minValue;
            int maxValue;
            if (RadialTimePickerView.this.mShowHours) {
                stepSize = RadialTimePickerView.SELECTOR_DOT;
                int currentHour24 = RadialTimePickerView.this.getCurrentHour();
                if (RadialTimePickerView.this.mIs24HourMode) {
                    initialStep = currentHour24;
                    minValue = RadialTimePickerView.SELECTOR_CIRCLE;
                    maxValue = 23;
                } else {
                    initialStep = hour24To12(currentHour24);
                    minValue = RadialTimePickerView.SELECTOR_DOT;
                    maxValue = RadialTimePickerView.NUM_POSITIONS;
                }
            } else {
                stepSize = 5;
                initialStep = RadialTimePickerView.this.getCurrentMinute() / 5;
                minValue = RadialTimePickerView.SELECTOR_CIRCLE;
                maxValue = 55;
            }
            int clampedValue = MathUtils.constrain((initialStep + step) * stepSize, minValue, maxValue);
            if (RadialTimePickerView.this.mShowHours) {
                RadialTimePickerView.this.setCurrentHour(clampedValue);
            } else {
                RadialTimePickerView.this.setCurrentMinute(clampedValue);
            }
        }

        protected int getVirtualViewAt(float x, float y) {
            int degrees = RadialTimePickerView.this.getDegreesFromXY(x, y, true);
            if (degrees == -1) {
                return RtlSpacingHelper.UNDEFINED;
            }
            int snapDegrees = RadialTimePickerView.snapOnly30s(degrees, RadialTimePickerView.SELECTOR_CIRCLE) % HwModemCapability.MODEM_CAP_MAX;
            if (RadialTimePickerView.this.mShowHours) {
                int hour24 = RadialTimePickerView.this.getHourForDegrees(snapDegrees, RadialTimePickerView.this.getInnerCircleFromXY(x, y));
                return makeId(RadialTimePickerView.SELECTOR_DOT, RadialTimePickerView.this.mIs24HourMode ? hour24 : hour24To12(hour24));
            }
            int minute;
            int current = RadialTimePickerView.this.getCurrentMinute();
            int touched = RadialTimePickerView.this.getMinuteForDegrees(degrees);
            int snapped = RadialTimePickerView.this.getMinuteForDegrees(snapDegrees);
            if (getCircularDiff(current, touched, RadialTimePickerView.MINUTES_IN_CIRCLE) < getCircularDiff(snapped, touched, RadialTimePickerView.MINUTES_IN_CIRCLE)) {
                minute = current;
            } else {
                minute = snapped;
            }
            return makeId(RadialTimePickerView.SELECTOR_LINE, minute);
        }

        private int getCircularDiff(int first, int second, int max) {
            int diff = Math.abs(first - second);
            return diff > max / RadialTimePickerView.SELECTOR_LINE ? max - diff : diff;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            int i;
            if (RadialTimePickerView.this.mShowHours) {
                int min = RadialTimePickerView.this.mIs24HourMode ? RadialTimePickerView.SELECTOR_CIRCLE : RadialTimePickerView.SELECTOR_DOT;
                int max = RadialTimePickerView.this.mIs24HourMode ? 23 : RadialTimePickerView.NUM_POSITIONS;
                for (i = min; i <= max; i += RadialTimePickerView.SELECTOR_DOT) {
                    virtualViewIds.add(makeId(RadialTimePickerView.SELECTOR_DOT, i));
                }
                return;
            }
            int current = RadialTimePickerView.this.getCurrentMinute();
            i = RadialTimePickerView.SELECTOR_CIRCLE;
            while (i < RadialTimePickerView.MINUTES_IN_CIRCLE) {
                virtualViewIds.add(makeId(RadialTimePickerView.SELECTOR_LINE, i));
                if (current > i && current < i + 5) {
                    virtualViewIds.add(makeId(RadialTimePickerView.SELECTOR_LINE, current));
                }
                i += 5;
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setClassName(getClass().getName());
            event.setContentDescription(getVirtualViewDescription(getTypeFromId(virtualViewId), getValueFromId(virtualViewId)));
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setClassName(getClass().getName());
            node.addAction(AccessibilityAction.ACTION_CLICK);
            int type = getTypeFromId(virtualViewId);
            int value = getValueFromId(virtualViewId);
            node.setContentDescription(getVirtualViewDescription(type, value));
            getBoundsForVirtualView(virtualViewId, this.mTempRect);
            node.setBoundsInParent(this.mTempRect);
            node.setSelected(isVirtualViewSelected(type, value));
            int nextId = getVirtualViewIdAfter(type, value);
            if (nextId != RtlSpacingHelper.UNDEFINED) {
                node.setTraversalBefore(RadialTimePickerView.this, nextId);
            }
        }

        private int getVirtualViewIdAfter(int type, int value) {
            int nextValue;
            if (type == RadialTimePickerView.SELECTOR_DOT) {
                nextValue = value + RadialTimePickerView.SELECTOR_DOT;
                if (nextValue <= (RadialTimePickerView.this.mIs24HourMode ? 23 : RadialTimePickerView.NUM_POSITIONS)) {
                    return makeId(type, nextValue);
                }
            } else if (type == RadialTimePickerView.SELECTOR_LINE) {
                int current = RadialTimePickerView.this.getCurrentMinute();
                nextValue = (value - (value % 5)) + 5;
                if (value < current && nextValue > current) {
                    return makeId(type, current);
                }
                if (nextValue < RadialTimePickerView.MINUTES_IN_CIRCLE) {
                    return makeId(type, nextValue);
                }
            }
            return RtlSpacingHelper.UNDEFINED;
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action == 16) {
                int type = getTypeFromId(virtualViewId);
                int value = getValueFromId(virtualViewId);
                if (type == RadialTimePickerView.SELECTOR_DOT) {
                    RadialTimePickerView.this.setCurrentHour(RadialTimePickerView.this.mIs24HourMode ? value : hour12To24(value, RadialTimePickerView.this.mAmOrPm));
                    return true;
                } else if (type == RadialTimePickerView.SELECTOR_LINE) {
                    RadialTimePickerView.this.setCurrentMinute(value);
                    return true;
                }
            }
            return false;
        }

        private int hour12To24(int hour12, int amOrPm) {
            int hour24 = hour12;
            if (hour12 == RadialTimePickerView.NUM_POSITIONS) {
                if (amOrPm == 0) {
                    return RadialTimePickerView.SELECTOR_CIRCLE;
                }
                return hour24;
            } else if (amOrPm == RadialTimePickerView.SELECTOR_DOT) {
                return hour12 + RadialTimePickerView.NUM_POSITIONS;
            } else {
                return hour24;
            }
        }

        private int hour24To12(int hour24) {
            if (hour24 == 0) {
                return RadialTimePickerView.NUM_POSITIONS;
            }
            if (hour24 > RadialTimePickerView.NUM_POSITIONS) {
                return hour24 - 12;
            }
            return hour24;
        }

        private void getBoundsForVirtualView(int virtualViewId, Rect bounds) {
            float centerRadius;
            float radius;
            float degrees;
            int type = getTypeFromId(virtualViewId);
            int value = getValueFromId(virtualViewId);
            if (type == RadialTimePickerView.SELECTOR_DOT) {
                if (RadialTimePickerView.this.getInnerCircleForHour(value)) {
                    centerRadius = (float) (RadialTimePickerView.this.mCircleRadius - RadialTimePickerView.this.mTextInset[RadialTimePickerView.SELECTOR_LINE]);
                    radius = (float) RadialTimePickerView.this.mSelectorRadius;
                } else {
                    centerRadius = (float) (RadialTimePickerView.this.mCircleRadius - RadialTimePickerView.this.mTextInset[RadialTimePickerView.SELECTOR_CIRCLE]);
                    radius = (float) RadialTimePickerView.this.mSelectorRadius;
                }
                degrees = (float) RadialTimePickerView.this.getDegreesForHour(value);
            } else if (type == RadialTimePickerView.SELECTOR_LINE) {
                centerRadius = (float) (RadialTimePickerView.this.mCircleRadius - RadialTimePickerView.this.mTextInset[RadialTimePickerView.SELECTOR_DOT]);
                degrees = (float) RadialTimePickerView.this.getDegreesForMinute(value);
                radius = (float) RadialTimePickerView.this.mSelectorRadius;
            } else {
                centerRadius = 0.0f;
                degrees = 0.0f;
                radius = 0.0f;
            }
            double radians = Math.toRadians((double) degrees);
            float xCenter = ((float) RadialTimePickerView.this.mXCenter) + (((float) Math.sin(radians)) * centerRadius);
            float yCenter = ((float) RadialTimePickerView.this.mYCenter) - (((float) Math.cos(radians)) * centerRadius);
            bounds.set((int) (xCenter - radius), (int) (yCenter - radius), (int) (xCenter + radius), (int) (yCenter + radius));
        }

        private CharSequence getVirtualViewDescription(int type, int value) {
            if (type == RadialTimePickerView.SELECTOR_DOT || type == RadialTimePickerView.SELECTOR_LINE) {
                return Integer.toString(value);
            }
            return null;
        }

        private boolean isVirtualViewSelected(int type, int value) {
            if (type == RadialTimePickerView.SELECTOR_DOT) {
                return RadialTimePickerView.this.getCurrentHour() == value;
            } else {
                if (type == RadialTimePickerView.SELECTOR_LINE) {
                    return RadialTimePickerView.this.getCurrentMinute() == value;
                } else {
                    return false;
                }
            }
        }

        private int makeId(int type, int value) {
            return (type << RadialTimePickerView.SELECTOR_CIRCLE) | (value << 8);
        }

        private int getTypeFromId(int id) {
            return (id >>> RadialTimePickerView.SELECTOR_CIRCLE) & 15;
        }

        private int getValueFromId(int id) {
            return (id >>> 8) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.RadialTimePickerView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.RadialTimePickerView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.RadialTimePickerView.<clinit>():void");
    }

    private static void preparePrefer30sMap() {
        int snappedOutputDegrees = SELECTOR_CIRCLE;
        int count = SELECTOR_DOT;
        int expectedCount = 8;
        for (int degrees = SELECTOR_CIRCLE; degrees < MetricsEvent.ACTION_QS_EDIT_REMOVE; degrees += SELECTOR_DOT) {
            SNAP_PREFER_30S_MAP[degrees] = snappedOutputDegrees;
            if (count == expectedCount) {
                snappedOutputDegrees += DEGREES_FOR_ONE_MINUTE;
                if (snappedOutputDegrees == HwModemCapability.MODEM_CAP_MAX) {
                    expectedCount = 7;
                } else if (snappedOutputDegrees % DEGREES_FOR_ONE_HOUR == 0) {
                    expectedCount = 14;
                } else {
                    expectedCount = 4;
                }
                count = SELECTOR_DOT;
            } else {
                count += SELECTOR_DOT;
            }
        }
    }

    private static int snapPrefer30s(int degrees) {
        if (SNAP_PREFER_30S_MAP == null) {
            return -1;
        }
        return SNAP_PREFER_30S_MAP[degrees];
    }

    private static int snapOnly30s(int degrees, int forceHigherOrLower) {
        int floor = (degrees / DEGREES_FOR_ONE_HOUR) * DEGREES_FOR_ONE_HOUR;
        int ceiling = floor + DEGREES_FOR_ONE_HOUR;
        if (forceHigherOrLower == SELECTOR_DOT) {
            return ceiling;
        }
        if (forceHigherOrLower == -1) {
            if (degrees == floor) {
                floor -= 30;
            }
            return floor;
        } else if (degrees - floor < ceiling - degrees) {
            return floor;
        } else {
            return ceiling;
        }
    }

    public RadialTimePickerView(Context context) {
        this(context, null);
    }

    public RadialTimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
    }

    public RadialTimePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, SELECTOR_CIRCLE);
    }

    public RadialTimePickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.HOURS_TO_MINUTES = new AnonymousClass1("hoursToMinutes");
        this.mHours12Texts = new String[NUM_POSITIONS];
        this.mOuterHours24Texts = new String[NUM_POSITIONS];
        this.mInnerHours24Texts = new String[NUM_POSITIONS];
        this.mMinutesTexts = new String[NUM_POSITIONS];
        this.mPaint = new Paint[SELECTOR_LINE];
        this.mPaintCenter = new Paint();
        this.mPaintSelector = new Paint[3];
        this.mPaintBackground = new Paint();
        this.mTextColor = new ColorStateList[3];
        this.mTextSize = new int[3];
        this.mTextInset = new int[3];
        this.mOuterTextX = (float[][]) Array.newInstance(Float.TYPE, new int[]{SELECTOR_LINE, NUM_POSITIONS});
        this.mOuterTextY = (float[][]) Array.newInstance(Float.TYPE, new int[]{SELECTOR_LINE, NUM_POSITIONS});
        this.mInnerTextX = new float[NUM_POSITIONS];
        this.mInnerTextY = new float[NUM_POSITIONS];
        this.mSelectionDegrees = new int[SELECTOR_LINE];
        this.mSelectorPath = new Path();
        this.mInputEnabled = true;
        this.mChangedDuringTouch = false;
        applyAttributes(attrs, defStyleAttr, defStyleRes);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.disabledAlpha, outValue, true);
        this.mDisabledAlpha = outValue.getFloat();
        this.mTypeface = Typeface.create("sans-serif", SELECTOR_CIRCLE);
        this.mPaint[SELECTOR_CIRCLE] = new Paint();
        this.mPaint[SELECTOR_CIRCLE].setAntiAlias(true);
        this.mPaint[SELECTOR_CIRCLE].setTextAlign(Align.CENTER);
        this.mPaint[SELECTOR_DOT] = new Paint();
        this.mPaint[SELECTOR_DOT].setAntiAlias(true);
        this.mPaint[SELECTOR_DOT].setTextAlign(Align.CENTER);
        this.mPaintCenter.setAntiAlias(true);
        this.mPaintSelector[SELECTOR_CIRCLE] = new Paint();
        this.mPaintSelector[SELECTOR_CIRCLE].setAntiAlias(true);
        this.mPaintSelector[SELECTOR_DOT] = new Paint();
        this.mPaintSelector[SELECTOR_DOT].setAntiAlias(true);
        this.mPaintSelector[SELECTOR_LINE] = new Paint();
        this.mPaintSelector[SELECTOR_LINE].setAntiAlias(true);
        this.mPaintSelector[SELECTOR_LINE].setStrokeWidth(2.0f);
        this.mPaintBackground.setAntiAlias(true);
        Resources res = getResources();
        this.mSelectorRadius = res.getDimensionPixelSize(R.dimen.timepicker_selector_radius);
        this.mSelectorStroke = res.getDimensionPixelSize(R.dimen.timepicker_selector_stroke);
        this.mSelectorDotRadius = res.getDimensionPixelSize(R.dimen.timepicker_selector_dot_radius);
        this.mCenterDotRadius = res.getDimensionPixelSize(R.dimen.timepicker_center_dot_radius);
        this.mTextSize[SELECTOR_CIRCLE] = res.getDimensionPixelSize(R.dimen.timepicker_text_size_normal);
        this.mTextSize[SELECTOR_DOT] = res.getDimensionPixelSize(R.dimen.timepicker_text_size_normal);
        this.mTextSize[SELECTOR_LINE] = res.getDimensionPixelSize(R.dimen.timepicker_text_size_inner);
        this.mTextInset[SELECTOR_CIRCLE] = res.getDimensionPixelSize(R.dimen.timepicker_text_inset_normal);
        this.mTextInset[SELECTOR_DOT] = res.getDimensionPixelSize(R.dimen.timepicker_text_inset_normal);
        this.mTextInset[SELECTOR_LINE] = res.getDimensionPixelSize(R.dimen.timepicker_text_inset_inner);
        this.mShowHours = true;
        this.mHoursToMinutes = 0.0f;
        this.mIs24HourMode = false;
        this.mAmOrPm = SELECTOR_CIRCLE;
        this.mTouchHelper = new RadialPickerTouchHelper();
        setAccessibilityDelegate(this.mTouchHelper);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(SELECTOR_DOT);
        }
        initHoursAndMinutesText();
        initData();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int currentHour = calendar.get(11);
        int currentMinute = calendar.get(NUM_POSITIONS);
        setCurrentHourInternal(currentHour, false, false);
        setCurrentMinuteInternal(currentMinute, false);
        setHapticFeedbackEnabled(true);
    }

    void applyAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int selectorActivatedColor;
        Context context = getContext();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        ColorStateList numbersTextColor = a.getColorStateList(3);
        ColorStateList numbersInnerTextColor = a.getColorStateList(9);
        ColorStateList[] colorStateListArr = this.mTextColor;
        if (numbersTextColor == null) {
            numbersTextColor = ColorStateList.valueOf(MISSING_COLOR);
        }
        colorStateListArr[SELECTOR_CIRCLE] = numbersTextColor;
        colorStateListArr = this.mTextColor;
        if (numbersInnerTextColor == null) {
            numbersInnerTextColor = ColorStateList.valueOf(MISSING_COLOR);
        }
        colorStateListArr[SELECTOR_LINE] = numbersInnerTextColor;
        this.mTextColor[SELECTOR_DOT] = this.mTextColor[SELECTOR_CIRCLE];
        ColorStateList selectorColors = a.getColorStateList(5);
        if (selectorColors != null) {
            selectorActivatedColor = selectorColors.getColorForState(StateSet.get(40), SELECTOR_CIRCLE);
        } else {
            selectorActivatedColor = MISSING_COLOR;
        }
        this.mPaintCenter.setColor(selectorActivatedColor);
        int[] stateSetActivated = StateSet.get(40);
        this.mSelectorColor = selectorActivatedColor;
        this.mSelectorDotColor = this.mTextColor[SELECTOR_CIRCLE].getColorForState(stateSetActivated, SELECTOR_CIRCLE);
        this.mPaintBackground.setColor(a.getColor(4, context.getColor(R.color.timepicker_default_numbers_background_color_material)));
        a.recycle();
    }

    public void initialize(int hour, int minute, boolean is24HourMode) {
        if (this.mIs24HourMode != is24HourMode) {
            this.mIs24HourMode = is24HourMode;
            initData();
        }
        setCurrentHourInternal(hour, false, false);
        setCurrentMinuteInternal(minute, false);
    }

    public void setCurrentItemShowing(int item, boolean animate) {
        switch (item) {
            case SELECTOR_CIRCLE /*0*/:
                showHours(animate);
            case SELECTOR_DOT /*1*/:
                showMinutes(animate);
            default:
                Log.e(TAG, "ClockView does not support showing item " + item);
        }
    }

    public int getCurrentItemShowing() {
        return this.mShowHours ? SELECTOR_CIRCLE : SELECTOR_DOT;
    }

    public void setOnValueSelectedListener(OnValueSelectedListener listener) {
        this.mListener = listener;
    }

    public void setCurrentHour(int hour) {
        setCurrentHourInternal(hour, true, false);
    }

    private void setCurrentHourInternal(int hour, boolean callback, boolean autoAdvance) {
        this.mSelectionDegrees[SELECTOR_CIRCLE] = (hour % NUM_POSITIONS) * DEGREES_FOR_ONE_HOUR;
        int amOrPm = (hour == 0 || hour % 24 < NUM_POSITIONS) ? SELECTOR_CIRCLE : SELECTOR_DOT;
        boolean isOnInnerCircle = getInnerCircleForHour(hour);
        if (!(this.mAmOrPm == amOrPm && this.mIsOnInnerCircle == isOnInnerCircle)) {
            this.mAmOrPm = amOrPm;
            this.mIsOnInnerCircle = isOnInnerCircle;
            initData();
            this.mTouchHelper.invalidateRoot();
        }
        invalidate();
        if (callback && this.mListener != null) {
            this.mListener.onValueSelected(SELECTOR_CIRCLE, hour, autoAdvance);
        }
    }

    public int getCurrentHour() {
        return getHourForDegrees(this.mSelectionDegrees[SELECTOR_CIRCLE], this.mIsOnInnerCircle);
    }

    private int getHourForDegrees(int degrees, boolean innerCircle) {
        int hour = (degrees / DEGREES_FOR_ONE_HOUR) % NUM_POSITIONS;
        if (this.mIs24HourMode) {
            if (!innerCircle && hour == 0) {
                return NUM_POSITIONS;
            }
            if (!innerCircle || hour == 0) {
                return hour;
            }
            return hour + NUM_POSITIONS;
        } else if (this.mAmOrPm == SELECTOR_DOT) {
            return hour + NUM_POSITIONS;
        } else {
            return hour;
        }
    }

    private int getDegreesForHour(int hour) {
        if (this.mIs24HourMode) {
            if (hour >= NUM_POSITIONS) {
                hour -= 12;
            }
        } else if (hour == NUM_POSITIONS) {
            hour = SELECTOR_CIRCLE;
        }
        return hour * DEGREES_FOR_ONE_HOUR;
    }

    private boolean getInnerCircleForHour(int hour) {
        return this.mIs24HourMode && (hour == 0 || hour > NUM_POSITIONS);
    }

    public void setCurrentMinute(int minute) {
        setCurrentMinuteInternal(minute, true);
    }

    private void setCurrentMinuteInternal(int minute, boolean callback) {
        this.mSelectionDegrees[SELECTOR_DOT] = (minute % MINUTES_IN_CIRCLE) * DEGREES_FOR_ONE_MINUTE;
        invalidate();
        if (callback && this.mListener != null) {
            this.mListener.onValueSelected(SELECTOR_DOT, minute, false);
        }
    }

    public int getCurrentMinute() {
        return getMinuteForDegrees(this.mSelectionDegrees[SELECTOR_DOT]);
    }

    private int getMinuteForDegrees(int degrees) {
        return degrees / DEGREES_FOR_ONE_MINUTE;
    }

    private int getDegreesForMinute(int minute) {
        return minute * DEGREES_FOR_ONE_MINUTE;
    }

    public boolean setAmOrPm(int amOrPm) {
        if (this.mAmOrPm == amOrPm || this.mIs24HourMode) {
            return false;
        }
        this.mAmOrPm = amOrPm;
        invalidate();
        this.mTouchHelper.invalidateRoot();
        return true;
    }

    public int getAmOrPm() {
        return this.mAmOrPm;
    }

    public void showHours(boolean animate) {
        showPicker(true, animate);
    }

    public void showMinutes(boolean animate) {
        showPicker(false, animate);
    }

    private void initHoursAndMinutesText() {
        for (int i = SELECTOR_CIRCLE; i < NUM_POSITIONS; i += SELECTOR_DOT) {
            String[] strArr = this.mHours12Texts;
            Object[] objArr = new Object[SELECTOR_DOT];
            objArr[SELECTOR_CIRCLE] = Integer.valueOf(HOURS_NUMBERS[i]);
            strArr[i] = String.format("%d", objArr);
            strArr = this.mInnerHours24Texts;
            objArr = new Object[SELECTOR_DOT];
            objArr[SELECTOR_CIRCLE] = Integer.valueOf(HOURS_NUMBERS_24[i]);
            strArr[i] = String.format("%02d", objArr);
            strArr = this.mOuterHours24Texts;
            objArr = new Object[SELECTOR_DOT];
            objArr[SELECTOR_CIRCLE] = Integer.valueOf(HOURS_NUMBERS[i]);
            strArr[i] = String.format("%d", objArr);
            strArr = this.mMinutesTexts;
            objArr = new Object[SELECTOR_DOT];
            objArr[SELECTOR_CIRCLE] = Integer.valueOf(MINUTES_NUMBERS[i]);
            strArr[i] = String.format("%02d", objArr);
        }
    }

    private void initData() {
        if (this.mIs24HourMode) {
            this.mOuterTextHours = this.mOuterHours24Texts;
            this.mInnerTextHours = this.mInnerHours24Texts;
        } else {
            this.mOuterTextHours = this.mHours12Texts;
            this.mInnerTextHours = this.mHours12Texts;
        }
        this.mMinutesText = this.mMinutesTexts;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            this.mXCenter = getWidth() / SELECTOR_LINE;
            this.mYCenter = getHeight() / SELECTOR_LINE;
            this.mCircleRadius = Math.min(this.mXCenter, this.mYCenter);
            this.mMinDistForInnerNumber = (this.mCircleRadius - this.mTextInset[SELECTOR_LINE]) - this.mSelectorRadius;
            this.mMaxDistForOuterNumber = (this.mCircleRadius - this.mTextInset[SELECTOR_CIRCLE]) + this.mSelectorRadius;
            this.mHalfwayDist = this.mCircleRadius - ((this.mTextInset[SELECTOR_CIRCLE] + this.mTextInset[SELECTOR_LINE]) / SELECTOR_LINE);
            calculatePositionsHours();
            calculatePositionsMinutes();
            this.mTouchHelper.invalidateRoot();
        }
    }

    public void onDraw(Canvas canvas) {
        float alphaMod = this.mInputEnabled ? LayoutParams.BRIGHTNESS_OVERRIDE_FULL : this.mDisabledAlpha;
        drawCircleBackground(canvas);
        Path selectorPath = this.mSelectorPath;
        drawSelector(canvas, selectorPath);
        drawHours(canvas, selectorPath, alphaMod);
        drawMinutes(canvas, selectorPath, alphaMod);
        drawCenter(canvas, alphaMod);
    }

    private void showPicker(boolean hours, boolean animate) {
        if (this.mShowHours != hours) {
            this.mShowHours = hours;
            if (animate) {
                animatePicker(hours, 500);
            } else {
                if (this.mHoursToMinutesAnimator != null && this.mHoursToMinutesAnimator.isStarted()) {
                    this.mHoursToMinutesAnimator.cancel();
                    this.mHoursToMinutesAnimator = null;
                }
                this.mHoursToMinutes = hours ? 0.0f : LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            }
            initData();
            invalidate();
            this.mTouchHelper.invalidateRoot();
        }
    }

    private void animatePicker(boolean hoursToMinutes, long duration) {
        int i;
        if (hoursToMinutes) {
            i = SELECTOR_CIRCLE;
        } else {
            i = SELECTOR_DOT;
        }
        float target = (float) i;
        if (this.mHoursToMinutes == target) {
            if (this.mHoursToMinutesAnimator != null && this.mHoursToMinutesAnimator.isStarted()) {
                this.mHoursToMinutesAnimator.cancel();
                this.mHoursToMinutesAnimator = null;
            }
            return;
        }
        Property property = this.HOURS_TO_MINUTES;
        float[] fArr = new float[SELECTOR_DOT];
        fArr[SELECTOR_CIRCLE] = target;
        this.mHoursToMinutesAnimator = ObjectAnimator.ofFloat(this, property, fArr);
        this.mHoursToMinutesAnimator.setAutoCancel(true);
        this.mHoursToMinutesAnimator.setDuration(duration);
        this.mHoursToMinutesAnimator.start();
    }

    private void drawCircleBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, (float) this.mCircleRadius, this.mPaintBackground);
    }

    private void drawHours(Canvas canvas, Path selectorPath, float alphaMod) {
        int hoursAlpha = (int) ((((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - this.mHoursToMinutes) * 255.0f) * alphaMod) + 0.5f);
        if (hoursAlpha > 0) {
            canvas.save(SELECTOR_LINE);
            canvas.clipPath(selectorPath, Op.DIFFERENCE);
            drawHoursClipped(canvas, hoursAlpha, false);
            canvas.restore();
            canvas.save(SELECTOR_LINE);
            canvas.clipPath(selectorPath, Op.INTERSECT);
            drawHoursClipped(canvas, hoursAlpha, true);
            canvas.restore();
        }
    }

    private void drawHoursClipped(Canvas canvas, int hoursAlpha, boolean showActivated) {
        float f = (float) this.mTextSize[SELECTOR_CIRCLE];
        Typeface typeface = this.mTypeface;
        ColorStateList colorStateList = this.mTextColor[SELECTOR_CIRCLE];
        String[] strArr = this.mOuterTextHours;
        float[] fArr = this.mOuterTextX[SELECTOR_CIRCLE];
        float[] fArr2 = this.mOuterTextY[SELECTOR_CIRCLE];
        Paint paint = this.mPaint[SELECTOR_CIRCLE];
        boolean z = showActivated && !this.mIsOnInnerCircle;
        drawTextElements(canvas, f, typeface, colorStateList, strArr, fArr, fArr2, paint, hoursAlpha, z, this.mSelectionDegrees[SELECTOR_CIRCLE], showActivated);
        if (this.mIs24HourMode && this.mInnerTextHours != null) {
            drawTextElements(canvas, (float) this.mTextSize[SELECTOR_LINE], this.mTypeface, this.mTextColor[SELECTOR_LINE], this.mInnerTextHours, this.mInnerTextX, this.mInnerTextY, this.mPaint[SELECTOR_CIRCLE], hoursAlpha, showActivated ? this.mIsOnInnerCircle : false, this.mSelectionDegrees[SELECTOR_CIRCLE], showActivated);
        }
    }

    private void drawMinutes(Canvas canvas, Path selectorPath, float alphaMod) {
        int minutesAlpha = (int) (((this.mHoursToMinutes * 255.0f) * alphaMod) + 0.5f);
        if (minutesAlpha > 0) {
            canvas.save(SELECTOR_LINE);
            canvas.clipPath(selectorPath, Op.DIFFERENCE);
            drawMinutesClipped(canvas, minutesAlpha, false);
            canvas.restore();
            canvas.save(SELECTOR_LINE);
            canvas.clipPath(selectorPath, Op.INTERSECT);
            drawMinutesClipped(canvas, minutesAlpha, true);
            canvas.restore();
        }
    }

    private void drawMinutesClipped(Canvas canvas, int minutesAlpha, boolean showActivated) {
        drawTextElements(canvas, (float) this.mTextSize[SELECTOR_DOT], this.mTypeface, this.mTextColor[SELECTOR_DOT], this.mMinutesText, this.mOuterTextX[SELECTOR_DOT], this.mOuterTextY[SELECTOR_DOT], this.mPaint[SELECTOR_DOT], minutesAlpha, showActivated, this.mSelectionDegrees[SELECTOR_DOT], showActivated);
    }

    private void drawCenter(Canvas canvas, float alphaMod) {
        this.mPaintCenter.setAlpha((int) ((255.0f * alphaMod) + 0.5f));
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, (float) this.mCenterDotRadius, this.mPaintCenter);
    }

    private int getMultipliedAlpha(int argb, int alpha) {
        return (int) ((((double) Color.alpha(argb)) * (((double) alpha) / 255.0d)) + 0.5d);
    }

    private void drawSelector(Canvas canvas, Path selectorPath) {
        int hoursIndex = this.mIsOnInnerCircle ? SELECTOR_LINE : SELECTOR_CIRCLE;
        int hoursInset = this.mTextInset[hoursIndex];
        int hoursAngleDeg = this.mSelectionDegrees[hoursIndex % SELECTOR_LINE];
        float hoursDotScale = (float) (this.mSelectionDegrees[hoursIndex % SELECTOR_LINE] % DEGREES_FOR_ONE_HOUR != 0 ? SELECTOR_DOT : SELECTOR_CIRCLE);
        int minutesInset = this.mTextInset[SELECTOR_DOT];
        int minutesAngleDeg = this.mSelectionDegrees[SELECTOR_DOT];
        float minutesDotScale = (float) (this.mSelectionDegrees[SELECTOR_DOT] % DEGREES_FOR_ONE_HOUR != 0 ? SELECTOR_DOT : SELECTOR_CIRCLE);
        int selRadius = this.mSelectorRadius;
        float selLength = ((float) this.mCircleRadius) - MathUtils.lerp((float) hoursInset, (float) minutesInset, this.mHoursToMinutes);
        double selAngleRad = Math.toRadians((double) MathUtils.lerpDeg((float) hoursAngleDeg, (float) minutesAngleDeg, this.mHoursToMinutes));
        float selCenterX = ((float) this.mXCenter) + (((float) Math.sin(selAngleRad)) * selLength);
        float selCenterY = ((float) this.mYCenter) - (((float) Math.cos(selAngleRad)) * selLength);
        Paint paint = this.mPaintSelector[SELECTOR_CIRCLE];
        paint.setColor(this.mSelectorColor);
        canvas.drawCircle(selCenterX, selCenterY, (float) selRadius, paint);
        if (selectorPath != null) {
            selectorPath.reset();
            selectorPath.addCircle(selCenterX, selCenterY, (float) selRadius, Direction.CCW);
        }
        float dotScale = MathUtils.lerp(hoursDotScale, minutesDotScale, this.mHoursToMinutes);
        if (dotScale > 0.0f) {
            Paint dotPaint = this.mPaintSelector[SELECTOR_DOT];
            dotPaint.setColor(this.mSelectorDotColor);
            canvas.drawCircle(selCenterX, selCenterY, ((float) this.mSelectorDotRadius) * dotScale, dotPaint);
        }
        double sin = Math.sin(selAngleRad);
        double cos = Math.cos(selAngleRad);
        float lineLength = selLength - ((float) selRadius);
        float linePointX = (float) (((int) (((double) lineLength) * sin)) + (this.mXCenter + ((int) (((double) this.mCenterDotRadius) * sin))));
        float linePointY = (float) ((this.mYCenter - ((int) (((double) this.mCenterDotRadius) * cos))) - ((int) (((double) lineLength) * cos)));
        Paint linePaint = this.mPaintSelector[SELECTOR_LINE];
        linePaint.setColor(this.mSelectorColor);
        linePaint.setStrokeWidth((float) this.mSelectorStroke);
        canvas.drawLine((float) this.mXCenter, (float) this.mYCenter, linePointX, linePointY, linePaint);
    }

    private void calculatePositionsHours() {
        calculatePositions(this.mPaint[SELECTOR_CIRCLE], (float) (this.mCircleRadius - this.mTextInset[SELECTOR_CIRCLE]), (float) this.mXCenter, (float) this.mYCenter, (float) this.mTextSize[SELECTOR_CIRCLE], this.mOuterTextX[SELECTOR_CIRCLE], this.mOuterTextY[SELECTOR_CIRCLE]);
        if (this.mIs24HourMode) {
            calculatePositions(this.mPaint[SELECTOR_CIRCLE], (float) (this.mCircleRadius - this.mTextInset[SELECTOR_LINE]), (float) this.mXCenter, (float) this.mYCenter, (float) this.mTextSize[SELECTOR_LINE], this.mInnerTextX, this.mInnerTextY);
        }
    }

    private void calculatePositionsMinutes() {
        calculatePositions(this.mPaint[SELECTOR_DOT], (float) (this.mCircleRadius - this.mTextInset[SELECTOR_DOT]), (float) this.mXCenter, (float) this.mYCenter, (float) this.mTextSize[SELECTOR_DOT], this.mOuterTextX[SELECTOR_DOT], this.mOuterTextY[SELECTOR_DOT]);
    }

    private static void calculatePositions(Paint paint, float radius, float xCenter, float yCenter, float textSize, float[] x, float[] y) {
        paint.setTextSize(textSize);
        yCenter -= (paint.descent() + paint.ascent()) / 2.0f;
        for (int i = SELECTOR_CIRCLE; i < NUM_POSITIONS; i += SELECTOR_DOT) {
            x[i] = xCenter - (COS_30[i] * radius);
            y[i] = yCenter - (SIN_30[i] * radius);
        }
    }

    private void drawTextElements(Canvas canvas, float textSize, Typeface typeface, ColorStateList textColor, String[] texts, float[] textX, float[] textY, Paint paint, int alpha, boolean showActivated, int activatedDegrees, boolean activatedOnly) {
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
        float activatedIndex = ((float) activatedDegrees) / 30.0f;
        int activatedFloor = (int) activatedIndex;
        int activatedCeil = ((int) Math.ceil((double) activatedIndex)) % NUM_POSITIONS;
        int i = SELECTOR_CIRCLE;
        while (i < NUM_POSITIONS) {
            boolean activated = activatedFloor == i || activatedCeil == i;
            if (!activatedOnly || activated) {
                int i2 = (showActivated && activated) ? 32 : SELECTOR_CIRCLE;
                int color = textColor.getColorForState(StateSet.get(i2 | 8), SELECTOR_CIRCLE);
                paint.setColor(color);
                paint.setAlpha(getMultipliedAlpha(color, alpha));
                canvas.drawText(texts[i], textX[i], textY[i], paint);
            }
            i += SELECTOR_DOT;
        }
    }

    private int getDegreesFromXY(float x, float y, boolean constrainOutside) {
        int innerBound;
        int outerBound;
        if (this.mIs24HourMode && this.mShowHours) {
            innerBound = this.mMinDistForInnerNumber;
            outerBound = this.mMaxDistForOuterNumber;
        } else {
            int center = this.mCircleRadius - this.mTextInset[this.mShowHours ? SELECTOR_CIRCLE : SELECTOR_DOT];
            innerBound = center - this.mSelectorRadius;
            outerBound = center + this.mSelectorRadius;
        }
        double dX = (double) (x - ((float) this.mXCenter));
        double dY = (double) (y - ((float) this.mYCenter));
        double distFromCenter = Math.sqrt((dX * dX) + (dY * dY));
        if (distFromCenter < ((double) innerBound) || (constrainOutside && distFromCenter > ((double) outerBound))) {
            return -1;
        }
        int degrees = (int) (Math.toDegrees(Math.atan2(dY, dX) + 1.5707963267948966d) + 0.5d);
        if (degrees < 0) {
            return degrees + HwModemCapability.MODEM_CAP_MAX;
        }
        return degrees;
    }

    private boolean getInnerCircleFromXY(float x, float y) {
        boolean z = false;
        if (!this.mIs24HourMode || !this.mShowHours) {
            return false;
        }
        double dX = (double) (x - ((float) this.mXCenter));
        double dY = (double) (y - ((float) this.mYCenter));
        if (Math.sqrt((dX * dX) + (dY * dY)) <= ((double) this.mHalfwayDist)) {
            z = true;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mInputEnabled) {
            return true;
        }
        int action = event.getActionMasked();
        if (!(action == SELECTOR_LINE || action == SELECTOR_DOT)) {
            if (action == 0) {
            }
            return true;
        }
        boolean forceSelection = false;
        boolean autoAdvance = false;
        if (action == 0) {
            this.mChangedDuringTouch = false;
        } else if (action == SELECTOR_DOT) {
            autoAdvance = true;
            if (!this.mChangedDuringTouch) {
                forceSelection = true;
            }
        }
        this.mChangedDuringTouch |= handleTouchInput(event.getX(), event.getY(), forceSelection, autoAdvance);
        return true;
    }

    private boolean handleTouchInput(float x, float y, boolean forceSelection, boolean autoAdvance) {
        boolean isOnInnerCircle = getInnerCircleFromXY(x, y);
        int degrees = getDegreesFromXY(x, y, false);
        if (degrees == -1) {
            return false;
        }
        boolean valueChanged;
        int type;
        int newValue;
        animatePicker(this.mShowHours, 60);
        int snapDegrees;
        if (this.mShowHours) {
            snapDegrees = snapOnly30s(degrees, SELECTOR_CIRCLE) % HwModemCapability.MODEM_CAP_MAX;
            valueChanged = this.mIsOnInnerCircle == isOnInnerCircle ? this.mSelectionDegrees[SELECTOR_CIRCLE] != snapDegrees : true;
            this.mIsOnInnerCircle = isOnInnerCircle;
            this.mSelectionDegrees[SELECTOR_CIRCLE] = snapDegrees;
            type = SELECTOR_CIRCLE;
            newValue = getCurrentHour();
        } else {
            snapDegrees = snapPrefer30s(degrees) % HwModemCapability.MODEM_CAP_MAX;
            valueChanged = this.mSelectionDegrees[SELECTOR_DOT] != snapDegrees;
            this.mSelectionDegrees[SELECTOR_DOT] = snapDegrees;
            type = SELECTOR_DOT;
            newValue = getCurrentMinute();
        }
        if (!valueChanged && !forceSelection && !autoAdvance) {
            return false;
        }
        if (this.mListener != null) {
            this.mListener.onValueSelected(type, newValue, autoAdvance);
        }
        if (valueChanged || forceSelection) {
            performHapticFeedback(4);
            invalidate();
        }
        return true;
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    public void setInputEnabled(boolean inputEnabled) {
        this.mInputEnabled = inputEnabled;
        invalidate();
    }
}
