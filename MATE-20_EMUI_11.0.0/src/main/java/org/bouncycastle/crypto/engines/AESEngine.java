package org.bouncycastle.crypto.engines;

import java.lang.reflect.Array;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.math.ec.Tnaf;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class AESEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final byte[] S = {99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118, -54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64, -73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21, 4, -57, 35, -61, 24, -106, 5, -102, 7, 18, Byte.MIN_VALUE, -30, -21, 39, -78, 117, 9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124, 83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49, -48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, Byte.MAX_VALUE, 80, 60, -97, -88, 81, -93, 64, -113, -110, -99, 56, -11, PSSSigner.TRAILER_IMPLICIT, -74, -38, 33, Tnaf.POW_2_WIDTH, -1, -13, -46, -51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115, 96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37, -32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121, -25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8, -70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118, 112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98, -31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33, -116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22};
    private static final byte[] Si = {82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5, 124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53, 84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78, 8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37, 114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110, 108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124, -112, -40, -85, 0, -116, PSSSigner.TRAILER_IMPLICIT, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6, -48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107, 58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115, -106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110, 71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27, -4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12, 31, -35, -88, 51, -120, 7, -57, 49, -79, 18, Tnaf.POW_2_WIDTH, 89, 39, Byte.MIN_VALUE, -20, 95, 96, 81, Byte.MAX_VALUE, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17, -96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97, 23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125};
    private static final int[] T0 = {-1520213050, -2072216328, -1720223762, -1921287178, 234025727, -1117033514, -1318096930, 1422247313, 1345335392, 50397442, -1452841010, 2099981142, 436141799, 1658312629, -424957107, -1703512340, 1170918031, -1652391393, 1086966153, -2021818886, 368769775, -346465870, -918075506, 200339707, -324162239, 1742001331, -39673249, -357585083, -1080255453, -140204973, -1770884380, 1539358875, -1028147339, 486407649, -1366060227, 1780885068, 1513502316, 1094664062, 49805301, 1338821763, 1546925160, -190470831, 887481809, 150073849, -1821281822, 1943591083, 1395732834, 1058346282, 201589768, 1388824469, 1696801606, 1589887901, 672667696, -1583966665, 251987210, -1248159185, 151455502, 907153956, -1686077413, 1038279391, 652995533, 1764173646, -843926913, -1619692054, 453576978, -1635548387, 1949051992, 773462580, 756751158, -1301385508, -296068428, -73359269, -162377052, 1295727478, 1641469623, -827083907, 2066295122, 1055122397, 1898917726, -1752923117, -179088474, 1758581177, 0, 753790401, 1612718144, 536673507, -927878791, -312779850, -1100322092, 1187761037, -641810841, 1262041458, -565556588, -733197160, -396863312, 1255133061, 1808847035, 720367557, -441800113, 385612781, -985447546, -682799718, 1429418854, -1803188975, -817543798, 284817897, 100794884, -2122350594, -263171936, 1144798328, -1163944155, -475486133, -212774494, -22830243, -1069531008, -1970303227, -1382903233, -1130521311, 1211644016, 83228145, -541279133, -1044990345, 1977277103, 1663115586, 806359072, 452984805, 250868733, 1842533055, 1288555905, 336333848, 890442534, 804056259, -513843266, -1567123659, -867941240, 957814574, 1472513171, -223893675, -2105639172, 1195195770, -1402706744, -413311558, 723065138, -1787595802, -1604296512, -1736343271, -783331426, 2145180835, 1713513028, 2116692564, -1416589253, -2088204277, -901364084, 703524551, -742868885, 1007948840, 2044649127, -497131844, 487262998, 1994120109, 1004593371, 1446130276, 1312438900, 503974420, -615954030, 168166924, 1814307912, -463709000, 1573044895, 1859376061, -273896381, -1503501628, -1466855111, -1533700815, 937747667, -1954973198, 854058965, 1137232011, 1496790894, -1217565222, -1936880383, 1691735473, -766620004, -525751991, -1267962664, -95005012, 133494003, 636152527, -1352309302, -1904575756, -374428089, 403179536, -709182865, -2005370640, 1864705354, 1915629148, 605822008, -240736681, -944458637, 1371981463, 602466507, 2094914977, -1670089496, 555687742, -582268010, -591544991, -2037675251, -2054518257, -1871679264, 1111375484, -994724495, -1436129588, -666351472, 84083462, 32962295, 302911004, -1553899070, 1597322602, -111716434, -793134743, -1853454825, 1489093017, 656219450, -1180787161, 954327513, 335083755, -1281845205, 856756514, -1150719534, 1893325225, -1987146233, -1483434957, -1231316179, 572399164, -1836611819, 552200649, 1238290055, -11184726, 2015897680, 2061492133, -1886614525, -123625127, -2138470135, 386731290, -624967835, 837215959, -968736124, -1201116976, -1019133566, -1332111063, 1999449434, 286199582, -877612933, -61582168, -692339859, 974525996};
    private static final int[] Tinv0 = {1353184337, 1399144830, -1012656358, -1772214470, -882136261, -247096033, -1420232020, -1828461749, 1442459680, -160598355, -1854485368, 625738485, -52959921, -674551099, -2143013594, -1885117771, 1230680542, 1729870373, -1743852987, -507445667, 41234371, 317738113, -1550367091, -956705941, -413167869, -1784901099, -344298049, -631680363, 763608788, -752782248, 694804553, 1154009486, 1787413109, 2021232372, 1799248025, -579749593, -1236278850, 397248752, 1722556617, -1271214467, 407560035, -2110711067, 1613975959, 1165972322, -529046351, -2068943941, 480281086, -1809118983, 1483229296, 436028815, -2022908268, -1208452270, 601060267, -503166094, 1468997603, 715871590, 120122290, 63092015, -1703164538, -1526188077, -226023376, -1297760477, -1167457534, 1552029421, 723308426, -1833666137, -252573709, -1578997426, -839591323, -708967162, 526529745, -1963022652, -1655493068, -1604979806, 853641733, 1978398372, 971801355, -1427152832, 111112542, 1360031421, -108388034, 1023860118, -1375387939, 1186850381, -1249028975, 90031217, 1876166148, -15380384, 620468249, -1746289194, -868007799, 2006899047, -1119688528, -2004121337, 945494503, -605108103, 1191869601, -384875908, -920746760, 0, -2088337399, 1223502642, -1401941730, 1316117100, -67170563, 1446544655, 517320253, 658058550, 1691946762, 564550760, -783000677, 976107044, -1318647284, 266819475, -761860428, -1634624741, 1338359936, -1574904735, 1766553434, 370807324, 179999714, -450191168, 1138762300, 488053522, 185403662, -1379431438, -1180125651, -928440812, -2061897385, 1275557295, -1143105042, -44007517, -1624899081, -1124765092, -985962940, 880737115, 1982415755, -590994485, 1761406390, 1676797112, -891538985, 277177154, 1076008723, 538035844, 2099530373, -130171950, 288553390, 1839278535, 1261411869, -214912292, -330136051, -790380169, 1813426987, -1715900247, -95906799, 577038663, -997393240, 440397984, -668172970, -275762398, -951170681, -1043253031, -22885748, 906744984, -813566554, 685669029, 646887386, -1530942145, -459458004, 227702864, -1681105046, 1648787028, -1038905866, -390539120, 1593260334, -173030526, -1098883681, 2090061929, -1456614033, -1290656305, 999926984, -1484974064, 1852021992, 2075868123, 158869197, -199730834, 28809964, -1466282109, 1701746150, 2129067946, 147831841, -420997649, -644094022, -835293366, -737566742, -696471511, -1347247055, 824393514, 815048134, -1067015627, 935087732, -1496677636, -1328508704, 366520115, 1251476721, -136647615, 240176511, 804688151, -1915335306, 1303441219, 1414376140, -553347356, -474623586, 461924940, -1205916479, 2136040774, 82468509, 1563790337, 1937016826, 776014843, 1511876531, 1389550482, 861278441, 323475053, -1939744870, 2047648055, -1911228327, -1992551445, -299390514, 902390199, -303751967, 1018251130, 1507840668, 1064563285, 2043548696, -1086863501, -355600557, 1537932639, 342834655, -2032450440, -2114736182, 1053059257, 741614648, 1598071746, 1925389590, 203809468, -1958134744, 1100287487, 1895934009, -558691320, -1662733096, -1866377628, 1636092795, 1890988757, 1952214088, 1113045200};
    private static final int m1 = -2139062144;
    private static final int m2 = 2139062143;
    private static final int m3 = 27;
    private static final int m4 = -1061109568;
    private static final int m5 = 1061109567;
    private static final int[] rcon = {1, 2, 4, 8, 16, 32, 64, 128, 27, 54, 108, 216, 171, 77, 154, 47, 94, 188, 99, 198, 151, 53, 106, 212, 179, 125, 250, 239, 197, 145};
    private int C0;
    private int C1;
    private int C2;
    private int C3;
    private int ROUNDS;
    private int[][] WorkingKey = null;
    private boolean forEncryption;
    private byte[] s;

    private static int FFmulX(int i) {
        return (((i & m1) >>> 7) * 27) ^ ((m2 & i) << 1);
    }

    private static int FFmulX2(int i) {
        int i2 = i & m4;
        int i3 = i2 ^ (i2 >>> 1);
        return (i3 >>> 5) ^ (((m5 & i) << 2) ^ (i3 >>> 2));
    }

    private void decryptBlock(int[][] iArr) {
        int i = this.C0;
        int i2 = this.ROUNDS;
        char c = 0;
        int i3 = i ^ iArr[i2][0];
        int i4 = 1;
        int i5 = this.C1 ^ iArr[i2][1];
        int i6 = this.C2 ^ iArr[i2][2];
        int i7 = i2 - 1;
        int i8 = iArr[i2][3] ^ this.C3;
        while (i7 > i4) {
            int[] iArr2 = Tinv0;
            int shift = (((shift(iArr2[(i8 >> 8) & GF2Field.MASK], 24) ^ iArr2[i3 & GF2Field.MASK]) ^ shift(Tinv0[(i6 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i5 >> 24) & GF2Field.MASK], 8)) ^ iArr[i7][c];
            int[] iArr3 = Tinv0;
            int shift2 = (((shift(iArr3[(i3 >> 8) & GF2Field.MASK], 24) ^ iArr3[i5 & GF2Field.MASK]) ^ shift(Tinv0[(i8 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i6 >> 24) & GF2Field.MASK], 8)) ^ iArr[i7][i4];
            int[] iArr4 = Tinv0;
            int shift3 = (((shift(iArr4[(i5 >> 8) & GF2Field.MASK], 24) ^ iArr4[i6 & GF2Field.MASK]) ^ shift(Tinv0[(i3 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i8 >> 24) & GF2Field.MASK], 8)) ^ iArr[i7][2];
            int[] iArr5 = Tinv0;
            int shift4 = shift(Tinv0[(i3 >> 24) & GF2Field.MASK], 8) ^ ((iArr5[i8 & GF2Field.MASK] ^ shift(iArr5[(i6 >> 8) & GF2Field.MASK], 24)) ^ shift(Tinv0[(i5 >> 16) & GF2Field.MASK], 16));
            int i9 = i7 - 1;
            int i10 = shift4 ^ iArr[i7][3];
            int[] iArr6 = Tinv0;
            int shift5 = (((shift(iArr6[(i10 >> 8) & GF2Field.MASK], 24) ^ iArr6[shift & GF2Field.MASK]) ^ shift(Tinv0[(shift3 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(shift2 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][0];
            int[] iArr7 = Tinv0;
            int shift6 = (((shift(iArr7[(shift >> 8) & GF2Field.MASK], 24) ^ iArr7[shift2 & GF2Field.MASK]) ^ shift(Tinv0[(i10 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(shift3 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][1];
            int[] iArr8 = Tinv0;
            int shift7 = (((shift(iArr8[(shift2 >> 8) & GF2Field.MASK], 24) ^ iArr8[shift3 & GF2Field.MASK]) ^ shift(Tinv0[(shift >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i10 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][2];
            int[] iArr9 = Tinv0;
            int shift8 = ((iArr9[i10 & GF2Field.MASK] ^ shift(iArr9[(shift3 >> 8) & GF2Field.MASK], 24)) ^ shift(Tinv0[(shift2 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(shift >> 24) & GF2Field.MASK], 8);
            int i11 = i9 - 1;
            i8 = iArr[i9][3] ^ shift8;
            i3 = shift5;
            i5 = shift6;
            i6 = shift7;
            i4 = 1;
            i7 = i11;
            c = 0;
        }
        int[] iArr10 = Tinv0;
        int shift9 = (((shift(iArr10[(i8 >> 8) & GF2Field.MASK], 24) ^ iArr10[i3 & GF2Field.MASK]) ^ shift(Tinv0[(i6 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i5 >> 24) & GF2Field.MASK], 8)) ^ iArr[i7][0];
        int[] iArr11 = Tinv0;
        int shift10 = (((shift(iArr11[(i3 >> 8) & GF2Field.MASK], 24) ^ iArr11[i5 & GF2Field.MASK]) ^ shift(Tinv0[(i8 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i6 >> 24) & GF2Field.MASK], 8)) ^ iArr[i7][1];
        int[] iArr12 = Tinv0;
        int shift11 = (((shift(iArr12[(i5 >> 8) & GF2Field.MASK], 24) ^ iArr12[i6 & GF2Field.MASK]) ^ shift(Tinv0[(i3 >> 16) & GF2Field.MASK], 16)) ^ shift(Tinv0[(i8 >> 24) & GF2Field.MASK], 8)) ^ iArr[i7][2];
        int[] iArr13 = Tinv0;
        int shift12 = (shift(Tinv0[(i3 >> 24) & GF2Field.MASK], 8) ^ ((iArr13[i8 & GF2Field.MASK] ^ shift(iArr13[(i6 >> 8) & GF2Field.MASK], 24)) ^ shift(Tinv0[(i5 >> 16) & GF2Field.MASK], 16))) ^ iArr[i7][3];
        byte[] bArr = Si;
        byte[] bArr2 = this.s;
        this.C0 = ((((bArr[shift9 & GF2Field.MASK] & 255) ^ ((bArr2[(shift12 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr2[(shift11 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr[(shift10 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][0];
        this.C1 = ((((bArr2[shift10 & GF2Field.MASK] & 255) ^ ((bArr2[(shift9 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(shift12 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr2[(shift11 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][1];
        this.C2 = ((((bArr2[shift11 & GF2Field.MASK] & 255) ^ ((bArr[(shift10 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(shift9 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr2[(shift12 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][2];
        this.C3 = ((((bArr[shift12 & GF2Field.MASK] & 255) ^ ((bArr2[(shift11 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr2[(shift10 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr2[(shift9 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][3];
    }

    private void encryptBlock(int[][] iArr) {
        char c = 0;
        int i = this.C0 ^ iArr[0][0];
        int i2 = 1;
        int i3 = this.C1 ^ iArr[0][1];
        int i4 = this.C2 ^ iArr[0][2];
        int i5 = this.C3 ^ iArr[0][3];
        int i6 = i4;
        int i7 = i3;
        int i8 = i;
        int i9 = 1;
        while (i9 < this.ROUNDS - i2) {
            int[] iArr2 = T0;
            int shift = (((shift(iArr2[(i7 >> 8) & GF2Field.MASK], 24) ^ iArr2[i8 & GF2Field.MASK]) ^ shift(T0[(i6 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i5 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][c];
            int[] iArr3 = T0;
            int shift2 = (((shift(iArr3[(i6 >> 8) & GF2Field.MASK], 24) ^ iArr3[i7 & GF2Field.MASK]) ^ shift(T0[(i5 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i8 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][i2];
            int[] iArr4 = T0;
            int shift3 = (((shift(iArr4[(i5 >> 8) & GF2Field.MASK], 24) ^ iArr4[i6 & GF2Field.MASK]) ^ shift(T0[(i8 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i7 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][2];
            int[] iArr5 = T0;
            int shift4 = ((shift(iArr5[(i8 >> 8) & GF2Field.MASK], 24) ^ iArr5[i5 & GF2Field.MASK]) ^ shift(T0[(i7 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i6 >> 24) & GF2Field.MASK], 8);
            int i10 = i9 + 1;
            int i11 = iArr[i9][3] ^ shift4;
            int[] iArr6 = T0;
            i8 = (((shift(iArr6[(shift2 >> 8) & GF2Field.MASK], 24) ^ iArr6[shift & GF2Field.MASK]) ^ shift(T0[(shift3 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i11 >> 24) & GF2Field.MASK], 8)) ^ iArr[i10][0];
            int[] iArr7 = T0;
            int shift5 = (((shift(iArr7[(shift3 >> 8) & GF2Field.MASK], 24) ^ iArr7[shift2 & GF2Field.MASK]) ^ shift(T0[(i11 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(shift >> 24) & GF2Field.MASK], 8)) ^ iArr[i10][1];
            int[] iArr8 = T0;
            int shift6 = (((shift(iArr8[(i11 >> 8) & GF2Field.MASK], 24) ^ iArr8[shift3 & GF2Field.MASK]) ^ shift(T0[(shift >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(shift2 >> 24) & GF2Field.MASK], 8)) ^ iArr[i10][2];
            int[] iArr9 = T0;
            int shift7 = ((iArr9[i11 & GF2Field.MASK] ^ shift(iArr9[(shift >> 8) & GF2Field.MASK], 24)) ^ shift(T0[(shift2 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(shift3 >> 24) & GF2Field.MASK], 8);
            int i12 = i10 + 1;
            int i13 = shift7 ^ iArr[i10][3];
            i7 = shift5;
            i6 = shift6;
            i2 = 1;
            i5 = i13;
            i9 = i12;
            c = 0;
        }
        int[] iArr10 = T0;
        int shift8 = (((shift(iArr10[(i7 >> 8) & GF2Field.MASK], 24) ^ iArr10[i8 & GF2Field.MASK]) ^ shift(T0[(i6 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i5 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][0];
        int[] iArr11 = T0;
        int shift9 = (((shift(iArr11[(i6 >> 8) & GF2Field.MASK], 24) ^ iArr11[i7 & GF2Field.MASK]) ^ shift(T0[(i5 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i8 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][1];
        int[] iArr12 = T0;
        int shift10 = (((shift(iArr12[(i5 >> 8) & GF2Field.MASK], 24) ^ iArr12[i6 & GF2Field.MASK]) ^ shift(T0[(i8 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i7 >> 24) & GF2Field.MASK], 8)) ^ iArr[i9][2];
        int[] iArr13 = T0;
        int shift11 = ((shift(iArr13[(i8 >> 8) & GF2Field.MASK], 24) ^ iArr13[i5 & GF2Field.MASK]) ^ shift(T0[(i7 >> 16) & GF2Field.MASK], 16)) ^ shift(T0[(i6 >> 24) & GF2Field.MASK], 8);
        int i14 = i9 + 1;
        int i15 = iArr[i9][3] ^ shift11;
        byte[] bArr = S;
        int i16 = (bArr[shift8 & GF2Field.MASK] & 255) ^ ((bArr[(shift9 >> 8) & GF2Field.MASK] & 255) << 8);
        byte[] bArr2 = this.s;
        this.C0 = ((i16 ^ ((bArr2[(shift10 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr2[(i15 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i14][0];
        this.C1 = ((((bArr2[shift9 & GF2Field.MASK] & 255) ^ ((bArr[(shift10 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(i15 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr2[(shift8 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i14][1];
        this.C2 = ((((bArr2[shift10 & GF2Field.MASK] & 255) ^ ((bArr[(i15 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(shift8 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr[(shift9 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i14][2];
        this.C3 = ((((bArr2[i15 & GF2Field.MASK] & 255) ^ ((bArr2[(shift8 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr2[(shift9 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr[(shift10 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i14][3];
    }

    private int[][] generateWorkingKey(byte[] bArr, boolean z) {
        int length = bArr.length;
        if (length < 16 || length > 32 || (length & 7) != 0) {
            throw new IllegalArgumentException("Key length not 128/192/256 bits.");
        }
        int i = length >>> 2;
        this.ROUNDS = i + 6;
        int[][] iArr = (int[][]) Array.newInstance(int.class, this.ROUNDS + 1, 4);
        if (i == 4) {
            int littleEndianToInt = Pack.littleEndianToInt(bArr, 0);
            iArr[0][0] = littleEndianToInt;
            int littleEndianToInt2 = Pack.littleEndianToInt(bArr, 4);
            iArr[0][1] = littleEndianToInt2;
            int littleEndianToInt3 = Pack.littleEndianToInt(bArr, 8);
            iArr[0][2] = littleEndianToInt3;
            int littleEndianToInt4 = Pack.littleEndianToInt(bArr, 12);
            iArr[0][3] = littleEndianToInt4;
            int i2 = littleEndianToInt3;
            int i3 = littleEndianToInt;
            int i4 = littleEndianToInt4;
            for (int i5 = 1; i5 <= 10; i5++) {
                i3 ^= subWord(shift(i4, 8)) ^ rcon[i5 - 1];
                iArr[i5][0] = i3;
                littleEndianToInt2 ^= i3;
                iArr[i5][1] = littleEndianToInt2;
                i2 ^= littleEndianToInt2;
                iArr[i5][2] = i2;
                i4 ^= i2;
                iArr[i5][3] = i4;
            }
        } else if (i == 6) {
            int littleEndianToInt5 = Pack.littleEndianToInt(bArr, 0);
            iArr[0][0] = littleEndianToInt5;
            int littleEndianToInt6 = Pack.littleEndianToInt(bArr, 4);
            iArr[0][1] = littleEndianToInt6;
            int littleEndianToInt7 = Pack.littleEndianToInt(bArr, 8);
            iArr[0][2] = littleEndianToInt7;
            int littleEndianToInt8 = Pack.littleEndianToInt(bArr, 12);
            iArr[0][3] = littleEndianToInt8;
            int littleEndianToInt9 = Pack.littleEndianToInt(bArr, 16);
            iArr[1][0] = littleEndianToInt9;
            int littleEndianToInt10 = Pack.littleEndianToInt(bArr, 20);
            iArr[1][1] = littleEndianToInt10;
            int subWord = littleEndianToInt5 ^ (subWord(shift(littleEndianToInt10, 8)) ^ 1);
            iArr[1][2] = subWord;
            int i6 = littleEndianToInt6 ^ subWord;
            iArr[1][3] = i6;
            int i7 = littleEndianToInt7 ^ i6;
            iArr[2][0] = i7;
            int i8 = littleEndianToInt8 ^ i7;
            iArr[2][1] = i8;
            int i9 = littleEndianToInt9 ^ i8;
            iArr[2][2] = i9;
            int i10 = littleEndianToInt10 ^ i9;
            iArr[2][3] = i10;
            int i11 = i9;
            int i12 = 2;
            int i13 = i8;
            int i14 = i7;
            int i15 = subWord;
            int i16 = i10;
            for (int i17 = 3; i17 < 12; i17 += 3) {
                int subWord2 = subWord(shift(i16, 8)) ^ i12;
                int i18 = i12 << 1;
                int i19 = i15 ^ subWord2;
                iArr[i17][0] = i19;
                int i20 = i6 ^ i19;
                iArr[i17][1] = i20;
                int i21 = i14 ^ i20;
                iArr[i17][2] = i21;
                int i22 = i13 ^ i21;
                iArr[i17][3] = i22;
                int i23 = i11 ^ i22;
                int i24 = i17 + 1;
                iArr[i24][0] = i23;
                int i25 = i16 ^ i23;
                iArr[i24][1] = i25;
                int subWord3 = subWord(shift(i25, 8)) ^ i18;
                i12 = i18 << 1;
                i15 = i19 ^ subWord3;
                iArr[i24][2] = i15;
                i6 = i20 ^ i15;
                iArr[i24][3] = i6;
                i14 = i21 ^ i6;
                int i26 = i17 + 2;
                iArr[i26][0] = i14;
                i13 = i22 ^ i14;
                iArr[i26][1] = i13;
                i11 = i23 ^ i13;
                iArr[i26][2] = i11;
                i16 = i25 ^ i11;
                iArr[i26][3] = i16;
            }
            int subWord4 = (subWord(shift(i16, 8)) ^ i12) ^ i15;
            iArr[12][0] = subWord4;
            int i27 = subWord4 ^ i6;
            iArr[12][1] = i27;
            int i28 = i27 ^ i14;
            iArr[12][2] = i28;
            iArr[12][3] = i28 ^ i13;
        } else if (i == 8) {
            int littleEndianToInt11 = Pack.littleEndianToInt(bArr, 0);
            iArr[0][0] = littleEndianToInt11;
            int littleEndianToInt12 = Pack.littleEndianToInt(bArr, 4);
            iArr[0][1] = littleEndianToInt12;
            int littleEndianToInt13 = Pack.littleEndianToInt(bArr, 8);
            iArr[0][2] = littleEndianToInt13;
            int littleEndianToInt14 = Pack.littleEndianToInt(bArr, 12);
            iArr[0][3] = littleEndianToInt14;
            int littleEndianToInt15 = Pack.littleEndianToInt(bArr, 16);
            iArr[1][0] = littleEndianToInt15;
            int littleEndianToInt16 = Pack.littleEndianToInt(bArr, 20);
            iArr[1][1] = littleEndianToInt16;
            int littleEndianToInt17 = Pack.littleEndianToInt(bArr, 24);
            iArr[1][2] = littleEndianToInt17;
            int littleEndianToInt18 = Pack.littleEndianToInt(bArr, 28);
            iArr[1][3] = littleEndianToInt18;
            int i29 = littleEndianToInt11;
            int i30 = littleEndianToInt17;
            int i31 = littleEndianToInt18;
            int i32 = littleEndianToInt16;
            int i33 = littleEndianToInt15;
            int i34 = 1;
            for (int i35 = 2; i35 < 14; i35 += 2) {
                int subWord5 = subWord(shift(i31, 8)) ^ i34;
                i34 <<= 1;
                i29 ^= subWord5;
                iArr[i35][0] = i29;
                littleEndianToInt12 ^= i29;
                iArr[i35][1] = littleEndianToInt12;
                littleEndianToInt13 ^= littleEndianToInt12;
                iArr[i35][2] = littleEndianToInt13;
                littleEndianToInt14 ^= littleEndianToInt13;
                iArr[i35][3] = littleEndianToInt14;
                i33 ^= subWord(littleEndianToInt14);
                int i36 = i35 + 1;
                iArr[i36][0] = i33;
                i32 ^= i33;
                iArr[i36][1] = i32;
                i30 ^= i32;
                iArr[i36][2] = i30;
                i31 ^= i30;
                iArr[i36][3] = i31;
            }
            int subWord6 = (subWord(shift(i31, 8)) ^ i34) ^ i29;
            iArr[14][0] = subWord6;
            int i37 = subWord6 ^ littleEndianToInt12;
            iArr[14][1] = i37;
            int i38 = i37 ^ littleEndianToInt13;
            iArr[14][2] = i38;
            iArr[14][3] = i38 ^ littleEndianToInt14;
        } else {
            throw new IllegalStateException("Should never get here");
        }
        if (!z) {
            for (int i39 = 1; i39 < this.ROUNDS; i39++) {
                for (int i40 = 0; i40 < 4; i40++) {
                    iArr[i39][i40] = inv_mcol(iArr[i39][i40]);
                }
            }
        }
        return iArr;
    }

    private static int inv_mcol(int i) {
        int shift = shift(i, 8) ^ i;
        int FFmulX = i ^ FFmulX(shift);
        int FFmulX2 = shift ^ FFmulX2(FFmulX);
        return FFmulX ^ (FFmulX2 ^ shift(FFmulX2, 16));
    }

    private void packBlock(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = this.C0;
        bArr[i] = (byte) i3;
        int i4 = i2 + 1;
        bArr[i2] = (byte) (i3 >> 8);
        int i5 = i4 + 1;
        bArr[i4] = (byte) (i3 >> 16);
        int i6 = i5 + 1;
        bArr[i5] = (byte) (i3 >> 24);
        int i7 = i6 + 1;
        int i8 = this.C1;
        bArr[i6] = (byte) i8;
        int i9 = i7 + 1;
        bArr[i7] = (byte) (i8 >> 8);
        int i10 = i9 + 1;
        bArr[i9] = (byte) (i8 >> 16);
        int i11 = i10 + 1;
        bArr[i10] = (byte) (i8 >> 24);
        int i12 = i11 + 1;
        int i13 = this.C2;
        bArr[i11] = (byte) i13;
        int i14 = i12 + 1;
        bArr[i12] = (byte) (i13 >> 8);
        int i15 = i14 + 1;
        bArr[i14] = (byte) (i13 >> 16);
        int i16 = i15 + 1;
        bArr[i15] = (byte) (i13 >> 24);
        int i17 = i16 + 1;
        int i18 = this.C3;
        bArr[i16] = (byte) i18;
        int i19 = i17 + 1;
        bArr[i17] = (byte) (i18 >> 8);
        bArr[i19] = (byte) (i18 >> 16);
        bArr[i19 + 1] = (byte) (i18 >> 24);
    }

    private static int shift(int i, int i2) {
        return (i << (-i2)) | (i >>> i2);
    }

    private static int subWord(int i) {
        byte[] bArr = S;
        return (bArr[(i >> 24) & GF2Field.MASK] << 24) | (bArr[i & GF2Field.MASK] & 255) | ((bArr[(i >> 8) & GF2Field.MASK] & 255) << 8) | ((bArr[(i >> 16) & GF2Field.MASK] & 255) << 16);
    }

    private void unpackBlock(byte[] bArr, int i) {
        int i2 = i + 1;
        this.C0 = bArr[i] & 255;
        int i3 = i2 + 1;
        this.C0 |= (bArr[i2] & 255) << 8;
        int i4 = i3 + 1;
        this.C0 |= (bArr[i3] & 255) << 16;
        int i5 = i4 + 1;
        this.C0 |= bArr[i4] << 24;
        int i6 = i5 + 1;
        this.C1 = bArr[i5] & 255;
        int i7 = i6 + 1;
        this.C1 = ((bArr[i6] & 255) << 8) | this.C1;
        int i8 = i7 + 1;
        this.C1 |= (bArr[i7] & 255) << 16;
        int i9 = i8 + 1;
        this.C1 |= bArr[i8] << 24;
        int i10 = i9 + 1;
        this.C2 = bArr[i9] & 255;
        int i11 = i10 + 1;
        this.C2 = ((bArr[i10] & 255) << 8) | this.C2;
        int i12 = i11 + 1;
        this.C2 |= (bArr[i11] & 255) << 16;
        int i13 = i12 + 1;
        this.C2 |= bArr[i12] << 24;
        int i14 = i13 + 1;
        this.C3 = bArr[i13] & 255;
        int i15 = i14 + 1;
        this.C3 = ((bArr[i14] & 255) << 8) | this.C3;
        this.C3 |= (bArr[i15] & 255) << 16;
        this.C3 = (bArr[i15 + 1] << 24) | this.C3;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return "AES";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return 16;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof KeyParameter) {
            this.WorkingKey = generateWorkingKey(((KeyParameter) cipherParameters).getKey(), z);
            this.forEncryption = z;
            this.s = Arrays.clone(z ? S : Si);
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to AES init - " + cipherParameters.getClass().getName());
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        if (this.WorkingKey == null) {
            throw new IllegalStateException("AES engine not initialised");
        } else if (i + 16 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i2 + 16 <= bArr2.length) {
            boolean z = this.forEncryption;
            unpackBlock(bArr, i);
            int[][] iArr = this.WorkingKey;
            if (z) {
                encryptBlock(iArr);
            } else {
                decryptBlock(iArr);
            }
            packBlock(bArr2, i2);
            return 16;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
    }
}
