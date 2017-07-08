package tmsdk.bg.module.network;

import java.util.ArrayList;

/* compiled from: Unknown */
final class b {
    public static CodeName[] xA;
    public static CodeName[] xB;
    public static CodeName[] xC;
    public static CodeName[] xD;
    public static CodeName[] xE;
    public static CodeName[] xF;
    public static CodeName[] xG;
    public static CodeName[] xH;
    public static CodeName[] xI;
    public static CodeName[] xJ;
    public static CodeName[] xK;
    public static CodeName[] xL;
    public static CodeName[] xM;
    public static CodeName[] xN;
    public static CodeName[] xO;
    public static CodeName[] xP;
    public static CodeName[] xQ;
    public static CodeName[] xR;
    public static CodeName[] xS;
    public static CodeName[] xT;
    public static CodeName[] xU;
    public static CodeName[] xV;
    public static CodeName[] xm;
    public static CodeName[] xn;
    public static CodeName[] xo;
    public static CodeName[] xp;
    public static CodeName[] xq;
    public static CodeName[] xr;
    public static CodeName[] xs;
    public static CodeName[] xt;
    public static CodeName[] xu;
    public static CodeName[] xv;
    public static CodeName[] xw;
    public static CodeName[] xx;
    public static CodeName[] xy;
    public static CodeName[] xz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.module.network.b.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.module.network.b.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.module.network.b.<clinit>():void");
    }

    private static ArrayList<CodeName> a(CodeName[] codeNameArr) {
        if (codeNameArr == null) {
            return null;
        }
        ArrayList<CodeName> arrayList = new ArrayList();
        for (CodeName codeName : codeNameArr) {
            arrayList.add(new CodeName(codeName));
        }
        return arrayList;
    }

    public static ArrayList<CodeName> getAllProvinces() {
        return a(xm);
    }

    public static ArrayList<CodeName> getBrands(String str) {
        CodeName[] codeNameArr = null;
        if ("CMCC".equals(str)) {
            codeNameArr = xT;
        } else if ("UNICOM".equals(str)) {
            codeNameArr = xU;
        } else if ("TELECOM".equals(str)) {
            codeNameArr = xV;
        }
        return a(codeNameArr);
    }

    public static ArrayList<CodeName> getCarries() {
        return a(xS);
    }

    public static ArrayList<CodeName> getCities(String str) {
        CodeName[] codeNameArr = null;
        if ("10".equals(str)) {
            codeNameArr = xn;
        } else if ("20".equals(str)) {
            codeNameArr = xo;
        } else if ("21".equals(str)) {
            codeNameArr = xp;
        } else if ("22".equals(str)) {
            codeNameArr = xq;
        } else if ("23".equals(str)) {
            codeNameArr = xr;
        } else if ("24".equals(str)) {
            codeNameArr = xs;
        } else if ("25".equals(str)) {
            codeNameArr = xt;
        } else if ("27".equals(str)) {
            codeNameArr = xu;
        } else if ("28".equals(str)) {
            codeNameArr = xv;
        } else if ("29".equals(str)) {
            codeNameArr = xw;
        } else if ("311".equals(str)) {
            codeNameArr = xx;
        } else if ("351".equals(str)) {
            codeNameArr = xy;
        } else if ("371".equals(str)) {
            codeNameArr = xz;
        } else if ("431".equals(str)) {
            codeNameArr = xA;
        } else if ("451".equals(str)) {
            codeNameArr = xB;
        } else if ("471".equals(str)) {
            codeNameArr = xC;
        } else if ("531".equals(str)) {
            codeNameArr = xD;
        } else if ("551".equals(str)) {
            codeNameArr = xE;
        } else if ("571".equals(str)) {
            codeNameArr = xF;
        } else if ("591".equals(str)) {
            codeNameArr = xG;
        } else if ("731".equals(str)) {
            codeNameArr = xH;
        } else if ("771".equals(str)) {
            codeNameArr = xI;
        } else if ("791".equals(str)) {
            codeNameArr = xJ;
        } else if ("851".equals(str)) {
            codeNameArr = xK;
        } else if ("871".equals(str)) {
            codeNameArr = xL;
        } else if ("891".equals(str)) {
            codeNameArr = xM;
        } else if ("898".equals(str)) {
            codeNameArr = xN;
        } else if ("931".equals(str)) {
            codeNameArr = xO;
        } else if ("951".equals(str)) {
            codeNameArr = xP;
        } else if ("971".equals(str)) {
            codeNameArr = xQ;
        } else if ("991".equals(str)) {
            codeNameArr = xR;
        }
        return a(codeNameArr);
    }
}
