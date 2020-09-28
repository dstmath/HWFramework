package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntFunction;

public class DatePicker extends FrameLayout {
    private static final String LOG_TAG = DatePicker.class.getSimpleName();
    public static final int MODE_CALENDAR = 2;
    public static final int MODE_SPINNER = 1;
    @UnsupportedAppUsage
    private final DatePickerDelegate mDelegate;
    private final int mMode;

    /* access modifiers changed from: package-private */
    public interface DatePickerDelegate {
        void autofill(AutofillValue autofillValue);

        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        AutofillValue getAutofillValue();

        CalendarView getCalendarView();

        boolean getCalendarViewShown();

        int getDayOfMonth();

        int getFirstDayOfWeek();

        Calendar getMaxDate();

        Calendar getMinDate();

        int getMonth();

        boolean getSpinnersShown();

        int getYear();

        void init(int i, int i2, int i3, OnDateChangedListener onDateChangedListener);

        boolean isEnabled();

        void onConfigurationChanged(Configuration configuration);

        void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onRestoreInstanceState(Parcelable parcelable);

        Parcelable onSaveInstanceState(Parcelable parcelable);

        void setAutoFillChangeListener(OnDateChangedListener onDateChangedListener);

        void setCalendarViewShown(boolean z);

        void setEnabled(boolean z);

        void setFirstDayOfWeek(int i);

        void setMaxDate(long j);

        void setMinDate(long j);

        void setOnDateChangedListener(OnDateChangedListener onDateChangedListener);

        void setSpinnersShown(boolean z);

        void setValidationCallback(ValidationCallback validationCallback);

        void updateDate(int i, int i2, int i3);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DatePickerMode {
    }

    public interface OnDateChangedListener {
        void onDateChanged(DatePicker datePicker, int i, int i2, int i3);
    }

    public interface ValidationCallback {
        void onValidationChanged(boolean z);
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<DatePicker> {
        private int mCalendarViewShownId;
        private int mDatePickerModeId;
        private int mDayOfMonthId;
        private int mFirstDayOfWeekId;
        private int mMaxDateId;
        private int mMinDateId;
        private int mMonthId;
        private boolean mPropertiesMapped = false;
        private int mSpinnersShownId;
        private int mYearId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mCalendarViewShownId = propertyMapper.mapBoolean("calendarViewShown", 16843596);
            SparseArray<String> datePickerModeEnumMapping = new SparseArray<>();
            datePickerModeEnumMapping.put(1, "spinner");
            datePickerModeEnumMapping.put(2, "calendar");
            Objects.requireNonNull(datePickerModeEnumMapping);
            this.mDatePickerModeId = propertyMapper.mapIntEnum("datePickerMode", 16843955, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mDayOfMonthId = propertyMapper.mapInt("dayOfMonth", 0);
            this.mFirstDayOfWeekId = propertyMapper.mapInt("firstDayOfWeek", 16843581);
            this.mMaxDateId = propertyMapper.mapLong("maxDate", 16843584);
            this.mMinDateId = propertyMapper.mapLong("minDate", 16843583);
            this.mMonthId = propertyMapper.mapInt("month", 0);
            this.mSpinnersShownId = propertyMapper.mapBoolean("spinnersShown", 16843595);
            this.mYearId = propertyMapper.mapInt(MediaStore.Audio.AudioColumns.YEAR, 0);
            this.mPropertiesMapped = true;
        }

        public void readProperties(DatePicker node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mCalendarViewShownId, node.getCalendarViewShown());
                propertyReader.readIntEnum(this.mDatePickerModeId, node.getMode());
                propertyReader.readInt(this.mDayOfMonthId, node.getDayOfMonth());
                propertyReader.readInt(this.mFirstDayOfWeekId, node.getFirstDayOfWeek());
                propertyReader.readLong(this.mMaxDateId, node.getMaxDate());
                propertyReader.readLong(this.mMinDateId, node.getMinDate());
                propertyReader.readInt(this.mMonthId, node.getMonth());
                propertyReader.readBoolean(this.mSpinnersShownId, node.getSpinnersShown());
                propertyReader.readInt(this.mYearId, node.getYear());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 16843612);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(1);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.DatePicker, attrs, a, defStyleAttr, defStyleRes);
        boolean isDialogMode = a.getBoolean(17, false);
        int requestedMode = a.getInt(16, 1);
        int firstDayOfWeek = a.getInt(3, 0);
        a.recycle();
        if (requestedMode != 2 || !isDialogMode) {
            this.mMode = requestedMode;
        } else {
            this.mMode = context.getResources().getInteger(R.integer.date_picker_mode);
        }
        if (this.mMode != 2) {
            this.mDelegate = createSpinnerUIDelegate(context, attrs, defStyleAttr, defStyleRes);
        } else {
            this.mDelegate = createCalendarUIDelegate(context, attrs, defStyleAttr, defStyleRes);
        }
        if (firstDayOfWeek != 0) {
            setFirstDayOfWeek(firstDayOfWeek);
        }
        this.mDelegate.setAutoFillChangeListener(new OnDateChangedListener(context) {
            /* class android.widget.$$Lambda$DatePicker$AnJPL5BrPXPJaOcWUABHJq84 */
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.widget.DatePicker.OnDateChangedListener
            public final void onDateChanged(DatePicker datePicker, int i, int i2, int i3) {
                DatePicker.this.lambda$new$0$DatePicker(this.f$1, datePicker, i, i2, i3);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$DatePicker(Context context, DatePicker v, int y, int m, int d) {
        AutofillManager afm = (AutofillManager) context.getSystemService(AutofillManager.class);
        if (afm != null) {
            afm.notifyValueChanged(this);
        }
    }

    private DatePickerDelegate createSpinnerUIDelegate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        return new DatePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    private DatePickerDelegate createCalendarUIDelegate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        return new DatePickerCalendarDelegate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    public int getMode() {
        return this.mMode;
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        this.mDelegate.init(year, monthOfYear, dayOfMonth, onDateChangedListener);
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        this.mDelegate.setOnDateChangedListener(onDateChangedListener);
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        this.mDelegate.updateDate(year, month, dayOfMonth);
    }

    public int getYear() {
        return this.mDelegate.getYear();
    }

    public int getMonth() {
        return this.mDelegate.getMonth();
    }

    public int getDayOfMonth() {
        return this.mDelegate.getDayOfMonth();
    }

    public long getMinDate() {
        return this.mDelegate.getMinDate().getTimeInMillis();
    }

    public void setMinDate(long minDate) {
        this.mDelegate.setMinDate(minDate);
    }

    public long getMaxDate() {
        return this.mDelegate.getMaxDate().getTimeInMillis();
    }

    public void setMaxDate(long maxDate) {
        this.mDelegate.setMaxDate(maxDate);
    }

    @UnsupportedAppUsage
    public void setValidationCallback(ValidationCallback callback) {
        this.mDelegate.setValidationCallback(callback);
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        if (this.mDelegate.isEnabled() != enabled) {
            super.setEnabled(enabled);
            this.mDelegate.setEnabled(enabled);
        }
    }

    @Override // android.view.View
    public boolean isEnabled() {
        return this.mDelegate.isEnabled();
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        return this.mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        this.mDelegate.onPopulateAccessibilityEvent(event);
    }

    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public CharSequence getAccessibilityClassName() {
        return DatePicker.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDelegate.onConfigurationChanged(newConfig);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        if (firstDayOfWeek < 1 || firstDayOfWeek > 7) {
            throw new IllegalArgumentException("firstDayOfWeek must be between 1 and 7");
        }
        this.mDelegate.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return this.mDelegate.getFirstDayOfWeek();
    }

    @Deprecated
    public boolean getCalendarViewShown() {
        return this.mDelegate.getCalendarViewShown();
    }

    @Deprecated
    public CalendarView getCalendarView() {
        return this.mDelegate.getCalendarView();
    }

    @Deprecated
    public void setCalendarViewShown(boolean shown) {
        this.mDelegate.setCalendarViewShown(shown);
    }

    @Deprecated
    public boolean getSpinnersShown() {
        return this.mDelegate.getSpinnersShown();
    }

    @Deprecated
    public void setSpinnersShown(boolean shown) {
        this.mDelegate.setSpinnersShown(shown);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.onSaveInstanceState());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        View.BaseSavedState ss = (View.BaseSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDelegate.onRestoreInstanceState(ss);
    }

    static abstract class AbstractDatePickerDelegate implements DatePickerDelegate {
        protected OnDateChangedListener mAutoFillChangeListener;
        private long mAutofilledValue;
        protected Context mContext;
        protected Calendar mCurrentDate;
        protected Locale mCurrentLocale;
        protected DatePicker mDelegator;
        protected OnDateChangedListener mOnDateChangedListener;
        protected ValidationCallback mValidationCallback;

        public AbstractDatePickerDelegate(DatePicker delegator, Context context) {
            this.mDelegator = delegator;
            this.mContext = context;
            setCurrentLocale(Locale.getDefault());
        }

        /* access modifiers changed from: protected */
        public void setCurrentLocale(Locale locale) {
            if (!locale.equals(this.mCurrentLocale)) {
                this.mCurrentLocale = locale;
                onLocaleChanged(locale);
            }
        }

        @Override // android.widget.DatePicker.DatePickerDelegate
        public void setOnDateChangedListener(OnDateChangedListener callback) {
            this.mOnDateChangedListener = callback;
        }

        @Override // android.widget.DatePicker.DatePickerDelegate
        public void setAutoFillChangeListener(OnDateChangedListener callback) {
            this.mAutoFillChangeListener = callback;
        }

        @Override // android.widget.DatePicker.DatePickerDelegate
        public void setValidationCallback(ValidationCallback callback) {
            this.mValidationCallback = callback;
        }

        @Override // android.widget.DatePicker.DatePickerDelegate
        public final void autofill(AutofillValue value) {
            if (value == null || !value.isDate()) {
                String str = DatePicker.LOG_TAG;
                Log.w(str, value + " could not be autofilled into " + this);
                return;
            }
            long time = value.getDateValue();
            Calendar cal = Calendar.getInstance(this.mCurrentLocale);
            cal.setTimeInMillis(time);
            updateDate(cal.get(1), cal.get(2), cal.get(5));
            this.mAutofilledValue = time;
        }

        @Override // android.widget.DatePicker.DatePickerDelegate
        public final AutofillValue getAutofillValue() {
            long time = this.mAutofilledValue;
            if (time == 0) {
                time = this.mCurrentDate.getTimeInMillis();
            }
            return AutofillValue.forDate(time);
        }

        /* access modifiers changed from: protected */
        public void resetAutofilledValue() {
            this.mAutofilledValue = 0;
        }

        /* access modifiers changed from: protected */
        public void onValidationChanged(boolean valid) {
            ValidationCallback validationCallback = this.mValidationCallback;
            if (validationCallback != null) {
                validationCallback.onValidationChanged(valid);
            }
        }

        /* access modifiers changed from: protected */
        public void onLocaleChanged(Locale locale) {
        }

        @Override // android.widget.DatePicker.DatePickerDelegate
        public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
            event.getText().add(getFormattedCurrentDate());
        }

        /* access modifiers changed from: protected */
        public String getFormattedCurrentDate() {
            return DateUtils.formatDateTime(this.mContext, this.mCurrentDate.getTimeInMillis(), 22);
        }

        /* access modifiers changed from: package-private */
        public static class SavedState extends View.BaseSavedState {
            public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
                /* class android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.AnonymousClass1 */

                @Override // android.os.Parcelable.Creator
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }

                @Override // android.os.Parcelable.Creator
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
            private final int mCurrentView;
            private final int mListPosition;
            private final int mListPositionOffset;
            private final long mMaxDate;
            private final long mMinDate;
            private final int mSelectedDay;
            private final int mSelectedMonth;
            private final int mSelectedYear;

            public SavedState(Parcelable superState, int year, int month, int day, long minDate, long maxDate) {
                this(superState, year, month, day, minDate, maxDate, 0, 0, 0);
            }

            public SavedState(Parcelable superState, int year, int month, int day, long minDate, long maxDate, int currentView, int listPosition, int listPositionOffset) {
                super(superState);
                this.mSelectedYear = year;
                this.mSelectedMonth = month;
                this.mSelectedDay = day;
                this.mMinDate = minDate;
                this.mMaxDate = maxDate;
                this.mCurrentView = currentView;
                this.mListPosition = listPosition;
                this.mListPositionOffset = listPositionOffset;
            }

            private SavedState(Parcel in) {
                super(in);
                this.mSelectedYear = in.readInt();
                this.mSelectedMonth = in.readInt();
                this.mSelectedDay = in.readInt();
                this.mMinDate = in.readLong();
                this.mMaxDate = in.readLong();
                this.mCurrentView = in.readInt();
                this.mListPosition = in.readInt();
                this.mListPositionOffset = in.readInt();
            }

            @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(this.mSelectedYear);
                dest.writeInt(this.mSelectedMonth);
                dest.writeInt(this.mSelectedDay);
                dest.writeLong(this.mMinDate);
                dest.writeLong(this.mMaxDate);
                dest.writeInt(this.mCurrentView);
                dest.writeInt(this.mListPosition);
                dest.writeInt(this.mListPositionOffset);
            }

            public int getSelectedDay() {
                return this.mSelectedDay;
            }

            public int getSelectedMonth() {
                return this.mSelectedMonth;
            }

            public int getSelectedYear() {
                return this.mSelectedYear;
            }

            public long getMinDate() {
                return this.mMinDate;
            }

            public long getMaxDate() {
                return this.mMaxDate;
            }

            public int getCurrentView() {
                return this.mCurrentView;
            }

            public int getListPosition() {
                return this.mListPosition;
            }

            public int getListPositionOffset() {
                return this.mListPositionOffset;
            }
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        structure.setAutofillId(getAutofillId());
        onProvideAutofillStructure(structure, flags);
    }

    @Override // android.view.View
    public void autofill(AutofillValue value) {
        if (isEnabled()) {
            this.mDelegate.autofill(value);
        }
    }

    @Override // android.view.View
    public int getAutofillType() {
        return isEnabled() ? 4 : 0;
    }

    @Override // android.view.View
    public AutofillValue getAutofillValue() {
        if (isEnabled()) {
            return this.mDelegate.getAutofillValue();
        }
        return null;
    }
}
