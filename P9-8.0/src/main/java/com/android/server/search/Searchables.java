package com.android.server.search;

import android.app.AppGlobals;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Searchables {
    public static String ENHANCED_GOOGLE_SEARCH_COMPONENT_NAME = "com.google.android.providers.enhancedgooglesearch/.Launcher";
    private static final Comparator<ResolveInfo> GLOBAL_SEARCH_RANKER = new Comparator<ResolveInfo>() {
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            if (lhs == rhs) {
                return 0;
            }
            boolean lhsSystem = Searchables.isSystemApp(lhs);
            boolean rhsSystem = Searchables.isSystemApp(rhs);
            if (lhsSystem && (rhsSystem ^ 1) != 0) {
                return -1;
            }
            if (!rhsSystem || (lhsSystem ^ 1) == 0) {
                return rhs.priority - lhs.priority;
            }
            return 1;
        }
    };
    public static String GOOGLE_SEARCH_COMPONENT_NAME = "com.android.googlesearch/.GoogleSearch";
    private static final String LOG_TAG = "Searchables";
    private static final String MD_LABEL_DEFAULT_SEARCHABLE = "android.app.default_searchable";
    private static final String MD_SEARCHABLE_SYSTEM_SEARCH = "*";
    private Context mContext;
    private ComponentName mCurrentGlobalSearchActivity = null;
    private List<ResolveInfo> mGlobalSearchActivities;
    private ArrayList<SearchableInfo> mOnlineSearchablesInGlobalSearchList = null;
    private final IPackageManager mPm;
    private ArrayList<SearchableInfo> mSearchablesInGlobalSearchList = null;
    private ArrayList<SearchableInfo> mSearchablesList = null;
    private HashMap<ComponentName, SearchableInfo> mSearchablesMap = null;
    private int mUserId;
    private ComponentName mWebSearchActivity = null;

    public Searchables(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
        this.mPm = AppGlobals.getPackageManager();
    }

    /* JADX WARNING: Missing block: B:10:?, code:
            r1 = r12.mPm.getActivityInfo(r13, 128, r12.mUserId);
     */
    /* JADX WARNING: Missing block: B:11:0x001a, code:
            if (r1 != null) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:12:0x001c, code:
            android.util.Log.v(LOG_TAG, "getted activity info is null");
     */
    /* JADX WARNING: Missing block: B:13:0x0025, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:17:0x0029, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x002a, code:
            android.util.Log.e(LOG_TAG, "Error getting activity info " + r4);
     */
    /* JADX WARNING: Missing block: B:19:0x0044, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:20:0x0045, code:
            r5 = null;
            r2 = r1.metaData;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            if (r2 == null) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:22:0x004a, code:
            r5 = r2.getString(MD_LABEL_DEFAULT_SEARCHABLE);
     */
    /* JADX WARNING: Missing block: B:23:0x0051, code:
            if (r5 != null) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:24:0x0053, code:
            r2 = r1.applicationInfo.metaData;
     */
    /* JADX WARNING: Missing block: B:25:0x0057, code:
            if (r2 == null) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:26:0x0059, code:
            r5 = r2.getString(MD_LABEL_DEFAULT_SEARCHABLE);
     */
    /* JADX WARNING: Missing block: B:27:0x0060, code:
            if (r5 == null) goto L_0x00aa;
     */
    /* JADX WARNING: Missing block: B:29:0x0069, code:
            if (r5.equals(MD_SEARCHABLE_SYSTEM_SEARCH) == false) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:30:0x006b, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:31:0x006c, code:
            r3 = r13.getPackageName();
     */
    /* JADX WARNING: Missing block: B:32:0x0077, code:
            if (r5.charAt(0) != '.') goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:33:0x0079, code:
            r6 = new android.content.ComponentName(r3, r3 + r5);
     */
    /* JADX WARNING: Missing block: B:34:0x008f, code:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r7 = (android.app.SearchableInfo) r12.mSearchablesMap.get(r6);
     */
    /* JADX WARNING: Missing block: B:37:0x009a, code:
            if (r7 == null) goto L_0x00a9;
     */
    /* JADX WARNING: Missing block: B:38:0x009c, code:
            r12.mSearchablesMap.put(r13, r7);
     */
    /* JADX WARNING: Missing block: B:39:0x00a1, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:40:0x00a2, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:41:0x00a3, code:
            r6 = new android.content.ComponentName(r3, r5);
     */
    /* JADX WARNING: Missing block: B:42:0x00a9, code:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:43:0x00aa, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SearchableInfo getSearchableInfo(ComponentName activity) {
        synchronized (this) {
            SearchableInfo result = (SearchableInfo) this.mSearchablesMap.get(activity);
            if (result != null) {
                return result;
            }
        }
    }

    public void updateSearchableList() {
        HashMap<ComponentName, SearchableInfo> newSearchablesMap = new HashMap();
        ArrayList<SearchableInfo> newSearchablesList = new ArrayList();
        ArrayList<SearchableInfo> newSearchablesInGlobalSearchList = new ArrayList();
        HashMap<ComponentName, SearchableInfo> newOnlineSearchablesMap = new HashMap();
        ArrayList<SearchableInfo> newOnlineSearchablesInGlobalSearchList = new ArrayList();
        Intent intent = new Intent("android.intent.action.SEARCH");
        long ident = Binder.clearCallingIdentity();
        try {
            List<ResolveInfo> searchList = queryIntentActivities(intent, 268435584);
            List<ResolveInfo> webSearchInfoList = queryIntentActivities(new Intent("android.intent.action.WEB_SEARCH"), 268435584);
            if (!(searchList == null && webSearchInfoList == null)) {
                int search_count = searchList == null ? 0 : searchList.size();
                int count = search_count + (webSearchInfoList == null ? 0 : webSearchInfoList.size());
                for (int ii = 0; ii < count; ii++) {
                    ResolveInfo info;
                    if (ii < search_count) {
                        info = (ResolveInfo) searchList.get(ii);
                    } else {
                        info = (ResolveInfo) webSearchInfoList.get(ii - search_count);
                    }
                    ActivityInfo ai = info.activityInfo;
                    if (newSearchablesMap.get(new ComponentName(ai.packageName, ai.name)) == null) {
                        SearchableInfo searchable = SearchableInfo.getActivityMetaData(this.mContext, ai, this.mUserId);
                        if (searchable != null) {
                            newSearchablesList.add(searchable);
                            newSearchablesMap.put(searchable.getSearchActivity(), searchable);
                            if (searchable.shouldIncludeInGlobalSearch()) {
                                newSearchablesInGlobalSearchList.add(searchable);
                            }
                        }
                    }
                }
            }
            List<ResolveInfo> newGlobalSearchActivities = findGlobalSearchActivities();
            ComponentName newGlobalSearchActivity = findGlobalSearchActivity(newGlobalSearchActivities);
            ComponentName newWebSearchActivity = findWebSearchActivity(newGlobalSearchActivity);
            List<ResolveInfo> onlineSearchInfoList = queryIntentActivities(new Intent("huawei.intent.action.ONLINESEARCH"), 128);
            if (onlineSearchInfoList != null) {
                int online_search_count = onlineSearchInfoList.size();
                for (int i2 = 0; i2 < online_search_count; i2++) {
                    ActivityInfo ai2 = ((ResolveInfo) onlineSearchInfoList.get(i2)).activityInfo;
                    if (newOnlineSearchablesMap.get(new ComponentName(ai2.packageName, ai2.name)) == null) {
                        SearchableInfo searchable_online = SearchableInfo.getActivityMetaDataOnline(this.mContext, ai2, this.mUserId);
                        if (searchable_online != null) {
                            newOnlineSearchablesInGlobalSearchList.add(searchable_online);
                            newOnlineSearchablesMap.put(searchable_online.getSearchActivity(), searchable_online);
                        }
                    }
                }
            }
            synchronized (this) {
                this.mSearchablesMap = newSearchablesMap;
                this.mSearchablesList = newSearchablesList;
                this.mSearchablesInGlobalSearchList = newSearchablesInGlobalSearchList;
                this.mOnlineSearchablesInGlobalSearchList = newOnlineSearchablesInGlobalSearchList;
                this.mGlobalSearchActivities = newGlobalSearchActivities;
                this.mCurrentGlobalSearchActivity = newGlobalSearchActivity;
                this.mWebSearchActivity = newWebSearchActivity;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private List<ResolveInfo> findGlobalSearchActivities() {
        List<ResolveInfo> activities = queryIntentActivities(new Intent("android.search.action.GLOBAL_SEARCH"), 268500992);
        if (!(activities == null || (activities.isEmpty() ^ 1) == 0)) {
            Collections.sort(activities, GLOBAL_SEARCH_RANKER);
        }
        return activities;
    }

    private ComponentName findGlobalSearchActivity(List<ResolveInfo> installed) {
        String searchProviderSetting = getGlobalSearchProviderSetting();
        if (!TextUtils.isEmpty(searchProviderSetting)) {
            ComponentName globalSearchComponent = ComponentName.unflattenFromString(searchProviderSetting);
            if (globalSearchComponent != null && isInstalled(globalSearchComponent)) {
                return globalSearchComponent;
            }
        }
        return getDefaultGlobalSearchProvider(installed);
    }

    private boolean isInstalled(ComponentName globalSearch) {
        Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
        intent.setComponent(globalSearch);
        List<ResolveInfo> activities = queryIntentActivities(intent, 65536);
        if (activities == null || (activities.isEmpty() ^ 1) == 0) {
            return false;
        }
        return true;
    }

    private static final boolean isSystemApp(ResolveInfo res) {
        return (res.activityInfo.applicationInfo.flags & 1) != 0;
    }

    private ComponentName getDefaultGlobalSearchProvider(List<ResolveInfo> providerList) {
        if (providerList == null || (providerList.isEmpty() ^ 1) == 0) {
            Log.w(LOG_TAG, "No global search activity found");
            return null;
        }
        ActivityInfo ai = ((ResolveInfo) providerList.get(0)).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private String getGlobalSearchProviderSetting() {
        return Secure.getString(this.mContext.getContentResolver(), "search_global_search_activity");
    }

    private ComponentName findWebSearchActivity(ComponentName globalSearchActivity) {
        if (globalSearchActivity == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.WEB_SEARCH");
        intent.setPackage(globalSearchActivity.getPackageName());
        List<ResolveInfo> activities = queryIntentActivities(intent, 65536);
        if (activities == null || (activities.isEmpty() ^ 1) == 0) {
            Log.w(LOG_TAG, "No web search activity found");
            return null;
        }
        ActivityInfo ai = ((ResolveInfo) activities.get(0)).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        List<ResolveInfo> activities = null;
        try {
            return this.mPm.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, this.mUserId).getList();
        } catch (RemoteException e) {
            return activities;
        }
    }

    public synchronized ArrayList<SearchableInfo> getSearchablesList() {
        return new ArrayList(this.mSearchablesList);
    }

    public synchronized ArrayList<SearchableInfo> getSearchablesInGlobalSearchList() {
        return new ArrayList(this.mSearchablesInGlobalSearchList);
    }

    public synchronized ArrayList<SearchableInfo> getOnlineSearchablesInGlobalSearchList() {
        return new ArrayList(this.mOnlineSearchablesInGlobalSearchList);
    }

    public synchronized ArrayList<ResolveInfo> getGlobalSearchActivities() {
        return new ArrayList(this.mGlobalSearchActivities);
    }

    public synchronized ComponentName getGlobalSearchActivity() {
        return this.mCurrentGlobalSearchActivity;
    }

    public synchronized ComponentName getWebSearchActivity() {
        return this.mWebSearchActivity;
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Searchable authorities:");
        synchronized (this) {
            if (this.mSearchablesList != null) {
                for (SearchableInfo info : this.mSearchablesList) {
                    pw.print("  ");
                    pw.println(info.getSuggestAuthority());
                }
            }
        }
    }
}
