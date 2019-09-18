package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

class LongArray implements Cloneable {
    private static final short[] INTERLEAVE2_TABLE = {0, 1, 4, 5, 16, 17, 20, 21, 64, 65, 68, 69, 80, 81, 84, 85, 256, 257, 260, 261, 272, 273, 276, 277, 320, 321, 324, 325, 336, 337, 340, 341, 1024, 1025, 1028, 1029, 1040, 1041, 1044, 1045, 1088, 1089, 1092, 1093, 1104, 1105, 1108, 1109, 1280, 1281, 1284, 1285, 1296, 1297, 1300, 1301, 1344, 1345, 1348, 1349, 1360, 1361, 1364, 1365, 4096, 4097, 4100, 4101, 4112, 4113, 4116, 4117, 4160, 4161, 4164, 4165, 4176, 4177, 4180, 4181, 4352, 4353, 4356, 4357, 4368, 4369, 4372, 4373, 4416, 4417, 4420, 4421, 4432, 4433, 4436, 4437, 5120, 5121, 5124, 5125, 5136, 5137, 5140, 5141, 5184, 5185, 5188, 5189, 5200, 5201, 5204, 5205, 5376, 5377, 5380, 5381, 5392, 5393, 5396, 5397, 5440, 5441, 5444, 5445, 5456, 5457, 5460, 5461, 16384, 16385, 16388, 16389, 16400, 16401, 16404, 16405, 16448, 16449, 16452, 16453, 16464, 16465, 16468, 16469, 16640, 16641, 16644, 16645, 16656, 16657, 16660, 16661, 16704, 16705, 16708, 16709, 16720, 16721, 16724, 16725, 17408, 17409, 17412, 17413, 17424, 17425, 17428, 17429, 17472, 17473, 17476, 17477, 17488, 17489, 17492, 17493, 17664, 17665, 17668, 17669, 17680, 17681, 17684, 17685, 17728, 17729, 17732, 17733, 17744, 17745, 17748, 17749, 20480, 20481, 20484, 20485, 20496, 20497, 20500, 20501, 20544, 20545, 20548, 20549, 20560, 20561, 20564, 20565, 20736, 20737, 20740, 20741, 20752, 20753, 20756, 20757, 20800, 20801, 20804, 20805, 20816, 20817, 20820, 20821, 21504, 21505, 21508, 21509, 21520, 21521, 21524, 21525, 21568, 21569, 21572, 21573, 21584, 21585, 21588, 21589, 21760, 21761, 21764, 21765, 21776, 21777, 21780, 21781, 21824, 21825, 21828, 21829, 21840, 21841, 21844, 21845};
    private static final int[] INTERLEAVE3_TABLE = {0, 1, 8, 9, 64, 65, 72, 73, 512, 513, 520, 521, 576, 577, 584, 585, 4096, 4097, 4104, 4105, 4160, 4161, 4168, 4169, 4608, 4609, 4616, 4617, 4672, 4673, 4680, 4681, 32768, 32769, 32776, 32777, 32832, 32833, 32840, 32841, 33280, 33281, 33288, 33289, 33344, 33345, 33352, 33353, 36864, 36865, 36872, 36873, 36928, 36929, 36936, 36937, 37376, 37377, 37384, 37385, 37440, 37441, 37448, 37449, 262144, 262145, 262152, 262153, 262208, 262209, 262216, 262217, 262656, 262657, 262664, 262665, 262720, 262721, 262728, 262729, 266240, 266241, 266248, 266249, 266304, 266305, 266312, 266313, 266752, 266753, 266760, 266761, 266816, 266817, 266824, 266825, 294912, 294913, 294920, 294921, 294976, 294977, 294984, 294985, 295424, 295425, 295432, 295433, 295488, 295489, 295496, 295497, 299008, 299009, 299016, 299017, 299072, 299073, 299080, 299081, 299520, 299521, 299528, 299529, 299584, 299585, 299592, 299593};
    private static final int[] INTERLEAVE4_TABLE = {0, 1, 16, 17, 256, 257, 272, 273, 4096, 4097, 4112, 4113, 4352, 4353, 4368, 4369, 65536, 65537, 65552, 65553, 65792, 65793, 65808, 65809, 69632, 69633, 69648, 69649, 69888, 69889, 69904, 69905, 1048576, 1048577, 1048592, 1048593, 1048832, 1048833, 1048848, 1048849, 1052672, 1052673, 1052688, 1052689, 1052928, 1052929, 1052944, 1052945, 1114112, 1114113, 1114128, 1114129, 1114368, 1114369, 1114384, 1114385, 1118208, 1118209, 1118224, 1118225, 1118464, 1118465, 1118480, 1118481, 16777216, 16777217, 16777232, 16777233, 16777472, 16777473, 16777488, 16777489, 16781312, 16781313, 16781328, 16781329, 16781568, 16781569, 16781584, 16781585, 16842752, 16842753, 16842768, 16842769, 16843008, 16843009, 16843024, 16843025, 16846848, 16846849, 16846864, 16846865, 16847104, 16847105, 16847120, 16847121, 17825792, 17825793, 17825808, 17825809, 17826048, 17826049, 17826064, 17826065, 17829888, 17829889, 17829904, 17829905, 17830144, 17830145, 17830160, 17830161, 17891328, 17891329, 17891344, 17891345, 17891584, 17891585, 17891600, 17891601, 17895424, 17895425, 17895440, 17895441, 17895680, 17895681, 17895696, 17895697, 268435456, 268435457, 268435472, 268435473, 268435712, 268435713, 268435728, 268435729, 268439552, 268439553, 268439568, 268439569, 268439808, 268439809, 268439824, 268439825, 268500992, 268500993, 268501008, 268501009, 268501248, 268501249, 268501264, 268501265, 268505088, 268505089, 268505104, 268505105, 268505344, 268505345, 268505360, 268505361, 269484032, 269484033, 269484048, 269484049, 269484288, 269484289, 269484304, 269484305, 269488128, 269488129, 269488144, 269488145, 269488384, 269488385, 269488400, 269488401, 269549568, 269549569, 269549584, 269549585, 269549824, 269549825, 269549840, 269549841, 269553664, 269553665, 269553680, 269553681, 269553920, 269553921, 269553936, 269553937, 285212672, 285212673, 285212688, 285212689, 285212928, 285212929, 285212944, 285212945, 285216768, 285216769, 285216784, 285216785, 285217024, 285217025, 285217040, 285217041, 285278208, 285278209, 285278224, 285278225, 285278464, 285278465, 285278480, 285278481, 285282304, 285282305, 285282320, 285282321, 285282560, 285282561, 285282576, 285282577, 286261248, 286261249, 286261264, 286261265, 286261504, 286261505, 286261520, 286261521, 286265344, 286265345, 286265360, 286265361, 286265600, 286265601, 286265616, 286265617, 286326784, 286326785, 286326800, 286326801, 286327040, 286327041, 286327056, 286327057, 286330880, 286330881, 286330896, 286330897, 286331136, 286331137, 286331152, 286331153};
    private static final int[] INTERLEAVE5_TABLE = {0, 1, 32, 33, 1024, 1025, 1056, 1057, 32768, 32769, 32800, 32801, 33792, 33793, 33824, 33825, 1048576, 1048577, 1048608, 1048609, 1049600, 1049601, 1049632, 1049633, 1081344, 1081345, 1081376, 1081377, 1082368, 1082369, 1082400, 1082401, 33554432, 33554433, 33554464, 33554465, 33555456, 33555457, 33555488, 33555489, 33587200, 33587201, 33587232, 33587233, 33588224, 33588225, 33588256, 33588257, 34603008, 34603009, 34603040, 34603041, 34604032, 34604033, 34604064, 34604065, 34635776, 34635777, 34635808, 34635809, 34636800, 34636801, 34636832, 34636833, 1073741824, 1073741825, 1073741856, 1073741857, 1073742848, 1073742849, 1073742880, 1073742881, 1073774592, 1073774593, 1073774624, 1073774625, 1073775616, 1073775617, 1073775648, 1073775649, 1074790400, 1074790401, 1074790432, 1074790433, 1074791424, 1074791425, 1074791456, 1074791457, 1074823168, 1074823169, 1074823200, 1074823201, 1074824192, 1074824193, 1074824224, 1074824225, 1107296256, 1107296257, 1107296288, 1107296289, 1107297280, 1107297281, 1107297312, 1107297313, 1107329024, 1107329025, 1107329056, 1107329057, 1107330048, 1107330049, 1107330080, 1107330081, 1108344832, 1108344833, 1108344864, 1108344865, 1108345856, 1108345857, 1108345888, 1108345889, 1108377600, 1108377601, 1108377632, 1108377633, 1108378624, 1108378625, 1108378656, 1108378657};
    private static final long[] INTERLEAVE7_TABLE = {0, 1, 128, 129, 16384, 16385, 16512, 16513, 2097152, 2097153, 2097280, 2097281, 2113536, 2113537, 2113664, 2113665, 268435456, 268435457, 268435584, 268435585, 268451840, 268451841, 268451968, 268451969, 270532608, 270532609, 270532736, 270532737, 270548992, 270548993, 270549120, 270549121, 34359738368L, 34359738369L, 34359738496L, 34359738497L, 34359754752L, 34359754753L, 34359754880L, 34359754881L, 34361835520L, 34361835521L, 34361835648L, 34361835649L, 34361851904L, 34361851905L, 34361852032L, 34361852033L, 34628173824L, 34628173825L, 34628173952L, 34628173953L, 34628190208L, 34628190209L, 34628190336L, 34628190337L, 34630270976L, 34630270977L, 34630271104L, 34630271105L, 34630287360L, 34630287361L, 34630287488L, 34630287489L, 4398046511104L, 4398046511105L, 4398046511232L, 4398046511233L, 4398046527488L, 4398046527489L, 4398046527616L, 4398046527617L, 4398048608256L, 4398048608257L, 4398048608384L, 4398048608385L, 4398048624640L, 4398048624641L, 4398048624768L, 4398048624769L, 4398314946560L, 4398314946561L, 4398314946688L, 4398314946689L, 4398314962944L, 4398314962945L, 4398314963072L, 4398314963073L, 4398317043712L, 4398317043713L, 4398317043840L, 4398317043841L, 4398317060096L, 4398317060097L, 4398317060224L, 4398317060225L, 4432406249472L, 4432406249473L, 4432406249600L, 4432406249601L, 4432406265856L, 4432406265857L, 4432406265984L, 4432406265985L, 4432408346624L, 4432408346625L, 4432408346752L, 4432408346753L, 4432408363008L, 4432408363009L, 4432408363136L, 4432408363137L, 4432674684928L, 4432674684929L, 4432674685056L, 4432674685057L, 4432674701312L, 4432674701313L, 4432674701440L, 4432674701441L, 4432676782080L, 4432676782081L, 4432676782208L, 4432676782209L, 4432676798464L, 4432676798465L, 4432676798592L, 4432676798593L, 562949953421312L, 562949953421313L, 562949953421440L, 562949953421441L, 562949953437696L, 562949953437697L, 562949953437824L, 562949953437825L, 562949955518464L, 562949955518465L, 562949955518592L, 562949955518593L, 562949955534848L, 562949955534849L, 562949955534976L, 562949955534977L, 562950221856768L, 562950221856769L, 562950221856896L, 562950221856897L, 562950221873152L, 562950221873153L, 562950221873280L, 562950221873281L, 562950223953920L, 562950223953921L, 562950223954048L, 562950223954049L, 562950223970304L, 562950223970305L, 562950223970432L, 562950223970433L, 562984313159680L, 562984313159681L, 562984313159808L, 562984313159809L, 562984313176064L, 562984313176065L, 562984313176192L, 562984313176193L, 562984315256832L, 562984315256833L, 562984315256960L, 562984315256961L, 562984315273216L, 562984315273217L, 562984315273344L, 562984315273345L, 562984581595136L, 562984581595137L, 562984581595264L, 562984581595265L, 562984581611520L, 562984581611521L, 562984581611648L, 562984581611649L, 562984583692288L, 562984583692289L, 562984583692416L, 562984583692417L, 562984583708672L, 562984583708673L, 562984583708800L, 562984583708801L, 567347999932416L, 567347999932417L, 567347999932544L, 567347999932545L, 567347999948800L, 567347999948801L, 567347999948928L, 567347999948929L, 567348002029568L, 567348002029569L, 567348002029696L, 567348002029697L, 567348002045952L, 567348002045953L, 567348002046080L, 567348002046081L, 567348268367872L, 567348268367873L, 567348268368000L, 567348268368001L, 567348268384256L, 567348268384257L, 567348268384384L, 567348268384385L, 567348270465024L, 567348270465025L, 567348270465152L, 567348270465153L, 567348270481408L, 567348270481409L, 567348270481536L, 567348270481537L, 567382359670784L, 567382359670785L, 567382359670912L, 567382359670913L, 567382359687168L, 567382359687169L, 567382359687296L, 567382359687297L, 567382361767936L, 567382361767937L, 567382361768064L, 567382361768065L, 567382361784320L, 567382361784321L, 567382361784448L, 567382361784449L, 567382628106240L, 567382628106241L, 567382628106368L, 567382628106369L, 567382628122624L, 567382628122625L, 567382628122752L, 567382628122753L, 567382630203392L, 567382630203393L, 567382630203520L, 567382630203521L, 567382630219776L, 567382630219777L, 567382630219904L, 567382630219905L, 72057594037927936L, 72057594037927937L, 72057594037928064L, 72057594037928065L, 72057594037944320L, 72057594037944321L, 72057594037944448L, 72057594037944449L, 72057594040025088L, 72057594040025089L, 72057594040025216L, 72057594040025217L, 72057594040041472L, 72057594040041473L, 72057594040041600L, 72057594040041601L, 72057594306363392L, 72057594306363393L, 72057594306363520L, 72057594306363521L, 72057594306379776L, 72057594306379777L, 72057594306379904L, 72057594306379905L, 72057594308460544L, 72057594308460545L, 72057594308460672L, 72057594308460673L, 72057594308476928L, 72057594308476929L, 72057594308477056L, 72057594308477057L, 72057628397666304L, 72057628397666305L, 72057628397666432L, 72057628397666433L, 72057628397682688L, 72057628397682689L, 72057628397682816L, 72057628397682817L, 72057628399763456L, 72057628399763457L, 72057628399763584L, 72057628399763585L, 72057628399779840L, 72057628399779841L, 72057628399779968L, 72057628399779969L, 72057628666101760L, 72057628666101761L, 72057628666101888L, 72057628666101889L, 72057628666118144L, 72057628666118145L, 72057628666118272L, 72057628666118273L, 72057628668198912L, 72057628668198913L, 72057628668199040L, 72057628668199041L, 72057628668215296L, 72057628668215297L, 72057628668215424L, 72057628668215425L, 72061992084439040L, 72061992084439041L, 72061992084439168L, 72061992084439169L, 72061992084455424L, 72061992084455425L, 72061992084455552L, 72061992084455553L, 72061992086536192L, 72061992086536193L, 72061992086536320L, 72061992086536321L, 72061992086552576L, 72061992086552577L, 72061992086552704L, 72061992086552705L, 72061992352874496L, 72061992352874497L, 72061992352874624L, 72061992352874625L, 72061992352890880L, 72061992352890881L, 72061992352891008L, 72061992352891009L, 72061992354971648L, 72061992354971649L, 72061992354971776L, 72061992354971777L, 72061992354988032L, 72061992354988033L, 72061992354988160L, 72061992354988161L, 72062026444177408L, 72062026444177409L, 72062026444177536L, 72062026444177537L, 72062026444193792L, 72062026444193793L, 72062026444193920L, 72062026444193921L, 72062026446274560L, 72062026446274561L, 72062026446274688L, 72062026446274689L, 72062026446290944L, 72062026446290945L, 72062026446291072L, 72062026446291073L, 72062026712612864L, 72062026712612865L, 72062026712612992L, 72062026712612993L, 72062026712629248L, 72062026712629249L, 72062026712629376L, 72062026712629377L, 72062026714710016L, 72062026714710017L, 72062026714710144L, 72062026714710145L, 72062026714726400L, 72062026714726401L, 72062026714726528L, 72062026714726529L, 72620543991349248L, 72620543991349249L, 72620543991349376L, 72620543991349377L, 72620543991365632L, 72620543991365633L, 72620543991365760L, 72620543991365761L, 72620543993446400L, 72620543993446401L, 72620543993446528L, 72620543993446529L, 72620543993462784L, 72620543993462785L, 72620543993462912L, 72620543993462913L, 72620544259784704L, 72620544259784705L, 72620544259784832L, 72620544259784833L, 72620544259801088L, 72620544259801089L, 72620544259801216L, 72620544259801217L, 72620544261881856L, 72620544261881857L, 72620544261881984L, 72620544261881985L, 72620544261898240L, 72620544261898241L, 72620544261898368L, 72620544261898369L, 72620578351087616L, 72620578351087617L, 72620578351087744L, 72620578351087745L, 72620578351104000L, 72620578351104001L, 72620578351104128L, 72620578351104129L, 72620578353184768L, 72620578353184769L, 72620578353184896L, 72620578353184897L, 72620578353201152L, 72620578353201153L, 72620578353201280L, 72620578353201281L, 72620578619523072L, 72620578619523073L, 72620578619523200L, 72620578619523201L, 72620578619539456L, 72620578619539457L, 72620578619539584L, 72620578619539585L, 72620578621620224L, 72620578621620225L, 72620578621620352L, 72620578621620353L, 72620578621636608L, 72620578621636609L, 72620578621636736L, 72620578621636737L, 72624942037860352L, 72624942037860353L, 72624942037860480L, 72624942037860481L, 72624942037876736L, 72624942037876737L, 72624942037876864L, 72624942037876865L, 72624942039957504L, 72624942039957505L, 72624942039957632L, 72624942039957633L, 72624942039973888L, 72624942039973889L, 72624942039974016L, 72624942039974017L, 72624942306295808L, 72624942306295809L, 72624942306295936L, 72624942306295937L, 72624942306312192L, 72624942306312193L, 72624942306312320L, 72624942306312321L, 72624942308392960L, 72624942308392961L, 72624942308393088L, 72624942308393089L, 72624942308409344L, 72624942308409345L, 72624942308409472L, 72624942308409473L, 72624976397598720L, 72624976397598721L, 72624976397598848L, 72624976397598849L, 72624976397615104L, 72624976397615105L, 72624976397615232L, 72624976397615233L, 72624976399695872L, 72624976399695873L, 72624976399696000L, 72624976399696001L, 72624976399712256L, 72624976399712257L, 72624976399712384L, 72624976399712385L, 72624976666034176L, 72624976666034177L, 72624976666034304L, 72624976666034305L, 72624976666050560L, 72624976666050561L, 72624976666050688L, 72624976666050689L, 72624976668131328L, 72624976668131329L, 72624976668131456L, 72624976668131457L, 72624976668147712L, 72624976668147713L, 72624976668147840L, 72624976668147841L};
    private static final String ZEROES = "0000000000000000000000000000000000000000000000000000000000000000";
    static final byte[] bitLengths = {0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};
    private long[] m_ints;

    public LongArray(int intLen) {
        this.m_ints = new long[intLen];
    }

    public LongArray(long[] ints) {
        this.m_ints = ints;
    }

    public LongArray(long[] ints, int off, int len) {
        if (off == 0 && len == ints.length) {
            this.m_ints = ints;
            return;
        }
        this.m_ints = new long[len];
        System.arraycopy(ints, off, this.m_ints, 0, len);
    }

    public LongArray(BigInteger bigInt) {
        int iarrJ;
        if (bigInt == null || bigInt.signum() < 0) {
            throw new IllegalArgumentException("invalid F2m field value");
        }
        int i = 0;
        if (bigInt.signum() == 0) {
            this.m_ints = new long[]{0};
            return;
        }
        byte[] barr = bigInt.toByteArray();
        int barrLen = barr.length;
        int barrStart = 0;
        if (barr[0] == 0) {
            barrLen--;
            barrStart = 1;
        }
        int intLen = (barrLen + 7) / 8;
        this.m_ints = new long[intLen];
        int iarrJ2 = intLen - 1;
        int rem = (barrLen % 8) + barrStart;
        long temp = 0;
        int barrI = barrStart;
        if (barrStart < rem) {
            while (barrI < rem) {
                temp = (temp << 8) | ((long) (barr[barrI] & 255));
                barrI++;
            }
            iarrJ = iarrJ2 - 1;
            this.m_ints[iarrJ2] = temp;
        } else {
            iarrJ = iarrJ2;
        }
        while (iarrJ >= 0) {
            long temp2 = 0;
            int i2 = i;
            while (i2 < 8) {
                temp2 = (temp2 << 8) | ((long) (barr[barrI] & 255));
                i2++;
                barrI++;
                barrLen = barrLen;
            }
            this.m_ints[iarrJ] = temp2;
            iarrJ--;
            i = 0;
        }
    }

    public boolean isOne() {
        long[] a = this.m_ints;
        if (a[0] != 1) {
            return false;
        }
        for (int i = 1; i < a.length; i++) {
            if (a[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isZero() {
        long[] a = this.m_ints;
        for (long j : a) {
            if (j != 0) {
                return false;
            }
        }
        return true;
    }

    public int getUsedLength() {
        return getUsedLengthFrom(this.m_ints.length);
    }

    public int getUsedLengthFrom(int from) {
        long[] a = this.m_ints;
        int from2 = Math.min(from, a.length);
        if (from2 < 1) {
            return 0;
        }
        if (a[0] != 0) {
            do {
                from2--;
            } while (a[from2] == 0);
            return from2 + 1;
        }
        do {
            from2--;
            if (a[from2] != 0) {
                return from2 + 1;
            }
        } while (from2 > 0);
        return 0;
    }

    public int degree() {
        int i = this.m_ints.length;
        while (i != 0) {
            i--;
            long w = this.m_ints[i];
            if (w != 0) {
                return (i << 6) + bitLength(w);
            }
        }
        return 0;
    }

    private int degreeFrom(int limit) {
        int i = (limit + 62) >>> 6;
        while (i != 0) {
            i--;
            long w = this.m_ints[i];
            if (w != 0) {
                return (i << 6) + bitLength(w);
            }
        }
        return 0;
    }

    /* JADX WARNING: type inference failed for: r3v6, types: [byte[]] */
    /* JADX WARNING: type inference failed for: r3v7, types: [byte] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r3v7, types: [byte] */
    private static int bitLength(long w) {
        int k;
        int b = 32;
        int u = (int) (w >>> 32);
        if (u == 0) {
            u = (int) w;
            b = 0;
        }
        int t = u >>> 16;
        if (t == 0) {
            int t2 = u >>> 8;
            k = t2 == 0 ? bitLengths[u] : bitLengths[t2] + 8;
        } else {
            int k2 = t >>> 8;
            k = k2 == 0 ? 16 + bitLengths[t] : 24 + bitLengths[k2];
        }
        return b + k;
    }

    private long[] resizedInts(int newLen) {
        long[] newInts = new long[newLen];
        System.arraycopy(this.m_ints, 0, newInts, 0, Math.min(this.m_ints.length, newLen));
        return newInts;
    }

    public BigInteger toBigInteger() {
        int usedLen = getUsedLength();
        if (usedLen == 0) {
            return ECConstants.ZERO;
        }
        long highestInt = this.m_ints[usedLen - 1];
        int i = 8;
        byte[] temp = new byte[8];
        boolean trailingZeroBytesDone = false;
        int barrI = 0;
        for (int j = 7; j >= 0; j--) {
            byte thisByte = (byte) ((int) (highestInt >>> (8 * j)));
            if (trailingZeroBytesDone || thisByte != 0) {
                trailingZeroBytesDone = true;
                temp[barrI] = thisByte;
                barrI++;
            }
        }
        byte[] barr = new byte[(((usedLen - 1) * 8) + barrI)];
        for (int j2 = 0; j2 < barrI; j2++) {
            barr[j2] = temp[j2];
        }
        int iarrJ = usedLen - 2;
        while (iarrJ >= 0) {
            long mi = this.m_ints[iarrJ];
            int barrI2 = barrI;
            int j3 = 7;
            while (j3 >= 0) {
                barr[barrI2] = (byte) ((int) (mi >>> (i * j3)));
                j3--;
                barrI2++;
                temp = temp;
                i = 8;
            }
            iarrJ--;
            barrI = barrI2;
            i = 8;
        }
        return new BigInteger(1, barr);
    }

    private static long shiftUp(long[] x, int xOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        for (int i = 0; i < count; i++) {
            long next = x[xOff + i];
            x[xOff + i] = (next << shift) | prev;
            prev = next >>> shiftInv;
        }
        return prev;
    }

    private static long shiftUp(long[] x, int xOff, long[] z, int zOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        for (int i = 0; i < count; i++) {
            long next = x[xOff + i];
            z[zOff + i] = (next << shift) | prev;
            prev = next >>> shiftInv;
        }
        return prev;
    }

    public LongArray addOne() {
        if (this.m_ints.length == 0) {
            return new LongArray(new long[]{1});
        }
        long[] ints = resizedInts(Math.max(1, getUsedLength()));
        ints[0] = 1 ^ ints[0];
        return new LongArray(ints);
    }

    private void addShiftedByBitsSafe(LongArray other, int otherDegree, int bits) {
        int otherLen = (otherDegree + 63) >>> 6;
        int words = bits >>> 6;
        int shift = bits & 63;
        if (shift == 0) {
            add(this.m_ints, words, other.m_ints, 0, otherLen);
            return;
        }
        long carry = addShiftedUp(this.m_ints, words, other.m_ints, 0, otherLen, shift);
        if (carry != 0) {
            long[] jArr = this.m_ints;
            int i = otherLen + words;
            jArr[i] = jArr[i] ^ carry;
        }
    }

    private static long addShiftedUp(long[] x, int xOff, long[] y, int yOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        for (int i = 0; i < count; i++) {
            long next = y[yOff + i];
            int i2 = xOff + i;
            x[i2] = x[i2] ^ ((next << shift) | prev);
            prev = next >>> shiftInv;
        }
        return prev;
    }

    private static long addShiftedDown(long[] x, int xOff, long[] y, int yOff, int count, int shift) {
        int shiftInv = 64 - shift;
        long prev = 0;
        int i = count;
        while (true) {
            i--;
            if (i < 0) {
                return prev;
            }
            long next = y[yOff + i];
            int i2 = xOff + i;
            x[i2] = x[i2] ^ ((next >>> shift) | prev);
            prev = next << shiftInv;
        }
    }

    public void addShiftedByWords(LongArray other, int words) {
        int otherUsedLen = other.getUsedLength();
        if (otherUsedLen != 0) {
            int minLen = otherUsedLen + words;
            if (minLen > this.m_ints.length) {
                this.m_ints = resizedInts(minLen);
            }
            add(this.m_ints, words, other.m_ints, 0, otherUsedLen);
        }
    }

    private static void add(long[] x, int xOff, long[] y, int yOff, int count) {
        for (int i = 0; i < count; i++) {
            int i2 = xOff + i;
            x[i2] = x[i2] ^ y[yOff + i];
        }
    }

    private static void add(long[] x, int xOff, long[] y, int yOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = x[xOff + i] ^ y[yOff + i];
        }
    }

    private static void addBoth(long[] x, int xOff, long[] y1, int y1Off, long[] y2, int y2Off, int count) {
        for (int i = 0; i < count; i++) {
            int i2 = xOff + i;
            x[i2] = x[i2] ^ (y1[y1Off + i] ^ y2[y2Off + i]);
        }
    }

    private static void distribute(long[] x, int src, int dst1, int dst2, int count) {
        for (int i = 0; i < count; i++) {
            long v = x[src + i];
            int i2 = dst1 + i;
            x[i2] = x[i2] ^ v;
            int i3 = dst2 + i;
            x[i3] = x[i3] ^ v;
        }
    }

    public int getLength() {
        return this.m_ints.length;
    }

    private static void flipWord(long[] buf, int off, int bit, long word) {
        int n = (bit >>> 6) + off;
        int shift = bit & 63;
        if (shift == 0) {
            buf[n] = buf[n] ^ word;
            return;
        }
        buf[n] = buf[n] ^ (word << shift);
        long word2 = word >>> (64 - shift);
        if (word2 != 0) {
            int n2 = n + 1;
            buf[n2] = buf[n2] ^ word2;
        }
    }

    public boolean testBitZero() {
        return this.m_ints.length > 0 && (this.m_ints[0] & 1) != 0;
    }

    private static boolean testBit(long[] buf, int off, int n) {
        return (buf[off + (n >>> 6)] & (1 << (n & 63))) != 0;
    }

    private static void flipBit(long[] buf, int off, int n) {
        int i = off + (n >>> 6);
        buf[i] = buf[i] ^ (1 << (n & 63));
    }

    private static void multiplyWord(long a, long[] b, int bLen, long[] c, int cOff) {
        long[] jArr;
        int i = bLen;
        long[] jArr2 = c;
        int i2 = cOff;
        if ((a & 1) != 0) {
            jArr = b;
            add(jArr2, i2, jArr, 0, i);
        } else {
            jArr = b;
        }
        long a2 = a;
        int k = 1;
        while (true) {
            int k2 = k;
            long j = a2 >>> 1;
            long a3 = j;
            if (j != 0) {
                if ((a3 & 1) != 0) {
                    long carry = addShiftedUp(jArr2, i2, jArr, 0, i, k2);
                    if (carry != 0) {
                        int i3 = i2 + i;
                        jArr2[i3] = jArr2[i3] ^ carry;
                    }
                }
                k = k2 + 1;
                a2 = a3;
            } else {
                return;
            }
        }
    }

    public LongArray modMultiplyLD(LongArray other, int m, int[] ks) {
        long[] T0;
        int[] ti;
        int i = m;
        int[] iArr = ks;
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            int i2 = aDeg;
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return reduceResult(c0, 0, cLen, i, iArr);
        }
        int aDeg2 = cLen;
        int bMax = ((bDeg + 7) + 63) >>> 6;
        int[] ti2 = new int[16];
        long[] T02 = new long[(bMax << 4)];
        int tOff = bMax;
        ti2[1] = tOff;
        System.arraycopy(B.m_ints, 0, T02, tOff, bLen);
        int i3 = 2;
        int tOff2 = tOff;
        while (i3 < 16) {
            int i4 = tOff2 + bMax;
            tOff2 = i4;
            ti2[i3] = i4;
            if ((i3 & 1) == 0) {
                T0 = T02;
                ti = ti2;
                shiftUp(T02, tOff2 >>> 1, T0, tOff2, bMax, 1);
            } else {
                T0 = T02;
                ti = ti2;
                add(T0, bMax, T0, tOff2 - bMax, T0, tOff2, bMax);
            }
            i3++;
            ti2 = ti;
            T02 = T0;
        }
        int[] ti3 = ti2;
        long[] T03 = T02;
        long[] T1 = new long[T03.length];
        shiftUp(T03, 0, T1, 0, T03.length, 4);
        long[] a = A.m_ints;
        long[] c = new long[aDeg2];
        int k = 56;
        int k2 = 56;
        while (true) {
            int k3 = k2;
            if (k3 < 0) {
                break;
            }
            int j = 1;
            while (true) {
                int j2 = j;
                if (j2 >= aLen) {
                    break;
                }
                long[] c2 = c;
                int aVal = (int) (a[j2] >>> k3);
                int i5 = aVal;
                long[] T12 = T1;
                addBoth(c2, j2 - 1, T03, ti3[aVal & 15], T12, ti3[(aVal >>> 4) & 15], bMax);
                j = j2 + 2;
                c = c2;
                a = a;
                T1 = T12;
                bDeg = bDeg;
            }
            long[] jArr = a;
            long[] jArr2 = T1;
            shiftUp(c, 0, aDeg2, 8);
            k2 = k3 - 8;
            bDeg = bDeg;
        }
        long[] c3 = c;
        long[] a2 = a;
        long[] T13 = T1;
        while (true) {
            int k4 = k;
            if (k4 < 0) {
                return reduceResult(c3, 0, aDeg2, i, iArr);
            }
            int j3 = 0;
            while (true) {
                int j4 = j3;
                if (j4 >= aLen) {
                    break;
                }
                int aVal2 = (int) (a2[j4] >>> k4);
                int i6 = aVal2;
                addBoth(c3, j4, T03, ti3[aVal2 & 15], T13, ti3[(aVal2 >>> 4) & 15], bMax);
                j3 = j4 + 2;
            }
            if (k4 > 0) {
                shiftUp(c3, 0, aDeg2, 8);
            }
            k = k4 - 8;
        }
    }

    public LongArray modMultiply(LongArray other, int m, int[] ks) {
        int aPos;
        long[] c;
        long[] a;
        int[] ti;
        long[] T0;
        int i = m;
        int[] iArr = ks;
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            int i2 = aDeg;
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return reduceResult(c0, 0, cLen, i, iArr);
        }
        int aDeg2 = cLen;
        int bMax = ((bDeg + 7) + 63) >>> 6;
        int[] ti2 = new int[16];
        long[] T02 = new long[(bMax << 4)];
        int tOff = bMax;
        ti2[1] = tOff;
        System.arraycopy(B.m_ints, 0, T02, tOff, bLen);
        int i3 = 2;
        int tOff2 = tOff;
        while (i3 < 16) {
            int i4 = tOff2 + bMax;
            tOff2 = i4;
            ti2[i3] = i4;
            if ((i3 & 1) == 0) {
                T0 = T02;
                ti = ti2;
                shiftUp(T02, tOff2 >>> 1, T02, tOff2, bMax, 1);
            } else {
                T0 = T02;
                ti = ti2;
                add(T0, bMax, T02, tOff2 - bMax, T0, tOff2, bMax);
            }
            i3++;
            T02 = T0;
            ti2 = ti;
        }
        int[] ti3 = ti2;
        long[] T03 = T02;
        long[] T1 = new long[T03.length];
        shiftUp(T03, 0, T1, 0, T03.length, 4);
        long[] a2 = A.m_ints;
        long[] c2 = new long[(aDeg2 << 3)];
        int aPos2 = 0;
        while (true) {
            int aPos3 = aPos2;
            if (aPos3 >= aLen) {
                break;
            }
            int bDeg2 = bDeg;
            int aPos4 = aPos3;
            long aVal = a2[aPos3];
            while (true) {
                int cOff = aPos4;
                int u = ((int) aVal) & 15;
                long aVal2 = aVal >>> 4;
                aPos = aPos3;
                c = c2;
                a = a2;
                addBoth(c2, cOff, T03, ti3[u], T1, ti3[((int) aVal2) & 15], bMax);
                aVal = aVal2 >>> 4;
                if (aVal == 0) {
                    break;
                }
                aPos4 = cOff + aDeg2;
                c2 = c;
                aPos3 = aPos;
                a2 = a;
            }
            aPos2 = aPos + 1;
            c2 = c;
            a2 = a;
            bDeg = bDeg2;
        }
        long[] c3 = c2;
        long[] jArr = a2;
        int cOff2 = c3.length;
        while (true) {
            int i5 = cOff2 - aDeg2;
            cOff2 = i5;
            if (i5 != 0) {
                long[] T12 = T1;
                addShiftedUp(c3, cOff2 - aDeg2, c3, cOff2, aDeg2, 8);
                T1 = T12;
            } else {
                return reduceResult(c3, 0, aDeg2, i, iArr);
            }
        }
    }

    public LongArray modMultiplyAlt(LongArray other, int m, int[] ks) {
        LongArray B;
        LongArray A;
        int index;
        int[] ci;
        int top;
        boolean z;
        int top2;
        int i = m;
        int[] iArr = ks;
        int aDeg = degree();
        if (aDeg == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A2 = this;
        LongArray B2 = other;
        if (aDeg > bDeg) {
            A2 = other;
            B2 = this;
            int tmp = aDeg;
            aDeg = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A2.m_ints[0];
            if (a0 == 1) {
                return B2;
            }
            long[] c0 = new long[cLen];
            int i2 = aDeg;
            multiplyWord(a0, B2.m_ints, bLen, c0, 0);
            return reduceResult(c0, 0, cLen, i, iArr);
        }
        int aDeg2 = cLen;
        int top3 = 64;
        int bMax = ((bDeg + (64 < 64 ? 16 : 16 - 1)) + 63) >>> 6;
        int bTotal = bMax * 8;
        int stride = 4 * 8;
        int[] ci2 = new int[(1 << 4)];
        int cTotal = aLen;
        ci2[0] = cTotal;
        int cTotal2 = cTotal + bTotal;
        ci2[1] = cTotal2;
        int i3 = 2;
        while (true) {
            int i4 = i3;
            if (i4 >= ci2.length) {
                break;
            }
            cTotal2 += aDeg2;
            ci2[i4] = cTotal2;
            i3 = i4 + 1;
        }
        int cTotal3 = cTotal2 + aDeg2 + 1;
        long[] c = new long[cTotal3];
        int i5 = bDeg;
        int bLen2 = bLen;
        int bTotal2 = bTotal;
        int bMax2 = bMax;
        int[] ci3 = ci2;
        int i6 = cTotal3;
        interleave(A2.m_ints, 0, c, 0, aLen, 4);
        int bOff = aLen;
        int bLen3 = bLen2;
        int i7 = 0;
        System.arraycopy(B2.m_ints, 0, c, bOff, bLen3);
        int bOff2 = bOff;
        int bOff3 = 1;
        while (true) {
            int bank = bOff3;
            if (bank >= 8) {
                break;
            }
            int bMax3 = bMax2;
            int bank2 = bOff2 + bMax3;
            int bMax4 = bMax3;
            int bank3 = bank;
            shiftUp(c, aLen, c, bank2, bMax4, bank3);
            bOff3 = bank3 + 1;
            i7 = i7;
            bOff2 = bank2;
            bLen3 = bLen3;
            bMax2 = bMax4;
        }
        int i8 = i7;
        int i9 = bLen3;
        int bMax5 = bMax2;
        int MASK = (1 << 4) - 1;
        int MASK2 = i8;
        while (true) {
            int aPos = i8;
            while (true) {
                int top4 = 0;
                long aVal = c[aPos] >>> MASK2;
                int bOff4 = aLen;
                while (true) {
                    int index2 = ((int) aVal) & MASK;
                    if (index2 != 0) {
                        A = A2;
                        ci = ci3;
                        B = B2;
                        int i10 = index2;
                        index = bMax5;
                        add(c, aPos + ci[index2], c, bOff4, index);
                    } else {
                        A = A2;
                        B = B2;
                        int i11 = index2;
                        ci = ci3;
                        index = bMax5;
                    }
                    long aVal2 = aVal;
                    int bank4 = top4 + 1;
                    if (bank4 == 8) {
                        break;
                    }
                    bOff4 += index;
                    ci3 = ci;
                    bMax5 = index;
                    A2 = A;
                    B2 = B;
                    top4 = bank4;
                    aVal = aVal2 >>> 4;
                }
                aPos++;
                if (aPos >= aLen) {
                    break;
                }
                ci3 = ci;
                bMax5 = index;
                A2 = A;
                B2 = B;
            }
            int i12 = MASK2 + stride;
            MASK2 = i12;
            if (i12 < top3) {
                top = top3;
                z = true;
            } else if (MASK2 >= 64) {
                break;
            } else {
                top = top3;
                z = true;
                int k = 64 - 4;
                MASK &= MASK << (top - k);
                MASK2 = k;
            }
            shiftUp(c, aLen, bTotal2, 8);
            ci3 = ci;
            bMax5 = index;
            int bMax6 = z;
            top3 = top;
            A2 = A;
            B2 = B;
        }
        int ciPos = ci.length;
        while (true) {
            ciPos--;
            if (ciPos > 1) {
                if ((((long) ciPos) & 1) == 0) {
                    top2 = top3;
                    addShiftedUp(c, ci[ciPos >>> 1], c, ci[ciPos], aDeg2, 16);
                } else {
                    top2 = top3;
                    distribute(c, ci[ciPos], ci[ciPos - 1], ci[1], aDeg2);
                }
                top3 = top2;
            } else {
                int i13 = top3;
                return reduceResult(c, ci[1], aDeg2, i, iArr);
            }
        }
    }

    public LongArray modReduce(int m, int[] ks) {
        long[] buf = Arrays.clone(this.m_ints);
        return new LongArray(buf, 0, reduceInPlace(buf, 0, buf.length, m, ks));
    }

    public LongArray multiply(LongArray other, int m, int[] ks) {
        int aDeg;
        int aDeg2 = degree();
        if (aDeg2 == 0) {
            return this;
        }
        int bDeg = other.degree();
        if (bDeg == 0) {
            return other;
        }
        LongArray A = this;
        LongArray B = other;
        if (aDeg2 > bDeg) {
            A = other;
            B = this;
            int tmp = aDeg2;
            aDeg2 = bDeg;
            bDeg = tmp;
        }
        int aLen = (aDeg2 + 63) >>> 6;
        int bLen = (bDeg + 63) >>> 6;
        int cLen = ((aDeg2 + bDeg) + 62) >>> 6;
        if (aLen == 1) {
            long a0 = A.m_ints[0];
            if (a0 == 1) {
                return B;
            }
            long[] c0 = new long[cLen];
            multiplyWord(a0, B.m_ints, bLen, c0, 0);
            return new LongArray(c0, 0, cLen);
        }
        int bMax = ((bDeg + 7) + 63) >>> 6;
        int[] ti = new int[16];
        long[] T0 = new long[(bMax << 4)];
        int tOff = bMax;
        ti[1] = tOff;
        System.arraycopy(B.m_ints, 0, T0, tOff, bLen);
        int tOff2 = tOff;
        for (int i = 2; i < 16; i++) {
            int i2 = tOff2 + bMax;
            tOff2 = i2;
            ti[i] = i2;
            if ((i & 1) == 0) {
                shiftUp(T0, tOff2 >>> 1, T0, tOff2, bMax, 1);
            } else {
                add(T0, bMax, T0, tOff2 - bMax, T0, tOff2, bMax);
            }
        }
        long[] T1 = new long[T0.length];
        shiftUp(T0, 0, T1, 0, T0.length, 4);
        long[] a = A.m_ints;
        long[] c = new long[(cLen << 3)];
        int aPos = 0;
        while (aPos < aLen) {
            long[] T02 = T0;
            long aVal = a[aPos];
            int cOff = aPos;
            while (true) {
                aDeg = aDeg2;
                long aVal2 = aVal >>> 4;
                addBoth(c, cOff, T02, ti[((int) aVal) & 15], T1, ti[((int) aVal2) & 15], bMax);
                aVal = aVal2 >>> 4;
                if (aVal == 0) {
                    break;
                }
                cOff += cLen;
                aDeg2 = aDeg;
            }
            aPos++;
            T0 = T02;
            aDeg2 = aDeg;
        }
        long[] T03 = T0;
        int cOff2 = c.length;
        while (true) {
            int i3 = cOff2 - cLen;
            int cOff3 = i3;
            if (i3 != 0) {
                long[] c2 = c;
                long[] jArr = T03;
                addShiftedUp(c, cOff3 - cLen, c2, cOff3, cLen, 8);
                c = c2;
                ti = ti;
                cOff2 = cOff3;
                a = a;
            } else {
                long[] jArr2 = a;
                int[] iArr = ti;
                long[] jArr3 = T03;
                return new LongArray(c, 0, cLen);
            }
        }
    }

    public void reduce(int m, int[] ks) {
        long[] buf = this.m_ints;
        int rLen = reduceInPlace(buf, 0, buf.length, m, ks);
        if (rLen < buf.length) {
            this.m_ints = new long[rLen];
            System.arraycopy(buf, 0, this.m_ints, 0, rLen);
        }
    }

    private static LongArray reduceResult(long[] buf, int off, int len, int m, int[] ks) {
        return new LongArray(buf, off, reduceInPlace(buf, off, len, m, ks));
    }

    private static int reduceInPlace(long[] buf, int off, int len, int m, int[] ks) {
        int excessBits;
        int len2;
        int len3;
        int numBits;
        int vectorWiseWords;
        long[] jArr = buf;
        int i = off;
        int i2 = len;
        int vectorableWords = m;
        int[] iArr = ks;
        int mLen = (vectorableWords + 63) >>> 6;
        if (i2 < mLen) {
            return i2;
        }
        int numBits2 = Math.min(i2 << 6, (vectorableWords << 1) - 1);
        int excessBits2 = (i2 << 6) - numBits2;
        int len4 = i2;
        while (true) {
            excessBits = excessBits2;
            if (excessBits < 64) {
                break;
            }
            len4--;
            excessBits2 = excessBits - 64;
        }
        int kMax = iArr[iArr.length - 1];
        int kNext = iArr.length > 1 ? iArr[iArr.length - 2] : 0;
        int wordWiseLimit = Math.max(vectorableWords, kMax + 64);
        int vectorableWords2 = (Math.min(numBits2 - wordWiseLimit, vectorableWords - kNext) + excessBits) >> 6;
        if (vectorableWords2 > 1) {
            int vectorWiseWords2 = len4 - vectorableWords2;
            int vectorWiseWords3 = vectorWiseWords2;
            int i3 = vectorableWords2;
            int i4 = numBits2;
            numBits = wordWiseLimit;
            reduceVectorWise(jArr, i, len4, vectorWiseWords2, vectorableWords, iArr);
            while (true) {
                vectorWiseWords = vectorWiseWords3;
                if (len4 <= vectorWiseWords) {
                    break;
                }
                len4--;
                jArr[i + len4] = 0;
                vectorWiseWords3 = vectorWiseWords;
            }
            len2 = len4;
            len3 = vectorWiseWords << 6;
        } else {
            int i5 = numBits2;
            numBits = wordWiseLimit;
            len2 = len4;
            len3 = i5;
        }
        if (len3 > numBits) {
            reduceWordWise(jArr, i, len2, numBits, vectorableWords, iArr);
            len3 = numBits;
        }
        if (len3 > vectorableWords) {
            reduceBitWise(jArr, i, len3, vectorableWords, iArr);
        }
        return mLen;
    }

    private static void reduceBitWise(long[] buf, int off, int bitlength, int m, int[] ks) {
        while (true) {
            bitlength--;
            if (bitlength < m) {
                return;
            }
            if (testBit(buf, off, bitlength)) {
                reduceBit(buf, off, bitlength, m, ks);
            }
        }
    }

    private static void reduceBit(long[] buf, int off, int bit, int m, int[] ks) {
        flipBit(buf, off, bit);
        int n = bit - m;
        int j = ks.length;
        while (true) {
            j--;
            if (j >= 0) {
                flipBit(buf, off, ks[j] + n);
            } else {
                flipBit(buf, off, n);
                return;
            }
        }
    }

    private static void reduceWordWise(long[] buf, int off, int len, int toBit, int m, int[] ks) {
        int toPos;
        int toPos2 = toBit >>> 6;
        int len2 = len;
        while (true) {
            toPos = toPos2;
            int len3 = len2 - 1;
            if (len3 <= toPos) {
                break;
            }
            long word = buf[off + len3];
            if (word != 0) {
                buf[off + len3] = 0;
                reduceWord(buf, off, len3 << 6, word, m, ks);
            }
            toPos2 = toPos;
            len2 = len3;
        }
        int partial = toBit & 63;
        long word2 = buf[off + toPos] >>> partial;
        if (word2 != 0) {
            int i = off + toPos;
            buf[i] = buf[i] ^ (word2 << partial);
            reduceWord(buf, off, toBit, word2, m, ks);
        }
    }

    private static void reduceWord(long[] buf, int off, int bit, long word, int m, int[] ks) {
        int offset = bit - m;
        int j = ks.length;
        while (true) {
            j--;
            if (j >= 0) {
                flipWord(buf, off, ks[j] + offset, word);
            } else {
                flipWord(buf, off, offset, word);
                return;
            }
        }
    }

    private static void reduceVectorWise(long[] buf, int off, int len, int words, int m, int[] ks) {
        int baseBit = (words << 6) - m;
        int j = ks.length;
        while (true) {
            int j2 = j - 1;
            if (j2 >= 0) {
                flipVector(buf, off, buf, off + words, len - words, baseBit + ks[j2]);
                j = j2;
            } else {
                flipVector(buf, off, buf, off + words, len - words, baseBit);
                return;
            }
        }
    }

    private static void flipVector(long[] x, int xOff, long[] y, int yOff, int yLen, int bits) {
        int xOff2 = xOff + (bits >>> 6);
        int bits2 = bits & 63;
        if (bits2 == 0) {
            add(x, xOff2, y, yOff, yLen);
            return;
        }
        x[xOff2] = x[xOff2] ^ addShiftedDown(x, xOff2 + 1, y, yOff, yLen, 64 - bits2);
    }

    public LongArray modSquare(int m, int[] ks) {
        int len = getUsedLength();
        if (len == 0) {
            return this;
        }
        int _2len = len << 1;
        long[] r = new long[_2len];
        int pos = 0;
        while (pos < _2len) {
            long mi = this.m_ints[pos >>> 1];
            int pos2 = pos + 1;
            r[pos] = interleave2_32to64((int) mi);
            pos = pos2 + 1;
            r[pos2] = interleave2_32to64((int) (mi >>> 32));
        }
        return new LongArray(r, 0, reduceInPlace(r, 0, r.length, m, ks));
    }

    public LongArray modSquareN(int n, int m, int[] ks) {
        int len = getUsedLength();
        if (len == 0) {
            return this;
        }
        long[] r = new long[(((m + 63) >>> 6) << 1)];
        System.arraycopy(this.m_ints, 0, r, 0, len);
        while (true) {
            n--;
            if (n < 0) {
                return new LongArray(r, 0, len);
            }
            squareInPlace(r, len, m, ks);
            len = reduceInPlace(r, 0, r.length, m, ks);
        }
    }

    public LongArray square(int m, int[] ks) {
        int len = getUsedLength();
        if (len == 0) {
            return this;
        }
        int _2len = len << 1;
        long[] r = new long[_2len];
        int pos = 0;
        while (pos < _2len) {
            long mi = this.m_ints[pos >>> 1];
            int pos2 = pos + 1;
            r[pos] = interleave2_32to64((int) mi);
            pos = pos2 + 1;
            r[pos2] = interleave2_32to64((int) (mi >>> 32));
        }
        return new LongArray(r, 0, r.length);
    }

    private static void squareInPlace(long[] x, int xLen, int m, int[] ks) {
        int pos = xLen << 1;
        while (true) {
            xLen--;
            if (xLen >= 0) {
                long xVal = x[xLen];
                int pos2 = pos - 1;
                x[pos2] = interleave2_32to64((int) (xVal >>> 32));
                pos = pos2 - 1;
                x[pos] = interleave2_32to64((int) xVal);
            } else {
                return;
            }
        }
    }

    private static void interleave(long[] x, int xOff, long[] z, int zOff, int count, int width) {
        if (width == 3) {
            interleave3(x, xOff, z, zOff, count);
        } else if (width == 5) {
            interleave5(x, xOff, z, zOff, count);
        } else if (width != 7) {
            interleave2_n(x, xOff, z, zOff, count, bitLengths[width] - 1);
        } else {
            interleave7(x, xOff, z, zOff, count);
        }
    }

    private static void interleave3(long[] x, int xOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave3(x[xOff + i]);
        }
    }

    private static long interleave3(long x) {
        return (interleave3_21to63(((int) (x >>> 42)) & 2097151) << 2) | interleave3_21to63(((int) x) & 2097151) | (Long.MIN_VALUE & x) | (interleave3_21to63(((int) (x >>> 21)) & 2097151) << 1);
    }

    private static long interleave3_21to63(int x) {
        int r00 = INTERLEAVE3_TABLE[x & 127];
        return ((((long) INTERLEAVE3_TABLE[x >>> 14]) & 4294967295L) << 42) | ((((long) INTERLEAVE3_TABLE[(x >>> 7) & 127]) & 4294967295L) << 21) | (4294967295L & ((long) r00));
    }

    private static void interleave5(long[] x, int xOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave5(x[xOff + i]);
        }
    }

    private static long interleave5(long x) {
        return interleave3_13to65(((int) x) & 8191) | (interleave3_13to65(((int) (x >>> 13)) & 8191) << 1) | (interleave3_13to65(((int) (x >>> 26)) & 8191) << 2) | (interleave3_13to65(((int) (x >>> 39)) & 8191) << 3) | (interleave3_13to65(((int) (x >>> 52)) & 8191) << 4);
    }

    private static long interleave3_13to65(int x) {
        return ((((long) INTERLEAVE5_TABLE[x >>> 7]) & 4294967295L) << 35) | (4294967295L & ((long) INTERLEAVE5_TABLE[x & 127]));
    }

    private static void interleave7(long[] x, int xOff, long[] z, int zOff, int count) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave7(x[xOff + i]);
        }
    }

    private static long interleave7(long x) {
        return INTERLEAVE7_TABLE[((int) x) & 511] | (Long.MIN_VALUE & x) | (INTERLEAVE7_TABLE[((int) (x >>> 9)) & 511] << 1) | (INTERLEAVE7_TABLE[((int) (x >>> 18)) & 511] << 2) | (INTERLEAVE7_TABLE[((int) (x >>> 27)) & 511] << 3) | (INTERLEAVE7_TABLE[((int) (x >>> 36)) & 511] << 4) | (INTERLEAVE7_TABLE[((int) (x >>> 45)) & 511] << 5) | (INTERLEAVE7_TABLE[((int) (x >>> 54)) & 511] << 6);
    }

    private static void interleave2_n(long[] x, int xOff, long[] z, int zOff, int count, int rounds) {
        for (int i = 0; i < count; i++) {
            z[zOff + i] = interleave2_n(x[xOff + i], rounds);
        }
    }

    private static long interleave2_n(long x, int rounds) {
        while (rounds > 1) {
            rounds -= 2;
            x = (interleave4_16to64(((int) (x >>> 16)) & 65535) << 1) | interleave4_16to64(((int) x) & 65535) | (interleave4_16to64(((int) (x >>> 32)) & 65535) << 2) | (interleave4_16to64(((int) (x >>> 48)) & 65535) << 3);
        }
        if (rounds > 0) {
            return interleave2_32to64((int) x) | (interleave2_32to64((int) (x >>> 32)) << 1);
        }
        return x;
    }

    private static long interleave4_16to64(int x) {
        return ((((long) INTERLEAVE4_TABLE[x >>> 8]) & 4294967295L) << 32) | (4294967295L & ((long) INTERLEAVE4_TABLE[x & 255]));
    }

    private static long interleave2_32to64(int x) {
        return ((((long) (INTERLEAVE2_TABLE[(x >>> 16) & 255] | (INTERLEAVE2_TABLE[x >>> 24] << 16))) & 4294967295L) << 32) | (4294967295L & ((long) (INTERLEAVE2_TABLE[x & 255] | (INTERLEAVE2_TABLE[(x >>> 8) & 255] << 16))));
    }

    public LongArray modInverse(int m, int[] ks) {
        boolean z;
        int i = m;
        int uzDegree = degree();
        if (uzDegree == 0) {
            int[] iArr = ks;
            int i2 = uzDegree;
            throw new IllegalStateException();
        } else if (uzDegree == 1) {
            return this;
        } else {
            LongArray uz = (LongArray) clone();
            int t = (i + 63) >>> 6;
            LongArray vz = new LongArray(t);
            reduceBit(vz.m_ints, 0, i, i, ks);
            LongArray g1z = new LongArray(t);
            g1z.m_ints[0] = 1;
            int[] uvDeg = {uzDegree, i + 1};
            LongArray[] uv = {uz, vz};
            int[] ggDeg = {1, 0};
            LongArray[] gg = {g1z, new LongArray(t)};
            int b = 1;
            int duv1 = uvDeg[1];
            int dgg1 = ggDeg[1];
            int j = duv1 - uvDeg[1 - 1];
            while (true) {
                int j2 = j;
                if (j2 < 0) {
                    j2 = -j2;
                    uvDeg[b] = duv1;
                    ggDeg[b] = dgg1;
                    z = true;
                    b = 1 - b;
                    duv1 = uvDeg[b];
                    dgg1 = ggDeg[b];
                } else {
                    z = true;
                }
                int dgg12 = dgg1;
                int uzDegree2 = uzDegree;
                LongArray uz2 = uz;
                uv[b].addShiftedByBitsSafe(uv[1 - b], uvDeg[1 - b], j2);
                int duv2 = uv[b].degreeFrom(duv1);
                if (duv2 == 0) {
                    return gg[1 - b];
                }
                int dgg2 = ggDeg[1 - b];
                int t2 = t;
                gg[b].addShiftedByBitsSafe(gg[1 - b], dgg2, j2);
                int dgg22 = dgg2 + j2;
                if (dgg22 > dgg12) {
                    dgg12 = dgg22;
                } else if (dgg22 == dgg12) {
                    dgg12 = gg[b].degreeFrom(dgg12);
                }
                dgg1 = dgg12;
                int j3 = (duv2 - duv1) + j2;
                duv1 = duv2;
                boolean z2 = z;
                uzDegree = uzDegree2;
                uz = uz2;
                t = t2;
                j = j3;
                int j4 = m;
            }
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof LongArray)) {
            return false;
        }
        LongArray other = (LongArray) o;
        int usedLen = getUsedLength();
        if (other.getUsedLength() != usedLen) {
            return false;
        }
        for (int i = 0; i < usedLen; i++) {
            if (this.m_ints[i] != other.m_ints[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int usedLen = getUsedLength();
        int hash = 1;
        for (int i = 0; i < usedLen; i++) {
            long mi = this.m_ints[i];
            hash = (((hash * 31) ^ ((int) mi)) * 31) ^ ((int) (mi >>> 32));
        }
        return hash;
    }

    public Object clone() {
        return new LongArray(Arrays.clone(this.m_ints));
    }

    public String toString() {
        int i = getUsedLength();
        if (i == 0) {
            return "0";
        }
        int i2 = i - 1;
        StringBuffer sb = new StringBuffer(Long.toBinaryString(this.m_ints[i2]));
        while (true) {
            i2--;
            if (i2 < 0) {
                return sb.toString();
            }
            String s = Long.toBinaryString(this.m_ints[i2]);
            int len = s.length();
            if (len < 64) {
                sb.append(ZEROES.substring(len));
            }
            sb.append(s);
        }
    }
}
