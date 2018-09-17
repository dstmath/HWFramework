package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ae extends JceStruct {
    public int aE = 0;
    public int aJ = -1;
    public int aK = -1;
    public int aL = 0;
    public String aM = "";
    public String aN = "";
    public int aO = -1;
    public int aP = 0;
    public String checkSum = "";
    public String downNetName = "";
    public int downSize = -1;
    public byte downType = (byte) 0;
    public int downnetType = 0;
    public int errorCode = 0;
    public String errorMsg = "";
    public int fileSize = 0;
    public int rssi = -1;
    public int sdcardStatus = -1;
    public byte success = (byte) 1;
    public int timestamp = -1;
    public String url = "";

    public JceStruct newInit() {
        return new ae();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aE = jceInputStream.read(this.aE, 0, true);
        this.checkSum = jceInputStream.readString(1, false);
        this.timestamp = jceInputStream.read(this.timestamp, 2, false);
        this.url = jceInputStream.readString(3, false);
        this.success = (byte) jceInputStream.read(this.success, 4, false);
        this.downSize = jceInputStream.read(this.downSize, 5, false);
        this.aJ = jceInputStream.read(this.aJ, 6, false);
        this.aK = jceInputStream.read(this.aK, 7, false);
        this.downType = (byte) jceInputStream.read(this.downType, 8, false);
        this.errorCode = jceInputStream.read(this.errorCode, 9, false);
        this.downnetType = jceInputStream.read(this.downnetType, 10, false);
        this.downNetName = jceInputStream.readString(11, false);
        this.aL = jceInputStream.read(this.aL, 12, false);
        this.aM = jceInputStream.readString(13, false);
        this.errorMsg = jceInputStream.readString(14, false);
        this.rssi = jceInputStream.read(this.rssi, 15, false);
        this.sdcardStatus = jceInputStream.read(this.sdcardStatus, 16, false);
        this.fileSize = jceInputStream.read(this.fileSize, 17, false);
        this.aN = jceInputStream.readString(18, false);
        this.aO = jceInputStream.read(this.aO, 19, false);
        this.aP = jceInputStream.read(this.aP, 20, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aE, 0);
        if (this.checkSum != null) {
            jceOutputStream.write(this.checkSum, 1);
        }
        if (this.timestamp != -1) {
            jceOutputStream.write(this.timestamp, 2);
        }
        if (this.url != null) {
            jceOutputStream.write(this.url, 3);
        }
        if (this.success != (byte) 1) {
            jceOutputStream.write(this.success, 4);
        }
        if (this.downSize != -1) {
            jceOutputStream.write(this.downSize, 5);
        }
        if (this.aJ != -1) {
            jceOutputStream.write(this.aJ, 6);
        }
        if (this.aK != -1) {
            jceOutputStream.write(this.aK, 7);
        }
        if (this.downType != (byte) 0) {
            jceOutputStream.write(this.downType, 8);
        }
        if (this.errorCode != 0) {
            jceOutputStream.write(this.errorCode, 9);
        }
        if (this.downnetType != 0) {
            jceOutputStream.write(this.downnetType, 10);
        }
        if (this.downNetName != null) {
            jceOutputStream.write(this.downNetName, 11);
        }
        if (this.aL != 0) {
            jceOutputStream.write(this.aL, 12);
        }
        if (this.aM != null) {
            jceOutputStream.write(this.aM, 13);
        }
        if (this.errorMsg != null) {
            jceOutputStream.write(this.errorMsg, 14);
        }
        if (this.rssi != -1) {
            jceOutputStream.write(this.rssi, 15);
        }
        if (this.sdcardStatus != -1) {
            jceOutputStream.write(this.sdcardStatus, 16);
        }
        if (this.fileSize != 0) {
            jceOutputStream.write(this.fileSize, 17);
        }
        if (this.aN != null) {
            jceOutputStream.write(this.aN, 18);
        }
        if (this.aO != -1) {
            jceOutputStream.write(this.aO, 19);
        }
        if (this.aP != 0) {
            jceOutputStream.write(this.aP, 20);
        }
    }
}
