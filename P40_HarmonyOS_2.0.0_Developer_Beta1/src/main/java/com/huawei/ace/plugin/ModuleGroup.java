package com.huawei.ace.plugin;

import com.huawei.ace.plugin.internal.DefaultFunctionCodec;
import com.huawei.ace.plugin.internal.FrontendMessageReply;
import com.huawei.ace.plugin.internal.FunctionCodec;
import com.huawei.ace.plugin.internal.PluginHandler;
import com.huawei.ace.plugin.internal.PluginHandlersManager;
import com.huawei.ace.runtime.ALog;
import java.nio.ByteBuffer;

public final class ModuleGroup {
    private static final String TAG = "ModuleGroup#";
    private final FunctionCodec codec;
    private final String logTag;
    private final PluginHandlersManager manager;
    private final String name;

    public interface ModuleGroupHandler {
        void onFunctionCall(Function function, Result result);
    }

    public ModuleGroup(String str) {
        if (str == null || "".equals(str)) {
            ALog.e(TAG, "ModuleGroup name must not be null or empty string");
            throw new IllegalArgumentException("group name is null or empty string");
        }
        this.manager = PluginHandlersManager.INSTANCE;
        this.codec = DefaultFunctionCodec.INSTANCE;
        this.logTag = TAG + str;
        this.name = str;
    }

    public void setModuleGroupHandler(ModuleGroupHandler moduleGroupHandler) {
        setModuleGroupHandler(moduleGroupHandler, null);
    }

    public void setModuleGroupHandler(ModuleGroupHandler moduleGroupHandler, Integer num) {
        IncomingFunctionCallHandler incomingFunctionCallHandler;
        PluginHandlersManager pluginHandlersManager = this.manager;
        String str = this.name;
        if (moduleGroupHandler == null) {
            incomingFunctionCallHandler = null;
        } else {
            incomingFunctionCallHandler = new IncomingFunctionCallHandler(moduleGroupHandler);
        }
        pluginHandlersManager.setPluginHandler(str, num, incomingFunctionCallHandler);
    }

    public static void registerModuleGroup(String str, ModuleGroupHandler moduleGroupHandler, Integer num) {
        new ModuleGroup(str).setModuleGroupHandler(moduleGroupHandler, num);
    }

    /* access modifiers changed from: private */
    public final class IncomingFunctionCallHandler implements PluginHandler {
        private final ModuleGroupHandler handler;

        IncomingFunctionCallHandler(ModuleGroupHandler moduleGroupHandler) {
            this.handler = moduleGroupHandler;
        }

        @Override // com.huawei.ace.plugin.internal.PluginHandler
        public void onReceiveMessage(ByteBuffer byteBuffer, final FrontendMessageReply frontendMessageReply, String str, int i) {
            Function decodeFunction = ModuleGroup.this.codec.decodeFunction(byteBuffer);
            String str2 = ModuleGroup.this.logTag;
            ALog.i(str2, "receive action message, containerId = " + i + ", requestGroup = " + str + ", callName = " + decodeFunction.name + ", replyId = " + frontendMessageReply.getReplyId());
            this.handler.onFunctionCall(decodeFunction, new Result() {
                /* class com.huawei.ace.plugin.ModuleGroup.IncomingFunctionCallHandler.AnonymousClass1 */

                @Override // com.huawei.ace.plugin.Result
                public void success(Object obj) {
                    frontendMessageReply.reply(ModuleGroup.this.codec.encodeSuccessReply(0, obj));
                }

                @Override // com.huawei.ace.plugin.Result
                public void error(int i, Object obj) {
                    frontendMessageReply.reply(ModuleGroup.this.codec.encodeErrorReply(i, obj));
                }

                @Override // com.huawei.ace.plugin.Result
                public void successWithRawString(String str) {
                    frontendMessageReply.reply(ModuleGroup.this.codec.encodeSuccessReply(str));
                }

                @Override // com.huawei.ace.plugin.Result
                public void notExistFunction() {
                    frontendMessageReply.replyPluginGetError(104, "function not defined in platform side");
                }
            });
        }
    }
}
