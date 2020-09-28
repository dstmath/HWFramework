package com.google.android.textclassifier;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AnnotatorModel implements AutoCloseable {
    static final String TYPE_ADDRESS = "address";
    static final String TYPE_DATE = "date";
    static final String TYPE_DATE_TIME = "datetime";
    static final String TYPE_EMAIL = "email";
    static final String TYPE_FLIGHT_NUMBER = "flight";
    static final String TYPE_OTHER = "other";
    static final String TYPE_PHONE = "phone";
    static final String TYPE_UNKNOWN = "";
    static final String TYPE_URL = "url";
    private long annotatorPtr;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private native AnnotatedSpan[] nativeAnnotate(long j, String str, AnnotationOptions annotationOptions);

    private native ClassificationResult[] nativeClassifyText(long j, String str, int i, int i2, ClassificationOptions classificationOptions, Object obj, String str2);

    private native void nativeCloseAnnotator(long j);

    private static native String nativeGetLocales(int i);

    private static native String nativeGetName(int i);

    private native long nativeGetNativeModelPtr(long j);

    private static native int nativeGetVersion(int i);

    private native boolean nativeInitializeContactEngine(long j, byte[] bArr);

    private native boolean nativeInitializeInstalledAppEngine(long j, byte[] bArr);

    private native boolean nativeInitializeKnowledgeEngine(long j, byte[] bArr);

    private native byte[] nativeLookUpKnowledgeEntity(long j, String str);

    private static native long nativeNewAnnotator(int i);

    private static native long nativeNewAnnotatorFromPath(String str);

    private native int[] nativeSuggestSelection(long j, String str, int i, int i2, SelectionOptions selectionOptions);

    static {
        System.loadLibrary("textclassifier");
    }

    public enum AnnotationUsecase {
        SMART(0),
        RAW(1);
        
        private final int value;

        private AnnotationUsecase(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }
    }

    public AnnotatorModel(int fileDescriptor) {
        this.annotatorPtr = nativeNewAnnotator(fileDescriptor);
        if (this.annotatorPtr == 0) {
            throw new IllegalArgumentException("Couldn't initialize TC from file descriptor.");
        }
    }

    public AnnotatorModel(String path) {
        this.annotatorPtr = nativeNewAnnotatorFromPath(path);
        if (this.annotatorPtr == 0) {
            throw new IllegalArgumentException("Couldn't initialize TC from given file.");
        }
    }

    public void initializeKnowledgeEngine(byte[] serializedConfig) {
        if (!nativeInitializeKnowledgeEngine(this.annotatorPtr, serializedConfig)) {
            throw new IllegalArgumentException("Couldn't initialize the KG engine");
        }
    }

    public void initializeContactEngine(byte[] serializedConfig) {
        if (!nativeInitializeContactEngine(this.annotatorPtr, serializedConfig)) {
            throw new IllegalArgumentException("Couldn't initialize the contact engine");
        }
    }

    public void initializeInstalledAppEngine(byte[] serializedConfig) {
        if (!nativeInitializeInstalledAppEngine(this.annotatorPtr, serializedConfig)) {
            throw new IllegalArgumentException("Couldn't initialize the installed app engine");
        }
    }

    public int[] suggestSelection(String context, int selectionBegin, int selectionEnd, SelectionOptions options) {
        return nativeSuggestSelection(this.annotatorPtr, context, selectionBegin, selectionEnd, options);
    }

    public ClassificationResult[] classifyText(String context, int selectionBegin, int selectionEnd, ClassificationOptions options) {
        return classifyText(context, selectionBegin, selectionEnd, options, null, null);
    }

    public ClassificationResult[] classifyText(String context, int selectionBegin, int selectionEnd, ClassificationOptions options, Object appContext, String deviceLocales) {
        return nativeClassifyText(this.annotatorPtr, context, selectionBegin, selectionEnd, options, appContext, deviceLocales);
    }

    public AnnotatedSpan[] annotate(String text, AnnotationOptions options) {
        return nativeAnnotate(this.annotatorPtr, text, options);
    }

    public byte[] lookUpKnowledgeEntity(String id) {
        return nativeLookUpKnowledgeEntity(this.annotatorPtr, id);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (this.isClosed.compareAndSet(false, true)) {
            nativeCloseAnnotator(this.annotatorPtr);
            this.annotatorPtr = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public static String getLocales(int fd) {
        return nativeGetLocales(fd);
    }

    public static int getVersion(int fd) {
        return nativeGetVersion(fd);
    }

    public static String getName(int fd) {
        return nativeGetName(fd);
    }

    public static final class DatetimeResult {
        public static final int GRANULARITY_DAY = 3;
        public static final int GRANULARITY_HOUR = 4;
        public static final int GRANULARITY_MINUTE = 5;
        public static final int GRANULARITY_MONTH = 1;
        public static final int GRANULARITY_SECOND = 6;
        public static final int GRANULARITY_WEEK = 2;
        public static final int GRANULARITY_YEAR = 0;
        private final int granularity;
        private final long timeMsUtc;

        public DatetimeResult(long timeMsUtc2, int granularity2) {
            this.timeMsUtc = timeMsUtc2;
            this.granularity = granularity2;
        }

        public long getTimeMsUtc() {
            return this.timeMsUtc;
        }

        public int getGranularity() {
            return this.granularity;
        }
    }

    public static final class ClassificationResult {
        private final String appName;
        private final String appPackageName;
        private final String collection;
        private final String contactEmailAddress;
        private final String contactGivenName;
        private final String contactId;
        private final String contactName;
        private final String contactNickname;
        private final String contactPhoneNumber;
        private final DatetimeResult datetimeResult;
        private final long durationMs;
        private final NamedVariant[] entityData;
        private final long numericValue;
        private final RemoteActionTemplate[] remoteActionTemplates;
        private final float score;
        private final byte[] serializedEntityData;
        private final byte[] serializedKnowledgeResult;

        public ClassificationResult(String collection2, float score2, DatetimeResult datetimeResult2, byte[] serializedKnowledgeResult2, String contactName2, String contactGivenName2, String contactNickname2, String contactEmailAddress2, String contactPhoneNumber2, String contactId2, String appName2, String appPackageName2, NamedVariant[] entityData2, byte[] serializedEntityData2, RemoteActionTemplate[] remoteActionTemplates2, long durationMs2, long numericValue2) {
            this.collection = collection2;
            this.score = score2;
            this.datetimeResult = datetimeResult2;
            this.serializedKnowledgeResult = serializedKnowledgeResult2;
            this.contactName = contactName2;
            this.contactGivenName = contactGivenName2;
            this.contactNickname = contactNickname2;
            this.contactEmailAddress = contactEmailAddress2;
            this.contactPhoneNumber = contactPhoneNumber2;
            this.contactId = contactId2;
            this.appName = appName2;
            this.appPackageName = appPackageName2;
            this.entityData = entityData2;
            this.serializedEntityData = serializedEntityData2;
            this.remoteActionTemplates = remoteActionTemplates2;
            this.durationMs = durationMs2;
            this.numericValue = numericValue2;
        }

        public String getCollection() {
            return this.collection;
        }

        public float getScore() {
            return this.score;
        }

        public DatetimeResult getDatetimeResult() {
            return this.datetimeResult;
        }

        public byte[] getSerializedKnowledgeResult() {
            return this.serializedKnowledgeResult;
        }

        public String getContactName() {
            return this.contactName;
        }

        public String getContactGivenName() {
            return this.contactGivenName;
        }

        public String getContactNickname() {
            return this.contactNickname;
        }

        public String getContactEmailAddress() {
            return this.contactEmailAddress;
        }

        public String getContactPhoneNumber() {
            return this.contactPhoneNumber;
        }

        public String getContactId() {
            return this.contactId;
        }

        public String getAppName() {
            return this.appName;
        }

        public String getAppPackageName() {
            return this.appPackageName;
        }

        public NamedVariant[] getEntityData() {
            return this.entityData;
        }

        public byte[] getSerializedEntityData() {
            return this.serializedEntityData;
        }

        public RemoteActionTemplate[] getRemoteActionTemplates() {
            return this.remoteActionTemplates;
        }

        public long getDurationMs() {
            return this.durationMs;
        }

        public long getNumericValue() {
            return this.numericValue;
        }
    }

    public static final class AnnotatedSpan {
        private final ClassificationResult[] classification;
        private final int endIndex;
        private final int startIndex;

        AnnotatedSpan(int startIndex2, int endIndex2, ClassificationResult[] classification2) {
            this.startIndex = startIndex2;
            this.endIndex = endIndex2;
            this.classification = classification2;
        }

        public int getStartIndex() {
            return this.startIndex;
        }

        public int getEndIndex() {
            return this.endIndex;
        }

        public ClassificationResult[] getClassification() {
            return this.classification;
        }
    }

    public static final class SelectionOptions {
        private final int annotationUsecase;
        private final String detectedTextLanguageTags;
        private final String locales;

        public SelectionOptions(String locales2, String detectedTextLanguageTags2, int annotationUsecase2) {
            this.locales = locales2;
            this.detectedTextLanguageTags = detectedTextLanguageTags2;
            this.annotationUsecase = annotationUsecase2;
        }

        public SelectionOptions(String locales2, String detectedTextLanguageTags2) {
            this(locales2, detectedTextLanguageTags2, AnnotationUsecase.SMART.getValue());
        }

        public String getLocales() {
            return this.locales;
        }

        public String getDetectedTextLanguageTags() {
            return this.detectedTextLanguageTags;
        }

        public int getAnnotationUsecase() {
            return this.annotationUsecase;
        }
    }

    public static final class ClassificationOptions {
        private final int annotationUsecase;
        private final String detectedTextLanguageTags;
        private final String locales;
        private final long referenceTimeMsUtc;
        private final String referenceTimezone;

        public ClassificationOptions(long referenceTimeMsUtc2, String referenceTimezone2, String locales2, String detectedTextLanguageTags2, int annotationUsecase2) {
            this.referenceTimeMsUtc = referenceTimeMsUtc2;
            this.referenceTimezone = referenceTimezone2;
            this.locales = locales2;
            this.detectedTextLanguageTags = detectedTextLanguageTags2;
            this.annotationUsecase = annotationUsecase2;
        }

        public ClassificationOptions(long referenceTimeMsUtc2, String referenceTimezone2, String locales2, String detectedTextLanguageTags2) {
            this(referenceTimeMsUtc2, referenceTimezone2, locales2, detectedTextLanguageTags2, AnnotationUsecase.SMART.getValue());
        }

        public long getReferenceTimeMsUtc() {
            return this.referenceTimeMsUtc;
        }

        public String getReferenceTimezone() {
            return this.referenceTimezone;
        }

        public String getLocale() {
            return this.locales;
        }

        public String getDetectedTextLanguageTags() {
            return this.detectedTextLanguageTags;
        }

        public int getAnnotationUsecase() {
            return this.annotationUsecase;
        }
    }

    public static final class AnnotationOptions {
        private final int annotationUsecase;
        private final String detectedTextLanguageTags;
        private final String[] entityTypes;
        private final boolean isSerializedEntityDataEnabled;
        private final String locales;
        private final long referenceTimeMsUtc;
        private final String referenceTimezone;

        public AnnotationOptions(long referenceTimeMsUtc2, String referenceTimezone2, String locales2, String detectedTextLanguageTags2, Collection<String> entityTypes2, int annotationUsecase2, boolean isSerializedEntityDataEnabled2) {
            this.referenceTimeMsUtc = referenceTimeMsUtc2;
            this.referenceTimezone = referenceTimezone2;
            this.locales = locales2;
            this.detectedTextLanguageTags = detectedTextLanguageTags2;
            String[] strArr = new String[0];
            this.entityTypes = entityTypes2 != null ? (String[]) entityTypes2.toArray(strArr) : strArr;
            this.annotationUsecase = annotationUsecase2;
            this.isSerializedEntityDataEnabled = isSerializedEntityDataEnabled2;
        }

        public AnnotationOptions(long referenceTimeMsUtc2, String referenceTimezone2, String locales2, String detectedTextLanguageTags2) {
            this(referenceTimeMsUtc2, referenceTimezone2, locales2, detectedTextLanguageTags2, null, AnnotationUsecase.SMART.getValue(), false);
        }

        public long getReferenceTimeMsUtc() {
            return this.referenceTimeMsUtc;
        }

        public String getReferenceTimezone() {
            return this.referenceTimezone;
        }

        public String getLocale() {
            return this.locales;
        }

        public String getDetectedTextLanguageTags() {
            return this.detectedTextLanguageTags;
        }

        public String[] getEntityTypes() {
            return this.entityTypes;
        }

        public int getAnnotationUsecase() {
            return this.annotationUsecase;
        }

        public boolean isSerializedEntityDataEnabled() {
            return this.isSerializedEntityDataEnabled;
        }
    }

    /* access modifiers changed from: package-private */
    public long getNativeAnnotator() {
        return nativeGetNativeModelPtr(this.annotatorPtr);
    }
}
