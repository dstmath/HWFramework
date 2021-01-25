package ohos.net;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class HttpProxy implements Sequenceable {
    public static final String LOCAL_HOST = "localhost";
    public static final int LOCAL_PORT = -1;
    public String exclusionList;
    public String host;
    public String[] parsedExclusionList;
    public int port;

    public HttpProxy(int i) {
        this.port = i;
    }

    public HttpProxy() {
    }

    public HttpProxy(HttpProxy httpProxy) {
        this.host = httpProxy.host;
        this.port = httpProxy.port;
        this.exclusionList = httpProxy.exclusionList;
        this.parsedExclusionList = httpProxy.parsedExclusionList;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HttpProxy)) {
            return false;
        }
        HttpProxy httpProxy = (HttpProxy) obj;
        String str = this.exclusionList;
        if ((str == null || str.equals(httpProxy.exclusionList)) && Objects.equals(this.host, httpProxy.host) && this.port == httpProxy.port) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        String str = this.host;
        int i = 0;
        int hashCode = str == null ? 0 : str.hashCode();
        String str2 = this.exclusionList;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return hashCode + i + this.port;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.host = null;
        this.port = 0;
        parcel.readByte();
        if (parcel.readByte() != 0) {
            this.host = parcel.readString();
            this.port = parcel.readInt();
        }
        this.exclusionList = parcel.readString();
        this.parsedExclusionList = parcel.readStringArray();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeByte((byte) 0);
        if (this.host != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.host);
            parcel.writeInt(this.port);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeString(this.exclusionList);
        String[] strArr = this.parsedExclusionList;
        if (strArr != null) {
            parcel.writeStringArray(strArr);
        } else {
            parcel.writeInt(-1);
        }
        return true;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String[] getExclusionList() {
        String[] strArr = this.parsedExclusionList;
        return strArr != null ? (String[]) strArr.clone() : new String[0];
    }
}
