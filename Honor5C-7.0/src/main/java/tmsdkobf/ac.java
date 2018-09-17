package tmsdkobf;

/* compiled from: Unknown */
public final class ac extends fs {
    public String aA;
    public int aB;
    public String fileName;
    public int type;

    public ac() {
        this.fileName = "";
        this.aA = "";
        this.type = 1;
        this.aB = 0;
    }

    public fs newInit() {
        return new ac();
    }

    public void readFrom(fq fqVar) {
        this.fileName = fqVar.a(0, true);
        this.aA = fqVar.a(1, true);
        this.type = fqVar.a(this.type, 2, false);
        this.aB = fqVar.a(this.aB, 3, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fileName, 0);
        frVar.a(this.aA, 1);
        if (1 != this.type) {
            frVar.write(this.type, 2);
        }
        frVar.write(this.aB, 3);
    }
}
