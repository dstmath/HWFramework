package com.huawei.coauth.fusion;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.coauth.fusion.FusionAuthContext;
import com.huawei.coauth.utils.LogUtils;
import com.huawei.fusionauth.fusion.IFusion;
import com.huawei.fusionauth.fusion.IFusionCallback;
import com.huawei.fusionauth.fusion.ITemplateIdCallback;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FusionAuth {
    private static final FusionAuth INSTANCE = new FusionAuth();
    private static final String TAG = "FusionAuth";
    private final Object INSTANCE_SYNC = new Object();

    public interface FusionTemplateIdCallback {
        void onFailed(int i);

        void onSuccess(List<String> list);
    }

    public static FusionAuth getInstance() {
        return INSTANCE;
    }

    private FusionAuth() {
    }

    public void fusionAuth(FusionAuthContext fusionAuthContext, FusionAuthCallback callback) {
        if (callback != null) {
            if (fusionAuthContext == null || fusionAuthContext.appContext == null || fusionAuthContext.authTypes == null || fusionAuthContext.authTypes.length == 0) {
                callback.onAuthFinish(fusionAuthContext, -2);
                return;
            }
            synchronized (this.INSTANCE_SYNC) {
                LogUtils.info(TAG, "Receive client's fusionAuth request.");
                FusionConnectManager.connectService(fusionAuthContext.appContext.getApplicationContext()).ifPresent(new FusionAuthConsumer(fusionAuthContext, callback));
            }
        }
    }

    public void queryTemplateId(Context context, FusionAuthType authType, FusionTemplateIdCallback callback) {
        if (callback != null) {
            if (authType == null || authType.getValue() > FusionAuthType.BEHAVIOR.getValue()) {
                callback.onFailed(-2);
                return;
            }
            synchronized (this.INSTANCE_SYNC) {
                LogUtils.info(TAG, "Receive client's fusionAuth request.");
                FusionConnectManager.connectService(context).ifPresent(new QueryConsumer(authType, callback));
            }
        }
    }

    private static class QueryConsumer implements Consumer<IFusion> {
        FusionAuthType authType;
        FusionTemplateIdCallback callback;

        private QueryConsumer(FusionAuthType authType2, FusionTemplateIdCallback callback2) {
            this.authType = authType2;
            this.callback = callback2;
        }

        public void accept(IFusion trustedThingsService) {
            try {
                LogUtils.info(FusionAuth.TAG, "prepare to query template id");
                trustedThingsService.query(this.authType.getValue(), new QueryCallback(this.callback));
            } catch (RemoteException e) {
                LogUtils.info(FusionAuth.TAG, "QueryConsumer RemoteException");
            }
        }
    }

    /* access modifiers changed from: private */
    public static class QueryCallback extends ITemplateIdCallback.Stub {
        private FusionTemplateIdCallback callback;

        private QueryCallback(FusionTemplateIdCallback callback2) {
            this.callback = callback2;
        }

        @Override // com.huawei.fusionauth.fusion.ITemplateIdCallback
        public void onSuccess(String[] templateIds) throws RemoteException {
            if (templateIds != null) {
                this.callback.onSuccess(Arrays.asList(templateIds));
            }
        }

        @Override // com.huawei.fusionauth.fusion.ITemplateIdCallback
        public void onFailed(int errCode) throws RemoteException {
            this.callback.onFailed(errCode);
        }
    }

    private static class FusionAuthConsumer implements Consumer<IFusion> {
        private FusionAuthCallback callback;
        private FusionAuthContext fusionAuthContext;

        private FusionAuthConsumer(FusionAuthContext fusionAuthContext2, FusionAuthCallback callback2) {
            this.fusionAuthContext = fusionAuthContext2;
            this.callback = callback2;
        }

        public void accept(IFusion trustedThingsService) {
            try {
                LogUtils.info(FusionAuth.TAG, "prepare to start auth");
                FusionAuth.triggerState(this.fusionAuthContext, FusionAuthContext.Status.RUNNING);
                trustedThingsService.start(this.fusionAuthContext.toBundle(), new FusionCallback(this.fusionAuthContext, this.callback));
            } catch (RemoteException e) {
                this.callback.onAuthFinish(this.fusionAuthContext, -1);
            }
        }
    }

    public void cancelAuth(FusionAuthContext fusionAuthContext) {
        if (fusionAuthContext != null && fusionAuthContext.appContext != null) {
            synchronized (this.INSTANCE_SYNC) {
                LogUtils.info(TAG, "Receive client's cancelAuth request.");
                FusionConnectManager.connectService(fusionAuthContext.appContext.getApplicationContext()).ifPresent(new FusionCancelConsumer(fusionAuthContext));
            }
        }
    }

    private static class FusionCancelConsumer implements Consumer<IFusion> {
        private FusionAuthContext fusionAuthContext;

        private FusionCancelConsumer(FusionAuthContext fusionAuthContext2) {
            this.fusionAuthContext = fusionAuthContext2;
        }

        public void accept(IFusion trustedThingsService) {
            try {
                trustedThingsService.cancel(this.fusionAuthContext.toBundle());
            } catch (RemoteException e) {
                LogUtils.error(FusionAuth.TAG, "remote exception whe cancel.");
            }
        }
    }

    public interface FusionAuthCallback {
        void onAuthFinish(FusionAuthContext fusionAuthContext, int i);

        default void onAuthStart(FusionAuthContext authContext) {
        }

        default void onAuthProcess(FusionAuthContext authContext, int status) {
        }
    }

    /* access modifiers changed from: private */
    public static class FusionCallback extends IFusionCallback.Stub {
        private static final String AUTH_TYPE = "AUTH_TYPE";
        private static final String TEMPLATE_ID = "TEMPLATE_ID";
        FusionAuthCallback callback;
        FusionAuthContext context;

        FusionCallback(FusionAuthContext context2, FusionAuthCallback callback2) {
            this.context = context2;
            this.callback = callback2;
        }

        @Override // com.huawei.fusionauth.fusion.IFusionCallback
        public void onAuthStart(Bundle extra) throws RemoteException {
            this.callback.onAuthStart(this.context);
        }

        @Override // com.huawei.fusionauth.fusion.IFusionCallback
        public void onAuthProcess(int process, Bundle extra) throws RemoteException {
            this.callback.onAuthProcess(this.context, process);
        }

        @Override // com.huawei.fusionauth.fusion.IFusionCallback
        public void onAuthFinish(int result, Bundle extra) throws RemoteException {
            FusionAuth.triggerState(this.context, FusionAuthContext.Status.FINISHED);
            if (extra != null && extra.containsKey(TEMPLATE_ID) && extra.containsKey(AUTH_TYPE)) {
                this.context.authToken = new FusionAuthToken(FusionAuthType.valueOf(extra.getInt(AUTH_TYPE)), extra.getString(TEMPLATE_ID));
            }
            this.callback.onAuthFinish(this.context, result);
        }
    }

    /* access modifiers changed from: private */
    public static void triggerState(FusionAuthContext context, FusionAuthContext.Status status) {
        if (context != null && status != null) {
            LogUtils.info(TAG, "from " + context.getStatus().getValue() + " to " + status.getValue());
            context.setStatus(status);
        }
    }
}
