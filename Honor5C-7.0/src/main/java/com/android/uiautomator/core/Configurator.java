package com.android.uiautomator.core;

@Deprecated
public final class Configurator {
    private static Configurator sConfigurator;
    private long mKeyInjectionDelay;
    private long mScrollEventWaitTimeout;
    private long mWaitForActionAcknowledgment;
    private long mWaitForIdleTimeout;
    private long mWaitForSelector;

    private Configurator() {
        this.mWaitForIdleTimeout = 10000;
        this.mWaitForSelector = 10000;
        this.mWaitForActionAcknowledgment = 3000;
        this.mScrollEventWaitTimeout = 200;
        this.mKeyInjectionDelay = 0;
    }

    public static Configurator getInstance() {
        if (sConfigurator == null) {
            sConfigurator = new Configurator();
        }
        return sConfigurator;
    }

    public Configurator setWaitForIdleTimeout(long timeout) {
        this.mWaitForIdleTimeout = timeout;
        return this;
    }

    public long getWaitForIdleTimeout() {
        return this.mWaitForIdleTimeout;
    }

    public Configurator setWaitForSelectorTimeout(long timeout) {
        this.mWaitForSelector = timeout;
        return this;
    }

    public long getWaitForSelectorTimeout() {
        return this.mWaitForSelector;
    }

    public Configurator setScrollAcknowledgmentTimeout(long timeout) {
        this.mScrollEventWaitTimeout = timeout;
        return this;
    }

    public long getScrollAcknowledgmentTimeout() {
        return this.mScrollEventWaitTimeout;
    }

    public Configurator setActionAcknowledgmentTimeout(long timeout) {
        this.mWaitForActionAcknowledgment = timeout;
        return this;
    }

    public long getActionAcknowledgmentTimeout() {
        return this.mWaitForActionAcknowledgment;
    }

    public Configurator setKeyInjectionDelay(long delay) {
        this.mKeyInjectionDelay = delay;
        return this;
    }

    public long getKeyInjectionDelay() {
        return this.mKeyInjectionDelay;
    }
}
