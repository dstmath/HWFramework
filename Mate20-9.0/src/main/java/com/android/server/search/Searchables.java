package com.android.server.search;

import android.app.AppGlobals;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.LocalServices;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
            if (lhsSystem && !rhsSystem) {
                return -1;
            }
            if (!rhsSystem || lhsSystem) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0036, code lost:
        r2 = r11.mPm.getActivityInfo(r12, 128, r11.mUserId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0038, code lost:
        if (r2 != null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003a, code lost:
        android.util.Log.v(LOG_TAG, "getted activity info is null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0041, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
        r3 = null;
        r4 = r2.metaData;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0045, code lost:
        if (r4 == null) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r3 = r4.getString(MD_LABEL_DEFAULT_SEARCHABLE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004d, code lost:
        if (r3 != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
        r4 = r2.applicationInfo.metaData;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0053, code lost:
        if (r4 == null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        r3 = r4.getString(MD_LABEL_DEFAULT_SEARCHABLE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005b, code lost:
        if (r3 == null) goto L_0x00c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0063, code lost:
        if (r3.equals(MD_SEARCHABLE_SYSTEM_SEARCH) == false) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0065, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0066, code lost:
        r5 = r12.getPackageName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0071, code lost:
        if (r3.charAt(0) != '.') goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0073, code lost:
        r6 = new android.content.ComponentName(r5, r5 + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0088, code lost:
        r6 = new android.content.ComponentName(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008d, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r0 = r11.mSearchablesMap.get(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0097, code lost:
        if (r0 == null) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0099, code lost:
        r11.mSearchablesMap.put(r12, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009e, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009f, code lost:
        if (r0 == null) goto L_0x00c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b9, code lost:
        if (((android.content.pm.PackageManagerInternal) com.android.server.LocalServices.getService(android.content.pm.PackageManagerInternal.class)).canAccessComponent(android.os.Binder.getCallingUid(), r0.getSearchActivity(), android.os.UserHandle.getCallingUserId()) == false) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00bb, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00bc, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00c0, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c1, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00c2, code lost:
        android.util.Log.e(LOG_TAG, "Error getting activity info " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d8, code lost:
        return null;
     */
    public SearchableInfo getSearchableInfo(ComponentName activity) {
        synchronized (this) {
            SearchableInfo result = this.mSearchablesMap.get(activity);
            if (result != null) {
                if (((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).canAccessComponent(Binder.getCallingUid(), result.getSearchActivity(), UserHandle.getCallingUserId())) {
                    return result;
                }
                return null;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00eb A[Catch:{ all -> 0x0159 }] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0140 A[SYNTHETIC] */
    public void updateSearchableList() {
        List<ResolveInfo> onlineSearchInfoList;
        Intent intent;
        ResolveInfo resolveInfo;
        HashMap<ComponentName, SearchableInfo> newSearchablesMap = new HashMap<>();
        ArrayList<SearchableInfo> newSearchablesList = new ArrayList<>();
        ArrayList<SearchableInfo> newSearchablesInGlobalSearchList = new ArrayList<>();
        HashMap<ComponentName, SearchableInfo> newOnlineSearchablesMap = new HashMap<>();
        ArrayList<SearchableInfo> newOnlineSearchablesInGlobalSearchList = new ArrayList<>();
        Intent intent2 = new Intent("android.intent.action.SEARCH");
        long ident = Binder.clearCallingIdentity();
        try {
            List<ResolveInfo> searchList = queryIntentActivities(intent2, 268435584);
            Intent webSearchIntent = new Intent("android.intent.action.WEB_SEARCH");
            List<ResolveInfo> webSearchInfoList = queryIntentActivities(webSearchIntent, 268435584);
            if (searchList == null) {
                if (webSearchInfoList == null) {
                    Intent intent3 = intent2;
                    Intent intent4 = webSearchIntent;
                    List<ResolveInfo> list = webSearchInfoList;
                    List<ResolveInfo> newGlobalSearchActivities = findGlobalSearchActivities();
                    ComponentName newGlobalSearchActivity = findGlobalSearchActivity(newGlobalSearchActivities);
                    ComponentName newWebSearchActivity = findWebSearchActivity(newGlobalSearchActivity);
                    Intent onlineSearchIntent = new Intent("huawei.intent.action.ONLINESEARCH");
                    onlineSearchInfoList = queryIntentActivities(onlineSearchIntent, 128);
                    if (onlineSearchInfoList != null) {
                        int online_search_count = onlineSearchInfoList.size();
                        int i2 = 0;
                        while (true) {
                            int i22 = i2;
                            if (i22 >= online_search_count) {
                                break;
                            }
                            int online_search_count2 = online_search_count;
                            Intent onlineSearchIntent2 = onlineSearchIntent;
                            ResolveInfo info2 = onlineSearchInfoList.get(i22);
                            ActivityInfo ai2 = info2.activityInfo;
                            ResolveInfo resolveInfo2 = info2;
                            List<ResolveInfo> onlineSearchInfoList2 = onlineSearchInfoList;
                            List<ResolveInfo> searchList2 = searchList;
                            if (newOnlineSearchablesMap.get(new ComponentName(ai2.packageName, ai2.name)) == null) {
                                SearchableInfo searchable_online = SearchableInfo.getActivityMetaDataOnline(this.mContext, ai2, this.mUserId);
                                if (searchable_online != null) {
                                    newOnlineSearchablesInGlobalSearchList.add(searchable_online);
                                    newOnlineSearchablesMap.put(searchable_online.getSearchActivity(), searchable_online);
                                }
                            }
                            i2 = i22 + 1;
                            online_search_count = online_search_count2;
                            onlineSearchIntent = onlineSearchIntent2;
                            onlineSearchInfoList = onlineSearchInfoList2;
                            searchList = searchList2;
                        }
                    }
                    List<ResolveInfo> searchList3 = searchList;
                    Intent intent5 = onlineSearchIntent;
                    List<ResolveInfo> list2 = onlineSearchInfoList;
                    synchronized (this) {
                        this.mSearchablesMap = newSearchablesMap;
                        this.mSearchablesList = newSearchablesList;
                        this.mSearchablesInGlobalSearchList = newSearchablesInGlobalSearchList;
                        this.mOnlineSearchablesInGlobalSearchList = newOnlineSearchablesInGlobalSearchList;
                        this.mGlobalSearchActivities = newGlobalSearchActivities;
                        this.mCurrentGlobalSearchActivity = newGlobalSearchActivity;
                        this.mWebSearchActivity = newWebSearchActivity;
                    }
                    Binder.restoreCallingIdentity(ident);
                    List<ResolveInfo> list3 = searchList3;
                }
            }
            int search_count = searchList == null ? 0 : searchList.size();
            int count = search_count + (webSearchInfoList == null ? 0 : webSearchInfoList.size());
            int ii = 0;
            while (true) {
                int ii2 = ii;
                if (ii2 >= count) {
                    break;
                }
                if (ii2 < search_count) {
                    try {
                        resolveInfo = searchList.get(ii2);
                        intent = intent2;
                    } catch (Throwable th) {
                        th = th;
                        Intent intent6 = intent2;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } else {
                    intent = intent2;
                    try {
                        resolveInfo = webSearchInfoList.get(ii2 - search_count);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                ResolveInfo info = resolveInfo;
                Intent webSearchIntent2 = webSearchIntent;
                ActivityInfo ai = info.activityInfo;
                ResolveInfo resolveInfo3 = info;
                List<ResolveInfo> webSearchInfoList2 = webSearchInfoList;
                int search_count2 = search_count;
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
                ii = ii2 + 1;
                intent2 = intent;
                webSearchIntent = webSearchIntent2;
                webSearchInfoList = webSearchInfoList2;
                search_count = search_count2;
            }
            Intent intent7 = webSearchIntent;
            List<ResolveInfo> list4 = webSearchInfoList;
            List<ResolveInfo> newGlobalSearchActivities2 = findGlobalSearchActivities();
            ComponentName newGlobalSearchActivity2 = findGlobalSearchActivity(newGlobalSearchActivities2);
            ComponentName newWebSearchActivity2 = findWebSearchActivity(newGlobalSearchActivity2);
            Intent onlineSearchIntent3 = new Intent("huawei.intent.action.ONLINESEARCH");
            onlineSearchInfoList = queryIntentActivities(onlineSearchIntent3, 128);
            if (onlineSearchInfoList != null) {
            }
            List<ResolveInfo> searchList32 = searchList;
            Intent intent52 = onlineSearchIntent3;
            List<ResolveInfo> list22 = onlineSearchInfoList;
            synchronized (this) {
            }
            Binder.restoreCallingIdentity(ident);
            List<ResolveInfo> list32 = searchList32;
        } catch (Throwable th3) {
            th = th3;
            Intent intent8 = intent2;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    private List<ResolveInfo> findGlobalSearchActivities() {
        List<ResolveInfo> activities = queryIntentActivities(new Intent("android.search.action.GLOBAL_SEARCH"), 268500992);
        if (activities != null && !activities.isEmpty()) {
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
        if (activities == null || activities.isEmpty()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static final boolean isSystemApp(ResolveInfo res) {
        return (res.activityInfo.applicationInfo.flags & 1) != 0;
    }

    private ComponentName getDefaultGlobalSearchProvider(List<ResolveInfo> providerList) {
        if (providerList == null || providerList.isEmpty()) {
            Log.w(LOG_TAG, "No global search activity found");
            return null;
        }
        ActivityInfo ai = providerList.get(0).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private String getGlobalSearchProviderSetting() {
        return Settings.Secure.getString(this.mContext.getContentResolver(), "search_global_search_activity");
    }

    private ComponentName findWebSearchActivity(ComponentName globalSearchActivity) {
        if (globalSearchActivity == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.WEB_SEARCH");
        intent.setPackage(globalSearchActivity.getPackageName());
        List<ResolveInfo> activities = queryIntentActivities(intent, 65536);
        if (activities == null || activities.isEmpty()) {
            Log.w(LOG_TAG, "No web search activity found");
            return null;
        }
        ActivityInfo ai = activities.get(0).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        try {
            return this.mPm.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 8388608 | flags, this.mUserId).getList();
        } catch (RemoteException e) {
            return null;
        }
    }

    public synchronized ArrayList<SearchableInfo> getSearchablesList() {
        return createFilterdSearchableInfoList(this.mSearchablesList);
    }

    public synchronized ArrayList<SearchableInfo> getSearchablesInGlobalSearchList() {
        return createFilterdSearchableInfoList(this.mSearchablesInGlobalSearchList);
    }

    public synchronized ArrayList<SearchableInfo> getOnlineSearchablesInGlobalSearchList() {
        return new ArrayList<>(this.mOnlineSearchablesInGlobalSearchList);
    }

    public synchronized ArrayList<ResolveInfo> getGlobalSearchActivities() {
        return createFilterdResolveInfoList(this.mGlobalSearchActivities);
    }

    private ArrayList<SearchableInfo> createFilterdSearchableInfoList(List<SearchableInfo> list) {
        if (list == null) {
            return null;
        }
        ArrayList<SearchableInfo> resultList = new ArrayList<>(list.size());
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        int callingUid = Binder.getCallingUid();
        int callingUserId = UserHandle.getCallingUserId();
        for (SearchableInfo info : list) {
            if (pm.canAccessComponent(callingUid, info.getSearchActivity(), callingUserId)) {
                resultList.add(info);
            }
        }
        return resultList;
    }

    private ArrayList<ResolveInfo> createFilterdResolveInfoList(List<ResolveInfo> list) {
        if (list == null) {
            return null;
        }
        ArrayList<ResolveInfo> resultList = new ArrayList<>(list.size());
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        int callingUid = Binder.getCallingUid();
        int callingUserId = UserHandle.getCallingUserId();
        for (ResolveInfo info : list) {
            if (pm.canAccessComponent(callingUid, info.activityInfo.getComponentName(), callingUserId)) {
                resultList.add(info);
            }
        }
        return resultList;
    }

    public synchronized ComponentName getGlobalSearchActivity() {
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        int callingUid = Binder.getCallingUid();
        int callingUserId = UserHandle.getCallingUserId();
        if (this.mCurrentGlobalSearchActivity == null || !pm.canAccessComponent(callingUid, this.mCurrentGlobalSearchActivity, callingUserId)) {
            return null;
        }
        return this.mCurrentGlobalSearchActivity;
    }

    public synchronized ComponentName getWebSearchActivity() {
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        int callingUid = Binder.getCallingUid();
        int callingUserId = UserHandle.getCallingUserId();
        if (this.mWebSearchActivity == null || !pm.canAccessComponent(callingUid, this.mWebSearchActivity, callingUserId)) {
            return null;
        }
        return this.mWebSearchActivity;
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Searchable authorities:");
        synchronized (this) {
            if (this.mSearchablesList != null) {
                Iterator<SearchableInfo> it = this.mSearchablesList.iterator();
                while (it.hasNext()) {
                    pw.print("  ");
                    pw.println(it.next().getSuggestAuthority());
                }
            }
        }
    }
}
