package android.preference;

import android.R;
import android.content.Context;
import android.util.AttributeSet;

public class PreferenceCategory extends PreferenceGroup {
    private static final String TAG = "PreferenceCategory";

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceCategoryStyle);
    }

    public PreferenceCategory(Context context) {
        this(context, null);
    }

    protected boolean onPrepareAddPreference(Preference preference) {
        if (!(preference instanceof PreferenceCategory)) {
            return super.onPrepareAddPreference(preference);
        }
        throw new IllegalArgumentException("Cannot add a PreferenceCategory directly to a PreferenceCategory");
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean shouldDisableDependents() {
        return !super.isEnabled();
    }
}
