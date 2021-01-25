package android.net.lowpan;

import android.content.Context;
import android.net.lowpan.ILowpanManager;
import android.net.lowpan.ILowpanManagerListener;
import android.net.lowpan.LowpanManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class LowpanManager {
    private static final String TAG = LowpanManager.class.getSimpleName();
    private final Map<IBinder, WeakReference<LowpanInterface>> mBinderCache = new WeakHashMap();
    private final Context mContext;
    private final Map<String, LowpanInterface> mInterfaceCache = new HashMap();
    private final Map<Integer, ILowpanManagerListener> mListenerMap = new HashMap();
    private final Looper mLooper;
    private final ILowpanManager mService;

    public static abstract class Callback {
        public void onInterfaceAdded(LowpanInterface lowpanInterface) {
        }

        public void onInterfaceRemoved(LowpanInterface lowpanInterface) {
        }
    }

    public static LowpanManager from(Context context) {
        return (LowpanManager) context.getSystemService("lowpan");
    }

    public static LowpanManager getManager() {
        IBinder binder = ServiceManager.getService("lowpan");
        if (binder != null) {
            return new LowpanManager(ILowpanManager.Stub.asInterface(binder));
        }
        return null;
    }

    LowpanManager(ILowpanManager service) {
        this.mService = service;
        this.mContext = null;
        this.mLooper = null;
    }

    public LowpanManager(Context context, ILowpanManager service, Looper looper) {
        this.mContext = context;
        this.mService = service;
        this.mLooper = looper;
    }

    public LowpanInterface getInterfaceNoCreate(ILowpanInterface ifaceService) {
        LowpanInterface iface = null;
        synchronized (this.mBinderCache) {
            if (this.mBinderCache.containsKey(ifaceService.asBinder())) {
                iface = this.mBinderCache.get(ifaceService.asBinder()).get();
            }
        }
        return iface;
    }

    public LowpanInterface getInterface(final ILowpanInterface ifaceService) {
        LowpanInterface iface = null;
        try {
            synchronized (this.mBinderCache) {
                if (this.mBinderCache.containsKey(ifaceService.asBinder())) {
                    iface = this.mBinderCache.get(ifaceService.asBinder()).get();
                }
                if (iface == null) {
                    final String ifaceName = ifaceService.getName();
                    iface = new LowpanInterface(this.mContext, ifaceService, this.mLooper);
                    synchronized (this.mInterfaceCache) {
                        this.mInterfaceCache.put(iface.getName(), iface);
                    }
                    this.mBinderCache.put(ifaceService.asBinder(), new WeakReference<>(iface));
                    ifaceService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class android.net.lowpan.LowpanManager.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (LowpanManager.this.mInterfaceCache) {
                                LowpanInterface iface = (LowpanInterface) LowpanManager.this.mInterfaceCache.get(ifaceName);
                                if (iface != null && iface.getService() == ifaceService) {
                                    LowpanManager.this.mInterfaceCache.remove(ifaceName);
                                }
                            }
                        }
                    }, 0);
                }
            }
            return iface;
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public LowpanInterface getInterface(String name) {
        LowpanInterface iface = null;
        try {
            synchronized (this.mInterfaceCache) {
                if (this.mInterfaceCache.containsKey(name)) {
                    iface = this.mInterfaceCache.get(name);
                } else {
                    ILowpanInterface ifaceService = this.mService.getInterface(name);
                    if (ifaceService != null) {
                        iface = getInterface(ifaceService);
                    }
                }
            }
            return iface;
        } catch (RemoteException x) {
            throw x.rethrowFromSystemServer();
        }
    }

    public LowpanInterface getInterface() {
        String[] ifaceList = getInterfaceList();
        if (ifaceList.length > 0) {
            return getInterface(ifaceList[0]);
        }
        return null;
    }

    public String[] getInterfaceList() {
        try {
            return this.mService.getInterfaceList();
        } catch (RemoteException x) {
            throw x.rethrowFromSystemServer();
        }
    }

    public void registerCallback(final Callback cb, final Handler handler) throws LowpanException {
        ILowpanManagerListener.Stub listenerBinder = new ILowpanManagerListener.Stub() {
            /* class android.net.lowpan.LowpanManager.AnonymousClass2 */
            private Handler mHandler;

            {
                Handler handler = handler;
                if (handler != null) {
                    this.mHandler = handler;
                } else if (LowpanManager.this.mLooper != null) {
                    this.mHandler = new Handler(LowpanManager.this.mLooper);
                } else {
                    this.mHandler = new Handler();
                }
            }

            @Override // android.net.lowpan.ILowpanManagerListener
            public void onInterfaceAdded(ILowpanInterface ifaceService) {
                this.mHandler.post(new Runnable(ifaceService, cb) {
                    /* class android.net.lowpan.$$Lambda$LowpanManager$2$2qKIy18LeIjTlm4mROgpHOPNU0 */
                    private final /* synthetic */ ILowpanInterface f$1;
                    private final /* synthetic */ LowpanManager.Callback f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LowpanManager.AnonymousClass2.this.lambda$onInterfaceAdded$0$LowpanManager$2(this.f$1, this.f$2);
                    }
                });
            }

            public /* synthetic */ void lambda$onInterfaceAdded$0$LowpanManager$2(ILowpanInterface ifaceService, Callback cb) {
                LowpanInterface iface = LowpanManager.this.getInterface(ifaceService);
                if (iface != null) {
                    cb.onInterfaceAdded(iface);
                }
            }

            @Override // android.net.lowpan.ILowpanManagerListener
            public void onInterfaceRemoved(ILowpanInterface ifaceService) {
                this.mHandler.post(new Runnable(ifaceService, cb) {
                    /* class android.net.lowpan.$$Lambda$LowpanManager$2$jhNE3pUzRwHtqpTRJOtHQRfgQ70 */
                    private final /* synthetic */ ILowpanInterface f$1;
                    private final /* synthetic */ LowpanManager.Callback f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LowpanManager.AnonymousClass2.this.lambda$onInterfaceRemoved$1$LowpanManager$2(this.f$1, this.f$2);
                    }
                });
            }

            public /* synthetic */ void lambda$onInterfaceRemoved$1$LowpanManager$2(ILowpanInterface ifaceService, Callback cb) {
                LowpanInterface iface = LowpanManager.this.getInterfaceNoCreate(ifaceService);
                if (iface != null) {
                    cb.onInterfaceRemoved(iface);
                }
            }
        };
        try {
            this.mService.addListener(listenerBinder);
            synchronized (this.mListenerMap) {
                this.mListenerMap.put(Integer.valueOf(System.identityHashCode(cb)), listenerBinder);
            }
        } catch (RemoteException x) {
            throw x.rethrowFromSystemServer();
        }
    }

    public void registerCallback(Callback cb) throws LowpanException {
        registerCallback(cb, null);
    }

    public void unregisterCallback(Callback cb) {
        ILowpanManagerListener listenerBinder;
        Integer hashCode = Integer.valueOf(System.identityHashCode(cb));
        synchronized (this.mListenerMap) {
            listenerBinder = this.mListenerMap.get(hashCode);
            this.mListenerMap.remove(hashCode);
        }
        if (listenerBinder != null) {
            try {
                this.mService.removeListener(listenerBinder);
            } catch (RemoteException x) {
                throw x.rethrowFromSystemServer();
            }
        } else {
            throw new RuntimeException("Attempt to unregister an unknown callback");
        }
    }
}
