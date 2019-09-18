package android.preference;

import android.annotation.SystemApi;
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

public class PreferenceManager {
    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    public static final String METADATA_KEY_PREFERENCES = "android.preference";
    private static final int STORAGE_CREDENTIAL_PROTECTED = 2;
    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;
    private static final String TAG = "PreferenceManager";
    private Activity mActivity;
    private List<OnActivityDestroyListener> mActivityDestroyListeners;
    private List<OnActivityResultListener> mActivityResultListeners;
    private List<OnActivityStopListener> mActivityStopListeners;
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private PreferenceFragment mFragment;
    private long mNextId = 0;
    private int mNextRequestCode;
    private boolean mNoCommit;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private PreferenceDataStore mPreferenceDataStore;
    private PreferenceScreen mPreferenceScreen;
    private List<DialogInterface> mPreferencesScreens;
    private SharedPreferences mSharedPreferences;
    private int mSharedPreferencesMode;
    private String mSharedPreferencesName;
    private int mStorage = 0;

    public interface OnActivityDestroyListener {
        void onActivityDestroy();
    }

    public interface OnActivityResultListener {
        boolean onActivityResult(int i, int i2, Intent intent);
    }

    public interface OnActivityStopListener {
        void onActivityStop();
    }

    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);
    }

    public PreferenceManager(Activity activity, int firstRequestCode) {
        this.mActivity = activity;
        this.mNextRequestCode = firstRequestCode;
        init(activity);
    }

    PreferenceManager(Context context) {
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    /* access modifiers changed from: package-private */
    public void setFragment(PreferenceFragment fragment) {
        this.mFragment = fragment;
    }

    /* access modifiers changed from: package-private */
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v3, resolved type: android.preference.PreferenceScreen} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
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
                        rootPreferences = inflater.inflate((XmlPullParser) parser, rootPreferences, true);
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

    public PreferenceScreen inflateFromResource(Context context, int resId, PreferenceScreen rootPreferences) {
        setNoCommit(true);
        PreferenceScreen rootPreferences2 = (PreferenceScreen) new PreferenceInflater(context, this).inflate(resId, rootPreferences, true);
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
            switch (this.mStorage) {
                case 1:
                    storageContext = this.mContext.createDeviceProtectedStorageContext();
                    break;
                case 2:
                    storageContext = this.mContext.createCredentialProtectedStorageContext();
                    break;
                default:
                    storageContext = this.mContext;
                    break;
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
    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceScreen;
    }

    /* access modifiers changed from: package-private */
    public boolean setPreferences(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == this.mPreferenceScreen) {
            return false;
        }
        this.mPreferenceScreen = preferenceScreen;
        return true;
    }

    public Preference findPreference(CharSequence key) {
        if (this.mPreferenceScreen == null) {
            return null;
        }
        return this.mPreferenceScreen.findPreference(key);
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
    public boolean shouldCommit() {
        return !this.mNoCommit;
    }

    private void setNoCommit(boolean noCommit) {
        if (!noCommit && this.mEditor != null) {
            try {
                this.mEditor.apply();
            } catch (AbstractMethodError e) {
                this.mEditor.commit();
            }
        }
        this.mNoCommit = noCommit;
    }

    /* access modifiers changed from: package-private */
    public Activity getActivity() {
        return this.mActivity;
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: package-private */
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
    public void unregisterOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners != null) {
                this.mActivityResultListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0020, code lost:
        if (r0.get(r2).onActivityResult(r5, r6, r7) == false) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        r1 = r0.size();
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        if (r2 >= r1) goto L_0x0026;
     */
    public void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        synchronized (this) {
            if (this.mActivityResultListeners != null) {
                List<OnActivityResultListener> list = new ArrayList<>(this.mActivityResultListeners);
            }
        }
    }

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

    public void unregisterOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (this.mActivityStopListeners != null) {
                this.mActivityStopListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        r0.get(r2).onActivityStop();
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        r1 = r0.size();
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        if (r2 >= r1) goto L_0x0022;
     */
    public void dispatchActivityStop() {
        synchronized (this) {
            if (this.mActivityStopListeners != null) {
                List<OnActivityStopListener> list = new ArrayList<>(this.mActivityStopListeners);
            }
        }
    }

    /* access modifiers changed from: package-private */
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
    public void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                this.mActivityDestroyListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        r0.get(r1).dismiss();
        r1 = r1 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0014, code lost:
        r1 = r0.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        if (r1 < 0) goto L_0x0028;
     */
    private void dismissAllScreens() {
        synchronized (this) {
            if (this.mPreferencesScreens != null) {
                ArrayList<DialogInterface> screensToDismiss = new ArrayList<>(this.mPreferencesScreens);
                this.mPreferencesScreens.clear();
            }
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
