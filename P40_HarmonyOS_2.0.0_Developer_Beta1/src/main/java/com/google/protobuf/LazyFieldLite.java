package com.google.protobuf;

import java.io.IOException;

public class LazyFieldLite {
    private static final ExtensionRegistryLite EMPTY_REGISTRY = ExtensionRegistryLite.getEmptyRegistry();
    private ByteString delayedBytes;
    private ExtensionRegistryLite extensionRegistry;
    private volatile ByteString memoizedBytes;
    protected volatile MessageLite value;

    public LazyFieldLite(ExtensionRegistryLite extensionRegistry2, ByteString bytes) {
        checkArguments(extensionRegistry2, bytes);
        this.extensionRegistry = extensionRegistry2;
        this.delayedBytes = bytes;
    }

    public LazyFieldLite() {
    }

    public static LazyFieldLite fromValue(MessageLite value2) {
        LazyFieldLite lf = new LazyFieldLite();
        lf.setValue(value2);
        return lf;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LazyFieldLite)) {
            return false;
        }
        LazyFieldLite other = (LazyFieldLite) o;
        MessageLite value1 = this.value;
        MessageLite value2 = other.value;
        if (value1 == null && value2 == null) {
            return toByteString().equals(other.toByteString());
        }
        if (value1 != null && value2 != null) {
            return value1.equals(value2);
        }
        if (value1 != null) {
            return value1.equals(other.getValue(value1.getDefaultInstanceForType()));
        }
        return getValue(value2.getDefaultInstanceForType()).equals(value2);
    }

    public int hashCode() {
        return 1;
    }

    public boolean containsDefaultInstance() {
        ByteString byteString;
        return this.memoizedBytes == ByteString.EMPTY || (this.value == null && ((byteString = this.delayedBytes) == null || byteString == ByteString.EMPTY));
    }

    public void clear() {
        this.delayedBytes = null;
        this.value = null;
        this.memoizedBytes = null;
    }

    public void set(LazyFieldLite other) {
        this.delayedBytes = other.delayedBytes;
        this.value = other.value;
        this.memoizedBytes = other.memoizedBytes;
        ExtensionRegistryLite extensionRegistryLite = other.extensionRegistry;
        if (extensionRegistryLite != null) {
            this.extensionRegistry = extensionRegistryLite;
        }
    }

    public MessageLite getValue(MessageLite defaultInstance) {
        ensureInitialized(defaultInstance);
        return this.value;
    }

    public MessageLite setValue(MessageLite value2) {
        MessageLite originalValue = this.value;
        this.delayedBytes = null;
        this.memoizedBytes = null;
        this.value = value2;
        return originalValue;
    }

    public void merge(LazyFieldLite other) {
        ByteString byteString;
        if (!other.containsDefaultInstance()) {
            if (containsDefaultInstance()) {
                set(other);
                return;
            }
            if (this.extensionRegistry == null) {
                this.extensionRegistry = other.extensionRegistry;
            }
            ByteString byteString2 = this.delayedBytes;
            if (byteString2 != null && (byteString = other.delayedBytes) != null) {
                this.delayedBytes = byteString2.concat(byteString);
            } else if (this.value == null && other.value != null) {
                setValue(mergeValueAndBytes(other.value, this.delayedBytes, this.extensionRegistry));
            } else if (this.value != null && other.value == null) {
                setValue(mergeValueAndBytes(this.value, other.delayedBytes, other.extensionRegistry));
            } else if (other.extensionRegistry != null) {
                setValue(mergeValueAndBytes(this.value, other.toByteString(), other.extensionRegistry));
            } else if (this.extensionRegistry != null) {
                setValue(mergeValueAndBytes(other.value, toByteString(), this.extensionRegistry));
            } else {
                setValue(mergeValueAndBytes(this.value, other.toByteString(), EMPTY_REGISTRY));
            }
        }
    }

    public void mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry2) throws IOException {
        if (containsDefaultInstance()) {
            setByteString(input.readBytes(), extensionRegistry2);
            return;
        }
        if (this.extensionRegistry == null) {
            this.extensionRegistry = extensionRegistry2;
        }
        ByteString byteString = this.delayedBytes;
        if (byteString != null) {
            setByteString(byteString.concat(input.readBytes()), this.extensionRegistry);
            return;
        }
        try {
            setValue(this.value.toBuilder().mergeFrom(input, extensionRegistry2).build());
        } catch (InvalidProtocolBufferException e) {
        }
    }

    private static MessageLite mergeValueAndBytes(MessageLite value2, ByteString otherBytes, ExtensionRegistryLite extensionRegistry2) {
        try {
            return value2.toBuilder().mergeFrom(otherBytes, extensionRegistry2).build();
        } catch (InvalidProtocolBufferException e) {
            return value2;
        }
    }

    public void setByteString(ByteString bytes, ExtensionRegistryLite extensionRegistry2) {
        checkArguments(extensionRegistry2, bytes);
        this.delayedBytes = bytes;
        this.extensionRegistry = extensionRegistry2;
        this.value = null;
        this.memoizedBytes = null;
    }

    public int getSerializedSize() {
        if (this.memoizedBytes != null) {
            return this.memoizedBytes.size();
        }
        ByteString byteString = this.delayedBytes;
        if (byteString != null) {
            return byteString.size();
        }
        if (this.value != null) {
            return this.value.getSerializedSize();
        }
        return 0;
    }

    public ByteString toByteString() {
        if (this.memoizedBytes != null) {
            return this.memoizedBytes;
        }
        ByteString byteString = this.delayedBytes;
        if (byteString != null) {
            return byteString;
        }
        synchronized (this) {
            if (this.memoizedBytes != null) {
                return this.memoizedBytes;
            }
            if (this.value == null) {
                this.memoizedBytes = ByteString.EMPTY;
            } else {
                this.memoizedBytes = this.value.toByteString();
            }
            return this.memoizedBytes;
        }
    }

    /* access modifiers changed from: protected */
    public void ensureInitialized(MessageLite defaultInstance) {
        if (this.value == null) {
            synchronized (this) {
                if (this.value == null) {
                    try {
                        if (this.delayedBytes != null) {
                            this.value = (MessageLite) defaultInstance.getParserForType().parseFrom(this.delayedBytes, this.extensionRegistry);
                            this.memoizedBytes = this.delayedBytes;
                        } else {
                            this.value = defaultInstance;
                            this.memoizedBytes = ByteString.EMPTY;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        this.value = defaultInstance;
                        this.memoizedBytes = ByteString.EMPTY;
                    }
                }
            }
        }
    }

    private static void checkArguments(ExtensionRegistryLite extensionRegistry2, ByteString bytes) {
        if (extensionRegistry2 == null) {
            throw new NullPointerException("found null ExtensionRegistry");
        } else if (bytes == null) {
            throw new NullPointerException("found null ByteString");
        }
    }
}
