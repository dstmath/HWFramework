package android.test;

import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.ArrayBlockingQueue;

public class LoaderTestCase extends AndroidTestCase {
    static {
        AnonymousClass1 anonymousClass1 = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... args) {
                return null;
            }

            protected void onPostExecute(Void result) {
            }
        };
    }

    public <T> T getLoaderResultSynchronously(final Loader<T> loader) {
        final ArrayBlockingQueue<T> queue = new ArrayBlockingQueue(1);
        final OnLoadCompleteListener<T> listener = new OnLoadCompleteListener<T>() {
            public void onLoadComplete(Loader<T> completedLoader, T data) {
                completedLoader.unregisterListener(this);
                completedLoader.stopLoading();
                completedLoader.reset();
                queue.add(data);
            }
        };
        new Handler(Looper.getMainLooper()) {
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
