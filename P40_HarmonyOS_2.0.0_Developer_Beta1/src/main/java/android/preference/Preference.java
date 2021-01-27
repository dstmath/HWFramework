package android.preference;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.CharSequences;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Deprecated
public class Preference implements Comparable<Preference> {
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    private boolean mBaseMethodCalled;
    private Context mContext;
    private Object mDefaultValue;
    private String mDependencyKey;
    private boolean mDependencyMet;
    private List<Preference> mDependents;
    private boolean mEnabled;
    private Bundle mExtras;
    private String mFragment;
    private boolean mHasSingleLineTitleAttr;
    private Drawable mIcon;
    private int mIconResId;
    private boolean mIconSpaceReserved;
    private long mId;
    private Intent mIntent;
    private String mKey;
    @UnsupportedAppUsage
    private int mLayoutResId;
    private OnPreferenceChangeInternalListener mListener;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private int mOrder;
    private boolean mParentDependencyMet;
    private PreferenceGroup mParentGroup;
    private boolean mPersistent;
    private PreferenceDataStore mPreferenceDataStore;
    private int mPreferenceId;
    private PreferenceManager mPreferenceManager;
    private boolean mRecycleEnabled;
    private boolean mRequiresKey;
    private boolean mSelectable;
    private boolean mShouldDisableView;
    private boolean mSingleLineTitle;
    @UnsupportedAppUsage
    private CharSequence mSummary;
    private CharSequence mTitle;
    private int mTitleRes;
    @UnsupportedAppUsage
    private int mWidgetLayoutResId;

    /* access modifiers changed from: package-private */
    public interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(Preference preference);

        void onPreferenceHierarchyChange(Preference preference);
    }

    @Deprecated
    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object obj);
    }

    @Deprecated
    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mOrder = Integer.MAX_VALUE;
        this.mEnabled = true;
        this.mSelectable = true;
        this.mPersistent = true;
        this.mDependencyMet = true;
        this.mParentDependencyMet = true;
        this.mRecycleEnabled = true;
        this.mSingleLineTitle = true;
        this.mShouldDisableView = true;
        this.mPreferenceId = -1;
        this.mLayoutResId = R.layout.preference;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mIconResId = a.getResourceId(attr, 0);
                    break;
                case 1:
                    this.mPersistent = a.getBoolean(attr, this.mPersistent);
                    break;
                case 2:
                    this.mEnabled = a.getBoolean(attr, true);
                    break;
                case 3:
                    this.mLayoutResId = a.getResourceId(attr, this.mLayoutResId);
                    break;
                case 4:
                    this.mTitleRes = a.getResourceId(attr, 0);
                    this.mTitle = a.getText(attr);
                    break;
                case 5:
                    this.mSelectable = a.getBoolean(attr, true);
                    break;
                case 6:
                    this.mKey = a.getString(attr);
                    break;
                case 7:
                    this.mSummary = a.getText(attr);
                    break;
                case 8:
                    this.mOrder = a.getInt(attr, this.mOrder);
                    break;
                case 9:
                    this.mWidgetLayoutResId = a.getResourceId(attr, this.mWidgetLayoutResId);
                    break;
                case 10:
                    this.mDependencyKey = a.getString(attr);
                    break;
                case 11:
                    this.mDefaultValue = onGetDefaultValue(a, attr);
                    break;
                case 12:
                    this.mShouldDisableView = a.getBoolean(attr, this.mShouldDisableView);
                    break;
                case 13:
                    this.mFragment = a.getString(attr);
                    break;
                case 14:
                    this.mRecycleEnabled = a.getBoolean(attr, this.mRecycleEnabled);
                    break;
                case 15:
                    this.mSingleLineTitle = a.getBoolean(attr, this.mSingleLineTitle);
                    this.mHasSingleLineTitleAttr = true;
                    break;
                case 16:
                    this.mIconSpaceReserved = a.getBoolean(attr, this.mIconSpaceReserved);
                    break;
            }
        }
        a.recycle();
        TypedArray ahwext = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i2 = ahwext.getIndexCount() - 1; i2 >= 0; i2--) {
            int attr2 = ahwext.getIndex(i2);
            if (attr2 == 0) {
                this.mPreferenceId = ahwext.getResourceId(attr2, -1);
            }
        }
        ahwext.recycle();
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842894);
    }

    public Preference(Context context) {
        this(context, null);
    }

    /* access modifiers changed from: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void setFragment(String fragment) {
        this.mFragment = fragment;
    }

    public String getFragment() {
        return this.mFragment;
    }

    public void setPreferenceDataStore(PreferenceDataStore dataStore) {
        this.mPreferenceDataStore = dataStore;
    }

    public PreferenceDataStore getPreferenceDataStore() {
        PreferenceDataStore preferenceDataStore = this.mPreferenceDataStore;
        if (preferenceDataStore != null) {
            return preferenceDataStore;
        }
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager != null) {
            return preferenceManager.getPreferenceDataStore();
        }
        return null;
    }

    public Bundle getExtras() {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        return this.mExtras;
    }

    public Bundle peekExtras() {
        return this.mExtras;
    }

    public void setLayoutResource(int layoutResId) {
        if (layoutResId != this.mLayoutResId) {
            this.mRecycleEnabled = false;
        }
        this.mLayoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return this.mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != this.mWidgetLayoutResId) {
            this.mRecycleEnabled = false;
        }
        this.mWidgetLayoutResId = widgetLayoutResId;
    }

    public int getWidgetLayoutResource() {
        return this.mWidgetLayoutResId;
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
            int i = this.mPreferenceId;
            if (i != -1) {
                convertView.setId(i);
            }
        }
        onBindView(convertView);
        return convertView;
    }

    /* access modifiers changed from: protected */
    public View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(this.mLayoutResId, parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (widgetFrame != null) {
            int i = this.mWidgetLayoutResId;
            if (i != 0) {
                layoutInflater.inflate(i, widgetFrame);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        return layout;
    }

    /* access modifiers changed from: protected */
    public void onBindView(View view) {
        TextView titleView = (TextView) view.findViewById(16908310);
        int i = 8;
        if (titleView != null) {
            CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(0);
                if (this.mHasSingleLineTitleAttr) {
                    titleView.setSingleLine(this.mSingleLineTitle);
                }
            } else {
                titleView.setVisibility(8);
            }
        }
        TextView summaryView = (TextView) view.findViewById(16908304);
        if (summaryView != null) {
            CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(0);
            } else {
                summaryView.setVisibility(8);
            }
        }
        ImageView imageView = (ImageView) view.findViewById(16908294);
        if (imageView != null) {
            if (!(this.mIconResId == 0 && this.mIcon == null)) {
                if (this.mIcon == null) {
                    this.mIcon = getContext().getDrawable(this.mIconResId);
                }
                Drawable drawable = this.mIcon;
                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }
            }
            if (this.mIcon != null) {
                imageView.setVisibility(0);
            } else {
                imageView.setVisibility(this.mIconSpaceReserved ? 4 : 8);
            }
        }
        View imageFrame = view.findViewById(16908350);
        if (imageFrame != null) {
            if (this.mIcon != null) {
                imageFrame.setVisibility(0);
            } else {
                if (this.mIconSpaceReserved) {
                    i = 4;
                }
                imageFrame.setVisibility(i);
            }
        }
        if (this.mShouldDisableView) {
            setEnabledStateOnViews(view, isEnabled());
        }
    }

    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    public void setOrder(int order) {
        if (order != this.mOrder) {
            this.mOrder = order;
            notifyHierarchyChanged();
        }
    }

    public int getOrder() {
        return this.mOrder;
    }

    public void setTitle(CharSequence title) {
        if ((title == null && this.mTitle != null) || (title != null && !title.equals(this.mTitle))) {
            this.mTitleRes = 0;
            this.mTitle = title;
            notifyChanged();
        }
    }

    public void setTitle(int titleResId) {
        setTitle(this.mContext.getString(titleResId));
        this.mTitleRes = titleResId;
    }

    public int getTitleRes() {
        return this.mTitleRes;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && this.mIcon != null) || (icon != null && this.mIcon != icon)) {
            this.mIcon = icon;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        if (this.mIconResId != iconResId) {
            this.mIconResId = iconResId;
            setIcon(this.mContext.getDrawable(iconResId));
        }
    }

    public Drawable getIcon() {
        if (this.mIcon == null && this.mIconResId != 0) {
            this.mIcon = getContext().getDrawable(this.mIconResId);
        }
        return this.mIcon;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void setSummary(CharSequence summary) {
        if ((summary == null && this.mSummary != null) || (summary != null && !summary.equals(this.mSummary))) {
            this.mSummary = summary;
            notifyChanged();
        }
    }

    public void setSummary(int summaryResId) {
        setSummary(this.mContext.getString(summaryResId));
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isEnabled() {
        return this.mEnabled && this.mDependencyMet && this.mParentDependencyMet;
    }

    public void setSelectable(boolean selectable) {
        if (this.mSelectable != selectable) {
            this.mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return this.mSelectable;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        this.mShouldDisableView = shouldDisableView;
        notifyChanged();
    }

    public boolean getShouldDisableView() {
        return this.mShouldDisableView;
    }

    public void setRecycleEnabled(boolean enabled) {
        this.mRecycleEnabled = enabled;
        notifyChanged();
    }

    public boolean isRecycleEnabled() {
        return this.mRecycleEnabled;
    }

    public void setSingleLineTitle(boolean singleLineTitle) {
        this.mHasSingleLineTitleAttr = true;
        this.mSingleLineTitle = singleLineTitle;
        notifyChanged();
    }

    public boolean isSingleLineTitle() {
        return this.mSingleLineTitle;
    }

    public void setIconSpaceReserved(boolean iconSpaceReserved) {
        this.mIconSpaceReserved = iconSpaceReserved;
        notifyChanged();
    }

    public boolean isIconSpaceReserved() {
        return this.mIconSpaceReserved;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public long getId() {
        return this.mId;
    }

    public void setPreferenceId(int id) {
        this.mPreferenceId = id;
    }

    public int getPreferenceId() {
        return this.mPreferenceId;
    }

    /* access modifiers changed from: protected */
    public void onClick() {
    }

    public void setKey(String key) {
        this.mKey = key;
        if (this.mRequiresKey && !hasKey()) {
            requireKey();
        }
    }

    public String getKey() {
        return this.mKey;
    }

    /* access modifiers changed from: package-private */
    public void requireKey() {
        if (this.mKey != null) {
            this.mRequiresKey = true;
            return;
        }
        throw new IllegalStateException("Preference does not have a key assigned.");
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(this.mKey);
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    /* access modifiers changed from: protected */
    public boolean shouldPersist() {
        return this.mPreferenceManager != null && isPersistent() && hasKey();
    }

    public void setPersistent(boolean persistent) {
        this.mPersistent = persistent;
    }

    /* access modifiers changed from: protected */
    public boolean callChangeListener(Object newValue) {
        OnPreferenceChangeListener onPreferenceChangeListener = this.mOnChangeListener;
        return onPreferenceChangeListener == null || onPreferenceChangeListener.onPreferenceChange(this, newValue);
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.mOnChangeListener = onPreferenceChangeListener;
    }

    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return this.mOnChangeListener;
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        this.mOnClickListener = onPreferenceClickListener;
    }

    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return this.mOnClickListener;
    }

    @UnsupportedAppUsage
    public void performClick(PreferenceScreen preferenceScreen) {
        if (isEnabled()) {
            onClick();
            OnPreferenceClickListener onPreferenceClickListener = this.mOnClickListener;
            if (onPreferenceClickListener == null || !onPreferenceClickListener.onPreferenceClick(this)) {
                PreferenceManager preferenceManager = getPreferenceManager();
                if (preferenceManager != null) {
                    PreferenceManager.OnPreferenceTreeClickListener listener = preferenceManager.getOnPreferenceTreeClickListener();
                    if (!(preferenceScreen == null || listener == null || !listener.onPreferenceTreeClick(preferenceScreen, this))) {
                        return;
                    }
                }
                if (this.mIntent != null) {
                    getContext().startActivity(this.mIntent);
                }
            }
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mPreferenceManager == null || getPreferenceDataStore() != null) {
            return null;
        }
        return this.mPreferenceManager.getSharedPreferences();
    }

    public SharedPreferences.Editor getEditor() {
        if (this.mPreferenceManager == null || getPreferenceDataStore() != null) {
            return null;
        }
        return this.mPreferenceManager.getEditor();
    }

    public boolean shouldCommit() {
        PreferenceManager preferenceManager = this.mPreferenceManager;
        if (preferenceManager == null) {
            return false;
        }
        return preferenceManager.shouldCommit();
    }

    public int compareTo(Preference another) {
        int i = this.mOrder;
        int i2 = another.mOrder;
        if (i != i2) {
            return i - i2;
        }
        CharSequence charSequence = this.mTitle;
        CharSequence charSequence2 = another.mTitle;
        if (charSequence == charSequence2) {
            return 0;
        }
        if (charSequence == null) {
            return 1;
        }
        if (charSequence2 == null) {
            return -1;
        }
        return CharSequences.compareToIgnoreCase(charSequence, charSequence2);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    public void notifyChanged() {
        OnPreferenceChangeInternalListener onPreferenceChangeInternalListener = this.mListener;
        if (onPreferenceChangeInternalListener != null) {
            onPreferenceChangeInternalListener.onPreferenceChange(this);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyHierarchyChanged() {
        OnPreferenceChangeInternalListener onPreferenceChangeInternalListener = this.mListener;
        if (onPreferenceChangeInternalListener != null) {
            onPreferenceChangeInternalListener.onPreferenceHierarchyChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        this.mId = preferenceManager.getNextId();
        dispatchSetInitialValue();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToActivity() {
        registerDependency();
    }

    /* access modifiers changed from: package-private */
    public void assignParent(PreferenceGroup parentGroup) {
        this.mParentGroup = parentGroup;
    }

    private void registerDependency() {
        if (!TextUtils.isEmpty(this.mDependencyKey)) {
            Preference preference = findPreferenceInHierarchy(this.mDependencyKey);
            if (preference != null) {
                preference.registerDependent(this);
                return;
            }
            throw new IllegalStateException("Dependency \"" + this.mDependencyKey + "\" not found for preference \"" + this.mKey + "\" (title: \"" + ((Object) this.mTitle) + "\"");
        }
    }

    private void unregisterDependency() {
        Preference oldDependency;
        String str = this.mDependencyKey;
        if (str != null && (oldDependency = findPreferenceInHierarchy(str)) != null) {
            oldDependency.unregisterDependent(this);
        }
    }

    /* access modifiers changed from: protected */
    public Preference findPreferenceInHierarchy(String key) {
        PreferenceManager preferenceManager;
        if (TextUtils.isEmpty(key) || (preferenceManager = this.mPreferenceManager) == null) {
            return null;
        }
        return preferenceManager.findPreference(key);
    }

    @UnsupportedAppUsage
    private void registerDependent(Preference dependent) {
        if (this.mDependents == null) {
            this.mDependents = new ArrayList();
        }
        this.mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        List<Preference> list = this.mDependents;
        if (list != null) {
            list.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        List<Preference> dependents = this.mDependents;
        if (dependents != null) {
            int dependentsCount = dependents.size();
            for (int i = 0; i < dependentsCount; i++) {
                dependents.get(i).onDependencyChanged(this, disableDependents);
            }
        }
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (this.mDependencyMet == disableDependent) {
            this.mDependencyMet = !disableDependent;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public void onParentChanged(Preference parent, boolean disableChild) {
        if (this.mParentDependencyMet == disableChild) {
            this.mParentDependencyMet = !disableChild;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public void setDependency(String dependencyKey) {
        unregisterDependency();
        this.mDependencyKey = dependencyKey;
        registerDependency();
    }

    public String getDependency() {
        return this.mDependencyKey;
    }

    public PreferenceGroup getParent() {
        return this.mParentGroup;
    }

    /* access modifiers changed from: protected */
    public void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (getPreferenceDataStore() != null) {
            onSetInitialValue(true, this.mDefaultValue);
        } else if (!shouldPersist() || !getSharedPreferences().contains(this.mKey)) {
            Object obj = this.mDefaultValue;
            if (obj != null) {
                onSetInitialValue(false, obj);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }

    /* access modifiers changed from: protected */
    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    private void tryCommit(SharedPreferences.Editor editor) {
        if (this.mPreferenceManager.shouldCommit()) {
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }
        if (TextUtils.equals(value, getPersistedString(null))) {
            return true;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putString(this.mKey, value);
        } else {
            SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
            editor.putString(this.mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getString(this.mKey, defaultReturnValue);
        }
        return this.mPreferenceManager.getSharedPreferences().getString(this.mKey, defaultReturnValue);
    }

    public boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }
        if (values.equals(getPersistedStringSet(null))) {
            return true;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putStringSet(this.mKey, values);
        } else {
            SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
            editor.putStringSet(this.mKey, values);
            tryCommit(editor);
        }
        return true;
    }

    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getStringSet(this.mKey, defaultReturnValue);
        }
        return this.mPreferenceManager.getSharedPreferences().getStringSet(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistInt(int value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedInt(~value)) {
            return true;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putInt(this.mKey, value);
        } else {
            SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
            editor.putInt(this.mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int getPersistedInt(int defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getInt(this.mKey, defaultReturnValue);
        }
        return this.mPreferenceManager.getSharedPreferences().getInt(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedFloat(Float.NaN)) {
            return true;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putFloat(this.mKey, value);
        } else {
            SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
            editor.putFloat(this.mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public float getPersistedFloat(float defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getFloat(this.mKey, defaultReturnValue);
        }
        return this.mPreferenceManager.getSharedPreferences().getFloat(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistLong(long value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedLong(~value)) {
            return true;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putLong(this.mKey, value);
        } else {
            SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
            editor.putLong(this.mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public long getPersistedLong(long defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getLong(this.mKey, defaultReturnValue);
        }
        return this.mPreferenceManager.getSharedPreferences().getLong(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistBoolean(boolean value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedBoolean(!value)) {
            return true;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putBoolean(this.mKey, value);
        } else {
            SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
            editor.putBoolean(this.mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getBoolean(this.mKey, defaultReturnValue);
        }
        return this.mPreferenceManager.getSharedPreferences().getBoolean(this.mKey, defaultReturnValue);
    }

    @Override // java.lang.Object
    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    /* access modifiers changed from: package-private */
    public StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title);
            sb.append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary);
            sb.append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    /* access modifiers changed from: package-private */
    public void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            this.mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!this.mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            } else if (state != null) {
                container.putParcelable(this.mKey, state);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        this.mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    /* access modifiers changed from: package-private */
    public void dispatchRestoreInstanceState(Bundle container) {
        Parcelable state;
        if (hasKey() && (state = container.getParcelable(this.mKey)) != null) {
            this.mBaseMethodCalled = false;
            onRestoreInstanceState(state);
            if (!this.mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        this.mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }

    @Deprecated
    public static class BaseSavedState extends AbsSavedState {
        public static final Parcelable.Creator<BaseSavedState> CREATOR = new Parcelable.Creator<BaseSavedState>() {
            /* class android.preference.Preference.BaseSavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };

        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
    }
}
