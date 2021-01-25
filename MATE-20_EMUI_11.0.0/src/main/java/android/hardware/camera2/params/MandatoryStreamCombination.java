package android.hardware.camera2.params;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.legacy.LegacyCameraDevice;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class MandatoryStreamCombination {
    private static final String TAG = "MandatoryStreamCombination";
    private static StreamCombinationTemplate[] sBurstCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.MAXIMUM)}, "Maximum-resolution GPU processing with preview"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Maximum-resolution in-app processing with preview"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Maximum-resolution two-input in-app processsing")};
    private static StreamCombinationTemplate[] sFullCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.MAXIMUM), new StreamTemplate(34, SizeThreshold.MAXIMUM)}, "Maximum-resolution GPU processing with preview"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.MAXIMUM), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Maximum-resolution in-app processing with preview"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.MAXIMUM), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Maximum-resolution two-input in-app processsing"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "Video recording with maximum-size video snapshot"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.VGA), new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Standard video recording plus maximum-resolution in-app processing"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.VGA), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Preview plus two-input maximum-resolution in-app processing")};
    private static StreamCombinationTemplate[] sFullPrivateReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.RECORD)}, "High-resolution ZSL in-app video processing with regular preview", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Maximum-resolution ZSL in-app processing with regular preview", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "Maximum-resolution two-input ZSL in-app processing", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL still capture and in-app processing", ReprocessType.PRIVATE)};
    private static StreamCombinationTemplate[] sFullYUVReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW)}, "Maximum-resolution multi-frame image fusion in-app processing with regular preview", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW)}, "Maximum-resolution multi-frame image fusion two-input in-app processing", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.RECORD)}, "High-resolution ZSL in-app video processing with regular preview", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL still capture and in-app processing", ReprocessType.YUV)};
    private static StreamCombinationTemplate[] sLegacyCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.MAXIMUM)}, "Simple preview, GPU video processing, or no-preview video recording"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "No-viewfinder still image capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.MAXIMUM)}, "In-application video/image processing"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "Standard still imaging"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "In-app processing plus still capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.PREVIEW)}, "Standard recording"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW)}, "Preview plus in-app processing"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "Still capture plus in-app processing")};
    private static StreamCombinationTemplate[] sLevel3Combinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.VGA), new StreamTemplate(35, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "In-app viewfinder analysis with dynamic selection of output format"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.VGA), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "In-app viewfinder analysis with dynamic selection of output format")};
    private static StreamCombinationTemplate[] sLevel3PrivateReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.VGA), new StreamTemplate(32, SizeThreshold.MAXIMUM), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "In-app viewfinder analysis with ZSL, RAW, and JPEG reprocessing output", ReprocessType.PRIVATE)};
    private static StreamCombinationTemplate[] sLevel3YUVReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.VGA), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "In-app viewfinder analysis with ZSL and RAW", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.VGA), new StreamTemplate(32, SizeThreshold.MAXIMUM), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "In-app viewfinder analysis with ZSL, RAW, and JPEG reprocessing output", ReprocessType.YUV)};
    private static StreamCombinationTemplate[] sLimitedCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.RECORD)}, "High-resolution video recording with preview"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.RECORD)}, "High-resolution in-app video processing with preview"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.RECORD)}, "Two-input in-app video processing"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.RECORD), new StreamTemplate(256, SizeThreshold.RECORD)}, "High-resolution recording with video snapshot"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.RECORD), new StreamTemplate(256, SizeThreshold.RECORD)}, "High-resolution in-app processing with video snapshot"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "Two-input in-app processing with still capture")};
    private static StreamCombinationTemplate[] sLimitedPrivateReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "No-viewfinder still image reprocessing", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL(Zero-Shutter-Lag) still imaging", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL still and in-app processing imaging", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL in-app processing with still capture", ReprocessType.PRIVATE)};
    private static StreamCombinationTemplate[] sLimitedYUVReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "No-viewfinder still image reprocessing", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL(Zero-Shutter-Lag) still imaging", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL still and in-app processing imaging", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM)}, "ZSL in-app processing with still capture", ReprocessType.YUV)};
    private static StreamCombinationTemplate[] sRAWPrivateReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL in-app processing and DNG capture", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL in-app processing and preview with DNG capture", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL two-input in-app processing and DNG capture", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL still capture and preview with DNG capture", ReprocessType.PRIVATE), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL in-app processing with still capture and DNG capture", ReprocessType.PRIVATE)};
    private static StreamCombinationTemplate[] sRAWYUVReprocCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL in-app processing and DNG capture", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL in-app processing and preview with DNG capture", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL two-input in-app processing and DNG capture", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL still capture and preview with DNG capture", ReprocessType.YUV), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Mutually exclusive ZSL in-app processing with still capture and DNG capture", ReprocessType.YUV)};
    private static StreamCombinationTemplate[] sRawCombinations = {new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "No-preview DNG capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Standard DNG capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "In-app processing plus DNG capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Video recording with DNG capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Preview with in-app processing and DNG capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Two-input in-app processing plus DNG capture"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(34, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "Still capture with simultaneous JPEG and DNG"), new StreamCombinationTemplate(new StreamTemplate[]{new StreamTemplate(35, SizeThreshold.PREVIEW), new StreamTemplate(256, SizeThreshold.MAXIMUM), new StreamTemplate(32, SizeThreshold.MAXIMUM)}, "In-app processing with simultaneous JPEG and DNG")};
    private final String mDescription;
    private final boolean mIsReprocessable;
    private final ArrayList<MandatoryStreamInformation> mStreamsInformation = new ArrayList<>();

    /* access modifiers changed from: private */
    public enum ReprocessType {
        NONE,
        PRIVATE,
        YUV
    }

    /* access modifiers changed from: private */
    public enum SizeThreshold {
        VGA,
        PREVIEW,
        RECORD,
        MAXIMUM
    }

    public static final class MandatoryStreamInformation {
        private final ArrayList<Size> mAvailableSizes;
        private final int mFormat;
        private final boolean mIsInput;

        public MandatoryStreamInformation(List<Size> availableSizes, int format) {
            this(availableSizes, format, false);
        }

        public MandatoryStreamInformation(List<Size> availableSizes, int format, boolean isInput) {
            this.mAvailableSizes = new ArrayList<>();
            if (!availableSizes.isEmpty()) {
                this.mAvailableSizes.addAll(availableSizes);
                this.mFormat = StreamConfigurationMap.checkArgumentFormat(format);
                this.mIsInput = isInput;
                return;
            }
            throw new IllegalArgumentException("No available sizes");
        }

        public boolean isInput() {
            return this.mIsInput;
        }

        public List<Size> getAvailableSizes() {
            return Collections.unmodifiableList(this.mAvailableSizes);
        }

        public int getFormat() {
            return this.mFormat;
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MandatoryStreamInformation)) {
                return false;
            }
            MandatoryStreamInformation other = (MandatoryStreamInformation) obj;
            if (this.mFormat == other.mFormat && this.mIsInput == other.mIsInput && this.mAvailableSizes.size() == other.mAvailableSizes.size()) {
                return this.mAvailableSizes.equals(other.mAvailableSizes);
            }
            return false;
        }

        public int hashCode() {
            return HashCodeHelpers.hashCode(this.mFormat, Boolean.hashCode(this.mIsInput), this.mAvailableSizes.hashCode());
        }
    }

    public MandatoryStreamCombination(List<MandatoryStreamInformation> streamsInformation, String description, boolean isReprocessable) {
        if (!streamsInformation.isEmpty()) {
            this.mStreamsInformation.addAll(streamsInformation);
            this.mDescription = description;
            this.mIsReprocessable = isReprocessable;
            return;
        }
        throw new IllegalArgumentException("Empty stream information");
    }

    public CharSequence getDescription() {
        return this.mDescription;
    }

    public boolean isReprocessable() {
        return this.mIsReprocessable;
    }

    public List<MandatoryStreamInformation> getStreamsInformation() {
        return Collections.unmodifiableList(this.mStreamsInformation);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MandatoryStreamCombination)) {
            return false;
        }
        MandatoryStreamCombination other = (MandatoryStreamCombination) obj;
        if (this.mDescription == other.mDescription && this.mIsReprocessable == other.mIsReprocessable && this.mStreamsInformation.size() == other.mStreamsInformation.size()) {
            return this.mStreamsInformation.equals(other.mStreamsInformation);
        }
        return false;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(Boolean.hashCode(this.mIsReprocessable), this.mDescription.hashCode(), this.mStreamsInformation.hashCode());
    }

    /* access modifiers changed from: private */
    public static final class StreamTemplate {
        public int mFormat;
        public boolean mIsInput;
        public SizeThreshold mSizeThreshold;

        public StreamTemplate(int format, SizeThreshold sizeThreshold) {
            this(format, sizeThreshold, false);
        }

        public StreamTemplate(int format, SizeThreshold sizeThreshold, boolean isInput) {
            this.mFormat = format;
            this.mSizeThreshold = sizeThreshold;
            this.mIsInput = isInput;
        }
    }

    /* access modifiers changed from: private */
    public static final class StreamCombinationTemplate {
        public String mDescription;
        public ReprocessType mReprocessType;
        public StreamTemplate[] mStreamTemplates;

        public StreamCombinationTemplate(StreamTemplate[] streamTemplates, String description) {
            this(streamTemplates, description, ReprocessType.NONE);
        }

        public StreamCombinationTemplate(StreamTemplate[] streamTemplates, String description, ReprocessType reprocessType) {
            this.mStreamTemplates = streamTemplates;
            this.mReprocessType = reprocessType;
            this.mDescription = description;
        }
    }

    public static final class Builder {
        private final Size kPreviewSizeBound = new Size(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1088);
        private int mCameraId;
        private List<Integer> mCapabilities;
        private Size mDisplaySize;
        private int mHwLevel;
        private boolean mIsHiddenPhysicalCamera;
        private StreamConfigurationMap mStreamConfigMap;

        public Builder(int cameraId, int hwLevel, Size displaySize, List<Integer> capabilities, StreamConfigurationMap sm) {
            this.mCameraId = cameraId;
            this.mDisplaySize = displaySize;
            this.mCapabilities = capabilities;
            this.mStreamConfigMap = sm;
            this.mHwLevel = hwLevel;
            this.mIsHiddenPhysicalCamera = CameraManager.isHiddenPhysicalCamera(Integer.toString(this.mCameraId));
        }

        public List<MandatoryStreamCombination> getAvailableMandatoryStreamCombinations() {
            if (!isColorOutputSupported()) {
                Log.v(MandatoryStreamCombination.TAG, "Device is not backward compatible!");
                return null;
            } else if (this.mCameraId >= 0 || isExternalCamera()) {
                ArrayList<StreamCombinationTemplate> availableTemplates = new ArrayList<>();
                if (isHardwareLevelAtLeastLegacy()) {
                    availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLegacyCombinations));
                }
                if (isHardwareLevelAtLeastLimited() || isExternalCamera()) {
                    availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLimitedCombinations));
                    if (isPrivateReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLimitedPrivateReprocCombinations));
                    }
                    if (isYUVReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLimitedYUVReprocCombinations));
                    }
                }
                if (isCapabilitySupported(6)) {
                    availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sBurstCombinations));
                }
                if (isHardwareLevelAtLeastFull()) {
                    availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sFullCombinations));
                    if (isPrivateReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sFullPrivateReprocCombinations));
                    }
                    if (isYUVReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sFullYUVReprocCombinations));
                    }
                }
                if (isCapabilitySupported(3)) {
                    availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sRawCombinations));
                    if (isPrivateReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sRAWPrivateReprocCombinations));
                    }
                    if (isYUVReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sRAWYUVReprocCombinations));
                    }
                }
                if (isHardwareLevelAtLeastLevel3()) {
                    availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLevel3Combinations));
                    if (isPrivateReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLevel3PrivateReprocCombinations));
                    }
                    if (isYUVReprocessingSupported()) {
                        availableTemplates.addAll(Arrays.asList(MandatoryStreamCombination.sLevel3YUVReprocCombinations));
                    }
                }
                return generateAvailableCombinations(availableTemplates);
            } else {
                Log.i(MandatoryStreamCombination.TAG, "Invalid camera id");
                return null;
            }
        }

        private List<MandatoryStreamCombination> generateAvailableCombinations(ArrayList<StreamCombinationTemplate> availableTemplates) {
            Size maxPrivateInputSize;
            Size maxYUVInputSize;
            int i;
            StreamTemplate[] streamTemplateArr;
            List<Size> sizes;
            int format;
            if (availableTemplates.isEmpty()) {
                Log.e(MandatoryStreamCombination.TAG, "No available stream templates!");
                return null;
            }
            HashMap<Pair<SizeThreshold, Integer>, List<Size>> availableSizes = enumerateAvailableSizes();
            if (availableSizes == null) {
                Log.e(MandatoryStreamCombination.TAG, "Available size enumeration failed!");
                return null;
            }
            Size[] rawSizes = this.mStreamConfigMap.getOutputSizes(32);
            ArrayList<Size> availableRawSizes = new ArrayList<>();
            if (rawSizes != null) {
                availableRawSizes.ensureCapacity(rawSizes.length);
                availableRawSizes.addAll(Arrays.asList(rawSizes));
            }
            Size maxPrivateInputSize2 = new Size(0, 0);
            if (isPrivateReprocessingSupported()) {
                maxPrivateInputSize = getMaxSize(this.mStreamConfigMap.getInputSizes(34));
            } else {
                maxPrivateInputSize = maxPrivateInputSize2;
            }
            Size maxYUVInputSize2 = new Size(0, 0);
            if (isYUVReprocessingSupported()) {
                maxYUVInputSize = getMaxSize(this.mStreamConfigMap.getInputSizes(35));
            } else {
                maxYUVInputSize = maxYUVInputSize2;
            }
            ArrayList<MandatoryStreamCombination> availableStreamCombinations = new ArrayList<>();
            availableStreamCombinations.ensureCapacity(availableTemplates.size());
            Iterator<StreamCombinationTemplate> it = availableTemplates.iterator();
            while (it.hasNext()) {
                StreamCombinationTemplate combTemplate = it.next();
                ArrayList<MandatoryStreamInformation> streamsInfo = new ArrayList<>();
                streamsInfo.ensureCapacity(combTemplate.mStreamTemplates.length);
                boolean isReprocessable = combTemplate.mReprocessType != ReprocessType.NONE;
                if (isReprocessable) {
                    ArrayList<Size> inputSize = new ArrayList<>();
                    if (combTemplate.mReprocessType == ReprocessType.PRIVATE) {
                        inputSize.add(maxPrivateInputSize);
                        format = 34;
                    } else {
                        inputSize.add(maxYUVInputSize);
                        format = 35;
                    }
                    streamsInfo.add(new MandatoryStreamInformation(inputSize, format, true));
                    streamsInfo.add(new MandatoryStreamInformation(inputSize, format));
                }
                StreamTemplate[] streamTemplateArr2 = combTemplate.mStreamTemplates;
                int i2 = 0;
                for (int length = streamTemplateArr2.length; i2 < length; length = i) {
                    StreamTemplate template = streamTemplateArr2[i2];
                    if (template.mFormat == 32) {
                        sizes = availableRawSizes;
                        streamTemplateArr = streamTemplateArr2;
                        i = length;
                    } else {
                        streamTemplateArr = streamTemplateArr2;
                        i = length;
                        sizes = availableSizes.get(new Pair<>(template.mSizeThreshold, new Integer(template.mFormat)));
                    }
                    try {
                        streamsInfo.add(new MandatoryStreamInformation(sizes, template.mFormat));
                        i2++;
                        it = it;
                        streamTemplateArr2 = streamTemplateArr;
                    } catch (IllegalArgumentException e) {
                        Log.e(MandatoryStreamCombination.TAG, "No available sizes found for format: " + template.mFormat + " size threshold: " + template.mSizeThreshold + " combination: " + combTemplate.mDescription);
                        return null;
                    }
                }
                try {
                    availableStreamCombinations.add(new MandatoryStreamCombination(streamsInfo, combTemplate.mDescription, isReprocessable));
                    it = it;
                } catch (IllegalArgumentException e2) {
                    Log.e(MandatoryStreamCombination.TAG, "No stream information for mandatory combination: " + combTemplate.mDescription);
                    return null;
                }
            }
            return Collections.unmodifiableList(availableStreamCombinations);
        }

        private HashMap<Pair<SizeThreshold, Integer>, List<Size>> enumerateAvailableSizes() {
            Size recordingMaxSize;
            int[] formats = {34, 35, 256};
            new Size(0, 0);
            new Size(0, 0);
            Size vgaSize = new Size(640, 480);
            if (isExternalCamera() || this.mIsHiddenPhysicalCamera) {
                recordingMaxSize = getMaxCameraRecordingSize();
            } else {
                recordingMaxSize = getMaxRecordingSize();
            }
            if (recordingMaxSize == null) {
                Log.e(MandatoryStreamCombination.TAG, "Failed to find maximum recording size!");
                return null;
            }
            HashMap<Integer, Size[]> allSizes = new HashMap<>();
            for (int format : formats) {
                allSizes.put(new Integer(format), this.mStreamConfigMap.getOutputSizes(format));
            }
            List<Size> previewSizes = getSizesWithinBound(allSizes.get(new Integer(34)), this.kPreviewSizeBound);
            if (previewSizes == null || previewSizes.isEmpty()) {
                Log.e(MandatoryStreamCombination.TAG, "No preview sizes within preview size bound!");
                return null;
            }
            Size previewMaxSize = getMaxPreviewSize(getAscendingOrderSizes(previewSizes, false));
            HashMap<Pair<SizeThreshold, Integer>, List<Size>> availableSizes = new HashMap<>();
            for (int format2 : formats) {
                Integer intFormat = new Integer(format2);
                Size[] sizes = allSizes.get(intFormat);
                availableSizes.put(new Pair<>(SizeThreshold.VGA, intFormat), getSizesWithinBound(sizes, vgaSize));
                availableSizes.put(new Pair<>(SizeThreshold.PREVIEW, intFormat), getSizesWithinBound(sizes, previewMaxSize));
                availableSizes.put(new Pair<>(SizeThreshold.RECORD, intFormat), getSizesWithinBound(sizes, recordingMaxSize));
                availableSizes.put(new Pair<>(SizeThreshold.MAXIMUM, intFormat), Arrays.asList(sizes));
            }
            return availableSizes;
        }

        private static List<Size> getSizesWithinBound(Size[] sizes, Size bound) {
            ArrayList<Size> ret = new ArrayList<>();
            for (Size size : sizes) {
                if (size.getWidth() <= bound.getWidth() && size.getHeight() <= bound.getHeight()) {
                    ret.add(size);
                }
            }
            return ret;
        }

        public static Size getMaxSize(Size... sizes) {
            if (sizes == null || sizes.length == 0) {
                throw new IllegalArgumentException("sizes was empty");
            }
            Size sz = sizes[0];
            for (Size size : sizes) {
                if (size.getWidth() * size.getHeight() > sz.getWidth() * sz.getHeight()) {
                    sz = size;
                }
            }
            return sz;
        }

        private boolean isHardwareLevelAtLeast(int level) {
            int[] sortedHwLevels = {2, 4, 0, 1, 3};
            if (level == this.mHwLevel) {
                return true;
            }
            for (int sortedlevel : sortedHwLevels) {
                if (sortedlevel == level) {
                    return true;
                }
                if (sortedlevel == this.mHwLevel) {
                    return false;
                }
            }
            return false;
        }

        private boolean isExternalCamera() {
            return this.mHwLevel == 4;
        }

        private boolean isHardwareLevelAtLeastLegacy() {
            return isHardwareLevelAtLeast(2);
        }

        private boolean isHardwareLevelAtLeastLimited() {
            return isHardwareLevelAtLeast(0);
        }

        private boolean isHardwareLevelAtLeastFull() {
            return isHardwareLevelAtLeast(1);
        }

        private boolean isHardwareLevelAtLeastLevel3() {
            return isHardwareLevelAtLeast(3);
        }

        private boolean isCapabilitySupported(int capability) {
            return this.mCapabilities.contains(Integer.valueOf(capability));
        }

        private boolean isColorOutputSupported() {
            return isCapabilitySupported(0);
        }

        private boolean isPrivateReprocessingSupported() {
            return isCapabilitySupported(4);
        }

        private boolean isYUVReprocessingSupported() {
            return isCapabilitySupported(7);
        }

        private Size getMaxRecordingSize() {
            int quality = 8;
            if (!CamcorderProfile.hasProfile(this.mCameraId, 8)) {
                if (CamcorderProfile.hasProfile(this.mCameraId, 6)) {
                    quality = 6;
                } else if (CamcorderProfile.hasProfile(this.mCameraId, 5)) {
                    quality = 5;
                } else if (CamcorderProfile.hasProfile(this.mCameraId, 4)) {
                    quality = 4;
                } else if (CamcorderProfile.hasProfile(this.mCameraId, 7)) {
                    quality = 7;
                } else if (CamcorderProfile.hasProfile(this.mCameraId, 3)) {
                    quality = 3;
                } else if (CamcorderProfile.hasProfile(this.mCameraId, 2)) {
                    quality = 2;
                } else {
                    quality = -1;
                }
            }
            if (quality < 0) {
                return null;
            }
            CamcorderProfile maxProfile = CamcorderProfile.get(this.mCameraId, quality);
            return new Size(maxProfile.videoFrameWidth, maxProfile.videoFrameHeight);
        }

        private Size getMaxCameraRecordingSize() {
            Size FULLHD = new Size(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080);
            Size[] videoSizeArr = this.mStreamConfigMap.getOutputSizes(MediaRecorder.class);
            List<Size> sizes = new ArrayList<>();
            if (videoSizeArr != null) {
                for (Size sz : videoSizeArr) {
                    if (sz.getWidth() <= FULLHD.getWidth() && sz.getHeight() <= FULLHD.getHeight()) {
                        sizes.add(sz);
                    }
                }
            }
            for (Size sz2 : getAscendingOrderSizes(sizes, false)) {
                if (((double) this.mStreamConfigMap.getOutputMinFrameDuration(MediaRecorder.class, sz2)) > 3.3222591362126246E7d) {
                    Log.i(MandatoryStreamCombination.TAG, "External camera " + this.mCameraId + " has max video size:" + sz2);
                    return sz2;
                }
            }
            Log.w(MandatoryStreamCombination.TAG, "Camera " + this.mCameraId + " does not support any 30fps video output");
            return FULLHD;
        }

        private Size getMaxPreviewSize(List<Size> orderedPreviewSizes) {
            if (orderedPreviewSizes != null) {
                for (Size size : orderedPreviewSizes) {
                    if (this.mDisplaySize.getWidth() >= size.getWidth() && this.mDisplaySize.getWidth() >= size.getHeight()) {
                        return size;
                    }
                }
            }
            Log.w(MandatoryStreamCombination.TAG, "Camera " + this.mCameraId + " maximum preview size search failed with display size " + this.mDisplaySize);
            return this.kPreviewSizeBound;
        }

        /* access modifiers changed from: private */
        public static int compareSizes(int widthA, int heightA, int widthB, int heightB) {
            long left = ((long) widthA) * ((long) heightA);
            long right = ((long) widthB) * ((long) heightB);
            if (left == right) {
                left = (long) widthA;
                right = (long) widthB;
            }
            if (left < right) {
                return -1;
            }
            return left > right ? 1 : 0;
        }

        public static class SizeComparator implements Comparator<Size> {
            public int compare(Size lhs, Size rhs) {
                return Builder.compareSizes(lhs.getWidth(), lhs.getHeight(), rhs.getWidth(), rhs.getHeight());
            }
        }

        private static List<Size> getAscendingOrderSizes(List<Size> sizeList, boolean ascending) {
            Comparator<Size> comparator = new SizeComparator();
            List<Size> sortedSizes = new ArrayList<>();
            sortedSizes.addAll(sizeList);
            Collections.sort(sortedSizes, comparator);
            if (!ascending) {
                Collections.reverse(sortedSizes);
            }
            return sortedSizes;
        }
    }
}
