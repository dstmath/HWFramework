package com.google.android.textclassifier;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ActionsSuggestionsModel implements AutoCloseable {
    private long actionsModelPtr;
    private final AtomicBoolean isClosed;

    public static final class ActionSuggestionOptions {
    }

    private native void nativeCloseActionsModel(long j);

    private static native String nativeGetLocales(int i);

    private static native String nativeGetName(int i);

    private static native int nativeGetVersion(int i);

    private static native long nativeNewActionsModel(int i, byte[] bArr);

    private static native long nativeNewActionsModelFromPath(String str, byte[] bArr);

    private native ActionSuggestion[] nativeSuggestActions(long j, Conversation conversation, ActionSuggestionOptions actionSuggestionOptions, long j2, Object obj, String str, boolean z);

    static {
        System.loadLibrary("textclassifier");
    }

    public ActionsSuggestionsModel(int fileDescriptor, byte[] serializedPreconditions) {
        this.isClosed = new AtomicBoolean(false);
        this.actionsModelPtr = nativeNewActionsModel(fileDescriptor, serializedPreconditions);
        if (this.actionsModelPtr == 0) {
            throw new IllegalArgumentException("Couldn't initialize actions model from file descriptor.");
        }
    }

    public ActionsSuggestionsModel(int fileDescriptor) {
        this(fileDescriptor, (byte[]) null);
    }

    public ActionsSuggestionsModel(String path, byte[] serializedPreconditions) {
        this.isClosed = new AtomicBoolean(false);
        this.actionsModelPtr = nativeNewActionsModelFromPath(path, serializedPreconditions);
        if (this.actionsModelPtr == 0) {
            throw new IllegalArgumentException("Couldn't initialize actions model from given file.");
        }
    }

    public ActionsSuggestionsModel(String path) {
        this(path, (byte[]) null);
    }

    public ActionSuggestion[] suggestActions(Conversation conversation, ActionSuggestionOptions options, AnnotatorModel annotator) {
        return nativeSuggestActions(this.actionsModelPtr, conversation, options, annotator != null ? annotator.getNativeAnnotator() : 0, null, null, false);
    }

    public ActionSuggestion[] suggestActionsWithIntents(Conversation conversation, ActionSuggestionOptions options, Object appContext, String deviceLocales, AnnotatorModel annotator) {
        return nativeSuggestActions(this.actionsModelPtr, conversation, options, annotator != null ? annotator.getNativeAnnotator() : 0, appContext, deviceLocales, true);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (this.isClosed.compareAndSet(false, true)) {
            nativeCloseActionsModel(this.actionsModelPtr);
            this.actionsModelPtr = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public static String getLocales(int fd) {
        return nativeGetLocales(fd);
    }

    public static int getVersion(int fd) {
        return nativeGetVersion(fd);
    }

    public static String getName(int fd) {
        return nativeGetName(fd);
    }

    public static final class ActionSuggestion {
        private final String actionType;
        private final NamedVariant[] entityData;
        private final RemoteActionTemplate[] remoteActionTemplates;
        private final String responseText;
        private final float score;
        private final byte[] serializedEntityData;

        public ActionSuggestion(String responseText2, String actionType2, float score2, NamedVariant[] entityData2, byte[] serializedEntityData2, RemoteActionTemplate[] remoteActionTemplates2) {
            this.responseText = responseText2;
            this.actionType = actionType2;
            this.score = score2;
            this.entityData = entityData2;
            this.serializedEntityData = serializedEntityData2;
            this.remoteActionTemplates = remoteActionTemplates2;
        }

        public String getResponseText() {
            return this.responseText;
        }

        public String getActionType() {
            return this.actionType;
        }

        public float getScore() {
            return this.score;
        }

        public NamedVariant[] getEntityData() {
            return this.entityData;
        }

        public byte[] getSerializedEntityData() {
            return this.serializedEntityData;
        }

        public RemoteActionTemplate[] getRemoteActionTemplates() {
            return this.remoteActionTemplates;
        }
    }

    public static final class ConversationMessage {
        private final String detectedTextLanguageTags;
        private final long referenceTimeMsUtc;
        private final String referenceTimezone;
        private final String text;
        private final int userId;

        public ConversationMessage(int userId2, String text2, long referenceTimeMsUtc2, String referenceTimezone2, String detectedTextLanguageTags2) {
            this.userId = userId2;
            this.text = text2;
            this.referenceTimeMsUtc = referenceTimeMsUtc2;
            this.referenceTimezone = referenceTimezone2;
            this.detectedTextLanguageTags = detectedTextLanguageTags2;
        }

        public int getUserId() {
            return this.userId;
        }

        public String getText() {
            return this.text;
        }

        public long getReferenceTimeMsUtc() {
            return this.referenceTimeMsUtc;
        }

        public String getReferenceTimezone() {
            return this.referenceTimezone;
        }

        public String getDetectedTextLanguageTags() {
            return this.detectedTextLanguageTags;
        }
    }

    public static final class Conversation {
        public final ConversationMessage[] conversationMessages;

        public Conversation(ConversationMessage[] conversationMessages2) {
            this.conversationMessages = conversationMessages2;
        }

        public ConversationMessage[] getConversationMessages() {
            return this.conversationMessages;
        }
    }
}
