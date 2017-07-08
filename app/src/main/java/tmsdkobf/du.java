package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class du extends fs {
    static final /* synthetic */ boolean fJ = false;
    static ArrayList<et> iS;
    public ArrayList<et> iR;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.du.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.du.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.du.<clinit>():void");
    }

    public du() {
        this.iR = null;
        d(this.iR);
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

    public ArrayList<et> d() {
        return this.iR;
    }

    public void d(ArrayList<et> arrayList) {
        this.iR = arrayList;
    }

    public boolean equals(Object obj) {
        return ft.equals(this.iR, ((du) obj).iR);
    }

    public void readFrom(fq fqVar) {
        if (iS == null) {
            iS = new ArrayList();
            iS.add(new et());
        }
        d((ArrayList) fqVar.b(iS, 0, false));
    }

    public void writeTo(fr frVar) {
        if (this.iR != null) {
            frVar.a(this.iR, 0);
        }
    }
}
