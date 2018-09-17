package com.android.server.rms.io;

import android.rms.utils.Utils;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.record.ResourceRecordStore;
import com.android.server.rms.record.ResourceUtils;
import com.android.server.rms.utils.Interrupt;
import java.util.ArrayList;
import java.util.List;

public class IOExceptionHandle {
    private static final int BIGDATA_FACTOR_APP_VALUE = 1048576;
    private static final int BIG_LOG_FACTOR_APP_VALUE = 1;
    public static final long BYTES_SIZE_1G = ((long) (Utils.DEBUG ? 10485760 : 1073741824));
    private static final String DEVICE_STATUS_PATH = "log";
    public static final int EXCEPTION_TYPE_APP_EXCEPTION = 6;
    public static final int EXCEPTION_TYPE_WRITE_TYPES = 1;
    public static final int IO_EXCEPTION_DAYS_LIMIT = (Utils.DEBUG ? 1 : 7);
    private static final String TAG = "RMS.IO.IOExceptionHandle";
    private static final long TIME_VALUES_MONTH = 2592000000L;
    private static final String WRITE_BYTES_FILE_PREFIX = "total_writebytes";
    private static final int WRITTEN_BYTES_EXCEPTION_DAYS_LIMIT = 7;
    private static final long WRITTEN_BYTES_EXCEPTION_LIMIT = (BYTES_SIZE_1G * 12);
    private IOStatsService mIOStatsService = null;
    private final Interrupt mInterrupt = new Interrupt();

    static class AppExceptionData {
        public String mPkgName;
        public long mStartTime;
        public long mTotalWrittenBytes;

        AppExceptionData() {
        }
    }

    interface CheckHandler {
        ExceptionData check();
    }

    static class CheckAppHandler implements CheckHandler {
        private IOExceptionHandle mIOExceptionHandle = null;

        public CheckAppHandler(IOExceptionHandle iOExceptionHandle) {
            this.mIOExceptionHandle = iOExceptionHandle;
        }

        public ExceptionData check() {
            List<AppExceptionData> appExceptionList = this.mIOExceptionHandle.checkExceptionOnApp();
            if (appExceptionList == null || appExceptionList.size() <= 0) {
                return null;
            }
            return new ExceptionData(6, appExceptionList);
        }
    }

    static class ExceptionData {
        public Object mData;
        public int mExceptionType;

        public ExceptionData(int exceptionType, Object data) {
            this.mExceptionType = exceptionType;
            this.mData = data;
        }
    }

    static class LifeTimeData {
        public int mLifeTime;
        public long mTime;

        public LifeTimeData(long time, int lifeTime) {
            this.mTime = time;
            this.mLifeTime = lifeTime;
        }
    }

    static class WriteBytesData {
        public long mTime;
        public long mWriteBytes;

        public WriteBytesData(long time, long writeBytes) {
            this.mTime = time;
            this.mWriteBytes = writeBytes;
        }
    }

    public IOExceptionHandle(IOStatsService iOStatsService) {
        this.mIOStatsService = iOStatsService;
        this.mInterrupt.reset();
    }

    private List<AppExceptionData> checkExceptionOnApp() {
        Log.i(TAG, "do the checking on the App");
        SparseArray<IOStatsHistory> historyList = this.mIOStatsService.getAllIOStatsCollection();
        if (historyList == null || historyList.size() == 0) {
            Log.e(TAG, "checkExceptionOnApp, the historyList is empty");
            return null;
        }
        List<AppExceptionData> exceptionList = new ArrayList();
        int historyListSize = historyList.size();
        for (int index = 0; index < historyListSize; index++) {
            AppExceptionData appExceptionData = ((IOStatsHistory) historyList.get(historyList.keyAt(index))).checkIfAppIOException();
            if (appExceptionData != null) {
                exceptionList.add(appExceptionData);
            }
        }
        if (exceptionList.size() > 0) {
            Log.i(TAG, "An App Exception occurs");
        } else if (exceptionList.size() == 0) {
            Log.i(TAG, "no error on the app");
        }
        return exceptionList;
    }

    public void interrupt() {
        this.mInterrupt.trigger();
    }

    public ExceptionData checkIfExistException() {
        List<CheckHandler> checkList = new ArrayList();
        checkList.add(new CheckAppHandler(this));
        ExceptionData result = null;
        for (CheckHandler checkHandler : checkList) {
            if (!this.mInterrupt.checkInterruptAndReset()) {
                result = checkHandler.check();
                if (result != null) {
                    break;
                }
            }
            Log.e(TAG, "checkIfExistException,interrupted by user");
            break;
        }
        return result;
    }

    public void handleIOException(ExceptionData exceptionData) {
        if (exceptionData == null) {
            Log.e(TAG, "handleIOException,the exceptionData is null");
            return;
        }
        Log.i(TAG, "handleIOException,the exception type:" + exceptionData.mExceptionType);
        if (this.mInterrupt.checkInterruptAndReset()) {
            Log.e(TAG, "handleIOException,interrupted by user");
            return;
        }
        switch (exceptionData.mExceptionType) {
            case 6:
                uploadAppException(exceptionData.mData);
                break;
        }
    }

    private void uploadAppException(Object exceptionData) {
        Iterable appExceptionList = null;
        boolean isInvalid = false;
        try {
            appExceptionList = (List) exceptionData;
        } catch (Exception ex) {
            Log.e(TAG, "uploadAppException,the exceptionData is invalid:" + ex.getMessage());
            isInvalid = true;
        }
        if (!isInvalid) {
            ResourceRecordStore resourceRecordStore = ResourceRecordStore.getInstance();
            for (AppExceptionData appExceptionData : appExceptionList) {
                ResourceUtils.uploadBigDataLogToIMonitor(25, appExceptionData.mPkgName, 1, (int) (appExceptionData.mTotalWrittenBytes / MemoryConstant.MB_SIZE));
                if (resourceRecordStore != null) {
                    resourceRecordStore.createAndCheckUploadBigDataInfos(-1, 25, appExceptionData.mPkgName, 1, 0, (int) (appExceptionData.mTotalWrittenBytes / MemoryConstant.MB_SIZE), null);
                }
            }
        }
    }
}
