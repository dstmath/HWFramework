package android.printservice.recommendation;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class RecommendationInfo implements Parcelable {
    public static final Creator<RecommendationInfo> CREATOR = new Creator<RecommendationInfo>() {
        public RecommendationInfo createFromParcel(Parcel in) {
            return new RecommendationInfo(in, null);
        }

        public RecommendationInfo[] newArray(int size) {
            return new RecommendationInfo[size];
        }
    };
    private final List<InetAddress> mDiscoveredPrinters;
    private final CharSequence mName;
    private final CharSequence mPackageName;
    private final boolean mRecommendsMultiVendorService;

    /* synthetic */ RecommendationInfo(Parcel parcel, RecommendationInfo -this1) {
        this(parcel);
    }

    public RecommendationInfo(CharSequence packageName, CharSequence name, List<InetAddress> discoveredPrinters, boolean recommendsMultiVendorService) {
        this.mPackageName = Preconditions.checkStringNotEmpty(packageName);
        this.mName = Preconditions.checkStringNotEmpty(name);
        this.mDiscoveredPrinters = (List) Preconditions.checkCollectionElementsNotNull(discoveredPrinters, "discoveredPrinters");
        this.mRecommendsMultiVendorService = recommendsMultiVendorService;
    }

    @Deprecated
    public RecommendationInfo(CharSequence packageName, CharSequence name, int numDiscoveredPrinters, boolean recommendsMultiVendorService) {
        throw new IllegalArgumentException("This constructor has been deprecated");
    }

    private static ArrayList<InetAddress> readDiscoveredPrinters(Parcel parcel) {
        int numDiscoveredPrinters = parcel.readInt();
        ArrayList<InetAddress> discoveredPrinters = new ArrayList(numDiscoveredPrinters);
        int i = 0;
        while (i < numDiscoveredPrinters) {
            try {
                discoveredPrinters.add(InetAddress.getByAddress(parcel.readBlob()));
                i++;
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return discoveredPrinters;
    }

    private RecommendationInfo(Parcel parcel) {
        boolean z = false;
        CharSequence readCharSequence = parcel.readCharSequence();
        CharSequence readCharSequence2 = parcel.readCharSequence();
        List readDiscoveredPrinters = readDiscoveredPrinters(parcel);
        if (parcel.readByte() != (byte) 0) {
            z = true;
        }
        this(readCharSequence, readCharSequence2, readDiscoveredPrinters, z);
    }

    public CharSequence getPackageName() {
        return this.mPackageName;
    }

    public boolean recommendsMultiVendorService() {
        return this.mRecommendsMultiVendorService;
    }

    public List<InetAddress> getDiscoveredPrinters() {
        return this.mDiscoveredPrinters;
    }

    public int getNumDiscoveredPrinters() {
        return this.mDiscoveredPrinters.size();
    }

    public CharSequence getName() {
        return this.mName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mPackageName);
        dest.writeCharSequence(this.mName);
        dest.writeInt(this.mDiscoveredPrinters.size());
        for (InetAddress printer : this.mDiscoveredPrinters) {
            dest.writeBlob(printer.getAddress());
        }
        dest.writeByte((byte) (this.mRecommendsMultiVendorService ? 1 : 0));
    }
}
