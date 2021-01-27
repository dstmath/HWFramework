package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;

public class CamelliaEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final int MASK8 = 255;
    private static final int[] SBOX1_1110 = {1886416896, -2105376256, 741092352, -320017408, -1280068864, 656877312, -1061109760, -437918464, -454761472, -2054847232, 1465341696, 892679424, -353703424, 202116096, -1364283904, 1094795520, 589505280, -269488384, 1802201856, -1819045120, 1162167552, 421075200, -1515870976, 555819264, -303174400, 235802112, 1330597632, 1313754624, 488447232, 1701143808, -1835888128, -1111638784, -2038004224, -1195853824, -1347440896, -1886417152, 2088532992, -336860416, 522133248, -825307648, 1044266496, 808464384, -589505536, 1600085760, 1583242752, -976894720, 185273088, 437918208, -1499027968, -505290496, 960051456, -892679680, -707406592, 1195853568, 1566399744, 1027423488, -640034560, 16843008, 1515870720, -690563584, 1364283648, 1448498688, 1819044864, 1296911616, -1953789184, 218959104, -1701144064, 1717986816, -67372288, -858993664, -1330597888, 757935360, 1953788928, 303174144, 724249344, 538976256, -252645376, -1313754880, -2071690240, -1717987072, -538976512, 1280068608, -875836672, -1027423744, 875836416, 2122219008, 1987474944, 84215040, 1835887872, -1212696832, -1448498944, 825307392, -774778624, 387389184, 67372032, -673720576, 336860160, 1482184704, 976894464, 1633771776, -555819520, 454761216, 286331136, 471604224, 842150400, 252645120, -1667458048, 370546176, 1397969664, 404232192, -218959360, 572662272, -16843264, 1145324544, -808464640, -1296911872, -1010580736, -1246382848, 2054846976, -1852731136, 606348288, 134744064, -387389440, -1465341952, 1616928768, -50529280, 1768515840, 1347440640, -1431655936, -791621632, -1600086016, 2105376000, -1583243008, -1987475200, 1650614784, -1751673088, 1414812672, 1532713728, 505290240, -1785359104, -522133504, -256, 1684300800, -757935616, 269488128, -993737728, 0, 1212696576, -1549556992, -134744320, 1970631936, -606348544, -1970632192, 50529024, -421075456, -623191552, 151587072, 1061109504, -572662528, -1802202112, -2021161216, 1549556736, -2088533248, 33686016, -842150656, 1246382592, -1869574144, 858993408, 1936945920, 1734829824, -151587328, -202116352, -1650615040, 2139062016, -1077952768, -488447488, 1381126656, -1684301056, -656877568, 640034304, -926365696, 926365440, -960051712, 993737472, -2122219264, -1768516096, 1869573888, 1263225600, 320017152, -1094795776, 1667457792, 774778368, -370546432, 2038003968, -1482184960, -1936946176, -1616929024, 1852730880, -1128481792, -1903260160, 690563328, -168430336, -101058304, -1229539840, 791621376, -33686272, -1263225856, 1499027712, 2021160960, -1734830080, 101058048, 1785358848, -404232448, 1179010560, 1903259904, -1162167808, -724249600, 623191296, -1414812928, 1111638528, -2004318208, -1566400000, -1920103168, -84215296, 1920102912, 117901056, -1179010816, 1431655680, -117901312, -286331392, -1397969920, 168430080, 909522432, 1229539584, 707406336, 1751672832, 1010580480, 943208448, -235802368, -1532713984, 1077952512, 673720320, -741092608, 2071689984, -1145324800, -909522688, 1128481536, -1044266752, 353703168, -471604480, -1381126912, -185273344, 2004317952, -943208704, -2139062272, -1633772032};
    private static final int[] SBOX2_0222 = {14737632, 328965, 5789784, 14277081, 6776679, 5131854, 8487297, 13355979, 13224393, 723723, 11447982, 6974058, 14013909, 1579032, 6118749, 8553090, 4605510, 14671839, 14079702, 2565927, 9079434, 3289650, 4934475, 4342338, 14408667, 1842204, 10395294, 10263708, 3815994, 13290186, 2434341, 8092539, 855309, 7434609, 6250335, 2039583, 16316664, 14145495, 4079166, 10329501, 8158332, 6316128, 12171705, 12500670, 12369084, 9145227, 1447446, 3421236, 5066061, 12829635, 7500402, 9803157, 11250603, 9342606, 12237498, 8026746, 11776947, 131586, 11842740, 11382189, 10658466, 11316396, 14211288, 10132122, 1513239, 1710618, 3487029, 13421772, 16250871, 10066329, 6381921, 5921370, 15263976, 2368548, 5658198, 4210752, 14803425, 6513507, 592137, 3355443, 12566463, 10000536, 9934743, 8750469, 6842472, 16579836, 15527148, 657930, 14342874, 7303023, 5460819, 6447714, 10724259, 3026478, 526344, 11513775, 2631720, 11579568, 7631988, 12763842, 12434877, 3552822, 2236962, 3684408, 6579300, 1973790, 3750201, 2894892, 10921638, 3158064, 15066597, 4473924, 16645629, 8947848, 10461087, 6645093, 8882055, 7039851, 16053492, 2302755, 4737096, 1052688, 13750737, 5329233, 12632256, 16382457, 13816530, 10526880, 5592405, 10592673, 4276545, 16448250, 4408131, 1250067, 12895428, 3092271, 11053224, 11974326, 3947580, 2829099, 12698049, 16777215, 13158600, 10855845, 2105376, 9013641, 0, 9474192, 4671303, 15724527, 15395562, 12040119, 1381653, 394758, 13487565, 11908533, 1184274, 8289918, 12303291, 2697513, 986895, 12105912, 460551, 263172, 10197915, 9737364, 2171169, 6710886, 15132390, 13553358, 15592941, 15198183, 3881787, 16711422, 8355711, 12961221, 10790052, 3618615, 11645361, 5000268, 9539985, 7237230, 9276813, 7763574, 197379, 2960685, 14606046, 9868950, 2500134, 8224125, 13027014, 6052956, 13882323, 15921906, 5197647, 1644825, 4144959, 14474460, 7960953, 1907997, 5395026, 15461355, 15987699, 7171437, 6184542, 16514043, 6908265, 11711154, 15790320, 3223857, 789516, 13948116, 13619151, 9211020, 14869218, 7697781, 11119017, 4868682, 5723991, 8684676, 1118481, 4539717, 1776411, 16119285, 15000804, 921102, 7566195, 11184810, 15856113, 14540253, 5855577, 1315860, 7105644, 9605778, 5526612, 13684944, 7895160, 7368816, 14935011, 4802889, 8421504, 5263440, 10987431, 16185078, 7829367, 9671571, 8816262, 8618883, 2763306, 13092807, 5987163, 15329769, 15658734, 9408399, 65793, 4013373};
    private static final int[] SBOX3_3033 = {939538488, 1090535745, 369104406, 1979741814, -654255655, -1828678765, 1610637408, -234818830, 1912631922, -1040137534, -1426019413, -1711236454, 1962964341, 100664838, 1459640151, -1610571616, -1862233711, -150931465, -1258244683, -922695223, -1577016670, -1946121076, -771697966, -1879011184, -167708938, 117442311, -1493129305, 654321447, -1912566130, -1308577102, 1224755529, -570368290, 1124090691, 1543527516, -687810601, -956250169, 1040203326, -184486411, -1895788657, 1728079719, 520101663, 402659352, 1845522030, -1358909521, 788541231, -503258398, -2063563387, 218107149, 1392530259, -268373776, -1677681508, 1694524773, -369038614, -1560239197, -1375686994, -1644126562, -335483668, -2147450752, 754986285, 1795189611, -1476351832, 721431339, 905983542, -1509906778, -989805115, -2046785914, 1291865421, 855651123, -50266627, 1711302246, 1476417624, -1778346346, 973093434, 150997257, -1795123819, 268439568, 2013296760, -671033128, 1107313218, -872362804, -285151249, 637543974, -452925979, 1627414881, 436214298, 1056980799, 989870907, -2113895806, -1241467210, -620700709, -738143020, -1744791400, -402593560, -1962898549, 33554946, -352261141, 167774730, 738208812, 486546717, -1342132048, 1862299503, -1929343603, -2013230968, 234884622, 419436825, -2030008441, 1308642894, 184552203, -1459574359, 201329676, 2030074233, 285217041, 2130739071, 570434082, -419371033, 1493195097, -520035871, -637478182, 1023425853, -939472696, 301994514, 67109892, 1946186868, 1409307732, 805318704, 2113961598, -1275022156, 671098920, 1426085205, 1744857192, 1342197840, -1107247426, -805252912, -1006582588, 822096177, -889140277, 704653866, -1392464467, 251662095, -905917750, 1879076976, -16711681, 838873650, 1761634665, 134219784, 1644192354, 0, 603989028, -788475439, -83821573, -1174357318, -318706195, 1157645637, -2130673279, 1929409395, 1828744557, -2080340860, -1627349089, -301928722, 1241533002, -1023360061, 771763758, -1056915007, 16777473, -436148506, 620766501, 1207978056, -1728013927, -1191134791, -1291799629, 2063629179, -117376519, -838807858, -1090469953, -553590817, 1895854449, 687876393, -855585331, 1811967084, 318771987, 1677747300, -1694458981, 1660969827, -1660904035, -1073692480, 1258310475, -1224689737, -1526684251, -1996453495, 1593859935, -1325354575, 385881879, -201263884, -1140802372, -754920493, 1174423110, -822030385, 922761015, 1577082462, 1191200583, -1811901292, -100599046, -67044100, 1526750043, -1761568873, -33489154, 1509972570, -1409241940, 1006648380, 1275087948, 50332419, 889206069, -218041357, 587211555, -1207912264, 1560304989, 1778412138, -1845456238, -721365547, 553656609, 1140868164, 1358975313, -973027642, 2097184125, 956315961, -2097118333, -603923236, -1442796886, 2080406652, 1996519287, 1442862678, 83887365, 452991771, -1543461724, 352326933, 872428596, 503324190, 469769244, -134153992, 1375752786, 536879136, 335549460, -385816087, -1124024899, -587145763, -469703452, -1593794143, -536813344, -1979676022, -251596303, -704588074, 2046851706, -1157579845, -486480925, 1073758272, 1325420367};
    private static final int[] SBOX4_4404 = {1886388336, 741081132, -1280114509, -1061158720, -454819612, 1465319511, -353763094, -1364328274, 589496355, 1802174571, 1162149957, -1515913051, -303234835, 1330577487, 488439837, -1835925358, -2038038394, -1347485521, 2088501372, 522125343, 1044250686, -589561636, 1583218782, 185270283, -1499070298, 960036921, -707460907, 1566376029, -640089895, 1515847770, 1364262993, 1819017324, -1953824629, -1701183334, -67436293, -1330642768, 1953759348, 724238379, -252706576, -2071723900, -539033377, -875888437, 875823156, 1987444854, 1835860077, -1448542039, -774831919, 67371012, 336855060, 976879674, -555876130, 286326801, 842137650, -1667497828, 1397948499, -219021070, -16908034, -808517425, -1010630461, 2054815866, 606339108, -387448600, 1616904288, 1768489065, -1431699286, -1600126816, -1583284063, 1650589794, 1414791252, 505282590, -522190624, 1684275300, 269484048, 0, -1549598557, 1970602101, -1970667382, -421134106, 151584777, -572718883, -2021195641, -2088566653, -842202931, -1869610864, 1936916595, -151650058, -1650655075, -1078001473, 1381105746, -656932648, -926416696, -960102202, -2122252159, 1869545583, 320012307, 1667432547, -370605847, -1482227545, -1616969569, -1128529732, 690552873, -101121799, 791609391, -1263271756, 2021130360, 101056518, -404291353, 1903231089, -724303660, -1414856533, -2004352888, -1920139123, 1920073842, -1179057991, -117964552, -1398013780, 909508662, 707395626, 1010565180, -235863823, 1077936192, -741146413, -1145372485, 1128464451, 353697813, -1381171027, 2004287607, -2139094912, -2105409406, -320077588, 656867367, -437976859, -2054881147, 892665909, 202113036, 1094778945, -269549329, -1819082605, 421068825, 555810849, 235798542, 1313734734, 1701118053, -1111686979, -1195900744, -1886453617, -336920341, -825360178, 808452144, 1600061535, -976944955, 437911578, -505347871, -892731190, 1195835463, 1027407933, 16842753, -690618154, 1448476758, 1296891981, 218955789, 1717960806, -859045684, 757923885, 303169554, 538968096, -1313800015, -1718026087, 1280049228, -1027473214, 2122186878, 84213765, -1212743497, 825294897, 387383319, -673775401, 1482162264, 1633747041, 454754331, 471597084, 252641295, 370540566, 404226072, 572653602, 1145307204, -1296957262, -1246429003, -1852768111, 134742024, -1465384792, -50593540, 1347420240, -791674672, 2105344125, -1987510135, -1751711593, 1532690523, -1785397099, -65281, -757989166, -993787708, 1212678216, -134807305, -606404389, 50528259, -623247142, 1061093439, -1802239852, 1549533276, 33685506, 1246363722, 858980403, 1734803559, -202178317, 2139029631, -488505118, -1684340581, 640024614, 926351415, 993722427, -1768554346, 1263206475, -1094844226, 774766638, 2037973113, -1936981876, 1852702830, -1903296370, -168492811, -1229586250, -33750787, 1499005017, -1734868840, 1785331818, 1178992710, -1162215238, 623181861, 1111621698, -1566441310, -84279046, 117899271, 1431634005, -286392082, 168427530, 1229520969, 1751646312, 943194168, -1532755804, 673710120, 2071658619, -909573943, -1044315967, -471662365, -185335564, -943259449, -1633812322};
    private static final int[] SIGMA = {-1600231809, 1003262091, -1233459112, 1286239154, -957401297, -380665154, 1426019237, -237801700, 283453434, -563598051, -1336506174, -1276722691};
    private boolean _keyIs128;
    private boolean initialised = false;
    private int[] ke = new int[12];
    private int[] kw = new int[8];
    private int[] state = new int[4];
    private int[] subkey = new int[96];

    private int bytes2int(byte[] bArr, int i) {
        int i2 = 0;
        for (int i3 = 0; i3 < 4; i3++) {
            i2 = (i2 << 8) + (bArr[i3 + i] & 255);
        }
        return i2;
    }

    private void camelliaF2(int[] iArr, int[] iArr2, int i) {
        int i2 = iArr[0] ^ iArr2[i + 0];
        int[] iArr3 = SBOX4_4404;
        int i3 = iArr3[i2 & 255];
        int[] iArr4 = SBOX3_3033;
        int i4 = i3 ^ iArr4[(i2 >>> 8) & 255];
        int[] iArr5 = SBOX2_0222;
        int i5 = i4 ^ iArr5[(i2 >>> 16) & 255];
        int[] iArr6 = SBOX1_1110;
        int i6 = iArr6[(i2 >>> 24) & 255] ^ i5;
        int i7 = iArr[1] ^ iArr2[i + 1];
        int i8 = (((iArr3[(i7 >>> 8) & 255] ^ iArr6[i7 & 255]) ^ iArr4[(i7 >>> 16) & 255]) ^ iArr5[(i7 >>> 24) & 255]) ^ i6;
        iArr[2] = iArr[2] ^ i8;
        iArr[3] = (rightRotate(i6, 8) ^ i8) ^ iArr[3];
        int i9 = iArr[2] ^ iArr2[i + 2];
        int[] iArr7 = SBOX4_4404;
        int i10 = iArr7[i9 & 255];
        int[] iArr8 = SBOX3_3033;
        int i11 = i10 ^ iArr8[(i9 >>> 8) & 255];
        int[] iArr9 = SBOX2_0222;
        int i12 = i11 ^ iArr9[(i9 >>> 16) & 255];
        int[] iArr10 = SBOX1_1110;
        int i13 = iArr10[(i9 >>> 24) & 255] ^ i12;
        int i14 = iArr2[i + 3] ^ iArr[3];
        int i15 = (iArr9[(i14 >>> 24) & 255] ^ ((iArr10[i14 & 255] ^ iArr7[(i14 >>> 8) & 255]) ^ iArr8[(i14 >>> 16) & 255])) ^ i13;
        iArr[0] = iArr[0] ^ i15;
        iArr[1] = (i15 ^ rightRotate(i13, 8)) ^ iArr[1];
    }

    private void camelliaFLs(int[] iArr, int[] iArr2, int i) {
        iArr[1] = iArr[1] ^ leftRotate(iArr[0] & iArr2[i + 0], 1);
        iArr[0] = iArr[0] ^ (iArr2[i + 1] | iArr[1]);
        iArr[2] = iArr[2] ^ (iArr2[i + 3] | iArr[3]);
        iArr[3] = leftRotate(iArr2[i + 2] & iArr[2], 1) ^ iArr[3];
    }

    private static void decroldq(int i, int[] iArr, int i2, int[] iArr2, int i3) {
        int i4 = i3 + 2;
        int i5 = i2 + 0;
        int i6 = i2 + 1;
        int i7 = 32 - i;
        iArr2[i4] = (iArr[i5] << i) | (iArr[i6] >>> i7);
        int i8 = i3 + 3;
        int i9 = i2 + 2;
        iArr2[i8] = (iArr[i6] << i) | (iArr[i9] >>> i7);
        int i10 = i3 + 0;
        int i11 = i2 + 3;
        iArr2[i10] = (iArr[i9] << i) | (iArr[i11] >>> i7);
        int i12 = i3 + 1;
        iArr2[i12] = (iArr[i11] << i) | (iArr[i5] >>> i7);
        iArr[i5] = iArr2[i4];
        iArr[i6] = iArr2[i8];
        iArr[i9] = iArr2[i10];
        iArr[i11] = iArr2[i12];
    }

    private static void decroldqo32(int i, int[] iArr, int i2, int[] iArr2, int i3) {
        int i4 = i3 + 2;
        int i5 = i2 + 1;
        int i6 = i - 32;
        int i7 = i2 + 2;
        int i8 = 64 - i;
        iArr2[i4] = (iArr[i5] << i6) | (iArr[i7] >>> i8);
        int i9 = i3 + 3;
        int i10 = i2 + 3;
        iArr2[i9] = (iArr[i7] << i6) | (iArr[i10] >>> i8);
        int i11 = i3 + 0;
        int i12 = i2 + 0;
        iArr2[i11] = (iArr[i10] << i6) | (iArr[i12] >>> i8);
        int i13 = i3 + 1;
        iArr2[i13] = (iArr[i5] >>> i8) | (iArr[i12] << i6);
        iArr[i12] = iArr2[i4];
        iArr[i5] = iArr2[i9];
        iArr[i7] = iArr2[i11];
        iArr[i10] = iArr2[i13];
    }

    private void int2bytes(int i, byte[] bArr, int i2) {
        for (int i3 = 0; i3 < 4; i3++) {
            bArr[(3 - i3) + i2] = (byte) i;
            i >>>= 8;
        }
    }

    private static int leftRotate(int i, int i2) {
        return (i << i2) + (i >>> (32 - i2));
    }

    private int processBlock128(byte[] bArr, int i, byte[] bArr2, int i2) {
        for (int i3 = 0; i3 < 4; i3++) {
            this.state[i3] = bytes2int(bArr, (i3 * 4) + i);
            int[] iArr = this.state;
            iArr[i3] = iArr[i3] ^ this.kw[i3];
        }
        camelliaF2(this.state, this.subkey, 0);
        camelliaF2(this.state, this.subkey, 4);
        camelliaF2(this.state, this.subkey, 8);
        camelliaFLs(this.state, this.ke, 0);
        camelliaF2(this.state, this.subkey, 12);
        camelliaF2(this.state, this.subkey, 16);
        camelliaF2(this.state, this.subkey, 20);
        camelliaFLs(this.state, this.ke, 4);
        camelliaF2(this.state, this.subkey, 24);
        camelliaF2(this.state, this.subkey, 28);
        camelliaF2(this.state, this.subkey, 32);
        int[] iArr2 = this.state;
        int i4 = iArr2[2];
        int[] iArr3 = this.kw;
        iArr2[2] = iArr3[4] ^ i4;
        iArr2[3] = iArr2[3] ^ iArr3[5];
        iArr2[0] = iArr2[0] ^ iArr3[6];
        iArr2[1] = iArr3[7] ^ iArr2[1];
        int2bytes(iArr2[2], bArr2, i2);
        int2bytes(this.state[3], bArr2, i2 + 4);
        int2bytes(this.state[0], bArr2, i2 + 8);
        int2bytes(this.state[1], bArr2, i2 + 12);
        return 16;
    }

    private int processBlock192or256(byte[] bArr, int i, byte[] bArr2, int i2) {
        for (int i3 = 0; i3 < 4; i3++) {
            this.state[i3] = bytes2int(bArr, (i3 * 4) + i);
            int[] iArr = this.state;
            iArr[i3] = iArr[i3] ^ this.kw[i3];
        }
        camelliaF2(this.state, this.subkey, 0);
        camelliaF2(this.state, this.subkey, 4);
        camelliaF2(this.state, this.subkey, 8);
        camelliaFLs(this.state, this.ke, 0);
        camelliaF2(this.state, this.subkey, 12);
        camelliaF2(this.state, this.subkey, 16);
        camelliaF2(this.state, this.subkey, 20);
        camelliaFLs(this.state, this.ke, 4);
        camelliaF2(this.state, this.subkey, 24);
        camelliaF2(this.state, this.subkey, 28);
        camelliaF2(this.state, this.subkey, 32);
        camelliaFLs(this.state, this.ke, 8);
        camelliaF2(this.state, this.subkey, 36);
        camelliaF2(this.state, this.subkey, 40);
        camelliaF2(this.state, this.subkey, 44);
        int[] iArr2 = this.state;
        int i4 = iArr2[2];
        int[] iArr3 = this.kw;
        iArr2[2] = i4 ^ iArr3[4];
        iArr2[3] = iArr2[3] ^ iArr3[5];
        iArr2[0] = iArr2[0] ^ iArr3[6];
        iArr2[1] = iArr3[7] ^ iArr2[1];
        int2bytes(iArr2[2], bArr2, i2);
        int2bytes(this.state[3], bArr2, i2 + 4);
        int2bytes(this.state[0], bArr2, i2 + 8);
        int2bytes(this.state[1], bArr2, i2 + 12);
        return 16;
    }

    private static int rightRotate(int i, int i2) {
        return (i >>> i2) + (i << (32 - i2));
    }

    private static void roldq(int i, int[] iArr, int i2, int[] iArr2, int i3) {
        int i4 = i3 + 0;
        int i5 = i2 + 0;
        int i6 = i2 + 1;
        int i7 = 32 - i;
        iArr2[i4] = (iArr[i5] << i) | (iArr[i6] >>> i7);
        int i8 = i3 + 1;
        int i9 = i2 + 2;
        iArr2[i8] = (iArr[i6] << i) | (iArr[i9] >>> i7);
        int i10 = i3 + 2;
        int i11 = i2 + 3;
        iArr2[i10] = (iArr[i9] << i) | (iArr[i11] >>> i7);
        int i12 = i3 + 3;
        iArr2[i12] = (iArr[i11] << i) | (iArr[i5] >>> i7);
        iArr[i5] = iArr2[i4];
        iArr[i6] = iArr2[i8];
        iArr[i9] = iArr2[i10];
        iArr[i11] = iArr2[i12];
    }

    private static void roldqo32(int i, int[] iArr, int i2, int[] iArr2, int i3) {
        int i4 = i3 + 0;
        int i5 = i2 + 1;
        int i6 = i - 32;
        int i7 = i2 + 2;
        int i8 = 64 - i;
        iArr2[i4] = (iArr[i5] << i6) | (iArr[i7] >>> i8);
        int i9 = i3 + 1;
        int i10 = i2 + 3;
        iArr2[i9] = (iArr[i7] << i6) | (iArr[i10] >>> i8);
        int i11 = i3 + 2;
        int i12 = i2 + 0;
        iArr2[i11] = (iArr[i10] << i6) | (iArr[i12] >>> i8);
        int i13 = i3 + 3;
        iArr2[i13] = (iArr[i5] >>> i8) | (iArr[i12] << i6);
        iArr[i12] = iArr2[i4];
        iArr[i5] = iArr2[i9];
        iArr[i7] = iArr2[i11];
        iArr[i10] = iArr2[i13];
    }

    private void setKey(boolean z, byte[] bArr) {
        int[] iArr = new int[8];
        int[] iArr2 = new int[4];
        int[] iArr3 = new int[4];
        int[] iArr4 = new int[4];
        int length = bArr.length;
        if (length != 16) {
            if (length == 24) {
                iArr[0] = bytes2int(bArr, 0);
                iArr[1] = bytes2int(bArr, 4);
                iArr[2] = bytes2int(bArr, 8);
                iArr[3] = bytes2int(bArr, 12);
                iArr[4] = bytes2int(bArr, 16);
                iArr[5] = bytes2int(bArr, 20);
                iArr[6] = ~iArr[4];
                iArr[7] = ~iArr[5];
            } else if (length == 32) {
                iArr[0] = bytes2int(bArr, 0);
                iArr[1] = bytes2int(bArr, 4);
                iArr[2] = bytes2int(bArr, 8);
                iArr[3] = bytes2int(bArr, 12);
                iArr[4] = bytes2int(bArr, 16);
                iArr[5] = bytes2int(bArr, 20);
                iArr[6] = bytes2int(bArr, 24);
                iArr[7] = bytes2int(bArr, 28);
            } else {
                throw new IllegalArgumentException("key sizes are only 16/24/32 bytes.");
            }
            this._keyIs128 = false;
        } else {
            this._keyIs128 = true;
            iArr[0] = bytes2int(bArr, 0);
            iArr[1] = bytes2int(bArr, 4);
            iArr[2] = bytes2int(bArr, 8);
            iArr[3] = bytes2int(bArr, 12);
            iArr[7] = 0;
            iArr[6] = 0;
            iArr[5] = 0;
            iArr[4] = 0;
        }
        for (int i = 0; i < 4; i++) {
            iArr2[i] = iArr[i] ^ iArr[i + 4];
        }
        camelliaF2(iArr2, SIGMA, 0);
        for (int i2 = 0; i2 < 4; i2++) {
            iArr2[i2] = iArr2[i2] ^ iArr[i2];
        }
        camelliaF2(iArr2, SIGMA, 4);
        if (this._keyIs128) {
            int[] iArr5 = this.kw;
            if (z) {
                iArr5[0] = iArr[0];
                iArr5[1] = iArr[1];
                iArr5[2] = iArr[2];
                iArr5[3] = iArr[3];
                roldq(15, iArr, 0, this.subkey, 4);
                roldq(30, iArr, 0, this.subkey, 12);
                roldq(15, iArr, 0, iArr4, 0);
                int[] iArr6 = this.subkey;
                iArr6[18] = iArr4[2];
                iArr6[19] = iArr4[3];
                roldq(17, iArr, 0, this.ke, 4);
                roldq(17, iArr, 0, this.subkey, 24);
                roldq(17, iArr, 0, this.subkey, 32);
                int[] iArr7 = this.subkey;
                iArr7[0] = iArr2[0];
                iArr7[1] = iArr2[1];
                iArr7[2] = iArr2[2];
                iArr7[3] = iArr2[3];
                roldq(15, iArr2, 0, iArr7, 8);
                roldq(15, iArr2, 0, this.ke, 0);
                roldq(15, iArr2, 0, iArr4, 0);
                int[] iArr8 = this.subkey;
                iArr8[16] = iArr4[0];
                iArr8[17] = iArr4[1];
                roldq(15, iArr2, 0, iArr8, 20);
                roldqo32(34, iArr2, 0, this.subkey, 28);
                roldq(17, iArr2, 0, this.kw, 4);
                return;
            }
            iArr5[4] = iArr[0];
            iArr5[5] = iArr[1];
            iArr5[6] = iArr[2];
            iArr5[7] = iArr[3];
            decroldq(15, iArr, 0, this.subkey, 28);
            decroldq(30, iArr, 0, this.subkey, 20);
            decroldq(15, iArr, 0, iArr4, 0);
            int[] iArr9 = this.subkey;
            iArr9[16] = iArr4[0];
            iArr9[17] = iArr4[1];
            decroldq(17, iArr, 0, this.ke, 0);
            decroldq(17, iArr, 0, this.subkey, 8);
            decroldq(17, iArr, 0, this.subkey, 0);
            int[] iArr10 = this.subkey;
            iArr10[34] = iArr2[0];
            iArr10[35] = iArr2[1];
            iArr10[32] = iArr2[2];
            iArr10[33] = iArr2[3];
            decroldq(15, iArr2, 0, iArr10, 24);
            decroldq(15, iArr2, 0, this.ke, 4);
            decroldq(15, iArr2, 0, iArr4, 0);
            int[] iArr11 = this.subkey;
            iArr11[18] = iArr4[2];
            iArr11[19] = iArr4[3];
            decroldq(15, iArr2, 0, iArr11, 12);
            decroldqo32(34, iArr2, 0, this.subkey, 4);
            roldq(17, iArr2, 0, this.kw, 0);
            return;
        }
        for (int i3 = 0; i3 < 4; i3++) {
            iArr3[i3] = iArr2[i3] ^ iArr[i3 + 4];
        }
        camelliaF2(iArr3, SIGMA, 8);
        int[] iArr12 = this.kw;
        if (z) {
            iArr12[0] = iArr[0];
            iArr12[1] = iArr[1];
            iArr12[2] = iArr[2];
            iArr12[3] = iArr[3];
            roldqo32(45, iArr, 0, this.subkey, 16);
            roldq(15, iArr, 0, this.ke, 4);
            roldq(17, iArr, 0, this.subkey, 32);
            roldqo32(34, iArr, 0, this.subkey, 44);
            roldq(15, iArr, 4, this.subkey, 4);
            roldq(15, iArr, 4, this.ke, 0);
            roldq(30, iArr, 4, this.subkey, 24);
            roldqo32(34, iArr, 4, this.subkey, 36);
            roldq(15, iArr2, 0, this.subkey, 8);
            roldq(30, iArr2, 0, this.subkey, 20);
            int[] iArr13 = this.ke;
            iArr13[8] = iArr2[1];
            iArr13[9] = iArr2[2];
            iArr13[10] = iArr2[3];
            iArr13[11] = iArr2[0];
            roldqo32(49, iArr2, 0, this.subkey, 40);
            int[] iArr14 = this.subkey;
            iArr14[0] = iArr3[0];
            iArr14[1] = iArr3[1];
            iArr14[2] = iArr3[2];
            iArr14[3] = iArr3[3];
            roldq(30, iArr3, 0, iArr14, 12);
            roldq(30, iArr3, 0, this.subkey, 28);
            roldqo32(51, iArr3, 0, this.kw, 4);
            return;
        }
        iArr12[4] = iArr[0];
        iArr12[5] = iArr[1];
        iArr12[6] = iArr[2];
        iArr12[7] = iArr[3];
        decroldqo32(45, iArr, 0, this.subkey, 28);
        decroldq(15, iArr, 0, this.ke, 4);
        decroldq(17, iArr, 0, this.subkey, 12);
        decroldqo32(34, iArr, 0, this.subkey, 0);
        decroldq(15, iArr, 4, this.subkey, 40);
        decroldq(15, iArr, 4, this.ke, 8);
        decroldq(30, iArr, 4, this.subkey, 20);
        decroldqo32(34, iArr, 4, this.subkey, 8);
        decroldq(15, iArr2, 0, this.subkey, 36);
        decroldq(30, iArr2, 0, this.subkey, 24);
        int[] iArr15 = this.ke;
        iArr15[2] = iArr2[1];
        iArr15[3] = iArr2[2];
        iArr15[0] = iArr2[3];
        iArr15[1] = iArr2[0];
        decroldqo32(49, iArr2, 0, this.subkey, 4);
        int[] iArr16 = this.subkey;
        iArr16[46] = iArr3[0];
        iArr16[47] = iArr3[1];
        iArr16[44] = iArr3[2];
        iArr16[45] = iArr3[3];
        decroldq(30, iArr3, 0, iArr16, 32);
        decroldq(30, iArr3, 0, this.subkey, 16);
        roldqo32(51, iArr3, 0, this.kw, 0);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return "Camellia";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return 16;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof KeyParameter) {
            setKey(z, ((KeyParameter) cipherParameters).getKey());
            this.initialised = true;
            return;
        }
        throw new IllegalArgumentException("only simple KeyParameter expected.");
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (!this.initialised) {
            throw new IllegalStateException("Camellia engine not initialised");
        } else if (i + 16 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i2 + 16 <= bArr2.length) {
            return this._keyIs128 ? processBlock128(bArr, i, bArr2, i2) : processBlock192or256(bArr, i, bArr2, i2);
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
    }
}
