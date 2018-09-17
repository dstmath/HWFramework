package tmsdkobf;

/* compiled from: Unknown */
public final class be extends fs {
    public int ch;
    public int ci;
    public int city;
    public long cj;
    public int ck;
    public boolean cl;
    public int cm;
    public int province;

    public be() {
        this.province = 0;
        this.city = 0;
        this.ch = 0;
        this.ci = 0;
        this.cj = -1;
        this.ck = -1;
        this.cl = true;
        this.cm = 255;
    }

    public fs newInit() {
        return new be();
    }

    public void readFrom(fq fqVar) {
        this.province = fqVar.a(this.province, 0, true);
        this.city = fqVar.a(this.city, 1, true);
        this.ch = fqVar.a(this.ch, 2, true);
        this.ci = fqVar.a(this.ci, 3, false);
        this.cj = fqVar.a(this.cj, 4, false);
        this.ck = fqVar.a(this.ck, 5, false);
        this.cl = fqVar.a(this.cl, 6, false);
        this.cm = fqVar.a(this.cm, 7, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.province, 0);
        frVar.write(this.city, 1);
        frVar.write(this.ch, 2);
        if (this.ci != 0) {
            frVar.write(this.ci, 3);
        }
        if (this.cj != -1) {
            frVar.b(this.cj, 4);
        }
        if (this.ck != -1) {
            frVar.write(this.ck, 5);
        }
        if (!this.cl) {
            frVar.a(this.cl, 6);
        }
        if (this.cm != 255) {
            frVar.write(this.cm, 7);
        }
    }
}
