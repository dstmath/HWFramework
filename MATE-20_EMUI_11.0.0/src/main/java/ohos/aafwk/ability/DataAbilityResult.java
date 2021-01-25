package ohos.aafwk.ability;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class DataAbilityResult implements Sequenceable {
    public static final Sequenceable.Producer<DataAbilityResult> PRODUCER = $$Lambda$DataAbilityResult$g3N6lBbzh8E9ZQPt9BUKTHQCd3A.INSTANCE;
    private Integer count;
    private Uri uri;

    public DataAbilityResult(Uri uri2) {
        this(uri2, null);
    }

    public DataAbilityResult(int i) {
        this(null, Integer.valueOf(i));
    }

    public DataAbilityResult(Uri uri2, Integer num) {
        this.uri = uri2;
        this.count = num;
    }

    private DataAbilityResult() {
    }

    static /* synthetic */ DataAbilityResult lambda$static$0(Parcel parcel) {
        DataAbilityResult dataAbilityResult = new DataAbilityResult();
        dataAbilityResult.unmarshalling(parcel);
        return dataAbilityResult;
    }

    public DataAbilityResult(Parcel parcel) {
        parcel.readSequenceable(this);
    }

    public Uri getUri() {
        return this.uri;
    }

    public Integer getCount() {
        return this.count;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DataAbilityResult(");
        if (this.uri != null) {
            sb.append("uri=");
            sb.append(this.uri);
            sb.append(" ");
        }
        if (this.count != null) {
            sb.append("count=");
            sb.append(this.count);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    public boolean marshalling(Parcel parcel) {
        if (this.uri != null) {
            if (!parcel.writeInt(1)) {
                return false;
            }
            parcel.writeSequenceable(this.uri);
        } else if (!parcel.writeInt(0)) {
            return false;
        }
        if (this.count != null) {
            if (!parcel.writeInt(1) || !parcel.writeInt(this.count.intValue())) {
                return false;
            }
        } else if (!parcel.writeInt(0)) {
            return false;
        }
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        Integer num = null;
        this.uri = parcel.readInt() != 0 ? Uri.readFromParcel(parcel) : null;
        if (parcel.readInt() != 0) {
            num = Integer.valueOf(parcel.readInt());
        }
        this.count = num;
        return true;
    }

    public static DataAbilityResult createFromParcel(Parcel parcel) {
        return new DataAbilityResult(parcel);
    }
}
