package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class fv extends JceStruct {
    public int R = 0;
    public boolean no = false;
    public String np = "";
    public String nq = "";
    public String nr = "";
    public String ns = "";
    public String nt = "";
    public String nu = "";
    public String nv = "";
    public ArrayList<String> nw = new ArrayList();

    public ArrayList<String> H() {
        return this.nw;
    }

    public void H(int i) {
        this.R = i;
    }

    public void O(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.np = str;
    }

    public void P(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.nq = str;
    }

    public void Q(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.nr = str;
    }

    public void R(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.ns = str;
    }

    public void S(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.nt = str;
    }

    public void T(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.nu = str;
    }

    public void U(String str) {
        if (str != null) {
            this.nw.add(str);
        }
        this.nv = str;
    }

    public void d(boolean z) {
        this.no = z;
    }

    public void readFrom(JceInputStream jceInputStream) {
        d(jceInputStream.read(this.no, 0, true));
        if (this.nw != null) {
            this.nw.clear();
        }
        O(jceInputStream.readString(1, true));
        P(jceInputStream.readString(2, true));
        Q(jceInputStream.readString(3, true));
        R(jceInputStream.readString(4, true));
        S(jceInputStream.readString(5, true));
        T(jceInputStream.readString(6, true));
        U(jceInputStream.readString(7, true));
        H(jceInputStream.read(this.R, 8, true));
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.no, 0);
        if (this.np != null) {
            jceOutputStream.write(this.np, 1);
        }
        if (this.nq != null) {
            jceOutputStream.write(this.nq, 2);
        }
        if (this.nr != null) {
            jceOutputStream.write(this.nr, 3);
        }
        if (this.ns != null) {
            jceOutputStream.write(this.ns, 4);
        }
        if (this.nt != null) {
            jceOutputStream.write(this.nt, 5);
        }
        if (this.nu != null) {
            jceOutputStream.write(this.nu, 6);
        }
        if (this.nv != null) {
            jceOutputStream.write(this.nv, 7);
        }
        jceOutputStream.write(this.R, 8);
    }
}
