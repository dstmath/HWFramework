package com.huawei.hiai.awareness.client;

import android.app.PendingIntent;

public class AwarenessRequest {
    public static final String MESSAGE_TYPE = "context_awareness_request";
    private AwarenessEnvelope envelope;

    private AwarenessRequest() {
    }

    public static AwarenessRequest checkFence(AwarenessFence fence) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.CHECK_FENCE).putArg(AwarenessFence.MESSAGE_TYPE, fence);
        return request;
    }

    public static AwarenessRequest checkSnapshot(AwarenessSnapshot snapshot) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.CHECK_SNAPSHOT).putArg(AwarenessSnapshot.MESSAGE_TYPE, snapshot);
        return request;
    }

    public static AwarenessRequest registerFence(AwarenessFence fence, PendingIntent pendingOperation) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.REGISTER_FENCE_BY_PENDING_INTENT).putArg(AwarenessFence.MESSAGE_TYPE, fence).putArg(Field.PENDING_OPERATION, pendingOperation);
        return request;
    }

    public static AwarenessRequest registerFence(AwarenessFence fence, OnEnvelopeReceiver receiver) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.REGISTER_FENCE_BY_LISTENER).putArg(AwarenessFence.MESSAGE_TYPE, fence).putArg(Field.ON_ENVELOPE_RECEIVER, receiver.asBinder());
        return request;
    }

    public static AwarenessRequest unregisterFence(AwarenessFence fence) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.UNREGISTER_FENCE).putArg(AwarenessFence.MESSAGE_TYPE, fence);
        return request;
    }

    public static AwarenessRequest unregisterFence(String identifier) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.UNREGISTER_FENCE).putArg(Field.FENCE_ID, identifier);
        return request;
    }

    public static AwarenessRequest getSnapshot(AwarenessSnapshot snapshot, OnEnvelopeReceiver receiver) {
        AwarenessRequest request = new AwarenessRequest();
        request.envelope = AwarenessEnvelope.create(MessageType.GET_SNAPSHOT).putArg(AwarenessSnapshot.MESSAGE_TYPE, snapshot).putArg(Field.ON_ENVELOPE_RECEIVER, receiver.asBinder());
        return request;
    }

    public AwarenessRequest addOnResultListener(OnResultListener listener) {
        this.envelope.putArg(Field.ON_RESULT_LISTENER, listener.asBinder());
        return this;
    }

    public AwarenessRequest setPackageName(String packageName) {
        this.envelope.putArg(Field.PACKAGE_NAME, packageName);
        return this;
    }

    public String getMessageType() {
        return this.envelope.getMessageType();
    }

    public AwarenessEnvelope toEnvelope() {
        return this.envelope;
    }

    public class MessageType {
        public static final String CHECK_FENCE = "check_fence";
        public static final String CHECK_SNAPSHOT = "check_snapshot";
        public static final String GET_SNAPSHOT = "get_snapshot";
        public static final String REGISTER_FENCE_BY_LISTENER = "register_fence_by_listener";
        public static final String REGISTER_FENCE_BY_PENDING_INTENT = "register_fence_by_pending_intent";
        public static final String UNREGISTER_FENCE = "unregister_fence";

        private MessageType() {
        }
    }

    public class Field {
        public static final String FENCE_ID = "fence_id";
        public static final String ON_ENVELOPE_RECEIVER = "on_envelope_receiver";
        public static final String ON_RESULT_LISTENER = "on_result_listener";
        public static final String PACKAGE_NAME = "package_name";
        public static final String PENDING_OPERATION = "pending_operation";

        private Field() {
        }
    }
}
