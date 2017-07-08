package tmsdkobf;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tmsdkobf.gw.a;

/* compiled from: Unknown */
public class gs {
    private static gs pp;
    private lc ok;

    private gs() {
        this.ok = ((ln) fe.ad(9)).bp("QQSecureProvider");
    }

    public static gs aW() {
        if (pp == null) {
            pp = new gs();
        }
        return pp;
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

    public List<a> aQ(String str) {
        return c(str, hb.bo());
    }

    public long aR(String str) {
        if (str == null) {
            return -1;
        }
        Cursor bl = this.ok.bl("SELECT info1,info2 FROM up WHERE info1='" + str + "'");
        if (bl != null) {
            try {
                int columnIndex = bl.getColumnIndex("info1");
                int columnIndex2 = bl.getColumnIndex("info2");
                do {
                    if (!bl.moveToNext()) {
                        if (bl != null) {
                            bl.close();
                        }
                    }
                } while (bl.getString(columnIndex) == null);
                long j = bl.getLong(columnIndex2);
                if (bl != null) {
                    bl.close();
                }
                return j;
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
        return -1;
    }

    public HashMap<String, gw> aX() {
        HashMap<String, gw> hashMap = new HashMap();
        Cursor bl = this.ok.bl("SELECT info1 FROM dcr_info");
        if (bl != null) {
            try {
                int columnIndex = bl.getColumnIndex("info1");
                while (bl.moveToNext()) {
                    hashMap.put(bl.getString(columnIndex), null);
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
        return hashMap;
    }

    public ArrayList<String> aY() {
        ArrayList<String> arrayList = new ArrayList();
        Cursor bl = this.ok.bl("SELECT info1 FROM dcr_info");
        if (bl != null) {
            try {
                int columnIndex = bl.getColumnIndex("info1");
                while (bl.moveToNext()) {
                    arrayList.add(bl.getString(columnIndex));
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
        return arrayList;
    }

    public List<a> c(String str, boolean z) {
        List<a> list = null;
        if (str == null) {
            return null;
        }
        Cursor bl = this.ok.bl("SELECT info2,info3,info4 FROM dcp_info WHERE info1='" + str + "'");
        if (bl != null) {
            List<a> arrayList = new ArrayList();
            try {
                int columnIndex = bl.getColumnIndex("info2");
                int columnIndex2 = bl.getColumnIndex("info3");
                int columnIndex3 = bl.getColumnIndex("info4");
                while (bl.moveToNext()) {
                    a aVar = new a();
                    aVar.pC = bl.getBlob(columnIndex);
                    aVar.pD = !z ? bl.getBlob(columnIndex2) : bl.getBlob(columnIndex3);
                    if (aVar.pD == null || aVar.pD.length < 1) {
                        aVar.pD = bl.getBlob(columnIndex2);
                    }
                    arrayList.add(aVar);
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
            list = arrayList;
        }
        this.ok.close();
        return list;
    }
}
