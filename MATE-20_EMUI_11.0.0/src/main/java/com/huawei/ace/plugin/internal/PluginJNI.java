package com.huawei.ace.plugin.internal;

import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AcePluginCallback;
import com.huawei.ace.runtime.AcePluginMessage;
import java.nio.ByteBuffer;

public class PluginJNI implements AcePluginMessage {
    private static final String TAG = "PluginJNI";
    private AcePluginCallback pluginCallback = null;

    private static native void nativeInvokePlatformMessageResponseCallback(int i, int i2, ByteBuffer byteBuffer, int i3, boolean z);

    private static native void nativeNotifyPlatformEvents(int i, String str, ByteBuffer byteBuffer, int i2);

    private static native void nativeReplyPluginGetErrorCallback(int i, int i2, int i3, String str);

    @Override // com.huawei.ace.runtime.AcePluginMessage
    public void handlePlatformMessage(String str, byte[] bArr, int i, int i2, int i3, boolean z) {
        try {
            PluginHandlersManager.INSTANCE.handleMessageFromJs(str, bArr, i, i3, z, this.pluginCallback);
        } catch (RuntimeException unused) {
            ALog.e(TAG, "plugin/app process request runtime exception");
            replyPluginGetErrorCallback(i, i3, 2, "process request runtime exception");
        } catch (Exception unused2) {
            ALog.e(TAG, "plugin/app process request unchecked exception");
            replyPluginGetErrorCallback(i, i3, 2, "process request unchecked exception");
        }
    }

    public void setPluginLoadHandler(AcePluginCallback acePluginCallback) {
        this.pluginCallback = acePluginCallback;
    }

    public static void invokePlatformMessageResponseCallback(int i, int i2, ByteBuffer byteBuffer, int i3, boolean z) {
        nativeInvokePlatformMessageResponseCallback(i2, i, byteBuffer, i3, z);
    }

    public static void replyPluginGetErrorCallback(int i, int i2, int i3, String str) {
        nativeReplyPluginGetErrorCallback(i2, i, i3, str);
    }

    public static void notifyPlatformEvents(String str, int i, ByteBuffer byteBuffer, int i2) {
        nativeNotifyPlatformEvents(i, str, byteBuffer, i2);
    }
}
