package com.android.framework.protobuf;

import java.util.List;

public interface ProtocolStringList extends List<String> {
    List<ByteString> asByteStringList();
}
