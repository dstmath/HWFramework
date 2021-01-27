package com.google.protobuf;

import java.util.Collections;
import java.util.List;

public class UninitializedMessageException extends RuntimeException {
    private static final long serialVersionUID = -7466929953374883507L;
    private final List<String> missingFields;

    public UninitializedMessageException(MessageLite message) {
        super("Message was missing required fields.  (Lite runtime could not determine which fields were missing).");
        this.missingFields = null;
    }

    public UninitializedMessageException(List<String> missingFields2) {
        super(buildDescription(missingFields2));
        this.missingFields = missingFields2;
    }

    public List<String> getMissingFields() {
        return Collections.unmodifiableList(this.missingFields);
    }

    public InvalidProtocolBufferException asInvalidProtocolBufferException() {
        return new InvalidProtocolBufferException(getMessage());
    }

    private static String buildDescription(List<String> missingFields2) {
        StringBuilder description = new StringBuilder("Message missing required fields: ");
        boolean first = true;
        for (String field : missingFields2) {
            if (first) {
                first = false;
            } else {
                description.append(", ");
            }
            description.append(field);
        }
        return description.toString();
    }
}
