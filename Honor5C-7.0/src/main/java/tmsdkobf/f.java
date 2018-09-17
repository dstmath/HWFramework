package tmsdkobf;

/* compiled from: Unknown */
public final class f extends fs {
    public int authType;
    public int q;
    public String r;
    public String s;
    public String t;
    public int u;
    public int v;

    public f() {
        this.q = 2;
        this.authType = 0;
        this.r = "";
        this.s = "";
        this.t = "";
        this.u = 0;
        this.v = 0;
    }

    public fs newInit() {
        return new f();
    }

    public void readFrom(fq fqVar) {
        this.q = fqVar.a(this.q, 0, true);
        this.authType = fqVar.a(this.authType, 1, true);
        this.r = fqVar.a(2, false);
        this.s = fqVar.a(3, false);
        this.t = fqVar.a(4, false);
        this.u = fqVar.a(this.u, 5, false);
        this.v = fqVar.a(this.v, 6, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.q, 0);
        frVar.write(this.authType, 1);
        if (this.r != null) {
            frVar.a(this.r, 2);
        }
        if (this.s != null) {
            frVar.a(this.s, 3);
        }
        if (this.t != null) {
            frVar.a(this.t, 4);
        }
        if (this.u != 0) {
            frVar.write(this.u, 5);
        }
        if (this.v != 0) {
            frVar.write(this.v, 6);
        }
    }
}
