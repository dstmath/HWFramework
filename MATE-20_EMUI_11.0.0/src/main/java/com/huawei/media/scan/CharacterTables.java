package com.huawei.media.scan;

class CharacterTables {
    private static final char[] FREQUENT_CN = {19968, 19975, 19977, 19978, 19979, 19981, 19982, 19987, 19990, 19994, 19996, 20004, 20010, 20013, 20026, 20027, 20030, 20040, 20043, 20048, 20063, 20070, 20080, 20102, 20107, 20108, 20110, 20116, 20122, 20123, 20132, 20135, 20139, 20140, 20154, 20160, 20171, 20174, 20182, 20195, 20197, 20204, 20214, 20215, 20219, 20225, 20248, 20250, 20256, 20294, 20301, 20307, 20309, 20316, 20320, 20351, 20379, 20445, 20449, 20462, 20540, 20570, 20581, 20687, 20799, 20803, 20808, 20809, 20811, 20813, 20837, 20840, 20844, 20849, 20851, 20854, 20855, 20869, 20876, 20891, 20986, 20987, 20998, 21015, 21019, 21033, 21035, 21040, 21046, 21069, 21147, 21150, 21151, 21152, 21153, 21160, 21161, 21253, 21270, 21271, 21306, 21307, 21313, 21326, 21333, 21335, 21338, 21345, 21407, 21435, 21442, 21450, 21451, 21453, 21457, 21463, 21464, 21475, 21482, 21487, 21488, 21495, 21496, 21508, 21512, 21516, 21517, 21518, 21521, 21543, 21578, 21592, 21608, 21644, 21697, 21830, 22120, 22235, 22238, 22240, 22242, 22253, 22269, 22270, 22312, 22320, 22330, 22363, 22411, 22478, 22522, 22763, 22768, 22788, 22791, 22797, 22806, 22810, 22823, 22825, 22826, 22836, 22899, 22909, 22914, 23089, 23376, 23383, 23398, 23433, 23436, 23450, 23453, 23454, 23458, 23478, 23481, 23494, 23545, 23548, 23558, 23567, 23569, 23572, 23601, 23637, 23665, 24030, 24037, 24049, 24050, 24066, 24067, 24072, 24086, 24102, 24110, 24120, 24179, 24180, 24182, 24191, 24211, 24212, 24215, 24230, 24247, 24314, 24320, 24335, 24341, 24352, 24378, 24403, 24405, 24418, 24425, 24433, 24456, 24471, 24494, 24515, 24555, 24577, 24590, 24615, 24635, 24687, 24744, 24773, 24819, 24847, 24863, 25103, 25104, 25105, 25110, 25112, 25143, 25151, 25152, 25163, 25165, 25171, 25216, 25237, 25252, 25253, 25307, 25345, 25351, 25454, 25490, 25509, 25512, 25552, 25628, 25773, 25903, 25910, 25918, 25919, 25928, 25945, 25968, 25991, 26009, 26031, 26032, 26041, 26053, 26080, 26085, 26102, 26126, 26131, 26143, 26159, 26174, 26356, 26368, 26376, 26377, 26381, 26399, 26412, 26415, 26426, 26435, 26465, 26469, 26495, 26519, 26524, 26597, 26631, 26657, 26679, 26684, 26696, 27004, 27169, 27425, 27426, 27454, 27491, 27492, 27604, 27665, 27668, 27700, 27714, 27743, 27809, 27835, 27861, 27880, 27963, 27969, 27979, 28023, 28040, 28145, 28165, 28216, 28304, 28779, 28857, 28909, 28982, 29031, 29233, 29255, 29256, 29260, 29289, 29305, 29579, 29609, 29616, 29699, 29702, 29983, 29992, 30001, 30005, 30007, 30028, 30149, 30331, 30333, 30334, 30340, 30424, 30446, 30452, 30456, 30465, 30475, 30495, 30528, 30693, 30721, 30740, 31034, 31038, 31070, 31119, 31163, 31181, 31185, 31215, 31243, 31350, 31354, 31435, 31449, 31456, 31532, 31561, 31572, 31616, 31649, 31867, 31934, 31995, 32034, 32418, 32423, 32447, 32452, 32454, 32463, 32467, 32473, 32476, 32479, 32534, 32593, 32622, 32654, 32769, 32771, 32773, 32780, 32852, 32946, 33021, 33258, 33394, 33410, 33457, 33521, 33616, 33647, 33829, 34255, 34892, 34920, 34987, 35013, 35199, 35201, 35265, 35266, 35268, 35270, 35299, 35328, 35745, 35748, 35753, 35758, 35759, 35760, 35770, 35774, 35777, 35780, 35782, 35797, 35805, 35810, 35813, 35814, 35821, 35828, 35831, 35835, 35843, 36130, 36136, 36141, 36148, 36153, 36164, 36215, 36229, 36335, 36523, 36710, 36716, 36733, 36798, 36807, 36816, 36817, 36824, 36825, 36827, 36830, 36873, 36890, 36895, 36896, 36947, 37027, 37096, 37117, 37197, 37202, 37324, 37325, 37327, 37329, 38144, 38271, 38376, 38382, 38388, 38395, 38405, 38451, 38469, 38480, 38498, 38598, 38656, 38754, 38899, 39029, 39033, 39057, 39064, 39118, 39135, 39318, 39321, 39532, 39564, 39640, 40857};
    private static final char[] FREQUENT_JA = {12293, 12353, 12354, 12356, 12358, 12360, 12362, 12363, 12364, 12365, 12366, 12367, 12368, 12369, 12370, 12371, 12372, 12373, 12374, 12375, 12376, 12377, 12378, 12379, 12381, 12383, 12384, 12385, 12387, 12388, 12390, 12391, 12392, 12393, 12394, 12395, 12397, 12398, 12399, 12400, 12401, 12402, 12403, 12405, 12406, 12408, 12409, 12411, 12414, 12415, 12416, 12417, 12418, 12419, 12420, 12422, 12423, 12424, 12425, 12426, 12427, 12428, 12429, 12431, 12434, 12435, 12449, 12450, 12451, 12452, 12454, 12455, 12456, 12457, 12458, 12459, 12460, 12461, 12462, 12463, 12464, 12465, 12466, 12467, 12468, 12469, 12470, 12471, 12472, 12473, 12474, 12475, 12476, 12477, 12479, 12480, 12481, 12483, 12484, 12486, 12487, 12488, 12489, 12490, 12491, 12493, 12494, 12495, 12496, 12497, 12498, 12499, 12500, 12501, 12502, 12503, 12504, 12505, 12506, 12507, 12508, 12509, 12510, 12511, 12512, 12513, 12514, 12515, 12516, 12517, 12518, 12519, 12521, 12522, 12523, 12524, 12525, 12527, 12531, 12532, 12541, 19968, 19975, 19977, 19978, 19979, 19981, 19990, 20013, 20027, 20104, 20107, 20108, 20117, 20132, 20140, 20154, 20170, 20171, 20181, 20182, 20184, 20195, 20197, 20214, 20250, 20301, 20303, 20307, 20309, 20316, 20351, 20379, 20385, 20415, 20445, 20449, 20491, 20687, 20778, 20803, 20808, 20809, 20837, 20840, 20844, 20855, 20869, 20870, 20889, 20986, 20998, 20999, 21021, 21029, 21033, 21046, 21069, 21147, 21152, 21205, 21209, 21215, 21270, 21271, 21306, 21407, 21442, 21451, 21462, 21463, 21475, 21476, 21487, 21488, 21495, 21512, 21516, 21517, 21521, 21578, 21608, 21619, 21644, 21697, 21729, 21830, 21839, 21942, 22120, 22238, 22259, 22269, 22290, 22303, 22312, 22320, 22411, 22577, 22580, 22770, 22793, 22806, 22810, 22823, 22825, 22899, 22909, 23130, 23376, 23383, 23398, 23433, 23450, 23455, 23460, 23470, 23478, 23481, 23550, 23554, 23567, 23569, 23627, 23665, 23713, 23798, 24029, 24037, 24066, 24111, 24120, 24179, 24180, 24195, 24215, 24230, 24235, 24335, 24341, 24375, 24403, 24418, 24460, 24471, 24515, 24517, 24540, 24565, 24605, 24615, 24693, 24773, 24819, 24847, 24859, 24863, 25104, 25126, 25152, 25163, 25237, 25345, 25351, 25506, 25522, 25658, 25903, 25918, 25945, 25958, 25968, 25991, 26009, 26032, 26041, 26053, 26085, 26126, 26144, 26178, 26356, 26360, 26368, 26376, 26377, 26399, 26408, 26412, 26448, 26449, 26469, 26481, 26495, 26524, 26657, 26666, 26684, 26908, 26989, 27005, 27096, 27231, 27425, 27490, 27491, 27531, 27671, 27700, 27714, 27770, 27835, 27841, 27861, 27880, 27963, 27969, 28023, 28168, 28436, 28779, 28857, 28961, 29256, 29289, 29305, 29366, 29694, 29702, 29983, 29987, 29992, 30000, 30007, 30010, 30011, 30028, 30058, 30149, 30330, 30331, 30333, 30340, 30446, 30452, 30456, 30476, 30495, 30528, 30693, 30707, 30906, 31034, 31038, 31070, 31119, 31169, 31185, 31246, 31278, 31295, 31354, 31435, 31505, 31532, 31561, 31572, 31649, 31995, 32004, 32032, 32034, 32048, 32057, 32066, 32068, 32076, 32080, 32154, 32207, 32218, 32232, 32654, 32771, 32773, 32862, 32887, 32946, 33021, 33145, 33258, 33391, 33394, 33457, 33464, 33521, 33865, 34892, 34899, 34920, 35069, 35199, 35201, 35211, 35215, 35239, 35299, 35328, 35336, 35352, 35373, 35441, 35443, 35469, 35486, 35500, 35501, 35519, 35527, 35703, 35895, 36009, 36023, 36039, 36074, 36092, 36523, 36554, 36578, 36617, 36796, 36817, 36820, 36861, 36865, 36890, 36895, 36899, 36939, 36942, 36947, 36948, 36949, 36984, 37096, 37117, 37197, 37325, 37326, 37329, 37682, 38263, 38272, 38283, 38291, 38306, 38442, 38480, 38498, 38500, 38555, 38598, 38609, 38651, 38754, 38899, 38988, 39006, 39080, 39135, 39208, 39365, 39443, 39640, 65367};
    private static final char[] FREQUENT_TW = {19968, 19977, 19978, 19979, 19981, 19990, 20006, 20013, 20027, 20043, 20063, 20102, 20107, 20108, 20116, 20123, 20126, 20132, 20139, 20154, 20160, 20170, 20171, 20182, 20195, 20197, 20214, 20219, 20221, 20294, 20301, 20303, 20309, 20316, 20320, 20351, 20358, 20379, 20415, 20418, 20445, 20449, 20462, 20491, 20497, 20570, 20581, 20633, 20659, 20687, 20729, 20778, 20803, 20808, 20809, 20813, 20818, 20837, 20839, 20840, 20841, 20844, 20845, 20849, 20854, 20855, 20874, 20877, 20986, 20998, 21015, 21029, 21033, 21040, 21063, 21069, 21109, 21147, 21151, 21152, 21161, 21205, 21209, 21253, 21270, 21271, 21312, 21313, 21335, 21338, 21345, 21360, 21363, 21407, 21435, 21443, 21448, 21450, 21451, 21462, 21463, 21475, 21482, 21487, 21488, 21496, 21507, 21508, 21512, 21516, 21517, 21578, 21644, 21697, 21729, 21830, 21839, 21916, 21934, 21966, 22120, 22235, 22238, 22240, 22283, 22290, 22294, 22296, 22312, 22320, 22411, 22478, 22522, 22577, 22580, 22763, 22806, 22810, 22823, 22825, 22826, 22855, 22899, 22905, 22909, 22914, 23376, 23383, 23416, 23433, 23436, 23450, 23458, 23478, 23481, 23526, 23542, 23559, 23560, 23563, 23565, 23566, 23567, 23569, 23601, 23637, 23665, 24037, 24049, 24050, 24066, 24107, 24118, 24120, 24179, 24180, 24215, 24230, 24247, 24291, 24314, 24335, 24341, 24373, 24375, 24433, 24456, 24460, 24471, 24478, 24515, 24555, 24615, 24687, 24744, 24773, 24819, 24847, 24859, 24863, 25033, 25104, 25105, 25110, 25136, 25138, 25142, 25151, 25152, 25163, 25165, 25171, 25214, 25216, 25237, 25289, 25293, 25351, 25490, 25509, 25512, 25552, 25628, 25705, 25910, 25913, 25918, 25919, 25945, 25972, 25976, 25991, 26009, 26031, 26032, 26041, 26044, 26053, 26085, 26126, 26131, 26143, 26159, 26178, 26356, 26360, 26368, 26371, 26376, 26377, 26381, 26399, 26410, 26412, 26481, 26495, 26519, 26524, 26597, 26684, 26696, 26781, 26989, 27138, 27155, 27161, 27171, 27231, 27298, 27402, 27425, 27454, 27468, 27489, 27491, 27492, 27599, 27604, 27665, 27683, 27700, 27714, 27794, 27861, 27963, 27969, 28023, 28040, 28165, 28207, 28436, 28771, 28858, 28961, 28982, 29031, 29105, 29255, 29256, 29260, 29289, 29305, 29579, 29609, 29694, 29699, 29702, 29983, 29986, 29992, 30001, 30007, 30028, 30041, 30059, 30070, 30331, 30332, 30333, 30334, 30340, 30446, 30452, 30456, 30475, 30495, 30524, 30693, 30908, 31034, 31038, 31070, 31080, 31119, 31169, 31185, 31243, 31278, 31309, 31354, 31435, 31449, 31456, 31532, 31561, 31649, 31680, 31687, 31777, 31934, 31995, 32004, 32005, 32026, 32048, 32068, 32080, 32102, 32113, 32147, 32178, 32218, 32291, 32317, 32654, 32681, 32769, 32771, 32773, 32780, 32862, 32879, 32882, 32929, 32946, 33021, 33126, 33258, 33267, 33287, 33289, 33394, 33457, 33521, 33775, 33836, 33853, 33879, 34214, 34255, 34269, 34389, 34399, 34892, 34899, 34920, 34987, 35037, 35041, 35069, 35199, 35201, 35211, 35222, 35258, 35261, 35264, 35299, 35328, 35330, 35336, 35338, 35342, 35352, 35373, 35387, 35413, 35430, 35441, 35442, 35469, 35486, 35498, 35519, 35531, 35542, 35613, 35657, 35672, 35696, 35703, 35712, 35722, 35731, 36008, 36023, 36027, 36039, 36067, 36074, 36092, 36215, 36229, 36319, 36335, 36523, 36554, 36617, 36681, 36817, 36865, 36889, 36890, 36895, 36896, 36899, 36914, 36938, 36939, 36942, 36947, 36948, 36984, 36996, 37002, 37027, 37096, 37117, 37197, 37202, 37291, 37325, 37327, 37329, 37636, 38263, 38272, 38283, 38291, 38321, 38364, 38463, 38480, 38498, 38500, 38555, 38598, 38626, 38651, 38656, 38750, 38754, 38899, 38913, 38957, 38988, 39006, 39080, 39135, 39184, 39208, 39318, 39321, 39340, 39511, 39636, 39640, 40636, 40643, 40657, 40670, 40845};

    CharacterTables() {
    }

    private static boolean isFrequent(char[] array, char chars) {
        int start = 0;
        int end = array.length - 1;
        int mid = (0 + end) / 2;
        while (start <= end) {
            if (chars == array[mid]) {
                return true;
            }
            if (chars > array[mid]) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
            mid = (start + end) / 2;
        }
        return false;
    }

    static boolean isFrequentCn(char chars) {
        return isFrequent(FREQUENT_CN, chars);
    }

    static boolean isFrequentJa(char chars) {
        return isFrequent(FREQUENT_JA, chars);
    }

    static boolean isFrequentTw(char chars) {
        return isFrequent(FREQUENT_TW, chars);
    }

    static boolean isFrequentHan(char chars) {
        return isFrequentCn(chars) || isFrequentTw(chars) || isFrequentJa(chars);
    }
}
