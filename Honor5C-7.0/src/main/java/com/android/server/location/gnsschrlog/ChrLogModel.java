package com.android.server.location.gnsschrlog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChrLogModel {
    public CSegCOMHEAD chrLogComHeadModel;
    public CSegFILEHEAD chrLogFileHeadModel;
    public List<ChrLogBaseModel> logEvents;

    public ChrLogModel() {
        this.chrLogFileHeadModel = new CSegFILEHEAD();
        this.chrLogComHeadModel = new CSegCOMHEAD();
        this.logEvents = new ArrayList();
    }

    public byte[] toByteArray() {
        int length = this.chrLogFileHeadModel.getTotalBytes() + this.chrLogComHeadModel.getTotalBytes();
        for (ChrLogBaseModel logEvent : this.logEvents) {
            length += logEvent.getTotalBytes();
        }
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[length]);
        bytebuf.put(this.chrLogFileHeadModel.toByteArray());
        bytebuf.put(this.chrLogComHeadModel.toByteArray());
        for (ChrLogBaseModel logEvent2 : this.logEvents) {
            bytebuf.put(logEvent2.toByteArray());
        }
        return bytebuf.array();
    }
}
