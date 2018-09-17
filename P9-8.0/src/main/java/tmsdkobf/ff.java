package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ff extends JceStruct {
    static ArrayList<fe> lW;
    static ArrayList<dw> lX;
    static ArrayList<String> lY;
    static ArrayList<String> lZ;
    static ArrayList<Integer> ma;
    public int category = 0;
    public int cn = 0;
    public String description = "";
    public int dp = 0;
    public int id = 0;
    public byte lI = (byte) 0;
    public ArrayList<fe> lJ = null;
    public int lK = 0;
    public int lL = 0;
    public int lM = 0;
    public int lN = 0;
    public int lO = 0;
    public int lP = 0;
    public ArrayList<dw> lQ = null;
    public int lR = 0;
    public int lS = 0;
    public ArrayList<String> lT = null;
    public ArrayList<String> lU = null;
    public ArrayList<Integer> lV = null;
    public String label = "";
    public int level = 0;
    public String name = "";
    public int timestamp = 0;
    public String url = "";

    public void readFrom(JceInputStream jceInputStream) {
        this.id = jceInputStream.read(this.id, 0, true);
        this.name = jceInputStream.readString(1, true);
        this.timestamp = jceInputStream.read(this.timestamp, 2, true);
        this.lI = (byte) jceInputStream.read(this.lI, 3, true);
        this.description = jceInputStream.readString(4, true);
        if (lW == null) {
            lW = new ArrayList();
            lW.add(new fe());
        }
        this.lJ = (ArrayList) jceInputStream.read(lW, 5, true);
        this.lK = jceInputStream.read(this.lK, 6, false);
        this.lL = jceInputStream.read(this.lL, 7, false);
        this.label = jceInputStream.readString(8, false);
        this.lM = jceInputStream.read(this.lM, 9, false);
        this.lN = jceInputStream.read(this.lN, 10, false);
        this.level = jceInputStream.read(this.level, 11, false);
        this.cn = jceInputStream.read(this.cn, 12, false);
        this.url = jceInputStream.readString(13, false);
        this.lO = jceInputStream.read(this.lO, 14, false);
        this.lP = jceInputStream.read(this.lP, 15, false);
        if (lX == null) {
            lX = new ArrayList();
            lX.add(new dw());
        }
        this.lQ = (ArrayList) jceInputStream.read(lX, 16, false);
        this.dp = jceInputStream.read(this.dp, 17, false);
        this.lR = jceInputStream.read(this.lR, 18, false);
        this.lS = jceInputStream.read(this.lS, 19, false);
        if (lY == null) {
            lY = new ArrayList();
            lY.add("");
        }
        this.lT = (ArrayList) jceInputStream.read(lY, 20, false);
        if (lZ == null) {
            lZ = new ArrayList();
            lZ.add("");
        }
        this.lU = (ArrayList) jceInputStream.read(lZ, 21, false);
        if (ma == null) {
            ma = new ArrayList();
            ma.add(Integer.valueOf(0));
        }
        this.lV = (ArrayList) jceInputStream.read(ma, 22, false);
        this.category = jceInputStream.read(this.category, 23, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.id, 0);
        jceOutputStream.write(this.name, 1);
        jceOutputStream.write(this.timestamp, 2);
        jceOutputStream.write(this.lI, 3);
        jceOutputStream.write(this.description, 4);
        jceOutputStream.write(this.lJ, 5);
        jceOutputStream.write(this.lK, 6);
        jceOutputStream.write(this.lL, 7);
        if (this.label != null) {
            jceOutputStream.write(this.label, 8);
        }
        jceOutputStream.write(this.lM, 9);
        jceOutputStream.write(this.lN, 10);
        jceOutputStream.write(this.level, 11);
        jceOutputStream.write(this.cn, 12);
        if (this.url != null) {
            jceOutputStream.write(this.url, 13);
        }
        jceOutputStream.write(this.lO, 14);
        jceOutputStream.write(this.lP, 15);
        if (this.lQ != null) {
            jceOutputStream.write(this.lQ, 16);
        }
        jceOutputStream.write(this.dp, 17);
        jceOutputStream.write(this.lR, 18);
        jceOutputStream.write(this.lS, 19);
        if (this.lT != null) {
            jceOutputStream.write(this.lT, 20);
        }
        if (this.lU != null) {
            jceOutputStream.write(this.lU, 21);
        }
        if (this.lV != null) {
            jceOutputStream.write(this.lV, 22);
        }
        jceOutputStream.write(this.category, 23);
    }
}
