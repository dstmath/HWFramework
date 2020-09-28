package huawei.android.widget;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import com.android.internal.app.LocaleHelper;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public class DatePicker extends android.widget.DatePicker {
    private static final String ARAB_SCRIPT_SUBTAG = "Arab";
    private static final String DATEPICKER_SPINNER_CLASS_NAME = "android.widget.DatePickerSpinnerDelegate";
    private static final int DEFAULT_DATE_ORDER_FIRST = 1;
    private static final int DEFAULT_DATE_ORDER_SECOND = 2;
    private static final int DIVIDER_NUMBER = 2;
    private static final String GOOGLE_DP_CLASSNAME = "android.widget.DatePicker";
    private static final String HEBR_SCRIPT_SUBTAG = "Hebr";
    private static final String TAG = "DatePicker";
    private View mDateDividerDayYear;
    private View mDateDividerMonthDay;
    private Class mDatePickerSpinnerClass;
    private Class mGoogleDatePickerClass;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, context.getResources().getIdentifier("datePickerStyle", "attr", "android"));
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initInvokeClass();
        if (getDaySpinner() == null || getMonthSpinner() == null || getYearSpinner() == null) {
            Log.w(TAG, "getDaySpinner = " + getDaySpinner() + ", getMonthSpinner = " + getMonthSpinner() + ", getYearSpinner() = " + getYearSpinner());
            return;
        }
        if (((String[]) reflectMember(DATEPICKER_SPINNER_CLASS_NAME, "mShortMonths")) != null) {
            String[] shortMonths = LocaleData.get(Locale.getDefault()).shortStandAloneMonthNames;
        }
        addFireLists(getDaySpinner(), getMonthSpinner(), getYearSpinner());
        setDivider();
        if (getDaySpinner() != null && HwWidgetFactory.isHwTheme(context)) {
            getDaySpinner().setFormatter(null);
        }
        reorderSpinners();
    }

    private void setDivider() {
        View view;
        View view2;
        this.mDateDividerMonthDay = findViewById(34603101);
        this.mDateDividerDayYear = findViewById(34603102);
        LinearLayout spinnerGroup = getSpinners();
        if (spinnerGroup == null) {
            Log.w(TAG, "can not get the spinners.");
            return;
        }
        int childCount = spinnerGroup.getChildCount();
        if (childCount > 2 && (view2 = this.mDateDividerDayYear) != null) {
            spinnerGroup.addView(view2, 2);
        }
        if (childCount > 1 && (view = this.mDateDividerMonthDay) != null) {
            spinnerGroup.addView(view, 1);
        }
    }

    private void addFireLists(NumberPicker daySpinner, NumberPicker monthSpinner, NumberPicker yearSpinner) {
        daySpinner.addFireList(monthSpinner);
        daySpinner.addFireList(yearSpinner);
        monthSpinner.addFireList(daySpinner);
        monthSpinner.addFireList(yearSpinner);
        yearSpinner.addFireList(daySpinner);
        yearSpinner.addFireList(monthSpinner);
    }

    private NumberPicker getDaySpinner() {
        Object obj = reflectMember(DATEPICKER_SPINNER_CLASS_NAME, "mDaySpinner");
        if (obj != null) {
            return (NumberPicker) obj;
        }
        return null;
    }

    private NumberPicker getMonthSpinner() {
        Object obj = reflectMember(DATEPICKER_SPINNER_CLASS_NAME, "mMonthSpinner");
        if (obj != null) {
            return (NumberPicker) obj;
        }
        return null;
    }

    private NumberPicker getYearSpinner() {
        Object obj = reflectMember(DATEPICKER_SPINNER_CLASS_NAME, "mYearSpinner");
        if (obj != null) {
            return (NumberPicker) obj;
        }
        return null;
    }

    private LinearLayout getSpinners() {
        Object obj = reflectMember(DATEPICKER_SPINNER_CLASS_NAME, "mSpinners");
        if (obj instanceof LinearLayout) {
            return (LinearLayout) obj;
        }
        return null;
    }

    private Object reflectMember(String className, String memberName) {
        Class targetClass;
        Field field;
        try {
            Object delegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleDatePickerClass);
            if (delegateObj == null || (targetClass = Class.forName(className)) == null || (field = targetClass.getDeclaredField(memberName)) == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(delegateObj);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "targetClass not found");
            return null;
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission Denial");
            return null;
        } catch (NoSuchFieldException e3) {
            Log.e(TAG, "reflect fail NoSuchFieldException");
            return null;
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "the memberName is not found");
            return null;
        } catch (IllegalAccessException e5) {
            Log.e(TAG, "Permission Denial: private Security permissions");
            return null;
        }
    }

    private void reorderSpinners() {
        if (getSpinners() != null) {
            LinearLayout spinners = getSpinners();
            spinners.removeAllViews();
            Locale locale = Locale.getDefault();
            char[] orders = ICU.getDateFormatOrder(DateFormat.getBestDateTimePattern(locale, "yyyyMMMdd"));
            getDirectionFromLocale(orders, locale);
            int spinnerCount = orders.length;
            for (int i = 0; i < spinnerCount; i++) {
                char c = orders[i];
                if (c == 'M') {
                    spinners.addView(getMonthSpinner());
                    invokeSetImeOptions(getMonthSpinner(), spinnerCount, i);
                } else if (c == 'd') {
                    spinners.addView(getDaySpinner());
                    invokeSetImeOptions(getDaySpinner(), spinnerCount, i);
                } else if (c == 'y') {
                    spinners.addView(getYearSpinner());
                    invokeSetImeOptions(getYearSpinner(), spinnerCount, i);
                } else {
                    throw new IllegalArgumentException(Arrays.toString(orders));
                }
            }
        }
    }

    private void invokeSetImeOptions(NumberPicker spinner, int spinnerCount, int spinnerIndex) {
        Object delegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGoogleDatePickerClass);
        if (delegateObj != null) {
            ReflectUtil.callMethod(delegateObj, "setImeOptions", new Class[]{NumberPicker.class, Integer.TYPE, Integer.TYPE}, new Object[]{spinner, Integer.valueOf(spinnerCount), Integer.valueOf(spinnerIndex)}, this.mDatePickerSpinnerClass);
        }
    }

    private static void getDirectionFromLocale(char[] orders, Locale locale) {
        if (isRightToLeft(locale)) {
            int length = orders.length;
            for (int i = 0; i < length / 2; i++) {
                char temp = orders[i];
                orders[i] = orders[(length - 1) - i];
                orders[(length - 1) - i] = temp;
            }
        }
    }

    private static boolean isRightToLeft(Locale locale) {
        if (locale != null && !locale.equals(Locale.ROOT)) {
            String scriptSubtag = ICU.addLikelySubtags(locale).getScript();
            if (scriptSubtag == null) {
                byte directionality = Character.getDirectionality(LocaleHelper.getDisplayName(locale, locale, false).charAt(0));
                if (directionality == 1 || directionality == 2) {
                    return true;
                }
                return false;
            } else if (scriptSubtag.equalsIgnoreCase(ARAB_SCRIPT_SUBTAG) || scriptSubtag.equalsIgnoreCase(HEBR_SCRIPT_SUBTAG)) {
                return true;
            }
        }
        return false;
    }

    private void initInvokeClass() {
        if (this.mDatePickerSpinnerClass == null) {
            try {
                this.mDatePickerSpinnerClass = Class.forName(DATEPICKER_SPINNER_CLASS_NAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mTimePSpinnerClass not found");
            }
        }
        if (this.mGoogleDatePickerClass == null) {
            try {
                this.mGoogleDatePickerClass = Class.forName(GOOGLE_DP_CLASSNAME);
            } catch (ClassNotFoundException e2) {
                Log.e(TAG, "mGTimePickerClass not found");
            }
        }
    }
}
