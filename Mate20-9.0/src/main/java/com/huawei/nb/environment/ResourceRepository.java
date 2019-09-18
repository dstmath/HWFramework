package com.huawei.nb.environment;

import com.huawei.nb.utils.time.TimeHelper;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResourceRepository implements Resource {
    public static final int MEMORY_LOW = 0;
    public static final int MEMORY_OK = 1;
    public static final int NETWORK_TYPE_MOBILE = 2;
    public static final int NETWORK_TYPE_NONE = 0;
    public static final int NETWORK_TYPE_WIFI = 1;
    public static final int POWER_CHARGING = 1;
    public static final int POWER_DISCHARGING = 0;
    public static final int SCREEN_OFF = 0;
    public static final int SCREEN_ON = 1;
    private static final Object mLock = new Object();
    private static volatile ResourceRepository resourceRepository = null;
    private volatile int memory = 0;
    private BlockingQueue<ResourceType> msgQueue = new LinkedBlockingDeque();
    private volatile int networkStatus = 0;
    private volatile int power = 0;
    private volatile int powerStatus = 0;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile int screenStatus = 0;
    private volatile long timer = 0;

    public static ResourceRepository getInstance() {
        if (resourceRepository == null) {
            synchronized (mLock) {
                if (resourceRepository == null) {
                    resourceRepository = new ResourceRepository();
                }
            }
        }
        return resourceRepository;
    }

    private ResourceRepository() {
    }

    public int getPower() {
        try {
            this.rwLock.readLock().lock();
            return this.power;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void setPower(int power2) {
        try {
            this.rwLock.writeLock().lock();
            this.power = power2;
            this.msgQueue.add(ResourceType.POWER);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public int getPowerStatus() {
        try {
            this.rwLock.readLock().lock();
            return this.powerStatus;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void setPowerStatus(int powerStatus2) {
        try {
            this.rwLock.writeLock().lock();
            this.powerStatus = powerStatus2;
            this.msgQueue.add(ResourceType.POWER_STATUS);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public int getNetworkStatus() {
        try {
            this.rwLock.readLock().lock();
            return this.networkStatus;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void setNetworkStatus(int networkStatus2) {
        try {
            this.rwLock.writeLock().lock();
            this.networkStatus = networkStatus2;
            this.msgQueue.add(ResourceType.NETWORK_STATUS);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public int getScreenStatus() {
        try {
            this.rwLock.readLock().lock();
            return this.screenStatus;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void setScreenStatus(int screenStatus2) {
        try {
            this.rwLock.writeLock().lock();
            this.screenStatus = screenStatus2;
            this.msgQueue.add(ResourceType.SCREEN_STATUS);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public int getMemory() {
        try {
            this.rwLock.readLock().lock();
            return this.memory;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void setMemory(int memory2) {
        try {
            this.rwLock.writeLock().lock();
            this.memory = memory2;
            this.msgQueue.add(ResourceType.MEMORY);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public long getTimer() {
        try {
            this.rwLock.readLock().lock();
            return this.timer;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    public void setTimer() {
        try {
            this.rwLock.writeLock().lock();
            this.timer = TimeHelper.currentTimeMillis();
            this.msgQueue.add(ResourceType.TIMER);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    public ResourceType take() throws InterruptedException {
        return this.msgQueue.take();
    }

    public Object getResourceByType(ResourceType type) {
        switch (type) {
            case POWER:
                return Integer.valueOf(getPower());
            case POWER_STATUS:
                return Integer.valueOf(getPowerStatus());
            case NETWORK_STATUS:
                return Integer.valueOf(getNetworkStatus());
            case MEMORY:
                return Integer.valueOf(getMemory());
            case SCREEN_STATUS:
                return Integer.valueOf(getScreenStatus());
            case TIMER:
                return Long.valueOf(getTimer());
            default:
                return null;
        }
    }
}
