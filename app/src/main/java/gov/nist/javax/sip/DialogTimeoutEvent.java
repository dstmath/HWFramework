package gov.nist.javax.sip;

import java.util.EventObject;
import javax.sip.Dialog;

public class DialogTimeoutEvent extends EventObject {
    private static final long serialVersionUID = -2514000059989311925L;
    private Dialog m_dialog;
    private Reason m_reason;

    public enum Reason {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.DialogTimeoutEvent.Reason.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.DialogTimeoutEvent.Reason.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogTimeoutEvent.Reason.<clinit>():void");
        }
    }

    public DialogTimeoutEvent(Object source, Dialog dialog, Reason reason) {
        super(source);
        this.m_dialog = null;
        this.m_reason = null;
        this.m_dialog = dialog;
        this.m_reason = reason;
    }

    public Dialog getDialog() {
        return this.m_dialog;
    }

    public Reason getReason() {
        return this.m_reason;
    }
}
