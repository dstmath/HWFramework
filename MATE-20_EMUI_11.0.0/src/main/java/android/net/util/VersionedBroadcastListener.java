package android.net.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VersionedBroadcastListener {
    private static final boolean DBG = false;
    private final Consumer<Intent> mCallback;
    private final Context mContext;
    private final IntentFilter mFilter;
    private final AtomicInteger mGenerationNumber = new AtomicInteger(0);
    private final Handler mHandler;
    private BroadcastReceiver mReceiver;
    private final String mTag;

    public interface IntentCallback {
        void run(Intent intent);
    }

    public VersionedBroadcastListener(String tag, Context ctx, Handler handler, IntentFilter filter, Consumer<Intent> callback) {
        this.mTag = tag;
        this.mContext = ctx;
        this.mHandler = handler;
        this.mFilter = filter;
        this.mCallback = callback;
    }

    public void startListening() {
        if (this.mReceiver == null) {
            this.mReceiver = new Receiver(this.mTag, this.mGenerationNumber, this.mCallback);
            this.mContext.registerReceiver(this.mReceiver, this.mFilter, null, this.mHandler);
        }
    }

    public void stopListening() {
        if (this.mReceiver != null) {
            this.mGenerationNumber.incrementAndGet();
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    private static class Receiver extends BroadcastReceiver {
        public final AtomicInteger atomicGenerationNumber;
        public final Consumer<Intent> callback;
        public final int generationNumber;
        public final String tag;

        public Receiver(String tag2, AtomicInteger atomicGenerationNumber2, Consumer<Intent> callback2) {
            this.tag = tag2;
            this.atomicGenerationNumber = atomicGenerationNumber2;
            this.callback = callback2;
            this.generationNumber = atomicGenerationNumber2.incrementAndGet();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (this.generationNumber == this.atomicGenerationNumber.get()) {
                this.callback.accept(intent);
            }
        }
    }
}
