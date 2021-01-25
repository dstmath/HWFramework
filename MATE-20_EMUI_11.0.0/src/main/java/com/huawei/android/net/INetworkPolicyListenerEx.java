package com.huawei.android.net;

import android.net.INetworkPolicyListener;
import android.os.RemoteException;

public class INetworkPolicyListenerEx {
    private INetworkPolicyListener mListener = new INetworkPolicyListener.Stub() {
        /* class com.huawei.android.net.INetworkPolicyListenerEx.AnonymousClass1 */

        public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
            INetworkPolicyListenerEx.this.onUidRulesChanged(uid, uidRules);
        }

        public void onMeteredIfacesChanged(String[] meteredIfaces) throws RemoteException {
            INetworkPolicyListenerEx.this.onMeteredIfacesChanged(meteredIfaces);
        }

        public void onRestrictBackgroundChanged(boolean restrictBackground) throws RemoteException {
            INetworkPolicyListenerEx.this.onRestrictBackgroundChanged(restrictBackground);
        }

        public void onUidPoliciesChanged(int uid, int uidPolicies) throws RemoteException {
            INetworkPolicyListenerEx.this.onUidPoliciesChanged(uid, uidPolicies);
        }

        public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) throws RemoteException {
        }
    };

    public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
    }

    public void onMeteredIfacesChanged(String[] meteredIfaces) throws RemoteException {
    }

    public void onRestrictBackgroundChanged(boolean restrictBackground) throws RemoteException {
    }

    public void onUidPoliciesChanged(int uid, int uidPolicies) throws RemoteException {
    }

    public INetworkPolicyListener getListener() {
        return this.mListener;
    }
}
