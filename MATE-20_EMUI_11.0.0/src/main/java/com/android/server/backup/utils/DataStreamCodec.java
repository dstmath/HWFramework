package com.android.server.backup.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface DataStreamCodec<T> {
    T deserialize(DataInputStream dataInputStream) throws IOException;

    void serialize(T t, DataOutputStream dataOutputStream) throws IOException;
}
