package android.test;

import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.ArrayBlockingQueue;

public class LoaderTestCase extends AndroidTestCase {

    /* renamed from: android.test.LoaderTestCase.2 */
    class AnonymousClass2 implements OnLoadCompleteListener<T> {
        final /* synthetic */ ArrayBlockingQueue val$queue;

        AnonymousClass2(ArrayBlockingQueue val$queue) {
            this.val$queue = val$queue;
        }

        public void onLoadComplete(Loader<T> completedLoader, T data) {
            completedLoader.unregisterListener(this);
            completedLoader.stopLoading();
            completedLoader.reset();
            this.val$queue.add(data);
        }
    }

    /* renamed from: android.test.LoaderTestCase.3 */
    class AnonymousClass3 extends Handler {
        final /* synthetic */ OnLoadCompleteListener val$listener;
        final /* synthetic */ Loader val$loader;

        AnonymousClass3(Looper $anonymous0, Loader val$loader, OnLoadCompleteListener val$listener) {
            this.val$loader = val$loader;
            this.val$listener = val$listener;
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            this.val$loader.registerListener(0, this.val$listener);
            this.val$loader.startLoading();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.test.LoaderTestCase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.test.LoaderTestCase.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.test.LoaderTestCase.<clinit>():void");
    }

    public <T> T getLoaderResultSynchronously(Loader<T> loader) {
        ArrayBlockingQueue<T> queue = new ArrayBlockingQueue(1);
        new AnonymousClass3(Looper.getMainLooper(), loader, new AnonymousClass2(queue)).sendEmptyMessage(0);
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("waiting thread interrupted", e);
        }
    }
}
