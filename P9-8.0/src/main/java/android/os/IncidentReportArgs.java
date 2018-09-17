package android.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable.Creator;
import android.util.IntArray;
import java.util.ArrayList;

public final class IncidentReportArgs implements Parcelable {
    public static final Creator<IncidentReportArgs> CREATOR = new Creator<IncidentReportArgs>() {
        public IncidentReportArgs createFromParcel(Parcel in) {
            return new IncidentReportArgs(in);
        }

        public IncidentReportArgs[] newArray(int size) {
            return new IncidentReportArgs[size];
        }
    };
    private boolean mAll;
    private final ArrayList<byte[]> mHeaders = new ArrayList();
    private final IntArray mSections = new IntArray();

    public IncidentReportArgs(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        out.writeInt(this.mAll ? 1 : 0);
        int N = this.mSections.size();
        out.writeInt(N);
        for (i = 0; i < N; i++) {
            out.writeInt(this.mSections.get(i));
        }
        N = this.mHeaders.size();
        out.writeInt(N);
        for (i = 0; i < N; i++) {
            out.writeByteArray((byte[]) this.mHeaders.get(i));
        }
    }

    public void readFromParcel(Parcel in) {
        int i;
        boolean z = false;
        if (in.readInt() != 0) {
            z = true;
        }
        this.mAll = z;
        this.mSections.clear();
        int N = in.readInt();
        for (i = 0; i < N; i++) {
            this.mSections.add(in.readInt());
        }
        this.mHeaders.clear();
        N = in.readInt();
        for (i = 0; i < N; i++) {
            this.mHeaders.add(in.createByteArray());
        }
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
        sb.append(" headers)");
        return sb.toString();
    }

    public void setAll(boolean all) {
        this.mAll = all;
        if (all) {
            this.mSections.clear();
        }
    }

    public void addSection(int section) {
        if (!this.mAll) {
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

    public static IncidentReportArgs parseSetting(String setting) throws IllegalArgumentException {
        if (setting == null || setting.length() == 0) {
            return null;
        }
        setting = setting.trim();
        if (setting.length() == 0 || "disabled".equals(setting)) {
            return null;
        }
        IncidentReportArgs args = new IncidentReportArgs();
        if ("all".equals(setting)) {
            args.setAll(true);
            return args;
        } else if ("none".equals(setting)) {
            return args;
        } else {
            String[] splits = setting.split(",");
            int N = splits.length;
            for (int i = 0; i < N; i++) {
                String str = splits[i].trim();
                if (str.length() != 0) {
                    try {
                        int section = Integer.parseInt(str);
                        if (section < 1) {
                            throw new IllegalArgumentException("Malformed setting. Illegal section at index " + i + ": section='" + str + "' setting='" + setting + "'");
                        }
                        args.addSection(section);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Malformed setting. Bad integer at section index " + i + ": section='" + str + "' setting='" + setting + "'");
                    }
                }
            }
            return args;
        }
    }
}
