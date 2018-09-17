package android.preference;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.preference.GenericInflater.Parent;
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
    private Editor mEditor;
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

    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);
    }

    public interface OnActivityResultListener {
        boolean onActivityResult(int i, int i2, Intent intent);
    }

    public interface OnActivityStopListener {
        void onActivityStop();
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

    void setFragment(PreferenceFragment fragment) {
        this.mFragment = fragment;
    }

    PreferenceFragment getFragment() {
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

    PreferenceScreen inflateFromIntent(Intent queryIntent, PreferenceScreen rootPreferences) {
        List<ResolveInfo> activities = queryIntentActivities(queryIntent);
        HashSet<String> inflatedRes = new HashSet();
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityInfo activityInfo = ((ResolveInfo) activities.get(i)).activityInfo;
            Bundle metaData = activityInfo.metaData;
            if (metaData != null && (metaData.containsKey(METADATA_KEY_PREFERENCES) ^ 1) == 0) {
                String uniqueResId = activityInfo.packageName + ":" + activityInfo.metaData.getInt(METADATA_KEY_PREFERENCES);
                if (!inflatedRes.contains(uniqueResId)) {
                    inflatedRes.add(uniqueResId);
                    try {
                        Context context = this.mContext.createPackageContext(activityInfo.packageName, 0);
                        PreferenceInflater inflater = new PreferenceInflater(context, this);
                        XmlResourceParser parser = activityInfo.loadXmlMetaData(context.getPackageManager(), METADATA_KEY_PREFERENCES);
                        rootPreferences = (PreferenceScreen) inflater.inflate((XmlPullParser) parser, (Parent) rootPreferences, true);
                        parser.close();
                    } catch (NameNotFoundException e) {
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
        rootPreferences = (PreferenceScreen) new PreferenceInflater(context, this).inflate(resId, (Parent) rootPreferences, true);
        rootPreferences.onAttachedToHierarchy(this);
        setNoCommit(false);
        return rootPreferences;
    }

    public PreferenceScreen createPreferenceScreen(Context context) {
        PreferenceScreen preferenceScreen = new PreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    long getNextId() {
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

    public boolean isStorageCredentialProtected() {
        return this.mStorage == 2;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mPreferenceDataStore != null) {
            return null;
        }
        if (this.mSharedPreferences == null) {
            Context storageContext;
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

    PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceScreen;
    }

    boolean setPreferences(PreferenceScreen preferenceScreen) {
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
        if (readAgain || (defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false) ^ 1) != 0) {
            PreferenceManager pm = new PreferenceManager(context);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);
            Editor editor = defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true);
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    Editor getEditor() {
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

    boolean shouldCommit() {
        return this.mNoCommit ^ 1;
    }

    private void setNoCommit(boolean noCommit) {
        if (!(noCommit || this.mEditor == null)) {
            try {
                this.mEditor.apply();
            } catch (AbstractMethodError e) {
                this.mEditor.commit();
            }
        }
        this.mNoCommit = noCommit;
    }

    Activity getActivity() {
        return this.mActivity;
    }

    Context getContext() {
        return this.mContext;
    }

    void registerOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners == null) {
                this.mActivityResultListeners = new ArrayList();
            }
            if (!this.mActivityResultListeners.contains(listener)) {
                this.mActivityResultListeners.add(listener);
            }
        }
    }

    void unregisterOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners != null) {
                this.mActivityResultListeners.remove(listener);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x000f, code:
            r0 = r2.size();
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:10:0x0014, code:
            if (r1 >= r0) goto L_0x0022;
     */
    /* JADX WARNING: Missing block: B:12:0x0020, code:
            if (((android.preference.PreferenceManager.OnActivityResultListener) r2.get(r1)).onActivityResult(r5, r6, r7) == false) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            return;
     */
    /* JADX WARNING: Missing block: B:17:0x0026, code:
            r1 = r1 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        synchronized (this) {
            if (this.mActivityResultListeners == null) {
                return;
            }
            List<OnActivityResultListener> list = new ArrayList(this.mActivityResultListeners);
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

    /* JADX WARNING: Missing block: B:9:0x000f, code:
            r0 = r2.size();
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:10:0x0014, code:
            if (r1 >= r0) goto L_0x0025;
     */
    /* JADX WARNING: Missing block: B:11:0x0016, code:
            ((android.preference.PreferenceManager.OnActivityStopListener) r2.get(r1)).onActivityStop();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:15:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dispatchActivityStop() {
        synchronized (this) {
            if (this.mActivityStopListeners == null) {
                return;
            }
            List<OnActivityStopListener> list = new ArrayList(this.mActivityStopListeners);
        }
    }

    void registerOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners == null) {
                this.mActivityDestroyListeners = new ArrayList();
            }
            if (!this.mActivityDestroyListeners.contains(listener)) {
                this.mActivityDestroyListeners.add(listener);
            }
        }
    }

    void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                this.mActivityDestroyListeners.remove(listener);
            }
        }
    }

    void dispatchActivityDestroy() {
        List list = null;
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                list = new ArrayList(this.mActivityDestroyListeners);
            }
        }
        if (list != null) {
            int N = list.size();
            for (int i = 0; i < N; i++) {
                ((OnActivityDestroyListener) list.get(i)).onActivityDestroy();
            }
        }
        dismissAllScreens();
    }

    int getNextRequestCode() {
        int i;
        synchronized (this) {
            i = this.mNextRequestCode;
            this.mNextRequestCode = i + 1;
        }
        return i;
    }

    void addPreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            if (this.mPreferencesScreens == null) {
                this.mPreferencesScreens = new ArrayList();
            }
            this.mPreferencesScreens.add(screen);
        }
    }

    void removePreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            if (this.mPreferencesScreens == null) {
                return;
            }
            this.mPreferencesScreens.remove(screen);
        }
    }

    void dispatchNewIntent(Intent intent) {
        dismissAllScreens();
    }

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            r0 = r1.size() - 1;
     */
    /* JADX WARNING: Missing block: B:10:0x001a, code:
            if (r0 < 0) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:11:0x001c, code:
            ((android.content.DialogInterface) r1.get(r0)).dismiss();
            r0 = r0 - 1;
     */
    /* JADX WARNING: Missing block: B:15:0x002b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void dismissAllScreens() {
        synchronized (this) {
            if (this.mPreferencesScreens == null) {
            } else {
                ArrayList<DialogInterface> screensToDismiss = new ArrayList(this.mPreferencesScreens);
                this.mPreferencesScreens.clear();
            }
        }
    }

    void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        this.mOnPreferenceTreeClickListener = listener;
    }

    OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return this.mOnPreferenceTreeClickListener;
    }
}
