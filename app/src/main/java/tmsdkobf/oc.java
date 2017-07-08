package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class oc extends fs {
    static ArrayList<oe> DY;
    static od DZ;
    static oh Ea;
    public od DW;
    public oh DX;
    public int iCid;
    public int iLac;
    public long luLoc;
    public short sBsss;
    public short sDataState;
    public short sMcc;
    public short sMnc;
    public short sNetworkType;
    public short sNumNeighbors;
    public long uTimeInSeconds;
    public ArrayList<oe> vecNeighbors;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.oc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.oc.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.oc.<clinit>():void");
    }

    public oc() {
        this.uTimeInSeconds = 0;
        this.sNetworkType = (short) 0;
        this.sDataState = (short) 0;
        this.iCid = 0;
        this.iLac = 0;
        this.luLoc = 0;
        this.sBsss = (short) 0;
        this.sMcc = (short) 0;
        this.sMnc = (short) 0;
        this.sNumNeighbors = (short) 0;
        this.vecNeighbors = null;
        this.DW = null;
        this.DX = null;
    }

    public fs newInit() {
        return new oc();
    }

    public void readFrom(fq fqVar) {
        this.uTimeInSeconds = fqVar.a(this.uTimeInSeconds, 0, true);
        this.sNetworkType = (short) fqVar.a(this.sNetworkType, 1, true);
        this.sDataState = (short) fqVar.a(this.sDataState, 2, true);
        this.iCid = fqVar.a(this.iCid, 3, true);
        this.iLac = fqVar.a(this.iLac, 4, true);
        this.luLoc = fqVar.a(this.luLoc, 5, true);
        this.sBsss = (short) fqVar.a(this.sBsss, 6, true);
        this.sMcc = (short) fqVar.a(this.sMcc, 7, true);
        this.sMnc = (short) fqVar.a(this.sMnc, 8, true);
        this.sNumNeighbors = (short) fqVar.a(this.sNumNeighbors, 9, true);
        this.vecNeighbors = (ArrayList) fqVar.b(DY, 10, true);
        this.DW = (od) fqVar.a(DZ, 11, true);
        this.DX = (oh) fqVar.a(Ea, 12, true);
    }

    public void writeTo(fr frVar) {
        frVar.b(this.uTimeInSeconds, 0);
        frVar.a(this.sNetworkType, 1);
        frVar.a(this.sDataState, 2);
        frVar.write(this.iCid, 3);
        frVar.write(this.iLac, 4);
        frVar.b(this.luLoc, 5);
        frVar.a(this.sBsss, 6);
        frVar.a(this.sMcc, 7);
        frVar.a(this.sMnc, 8);
        frVar.a(this.sNumNeighbors, 9);
        frVar.a(this.vecNeighbors, 10);
        frVar.a(this.DW, 11);
        frVar.a(this.DX, 12);
    }
}
