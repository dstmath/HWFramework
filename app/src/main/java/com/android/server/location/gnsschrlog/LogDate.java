package com.android.server.location.gnsschrlog;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

public class LogDate {
    private Calendar calendarNow;
    private int length;
    private Date value;

    public Date getValue() {
        return (Date) this.value.clone();
    }

    public void setValue(Date value) {
        if (value == null) {
            this.value = new Date();
        } else {
            this.value = (Date) value.clone();
        }
        this.calendarNow.setTime(this.value);
    }

    public int getLength() {
        return this.length;
    }

    public LogDate(int pLength) {
        this.length = 8;
        this.value = new Date();
        this.calendarNow = Calendar.getInstance();
        this.length = pLength;
    }

    public byte[] toByteArray() {
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
        bytebuf.put((byte) (this.calendarNow.get(1) - 2000));
        bytebuf.put((byte) (this.calendarNow.get(2) + 1));
        bytebuf.put((byte) this.calendarNow.get(5));
        bytebuf.put((byte) this.calendarNow.get(11));
        bytebuf.put((byte) this.calendarNow.get(12));
        bytebuf.put((byte) this.calendarNow.get(13));
        if (this.length == 8) {
            bytebuf.put(ByteConvert.shortToBytes((short) this.calendarNow.get(14)));
        }
        return bytebuf.array();
    }
}
