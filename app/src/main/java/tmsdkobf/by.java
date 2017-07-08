package tmsdkobf;

/* compiled from: Unknown */
public final class by extends fs {
    public String ej;
    public int ek;

    public by() {
        this.ej = "";
        this.ek = 0;
    }

    public fs newInit() {
        return new by();
    }

    public void readFrom(fq fqVar) {
        this.ej = fqVar.a(0, true);
        this.ek = fqVar.a(this.ek, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ej, 0);
        if (this.ek != 0) {
            frVar.write(this.ek, 1);
        }
    }
}
