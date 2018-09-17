package android.net;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;

public class HwNetworkPolicyManager extends NetworkPolicyManager {
    public static final int POLICY_HW_BASE = 1;
    public static final int POLICY_HW_DEFAULT = 0;
    public static final int POLICY_HW_RESTRICT_MOBILE = 1;
    public static final int POLICY_HW_RESTRICT_ROAMING_MOBILE = 4;
    public static final int POLICY_HW_RESTRICT_WIFI = 2;
    public static final String TAG = "HwNetworkPolicyManager";
    public static final int TRANSCODE_ADD_HWUIDPOLICY = 201;
    public static final int TRANSCODE_FORCE_UPDATE_POLICY = 205;
    public static final int TRANSCODE_GET_HWUIDPOLICY = 203;
    public static final int TRANSCODE_GET_UIDS_WITHPOLICY = 204;
    public static final int TRANSCODE_REMOVE_HWUIDPOLICY = 202;
    public static final int TRANSCODE_SET_HWUIDPOLICY = 200;
    public IBinder mNetworkPolicyManagerService;

    public HwNetworkPolicyManager(Context context, INetworkPolicyManager service) {
        super(context, service);
        this.mNetworkPolicyManagerService = ServiceManager.getService("netpolicy");
    }

    public static HwNetworkPolicyManager from(Context context) {
        return (HwNetworkPolicyManager) context.getSystemService("netpolicy");
    }

    public void setHwUidPolicy(int uid, int policy) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(uid);
            data.writeInt(policy);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_SET_HWUIDPOLICY, data, null, POLICY_HW_DEFAULT);
            Slog.i(TAG, "setHwUidPolicy uid = " + uid + " policy = " + policy);
            if (data != null) {
                data.recycle();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
        }
    }

    public void addHwUidPolicy(int uid, int policy) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(uid);
            data.writeInt(policy);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_ADD_HWUIDPOLICY, data, null, POLICY_HW_DEFAULT);
            Slog.i(TAG, "addHwUidPolicy uid = " + uid + " policy = " + policy);
            if (data != null) {
                data.recycle();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
        }
    }

    public void removeHwUidPolicy(int uid, int policy) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(uid);
            data.writeInt(policy);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_REMOVE_HWUIDPOLICY, data, null, POLICY_HW_DEFAULT);
            Slog.i(TAG, "removeHwUidPolicy uid = " + uid + " policy = " + policy);
            if (data != null) {
                data.recycle();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
        }
    }

    public int getHwUidPolicy(int uid) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInt(uid);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_GET_HWUIDPOLICY, data, reply, POLICY_HW_DEFAULT);
            int policy = reply.readInt();
            Slog.i(TAG, "getHwUidPolicy uid = " + uid + " policy = " + policy);
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            return policy;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        }
    }

    public int[] getHwUidsWithPolicy(int policy) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInt(policy);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_GET_UIDS_WITHPOLICY, data, reply, POLICY_HW_DEFAULT);
            int[] uids = new int[reply.readInt()];
            reply.readIntArray(uids);
            Slog.i(TAG, "getHwUidPolicy uids = " + uids.toString() + " policy = " + policy);
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        }
    }

    public void forceUpdatePolicy(boolean isRoaming) {
        int i = POLICY_HW_DEFAULT;
        Parcel data = Parcel.obtain();
        if (isRoaming) {
            i = POLICY_HW_RESTRICT_MOBILE;
        }
        try {
            data.writeInt(i);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_FORCE_UPDATE_POLICY, data, null, POLICY_HW_DEFAULT);
            Slog.i(TAG, "forceUpdatePolicy isRoaming = " + isRoaming);
            if (data != null) {
                data.recycle();
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
        }
    }
}
