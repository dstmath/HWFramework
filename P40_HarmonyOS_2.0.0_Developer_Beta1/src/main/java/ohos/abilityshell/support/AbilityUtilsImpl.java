package ohos.abilityshell.support;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import java.util.Optional;
import ohos.abilityshell.AbilityShellData;
import ohos.abilityshell.DistributedImpl;
import ohos.abilityshell.IDistributedManager;
import ohos.abilityshell.utils.IntentConverter;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class AbilityUtilsImpl implements IAbilityUtils {
    private static final IDistributedManager DISTRIBUTED_IMPL = new DistributedImpl();
    private static final int INVALID_REQUEST_CODE = -1;
    private static final int MAX_REQUEST_CODE = 65535;
    private static final int MIN_REQUEST_CODE = 0;
    private static final HiLogLabel SUPPORT_LABEL = new HiLogLabel(3, 218108160, "AZSupport");

    private boolean isRequestCodeValid(int i) {
        return i >= 0 && i <= 65535;
    }

    @Override // ohos.abilityshell.support.IAbilityUtils
    public void startAbility(Context context, Intent intent) {
        startAbilityForResult(context, intent, false, -1, AbilityInfo.AbilityType.UNKNOWN);
    }

    @Override // ohos.abilityshell.support.IAbilityUtils
    public void startForegroundAbility(Context context, Intent intent) {
        startAbilityForResult(context, intent, true, -1, AbilityInfo.AbilityType.SERVICE);
    }

    @Override // ohos.abilityshell.support.IAbilityUtils
    public void startAbilityForResult(Context context, Intent intent, int i) {
        startAbilityForResult(context, intent, false, i, AbilityInfo.AbilityType.PAGE);
    }

    private void startAbilityForResult(Context context, Intent intent, boolean z, int i, AbilityInfo.AbilityType abilityType) {
        AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::startAbility", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::startAbility intent is null!", new Object[0]);
            throw new IllegalArgumentException("context or intent is null, can't start ability");
        }
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, null);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::startAbility createZidaneIntent failed", new Object[0]);
            return;
        }
        AbilityShellData selectAbility = selectAbility(createZidaneIntent.get());
        if (selectAbility == null || !selectAbility.getLocal() || selectAbility.getShellInfo() == null || selectAbility.getAbilityInfo() == null) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::startAbility selectAbility failed", new Object[0]);
            throw new SecurityException("ability can not be found, can't start ability");
        }
        checkAbilityType(abilityType, selectAbility.getAbilityInfo().getType());
        startLocalAbility(context, selectAbility, createZidaneIntent.get(), z, i);
    }

    private void startLocalAbility(Context context, AbilityShellData abilityShellData, ohos.aafwk.content.Intent intent, boolean z, int i) {
        ShellInfo shellInfo = abilityShellData.getShellInfo();
        AbilityInfo abilityInfo = abilityShellData.getAbilityInfo();
        Optional<Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
        if (!createAndroidIntent.isPresent()) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::startLocalAbility createAndroidIntent", new Object[0]);
            return;
        }
        handleForwardFlag(intent, createAndroidIntent.get());
        if (context instanceof Service) {
            createAndroidIntent.get().addFlags(268435456);
        }
        if (abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
            if (!isRequestCodeValid(i)) {
                try {
                    context.startActivity(createAndroidIntent.get());
                } catch (ActivityNotFoundException unused) {
                    throw new SecurityException("ability can not be found, can't start ability");
                }
            } else if (context instanceof Activity) {
                try {
                    ((Activity) context).startActivityForResult(createAndroidIntent.get(), i);
                } catch (ActivityNotFoundException unused2) {
                    throw new SecurityException("ability can not be found, can't start ability");
                }
            } else {
                AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::startLocalAbility only Activity support", new Object[0]);
            }
        } else if (abilityInfo.getType() != AbilityInfo.AbilityType.SERVICE) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::startLocalAbility not page an service ability", new Object[0]);
        } else if (z) {
            try {
                context.startForegroundService(createAndroidIntent.get());
            } catch (SecurityException unused3) {
                throw new SecurityException("ability can not be found, can't start ability");
            } catch (IllegalStateException unused4) {
                throw new IllegalStateException("caller is wrong state, can't start ability");
            }
        } else {
            context.startService(createAndroidIntent.get());
        }
    }

    private void checkAbilityType(AbilityInfo.AbilityType abilityType, AbilityInfo.AbilityType abilityType2) {
        if (abilityType != AbilityInfo.AbilityType.UNKNOWN && abilityType != abilityType2) {
            throw new IllegalStateException("request ability type is wrong, start ability failed");
        }
    }

    private boolean stopLocalAbility(Context context, AbilityShellData abilityShellData, ohos.aafwk.content.Intent intent) {
        AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility called", new Object[0]);
        ShellInfo shellInfo = abilityShellData.getShellInfo();
        if (shellInfo == null) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility shellInfo is null", new Object[0]);
            return false;
        } else if (shellInfo.getType() == ShellInfo.ShellType.SERVICE) {
            Optional<Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
            if (!createAndroidIntent.isPresent()) {
                AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility createAndroidIntent failed", new Object[0]);
                return false;
            }
            try {
                return context.stopService(createAndroidIntent.get());
            } catch (SecurityException unused) {
                AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility ability not found", new Object[0]);
                throw new SecurityException("ability can not be found, can't stop ability");
            } catch (IllegalStateException unused2) {
                AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility caller is wrong state.", new Object[0]);
                throw new IllegalStateException("caller is wrong state, can't stop ability");
            }
        } else {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility ShellType not SERVICE", new Object[0]);
            throw new IllegalStateException("request ability is not a service, stop ability failed");
        }
    }

    private void handleForwardFlag(ohos.aafwk.content.Intent intent, Intent intent2) {
        if ((intent.getFlags() & 4) != 0) {
            AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::handleForwardFlag have FLAG_ABILITY_FORWARD_RESULT", new Object[0]);
            intent2.setFlags(intent2.getFlags() | 33554432);
        }
    }

    @Override // ohos.abilityshell.support.IAbilityUtils
    public boolean stopAbility(Context context, Intent intent) {
        AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::stopAbility", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopAbility intent is null!", new Object[0]);
            throw new IllegalArgumentException("context or intent is null, can't stop ability");
        }
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, null);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopAbility createZidaneIntent failed", new Object[0]);
            return false;
        }
        AbilityShellData selectAbility = selectAbility(createZidaneIntent.get());
        if (selectAbility != null && selectAbility.getLocal()) {
            return stopLocalAbility(context, selectAbility, createZidaneIntent.get());
        }
        AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopAbility selectAbility failed", new Object[0]);
        return false;
    }

    @Override // ohos.abilityshell.support.IAbilityUtils
    public int connectAbility(Context context, Intent intent, ServiceConnection serviceConnection) {
        AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::connectAbility", new Object[0]);
        if (context == null || intent == null) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::connectAbility intent is null!", new Object[0]);
            throw new IllegalArgumentException("context or intent is null, can't connect ability");
        }
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, null);
        if (!createZidaneIntent.isPresent()) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::connectAbility createZidaneIntent failed", new Object[0]);
            return -1;
        }
        AbilityShellData selectAbility = selectAbility(createZidaneIntent.get());
        if (selectAbility == null) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::connectAbility selectAbility failed", new Object[0]);
            return -1;
        }
        ShellInfo shellInfo = selectAbility.getShellInfo();
        if (shellInfo == null || shellInfo.getType() != ShellInfo.ShellType.SERVICE) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::connectLocalAbility ShellType not SERVICE", new Object[0]);
            throw new IllegalStateException("request ability is not a service, connect ability failed");
        } else if (selectAbility.getLocal()) {
            return connectLocalAbility(context, shellInfo, createZidaneIntent.get(), serviceConnection);
        } else {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::unsupport connect remote ability", new Object[0]);
            return -1;
        }
    }

    private int connectLocalAbility(Context context, ShellInfo shellInfo, ohos.aafwk.content.Intent intent, ServiceConnection serviceConnection) {
        AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::connectLocalAbility called", new Object[0]);
        Optional<Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
        if (!createAndroidIntent.isPresent()) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::stopLocalAbility createAndroidIntent failed", new Object[0]);
            return -1;
        }
        try {
            if (context.bindService(createAndroidIntent.get(), serviceConnection, 1)) {
                return 0;
            }
            return -1;
        } catch (SecurityException unused) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::connectLocalAbility ability not found", new Object[0]);
            throw new SecurityException("ability can not be found, can't connect ability");
        }
    }

    @Override // ohos.abilityshell.support.IAbilityUtils
    public void disconnectAbility(Context context, ServiceConnection serviceConnection) {
        AppLog.d(SUPPORT_LABEL, "AbilityUtilsImpl::disconnectAbility", new Object[0]);
        if (context != null) {
            context.unbindService(serviceConnection);
        } else {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::disconnectAbility context is null!", new Object[0]);
            throw new IllegalArgumentException("context is null, can't disconnect ability");
        }
    }

    private AbilityShellData selectAbility(ohos.aafwk.content.Intent intent) {
        try {
            return DISTRIBUTED_IMPL.selectAbility(intent);
        } catch (RemoteException e) {
            AppLog.e(SUPPORT_LABEL, "AbilityUtilsImpl::selectAbility RemoteException: %{public}s", e.getMessage());
            return null;
        }
    }
}
