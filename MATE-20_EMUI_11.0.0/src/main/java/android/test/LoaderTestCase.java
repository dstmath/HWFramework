package android.test;

import android.content.Loader;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.ArrayBlockingQueue;

public class LoaderTestCase extends AndroidTestCase {
    static {
        new AsyncTask<Void, Void, Void>() {
            /* class android.test.LoaderTestCase.AnonymousClass1 */

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... args) {
                return null;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void result) {
            }
        };
    }

    public <T> T getLoaderResultSynchronously(final Loader<T> loader) {
        final ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(1);
        final Loader.OnLoadCompleteListener<T> listener = new Loader.OnLoadCompleteListener<T>() {
            /* class android.test.LoaderTestCase.AnonymousClass2 */

            @Override // android.content.Loader.OnLoadCompleteListener
            public void onLoadComplete(Loader<T> completedLoader, T data) {
                completedLoader.unregisterListener(this);
                completedLoader.stopLoading();
                completedLoader.reset();
                queue.add(data);
            }
        };
        new Handler(Looper.getMainLooper()) {
            /* class android.test.LoaderTestCase.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                loader.registerListener(0, listener);
                loader.startLoading();
            }
        }.sendEmptyMessage(0);
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("waiting thread interrupted", e);
        }
    }
}
