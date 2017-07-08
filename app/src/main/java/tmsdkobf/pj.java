package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdkobf.pr.b;

/* compiled from: Unknown */
public class pj extends os implements pp {
    private int HI;
    private ArrayList<Integer> l;

    public pj(Context context, boolean z, oq oqVar) {
        super(context, z, oqVar);
        this.l = new ArrayList();
        this.HI = 0;
        if (oqVar == null) {
            gK();
            return;
        }
        ArrayList aB = oqVar.aB();
        if (aB != null && aB.size() > 0) {
            this.l.clear();
            Iterator it = aB.iterator();
            while (it.hasNext()) {
                Integer num = (Integer) it.next();
                if (num != null) {
                    this.l.add(num);
                    pa.h("TcpIpPlot", "init ports : " + num);
                }
            }
            return;
        }
        gK();
    }

    private void gK() {
        this.l = new ArrayList();
        if (this.EA) {
            this.l.add(Integer.valueOf(8080));
            return;
        }
        this.l.add(Integer.valueOf(443));
        this.l.add(Integer.valueOf(14000));
        this.l.add(Integer.valueOf(8080));
    }

    public void K(boolean z) {
    }

    public void cm(int i) {
        if (this.l == null) {
            this.l = new ArrayList();
        }
        this.l.add(Integer.valueOf(i));
    }

    protected void fS() {
        this.HI = 0;
        super.fS();
    }

    public void gJ() {
        if (this.l == null) {
            this.l = new ArrayList();
        } else {
            this.l.clear();
        }
    }

    public void gL() {
        if (gd()) {
            this.HI++;
            if (this.l != null && this.l.size() <= this.HI) {
                this.HI = 0;
            }
        }
    }

    public b gM() {
        b bVar;
        String fZ = fZ();
        if (this.l != null && this.l.size() > this.HI) {
            bVar = new b(fZ, ((Integer) this.l.get(this.HI)).intValue());
        } else {
            pa.h("TcpIpPlot", "getPlotIPPoint error");
            gK();
            this.HI = 0;
            bVar = gM();
        }
        if (bVar != null) {
            pa.h("TcpIpPlot", "tcp ip : " + bVar.fZ() + " port : " + bVar.getPort());
        }
        return bVar;
    }

    public void gN() {
    }

    public int gO() {
        return gc();
    }

    public int gP() {
        return this.l == null ? 0 : this.l.size();
    }
}
