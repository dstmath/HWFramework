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
import com.android.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

public class PrintFileDocumentAdapter extends PrintDocumentAdapter {
    private static final String LOG_TAG = "PrintedFileDocAdapter";
    private final Context mContext;
    private final PrintDocumentInfo mDocumentInfo;
    private final File mFile;
    private WriteFileAsyncTask mWriteFileAsyncTask;

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

    @Override // android.print.PrintDocumentAdapter
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, PrintDocumentAdapter.LayoutResultCallback callback, Bundle metadata) {
        callback.onLayoutFinished(this.mDocumentInfo, false);
    }

    @Override // android.print.PrintDocumentAdapter
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback callback) {
        this.mWriteFileAsyncTask = new WriteFileAsyncTask(destination, cancellationSignal, callback);
        this.mWriteFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    private final class WriteFileAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CancellationSignal mCancellationSignal;
        private final ParcelFileDescriptor mDestination;
        private final PrintDocumentAdapter.WriteResultCallback mResultCallback;

        public WriteFileAsyncTask(ParcelFileDescriptor destination, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback callback) {
            this.mDestination = destination;
            this.mResultCallback = callback;
            this.mCancellationSignal = cancellationSignal;
            this.mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener(PrintFileDocumentAdapter.this) {
                /* class android.print.PrintFileDocumentAdapter.WriteFileAsyncTask.AnonymousClass1 */

                @Override // android.os.CancellationSignal.OnCancelListener
                public void onCancel() {
                    WriteFileAsyncTask.this.cancel(true);
                }
            });
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
            $closeResource(r3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
            throw r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0031, code lost:
            throw r3;
         */
        public Void doInBackground(Void... params) {
            try {
                InputStream in = new FileInputStream(PrintFileDocumentAdapter.this.mFile);
                OutputStream out = new FileOutputStream(this.mDestination.getFileDescriptor());
                FileUtils.copy(in, out, this.mCancellationSignal, (Executor) null, (FileUtils.ProgressListener) null);
                $closeResource(null, out);
                $closeResource(null, in);
            } catch (OperationCanceledException e) {
            } catch (IOException e2) {
                Log.e(PrintFileDocumentAdapter.LOG_TAG, "Error writing data!", e2);
                this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cannot_write));
            }
            return null;
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
            this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cancelled));
        }
    }
}
