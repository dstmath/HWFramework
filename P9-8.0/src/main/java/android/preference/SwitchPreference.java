package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.android.internal.R;

public class SwitchPreference extends TwoStatePreference {
    private final Listener mListener;
    private CharSequence mSwitchOff;
    private CharSequence mSwitchOn;

    private class Listener implements OnCheckedChangeListener {
        /* synthetic */ Listener(SwitchPreference this$0, Listener -this1) {
            this();
        }

        private Listener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (SwitchPreference.this.callChangeListener(Boolean.valueOf(isChecked))) {
                SwitchPreference.this.setChecked(isChecked);
            } else {
                buttonView.setChecked(isChecked ^ 1);
            }
        }
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mListener = new Listener(this, null);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(0));
        setSummaryOff(a.getString(1));
        setSwitchTextOn(a.getString(3));
        setSwitchTextOff(a.getString(4));
        setDisableDependentsState(a.getBoolean(2, false));
        a.recycle();
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    public SwitchPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(android.R.id.switch_widget);
        if (checkableView != null && (checkableView instanceof Checkable)) {
            if (checkableView instanceof Switch) {
                ((Switch) checkableView).setOnCheckedChangeListener(null);
            }
            ((Checkable) checkableView).setChecked(this.mChecked);
            if (checkableView instanceof Switch) {
                Switch switchView = (Switch) checkableView;
                switchView.setTextOn(this.mSwitchOn);
                switchView.setTextOff(this.mSwitchOff);
                switchView.setOnCheckedChangeListener(this.mListener);
            }
        }
        syncSummaryView(view);
    }

    public void setSwitchTextOn(CharSequence onText) {
        this.mSwitchOn = onText;
        notifyChanged();
    }

    public void setSwitchTextOff(CharSequence offText) {
        this.mSwitchOff = offText;
        notifyChanged();
    }

    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    public CharSequence getSwitchTextOn() {
        return this.mSwitchOn;
    }

    public CharSequence getSwitchTextOff() {
        return this.mSwitchOff;
    }
}
