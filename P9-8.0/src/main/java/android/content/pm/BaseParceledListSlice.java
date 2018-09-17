package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

abstract class BaseParceledListSlice<T> implements Parcelable {
    private static boolean DEBUG = false;
    private static final int MAX_IPC_SIZE = 65536;
    private static String TAG = "ParceledListSlice";
    private int mInlineCountLimit = Integer.MAX_VALUE;
    private final List<T> mList;

    protected abstract Creator<?> readParcelableCreator(Parcel parcel, ClassLoader classLoader);

    protected abstract void writeElement(T t, Parcel parcel, int i);

    protected abstract void writeParcelableCreator(T t, Parcel parcel);

    public BaseParceledListSlice(List<T> list) {
        this.mList = list;
    }

    BaseParceledListSlice(Parcel p, ClassLoader loader) {
        int N = p.readInt();
        this.mList = new ArrayList(N);
        if (DEBUG) {
            Log.d(TAG, "Retrieving " + N + " items");
        }
        if (N > 0) {
            T parcelable;
            Creator<?> creator = readParcelableCreator(p, loader);
            Class listElementClass = null;
            int i = 0;
            while (i < N && p.readInt() != 0) {
                parcelable = readCreator(creator, p, loader);
                if (listElementClass == null) {
                    listElementClass = parcelable.getClass();
                } else {
                    verifySameType(listElementClass, parcelable.getClass());
                }
                this.mList.add(parcelable);
                if (DEBUG) {
                    Log.d(TAG, "Read inline #" + i + ": " + this.mList.get(this.mList.size() - 1));
                }
                i++;
            }
            if (i < N) {
                IBinder retriever = p.readStrongBinder();
                while (i < N) {
                    if (DEBUG) {
                        Log.d(TAG, "Reading more @" + i + " of " + N + ": retriever=" + retriever);
                    }
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(i);
                    try {
                        retriever.transact(1, data, reply, 0);
                        while (i < N && reply.readInt() != 0) {
                            parcelable = reply.readCreator(creator, loader);
                            verifySameType(listElementClass, parcelable.getClass());
                            this.mList.add(parcelable);
                            if (DEBUG) {
                                Log.d(TAG, "Read extra #" + i + ": " + this.mList.get(this.mList.size() - 1));
                            }
                            i++;
                        }
                        reply.recycle();
                        data.recycle();
                    } catch (RemoteException e) {
                        Log.w(TAG, "Failure retrieving array; only received " + i + " of " + N, e);
                        return;
                    }
                }
            }
        }
    }

    private T readCreator(Creator<?> creator, Parcel p, ClassLoader loader) {
        if (creator instanceof ClassLoaderCreator) {
            return ((ClassLoaderCreator) creator).createFromParcel(p, loader);
        }
        return creator.createFromParcel(p);
    }

    private static void verifySameType(Class<?> expected, Class<?> actual) {
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

    public void writeToParcel(Parcel dest, final int flags) {
        final int N = this.mList.size();
        int callFlags = flags;
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
                writeElement(parcelable, dest, flags);
                if (DEBUG) {
                    Log.d(TAG, "Wrote inline #" + i + ": " + this.mList.get(i));
                }
                i++;
            }
            if (i < N) {
                dest.writeInt(0);
                Binder retriever = new Binder() {
                    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                        if (code != 1) {
                            return super.onTransact(code, data, reply, flags);
                        }
                        int i = data.readInt();
                        if (BaseParceledListSlice.DEBUG) {
                            Log.d(BaseParceledListSlice.TAG, "Writing more @" + i + " of " + N);
                        }
                        while (i < N && reply.dataSize() < 65536) {
                            reply.writeInt(1);
                            T parcelable = BaseParceledListSlice.this.mList.get(i);
                            BaseParceledListSlice.verifySameType(listElementClass, parcelable.getClass());
                            BaseParceledListSlice.this.writeElement(parcelable, reply, flags);
                            if (BaseParceledListSlice.DEBUG) {
                                Log.d(BaseParceledListSlice.TAG, "Wrote extra #" + i + ": " + BaseParceledListSlice.this.mList.get(i));
                            }
                            i++;
                        }
                        if (i < N) {
                            if (BaseParceledListSlice.DEBUG) {
                                Log.d(BaseParceledListSlice.TAG, "Breaking @" + i + " of " + N);
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
