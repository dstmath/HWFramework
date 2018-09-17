package tmsdkobf;

/* compiled from: Unknown */
public final class od extends fs {
    public int Eb;
    public boolean Ec;

    public od() {
        this.Eb = 0;
        this.Ec = true;
    }

    public fs newInit() {
        return new od();
    }

    public void readFrom(fq fqVar) {
        this.Eb = fqVar.a(this.Eb, 0, true);
        this.Ec = fqVar.a(this.Ec, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.Eb, 0);
        frVar.a(this.Ec, 1);
    }
}
