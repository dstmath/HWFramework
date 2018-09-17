package android.printservice;

import android.graphics.drawable.Icon;
import android.os.RemoteException;
import android.print.PrinterId;
import android.util.Log;

public final class CustomPrinterIconCallback {
    private static final String LOG_TAG = "CustomPrinterIconCB";
    private final IPrintServiceClient mObserver;
    private final PrinterId mPrinterId;

    CustomPrinterIconCallback(PrinterId printerId, IPrintServiceClient observer) {
        this.mPrinterId = printerId;
        this.mObserver = observer;
    }

    public boolean onCustomPrinterIconLoaded(Icon icon) {
        try {
            this.mObserver.onCustomPrinterIconLoaded(this.mPrinterId, icon);
            return true;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Could not update icon", e);
            return false;
        }
    }
}
