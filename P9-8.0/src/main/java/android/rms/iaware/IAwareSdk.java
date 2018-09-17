package android.rms.iaware;

import android.os.Parcel;

public class IAwareSdk {
    private static final int FIRST_ASYNC_CALL_TRANSACTION = 10001;
    private static final int FIRST_SYNC_CALL_TRANSACTION = 1;
    private static final int LAST_ASYNC_CALL_TRANSACTION = 16777215;
    private static final int LAST_SYNC_CALL_TRANSACTION = 10000;
    private static final int TRANSACTION_ASYNC_REPORT_DATA = 10001;
    private static final int TRANSACTION_REPORT_DATA = 1;

    private static void reportData(int resId, String message, boolean async) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInt(resId);
        data.writeLong(System.currentTimeMillis());
        data.writeString(message);
        IAwareSdkCore.handleEvent(async ? 10001 : 1, data, reply, resId);
        reply.recycle();
        data.recycle();
    }

    public static void reportData(int resId, String message, long timeStamp) {
        reportData(resId, message, false);
    }

    public static void asyncReportData(int resId, String message, long timeStamp) {
        reportData(resId, message, true);
    }
}
