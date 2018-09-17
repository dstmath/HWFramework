package tmsdkobf;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.Map;

public class rm {
    private static rm PT;
    private jv om = ((kf) fj.D(9)).ap("QQSecureProvider");

    private rm() {
    }

    public static rm km() {
        if (PT == null) {
            PT = new rm();
        }
        return PT;
    }

    public byte[] dm(String str) {
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
        return bArr;
    }

    public Map<String, String> j(String str, boolean z) {
        return qo.jz().j(str, z);
    }

    public ArrayList<String> kn() {
        ArrayList<String> arrayList = new ArrayList();
        Cursor al = this.om.al("SELECT info1 FROM dcr_info");
        if (al != null) {
            try {
                int columnIndex = al.getColumnIndex("info1");
                while (al.moveToNext()) {
                    arrayList.add(al.getString(columnIndex));
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
        return arrayList;
    }
}
