package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cy extends JceStruct {
    public String description = "";
    public String flawName = "";
    public String maliceBody = "";
    public String maliceTitle = "";
    public long maliceType = 0;
    public String screenshotUrl = "";
    public String title = "";
    public String webIconUrl = "";

    public JceStruct newInit() {
        return new cy();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.title = jceInputStream.readString(0, false);
        this.description = jceInputStream.readString(1, false);
        this.webIconUrl = jceInputStream.readString(2, false);
        this.screenshotUrl = jceInputStream.readString(3, false);
        this.maliceType = jceInputStream.read(this.maliceType, 4, false);
        this.maliceTitle = jceInputStream.readString(5, false);
        this.maliceBody = jceInputStream.readString(6, false);
        this.flawName = jceInputStream.readString(7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.title != null) {
            jceOutputStream.write(this.title, 0);
        }
        if (this.description != null) {
            jceOutputStream.write(this.description, 1);
        }
        if (this.webIconUrl != null) {
            jceOutputStream.write(this.webIconUrl, 2);
        }
        if (this.screenshotUrl != null) {
            jceOutputStream.write(this.screenshotUrl, 3);
        }
        if (this.maliceType != 0) {
            jceOutputStream.write(this.maliceType, 4);
        }
        if (this.maliceTitle != null) {
            jceOutputStream.write(this.maliceTitle, 5);
        }
        if (this.maliceBody != null) {
            jceOutputStream.write(this.maliceBody, 6);
        }
        if (this.flawName != null) {
            jceOutputStream.write(this.flawName, 7);
        }
    }
}
