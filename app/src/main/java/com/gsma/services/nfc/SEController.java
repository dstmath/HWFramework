package com.gsma.services.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;
import java.io.IOException;

@Deprecated
public class SEController {
    private static Context mContext;

    public interface Callbacks {
        void onGetDefaultController(SEController sEController);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.gsma.services.nfc.SEController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.gsma.services.nfc.SEController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gsma.services.nfc.SEController.<clinit>():void");
    }

    SEController() {
    }

    @Deprecated
    public static void getDefaultController(Context context, Callbacks cb) throws IOException {
        mContext = context;
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String getActiveSecureElement() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void setActiveSecureElement(String SEName) throws IllegalStateException, SecurityException, UnsupportedOperationException {
        if (!NfcAdapter.getNfcAdapter(mContext).isEnabled()) {
            throw new IllegalStateException("Nfc not enabled");
        } else if (canUseApi()) {
            throw new UnsupportedOperationException();
        } else {
            throw new SecurityException("Can not use this API");
        }
    }

    private boolean canUseApi() {
        return false;
    }
}
