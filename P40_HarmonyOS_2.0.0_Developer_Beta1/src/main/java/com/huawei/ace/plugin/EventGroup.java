package com.huawei.ace.plugin;

import com.huawei.ace.plugin.internal.DefaultFunctionCodec;
import com.huawei.ace.plugin.internal.FrontendMessageReply;
import com.huawei.ace.plugin.internal.FunctionCodec;
import com.huawei.ace.plugin.internal.PluginHandler;
import com.huawei.ace.plugin.internal.PluginHandlersManager;
import com.huawei.ace.runtime.ALog;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import ohos.dmsdp.sdk.DMSDPConfig;

public final class EventGroup {
    private static final String TAG = "EventGroup#";
    private final FunctionCodec codec;
    private final String groupName;
    private final String logTag;
    private final PluginHandlersManager manager;

    public EventGroup(String str) {
        if (str == null || "".equals(str)) {
            ALog.e(TAG, "group name must not be null or empty string.");
            throw new IllegalArgumentException("group name is null or empty string");
        }
        this.manager = PluginHandlersManager.INSTANCE;
        this.codec = DefaultFunctionCodec.INSTANCE;
        this.logTag = TAG + str;
        this.groupName = str;
    }

    public void setEventGroupHandler(EventGroupHandler eventGroupHandler) {
        setEventGroupHandler(eventGroupHandler, null);
    }

    public void setEventGroupHandler(EventGroupHandler eventGroupHandler, Integer num) {
        IncomingSubscribeRequestHandler incomingSubscribeRequestHandler;
        PluginHandlersManager pluginHandlersManager = this.manager;
        String str = this.groupName;
        if (eventGroupHandler == null) {
            incomingSubscribeRequestHandler = null;
        } else {
            incomingSubscribeRequestHandler = new IncomingSubscribeRequestHandler(eventGroupHandler);
        }
        pluginHandlersManager.setPluginHandler(str, num, incomingSubscribeRequestHandler);
    }

    public static void registerEventGroup(String str, EventGroupHandler eventGroupHandler, Integer num) {
        new EventGroup(str).setEventGroupHandler(eventGroupHandler, num);
    }

    public interface EventGroupHandler {
        void onSubscribe(List<Object> list, EventNotifier eventNotifier, Result result);

        default void onUnsubscribe(List<Object> list, Result result) {
        }

        default void onUnsubscribe(List<Object> list, EventNotifier eventNotifier, Result result) {
            onUnsubscribe(list, result);
        }
    }

    /* access modifiers changed from: private */
    public final class IncomingSubscribeRequestHandler implements PluginHandler {
        private static final String FA_EVENT_PLUGIN_PREFIX = "AceInternalEventGroup/FeatureAbility#";
        private final Map<Object, EventNotifier> abilityNotifierMap = new HashMap();
        private final EventGroupHandler handler;

        IncomingSubscribeRequestHandler(EventGroupHandler eventGroupHandler) {
            this.handler = eventGroupHandler;
        }

        @Override // com.huawei.ace.plugin.internal.PluginHandler
        public void onReceiveMessage(ByteBuffer byteBuffer, FrontendMessageReply frontendMessageReply, String str, int i) {
            Function decodeFunction = EventGroup.this.codec.decodeFunction(byteBuffer);
            String str2 = EventGroup.this.logTag;
            ALog.i(str2, "receive action message, containerId = " + i + ", requestGroup = " + str + ", callName = " + decodeFunction.name + ", replyId = " + frontendMessageReply.getReplyId());
            if ("subscribe".equals(decodeFunction.name)) {
                onSubscribe(decodeFunction.arguments, frontendMessageReply, str, i);
            } else if ("unsubscribe".equals(decodeFunction.name)) {
                onUnsubscribe(decodeFunction.arguments, frontendMessageReply, str, i);
            } else {
                frontendMessageReply.replyPluginGetError(200, "function name is unexpected in Event Group");
            }
        }

        private void onSubscribe(List<Object> list, FrontendMessageReply frontendMessageReply, String str, int i) {
            boolean startsWith = str.startsWith(FA_EVENT_PLUGIN_PREFIX);
            Object obj = str;
            if (!startsWith) {
                obj = Integer.valueOf(i);
            }
            if (this.abilityNotifierMap.containsKey(obj)) {
                String str2 = EventGroup.this.logTag;
                ALog.e(str2, "is already subscribed, notifierKey = " + obj.toString());
                frontendMessageReply.replyPluginGetError(106, "already subscribed");
                return;
            }
            EventNotifierImplementation eventNotifierImplementation = new EventNotifierImplementation(i);
            try {
                this.handler.onSubscribe(list, eventNotifierImplementation, new ResultImplementation(frontendMessageReply, obj, eventNotifierImplementation));
            } catch (IllegalArgumentException e) {
                String str3 = EventGroup.this.logTag;
                ALog.e(str3, "Failed to open event subscription, notifierKey = " + obj.toString());
                frontendMessageReply.replyPluginGetError(2, e.getMessage());
            }
        }

        private void onUnsubscribe(List<Object> list, FrontendMessageReply frontendMessageReply, String str, int i) {
            boolean startsWith = str.startsWith(FA_EVENT_PLUGIN_PREFIX);
            Object obj = str;
            if (!startsWith) {
                obj = Integer.valueOf(i);
            }
            EventNotifier eventNotifier = this.abilityNotifierMap.get(obj);
            ResultImplementation resultImplementation = new ResultImplementation(this, frontendMessageReply, obj);
            if (eventNotifier != null) {
                try {
                    this.handler.onUnsubscribe(list, eventNotifier, resultImplementation);
                } catch (IllegalArgumentException e) {
                    String str2 = EventGroup.this.logTag;
                    ALog.e(str2, "Failed to close event subscription, notifierKey = " + obj.toString());
                    frontendMessageReply.replyPluginGetError(2, e.getMessage());
                }
            } else {
                String str3 = EventGroup.this.logTag;
                ALog.e(str3, "No active subscription to cancel, notifierKey = " + obj.toString());
                frontendMessageReply.replyPluginGetError(105, "No active subscription to cancel");
            }
        }

        /* access modifiers changed from: private */
        public final class EventNotifierImplementation implements EventNotifier {
            private final int containerId;
            private String eventGroupName;
            private final AtomicBoolean isEnded = new AtomicBoolean(false);

            public EventNotifierImplementation(int i) {
                this.containerId = i;
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void success(Object obj) {
                success(0, obj);
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void success(int i, Object obj) {
                if (!this.isEnded.get() && existNotifier()) {
                    PluginHandlersManager pluginHandlersManager = EventGroup.this.manager;
                    String str = this.eventGroupName;
                    if (str == null) {
                        str = EventGroup.this.groupName;
                    }
                    pluginHandlersManager.notify(str, this.containerId, EventGroup.this.codec.encodeSuccessReply(i, obj));
                }
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void error(int i, Object obj) {
                if (!this.isEnded.get() && existNotifier()) {
                    PluginHandlersManager pluginHandlersManager = EventGroup.this.manager;
                    String str = this.eventGroupName;
                    if (str == null) {
                        str = EventGroup.this.groupName;
                    }
                    pluginHandlersManager.notify(str, this.containerId, EventGroup.this.codec.encodeErrorReply(i, obj));
                }
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void endOfNotify() {
                if (!this.isEnded.get() && existNotifier()) {
                    this.isEnded.set(true);
                    Map map = IncomingSubscribeRequestHandler.this.abilityNotifierMap;
                    Object obj = this.eventGroupName;
                    if (obj == null) {
                        obj = Integer.valueOf(this.containerId);
                    }
                    map.remove(obj);
                    EventGroup.this.manager.notify(this.eventGroupName, this.containerId, EventGroup.this.codec.encodeSuccessReply(0, "endOfNotify"));
                }
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void setAbilityName(String str) {
                if (str != null) {
                    this.eventGroupName = EventGroup.this.groupName + DMSDPConfig.SPLIT + str;
                }
            }

            private boolean existNotifier() {
                if (this.eventGroupName != null) {
                    if (IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(this.eventGroupName)) {
                        return true;
                    }
                    String str = EventGroup.this.logTag;
                    ALog.w(str, "multi notifier is inactive, report event failed, notifierKey = " + this.eventGroupName);
                    return false;
                } else if (IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(Integer.valueOf(this.containerId))) {
                    return true;
                } else {
                    String str2 = EventGroup.this.logTag;
                    ALog.w(str2, "notifier is inactive, report event failed, notifierKey = " + this.containerId);
                    return false;
                }
            }
        }

        /* access modifiers changed from: private */
        public final class ResultImplementation implements Result {
            private final FrontendMessageReply callback;
            private final EventNotifier notifier;
            private final Object notifierKey;

            public ResultImplementation(FrontendMessageReply frontendMessageReply, Object obj, EventNotifier eventNotifier) {
                this.callback = frontendMessageReply;
                this.notifierKey = obj;
                this.notifier = eventNotifier;
            }

            public ResultImplementation(IncomingSubscribeRequestHandler incomingSubscribeRequestHandler, FrontendMessageReply frontendMessageReply, Object obj) {
                this(frontendMessageReply, obj, null);
            }

            @Override // com.huawei.ace.plugin.Result
            public void success(Object obj) {
                this.callback.reply(EventGroup.this.codec.encodeSuccessReply(0, obj));
                afterSuccessed();
            }

            @Override // com.huawei.ace.plugin.Result
            public void error(int i, Object obj) {
                this.callback.reply(EventGroup.this.codec.encodeErrorReply(i, obj));
            }

            @Override // com.huawei.ace.plugin.Result
            public void successWithRawString(String str) {
                this.callback.reply(EventGroup.this.codec.encodeSuccessReply(str));
                afterSuccessed();
            }

            @Override // com.huawei.ace.plugin.Result
            public void notExistFunction() {
                this.callback.replyPluginGetError(104, "function not defined in platform side");
                IncomingSubscribeRequestHandler.this.abilityNotifierMap.remove(this.notifierKey);
            }

            private void afterSuccessed() {
                if (this.notifier == null) {
                    String str = EventGroup.this.logTag;
                    ALog.i(str, "unsubscribe success and delete EventNotifier, notifierKey = " + this.notifierKey.toString());
                    IncomingSubscribeRequestHandler.this.abilityNotifierMap.remove(this.notifierKey);
                    return;
                }
                String str2 = EventGroup.this.logTag;
                ALog.i(str2, "subscribe success and record EventNotifier, notifierKey = " + this.notifierKey.toString());
                IncomingSubscribeRequestHandler.this.abilityNotifierMap.put(this.notifierKey, this.notifier);
            }
        }
    }
}
