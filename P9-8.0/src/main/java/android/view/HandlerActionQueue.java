package android.view;

import android.os.Handler;
import com.android.internal.util.GrowingArrayUtils;

public class HandlerActionQueue {
    private HandlerAction[] mActions;
    private int mCount;

    private static class HandlerAction {
        final Runnable action;
        final long delay;

        public HandlerAction(Runnable action, long delay) {
            this.action = action;
            this.delay = delay;
        }

        public boolean matches(Runnable otherAction) {
            if (otherAction == null && this.action == null) {
                return true;
            }
            return this.action != null ? this.action.equals(otherAction) : false;
        }
    }

    public void post(Runnable action) {
        postDelayed(action, 0);
    }

    public void postDelayed(Runnable action, long delayMillis) {
        Object handlerAction = new HandlerAction(action, delayMillis);
        synchronized (this) {
            if (this.mActions == null) {
                this.mActions = new HandlerAction[4];
            }
            this.mActions = (HandlerAction[]) GrowingArrayUtils.append(this.mActions, this.mCount, handlerAction);
            this.mCount++;
        }
    }

    public void removeCallbacks(Runnable action) {
        synchronized (this) {
            int count = this.mCount;
            int j = 0;
            HandlerAction[] actions = this.mActions;
            for (int i = 0; i < count; i++) {
                if (!actions[i].matches(action)) {
                    if (j != i) {
                        actions[j] = actions[i];
                    }
                    j++;
                }
            }
            this.mCount = j;
            while (j < count) {
                actions[j] = null;
                j++;
            }
        }
    }

    public void executeActions(Handler handler) {
        synchronized (this) {
            HandlerAction[] actions = this.mActions;
            int count = this.mCount;
            for (int i = 0; i < count; i++) {
                HandlerAction handlerAction = actions[i];
                handler.postDelayed(handlerAction.action, handlerAction.delay);
            }
            this.mActions = null;
            this.mCount = 0;
        }
    }

    public int size() {
        return this.mCount;
    }

    public Runnable getRunnable(int index) {
        if (index < this.mCount) {
            return this.mActions[index].action;
        }
        throw new IndexOutOfBoundsException();
    }

    public long getDelay(int index) {
        if (index < this.mCount) {
            return this.mActions[index].delay;
        }
        throw new IndexOutOfBoundsException();
    }
}
