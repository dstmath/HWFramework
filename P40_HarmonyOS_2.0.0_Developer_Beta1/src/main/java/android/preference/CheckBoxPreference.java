package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import com.android.internal.R;

@Deprecated
public class CheckBoxPreference extends TwoStatePreference {
    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(0));
        setSummaryOff(a.getString(1));
        setDisableDependentsState(a.getBoolean(2, false));
        a.recycle();
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }

    public CheckBoxPreference(Context context) {
        this(context, null);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onBindView(View view) {
        super.onBindView(view);
        View checkboxView = view.findViewById(16908289);
        if (checkboxView != null && (checkboxView instanceof Checkable)) {
            ((Checkable) checkboxView).setChecked(this.mChecked);
        }
        syncSummaryView(view);
    }
}
