package com.android.commands.monkey;

public interface MonkeyEventSource {
    MonkeyEvent getNextEvent();

    void setVerbose(int i);

    boolean validate();
}
