package ohos.dcall;

import java.util.ArrayList;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class CallHandler implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) DistributedCallUtils.LOG_ID_DCALL, "CallHandler");
    private ElementName mElementName;
    private String mId;
    private int mUserId;

    public CallHandler() {
        this(null, null);
    }

    public CallHandler(ElementName elementName, String str) {
        this(elementName, str, 0);
    }

    public CallHandler(ElementName elementName, String str, int i) {
        this.mElementName = elementName;
        this.mId = str;
        this.mUserId = i;
    }

    public ElementName getElementName() {
        return this.mElementName;
    }

    public String getId() {
        return this.mId;
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(wrap: ohos.bundle.ElementName : 0x0005: IGET  (r1v1 ohos.bundle.ElementName) = (r1v0 'this' ohos.dcall.CallHandler A[IMMUTABLE_TYPE, THIS]) ohos.dcall.CallHandler.mElementName ohos.bundle.ElementName)] */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mElementName);
        return sb.toString();
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        ElementName elementName = this.mElementName;
        if (elementName != null) {
            elementName.marshalling(parcel);
        } else {
            new ElementName().marshalling(parcel);
        }
        parcel.writeString(this.mId);
        parcel.writeInt(this.mUserId);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.mElementName = new ElementName();
        this.mElementName.unmarshalling(parcel);
        this.mId = parcel.readString();
        this.mUserId = parcel.readInt();
        return true;
    }

    static List<CallHandler> createListFromParcel(MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (readInt <= 0) {
            HiLog.error(TAG, "createCellInfoListFromParcel size: %{public}d", new Object[]{Integer.valueOf(readInt)});
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(readInt);
        while (true) {
            int i = readInt - 1;
            if (readInt <= 0) {
                return arrayList;
            }
            CallHandler callHandler = new CallHandler();
            messageParcel.readSequenceable(callHandler);
            arrayList.add(callHandler);
            readInt = i;
        }
    }
}
