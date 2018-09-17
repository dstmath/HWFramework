package tmsdkobf;

/* compiled from: Unknown */
public final class bb extends fs {
    public String postfix;
    public String prefix;
    public int type;
    public int unit;

    public bb() {
        this.unit = 0;
        this.type = 0;
        this.prefix = "";
        this.postfix = "";
    }

    public fs newInit() {
        return new bb();
    }

    public void readFrom(fq fqVar) {
        this.unit = fqVar.a(this.unit, 0, true);
        this.type = fqVar.a(this.type, 1, true);
        this.prefix = fqVar.a(2, true);
        this.postfix = fqVar.a(3, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.unit, 0);
        frVar.write(this.type, 1);
        frVar.a(this.prefix, 2);
        if (this.postfix != null) {
            frVar.a(this.postfix, 3);
        }
    }
}
