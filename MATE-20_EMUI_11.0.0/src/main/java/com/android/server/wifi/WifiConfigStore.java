package com.android.server.wifi;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.AtomicFile;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.hotspot2.CustPasspointConfigStoreData;
import com.android.server.wifi.hwUtil.PasspointUtil;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.DataIntegrityChecker;
import com.android.server.wifi.util.XmlUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WifiConfigStore {
    private static final int BUFFERED_WRITE_ALARM_INTERVAL_MS = 10000;
    @VisibleForTesting
    public static final String BUFFERED_WRITE_ALARM_TAG = "WriteBufferAlarm";
    private static final int CURRENT_CONFIG_STORE_DATA_VERSION = 1;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
    private static final int INITIAL_CONFIG_STORE_DATA_VERSION = 1;
    private static final String STORE_DIRECTORY_NAME = "wifi";
    private static final String STORE_FILE_NAME_SHARED_GENERAL = "WifiConfigStore.xml";
    private static final String STORE_FILE_NAME_USER_GENERAL = "WifiConfigStore.xml";
    private static final String STORE_FILE_NAME_USER_NETWORK_SUGGESTIONS = "WifiConfigStoreNetworkSuggestions.xml";
    public static final int STORE_FILE_SHARED_GENERAL = 0;
    public static final int STORE_FILE_USER_GENERAL = 1;
    public static final int STORE_FILE_USER_NETWORK_SUGGESTIONS = 2;
    private static final SparseArray<String> STORE_ID_TO_FILE_NAME = new SparseArray<String>() {
        /* class com.android.server.wifi.WifiConfigStore.AnonymousClass1 */

        {
            put(0, "WifiConfigStore.xml");
            put(1, "WifiConfigStore.xml");
            put(2, WifiConfigStore.STORE_FILE_NAME_USER_NETWORK_SUGGESTIONS);
        }
    };
    private static final String TAG = "WifiConfigStore";
    private static boolean VDBG = false;
    private static boolean VVDBG = false;
    private static final String XML_TAG_DOCUMENT_HEADER = "WifiConfigStoreData";
    private static final String XML_TAG_VERSION = "Version";
    private final AlarmManager mAlarmManager;
    private final AlarmManager.OnAlarmListener mBufferedWriteListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConfigStore.AnonymousClass2 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            try {
                WifiConfigStore.this.writeBufferedData();
            } catch (IOException e) {
                Log.wtf(WifiConfigStore.TAG, "Buffered write failed", e);
            }
        }
    };
    private boolean mBufferedWritePending = false;
    private final Clock mClock;
    private StoreFile mCustStore;
    private final Handler mEventHandler;
    private StoreFile mSharedStore;
    private final List<StoreData> mStoreDataList;
    private List<StoreFile> mUserStores;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiMetrics mWifiMetrics;

    public interface StoreData {
        void deserializeData(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException;

        String getName();

        int getStoreFileId();

        boolean hasNewDataToSerialize();

        void resetData();

        void serializeData(XmlSerializer xmlSerializer) throws XmlPullParserException, IOException;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface StoreFileId {
    }

    static {
        boolean z = HWFLOW;
        VDBG = z;
        VVDBG = z;
    }

    public WifiConfigStore(Context context, Looper looper, Clock clock, WifiMetrics wifiMetrics, StoreFile sharedStore) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
        this.mWifiMetrics = wifiMetrics;
        this.mStoreDataList = new ArrayList();
        this.mSharedStore = sharedStore;
        this.mUserStores = null;
        this.mCustStore = PasspointUtil.createCustFile();
    }

    public void setUserStores(List<StoreFile> userStores) {
        Preconditions.checkNotNull(userStores);
        this.mUserStores = userStores;
    }

    public boolean registerStoreData(StoreData storeData) {
        if (storeData == null) {
            Log.e(TAG, "Unable to register null store data");
            return false;
        }
        int storeFileId = storeData.getStoreFileId();
        if (STORE_ID_TO_FILE_NAME.get(storeFileId) == null) {
            Log.e(TAG, "Invalid shared store file specified" + storeFileId);
            return false;
        }
        this.mStoreDataList.add(storeData);
        return true;
    }

    private static StoreFile createFile(File storeBaseDir, int fileId) {
        File storeDir = new File(storeBaseDir, STORE_DIRECTORY_NAME);
        if (storeDir.exists() || storeDir.mkdir()) {
            return new StoreFile(new File(storeDir, STORE_ID_TO_FILE_NAME.get(fileId)), fileId);
        }
        Log.w(TAG, "Could not create store directory " + storeDir);
        return null;
    }

    public static StoreFile createSharedFile() {
        return createFile(Environment.getDataMiscDirectory(), 0);
    }

    public static List<StoreFile> createUserFiles(int userId) {
        List<StoreFile> storeFiles = new ArrayList<>();
        for (Integer num : Arrays.asList(1, 2)) {
            StoreFile storeFile = createFile(Environment.getDataMiscCeDirectory(userId), num.intValue());
            if (storeFile == null) {
                return null;
            }
            storeFiles.add(storeFile);
        }
        return storeFiles;
    }

    public void enableVerboseLogging(boolean verbose) {
        this.mVerboseLoggingEnabled = verbose;
    }

    public boolean areStoresPresent() {
        List<StoreFile> list;
        StoreFile storeFile;
        StoreFile storeFile2 = this.mSharedStore;
        if (storeFile2 == null) {
            Log.e(TAG, "areStoresPresent, mSharedStore is null");
            return false;
        } else if (storeFile2.exists() || (((list = this.mUserStores) != null && list.size() > 0) || ((storeFile = this.mCustStore) != null && storeFile.exists()))) {
            return true;
        } else {
            return false;
        }
    }

    private List<StoreData> retrieveStoreDataListForStoreFile(StoreFile storeFile) {
        return (List) this.mStoreDataList.stream().filter(new Predicate() {
            /* class com.android.server.wifi.$$Lambda$WifiConfigStore$Vf3hpICqGHc8cqcggYv9pHhYS2Y */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return WifiConfigStore.lambda$retrieveStoreDataListForStoreFile$0(WifiConfigStore.StoreFile.this, (WifiConfigStore.StoreData) obj);
            }
        }).collect(Collectors.toList());
    }

    static /* synthetic */ boolean lambda$retrieveStoreDataListForStoreFile$0(StoreFile storeFile, StoreData s) {
        return s.getStoreFileId() == storeFile.mFileId;
    }

    private boolean hasNewDataToSerialize(StoreFile storeFile) {
        return retrieveStoreDataListForStoreFile(storeFile).stream().anyMatch($$Lambda$WifiConfigStore$E5gj7z5ed5kRI6vVgdfi0Qq87ak.INSTANCE);
    }

    public void write(boolean forceSync) throws XmlPullParserException, IOException {
        StoreFile storeFile = this.mSharedStore;
        if (storeFile == null) {
            Log.e(TAG, "write, mSharedStore is null");
            return;
        }
        boolean hasAnyNewData = false;
        if (hasNewDataToSerialize(storeFile)) {
            this.mSharedStore.storeRawDataToWrite(serializeData(this.mSharedStore));
            hasAnyNewData = true;
        }
        List<StoreFile> list = this.mUserStores;
        if (list != null) {
            for (StoreFile userStoreFile : list) {
                if (hasNewDataToSerialize(userStoreFile)) {
                    userStoreFile.storeRawDataToWrite(serializeData(userStoreFile));
                    hasAnyNewData = true;
                }
            }
        }
        if (hasAnyNewData) {
            if (forceSync) {
                writeBufferedData();
            } else {
                startBufferedWriteAlarm();
            }
        } else if (forceSync && this.mBufferedWritePending) {
            writeBufferedData();
        }
    }

    private byte[] serializeData(StoreFile storeFile) throws XmlPullParserException, IOException {
        List<StoreData> storeDataList = retrieveStoreDataListForStoreFile(storeFile);
        XmlSerializer out = new FastXmlSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        out.setOutput(outputStream, StandardCharsets.UTF_8.name());
        XmlUtil.writeDocumentStart(out, XML_TAG_DOCUMENT_HEADER);
        XmlUtil.writeNextValue(out, XML_TAG_VERSION, 1);
        for (StoreData storeData : storeDataList) {
            String tag = storeData.getName();
            XmlUtil.writeNextSectionStart(out, tag);
            storeData.serializeData(out);
            XmlUtil.writeNextSectionEnd(out, tag);
        }
        XmlUtil.writeDocumentEnd(out, XML_TAG_DOCUMENT_HEADER);
        return outputStream.toByteArray();
    }

    private void startBufferedWriteAlarm() {
        if (!this.mBufferedWritePending) {
            this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS, BUFFERED_WRITE_ALARM_TAG, this.mBufferedWriteListener, this.mEventHandler);
            this.mBufferedWritePending = true;
        }
    }

    private void stopBufferedWriteAlarm() {
        if (this.mBufferedWritePending) {
            this.mAlarmManager.cancel(this.mBufferedWriteListener);
            this.mBufferedWritePending = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeBufferedData() throws IOException {
        Log.d(TAG, "Writing to stores started");
        stopBufferedWriteAlarm();
        long writeStartTime = this.mClock.getElapsedSinceBootMillis();
        StoreFile storeFile = this.mSharedStore;
        if (storeFile == null) {
            Log.e(TAG, "writeBufferedData, mSharedStore is null");
            return;
        }
        storeFile.writeBufferedRawData();
        List<StoreFile> list = this.mUserStores;
        if (list != null) {
            for (StoreFile userStoreFile : list) {
                userStoreFile.writeBufferedRawData();
            }
        }
        long writeTime = this.mClock.getElapsedSinceBootMillis() - writeStartTime;
        try {
            this.mWifiMetrics.noteWifiConfigStoreWriteDuration(Math.toIntExact(writeTime));
        } catch (ArithmeticException e) {
        }
        Log.i(TAG, "Writing to stores completed in " + writeTime + " ms.");
    }

    public void read() throws XmlPullParserException, IOException {
        StoreFile storeFile = this.mSharedStore;
        if (storeFile == null) {
            Log.e(TAG, "read, mSharedStore is null");
            return;
        }
        resetStoreData(storeFile);
        List<StoreFile> list = this.mUserStores;
        if (list != null) {
            for (StoreFile userStoreFile : list) {
                resetStoreData(userStoreFile);
            }
        }
        long readStartTime = this.mClock.getElapsedSinceBootMillis();
        deserializeData(this.mSharedStore.readRawData(), this.mSharedStore);
        List<StoreFile> list2 = this.mUserStores;
        if (list2 != null) {
            for (StoreFile userStoreFile2 : list2) {
                deserializeData(userStoreFile2.readRawData(), userStoreFile2);
            }
        }
        byte[] custDataBytes = null;
        StoreFile storeFile2 = this.mCustStore;
        if (storeFile2 != null) {
            custDataBytes = storeFile2.readRawData();
        }
        long readTime = this.mClock.getElapsedSinceBootMillis() - readStartTime;
        try {
            this.mWifiMetrics.noteWifiConfigStoreReadDuration(Math.toIntExact(readTime));
        } catch (ArithmeticException e) {
        }
        Log.i(TAG, "Reading from all stores completed in " + readTime + " ms.");
        deserializeCustData(custDataBytes);
    }

    public void switchUserStoresAndRead(List<StoreFile> userStores) throws XmlPullParserException, IOException {
        Preconditions.checkNotNull(userStores);
        List<StoreFile> list = this.mUserStores;
        if (list != null) {
            for (StoreFile userStoreFile : list) {
                resetStoreData(userStoreFile);
            }
        }
        stopBufferedWriteAlarm();
        this.mUserStores = userStores;
        long readStartTime = this.mClock.getElapsedSinceBootMillis();
        for (StoreFile userStoreFile2 : this.mUserStores) {
            deserializeData(userStoreFile2.readRawData(), userStoreFile2);
        }
        byte[] custDataBytes = null;
        StoreFile storeFile = this.mCustStore;
        if (storeFile != null) {
            custDataBytes = storeFile.readRawData();
        }
        long readTime = this.mClock.getElapsedSinceBootMillis() - readStartTime;
        this.mWifiMetrics.noteWifiConfigStoreReadDuration(Math.toIntExact(readTime));
        Log.i(TAG, "Reading from user stores completed in " + readTime + " ms.");
        deserializeCustData(custDataBytes);
    }

    private void resetStoreData(StoreFile storeFile) {
        for (StoreData storeData : retrieveStoreDataListForStoreFile(storeFile)) {
            storeData.resetData();
        }
    }

    private void indicateNoDataForStoreDatas(Collection<StoreData> storeDataSet) throws XmlPullParserException, IOException {
        for (StoreData storeData : storeDataSet) {
            storeData.deserializeData(null, 0);
        }
    }

    private void deserializeData(byte[] dataBytes, StoreFile storeFile) throws XmlPullParserException, IOException {
        List<StoreData> storeDataList = retrieveStoreDataListForStoreFile(storeFile);
        if (dataBytes == null) {
            indicateNoDataForStoreDatas(storeDataList);
            return;
        }
        XmlPullParser in = Xml.newPullParser();
        in.setInput(new ByteArrayInputStream(dataBytes), StandardCharsets.UTF_8.name());
        int rootTagDepth = in.getDepth() + 1;
        parseDocumentStartAndVersionFromXml(in);
        String[] headerName = new String[1];
        Set<StoreData> storeDatasInvoked = new HashSet<>();
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, rootTagDepth)) {
            StoreData storeData = storeDataList.stream().filter(new Predicate(headerName) {
                /* class com.android.server.wifi.$$Lambda$WifiConfigStore$az8WaXTZV3Kp3BFgTqqEllqrK4 */
                private final /* synthetic */ String[] f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ((WifiConfigStore.StoreData) obj).getName().equals(this.f$0[0]);
                }
            }).findAny().orElse(null);
            if (storeData != null) {
                storeData.deserializeData(in, rootTagDepth + 1);
                storeDatasInvoked.add(storeData);
            } else {
                throw new XmlPullParserException("Unknown store data: " + headerName[0] + ". List of store data: " + storeDataList);
            }
        }
        Set<StoreData> storeDatasNotInvoked = new HashSet<>(storeDataList);
        storeDatasNotInvoked.removeAll(storeDatasInvoked);
        indicateNoDataForStoreDatas(storeDatasNotInvoked);
    }

    private void deserializeCustData(byte[] dataBytes) throws XmlPullParserException, IOException {
        if (dataBytes != null) {
            XmlPullParser in = Xml.newPullParser();
            in.setInput(new ByteArrayInputStream(dataBytes), StandardCharsets.UTF_8.name());
            int rootTagDepth = in.getDepth() + 1;
            parseDocumentStartAndVersionFromXml(in);
            String[] headerName = new String[1];
            while (XmlUtil.gotoNextSectionOrEnd(in, headerName, rootTagDepth)) {
                StoreData storeData = this.mStoreDataList.stream().filter(new Predicate(headerName) {
                    /* class com.android.server.wifi.$$Lambda$WifiConfigStore$kc8CYYR0ABjNRZdg5MdfNVJNng */
                    private final /* synthetic */ String[] f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return ((WifiConfigStore.StoreData) obj).getName().equals(this.f$0[0]);
                    }
                }).findAny().orElse(null);
                if (storeData != null && (storeData instanceof CustPasspointConfigStoreData)) {
                    ((CustPasspointConfigStoreData) storeData).deserializeCustData(in, rootTagDepth + 1);
                }
            }
        }
    }

    private static int parseDocumentStartAndVersionFromXml(XmlPullParser in) throws XmlPullParserException, IOException {
        XmlUtil.gotoDocumentStart(in, XML_TAG_DOCUMENT_HEADER);
        int version = ((Integer) XmlUtil.readNextValueWithName(in, XML_TAG_VERSION)).intValue();
        if (version >= 1 && version <= 1) {
            return version;
        }
        throw new XmlPullParserException("Invalid version of data: " + version);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConfigStore");
        pw.println("WifiConfigStore - Store Data Begin ----");
        for (StoreData storeData : this.mStoreDataList) {
            pw.print("StoreData =>");
            pw.print(" ");
            pw.print("Name: " + storeData.getName());
            pw.print(", ");
            pw.print("File Id: " + storeData.getStoreFileId());
            pw.print(", ");
            pw.println("File Name: " + STORE_ID_TO_FILE_NAME.get(storeData.getStoreFileId()));
        }
        pw.println("WifiConfigStore - Store Data End ----");
    }

    public static class StoreFile {
        private static final int FILE_MODE = 384;
        private final AtomicFile mAtomicFile;
        private DataIntegrityChecker mDataIntegrityChecker = new DataIntegrityChecker(this.mFileName);
        private int mFileId;
        private String mFileName = this.mAtomicFile.getBaseFile().getAbsolutePath();
        private byte[] mWriteData;

        public StoreFile(File file, int fileId) {
            this.mAtomicFile = new AtomicFile(file);
            this.mFileId = fileId;
        }

        public boolean exists() {
            return this.mAtomicFile.exists();
        }

        public byte[] readRawData() throws IOException {
            try {
                return this.mAtomicFile.readFully();
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        public void storeRawDataToWrite(byte[] data) {
            this.mWriteData = data;
        }

        public void writeBufferedRawData() throws IOException {
            byte[] writeData = this.mWriteData;
            if (writeData != null) {
                FileOutputStream out = null;
                try {
                    out = this.mAtomicFile.startWrite();
                    FileUtils.setPermissions(this.mFileName, FILE_MODE, -1, -1);
                    out.write(writeData);
                    this.mAtomicFile.finishWrite(out);
                    this.mWriteData = null;
                } catch (IOException e) {
                    if (out != null) {
                        this.mAtomicFile.failWrite(out);
                    }
                    throw e;
                }
            }
        }
    }
}
