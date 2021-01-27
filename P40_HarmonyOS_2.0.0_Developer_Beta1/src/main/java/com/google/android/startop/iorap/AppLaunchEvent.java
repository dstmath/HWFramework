package com.google.android.startop.iorap;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public abstract class AppLaunchEvent implements Parcelable {
    public static Parcelable.Creator<AppLaunchEvent> CREATOR = new Parcelable.Creator<AppLaunchEvent>() {
        /* class com.google.android.startop.iorap.AppLaunchEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppLaunchEvent createFromParcel(Parcel source) {
            int typeIndex = source.readInt();
            Class<?> kls = AppLaunchEvent.getClassFromTypeIndex(typeIndex);
            if (kls != null) {
                try {
                    return (AppLaunchEvent) kls.getConstructor(Parcel.class).newInstance(source);
                } catch (InstantiationException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                } catch (InvocationTargetException e3) {
                    throw new AssertionError(e3);
                } catch (NoSuchMethodException e4) {
                    throw new AssertionError(e4);
                }
            } else {
                throw new IllegalArgumentException("Invalid type index: " + typeIndex);
            }
        }

        @Override // android.os.Parcelable.Creator
        public AppLaunchEvent[] newArray(int size) {
            return new AppLaunchEvent[0];
        }
    };
    private static Class<?>[] sTypes = {IntentStarted.class, IntentFailed.class, ActivityLaunched.class, ActivityLaunchFinished.class, ActivityLaunchCancelled.class};
    public final long sequenceId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SequenceId {
    }

    protected AppLaunchEvent(long sequenceId2) {
        this.sequenceId = sequenceId2;
    }

    @Override // java.lang.Object
    public boolean equals(Object other) {
        if (other instanceof AppLaunchEvent) {
            return equals((AppLaunchEvent) other);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean equals(AppLaunchEvent other) {
        return this.sequenceId == other.sequenceId;
    }

    @Override // java.lang.Object
    public String toString() {
        return getClass().getSimpleName() + "{sequenceId=" + Long.toString(this.sequenceId) + toStringBody() + "}";
    }

    /* access modifiers changed from: protected */
    public String toStringBody() {
        return "";
    }

    public static final class IntentStarted extends AppLaunchEvent {
        public final Intent intent;

        public IntentStarted(long sequenceId, Intent intent2) {
            super(sequenceId);
            this.intent = intent2;
            Objects.requireNonNull(intent2, "intent");
        }

        @Override // com.google.android.startop.iorap.AppLaunchEvent, java.lang.Object
        public boolean equals(Object other) {
            if (!(other instanceof IntentStarted) || !this.intent.equals(((IntentStarted) other).intent) || !AppLaunchEvent.super.equals(other)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent
        public String toStringBody() {
            return ", intent=" + this.intent.toString();
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent
        public void writeToParcelImpl(Parcel p, int flags) {
            AppLaunchEvent.super.writeToParcelImpl(p, flags);
            IntentProtoParcelable.write(p, this.intent, flags);
        }

        IntentStarted(Parcel p) {
            super(p);
            this.intent = IntentProtoParcelable.create(p);
        }
    }

    public static final class IntentFailed extends AppLaunchEvent {
        public IntentFailed(long sequenceId) {
            super(sequenceId);
        }

        @Override // com.google.android.startop.iorap.AppLaunchEvent, java.lang.Object
        public boolean equals(Object other) {
            if (other instanceof IntentFailed) {
                return AppLaunchEvent.super.equals(other);
            }
            return false;
        }

        IntentFailed(Parcel p) {
            super(p);
        }
    }

    public static abstract class BaseWithActivityRecordData extends AppLaunchEvent {
        public final byte[] activityRecordSnapshot;

        protected BaseWithActivityRecordData(long sequenceId, byte[] snapshot) {
            super(sequenceId);
            this.activityRecordSnapshot = snapshot;
            Objects.requireNonNull(snapshot, "snapshot");
        }

        @Override // com.google.android.startop.iorap.AppLaunchEvent, java.lang.Object
        public boolean equals(Object other) {
            if (!(other instanceof BaseWithActivityRecordData) || !this.activityRecordSnapshot.equals(((BaseWithActivityRecordData) other).activityRecordSnapshot) || !AppLaunchEvent.super.equals(other)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent
        public String toStringBody() {
            return ", " + this.activityRecordSnapshot.toString();
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent
        public void writeToParcelImpl(Parcel p, int flags) {
            AppLaunchEvent.super.writeToParcelImpl(p, flags);
            ActivityRecordProtoParcelable.write(p, this.activityRecordSnapshot, flags);
        }

        BaseWithActivityRecordData(Parcel p) {
            super(p);
            this.activityRecordSnapshot = ActivityRecordProtoParcelable.create(p);
        }
    }

    public static final class ActivityLaunched extends BaseWithActivityRecordData {
        public final int temperature;

        public ActivityLaunched(long sequenceId, byte[] snapshot, int temperature2) {
            super(sequenceId, snapshot);
            this.temperature = temperature2;
        }

        @Override // com.google.android.startop.iorap.AppLaunchEvent.BaseWithActivityRecordData, com.google.android.startop.iorap.AppLaunchEvent, java.lang.Object
        public boolean equals(Object other) {
            if (!(other instanceof ActivityLaunched) || this.temperature != ((ActivityLaunched) other).temperature || !super.equals(other)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent.BaseWithActivityRecordData, com.google.android.startop.iorap.AppLaunchEvent
        public String toStringBody() {
            return ", temperature=" + Integer.toString(this.temperature);
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent.BaseWithActivityRecordData, com.google.android.startop.iorap.AppLaunchEvent
        public void writeToParcelImpl(Parcel p, int flags) {
            super.writeToParcelImpl(p, flags);
            p.writeInt(this.temperature);
        }

        ActivityLaunched(Parcel p) {
            super(p);
            this.temperature = p.readInt();
        }
    }

    public static final class ActivityLaunchFinished extends BaseWithActivityRecordData {
        public ActivityLaunchFinished(long sequenceId, byte[] snapshot) {
            super(sequenceId, snapshot);
        }

        @Override // com.google.android.startop.iorap.AppLaunchEvent.BaseWithActivityRecordData, com.google.android.startop.iorap.AppLaunchEvent, java.lang.Object
        public boolean equals(Object other) {
            if (other instanceof ActivityLaunched) {
                return super.equals(other);
            }
            return false;
        }
    }

    public static class ActivityLaunchCancelled extends AppLaunchEvent {
        public final byte[] activityRecordSnapshot;

        public ActivityLaunchCancelled(long sequenceId, byte[] snapshot) {
            super(sequenceId);
            this.activityRecordSnapshot = snapshot;
        }

        @Override // com.google.android.startop.iorap.AppLaunchEvent, java.lang.Object
        public boolean equals(Object other) {
            if (!(other instanceof ActivityLaunchCancelled) || !Objects.equals(this.activityRecordSnapshot, ((ActivityLaunchCancelled) other).activityRecordSnapshot) || !AppLaunchEvent.super.equals(other)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent
        public String toStringBody() {
            return ", " + this.activityRecordSnapshot.toString();
        }

        /* access modifiers changed from: protected */
        @Override // com.google.android.startop.iorap.AppLaunchEvent
        public void writeToParcelImpl(Parcel p, int flags) {
            AppLaunchEvent.super.writeToParcelImpl(p, flags);
            if (this.activityRecordSnapshot != null) {
                p.writeBoolean(true);
                ActivityRecordProtoParcelable.write(p, this.activityRecordSnapshot, flags);
                return;
            }
            p.writeBoolean(false);
        }

        ActivityLaunchCancelled(Parcel p) {
            super(p);
            if (p.readBoolean()) {
                this.activityRecordSnapshot = ActivityRecordProtoParcelable.create(p);
            } else {
                this.activityRecordSnapshot = null;
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(getTypeIndex());
        writeToParcelImpl(p, flags);
    }

    /* access modifiers changed from: protected */
    public void writeToParcelImpl(Parcel p, int flags) {
        p.writeLong(this.sequenceId);
    }

    protected AppLaunchEvent(Parcel p) {
        this.sequenceId = p.readLong();
    }

    private int getTypeIndex() {
        int i = 0;
        while (true) {
            Class<?>[] clsArr = sTypes;
            if (i >= clsArr.length) {
                throw new AssertionError("sTypes did not include this type: " + getClass());
            } else if (clsArr[i].equals(getClass())) {
                return i;
            } else {
                i++;
            }
        }
    }

    /* access modifiers changed from: private */
    public static Class<?> getClassFromTypeIndex(int typeIndex) {
        if (typeIndex < 0) {
            return null;
        }
        Class<?>[] clsArr = sTypes;
        if (typeIndex < clsArr.length) {
            return clsArr[typeIndex];
        }
        return null;
    }

    public static class ActivityRecordProtoParcelable {
        public static void write(Parcel p, byte[] activityRecordSnapshot, int flags) {
            p.writeByteArray(activityRecordSnapshot);
        }

        public static byte[] create(Parcel p) {
            return p.createByteArray();
        }
    }

    public static class IntentProtoParcelable {
        private static final int INTENT_PROTO_CHUNK_SIZE = 1024;

        public static void write(Parcel p, Intent intent, int flags) {
            ProtoOutputStream protoOutputStream = new ProtoOutputStream((int) INTENT_PROTO_CHUNK_SIZE);
            intent.writeToProto(protoOutputStream);
            p.writeByteArray(protoOutputStream.getBytes());
        }

        public static Intent create(Parcel p) {
            p.createByteArray();
            return new Intent("<cannot deserialize IntentProto>");
        }
    }
}
