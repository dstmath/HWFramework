package com.android.server.emcom.grabservice;

interface IMessage {
    boolean isGroupMessage(String str);

    boolean isMoney(String str, int i);
}
