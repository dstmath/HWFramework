package android.hsm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;

public class MediaTransactWrapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaTransactWrapper";
    private static SoftReference<IBinder> mBinder = null;

    public static void musicPlaying(int uid, int pid) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                data.writeInt(0);
                data.writeInt(uid);
                data.writeInt(pid);
                b.transact(102, data, reply, 0);
                reply.readException();
                int readInt = reply.readInt();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "musicPlaying transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPlaying transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        recycleParcel(data);
        recycleParcel(reply);
    }

    public static void musicPausedOrStopped(int uid, int pid) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                data.writeInt(1);
                data.writeInt(uid);
                data.writeInt(pid);
                b.transact(102, data, reply, 0);
                reply.readException();
                int readInt = reply.readInt();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        recycleParcel(data);
        recycleParcel(reply);
    }

    public static Set<Integer> playingMusicUidSet() {
        Set<Integer> result = new HashSet<>();
        String strUids = playingMusicUidStr();
        if (strUids != null && !strUids.isEmpty()) {
            String[] ids = strUids.split("\\|");
            if (ids != null) {
                for (String valueOf : ids) {
                    result.add(Integer.valueOf(valueOf));
                }
            }
        }
        return result;
    }

    private static String playingMusicUidStr() {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                b.transact(103, data, reply, 0);
                reply.readException();
                String result = reply.readString();
                recycleParcel(data);
                recycleParcel(reply);
                return result;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        recycleParcel(data);
        recycleParcel(reply);
        return null;
    }

    private static synchronized IBinder getBinder() {
        IBinder iBinder;
        synchronized (MediaTransactWrapper.class) {
            if (mBinder == null || mBinder.get() == null) {
                mBinder = new SoftReference<>(ServiceManager.getService(HsmTransactExt.SERVICE_NAME));
            }
            iBinder = mBinder.get();
        }
        return iBinder;
    }

    private static void recycleParcel(Parcel p) {
        if (p != null) {
            p.recycle();
        }
    }
}
