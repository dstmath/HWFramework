package android.net.lowpan;

import android.content.Context;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.lowpan.ILowpanInterfaceListener;
import android.net.lowpan.LowpanInterface;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import java.util.HashMap;

public class LowpanInterface {
    public static final String EMPTY_PARTITION_ID = "";
    public static final String NETWORK_TYPE_THREAD_V1 = "org.threadgroup.thread.v1";
    public static final String ROLE_COORDINATOR = "coordinator";
    public static final String ROLE_DETACHED = "detached";
    public static final String ROLE_END_DEVICE = "end-device";
    public static final String ROLE_LEADER = "leader";
    public static final String ROLE_ROUTER = "router";
    public static final String ROLE_SLEEPY_END_DEVICE = "sleepy-end-device";
    public static final String ROLE_SLEEPY_ROUTER = "sleepy-router";
    public static final String STATE_ATTACHED = "attached";
    public static final String STATE_ATTACHING = "attaching";
    public static final String STATE_COMMISSIONING = "commissioning";
    public static final String STATE_FAULT = "fault";
    public static final String STATE_OFFLINE = "offline";
    private static final String TAG = LowpanInterface.class.getSimpleName();
    private final ILowpanInterface mBinder;
    private final HashMap<Integer, ILowpanInterfaceListener> mListenerMap = new HashMap<>();
    private final Looper mLooper;

    public static abstract class Callback {
        public void onConnectedChanged(boolean value) {
        }

        public void onEnabledChanged(boolean value) {
        }

        public void onUpChanged(boolean value) {
        }

        public void onRoleChanged(String value) {
        }

        public void onStateChanged(String state) {
        }

        public void onLowpanIdentityChanged(LowpanIdentity value) {
        }

        public void onLinkNetworkAdded(IpPrefix prefix) {
        }

        public void onLinkNetworkRemoved(IpPrefix prefix) {
        }

        public void onLinkAddressAdded(LinkAddress address) {
        }

        public void onLinkAddressRemoved(LinkAddress address) {
        }
    }

    public LowpanInterface(Context context, ILowpanInterface service, Looper looper) {
        this.mBinder = service;
        this.mLooper = looper;
    }

    public ILowpanInterface getService() {
        return this.mBinder;
    }

    public void form(LowpanProvision provision) throws LowpanException {
        try {
            this.mBinder.form(provision);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void join(LowpanProvision provision) throws LowpanException {
        try {
            this.mBinder.join(provision);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void attach(LowpanProvision provision) throws LowpanException {
        try {
            this.mBinder.attach(provision);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void leave() throws LowpanException {
        try {
            this.mBinder.leave();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public LowpanCommissioningSession startCommissioningSession(LowpanBeaconInfo beaconInfo) throws LowpanException {
        try {
            this.mBinder.startCommissioningSession(beaconInfo);
            return new LowpanCommissioningSession(this.mBinder, beaconInfo, this.mLooper);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void reset() throws LowpanException {
        try {
            this.mBinder.reset();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public String getName() {
        try {
            return this.mBinder.getName();
        } catch (DeadObjectException e) {
            return "";
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public boolean isEnabled() {
        try {
            return this.mBinder.isEnabled();
        } catch (DeadObjectException e) {
            return false;
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public void setEnabled(boolean enabled) throws LowpanException {
        try {
            this.mBinder.setEnabled(enabled);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public boolean isUp() {
        try {
            return this.mBinder.isUp();
        } catch (DeadObjectException e) {
            return false;
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public boolean isConnected() {
        try {
            return this.mBinder.isConnected();
        } catch (DeadObjectException e) {
            return false;
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public boolean isCommissioned() {
        try {
            return this.mBinder.isCommissioned();
        } catch (DeadObjectException e) {
            return false;
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public String getState() {
        try {
            return this.mBinder.getState();
        } catch (DeadObjectException e) {
            return "fault";
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public String getPartitionId() {
        try {
            return this.mBinder.getPartitionId();
        } catch (DeadObjectException e) {
            return "";
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public LowpanIdentity getLowpanIdentity() {
        try {
            return this.mBinder.getLowpanIdentity();
        } catch (DeadObjectException e) {
            return new LowpanIdentity();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public String getRole() {
        try {
            return this.mBinder.getRole();
        } catch (DeadObjectException e) {
            return "detached";
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public LowpanCredential getLowpanCredential() {
        try {
            return this.mBinder.getLowpanCredential();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public String[] getSupportedNetworkTypes() throws LowpanException {
        try {
            return this.mBinder.getSupportedNetworkTypes();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public LowpanChannelInfo[] getSupportedChannels() throws LowpanException {
        try {
            return this.mBinder.getSupportedChannels();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void registerCallback(final Callback cb, final Handler handler) {
        ILowpanInterfaceListener.Stub listenerBinder = new ILowpanInterfaceListener.Stub() {
            /* class android.net.lowpan.LowpanInterface.AnonymousClass1 */
            private Handler mHandler;

            {
                Handler handler = handler;
                if (handler != null) {
                    this.mHandler = handler;
                } else if (LowpanInterface.this.mLooper != null) {
                    this.mHandler = new Handler(LowpanInterface.this.mLooper);
                } else {
                    this.mHandler = new Handler();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onEnabledChanged(boolean value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$LMuYw1xVwTG7Wbs4COpO6TLHuQ0 */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onEnabledChanged$0(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onConnectedChanged(boolean value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$Nidk8wBLJKibO6BNky_lJftmGs */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onConnectedChanged$1(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onUpChanged(boolean value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$a1rvbSIFSC6J5j7aKUf1ekbmIIA */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onUpChanged$2(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onRoleChanged(String value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$9yiRqHwJmFcLEKn1vk5rA75W0M */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onRoleChanged$3(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onStateChanged(String value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$5PUJBkKF3VANgkiEem5Oq8oyB6U */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onStateChanged$4(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            static /* synthetic */ void lambda$onLowpanIdentityChanged$5(Callback cb, LowpanIdentity value) {
                cb.onLowpanIdentityChanged(value);
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLowpanIdentityChanged(LowpanIdentity value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$rl_ENeH3C5Kvf22BOtLnzEhs5c */
                    private final /* synthetic */ LowpanIdentity f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onLowpanIdentityChanged$5(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLinkNetworkAdded(IpPrefix value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$oacwoIgJ4pmkBqVtGJfFzk7A35k */
                    private final /* synthetic */ IpPrefix f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onLinkNetworkAdded$6(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLinkNetworkRemoved(IpPrefix value) {
                this.mHandler.post(new Runnable(value) {
                    /* class android.net.lowpan.$$Lambda$LowpanInterface$1$cH3X25eT4t6pHlLvzBjlSOMs2vc */
                    private final /* synthetic */ IpPrefix f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LowpanInterface.AnonymousClass1.lambda$onLinkNetworkRemoved$7(LowpanInterface.Callback.this, this.f$1);
                    }
                });
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLinkAddressAdded(String value) {
                try {
                    this.mHandler.post(new Runnable(new LinkAddress(value)) {
                        /* class android.net.lowpan.$$Lambda$LowpanInterface$1$i2_6hzE6WEaUSOaaltxLebbf7E */
                        private final /* synthetic */ LinkAddress f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            LowpanInterface.AnonymousClass1.lambda$onLinkAddressAdded$8(LowpanInterface.Callback.this, this.f$1);
                        }
                    });
                } catch (IllegalArgumentException x) {
                    String str = LowpanInterface.TAG;
                    Log.e(str, "onLinkAddressAdded: Bad LinkAddress \"" + value + "\", " + x);
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLinkAddressRemoved(String value) {
                try {
                    this.mHandler.post(new Runnable(new LinkAddress(value)) {
                        /* class android.net.lowpan.$$Lambda$LowpanInterface$1$bAiJozbLxVR9_EMESl7KCJxLARA */
                        private final /* synthetic */ LinkAddress f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            LowpanInterface.AnonymousClass1.lambda$onLinkAddressRemoved$9(LowpanInterface.Callback.this, this.f$1);
                        }
                    });
                } catch (IllegalArgumentException x) {
                    String str = LowpanInterface.TAG;
                    Log.e(str, "onLinkAddressRemoved: Bad LinkAddress \"" + value + "\", " + x);
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onReceiveFromCommissioner(byte[] packet) {
            }
        };
        try {
            this.mBinder.addListener(listenerBinder);
            synchronized (this.mListenerMap) {
                this.mListenerMap.put(Integer.valueOf(System.identityHashCode(cb)), listenerBinder);
            }
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public void registerCallback(Callback cb) {
        registerCallback(cb, null);
    }

    public void unregisterCallback(Callback cb) {
        int hashCode = System.identityHashCode(cb);
        synchronized (this.mListenerMap) {
            ILowpanInterfaceListener listenerBinder = this.mListenerMap.get(Integer.valueOf(hashCode));
            if (listenerBinder != null) {
                this.mListenerMap.remove(Integer.valueOf(hashCode));
                try {
                    this.mBinder.removeListener(listenerBinder);
                } catch (DeadObjectException e) {
                } catch (RemoteException x) {
                    throw x.rethrowAsRuntimeException();
                }
            }
        }
    }

    public LowpanScanner createScanner() {
        return new LowpanScanner(this.mBinder);
    }

    public LinkAddress[] getLinkAddresses() throws LowpanException {
        try {
            String[] linkAddressStrings = this.mBinder.getLinkAddresses();
            LinkAddress[] ret = new LinkAddress[linkAddressStrings.length];
            int i = 0;
            int length = linkAddressStrings.length;
            int i2 = 0;
            while (i2 < length) {
                int i3 = i + 1;
                ret[i] = new LinkAddress(linkAddressStrings[i2]);
                i2++;
                i = i3;
            }
            return ret;
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public IpPrefix[] getLinkNetworks() throws LowpanException {
        try {
            return this.mBinder.getLinkNetworks();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void addOnMeshPrefix(IpPrefix prefix, int flags) throws LowpanException {
        try {
            this.mBinder.addOnMeshPrefix(prefix, flags);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void removeOnMeshPrefix(IpPrefix prefix) {
        try {
            this.mBinder.removeOnMeshPrefix(prefix);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            Log.e(TAG, x2.toString());
        }
    }

    public void addExternalRoute(IpPrefix prefix, int flags) throws LowpanException {
        try {
            this.mBinder.addExternalRoute(prefix, flags);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void removeExternalRoute(IpPrefix prefix) {
        try {
            this.mBinder.removeExternalRoute(prefix);
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            Log.e(TAG, x2.toString());
        }
    }
}
