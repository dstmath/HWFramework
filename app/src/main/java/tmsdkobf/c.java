package tmsdkobf;

/* compiled from: Unknown */
public final class c extends fs {
    public int a;
    public int b;
    public int start;

    public c() {
        this.start = 0;
        this.a = 0;
        this.b = 0;
    }

    public fs newInit() {
        return new c();
    }

    public void readFrom(fq fqVar) {
        this.start = fqVar.a(this.start, 0, false);
        this.a = fqVar.a(this.a, 1, false);
        this.b = fqVar.a(this.b, 2, false);
    }

    public void writeTo(fr frVar) {
        if (this.start != 0) {
            frVar.write(this.start, 0);
        }
        if (this.a != 0) {
            frVar.write(this.a, 1);
        }
        if (this.b != 0) {
            frVar.write(this.b, 2);
        }
    }
}
