package tmsdkobf;

/* compiled from: Unknown */
public class fy extends py implements Cloneable {
    private int downloadCount;
    private String eD;
    private int jY;
    private int kz;
    private String nA;
    private int nB;
    private int nC;
    private String nD;
    private int nE;
    private int nF;
    private int nG;
    private boolean nH;
    private String nI;
    private String nJ;
    private String nK;
    private String nL;
    private int nM;
    private int nN;
    private String nO;
    private long nP;
    private String nQ;
    private String nR;
    private int nS;
    private int nT;
    private int nU;
    private String nV;
    private int nW;
    private int nt;
    private String nu;
    private float nv;
    private String nw;
    private long nx;
    private boolean ny;
    private int nz;

    public fy() {
        this.nt = -1;
        this.nu = "";
        this.nv = 0.0f;
        this.nw = "";
        this.jY = 0;
        this.eD = "";
        this.downloadCount = 0;
        this.nx = 0;
        this.ny = false;
        this.nz = -1;
        this.nA = "";
        this.nC = -1;
        this.nD = "";
        this.nE = -1;
        this.nF = 0;
        this.nG = -1;
        this.nH = false;
        this.nI = null;
        this.nJ = "";
        this.nK = "";
        this.nL = "";
        this.nM = 0;
        this.nN = 0;
        this.nO = "";
        this.nP = 0;
        this.nQ = "";
        this.nR = "";
        this.nS = 0;
        this.nT = 0;
        this.nU = 0;
        this.kz = 0;
        this.nV = "";
        this.nW = 0;
    }

    public int al() {
        return this.nt;
    }

    public boolean am() {
        return this.ny;
    }

    public void an(int i) {
        this.nt = i;
    }

    public void ao(int i) {
        this.nB = i;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        boolean z2 = false;
        if (obj != null && (obj instanceof fy)) {
            fy fyVar = (fy) obj;
            if (hG()) {
                if (!(fyVar.aZ() == null || aZ() == null)) {
                    if (aZ().toLowerCase().hashCode() == fyVar.aZ().toLowerCase().hashCode()) {
                        z2 = true;
                    }
                    return z2;
                }
            } else if (!(getPackageName() == null || fyVar.getPackageName() == null)) {
                if (getPackageName().hashCode() == fyVar.getPackageName().hashCode()) {
                    if (this.ny != fyVar.am()) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
        }
        return false;
    }

    public void l(int i) {
        super.l(i);
        ao(i);
    }
}
