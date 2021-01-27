package ohos.data.distributed.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KvManagerFactory {
    private static final KvManagerFactory FACTORY = new KvManagerFactory();
    private static final Map<KvManagerConfig, KvManagerImpl> KV_MANAGERS = new HashMap();
    private static final String LABEL = "KvManagerFactory";
    private static final Object LOCK = new Object();
    private static KvStoreServiceDeathRecipient deathObserver = null;
    private static boolean isServiceDeathReg = false;

    private KvManagerFactory() {
    }

    public static final KvManagerFactory getInstance() {
        return FACTORY;
    }

    public KvManager createKvManager(KvManagerConfig kvManagerConfig) throws KvStoreException {
        if (Objects.isNull(kvManagerConfig) || kvManagerConfig.isInvalid()) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Input param is null or invalid.");
        }
        synchronized (LOCK) {
            KvManagerImpl computeIfAbsent = KV_MANAGERS.computeIfAbsent(kvManagerConfig, $$Lambda$KvManagerFactory$JxupO5AzvjwmiRfkTROjMrjwI8.INSTANCE);
            if (!isServiceDeathReg && deathObserver != null) {
                LogPrint.info(LABEL, "Register stored deathObserver when createKvManager.", new Object[0]);
                try {
                    if (computeIfAbsent instanceof KvManagerImpl) {
                        computeIfAbsent.registerKvStoreServiceDeathRecipient(deathObserver);
                        isServiceDeathReg = true;
                    } else {
                        LogPrint.error(LABEL, "kvManager not instanceof KvManagerImpl", new Object[0]);
                        return computeIfAbsent;
                    }
                } catch (KvStoreException unused) {
                    LogPrint.error(LABEL, "Register stored deathObserver when createKvManager caught exception.", new Object[0]);
                }
            }
            return computeIfAbsent;
        }
    }

    static /* synthetic */ KvManagerImpl lambda$createKvManager$0(KvManagerConfig kvManagerConfig) {
        return new KvManagerImpl(kvManagerConfig);
    }

    public void registerKvStoreServiceDeathRecipient(KvStoreServiceDeathRecipient kvStoreServiceDeathRecipient) throws KvStoreException {
        if (!Objects.isNull(kvStoreServiceDeathRecipient)) {
            synchronized (LOCK) {
                if (deathObserver == null) {
                    deathObserver = kvStoreServiceDeathRecipient;
                    if (KV_MANAGERS.isEmpty()) {
                        LogPrint.info(LABEL, "No manager in map. Will register later when a manager is created.", new Object[0]);
                        return;
                    }
                    KvManagerImpl next = KV_MANAGERS.values().iterator().next();
                    if (next instanceof KvManagerImpl) {
                        next.registerKvStoreServiceDeathRecipient(kvStoreServiceDeathRecipient);
                        isServiceDeathReg = true;
                    } else {
                        LogPrint.error(LABEL, "kvManager not instanceof KvManagerImpl", new Object[0]);
                    }
                    return;
                }
                LogPrint.error(LABEL, "DeathObserver already registered.", new Object[0]);
                throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "DeathObserver already registered.");
            }
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Input deathObserver is null.");
    }

    public void unRegisterKvStoreServiceDeathRecipient(KvStoreServiceDeathRecipient kvStoreServiceDeathRecipient) throws KvStoreException {
        if (!Objects.isNull(kvStoreServiceDeathRecipient)) {
            synchronized (LOCK) {
                if (deathObserver == null) {
                    LogPrint.error(LABEL, "DeathObserver not registered.", new Object[0]);
                    throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "DeathObserver not registered.");
                } else if (!isServiceDeathReg) {
                    LogPrint.info(LABEL, "DeathObserver stored but has not been registered.", new Object[0]);
                    deathObserver = null;
                } else if (KV_MANAGERS.isEmpty()) {
                    LogPrint.error(LABEL, "No manager in map. This should not happen.", new Object[0]);
                } else {
                    KvManagerImpl next = KV_MANAGERS.values().iterator().next();
                    if (next instanceof KvManagerImpl) {
                        next.unRegisterKvStoreServiceDeathRecipient(kvStoreServiceDeathRecipient);
                        deathObserver = null;
                        isServiceDeathReg = false;
                    } else {
                        LogPrint.error(LABEL, "kvManager not instanceof KvManagerImpl", new Object[0]);
                    }
                }
            }
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
        }
    }
}
