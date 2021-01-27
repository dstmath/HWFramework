package android.print;

import android.os.Parcel;
import android.os.Parcelable;
import android.print.PrintAttributes;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

public final class PrinterCapabilitiesInfo implements Parcelable {
    public static final Parcelable.Creator<PrinterCapabilitiesInfo> CREATOR = new Parcelable.Creator<PrinterCapabilitiesInfo>() {
        /* class android.print.PrinterCapabilitiesInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PrinterCapabilitiesInfo createFromParcel(Parcel parcel) {
            return new PrinterCapabilitiesInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PrinterCapabilitiesInfo[] newArray(int size) {
            return new PrinterCapabilitiesInfo[size];
        }
    };
    private static final PrintAttributes.Margins DEFAULT_MARGINS = new PrintAttributes.Margins(0, 0, 0, 0);
    public static final int DEFAULT_UNDEFINED = -1;
    private static final int PROPERTY_COLOR_MODE = 2;
    private static final int PROPERTY_COUNT = 4;
    private static final int PROPERTY_DUPLEX_MODE = 3;
    private static final int PROPERTY_MEDIA_SIZE = 0;
    private static final int PROPERTY_RESOLUTION = 1;
    private int mColorModes;
    private final int[] mDefaults;
    private int mDuplexModes;
    private List<PrintAttributes.MediaSize> mMediaSizes;
    private PrintAttributes.Margins mMinMargins;
    private List<PrintAttributes.Resolution> mResolutions;

    public PrinterCapabilitiesInfo() {
        this.mMinMargins = DEFAULT_MARGINS;
        this.mDefaults = new int[4];
        Arrays.fill(this.mDefaults, -1);
    }

    public PrinterCapabilitiesInfo(PrinterCapabilitiesInfo prototype) {
        this.mMinMargins = DEFAULT_MARGINS;
        this.mDefaults = new int[4];
        copyFrom(prototype);
    }

    public void copyFrom(PrinterCapabilitiesInfo other) {
        if (this != other) {
            this.mMinMargins = other.mMinMargins;
            List<PrintAttributes.MediaSize> list = other.mMediaSizes;
            if (list != null) {
                List<PrintAttributes.MediaSize> list2 = this.mMediaSizes;
                if (list2 != null) {
                    list2.clear();
                    this.mMediaSizes.addAll(other.mMediaSizes);
                } else {
                    this.mMediaSizes = new ArrayList(list);
                }
            } else {
                this.mMediaSizes = null;
            }
            List<PrintAttributes.Resolution> list3 = other.mResolutions;
            if (list3 != null) {
                List<PrintAttributes.Resolution> list4 = this.mResolutions;
                if (list4 != null) {
                    list4.clear();
                    this.mResolutions.addAll(other.mResolutions);
                } else {
                    this.mResolutions = new ArrayList(list3);
                }
            } else {
                this.mResolutions = null;
            }
            this.mColorModes = other.mColorModes;
            this.mDuplexModes = other.mDuplexModes;
            int defaultCount = other.mDefaults.length;
            for (int i = 0; i < defaultCount; i++) {
                this.mDefaults[i] = other.mDefaults[i];
            }
        }
    }

    public List<PrintAttributes.MediaSize> getMediaSizes() {
        return Collections.unmodifiableList(this.mMediaSizes);
    }

    public List<PrintAttributes.Resolution> getResolutions() {
        return Collections.unmodifiableList(this.mResolutions);
    }

    public PrintAttributes.Margins getMinMargins() {
        return this.mMinMargins;
    }

    public int getColorModes() {
        return this.mColorModes;
    }

    public int getDuplexModes() {
        return this.mDuplexModes;
    }

    public PrintAttributes getDefaults() {
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMinMargins(this.mMinMargins);
        int mediaSizeIndex = this.mDefaults[0];
        if (mediaSizeIndex >= 0) {
            builder.setMediaSize(this.mMediaSizes.get(mediaSizeIndex));
        }
        int resolutionIndex = this.mDefaults[1];
        if (resolutionIndex >= 0) {
            builder.setResolution(this.mResolutions.get(resolutionIndex));
        }
        int colorMode = this.mDefaults[2];
        if (colorMode > 0) {
            builder.setColorMode(colorMode);
        }
        int duplexMode = this.mDefaults[3];
        if (duplexMode > 0) {
            builder.setDuplexMode(duplexMode);
        }
        return builder.build();
    }

    /* access modifiers changed from: private */
    public static void enforceValidMask(int mask, IntConsumer enforceSingle) {
        int current = mask;
        while (current > 0) {
            int currentMode = 1 << Integer.numberOfTrailingZeros(current);
            current &= ~currentMode;
            enforceSingle.accept(currentMode);
        }
    }

    private PrinterCapabilitiesInfo(Parcel parcel) {
        this.mMinMargins = DEFAULT_MARGINS;
        this.mDefaults = new int[4];
        this.mMinMargins = (PrintAttributes.Margins) Preconditions.checkNotNull(readMargins(parcel));
        readMediaSizes(parcel);
        readResolutions(parcel);
        this.mColorModes = parcel.readInt();
        enforceValidMask(this.mColorModes, $$Lambda$PrinterCapabilitiesInfo$2mJhwjGC7Dgi0vwDsnG83V2s6sE.INSTANCE);
        this.mDuplexModes = parcel.readInt();
        enforceValidMask(this.mDuplexModes, $$Lambda$PrinterCapabilitiesInfo$TL1SYHyXTbqj2Nseol9bDJQOn3U.INSTANCE);
        readDefaults(parcel);
        boolean z = false;
        Preconditions.checkArgument(this.mMediaSizes.size() > this.mDefaults[0]);
        Preconditions.checkArgument(this.mResolutions.size() > this.mDefaults[1] ? true : z);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        writeMargins(this.mMinMargins, parcel);
        writeMediaSizes(parcel);
        writeResolutions(parcel);
        parcel.writeInt(this.mColorModes);
        parcel.writeInt(this.mDuplexModes);
        writeDefaults(parcel);
    }

    public int hashCode() {
        int i = 1 * 31;
        PrintAttributes.Margins margins = this.mMinMargins;
        int i2 = 0;
        int result = (i + (margins == null ? 0 : margins.hashCode())) * 31;
        List<PrintAttributes.MediaSize> list = this.mMediaSizes;
        int result2 = (result + (list == null ? 0 : list.hashCode())) * 31;
        List<PrintAttributes.Resolution> list2 = this.mResolutions;
        if (list2 != null) {
            i2 = list2.hashCode();
        }
        return ((((((result2 + i2) * 31) + this.mColorModes) * 31) + this.mDuplexModes) * 31) + Arrays.hashCode(this.mDefaults);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrinterCapabilitiesInfo other = (PrinterCapabilitiesInfo) obj;
        PrintAttributes.Margins margins = this.mMinMargins;
        if (margins == null) {
            if (other.mMinMargins != null) {
                return false;
            }
        } else if (!margins.equals(other.mMinMargins)) {
            return false;
        }
        List<PrintAttributes.MediaSize> list = this.mMediaSizes;
        if (list == null) {
            if (other.mMediaSizes != null) {
                return false;
            }
        } else if (!list.equals(other.mMediaSizes)) {
            return false;
        }
        List<PrintAttributes.Resolution> list2 = this.mResolutions;
        if (list2 == null) {
            if (other.mResolutions != null) {
                return false;
            }
        } else if (!list2.equals(other.mResolutions)) {
            return false;
        }
        if (this.mColorModes == other.mColorModes && this.mDuplexModes == other.mDuplexModes && Arrays.equals(this.mDefaults, other.mDefaults)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "PrinterInfo{minMargins=" + this.mMinMargins + ", mediaSizes=" + this.mMediaSizes + ", resolutions=" + this.mResolutions + ", colorModes=" + colorModesToString() + ", duplexModes=" + duplexModesToString() + "\"}";
    }

    private String colorModesToString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        int colorModes = this.mColorModes;
        while (colorModes != 0) {
            int colorMode = 1 << Integer.numberOfTrailingZeros(colorModes);
            colorModes &= ~colorMode;
            if (builder.length() > 1) {
                builder.append(", ");
            }
            builder.append(PrintAttributes.colorModeToString(colorMode));
        }
        builder.append(']');
        return builder.toString();
    }

    private String duplexModesToString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        int duplexModes = this.mDuplexModes;
        while (duplexModes != 0) {
            int duplexMode = 1 << Integer.numberOfTrailingZeros(duplexModes);
            duplexModes &= ~duplexMode;
            if (builder.length() > 1) {
                builder.append(", ");
            }
            builder.append(PrintAttributes.duplexModeToString(duplexMode));
        }
        builder.append(']');
        return builder.toString();
    }

    private void writeMediaSizes(Parcel parcel) {
        List<PrintAttributes.MediaSize> list = this.mMediaSizes;
        if (list == null) {
            parcel.writeInt(0);
            return;
        }
        int mediaSizeCount = list.size();
        parcel.writeInt(mediaSizeCount);
        for (int i = 0; i < mediaSizeCount; i++) {
            this.mMediaSizes.get(i).writeToParcel(parcel);
        }
    }

    private void readMediaSizes(Parcel parcel) {
        int mediaSizeCount = parcel.readInt();
        if (mediaSizeCount > 0 && this.mMediaSizes == null) {
            this.mMediaSizes = new ArrayList();
        }
        for (int i = 0; i < mediaSizeCount; i++) {
            this.mMediaSizes.add(PrintAttributes.MediaSize.createFromParcel(parcel));
        }
    }

    private void writeResolutions(Parcel parcel) {
        List<PrintAttributes.Resolution> list = this.mResolutions;
        if (list == null) {
            parcel.writeInt(0);
            return;
        }
        int resolutionCount = list.size();
        parcel.writeInt(resolutionCount);
        for (int i = 0; i < resolutionCount; i++) {
            this.mResolutions.get(i).writeToParcel(parcel);
        }
    }

    private void readResolutions(Parcel parcel) {
        int resolutionCount = parcel.readInt();
        if (resolutionCount > 0 && this.mResolutions == null) {
            this.mResolutions = new ArrayList();
        }
        for (int i = 0; i < resolutionCount; i++) {
            this.mResolutions.add(PrintAttributes.Resolution.createFromParcel(parcel));
        }
    }

    private void writeMargins(PrintAttributes.Margins margins, Parcel parcel) {
        if (margins == null) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(1);
        margins.writeToParcel(parcel);
    }

    private PrintAttributes.Margins readMargins(Parcel parcel) {
        if (parcel.readInt() == 1) {
            return PrintAttributes.Margins.createFromParcel(parcel);
        }
        return null;
    }

    private void readDefaults(Parcel parcel) {
        int defaultCount = parcel.readInt();
        for (int i = 0; i < defaultCount; i++) {
            this.mDefaults[i] = parcel.readInt();
        }
    }

    private void writeDefaults(Parcel parcel) {
        int defaultCount = this.mDefaults.length;
        parcel.writeInt(defaultCount);
        for (int i = 0; i < defaultCount; i++) {
            parcel.writeInt(this.mDefaults[i]);
        }
    }

    public static final class Builder {
        private final PrinterCapabilitiesInfo mPrototype;

        public Builder(PrinterId printerId) {
            if (printerId != null) {
                this.mPrototype = new PrinterCapabilitiesInfo();
                return;
            }
            throw new IllegalArgumentException("printerId cannot be null.");
        }

        public Builder addMediaSize(PrintAttributes.MediaSize mediaSize, boolean isDefault) {
            if (this.mPrototype.mMediaSizes == null) {
                this.mPrototype.mMediaSizes = new ArrayList();
            }
            int insertionIndex = this.mPrototype.mMediaSizes.size();
            this.mPrototype.mMediaSizes.add(mediaSize);
            if (isDefault) {
                throwIfDefaultAlreadySpecified(0);
                this.mPrototype.mDefaults[0] = insertionIndex;
            }
            return this;
        }

        public Builder addResolution(PrintAttributes.Resolution resolution, boolean isDefault) {
            if (this.mPrototype.mResolutions == null) {
                this.mPrototype.mResolutions = new ArrayList();
            }
            int insertionIndex = this.mPrototype.mResolutions.size();
            this.mPrototype.mResolutions.add(resolution);
            if (isDefault) {
                throwIfDefaultAlreadySpecified(1);
                this.mPrototype.mDefaults[1] = insertionIndex;
            }
            return this;
        }

        public Builder setMinMargins(PrintAttributes.Margins margins) {
            if (margins != null) {
                this.mPrototype.mMinMargins = margins;
                return this;
            }
            throw new IllegalArgumentException("margins cannot be null");
        }

        public Builder setColorModes(int colorModes, int defaultColorMode) {
            PrinterCapabilitiesInfo.enforceValidMask(colorModes, $$Lambda$PrinterCapabilitiesInfo$Builder$dbsSt8pZfd6hqZ6hGCnpzhPK6Uk.INSTANCE);
            PrintAttributes.enforceValidColorMode(defaultColorMode);
            this.mPrototype.mColorModes = colorModes;
            this.mPrototype.mDefaults[2] = defaultColorMode;
            return this;
        }

        public Builder setDuplexModes(int duplexModes, int defaultDuplexMode) {
            PrinterCapabilitiesInfo.enforceValidMask(duplexModes, $$Lambda$PrinterCapabilitiesInfo$Builder$gsgXbNHGWpWENdPzemgHcCY8HnE.INSTANCE);
            PrintAttributes.enforceValidDuplexMode(defaultDuplexMode);
            this.mPrototype.mDuplexModes = duplexModes;
            this.mPrototype.mDefaults[3] = defaultDuplexMode;
            return this;
        }

        public PrinterCapabilitiesInfo build() {
            if (this.mPrototype.mMediaSizes == null || this.mPrototype.mMediaSizes.isEmpty()) {
                throw new IllegalStateException("No media size specified.");
            } else if (this.mPrototype.mDefaults[0] == -1) {
                throw new IllegalStateException("No default media size specified.");
            } else if (this.mPrototype.mResolutions == null || this.mPrototype.mResolutions.isEmpty()) {
                throw new IllegalStateException("No resolution specified.");
            } else if (this.mPrototype.mDefaults[1] == -1) {
                throw new IllegalStateException("No default resolution specified.");
            } else if (this.mPrototype.mColorModes == 0) {
                throw new IllegalStateException("No color mode specified.");
            } else if (this.mPrototype.mDefaults[2] != -1) {
                if (this.mPrototype.mDuplexModes == 0) {
                    setDuplexModes(1, 1);
                }
                if (this.mPrototype.mMinMargins != null) {
                    return this.mPrototype;
                }
                throw new IllegalArgumentException("margins cannot be null");
            } else {
                throw new IllegalStateException("No default color mode specified.");
            }
        }

        private void throwIfDefaultAlreadySpecified(int propertyIndex) {
            if (this.mPrototype.mDefaults[propertyIndex] != -1) {
                throw new IllegalArgumentException("Default already specified.");
            }
        }
    }
}
