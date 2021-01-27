package com.android.framework.protobuf;

import com.android.framework.protobuf.MessageLite;
import com.android.framework.protobuf.WireFormat;

public abstract class ExtensionLite<ContainingType extends MessageLite, Type> {
    public abstract Type getDefaultValue();

    public abstract WireFormat.FieldType getLiteType();

    public abstract MessageLite getMessageDefaultInstance();

    public abstract int getNumber();

    public abstract boolean isRepeated();

    /* access modifiers changed from: package-private */
    public boolean isLite() {
        return true;
    }
}
