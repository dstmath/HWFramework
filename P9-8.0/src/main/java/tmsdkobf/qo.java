package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.qq.taf.jce.a;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.m;

public class qo {
    private static qo NY;
    private jv om = ((kf) fj.D(9)).ap("QQSecureProvider");

    private qo() {
    }

    public static qo jz() {
        if (NY == null) {
            NY = new qo();
        }
        return NY;
    }

    private String v(byte[] bArr) {
        if (bArr != null) {
            byte[] decrypt = TccCryptor.decrypt(bArr, null);
            if (decrypt != null) {
                return new String(decrypt);
            }
        }
        return null;
    }

    public boolean addUninstallPkg(String str) {
        if (str == null) {
            return false;
        }
        ArrayList arrayList = new ArrayList();
        String[] strArr = new String[]{str};
        arrayList.add(ContentProviderOperation.newDelete(this.om.an("up")).withSelection("info1 = ?", strArr).build());
        ContentValues contentValues = new ContentValues();
        contentValues.put("info1", str);
        contentValues.put("info2", Long.valueOf(System.currentTimeMillis()));
        arrayList.add(ContentProviderOperation.newInsert(this.om.am("up")).withValues(contentValues).build());
        this.om.applyBatch(arrayList);
        this.om.close();
        return true;
    }

    public List<qu> cV(String str) {
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
        return bArr != null ? qs.b(str, bArr) : null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00fa  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Map<String, qv> cW(String str) {
        Exception e;
        Throwable th;
        boolean iW = m.iW();
        Map<String, qv> map = null;
        Cursor al = this.om.al("SELECT * FROM dcp_info WHERE info2=(x'" + a.c(TccCryptor.encrypt(str.getBytes(), null)) + "')");
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info1");
                int columnIndex2 = al.getColumnIndex("info3");
                int columnIndex3 = al.getColumnIndex("info4");
                Map<String, qv> hashMap = new HashMap();
                while (al.moveToNext()) {
                    try {
                        String string = al.getString(columnIndex);
                        try {
                            qv qvVar = (qv) hashMap.get(string);
                            if (qvVar == null) {
                                qvVar = new qv();
                                qvVar.MB = string;
                                hashMap.put(qvVar.MB, qvVar);
                                qvVar = (qv) hashMap.get(string);
                            }
                            if (qvVar.Oz == null) {
                                qvVar.Oz = new HashMap();
                            }
                            byte[] blob = al.getBlob(columnIndex3);
                            if (iW && blob != null) {
                                qvVar.Oz.put(str, v(al.getBlob(columnIndex3)));
                            } else {
                                qvVar.Oz.put(str, v(al.getBlob(columnIndex2)));
                            }
                        } catch (Exception e2) {
                            e = e2;
                            map = hashMap;
                        } catch (Throwable th2) {
                            th = th2;
                            map = hashMap;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        map = hashMap;
                        try {
                            e.printStackTrace();
                            if (al != null) {
                            }
                            this.om.close();
                            return map;
                        } catch (Throwable th3) {
                            th = th3;
                            if (al != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        map = hashMap;
                        if (al != null) {
                            al.close();
                        }
                        throw th;
                    }
                }
                if (al != null) {
                    al.close();
                }
                map = hashMap;
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (al != null) {
                    al.close();
                }
                this.om.close();
                return map;
            }
        }
        this.om.close();
        return map;
    }

    public boolean delUninstallPkg(String str) {
        if (str == null) {
            return false;
        }
        ArrayList arrayList = new ArrayList();
        String[] strArr = new String[]{str};
        arrayList.add(ContentProviderOperation.newDelete(this.om.an("up")).withSelection("info1 = ?", strArr).build());
        this.om.applyBatch(arrayList);
        this.om.close();
        return true;
    }

    public Map<String, String> j(String str, boolean z) {
        Map hashMap = new HashMap();
        Cursor al = this.om.al("SELECT info2,info3,info4 FROM dcp_info WHERE info1='" + str + "'");
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info2");
                int columnIndex2 = al.getColumnIndex("info3");
                int columnIndex3 = al.getColumnIndex("info4");
                while (al.moveToNext()) {
                    byte[] blob = al.getBlob(columnIndex3);
                    if (z && blob != null) {
                        hashMap.put(v(al.getBlob(columnIndex)), v(al.getBlob(columnIndex3)));
                    } else {
                        hashMap.put(v(al.getBlob(columnIndex)), v(al.getBlob(columnIndex2)));
                    }
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
        return hashMap;
    }

    public HashMap<String, qv> jA() {
        HashMap<String, qv> hashMap = new HashMap();
        Cursor al = this.om.al("SELECT info1,info2 FROM dcr_info");
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info1");
                int columnIndex2 = al.getColumnIndex("info2");
                while (al.moveToNext()) {
                    String string = al.getString(columnIndex);
                    byte[] blob = al.getBlob(columnIndex2);
                    qv qvVar = new qv();
                    qvVar.MB = string;
                    List b = qs.b(string, blob);
                    if (b != null) {
                        qvVar.C(b);
                        hashMap.put(string, qvVar);
                    }
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
        return hashMap;
    }

    public Map<String, Long> jB() {
        Map hashMap = new HashMap();
        Cursor al = this.om.al("SELECT info1,info2 FROM up");
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info1");
                int columnIndex2 = al.getColumnIndex("info2");
                while (al.moveToNext()) {
                    if (al.getString(columnIndex) != null) {
                        hashMap.put(al.getString(columnIndex), Long.valueOf(al.getLong(columnIndex2)));
                    }
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
        return hashMap;
    }

    public boolean jC() {
        boolean z = true;
        Cursor al = this.om.al("SELECT * FROM dcp_info");
        if (al != null && al.getCount() < 10) {
            z = false;
        }
        if (al != null) {
            al.close();
        }
        this.om.close();
        return z;
    }

    public List<String> y(List<am> list) {
        if (list == null) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (am amVar : list) {
            boolean z = false;
            String str = null;
            Map treeMap = new TreeMap();
            Iterator it = amVar.bv.iterator();
            while (it.hasNext()) {
                Map map = (Map) it.next();
                if (map.get(Integer.valueOf(2)) != null) {
                    str = ((String) map.get(Integer.valueOf(2))).toLowerCase();
                    z = ((String) map.get(Integer.valueOf(16))).equals("1");
                    if (z) {
                        break;
                    }
                } else if (map.get(Integer.valueOf(5)) != null) {
                    String str2 = (String) map.get(Integer.valueOf(6));
                    String str3 = (String) map.get(Integer.valueOf(5));
                    CharSequence charSequence = (String) map.get(Integer.valueOf(17));
                    if (TextUtils.isEmpty(charSequence)) {
                        Object charSequence2 = str2;
                    }
                    if (!(str2 == null || str3 == null)) {
                        treeMap.put(str3, new String[]{str2, charSequence2});
                    }
                }
            }
            if (str != null) {
                String str4 = "info1 = ?";
                String[] strArr = new String[]{str};
                arrayList2.add(ContentProviderOperation.newDelete(this.om.an("dcr_info")).withSelection(str4, strArr).build());
                arrayList2.add(ContentProviderOperation.newDelete(this.om.an("dcp_info")).withSelection(str4, strArr).build());
                if (!z) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("info1", str);
                    contentValues.put("info2", amVar.toByteArray());
                    arrayList2.add(ContentProviderOperation.newInsert(this.om.am("dcr_info")).withValues(contentValues).build());
                    for (Entry entry : treeMap.entrySet()) {
                        contentValues = new ContentValues();
                        contentValues.put("info2", lq.at(((String) entry.getKey()).toUpperCase()));
                        String[] strArr2 = (String[]) entry.getValue();
                        if (strArr2.length > 0) {
                            contentValues.put("info3", lq.at(strArr2[0].toUpperCase()));
                        }
                        if (strArr2.length > 1 && !TextUtils.isEmpty(strArr2[1])) {
                            contentValues.put("info4", lq.at(strArr2[1].toUpperCase()));
                        }
                        contentValues.put("info1", str);
                        arrayList2.add(ContentProviderOperation.newInsert(this.om.am("dcp_info")).withValues(contentValues).build());
                    }
                }
                arrayList.add(str);
            }
        }
        this.om.applyBatch(arrayList2);
        this.om.close();
        return arrayList;
    }
}
