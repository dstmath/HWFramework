package huawei.android.telephony.wrapper;

import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Method;

public class DummyPhoneWrapper implements PhoneWrapper {
    private static final Class<?> CLASS_Phone = null;
    private static final String LOG_TAG = "DummyPhoneWrapper";
    private static final Method METHOD_getSubscription = null;
    private static DummyPhoneWrapper mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.telephony.wrapper.DummyPhoneWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.telephony.wrapper.DummyPhoneWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.telephony.wrapper.DummyPhoneWrapper.<clinit>():void");
    }

    public static PhoneWrapper getInstance() {
        return mInstance;
    }

    public int getSubscription(Phone phone) {
        try {
            return ((Integer) HwReflectUtils.invoke(phone, METHOD_getSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getSubscription cause exception!" + e.toString());
            return 0;
        }
    }
}
