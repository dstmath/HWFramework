package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public class gq {
    private static Object lock = new Object();
    private static gq oF;
    private jx nZ = ((kf) fj.D(9)).getPreferenceService("prfle_cnfg_dao");

    private gq() {
    }

    private String U(int i) {
        return "profile_quantity_" + i;
    }

    private String V(int i) {
        return "profile_last_enqueue_key_" + i;
    }

    public static gq aZ() {
        if (oF == null) {
            synchronized (lock) {
                if (oF == null) {
                    oF = new gq();
                }
            }
        }
        return oF;
    }

    private ar aj(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        try {
            JceInputStream jceInputStream = new JceInputStream(lq.at(str));
            jceInputStream.setServerEncoding("UTF-8");
            return (ar) jceInputStream.read(new ar(), 0, false);
        } catch (Throwable th) {
            return null;
        }
    }

    private String c(ar arVar) {
        if (arVar == null) {
            return "";
        }
        JceOutputStream jceOutputStream = new JceOutputStream();
        jceOutputStream.setServerEncoding("UTF-8");
        jceOutputStream.write((JceStruct) arVar, 0);
        return lq.bytesToHexString(jceOutputStream.toByteArray());
    }

    public ar R(int i) {
        String string = this.nZ.getString(V(i), null);
        return string != null ? aj(string) : null;
    }

    public int S(int i) {
        return this.nZ.getInt(U(i), 0);
    }

    public void T(int i) {
        this.nZ.putInt(U(i), 0);
    }

    public void a(ar arVar) {
        if (arVar != null) {
            this.nZ.putString(V(arVar.bK), c(arVar));
        }
    }

    public boolean b(ar arVar) {
        return gr.a(R(arVar.bK), arVar);
    }

    public int ba() {
        return this.nZ.getInt("profile_task_id", 0);
    }

    public void bb() {
        int ba = ba();
        if (ba < 0) {
            ba = 0;
        }
        this.nZ.putInt("profile_task_id", ba + 1);
    }

    public void g(boolean z) {
        this.nZ.putBoolean("profile_soft_list_upload_opened", z);
    }

    public void h(int i, int i2) {
        this.nZ.putInt(U(i), S(i) + i2);
    }

    public void i(int i, int i2) {
        this.nZ.putInt(U(i), S(i) - i2);
    }
}
