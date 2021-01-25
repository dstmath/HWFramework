package ohos.global.icu.util;

import java.util.Date;
import ohos.global.icu.impl.Grego;
import ohos.workschedulerservice.controller.WorkStatus;

public class AnnualTimeZoneRule extends TimeZoneRule {
    public static final int MAX_YEAR = Integer.MAX_VALUE;
    private static final long serialVersionUID = -8870666707791230688L;
    private final DateTimeRule dateTimeRule;
    private final int endYear;
    private final int startYear;

    @Override // ohos.global.icu.util.TimeZoneRule
    public boolean isTransitionRule() {
        return true;
    }

    public AnnualTimeZoneRule(String str, int i, int i2, DateTimeRule dateTimeRule2, int i3, int i4) {
        super(str, i, i2);
        this.dateTimeRule = dateTimeRule2;
        this.startYear = i3;
        this.endYear = i4;
    }

    public DateTimeRule getRule() {
        return this.dateTimeRule;
    }

    public int getStartYear() {
        return this.startYear;
    }

    public int getEndYear() {
        return this.endYear;
    }

    public Date getStartInYear(int i, int i2, int i3) {
        long j;
        long j2;
        if (i < this.startYear || i > this.endYear) {
            return null;
        }
        int dateRuleType = this.dateTimeRule.getDateRuleType();
        if (dateRuleType == 0) {
            j = Grego.fieldsToDay(i, this.dateTimeRule.getRuleMonth(), this.dateTimeRule.getRuleDayOfMonth());
        } else {
            boolean z = false;
            if (dateRuleType == 1) {
                int ruleWeekInMonth = this.dateTimeRule.getRuleWeekInMonth();
                if (ruleWeekInMonth > 0) {
                    j2 = Grego.fieldsToDay(i, this.dateTimeRule.getRuleMonth(), 1) + ((long) ((ruleWeekInMonth - 1) * 7));
                    z = true;
                } else {
                    j2 = Grego.fieldsToDay(i, this.dateTimeRule.getRuleMonth(), Grego.monthLength(i, this.dateTimeRule.getRuleMonth())) + ((long) ((ruleWeekInMonth + 1) * 7));
                }
            } else {
                int ruleMonth = this.dateTimeRule.getRuleMonth();
                int ruleDayOfMonth = this.dateTimeRule.getRuleDayOfMonth();
                if (dateRuleType != 3) {
                    z = true;
                } else if (ruleMonth == 1 && ruleDayOfMonth == 29 && !Grego.isLeapYear(i)) {
                    ruleDayOfMonth--;
                }
                j2 = Grego.fieldsToDay(i, ruleMonth, ruleDayOfMonth);
            }
            int ruleDayOfWeek = this.dateTimeRule.getRuleDayOfWeek() - Grego.dayOfWeek(j2);
            if (z) {
                if (ruleDayOfWeek < 0) {
                    ruleDayOfWeek += 7;
                }
            } else if (ruleDayOfWeek > 0) {
                ruleDayOfWeek -= 7;
            }
            j = ((long) ruleDayOfWeek) + j2;
        }
        long ruleMillisInDay = (j * WorkStatus.RARE_DELAY_TIME) + ((long) this.dateTimeRule.getRuleMillisInDay());
        if (this.dateTimeRule.getTimeRuleType() != 2) {
            ruleMillisInDay -= (long) i2;
        }
        if (this.dateTimeRule.getTimeRuleType() == 0) {
            ruleMillisInDay -= (long) i3;
        }
        return new Date(ruleMillisInDay);
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getFirstStart(int i, int i2) {
        return getStartInYear(this.startYear, i, i2);
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getFinalStart(int i, int i2) {
        int i3 = this.endYear;
        if (i3 == Integer.MAX_VALUE) {
            return null;
        }
        return getStartInYear(i3, i, i2);
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getNextStart(long j, int i, int i2, boolean z) {
        int i3 = Grego.timeToFields(j, (int[]) null)[0];
        if (i3 < this.startYear) {
            return getFirstStart(i, i2);
        }
        Date startInYear = getStartInYear(i3, i, i2);
        if (startInYear != null) {
            return (startInYear.getTime() < j || (!z && startInYear.getTime() == j)) ? getStartInYear(i3 + 1, i, i2) : startInYear;
        }
        return startInYear;
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getPreviousStart(long j, int i, int i2, boolean z) {
        int i3 = Grego.timeToFields(j, (int[]) null)[0];
        if (i3 > this.endYear) {
            return getFinalStart(i, i2);
        }
        Date startInYear = getStartInYear(i3, i, i2);
        if (startInYear != null) {
            return (startInYear.getTime() > j || (!z && startInYear.getTime() == j)) ? getStartInYear(i3 - 1, i, i2) : startInYear;
        }
        return startInYear;
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public boolean isEquivalentTo(TimeZoneRule timeZoneRule) {
        if (!(timeZoneRule instanceof AnnualTimeZoneRule)) {
            return false;
        }
        AnnualTimeZoneRule annualTimeZoneRule = (AnnualTimeZoneRule) timeZoneRule;
        if (this.startYear == annualTimeZoneRule.startYear && this.endYear == annualTimeZoneRule.endYear && this.dateTimeRule.equals(annualTimeZoneRule.dateTimeRule)) {
            return super.isEquivalentTo(timeZoneRule);
        }
        return false;
    }

    @Override // ohos.global.icu.util.TimeZoneRule, java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", rule={" + this.dateTimeRule + "}");
        StringBuilder sb2 = new StringBuilder();
        sb2.append(", startYear=");
        sb2.append(this.startYear);
        sb.append(sb2.toString());
        sb.append(", endYear=");
        int i = this.endYear;
        if (i == Integer.MAX_VALUE) {
            sb.append("max");
        } else {
            sb.append(i);
        }
        return sb.toString();
    }
}
