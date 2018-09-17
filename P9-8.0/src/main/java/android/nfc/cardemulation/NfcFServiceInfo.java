package android.nfc.cardemulation;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import org.xmlpull.v1.XmlPullParserException;

public final class NfcFServiceInfo implements Parcelable {
    public static final Creator<NfcFServiceInfo> CREATOR = new Creator<NfcFServiceInfo>() {
        public NfcFServiceInfo createFromParcel(Parcel source) {
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            String systemCode = source.readString();
            String dynamicSystemCode = null;
            if (source.readInt() != 0) {
                dynamicSystemCode = source.readString();
            }
            String nfcid2 = source.readString();
            String dynamicNfcid2 = null;
            if (source.readInt() != 0) {
                dynamicNfcid2 = source.readString();
            }
            return new NfcFServiceInfo(info, description, systemCode, dynamicSystemCode, nfcid2, dynamicNfcid2, source.readInt());
        }

        public NfcFServiceInfo[] newArray(int size) {
            return new NfcFServiceInfo[size];
        }
    };
    static final String TAG = "NfcFServiceInfo";
    final String mDescription;
    String mDynamicNfcid2;
    String mDynamicSystemCode;
    final String mNfcid2;
    final ResolveInfo mService;
    final String mSystemCode;
    final int mUid;

    public NfcFServiceInfo(ResolveInfo info, String description, String systemCode, String dynamicSystemCode, String nfcid2, String dynamicNfcid2, int uid) {
        this.mService = info;
        this.mDescription = description;
        this.mSystemCode = systemCode;
        this.mDynamicSystemCode = dynamicSystemCode;
        this.mNfcid2 = nfcid2;
        this.mDynamicNfcid2 = dynamicNfcid2;
        this.mUid = uid;
    }

    public NfcFServiceInfo(PackageManager pm, ResolveInfo info) throws XmlPullParserException, IOException {
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser = null;
        try {
            parser = si.loadXmlMetaData(pm, HostNfcFService.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No android.nfc.cardemulation.host_nfcf_service meta-data");
            }
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
            }
            if ("host-nfcf-service".equals(parser.getName())) {
                Resources res = pm.getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.HostNfcFService);
                this.mService = info;
                this.mDescription = sa.getString(0);
                this.mDynamicSystemCode = null;
                this.mDynamicNfcid2 = null;
                sa.recycle();
                String systemCode = null;
                String nfcid2 = null;
                int depth = parser.getDepth();
                while (true) {
                    eventType = parser.next();
                    if ((eventType != 3 || parser.getDepth() > depth) && eventType != 1) {
                        String tagName = parser.getName();
                        TypedArray a;
                        if (eventType == 2 && "system-code-filter".equals(tagName) && systemCode == null) {
                            a = res.obtainAttributes(attrs, R.styleable.SystemCodeFilter);
                            systemCode = a.getString(0).toUpperCase();
                            Log.d(TAG, "systemCode: " + systemCode);
                            if (!(NfcFCardEmulation.isValidSystemCode(systemCode) || (systemCode.equalsIgnoreCase(WifiEnterpriseConfig.EMPTY_VALUE) ^ 1) == 0)) {
                                Log.e(TAG, "Invalid System Code: " + systemCode);
                                systemCode = null;
                            }
                            a.recycle();
                        } else if (eventType == 2 && "nfcid2-filter".equals(tagName) && nfcid2 == null) {
                            a = res.obtainAttributes(attrs, R.styleable.Nfcid2Filter);
                            nfcid2 = a.getString(0).toUpperCase();
                            if (!(nfcid2.equalsIgnoreCase("RANDOM") || (nfcid2.equalsIgnoreCase(WifiEnterpriseConfig.EMPTY_VALUE) ^ 1) == 0 || (NfcFCardEmulation.isValidNfcid2(nfcid2) ^ 1) == 0)) {
                                Log.e(TAG, "Invalid NFCID2: " + nfcid2);
                                nfcid2 = null;
                            }
                            a.recycle();
                        }
                    }
                }
                if (systemCode == null) {
                    systemCode = WifiEnterpriseConfig.EMPTY_VALUE;
                }
                this.mSystemCode = systemCode;
                if (nfcid2 == null) {
                    nfcid2 = WifiEnterpriseConfig.EMPTY_VALUE;
                }
                this.mNfcid2 = nfcid2;
                if (parser != null) {
                    parser.close();
                }
                this.mUid = si.applicationInfo.uid;
                return;
            }
            throw new XmlPullParserException("Meta-data does not start with <host-nfcf-service> tag");
        } catch (NameNotFoundException e) {
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public String getSystemCode() {
        return this.mDynamicSystemCode == null ? this.mSystemCode : this.mDynamicSystemCode;
    }

    public void setOrReplaceDynamicSystemCode(String systemCode) {
        this.mDynamicSystemCode = systemCode;
    }

    public String getNfcid2() {
        return this.mDynamicNfcid2 == null ? this.mNfcid2 : this.mDynamicNfcid2;
    }

    public void setOrReplaceDynamicNfcid2(String nfcid2) {
        this.mDynamicNfcid2 = nfcid2;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public int getUid() {
        return this.mUid;
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public String toString() {
        StringBuilder out = new StringBuilder("NfcFService: ");
        out.append(getComponent());
        out.append(", description: ").append(this.mDescription);
        out.append(", System Code: ").append(this.mSystemCode);
        if (this.mDynamicSystemCode != null) {
            out.append(", dynamic System Code: ").append(this.mDynamicSystemCode);
        }
        out.append(", NFCID2: ").append(this.mNfcid2);
        if (this.mDynamicNfcid2 != null) {
            out.append(", dynamic NFCID2: ").append(this.mDynamicNfcid2);
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NfcFServiceInfo)) {
            return false;
        }
        NfcFServiceInfo thatService = (NfcFServiceInfo) o;
        return thatService.getComponent().equals(getComponent()) && thatService.mSystemCode.equalsIgnoreCase(this.mSystemCode) && thatService.mNfcid2.equalsIgnoreCase(this.mNfcid2);
    }

    public int hashCode() {
        return getComponent().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeString(this.mSystemCode);
        dest.writeInt(this.mDynamicSystemCode != null ? 1 : 0);
        if (this.mDynamicSystemCode != null) {
            dest.writeString(this.mDynamicSystemCode);
        }
        dest.writeString(this.mNfcid2);
        if (this.mDynamicNfcid2 == null) {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mDynamicNfcid2 != null) {
            dest.writeString(this.mDynamicNfcid2);
        }
        dest.writeInt(this.mUid);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    " + getComponent() + " (Description: " + getDescription() + ")");
        pw.println("    System Code: " + getSystemCode());
        pw.println("    NFCID2: " + getNfcid2());
    }
}
