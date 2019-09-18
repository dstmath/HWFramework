package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

abstract class BaseParceledListSlice<T> implements Parcelable {
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static final int MAX_IPC_SIZE = 65536;
    /* access modifiers changed from: private */
    public static String TAG = "ParceledListSlice";
    private int mInlineCountLimit = Integer.MAX_VALUE;
    /* access modifiers changed from: private */
    public final List<T> mList;

    /* access modifiers changed from: protected */
    public abstract Parcelable.Creator<?> readParcelableCreator(Parcel parcel, ClassLoader classLoader);

    /* access modifiers changed from: protected */
    public abstract void writeElement(T t, Parcel parcel, int i);

    /* access modifiers changed from: protected */
    public abstract void writeParcelableCreator(T t, Parcel parcel);

    public BaseParceledListSlice(List<T> list) {
        this.mList = list;
    }

    BaseParceledListSlice(Parcel p, ClassLoader loader) {
        ClassLoader classLoader = loader;
        int N = p.readInt();
        this.mList = new ArrayList(N);
        if (DEBUG) {
            Log.d(TAG, "Retrieving " + N + " items");
        }
        if (N > 0) {
            Parcelable.Creator<?> creator = readParcelableCreator(p, loader);
            int i = 0;
            Class<?> listElementClass = null;
            int i2 = 0;
            while (i2 < N && p.readInt() != 0) {
                T parcelable = readCreator(creator, p, classLoader);
                if (listElementClass == null) {
                    listElementClass = parcelable.getClass();
                } else {
                    verifySameType(listElementClass, parcelable.getClass());
                }
                this.mList.add(parcelable);
                if (DEBUG) {
                    Log.d(TAG, "Read inline #" + i2 + ": " + this.mList.get(this.mList.size() - 1));
                }
                i2++;
            }
            Parcel parcel = p;
            if (i2 < N) {
                IBinder retriever = p.readStrongBinder();
                int i3 = i2;
                while (i3 < N) {
                    if (DEBUG) {
                        Log.d(TAG, "Reading more @" + i3 + " of " + N + ": retriever=" + retriever);
                    }
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(i3);
                    try {
                        retriever.transact(1, data, reply, i);
                        while (i3 < N && reply.readInt() != 0) {
                            T parcelable2 = readCreator(creator, reply, classLoader);
                            verifySameType(listElementClass, parcelable2.getClass());
                            this.mList.add(parcelable2);
                            if (DEBUG) {
                                Log.d(TAG, "Read extra #" + i3 + ": " + this.mList.get(this.mList.size() - 1));
                            }
                            i3++;
                        }
                        reply.recycle();
                        data.recycle();
                        i = 0;
                    } catch (RemoteException e) {
                        RemoteException remoteException = e;
                        Log.w(TAG, "Failure retrieving array; only received " + i3 + " of " + N, e);
                        return;
                    }
                }
            }
        }
    }

    private T readCreator(Parcelable.Creator<?> creator, Parcel p, ClassLoader loader) {
        if (creator instanceof Parcelable.ClassLoaderCreator) {
            return ((Parcelable.ClassLoaderCreator) creator).createFromParcel(p, loader);
        }
        return creator.createFromParcel(p);
    }

    /* access modifiers changed from: private */
    public static void verifySameType(Class<?> expected, Class<?> actual) {
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Can't unparcel type " + actual.getName() + " in list of type " + expected.getName());
        }
    }

    public List<T> getList() {
        return this.mList;
    }

    public void setInlineCountLimit(int maxCount) {
        this.mInlineCountLimit = maxCount;
    }

    public void writeToParcel(Parcel dest, int flags) {
        final int N = this.mList.size();
        final int callFlags = flags;
        dest.writeInt(N);
        if (DEBUG) {
            Log.d(TAG, "Writing " + N + " items");
        }
        if (N > 0) {
            final Class<?> listElementClass = this.mList.get(0).getClass();
            writeParcelableCreator(this.mList.get(0), dest);
            int i = 0;
            while (i < N && i < this.mInlineCountLimit && dest.dataSize() < 65536) {
                dest.writeInt(1);
                T parcelable = this.mList.get(i);
                verifySameType(listElementClass, parcelable.getClass());
                writeElement(parcelable, dest, callFlags);
                if (DEBUG) {
                    Log.d(TAG, "Wrote inline #" + i + ": " + this.mList.get(i));
                }
                i++;
            }
            if (i < N) {
                dest.writeInt(0);
                Binder retriever = new Binder() {
                    /* access modifiers changed from: protected */
                    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                        if (code != 1) {
                            return super.onTransact(code, data, reply, flags);
                        }
                        int i = data.readInt();
                        if (BaseParceledListSlice.DEBUG) {
                            String access$100 = BaseParceledListSlice.TAG;
                            Log.d(access$100, "Writing more @" + i + " of " + N);
                        }
                        while (i < N && reply.dataSize() < 65536) {
                            reply.writeInt(1);
                            T parcelable = BaseParceledListSlice.this.mList.get(i);
                            BaseParceledListSlice.verifySameType(listElementClass, parcelable.getClass());
                            BaseParceledListSlice.this.writeElement(parcelable, reply, callFlags);
                            if (BaseParceledListSlice.DEBUG) {
                                String access$1002 = BaseParceledListSlice.TAG;
                                Log.d(access$1002, "Wrote extra #" + i + ": " + BaseParceledListSlice.this.mList.get(i));
                            }
                            i++;
                        }
                        if (i < N) {
                            if (BaseParceledListSlice.DEBUG) {
                                String access$1003 = BaseParceledListSlice.TAG;
                                Log.d(access$1003, "Breaking @" + i + " of " + N);
                            }
                            reply.writeInt(0);
                        }
                        return true;
                    }
                };
                if (DEBUG) {
                    Log.d(TAG, "Breaking @" + i + " of " + N + ": retriever=" + retriever);
                }
                dest.writeStrongBinder(retriever);
            }
        }
    }
}
