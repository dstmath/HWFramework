package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import com.android.server.pm.BlackListInfo;
import com.android.server.pm.BlackListInfoEx;
import java.util.ArrayList;
import java.util.Iterator;

public class BlackListAppsUtilsEx {
    public static void readBlackList(BlackListInfoEx info) {
        BlackListAppsUtils.readBlackList(info.getBlackListInfo());
    }

    public static void readDisableAppList(BlackListInfoEx info) {
        BlackListAppsUtils.readDisableAppList(info.getBlackListInfo());
    }

    public static boolean isBlackListUpdate(BlackListInfoEx blackListInfo, BlackListInfoEx disabledApps) {
        return BlackListAppsUtils.isBlackListUpdate(blackListInfo.getBlackListInfo(), disabledApps.getBlackListInfo());
    }

    public static boolean writeBlackListToXml(BlackListInfoEx blackListInfo) {
        return BlackListAppsUtils.writeBlackListToXml(blackListInfo.getBlackListInfo());
    }

    public static boolean deleteDisableAppListFile() {
        return BlackListAppsUtils.deleteDisableAppListFile();
    }

    public static boolean comparePackage(PackageParserEx.PackageEx info, BlackListInfoEx.BlackListAppEx app) {
        return BlackListAppsUtils.comparePackage((PackageParser.Package) info.getPackage(), app.getBlackListApp());
    }

    public static boolean containsApp(ArrayList<BlackListInfoEx.BlackListAppEx> blackListApps, BlackListInfoEx.BlackListAppEx app) {
        ArrayList<BlackListInfo.BlackListApp> listApps = new ArrayList<>();
        Iterator<BlackListInfoEx.BlackListAppEx> it = blackListApps.iterator();
        while (it.hasNext()) {
            listApps.add(it.next().getBlackListApp());
        }
        return BlackListAppsUtils.containsApp(listApps, app.getBlackListApp());
    }
}
