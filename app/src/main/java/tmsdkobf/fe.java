package tmsdkobf;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tmsdk.common.CallerIdent;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;

/* compiled from: Unknown */
public class fe {
    private static ReentrantReadWriteLock lV;
    private static HashMap<String, Object> lW;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fe.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fe.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fe.<clinit>():void");
    }

    private static Object a(int i, long j) {
        String str = "" + i + "-" + j;
        lV.readLock().lock();
        Object obj = lW.get(str);
        lV.readLock().unlock();
        return obj != null ? obj : b(i, j);
    }

    public static Object ad(int i) {
        return a(i, CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST));
    }

    private static Object b(int i, long j) {
        Object obj;
        Object obj2 = null;
        switch (i) {
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new lq(j, "com.tencent.meri");
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new hz(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case UrlCheckType.MAKE_MONEY /*9*/:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new ig(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case UrlCheckType.MSG_REACTIONARY /*12*/:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new gd(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case NumQueryRet.USED_FOR_Calling /*17*/:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new fd(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            default:
                obj = null;
                break;
        }
        if (!(obj == null || obj2 == null)) {
            lV.writeLock().lock();
            if (lW.get(obj) == null) {
                lW.put(obj, obj2);
            }
            lV.writeLock().unlock();
        }
        return obj2;
    }
}
