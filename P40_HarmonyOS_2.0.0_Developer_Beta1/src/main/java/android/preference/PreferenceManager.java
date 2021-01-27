package android.preference;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.SettingsStringUtil;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

@Deprecated
public class PreferenceManager {
    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    public static final String METADATA_KEY_PREFERENCES = "android.preference";
    private static final int STORAGE_CREDENTIAL_PROTECTED = 2;
    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;
    private static final String TAG = "PreferenceManager";
    private Activity mActivity;
    @UnsupportedAppUsage
    private List<OnActivityDestroyListener> mActivityDestroyListeners;
    private List<OnActivityResultListener> mActivityResultListeners;
    private List<OnActivityStopListener> mActivityStopListeners;
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private PreferenceFragment mFragment;
    private long mNextId = 0;
    private int mNextRequestCode;
    private boolean mNoCommit;
    @UnsupportedAppUsage
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private PreferenceDataStore mPreferenceDataStore;
    private PreferenceScreen mPreferenceScreen;
    private List<DialogInterface> mPreferencesScreens;
    @UnsupportedAppUsage
    private SharedPreferences mSharedPreferences;
    private int mSharedPreferencesMode;
    private String mSharedPreferencesName;
    private int mStorage = 0;

    @Deprecated
    public interface OnActivityDestroyListener {
        void onActivityDestroy();
    }

    @Deprecated
    public interface OnActivityResultListener {
        boolean onActivityResult(int i, int i2, Intent intent);
    }

    @Deprecated
    public interface OnActivityStopListener {
        void onActivityStop();
    }

    @Deprecated
    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);
    }

    @UnsupportedAppUsage
    public PreferenceManager(Activity activity, int firstRequestCode) {
        this.mActivity = activity;
        this.mNextRequestCode = firstRequestCode;
        init(activity);
    }

    @UnsupportedAppUsage
    PreferenceManager(Context context) {
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void setFragment(PreferenceFragment fragment) {
        this.mFragment = fragment;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public PreferenceFragment getFragment() {
        return this.mFragment;
    }

    public void setPreferenceDataStore(PreferenceDataStore dataStore) {
        this.mPreferenceDataStore = dataStore;
    }

    public PreferenceDataStore getPreferenceDataStore() {
        return this.mPreferenceDataStore;
    }

    private List<ResolveInfo> queryIntentActivities(Intent queryIntent) {
        return this.mContext.getPackageManager().queryIntentActivities(queryIntent, 128);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public PreferenceScreen inflateFromIntent(Intent queryIntent, PreferenceScreen rootPreferences) {
        List<ResolveInfo> activities = queryIntentActivities(queryIntent);
        HashSet<String> inflatedRes = new HashSet<>();
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityInfo activityInfo = activities.get(i).activityInfo;
            Bundle metaData = activityInfo.metaData;
            if (metaData != null && metaData.containsKey(METADATA_KEY_PREFERENCES)) {
                String uniqueResId = activityInfo.packageName + SettingsStringUtil.DELIMITER + activityInfo.metaData.getInt(METADATA_KEY_PREFERENCES);
                if (!inflatedRes.contains(uniqueResId)) {
                    inflatedRes.add(uniqueResId);
                    try {
                        Context context = this.mContext.createPackageContext(activityInfo.packageName, 0);
                        PreferenceInflater inflater = new PreferenceInflater(context, this);
                        XmlResourceParser parser = activityInfo.loadXmlMetaData(context.getPackageManager(), METADATA_KEY_PREFERENCES);
                        rootPreferences = (PreferenceScreen) inflater.inflate((XmlPullParser) parser, (XmlResourceParser) rootPreferences, true);
                        parser.close();
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, "Could not create context for " + activityInfo.packageName + ": " + Log.getStackTraceString(e));
                    }
                }
            }
        }
        rootPreferences.onAttachedToHierarchy(this);
        return rootPreferences;
    }

    @UnsupportedAppUsage
    public PreferenceScreen inflateFromResource(Context context, int resId, PreferenceScreen rootPreferences) {
        setNoCommit(true);
        PreferenceScreen rootPreferences2 = (PreferenceScreen) new PreferenceInflater(context, this).inflate(resId, (int) rootPreferences, true);
        rootPreferences2.onAttachedToHierarchy(this);
        setNoCommit(false);
        return rootPreferences2;
    }

    public PreferenceScreen createPreferenceScreen(Context context) {
        PreferenceScreen preferenceScreen = new PreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    /* access modifiers changed from: package-private */
    public long getNextId() {
        long j;
        synchronized (this) {
            j = this.mNextId;
            this.mNextId = 1 + j;
        }
        return j;
    }

    public String getSharedPreferencesName() {
        return this.mSharedPreferencesName;
    }

    public void setSharedPreferencesName(String sharedPreferencesName) {
        this.mSharedPreferencesName = sharedPreferencesName;
        this.mSharedPreferences = null;
    }

    public int getSharedPreferencesMode() {
        return this.mSharedPreferencesMode;
    }

    public void setSharedPreferencesMode(int sharedPreferencesMode) {
        this.mSharedPreferencesMode = sharedPreferencesMode;
        this.mSharedPreferences = null;
    }

    public void setStorageDefault() {
        this.mStorage = 0;
        this.mSharedPreferences = null;
    }

    public void setStorageDeviceProtected() {
        this.mStorage = 1;
        this.mSharedPreferences = null;
    }

    @SystemApi
    public void setStorageCredentialProtected() {
        this.mStorage = 2;
        this.mSharedPreferences = null;
    }

    public boolean isStorageDefault() {
        return this.mStorage == 0;
    }

    public boolean isStorageDeviceProtected() {
        return this.mStorage == 1;
    }

    @SystemApi
    public boolean isStorageCredentialProtected() {
        return this.mStorage == 2;
    }

    public SharedPreferences getSharedPreferences() {
        Context storageContext;
        if (this.mPreferenceDataStore != null) {
            return null;
        }
        if (this.mSharedPreferences == null) {
            int i = this.mStorage;
            if (i == 1) {
                storageContext = this.mContext.createDeviceProtectedStorageContext();
            } else if (i != 2) {
                storageContext = this.mContext;
            } else {
                storageContext = this.mContext.createCredentialProtectedStorageContext();
            }
            this.mSharedPreferences = storageContext.getSharedPreferences(this.mSharedPreferencesName, this.mSharedPreferencesMode);
        }
        return this.mSharedPreferences;
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode());
    }

    public static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceScreen;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean setPreferences(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == this.mPreferenceScreen) {
            return false;
        }
        this.mPreferenceScreen = preferenceScreen;
        return true;
    }

    public Preference findPreference(CharSequence key) {
        PreferenceScreen preferenceScreen = this.mPreferenceScreen;
        if (preferenceScreen == null) {
            return null;
        }
        return preferenceScreen.findPreference(key);
    }

    public static void setDefaultValues(Context context, int resId, boolean readAgain) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain);
    }

    public static void setDefaultValues(Context context, String sharedPreferencesName, int sharedPreferencesMode, int resId, boolean readAgain) {
        SharedPreferences defaultValueSp = context.getSharedPreferences(KEY_HAS_SET_DEFAULT_VALUES, 0);
        if (readAgain || !defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager pm = new PreferenceManager(context);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);
            SharedPreferences.Editor editor = defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true);
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public SharedPreferences.Editor getEditor() {
        if (this.mPreferenceDataStore != null) {
            return null;
        }
        if (!this.mNoCommit) {
            return getSharedPreferences().edit();
        }
        if (this.mEditor == null) {
            this.mEditor = getSharedPreferences().edit();
        }
        return this.mEditor;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean shouldCommit() {
        return !this.mNoCommit;
    }

    @UnsupportedAppUsage
    private void setNoCommit(boolean noCommit) {
        SharedPreferences.Editor editor;
        if (!noCommit && (editor = this.mEditor) != null) {
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                this.mEditor.commit();
            }
        }
        this.mNoCommit = noCommit;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public Activity getActivity() {
        return this.mActivity;
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void registerOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners == null) {
                this.mActivityResultListeners = new ArrayList();
            }
            if (!this.mActivityResultListeners.contains(listener)) {
                this.mActivityResultListeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void unregisterOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners != null) {
                this.mActivityResultListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        List<OnActivityResultListener> list;
        synchronized (this) {
            if (this.mActivityResultListeners != null) {
                list = new ArrayList<>(this.mActivityResultListeners);
            } else {
                return;
            }
        }
        int N = list.size();
        int i = 0;
        while (i < N && !list.get(i).onActivityResult(requestCode, resultCode, data)) {
            i++;
        }
    }

    @UnsupportedAppUsage
    public void registerOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (this.mActivityStopListeners == null) {
                this.mActivityStopListeners = new ArrayList();
            }
            if (!this.mActivityStopListeners.contains(listener)) {
                this.mActivityStopListeners.add(listener);
            }
        }
    }

    @UnsupportedAppUsage
    public void unregisterOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (this.mActivityStopListeners != null) {
                this.mActivityStopListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityStop() {
        List<OnActivityStopListener> list;
        synchronized (this) {
            if (this.mActivityStopListeners != null) {
                list = new ArrayList<>(this.mActivityStopListeners);
            } else {
                return;
            }
        }
        int N = list.size();
        for (int i = 0; i < N; i++) {
            list.get(i).onActivityStop();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void registerOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners == null) {
                this.mActivityDestroyListeners = new ArrayList();
            }
            if (!this.mActivityDestroyListeners.contains(listener)) {
                this.mActivityDestroyListeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                this.mActivityDestroyListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityDestroy() {
        List<OnActivityDestroyListener> list = null;
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                list = new ArrayList<>(this.mActivityDestroyListeners);
            }
        }
        if (list != null) {
            int N = list.size();
            for (int i = 0; i < N; i++) {
                list.get(i).onActivityDestroy();
            }
        }
        dismissAllScreens();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public int getNextRequestCode() {
        int i;
        synchronized (this) {
            i = this.mNextRequestCode;
            this.mNextRequestCode = i + 1;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void addPreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            if (this.mPreferencesScreens == null) {
                this.mPreferencesScreens = new ArrayList();
            }
            this.mPreferencesScreens.add(screen);
        }
    }

    /* access modifiers changed from: package-private */
    public void removePreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            if (this.mPreferencesScreens != null) {
                this.mPreferencesScreens.remove(screen);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchNewIntent(Intent intent) {
        dismissAllScreens();
    }

    private void dismissAllScreens() {
        ArrayList<DialogInterface> screensToDismiss;
        synchronized (this) {
            if (this.mPreferencesScreens != null) {
                screensToDismiss = new ArrayList<>(this.mPreferencesScreens);
                this.mPreferencesScreens.clear();
            } else {
                return;
            }
        }
        for (int i = screensToDismiss.size() - 1; i >= 0; i--) {
            screensToDismiss.get(i).dismiss();
        }
    }

    /* access modifiers changed from: package-private */
    public void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        this.mOnPreferenceTreeClickListener = listener;
    }

    /* access modifiers changed from: package-private */
    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return this.mOnPreferenceTreeClickListener;
    }
}
