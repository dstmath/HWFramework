package com.android.commands.monkey;

import android.app.IActivityManager;
import android.view.IWindowManager;
import java.io.FileOutputStream;
import java.io.IOException;

public class MonkeyFlipEvent extends MonkeyEvent {
    private static final byte[] FLIP_0 = null;
    private static final byte[] FLIP_1 = null;
    private final boolean mKeyboardOpen;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeyFlipEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeyFlipEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeyFlipEvent.<clinit>():void");
    }

    public MonkeyFlipEvent(boolean keyboardOpen) {
        super(5);
        this.mKeyboardOpen = keyboardOpen;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        IOException e;
        if (verbose > 0) {
            System.out.println(":Sending Flip keyboardOpen=" + this.mKeyboardOpen);
        }
        FileOutputStream f = null;
        try {
            FileOutputStream f2 = new FileOutputStream("/dev/input/event0");
            try {
                f2.write(this.mKeyboardOpen ? FLIP_0 : FLIP_1);
                f2.close();
                return 1;
            } catch (IOException e2) {
                e = e2;
                f = f2;
                System.out.println("Got IOException performing flip" + e);
                if (f != null) {
                    try {
                        f.close();
                    } catch (IOException e3) {
                        System.out.println("FileOutputStream close error");
                    }
                }
                return 0;
            }
        } catch (IOException e4) {
            e = e4;
            System.out.println("Got IOException performing flip" + e);
            if (f != null) {
                f.close();
            }
            return 0;
        }
    }
}
