package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.BasicCloudField;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public final class dy extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ = false;
    public String cC;
    public String cE;
    public String cF;
    public String cG;
    public String cH;
    public int cJ;
    public short cP;
    public int fL;
    public String imsi;
    public String ja;
    public String jb;
    public String jc;
    public String jd;
    public String je;
    public String jf;
    public String jg;
    public String jh;
    public String name;
    public String r;
    public int type;
    public String version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dy.<clinit>():void");
    }

    public dy() {
        this.cG = "";
        this.name = "";
        this.version = "";
        this.cC = "";
        this.imsi = "";
        this.cE = "";
        this.ja = "";
        this.type = db.gN.value();
        this.jb = "";
        this.jc = "";
        this.jd = "";
        this.cF = "";
        this.cJ = 0;
        this.cH = "";
        this.fL = 0;
        this.je = "";
        this.cP = (short) 0;
        this.r = "";
        this.jf = "";
        this.jg = "";
        this.jh = "";
        C(this.cG);
        setName(this.name);
        r(this.version);
        e(this.cC);
        f(this.imsi);
        D(this.cE);
        E(this.ja);
        setType(this.type);
        F(this.jb);
        G(this.jc);
        H(this.jd);
        setPhone(this.cF);
        o(this.cJ);
        I(this.cH);
        f(this.fL);
        J(this.je);
        b(this.cP);
        t(this.r);
        K(this.jf);
        L(this.jg);
        M(this.jh);
    }

    public void C(String str) {
        this.cG = str;
    }

    public void D(String str) {
        this.cE = str;
    }

    public void E(String str) {
        this.ja = str;
    }

    public void F(String str) {
        this.jb = str;
    }

    public void G(String str) {
        this.jc = str;
    }

    public void H(String str) {
        this.jd = str;
    }

    public void I(String str) {
        this.cH = str;
    }

    public void J(String str) {
        this.je = str;
    }

    public void K(String str) {
        this.jf = str;
    }

    public void L(String str) {
        this.jg = str;
    }

    public void M(String str) {
        this.jh = str;
    }

    public void b(short s) {
        this.cP = (short) s;
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return obj;
    }

    public void display(StringBuilder stringBuilder, int i) {
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.cG, TMSDKContext.CON_LC);
        foVar.a(this.name, CheckVersionField.CHECK_VERSION_NAME);
        foVar.a(this.version, CheckVersionField.CHECK_VERSION_VERSION);
        foVar.a(this.cC, BasicCloudField.PHONE_IMEI);
        foVar.a(this.imsi, "imsi");
        foVar.a(this.cE, "qq");
        foVar.a(this.ja, "ip");
        foVar.a(this.type, "type");
        foVar.a(this.jb, "osversion");
        foVar.a(this.jc, "machineuid");
        foVar.a(this.jd, "machineconf");
        foVar.a(this.cF, "phone");
        foVar.a(this.cJ, "subplatform");
        foVar.a(this.cH, "channelid");
        foVar.a(this.fL, "isbuildin");
        foVar.a(this.je, "uuid");
        foVar.a(this.cP, "lang");
        foVar.a(this.r, "guid");
        foVar.a(this.jf, "sdk");
        foVar.a(this.jg, "sid");
        foVar.a(this.jh, "newguid");
    }

    public void e(String str) {
        this.cC = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dy dyVar = (dy) obj;
        if (ft.equals(this.cG, dyVar.cG) && ft.equals(this.name, dyVar.name) && ft.equals(this.version, dyVar.version) && ft.equals(this.cC, dyVar.cC) && ft.equals(this.imsi, dyVar.imsi) && ft.equals(this.cE, dyVar.cE) && ft.equals(this.ja, dyVar.ja) && ft.equals(this.type, dyVar.type) && ft.equals(this.jb, dyVar.jb) && ft.equals(this.jc, dyVar.jc) && ft.equals(this.jd, dyVar.jd) && ft.equals(this.cF, dyVar.cF) && ft.equals(this.cJ, dyVar.cJ) && ft.equals(this.cH, dyVar.cH) && ft.equals(this.fL, dyVar.fL) && ft.equals(this.je, dyVar.je) && ft.a(this.cP, dyVar.cP) && ft.equals(this.r, dyVar.r) && ft.equals(this.jf, dyVar.jf) && ft.equals(this.jg, dyVar.jg) && ft.equals(this.jh, dyVar.jh)) {
            z = true;
        }
        return z;
    }

    public void f(int i) {
        this.fL = i;
    }

    public void f(String str) {
        this.imsi = str;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void o(int i) {
        this.cJ = i;
    }

    public void r(String str) {
        this.version = str;
    }

    public void readFrom(fq fqVar) {
        C(fqVar.a(0, true));
        setName(fqVar.a(1, true));
        r(fqVar.a(2, true));
        e(fqVar.a(3, true));
        f(fqVar.a(4, true));
        D(fqVar.a(5, false));
        E(fqVar.a(6, false));
        setType(fqVar.a(this.type, 7, false));
        F(fqVar.a(8, false));
        G(fqVar.a(9, false));
        H(fqVar.a(10, false));
        setPhone(fqVar.a(11, false));
        o(fqVar.a(this.cJ, 12, false));
        I(fqVar.a(13, false));
        f(fqVar.a(this.fL, 14, false));
        J(fqVar.a(15, false));
        b(fqVar.a(this.cP, 16, false));
        t(fqVar.a(17, false));
        K(fqVar.a(18, false));
        L(fqVar.a(19, false));
        M(fqVar.a(20, false));
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setPhone(String str) {
        this.cF = str;
    }

    public void setType(int i) {
        this.type = i;
    }

    public void t(String str) {
        this.r = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.cG, 0);
        frVar.a(this.name, 1);
        frVar.a(this.version, 2);
        frVar.a(this.cC, 3);
        frVar.a(this.imsi, 4);
        if (this.cE != null) {
            frVar.a(this.cE, 5);
        }
        if (this.ja != null) {
            frVar.a(this.ja, 6);
        }
        frVar.write(this.type, 7);
        if (this.jb != null) {
            frVar.a(this.jb, 8);
        }
        if (this.jc != null) {
            frVar.a(this.jc, 9);
        }
        if (this.jd != null) {
            frVar.a(this.jd, 10);
        }
        if (this.cF != null) {
            frVar.a(this.cF, 11);
        }
        frVar.write(this.cJ, 12);
        if (this.cH != null) {
            frVar.a(this.cH, 13);
        }
        frVar.write(this.fL, 14);
        if (this.je != null) {
            frVar.a(this.je, 15);
        }
        frVar.a(this.cP, 16);
        if (this.r != null) {
            frVar.a(this.r, 17);
        }
        if (this.jf != null) {
            frVar.a(this.jf, 18);
        }
        if (this.jg != null) {
            frVar.a(this.jg, 19);
        }
        if (this.jh != null) {
            frVar.a(this.jh, 20);
        }
    }
}
