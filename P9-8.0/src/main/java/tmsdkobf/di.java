package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class di extends JceStruct {
    static byte[] hb = new byte[1];
    static byte[] hc = new byte[1];
    static ArrayList<Integer> hd = new ArrayList();
    static ArrayList<byte[]> he = new ArrayList();
    static ArrayList<ArrayList<byte[]>> hf = new ArrayList();
    public int gI = 0;
    public byte[] gJ = null;
    public String gK = "";
    public byte[] gL = null;
    public long gM = 0;
    public String gN = "";
    public int gO = 0;
    public String gP = "";
    public int gQ = 0;
    public String gR = "";
    public int gS = 0;
    public int gT = 0;
    public int gU = 0;
    public ArrayList<Integer> gV = null;
    public int gW = 0;
    public boolean gX = false;
    public int gY = 0;
    public ArrayList<byte[]> gZ = null;
    public int gn = -1;
    public ArrayList<ArrayList<byte[]>> ha = null;
    public int official = 0;

    static {
        hb[0] = (byte) 0;
        hc[0] = (byte) 0;
        hd.add(Integer.valueOf(0));
        byte[] bArr = new byte[1];
        bArr[0] = (byte) 0;
        he.add(bArr);
        ArrayList arrayList = new ArrayList();
        byte[] bArr2 = new byte[1];
        bArr2[0] = (byte) 0;
        arrayList.add(bArr2);
        hf.add(arrayList);
    }

    public JceStruct newInit() {
        return new di();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gI = jceInputStream.read(this.gI, 0, true);
        this.gJ = jceInputStream.read(hb, 1, false);
        this.gK = jceInputStream.readString(2, false);
        this.gL = jceInputStream.read(hc, 3, false);
        this.gM = jceInputStream.read(this.gM, 4, false);
        this.gN = jceInputStream.readString(5, false);
        this.gO = jceInputStream.read(this.gO, 6, false);
        this.gP = jceInputStream.readString(7, false);
        this.gQ = jceInputStream.read(this.gQ, 8, false);
        this.gR = jceInputStream.readString(9, false);
        this.gS = jceInputStream.read(this.gS, 10, false);
        this.gT = jceInputStream.read(this.gT, 11, false);
        this.gU = jceInputStream.read(this.gU, 12, false);
        this.gV = (ArrayList) jceInputStream.read(hd, 13, false);
        this.gW = jceInputStream.read(this.gW, 14, false);
        this.gX = jceInputStream.read(this.gX, 15, false);
        this.gY = jceInputStream.read(this.gY, 16, false);
        this.official = jceInputStream.read(this.official, 17, false);
        this.gZ = (ArrayList) jceInputStream.read(he, 18, false);
        this.gn = jceInputStream.read(this.gn, 20, false);
        this.ha = (ArrayList) jceInputStream.read(hf, 21, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.gI, 0);
        if (this.gJ != null) {
            jceOutputStream.write(this.gJ, 1);
        }
        if (this.gK != null) {
            jceOutputStream.write(this.gK, 2);
        }
        if (this.gL != null) {
            jceOutputStream.write(this.gL, 3);
        }
        if (this.gM != 0) {
            jceOutputStream.write(this.gM, 4);
        }
        if (this.gN != null) {
            jceOutputStream.write(this.gN, 5);
        }
        if (this.gO != 0) {
            jceOutputStream.write(this.gO, 6);
        }
        if (this.gP != null) {
            jceOutputStream.write(this.gP, 7);
        }
        if (this.gQ != 0) {
            jceOutputStream.write(this.gQ, 8);
        }
        if (this.gR != null) {
            jceOutputStream.write(this.gR, 9);
        }
        jceOutputStream.write(this.gS, 10);
        if (this.gT != 0) {
            jceOutputStream.write(this.gT, 11);
        }
        if (this.gU != 0) {
            jceOutputStream.write(this.gU, 12);
        }
        if (this.gV != null) {
            jceOutputStream.write(this.gV, 13);
        }
        if (this.gW != 0) {
            jceOutputStream.write(this.gW, 14);
        }
        jceOutputStream.write(this.gX, 15);
        if (this.gY != 0) {
            jceOutputStream.write(this.gY, 16);
        }
        jceOutputStream.write(this.official, 17);
        if (this.gZ != null) {
            jceOutputStream.write(this.gZ, 18);
        }
        if (this.gn != -1) {
            jceOutputStream.write(this.gn, 20);
        }
        if (this.ha != null) {
            jceOutputStream.write(this.ha, 21);
        }
    }
}
