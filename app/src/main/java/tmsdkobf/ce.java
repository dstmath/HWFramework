package tmsdkobf;

/* compiled from: Unknown */
public final class ce extends fs {
    public int eV;
    public int time;

    public ce() {
        this.eV = 0;
        this.time = 0;
    }

    public fs newInit() {
        return new ce();
    }

    public void readFrom(fq fqVar) {
        this.eV = fqVar.a(this.eV, 0, true);
        this.time = fqVar.a(this.time, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.eV, 0);
        frVar.write(this.time, 1);
    }
}
