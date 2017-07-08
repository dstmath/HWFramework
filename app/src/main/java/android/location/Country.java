package android.location;

import android.net.wifi.ScanResult.InformationElement;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import java.util.Locale;

public class Country implements Parcelable {
    public static final int COUNTRY_SOURCE_LOCALE = 3;
    public static final int COUNTRY_SOURCE_LOCATION = 1;
    public static final int COUNTRY_SOURCE_NETWORK = 0;
    public static final int COUNTRY_SOURCE_SIM = 2;
    public static final Creator<Country> CREATOR = null;
    public final String mCountryIso;
    private int mHashCode;
    private final int mSource;
    private final long mTimestamp;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.Country.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.Country.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.location.Country.<clinit>():void");
    }

    public Country(String countryIso, int source) {
        if (countryIso == null || source < 0 || source > COUNTRY_SOURCE_LOCALE) {
            throw new IllegalArgumentException();
        }
        this.mCountryIso = countryIso.toUpperCase(Locale.US);
        this.mSource = source;
        this.mTimestamp = SystemClock.elapsedRealtime();
    }

    private Country(String countryIso, int source, long timestamp) {
        if (countryIso == null || source < 0 || source > COUNTRY_SOURCE_LOCALE) {
            throw new IllegalArgumentException();
        }
        this.mCountryIso = countryIso.toUpperCase(Locale.US);
        this.mSource = source;
        this.mTimestamp = timestamp;
    }

    public Country(Country country) {
        this.mCountryIso = country.mCountryIso;
        this.mSource = country.mSource;
        this.mTimestamp = country.mTimestamp;
    }

    public final String getCountryIso() {
        return this.mCountryIso;
    }

    public final int getSource() {
        return this.mSource;
    }

    public final long getTimestamp() {
        return this.mTimestamp;
    }

    public int describeContents() {
        return COUNTRY_SOURCE_NETWORK;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mCountryIso);
        parcel.writeInt(this.mSource);
        parcel.writeLong(this.mTimestamp);
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (object == this) {
            return true;
        }
        if (!(object instanceof Country)) {
            return false;
        }
        Country c = (Country) object;
        if (!(this.mCountryIso.equals(c.getCountryIso()) && this.mSource == c.getSource())) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this.mHashCode == 0) {
            this.mHashCode = ((this.mCountryIso.hashCode() + InformationElement.EID_VSA) * 13) + this.mSource;
        }
        return this.mHashCode;
    }

    public boolean equalsIgnoreSource(Country country) {
        return country != null ? this.mCountryIso.equals(country.getCountryIso()) : false;
    }

    public String toString() {
        return "Country {ISO=" + this.mCountryIso + ", source=" + this.mSource + ", time=" + this.mTimestamp + "}";
    }
}
