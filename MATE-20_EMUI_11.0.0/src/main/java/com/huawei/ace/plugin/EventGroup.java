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

public final class EventGroup {
    private static final String TAG = "EventGroup#";
    private final FunctionCodec codec;
    private final String groupName;
    private final PluginHandlersManager manager;

    public interface EventGroupHandler {
        void onSubscribe(List<Object> list, EventNotifier eventNotifier, Result result);

        void onUnsubscribe(List<Object> list, Result result);
    }

    public EventGroup(String str) {
        if (str == null || "".equals(str)) {
            ALog.e(TAG, "group name must not be null or empty string.");
            throw new IllegalArgumentException("group name is null or empty string");
        }
        this.groupName = str;
        this.manager = PluginHandlersManager.INSTANCE;
        this.codec = DefaultFunctionCodec.INSTANCE;
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
            if ("subscribe".equals(decodeFunction.name)) {
                onSubscribe(decodeFunction.arguments, frontendMessageReply, str, i);
            } else if ("unsubscribe".equals(decodeFunction.name)) {
                onUnsubscribe(decodeFunction.arguments, frontendMessageReply, str, i);
            } else {
                frontendMessageReply.replyPluginGetError(200, "function name is unexpected in Event Group");
            }
        }

        private void onSubscribe(List<Object> list, FrontendMessageReply frontendMessageReply, String str, int i) {
            Object valueOf = str.startsWith(FA_EVENT_PLUGIN_PREFIX) ? str : Integer.valueOf(i);
            if (this.abilityNotifierMap.containsKey(valueOf)) {
                ALog.d(EventGroup.TAG + str, "is already subscribed");
                frontendMessageReply.replyPluginGetError(106, "already subscribed");
                return;
            }
            EventNotifierImplementation eventNotifierImplementation = new EventNotifierImplementation(i);
            try {
                this.handler.onSubscribe(list, eventNotifierImplementation, new ResultImplementation(frontendMessageReply, valueOf, eventNotifierImplementation));
            } catch (IllegalArgumentException e) {
                ALog.e(EventGroup.TAG + str, "Failed to open event subscription");
                frontendMessageReply.replyPluginGetError(2, e.getMessage());
            }
        }

        private void onUnsubscribe(List<Object> list, FrontendMessageReply frontendMessageReply, String str, int i) {
            Object valueOf = str.startsWith(FA_EVENT_PLUGIN_PREFIX) ? str : Integer.valueOf(i);
            EventNotifier eventNotifier = this.abilityNotifierMap.get(valueOf);
            ResultImplementation resultImplementation = new ResultImplementation(this, frontendMessageReply, valueOf);
            if (eventNotifier != null) {
                try {
                    this.handler.onUnsubscribe(list, resultImplementation);
                } catch (IllegalArgumentException e) {
                    ALog.e(EventGroup.TAG + str, "Failed to close event subscription");
                    frontendMessageReply.replyPluginGetError(2, e.getMessage());
                }
            } else {
                frontendMessageReply.replyPluginGetError(105, "No active subscription to cancel");
            }
        }

        /* access modifiers changed from: private */
        public final class EventNotifierImplementation implements EventNotifier {
            private String abilityName;
            private final int containerId;
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
                if (!this.isEnded.get()) {
                    String str = EventGroup.this.groupName;
                    if (this.abilityName != null) {
                        str = str + "#" + this.abilityName;
                        if (!IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(str)) {
                            ALog.w(EventGroup.TAG, "multi notifier is inactive, report success event failed");
                            return;
                        }
                    } else if (!IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(Integer.valueOf(this.containerId))) {
                        ALog.w(EventGroup.TAG, "notifier is inactive, report success event failed");
                        return;
                    }
                    EventGroup.this.manager.notify(str, this.containerId, EventGroup.this.codec.encodeSuccessReply(i, obj));
                }
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void error(int i, Object obj) {
                if (!this.isEnded.get()) {
                    String str = EventGroup.this.groupName;
                    if (this.abilityName != null) {
                        str = str + "#" + this.abilityName;
                        if (!IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(str)) {
                            ALog.w(EventGroup.TAG, "multi notifier is inactive, report error event failed");
                            return;
                        }
                    } else if (!IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(Integer.valueOf(this.containerId))) {
                        ALog.w(EventGroup.TAG, "notifier is inactive, report error event failed");
                        return;
                    }
                    EventGroup.this.manager.notify(str, this.containerId, EventGroup.this.codec.encodeErrorReply(i, obj));
                }
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void endOfNotify() {
                if (!this.isEnded.get()) {
                    String str = EventGroup.this.groupName;
                    if (this.abilityName != null) {
                        str = str + "#" + this.abilityName;
                        if (!IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(str)) {
                            ALog.w(EventGroup.TAG, "multi notifier is inactive, end of notify failed");
                            return;
                        } else {
                            this.isEnded.set(true);
                            IncomingSubscribeRequestHandler.this.abilityNotifierMap.remove(str);
                        }
                    } else if (!IncomingSubscribeRequestHandler.this.abilityNotifierMap.containsKey(Integer.valueOf(this.containerId))) {
                        ALog.w(EventGroup.TAG, "notifier is inactive, end of notify failed");
                        return;
                    } else {
                        this.isEnded.set(true);
                        IncomingSubscribeRequestHandler.this.abilityNotifierMap.remove(Integer.valueOf(this.containerId));
                    }
                    EventGroup.this.manager.notify(str, this.containerId, EventGroup.this.codec.encodeSuccessReply(0, "endOfNotify"));
                }
            }

            @Override // com.huawei.ace.plugin.EventNotifier
            public void setAbilityName(String str) {
                this.abilityName = str;
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
                    IncomingSubscribeRequestHandler.this.abilityNotifierMap.remove(this.notifierKey);
                } else {
                    IncomingSubscribeRequestHandler.this.abilityNotifierMap.put(this.notifierKey, this.notifier);
                }
            }
        }
    }
}
