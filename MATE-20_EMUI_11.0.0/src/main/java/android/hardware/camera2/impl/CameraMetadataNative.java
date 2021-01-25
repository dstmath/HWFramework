package android.hardware.camera2.impl;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.MarshalRegistry;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.marshal.impl.MarshalQueryableArray;
import android.hardware.camera2.marshal.impl.MarshalQueryableBlackLevelPattern;
import android.hardware.camera2.marshal.impl.MarshalQueryableBoolean;
import android.hardware.camera2.marshal.impl.MarshalQueryableColorSpaceTransform;
import android.hardware.camera2.marshal.impl.MarshalQueryableEnum;
import android.hardware.camera2.marshal.impl.MarshalQueryableHighSpeedVideoConfiguration;
import android.hardware.camera2.marshal.impl.MarshalQueryableMeteringRectangle;
import android.hardware.camera2.marshal.impl.MarshalQueryableNativeByteToInteger;
import android.hardware.camera2.marshal.impl.MarshalQueryablePair;
import android.hardware.camera2.marshal.impl.MarshalQueryableParcelable;
import android.hardware.camera2.marshal.impl.MarshalQueryablePrimitive;
import android.hardware.camera2.marshal.impl.MarshalQueryableRange;
import android.hardware.camera2.marshal.impl.MarshalQueryableRecommendedStreamConfiguration;
import android.hardware.camera2.marshal.impl.MarshalQueryableRect;
import android.hardware.camera2.marshal.impl.MarshalQueryableReprocessFormatsMap;
import android.hardware.camera2.marshal.impl.MarshalQueryableRggbChannelVector;
import android.hardware.camera2.marshal.impl.MarshalQueryableSize;
import android.hardware.camera2.marshal.impl.MarshalQueryableSizeF;
import android.hardware.camera2.marshal.impl.MarshalQueryableStreamConfiguration;
import android.hardware.camera2.marshal.impl.MarshalQueryableStreamConfigurationDuration;
import android.hardware.camera2.marshal.impl.MarshalQueryableString;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.HighSpeedVideoConfiguration;
import android.hardware.camera2.params.LensShadingMap;
import android.hardware.camera2.params.MandatoryStreamCombination;
import android.hardware.camera2.params.OisSample;
import android.hardware.camera2.params.RecommendedStreamConfiguration;
import android.hardware.camera2.params.RecommendedStreamConfigurationMap;
import android.hardware.camera2.params.ReprocessFormatsMap;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.TypeReference;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Size;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CameraMetadataNative implements Parcelable {
    private static final String CELLID_PROCESS = "CELLID";
    public static final Parcelable.Creator<CameraMetadataNative> CREATOR = new Parcelable.Creator<CameraMetadataNative>() {
        /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CameraMetadataNative createFromParcel(Parcel in) {
            CameraMetadataNative metadata = new CameraMetadataNative();
            metadata.readFromParcel(in);
            return metadata;
        }

        @Override // android.os.Parcelable.Creator
        public CameraMetadataNative[] newArray(int size) {
            return new CameraMetadataNative[size];
        }
    };
    private static final boolean DEBUG = false;
    private static final int FACE_LANDMARK_SIZE = 6;
    private static final String GPS_PROCESS = "GPS";
    public static final int NATIVE_JPEG_FORMAT = 33;
    public static final int NUM_TYPES = 6;
    private static final String TAG = "CameraMetadataJV";
    public static final int TYPE_BYTE = 0;
    public static final int TYPE_DOUBLE = 4;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_INT32 = 1;
    public static final int TYPE_INT64 = 3;
    public static final int TYPE_RATIONAL = 5;
    private static final HashMap<Key<?>, GetCommand> sGetCommandMap = new HashMap<>();
    private static final HashMap<Key<?>, SetCommand> sSetCommandMap = new HashMap<>();
    private int mCameraId;
    private Size mDisplaySize;
    @UnsupportedAppUsage
    private long mMetadataPtr;

    private native long nativeAllocate();

    private native long nativeAllocateCopy(CameraMetadataNative cameraMetadataNative) throws NullPointerException;

    private native synchronized void nativeClose();

    private native synchronized void nativeDump() throws IOException;

    private native synchronized ArrayList nativeGetAllVendorKeys(Class cls);

    private native synchronized int nativeGetEntryCount();

    private static native int nativeGetTagFromKey(String str, long j) throws IllegalArgumentException;

    @UnsupportedAppUsage
    private native synchronized int nativeGetTagFromKeyLocal(String str) throws IllegalArgumentException;

    private static native int nativeGetTypeFromTag(int i, long j) throws IllegalArgumentException;

    @UnsupportedAppUsage
    private native synchronized int nativeGetTypeFromTagLocal(int i) throws IllegalArgumentException;

    private native synchronized boolean nativeIsEmpty();

    private native synchronized void nativeReadFromParcel(Parcel parcel);

    @UnsupportedAppUsage
    private native synchronized byte[] nativeReadValues(int i);

    private static native int nativeSetupGlobalVendorTagDescriptor();

    private native synchronized void nativeSwap(CameraMetadataNative cameraMetadataNative) throws NullPointerException;

    private native synchronized void nativeWriteToParcel(Parcel parcel);

    private native synchronized void nativeWriteValues(int i, byte[] bArr);

    public static class Key<T> {
        private final String mFallbackName;
        private boolean mHasTag;
        private final int mHash;
        private final String mName;
        private int mTag;
        private final Class<T> mType;
        private final TypeReference<T> mTypeReference;
        private long mVendorId = Long.MAX_VALUE;

        public Key(String name, Class<T> type, long vendorId) {
            if (name == null) {
                throw new NullPointerException("Key needs a valid name");
            } else if (type != null) {
                this.mName = name;
                this.mFallbackName = null;
                this.mType = type;
                this.mVendorId = vendorId;
                this.mTypeReference = TypeReference.createSpecializedTypeReference((Class) type);
                this.mHash = this.mName.hashCode() ^ this.mTypeReference.hashCode();
            } else {
                throw new NullPointerException("Type needs to be non-null");
            }
        }

        public Key(String name, String fallbackName, Class<T> type) {
            if (name == null) {
                throw new NullPointerException("Key needs a valid name");
            } else if (type != null) {
                this.mName = name;
                this.mFallbackName = fallbackName;
                this.mType = type;
                this.mTypeReference = TypeReference.createSpecializedTypeReference((Class) type);
                this.mHash = this.mName.hashCode() ^ this.mTypeReference.hashCode();
            } else {
                throw new NullPointerException("Type needs to be non-null");
            }
        }

        public Key(String name, Class<T> type) {
            if (name == null) {
                throw new NullPointerException("Key needs a valid name");
            } else if (type != null) {
                this.mName = name;
                this.mFallbackName = null;
                this.mType = type;
                this.mTypeReference = TypeReference.createSpecializedTypeReference((Class) type);
                this.mHash = this.mName.hashCode() ^ this.mTypeReference.hashCode();
            } else {
                throw new NullPointerException("Type needs to be non-null");
            }
        }

        /* JADX DEBUG: Type inference failed for r0v4. Raw type applied. Possible types: java.lang.Class<? super T>, java.lang.Class<T> */
        public Key(String name, TypeReference<T> typeReference) {
            if (name == null) {
                throw new NullPointerException("Key needs a valid name");
            } else if (typeReference != null) {
                this.mName = name;
                this.mFallbackName = null;
                this.mType = (Class<? super T>) typeReference.getRawType();
                this.mTypeReference = typeReference;
                this.mHash = this.mName.hashCode() ^ this.mTypeReference.hashCode();
            } else {
                throw new NullPointerException("TypeReference needs to be non-null");
            }
        }

        public final String getName() {
            return this.mName;
        }

        public final int hashCode() {
            return this.mHash;
        }

        public final boolean equals(Object o) {
            Key<?> lhs;
            if (this == o) {
                return true;
            }
            if (o == null || hashCode() != o.hashCode()) {
                return false;
            }
            if (o instanceof CaptureResult.Key) {
                lhs = ((CaptureResult.Key) o).getNativeKey();
            } else if (o instanceof CaptureRequest.Key) {
                lhs = ((CaptureRequest.Key) o).getNativeKey();
            } else if (o instanceof CameraCharacteristics.Key) {
                lhs = ((CameraCharacteristics.Key) o).getNativeKey();
            } else if (!(o instanceof Key)) {
                return false;
            } else {
                lhs = (Key) o;
            }
            if (!this.mName.equals(lhs.mName) || !this.mTypeReference.equals(lhs.mTypeReference)) {
                return false;
            }
            return true;
        }

        @UnsupportedAppUsage
        public final int getTag() {
            if (!this.mHasTag) {
                this.mTag = CameraMetadataNative.getTag(this.mName, this.mVendorId);
                this.mHasTag = true;
            }
            return this.mTag;
        }

        public final Class<T> getType() {
            return this.mType;
        }

        public final long getVendorId() {
            return this.mVendorId;
        }

        public final TypeReference<T> getTypeReference() {
            return this.mTypeReference;
        }
    }

    private static String translateLocationProviderToProcess(String provider) {
        if (provider == null) {
            return null;
        }
        char c = 65535;
        int hashCode = provider.hashCode();
        if (hashCode != 102570) {
            if (hashCode == 1843485230 && provider.equals(LocationManager.NETWORK_PROVIDER)) {
                c = 1;
            }
        } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
            c = 0;
        }
        if (c == 0) {
            return GPS_PROCESS;
        }
        if (c != 1) {
            return null;
        }
        return CELLID_PROCESS;
    }

    private static String translateProcessToLocationProvider(String process) {
        if (process == null) {
            return null;
        }
        char c = 65535;
        int hashCode = process.hashCode();
        if (hashCode != 70794) {
            if (hashCode == 1984215549 && process.equals(CELLID_PROCESS)) {
                c = 1;
            }
        } else if (process.equals(GPS_PROCESS)) {
            c = 0;
        }
        if (c == 0) {
            return LocationManager.GPS_PROVIDER;
        }
        if (c != 1) {
            return null;
        }
        return LocationManager.NETWORK_PROVIDER;
    }

    public CameraMetadataNative() {
        this.mCameraId = -1;
        this.mDisplaySize = new Size(0, 0);
        this.mMetadataPtr = nativeAllocate();
        if (this.mMetadataPtr == 0) {
            throw new OutOfMemoryError("Failed to allocate native CameraMetadata");
        }
    }

    public CameraMetadataNative(CameraMetadataNative other) {
        this.mCameraId = -1;
        this.mDisplaySize = new Size(0, 0);
        this.mMetadataPtr = nativeAllocateCopy(other);
        if (this.mMetadataPtr == 0) {
            throw new OutOfMemoryError("Failed to allocate native CameraMetadata");
        }
    }

    public static CameraMetadataNative move(CameraMetadataNative other) {
        CameraMetadataNative newObject = new CameraMetadataNative();
        newObject.swap(other);
        return newObject;
    }

    static {
        sGetCommandMap.put(CameraCharacteristics.SCALER_AVAILABLE_FORMATS.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass2 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getAvailableFormats();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_FACES.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass3 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getFaces();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_FACE_RECTANGLES.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass4 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getFaceRectangles();
            }
        });
        sGetCommandMap.put(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass5 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getStreamConfigurationMap();
            }
        });
        sGetCommandMap.put(CameraCharacteristics.SCALER_MANDATORY_STREAM_COMBINATIONS.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass6 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMandatoryStreamCombinations();
            }
        });
        sGetCommandMap.put(CameraCharacteristics.CONTROL_MAX_REGIONS_AE.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass7 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxRegions(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass8 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxRegions(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.CONTROL_MAX_REGIONS_AF.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass9 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxRegions(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass10 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxNumOutputs(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass11 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxNumOutputs(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass12 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxNumOutputs(key);
            }
        });
        sGetCommandMap.put(CaptureRequest.TONEMAP_CURVE.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass13 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getTonemapCurve();
            }
        });
        sGetCommandMap.put(CaptureResult.JPEG_GPS_LOCATION.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass14 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getGpsLocation();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_LENS_SHADING_CORRECTION_MAP.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass15 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getLensShadingMap();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_OIS_SAMPLES.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass16 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getOisSamples();
            }
        });
        sSetCommandMap.put(CameraCharacteristics.SCALER_AVAILABLE_FORMATS.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass17 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setAvailableFormats((int[]) value);
            }
        });
        sSetCommandMap.put(CaptureResult.STATISTICS_FACE_RECTANGLES.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass18 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setFaceRectangles((Rect[]) value);
            }
        });
        sSetCommandMap.put(CaptureResult.STATISTICS_FACES.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass19 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setFaces((Face[]) value);
            }
        });
        sSetCommandMap.put(CaptureRequest.TONEMAP_CURVE.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass20 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setTonemapCurve(value);
            }
        });
        sSetCommandMap.put(CaptureResult.JPEG_GPS_LOCATION.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass21 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setGpsLocation(value);
            }
        });
        registerAllMarshalers();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        nativeWriteToParcel(dest);
    }

    public <T> T get(CameraCharacteristics.Key<T> key) {
        return (T) get(key.getNativeKey());
    }

    public <T> T get(CaptureResult.Key<T> key) {
        return (T) get(key.getNativeKey());
    }

    public <T> T get(CaptureRequest.Key<T> key) {
        return (T) get(key.getNativeKey());
    }

    public <T> T get(Key<T> key) {
        Preconditions.checkNotNull(key, "key must not be null");
        GetCommand g = sGetCommandMap.get(key);
        return g != null ? (T) g.getValue(this, key) : (T) getBase(key);
    }

    public void readFromParcel(Parcel in) {
        nativeReadFromParcel(in);
    }

    public static void setupGlobalVendorTagDescriptor() throws ServiceSpecificException {
        int err = nativeSetupGlobalVendorTagDescriptor();
        if (err != 0) {
            throw new ServiceSpecificException(err, "Failure to set up global vendor tags");
        }
    }

    public <T> void set(Key<T> key, T value) {
        SetCommand s = sSetCommandMap.get(key);
        if (s != null) {
            s.setValue(this, value);
        } else {
            setBase(key, value);
        }
    }

    public <T> void set(CaptureRequest.Key<T> key, T value) {
        set(key.getNativeKey(), value);
    }

    public <T> void set(CaptureResult.Key<T> key, T value) {
        set(key.getNativeKey(), value);
    }

    public <T> void set(CameraCharacteristics.Key<T> key, T value) {
        set(key.getNativeKey(), value);
    }

    private void close() {
        nativeClose();
        this.mMetadataPtr = 0;
    }

    private <T> T getBase(CameraCharacteristics.Key<T> key) {
        return (T) getBase(key.getNativeKey());
    }

    private <T> T getBase(CaptureResult.Key<T> key) {
        return (T) getBase(key.getNativeKey());
    }

    private <T> T getBase(CaptureRequest.Key<T> key) {
        return (T) getBase(key.getNativeKey());
    }

    private <T> T getBase(Key<T> key) {
        int tag = nativeGetTagFromKeyLocal(key.getName());
        byte[] values = readValues(tag);
        if (values == null && (((Key) key).mFallbackName == null || (values = readValues((tag = nativeGetTagFromKeyLocal(((Key) key).mFallbackName)))) == null)) {
            return null;
        }
        return getMarshalerForKey(key, nativeGetTypeFromTagLocal(tag)).unmarshal(ByteBuffer.wrap(values).order(ByteOrder.nativeOrder()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int[] getAvailableFormats() {
        int[] availableFormats = (int[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_FORMATS);
        if (availableFormats != null) {
            for (int i = 0; i < availableFormats.length; i++) {
                if (availableFormats[i] == 33) {
                    availableFormats[i] = 256;
                }
            }
        }
        return availableFormats;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setFaces(Face[] faces) {
        if (faces == null) {
            return false;
        }
        boolean fullMode = true;
        int numFaces = faces.length;
        for (Face face : faces) {
            if (face == null) {
                numFaces--;
                Log.w(TAG, "setFaces - null face detected, skipping");
            } else if (face.getId() == -1) {
                fullMode = false;
            }
        }
        Rect[] faceRectangles = new Rect[numFaces];
        byte[] faceScores = new byte[numFaces];
        int[] faceIds = null;
        int[] faceLandmarks = null;
        if (fullMode) {
            faceIds = new int[numFaces];
            faceLandmarks = new int[(numFaces * 6)];
        }
        int i = 0;
        for (Face face2 : faces) {
            if (face2 != null) {
                faceRectangles[i] = face2.getBounds();
                faceScores[i] = (byte) face2.getScore();
                if (fullMode) {
                    faceIds[i] = face2.getId();
                    int j = 0 + 1;
                    faceLandmarks[(i * 6) + 0] = face2.getLeftEyePosition().x;
                    int j2 = j + 1;
                    faceLandmarks[(i * 6) + j] = face2.getLeftEyePosition().y;
                    int j3 = j2 + 1;
                    faceLandmarks[(i * 6) + j2] = face2.getRightEyePosition().x;
                    int j4 = j3 + 1;
                    faceLandmarks[(i * 6) + j3] = face2.getRightEyePosition().y;
                    int j5 = j4 + 1;
                    faceLandmarks[(i * 6) + j4] = face2.getMouthPosition().x;
                    int i2 = j5 + 1;
                    faceLandmarks[(i * 6) + j5] = face2.getMouthPosition().y;
                }
                i++;
            }
        }
        set((CaptureResult.Key<CaptureResult.Key<Rect[]>>) CaptureResult.STATISTICS_FACE_RECTANGLES, (CaptureResult.Key<Rect[]>) faceRectangles);
        set((CaptureResult.Key<CaptureResult.Key<int[]>>) CaptureResult.STATISTICS_FACE_IDS, (CaptureResult.Key<int[]>) faceIds);
        set((CaptureResult.Key<CaptureResult.Key<int[]>>) CaptureResult.STATISTICS_FACE_LANDMARKS, (CaptureResult.Key<int[]>) faceLandmarks);
        set((CaptureResult.Key<CaptureResult.Key<byte[]>>) CaptureResult.STATISTICS_FACE_SCORES, (CaptureResult.Key<byte[]>) faceScores);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Face[] getFaces() {
        Integer faceDetectMode = (Integer) get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
        byte[] faceScores = (byte[]) get(CaptureResult.STATISTICS_FACE_SCORES);
        Rect[] faceRectangles = (Rect[]) get(CaptureResult.STATISTICS_FACE_RECTANGLES);
        int[] faceIds = (int[]) get(CaptureResult.STATISTICS_FACE_IDS);
        int[] faceLandmarks = (int[]) get(CaptureResult.STATISTICS_FACE_LANDMARKS);
        byte b = 1;
        if (areValuesAllNull(faceDetectMode, faceScores, faceRectangles, faceIds, faceLandmarks)) {
            return null;
        }
        if (faceDetectMode == null) {
            Log.w(TAG, "Face detect mode metadata is null, assuming the mode is SIMPLE");
            faceDetectMode = 1;
        } else if (faceDetectMode.intValue() > 2) {
            faceDetectMode = 2;
        } else if (faceDetectMode.intValue() == 0) {
            return new Face[0];
        } else {
            if (!(faceDetectMode.intValue() == 1 || faceDetectMode.intValue() == 2)) {
                Log.w(TAG, "Unknown face detect mode: " + faceDetectMode);
                return new Face[0];
            }
        }
        if (faceScores == null || faceRectangles == null) {
            return new Face[0];
        }
        if (faceScores.length != faceRectangles.length) {
            Log.w(TAG, String.format("Face score size(%d) doesn match face rectangle size(%d)!", Integer.valueOf(faceScores.length), Integer.valueOf(faceRectangles.length)));
        }
        int numFaces = Math.min(faceScores.length, faceRectangles.length);
        if (faceDetectMode.intValue() == 2) {
            if (faceIds == null || faceLandmarks == null) {
                Log.w(TAG, "Expect face ids and landmarks to be non-null for FULL mode,fallback to SIMPLE mode");
                faceDetectMode = 1;
            } else {
                if (!(faceIds.length == numFaces && faceLandmarks.length == numFaces * 6)) {
                    Log.w(TAG, String.format("Face id size(%d), or face landmark size(%d) don'tmatch face number(%d)!", Integer.valueOf(faceIds.length), Integer.valueOf(faceLandmarks.length * 6), Integer.valueOf(numFaces)));
                }
                numFaces = Math.min(Math.min(numFaces, faceIds.length), faceLandmarks.length / 6);
            }
        }
        ArrayList<Face> faceList = new ArrayList<>();
        byte b2 = 100;
        if (faceDetectMode.intValue() == 1) {
            for (int i = 0; i < numFaces; i++) {
                if (faceScores[i] <= 100 && faceScores[i] >= 1) {
                    faceList.add(new Face(faceRectangles[i], faceScores[i]));
                }
            }
        } else {
            int i2 = 0;
            while (i2 < numFaces) {
                if (faceScores[i2] <= b2 && faceScores[i2] >= b && faceIds[i2] >= 0) {
                    faceList.add(new Face(faceRectangles[i2], faceScores[i2], faceIds[i2], new Point(faceLandmarks[i2 * 6], faceLandmarks[(i2 * 6) + 1]), new Point(faceLandmarks[(i2 * 6) + 2], faceLandmarks[(i2 * 6) + 3]), new Point(faceLandmarks[(i2 * 6) + 4], faceLandmarks[(i2 * 6) + 5])));
                }
                i2++;
                b = 1;
                b2 = 100;
            }
        }
        Face[] faces = new Face[faceList.size()];
        faceList.toArray(faces);
        return faces;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Rect[] getFaceRectangles() {
        Rect[] faceRectangles = (Rect[]) getBase(CaptureResult.STATISTICS_FACE_RECTANGLES);
        if (faceRectangles == null) {
            return null;
        }
        Rect[] fixedFaceRectangles = new Rect[faceRectangles.length];
        for (int i = 0; i < faceRectangles.length; i++) {
            fixedFaceRectangles[i] = new Rect(faceRectangles[i].left, faceRectangles[i].top, faceRectangles[i].right - faceRectangles[i].left, faceRectangles[i].bottom - faceRectangles[i].top);
        }
        return fixedFaceRectangles;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LensShadingMap getLensShadingMap() {
        float[] lsmArray = (float[]) getBase(CaptureResult.STATISTICS_LENS_SHADING_MAP);
        Size s = (Size) get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE);
        if (lsmArray == null) {
            return null;
        }
        if (s != null) {
            return new LensShadingMap(lsmArray, s.getHeight(), s.getWidth());
        }
        Log.w(TAG, "getLensShadingMap - Lens shading map size was null.");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Location getGpsLocation() {
        String processingMethod = (String) get(CaptureResult.JPEG_GPS_PROCESSING_METHOD);
        double[] coords = (double[]) get(CaptureResult.JPEG_GPS_COORDINATES);
        Long timeStamp = (Long) get(CaptureResult.JPEG_GPS_TIMESTAMP);
        if (areValuesAllNull(processingMethod, coords, timeStamp)) {
            return null;
        }
        Location l = new Location(translateProcessToLocationProvider(processingMethod));
        if (timeStamp != null) {
            l.setTime(timeStamp.longValue() * 1000);
        } else {
            Log.w(TAG, "getGpsLocation - No timestamp for GPS location.");
        }
        if (coords != null) {
            l.setLatitude(coords[0]);
            l.setLongitude(coords[1]);
            l.setAltitude(coords[2]);
        } else {
            Log.w(TAG, "getGpsLocation - No coordinates for GPS location");
        }
        return l;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setGpsLocation(Location l) {
        if (l == null) {
            return false;
        }
        double[] coords = {l.getLatitude(), l.getLongitude(), l.getAltitude()};
        String processMethod = translateLocationProviderToProcess(l.getProvider());
        set((CaptureRequest.Key<CaptureRequest.Key<Long>>) CaptureRequest.JPEG_GPS_TIMESTAMP, (CaptureRequest.Key<Long>) Long.valueOf(l.getTime() / 1000));
        set((CaptureRequest.Key<CaptureRequest.Key<double[]>>) CaptureRequest.JPEG_GPS_COORDINATES, (CaptureRequest.Key<double[]>) coords);
        if (processMethod == null) {
            Log.w(TAG, "setGpsLocation - No process method, Location is not from a GPS or NETWORKprovider");
        } else {
            setBase((CaptureRequest.Key<CaptureRequest.Key<String>>) CaptureRequest.JPEG_GPS_PROCESSING_METHOD, (CaptureRequest.Key<String>) processMethod);
        }
        return true;
    }

    private void parseRecommendedConfigurations(RecommendedStreamConfiguration[] configurations, StreamConfigurationMap fullMap, boolean isDepth, ArrayList<ArrayList<StreamConfiguration>> streamConfigList, ArrayList<ArrayList<StreamConfigurationDuration>> streamDurationList, ArrayList<ArrayList<StreamConfigurationDuration>> streamStallList, boolean[] supportsPrivate) {
        int publicFormat;
        int i;
        int publicFormat2;
        int height;
        int width;
        int internalFormat;
        StreamConfigurationDuration minDurationConfiguration;
        int usecaseBitmap;
        int publicFormat3;
        Size sz;
        StreamConfigurationDuration stallDurationConfiguration;
        StreamConfigurationDuration minDurationConfiguration2;
        RecommendedStreamConfiguration[] recommendedStreamConfigurationArr = configurations;
        streamConfigList.ensureCapacity(32);
        streamDurationList.ensureCapacity(32);
        streamStallList.ensureCapacity(32);
        for (int i2 = 0; i2 < 32; i2++) {
            streamConfigList.add(new ArrayList<>());
            streamDurationList.add(new ArrayList<>());
            streamStallList.add(new ArrayList<>());
        }
        int publicFormat4 = recommendedStreamConfigurationArr.length;
        boolean z = false;
        int i3 = 0;
        while (i3 < publicFormat4) {
            RecommendedStreamConfiguration c = recommendedStreamConfigurationArr[i3];
            int width2 = c.getWidth();
            int height2 = c.getHeight();
            int internalFormat2 = c.getFormat();
            if (isDepth) {
                publicFormat = StreamConfigurationMap.depthFormatToPublic(internalFormat2);
            } else {
                publicFormat = StreamConfigurationMap.imageFormatToPublic(internalFormat2);
            }
            Size sz2 = new Size(width2, height2);
            int usecaseBitmap2 = c.getUsecaseBitmap();
            if (!c.isInput()) {
                StreamConfiguration streamConfiguration = new StreamConfiguration(internalFormat2, width2, height2, z);
                long minFrameDuration = fullMap.getOutputMinFrameDuration(publicFormat, sz2);
                if (minFrameDuration > 0) {
                    usecaseBitmap = usecaseBitmap2;
                    sz = sz2;
                    i = publicFormat4;
                    publicFormat3 = publicFormat;
                    internalFormat = internalFormat2;
                    width = width2;
                    height = height2;
                    minDurationConfiguration = new StreamConfigurationDuration(internalFormat2, width2, height2, minFrameDuration);
                } else {
                    i = publicFormat4;
                    usecaseBitmap = usecaseBitmap2;
                    sz = sz2;
                    publicFormat3 = publicFormat;
                    internalFormat = internalFormat2;
                    width = width2;
                    height = height2;
                    minDurationConfiguration = null;
                }
                long stallDuration = fullMap.getOutputStallDuration(publicFormat3, sz);
                if (stallDuration > 0) {
                    minDurationConfiguration2 = minDurationConfiguration;
                    stallDurationConfiguration = new StreamConfigurationDuration(internalFormat, width, height, stallDuration);
                } else {
                    minDurationConfiguration2 = minDurationConfiguration;
                    stallDurationConfiguration = null;
                }
                int i4 = 0;
                while (true) {
                    publicFormat2 = 32;
                    if (i4 >= 32) {
                        break;
                    }
                    if ((usecaseBitmap & (1 << i4)) != 0) {
                        streamConfigList.get(i4).add(streamConfiguration);
                        if (minFrameDuration > 0) {
                            streamDurationList.get(i4).add(minDurationConfiguration2);
                        }
                        if (stallDuration > 0) {
                            streamStallList.get(i4).add(stallDurationConfiguration);
                        }
                        if (supportsPrivate != null && !supportsPrivate[i4] && publicFormat3 == 34) {
                            supportsPrivate[i4] = true;
                        }
                    }
                    i4++;
                }
            } else {
                i = publicFormat4;
                publicFormat2 = 32;
                if (usecaseBitmap2 == 16) {
                    streamConfigList.get(4).add(new StreamConfiguration(internalFormat2, width2, height2, true));
                } else {
                    throw new IllegalArgumentException("Recommended input stream configurations should only be advertised in the ZSL use case!");
                }
            }
            i3++;
            recommendedStreamConfigurationArr = configurations;
            publicFormat4 = i;
            z = false;
        }
    }

    /* access modifiers changed from: private */
    public class StreamConfigurationData {
        StreamConfigurationDuration[] minDurationArray;
        StreamConfigurationDuration[] stallDurationArray;
        StreamConfiguration[] streamConfigurationArray;

        private StreamConfigurationData() {
            this.streamConfigurationArray = null;
            this.minDurationArray = null;
            this.stallDurationArray = null;
        }
    }

    public void initializeStreamConfigurationData(ArrayList<StreamConfiguration> sc, ArrayList<StreamConfigurationDuration> scd, ArrayList<StreamConfigurationDuration> scs, StreamConfigurationData scData) {
        if (scData != null && sc != null) {
            scData.streamConfigurationArray = new StreamConfiguration[sc.size()];
            scData.streamConfigurationArray = (StreamConfiguration[]) sc.toArray(scData.streamConfigurationArray);
            if (scd == null || scd.isEmpty()) {
                scData.minDurationArray = new StreamConfigurationDuration[0];
            } else {
                scData.minDurationArray = new StreamConfigurationDuration[scd.size()];
                scData.minDurationArray = (StreamConfigurationDuration[]) scd.toArray(scData.minDurationArray);
            }
            if (scs == null || scs.isEmpty()) {
                scData.stallDurationArray = new StreamConfigurationDuration[0];
                return;
            }
            scData.stallDurationArray = new StreamConfigurationDuration[scs.size()];
            scData.stallDurationArray = (StreamConfigurationDuration[]) scs.toArray(scData.stallDurationArray);
        }
    }

    public ArrayList<RecommendedStreamConfigurationMap> getRecommendedStreamConfigurations() {
        ArrayList<ArrayList<StreamConfigurationDuration>> streamDurationList;
        ArrayList<ArrayList<StreamConfigurationDuration>> streamStallList;
        boolean[] supportsPrivate;
        String str;
        ArrayList<ArrayList<StreamConfigurationDuration>> depthStreamDurationList;
        ArrayList<ArrayList<StreamConfigurationDuration>> depthStreamStallList;
        ArrayList<ArrayList<StreamConfiguration>> depthStreamConfigList;
        ArrayList<ArrayList<StreamConfigurationDuration>> streamStallList2;
        ArrayList<ArrayList<StreamConfigurationDuration>> streamDurationList2;
        ArrayList<ArrayList<StreamConfigurationDuration>> streamDurationList3;
        StreamConfigurationMap map;
        RecommendedStreamConfiguration[] configurations = (RecommendedStreamConfiguration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_RECOMMENDED_STREAM_CONFIGURATIONS);
        RecommendedStreamConfiguration[] depthConfigurations = (RecommendedStreamConfiguration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_RECOMMENDED_DEPTH_STREAM_CONFIGURATIONS);
        if (configurations == null && depthConfigurations == null) {
            return null;
        }
        StreamConfigurationMap fullMap = getStreamConfigurationMap();
        ArrayList<RecommendedStreamConfigurationMap> recommendedConfigurations = new ArrayList<>();
        ArrayList<ArrayList<StreamConfiguration>> streamConfigList = new ArrayList<>();
        ArrayList<ArrayList<StreamConfigurationDuration>> streamDurationList4 = new ArrayList<>();
        ArrayList<ArrayList<StreamConfigurationDuration>> streamStallList3 = new ArrayList<>();
        boolean[] supportsPrivate2 = new boolean[32];
        if (configurations != null) {
            str = TAG;
            supportsPrivate = supportsPrivate2;
            streamStallList = streamStallList3;
            streamDurationList = streamDurationList4;
            try {
                parseRecommendedConfigurations(configurations, fullMap, false, streamConfigList, streamDurationList4, streamStallList3, supportsPrivate);
            } catch (IllegalArgumentException e) {
                Log.e(str, "Failed parsing the recommended stream configurations!");
                return null;
            }
        } else {
            str = TAG;
            supportsPrivate = supportsPrivate2;
            streamStallList = streamStallList3;
            streamDurationList = streamDurationList4;
        }
        ArrayList<ArrayList<StreamConfiguration>> depthStreamConfigList2 = new ArrayList<>();
        ArrayList<ArrayList<StreamConfigurationDuration>> depthStreamDurationList2 = new ArrayList<>();
        ArrayList<ArrayList<StreamConfigurationDuration>> depthStreamStallList2 = new ArrayList<>();
        if (depthConfigurations != null) {
            depthStreamStallList = depthStreamStallList2;
            depthStreamDurationList = depthStreamDurationList2;
            depthStreamConfigList = depthStreamConfigList2;
            try {
                parseRecommendedConfigurations(depthConfigurations, fullMap, true, depthStreamConfigList2, depthStreamDurationList2, depthStreamStallList, null);
            } catch (IllegalArgumentException e2) {
                Log.e(str, "Failed parsing the recommended depth stream configurations!");
                return null;
            }
        } else {
            depthStreamStallList = depthStreamStallList2;
            depthStreamDurationList = depthStreamDurationList2;
            depthStreamConfigList = depthStreamConfigList2;
        }
        ReprocessFormatsMap inputOutputFormatsMap = (ReprocessFormatsMap) getBase(CameraCharacteristics.SCALER_AVAILABLE_RECOMMENDED_INPUT_OUTPUT_FORMATS_MAP);
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurations = (HighSpeedVideoConfiguration[]) getBase(CameraCharacteristics.CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS);
        boolean listHighResolution = isBurstSupported();
        recommendedConfigurations.ensureCapacity(32);
        int i = 0;
        for (int i2 = 32; i < i2; i2 = 32) {
            StreamConfigurationData scData = new StreamConfigurationData();
            if (configurations != null) {
                streamDurationList2 = streamDurationList;
                streamStallList2 = streamStallList;
                initializeStreamConfigurationData(streamConfigList.get(i), streamDurationList2.get(i), streamStallList2.get(i), scData);
            } else {
                streamStallList2 = streamStallList;
                streamDurationList2 = streamDurationList;
            }
            StreamConfigurationData depthScData = new StreamConfigurationData();
            if (depthConfigurations != null) {
                streamDurationList3 = streamDurationList2;
                initializeStreamConfigurationData(depthStreamConfigList.get(i), depthStreamDurationList.get(i), depthStreamStallList.get(i), depthScData);
            } else {
                streamDurationList3 = streamDurationList2;
            }
            if ((scData.streamConfigurationArray == null || scData.streamConfigurationArray.length == 0) && (depthScData.streamConfigurationArray == null || depthScData.streamConfigurationArray.length == 0)) {
                recommendedConfigurations.add(null);
            } else {
                if (i != 0) {
                    if (i == 1) {
                        map = new StreamConfigurationMap(scData.streamConfigurationArray, scData.minDurationArray, scData.stallDurationArray, null, null, null, null, null, null, null, null, null, highSpeedVideoConfigurations, null, listHighResolution, supportsPrivate[i]);
                    } else if (i != 2) {
                        if (i == 4) {
                            map = new StreamConfigurationMap(scData.streamConfigurationArray, scData.minDurationArray, scData.stallDurationArray, depthScData.streamConfigurationArray, depthScData.minDurationArray, depthScData.stallDurationArray, null, null, null, null, null, null, null, inputOutputFormatsMap, listHighResolution, supportsPrivate[i]);
                        } else if (!(i == 5 || i == 6)) {
                            map = new StreamConfigurationMap(scData.streamConfigurationArray, scData.minDurationArray, scData.stallDurationArray, depthScData.streamConfigurationArray, depthScData.minDurationArray, depthScData.stallDurationArray, null, null, null, null, null, null, null, null, listHighResolution, supportsPrivate[i]);
                        }
                    }
                    recommendedConfigurations.add(new RecommendedStreamConfigurationMap(map, i, supportsPrivate[i]));
                }
                map = new StreamConfigurationMap(scData.streamConfigurationArray, scData.minDurationArray, scData.stallDurationArray, null, null, null, null, null, null, null, null, null, null, null, listHighResolution, supportsPrivate[i]);
                recommendedConfigurations.add(new RecommendedStreamConfigurationMap(map, i, supportsPrivate[i]));
            }
            i++;
            streamStallList = streamStallList2;
            streamDurationList = streamDurationList3;
        }
        return recommendedConfigurations;
    }

    private boolean isBurstSupported() {
        for (int capability : (int[]) getBase(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)) {
            if (capability == 6) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MandatoryStreamCombination[] getMandatoryStreamCombinations() {
        int[] capabilities = (int[]) getBase(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        ArrayList<Integer> caps = new ArrayList<>();
        caps.ensureCapacity(capabilities.length);
        for (int c : capabilities) {
            caps.add(new Integer(c));
        }
        List<MandatoryStreamCombination> combs = new MandatoryStreamCombination.Builder(this.mCameraId, ((Integer) getBase(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue(), this.mDisplaySize, caps, getStreamConfigurationMap()).getAvailableMandatoryStreamCombinations();
        if (combs == null || combs.isEmpty()) {
            return null;
        }
        return (MandatoryStreamCombination[]) combs.toArray(new MandatoryStreamCombination[combs.size()]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private StreamConfigurationMap getStreamConfigurationMap() {
        return new StreamConfigurationMap((StreamConfiguration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_STREAM_CONFIGURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_MIN_FRAME_DURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_STALL_DURATIONS), (StreamConfiguration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_DEPTH_STREAM_CONFIGURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_DEPTH_MIN_FRAME_DURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_DEPTH_STALL_DURATIONS), (StreamConfiguration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_DYNAMIC_DEPTH_STREAM_CONFIGURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_DYNAMIC_DEPTH_MIN_FRAME_DURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.DEPTH_AVAILABLE_DYNAMIC_DEPTH_STALL_DURATIONS), (StreamConfiguration[]) getBase(CameraCharacteristics.HEIC_AVAILABLE_HEIC_STREAM_CONFIGURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.HEIC_AVAILABLE_HEIC_MIN_FRAME_DURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.HEIC_AVAILABLE_HEIC_STALL_DURATIONS), (HighSpeedVideoConfiguration[]) getBase(CameraCharacteristics.CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS), (ReprocessFormatsMap) getBase(CameraCharacteristics.SCALER_AVAILABLE_INPUT_OUTPUT_FORMATS_MAP), isBurstSupported());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> Integer getMaxRegions(Key<T> key) {
        int[] maxRegions = (int[]) getBase(CameraCharacteristics.CONTROL_MAX_REGIONS);
        if (maxRegions == null) {
            return null;
        }
        if (key.equals(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)) {
            return Integer.valueOf(maxRegions[0]);
        }
        if (key.equals(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB)) {
            return Integer.valueOf(maxRegions[1]);
        }
        if (key.equals(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)) {
            return Integer.valueOf(maxRegions[2]);
        }
        throw new AssertionError("Invalid key " + key);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> Integer getMaxNumOutputs(Key<T> key) {
        int[] maxNumOutputs = (int[]) getBase(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS);
        if (maxNumOutputs == null) {
            return null;
        }
        if (key.equals(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW)) {
            return Integer.valueOf(maxNumOutputs[0]);
        }
        if (key.equals(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC)) {
            return Integer.valueOf(maxNumOutputs[1]);
        }
        if (key.equals(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING)) {
            return Integer.valueOf(maxNumOutputs[2]);
        }
        throw new AssertionError("Invalid key " + key);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> TonemapCurve getTonemapCurve() {
        float[] red = (float[]) getBase(CaptureRequest.TONEMAP_CURVE_RED);
        float[] green = (float[]) getBase(CaptureRequest.TONEMAP_CURVE_GREEN);
        float[] blue = (float[]) getBase(CaptureRequest.TONEMAP_CURVE_BLUE);
        if (areValuesAllNull(red, green, blue)) {
            return null;
        }
        if (red != null && green != null && blue != null) {
            return new TonemapCurve(red, green, blue);
        }
        Log.w(TAG, "getTonemapCurve - missing tone curve components");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private OisSample[] getOisSamples() {
        long[] timestamps = (long[]) getBase(CaptureResult.STATISTICS_OIS_TIMESTAMPS);
        float[] xShifts = (float[]) getBase(CaptureResult.STATISTICS_OIS_X_SHIFTS);
        float[] yShifts = (float[]) getBase(CaptureResult.STATISTICS_OIS_Y_SHIFTS);
        if (timestamps == null) {
            if (xShifts != null) {
                throw new AssertionError("timestamps is null but xShifts is not");
            } else if (yShifts == null) {
                return null;
            } else {
                throw new AssertionError("timestamps is null but yShifts is not");
            }
        } else if (xShifts == null) {
            throw new AssertionError("timestamps is not null but xShifts is");
        } else if (yShifts == null) {
            throw new AssertionError("timestamps is not null but yShifts is");
        } else if (xShifts.length != timestamps.length) {
            throw new AssertionError(String.format("timestamps has %d entries but xShifts has %d", Integer.valueOf(timestamps.length), Integer.valueOf(xShifts.length)));
        } else if (yShifts.length == timestamps.length) {
            OisSample[] samples = new OisSample[timestamps.length];
            for (int i = 0; i < timestamps.length; i++) {
                samples[i] = new OisSample(timestamps[i], xShifts[i], yShifts[i]);
            }
            return samples;
        } else {
            throw new AssertionError(String.format("timestamps has %d entries but yShifts has %d", Integer.valueOf(timestamps.length), Integer.valueOf(yShifts.length)));
        }
    }

    private <T> void setBase(CameraCharacteristics.Key<T> key, T value) {
        setBase(key.getNativeKey(), value);
    }

    private <T> void setBase(CaptureResult.Key<T> key, T value) {
        setBase(key.getNativeKey(), value);
    }

    private <T> void setBase(CaptureRequest.Key<T> key, T value) {
        setBase(key.getNativeKey(), value);
    }

    private <T> void setBase(Key<T> key, T value) {
        int tag = nativeGetTagFromKeyLocal(key.getName());
        if (value == null) {
            writeValues(tag, null);
            return;
        }
        Marshaler<T> marshaler = getMarshalerForKey(key, nativeGetTypeFromTagLocal(tag));
        byte[] values = new byte[marshaler.calculateMarshalSize(value)];
        marshaler.marshal(value, ByteBuffer.wrap(values).order(ByteOrder.nativeOrder()));
        writeValues(tag, values);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setAvailableFormats(int[] value) {
        if (value == null) {
            return false;
        }
        int[] newValues = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            newValues[i] = value[i];
            if (value[i] == 256) {
                newValues[i] = 33;
            }
        }
        setBase((CameraCharacteristics.Key<CameraCharacteristics.Key<int[]>>) CameraCharacteristics.SCALER_AVAILABLE_FORMATS, (CameraCharacteristics.Key<int[]>) newValues);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setFaceRectangles(Rect[] faceRects) {
        if (faceRects == null) {
            return false;
        }
        Rect[] newFaceRects = new Rect[faceRects.length];
        for (int i = 0; i < newFaceRects.length; i++) {
            newFaceRects[i] = new Rect(faceRects[i].left, faceRects[i].top, faceRects[i].right + faceRects[i].left, faceRects[i].bottom + faceRects[i].top);
        }
        setBase((CaptureResult.Key<CaptureResult.Key<Rect[]>>) CaptureResult.STATISTICS_FACE_RECTANGLES, (CaptureResult.Key<Rect[]>) newFaceRects);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> boolean setTonemapCurve(TonemapCurve tc) {
        if (tc == null) {
            return false;
        }
        float[][] curve = new float[3][];
        for (int i = 0; i <= 2; i++) {
            curve[i] = new float[(tc.getPointCount(i) * 2)];
            tc.copyColorCurve(i, curve[i], 0);
        }
        setBase((CaptureRequest.Key<CaptureRequest.Key<float[]>>) CaptureRequest.TONEMAP_CURVE_RED, (CaptureRequest.Key<float[]>) curve[0]);
        setBase((CaptureRequest.Key<CaptureRequest.Key<float[]>>) CaptureRequest.TONEMAP_CURVE_GREEN, (CaptureRequest.Key<float[]>) curve[1]);
        setBase((CaptureRequest.Key<CaptureRequest.Key<float[]>>) CaptureRequest.TONEMAP_CURVE_BLUE, (CaptureRequest.Key<float[]>) curve[2]);
        return true;
    }

    public void setCameraId(int cameraId) {
        this.mCameraId = cameraId;
    }

    public void setDisplaySize(Size displaySize) {
        this.mDisplaySize = displaySize;
    }

    public void swap(CameraMetadataNative other) {
        nativeSwap(other);
        this.mCameraId = other.mCameraId;
        this.mDisplaySize = other.mDisplaySize;
    }

    public int getEntryCount() {
        return nativeGetEntryCount();
    }

    public boolean isEmpty() {
        return nativeIsEmpty();
    }

    public <K> ArrayList<K> getAllVendorKeys(Class<K> keyClass) {
        if (keyClass != null) {
            return nativeGetAllVendorKeys(keyClass);
        }
        throw new NullPointerException();
    }

    public static int getTag(String key) {
        return nativeGetTagFromKey(key, Long.MAX_VALUE);
    }

    public static int getTag(String key, long vendorId) {
        return nativeGetTagFromKey(key, vendorId);
    }

    public static int getNativeType(int tag, long vendorId) {
        return nativeGetTypeFromTag(tag, vendorId);
    }

    public void writeValues(int tag, byte[] src) {
        nativeWriteValues(tag, src);
    }

    public byte[] readValues(int tag) {
        return nativeReadValues(tag);
    }

    public void dumpToLog() {
        try {
            nativeDump();
        } catch (IOException e) {
            Log.wtf(TAG, "Dump logging failed", e);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private static <T> Marshaler<T> getMarshalerForKey(Key<T> key, int nativeType) {
        return MarshalRegistry.getMarshaler(key.getTypeReference(), nativeType);
    }

    private static void registerAllMarshalers() {
        for (MarshalQueryable query : new MarshalQueryable[]{new MarshalQueryablePrimitive(), new MarshalQueryableEnum(), new MarshalQueryableArray(), new MarshalQueryableBoolean(), new MarshalQueryableNativeByteToInteger(), new MarshalQueryableRect(), new MarshalQueryableSize(), new MarshalQueryableSizeF(), new MarshalQueryableString(), new MarshalQueryableReprocessFormatsMap(), new MarshalQueryableRange(), new MarshalQueryablePair(), new MarshalQueryableMeteringRectangle(), new MarshalQueryableColorSpaceTransform(), new MarshalQueryableStreamConfiguration(), new MarshalQueryableStreamConfigurationDuration(), new MarshalQueryableRggbChannelVector(), new MarshalQueryableBlackLevelPattern(), new MarshalQueryableHighSpeedVideoConfiguration(), new MarshalQueryableRecommendedStreamConfiguration(), new MarshalQueryableParcelable()}) {
            MarshalRegistry.registerMarshalQueryable(query);
        }
    }

    private static boolean areValuesAllNull(Object... objs) {
        for (Object o : objs) {
            if (o != null) {
                return false;
            }
        }
        return true;
    }
}
