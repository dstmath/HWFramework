package com.android.contacts.hap.numbermark.hwtoms.api;

import android.content.Context;
import android.text.TextUtils;
import com.android.contacts.hap.numbermark.hwtoms.model.request.TomsRequestBase;
import com.android.contacts.hap.numbermark.utils.NetUtils;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TomsInvocationHandler implements InvocationHandler {
    private static final String CHANNAL_NO = "221";
    private Context mContext;
    private IApiManager mIApiManager;

    public TomsInvocationHandler(Context context, IApiManager apiManager) {
        this.mContext = context;
        this.mIApiManager = apiManager;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TomsRequestBase base = args[0];
        if (!NetUtils.isNetworkAvailable(this.mContext)) {
            return null;
        }
        if (TextUtils.isEmpty(base.getImei())) {
            base.setImei(NetUtils.getUuid(this.mContext));
        }
        if (TextUtils.isEmpty(base.getChannelno())) {
            base.setChannelno("221");
        }
        return method.invoke(this.mIApiManager, args);
    }
}
