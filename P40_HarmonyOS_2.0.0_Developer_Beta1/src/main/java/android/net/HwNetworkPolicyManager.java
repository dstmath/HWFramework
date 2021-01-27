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
    private Context mHwContext;
    public IBinder mNetworkPolicyManagerService = ServiceManager.getService("netpolicy");

    public HwNetworkPolicyManager(Context context, INetworkPolicyManager service) {
        super(context, service);
        this.mHwContext = context;
    }

    public static HwNetworkPolicyManager from(Context context) {
        return (HwNetworkPolicyManager) context.getSystemService("netpolicy");
    }

    public void setHwUidPolicy(int uid, int policy) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(uid);
            data.writeInt(policy);
            data.writeString(this.mHwContext.getOpPackageName());
            this.mNetworkPolicyManagerService.transact(200, data, null, 0);
            Slog.i(TAG, "setHwUidPolicy uid = " + uid + " policy = " + policy + " opPackageName =  " + this.mHwContext.getOpPackageName());
            data.recycle();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            throw th;
        }
    }

    public void addHwUidPolicy(int uid, int policy) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(uid);
            data.writeInt(policy);
            data.writeString(this.mHwContext.getOpPackageName());
            this.mNetworkPolicyManagerService.transact(TRANSCODE_ADD_HWUIDPOLICY, data, null, 0);
            Slog.i(TAG, "addHwUidPolicy uid = " + uid + " policy = " + policy + " opPackageName = " + this.mHwContext.getOpPackageName());
            data.recycle();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            throw th;
        }
    }

    public void removeHwUidPolicy(int uid, int policy) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(uid);
            data.writeInt(policy);
            data.writeString(this.mHwContext.getOpPackageName());
            this.mNetworkPolicyManagerService.transact(202, data, null, 0);
            Slog.i(TAG, "removeHwUidPolicy uid = " + uid + " policy = " + policy + " opPackageName = " + this.mHwContext.getOpPackageName());
            data.recycle();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            throw th;
        }
    }

    public int getHwUidPolicy(int uid) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInt(uid);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_GET_HWUIDPOLICY, data, reply, 0);
            int policy = reply.readInt();
            Slog.i(TAG, "getHwUidPolicy uid = " + uid + " policy = " + policy);
            data.recycle();
            reply.recycle();
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
            throw th;
        }
    }

    public int[] getHwUidsWithPolicy(int policy) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInt(policy);
            this.mNetworkPolicyManagerService.transact(204, data, reply, 0);
            int uidSize = reply.readInt();
            if (uidSize > 19999) {
                uidSize = 19999;
                Slog.i(TAG, "getHwUidPolicy the uid size is untrusted value");
            }
            int[] uids = new int[uidSize];
            reply.readIntArray(uids);
            Slog.i(TAG, "getHwUidPolicy uids = " + uids.toString() + " policy = " + policy);
            data.recycle();
            reply.recycle();
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
            throw th;
        }
    }

    public void forceUpdatePolicy(boolean isRoaming) {
        Parcel data = Parcel.obtain();
        try {
            data.writeInt(isRoaming ? 1 : 0);
            this.mNetworkPolicyManagerService.transact(TRANSCODE_FORCE_UPDATE_POLICY, data, null, 0);
            Slog.i(TAG, "forceUpdatePolicy isRoaming = " + isRoaming);
            data.recycle();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            throw th;
        }
    }
}
