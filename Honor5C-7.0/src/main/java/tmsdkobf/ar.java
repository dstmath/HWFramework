package tmsdkobf;

/* compiled from: Unknown */
public final class ar extends fs {
    public String bA;
    public int bB;
    public int bC;
    public String imsi;

    public ar() {
        this.bA = "";
        this.bB = 0;
        this.imsi = "";
        this.bC = 0;
    }

    public fs newInit() {
        return new ar();
    }

    public void readFrom(fq fqVar) {
        this.bA = fqVar.a(0, true);
        this.bB = fqVar.a(this.bB, 1, true);
        this.imsi = fqVar.a(2, false);
        this.bC = fqVar.a(this.bC, 3, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bA, 0);
        frVar.write(this.bB, 1);
        if (this.imsi != null) {
            frVar.a(this.imsi, 2);
        }
        if (this.bC != 0) {
            frVar.write(this.bC, 3);
        }
    }
}
