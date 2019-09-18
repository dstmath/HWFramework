package com.huawei.opcollect;

import android.content.Context;
import android.os.Handler;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.io.PrintWriter;

public class OpInterfaceImpl implements OpInterface {
    private static final String TAG = "OpInterfaceImpl";
    private Context mContext;

    public OpInterfaceImpl() {
        OPCollectLog.r(TAG, "OpInterface impl");
    }

    public void initialize(Context context) {
        OPCollectLog.r(TAG, "OpInterface initialize impl: " + context);
        this.mContext = context;
        OdmfCollectScheduler.getInstance().initialize(context);
    }

    public void switchOn() {
        OPCollectLog.r(TAG, "OpInterface impl switch on.");
        if (!OPCollectUtils.isPkgInstalled(this.mContext, OPCollectUtils.ODMF_PACKAGE_NAME)) {
            OPCollectLog.e(TAG, "odmf is not installed..");
            return;
        }
        Handler handler = OdmfCollectScheduler.getInstance().getCtrlHandler();
        if (handler != null) {
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2, 0);
        }
    }

    public void switchOff() {
        OPCollectLog.r(TAG, "OpInterface impl switch off.");
        if (!OPCollectUtils.isPkgInstalled(this.mContext, OPCollectUtils.ODMF_PACKAGE_NAME)) {
            OPCollectLog.e(TAG, "odmf is not installed..");
            return;
        }
        Handler handler = OdmfCollectScheduler.getInstance().getCtrlHandler();
        if (handler != null) {
            handler.removeMessages(3);
            handler.sendEmptyMessageDelayed(3, 0);
        }
    }

    public void dump(PrintWriter pw) {
        OPCollectLog.r(TAG, "OpInterface dump impl");
        if (!OPCollectUtils.isPkgInstalled(this.mContext, OPCollectUtils.ODMF_PACKAGE_NAME)) {
            OPCollectLog.e(TAG, "odmf is not installed..");
        } else {
            OdmfCollectScheduler.dump(pw);
        }
    }
}
