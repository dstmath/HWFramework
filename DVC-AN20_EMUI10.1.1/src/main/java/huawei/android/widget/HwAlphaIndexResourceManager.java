package huawei.android.widget;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwAlphaIndexResourceManager {
    private static final int ALPHABET_NUM = 2;
    private static final int ALPHA_NUMBER_DICHOTOMY = 2;
    private static final int BUFFER_SIZE = 2048;
    private static final int DECIMAL_RADIX = 10;
    private static final int INITIAL_CAPACITY = 32;
    public static final int LANDSCAPE_ALPHA_COUNT_MAX = 18;
    private static final int LANGUAGE_FILE_MAX_LINE = 100000;
    private static final String LANGUAGE_INDEXER_FILE = "LanguageIndexerFile.conf";
    public static final int PORTRAIT_ALPHA_COUNT_MAX = 28;
    public static final String ROOT_ALPHA_INDEX = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z";
    private static final String TAG = "HwAlphaIndexResourceManager";
    private static Map<String, String> sLanguageIndexerMap = new HashMap((int) INITIAL_CAPACITY);
    private List<Alpha> mLabelList = new ArrayList((int) INITIAL_CAPACITY);

    static {
        sLanguageIndexerMap.put("ROOT_ALPHA_INDEX", ROOT_ALPHA_INDEX);
        sLanguageIndexerMap.put("ALBANIAN_ALPHA_INDEX", "A B C Ç D(1) DH(1) E Ë F G(1) GJ(1) H I J K L(1) LL(1) M N(1) NJ(1) O P Q R(1) RR(1) S SH(1) T(1) TH(1) U V X(1) XH(1) Y Z ZH");
        sLanguageIndexerMap.put("ARMENIAN_ALPHA_INDEX", "Ա Բ Գ Դ Ե Զ Է(1) Ը(1) Թ Ժ Ի Լ Խ(1) Ծ(1) Կ Հ Ձ(1) Ղ(1) Ճ(1) Մ Յ(1) Ն(1) Շ Ո(1) Չ(1) Պ Ջ(1) Ռ(1) Ս Վ Տ(1) Ր(1) Ց Փ(1) Ք(1) Օ Ֆ");
        sLanguageIndexerMap.put("ARABIC_ALPHA_INDEX", "ا ب ت ث ج ح خ د ذ ر ز س ش ص ض ط ظ ع غ ف ق ك ل م ن ه و ي");
        sLanguageIndexerMap.put("AZERBAIJANI_ALPHA_INDEX", "A B C(1) Ç(1) D E(1) Ə(1) F G(1) Ğ(1) H X I İ J K Q L M N O(1) Ö(1) P R S Ş T U(1) Ü(1) V Y Z W");
        sLanguageIndexerMap.put("BELARUSIAN_ALPHA_INDEX", "А Б В Г Д Е Ё Ж З І(1) Й(1) К Л М Н О П Р С Т У Ў(1) Ф(1) Х Ц Ч Ш Ы(1) Ь(1) Э(1) Ю Я");
        sLanguageIndexerMap.put("BENGALI_ALPHA_INDEX", "অ আ ই(1) ঈ(1) উ ঊ(1) ঋ(1) এ ঐ(1) ও(1) ঔ ক(1) খ(1) গ ঘ(1) ঙ(1) চ(1) ছ জ ঝ(1) ঞ(1) ট(1) ঠ ড(1) ঢ(1) ণ(1) ত থ দ(1) ধ(1) ন(1) প ফ(1) ব(1) ভ ম(1) য(1) র ল শ ষ(1) স(1) হ");
        sLanguageIndexerMap.put("BOSNIAN_ALPHA_INDEX", "A B C(1) Č(1) Ć(1) D DŽ(2) Đ E F G H I J K L LJ M N NJ O P Q R S Š(2) T U V W X Y Z Ž(2)");
        sLanguageIndexerMap.put("BULGARIAN_ALPHA_INDEX", "А Б В Г Д Е Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ю Я");
        sLanguageIndexerMap.put("BURMESE_ALPHA_INDEX", "က ခ ဂ ဃ င စ ဆ ဇ ဈ ဉ ည ဋ(1) ဌ(1) ဍ ဎ ဏ တ(1) ထ(1) ဒ ဓ န ပ ဖ ဗ(1) ဘ(1) မ ယ(1) ရ(1) လ ဝ သ(1) ဟ(1) ဠ အ");
        sLanguageIndexerMap.put("CROATIAN_ALPHA_INDEX", "A B C(1) Č(1) Ć(1) D DŽ(2) Đ E F G H I J K L LJ M N NJ O P Q R S Š(2) T U V W X Y Z Ž(2)");
        sLanguageIndexerMap.put("CZECH_ALPHA_INDEX", "A B C Č(2) D E F G H CH I J K L M N O P Q R Ř(2) S Š(2) T U V W X Y Z Ž(2)");
        sLanguageIndexerMap.put("DANISH_ALPHA_INDEX", "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Æ Ø Å");
        sLanguageIndexerMap.put("ESTONIAN_ALPHA_INDEX", "A B C D E F G H I J K L M N O P Q R S(1) Š(1) Z Ž T U V Õ Ä(1) Ö(1) Ü(1) X Y");
        sLanguageIndexerMap.put("FINNISH_ALPHA_INDEX", "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Å(1) Ä(1) Ö");
        sLanguageIndexerMap.put("FILIPINO_ALPHA_INDEX", "A B C D E F G H I J K L M N Ñ NG O P Q R S T U V W X Y Z");
        sLanguageIndexerMap.put("GEORGIAN_ALPHA_INDEX", "ა ბ გ დ ე ვ ზ თ ი კ ლ მ ნ ო პ(1) ჟ(1) რ ს ტ უ(1) ფ(1) ქ ღ(1) ყ(1) შ ჩ ც ძ წ(1) ჭ(1) ხ(1) ჯ ჰ");
        sLanguageIndexerMap.put("GREEK_ALPHA_INDEX", "Α Β Γ Δ Ε Ζ Η Θ Ι Κ Λ Μ Ν Ξ Ο Π Ρ Σ Τ Υ Φ Χ Ψ Ω");
        sLanguageIndexerMap.put("GUJARATI_ALPHA_INDEX", "અ આ ઇ ઈ ઉ ઊ(1) એ(1) ઐ(1) ઓ ઔ ક ખ(1) ગ(1) ઘ ઙ(1) ચ(1) છ(1) જ ઝ(1) ઞ(1) ટ(1) ઠ ડ(1) ઢ(1) ણ(1) ત થ દ(1) ધ(1) ન(1) પ ફ(1) બ(1) ભ(1) મ ય ર(1) લ(1) વ(1) શ ષ(1) સ(1) હ(1) ળ");
        sLanguageIndexerMap.put("HEBREW_ALPHA_INDEX", "א ב ג ד ה ו ז ח ט י כ ל מ נ ס ע פ צ ק ר ש ת");
        sLanguageIndexerMap.put("HINDI_ALPHA_INDEX", "अ आ(1) इ(1) ई उ(1) ऊ(1) ऋ ए(1) ऐ(1) ओ औ(1) क(1) ख ग(1) घ(1) ङ च(1) छ(1) ज झ(1) ञ(1) ट ठ(1) ड(1) ढ ण(1) त(1) थ द(1) ध(1) न प(1) फ(1) ब भ(1) म(1) य र(1) ल(1) व श(1) ष(1) स(1) ह");
        sLanguageIndexerMap.put("HUNGARIAN_ALPHA_INDEX", "A B C CS(2) D DZ(2) DZS(2) E F G(1) GY(1) H I J K L(1) LY(1) M N NY O(1) Ö(1) P Q R S SZ(2) T TY U(1) Ü(1) V W X Y Z ZS(2)");
        sLanguageIndexerMap.put("ICELANDIC_ALPHA_INDEX", "A Á B D Ð E É F G H I(1) Í(1) J K L M N O(1) Ó(1) P R S T U(1) Ú(1) V X Y(1) Ý(1) Þ Æ Ö");
        sLanguageIndexerMap.put("JAPANESE_ALPHA_INDEX", "あ か さ た な は ま や ら わ 他");
        sLanguageIndexerMap.put("KAZAKH_ALPHA_INDEX", "А Ә Б В Г(1) Ғ(1) Д Е(1) Ё(1) Ж З И(1) Й(1) К Қ(1) Л(1) М Н(1) Ң(1) О Ө(1) П(1) Р(1) С Т У(1) Ұ(1) Ү(1) Ф Х(1) Һ(1) Ц(1) Ч Ш Щ(1) Ъ(1) Ы(1) І Ь(1) Э(1) Ю Я");
        sLanguageIndexerMap.put("KYRGYZ_ALPHA_INDEX", "А Б В Г Д(1) Е(1) Ё(1) Ж З И(1) Й(1) К Л М Н(1) Ң(1) О Ө П Р С Т У Ү Ф(1) Х(1) Ц(1) Ч Ш Щ(1) Ъ(1) Ы(1) Ь Э Ю Я");
        sLanguageIndexerMap.put("KHMER_ALPHA_INDEX", "ក ខ គ ឃ ង(1) ច(1) ឆ ជ(1) ឈ(1) ញ(1) ដ ឋ(1) ឌ(1) ឍ(1) ណ ត ថ ទ ធ ន ប ផ ព ភ ម យ រ ល វ ស ហ ឡ អ");
        sLanguageIndexerMap.put("KANNADA_ALPHA_INDEX", "ಅ ಆ ಈ ಉ ಎ ಐ ಕ ಗ ಚ ಜ ತ ದ ಧ ನ ಪ ಫ ಬ ಭ ಮ ಯ ರ ಲ ವ ಶ ಷ ಸ ಹ");
        sLanguageIndexerMap.put("KOREAN_ALPHA_INDEX", "ㄱ ㄴ ㄷ ㄹ ㅁ ㅂ ㅅ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ");
        sLanguageIndexerMap.put("LAO_ALPHA_INDEX", "ກ ຂ ຄ ງ ຈ ສ ຊ ຍ ດ ຕ ຖ ທ ນ ບ ປ ຜ ຝ ພ ຟ ມ ຢ ຣ ລ ວ ຫ ອ ຮ");
        sLanguageIndexerMap.put("LATVIAN_ALPHA_INDEX", "A B C Č D E F G(1) Ģ(1) H I J K(1) Ķ(1) L Ļ M N(1) Ņ(1) O P Q R S(1) Š(1) T U V W X Z Ž");
        sLanguageIndexerMap.put("LITHUANIAN_ALPHA_INDEX", "A B C Č D E F G H I J K L M N O P R S Š T U V Z Ž");
        sLanguageIndexerMap.put("MACEDONIAN_ALPHA_INDEX", "А Б В Г Д Ѓ Е Ж З Ѕ И Ј К Л(1) Љ(1) М Н(1) Њ(1) О П Р С Т(1) Ќ(1) У Ф Х Ц Ч Џ Ш");
        sLanguageIndexerMap.put("MARATHI_ALPHA_INDEX", "अ आ इ ई उ ऊ ऋ(1) ए(1) ऐ(1) ओ(1) औ(1) अं अ: क ख ग घ(1) ङ(1) च(1) छ(1) ज(1) झ ञ ट ठ ड(1) ढ(1) ण(1) त(1) थ(1) द ध न प(1) फ(1) ब(1) भ(1) म(1) य र ल व(1) श(1) ष(1) स(1) ह(1) ळ क्ष ज्ञ");
        sLanguageIndexerMap.put("NEPALI_ALPHA_INDEX", "अ आ इ(1) ई(1) उ(1) ऊ ऋ ए(1) ऐ(1) ओ(1) औ क ख(1) ग(1) घ(1) ङ च छ ज(1) झ(1) ञ(1) ट ठ ड ढ(1) ण(1) त(1) थ द(1) ध(1) न(1) प फ ब भ(1) म(1) य(1) र ल व(1) श(1) ष(1) स ह");
        sLanguageIndexerMap.put("NORWEGIAN_ALPHA_INDEX", "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Æ Ø Å");
        sLanguageIndexerMap.put("PASHOTO_ALPHA_INDEX", "آ ا ب پ ت(1) ث(1) ج چ ح خ(1) د(1) ذ ر ز(1) ژ(1) س ش ص(1) ض(1) ط ظ ع غ ف(1) ق(1) ک گ ل م ن ه و ی");
        sLanguageIndexerMap.put("PERSIAN_ALPHA_INDEX", "آ ا ب پ ت(1) ث(1) ج چ ح خ(1) د(1) ذ ر ز(1) ژ(1) س ش ص(1) ض(1) ط ظ ع غ ف(1) ق(1) ک گ ل م ن و ه ی");
        sLanguageIndexerMap.put("POLISH_ALPHA_INDEX", "A Ą B C Ć D E Ę F G H I J K L Ł M N Ń O Ó P Q R S Ś T U V W X Y Z Ź Ż");
        sLanguageIndexerMap.put("PUNJABI_ALPHA_INDEX", "ੳ ਅ(1) ੲ(1) ਸ(1) ਹ ਕ ਖ ਗ ਘ(1) ਙ(1) ਚ ਛ ਜ ਝ(1) ਞ(1) ਟ ਠ ਡ ਢ(1) ਣ(1) ਤ ਥ ਦ ਧ ਨ ਪ ਫ ਬ ਭ(1) ਮ(1) ਯ(1) ਰ ਲ ਵ ੜ");
        sLanguageIndexerMap.put("ROMANIAN_ALPHA_INDEX", "A Ă(1) Â(1) B C D E F G H I(1) Î(1) J K L M N O P Q R S(1) Ș(1) T Ț U V W X Y Z");
        sLanguageIndexerMap.put("RUSSIAN_ALPHA_INDEX", "А Б В Г Д Е Ж З И(1) Й(1) К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ(1) Ы(1) Э Ю Я");
        sLanguageIndexerMap.put("SERBIAN_ALPHA_INDEX", "A B C Č(1) Ć(1) D Đ E F G H I J K L LJ M N NJ O P R S Š T U V Z Ž");
        sLanguageIndexerMap.put("SINHALA_ALPHA_INDEX", "අ ආ(1) ඇ(1) ඈ(1) ඉ(1) ඊ උ(1) ඌ(1) ඍ(1) එ(1) ඒ ඓ(1) ඔ(1) ඕ(1) ඖ ක(1) ඛ(1) ග(1) ඝ ඞ(1) ඟ(1) ච(1) ඡ ජ(1) ඣ(1) ඥ(1) ඤ ට(1) ඨ(1) ඩ(1) ඪ ණ(1) ඬ(1) ත(1) ථ ද(1) ධ(1) න(1) ඳ ප(1) ඵ(1) බ(1) භ ම(1) ඹ(1) ය(1) ර ල(1) ව(1) ශ(1) ෂ ස(1) හ(1) ළ(1) ෆ");
        sLanguageIndexerMap.put("SLOVAK_ALPHA_INDEX", "A Ä(2) B C Č D E F G H CH I J K L M N O Ô(2) P Q R S Š T U V W X Y Z Ž(2)");
        sLanguageIndexerMap.put("SLOVENIAN_ALPHA_INDEX", "A B C(1) Č(1) Ć(1) D Đ E F G H I J K L M N O P Q R S Š T U V W X Y Z Ž(2)");
        sLanguageIndexerMap.put("SPANISH_ALPHA_INDEX", "A B C D E F G H I J K L M N Ñ O P Q R S T U V W X Y Z");
        sLanguageIndexerMap.put("SWAHILI_ALPHA_INDEX", ROOT_ALPHA_INDEX);
        sLanguageIndexerMap.put("AMHARIC_ALPHA_INDEX", "ሀ ለ ሐ መ ሠ ረ ሰ ሸ ቀ በ ተ ቸ ኀ ነ(1) ኘ(1) አ ከ(1) ኸ(1) ወ ዐ(1) ዘ(1) ዠ(1) የ ደ ጀ ገ ጠ(1) ጨ(1) ጰ ጸ(1) ፀ(1) ፈ ፐ");
        sLanguageIndexerMap.put("MAORI_ALPHA_INDEX", ROOT_ALPHA_INDEX);
        sLanguageIndexerMap.put("TELUGR_ALPHA_INDEX", "అ ఆ ఇ ఈ ఉ(1) ఊ(1) ఎ(1) ఏ(1) ఐ ఒ ఓ ఔ(1) అం(1) క(1) ఖ(1) గ ఘ చ ఛ జ ఝ(1) ట(1) ఠ(1) డ(1) ఢ(1) ణ త థ ద ధ న ప ఫ(1) బ(1) భ(1) మ(1) య ర ల వ(1) శ(1) ష(1) స(1) హ");
        sLanguageIndexerMap.put("MALAYALAM_ALPHA_INDEX", "അ ആ ഇ(1) ഈ(1) ഉ(1) ഊ ഋ എ ഏ(1) ഐ(1) ഒ(1) ഓ ഔ ക ൿ(1) ഖ(1) ഗ(1) ഘ(1) ങ ച ഛ ജ(1) ഝ(1) ഞ(1) ട ഠ ഡ ഢ(1) ണ(1) ത(1) ഥ ദ ധ ന(1) പ(1) ഫ(1) ബ ഭ മ യ(1) ര(1) ല(1) വ ശ ഷ സ(1) ഹ(1) ള(1) ഴ റ");
        sLanguageIndexerMap.put("ODIA_ALPHA_INDEX", "ଅ ଆ ଇ(1) ଈ(1) ଉ(1) ଊ ଋ ଏ ଐ(1) ଓ(1) ଔ(1) କ ଖ ଗ ଘ(1) ଙ(1) ଚ(1) ଛ ଜ ଝ ଞ(1) ଟ(1) ଠ(1) ଡ ଢ ଣ ତ(1) ଥ(1) ଦ(1) ଧ ନ ପ ଫ(1) ବ(1) ଭ(1) ମ ଯ ର ଲ(1) ଶ(1) ଷ(1) ସ ହ");
        sLanguageIndexerMap.put("ASSAMESE_ALPHA_INDEX", "অ আ ই(1) ঈ(1) উ(1) ঊ ঋ এ ঐ(1) ও(1) ঔ(1) ক খ গ ঘ(1) ঙ(1) চ(1) ছ জ ঝ ঞ(1) ট(1) ঠ(1) ড ঢ ণ ত(1) থ(1) দ(1) ধ ন প ফ(1) ব(1) ভ(1) ম য ৰ ল(1) ৱ(1) শ(1) ষ স হ");
        sLanguageIndexerMap.put("MAITHILI_ALPHA_INDEX", "अ आ इ(1) ई(1) उ(1) ऊ ए ऐ ओ(1) औ(1) क(1) ख ग घ ङ(1) च(1) छ(1) ज झ ञ ट(1) ठ(1) ड(1) ढ ण त थ(1) द(1) ध(1) न प फ ब(1) भ(1) म(1) य र ल व(1) श(1) ष(1) स ह");
        sLanguageIndexerMap.put("MONGOLIAN_ALPHA_INDEX", "А Б В Г(1) Д(1) Е(1) Ж З И Й(1) К(1) Л(1) М(1) Н О Ө П(1) Р(1) С(1) Т У Ү Ф(1) Х(1) Ц(1) Ч Ш Щ Ъ(1) Ы(1) Ь(1) Э Ю Я");
        sLanguageIndexerMap.put("SWEDISH_ALPHA_INDEX", "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Å Ä Ö");
        sLanguageIndexerMap.put("TAIWAN_ALPHA_INDEX", "ㄅ ㄆ ㄇ ㄈ ㄉ ㄊ ㄋ ㄌ ㄍ ㄎ ㄏ ㄐ ㄑ ㄒ ㄓ ㄔ ㄕ ㄖ(1) ㄗ(1) ㄘ(1) ㄙ ㄚ(1) ㄛ(1) ㄜ(1) ㄝ(1) ㄞ ㄟ(1) ㄠ(1) ㄡ(1) ㄢ ㄣ(1) ㄤ(1) ㄥ(1) ㄦ(1) ㄧ ㄨ ㄩ");
        sLanguageIndexerMap.put("TAMIL_ALPHA_INDEX", "அ ஆ இ ஈ உ ஊ எ ஏ ஐ ஒ ஓ ஔ க ச ஞ த ந ப ம ய ர ல வ ஜ ஷ ஹ ஸ்ரீ");
        sLanguageIndexerMap.put("THAI_ALPHA_INDEX", "ก ข(1) ฃ(1) ค(1) ฅ ฆ(1) ง(1) จ(1) ฉ ช(1) ซ(1) ฌ(1) ญ ฎ(1) ฏ(1) ฐ(1) ฑ ฒ(1) ณ(1) ด(1) ต ถ(1) ท(1) ธ(1) น บ(1) ป(1) ผ ฝ(1) พ(1) ฟ ภ(1) ม(1) ย ร(1) ฤ(1) ล ฦ(1) ว(1) ศ ษ(1) ส(1) ห ฬ(1) อ(1) ฮ");
        sLanguageIndexerMap.put("TELUGU_ALPHA_INDEX", "అ ఆ ఇ ఈ ఉ(1) ఊ(1) ఎ(1) ఏ(1) ఐ ఒ ఓ ఔ(1) అం(1) క(1) ఖ(1) గ ఘ చ ఛ జ ఝ(1) ట(1) ఠ(1) డ(1) ఢ(1) ణ త థ ద ధ న ప ఫ(1) బ(1) భ(1) మ(1) య ర ల వ(1) శ(1) ష(1) స(1) హ");
        sLanguageIndexerMap.put("TIBETAN_ALPHA_INDEX", "ཀ ཁ ག ང ཅ ཆ ཇ ཉ ཏ ཐ ད ན པ ཕ བ མ ཙ ཚ ཛ ཝ ཞ ཟ འ ཡ ར ལ ཤ ས ཧ ཨ");
        sLanguageIndexerMap.put("TRADITIONAL_CHINESE_ALPHA_INDEX", "1劃 2劃 3劃 4劃 5劃 6劃 7劃 8劃 9劃 10劃 11劃 12劃 13劃 14劃 15劃 16劃 17劃 18劃 19劃 20劃 21劃 22劃 23劃 24劃 25劃 26劃");
        sLanguageIndexerMap.put("TURKISH_ALPHA_INDEX", "A B C Ç D E F G H I(1) İ(1) J K L M N O(1) Ö(1) P Q R S Ş T U(1) Ü(1) V W X Y Z");
        sLanguageIndexerMap.put("UKRAINIAN_ALPHA_INDEX", "А Б В Г(1) Ґ(1) Д Е(1) Є(1) Ж З И І(1) Ї(1) Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш(1) Щ(1) Ю Я");
        sLanguageIndexerMap.put("URDU_ALPHA_INDEX", "ا ب(1) پ(1) ت ٹ(1) ث(1) ج چ(1) ح(1) خ د(1) ڈ(1) ذ ر(1) ڑ(1) ز ژ(1) س(1) ش ص(1) ض(1) ط ظ(1) ع(1) غ ف(1) ق(1) ک گ(1) ل(1) م ن(1) و(1) ہ ھ ء ی ے");
        sLanguageIndexerMap.put("UYGHUR_ALPHA_INDEX", "ا ە ب پ ت ج(1) چ(1) خ د ر ز ژ س ش غ ف ق ك گ ڭ ل م ن ھ و ۇ(1) ۆ(1) ۈ(1) ۋ ې(1) ى(1) ي");
        sLanguageIndexerMap.put("UZBEK_ALPHA_INDEX", "A B D E F G H I J K L M N O P Q R S T U V X Y Z Oʻ Gʻ CH SH NG");
        sLanguageIndexerMap.put("VIETNAMESE_ALPHA_INDEX", "A Ă(1) Â(1) B C D Đ E(1) Ê(1) F G H I J K L M N O(1) Ô(1) Ơ(1) P Q R S T U(1) Ư(1) V W X Y Z");
        sLanguageIndexerMap.put("UYGUR_ALPHA_INDEX", "ا ە ب پ ت ج چ خ د ر ز ژ س ش غ ف ق ك گ ڭ ل م ن ھ و ۇ ۆ ۈ ۋ ې ى ي");
    }

    private HwAlphaIndexResourceManager(Locale locale) {
        String validLocale = locale.getLanguage();
        if ("zh".equals(validLocale) && "Hant".equals(locale.getScript())) {
            validLocale = "TW".equals(locale.getCountry()) ? "zh_TW" : "zh_Hant";
        }
        if ("fa".equals(validLocale) && "AF".equals(locale.getCountry())) {
            validLocale = "fa_AF";
        }
        try {
            sLanguageIndexerMap = parseLanguageIndexerFile(Resources.getSystem().getAssets().open(LANGUAGE_INDEXER_FILE));
        } catch (IOException e) {
            Log.e(TAG, "Can not find the LanguageIndexerFile.conf file");
        }
        if ("zh_Hant".equals(validLocale)) {
            this.mLabelList = getLabelList("TRADITIONAL_CHINESE_ALPHA_INDEX");
        } else if ("zh_TW".equals(validLocale)) {
            this.mLabelList = getLabelList("TAIWAN_ALPHA_INDEX");
        } else if ("fa_AF".equals(validLocale)) {
            this.mLabelList = getLabelList("PASHOTO_ALPHA_INDEX");
        } else if ("tl".equals(validLocale)) {
            this.mLabelList = getLabelList("FILIPINO_ALPHA_INDEX");
        } else if ("ug".equals(validLocale)) {
            this.mLabelList = getLabelList("UYGUR_ALPHA_INDEX");
        } else {
            String lang = locale.getDisplayLanguage(Locale.US).split(" ")[0].toUpperCase(Locale.US);
            this.mLabelList = getLabelList(lang + "_ALPHA_INDEX");
        }
        if (this.mLabelList.isEmpty()) {
            this.mLabelList = getLabelList("ROOT_ALPHA_INDEX");
        }
    }

    public static HwAlphaIndexResourceManager getInstance() {
        return getInstance(Locale.getDefault());
    }

    public static HwAlphaIndexResourceManager getInstance(Locale locale) {
        return new HwAlphaIndexResourceManager(locale);
    }

    private static List<String> getDisplayFromComplete(List<String> completeList) {
        List<String> bulletList = new ArrayList<>((int) INITIAL_CAPACITY);
        for (String item : completeList) {
            if (item.split(" ").length > 1) {
                bulletList.add(AlphaIndexerListView.BULLET_CHAR);
            } else {
                bulletList.add(item);
            }
        }
        return bulletList;
    }

    public static List<String> getRootPortraitDisplayAlphaIndex() {
        return new ArrayList(Arrays.asList(ROOT_ALPHA_INDEX.split(" ")));
    }

    public static List<String> getRootLandscapeDisplayAlphaIndex() {
        return getDisplayFromComplete(getRootLandscapeCompleteAlphaIndex());
    }

    public static List<String> getRootLandscapeCompleteAlphaIndex() {
        return populateBulletAlphaIndex(18, new ArrayList(Arrays.asList(ROOT_ALPHA_INDEX.split(" "))));
    }

    public static List<String> populateBulletAlphaIndex(int maxSize, List<String> alphaList) {
        if (alphaList == null || alphaList.size() < maxSize || alphaList.size() <= 0) {
            return alphaList;
        }
        int size = alphaList.size();
        int min = (maxSize - 1) / 2;
        int minBulletCount = 1;
        while ((minBulletCount + 1) * min < size - 1) {
            minBulletCount++;
        }
        int bulletCount = minBulletCount - 1;
        if ((bulletCount + 1) * min == size - 1) {
            bulletCount++;
        }
        int bulletSize = (size - 1) - ((bulletCount + 1) * min);
        int minBulletSize = min - bulletSize;
        List<String> bulletAlphaIndexList = new ArrayList<>((int) INITIAL_CAPACITY);
        bulletAlphaIndexList.add(alphaList.get(0));
        int pos = 0 + 1;
        if (pos >= size) {
            Log.w(TAG, "populateBulletAlphaIndex: pos(1) is invalid");
            return bulletAlphaIndexList;
        }
        while (bulletSize > 0) {
            bulletSize--;
            StringBuilder bulletContent = new StringBuilder((int) INITIAL_CAPACITY);
            int i = 0;
            while (i < bulletCount + 1 && pos < size) {
                bulletContent.append(alphaList.get(pos));
                bulletContent.append(' ');
                i++;
                pos++;
            }
            if (bulletContent.length() > 0) {
                bulletContent.setLength(bulletContent.length() - 1);
                bulletAlphaIndexList.add(bulletContent.toString());
            }
            if (pos >= size) {
                Log.w(TAG, "populateBulletAlphaIndex: pos(2) is invalid");
                return bulletAlphaIndexList;
            }
            bulletAlphaIndexList.add(alphaList.get(pos));
            pos++;
        }
        if (pos >= size) {
            Log.w(TAG, "populateBulletAlphaIndex: pos(3) is invalid");
            return bulletAlphaIndexList;
        }
        while (minBulletSize > 0) {
            minBulletSize--;
            StringBuilder bulletContent2 = new StringBuilder((int) INITIAL_CAPACITY);
            int i2 = 0;
            while (i2 < bulletCount && pos < size) {
                bulletContent2.append(alphaList.get(pos));
                bulletContent2.append(' ');
                i2++;
                pos++;
            }
            if (bulletContent2.length() > 0) {
                bulletContent2.setLength(bulletContent2.length() - 1);
                bulletAlphaIndexList.add(bulletContent2.toString());
            }
            if (pos >= size) {
                Log.w(TAG, "populateBulletAlphaIndex: pos(4) is invalid");
                return bulletAlphaIndexList;
            }
            bulletAlphaIndexList.add(alphaList.get(pos));
            pos++;
        }
        for (int i3 = pos; i3 < size; i3++) {
            bulletAlphaIndexList.add(alphaList.get(i3));
        }
        return bulletAlphaIndexList;
    }

    private List<Alpha> getLabelList(String alphaIndex) {
        String labelValue = sLanguageIndexerMap.get(alphaIndex);
        if (labelValue == null) {
            return new ArrayList(0);
        }
        String[] alphaArray = labelValue.split(" ");
        List<Alpha> labelList = new ArrayList<>((int) INITIAL_CAPACITY);
        for (String item : alphaArray) {
            if (!TextUtils.isEmpty(item.trim())) {
                String tempItem = item.trim();
                Alpha alpha = new Alpha();
                int pos = tempItem.indexOf("(");
                if (pos != -1) {
                    alpha.content = tempItem.substring(0, pos);
                    alpha.desc = Character.digit(tempItem.charAt(pos + 1), (int) DECIMAL_RADIX);
                } else {
                    alpha.content = tempItem;
                    alpha.desc = 0;
                }
                labelList.add(alpha);
            }
        }
        return labelList;
    }

    private Map<String, String> parseLanguageIndexerFile(InputStream file) {
        InputStreamReader reader = null;
        BufferedReader buffReader = null;
        Map<String, String> languageMapIndexer = new HashMap<>((int) INITIAL_CAPACITY);
        languageMapIndexer.put("ROOT_ALPHA_INDEX", ROOT_ALPHA_INDEX);
        try {
            InputStreamReader reader2 = new InputStreamReader(file, "utf-8");
            BufferedReader buffReader2 = new BufferedReader(reader2, BUFFER_SIZE);
            int lineNum = 0;
            while (true) {
                String line = buffReader2.readLine();
                if (line != null) {
                    lineNum++;
                    if (lineNum > LANGUAGE_FILE_MAX_LINE) {
                        Log.e(TAG, "beyond max line number");
                        break;
                    } else if (!line.startsWith(AlphaIndexerListView.DIGIT_LABEL)) {
                        String[] entries = line.trim().split(":");
                        if (entries.length != 2) {
                            Log.e(TAG, "Invalid line: " + lineNum);
                        } else {
                            languageMapIndexer.put(entries[0], entries[1]);
                        }
                    }
                }
            }
            try {
                reader2.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception when trying to close InputStreamReader.");
            }
            try {
                buffReader2.close();
                break;
            } catch (IOException e2) {
                Log.e(TAG, "Exception when trying to close BufferedReader.");
            }
        } catch (IOException e3) {
            Log.e(TAG, "Exception when parsing LANGUAGE_INDEXER_FILE.");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Exception when trying to close InputStreamReader.");
                }
            }
            if (0 != 0) {
                buffReader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    Log.e(TAG, "Exception when trying to close InputStreamReader.");
                }
            }
            if (0 != 0) {
                try {
                    buffReader.close();
                } catch (IOException e6) {
                    Log.e(TAG, "Exception when trying to close BufferedReader.");
                }
            }
            throw th;
        }
        return languageMapIndexer;
    }

    public String[] getAlphaIndex() {
        return new String[0];
    }

    public List<String> getPortraitDisplayAlphaIndex() {
        return getDisplayFromComplete(getPortraitCompleteAlphaIndex());
    }

    public List<String> getPortraitCompleteAlphaIndex() {
        return getAlphaIndexWithContract();
    }

    public List<String> getLandscapeDisplayAlphaIndex() {
        return getDisplayFromComplete(getLandscapeCompleteAlphaIndex());
    }

    public List<String> getLandscapeCompleteAlphaIndex() {
        return populateBulletAlphaIndex(18, getAlphaListContent(this.mLabelList));
    }

    private List<String> getAlphaIndexWithContract() {
        List<String> contentList = new ArrayList<>((int) INITIAL_CAPACITY);
        StringBuilder bulletContent = new StringBuilder((int) INITIAL_CAPACITY);
        for (Alpha alpha : this.mLabelList) {
            if (alpha.desc == 0) {
                if (bulletContent.length() > 0) {
                    bulletContent.setLength(bulletContent.length() - 1);
                    contentList.add(bulletContent.toString());
                    bulletContent.setLength(0);
                }
                contentList.add(alpha.content);
            } else if (alpha.desc == 1) {
                bulletContent.append(alpha.content);
                bulletContent.append(' ');
            }
        }
        return contentList;
    }

    public String[] getAlphaIndexWithoutDeletable() {
        return new String[0];
    }

    public String[] getAlphaIndexFewest() {
        return new String[0];
    }

    private List<String> getAlphaListContent(List<Alpha> list) {
        List<String> contentList = new ArrayList<>((int) INITIAL_CAPACITY);
        for (Alpha alpha : list) {
            contentList.add(alpha.content);
        }
        return contentList;
    }

    public static class Alpha {
        public static final int COLLAPSIBLE = 1;
        public static final int DELETABLE = 2;
        public static final int NORMAL = 0;
        public String content;
        public int desc;

        public Alpha(String content2, int desc2) {
            this.content = content2;
            this.desc = desc2;
        }

        public Alpha() {
        }
    }
}
