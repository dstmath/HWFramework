package tmsdkobf;

import java.util.ArrayList;
import java.util.Map;

/* compiled from: Unknown */
public final class ah extends fs {
    static ArrayList<Map<Integer, String>> aX;
    static byte[] aY;
    public ArrayList<Map<Integer, String>> aV;
    public byte[] aW;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ah.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ah.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ah.<clinit>():void");
    }

    public ah() {
        this.aV = null;
        this.aW = null;
    }

    public fs newInit() {
        return new ah();
    }

    public void readFrom(fq fqVar) {
        this.aV = (ArrayList) fqVar.b(aX, 0, true);
        this.aW = fqVar.a(aY, 1, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.aV, 0);
        if (this.aW != null) {
            frVar.a(this.aW, 1);
        }
    }
}
