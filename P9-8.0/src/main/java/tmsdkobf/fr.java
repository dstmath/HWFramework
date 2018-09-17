package tmsdkobf;

import android.content.Context;
import com.tencent.tcuser.util.a;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import tmsdk.common.TMSDKContext;

public class fr {
    private static volatile fr nc = null;
    private fv mZ;
    private fw na;
    private Map<String, String> nb = new HashMap();

    private void G(String str) {
        File file = new File(str);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private void a(Context context, Object obj, String str, String str2) {
        try {
            Object a = gb.a(context, str, obj, gb.e(context) + File.separator + str2);
            if (a != null) {
                if (a instanceof fv) {
                    this.mZ = (fv) a;
                } else if (a instanceof fw) {
                    this.na = (fw) a;
                }
            }
        } catch (Exception e) {
        }
    }

    public static fr r() {
        if (nc == null) {
            Class cls = fr.class;
            synchronized (fr.class) {
                if (nc == null) {
                    nc = new fr();
                }
            }
        }
        return nc;
    }

    public static void s() {
        nc = null;
    }

    public void a(int i, String str) {
        if (str != null) {
            kt.e(i, str);
        }
    }

    public void a(Context context) {
        a(context, new fv(), "cloudcmd", "tms_config.dat");
        if (this.mZ != null) {
            a(context, new fw(), "localrecord", "tms_record.dat");
            if (this.na == null) {
                this.na = new fw();
            } else {
                ArrayList I = this.na.I();
                if (I != null && I.size() > 0) {
                    Iterator it = I.iterator();
                    while (it.hasNext()) {
                        fu fuVar = (fu) it.next();
                        this.nb.put(fuVar.nf, fuVar.nn);
                    }
                }
            }
        }
    }

    public boolean a(t tVar, int i) {
        fv fvVar = new fv();
        t();
        boolean z = a.av((String) tVar.ar.get(0)) != 0;
        if (!z) {
            return true;
        }
        if ((System.currentTimeMillis() / 1000 <= ((long) i) ? 1 : null) == null) {
            return false;
        }
        fvVar.d(z);
        fvVar.H(i);
        fvVar.O((String) tVar.ar.get(1));
        fvVar.P((String) tVar.ar.get(2));
        fvVar.Q((String) tVar.ar.get(3));
        fvVar.R((String) tVar.ar.get(4));
        fvVar.S((String) tVar.ar.get(5));
        fvVar.T((String) tVar.ar.get(6));
        fvVar.U((String) tVar.ar.get(7));
        this.mZ = fvVar;
        boolean b = b(TMSDKContext.getApplicaionContext(), this.mZ, "cloudcmd", "tms_config.dat");
        if (b) {
            kt.e(1320067, "1");
        } else {
            kt.e(1320067, "0");
        }
        return b;
    }

    public void b(Context context) {
        if (this.nb != null) {
            ArrayList arrayList = new ArrayList();
            for (String str : this.nb.keySet()) {
                fu fuVar = new fu();
                fuVar.nf = str;
                fuVar.nn = (String) this.nb.get(str);
                arrayList.add(fuVar);
            }
            this.na.g(arrayList);
            b(context, this.na, "localrecord", "tms_record.dat");
            this.nb.clear();
        }
    }

    public boolean b(Context context, Object obj, String str, String str2) {
        try {
            String str3 = gb.e(context) + File.separator + str2;
            if (obj != null && gb.c(context, obj, str, str3) == 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void t() {
        String e = gb.e(TMSDKContext.getApplicaionContext());
        G(e + File.separator + "tms_config.dat");
        G(e + File.separator + "tms_record.dat");
    }

    public fv u() {
        return this.mZ;
    }

    public Map<String, String> v() {
        return this.nb;
    }
}
