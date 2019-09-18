package android.print;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.rms.iaware.AwareNRTConstant;
import android.telephony.PreciseDisconnectCause;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

public final class PrintAttributes implements Parcelable {
    public static final int COLOR_MODE_COLOR = 2;
    public static final int COLOR_MODE_MONOCHROME = 1;
    public static final Parcelable.Creator<PrintAttributes> CREATOR = new Parcelable.Creator<PrintAttributes>() {
        public PrintAttributes createFromParcel(Parcel parcel) {
            return new PrintAttributes(parcel);
        }

        public PrintAttributes[] newArray(int size) {
            return new PrintAttributes[size];
        }
    };
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
        private final PrintAttributes mAttributes = new PrintAttributes();

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

    @Retention(RetentionPolicy.SOURCE)
    @interface ColorMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface DuplexMode {
    }

    public static final class Margins {
        public static final Margins NO_MARGINS = new Margins(0, 0, 0, 0);
        private final int mBottomMils;
        private final int mLeftMils;
        private final int mRightMils;
        private final int mTopMils;

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

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel parcel) {
            parcel.writeInt(this.mLeftMils);
            parcel.writeInt(this.mTopMils);
            parcel.writeInt(this.mRightMils);
            parcel.writeInt(this.mBottomMils);
        }

        static Margins createFromParcel(Parcel parcel) {
            return new Margins(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt());
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * ((31 * 1) + this.mBottomMils)) + this.mLeftMils)) + this.mRightMils)) + this.mTopMils;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Margins other = (Margins) obj;
            if (this.mBottomMils == other.mBottomMils && this.mLeftMils == other.mLeftMils && this.mRightMils == other.mRightMils && this.mTopMils == other.mTopMils) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "Margins{" + "leftMils: " + this.mLeftMils + ", topMils: " + this.mTopMils + ", rightMils: " + this.mRightMils + ", bottomMils: " + this.mBottomMils + "}";
        }
    }

    public static final class MediaSize {
        public static final MediaSize ISO_A0;
        public static final MediaSize ISO_A1;
        public static final MediaSize ISO_A10;
        public static final MediaSize ISO_A2;
        public static final MediaSize ISO_A3;
        public static final MediaSize ISO_A4;
        public static final MediaSize ISO_A5;
        public static final MediaSize ISO_A6;
        public static final MediaSize ISO_A7;
        public static final MediaSize ISO_A8;
        public static final MediaSize ISO_A9;
        public static final MediaSize ISO_B0;
        public static final MediaSize ISO_B1;
        public static final MediaSize ISO_B10;
        public static final MediaSize ISO_B2;
        public static final MediaSize ISO_B3;
        public static final MediaSize ISO_B4;
        public static final MediaSize ISO_B5;
        public static final MediaSize ISO_B6;
        public static final MediaSize ISO_B7;
        public static final MediaSize ISO_B8;
        public static final MediaSize ISO_B9;
        public static final MediaSize ISO_C0;
        public static final MediaSize ISO_C1;
        public static final MediaSize ISO_C10;
        public static final MediaSize ISO_C2;
        public static final MediaSize ISO_C3;
        public static final MediaSize ISO_C4;
        public static final MediaSize ISO_C5;
        public static final MediaSize ISO_C6;
        public static final MediaSize ISO_C7;
        public static final MediaSize ISO_C8;
        public static final MediaSize ISO_C9;
        public static final MediaSize JIS_B0;
        public static final MediaSize JIS_B1;
        public static final MediaSize JIS_B10;
        public static final MediaSize JIS_B2;
        public static final MediaSize JIS_B3;
        public static final MediaSize JIS_B4;
        public static final MediaSize JIS_B5;
        public static final MediaSize JIS_B6;
        public static final MediaSize JIS_B7;
        public static final MediaSize JIS_B8;
        public static final MediaSize JIS_B9;
        public static final MediaSize JIS_EXEC;
        public static final MediaSize JPN_CHOU2;
        public static final MediaSize JPN_CHOU3;
        public static final MediaSize JPN_CHOU4;
        public static final MediaSize JPN_HAGAKI;
        public static final MediaSize JPN_KAHU;
        public static final MediaSize JPN_KAKU2;
        public static final MediaSize JPN_OUFUKU;
        public static final MediaSize JPN_YOU4;
        private static final String LOG_TAG = "MediaSize";
        public static final MediaSize NA_FOOLSCAP;
        public static final MediaSize NA_GOVT_LETTER;
        public static final MediaSize NA_INDEX_3X5;
        public static final MediaSize NA_INDEX_4X6;
        public static final MediaSize NA_INDEX_5X8;
        public static final MediaSize NA_JUNIOR_LEGAL;
        public static final MediaSize NA_LEDGER;
        public static final MediaSize NA_LEGAL;
        public static final MediaSize NA_LETTER;
        public static final MediaSize NA_MONARCH;
        public static final MediaSize NA_QUARTO;
        public static final MediaSize NA_TABLOID;
        public static final MediaSize OM_DAI_PA_KAI;
        public static final MediaSize OM_JUURO_KU_KAI;
        public static final MediaSize OM_PA_KAI;
        public static final MediaSize PRC_1;
        public static final MediaSize PRC_10;
        public static final MediaSize PRC_16K;
        public static final MediaSize PRC_2;
        public static final MediaSize PRC_3;
        public static final MediaSize PRC_4;
        public static final MediaSize PRC_5;
        public static final MediaSize PRC_6;
        public static final MediaSize PRC_7;
        public static final MediaSize PRC_8;
        public static final MediaSize PRC_9;
        public static final MediaSize ROC_16K;
        public static final MediaSize ROC_8K;
        public static final MediaSize UNKNOWN_LANDSCAPE;
        public static final MediaSize UNKNOWN_PORTRAIT;
        private static final Map<String, MediaSize> sIdToMediaSizeMap = new ArrayMap();
        private final int mHeightMils;
        private final String mId;
        public final String mLabel;
        public final int mLabelResId;
        public final String mPackageName;
        private final int mWidthMils;

        static {
            MediaSize mediaSize = new MediaSize("UNKNOWN_PORTRAIT", "android", 17040504, 1, Integer.MAX_VALUE);
            UNKNOWN_PORTRAIT = mediaSize;
            MediaSize mediaSize2 = new MediaSize("UNKNOWN_LANDSCAPE", "android", 17040503, Integer.MAX_VALUE, 1);
            UNKNOWN_LANDSCAPE = mediaSize2;
            MediaSize mediaSize3 = new MediaSize("ISO_A0", "android", 17040438, 33110, 46810);
            ISO_A0 = mediaSize3;
            MediaSize mediaSize4 = new MediaSize("ISO_A1", "android", 17040439, 23390, 33110);
            ISO_A1 = mediaSize4;
            MediaSize mediaSize5 = new MediaSize("ISO_A2", "android", 17040441, 16540, 23390);
            ISO_A2 = mediaSize5;
            MediaSize mediaSize6 = new MediaSize("ISO_A3", "android", 17040442, 11690, 16540);
            ISO_A3 = mediaSize6;
            MediaSize mediaSize7 = new MediaSize("ISO_A4", "android", 17040443, 8270, 11690);
            ISO_A4 = mediaSize7;
            MediaSize mediaSize8 = new MediaSize("ISO_A5", "android", 17040444, 5830, 8270);
            ISO_A5 = mediaSize8;
            MediaSize mediaSize9 = new MediaSize("ISO_A6", "android", 17040445, 4130, 5830);
            ISO_A6 = mediaSize9;
            MediaSize mediaSize10 = new MediaSize("ISO_A7", "android", 17040446, 2910, 4130);
            ISO_A7 = mediaSize10;
            MediaSize mediaSize11 = new MediaSize("ISO_A8", "android", 17040447, AwareNRTConstant.FIRST_THERMAL_CTRL_EVENT_ID, 2910);
            ISO_A8 = mediaSize11;
            MediaSize mediaSize12 = new MediaSize("ISO_A9", "android", 17040448, 1460, AwareNRTConstant.FIRST_THERMAL_CTRL_EVENT_ID);
            ISO_A9 = mediaSize12;
            MediaSize mediaSize13 = new MediaSize("ISO_A10", "android", 17040440, 1020, 1460);
            ISO_A10 = mediaSize13;
            MediaSize mediaSize14 = new MediaSize("ISO_B0", "android", 17040449, 39370, 55670);
            ISO_B0 = mediaSize14;
            MediaSize mediaSize15 = new MediaSize("ISO_B1", "android", 17040450, 27830, 39370);
            ISO_B1 = mediaSize15;
            MediaSize mediaSize16 = new MediaSize("ISO_B2", "android", 17040452, 19690, 27830);
            ISO_B2 = mediaSize16;
            MediaSize mediaSize17 = new MediaSize("ISO_B3", "android", 17040453, 13900, 19690);
            ISO_B3 = mediaSize17;
            MediaSize mediaSize18 = new MediaSize("ISO_B4", "android", 17040454, 9840, 13900);
            ISO_B4 = mediaSize18;
            MediaSize mediaSize19 = new MediaSize("ISO_B5", "android", 17040455, 6930, 9840);
            ISO_B5 = mediaSize19;
            MediaSize mediaSize20 = new MediaSize("ISO_B6", "android", 17040456, 4920, 6930);
            ISO_B6 = mediaSize20;
            MediaSize mediaSize21 = new MediaSize("ISO_B7", "android", 17040457, 3460, 4920);
            ISO_B7 = mediaSize21;
            MediaSize mediaSize22 = new MediaSize("ISO_B8", "android", 17040458, 2440, 3460);
            ISO_B8 = mediaSize22;
            MediaSize mediaSize23 = new MediaSize("ISO_B9", "android", 17040459, 1730, 2440);
            ISO_B9 = mediaSize23;
            MediaSize mediaSize24 = new MediaSize("ISO_B10", "android", 17040451, PreciseDisconnectCause.LOCAL_HO_NOT_FEASIBLE, 1730);
            ISO_B10 = mediaSize24;
            MediaSize mediaSize25 = new MediaSize("ISO_C0", "android", 17040460, 36100, 51060);
            ISO_C0 = mediaSize25;
            MediaSize mediaSize26 = new MediaSize("ISO_C1", "android", 17040461, 25510, 36100);
            ISO_C1 = mediaSize26;
            MediaSize mediaSize27 = new MediaSize("ISO_C2", "android", 17040463, 18030, 25510);
            ISO_C2 = mediaSize27;
            MediaSize mediaSize28 = new MediaSize("ISO_C3", "android", 17040464, 12760, 18030);
            ISO_C3 = mediaSize28;
            MediaSize mediaSize29 = new MediaSize("ISO_C4", "android", 17040465, 9020, 12760);
            ISO_C4 = mediaSize29;
            MediaSize mediaSize30 = new MediaSize("ISO_C5", "android", 17040466, 6380, 9020);
            ISO_C5 = mediaSize30;
            MediaSize mediaSize31 = new MediaSize("ISO_C6", "android", 17040467, 4490, 6380);
            ISO_C6 = mediaSize31;
            MediaSize mediaSize32 = new MediaSize("ISO_C7", "android", 17040468, 3190, 4490);
            ISO_C7 = mediaSize32;
            MediaSize mediaSize33 = new MediaSize("ISO_C8", "android", 17040469, 2240, 3190);
            ISO_C8 = mediaSize33;
            MediaSize mediaSize34 = new MediaSize("ISO_C9", "android", 17040470, 1570, 2240);
            ISO_C9 = mediaSize34;
            MediaSize mediaSize35 = new MediaSize("ISO_C10", "android", 17040462, 1100, 1570);
            ISO_C10 = mediaSize35;
            MediaSize mediaSize36 = new MediaSize("NA_LETTER", "android", 17040499, 8500, 11000);
            NA_LETTER = mediaSize36;
            MediaSize mediaSize37 = new MediaSize("NA_GOVT_LETTER", "android", 17040492, 8000, 10500);
            NA_GOVT_LETTER = mediaSize37;
            MediaSize mediaSize38 = new MediaSize("NA_LEGAL", "android", 17040498, 8500, 14000);
            NA_LEGAL = mediaSize38;
            MediaSize mediaSize39 = new MediaSize("NA_JUNIOR_LEGAL", "android", 17040496, 8000, 5000);
            NA_JUNIOR_LEGAL = mediaSize39;
            MediaSize mediaSize40 = new MediaSize("NA_LEDGER", "android", 17040497, 17000, 11000);
            NA_LEDGER = mediaSize40;
            MediaSize mediaSize41 = new MediaSize("NA_TABLOID", "android", 17040502, 11000, 17000);
            NA_TABLOID = mediaSize41;
            MediaSize mediaSize42 = new MediaSize("NA_INDEX_3X5", "android", 17040493, 3000, 5000);
            NA_INDEX_3X5 = mediaSize42;
            MediaSize mediaSize43 = new MediaSize("NA_INDEX_4X6", "android", 17040494, 4000, 6000);
            NA_INDEX_4X6 = mediaSize43;
            MediaSize mediaSize44 = new MediaSize("NA_INDEX_5X8", "android", 17040495, 5000, 8000);
            NA_INDEX_5X8 = mediaSize44;
            MediaSize mediaSize45 = new MediaSize("NA_MONARCH", "android", 17040500, 7250, 10500);
            NA_MONARCH = mediaSize45;
            MediaSize mediaSize46 = new MediaSize("NA_QUARTO", "android", 17040501, 8000, 10000);
            NA_QUARTO = mediaSize46;
            MediaSize mediaSize47 = new MediaSize("NA_FOOLSCAP", "android", 17040491, 8000, 13000);
            NA_FOOLSCAP = mediaSize47;
            MediaSize mediaSize48 = new MediaSize("ROC_8K", "android", 17040437, 10629, 15354);
            ROC_8K = mediaSize48;
            MediaSize mediaSize49 = new MediaSize("ROC_16K", "android", 17040436, 7677, 10629);
            ROC_16K = mediaSize49;
            MediaSize mediaSize50 = new MediaSize("PRC_1", "android", 17040425, 4015, 6496);
            PRC_1 = mediaSize50;
            MediaSize mediaSize51 = new MediaSize("PRC_2", "android", 17040428, 4015, 6929);
            PRC_2 = mediaSize51;
            MediaSize mediaSize52 = new MediaSize("PRC_3", "android", 17040429, 4921, 6929);
            PRC_3 = mediaSize52;
            MediaSize mediaSize53 = new MediaSize("PRC_4", "android", 17040430, 4330, 8189);
            PRC_4 = mediaSize53;
            MediaSize mediaSize54 = new MediaSize("PRC_5", "android", 17040431, 4330, 8661);
            PRC_5 = mediaSize54;
            MediaSize mediaSize55 = new MediaSize("PRC_6", "android", 17040432, 4724, 12599);
            PRC_6 = mediaSize55;
            MediaSize mediaSize56 = new MediaSize("PRC_7", "android", 17040433, 6299, 9055);
            PRC_7 = mediaSize56;
            MediaSize mediaSize57 = new MediaSize("PRC_8", "android", 17040434, 4724, 12165);
            PRC_8 = mediaSize57;
            MediaSize mediaSize58 = new MediaSize("PRC_9", "android", 17040435, 9016, 12756);
            PRC_9 = mediaSize58;
            MediaSize mediaSize59 = new MediaSize("PRC_10", "android", 17040426, 12756, 18032);
            PRC_10 = mediaSize59;
            MediaSize mediaSize60 = new MediaSize("PRC_16K", "android", 17040427, 5749, 8465);
            PRC_16K = mediaSize60;
            MediaSize mediaSize61 = new MediaSize("OM_PA_KAI", "android", 17040424, 10512, 15315);
            OM_PA_KAI = mediaSize61;
            MediaSize mediaSize62 = new MediaSize("OM_DAI_PA_KAI", "android", 17040422, 10827, 15551);
            OM_DAI_PA_KAI = mediaSize62;
            MediaSize mediaSize63 = new MediaSize("OM_JUURO_KU_KAI", "android", 17040423, 7796, 10827);
            OM_JUURO_KU_KAI = mediaSize63;
            MediaSize mediaSize64 = new MediaSize("JIS_B10", "android", 17040477, 1259, 1772);
            JIS_B10 = mediaSize64;
            MediaSize mediaSize65 = new MediaSize("JIS_B9", "android", 17040485, 1772, 2520);
            JIS_B9 = mediaSize65;
            MediaSize mediaSize66 = new MediaSize("JIS_B8", "android", 17040484, 2520, 3583);
            JIS_B8 = mediaSize66;
            MediaSize mediaSize67 = new MediaSize("JIS_B7", "android", 17040483, 3583, 5049);
            JIS_B7 = mediaSize67;
            MediaSize mediaSize68 = new MediaSize("JIS_B6", "android", 17040482, 5049, 7165);
            JIS_B6 = mediaSize68;
            MediaSize mediaSize69 = new MediaSize("JIS_B5", "android", 17040481, 7165, 10118);
            JIS_B5 = mediaSize69;
            MediaSize mediaSize70 = new MediaSize("JIS_B4", "android", 17040480, 10118, 14331);
            JIS_B4 = mediaSize70;
            MediaSize mediaSize71 = new MediaSize("JIS_B3", "android", 17040479, 14331, 20276);
            JIS_B3 = mediaSize71;
            MediaSize mediaSize72 = new MediaSize("JIS_B2", "android", 17040478, 20276, 28661);
            JIS_B2 = mediaSize72;
            MediaSize mediaSize73 = new MediaSize("JIS_B1", "android", 17040476, 28661, 40551);
            JIS_B1 = mediaSize73;
            MediaSize mediaSize74 = new MediaSize("JIS_B0", "android", 17040475, 40551, 57323);
            JIS_B0 = mediaSize74;
            MediaSize mediaSize75 = new MediaSize("JIS_EXEC", "android", 17040486, 8504, 12992);
            JIS_EXEC = mediaSize75;
            MediaSize mediaSize76 = new MediaSize("JPN_CHOU4", "android", 17040473, 3543, 8071);
            JPN_CHOU4 = mediaSize76;
            MediaSize mediaSize77 = new MediaSize("JPN_CHOU3", "android", 17040472, 4724, 9252);
            JPN_CHOU3 = mediaSize77;
            MediaSize mediaSize78 = new MediaSize("JPN_CHOU2", "android", 17040471, 4374, 5748);
            JPN_CHOU2 = mediaSize78;
            MediaSize mediaSize79 = new MediaSize("JPN_HAGAKI", "android", 17040474, 3937, 5827);
            JPN_HAGAKI = mediaSize79;
            MediaSize mediaSize80 = new MediaSize("JPN_OUFUKU", "android", 17040489, 5827, 7874);
            JPN_OUFUKU = mediaSize80;
            MediaSize mediaSize81 = new MediaSize("JPN_KAHU", "android", 17040487, 9449, 12681);
            JPN_KAHU = mediaSize81;
            MediaSize mediaSize82 = new MediaSize("JPN_KAKU2", "android", 17040488, 9449, 13071);
            JPN_KAKU2 = mediaSize82;
            MediaSize mediaSize83 = new MediaSize("JPN_YOU4", "android", 17040490, 4134, 9252);
            JPN_YOU4 = mediaSize83;
        }

        public MediaSize(String id, String packageName, int labelResId, int widthMils, int heightMils) {
            this(id, null, packageName, widthMils, heightMils, labelResId);
            sIdToMediaSizeMap.put(this.mId, this);
        }

        public MediaSize(String id, String label, int widthMils, int heightMils) {
            this(id, label, null, widthMils, heightMils, 0);
        }

        public static ArraySet<MediaSize> getAllPredefinedSizes() {
            ArraySet<MediaSize> definedMediaSizes = new ArraySet<>(sIdToMediaSizeMap.values());
            definedMediaSizes.remove(UNKNOWN_PORTRAIT);
            definedMediaSizes.remove(UNKNOWN_LANDSCAPE);
            return definedMediaSizes;
        }

        public MediaSize(String id, String label, String packageName, int widthMils, int heightMils, int labelResId) {
            this.mPackageName = packageName;
            this.mId = (String) Preconditions.checkStringNotEmpty(id, "id cannot be empty.");
            this.mLabelResId = labelResId;
            this.mWidthMils = Preconditions.checkArgumentPositive(widthMils, "widthMils cannot be less than or equal to zero.");
            this.mHeightMils = Preconditions.checkArgumentPositive(heightMils, "heightMils cannot be less than or equal to zero.");
            this.mLabel = label;
            Preconditions.checkArgument((TextUtils.isEmpty(label) ^ true) == (!TextUtils.isEmpty(packageName) && labelResId != 0) ? false : true, "label cannot be empty.");
        }

        public String getId() {
            return this.mId;
        }

        public String getLabel(PackageManager packageManager) {
            if (!TextUtils.isEmpty(this.mPackageName) && this.mLabelResId > 0) {
                try {
                    return packageManager.getResourcesForApplication(this.mPackageName).getString(this.mLabelResId);
                } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
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
            MediaSize mediaSize = new MediaSize(this.mId, this.mLabel, this.mPackageName, Math.min(this.mWidthMils, this.mHeightMils), Math.max(this.mWidthMils, this.mHeightMils), this.mLabelResId);
            return mediaSize;
        }

        public MediaSize asLandscape() {
            if (!isPortrait()) {
                return this;
            }
            MediaSize mediaSize = new MediaSize(this.mId, this.mLabel, this.mPackageName, Math.max(this.mWidthMils, this.mHeightMils), Math.min(this.mWidthMils, this.mHeightMils), this.mLabelResId);
            return mediaSize;
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel parcel) {
            parcel.writeString(this.mId);
            parcel.writeString(this.mLabel);
            parcel.writeString(this.mPackageName);
            parcel.writeInt(this.mWidthMils);
            parcel.writeInt(this.mHeightMils);
            parcel.writeInt(this.mLabelResId);
        }

        static MediaSize createFromParcel(Parcel parcel) {
            MediaSize mediaSize = new MediaSize(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readInt(), parcel.readInt());
            return mediaSize;
        }

        public int hashCode() {
            return (31 * ((31 * 1) + this.mWidthMils)) + this.mHeightMils;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MediaSize other = (MediaSize) obj;
            if (this.mWidthMils == other.mWidthMils && this.mHeightMils == other.mHeightMils) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "MediaSize{" + "id: " + this.mId + ", label: " + this.mLabel + ", packageName: " + this.mPackageName + ", heightMils: " + this.mHeightMils + ", widthMils: " + this.mWidthMils + ", labelResId: " + this.mLabelResId + "}";
        }

        public static MediaSize getStandardMediaSizeById(String id) {
            return sIdToMediaSizeMap.get(id);
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
            } else if (verticalDpi > 0) {
                this.mId = id;
                this.mLabel = label;
                this.mHorizontalDpi = horizontalDpi;
                this.mVerticalDpi = verticalDpi;
            } else {
                throw new IllegalArgumentException("verticalDpi cannot be less than or equal to zero.");
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

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel parcel) {
            parcel.writeString(this.mId);
            parcel.writeString(this.mLabel);
            parcel.writeInt(this.mHorizontalDpi);
            parcel.writeInt(this.mVerticalDpi);
        }

        static Resolution createFromParcel(Parcel parcel) {
            return new Resolution(parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readInt());
        }

        public int hashCode() {
            return (31 * ((31 * 1) + this.mHorizontalDpi)) + this.mVerticalDpi;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Resolution other = (Resolution) obj;
            if (this.mHorizontalDpi == other.mHorizontalDpi && this.mVerticalDpi == other.mVerticalDpi) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "Resolution{" + "id: " + this.mId + ", label: " + this.mLabel + ", horizontalDpi: " + this.mHorizontalDpi + ", verticalDpi: " + this.mVerticalDpi + "}";
        }
    }

    PrintAttributes() {
    }

    private PrintAttributes(Parcel parcel) {
        Margins margins = null;
        this.mMediaSize = parcel.readInt() == 1 ? MediaSize.createFromParcel(parcel) : null;
        this.mResolution = parcel.readInt() == 1 ? Resolution.createFromParcel(parcel) : null;
        this.mMinMargins = parcel.readInt() == 1 ? Margins.createFromParcel(parcel) : margins;
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
            parcel.writeInt(1);
            this.mMediaSize.writeToParcel(parcel);
        } else {
            parcel.writeInt(0);
        }
        if (this.mResolution != null) {
            parcel.writeInt(1);
            this.mResolution.writeToParcel(parcel);
        } else {
            parcel.writeInt(0);
        }
        if (this.mMinMargins != null) {
            parcel.writeInt(1);
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
        int hashCode = 31 * ((31 * ((31 * ((31 * ((31 * 1) + this.mColorMode)) + this.mDuplexMode)) + (this.mMinMargins == null ? 0 : this.mMinMargins.hashCode()))) + (this.mMediaSize == null ? 0 : this.mMediaSize.hashCode()));
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
        builder.append("mediaSize: ");
        builder.append(this.mMediaSize);
        if (this.mMediaSize != null) {
            builder.append(", orientation: ");
            builder.append(this.mMediaSize.isPortrait() ? "portrait" : "landscape");
        } else {
            builder.append(", orientation: ");
            builder.append("null");
        }
        builder.append(", resolution: ");
        builder.append(this.mResolution);
        builder.append(", minMargins: ");
        builder.append(this.mMinMargins);
        builder.append(", colorMode: ");
        builder.append(colorModeToString(this.mColorMode));
        builder.append(", duplexMode: ");
        builder.append(duplexModeToString(this.mDuplexMode));
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
            case 1:
                return "COLOR_MODE_MONOCHROME";
            case 2:
                return "COLOR_MODE_COLOR";
            default:
                return "COLOR_MODE_UNKNOWN";
        }
    }

    static String duplexModeToString(int duplexMode) {
        if (duplexMode == 4) {
            return "DUPLEX_MODE_SHORT_EDGE";
        }
        switch (duplexMode) {
            case 1:
                return "DUPLEX_MODE_NONE";
            case 2:
                return "DUPLEX_MODE_LONG_EDGE";
            default:
                return "DUPLEX_MODE_UNKNOWN";
        }
    }

    /* access modifiers changed from: package-private */
    public static void enforceValidColorMode(int colorMode) {
        if ((colorMode & 3) == 0 || Integer.bitCount(colorMode) != 1) {
            throw new IllegalArgumentException("invalid color mode: " + colorMode);
        }
    }

    /* access modifiers changed from: package-private */
    public static void enforceValidDuplexMode(int duplexMode) {
        if ((duplexMode & 7) == 0 || Integer.bitCount(duplexMode) != 1) {
            throw new IllegalArgumentException("invalid duplex mode: " + duplexMode);
        }
    }
}
