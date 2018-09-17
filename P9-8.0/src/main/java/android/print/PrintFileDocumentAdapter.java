package android.print;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;
import android.util.Log;
import com.android.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import libcore.io.IoUtils;

public class PrintFileDocumentAdapter extends PrintDocumentAdapter {
    private static final String LOG_TAG = "PrintedFileDocAdapter";
    private final Context mContext;
    private final PrintDocumentInfo mDocumentInfo;
    private final File mFile;
    private WriteFileAsyncTask mWriteFileAsyncTask;

    private final class WriteFileAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CancellationSignal mCancellationSignal;
        private final ParcelFileDescriptor mDestination;
        private final WriteResultCallback mResultCallback;

        public WriteFileAsyncTask(ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
            this.mDestination = destination;
            this.mResultCallback = callback;
            this.mCancellationSignal = cancellationSignal;
            this.mCancellationSignal.setOnCancelListener(new OnCancelListener() {
                public void onCancel() {
                    WriteFileAsyncTask.this.cancel(true);
                }
            });
        }

        protected Void doInBackground(Void... params) {
            IOException ioe;
            Throwable th;
            AutoCloseable in = null;
            OutputStream out = new FileOutputStream(this.mDestination.getFileDescriptor());
            byte[] buffer = new byte[8192];
            try {
                InputStream in2 = new FileInputStream(PrintFileDocumentAdapter.this.mFile);
                while (!isCancelled()) {
                    try {
                        int readByteCount = in2.read(buffer);
                        if (readByteCount < 0) {
                            break;
                        }
                        out.write(buffer, 0, readByteCount);
                    } catch (IOException e) {
                        ioe = e;
                        in = in2;
                    } catch (Throwable th2) {
                        th = th2;
                        Object in3 = in2;
                    }
                }
                IoUtils.closeQuietly(in2);
                IoUtils.closeQuietly(out);
                InputStream inputStream = in2;
            } catch (IOException e2) {
                ioe = e2;
                try {
                    Log.e(PrintFileDocumentAdapter.LOG_TAG, "Error writing data!", ioe);
                    this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cannot_write));
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(out);
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(out);
                    throw th;
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            this.mResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        }

        protected void onCancelled(Void result) {
            this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cancelled));
        }
    }

    public PrintFileDocumentAdapter(Context context, File file, PrintDocumentInfo documentInfo) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null!");
        } else if (documentInfo == null) {
            throw new IllegalArgumentException("documentInfo cannot be null!");
        } else {
            this.mContext = context;
            this.mFile = file;
            this.mDocumentInfo = documentInfo;
        }
    }

    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle metadata) {
        callback.onLayoutFinished(this.mDocumentInfo, false);
    }

    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        this.mWriteFileAsyncTask = new WriteFileAsyncTask(destination, cancellationSignal, callback);
        this.mWriteFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }
}
