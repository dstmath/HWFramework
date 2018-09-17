package tmsdkobf;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.qq.taf.jce.JceInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.m;

@SuppressLint({"DefaultLocale"})
public class qh {
    boolean MA;
    jv om;

    static class a {
        public String MB;
        public String mAppName;

        a(String str, String str2) {
            this.MB = str;
            this.mAppName = str2;
        }
    }

    public qh(boolean z) {
        this.om = null;
        this.MA = false;
        this.om = ((kf) fj.D(9)).ap("QQSecureProvider");
        this.MA = z;
    }

    private static void a(String str, qj qjVar) {
        int indexOf = str.indexOf(44);
        qjVar.Nh = Long.valueOf(str.substring(0, indexOf)).longValue() << 10;
        indexOf++;
        if (str.charAt(indexOf) != '-') {
            qjVar.Ni = Long.valueOf(str.substring(indexOf)).longValue() << 10;
        } else {
            qjVar.Ni = Long.MAX_VALUE;
        }
    }

    private static void b(String str, qj qjVar) {
        long currentTimeMillis = System.currentTimeMillis();
        int indexOf = str.indexOf(44);
        long longValue = Long.valueOf(str.substring(0, indexOf)).longValue();
        indexOf++;
        qjVar.Nk = currentTimeMillis - TimeUnit.DAYS.toMillis(longValue);
        if (str.charAt(indexOf) != '-') {
            qjVar.Nj = currentTimeMillis - TimeUnit.DAYS.toMillis(Long.valueOf(str.substring(indexOf)).longValue());
        } else {
            qjVar.Nj = 0;
        }
    }

    private byte[] cR(String str) {
        byte[] bArr = null;
        Cursor al = this.om.al("SELECT info2 FROM dcr_info WHERE info1='" + str + "'");
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info2");
                while (al.moveToNext()) {
                    bArr = al.getBlob(columnIndex);
                }
                if (al != null) {
                    al.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (al != null) {
                    al.close();
                }
            } catch (Throwable th) {
                if (al != null) {
                    al.close();
                }
            }
        }
        this.om.close();
        return bArr != null ? bArr : null;
    }

    private static String v(byte[] bArr) {
        if (bArr != null) {
            byte[] decrypt = TccCryptor.decrypt(bArr, null);
            if (decrypt != null) {
                return new String(decrypt);
            }
        }
        return null;
    }

    private byte[] w(byte[] bArr) {
        byte[] bArr2 = ((am) nn.a(bArr, new am(), false)).bw;
        return bArr2 != null ? bArr2 : null;
    }

    void U(boolean z) {
        this.MA = z;
    }

    public ql a(String str, ql -l_3_R) {
        byte[] cR = cR(str);
        if (cR == null) {
            return null;
        }
        byte[] w = w(cR);
        if (w == null) {
            return null;
        }
        try {
            JceInputStream jceInputStream = new JceInputStream(TccCryptor.decrypt(w, null));
            jceInputStream.setServerEncoding("UTF-8");
            ak akVar = new ak();
            akVar.readFrom(jceInputStream);
            Iterator it = akVar.br.iterator();
            while (it.hasNext()) {
                Map map = (Map) it.next();
                int parseInt = Integer.parseInt((String) map.get(Integer.valueOf(9)));
                int dg = rg.dg((String) map.get(Integer.valueOf(19)));
                String str2 = (String) map.get(Integer.valueOf(20));
                String str3 = (String) map.get(Integer.valueOf(3));
                String str4 = (String) map.get(Integer.valueOf(23));
                qj qjVar;
                String str5;
                if (TextUtils.isEmpty(str3)) {
                    str3 = (String) map.get(Integer.valueOf(4));
                    if (!TextUtils.isEmpty(str3)) {
                        qjVar = new qj(!this.MA ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18)), str3);
                        qjVar.Nl = 4;
                        qjVar.Ng = (String) map.get(Integer.valueOf(11));
                        qjVar.Nt = parseInt;
                        qjVar.Nu = dg;
                        qjVar.Ne = str2;
                        qjVar.Nv = str4;
                        if (!TextUtils.isEmpty(qjVar.Nv)) {
                            qjVar.Nw = rg.dh(str4);
                        }
                        if (!(qjVar.Ng == null || qjVar.Ng.isEmpty())) {
                            qjVar.Nf++;
                            qjVar.Ng = qjVar.Ng.toLowerCase();
                        }
                        str5 = (String) map.get(Integer.valueOf(12));
                        if (!(str5 == null || str5.isEmpty())) {
                            a(str5, qjVar);
                            qjVar.Nf++;
                        }
                        String str6 = (String) map.get(Integer.valueOf(13));
                        String str7 = (String) map.get(Integer.valueOf(14));
                        String str8 = (String) map.get(Integer.valueOf(15));
                        if (str7 != null && !str7.isEmpty()) {
                            b(str7, qjVar);
                            qjVar.Nf++;
                        } else if (str8 != null && !str8.isEmpty()) {
                            b(str8, qjVar);
                            qjVar.Nf++;
                        } else if (!(str6 == null || str6.isEmpty())) {
                            b(str6, qjVar);
                            qjVar.Nf++;
                        }
                        -l_3_R.b(qjVar);
                    }
                } else {
                    qjVar = new qj(!this.MA ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18)), str3);
                    qjVar.Nt = parseInt;
                    qjVar.Nl = 3;
                    qjVar.Nu = dg;
                    qjVar.Ne = str2;
                    qjVar.Nv = str4;
                    if (!TextUtils.isEmpty(qjVar.Nv)) {
                        qjVar.Nw = rg.dh(str4);
                    }
                    str5 = (String) map.get(Integer.valueOf(10));
                    if (!TextUtils.isEmpty(str5)) {
                        int intValue = Integer.valueOf(str5).intValue();
                        if (intValue > 0) {
                            qjVar.Np = qjVar.mDescription + "(" + String.format(m.cF("days_ago"), new Object[]{Integer.valueOf(intValue)}) + ")";
                            qjVar.mDescription += "(" + String.format(m.cF("in_recent_days"), new Object[]{Integer.valueOf(intValue)}) + ")";
                            qjVar.Ns = (long) intValue;
                            qjVar.Nf++;
                        }
                    }
                    -l_3_R.b(qjVar);
                }
            }
            if (!(-l_3_R.jr() || -l_3_R.js())) {
                qj qjVar2 = new qj(m.cF("deep_clean_other_rubbish"), "/");
                qjVar2.Nt = 1;
                -l_3_R.b(qjVar2);
            }
            -l_3_R.jt();
            return -l_3_R;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<a> cQ(String str) {
        Exception e;
        Throwable th;
        List<a> list = null;
        String str2 = "SELECT * FROM dcp_info WHERE info2=(x'" + com.qq.taf.jce.a.c(TccCryptor.encrypt(str.getBytes(), null)) + "')";
        Log.d("fgtDatabaseParse", getClass().getSimpleName() + " getRootPaths pkg:" + str);
        Cursor al = this.om.al(str2);
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info1");
                int columnIndex2 = al.getColumnIndex("info3");
                int columnIndex3 = al.getColumnIndex("info4");
                List<a> arrayList = new ArrayList();
                while (al.moveToNext()) {
                    try {
                        String string = al.getString(columnIndex);
                        String v = !this.MA ? v(al.getBlob(columnIndex2)) : v(al.getBlob(columnIndex3));
                        if (string != null) {
                            Log.d("fgtDatabaseParse", "add root path:" + string);
                            try {
                                arrayList.add(new a(string, v));
                            } catch (Exception e2) {
                                e = e2;
                                list = arrayList;
                            } catch (Throwable th2) {
                                th = th2;
                                list = arrayList;
                            }
                        }
                    } catch (Exception e3) {
                        e = e3;
                        list = arrayList;
                    } catch (Throwable th3) {
                        th = th3;
                        list = arrayList;
                    }
                }
                if (al != null) {
                    al.close();
                }
                list = arrayList;
            } catch (Exception e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (al != null) {
                        al.close();
                    }
                    this.om.close();
                    return list;
                } catch (Throwable th4) {
                    th = th4;
                    if (al != null) {
                        al.close();
                    }
                    throw th;
                }
            }
        }
        this.om.close();
        return list;
    }
}
