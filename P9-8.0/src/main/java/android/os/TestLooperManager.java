package android.os;

import android.util.ArraySet;
import java.util.concurrent.LinkedBlockingQueue;

public class TestLooperManager {
    private static final ArraySet<Looper> sHeldLoopers = new ArraySet();
    private final LinkedBlockingQueue<MessageExecution> mExecuteQueue = new LinkedBlockingQueue();
    private final Looper mLooper;
    private boolean mLooperBlocked;
    private final MessageQueue mQueue;
    private boolean mReleased;

    private class LooperHolder implements Runnable {
        /* synthetic */ LooperHolder(TestLooperManager this$0, LooperHolder -this1) {
            this();
        }

        private LooperHolder() {
        }

        public void run() {
            synchronized (TestLooperManager.this) {
                TestLooperManager.this.mLooperBlocked = true;
                TestLooperManager.this.notify();
            }
            while (!TestLooperManager.this.mReleased) {
                try {
                    MessageExecution take = (MessageExecution) TestLooperManager.this.mExecuteQueue.take();
                    if (take.m != null) {
                        processMessage(take);
                    }
                } catch (InterruptedException e) {
                }
            }
            synchronized (TestLooperManager.this) {
                TestLooperManager.this.mLooperBlocked = false;
            }
        }

        private void processMessage(MessageExecution mex) {
            synchronized (mex) {
                try {
                    mex.m.target.dispatchMessage(mex.m);
                    mex.response = null;
                } catch (Throwable t) {
                    mex.response = t;
                }
                mex.notifyAll();
            }
            return;
        }
    }

    private static class MessageExecution {
        private Message m;
        private Throwable response;

        /* synthetic */ MessageExecution(MessageExecution -this0) {
            this();
        }

        private MessageExecution() {
        }
    }

    public TestLooperManager(Looper looper) {
        synchronized (sHeldLoopers) {
            if (sHeldLoopers.contains(looper)) {
                throw new RuntimeException("TestLooperManager already held for this looper");
            }
            sHeldLoopers.add(looper);
        }
        this.mLooper = looper;
        this.mQueue = this.mLooper.getQueue();
        new Handler(looper).post(new LooperHolder(this, null));
    }

    public MessageQueue getMessageQueue() {
        checkReleased();
        return this.mQueue;
    }

    @Deprecated
    public MessageQueue getQueue() {
        return getMessageQueue();
    }

    public Message next() {
        while (!this.mLooperBlocked) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        checkReleased();
        return this.mQueue.next();
    }

    public void release() {
        synchronized (sHeldLoopers) {
            sHeldLoopers.remove(this.mLooper);
        }
        checkReleased();
        this.mReleased = true;
        this.mExecuteQueue.add(new MessageExecution());
    }

    public void execute(Message message) {
        checkReleased();
        if (Looper.myLooper() == this.mLooper) {
            message.target.dispatchMessage(message);
            return;
        }
        MessageExecution execution = new MessageExecution();
        execution.m = message;
        synchronized (execution) {
            this.mExecuteQueue.add(execution);
            try {
                execution.wait();
            } catch (InterruptedException e) {
            }
            if (execution.response != null) {
                throw new RuntimeException(execution.response);
            }
        }
    }

    public void recycle(Message msg) {
        checkReleased();
        msg.recycleUnchecked();
    }

    public boolean hasMessages(Handler h, Object object, int what) {
        checkReleased();
        return this.mQueue.hasMessages(h, what, object);
    }

    public boolean hasMessages(Handler h, Object object, Runnable r) {
        checkReleased();
        return this.mQueue.hasMessages(h, r, object);
    }

    private void checkReleased() {
        if (this.mReleased) {
            throw new RuntimeException("release() has already be called");
        }
    }
}
