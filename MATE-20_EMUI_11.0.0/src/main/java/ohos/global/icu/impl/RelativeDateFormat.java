package ohos.global.icu.impl;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.DisplayContext;
import ohos.global.icu.text.MessageFormat;
import ohos.global.icu.text.SimpleDateFormat;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class RelativeDateFormat extends DateFormat {
    private static final long serialVersionUID = 1131984966440549435L;
    private transient BreakIterator capitalizationBrkIter = null;
    private boolean capitalizationInfoIsSet = false;
    private boolean capitalizationOfRelativeUnitsForListOrMenu = false;
    private boolean capitalizationOfRelativeUnitsForStandAlone = false;
    private boolean combinedFormatHasDateAtStart = false;
    private MessageFormat fCombinedFormat;
    private DateFormat fDateFormat;
    private String fDatePattern = null;
    int fDateStyle;
    private SimpleDateFormat fDateTimeFormat = null;
    private transient List<URelativeString> fDates = null;
    ULocale fLocale;
    private DateFormat fTimeFormat;
    private String fTimePattern = null;
    int fTimeStyle;

    public static class URelativeString {
        public int offset;
        public String string;

        URelativeString(int i, String str) {
            this.offset = i;
            this.string = str;
        }

        URelativeString(String str, String str2) {
            this.offset = Integer.parseInt(str);
            this.string = str2;
        }
    }

    public RelativeDateFormat(int i, int i2, ULocale uLocale, Calendar calendar) {
        this.calendar = calendar;
        this.fLocale = uLocale;
        this.fTimeStyle = i;
        this.fDateStyle = i2;
        int i3 = this.fDateStyle;
        if (i3 != -1) {
            DateFormat dateInstance = DateFormat.getDateInstance(i3 & -129, uLocale);
            if (dateInstance instanceof SimpleDateFormat) {
                this.fDateTimeFormat = (SimpleDateFormat) dateInstance;
                this.fDatePattern = this.fDateTimeFormat.toPattern();
                int i4 = this.fTimeStyle;
                if (i4 != -1) {
                    DateFormat timeInstance = DateFormat.getTimeInstance(i4 & -129, uLocale);
                    if (timeInstance instanceof SimpleDateFormat) {
                        this.fTimePattern = ((SimpleDateFormat) timeInstance).toPattern();
                    }
                }
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for date style");
            }
        } else {
            DateFormat timeInstance2 = DateFormat.getTimeInstance(this.fTimeStyle & -129, uLocale);
            if (timeInstance2 instanceof SimpleDateFormat) {
                this.fDateTimeFormat = (SimpleDateFormat) timeInstance2;
                this.fTimePattern = this.fDateTimeFormat.toPattern();
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for time style");
            }
        }
        initializeCalendar(null, this.fLocale);
        loadDates();
        initializeCombinedFormat(this.calendar, this.fLocale);
    }

    @Override // ohos.global.icu.text.DateFormat
    public StringBuffer format(Calendar calendar, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        DisplayContext context = getContext(DisplayContext.Type.CAPITALIZATION);
        String stringForDay = this.fDateStyle != -1 ? getStringForDay(dayDifference(calendar)) : null;
        if (this.fDateTimeFormat != null) {
            if (stringForDay == null || this.fDatePattern == null || !(this.fTimePattern == null || this.fCombinedFormat == null || this.combinedFormatHasDateAtStart)) {
                this.fDateTimeFormat.setContext(context);
            } else {
                if (stringForDay.length() > 0 && UCharacter.isLowerCase(stringForDay.codePointAt(0)) && (context == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || ((context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && this.capitalizationOfRelativeUnitsForListOrMenu) || (context == DisplayContext.CAPITALIZATION_FOR_STANDALONE && this.capitalizationOfRelativeUnitsForStandAlone)))) {
                    if (this.capitalizationBrkIter == null) {
                        this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.fLocale);
                    }
                    stringForDay = UCharacter.toTitleCase(this.fLocale, stringForDay, this.capitalizationBrkIter, 768);
                }
                this.fDateTimeFormat.setContext(DisplayContext.CAPITALIZATION_NONE);
            }
        }
        if (this.fDateTimeFormat == null || (this.fDatePattern == null && this.fTimePattern == null)) {
            DateFormat dateFormat = this.fDateFormat;
            if (dateFormat != null) {
                if (stringForDay != null) {
                    stringBuffer.append(stringForDay);
                } else {
                    dateFormat.format(calendar, stringBuffer, fieldPosition);
                }
            }
        } else {
            String str = this.fDatePattern;
            if (str == null) {
                this.fDateTimeFormat.applyPattern(this.fTimePattern);
                this.fDateTimeFormat.format(calendar, stringBuffer, fieldPosition);
            } else if (this.fTimePattern != null) {
                if (stringForDay != null) {
                    str = "'" + stringForDay.replace("'", "''") + "'";
                }
                StringBuffer stringBuffer2 = new StringBuffer("");
                this.fCombinedFormat.format(new Object[]{this.fTimePattern, str}, stringBuffer2, new FieldPosition(0));
                this.fDateTimeFormat.applyPattern(stringBuffer2.toString());
                this.fDateTimeFormat.format(calendar, stringBuffer, fieldPosition);
            } else if (stringForDay != null) {
                stringBuffer.append(stringForDay);
            } else {
                this.fDateTimeFormat.applyPattern(str);
                this.fDateTimeFormat.format(calendar, stringBuffer, fieldPosition);
            }
        }
        return stringBuffer;
    }

    @Override // ohos.global.icu.text.DateFormat
    public void parse(String str, Calendar calendar, ParsePosition parsePosition) {
        throw new UnsupportedOperationException("Relative Date parse is not implemented yet");
    }

    @Override // ohos.global.icu.text.DateFormat
    public void setContext(DisplayContext displayContext) {
        super.setContext(displayContext);
        if (!this.capitalizationInfoIsSet && (displayContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || displayContext == DisplayContext.CAPITALIZATION_FOR_STANDALONE)) {
            initCapitalizationContextInfo(this.fLocale);
            this.capitalizationInfoIsSet = true;
        }
        if (this.capitalizationBrkIter != null) {
            return;
        }
        if (displayContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || ((displayContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && this.capitalizationOfRelativeUnitsForListOrMenu) || (displayContext == DisplayContext.CAPITALIZATION_FOR_STANDALONE && this.capitalizationOfRelativeUnitsForStandAlone))) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.fLocale);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getStringForDay(int i) {
        if (this.fDates == null) {
            loadDates();
        }
        for (URelativeString uRelativeString : this.fDates) {
            if (uRelativeString.offset == i) {
                return uRelativeString.string;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public final class RelDateFmtDataSink extends UResource.Sink {
        private RelDateFmtDataSink() {
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            if (value.getType() != 3) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    try {
                        int parseInt = Integer.parseInt(key.toString());
                        if (RelativeDateFormat.this.getStringForDay(parseInt) == null) {
                            RelativeDateFormat.this.fDates.add(new URelativeString(parseInt, value.getString()));
                        }
                    } catch (NumberFormatException unused) {
                        return;
                    }
                }
            }
        }
    }

    private synchronized void loadDates() {
        this.fDates = new ArrayList();
        UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, this.fLocale).getAllItemsWithFallback("fields/day/relative", new RelDateFmtDataSink());
    }

    private void initCapitalizationContextInfo(ULocale uLocale) {
        try {
            int[] intVector = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale).getWithFallback("contextTransforms/relative").getIntVector();
            if (intVector.length >= 2) {
                boolean z = false;
                this.capitalizationOfRelativeUnitsForListOrMenu = intVector[0] != 0;
                if (intVector[1] != 0) {
                    z = true;
                }
                this.capitalizationOfRelativeUnitsForStandAlone = z;
            }
        } catch (MissingResourceException unused) {
        }
    }

    private static int dayDifference(Calendar calendar) {
        Calendar calendar2 = (Calendar) calendar.clone();
        Date date = new Date(System.currentTimeMillis());
        calendar2.clear();
        calendar2.setTime(date);
        return calendar.get(20) - calendar2.get(20);
    }

    private Calendar initializeCalendar(TimeZone timeZone, ULocale uLocale) {
        if (this.calendar == null) {
            if (timeZone == null) {
                this.calendar = Calendar.getInstance(uLocale);
            } else {
                this.calendar = Calendar.getInstance(timeZone, uLocale);
            }
        }
        return this.calendar;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x007e  */
    private MessageFormat initializeCombinedFormat(Calendar calendar, ULocale uLocale) {
        String str;
        int i;
        int i2;
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        ICUResourceBundle findWithFallback = bundleInstance.findWithFallback("calendar/" + calendar.getType() + "/DateTimePatterns");
        if (findWithFallback == null && !calendar.getType().equals("gregorian")) {
            findWithFallback = bundleInstance.findWithFallback("calendar/gregorian/DateTimePatterns");
        }
        if (findWithFallback == null || findWithFallback.getSize() < 9) {
            str = "{1} {0}";
        } else {
            if (findWithFallback.getSize() >= 13) {
                int i3 = this.fDateStyle;
                if (i3 < 0 || i3 > 3) {
                    int i4 = this.fDateStyle;
                    if (i4 >= 128 && i4 <= 131) {
                        i2 = (i4 + 1) - 128;
                    }
                } else {
                    i2 = i3 + 1;
                }
                i = i2 + 8;
                str = findWithFallback.get(i).getType() != 8 ? findWithFallback.get(i).getString(0) : findWithFallback.getString(i);
            }
            i = 8;
            if (findWithFallback.get(i).getType() != 8) {
            }
        }
        this.combinedFormatHasDateAtStart = str.startsWith("{1}");
        this.fCombinedFormat = new MessageFormat(str, uLocale);
        return this.fCombinedFormat;
    }
}
