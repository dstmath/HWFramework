package tmsdk.common;

import android.content.Context;
import android.content.Intent;
import tmsdkobf.jj;
import tmsdkobf.jq;
import tmsdkobf.mn;

/* compiled from: Unknown */
public abstract class TMSBootReceiver extends jj {

    /* compiled from: Unknown */
    /* renamed from: tmsdk.common.TMSBootReceiver.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Context Ad;
        final /* synthetic */ TMSBootReceiver Ae;

        AnonymousClass1(TMSBootReceiver tMSBootReceiver, Context context) {
            this.Ae = tMSBootReceiver;
            this.Ad = context;
        }

        public void run() {
            int i = 0;
            mn mnVar = new mn();
            mnVar.r(0, (int) (System.currentTimeMillis() / 1000));
            if (!new a().j(this.Ad)) {
                i = 1;
            }
            mnVar.r(1, i);
            mnVar.commit();
        }
    }

    /* compiled from: Unknown */
    private static final class a {
        private static final short[] Af = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.TMSBootReceiver.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.TMSBootReceiver.a.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.TMSBootReceiver.a.<clinit>():void");
        }

        private a() {
        }

        private String a(short[] sArr) {
            StringBuffer stringBuffer = new StringBuffer();
            short[] b = b(sArr);
            for (short s : b) {
                stringBuffer.append((char) s);
            }
            return stringBuffer.toString();
        }

        private short[] b(short[] sArr) {
            short[] sArr2 = new short[sArr.length];
            int i = 35;
            int i2 = 0;
            while (i2 < sArr.length) {
                sArr2[i2] = (short) ((short) (sArr[i2] ^ i));
                i2++;
                i = (char) (i + 1);
            }
            return sArr2;
        }

        public boolean j(Context context) {
            return TMServiceFactory.getSystemInfoService().aC(a(Af));
        }
    }

    public TMSBootReceiver() {
    }

    public void doOnRecv(Context context, Intent intent) {
        jq.ct().c(new AnonymousClass1(this, context), "TMSBootReceiveThread").start();
    }
}
