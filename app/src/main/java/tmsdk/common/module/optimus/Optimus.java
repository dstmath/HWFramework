package tmsdk.common.module.optimus;

import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.optimus.impl.bean.BsBlackWhiteItem;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInfo;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsResult;
import tmsdk.common.utils.d;
import tmsdkobf.mz;

/* compiled from: Unknown */
public class Optimus {
    private static volatile boolean Di;
    private volatile boolean Dj;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.optimus.Optimus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.optimus.Optimus.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.optimus.Optimus.<clinit>():void");
    }

    public Optimus() {
        this.Dj = false;
    }

    private native void nativeCheck(BsInput bsInput, BsResult bsResult);

    private native void nativeCheckWithCloud(BsInput bsInput, BsCloudResult bsCloudResult, BsResult bsResult);

    private native void nativeFinish();

    private native List<BsInfo> nativeGetBsInfos(BsInput bsInput);

    private native String nativeGetUploadInfo();

    private native void nativeInit(String str, String str2);

    private native void nativeSetBlackWhiteItems(List<BsBlackWhiteItem> list, List<BsBlackWhiteItem> list2);

    public synchronized boolean check(BsInput bsInput, BsResult bsResult) {
        if (this.Dj) {
            try {
                nativeCheck(bsInput, bsResult);
                return true;
            } catch (Throwable th) {
            }
        }
        return false;
    }

    public synchronized boolean checkWithCloud(BsInput bsInput, BsCloudResult bsCloudResult, BsResult bsResult) {
        if (this.Dj) {
            try {
                nativeCheckWithCloud(bsInput, bsCloudResult, bsResult);
                return true;
            } catch (Throwable th) {
            }
        }
        return false;
    }

    public synchronized boolean finish() {
        try {
            nativeFinish();
        } catch (Throwable th) {
            this.Dj = false;
            return false;
        }
        return true;
    }

    public synchronized List<BsInfo> getBsInfos(BsInput bsInput) {
        List<BsInfo> list;
        list = null;
        if (this.Dj) {
            try {
                list = nativeGetBsInfos(bsInput);
            } catch (Throwable th) {
            }
        }
        return list;
    }

    public synchronized String getUploadInfo() {
        if (this.Dj) {
            try {
                return nativeGetUploadInfo();
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public synchronized boolean init(String str, String str2) {
        if (!Di) {
            String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.FAKE_BS_LIBNAME);
            d.e("QQPimSecure", "[Optimus]:load so:" + strFromEnvMap);
            Di = mz.e(TMSDKContext.getApplicaionContext(), strFromEnvMap);
        }
        if (Di) {
            try {
                nativeInit(str, str2);
                this.Dj = true;
                return true;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        this.Dj = false;
        return false;
    }

    public synchronized boolean setBlackWhiteItems(List<BsBlackWhiteItem> list, List<BsBlackWhiteItem> list2) {
        if (this.Dj) {
            try {
                nativeSetBlackWhiteItems(list, list2);
                return true;
            } catch (Throwable th) {
            }
        }
        return false;
    }
}
