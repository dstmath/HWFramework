package com.huawei.ohos.interwork;

import android.content.ActivityNotFoundException;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.app.Context;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class AndroidUtils {
    private static final int SHELL_ACTIVITY = 1;
    private static final int SHELL_SERVICE = 2;
    private static final String SHELL_TYPE = "shellType";
    private static final HiLogLabel SUPPORT_LABEL = new HiLogLabel(3, 218108160, "AZSupport");

    public static void startActivity(Context context, Intent intent) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::startActivity", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startActivity context or intent is null!", new Object[0]);
            return;
        }
        addIntentFlagAndType(intent, 1);
        try {
            if (context instanceof Ability) {
                ((Ability) context).startAbility(intent);
            } else if (context instanceof AbilitySlice) {
                ((AbilitySlice) context).startAbility(intent);
            } else {
                AppLog.e(SUPPORT_LABEL, "AndroidUtils::startActivity unknown context!", new Object[0]);
            }
        } catch (ActivityNotFoundException unused) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startActivity ActivityNotFoundException!", new Object[0]);
        }
    }

    public static void startActivityForResult(Context context, Intent intent, int i) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::startActivityForResult", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startActivityForResult context or intent is null!", new Object[0]);
            return;
        }
        addIntentFlagAndType(intent, 1);
        try {
            if (context instanceof Ability) {
                ((Ability) context).startAbilityForResult(intent, i);
            } else if (context instanceof AbilitySlice) {
                ((AbilitySlice) context).startAbilityForResult(intent, i);
            } else {
                AppLog.e(SUPPORT_LABEL, "AndroidUtils::startActivityForResult unknown context!", new Object[0]);
            }
        } catch (ActivityNotFoundException unused) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startActivityForResult ActivityNotFoundException!", new Object[0]);
        }
    }

    public static void startService(Context context, Intent intent) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::startService", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startService context or intent is null!", new Object[0]);
            return;
        }
        addIntentFlagAndType(intent, 2);
        if (context instanceof Ability) {
            ((Ability) context).startAbility(intent);
        } else if (context instanceof AbilitySlice) {
            ((AbilitySlice) context).startAbility(intent);
        } else {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startService unknown context!", new Object[0]);
        }
    }

    public static void startForegroundService(Context context, Intent intent) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::startForegroundService", new Object[0]);
        if (intent == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::startForegroundService intent is null!", new Object[0]);
            return;
        }
        intent.setFlags(512);
        startService(context, intent);
    }

    public static void stopService(Context context, Intent intent) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::stopService", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::stopService context or intent is null!", new Object[0]);
            return;
        }
        addIntentFlagAndType(intent, 2);
        if (context instanceof Ability) {
            ((Ability) context).stopAbility(intent);
        } else if (context instanceof AbilitySlice) {
            ((AbilitySlice) context).stopAbility(intent);
        } else {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::stopService unknown context!", new Object[0]);
        }
    }

    public static int bindService(Context context, Intent intent, IAbilityConnection iAbilityConnection) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::bindService", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::bindService context or intent is null!", new Object[0]);
            return -1;
        }
        addIntentFlagAndType(intent, 2);
        if (context instanceof Ability) {
            ((Ability) context).connectAbility(intent, iAbilityConnection);
            return 0;
        } else if (context instanceof AbilitySlice) {
            ((AbilitySlice) context).connectAbility(intent, iAbilityConnection);
            return 0;
        } else {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::bindService unknown context!", new Object[0]);
            return -1;
        }
    }

    public static int unbindService(Context context, IAbilityConnection iAbilityConnection) {
        AppLog.d(SUPPORT_LABEL, "AndroidUtils::unbindService", new Object[0]);
        if (context == null) {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::unbindService context or intent is null!", new Object[0]);
            return -1;
        } else if (context instanceof Ability) {
            ((Ability) context).disconnectAbility(iAbilityConnection);
            return 0;
        } else if (context instanceof AbilitySlice) {
            ((AbilitySlice) context).disconnectAbility(iAbilityConnection);
            return 0;
        } else {
            AppLog.e(SUPPORT_LABEL, "AndroidUtils::unbindService unknown context!", new Object[0]);
            return -1;
        }
    }

    private static void addIntentFlagAndType(Intent intent, int i) {
        intent.addFlags(16);
        IntentParams params = intent.getParams();
        if (params == null) {
            params = new IntentParams();
        }
        params.setParam(SHELL_TYPE, Integer.valueOf(i));
        intent.setParams(params);
    }

    private static void handleForwardFlag(Intent intent, android.content.Intent intent2) {
        if ((intent.getFlags() & 4) != 0) {
            AppLog.d(SUPPORT_LABEL, "AndroidUtils::handleForwardFlag have FLAG_ABILITY_FORWARD_RESULT", new Object[0]);
            intent2.setFlags(intent2.getFlags() | 33554432);
        }
    }
}
