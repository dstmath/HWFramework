package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.RouterActionCallBack;
import com.huawei.wallet.sdk.business.bankcard.api.WalletAction;
import com.huawei.wallet.sdk.business.bankcard.request.RouterRequest;
import com.huawei.wallet.sdk.business.bankcard.request.WalletActionRequest;
import com.huawei.wallet.sdk.business.bankcard.response.RouterResponse;
import com.huawei.wallet.sdk.business.bankcard.response.WalletActionResult;
import com.huawei.wallet.sdk.business.bankcard.util.ThreadPoolManager;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class LocalRouter {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile LocalRouter instance;
    private Map<String, WalletProvider> providerMap = new HashMap();

    private static class LocalTask implements Callable<RouterResponse> {
        private RouterActionCallBack callBack;
        private WalletAction mAction;
        private Context mContext;
        private WalletActionRequest mRequestData;
        private RouterResponse mResponse;

        public LocalTask(RouterActionCallBack callBack2, RouterResponse routerResponse, WalletActionRequest requestData, Context context, WalletAction maAction) {
            this.mContext = context;
            this.mResponse = routerResponse;
            this.mRequestData = requestData;
            this.mAction = maAction;
            this.callBack = callBack2;
        }

        public RouterResponse call() throws Exception {
            WalletActionResult actionResult = this.mAction.invoke(this.mContext, this.mRequestData);
            this.mResponse.setResultCode(actionResult.getResultCode());
            this.mResponse.setResultDesc(actionResult.getResultDesc());
            if (actionResult.isSuccess()) {
                LogC.d("ASync invoke action:" + this.mRequestData.getActionName() + " success!", false);
                this.callBack.onSuccess(this.mResponse);
            } else {
                LogC.d("ASync invoke action:" + this.mRequestData.getActionName() + " failed, resultCode:" + actionResult.getResultCode() + " resultDesc:" + actionResult.getResultDesc(), false);
                this.callBack.onFail(this.mResponse);
            }
            return this.mResponse;
        }
    }

    private LocalRouter() {
    }

    public static LocalRouter getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new LocalRouter();
                }
            }
        }
        return instance;
    }

    public RouterResponse invoke(RouterRequest routerRequest, RouterActionCallBack callBack) {
        LogC.d("Invoke action : " + routerRequest.getAbsoluteActionName(), false);
        WalletAction targetAction = findAction(routerRequest);
        if (targetAction == null) {
            LogC.d("Action not found,action:" + routerRequest.getAbsoluteActionName(), false);
            return new RouterResponse(RouterResponse.ERROR_CODE_ACTION_NOTFOUND, "Action not found,action:" + routerRequest.getAbsoluteActionName());
        }
        RouterResponse routerResponse = new RouterResponse();
        WalletActionRequest actionRequest = new WalletActionRequest(routerRequest.getAction(), routerRequest.getParamMap());
        if (callBack == null || !targetAction.isAsync(routerRequest.getContext(), actionRequest)) {
            LogC.d("Sync invoke action:" + routerRequest.getAbsoluteActionName(), false);
            routerResponse.setAsync(false);
            WalletActionResult actionResult = targetAction.invoke(routerRequest.getContext(), actionRequest);
            routerResponse.setSuccess(actionResult.isSuccess());
            routerResponse.addResultMap(actionResult.getResultParamMap());
        } else {
            LogC.d("ASync invoke action:" + routerRequest.getAbsoluteActionName(), false);
            routerResponse.setAsync(true);
            LocalTask localTask = new LocalTask(callBack, routerResponse, actionRequest, routerRequest.getContext(), targetAction);
            ThreadPoolManager.getInstance().getThreadPoolExecutor().submit(localTask);
        }
        return routerResponse;
    }

    private String getABTestSchema(RouterRequest routerRequest) {
        return null;
    }

    private WalletAction findAction(RouterRequest routerRequest) {
        String schemaName = getABTestSchema(routerRequest);
        Map<String, WalletProvider> map = this.providerMap;
        WalletProvider schemaProvider = map.get(schemaName + ":" + routerRequest.getProviderName());
        WalletProvider defaultProvider = this.providerMap.get(routerRequest.getProviderName());
        if (schemaProvider == null && defaultProvider == null) {
            LogC.d("Provider not found, provider" + routerRequest.getProviderName(), false);
            return null;
        }
        WalletAction targetAction = null;
        if (schemaProvider != null) {
            targetAction = schemaProvider.findAction(routerRequest.getAction());
        }
        if (targetAction == null) {
            targetAction = defaultProvider.findAction(routerRequest.getAction());
        }
        if (targetAction == null) {
            LogC.d("Action not found,action" + routerRequest.getAction(), false);
        }
        return targetAction;
    }

    public void registerProvider(WalletProvider provider) {
        this.providerMap.put(provider.getAbsoluteName(), provider);
    }

    public void registerProvider(String schema, WalletProvider provider) {
        if (schema != null && !"Default".equalsIgnoreCase(schema)) {
            provider.setSchema(schema);
        }
        registerProvider(provider);
    }
}
