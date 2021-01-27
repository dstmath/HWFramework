package com.android.server.pm;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import com.huawei.android.content.pm.ResolveInfoEx;
import java.util.ArrayList;
import java.util.List;

public class HwResolverManagerEx {
    public static void putPreferredActivityInPcMode(int userId, IntentFilter filter, PreferredActivityEx preferredActivity) {
        HwResolverManager.getInstance().putPreferredActivityInPcMode(userId, filter, preferredActivity.getPreferredActivity());
    }

    public static void preChooseBestActivity(Intent intent, List<ResolveInfoEx> querys, String resolvedType, int userId) {
        List<ResolveInfo> resolveList = new ArrayList<>();
        for (ResolveInfoEx infoEx : querys) {
            resolveList.add(infoEx.getResolveInfo());
        }
        HwResolverManager.getInstance().preChooseBestActivity(intent, resolveList, resolvedType, userId);
    }

    public static ResolveInfoEx findPreferredActivityInCache(PackageManagerServiceEx pms, Intent intent, String resolvedType, int flags, List<ResolveInfoEx> query, int userId) {
        List<ResolveInfo> resolveList = new ArrayList<>();
        for (ResolveInfoEx infoEx : query) {
            resolveList.add(infoEx.getResolveInfo());
        }
        ResolveInfo resolveInfo = HwResolverManager.getInstance().findPreferredActivityInCache(pms.getPackageManagerSerivce(), intent, resolvedType, flags, resolveList, userId);
        ResolveInfoEx infoEx2 = new ResolveInfoEx();
        infoEx2.setResolveInfo(resolveInfo);
        return infoEx2;
    }

    public static void filterResolveInfo(Context context, Intent intent, int userId, String resolvedType, List<ResolveInfoEx> resolveInfoExList) {
        List<ResolveInfo> resolveList = new ArrayList<>();
        for (ResolveInfoEx infoEx : resolveInfoExList) {
            resolveList.add(infoEx.getResolveInfo());
        }
        HwResolverManager.getInstance().filterResolveInfo(context, intent, userId, resolvedType, resolveList);
        resolveInfoExList.clear();
        for (ResolveInfo info : resolveList) {
            ResolveInfoEx infoEx2 = new ResolveInfoEx();
            infoEx2.setResolveInfo(info);
            resolveInfoExList.add(infoEx2);
        }
    }
}
