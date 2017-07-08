package tmsdkobf;

/* compiled from: Unknown */
public final class v extends fs {
    public String C;
    public int ae;
    public int af;
    public int bgColor;
    public String title;
    public int type;

    public v() {
        this.title = "";
        this.C = "";
        this.type = 0;
        this.ae = 0;
        this.af = 0;
        this.bgColor = 0;
    }

    public fs newInit() {
        return new v();
    }

    public void readFrom(fq fqVar) {
        this.title = fqVar.a(0, false);
        this.C = fqVar.a(1, false);
        this.type = fqVar.a(this.type, 2, false);
        this.ae = fqVar.a(this.ae, 3, false);
        this.af = fqVar.a(this.af, 4, false);
        this.bgColor = fqVar.a(this.bgColor, 5, false);
    }

    public void writeTo(fr frVar) {
        if (this.title != null) {
            frVar.a(this.title, 0);
        }
        if (this.C != null) {
            frVar.a(this.C, 1);
        }
        if (this.type != 0) {
            frVar.write(this.type, 2);
        }
        if (this.ae != 0) {
            frVar.write(this.ae, 3);
        }
        if (this.af != 0) {
            frVar.write(this.af, 4);
        }
        if (this.bgColor != 0) {
            frVar.write(this.bgColor, 5);
        }
    }
}
