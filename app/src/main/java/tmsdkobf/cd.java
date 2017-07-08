package tmsdkobf;

/* compiled from: Unknown */
public final class cd extends fs {
    public int eV;
    public int eW;
    public int eX;
    public int eY;
    public int eZ;
    public String ej;
    public String fa;
    public int localTagType;
    public String originName;
    public int scene;
    public int tagType;
    public String userDefineName;

    public cd() {
        this.ej = "";
        this.eV = 0;
        this.eW = 0;
        this.eX = 0;
        this.eY = 0;
        this.eZ = 0;
        this.tagType = 0;
        this.originName = "";
        this.userDefineName = "";
        this.scene = 0;
        this.localTagType = 0;
        this.fa = "";
    }

    public fs newInit() {
        return new cd();
    }

    public void readFrom(fq fqVar) {
        this.ej = fqVar.a(0, true);
        this.eV = fqVar.a(this.eV, 1, true);
        this.eW = fqVar.a(this.eW, 2, false);
        this.eX = fqVar.a(this.eX, 3, false);
        this.eY = fqVar.a(this.eY, 4, false);
        this.eZ = fqVar.a(this.eZ, 5, false);
        this.tagType = fqVar.a(this.tagType, 6, false);
        this.originName = fqVar.a(7, false);
        this.userDefineName = fqVar.a(8, false);
        this.scene = fqVar.a(this.scene, 9, false);
        this.localTagType = fqVar.a(this.localTagType, 10, false);
        this.fa = fqVar.a(11, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ej, 0);
        frVar.write(this.eV, 1);
        if (this.eW != 0) {
            frVar.write(this.eW, 2);
        }
        if (this.eX != 0) {
            frVar.write(this.eX, 3);
        }
        if (this.eY != 0) {
            frVar.write(this.eY, 4);
        }
        if (this.eZ != 0) {
            frVar.write(this.eZ, 5);
        }
        if (this.tagType != 0) {
            frVar.write(this.tagType, 6);
        }
        if (this.originName != null) {
            frVar.a(this.originName, 7);
        }
        if (this.userDefineName != null) {
            frVar.a(this.userDefineName, 8);
        }
        if (this.scene != 0) {
            frVar.write(this.scene, 9);
        }
        if (this.localTagType != 0) {
            frVar.write(this.localTagType, 10);
        }
        if (this.fa != null) {
            frVar.a(this.fa, 11);
        }
    }
}
