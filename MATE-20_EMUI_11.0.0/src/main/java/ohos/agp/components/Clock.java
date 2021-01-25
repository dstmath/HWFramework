package ohos.agp.components;

import ohos.app.Context;

public class Clock extends TextClock {
    public Clock(Context context) {
        super(context);
    }

    public Clock(Context context, AttrSet attrSet) {
        super(context, attrSet);
    }

    public Clock(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    @Override // ohos.agp.components.TextClock
    public void setFormat12Hour(CharSequence charSequence) {
        super.setFormat12Hour(charSequence);
    }

    @Override // ohos.agp.components.TextClock
    public CharSequence getFormat12Hour() {
        return super.getFormat12Hour();
    }

    @Override // ohos.agp.components.TextClock
    public void setFormat24Hour(CharSequence charSequence) {
        super.setFormat24Hour(charSequence);
    }

    @Override // ohos.agp.components.TextClock
    public CharSequence getFormat24Hour() {
        return super.getFormat24Hour();
    }

    @Override // ohos.agp.components.TextClock
    public void setTimeZone(String str) {
        super.setTimeZone(str);
    }

    @Override // ohos.agp.components.TextClock
    public String getTimeZone() {
        return super.getTimeZone();
    }

    @Override // ohos.agp.components.TextClock
    public void setTime(long j) {
        super.setTime(j);
    }

    @Override // ohos.agp.components.TextClock
    public long getTime() {
        return super.getTime();
    }

    @Override // ohos.agp.components.TextClock
    public void set24HourModeEnabled(boolean z) {
        super.set24HourModeEnabled(z);
    }

    @Override // ohos.agp.components.TextClock
    public boolean is24HourModeEnabled() {
        return super.is24HourModeEnabled();
    }
}
