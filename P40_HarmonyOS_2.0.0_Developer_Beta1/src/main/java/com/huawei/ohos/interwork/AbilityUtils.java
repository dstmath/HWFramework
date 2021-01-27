package com.huawei.ohos.interwork;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import ohos.abilityshell.support.AbilityUtilsProxy;

public class AbilityUtils {
    public static void startAbility(Context context, Intent intent) {
        AbilityUtilsProxy.getInstance().startAbility(context, intent);
    }

    public static void startForegroundAbility(Context context, Intent intent) {
        AbilityUtilsProxy.getInstance().startForegroundAbility(context, intent);
    }

    public static void startAbilityForResult(Context context, Intent intent, int i) {
        AbilityUtilsProxy.getInstance().startAbilityForResult(context, intent, i);
    }

    public static boolean stopAbility(Context context, Intent intent) {
        return AbilityUtilsProxy.getInstance().stopAbility(context, intent);
    }

    public static boolean connectAbility(Context context, Intent intent, ServiceConnection serviceConnection) {
        return AbilityUtilsProxy.getInstance().connectAbility(context, intent, serviceConnection) == 0;
    }

    public static void disconnectAbility(Context context, ServiceConnection serviceConnection) {
        AbilityUtilsProxy.getInstance().disconnectAbility(context, serviceConnection);
    }
}
