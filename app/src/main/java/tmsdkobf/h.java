package tmsdkobf;

/* compiled from: Unknown */
public final class h extends fs {
    public String C;
    public int D;
    public String title;
    public int type;

    public h() {
        this.title = "";
        this.C = "";
        this.type = 0;
        this.D = 0;
    }

    public fs newInit() {
        return new h();
    }

    public void readFrom(fq fqVar) {
        this.title = fqVar.a(0, true);
        this.C = fqVar.a(1, true);
        this.type = fqVar.a(this.type, 2, true);
        this.D = fqVar.a(this.D, 3, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.title, 0);
        frVar.a(this.C, 1);
        frVar.write(this.type, 2);
        frVar.write(this.D, 3);
    }
}
