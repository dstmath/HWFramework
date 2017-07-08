package android.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.PreferenceManager.OnPreferenceTreeClickListener;
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
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

public class Preference implements Comparable<Preference> {
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    private boolean mBaseMethodCalled;
    private boolean mCanRecycleLayout;
    private Context mContext;
    private Object mDefaultValue;
    private String mDependencyKey;
    private boolean mDependencyMet;
    private List<Preference> mDependents;
    private boolean mEnabled;
    private Bundle mExtras;
    private String mFragment;
    private Drawable mIcon;
    private int mIconResId;
    private long mId;
    private Intent mIntent;
    private String mKey;
    private int mLayoutResId;
    private OnPreferenceChangeInternalListener mListener;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private int mOrder;
    private boolean mParentDependencyMet;
    private boolean mPersistent;
    private int mPreferenceId;
    private PreferenceManager mPreferenceManager;
    private boolean mRequiresKey;
    private boolean mSelectable;
    private boolean mShouldDisableView;
    private CharSequence mSummary;
    private CharSequence mTitle;
    private int mTitleRes;
    private int mWidgetLayoutResId;

    public static class BaseSavedState extends AbsSavedState {
        public static final Creator<BaseSavedState> CREATOR = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.preference.Preference.BaseSavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.preference.Preference.BaseSavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.preference.Preference.BaseSavedState.<clinit>():void");
        }

        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
    }

    interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(Preference preference);

        void onPreferenceHierarchyChange(Preference preference);
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object obj);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }

    protected boolean persistInt(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.preference.Preference.persistInt(int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.preference.Preference.persistInt(int):boolean");
    }

    protected boolean persistLong(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.preference.Preference.persistLong(long):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.preference.Preference.persistLong(long):boolean");
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int i;
        this.mOrder = DEFAULT_ORDER;
        this.mEnabled = true;
        this.mSelectable = true;
        this.mPersistent = true;
        this.mDependencyMet = true;
        this.mParentDependencyMet = true;
        this.mShouldDisableView = true;
        this.mPreferenceId = -1;
        this.mLayoutResId = 17367204;
        this.mCanRecycleLayout = true;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        for (i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            switch (attr) {
                case TextToSpeech.SUCCESS /*0*/:
                    this.mIconResId = a.getResourceId(attr, 0);
                    break;
                case AudioState.ROUTE_EARPIECE /*1*/:
                    this.mPersistent = a.getBoolean(attr, this.mPersistent);
                    break;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    this.mEnabled = a.getBoolean(attr, true);
                    break;
                case Engine.DEFAULT_STREAM /*3*/:
                    this.mLayoutResId = a.getResourceId(attr, this.mLayoutResId);
                    break;
                case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                    this.mTitleRes = a.getResourceId(attr, 0);
                    this.mTitle = a.getString(attr);
                    break;
                case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                    this.mSelectable = a.getBoolean(attr, true);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                    this.mKey = a.getString(attr);
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                    this.mSummary = a.getString(attr);
                    break;
                case AudioState.ROUTE_SPEAKER /*8*/:
                    this.mOrder = a.getInt(attr, this.mOrder);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                    this.mWidgetLayoutResId = a.getResourceId(attr, this.mWidgetLayoutResId);
                    break;
                case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                    this.mDependencyKey = a.getString(attr);
                    break;
                case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
                    this.mDefaultValue = onGetDefaultValue(a, attr);
                    break;
                case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                    this.mShouldDisableView = a.getBoolean(attr, this.mShouldDisableView);
                    break;
                case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                    this.mFragment = a.getString(attr);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
        TypedArray ahwext = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.Preference, defStyleAttr, defStyleRes);
        for (i = ahwext.getIndexCount() - 1; i >= 0; i--) {
            attr = ahwext.getIndex(i);
            switch (attr) {
                case TextToSpeech.SUCCESS /*0*/:
                    this.mPreferenceId = ahwext.getResourceId(attr, -1);
                    break;
                default:
                    break;
            }
        }
        ahwext.recycle();
        if (!getClass().getName().startsWith(PreferenceManager.METADATA_KEY_PREFERENCES) && !getClass().getName().startsWith("com.android")) {
            this.mCanRecycleLayout = false;
        }
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public Preference(Context context) {
        this(context, null);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
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
            this.mCanRecycleLayout = false;
        }
        this.mLayoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return this.mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != this.mWidgetLayoutResId) {
            this.mCanRecycleLayout = false;
        }
        this.mWidgetLayoutResId = widgetLayoutResId;
    }

    public int getWidgetLayoutResource() {
        return this.mWidgetLayoutResId;
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
            if (this.mPreferenceId != -1) {
                convertView.setId(this.mPreferenceId);
            }
        }
        onBindView(convertView);
        return convertView;
    }

    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(this.mLayoutResId, parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (this.mWidgetLayoutResId != 0) {
                layoutInflater.inflate(this.mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        return layout;
    }

    protected void onBindView(View view) {
        int i = 0;
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        if (titleView != null) {
            CharSequence title = getTitle();
            if (TextUtils.isEmpty(title)) {
                titleView.setVisibility(8);
            } else {
                titleView.setText(title);
                titleView.setVisibility(0);
            }
        }
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
            CharSequence summary = getSummary();
            if (TextUtils.isEmpty(summary)) {
                summaryView.setVisibility(8);
            } else {
                summaryView.setText(summary);
                summaryView.setVisibility(0);
            }
        }
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        if (imageView != null) {
            int i2;
            if (!(this.mIconResId == 0 && this.mIcon == null)) {
                if (this.mIcon == null) {
                    this.mIcon = getContext().getDrawable(this.mIconResId);
                }
                if (this.mIcon != null) {
                    imageView.setImageDrawable(this.mIcon);
                }
            }
            if (this.mIcon != null) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            imageView.setVisibility(i2);
        }
        View imageFrame = view.findViewById(android.R.id.icon_frame);
        if (imageFrame != null) {
            if (this.mIcon == null) {
                i = 8;
            }
            imageFrame.setVisibility(i);
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
        if (title != null || this.mTitle == null) {
            if (title == null) {
                return;
            }
            if (title.equals(this.mTitle)) {
                return;
            }
        }
        this.mTitleRes = 0;
        this.mTitle = title;
        notifyChanged();
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
        if (icon != null || this.mIcon == null) {
            if (icon == null || this.mIcon == icon) {
                return;
            }
        }
        this.mIcon = icon;
        notifyChanged();
    }

    public void setIcon(int iconResId) {
        if (this.mIconResId != iconResId) {
            this.mIconResId = iconResId;
            setIcon(this.mContext.getDrawable(iconResId));
        }
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void setSummary(CharSequence summary) {
        if (summary != null || this.mSummary == null) {
            if (summary == null) {
                return;
            }
            if (summary.equals(this.mSummary)) {
                return;
            }
        }
        this.mSummary = summary;
        notifyChanged();
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
        return (this.mEnabled && this.mDependencyMet) ? this.mParentDependencyMet : false;
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

    long getId() {
        return this.mId;
    }

    public void setPreferenceId(int id) {
        this.mPreferenceId = id;
    }

    public int getPreferenceId() {
        return this.mPreferenceId;
    }

    protected void onClick() {
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

    void requireKey() {
        if (this.mKey == null) {
            throw new IllegalStateException("Preference does not have a key assigned.");
        }
        this.mRequiresKey = true;
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(this.mKey);
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    protected boolean shouldPersist() {
        return (this.mPreferenceManager == null || !isPersistent()) ? false : hasKey();
    }

    public void setPersistent(boolean persistent) {
        this.mPersistent = persistent;
    }

    protected boolean callChangeListener(Object newValue) {
        return this.mOnChangeListener == null ? true : this.mOnChangeListener.onPreferenceChange(this, newValue);
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

    public void performClick(PreferenceScreen preferenceScreen) {
        if (isEnabled()) {
            onClick();
            if (this.mOnClickListener == null || !this.mOnClickListener.onPreferenceClick(this)) {
                PreferenceManager preferenceManager = getPreferenceManager();
                if (preferenceManager != null) {
                    OnPreferenceTreeClickListener listener = preferenceManager.getOnPreferenceTreeClickListener();
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

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getSharedPreferences();
    }

    public Editor getEditor() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getEditor();
    }

    public boolean shouldCommit() {
        if (this.mPreferenceManager == null) {
            return false;
        }
        return this.mPreferenceManager.shouldCommit();
    }

    public /* bridge */ /* synthetic */ int compareTo(Object another) {
        return compareTo((Preference) another);
    }

    public int compareTo(Preference another) {
        if (this.mOrder != another.mOrder) {
            return this.mOrder - another.mOrder;
        }
        if (this.mTitle == another.mTitle) {
            return 0;
        }
        if (this.mTitle == null) {
            return 1;
        }
        if (another.mTitle == null) {
            return -1;
        }
        return CharSequences.compareToIgnoreCase(this.mTitle, another.mTitle);
    }

    final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        this.mListener = listener;
    }

    protected void notifyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceChange(this);
        }
    }

    protected void notifyHierarchyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceHierarchyChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        this.mId = preferenceManager.getNextId();
        dispatchSetInitialValue();
    }

    protected void onAttachedToActivity() {
        registerDependency();
    }

    private void registerDependency() {
        if (!TextUtils.isEmpty(this.mDependencyKey)) {
            Preference preference = findPreferenceInHierarchy(this.mDependencyKey);
            if (preference != null) {
                preference.registerDependent(this);
                return;
            }
            throw new IllegalStateException("Dependency \"" + this.mDependencyKey + "\" not found for preference \"" + this.mKey + "\" (title: \"" + this.mTitle + "\"");
        }
    }

    private void unregisterDependency() {
        if (this.mDependencyKey != null) {
            Preference oldDependency = findPreferenceInHierarchy(this.mDependencyKey);
            if (oldDependency != null) {
                oldDependency.unregisterDependent(this);
            }
        }
    }

    protected Preference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void registerDependent(Preference dependent) {
        if (this.mDependents == null) {
            this.mDependents = new ArrayList();
        }
        this.mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        if (this.mDependents != null) {
            this.mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        List<Preference> dependents = this.mDependents;
        if (dependents != null) {
            int dependentsCount = dependents.size();
            for (int i = 0; i < dependentsCount; i++) {
                ((Preference) dependents.get(i)).onDependencyChanged(this, disableDependents);
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

    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (shouldPersist() && getSharedPreferences().contains(this.mKey)) {
            onSetInitialValue(true, null);
        } else if (this.mDefaultValue != null) {
            onSetInitialValue(false, this.mDefaultValue);
        }
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    private void tryCommit(Editor editor) {
        if (this.mPreferenceManager.shouldCommit()) {
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    protected boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }
        if (TextUtils.equals(value, getPersistedString(null))) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putString(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected String getPersistedString(String defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getString(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    public boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }
        if (values.equals(getPersistedStringSet(null))) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putStringSet(this.mKey, values);
        tryCommit(editor);
        return true;
    }

    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getStringSet(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    protected int getPersistedInt(int defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getInt(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    protected boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedFloat(Float.NaN)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putFloat(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected float getPersistedFloat(float defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getFloat(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    protected long getPersistedLong(long defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getLong(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    protected boolean persistBoolean(boolean value) {
        boolean z = false;
        if (!shouldPersist()) {
            return false;
        }
        if (!value) {
            z = true;
        }
        if (value == getPersistedBoolean(z)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putBoolean(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (shouldPersist()) {
            return this.mPreferenceManager.getSharedPreferences().getBoolean(this.mKey, defaultReturnValue);
        }
        return defaultReturnValue;
    }

    boolean canRecycleLayout() {
        return this.mCanRecycleLayout;
    }

    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    void dispatchSaveInstanceState(Bundle container) {
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

    protected Parcelable onSaveInstanceState() {
        this.mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    void dispatchRestoreInstanceState(Bundle container) {
        if (hasKey()) {
            Parcelable state = container.getParcelable(this.mKey);
            if (state != null) {
                this.mBaseMethodCalled = false;
                onRestoreInstanceState(state);
                if (!this.mBaseMethodCalled) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        this.mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }
}
