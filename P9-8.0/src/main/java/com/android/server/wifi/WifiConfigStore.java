package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import com.android.internal.os.AtomicFile;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.wifi.hotspot2.CustPasspointConfigStoreData;
import com.android.server.wifi.util.PasspointUtil;
import com.android.server.wifi.util.XmlUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WifiConfigStore {
    private static final int BUFFERED_WRITE_ALARM_INTERVAL_MS = 10000;
    public static final String BUFFERED_WRITE_ALARM_TAG = "WriteBufferAlarm";
    private static final int CURRENT_CONFIG_STORE_DATA_VERSION = 1;
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    private static final int INITIAL_CONFIG_STORE_DATA_VERSION = 1;
    private static final String STORE_DIRECTORY_NAME = "wifi";
    private static final String STORE_FILE_NAME = "WifiConfigStore.xml";
    public static final String TAG = "WifiConfigStore";
    private static boolean VDBG = HWFLOW;
    private static boolean VVDBG = HWFLOW;
    private static final String XML_TAG_DOCUMENT_HEADER = "WifiConfigStoreData";
    private static final String XML_TAG_VERSION = "Version";
    private final AlarmManager mAlarmManager;
    private final OnAlarmListener mBufferedWriteListener = new OnAlarmListener() {
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
    private final Map<String, StoreData> mStoreDataList;
    private StoreFile mUserStore;
    private boolean mVerboseLoggingEnabled = false;

    public interface StoreData {
        void deserializeData(XmlPullParser xmlPullParser, int i, boolean z) throws XmlPullParserException, IOException;

        String getName();

        void resetData(boolean z);

        void serializeData(XmlSerializer xmlSerializer, boolean z) throws XmlPullParserException, IOException;

        boolean supportShareData();
    }

    public static class StoreFile {
        private static final int FILE_MODE = 384;
        private final AtomicFile mAtomicFile;
        private String mFileName = this.mAtomicFile.getBaseFile().getAbsolutePath();
        private byte[] mWriteData;

        public StoreFile(File file) {
            this.mAtomicFile = new AtomicFile(file);
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
            if (writeData == null) {
                Log.w(WifiConfigStore.TAG, "No data stored for writing to file: " + this.mFileName);
                return;
            }
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

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public WifiConfigStore(Context context, Looper looper, Clock clock, StoreFile sharedStore) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
        this.mStoreDataList = new HashMap();
        this.mSharedStore = sharedStore;
        this.mUserStore = null;
        this.mCustStore = PasspointUtil.createCustFile();
    }

    public void setUserStore(StoreFile userStore) {
        this.mUserStore = userStore;
    }

    public boolean registerStoreData(StoreData storeData) {
        if (storeData == null) {
            Log.e(TAG, "Unable to register null store data");
            return false;
        }
        this.mStoreDataList.put(storeData.getName(), storeData);
        return true;
    }

    private static StoreFile createFile(File storeBaseDir) {
        File storeDir = new File(storeBaseDir, STORE_DIRECTORY_NAME);
        if (!(storeDir.exists() || storeDir.mkdir())) {
            Log.w(TAG, "Could not create store directory " + storeDir);
        }
        return new StoreFile(new File(storeDir, STORE_FILE_NAME));
    }

    public static StoreFile createSharedFile() {
        return createFile(Environment.getDataMiscDirectory());
    }

    public static StoreFile createUserFile(int userId) {
        return createFile(Environment.getDataMiscCeDirectory(userId));
    }

    public void enableVerboseLogging(boolean verbose) {
        this.mVerboseLoggingEnabled = verbose;
    }

    public boolean areStoresPresent() {
        if (this.mSharedStore.exists() || (this.mUserStore != null && this.mUserStore.exists())) {
            return true;
        }
        return this.mCustStore != null ? this.mCustStore.exists() : false;
    }

    public void write(boolean forceSync) throws XmlPullParserException, IOException {
        this.mSharedStore.storeRawDataToWrite(serializeData(true));
        if (this.mUserStore != null) {
            this.mUserStore.storeRawDataToWrite(serializeData(false));
        }
        if (forceSync) {
            writeBufferedData();
        } else {
            startBufferedWriteAlarm();
        }
    }

    private byte[] serializeData(boolean shareData) throws XmlPullParserException, IOException {
        XmlSerializer out = new FastXmlSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        out.setOutput(outputStream, StandardCharsets.UTF_8.name());
        XmlUtil.writeDocumentStart(out, XML_TAG_DOCUMENT_HEADER);
        XmlUtil.writeNextValue(out, XML_TAG_VERSION, Integer.valueOf(1));
        for (Entry<String, StoreData> entry : this.mStoreDataList.entrySet()) {
            String tag = (String) entry.getKey();
            StoreData storeData = (StoreData) entry.getValue();
            if (!shareData || (storeData.supportShareData() ^ 1) == 0) {
                XmlUtil.writeNextSectionStart(out, tag);
                storeData.serializeData(out, shareData);
                XmlUtil.writeNextSectionEnd(out, tag);
            }
        }
        XmlUtil.writeDocumentEnd(out, XML_TAG_DOCUMENT_HEADER);
        return outputStream.toByteArray();
    }

    private void startBufferedWriteAlarm() {
        if (!this.mBufferedWritePending) {
            this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 10000, BUFFERED_WRITE_ALARM_TAG, this.mBufferedWriteListener, this.mEventHandler);
            this.mBufferedWritePending = true;
        }
    }

    private void stopBufferedWriteAlarm() {
        if (this.mBufferedWritePending) {
            this.mAlarmManager.cancel(this.mBufferedWriteListener);
            this.mBufferedWritePending = false;
        }
    }

    private void writeBufferedData() throws IOException {
        stopBufferedWriteAlarm();
        long writeStartTime = this.mClock.getElapsedSinceBootMillis();
        this.mSharedStore.writeBufferedRawData();
        if (this.mUserStore != null) {
            this.mUserStore.writeBufferedRawData();
        }
        Log.d(TAG, "Writing to stores completed in " + (this.mClock.getElapsedSinceBootMillis() - writeStartTime) + " ms.");
    }

    public void read() throws XmlPullParserException, IOException {
        resetStoreData(true);
        resetStoreData(false);
        long readStartTime = this.mClock.getElapsedSinceBootMillis();
        byte[] sharedDataBytes = this.mSharedStore.readRawData();
        byte[] userDataBytes = null;
        if (this.mUserStore != null) {
            userDataBytes = this.mUserStore.readRawData();
        }
        byte[] custDataBytes = null;
        if (this.mCustStore != null) {
            custDataBytes = this.mCustStore.readRawData();
        }
        Log.d(TAG, "Reading from stores completed in " + (this.mClock.getElapsedSinceBootMillis() - readStartTime) + " ms.");
        deserializeData(sharedDataBytes, true);
        deserializeData(userDataBytes, false);
        deserializeCustData(custDataBytes);
    }

    public void switchUserStoreAndRead(StoreFile userStore) throws XmlPullParserException, IOException {
        resetStoreData(false);
        stopBufferedWriteAlarm();
        this.mUserStore = userStore;
        long readStartTime = this.mClock.getElapsedSinceBootMillis();
        byte[] userDataBytes = this.mUserStore.readRawData();
        byte[] custDataBytes = null;
        if (this.mCustStore != null) {
            custDataBytes = this.mCustStore.readRawData();
        }
        Log.d(TAG, "Reading from user store completed in " + (this.mClock.getElapsedSinceBootMillis() - readStartTime) + " ms.");
        deserializeData(userDataBytes, false);
        deserializeCustData(custDataBytes);
    }

    private void resetStoreData(boolean shareData) {
        for (Entry<String, StoreData> entry : this.mStoreDataList.entrySet()) {
            ((StoreData) entry.getValue()).resetData(shareData);
        }
    }

    private void deserializeData(byte[] dataBytes, boolean shareData) throws XmlPullParserException, IOException {
        if (dataBytes != null) {
            XmlPullParser in = Xml.newPullParser();
            in.setInput(new ByteArrayInputStream(dataBytes), StandardCharsets.UTF_8.name());
            int rootTagDepth = in.getDepth() + 1;
            parseDocumentStartAndVersionFromXml(in);
            String[] headerName = new String[1];
            while (XmlUtil.gotoNextSectionOrEnd(in, headerName, rootTagDepth)) {
                StoreData storeData = (StoreData) this.mStoreDataList.get(headerName[0]);
                if (storeData == null) {
                    throw new XmlPullParserException("Unknown store data: " + headerName[0]);
                }
                storeData.deserializeData(in, rootTagDepth + 1, shareData);
            }
        }
    }

    private void deserializeCustData(byte[] dataBytes) throws XmlPullParserException, IOException {
        if (dataBytes != null) {
            XmlPullParser in = Xml.newPullParser();
            in.setInput(new ByteArrayInputStream(dataBytes), StandardCharsets.UTF_8.name());
            int rootTagDepth = in.getDepth() + 1;
            parseDocumentStartAndVersionFromXml(in);
            String[] headerName = new String[1];
            while (XmlUtil.gotoNextSectionOrEnd(in, headerName, rootTagDepth)) {
                StoreData storeData = (StoreData) this.mStoreDataList.get(headerName[0]);
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
}
