package android.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.R;

public class RingtonePreference extends Preference implements OnActivityResultListener {
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
        this(context, attrs, android.R.attr.ringtonePreferenceStyle);
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

    protected void onClick() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
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

    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, onRestoreRingtone());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, this.mShowDefault);
        if (this.mShowDefault) {
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(getRingtoneType()));
        }
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, this.mShowSilent);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, this.mRingtoneType);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_AUDIO_ATTRIBUTES_FLAGS, 64);
    }

    protected void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : ProxyInfo.LOCAL_EXCL_LIST);
    }

    protected Uri onRestoreRingtone() {
        String uriString = getPersistedString(null);
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }
        return Uri.parse(uriString);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        if (!(restorePersistedValue || TextUtils.isEmpty(defaultValue))) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        preferenceManager.registerOnActivityResultListener(this);
        this.mRequestCode = preferenceManager.getNextRequestCode();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != this.mRequestCode) {
            return false;
        }
        if (data != null) {
            Uri uri = (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (callChangeListener(uri != null ? uri.toString() : ProxyInfo.LOCAL_EXCL_LIST)) {
                onSaveRingtone(uri);
            }
        }
        return true;
    }
}
