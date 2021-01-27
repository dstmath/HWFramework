package android.speech.tts;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/* access modifiers changed from: package-private */
public class AudioPlaybackHandler {
    private static final boolean DBG = false;
    private static final String TAG = "TTS.AudioPlaybackHandler";
    private volatile PlaybackQueueItem mCurrentWorkItem = null;
    private final Thread mHandlerThread = new Thread(new MessageLoop(), "TTS.AudioPlaybackThread");
    private final LinkedBlockingQueue<PlaybackQueueItem> mQueue = new LinkedBlockingQueue<>();

    AudioPlaybackHandler() {
    }

    public void start() {
        this.mHandlerThread.start();
    }

    private void stop(PlaybackQueueItem item) {
        if (item != null) {
            item.stop(-2);
        }
    }

    public void enqueue(PlaybackQueueItem item) {
        try {
            this.mQueue.put(item);
        } catch (InterruptedException e) {
        }
    }

    public void stopForApp(Object callerIdentity) {
        removeWorkItemsFor(callerIdentity);
        PlaybackQueueItem current = this.mCurrentWorkItem;
        if (current != null && current.getCallerIdentity() == callerIdentity) {
            stop(current);
        }
    }

    public void stop() {
        removeAllMessages();
        stop(this.mCurrentWorkItem);
    }

    public boolean isSpeaking() {
        return (this.mQueue.peek() == null && this.mCurrentWorkItem == null) ? false : true;
    }

    public void quit() {
        removeAllMessages();
        stop(this.mCurrentWorkItem);
        this.mHandlerThread.interrupt();
    }

    private void removeAllMessages() {
        this.mQueue.clear();
    }

    private void removeWorkItemsFor(Object callerIdentity) {
        Iterator<PlaybackQueueItem> it = this.mQueue.iterator();
        while (it.hasNext()) {
            PlaybackQueueItem item = it.next();
            if (item.getCallerIdentity() == callerIdentity) {
                it.remove();
                stop(item);
            }
        }
    }

    private final class MessageLoop implements Runnable {
        private MessageLoop() {
        }

        @Override // java.lang.Runnable
        public void run() {
            while (true) {
                try {
                    PlaybackQueueItem item = (PlaybackQueueItem) AudioPlaybackHandler.this.mQueue.take();
                    AudioPlaybackHandler.this.mCurrentWorkItem = item;
                    item.run();
                    AudioPlaybackHandler.this.mCurrentWorkItem = null;
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
