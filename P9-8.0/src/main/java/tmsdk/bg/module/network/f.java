package tmsdk.bg.module.network;

import android.content.Context;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.ScriptHelper;
import tmsdkobf.ei;
import tmsdkobf.ej;
import tmsdkobf.md;
import tmsdkobf.mk;

final class f {
    private static String vq = "upload_config_des";
    private Context mContext = TMSDKContext.getApplicaionContext();
    private String vc;
    private final String vn = "MOBILE";
    private final String vo = "WIFI";
    private final String vp = "EXCLUDE";
    private final List<String> vr = new ArrayList();
    private final List<String> vs = new ArrayList();
    private final ArrayList<String> vt = new ArrayList();
    private md vu;
    private int vv = 0;
    private String vw;

    public f(String str) {
        this.vc = str;
        this.vu = new md("NetInterfaceManager");
    }

    private void a(ej ejVar) {
        if (ejVar != null && ejVar.kl != null) {
            Iterator it = ejVar.kl.iterator();
            while (it.hasNext()) {
                ei eiVar = (ei) it.next();
                if ("MOBILE".equalsIgnoreCase(eiVar.ki)) {
                    this.vr.clear();
                    this.vr.addAll(eiVar.kj);
                } else {
                    if ("WIFI".equalsIgnoreCase(eiVar.ki)) {
                        this.vs.clear();
                        this.vs.addAll(eiVar.kj);
                    } else {
                        if ("EXCLUDE".equalsIgnoreCase(eiVar.ki)) {
                            this.vt.clear();
                            this.vt.addAll(eiVar.kj);
                        }
                    }
                }
            }
        }
    }

    private boolean a(List<String> list, String str) {
        for (String startsWith : list) {
            if (str.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    private boolean bd(String str) {
        if (!str.startsWith("ppp")) {
            return false;
        }
        if (this.vw != null && this.vw.equals(str)) {
            return true;
        }
        this.vw = da();
        return this.vw != null && this.vw.equals(str);
    }

    private ej cY() {
        return (ej) mk.a(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, UpdateConfig.intToString(20001), new ej());
    }

    private void d(List<String> list) {
        String replaceAll = e(list).replaceAll("\n", ",");
        StringBuilder stringBuilder = new StringBuilder("IpAddr: ");
        stringBuilder.append(replaceAll).append(";");
        if (this.vu != null) {
            this.vu.a(vq, stringBuilder.toString(), true);
        }
    }

    private String da() {
        List<String> db = db();
        if (db.size() <= 1) {
            return null;
        }
        List arrayList = new ArrayList(1);
        for (String str : db) {
            if (str.startsWith("ppp")) {
                arrayList.add(str);
            }
        }
        if (arrayList == null || arrayList.size() <= 0) {
            return (String) db.get(0);
        }
        String str2 = (String) arrayList.get(0);
        if (arrayList.size() <= 1) {
            return str2;
        }
        d(arrayList);
        return str2;
    }

    private List<String> db() {
        List arrayList = new ArrayList(1);
        CharSequence runScript = ScriptHelper.runScript((int) CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY, "ip route");
        if (runScript != null) {
            Matcher matcher = Pattern.compile("dev\\s+([\\w]+)").matcher(runScript);
            while (matcher.find()) {
                String group = matcher.group(1);
                if (!arrayList.contains(group)) {
                    arrayList.add(group);
                }
            }
        }
        return arrayList;
    }

    private String e(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        CharSequence runScript = ScriptHelper.runScript((int) CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY, "ip addr");
        if (runScript != null) {
            StringBuilder stringBuilder2 = new StringBuilder("(");
            for (String str : list) {
                stringBuilder2.append("(?:" + str + ")|");
            }
            stringBuilder2.deleteCharAt(stringBuilder2.length() - 1);
            stringBuilder2.append(")");
            Matcher matcher = Pattern.compile("^\\d+:\\s+" + stringBuilder2.toString() + ".*$\n*" + "(^[^\\d].*$\n*)*", 8).matcher(runScript);
            while (matcher.find()) {
                String group = matcher.group(0);
                if (group != null) {
                    stringBuilder.append(group);
                }
            }
        }
        return stringBuilder.toString();
    }

    public boolean bb(String str) {
        return !bd(str) && a(this.vr, str);
    }

    public boolean bc(String str) {
        return !bd(str) && a(this.vs, str);
    }

    public void cZ() {
        a(cY());
    }
}
