package android.icu.util;

public class HebrewHoliday extends Holiday {
    public static HebrewHoliday ESTHER = new HebrewHoliday(6, 13, "Fast of Esther");
    public static HebrewHoliday GEDALIAH = new HebrewHoliday(0, 3, "Fast of Gedaliah");
    public static HebrewHoliday HANUKKAH = new HebrewHoliday(2, 25, "Hanukkah");
    public static HebrewHoliday HOSHANAH_RABBAH = new HebrewHoliday(0, 21, "Hoshanah Rabbah");
    public static HebrewHoliday LAG_BOMER = new HebrewHoliday(8, 18, "Lab B'Omer");
    public static HebrewHoliday PASSOVER = new HebrewHoliday(7, 15, 8, "Passover");
    public static HebrewHoliday PESACH_SHEINI = new HebrewHoliday(8, 14, "Pesach Sheini");
    public static HebrewHoliday PURIM = new HebrewHoliday(6, 14, "Purim");
    public static HebrewHoliday ROSH_HASHANAH = new HebrewHoliday(0, 1, 2, "Rosh Hashanah");
    public static HebrewHoliday SELIHOT = new HebrewHoliday(12, 21, "Selihot");
    public static HebrewHoliday SHAVUOT = new HebrewHoliday(9, 6, 2, "Shavuot");
    public static HebrewHoliday SHEMINI_ATZERET = new HebrewHoliday(0, 22, "Shemini Atzeret");
    public static HebrewHoliday SHUSHAN_PURIM = new HebrewHoliday(6, 15, "Shushan Purim");
    public static HebrewHoliday SIMCHAT_TORAH = new HebrewHoliday(0, 23, "Simchat Torah");
    public static HebrewHoliday SUKKOT = new HebrewHoliday(0, 15, 6, "Sukkot");
    public static HebrewHoliday TAMMUZ_17 = new HebrewHoliday(10, 17, "Fast of Tammuz 17");
    public static HebrewHoliday TEVET_10 = new HebrewHoliday(3, 10, "Fast of Tevet 10");
    public static HebrewHoliday TISHA_BAV = new HebrewHoliday(11, 9, "Fast of Tisha B'Av");
    public static HebrewHoliday TU_BSHEVAT = new HebrewHoliday(4, 15, "Tu B'Shevat");
    public static HebrewHoliday YOM_HAATZMAUT = new HebrewHoliday(8, 5, "Yom Ha'Atzmaut");
    public static HebrewHoliday YOM_HASHOAH = new HebrewHoliday(7, 27, "Yom Hashoah");
    public static HebrewHoliday YOM_HAZIKARON = new HebrewHoliday(8, 4, "Yom Hazikaron");
    public static HebrewHoliday YOM_KIPPUR = new HebrewHoliday(0, 10, "Yom Kippur");
    public static HebrewHoliday YOM_YERUSHALAYIM = new HebrewHoliday(8, 28, "Yom Yerushalayim");
    private static final HebrewCalendar gCalendar = new HebrewCalendar();

    public HebrewHoliday(int month, int date, String name) {
        this(month, date, 1, name);
    }

    public HebrewHoliday(int month, int date, int length, String name) {
        super(name, new SimpleDateRule(month, date, gCalendar));
    }
}
