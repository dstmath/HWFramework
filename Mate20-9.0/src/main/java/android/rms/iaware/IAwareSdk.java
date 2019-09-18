package android.rms.iaware;

import android.os.IBinder;
import android.os.Parcel;

public class IAwareSdk {
    private static final int TRANSACTION_ASYNC_REPORT_DATA = 10001;
    private static final int TRANSACTION_ASYNC_REPORT_DATA_WITH_CALLBACK = 10002;
    private static final int TRANSACTION_REPORT_DATA = 1;
    private static final int TRANSACTION_RIGSTER_FG_APP_TYPE_WITH_CALLBACK = 10003;

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

    public static void asyncReportDataWithCallback(int resId, String message, IBinder obj, long timeStamp) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInt(resId);
        data.writeLong(System.currentTimeMillis());
        data.writeString(message);
        data.writeStrongBinder(obj);
        IAwareSdkCore.handleEvent(10002, data, reply, resId);
        reply.recycle();
        data.recycle();
    }

    public static void rigsterForegroundAppTypeWithCallback(IForegroundAppTypeCallback obj) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeStrongBinder(obj);
        IAwareSdkCore.handleEvent(10003, data, reply);
        reply.recycle();
        data.recycle();
    }
}
