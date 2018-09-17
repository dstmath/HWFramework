package com.android.internal.telephony.cat;

import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.google.android.mms.pdu.PduPart;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.TimeZone;

/* compiled from: ResponseData */
class DTTZResponseData extends ResponseData {
    private Calendar mCalendar;

    public DTTZResponseData(Calendar cal) {
        this.mCalendar = cal;
    }

    public void format(ByteArrayOutputStream buf) {
        int i = 0;
        if (buf != null) {
            buf.write(CommandType.PROVIDE_LOCAL_INFORMATION.value() | PduPart.P_Q);
            byte[] data = new byte[8];
            data[0] = (byte) 7;
            if (this.mCalendar == null) {
                this.mCalendar = Calendar.getInstance();
            }
            data[1] = byteToBCD(this.mCalendar.get(1) % 100);
            data[2] = byteToBCD(this.mCalendar.get(2) + 1);
            data[3] = byteToBCD(this.mCalendar.get(5));
            data[4] = byteToBCD(this.mCalendar.get(11));
            data[5] = byteToBCD(this.mCalendar.get(12));
            data[6] = byteToBCD(this.mCalendar.get(13));
            String tz = SystemProperties.get("persist.sys.timezone", "");
            if (TextUtils.isEmpty(tz)) {
                data[7] = (byte) -1;
            } else {
                TimeZone zone = TimeZone.getTimeZone(tz);
                data[7] = getTZOffSetByte((long) (zone.getRawOffset() + zone.getDSTSavings()));
            }
            int length = data.length;
            while (i < length) {
                buf.write(data[i]);
                i++;
            }
        }
    }

    private byte byteToBCD(int value) {
        if (value >= 0 || value <= 99) {
            return (byte) ((value / 10) | ((value % 10) << 4));
        }
        CatLog.d((Object) this, "Err: byteToBCD conversion Value is " + value + " Value has to be between 0 and 99");
        return (byte) 0;
    }

    private byte getTZOffSetByte(long offSetVal) {
        int i;
        boolean isNegative = offSetVal < 0;
        byte bcdVal = byteToBCD((int) ((offSetVal / 900000) * ((long) (isNegative ? -1 : 1))));
        if (isNegative) {
            i = bcdVal | 8;
            bcdVal = (byte) i;
        } else {
            byte b = bcdVal;
        }
        return (byte) i;
    }
}
