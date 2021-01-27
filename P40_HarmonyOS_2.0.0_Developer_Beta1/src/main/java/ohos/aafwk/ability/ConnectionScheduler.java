package ohos.aafwk.ability;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.aafwk.utils.log.KeyLog;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.app.AbilityContext;
import ohos.app.Context;

/* access modifiers changed from: package-private */
public final class ConnectionScheduler {
    private static final LogLabel LABEL = LogLabel.create();
    private Context context;
    private final Map<AbilityContext, Map<IAbilityConnection, AbilityConnectionWrap>> serviceConns = new HashMap();

    ConnectionScheduler(Context context2) {
        this.context = context2;
    }

    /* access modifiers changed from: package-private */
    public boolean openServiceConnection(AbilityContext abilityContext, Intent intent, IAbilityConnection iAbilityConnection) throws IllegalArgumentException {
        AbilityConnectionWrap abilityConnectionWrap;
        if (abilityContext == null) {
            throw new IllegalArgumentException("open service connection failed. context is null.");
        } else if (intent == null) {
            throw new IllegalArgumentException("open service connection failed. intent is null, context: " + abilityContext);
        } else if (iAbilityConnection != null) {
            synchronized (this.serviceConns) {
                Map<IAbilityConnection, AbilityConnectionWrap> map = this.serviceConns.get(abilityContext);
                if (map == null) {
                    map = new HashMap<>();
                    this.serviceConns.put(abilityContext, map);
                }
                abilityConnectionWrap = map.get(iAbilityConnection);
                if (abilityConnectionWrap == null) {
                    abilityConnectionWrap = new AbilityConnectionWrap(abilityContext, iAbilityConnection, intent.getElement());
                    map.put(iAbilityConnection, abilityConnectionWrap);
                }
            }
            KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: element: %{public}s, context: %{public}s, conn: %{public}s, connWrap: %{public}s", LABEL.getTag(), KeyLog.CONNECT_ABILITY, KeyLog.LogState.START, Optional.of(intent).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"), abilityContext, iAbilityConnection, abilityConnectionWrap);
            if (this.context.connectAbility(intent, abilityConnectionWrap)) {
                return true;
            }
            Log.error(LABEL, "connect ability failed, connWrap: %{public}s", abilityConnectionWrap);
            return false;
        } else {
            throw new IllegalArgumentException("open service connection failed. conn is null, context: " + abilityContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void closeServiceConnection(AbilityContext abilityContext, IAbilityConnection iAbilityConnection) throws IllegalArgumentException {
        if (abilityContext == null) {
            throw new IllegalArgumentException("close service connection failed. context is null.");
        } else if (iAbilityConnection == null) {
            closeServiceConnectionInner(abilityContext);
        } else {
            closeServiceConnectionInner(abilityContext, iAbilityConnection, true);
        }
    }

    private void closeServiceConnectionInner(AbilityContext abilityContext, IAbilityConnection iAbilityConnection, boolean z) {
        AbilityConnectionWrap abilityConnectionWrap;
        synchronized (this.serviceConns) {
            if (abilityContext != null) {
                Map<IAbilityConnection, AbilityConnectionWrap> map = this.serviceConns.get(abilityContext);
                if (map != null) {
                    abilityConnectionWrap = map.get(iAbilityConnection);
                    if (abilityConnectionWrap != null) {
                        abilityConnectionWrap.setCallerRunning(z);
                        map.remove(iAbilityConnection);
                    }
                } else {
                    abilityConnectionWrap = null;
                }
            } else {
                throw new IllegalArgumentException("close service connection failed. context is null");
            }
        }
        if (abilityConnectionWrap != null) {
            KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: context: %{public}s, conn: %{public}s, connWrap: %{public}s", LABEL.getTag(), KeyLog.DISCONNECT_ABILITY, KeyLog.LogState.START, abilityContext, iAbilityConnection, abilityConnectionWrap);
            this.context.disconnectAbility(abilityConnectionWrap);
        }
    }

    private void closeServiceConnectionInner(AbilityContext abilityContext) {
        synchronized (this.serviceConns) {
            Map<IAbilityConnection, AbilityConnectionWrap> map = this.serviceConns.get(abilityContext);
            if (map != null) {
                if (Log.isDebuggable()) {
                    Log.debug(LABEL, "begin to remove. context: %{public}s", abilityContext);
                }
                for (IAbilityConnection iAbilityConnection : new ArrayList(map.keySet())) {
                    closeServiceConnectionInner(abilityContext, iAbilityConnection, false);
                }
                if (map.isEmpty()) {
                    Log.info(LABEL, "start to remove record for context. context: %{public}s", abilityContext);
                    this.serviceConns.remove(abilityContext);
                } else {
                    Log.error(LABEL, "some conns delete failed for context: %{public}s", abilityContext);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpServiceList(String str, PrintWriter printWriter, AbilityContext abilityContext) {
        synchronized (this.serviceConns) {
            Map<IAbilityConnection, AbilityConnectionWrap> map = this.serviceConns.get(abilityContext);
            if (map != null) {
                if (!map.isEmpty()) {
                    for (AbilityConnectionWrap abilityConnectionWrap : map.values()) {
                        abilityConnectionWrap.dump(str, printWriter);
                    }
                    return;
                }
            }
            Log.info(LABEL, "service list is null", new Object[0]);
            printWriter.println(str + "none");
        }
    }
}
