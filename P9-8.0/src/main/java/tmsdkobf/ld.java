package tmsdkobf;

import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;

public class ld {
    public static void ep() {
        try {
            ea eaVar = (ea) mk.a(TMSDKContext.getApplicaionContext(), UpdateConfig.APP_USAGE_PRE_NAME, UpdateConfig.intToString(40545), new ea(), "UTF-8");
            if (eaVar != null && eaVar.iC != null && eaVar.iC.size() > 0) {
                JSONArray jSONArray = new JSONArray();
                Iterator it = eaVar.iC.iterator();
                while (it.hasNext()) {
                    dz dzVar = (dz) it.next();
                    try {
                        int intValue = Integer.valueOf(dzVar.iu).intValue();
                        String str = dzVar.iv;
                        String str2 = dzVar.iw;
                        if (!(TextUtils.isEmpty(str) || TextUtils.isEmpty(str2))) {
                            ov a = TMServiceFactory.getSystemInfoService().a(str, 1);
                            if (a != null) {
                                JSONObject jSONObject = new JSONObject();
                                long j = 0;
                                long j2 = 0;
                                if (new File(str2).exists()) {
                                    try {
                                        Object invoke = Class.forName("android.system.Os").getMethod("stat", new Class[]{String.class}).invoke(null, new Object[]{str2});
                                        Class cls = invoke.getClass();
                                        j = cls.getField("st_mtime").getLong(invoke);
                                        j2 = cls.getField("st_atime").getLong(invoke);
                                    } catch (Throwable th) {
                                        j = -1;
                                        j2 = -1;
                                    }
                                }
                                jSONObject.put("id", String.valueOf(intValue));
                                jSONObject.put("version_code", String.valueOf(a.getVersionCode()));
                                jSONObject.put("mtime", String.valueOf(j));
                                jSONObject.put("atime", String.valueOf(j2));
                                jSONObject.put("is_build_in", !a.hx() ? "0" : "1");
                                jSONArray.put(jSONObject);
                            }
                        }
                    } catch (Throwable th2) {
                    }
                }
                if (jSONArray.length() > 0) {
                    la.a(jSONArray.toString(), getPath(), 151);
                }
                kz.o(System.currentTimeMillis() / 1000);
            }
        } catch (Throwable th3) {
        }
    }

    public static void eq() {
        try {
            final String path = getPath();
            ArrayList bD = la.bD(path);
            if (bD != null && !bD.isEmpty()) {
                JceStruct aoVar = new ao(151, new ArrayList());
                Iterator it = bD.iterator();
                while (it.hasNext()) {
                    try {
                        JSONArray jSONArray = new JSONArray((String) it.next());
                        int length = jSONArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject jSONObject = (JSONObject) jSONArray.get(i);
                            ap apVar = new ap(new HashMap());
                            apVar.bG.put(Integer.valueOf(1), jSONObject.getString("id"));
                            apVar.bG.put(Integer.valueOf(2), jSONObject.getString("version_code"));
                            apVar.bG.put(Integer.valueOf(3), jSONObject.getString("mtime"));
                            apVar.bG.put(Integer.valueOf(4), jSONObject.getString("atime"));
                            apVar.bG.put(Integer.valueOf(5), jSONObject.getString("is_build_in"));
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
            }
        } catch (Throwable th2) {
        }
    }

    public static void es() {
        try {
            File file = new File(TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + UpdateConfig.APP_USAGE_PRE_NAME);
            if (file.exists()) {
                file.delete();
            }
        } catch (Throwable th) {
        }
    }

    public static synchronized void et() {
        synchronized (ld.class) {
            final UpdateManager updateManager = (UpdateManager) ManagerCreatorC.getManager(UpdateManager.class);
            updateManager.check(Long.MIN_VALUE, new ICheckListener() {
                public void onCheckCanceled() {
                }

                public void onCheckEvent(int i) {
                }

                public void onCheckFinished(CheckResult -l_2_R) {
                    if (-l_2_R != null) {
                        updateManager.update(-l_2_R.mUpdateInfoList, new IUpdateListener() {
                            public void onProgressChanged(UpdateInfo updateInfo, int i) {
                            }

                            public void onUpdateCanceled() {
                            }

                            public void onUpdateEvent(UpdateInfo updateInfo, int i) {
                            }

                            public void onUpdateFinished() {
                            }

                            public void onUpdateStarted() {
                            }
                        });
                    }
                }

                public void onCheckStarted() {
                }
            }, -1);
        }
    }

    public static String getPath() {
        return TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + "d_" + 151;
    }
}
