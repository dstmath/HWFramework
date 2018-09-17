package tmsdkobf;

/* compiled from: Unknown */
public final class lx extends fs {
    public String bA;
    public String url;
    public int zB;

    public lx() {
        this.bA = "";
        this.url = "";
        this.zB = 0;
    }

    public void readFrom(fq fqVar) {
        this.bA = fqVar.a(0, true);
        this.url = fqVar.a(1, true);
        this.zB = fqVar.a(this.zB, 2, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bA, 0);
        frVar.a(this.url, 1);
        frVar.write(this.zB, 2);
    }
}
