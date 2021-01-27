package android.os;

import android.annotation.SystemApi;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable;
import android.util.IntArray;
import java.util.ArrayList;

@SystemApi
public final class IncidentReportArgs implements Parcelable {
    public static final Parcelable.Creator<IncidentReportArgs> CREATOR = new Parcelable.Creator<IncidentReportArgs>() {
        /* class android.os.IncidentReportArgs.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IncidentReportArgs createFromParcel(Parcel in) {
            return new IncidentReportArgs(in);
        }

        @Override // android.os.Parcelable.Creator
        public IncidentReportArgs[] newArray(int size) {
            return new IncidentReportArgs[size];
        }
    };
    private boolean mAll;
    private final ArrayList<byte[]> mHeaders;
    private int mPrivacyPolicy;
    private String mReceiverCls;
    private String mReceiverPkg;
    private final IntArray mSections;

    public IncidentReportArgs() {
        this.mSections = new IntArray();
        this.mHeaders = new ArrayList<>();
        this.mPrivacyPolicy = 200;
    }

    public IncidentReportArgs(Parcel in) {
        this.mSections = new IntArray();
        this.mHeaders = new ArrayList<>();
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mAll ? 1 : 0);
        int N = this.mSections.size();
        out.writeInt(N);
        for (int i = 0; i < N; i++) {
            out.writeInt(this.mSections.get(i));
        }
        int N2 = this.mHeaders.size();
        out.writeInt(N2);
        for (int i2 = 0; i2 < N2; i2++) {
            out.writeByteArray(this.mHeaders.get(i2));
        }
        out.writeInt(this.mPrivacyPolicy);
        out.writeString(this.mReceiverPkg);
        out.writeString(this.mReceiverCls);
    }

    public void readFromParcel(Parcel in) {
        this.mAll = in.readInt() != 0;
        this.mSections.clear();
        int N = in.readInt();
        for (int i = 0; i < N; i++) {
            this.mSections.add(in.readInt());
        }
        this.mHeaders.clear();
        int N2 = in.readInt();
        for (int i2 = 0; i2 < N2; i2++) {
            this.mHeaders.add(in.createByteArray());
        }
        this.mPrivacyPolicy = in.readInt();
        this.mReceiverPkg = in.readString();
        this.mReceiverCls = in.readString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Incident(");
        if (this.mAll) {
            sb.append("all");
        } else {
            int N = this.mSections.size();
            if (N > 0) {
                sb.append(this.mSections.get(0));
            }
            for (int i = 1; i < N; i++) {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                sb.append(this.mSections.get(i));
            }
        }
        sb.append(", ");
        sb.append(this.mHeaders.size());
        sb.append(" headers), ");
        sb.append("privacy: ");
        sb.append(this.mPrivacyPolicy);
        sb.append("receiver pkg: ");
        sb.append(this.mReceiverPkg);
        sb.append("receiver cls: ");
        sb.append(this.mReceiverCls);
        return sb.toString();
    }

    public void setAll(boolean all) {
        this.mAll = all;
        if (all) {
            this.mSections.clear();
        }
    }

    public void setPrivacyPolicy(int privacyPolicy) {
        if (privacyPolicy == 0 || privacyPolicy == 100 || privacyPolicy == 200) {
            this.mPrivacyPolicy = privacyPolicy;
        } else {
            this.mPrivacyPolicy = 200;
        }
    }

    public void addSection(int section) {
        if (!this.mAll && section > 1) {
            this.mSections.add(section);
        }
    }

    public boolean isAll() {
        return this.mAll;
    }

    public boolean containsSection(int section) {
        return this.mAll || this.mSections.indexOf(section) >= 0;
    }

    public int sectionCount() {
        return this.mSections.size();
    }

    public void addHeader(byte[] header) {
        this.mHeaders.add(header);
    }
}
