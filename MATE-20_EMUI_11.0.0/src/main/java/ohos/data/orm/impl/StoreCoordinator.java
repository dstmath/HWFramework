package ohos.data.orm.impl;

import java.util.concurrent.ConcurrentHashMap;
import ohos.app.Context;
import ohos.data.orm.OrmConfig;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmDatabase;
import ohos.data.orm.OrmMigration;
import ohos.data.orm.OrmObject;
import ohos.data.orm.OrmObjectObserver;

public final class StoreCoordinator {
    private static final Object LOCK = new Object();
    private static StoreCoordinator coordinator;
    private final Object MAP_LOCK = new Object();
    private final Object NOTIFY_MANAGER_LOCK = new Object();
    private final ConcurrentHashMap<String, OrmStore> aliasToOrmStore = new ConcurrentHashMap<>(10);
    private final ConcurrentHashMap<OrmContext, OrmStore> contextToOrmStore = new ConcurrentHashMap<>(20);
    private NotifyManager notifyManager = new NotifyManager();

    private StoreCoordinator() {
    }

    public static StoreCoordinator getInstance() {
        StoreCoordinator storeCoordinator;
        synchronized (LOCK) {
            if (coordinator == null) {
                coordinator = new StoreCoordinator();
            }
            storeCoordinator = coordinator;
        }
        return storeCoordinator;
    }

    public <T extends OrmDatabase> void createOrmStore(Context context, OrmConfig ormConfig, Class<T> cls, OrmMigration... ormMigrationArr) {
        String alias = ormConfig.getAlias();
        synchronized (this.MAP_LOCK) {
            if (!this.aliasToOrmStore.containsKey(alias)) {
                this.aliasToOrmStore.put(alias, OrmStore.open(context, ormConfig, cls, ormMigrationArr));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public OrmStore acquireOrmStore(String str, OrmContext ormContext) {
        synchronized (this.MAP_LOCK) {
            if (!this.aliasToOrmStore.containsKey(str)) {
                return null;
            }
            OrmStore ormStore = this.aliasToOrmStore.get(str);
            this.contextToOrmStore.put(ormContext, ormStore);
            return ormStore;
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseOrmStore(String str, OrmContext ormContext) {
        synchronized (this.MAP_LOCK) {
            OrmStore remove = this.contextToOrmStore.remove(ormContext);
            if (remove != null && !this.contextToOrmStore.containsValue(remove)) {
                removeOrmStore(str);
            }
        }
    }

    private void removeOrmStore(String str) {
        synchronized (this.MAP_LOCK) {
            this.aliasToOrmStore.remove(str).close();
        }
    }

    /* access modifiers changed from: package-private */
    public void registerStoreListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.registerStoreListener(str, ormObjectObserver, ormContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerContextListener(OrmContext ormContext, OrmObjectObserver ormObjectObserver, OrmContext ormContext2) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.registerContextListener(ormContext, ormObjectObserver, ormContext2);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerEntityListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.registerEntityListener(str, ormObjectObserver, ormContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerObjcetListener(OrmObject ormObject, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.registerObjcetListener(ormObject, ormObjectObserver, ormContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterStoreListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.unregisterStoreListener(str, ormObjectObserver, ormContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterContextListener(OrmContext ormContext, OrmObjectObserver ormObjectObserver, OrmContext ormContext2) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.unregisterContextListener(ormContext, ormObjectObserver, ormContext2);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterEntityListener(String str, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.unregisterEntityListener(str, ormObjectObserver, ormContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterObjectListener(OrmObject ormObject, OrmObjectObserver ormObjectObserver, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.unregisterObjectListener(ormObject, ormObjectObserver, ormContext);
        }
    }

    public void sendMessage(SaveRequest saveRequest, String str, OrmContext ormContext) {
        synchronized (this.NOTIFY_MANAGER_LOCK) {
            this.notifyManager.addMessageToQueue(saveRequest, ormContext, str);
        }
    }
}
