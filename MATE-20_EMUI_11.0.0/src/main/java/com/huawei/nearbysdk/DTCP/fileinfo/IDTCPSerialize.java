package com.huawei.nearbysdk.DTCP.fileinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IDTCPSerialize {
    void readFromDTCPStream(DataInputStream dataInputStream, int i) throws IOException;

    void writeToDTCPStream(DataOutputStream dataOutputStream, int i) throws IOException;
}
