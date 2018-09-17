package tmsdkobf;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.utils.f;

public class is {
    private il rX = new il(IncomingSmsFilterConsts.PAY_SMS);
    private Map<String, String> rY = new HashMap(16);
    private Map<String, String> rZ = new HashMap(16);
    private Map<String, String> sa = new HashMap(16);
    private boolean sb;

    private static boolean aI(String str) {
        return !TextUtils.isEmpty(str) && str.indexOf(42) >= 0;
    }

    private static boolean aJ(String str) {
        return str.length() > 0 && str.indexOf(42) == str.length() - 1;
    }

    private static String aK(String str) {
        return str.replace("\\", "\\\\").replace(".", "\\.").replace("+", "\\+").replace("*", ".*");
    }

    public void clear() {
        this.rX.clear();
        this.rY.clear();
        this.rZ.clear();
        this.sa.clear();
    }

    public String getName(String str) {
        String str2;
        String cz = qe.cz(str);
        if (qe.cy(cz)) {
            try {
                str2 = (String) this.rX.get(Integer.parseInt(cz));
            } catch (Throwable e) {
                f.b("ContactsMap", "minMatch to int", e);
                str2 = (String) this.rY.get(cz);
            }
        } else {
            str2 = (String) this.rY.get(cz);
        }
        if (str2 != null) {
            return str2;
        }
        CharSequence cx = qe.cx(str);
        CharSequence cv = qe.cv(cx);
        for (Entry entry : this.rZ.entrySet()) {
            String str3 = (String) entry.getKey();
            if (qe.cw(str3)) {
                if (cx.startsWith(str3)) {
                    return (String) entry.getValue();
                }
            } else if (cv.startsWith(str3)) {
                return (String) entry.getValue();
            }
        }
        for (Entry entry2 : this.sa.entrySet()) {
            Pattern compile = Pattern.compile((String) entry2.getKey());
            if (compile.matcher(cx).matches() || compile.matcher(cv).matches()) {
                return (String) entry2.getValue();
            }
        }
        return null;
    }

    public void i(String str, String str2) {
        if (this.sb && aI(str)) {
            j(str, str2);
        } else if (!TextUtils.isEmpty(str)) {
            Object str22;
            if (str22 == null) {
                str22 = "";
            }
            String cz = qe.cz(str);
            if (qe.cy(cz)) {
                try {
                    this.rX.put(Integer.parseInt(cz), str22);
                } catch (NumberFormatException e) {
                    f.e("ContactsMap", "Exception in parseInt(minMatch): " + e.getMessage());
                    this.rY.put(cz, str22);
                }
            } else {
                this.rY.put(cz, str22);
            }
        }
    }

    public void j(String str, String str2) {
        if (!TextUtils.isEmpty(str)) {
            Object str22;
            if (str22 == null) {
                str22 = "";
            }
            if (aJ(str)) {
                this.rZ.put(str.substring(0, str.length() - 1), str22);
            } else {
                this.sa.put(aK(str), str22);
            }
        }
    }
}
