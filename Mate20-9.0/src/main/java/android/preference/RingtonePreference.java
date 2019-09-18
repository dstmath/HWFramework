package android.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.R;

public class RingtonePreference extends Preference implements PreferenceManager.OnActivityResultListener {
    private static final String TAG = "RingtonePreference";
    private int mRequestCode;
    private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference, defStyleAttr, defStyleRes);
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            this.mRingtoneType = a.getInt(0, 1);
        } else if (getKey() != null && getKey().equals("ringtone1")) {
            this.mRingtoneType = 1;
        } else if (getKey() == null || !getKey().equals("ringtone2")) {
            this.mRingtoneType = a.getInt(0, 1);
        } else {
            this.mRingtoneType = 8;
        }
        this.mShowDefault = a.getBoolean(1, true);
        this.mShowSilent = a.getBoolean(2, true);
        a.recycle();
    }

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842899);
    }

    public RingtonePreference(Context context) {
        this(context, null);
    }

    public int getRingtoneType() {
        return this.mRingtoneType;
    }

    public void setRingtoneType(int type) {
        this.mRingtoneType = type;
    }

    public boolean getShowDefault() {
        return this.mShowDefault;
    }

    public void setShowDefault(boolean showDefault) {
        this.mShowDefault = showDefault;
    }

    public boolean getShowSilent() {
        return this.mShowSilent;
    }

    public void setShowSilent(boolean showSilent) {
        this.mShowSilent = showSilent;
    }

    /* access modifiers changed from: protected */
    public void onClick() {
        Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            intent.putExtra("SUBNUM", getKey());
        }
        onPrepareRingtonePickerIntent(intent);
        PreferenceFragment owningFragment = getPreferenceManager().getFragment();
        if (owningFragment != null) {
            owningFragment.startActivityForResult(intent, this.mRequestCode);
        } else {
            getPreferenceManager().getActivity().startActivityForResult(intent, this.mRequestCode);
        }
    }

    /* access modifiers changed from: protected */
    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", onRestoreRingtone());
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", this.mShowDefault);
        if (this.mShowDefault) {
            ringtonePickerIntent.putExtra("android.intent.extra.ringtone.DEFAULT_URI", RingtoneManager.getDefaultUri(getRingtoneType()));
        }
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", this.mShowSilent);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.TYPE", this.mRingtoneType);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.TITLE", getTitle());
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.AUDIO_ATTRIBUTES_FLAGS", 64);
    }

    /* access modifiers changed from: protected */
    public void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
    }

    /* access modifiers changed from: protected */
    public Uri onRestoreRingtone() {
        String uriString = getPersistedString(null);
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /* access modifiers changed from: protected */
    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        if (!restorePersistedValue && !TextUtils.isEmpty(defaultValue)) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        preferenceManager.registerOnActivityResultListener(this);
        this.mRequestCode = preferenceManager.getNextRequestCode();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != this.mRequestCode) {
            return false;
        }
        if (data != null) {
            Uri uri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
            if (callChangeListener(uri != null ? uri.toString() : "")) {
                onSaveRingtone(uri);
            }
        }
        return true;
    }
}
