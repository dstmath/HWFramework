package ohos.abilityshell.utils;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Sequenceable;

/* access modifiers changed from: package-private */
public final class SequenceableWrapper implements Parcelable {
    public static final Parcelable.Creator<SequenceableWrapper> CREATOR = new Parcelable.Creator<SequenceableWrapper>() {
        /* class ohos.abilityshell.utils.SequenceableWrapper.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SequenceableWrapper createFromParcel(Parcel parcel) {
            return new SequenceableWrapper(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SequenceableWrapper[] newArray(int i) {
            return new SequenceableWrapper[i];
        }
    };
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static final int PARCEL_FLAG_INT_NON_NULL_OBJ = 0;
    private static final int PARCEL_FLAG_INT_NULL_OBJ = -1;
    private final Object LOCK;
    private volatile boolean decoded;
    private byte[] parcelBytes;
    private Sequenceable wrappedSequenceable;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    SequenceableWrapper(Sequenceable sequenceable) {
        this.LOCK = new Object();
        this.decoded = true;
        this.wrappedSequenceable = sequenceable;
        encode();
    }

    private SequenceableWrapper(Parcel parcel) {
        this.LOCK = new Object();
        if (parcel.readInt() == -1) {
            this.decoded = true;
            return;
        }
        this.decoded = false;
        this.parcelBytes = parcel.createByteArray();
    }

    static SequenceableWrapper[] wrapArray(Sequenceable[] sequenceableArr) {
        if (sequenceableArr == null) {
            return new SequenceableWrapper[0];
        }
        return (SequenceableWrapper[]) Arrays.stream(sequenceableArr).map($$Lambda$LQ1eK0JnysW_ye23KkwgF9m_1Ko.INSTANCE).toArray($$Lambda$SequenceableWrapper$kcMlumE__qafcSZzu33btPY1gUk.INSTANCE);
    }

    static /* synthetic */ SequenceableWrapper[] lambda$wrapArray$0(int i) {
        return new SequenceableWrapper[i];
    }

    static Sequenceable[] unwrapArray(Parcelable[] parcelableArr, ClassLoader classLoader) {
        if (parcelableArr == null) {
            return new Sequenceable[0];
        }
        return (Sequenceable[]) Arrays.stream(parcelableArr).map(new Function(classLoader) {
            /* class ohos.abilityshell.utils.$$Lambda$SequenceableWrapper$CRNganTSTenElZDU5SWUFnaI_pE */
            private final /* synthetic */ ClassLoader f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return SequenceableWrapper.lambda$unwrapArray$1(this.f$0, (Parcelable) obj);
            }
        }).toArray($$Lambda$SequenceableWrapper$YKUuXcz1cutxprQrcFmdWLgq8gk.INSTANCE);
    }

    static /* synthetic */ Sequenceable lambda$unwrapArray$1(ClassLoader classLoader, Parcelable parcelable) {
        if (parcelable instanceof SequenceableWrapper) {
            return ((SequenceableWrapper) parcelable).getWrappedSequenceable(classLoader);
        }
        AppLog.e(LABEL, "not supported type %{public}s", parcelable.getClass().getName());
        throw new IllegalArgumentException("not supported type " + parcelable.getClass().getName());
    }

    static /* synthetic */ Sequenceable[] lambda$unwrapArray$2(int i) {
        return new Sequenceable[i];
    }

    /* access modifiers changed from: package-private */
    public Sequenceable getWrappedSequenceable(ClassLoader classLoader) {
        decode(classLoader);
        return this.wrappedSequenceable;
    }

    private void encode() {
        if (this.wrappedSequenceable != null) {
            ohos.utils.Parcel create = ohos.utils.Parcel.create();
            try {
                if (doMarshalling(create)) {
                    this.parcelBytes = create.getBytes();
                } else {
                    AppLog.e(LABEL, "write to parcel return false", new Object[0]);
                    throw new IllegalStateException("write to parcel return false");
                }
            } finally {
                create.reclaim();
            }
        }
    }

    private void decode(ClassLoader classLoader) {
        if (!this.decoded) {
            synchronized (this.LOCK) {
                if (!this.decoded) {
                    this.decoded = true;
                    if (this.parcelBytes != null) {
                        ohos.utils.Parcel create = ohos.utils.Parcel.create();
                        try {
                            if (create.writeBytes(this.parcelBytes)) {
                                this.wrappedSequenceable = doUnmarshalling(create, classLoader);
                            } else {
                                AppLog.e(LABEL, "write to parcel return false", new Object[0]);
                                throw new IllegalStateException("write to parcel return false");
                            }
                        } finally {
                            create.reclaim();
                        }
                    }
                }
            }
        }
    }

    private boolean doMarshalling(ohos.utils.Parcel parcel) {
        if (this.wrappedSequenceable == null) {
            return parcel.writeInt(-1);
        }
        if (parcel.writeInt(0) && parcel.writeString(this.wrappedSequenceable.getClass().getName())) {
            return this.wrappedSequenceable.marshalling(parcel);
        }
        return false;
    }

    private Sequenceable doUnmarshalling(ohos.utils.Parcel parcel, ClassLoader classLoader) {
        if (parcel.readInt() == -1) {
            return null;
        }
        return createSequenceable(parcel, parcel.readString(), classLoader);
    }

    private Sequenceable createSequenceable(ohos.utils.Parcel parcel, String str, ClassLoader classLoader) {
        return (Sequenceable) createSequenceableCreator(str, classLoader).createFromParcel(parcel);
    }

    private Sequenceable.Producer<?> createSequenceableCreator(String str, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        try {
            Class<?> cls = Class.forName(str, false, classLoader);
            if (Sequenceable.class.isAssignableFrom(cls)) {
                Field field = cls.getField("PRODUCER");
                if ((field.getModifiers() & 8) == 0) {
                    throw new IllegalStateException("fail to create parcelable creator due to PRODUCER is not static");
                } else if (Sequenceable.Producer.class.isAssignableFrom(field.getType())) {
                    Sequenceable.Producer<?> producer = (Sequenceable.Producer) field.get(null);
                    if (producer != null) {
                        return producer;
                    }
                    throw new IllegalStateException("PRODUCER is null for class " + str);
                } else {
                    throw new IllegalStateException("fail to create parcelable creator due to PRODUCER type is error");
                }
            } else {
                throw new IllegalStateException("fail to create parcelable creator due to this class is not parcelable");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassLoader=" + classLoader + " maybe not correct for class " + str, e);
        } catch (NoSuchFieldException e2) {
            throw new IllegalStateException("No PRODUCER found for class " + str, e2);
        } catch (IllegalAccessException e3) {
            throw new IllegalStateException("PRODUCER maybe not accessible for class " + str, e3);
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        byte[] bArr = this.parcelBytes;
        if (bArr == null) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(bArr.length);
        }
        parcel.writeByteArray(this.parcelBytes);
    }
}
