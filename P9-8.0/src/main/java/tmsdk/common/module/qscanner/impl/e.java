package tmsdk.common.module.qscanner.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class e implements Parcelable, Serializable {
    public static final Creator<e> CREATOR = new Creator<e>() {
        /* renamed from: bc */
        public e[] newArray(int i) {
            return new e[i];
        }

        /* renamed from: m */
        public e createFromParcel(Parcel parcel) {
            boolean z = false;
            e eVar = new e();
            eVar.packageName = parcel.readString();
            eVar.softName = parcel.readString();
            eVar.version = parcel.readString();
            eVar.versionCode = parcel.readInt();
            eVar.path = parcel.readString();
            eVar.BQ = parcel.readInt();
            eVar.bZ = parcel.readString();
            eVar.Cb = parcel.readString();
            eVar.size = parcel.readInt();
            eVar.cc = parcel.readString();
            eVar.plugins = parcel.createTypedArrayList(b.CREATOR);
            eVar.name = parcel.readString();
            eVar.type = parcel.readInt();
            eVar.lL = parcel.readInt();
            eVar.BU = parcel.readInt();
            eVar.name = parcel.readString();
            eVar.label = parcel.readString();
            eVar.BT = parcel.readString();
            eVar.url = parcel.readString();
            eVar.gS = parcel.readInt();
            eVar.Cc = parcel.readString();
            int readInt = parcel.readInt();
            if (readInt > 0) {
                eVar.Cd = new ArrayList(readInt);
                parcel.readStringList(eVar.Cd);
            }
            eVar.Ce = parcel.readInt();
            eVar.Cf = parcel.readInt();
            eVar.Cg = parcel.readByte() == (byte) 1;
            eVar.Ch = parcel.readByte() == (byte) 1;
            eVar.Ci = parcel.readByte() == (byte) 1;
            eVar.Cj = parcel.readByte() == (byte) 1;
            eVar.dp = parcel.readInt();
            eVar.category = parcel.readInt();
            eVar.official = parcel.readInt();
            eVar.Ck = parcel.readString();
            eVar.Cl = parcel.readString();
            eVar.Cm = parcel.readByte() == (byte) 1;
            eVar.lastModified = parcel.readLong();
            eVar.Cn = parcel.readByte() == (byte) 1;
            if (parcel.readByte() == (byte) 1) {
                z = true;
            }
            eVar.Co = z;
            return eVar;
        }
    };
    public int BQ;
    public String BT;
    public int BU;
    public String Cb;
    public String Cc;
    public List<String> Cd;
    public int Ce = -1;
    public int Cf = -1;
    public boolean Cg = false;
    public boolean Ch = false;
    public boolean Ci = false;
    public boolean Cj = false;
    public String Ck;
    public String Cl;
    public boolean Cm;
    public boolean Cn;
    public boolean Co = false;
    public String bZ;
    public int category = 0;
    public String cc;
    public int dp = 0;
    public String fA;
    public int gS;
    public int lL;
    public String label;
    public long lastModified;
    public String name;
    public int official = 0;
    public String packageName;
    public String path;
    public ArrayList<b> plugins;
    public int size;
    public String softName;
    public int type;
    public String url;
    public String version;
    public int versionCode;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeString(this.packageName);
        parcel.writeString(this.softName);
        parcel.writeString(this.version);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.path);
        parcel.writeInt(this.BQ);
        parcel.writeString(this.bZ);
        parcel.writeString(this.Cb);
        parcel.writeInt(this.size);
        parcel.writeString(this.cc);
        parcel.writeTypedList(this.plugins);
        parcel.writeString(this.name);
        parcel.writeInt(this.type);
        parcel.writeInt(this.lL);
        parcel.writeInt(this.BU);
        parcel.writeString(this.name);
        parcel.writeString(this.label);
        parcel.writeString(this.BT);
        parcel.writeString(this.url);
        parcel.writeInt(this.gS);
        parcel.writeString(this.Cc);
        if (this.Cd == null || this.Cd.size() == 0) {
            parcel.writeInt(0);
        } else {
            parcel.writeInt(this.Cd.size());
            parcel.writeStringList(this.Cd);
        }
        parcel.writeInt(this.Ce);
        parcel.writeInt(this.Cf);
        parcel.writeByte((byte) (!this.Cg ? 0 : 1));
        parcel.writeByte((byte) (!this.Ch ? 0 : 1));
        parcel.writeByte((byte) (!this.Ci ? 0 : 1));
        parcel.writeByte((byte) (!this.Cj ? 0 : 1));
        parcel.writeInt(this.dp);
        parcel.writeInt(this.category);
        parcel.writeInt(this.official);
        parcel.writeString(this.Ck);
        parcel.writeString(this.Cl);
        parcel.writeByte((byte) (!this.Cm ? 0 : 1));
        parcel.writeLong(this.lastModified);
        parcel.writeByte((byte) (!this.Cn ? 0 : 1));
        if (this.Co) {
            i2 = 1;
        }
        parcel.writeByte((byte) i2);
    }
}
