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
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                data.writeInt(0);
                data.writeInt(uid);
                data.writeInt(pid);
                b.transact(102, data, reply, 0);
                reply.readException();
                if (1 == reply.readInt()) {
                }
            }
            recycleParcel(data);
            recycleParcel(reply);
        } catch (RemoteException e) {
            Log.e(TAG, "musicPlaying transact catch remote exception: " + e.toString());
            recycleParcel(null);
            recycleParcel(null);
        } catch (Exception e2) {
            Log.e(TAG, "musicPlaying transact catch exception: " + e2.toString());
            e2.printStackTrace();
            recycleParcel(null);
            recycleParcel(null);
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
    }

    public static void musicPausedOrStopped(int uid, int pid) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                data.writeInt(1);
                data.writeInt(uid);
                data.writeInt(pid);
                b.transact(102, data, reply, 0);
                reply.readException();
                if (1 == reply.readInt()) {
                }
            }
            recycleParcel(data);
            recycleParcel(reply);
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception: " + e.toString());
            recycleParcel(null);
            recycleParcel(null);
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception: " + e2.toString());
            e2.printStackTrace();
            recycleParcel(null);
            recycleParcel(null);
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
    }

    public static Set<Integer> playingMusicUidSet() {
        Set<Integer> result = new HashSet();
        String strUids = playingMusicUidStr();
        if (!(strUids == null || (strUids.isEmpty() ^ 1) == 0)) {
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
                return result;
            }
            recycleParcel(data);
            recycleParcel(reply);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } finally {
            recycleParcel(data);
            recycleParcel(reply);
        }
    }

    private static synchronized IBinder getBinder() {
        IBinder iBinder;
        synchronized (MediaTransactWrapper.class) {
            if (mBinder == null || mBinder.get() == null) {
                mBinder = new SoftReference(ServiceManager.getService(HsmTransactExt.SERVICE_NAME));
            }
            iBinder = (IBinder) mBinder.get();
        }
        return iBinder;
    }

    private static void recycleParcel(Parcel p) {
        if (p != null) {
            p.recycle();
        }
    }
}
