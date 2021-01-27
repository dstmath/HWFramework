package com.huawei.ace.runtime;

public interface IEventReport {
    void sendComponentEvent(int i);

    void sendFrameworkAppStartEvent(int i);

    void sendRenderEvent(int i);
}
