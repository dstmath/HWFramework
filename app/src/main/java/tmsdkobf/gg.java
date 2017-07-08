package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/* compiled from: Unknown */
public class gg {
    private static gg oj;
    private lc ok;

    private gg() {
        this.ok = ((ln) fe.ad(9)).bp("QQSecureProvider");
    }

    public static gg aI() {
        if (oj == null) {
            oj = new gg();
        }
        return oj;
    }

    public List<String> a(List<ah> list) {
        if (list == null) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (ah ahVar : list) {
            Map treeMap = new TreeMap();
            Iterator it = ahVar.aV.iterator();
            String str = null;
            boolean z = false;
            while (it.hasNext()) {
                String str2;
                Map map = (Map) it.next();
                String toLowerCase;
                if (map.get(Integer.valueOf(2)) != null) {
                    toLowerCase = ((String) map.get(Integer.valueOf(2))).toLowerCase();
                    boolean equals = ((String) map.get(Integer.valueOf(16))).equals("1");
                    if (equals) {
                        str = toLowerCase;
                        z = equals;
                        break;
                    }
                    str = toLowerCase;
                    z = equals;
                } else if (map.get(Integer.valueOf(5)) != null) {
                    toLowerCase = (String) map.get(Integer.valueOf(6));
                    String str3 = (String) map.get(Integer.valueOf(5));
                    str2 = (String) map.get(Integer.valueOf(17));
                    if (TextUtils.isEmpty(str2)) {
                        str2 = toLowerCase;
                    }
                    if (!(toLowerCase == null || str3 == null)) {
                        treeMap.put(str3, toLowerCase + "\n" + str2);
                    }
                }
            }
            if (str != null) {
                str2 = "info1 = ?";
                String[] strArr = new String[]{str};
                arrayList2.add(ContentProviderOperation.newDelete(this.ok.bn("dcr_info")).withSelection(str2, strArr).build());
                arrayList2.add(ContentProviderOperation.newDelete(this.ok.bn("dcp_info")).withSelection(str2, strArr).build());
                if (!z) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("info1", str);
                    contentValues.put("info2", ahVar.toByteArray());
                    arrayList2.add(ContentProviderOperation.newInsert(this.ok.bm("dcr_info")).withValues(contentValues).build());
                    for (Entry entry : treeMap.entrySet()) {
                        ContentValues contentValues2 = new ContentValues();
                        contentValues2.put("info2", mo.cw(((String) entry.getKey()).toUpperCase()));
                        String[] split = ((String) entry.getValue()).split("\n");
                        if (split.length > 0) {
                            contentValues2.put("info3", mo.cw(split[0].toUpperCase()));
                        }
                        if (split.length > 1) {
                            contentValues2.put("info4", mo.cw(split[1].toUpperCase()));
                        }
                        contentValues2.put("info1", str);
                        arrayList2.add(ContentProviderOperation.newInsert(this.ok.bm("dcp_info")).withValues(contentValues2).build());
                    }
                }
                arrayList.add(str);
            }
        }
        this.ok.applyBatch(arrayList2);
        this.ok.close();
        return arrayList;
    }

    public Map<String, byte[]> a(String str, boolean z) {
        Map hashMap = new HashMap();
        Cursor bl = this.ok.bl("SELECT info2,info3,info4 FROM dcp_info WHERE info1='" + str + "'");
        if (bl != null) {
            int columnIndex = bl.getColumnIndex("info2");
            int columnIndex2 = bl.getColumnIndex("info3");
            int columnIndex3 = bl.getColumnIndex("info4");
            while (bl.moveToNext()) {
                if (bl.getBlob(columnIndex3) == null) {
                    try {
                        hashMap.put(fm.c(bl.getBlob(columnIndex)), bl.getBlob(columnIndex2));
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (bl != null) {
                            bl.close();
                        }
                    } catch (Throwable th) {
                        if (bl != null) {
                            bl.close();
                        }
                    }
                } else if (z) {
                    hashMap.put(fm.c(bl.getBlob(columnIndex)), bl.getBlob(columnIndex3));
                } else {
                    hashMap.put(fm.c(bl.getBlob(columnIndex)), bl.getBlob(columnIndex2));
                }
            }
            if (bl != null) {
                bl.close();
            }
        }
        this.ok.close();
        return hashMap;
    }

    public List<gi> aD(String str) {
        List<gi> list;
        Exception e;
        Cursor bl = this.ok.bl("SELECT info1 FROM dcp_info WHERE info2=(x'" + str + "')");
        if (bl == null) {
            list = null;
        } else {
            try {
                list = new ArrayList();
                try {
                    int columnIndex = bl.getColumnIndex("info1");
                    while (bl.moveToNext()) {
                        gi giVar = new gi();
                        giVar.om = bl.getString(columnIndex);
                        list.add(giVar);
                    }
                    if (bl != null) {
                        bl.close();
                    }
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
                list = null;
                try {
                    e.printStackTrace();
                    if (bl != null) {
                        bl.close();
                    }
                    this.ok.close();
                    return list;
                } catch (Throwable th) {
                    if (bl != null) {
                        bl.close();
                    }
                }
            }
        }
        this.ok.close();
        return list;
    }

    public byte[] aE(String str) {
        byte[] bArr = null;
        Cursor bl = this.ok.bl("SELECT info2 FROM dcr_info WHERE info1='" + str + "'");
        if (bl != null) {
            try {
                int columnIndex = bl.getColumnIndex("info2");
                while (bl.moveToNext()) {
                    bArr = bl.getBlob(columnIndex);
                }
                if (bl != null) {
                    bl.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (bl != null) {
                    bl.close();
                }
            } catch (Throwable th) {
                if (bl != null) {
                    bl.close();
                }
            }
        }
        this.ok.close();
        return bArr;
    }

    public void aF(String str) {
        if (str != null) {
            ArrayList arrayList = new ArrayList();
            String[] strArr = new String[]{str};
            arrayList.add(ContentProviderOperation.newDelete(this.ok.bn("up")).withSelection("info1 = ?", strArr).build());
            ContentValues contentValues = new ContentValues();
            contentValues.put("info1", str);
            contentValues.put("info2", Long.valueOf(System.currentTimeMillis()));
            arrayList.add(ContentProviderOperation.newInsert(this.ok.bm("up")).withValues(contentValues).build());
            this.ok.applyBatch(arrayList);
            this.ok.close();
        }
    }
}
