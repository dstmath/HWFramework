package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RcsEventQueryResultDescriptor implements Parcelable {
    public static final Parcelable.Creator<RcsEventQueryResultDescriptor> CREATOR = new Parcelable.Creator<RcsEventQueryResultDescriptor>() {
        /* class android.telephony.ims.RcsEventQueryResultDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsEventQueryResultDescriptor createFromParcel(Parcel in) {
            return new RcsEventQueryResultDescriptor(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsEventQueryResultDescriptor[] newArray(int size) {
            return new RcsEventQueryResultDescriptor[size];
        }
    };
    private final RcsQueryContinuationToken mContinuationToken;
    private final List<RcsEventDescriptor> mEvents;

    public RcsEventQueryResultDescriptor(RcsQueryContinuationToken continuationToken, List<RcsEventDescriptor> events) {
        this.mContinuationToken = continuationToken;
        this.mEvents = events;
    }

    /* access modifiers changed from: protected */
    public RcsEventQueryResult getRcsEventQueryResult(RcsControllerCall rcsControllerCall) {
        return new RcsEventQueryResult(this.mContinuationToken, (List) this.mEvents.stream().map(new Function() {
            /* class android.telephony.ims.$$Lambda$RcsEventQueryResultDescriptor$0eoTyoA0JNoBx53J3zXvi1fQcnA */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return RcsEventQueryResultDescriptor.lambda$getRcsEventQueryResult$0(RcsControllerCall.this, (RcsEventDescriptor) obj);
            }
        }).collect(Collectors.toList()));
    }

    protected RcsEventQueryResultDescriptor(Parcel in) {
        this.mContinuationToken = (RcsQueryContinuationToken) in.readParcelable(RcsQueryContinuationToken.class.getClassLoader());
        this.mEvents = new LinkedList();
        in.readList(this.mEvents, null);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mContinuationToken, flags);
        dest.writeList(this.mEvents);
    }
}
