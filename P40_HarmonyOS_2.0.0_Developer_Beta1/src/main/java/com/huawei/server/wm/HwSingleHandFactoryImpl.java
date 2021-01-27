package com.huawei.server.wm;

import android.content.Context;
import android.os.Handler;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.android.util.SlogEx;

public class HwSingleHandFactoryImpl extends HwSingleHandFactory {
    private static final String TAG = "HwSingleHandFactoryImpl";
    private IHwSingleHandAdapter mSingleHandAdapter;
    private HwSingleHandContentExBridgeEx mSingleHandContent;
    private IsingleHandInner mSingleHandInner;

    static {
        SlogEx.i(TAG, "loaded to system_server,who am I:" + HwSingleHandFactoryImpl.class.getName());
    }

    public IHwSingleHandAdapter getHwSingleHandAdapter(Context context, Handler handler, Handler uiHandler, WindowManagerServiceEx service) {
        if (this.mSingleHandContent == null) {
            getHwSingleHandContentEx(service);
        }
        if (this.mSingleHandAdapter == null) {
            IsingleHandInner isingleHandInner = this.mSingleHandContent;
            if (isingleHandInner instanceof IsingleHandInner) {
                this.mSingleHandInner = isingleHandInner;
            } else {
                SlogEx.e(TAG, "cast error");
            }
            this.mSingleHandAdapter = new HwSingleHandAdapterImpl(context, handler, uiHandler, service, this.mSingleHandInner);
        }
        return this.mSingleHandAdapter;
    }

    public HwSingleHandContentExBridgeEx getHwSingleHandContentEx(WindowManagerServiceEx serviceEx) {
        SlogEx.i(TAG, "loaded getHwSingleHandContentEx in parts");
        if (this.mSingleHandContent == null) {
            this.mSingleHandContent = new HwSingleHandContentEx(serviceEx);
        }
        return this.mSingleHandContent;
    }
}
