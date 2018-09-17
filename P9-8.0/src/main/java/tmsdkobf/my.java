package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class my extends JceStruct {
    static ArrayList<na> BA = new ArrayList();
    static mz BB = new mz();
    static nd BC = new nd();
    public mz By = null;
    public nd Bz = null;
    public int iCid = 0;
    public int iLac = 0;
    public long luLoc = 0;
    public short sBsss = (short) 0;
    public short sDataState = (short) 0;
    public short sMcc = (short) 0;
    public short sMnc = (short) 0;
    public short sNetworkType = (short) 0;
    public short sNumNeighbors = (short) 0;
    public long uTimeInSeconds = 0;
    public ArrayList<na> vecNeighbors = null;

    static {
        BA.add(new na());
    }

    public JceStruct newInit() {
        return new my();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.uTimeInSeconds = jceInputStream.read(this.uTimeInSeconds, 0, true);
        this.sNetworkType = (short) jceInputStream.read(this.sNetworkType, 1, true);
        this.sDataState = (short) jceInputStream.read(this.sDataState, 2, true);
        this.iCid = jceInputStream.read(this.iCid, 3, true);
        this.iLac = jceInputStream.read(this.iLac, 4, true);
        this.luLoc = jceInputStream.read(this.luLoc, 5, true);
        this.sBsss = (short) jceInputStream.read(this.sBsss, 6, true);
        this.sMcc = (short) jceInputStream.read(this.sMcc, 7, true);
        this.sMnc = (short) jceInputStream.read(this.sMnc, 8, true);
        this.sNumNeighbors = (short) jceInputStream.read(this.sNumNeighbors, 9, true);
        this.vecNeighbors = (ArrayList) jceInputStream.read(BA, 10, true);
        this.By = (mz) jceInputStream.read(BB, 11, true);
        this.Bz = (nd) jceInputStream.read(BC, 12, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.uTimeInSeconds, 0);
        jceOutputStream.write(this.sNetworkType, 1);
        jceOutputStream.write(this.sDataState, 2);
        jceOutputStream.write(this.iCid, 3);
        jceOutputStream.write(this.iLac, 4);
        jceOutputStream.write(this.luLoc, 5);
        jceOutputStream.write(this.sBsss, 6);
        jceOutputStream.write(this.sMcc, 7);
        jceOutputStream.write(this.sMnc, 8);
        jceOutputStream.write(this.sNumNeighbors, 9);
        jceOutputStream.write(this.vecNeighbors, 10);
        jceOutputStream.write(this.By, 11);
        jceOutputStream.write(this.Bz, 12);
    }
}
