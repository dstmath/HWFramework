package android.hishow;

import android.media.RingtoneManager;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Calendar;

public class AlarmInfo implements Parcelable {
    public static final Parcelable.Creator<AlarmInfo> CREATOR = new Parcelable.Creator<AlarmInfo>() {
        /* class android.hishow.AlarmInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AlarmInfo createFromParcel(Parcel source) {
            return new AlarmInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public AlarmInfo[] newArray(int size) {
            return new AlarmInfo[size];
        }
    };
    public static final int DEFAULT_ALARM_ID = -1;
    public static final int EVERYDAY_CODE = 127;
    public static final int EVERYDAY_RING = 2;
    public static final int JUSTONCE_RING = 0;
    public static final int MONTOFRIDAY_CODE = 31;
    public static final int MONTOFRIDAY_RING = 1;
    public static final int NODAY_CODE = 0;
    public static final int USER_DEFINED_RING = 3;
    public static final int WORKINGDAT_RING = 4;
    private int alarmType;
    private String alert;
    private int daysOfWeek;
    private String daysOfWeekShow;
    private int daysOfWeekType;
    private boolean enabled;
    private int hour;
    private int id;
    private String label;
    private int minutes;
    private long time;
    private boolean vibrate;
    private int volume;

    public AlarmInfo() {
        this.id = -1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        this.hour = calendar.get(11);
        this.minutes = calendar.get(12);
        this.vibrate = true;
        this.daysOfWeek = 0;
        this.daysOfWeekType = 0;
        this.alert = RingtoneManager.getDefaultUri(4).toString();
    }

    public AlarmInfo(Parcel source) {
        this.id = source.readInt();
        boolean z = false;
        this.enabled = source.readInt() == 1;
        this.hour = source.readInt();
        this.minutes = source.readInt();
        this.daysOfWeek = source.readInt();
        this.daysOfWeekType = source.readInt();
        this.daysOfWeekShow = source.readString();
        this.alarmType = source.readInt();
        this.time = source.readLong();
        this.vibrate = source.readInt() == 1 ? true : z;
        this.volume = source.readInt();
        this.label = source.readString();
        this.alert = source.readString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.enabled ? 1 : 0);
        dest.writeInt(this.hour);
        dest.writeInt(this.minutes);
        dest.writeInt(this.daysOfWeek);
        dest.writeInt(this.daysOfWeekType);
        dest.writeString(this.daysOfWeekShow);
        dest.writeInt(this.alarmType);
        dest.writeLong(this.time);
        dest.writeInt(this.vibrate ? 1 : 0);
        dest.writeInt(this.volume);
        dest.writeString(this.label);
        dest.writeString(this.alert);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id2) {
        this.id = id2;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled2) {
        this.enabled = enabled2;
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int hour2) {
        this.hour = hour2;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public void setMinutes(int minutes2) {
        this.minutes = minutes2;
    }

    public int getDaysOfWeek() {
        return this.daysOfWeek;
    }

    public void setDaysOfWeek(int daysOfWeek2) {
        this.daysOfWeek = daysOfWeek2;
    }

    public int getDaysOfWeekType() {
        return this.daysOfWeekType;
    }

    public void setDaysOfWeekType(int daysOfWeekType2) {
        this.daysOfWeekType = daysOfWeekType2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public String getDaysOfWeekShow() {
        return this.daysOfWeekShow;
    }

    public void setDaysOfWeekShow(String daysOfWeekShow2) {
        this.daysOfWeekShow = daysOfWeekShow2;
    }

    public boolean isVibrate() {
        return this.vibrate;
    }

    public void setVibrate(boolean vibrate2) {
        this.vibrate = vibrate2;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label2) {
        this.label = label2;
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int volume2) {
        this.volume = volume2;
    }

    public String getAlert() {
        return this.alert;
    }

    public void setAlert(String alert2) {
        this.alert = alert2;
    }

    public int getAlarmType() {
        return this.alarmType;
    }

    public void setAlarmType(int alarmtype) {
        this.alarmType = alarmtype;
    }
}
