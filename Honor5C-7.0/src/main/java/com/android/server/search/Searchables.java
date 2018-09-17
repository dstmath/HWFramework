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
import android.os.Bundle;
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
    public static String ENHANCED_GOOGLE_SEARCH_COMPONENT_NAME = null;
    private static final Comparator<ResolveInfo> GLOBAL_SEARCH_RANKER = null;
    public static String GOOGLE_SEARCH_COMPONENT_NAME = null;
    private static final String LOG_TAG = "Searchables";
    private static final String MD_LABEL_DEFAULT_SEARCHABLE = "android.app.default_searchable";
    private static final String MD_SEARCHABLE_SYSTEM_SEARCH = "*";
    private Context mContext;
    private ComponentName mCurrentGlobalSearchActivity;
    private List<ResolveInfo> mGlobalSearchActivities;
    private ArrayList<SearchableInfo> mOnlineSearchablesInGlobalSearchList;
    private final IPackageManager mPm;
    private ArrayList<SearchableInfo> mSearchablesInGlobalSearchList;
    private ArrayList<SearchableInfo> mSearchablesList;
    private HashMap<ComponentName, SearchableInfo> mSearchablesMap;
    private int mUserId;
    private ComponentName mWebSearchActivity;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.search.Searchables.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.search.Searchables.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.search.Searchables.<clinit>():void");
    }

    public Searchables(Context context, int userId) {
        this.mSearchablesMap = null;
        this.mSearchablesList = null;
        this.mSearchablesInGlobalSearchList = null;
        this.mOnlineSearchablesInGlobalSearchList = null;
        this.mCurrentGlobalSearchActivity = null;
        this.mWebSearchActivity = null;
        this.mContext = context;
        this.mUserId = userId;
        this.mPm = AppGlobals.getPackageManager();
    }

    public SearchableInfo getSearchableInfo(ComponentName activity) {
        synchronized (this) {
            SearchableInfo result = (SearchableInfo) this.mSearchablesMap.get(activity);
            if (result != null) {
                return result;
            }
            try {
                ActivityInfo ai = this.mPm.getActivityInfo(activity, DumpState.DUMP_PACKAGES, this.mUserId);
                if (ai == null) {
                    Log.v(LOG_TAG, "getted activity info is null");
                    return null;
                }
                String refActivityName = null;
                Bundle md = ai.metaData;
                if (md != null) {
                    refActivityName = md.getString(MD_LABEL_DEFAULT_SEARCHABLE);
                }
                if (refActivityName == null) {
                    md = ai.applicationInfo.metaData;
                    if (md != null) {
                        refActivityName = md.getString(MD_LABEL_DEFAULT_SEARCHABLE);
                    }
                }
                if (refActivityName == null || refActivityName.equals(MD_SEARCHABLE_SYSTEM_SEARCH)) {
                    return null;
                }
                ComponentName referredActivity;
                String pkg = activity.getPackageName();
                if (refActivityName.charAt(0) == '.') {
                    referredActivity = new ComponentName(pkg, pkg + refActivityName);
                } else {
                    referredActivity = new ComponentName(pkg, refActivityName);
                }
                synchronized (this) {
                    result = (SearchableInfo) this.mSearchablesMap.get(referredActivity);
                    if (result != null) {
                        this.mSearchablesMap.put(activity, result);
                        return result;
                    }
                    return null;
                }
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error getting activity info " + re);
                return null;
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
            List<ResolveInfo> onlineSearchInfoList = queryIntentActivities(new Intent("android.intent.action.ONLINESEARCH"), DumpState.DUMP_PACKAGES);
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
        if (!(activities == null || activities.isEmpty())) {
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
        List<ResolveInfo> activities = queryIntentActivities(intent, DumpState.DUMP_INSTALLS);
        if (activities == null || activities.isEmpty()) {
            return false;
        }
        return true;
    }

    private static final boolean isSystemApp(ResolveInfo res) {
        return (res.activityInfo.applicationInfo.flags & 1) != 0;
    }

    private ComponentName getDefaultGlobalSearchProvider(List<ResolveInfo> providerList) {
        if (providerList == null || providerList.isEmpty()) {
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
        List<ResolveInfo> activities = queryIntentActivities(intent, DumpState.DUMP_INSTALLS);
        if (activities == null || activities.isEmpty()) {
            Log.w(LOG_TAG, "No web search activity found");
            return null;
        }
        ActivityInfo ai = ((ResolveInfo) activities.get(0)).activityInfo;
        return new ComponentName(ai.packageName, ai.name);
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        List<ResolveInfo> activities = null;
        try {
            activities = this.mPm.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, this.mUserId).getList();
        } catch (RemoteException e) {
        }
        return activities;
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
