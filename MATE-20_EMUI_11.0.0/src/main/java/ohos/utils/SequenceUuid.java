package ohos.utils;

import java.util.UUID;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Sequenceable;

public class SequenceUuid implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218119424, "SequenceUuid");
    public static final Sequenceable.Producer<SequenceUuid> PRODUCER = $$Lambda$SequenceUuid$RUgwL7SGW4RW44bly5q820MmLk0.INSTANCE;
    private UUID sequenceUuid;

    static /* synthetic */ SequenceUuid lambda$static$0(Parcel parcel) {
        SequenceUuid sequenceUuid2 = new SequenceUuid();
        sequenceUuid2.unmarshalling(parcel);
        return sequenceUuid2;
    }

    public SequenceUuid() {
    }

    public SequenceUuid(UUID uuid) {
        this.sequenceUuid = uuid;
    }

    public static SequenceUuid uuidFromString(String str) {
        return new SequenceUuid(UUID.fromString(str));
    }

    public UUID getUuid() {
        return this.sequenceUuid;
    }

    public String toString() {
        return this.sequenceUuid.toString();
    }

    public int hashCode() {
        return this.sequenceUuid.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof SequenceUuid) {
            return this.sequenceUuid.equals(((SequenceUuid) obj).sequenceUuid);
        }
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeLong(this.sequenceUuid.getMostSignificantBits())) {
            HiLog.error(LABEL, "sequenceUuid write MostSignificantBits failed!", new Object[0]);
            return false;
        } else if (parcel.writeLong(this.sequenceUuid.getLeastSignificantBits())) {
            return true;
        } else {
            HiLog.error(LABEL, "sequenceUuid write LeastSignificantBits failed!", new Object[0]);
            return false;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.sequenceUuid = new UUID(parcel.readLong(), parcel.readLong());
        return true;
    }
}
