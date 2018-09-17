package tmsdk.common.module.qscanner.impl;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import tmsdkobf.dq;

public final class d extends JceStruct {
    static a BY;
    static ArrayList<dq> BZ;
    static ArrayList<Integer> Ca;
    public a BS = null;
    public String BT = "";
    public int BU = 0;
    public int BV = 0;
    public String BW = "";
    public ArrayList<Integer> BX = null;
    public int category = 0;
    public int dp = 0;
    public int lL = 0;
    public int lP = 0;
    public String label = "";
    public String name = "";
    public ArrayList<dq> plugins = null;
    public int type = 0;
    public String url = "";

    public void readFrom(JceInputStream jceInputStream) {
        if (BY == null) {
            BY = new a();
        }
        this.BS = (a) jceInputStream.read(BY, 0, true);
        this.type = jceInputStream.read(this.type, 1, true);
        this.label = jceInputStream.readString(2, false);
        this.BT = jceInputStream.readString(3, false);
        this.lL = jceInputStream.read(this.lL, 4, false);
        this.BU = jceInputStream.read(this.BU, 5, false);
        this.name = jceInputStream.readString(6, false);
        this.url = jceInputStream.readString(7, false);
        this.BV = jceInputStream.read(this.BV, 8, false);
        this.lP = jceInputStream.read(this.lP, 9, false);
        this.dp = jceInputStream.read(this.dp, 10, false);
        this.BW = jceInputStream.readString(11, false);
        if (BZ == null) {
            BZ = new ArrayList();
            BZ.add(new dq());
        }
        this.plugins = (ArrayList) jceInputStream.read(BZ, 12, false);
        if (Ca == null) {
            Ca = new ArrayList();
            Ca.add(Integer.valueOf(0));
        }
        this.BX = (ArrayList) jceInputStream.read(Ca, 13, false);
        this.category = jceInputStream.read(this.category, 14, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.BS, 0);
        jceOutputStream.write(this.type, 1);
        if (this.label != null) {
            jceOutputStream.write(this.label, 2);
        }
        if (this.BT != null) {
            jceOutputStream.write(this.BT, 3);
        }
        jceOutputStream.write(this.lL, 4);
        jceOutputStream.write(this.BU, 5);
        if (this.name != null) {
            jceOutputStream.write(this.name, 6);
        }
        if (this.url != null) {
            jceOutputStream.write(this.url, 7);
        }
        jceOutputStream.write(this.BV, 8);
        jceOutputStream.write(this.lP, 9);
        jceOutputStream.write(this.dp, 10);
        if (this.BW != null) {
            jceOutputStream.write(this.BW, 11);
        }
        if (this.plugins != null) {
            jceOutputStream.write(this.plugins, 12);
        }
        if (this.BX != null) {
            jceOutputStream.write(this.BX, 13);
        }
        jceOutputStream.write(this.category, 14);
    }
}
