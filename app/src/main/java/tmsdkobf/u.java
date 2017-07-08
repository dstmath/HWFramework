package tmsdkobf;

/* compiled from: Unknown */
public final class u extends fs {
    public int A;

    public u() {
        this.A = 0;
    }

    public fs newInit() {
        return new u();
    }

    public void readFrom(fq fqVar) {
        this.A = fqVar.a(this.A, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.A != 0) {
            frVar.write(this.A, 0);
        }
    }
}
