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
        public PrinterCapabilitiesInfo createFromParcel(Parcel parcel) {
            return new PrinterCapabilitiesInfo(parcel);
        }

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
    /* access modifiers changed from: private */
    public int mColorModes;
    /* access modifiers changed from: private */
    public final int[] mDefaults;
    /* access modifiers changed from: private */
    public int mDuplexModes;
    /* access modifiers changed from: private */
    public List<PrintAttributes.MediaSize> mMediaSizes;
    /* access modifiers changed from: private */
    public PrintAttributes.Margins mMinMargins;
    /* access modifiers changed from: private */
    public List<PrintAttributes.Resolution> mResolutions;

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
                List unused = this.mPrototype.mMediaSizes = new ArrayList();
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
                List unused = this.mPrototype.mResolutions = new ArrayList();
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
                PrintAttributes.Margins unused = this.mPrototype.mMinMargins = margins;
                return this;
            }
            throw new IllegalArgumentException("margins cannot be null");
        }

        public Builder setColorModes(int colorModes, int defaultColorMode) {
            PrinterCapabilitiesInfo.enforceValidMask(colorModes, $$Lambda$PrinterCapabilitiesInfo$Builder$dbsSt8pZfd6hqZ6hGCnpzhPK6Uk.INSTANCE);
            PrintAttributes.enforceValidColorMode(defaultColorMode);
            int unused = this.mPrototype.mColorModes = colorModes;
            this.mPrototype.mDefaults[2] = defaultColorMode;
            return this;
        }

        public Builder setDuplexModes(int duplexModes, int defaultDuplexMode) {
            PrinterCapabilitiesInfo.enforceValidMask(duplexModes, $$Lambda$PrinterCapabilitiesInfo$Builder$gsgXbNHGWpWENdPzemgHcCY8HnE.INSTANCE);
            PrintAttributes.enforceValidDuplexMode(defaultDuplexMode);
            int unused = this.mPrototype.mDuplexModes = duplexModes;
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
            if (other.mMediaSizes == null) {
                this.mMediaSizes = null;
            } else if (this.mMediaSizes != null) {
                this.mMediaSizes.clear();
                this.mMediaSizes.addAll(other.mMediaSizes);
            } else {
                this.mMediaSizes = new ArrayList(other.mMediaSizes);
            }
            if (other.mResolutions == null) {
                this.mResolutions = null;
            } else if (this.mResolutions != null) {
                this.mResolutions.clear();
                this.mResolutions.addAll(other.mResolutions);
            } else {
                this.mResolutions = new ArrayList(other.mResolutions);
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        writeMargins(this.mMinMargins, parcel);
        writeMediaSizes(parcel);
        writeResolutions(parcel);
        parcel.writeInt(this.mColorModes);
        parcel.writeInt(this.mDuplexModes);
        writeDefaults(parcel);
    }

    public int hashCode() {
        int i = 0;
        int result = 31 * ((31 * ((31 * 1) + (this.mMinMargins == null ? 0 : this.mMinMargins.hashCode()))) + (this.mMediaSizes == null ? 0 : this.mMediaSizes.hashCode()));
        if (this.mResolutions != null) {
            i = this.mResolutions.hashCode();
        }
        return (31 * ((31 * ((31 * (result + i)) + this.mColorModes)) + this.mDuplexModes)) + Arrays.hashCode(this.mDefaults);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrinterCapabilitiesInfo other = (PrinterCapabilitiesInfo) obj;
        if (this.mMinMargins == null) {
            if (other.mMinMargins != null) {
                return false;
            }
        } else if (!this.mMinMargins.equals(other.mMinMargins)) {
            return false;
        }
        if (this.mMediaSizes == null) {
            if (other.mMediaSizes != null) {
                return false;
            }
        } else if (!this.mMediaSizes.equals(other.mMediaSizes)) {
            return false;
        }
        if (this.mResolutions == null) {
            if (other.mResolutions != null) {
                return false;
            }
        } else if (!this.mResolutions.equals(other.mResolutions)) {
            return false;
        }
        if (this.mColorModes == other.mColorModes && this.mDuplexModes == other.mDuplexModes && Arrays.equals(this.mDefaults, other.mDefaults)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "PrinterInfo{" + "minMargins=" + this.mMinMargins + ", mediaSizes=" + this.mMediaSizes + ", resolutions=" + this.mResolutions + ", colorModes=" + colorModesToString() + ", duplexModes=" + duplexModesToString() + "\"}";
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
        if (this.mMediaSizes == null) {
            parcel.writeInt(0);
            return;
        }
        int mediaSizeCount = this.mMediaSizes.size();
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
        if (this.mResolutions == null) {
            parcel.writeInt(0);
            return;
        }
        int resolutionCount = this.mResolutions.size();
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
        parcel.writeInt(defaultCount);
        for (int writeInt : this.mDefaults) {
            parcel.writeInt(writeInt);
        }
    }
}
