package com.android.commands.monkey;

import java.util.LinkedList;
import java.util.Random;

public class MonkeyEventQueue extends LinkedList<MonkeyEvent> {
    private Random mRandom;
    private boolean mRandomizeThrottle;
    private long mThrottle;

    public MonkeyEventQueue(Random random, long throttle, boolean randomizeThrottle) {
        this.mRandom = random;
        this.mThrottle = throttle;
        this.mRandomizeThrottle = randomizeThrottle;
    }

    public void addLast(MonkeyEvent e) {
        super.add(e);
        if (e.isThrottlable()) {
            long throttle = this.mThrottle;
            if (this.mRandomizeThrottle && this.mThrottle > 0) {
                throttle = this.mRandom.nextLong();
                if (throttle < 0) {
                    throttle = -throttle;
                }
                throttle = (throttle % this.mThrottle) + 1;
            }
            super.add(new MonkeyThrottleEvent(throttle));
        }
    }
}
