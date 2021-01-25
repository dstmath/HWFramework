package com.huawei.i18n.tmr.address.en;

import java.util.regex.Pattern;

public class RegularExpression {
    private static final String BIG_LETT = "[A-Z]";
    private static final String BLANK = "(?:\\s*,\\s*|\\s+)";
    private static final String BLANK_2 = "(\\s*[,:]\\s*|\\s*)";
    private static final String BOUND_L = "(?<![a-zA-Z])";
    private static final String BOUND_R = "(?![a-zA-Z])";
    private static final String CITY = "(((?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}?[A-Z][A-Za-z]*(?:'s|'S|')?(?:\\s+(?:(?i)town|city|county|uptown|downtown))?(?:(?:\\s*,\\s*|\\s+)(?:(?:(?i)in|of)\\s+)?(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?))|(?:(?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}[A-Z][A-Za-z]*(?:'s|'S|')?))(?![a-zA-Z])";
    private static final String CITY_PRE = "(?:(?i)uptown|downtown)";
    private static final String CITY_SUF = "(?:(?i)town|city|county|uptown|downtown)";
    private static final String CODE = "(?:(?:(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d))|(?:\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b))";
    private static final String CODE_A = "(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d)";
    private static final String CODE_B = "\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b";
    private static final String DIRE = "(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?";
    private static final String LETTER = "[A-Za-z]";
    private static final String LOCATION = "(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE))";
    private static final String NOT = "(?i)(?:my|your|his|her|its|their|our|this|that|the|a|an|what|which|whose)";
    private static final String NUMBER = "[0-9]";
    private static final String NUM_BIG = "[0-9A-Z]";
    private static final String NUM_LETT = "[0-9A-Za-z]";
    private static final String POST_BOX = "(?<![a-zA-Z])(?i)(?:(?:PO\\s*BOX\\s*\\d+(?:\\s+PMB\\s*\\d+)?)|(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+\\s+PMB\\s*\\d+|\\d+\\s+PMB\\s*\\d+(?:(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+)?)";
    private static final String PRE_ALL = "(?:(?i)in|on|at|of|from|to|for|near)";
    private static final String PRE_BUILDING = "(?:(?i)in|at|to|from|near|reach)";
    private static final String PRE_CITY = "(?:(?i)in|at|of|from)";
    private static final String PRE_CITY_IN = "(?:(?i)in|at|on|of)";
    private static final String PRE_ROAD = "(?:(?i)in|at|on)";
    private static final String PRE_STATE = "(?:(?i)in|of)";
    private static final String PRE_S_2 = "(\\W+(?:(?i)(?:in|at|of|from)\\s+(?:the\\s+)?)?|\\W+)";
    private static final String PRE_S_52 = "(\\W+(?:(?:(?i)in|at|on)\\s+(?:the\\s+)?)?|\\W+)";
    private static final String PUNC = "(?:(?:[-&,.])|\\(|\\)|\"|/|\\s)";
    private static final String ROAD = "(?:(?<![a-zA-Z0-9])(?:(?:(?:(?:#)?\\d+(?:-\\d+)?)(?:\\s*,\\s*|\\s+)(?:(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?\\s+)?)?(?:(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)(?:\\s+(?:(?i)(?:and|&)\\s+)?))*?(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)\\s+(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?))";
    private static final String ROAD_SUB = "(?:(?<![a-zA-Z])(?:(?:(?i)in|at|on)(?:\\s+[0-9]{3,5})?|(?:(?:(?i)in|at|on)\\s+)?[0-9]{3,5})\\s+(?:[0-9A-Za-z]+(?:'s|'S|')?\\s+){1,3}(?:(?i)street|boulevard|avenue|lane|ave|av|blvd|ln|rd|st|crescent)(?:\\.?)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?)";
    private static final String ROAD_SUF = "(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)";
    private static final String ROAD_SUF_2 = "(?:boulevard|avenue|street|freeway|road|circle|way|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)(?:\\.|\\b))";
    private static final String ROAD_SUF_SUB = "(?:(?i)street|boulevard|avenue|lane|ave|av|blvd|ln|rd|st|crescent)(?:\\.?)";
    private static final String ROAD_SUITE = "(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w)";
    private static final String STATE = "(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?";
    private static final String SUF_S = "(?:'s|'S|')";
    private static final String S_52 = "((?:(?:(?<![a-zA-Z0-9])(?:(?:(?:(?:#)?\\d+(?:-\\d+)?)(?:\\s*,\\s*|\\s+)(?:(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?\\s+)?)?(?:(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)(?:\\s+(?:(?i)(?:and|&)\\s+)?))*?(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)\\s+(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?))(?:\\s*,\\s*|\\s+)(?:(?i)(?:and|&)\\s+)?)*(?:(?<![a-zA-Z0-9])(?:(?:(?:(?:#)?\\d+(?:-\\d+)?)(?:\\s*,\\s*|\\s+)(?:(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?\\s+)?)?(?:(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)(?:\\s+(?:(?i)(?:and|&)\\s+)?))*?(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)\\s+(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?)))(((?:\\s*,\\s*|\\s+)(?:(?:(?i)in|at|of|from)\\s+)?)((?<![a-zA-Z])(?i)(?:(?:PO\\s*BOX\\s*\\d+(?:\\s+PMB\\s*\\d+)?)|(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+\\s+PMB\\s*\\d+|\\d+\\s+PMB\\s*\\d+(?:(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+)?)(?:\\s*,\\s*|\\s+))?(((?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}?[A-Z][A-Za-z]*(?:'s|'S|')?(?:\\s+(?:(?i)town|city|county|uptown|downtown))?(?:(?:\\s*,\\s*|\\s+)(?:(?:(?i)in|of)\\s+)?(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?))|(?:(?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}[A-Z][A-Za-z]*(?:'s|'S|')?))(?![a-zA-Z])((?:\\s*,\\s*|\\s+)(?:(?:(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d))|(?:\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b)))?)?";
    private static final String S_52_SUB = "((?:(?<![a-zA-Z])(?:(?:(?i)in|at|on)(?:\\s+[0-9]{3,5})?|(?:(?:(?i)in|at|on)\\s+)?[0-9]{3,5})\\s+(?:[0-9A-Za-z]+(?:'s|'S|')?\\s+){1,3}(?:(?i)street|boulevard|avenue|lane|ave|av|blvd|ln|rd|st|crescent)(?:\\.?)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?))(((?:\\s*,\\s*|\\s+)(?:(?:(?i)in|at|of|from)\\s+)?)((?<![a-zA-Z])(?i)(?:(?:PO\\s*BOX\\s*\\d+(?:\\s+PMB\\s*\\d+)?)|(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+\\s+PMB\\s*\\d+|\\d+\\s+PMB\\s*\\d+(?:(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+)?)(?:\\s*,\\s*|\\s+))?(((?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}?[A-Z][A-Za-z]*(?:'s|'S|')?(?:\\s+(?:(?i)town|city|county|uptown|downtown))?(?:(?:\\s*,\\s*|\\s+)(?:(?:(?i)in|of)\\s+)?(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?))|(?:(?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}[A-Z][A-Za-z]*(?:'s|'S|')?))(?![a-zA-Z])((?:\\s*,\\s*|\\s+)(?:(?:(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d))|(?:\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b)))?)?";
    private static final String WORD_1 = "[A-Z][A-Za-z]*(?:'s|'S|')?";
    private static final String WORD_2 = "[A-Z][0-9A-Za-z]*(?:'s|'S|')?";
    private static final String WORD_23 = "(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)";
    private static final String WORD_3 = "[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?";
    private static final String WORD_4 = "[0-9A-Z][0-9A-Za-z]*(?:'s|'S|')?";
    private static final String WORD_5 = "[0-9A-Za-z]+(?:'s|'S|')?";

    /* access modifiers changed from: package-private */
    public Pattern getPat52() {
        return Pattern.compile(S_52);
    }

    /* access modifiers changed from: package-private */
    public Pattern getPat52Sub() {
        return Pattern.compile(S_52_SUB);
    }

    /* access modifiers changed from: package-private */
    public Pattern getPat28() {
        return Pattern.compile("(((?<![a-zA-Z])(?i)(?:(?:PO\\s*BOX\\s*\\d+(?:\\s+PMB\\s*\\d+)?)|(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+\\s+PMB\\s*\\d+|\\d+\\s+PMB\\s*\\d+(?:(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+)?)(?:\\s*,\\s*|\\s+))?(((?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}?[A-Z][A-Za-z]*(?:'s|'S|')?(?:\\s+(?:(?i)town|city|county|uptown|downtown))?(?:(?:\\s*,\\s*|\\s+)(?:(?:(?i)in|of)\\s+)?(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?))|(?:(?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}[A-Z][A-Za-z]*(?:'s|'S|')?))(?![a-zA-Z]))?(\\s*[,:]\\s*|\\s*)((?:(?:(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d))|(?:\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b)))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPat1346() {
        return Pattern.compile("(?:(?<![a-zA-Z])(?:(?i)in|at|to|from|near|reach)\\s+(?:(?i)the\\s+)?)?(?<![a-zA-Z])(?:(?:(?:[0-9A-Z][0-9A-Za-z]*(?:'s|'S|')?|south|east|north|west)(?![a-zA-Z])\\s*)|(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE))(?![a-zA-Z])\\s*)(?:((?:(?<![a-zA-Z])[0-9A-Z][0-9A-Za-z]*(?:'s|'S|')?(?![a-zA-Z]))*)(?:(?:(?<![a-zA-Z])(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE))(?![a-zA-Z])|(?<![a-zA-Z])(?:(?i)in|on|at|of|from|to|for|near)(?![a-zA-Z])|(?<![a-zA-Z])(?:(?i)town|city|county|uptown|downtown)(?![a-zA-Z])|(?:(?:[-&,.])|\\(|\\)|\"|/|\\s)|(?<![a-zA-Z])(?:(?i)and)(?![a-zA-Z])|(?<![a-zA-Z])(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?(?![a-zA-Z])|(?<![a-zA-Z])(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)(?![a-zA-Z])|(?<![a-zA-Z'])(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z])|(?<![a-zA-Z])(?:(?i)the)(?![a-zA-Z]))*))*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPat52s() {
        return Pattern.compile("(\\W+(?:(?:(?i)in|at|on)\\s+(?:the\\s+)?)?|\\W+)((?:(?:(?<![a-zA-Z0-9])(?:(?:(?:(?:#)?\\d+(?:-\\d+)?)(?:\\s*,\\s*|\\s+)(?:(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?\\s+)?)?(?:(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)(?:\\s+(?:(?i)(?:and|&)\\s+)?))*?(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)\\s+(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?))(?:\\s*,\\s*|\\s+)(?:(?i)(?:and|&)\\s+)?)*(?:(?<![a-zA-Z0-9])(?:(?:(?:(?:#)?\\d+(?:-\\d+)?)(?:\\s*,\\s*|\\s+)(?:(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?\\s+)?)?(?:(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)(?:\\s+(?:(?i)(?:and|&)\\s+)?))*?(?:[A-Z][0-9A-Za-z]*(?:'s|'S|')?|[0-9][0-9A-Za-z]*[A-Za-z](?:'s|'S|')?)\\s+(?:(?:(?i)boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)\\.?)|Way|WAY)(?![a-zA-Z])(?:\\s+(?:(?i)[nesw]|north|west|east|south|northeast|northwest|southeast|southwest|ne|nw|se|sw)\\.?(?![a-zA-Z]))?(?:(?:\\s*,\\s*|\\s+)(?:\\d+th\\s+)?(?<![a-zA-Z])suite(?:(?:\\s*\\d+(?:\\s*-?\\s*[A-Za-z])?)|(?:\\s+[A-Za-z])|(?:(?i)\\s+level(?:\\s+[A-Za-z]?)))?(?!\\w))?)))(((?:\\s*,\\s*|\\s+)(?:(?:(?i)in|at|of|from)\\s+)?)((?<![a-zA-Z])(?i)(?:(?:PO\\s*BOX\\s*\\d+(?:\\s+PMB\\s*\\d+)?)|(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+\\s+PMB\\s*\\d+|\\d+\\s+PMB\\s*\\d+(?:(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+)?)(?:\\s*,\\s*|\\s+))?(((?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}?[A-Z][A-Za-z]*(?:'s|'S|')?(?:\\s+(?:(?i)town|city|county|uptown|downtown))?(?:(?:\\s*,\\s*|\\s+)(?:(?:(?i)in|of)\\s+)?(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?))|(?:(?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}[A-Z][A-Za-z]*(?:'s|'S|')?))(?![a-zA-Z])((?:\\s*,\\s*|\\s+)(?:(?:(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d))|(?:\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b)))?)?");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPat2s() {
        return Pattern.compile("(\\W+(?:(?i)(?:in|at|of|from)\\s+(?:the\\s+)?)?|\\W+)((?<![a-zA-Z])(?i)(?:(?:PO\\s*BOX\\s*\\d+(?:\\s+PMB\\s*\\d+)?)|(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+\\s+PMB\\s*\\d+|\\d+\\s+PMB\\s*\\d+(?:(?:(?:Rural\\s+Route)|(?:RR))\\s*\\d+\\s+BOX\\s*\\d+)?)(?:\\s*,\\s*|\\s+))?(((?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}?[A-Z][A-Za-z]*(?:'s|'S|')?(?:\\s+(?:(?i)town|city|county|uptown|downtown))?(?:(?:\\s*,\\s*|\\s+)(?:(?:(?i)in|of)\\s+)?(?:(?:(?i)(?:Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Lississippi|Missouri|Montana|Nebraska|Nevada|New Jersey|New Mexico|New York|North Carolian|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|RL|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming|AL|WY|AK|AZ|AR|CA|Co|CT|DE|FL|GA|ID|IL|IA|KS|KY|LA|MD|MA|MI|MN|MS|MO|MT|NE|NV|NJ|NM|NY|NC|ND|PA|SC|SD|TN|TX|UT|VT|VA|WA|WV|WI))|(?:HI|OR|ME|OK|OH))\\.?(?:(?i)\\s+state)?))|(?:(?:(?:(?i)uptown|downtown)\\s+)?(?:[A-Z][A-Za-z]*(?:'s|'S|')?\\s+(?:(?:(?i)in|at|on|of)\\s+)?){0,3}[A-Z][A-Za-z]*(?:'s|'S|')?))(?![a-zA-Z])(?:(\\s*[,:]\\s*|\\s*)((?:(?:(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d))|(?:\\b(?:(?:[A-Z]{1,2}[0-9]{1,2} [0-9][A-Z]{2})|(?:[A-Z]{1,2}[0-9][A-Z] [0-9][A-Z]{2}))\\b))))?");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatRoad() {
        return Pattern.compile("(?i)(?:\\s*(?:(in|on|at)\\s+)?(?:the\\s+)?(boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Way|Fwy|Crescent|Highway))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatCodeA() {
        return Pattern.compile(CODE_A);
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatResultClean() {
        return Pattern.compile("(?:(?:[^0-9a-zA-Z]*)(?i)(?:(?:in|at|on|from|to|of|and)\\s+)?(?:(?:the)\\s+)?)(?:([\\s\\S]*)?,|([\\s\\S]*))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatBox() {
        return Pattern.compile(POST_BOX);
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatDir() {
        return Pattern.compile("\\s*(south|north|west|east)\\s*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatNot1() {
        return Pattern.compile("([\\s\\S]*?)(?<![a-zA-Z])(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE))(?![a-zA-Z])");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatNot2() {
        return Pattern.compile("[\\s\\S]*(?<![a-zA-Z])(?i)(?:my|your|his|her|its|their|our|this|that|the|a|an|what|which|whose)\\s+");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatNum() {
        return Pattern.compile("(?:(?:\\s*[:,\\.\"-]\\s*|\\s*)\\d+(?:\\s*[,\\.\":-]\\s*|\\s+))+");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatLocation() {
        return Pattern.compile("(?:([\\s\\S]*?)(?<![a-zA-Z])((?:(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE)))((?:\\s+|\\s*&\\s*)(?:(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE))))?)(?![a-zA-Z]))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatComma() {
        return Pattern.compile("(?:(?:[\\s\\S]*)(?:,|\\.)([\\s\\S]*))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatPreCity() {
        return Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatPreUni() {
        return Pattern.compile("(?:\\b(?i)(in|at|from|near|to|of|for)\\b([\\s\\S]*))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatNo() {
        return Pattern.compile("(?:[\\s\\S]*(?<![a-zA-Z])(?i)(the|in|on|at|from|to|of|for)(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatCut() {
        return Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatSingle() {
        return Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatBuilding() {
        return Pattern.compile("(?:[^0-9a-zA-Z]*|\\s*(?:(?i)the|this|a|that)\\s*)(?:(?:(?:(?i)(?:park|center|hotel|bar|hospital|theater|theatre|building|lounge|store|market|apartment|restaurant|museum|university|college|school|tower|guesthouse|mansion|motel|club|cafe|airport|stadium|station|bridge|bodega|tavern|boutique|zoo|mall|bkstore|inn|hostel|resort|institute|library|kingdergarten|cafeteria|bistro|canteen|hall|castle|garden|square|plaza|gallery|pier|wharf|shop|outlet|supermarket|district|clinic|cinema|gym|gymnasium|bowl|bus\\s+station|train\\s+station))|(?:House|HOUSE)))[^0-9a-zA-Z]*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatPreRoad() {
        return Pattern.compile("(?i)(?<![a-z])(?:(?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+)((?:[\\s\\S]+?)(?:(?<![a-z])((?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+))?(?:boulevard|avenue|street|freeway|road|circle|way|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)(?:\\.|\\b))(?![a-zA-Z])[\\s\\S]*)");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatNotRoad() {
        return Pattern.compile("(?i)((?<![a-zA-Z])(?:a|what|which|whose|i|you|this|that|my|his|her|out|their|its)\\s+)([\\s\\S]+)?(?:boulevard|avenue|street|freeway|road|circle|way|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)(?:\\.|\\b))(?![a-zA-Z])");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatPreBuilding() {
        return Pattern.compile("[\\s\\S]*(?<![a-zA-Z])((?i)(in|at|from|near|to|reach))\\b(\\s+(?i)the\\b)?(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+)?");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatPreCity2() {
        return Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatCity() {
        return Pattern.compile("([\\s\\S]*(?i)(town|city|county)\\b)(?:.*)");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatCity2() {
        return Pattern.compile("(\\s+(?i)(town|city|county))\\b.*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatClean() {
        return Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatCut2() {
        return Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(?:uptown|downtown)\\s+)?)?[\\s\\S]*");
    }

    /* access modifiers changed from: package-private */
    public Pattern getPatBig() {
        return Pattern.compile("[A-Z0-9]");
    }
}
