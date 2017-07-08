package tmsdkobf;

/* compiled from: Unknown */
public final class ck extends fs {
    public String description;
    public String flawName;
    public String maliceBody;
    public String maliceTitle;
    public long maliceType;
    public String screenshotUrl;
    public String title;
    public String webIconUrl;

    public ck() {
        this.title = "";
        this.description = "";
        this.webIconUrl = "";
        this.screenshotUrl = "";
        this.maliceType = 0;
        this.maliceTitle = "";
        this.maliceBody = "";
        this.flawName = "";
    }

    public fs newInit() {
        return new ck();
    }

    public void readFrom(fq fqVar) {
        this.title = fqVar.a(0, false);
        this.description = fqVar.a(1, false);
        this.webIconUrl = fqVar.a(2, false);
        this.screenshotUrl = fqVar.a(3, false);
        this.maliceType = fqVar.a(this.maliceType, 4, false);
        this.maliceTitle = fqVar.a(5, false);
        this.maliceBody = fqVar.a(6, false);
        this.flawName = fqVar.a(7, false);
    }

    public void writeTo(fr frVar) {
        if (this.title != null) {
            frVar.a(this.title, 0);
        }
        if (this.description != null) {
            frVar.a(this.description, 1);
        }
        if (this.webIconUrl != null) {
            frVar.a(this.webIconUrl, 2);
        }
        if (this.screenshotUrl != null) {
            frVar.a(this.screenshotUrl, 3);
        }
        if (this.maliceType != 0) {
            frVar.b(this.maliceType, 4);
        }
        if (this.maliceTitle != null) {
            frVar.a(this.maliceTitle, 5);
        }
        if (this.maliceBody != null) {
            frVar.a(this.maliceBody, 6);
        }
        if (this.flawName != null) {
            frVar.a(this.flawName, 7);
        }
    }
}
