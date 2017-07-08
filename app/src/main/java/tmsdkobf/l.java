package tmsdkobf;

/* compiled from: Unknown */
public final class l extends fs {
    public String I;
    public String J;
    public String K;
    public String L;
    public String M;

    public l() {
        this.I = "";
        this.J = "";
        this.K = "";
        this.L = "";
        this.M = "";
    }

    public fs newInit() {
        return new l();
    }

    public void readFrom(fq fqVar) {
        this.I = fqVar.a(0, false);
        this.J = fqVar.a(1, false);
        this.K = fqVar.a(2, false);
        this.L = fqVar.a(3, false);
        this.M = fqVar.a(4, false);
    }

    public void writeTo(fr frVar) {
        if (this.I != null) {
            frVar.a(this.I, 0);
        }
        if (this.J != null) {
            frVar.a(this.J, 1);
        }
        if (this.K != null) {
            frVar.a(this.K, 2);
        }
        if (this.L != null) {
            frVar.a(this.L, 3);
        }
        if (this.M != null) {
            frVar.a(this.M, 4);
        }
    }
}
