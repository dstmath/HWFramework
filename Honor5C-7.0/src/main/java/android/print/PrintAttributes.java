package android.print;

import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.hardware.Camera.Parameters;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Map;

public final class PrintAttributes implements Parcelable {
    public static final int COLOR_MODE_COLOR = 2;
    public static final int COLOR_MODE_MONOCHROME = 1;
    public static final Creator<PrintAttributes> CREATOR = null;
    public static final int DUPLEX_MODE_LONG_EDGE = 2;
    public static final int DUPLEX_MODE_NONE = 1;
    public static final int DUPLEX_MODE_SHORT_EDGE = 4;
    private static final int VALID_COLOR_MODES = 3;
    private static final int VALID_DUPLEX_MODES = 7;
    private int mColorMode;
    private int mDuplexMode;
    private MediaSize mMediaSize;
    private Margins mMinMargins;
    private Resolution mResolution;

    public static final class Builder {
        private final PrintAttributes mAttributes;

        public Builder() {
            this.mAttributes = new PrintAttributes();
        }

        public Builder setMediaSize(MediaSize mediaSize) {
            this.mAttributes.setMediaSize(mediaSize);
            return this;
        }

        public Builder setResolution(Resolution resolution) {
            this.mAttributes.setResolution(resolution);
            return this;
        }

        public Builder setMinMargins(Margins margins) {
            this.mAttributes.setMinMargins(margins);
            return this;
        }

        public Builder setColorMode(int colorMode) {
            this.mAttributes.setColorMode(colorMode);
            return this;
        }

        public Builder setDuplexMode(int duplexMode) {
            this.mAttributes.setDuplexMode(duplexMode);
            return this;
        }

        public PrintAttributes build() {
            return this.mAttributes;
        }
    }

    public static final class Margins {
        public static final Margins NO_MARGINS = null;
        private final int mBottomMils;
        private final int mLeftMils;
        private final int mRightMils;
        private final int mTopMils;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.print.PrintAttributes.Margins.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.print.PrintAttributes.Margins.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.print.PrintAttributes.Margins.<clinit>():void");
        }

        public Margins(int leftMils, int topMils, int rightMils, int bottomMils) {
            this.mTopMils = topMils;
            this.mLeftMils = leftMils;
            this.mRightMils = rightMils;
            this.mBottomMils = bottomMils;
        }

        public int getLeftMils() {
            return this.mLeftMils;
        }

        public int getTopMils() {
            return this.mTopMils;
        }

        public int getRightMils() {
            return this.mRightMils;
        }

        public int getBottomMils() {
            return this.mBottomMils;
        }

        void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mLeftMils);
            parcel.writeInt(this.mTopMils);
            parcel.writeInt(this.mRightMils);
            parcel.writeInt(this.mBottomMils);
        }

        static Margins createFromParcel(Parcel parcel) {
            return new Margins(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt());
        }

        public int hashCode() {
            return ((((((this.mBottomMils + 31) * 31) + this.mLeftMils) * 31) + this.mRightMils) * 31) + this.mTopMils;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Margins other = (Margins) obj;
            return this.mBottomMils == other.mBottomMils && this.mLeftMils == other.mLeftMils && this.mRightMils == other.mRightMils && this.mTopMils == other.mTopMils;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Margins{");
            builder.append("leftMils: ").append(this.mLeftMils);
            builder.append(", topMils: ").append(this.mTopMils);
            builder.append(", rightMils: ").append(this.mRightMils);
            builder.append(", bottomMils: ").append(this.mBottomMils);
            builder.append("}");
            return builder.toString();
        }
    }

    public static final class MediaSize {
        public static final MediaSize ISO_A0 = null;
        public static final MediaSize ISO_A1 = null;
        public static final MediaSize ISO_A10 = null;
        public static final MediaSize ISO_A2 = null;
        public static final MediaSize ISO_A3 = null;
        public static final MediaSize ISO_A4 = null;
        public static final MediaSize ISO_A5 = null;
        public static final MediaSize ISO_A6 = null;
        public static final MediaSize ISO_A7 = null;
        public static final MediaSize ISO_A8 = null;
        public static final MediaSize ISO_A9 = null;
        public static final MediaSize ISO_B0 = null;
        public static final MediaSize ISO_B1 = null;
        public static final MediaSize ISO_B10 = null;
        public static final MediaSize ISO_B2 = null;
        public static final MediaSize ISO_B3 = null;
        public static final MediaSize ISO_B4 = null;
        public static final MediaSize ISO_B5 = null;
        public static final MediaSize ISO_B6 = null;
        public static final MediaSize ISO_B7 = null;
        public static final MediaSize ISO_B8 = null;
        public static final MediaSize ISO_B9 = null;
        public static final MediaSize ISO_C0 = null;
        public static final MediaSize ISO_C1 = null;
        public static final MediaSize ISO_C10 = null;
        public static final MediaSize ISO_C2 = null;
        public static final MediaSize ISO_C3 = null;
        public static final MediaSize ISO_C4 = null;
        public static final MediaSize ISO_C5 = null;
        public static final MediaSize ISO_C6 = null;
        public static final MediaSize ISO_C7 = null;
        public static final MediaSize ISO_C8 = null;
        public static final MediaSize ISO_C9 = null;
        public static final MediaSize JIS_B0 = null;
        public static final MediaSize JIS_B1 = null;
        public static final MediaSize JIS_B10 = null;
        public static final MediaSize JIS_B2 = null;
        public static final MediaSize JIS_B3 = null;
        public static final MediaSize JIS_B4 = null;
        public static final MediaSize JIS_B5 = null;
        public static final MediaSize JIS_B6 = null;
        public static final MediaSize JIS_B7 = null;
        public static final MediaSize JIS_B8 = null;
        public static final MediaSize JIS_B9 = null;
        public static final MediaSize JIS_EXEC = null;
        public static final MediaSize JPN_CHOU2 = null;
        public static final MediaSize JPN_CHOU3 = null;
        public static final MediaSize JPN_CHOU4 = null;
        public static final MediaSize JPN_HAGAKI = null;
        public static final MediaSize JPN_KAHU = null;
        public static final MediaSize JPN_KAKU2 = null;
        public static final MediaSize JPN_OUFUKU = null;
        public static final MediaSize JPN_YOU4 = null;
        private static final String LOG_TAG = "MediaSize";
        public static final MediaSize NA_FOOLSCAP = null;
        public static final MediaSize NA_GOVT_LETTER = null;
        public static final MediaSize NA_INDEX_3X5 = null;
        public static final MediaSize NA_INDEX_4X6 = null;
        public static final MediaSize NA_INDEX_5X8 = null;
        public static final MediaSize NA_JUNIOR_LEGAL = null;
        public static final MediaSize NA_LEDGER = null;
        public static final MediaSize NA_LEGAL = null;
        public static final MediaSize NA_LETTER = null;
        public static final MediaSize NA_MONARCH = null;
        public static final MediaSize NA_QUARTO = null;
        public static final MediaSize NA_TABLOID = null;
        public static final MediaSize OM_DAI_PA_KAI = null;
        public static final MediaSize OM_JUURO_KU_KAI = null;
        public static final MediaSize OM_PA_KAI = null;
        public static final MediaSize PRC_1 = null;
        public static final MediaSize PRC_10 = null;
        public static final MediaSize PRC_16K = null;
        public static final MediaSize PRC_2 = null;
        public static final MediaSize PRC_3 = null;
        public static final MediaSize PRC_4 = null;
        public static final MediaSize PRC_5 = null;
        public static final MediaSize PRC_6 = null;
        public static final MediaSize PRC_7 = null;
        public static final MediaSize PRC_8 = null;
        public static final MediaSize PRC_9 = null;
        public static final MediaSize ROC_16K = null;
        public static final MediaSize ROC_8K = null;
        public static final MediaSize UNKNOWN_LANDSCAPE = null;
        public static final MediaSize UNKNOWN_PORTRAIT = null;
        private static final Map<String, MediaSize> sIdToMediaSizeMap = null;
        private final int mHeightMils;
        private final String mId;
        public final String mLabel;
        public final int mLabelResId;
        public final String mPackageName;
        private final int mWidthMils;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.print.PrintAttributes.MediaSize.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.print.PrintAttributes.MediaSize.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.print.PrintAttributes.MediaSize.<clinit>():void");
        }

        public MediaSize(String id, String packageName, int labelResId, int widthMils, int heightMils) {
            this(id, null, packageName, widthMils, heightMils, labelResId);
            sIdToMediaSizeMap.put(this.mId, this);
        }

        public MediaSize(String id, String label, int widthMils, int heightMils) {
            this(id, label, null, widthMils, heightMils, 0);
        }

        public static ArraySet<MediaSize> getAllPredefinedSizes() {
            ArraySet<MediaSize> definedMediaSizes = new ArraySet(sIdToMediaSizeMap.values());
            definedMediaSizes.remove(UNKNOWN_PORTRAIT);
            definedMediaSizes.remove(UNKNOWN_LANDSCAPE);
            return definedMediaSizes;
        }

        public MediaSize(String id, String label, String packageName, int widthMils, int heightMils, int labelResId) {
            boolean z;
            boolean z2 = true;
            this.mPackageName = packageName;
            this.mId = (String) Preconditions.checkStringNotEmpty(id, "id cannot be empty.");
            this.mLabelResId = labelResId;
            this.mWidthMils = Preconditions.checkArgumentPositive(widthMils, "widthMils cannot be less than or equal to zero.");
            this.mHeightMils = Preconditions.checkArgumentPositive(heightMils, "heightMils cannot be less than or equal to zero.");
            this.mLabel = label;
            boolean z3 = !TextUtils.isEmpty(label);
            if (TextUtils.isEmpty(packageName) || labelResId == 0) {
                z = false;
            } else {
                z = true;
            }
            if (z3 == z) {
                z2 = false;
            }
            Preconditions.checkArgument(z2, "label cannot be empty.");
        }

        public String getId() {
            return this.mId;
        }

        public String getLabel(PackageManager packageManager) {
            if (!TextUtils.isEmpty(this.mPackageName) && this.mLabelResId > 0) {
                try {
                    return packageManager.getResourcesForApplication(this.mPackageName).getString(this.mLabelResId);
                } catch (NotFoundException e) {
                    Log.w(LOG_TAG, "Could not load resouce" + this.mLabelResId + " from package " + this.mPackageName);
                }
            }
            return this.mLabel;
        }

        public int getWidthMils() {
            return this.mWidthMils;
        }

        public int getHeightMils() {
            return this.mHeightMils;
        }

        public boolean isPortrait() {
            return this.mHeightMils >= this.mWidthMils;
        }

        public MediaSize asPortrait() {
            if (isPortrait()) {
                return this;
            }
            return new MediaSize(this.mId, this.mLabel, this.mPackageName, Math.min(this.mWidthMils, this.mHeightMils), Math.max(this.mWidthMils, this.mHeightMils), this.mLabelResId);
        }

        public MediaSize asLandscape() {
            if (isPortrait()) {
                return new MediaSize(this.mId, this.mLabel, this.mPackageName, Math.max(this.mWidthMils, this.mHeightMils), Math.min(this.mWidthMils, this.mHeightMils), this.mLabelResId);
            }
            return this;
        }

        void writeToParcel(Parcel parcel) {
            parcel.writeString(this.mId);
            parcel.writeString(this.mLabel);
            parcel.writeString(this.mPackageName);
            parcel.writeInt(this.mWidthMils);
            parcel.writeInt(this.mHeightMils);
            parcel.writeInt(this.mLabelResId);
        }

        static MediaSize createFromParcel(Parcel parcel) {
            return new MediaSize(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readInt(), parcel.readInt());
        }

        public int hashCode() {
            return ((this.mWidthMils + 31) * 31) + this.mHeightMils;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MediaSize other = (MediaSize) obj;
            return this.mWidthMils == other.mWidthMils && this.mHeightMils == other.mHeightMils;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("MediaSize{");
            builder.append("id: ").append(this.mId);
            builder.append(", label: ").append(this.mLabel);
            builder.append(", packageName: ").append(this.mPackageName);
            builder.append(", heightMils: ").append(this.mHeightMils);
            builder.append(", widthMils: ").append(this.mWidthMils);
            builder.append(", labelResId: ").append(this.mLabelResId);
            builder.append("}");
            return builder.toString();
        }

        public static MediaSize getStandardMediaSizeById(String id) {
            return (MediaSize) sIdToMediaSizeMap.get(id);
        }
    }

    public static final class Resolution {
        private final int mHorizontalDpi;
        private final String mId;
        private final String mLabel;
        private final int mVerticalDpi;

        public Resolution(String id, String label, int horizontalDpi, int verticalDpi) {
            if (TextUtils.isEmpty(id)) {
                throw new IllegalArgumentException("id cannot be empty.");
            } else if (TextUtils.isEmpty(label)) {
                throw new IllegalArgumentException("label cannot be empty.");
            } else if (horizontalDpi <= 0) {
                throw new IllegalArgumentException("horizontalDpi cannot be less than or equal to zero.");
            } else if (verticalDpi <= 0) {
                throw new IllegalArgumentException("verticalDpi cannot be less than or equal to zero.");
            } else {
                this.mId = id;
                this.mLabel = label;
                this.mHorizontalDpi = horizontalDpi;
                this.mVerticalDpi = verticalDpi;
            }
        }

        public String getId() {
            return this.mId;
        }

        public String getLabel() {
            return this.mLabel;
        }

        public int getHorizontalDpi() {
            return this.mHorizontalDpi;
        }

        public int getVerticalDpi() {
            return this.mVerticalDpi;
        }

        void writeToParcel(Parcel parcel) {
            parcel.writeString(this.mId);
            parcel.writeString(this.mLabel);
            parcel.writeInt(this.mHorizontalDpi);
            parcel.writeInt(this.mVerticalDpi);
        }

        static Resolution createFromParcel(Parcel parcel) {
            return new Resolution(parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readInt());
        }

        public int hashCode() {
            return ((this.mHorizontalDpi + 31) * 31) + this.mVerticalDpi;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Resolution other = (Resolution) obj;
            return this.mHorizontalDpi == other.mHorizontalDpi && this.mVerticalDpi == other.mVerticalDpi;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Resolution{");
            builder.append("id: ").append(this.mId);
            builder.append(", label: ").append(this.mLabel);
            builder.append(", horizontalDpi: ").append(this.mHorizontalDpi);
            builder.append(", verticalDpi: ").append(this.mVerticalDpi);
            builder.append("}");
            return builder.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.print.PrintAttributes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.print.PrintAttributes.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.print.PrintAttributes.<clinit>():void");
    }

    /* synthetic */ PrintAttributes(Parcel parcel, PrintAttributes printAttributes) {
        this(parcel);
    }

    PrintAttributes() {
    }

    private PrintAttributes(Parcel parcel) {
        MediaSize createFromParcel;
        Resolution createFromParcel2;
        Margins margins = null;
        if (parcel.readInt() == DUPLEX_MODE_NONE) {
            createFromParcel = MediaSize.createFromParcel(parcel);
        } else {
            createFromParcel = null;
        }
        this.mMediaSize = createFromParcel;
        if (parcel.readInt() == DUPLEX_MODE_NONE) {
            createFromParcel2 = Resolution.createFromParcel(parcel);
        } else {
            createFromParcel2 = null;
        }
        this.mResolution = createFromParcel2;
        if (parcel.readInt() == DUPLEX_MODE_NONE) {
            margins = Margins.createFromParcel(parcel);
        }
        this.mMinMargins = margins;
        this.mColorMode = parcel.readInt();
        if (this.mColorMode != 0) {
            enforceValidColorMode(this.mColorMode);
        }
        this.mDuplexMode = parcel.readInt();
        if (this.mDuplexMode != 0) {
            enforceValidDuplexMode(this.mDuplexMode);
        }
    }

    public MediaSize getMediaSize() {
        return this.mMediaSize;
    }

    public void setMediaSize(MediaSize mediaSize) {
        this.mMediaSize = mediaSize;
    }

    public Resolution getResolution() {
        return this.mResolution;
    }

    public void setResolution(Resolution resolution) {
        this.mResolution = resolution;
    }

    public Margins getMinMargins() {
        return this.mMinMargins;
    }

    public void setMinMargins(Margins margins) {
        this.mMinMargins = margins;
    }

    public int getColorMode() {
        return this.mColorMode;
    }

    public void setColorMode(int colorMode) {
        enforceValidColorMode(colorMode);
        this.mColorMode = colorMode;
    }

    public boolean isPortrait() {
        return this.mMediaSize.isPortrait();
    }

    public int getDuplexMode() {
        return this.mDuplexMode;
    }

    public void setDuplexMode(int duplexMode) {
        enforceValidDuplexMode(duplexMode);
        this.mDuplexMode = duplexMode;
    }

    public PrintAttributes asPortrait() {
        if (isPortrait()) {
            return this;
        }
        PrintAttributes attributes = new PrintAttributes();
        attributes.setMediaSize(getMediaSize().asPortrait());
        Resolution oldResolution = getResolution();
        attributes.setResolution(new Resolution(oldResolution.getId(), oldResolution.getLabel(), oldResolution.getVerticalDpi(), oldResolution.getHorizontalDpi()));
        attributes.setMinMargins(getMinMargins());
        attributes.setColorMode(getColorMode());
        attributes.setDuplexMode(getDuplexMode());
        return attributes;
    }

    public PrintAttributes asLandscape() {
        if (!isPortrait()) {
            return this;
        }
        PrintAttributes attributes = new PrintAttributes();
        attributes.setMediaSize(getMediaSize().asLandscape());
        Resolution oldResolution = getResolution();
        attributes.setResolution(new Resolution(oldResolution.getId(), oldResolution.getLabel(), oldResolution.getVerticalDpi(), oldResolution.getHorizontalDpi()));
        attributes.setMinMargins(getMinMargins());
        attributes.setColorMode(getColorMode());
        attributes.setDuplexMode(getDuplexMode());
        return attributes;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        if (this.mMediaSize != null) {
            parcel.writeInt(DUPLEX_MODE_NONE);
            this.mMediaSize.writeToParcel(parcel);
        } else {
            parcel.writeInt(0);
        }
        if (this.mResolution != null) {
            parcel.writeInt(DUPLEX_MODE_NONE);
            this.mResolution.writeToParcel(parcel);
        } else {
            parcel.writeInt(0);
        }
        if (this.mMinMargins != null) {
            parcel.writeInt(DUPLEX_MODE_NONE);
            this.mMinMargins.writeToParcel(parcel);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeInt(this.mColorMode);
        parcel.writeInt(this.mDuplexMode);
    }

    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((this.mColorMode + 31) * 31) + this.mDuplexMode) * 31) + (this.mMinMargins == null ? 0 : this.mMinMargins.hashCode())) * 31) + (this.mMediaSize == null ? 0 : this.mMediaSize.hashCode())) * 31;
        if (this.mResolution != null) {
            i = this.mResolution.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrintAttributes other = (PrintAttributes) obj;
        if (this.mColorMode != other.mColorMode || this.mDuplexMode != other.mDuplexMode) {
            return false;
        }
        if (this.mMinMargins == null) {
            if (other.mMinMargins != null) {
                return false;
            }
        } else if (!this.mMinMargins.equals(other.mMinMargins)) {
            return false;
        }
        if (this.mMediaSize == null) {
            if (other.mMediaSize != null) {
                return false;
            }
        } else if (!this.mMediaSize.equals(other.mMediaSize)) {
            return false;
        }
        if (this.mResolution == null) {
            if (other.mResolution != null) {
                return false;
            }
        } else if (!this.mResolution.equals(other.mResolution)) {
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PrintAttributes{");
        builder.append("mediaSize: ").append(this.mMediaSize);
        if (this.mMediaSize != null) {
            builder.append(", orientation: ").append(this.mMediaSize.isPortrait() ? Parameters.SCENE_MODE_PORTRAIT : Parameters.SCENE_MODE_LANDSCAPE);
        } else {
            builder.append(", orientation: ").append("null");
        }
        builder.append(", resolution: ").append(this.mResolution);
        builder.append(", minMargins: ").append(this.mMinMargins);
        builder.append(", colorMode: ").append(colorModeToString(this.mColorMode));
        builder.append(", duplexMode: ").append(duplexModeToString(this.mDuplexMode));
        builder.append("}");
        return builder.toString();
    }

    public void clear() {
        this.mMediaSize = null;
        this.mResolution = null;
        this.mMinMargins = null;
        this.mColorMode = 0;
        this.mDuplexMode = 0;
    }

    public void copyFrom(PrintAttributes other) {
        this.mMediaSize = other.mMediaSize;
        this.mResolution = other.mResolution;
        this.mMinMargins = other.mMinMargins;
        this.mColorMode = other.mColorMode;
        this.mDuplexMode = other.mDuplexMode;
    }

    static String colorModeToString(int colorMode) {
        switch (colorMode) {
            case DUPLEX_MODE_NONE /*1*/:
                return "COLOR_MODE_MONOCHROME";
            case DUPLEX_MODE_LONG_EDGE /*2*/:
                return "COLOR_MODE_COLOR";
            default:
                return "COLOR_MODE_UNKNOWN";
        }
    }

    static String duplexModeToString(int duplexMode) {
        switch (duplexMode) {
            case DUPLEX_MODE_NONE /*1*/:
                return "DUPLEX_MODE_NONE";
            case DUPLEX_MODE_LONG_EDGE /*2*/:
                return "DUPLEX_MODE_LONG_EDGE";
            case DUPLEX_MODE_SHORT_EDGE /*4*/:
                return "DUPLEX_MODE_SHORT_EDGE";
            default:
                return "DUPLEX_MODE_UNKNOWN";
        }
    }

    static void enforceValidColorMode(int colorMode) {
        if ((colorMode & VALID_COLOR_MODES) == 0 || Integer.bitCount(colorMode) != DUPLEX_MODE_NONE) {
            throw new IllegalArgumentException("invalid color mode: " + colorMode);
        }
    }

    static void enforceValidDuplexMode(int duplexMode) {
        if ((duplexMode & VALID_DUPLEX_MODES) == 0 || Integer.bitCount(duplexMode) != DUPLEX_MODE_NONE) {
            throw new IllegalArgumentException("invalid duplex mode: " + duplexMode);
        }
    }
}
