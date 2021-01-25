package com.huawei.security.dpermission.utils;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.model.ReportInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.fastjson.JSONException;
import ohos.utils.zson.ZSONArray;
import ohos.utils.zson.ZSONObject;

public class DangerousPermissionDataHelper {
    private static final int DEFAULT_LIST_LENGTH = 10;
    private static final int DEFAULT_SET_LENGTH = 16;
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "DangerousPermissionDataHelper");
    private static final String FILE_NAME = "PermissionExcessiveRequire.json";
    private static final Object LOCK = new Object();
    private static final String PACKAGE_NAME = "PKGNAME";
    private static final String REPORT_LIST = "REPORT";
    private static final int SET_MAP_LENGTH = 2;

    private DangerousPermissionDataHelper() {
    }

    public static List<String> loadReportedPackages(Context context) {
        ArrayList arrayList = new ArrayList(10);
        String readDatabase = readDatabase(context);
        return !TextUtils.isEmpty(readDatabase) ? parseStringToPkgList(readDatabase) : arrayList;
    }

    public static List<ReportInfo> loadReportInfos(Context context, int i) {
        List arrayList = new ArrayList(10);
        String readDatabase = readDatabase(context);
        if (!TextUtils.isEmpty(readDatabase)) {
            arrayList = parseStringToReportInfo(readDatabase);
        }
        HiLog.debug(DPERMISSION_LABEL, "get report info total number: %{public}d.", new Object[]{Integer.valueOf(arrayList.size())});
        ArrayList arrayList2 = new ArrayList(10);
        Iterator it = arrayList.iterator();
        while (it.hasNext() && arrayList2.size() < i) {
            ReportInfo reportInfo = (ReportInfo) it.next();
            HiLog.debug(DPERMISSION_LABEL, "get report info: %{public}s.", new Object[]{reportInfo.toString()});
            arrayList2.add(reportInfo);
            it.remove();
        }
        if (!arrayList2.isEmpty()) {
            HashMap hashMap = new HashMap(2);
            hashMap.put(PACKAGE_NAME, parseStringToPkgList(readDatabase));
            hashMap.put(REPORT_LIST, arrayList);
            writeDatabase(context, parseTrackerInfoToString(hashMap));
        }
        return arrayList2;
    }

    public static boolean removePackage(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            HiLog.debug(DPERMISSION_LABEL, "removePackage get empty package name.", new Object[0]);
            return false;
        }
        HiLog.debug(DPERMISSION_LABEL, "DangerousPermissionDataHelper remove package  %{public}s", new Object[]{str});
        String readDatabase = readDatabase(context);
        if (TextUtils.isEmpty(readDatabase)) {
            return false;
        }
        List<String> parseStringToPkgList = parseStringToPkgList(readDatabase);
        if (parseStringToPkgList.contains(str)) {
            try {
                parseStringToPkgList.remove(str);
                HashMap hashMap = new HashMap(2);
                hashMap.put(PACKAGE_NAME, parseStringToPkgList);
                hashMap.put(REPORT_LIST, parseStringToReportInfo(readDatabase));
                return writeDatabase(context, parseTrackerInfoToString(hashMap));
            } catch (JSONException e) {
                HiLog.error(DPERMISSION_LABEL, "removePackage JSONException: %{public}s", new Object[]{e.getMessage()});
            } catch (Exception e2) {
                HiLog.error(DPERMISSION_LABEL, "removePackage unexpected exception: %{public}s", new Object[]{e2.getMessage()});
            }
        }
        return false;
    }

    private static List<String> parseStringToPkgList(String str) {
        ZSONArray zSONArray;
        ArrayList arrayList = new ArrayList(10);
        try {
            ZSONObject stringToZSON = ZSONObject.stringToZSON(str);
            if (stringToZSON == null || (zSONArray = stringToZSON.getZSONArray(PACKAGE_NAME)) == null) {
                return arrayList;
            }
            return zSONArray.toJavaList(String.class);
        } catch (JSONException e) {
            HiLog.error(DPERMISSION_LABEL, "parseStringToPkgList JSONException: %{public}s", new Object[]{e.getMessage()});
            return arrayList;
        } catch (Exception e2) {
            HiLog.error(DPERMISSION_LABEL, "parseStringToPkgList unexpected exception: %{public}s", new Object[]{e2.getMessage()});
            return arrayList;
        }
    }

    private static List<ReportInfo> parseStringToReportInfo(String str) {
        try {
            ZSONObject stringToZSON = ZSONObject.stringToZSON(str);
            if (stringToZSON == null) {
                return Collections.emptyList();
            }
            ZSONArray zSONArray = stringToZSON.getZSONArray(REPORT_LIST);
            if (zSONArray == null) {
                return Collections.emptyList();
            }
            return zSONArray.toJavaList(ReportInfo.class);
        } catch (JSONException e) {
            HiLog.error(DPERMISSION_LABEL, "parseStringToReportInfo JSONException: %{public}s", new Object[]{e.getMessage()});
            return Collections.emptyList();
        } catch (Exception e2) {
            HiLog.error(DPERMISSION_LABEL, "parseStringToReportInfo unexpected exception: %{public}s", new Object[]{e2.getMessage()});
            return Collections.emptyList();
        }
    }

    public static boolean updateReportInfos(Context context, List<ReportInfo> list) {
        if (list == null || list.isEmpty()) {
            HiLog.debug(DPERMISSION_LABEL, "updateReportInfos no need to update.", new Object[0]);
            return false;
        }
        HashSet hashSet = new HashSet(16);
        ArrayList arrayList = new ArrayList(10);
        String readDatabase = readDatabase(context);
        for (ReportInfo reportInfo : list) {
            if (reportInfo.checkIsValid()) {
                hashSet.add(reportInfo.getPackageName());
                arrayList.add(reportInfo);
            }
        }
        if (hashSet.isEmpty() || arrayList.isEmpty()) {
            HiLog.warn(DPERMISSION_LABEL, "updateReportInfos get invalid report info.", new Object[0]);
            return false;
        }
        arrayList.addAll(parseStringToReportInfo(readDatabase));
        hashSet.addAll(parseStringToPkgList(readDatabase));
        HashMap hashMap = new HashMap(2);
        hashMap.put(PACKAGE_NAME, hashSet);
        hashMap.put(REPORT_LIST, arrayList);
        String parseTrackerInfoToString = parseTrackerInfoToString(hashMap);
        if (TextUtils.isEmpty(parseTrackerInfoToString)) {
            return false;
        }
        return writeDatabase(context, parseTrackerInfoToString);
    }

    private static String parseTrackerInfoToString(Map<String, Object> map) {
        try {
            return new ZSONObject(map).toString();
        } catch (JSONException e) {
            HiLog.error(DPERMISSION_LABEL, "parseTrackerInfoToString JSONException: %{public}s.", new Object[]{e.getMessage()});
            return "";
        } catch (Exception e2) {
            HiLog.error(DPERMISSION_LABEL, "parseTrackerInfoToString get unexpected exception %{public}s.", new Object[]{e2.getMessage()});
            return "";
        }
    }

    private static String readDatabase(Context context) {
        synchronized (LOCK) {
            String str = "";
            if (context == null) {
                HiLog.warn(DPERMISSION_LABEL, "readDatabase get null context", new Object[0]);
                return str;
            }
            try {
                Path path = Paths.get(context.getFilesDir().getCanonicalPath(), FILE_NAME);
                if (Files.exists(path, new LinkOption[0])) {
                    str = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                }
            } catch (FileNotFoundException unused) {
                HiLog.error(DPERMISSION_LABEL, "readDataBase get FileNotFoundException.", new Object[0]);
            } catch (IOException e) {
                HiLog.error(DPERMISSION_LABEL, "readDataBase IOException: %{private}s", new Object[]{e.getMessage()});
            } catch (Exception e2) {
                HiLog.error(DPERMISSION_LABEL, "readDataBase unexpected exception: %{public}s", new Object[]{e2.getMessage()});
            }
            return str;
        }
    }

    private static boolean writeDatabase(Context context, String str) {
        synchronized (LOCK) {
            if (context == null) {
                HiLog.warn(DPERMISSION_LABEL, "writeDatabase get null context", new Object[0]);
                return false;
            }
            try {
                Files.write(Paths.get(context.getFilesDir().getCanonicalPath(), FILE_NAME), str.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
                return true;
            } catch (FileNotFoundException unused) {
                HiLog.error(DPERMISSION_LABEL, "writeDatabase get FileNotFoundException.", new Object[0]);
                return false;
            } catch (IOException e) {
                HiLog.error(DPERMISSION_LABEL, "writeDatabase IOException: %{public}s", new Object[]{e.getMessage()});
                return false;
            } catch (Exception e2) {
                HiLog.error(DPERMISSION_LABEL, "writeDatabase unexpected exception: %{public}s", new Object[]{e2.getMessage()});
                return false;
            }
        }
    }
}
