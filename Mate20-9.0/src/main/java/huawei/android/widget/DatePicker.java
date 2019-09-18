package huawei.android.widget;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public class DatePicker extends android.widget.DatePicker {
    private static String ARAB_SCRIPT_SUBTAG = "Arab";
    private static final String DATEPICKER_SPINNER_CLASS_NAME = "android.widget.DatePickerSpinnerDelegate";
    private static final int DEFAULT_DATE_ORDER_FIRST = 1;
    private static final int DEFAULT_DATE_ORDER_SECOND = 2;
    private static final String GOOGLE_DP_CLASSNAME = "android.widget.DatePicker";
    private static String HEBR_SCRIPT_SUBTAG = "Hebr";
    private static final String TAG = "DatePicker";
    private Class mDatePSpinnerClass;
    private View mDatedivider1;
    private View mDatedivider2;
    private Class mGDatePickerClass;

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
        this.mDatedivider1 = findViewById(34603101);
        this.mDatedivider2 = findViewById(34603102);
        LinearLayout spinnerGroup = getSpinners();
        if (spinnerGroup != null) {
            int childCount = spinnerGroup.getChildCount();
            if (childCount > 2 && this.mDatedivider2 != null) {
                spinnerGroup.addView(this.mDatedivider2, 2);
            }
            if (childCount > 1 && this.mDatedivider1 != null) {
                spinnerGroup.addView(this.mDatedivider1, 1);
                return;
            }
            return;
        }
        throw new RuntimeException("can not get the spinners.");
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
        if (obj != null) {
            return (LinearLayout) obj;
        }
        return null;
    }

    private Object reflectMember(String className, String memberName) {
        try {
            Object delegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGDatePickerClass);
            if (delegateObj != null) {
                Class targetClass = Class.forName(className);
                if (targetClass != null) {
                    Field field = targetClass.getDeclaredField(memberName);
                    if (field != null) {
                        field.setAccessible(true);
                        return field.get(delegateObj);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "targetClass not found");
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission Denial");
        } catch (NoSuchFieldException e3) {
            Log.e(TAG, "reflect fail NoSuchFieldException");
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "the memberName is not found");
        } catch (IllegalAccessException e5) {
            Log.e(TAG, "Permission Denial: private Security permissions");
        }
        return null;
    }

    private void reorderSpinners() {
        if (getSpinners() != null) {
            LinearLayout mSpinners = getSpinners();
            mSpinners.removeAllViews();
            Locale locale = Locale.getDefault();
            char[] order = ICU.getDateFormatOrder(DateFormat.getBestDateTimePattern(locale, "yyyyMMMdd"));
            getDirectionFromLocale(order, locale);
            int spinnerCount = order.length;
            for (int i = 0; i < spinnerCount; i++) {
                char c = order[i];
                if (c == 'M') {
                    mSpinners.addView(getMonthSpinner());
                    invokeSetImeOptions(getMonthSpinner(), spinnerCount, i);
                } else if (c == 'd') {
                    mSpinners.addView(getDaySpinner());
                    invokeSetImeOptions(getDaySpinner(), spinnerCount, i);
                } else if (c == 'y') {
                    mSpinners.addView(getYearSpinner());
                    invokeSetImeOptions(getYearSpinner(), spinnerCount, i);
                } else {
                    throw new IllegalArgumentException(Arrays.toString(order));
                }
            }
        }
    }

    private void invokeSetImeOptions(NumberPicker spinner, int spinnerCount, int spinnerIndex) {
        Object mDelegateObj = ReflectUtil.getObject(this, "mDelegate", this.mGDatePickerClass);
        if (mDelegateObj != null) {
            ReflectUtil.callMethod(mDelegateObj, "setImeOptions", new Class[]{NumberPicker.class, Integer.TYPE, Integer.TYPE}, new Object[]{spinner, Integer.valueOf(spinnerCount), Integer.valueOf(spinnerIndex)}, this.mDatePSpinnerClass);
        }
    }

    private static void getDirectionFromLocale(char[] order, Locale locale) {
        if (isRightToLeft(locale)) {
            int length = order.length;
            for (int i = 0; i < length / 2; i++) {
                char temp = order[i];
                order[i] = order[(length - 1) - i];
                order[(length - 1) - i] = temp;
            }
        }
    }

    private static boolean isRightToLeft(Locale locale) {
        if (locale != null && !locale.equals(Locale.ROOT)) {
            String scriptSubtag = ICU.addLikelySubtags(locale).getScript();
            if (scriptSubtag == null) {
                switch (Character.getDirectionality(locale.getDisplayName(locale).charAt(0))) {
                    case 1:
                    case 2:
                        return true;
                    default:
                        return false;
                }
            } else if (scriptSubtag.equalsIgnoreCase(ARAB_SCRIPT_SUBTAG) || scriptSubtag.equalsIgnoreCase(HEBR_SCRIPT_SUBTAG)) {
                return true;
            }
        }
        return false;
    }

    private void initInvokeClass() {
        if (this.mDatePSpinnerClass == null) {
            try {
                this.mDatePSpinnerClass = Class.forName(DATEPICKER_SPINNER_CLASS_NAME);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "mTimePSpinnerClass not found");
            }
        }
        if (this.mGDatePickerClass == null) {
            try {
                this.mGDatePickerClass = Class.forName(GOOGLE_DP_CLASSNAME);
            } catch (ClassNotFoundException e2) {
                Log.e(TAG, "mGTimePickerClass not found");
            }
        }
    }
}
