package tmsdk.common;

import android.content.Context;
import android.content.Intent;
import tmsdkobf.if;
import tmsdkobf.im;
import tmsdkobf.lp;

public abstract class TMSBootReceiver extends if {

    private static final class a {
        private static final short[] xr = new short[]{(short) 64, (short) 75, (short) 72, (short) 8, (short) 86, (short) 65, (short) 65, (short) 69, (short) 68, (short) 31, (short) 27, (short) 30, (short) 1, (short) 93, (short) 94, (short) 80, (short) 90, (short) 88, (short) 80, (short) 69, (short) 86, (short) 94, (short) 92};

        private a() {
        }

        /* synthetic */ a(AnonymousClass1 anonymousClass1) {
            this();
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

        public boolean m(Context context) {
            return TMServiceFactory.getSystemInfoService().ai(a(xr));
        }
    }

    public void doOnRecv(final Context context, Intent intent) {
        im.bJ().newFreeThread(new Runnable() {
            public void run() {
                int i = 0;
                lp lpVar = new lp();
                lpVar.t(0, (int) (System.currentTimeMillis() / 1000));
                if (!new a().m(context)) {
                    i = 1;
                }
                lpVar.t(1, i);
                lpVar.commit();
            }
        }, "TMSBootReceiveThread").start();
    }
}
