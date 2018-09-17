package tmsdkobf;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;

public class le {
    public static void ep() {
        try {
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            long eb = kz.eb();
            if ((currentTimeMillis >= eb ? 1 : null) != null) {
                List<UsageStats> queryUsageStats = ((UsageStatsManager) TMSDKContext.getApplicaionContext().getSystemService("usagestats")).queryUsageStats(0, eb, currentTimeMillis);
                kz.m(currentTimeMillis);
                if (queryUsageStats != null && queryUsageStats.size() > 0) {
                    JSONArray jSONArray = new JSONArray();
                    for (UsageStats usageStats : queryUsageStats) {
                        try {
                            if (!usageStats.getPackageName().startsWith("com.android")) {
                                if ((usageStats.getTotalTimeInForeground() <= 0 ? 1 : null) == null) {
                                    if ((usageStats.getLastTimeUsed() > 0 ? 1 : null) != null) {
                                        JSONObject jSONObject = new JSONObject();
                                        jSONObject.put("pkgName", usageStats.getPackageName());
                                        jSONObject.put("firstTimeStamp", String.valueOf(usageStats.getFirstTimeStamp() / 1000));
                                        jSONObject.put("lastTimeStamp", String.valueOf(usageStats.getLastTimeStamp() / 1000));
                                        jSONObject.put("lastTimeUsed", String.valueOf(usageStats.getLastTimeUsed() / 1000));
                                        jSONObject.put("totalTimeInForeground", String.valueOf(usageStats.getTotalTimeInForeground() / 1000));
                                        jSONArray.put(jSONObject);
                                    }
                                }
                            }
                        } catch (Throwable th) {
                        }
                    }
                    if (jSONArray.length() > 0) {
                        la.a(jSONArray.toString(), getPath(), 150);
                    }
                }
            }
        } catch (Throwable th2) {
        }
    }

    public static void eq() {
        try {
            final String path = getPath();
            ArrayList bD = la.bD(path);
            if (bD == null || bD.isEmpty()) {
                la.b(150, 1001, "");
                return;
            }
            JceStruct aoVar = new ao(150, new ArrayList());
            Iterator it = bD.iterator();
            while (it.hasNext()) {
                try {
                    JSONArray jSONArray = new JSONArray((String) it.next());
                    int length = jSONArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject jSONObject = (JSONObject) jSONArray.get(i);
                        ap apVar = new ap(new HashMap());
                        apVar.bG.put(Integer.valueOf(6), jSONObject.getString("pkgName"));
                        apVar.bG.put(Integer.valueOf(7), jSONObject.getString("firstTimeStamp"));
                        apVar.bG.put(Integer.valueOf(8), jSONObject.getString("lastTimeStamp"));
                        apVar.bG.put(Integer.valueOf(9), jSONObject.getString("lastTimeUsed"));
                        apVar.bG.put(Integer.valueOf(10), jSONObject.getString("totalTimeInForeground"));
                        aoVar.bD.add(apVar);
                    }
                } catch (Throwable th) {
                }
            }
            ob bK = im.bK();
            if (aoVar.bD.size() > 0 && bK != null) {
                bK.a(4060, aoVar, null, 0, new jy() {
                    public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                        if (i3 == 0 && i4 == 0) {
                            la.bF(path);
                            kz.n(System.currentTimeMillis() / 1000);
                        }
                    }
                });
            }
        } catch (Throwable th2) {
        }
    }

    public static String getPath() {
        return TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + "d_" + 150;
    }
}
