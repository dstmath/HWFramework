package tmsdkobf;

/* compiled from: Unknown */
public final class k extends fs {
    public int G;
    public int H;

    public k() {
        this.G = 0;
        this.H = 0;
    }

    public fs newInit() {
        return new k();
    }

    public void readFrom(fq fqVar) {
        this.G = fqVar.a(this.G, 0, false);
        this.H = fqVar.a(this.H, 1, false);
    }

    public void writeTo(fr frVar) {
        if (this.G != 0) {
            frVar.write(this.G, 0);
        }
        if (this.H != 0) {
            frVar.write(this.H, 1);
        }
    }
}
