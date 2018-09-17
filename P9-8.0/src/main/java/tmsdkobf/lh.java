package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class lh {
    public static void c(String str, int i) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("number", str);
            jSONObject.put("time", String.valueOf(System.currentTimeMillis()));
            jSONObject.put("tag", String.valueOf(i));
            la.a(jSONObject.toString(), getPath(), SmsCheckResult.ESCT_146);
        } catch (Throwable th) {
        }
    }

    public static void eq() {
        try {
            final String path = getPath();
            ArrayList bD = la.bD(path);
            if (bD != null && !bD.isEmpty()) {
                JceStruct aoVar = new ao(SmsCheckResult.ESCT_146, new ArrayList());
                Iterator it = bD.iterator();
                while (it.hasNext()) {
                    JSONObject jSONObject = new JSONObject((String) it.next());
                    ap apVar = new ap(new HashMap());
                    apVar.bG.put(Integer.valueOf(6), jSONObject.getString("number"));
                    apVar.bG.put(Integer.valueOf(7), jSONObject.getString("time"));
                    apVar.bG.put(Integer.valueOf(8), jSONObject.getString("tag"));
                    aoVar.bD.add(apVar);
                }
                ob bK = im.bK();
                if (aoVar.bD.size() > 0 && bK != null) {
                    bK.a(4060, aoVar, null, 0, new jy() {
                        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                            if (i3 == 0 && i4 == 0) {
                                la.bF(path);
                                kz.l(System.currentTimeMillis() / 1000);
                            }
                        }
                    });
                }
            }
        } catch (Throwable th) {
        }
    }

    public static String getPath() {
        return TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + "d_" + SmsCheckResult.ESCT_146;
    }
}
