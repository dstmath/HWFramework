package tmsdk.common.module.optimus;

import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.optimus.impl.bean.BsBlackWhiteItem;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInfo;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsResult;
import tmsdk.common.utils.f;
import tmsdkobf.ma;

public class Optimus {
    private static volatile boolean AJ;
    private volatile boolean AK = false;

    static {
        AJ = false;
        AJ = ma.f(TMSDKContext.getApplicaionContext(), "optimus-1.0.0-mfr");
    }

    private native void nativeCheck(BsInput bsInput, BsResult bsResult);

    private native void nativeCheckWithCloud(BsInput bsInput, BsCloudResult bsCloudResult, BsResult bsResult);

    private native void nativeFinish();

    private native List<BsInfo> nativeGetBsInfos(BsInput bsInput);

    private native String nativeGetUploadInfo();

    private native void nativeInit(String str, String str2);

    private native void nativeSetBlackWhiteItems(List<BsBlackWhiteItem> list, List<BsBlackWhiteItem> list2);

    public synchronized boolean check(BsInput bsInput, BsResult bsResult) {
        if (this.AK) {
            try {
                nativeCheck(bsInput, bsResult);
                return true;
            } catch (Throwable th) {
                return false;
            }
        }
    }

    public synchronized boolean checkWithCloud(BsInput bsInput, BsCloudResult bsCloudResult, BsResult bsResult) {
        if (this.AK) {
            try {
                nativeCheckWithCloud(bsInput, bsCloudResult, bsResult);
                return true;
            } catch (Throwable th) {
                return false;
            }
        }
    }

    public synchronized boolean finish() {
        try {
            nativeFinish();
        } catch (Throwable th) {
            this.AK = false;
            return false;
        }
        return true;
    }

    public synchronized List<BsInfo> getBsInfos(BsInput bsInput) {
        List<BsInfo> list;
        list = null;
        if (this.AK) {
            try {
                list = nativeGetBsInfos(bsInput);
            } catch (Throwable th) {
            }
        }
        return list;
    }

    public synchronized String getUploadInfo() {
        if (this.AK) {
            try {
                return nativeGetUploadInfo();
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public synchronized boolean init(String str, String str2) {
        if (!AJ) {
            String str3 = "optimus-1.0.0-mfr";
            f.d("QQPimSecure", "[Optimus]:load so:" + str3);
            AJ = ma.f(TMSDKContext.getApplicaionContext(), str3);
        }
        if (AJ) {
            try {
                nativeInit(str, str2);
                this.AK = true;
                return true;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        this.AK = false;
        return false;
    }

    public synchronized boolean setBlackWhiteItems(List<BsBlackWhiteItem> list, List<BsBlackWhiteItem> list2) {
        if (this.AK) {
            try {
                nativeSetBlackWhiteItems(list, list2);
                return true;
            } catch (Throwable th) {
                return false;
            }
        }
    }
}
