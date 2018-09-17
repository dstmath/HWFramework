package tmsdkobf;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;

public class lf {
    static long yg = -1;

    private static int a(ScanResult scanResult) {
        return (scanResult == null || scanResult.capabilities == null) ? -1 : !scanResult.capabilities.contains("WEP") ? !scanResult.capabilities.contains("PSK") ? !scanResult.capabilities.contains("EAP") ? 0 : 3 : 2 : 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00f5 A:{SYNTHETIC, Splitter: B:43:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0111 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032 A:{Catch:{ Throwable -> 0x00b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b3 A:{Catch:{ Throwable -> 0x00b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00f5 A:{SYNTHETIC, Splitter: B:43:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032 A:{Catch:{ Throwable -> 0x00b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0111 A:{SKIP} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void bG(String str) {
        synchronized (lf.class) {
            if (TextUtils.isEmpty(str)) {
                return;
            }
            try {
                JSONArray jSONArray;
                int i;
                final JSONArray jSONArray2;
                CharSequence eu;
                ArrayList bD = la.bD(getPath());
                if (bD != null) {
                    if (bD.size() > 0) {
                        jSONArray = new JSONArray((String) bD.get(0));
                        i = 0;
                        while (i < jSONArray.length()) {
                            JSONObject jSONObject = (JSONObject) jSONArray.get(i);
                            String string = jSONObject.getString("bssid");
                            long j = jSONObject.getLong("curr_time");
                            if (string.compareToIgnoreCase(str) != 0) {
                                i++;
                            } else {
                                if ((System.currentTimeMillis() - j >= 86400000 ? 1 : null) == null) {
                                    return;
                                }
                                jSONObject.put("curr_time", System.currentTimeMillis());
                                if (i >= jSONArray.length()) {
                                    jSONObject = new JSONObject();
                                    jSONObject.put("bssid", str);
                                    jSONObject.put("curr_time", System.currentTimeMillis());
                                    jSONArray.put(jSONObject);
                                }
                                jSONArray2 = jSONArray;
                                eu = eu();
                                if (TextUtils.isEmpty(eu)) {
                                    return;
                                }
                                JceStruct aoVar = new ao(90, new ArrayList());
                                ap apVar = new ap(new HashMap());
                                apVar.bG.put(Integer.valueOf(1), String.valueOf(7));
                                apVar.bG.put(Integer.valueOf(2), String.valueOf(System.currentTimeMillis()));
                                apVar.bG.put(Integer.valueOf(3), String.valueOf(eu));
                                apVar.bG.put(Integer.valueOf(9), String.valueOf(yg));
                                aoVar.bD.add(apVar);
                                ob bK = im.bK();
                                if (aoVar.bD.size() > 0 && bK != null) {
                                    bK.a(4060, aoVar, null, 0, new jy() {
                                        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                                            if (i3 == 0 && i4 == 0) {
                                                try {
                                                    la.a(jSONArray2.toString(), lf.getPath(), 90);
                                                } catch (Throwable th) {
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        if (i >= jSONArray.length()) {
                        }
                        jSONArray2 = jSONArray;
                        eu = eu();
                        if (TextUtils.isEmpty(eu)) {
                        }
                    }
                }
                jSONArray = new JSONArray();
                i = 0;
                while (i < jSONArray.length()) {
                }
                if (i >= jSONArray.length()) {
                }
                jSONArray2 = jSONArray;
                eu = eu();
                if (TextUtils.isEmpty(eu)) {
                }
            } catch (Throwable th) {
            }
        }
    }

    private static String eu() {
        try {
            List<ScanResult> scanResults = ((WifiManager) TMSDKContext.getApplicaionContext().getApplicationContext().getSystemService("wifi")).getScanResults();
            if (scanResults == null || scanResults.size() <= 0) {
                return null;
            }
            JSONArray jSONArray = new JSONArray();
            int i = 0;
            for (ScanResult scanResult : scanResults) {
                if (i > 50) {
                    break;
                }
                JSONObject jSONObject = new JSONObject();
                try {
                    jSONObject.putOpt("bssid", scanResult.BSSID);
                    jSONObject.putOpt("ssid", scanResult.SSID);
                    jSONObject.putOpt("secureType", Integer.valueOf(a(scanResult)));
                } catch (Throwable th) {
                }
                jSONArray.put(jSONObject);
                i++;
            }
            return jSONArray.toString();
        } catch (Throwable th2) {
            return null;
        }
    }

    public static String getPath() {
        return TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + "d_" + String.valueOf(90);
    }
}
