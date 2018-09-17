package android.printservice;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.print.PrintDocumentInfo;
import android.print.PrintJobId;
import android.util.Log;
import java.io.IOException;

public final class PrintDocument {
    private static final String LOG_TAG = "PrintDocument";
    private final PrintDocumentInfo mInfo;
    private final PrintJobId mPrintJobId;
    private final IPrintServiceClient mPrintServiceClient;

    PrintDocument(PrintJobId printJobId, IPrintServiceClient printServiceClient, PrintDocumentInfo info) {
        this.mPrintJobId = printJobId;
        this.mPrintServiceClient = printServiceClient;
        this.mInfo = info;
    }

    public PrintDocumentInfo getInfo() {
        PrintService.throwIfNotCalledOnMainThread();
        return this.mInfo;
    }

    public ParcelFileDescriptor getData() {
        PrintService.throwIfNotCalledOnMainThread();
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor source = fds[0];
            parcelFileDescriptor = fds[1];
            this.mPrintServiceClient.writePrintJobData(parcelFileDescriptor, this.mPrintJobId);
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                }
            }
            return source;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error calling getting print job data!", ioe);
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e2) {
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error calling getting print job data!", re);
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e4) {
                }
            }
        }
        return null;
    }
}
