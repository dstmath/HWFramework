package android.print;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.FileUtils;
import android.os.OperationCanceledException;
import android.os.ParcelFileDescriptor;
import android.print.PrintDocumentAdapter;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PrintFileDocumentAdapter extends PrintDocumentAdapter {
    private static final String LOG_TAG = "PrintedFileDocAdapter";
    /* access modifiers changed from: private */
    public final Context mContext;
    private final PrintDocumentInfo mDocumentInfo;
    /* access modifiers changed from: private */
    public final File mFile;
    private WriteFileAsyncTask mWriteFileAsyncTask;

    private final class WriteFileAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CancellationSignal mCancellationSignal;
        private final ParcelFileDescriptor mDestination;
        private final PrintDocumentAdapter.WriteResultCallback mResultCallback;

        public WriteFileAsyncTask(ParcelFileDescriptor destination, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback callback) {
            this.mDestination = destination;
            this.mResultCallback = callback;
            this.mCancellationSignal = cancellationSignal;
            this.mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener(PrintFileDocumentAdapter.this) {
                public void onCancel() {
                    WriteFileAsyncTask.this.cancel(true);
                }
            });
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0024, code lost:
            r3 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0025, code lost:
            r4 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x002a, code lost:
            r5 = r4;
            r4 = r3;
            r3 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
            r2 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0032, code lost:
            r3 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0036, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0037, code lost:
            r5 = r3;
            r3 = r2;
            r2 = r5;
         */
        public Void doInBackground(Void... params) {
            InputStream in;
            Throwable th;
            Throwable th2;
            OutputStream out;
            Throwable th3;
            Throwable th4;
            try {
                in = new FileInputStream(PrintFileDocumentAdapter.this.mFile);
                out = new FileOutputStream(this.mDestination.getFileDescriptor());
                FileUtils.copy(in, out, (FileUtils.ProgressListener) null, this.mCancellationSignal);
                $closeResource(null, out);
                $closeResource(null, in);
            } catch (OperationCanceledException e) {
            } catch (IOException e2) {
                Log.e(PrintFileDocumentAdapter.LOG_TAG, "Error writing data!", e2);
                this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(17041432));
            }
            return null;
            $closeResource(th, in);
            throw th2;
            $closeResource(th3, out);
            throw th4;
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void result) {
            this.mResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        }

        /* access modifiers changed from: protected */
        public void onCancelled(Void result) {
            this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(17041431));
        }
    }

    public PrintFileDocumentAdapter(Context context, File file, PrintDocumentInfo documentInfo) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null!");
        } else if (documentInfo != null) {
            this.mContext = context;
            this.mFile = file;
            this.mDocumentInfo = documentInfo;
        } else {
            throw new IllegalArgumentException("documentInfo cannot be null!");
        }
    }

    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, PrintDocumentAdapter.LayoutResultCallback callback, Bundle metadata) {
        callback.onLayoutFinished(this.mDocumentInfo, false);
    }

    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback callback) {
        this.mWriteFileAsyncTask = new WriteFileAsyncTask(destination, cancellationSignal, callback);
        this.mWriteFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }
}
