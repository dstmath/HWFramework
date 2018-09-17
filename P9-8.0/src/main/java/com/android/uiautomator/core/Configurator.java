package com.android.uiautomator.core;

@Deprecated
public final class Configurator {
    private static Configurator sConfigurator;
    private long mKeyInjectionDelay = 0;
    private long mScrollEventWaitTimeout = 200;
    private long mWaitForActionAcknowledgment = 3000;
    private long mWaitForIdleTimeout = 10000;
    private long mWaitForSelector = 10000;

    private Configurator() {
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
