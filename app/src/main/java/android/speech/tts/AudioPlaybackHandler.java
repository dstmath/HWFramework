package android.speech.tts;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

class AudioPlaybackHandler {
    private static final boolean DBG = false;
    private static final String TAG = "TTS.AudioPlaybackHandler";
    private volatile PlaybackQueueItem mCurrentWorkItem;
    private final Thread mHandlerThread;
    private final LinkedBlockingQueue<PlaybackQueueItem> mQueue;

    private final class MessageLoop implements Runnable {
        private MessageLoop() {
        }

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

    AudioPlaybackHandler() {
        this.mQueue = new LinkedBlockingQueue();
        this.mCurrentWorkItem = null;
        this.mHandlerThread = new Thread(new MessageLoop(), "TTS.AudioPlaybackThread");
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
        return (this.mQueue.peek() == null && this.mCurrentWorkItem == null) ? DBG : true;
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
            if (((PlaybackQueueItem) it.next()).getCallerIdentity() == callerIdentity) {
                it.remove();
            }
        }
    }
}
