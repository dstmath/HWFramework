package android.printservice.recommendation;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@SystemApi
public final class RecommendationInfo implements Parcelable {
    public static final Parcelable.Creator<RecommendationInfo> CREATOR = new Parcelable.Creator<RecommendationInfo>() {
        public RecommendationInfo createFromParcel(Parcel in) {
            return new RecommendationInfo(in);
        }

        public RecommendationInfo[] newArray(int size) {
            return new RecommendationInfo[size];
        }
    };
    private final List<InetAddress> mDiscoveredPrinters;
    private final CharSequence mName;
    private final CharSequence mPackageName;
    private final boolean mRecommendsMultiVendorService;

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
        ArrayList<InetAddress> discoveredPrinters = new ArrayList<>(numDiscoveredPrinters);
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

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    private RecommendationInfo(Parcel parcel) {
        this(parcel.readCharSequence(), parcel.readCharSequence(), (List<InetAddress>) readDiscoveredPrinters(parcel), parcel.readByte() != 0);
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
        dest.writeByte(this.mRecommendsMultiVendorService ? (byte) 1 : 0);
    }
}
