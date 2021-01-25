package ohos.abilityshell.support;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityUtilsProxy {
    private static final AbilityUtilsProxy INSTANCE = new AbilityUtilsProxy();
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108160, "AbilityUtilsProxy");

    private AbilityUtilsProxy() {
    }

    public static AbilityUtilsProxy getInstance() {
        return INSTANCE;
    }

    public void startAbility(Context context, Intent intent) {
        IAbilityUtils service = AbilityUtilsHelper.getService();
        if (service == null) {
            AppLog.e(LOG_LABEL, "AbilityUtilsProxy::startAbility can not get IAbilityUtils service!", new Object[0]);
        } else {
            service.startAbility(context, intent);
        }
    }

    public void startForegroundAbility(Context context, Intent intent) {
        IAbilityUtils service = AbilityUtilsHelper.getService();
        if (service == null) {
            AppLog.e(LOG_LABEL, "AbilityUtilsProxy::startForegroundAbility can not get IAbilityUtils service!", new Object[0]);
        } else {
            service.startForegroundAbility(context, intent);
        }
    }

    public void startAbilityForResult(Context context, Intent intent, int i) {
        IAbilityUtils service = AbilityUtilsHelper.getService();
        if (service == null) {
            AppLog.e(LOG_LABEL, "AbilityUtilsProxy::startAbilityForResult can not get IAbilityUtils service!", new Object[0]);
        } else {
            service.startAbilityForResult(context, intent, i);
        }
    }

    public boolean stopAbility(Context context, Intent intent) {
        IAbilityUtils service = AbilityUtilsHelper.getService();
        if (service != null) {
            return service.stopAbility(context, intent);
        }
        AppLog.e(LOG_LABEL, "AbilityUtilsProxy::stopAbility can not get IAbilityUtils service!", new Object[0]);
        return false;
    }

    public int connectAbility(Context context, Intent intent, ServiceConnection serviceConnection) {
        IAbilityUtils service = AbilityUtilsHelper.getService();
        if (service != null) {
            return service.connectAbility(context, intent, serviceConnection);
        }
        AppLog.e(LOG_LABEL, "AbilityUtilsProxy::connectAbility can not get IAbilityUtils service!", new Object[0]);
        return -1;
    }

    public void disconnectAbility(Context context, ServiceConnection serviceConnection) {
        IAbilityUtils service = AbilityUtilsHelper.getService();
        if (service == null) {
            AppLog.e(LOG_LABEL, "AbilityUtilsProxy::disconnectAbility can not get IAbilityUtils service!", new Object[0]);
        } else {
            service.disconnectAbility(context, serviceConnection);
        }
    }
}
