package com.android.server.appbinding.finders;

import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.carrier.CarrierMessagingClientService;
import android.service.carrier.ICarrierMessagingClientService;
import android.text.TextUtils;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.CollectionUtils;
import com.android.server.appbinding.AppBindingConstants;
import java.util.function.BiConsumer;

public class CarrierMessagingClientServiceFinder extends AppServiceFinder<CarrierMessagingClientService, ICarrierMessagingClientService> {
    private final OnRoleHoldersChangedListener mRoleHolderChangedListener = new OnRoleHoldersChangedListener() {
        /* class com.android.server.appbinding.finders.$$Lambda$CarrierMessagingClientServiceFinder$HEVyQ3IEZ8Eseze74Vyp3NHEREg */

        public final void onRoleHoldersChanged(String str, UserHandle userHandle) {
            CarrierMessagingClientServiceFinder.this.lambda$new$0$CarrierMessagingClientServiceFinder(str, userHandle);
        }
    };
    private final RoleManager mRoleManager;

    public CarrierMessagingClientServiceFinder(Context context, BiConsumer<AppServiceFinder, Integer> listener, Handler callbackHandler) {
        super(context, listener, callbackHandler);
        this.mRoleManager = (RoleManager) context.getSystemService(RoleManager.class);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public boolean isEnabled(AppBindingConstants constants) {
        return constants.SMS_SERVICE_ENABLED && this.mContext.getResources().getBoolean(17891563);
    }

    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public String getAppDescription() {
        return "[Default SMS app]";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public Class<CarrierMessagingClientService> getServiceClass() {
        return CarrierMessagingClientService.class;
    }

    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public ICarrierMessagingClientService asInterface(IBinder obj) {
        return ICarrierMessagingClientService.Stub.asInterface(obj);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public String getServiceAction() {
        return "android.telephony.action.CARRIER_MESSAGING_CLIENT_SERVICE";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public String getServicePermission() {
        return "android.permission.BIND_CARRIER_MESSAGING_CLIENT_SERVICE";
    }

    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public String getTargetPackage(int userId) {
        return (String) CollectionUtils.firstOrNull(this.mRoleManager.getRoleHoldersAsUser("android.app.role.SMS", UserHandle.of(userId)));
    }

    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public void startMonitoring() {
        this.mRoleManager.addOnRoleHoldersChangedListenerAsUser(BackgroundThread.getExecutor(), this.mRoleHolderChangedListener, UserHandle.ALL);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public String validateService(ServiceInfo service) {
        String packageName = service.packageName;
        String process = service.processName;
        if (process == null || TextUtils.equals(packageName, process)) {
            return "Service must not run on the main process";
        }
        return null;
    }

    @Override // com.android.server.appbinding.finders.AppServiceFinder
    public int getBindFlags(AppBindingConstants constants) {
        return constants.SMS_APP_BIND_FLAGS;
    }

    public /* synthetic */ void lambda$new$0$CarrierMessagingClientServiceFinder(String role, UserHandle user) {
        if ("android.app.role.SMS".equals(role)) {
            this.mListener.accept(this, Integer.valueOf(user.getIdentifier()));
        }
    }
}
