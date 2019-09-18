package com.huawei.wallet.sdk.common.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.common.utils.PollingOperate;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServerApiInvocationHandler implements InvocationHandler {
    private Object cardServer;
    private Context mContext;
    private PollingOperate pollingOperate = PollingOperate.getInstance();

    public ServerApiInvocationHandler(Context context, Object subject) {
        this.mContext = context;
        this.cardServer = subject;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
