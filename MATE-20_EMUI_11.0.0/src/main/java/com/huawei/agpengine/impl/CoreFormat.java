package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public enum CoreFormat {
    CORE_FORMAT_UNDEFINED(0),
    CORE_FORMAT_R4G4_UNORM_PACK8(1),
    CORE_FORMAT_R4G4B4A4_UNORM_PACK16(2),
    CORE_FORMAT_B4G4R4A4_UNORM_PACK16(3),
    CORE_FORMAT_R5G6B5_UNORM_PACK16(4),
    CORE_FORMAT_B5G6R5_UNORM_PACK16(5),
    CORE_FORMAT_R5G5B5A1_UNORM_PACK16(6),
    CORE_FORMAT_B5G5R5A1_UNORM_PACK16(7),
    CORE_FORMAT_A1R5G5B5_UNORM_PACK16(8),
    CORE_FORMAT_R8_UNORM(9),
    CORE_FORMAT_R8_SNORM(10),
    CORE_FORMAT_R8_USCALED(11),
    CORE_FORMAT_R8_SSCALED(12),
    CORE_FORMAT_R8_UINT(13),
    CORE_FORMAT_R8_SINT(14),
    CORE_FORMAT_R8_SRGB(15),
    CORE_FORMAT_R8G8_UNORM(16),
    CORE_FORMAT_R8G8_SNORM(17),
    CORE_FORMAT_R8G8_USCALED(18),
    CORE_FORMAT_R8G8_SSCALED(19),
    CORE_FORMAT_R8G8_UINT(20),
    CORE_FORMAT_R8G8_SINT(21),
    CORE_FORMAT_R8G8_SRGB(22),
    CORE_FORMAT_R8G8B8_UNORM(23),
    CORE_FORMAT_R8G8B8_SNORM(24),
    CORE_FORMAT_R8G8B8_USCALED(25),
    CORE_FORMAT_R8G8B8_SSCALED(26),
    CORE_FORMAT_R8G8B8_UINT(27),
    CORE_FORMAT_R8G8B8_SINT(28),
    CORE_FORMAT_R8G8B8_SRGB(29),
    CORE_FORMAT_B8G8R8_UNORM(30),
    CORE_FORMAT_B8G8R8_SNORM(31),
    CORE_FORMAT_B8G8R8_UINT(34),
    CORE_FORMAT_B8G8R8_SINT(35),
    CORE_FORMAT_B8G8R8_SRGB(36),
    CORE_FORMAT_R8G8B8A8_UNORM(37),
    CORE_FORMAT_R8G8B8A8_SNORM(38),
    CORE_FORMAT_R8G8B8A8_USCALED(39),
    CORE_FORMAT_R8G8B8A8_SSCALED(40),
    CORE_FORMAT_R8G8B8A8_UINT(41),
    CORE_FORMAT_R8G8B8A8_SINT(42),
    CORE_FORMAT_R8G8B8A8_SRGB(43),
    CORE_FORMAT_B8G8R8A8_UNORM(44),
    CORE_FORMAT_B8G8R8A8_SNORM(45),
    CORE_FORMAT_B8G8R8A8_UINT(48),
    CORE_FORMAT_B8G8R8A8_SINT(49),
    CORE_FORMAT_B8G8R8A8_SRGB(50),
    CORE_FORMAT_A8B8G8R8_UNORM_PACK32(51),
    CORE_FORMAT_A8B8G8R8_SNORM_PACK32(52),
    CORE_FORMAT_A8B8G8R8_USCALED_PACK32(53),
    CORE_FORMAT_A8B8G8R8_SSCALED_PACK32(54),
    CORE_FORMAT_A8B8G8R8_UINT_PACK32(55),
    CORE_FORMAT_A8B8G8R8_SINT_PACK32(56),
    CORE_FORMAT_A8B8G8R8_SRGB_PACK32(57),
    CORE_FORMAT_A2R10G10B10_UNORM_PACK32(58),
    CORE_FORMAT_A2R10G10B10_UINT_PACK32(62),
    CORE_FORMAT_A2R10G10B10_SINT_PACK32(63),
    CORE_FORMAT_A2B10G10R10_UNORM_PACK32(64),
    CORE_FORMAT_A2B10G10R10_SNORM_PACK32(65),
    CORE_FORMAT_A2B10G10R10_USCALED_PACK32(66),
    CORE_FORMAT_A2B10G10R10_SSCALED_PACK32(67),
    CORE_FORMAT_A2B10G10R10_UINT_PACK32(68),
    CORE_FORMAT_A2B10G10R10_SINT_PACK32(69),
    CORE_FORMAT_R16_UNORM(70),
    CORE_FORMAT_R16_SNORM(71),
    CORE_FORMAT_R16_USCALED(72),
    CORE_FORMAT_R16_SSCALED(73),
    CORE_FORMAT_R16_UINT(74),
    CORE_FORMAT_R16_SINT(75),
    CORE_FORMAT_R16_SFLOAT(76),
    CORE_FORMAT_R16G16_UNORM(77),
    CORE_FORMAT_R16G16_SNORM(78),
    CORE_FORMAT_R16G16_USCALED(79),
    CORE_FORMAT_R16G16_SSCALED(80),
    CORE_FORMAT_R16G16_UINT(81),
    CORE_FORMAT_R16G16_SINT(82),
    CORE_FORMAT_R16G16_SFLOAT(83),
    CORE_FORMAT_R16G16B16_UNORM(84),
    CORE_FORMAT_R16G16B16_SNORM(85),
    CORE_FORMAT_R16G16B16_USCALED(86),
    CORE_FORMAT_R16G16B16_SSCALED(87),
    CORE_FORMAT_R16G16B16_UINT(88),
    CORE_FORMAT_R16G16B16_SINT(89),
    CORE_FORMAT_R16G16B16_SFLOAT(90),
    CORE_FORMAT_R16G16B16A16_UNORM(91),
    CORE_FORMAT_R16G16B16A16_SNORM(92),
    CORE_FORMAT_R16G16B16A16_USCALED(93),
    CORE_FORMAT_R16G16B16A16_SSCALED(94),
    CORE_FORMAT_R16G16B16A16_UINT(95),
    CORE_FORMAT_R16G16B16A16_SINT(96),
    CORE_FORMAT_R16G16B16A16_SFLOAT(97),
    CORE_FORMAT_R32_UINT(98),
    CORE_FORMAT_R32_SINT(99),
    CORE_FORMAT_R32_SFLOAT(100),
    CORE_FORMAT_R32G32_UINT(101),
    CORE_FORMAT_R32G32_SINT(102),
    CORE_FORMAT_R32G32_SFLOAT(103),
    CORE_FORMAT_R32G32B32_UINT(104),
    CORE_FORMAT_R32G32B32_SINT(105),
    CORE_FORMAT_R32G32B32_SFLOAT(106),
    CORE_FORMAT_R32G32B32A32_UINT(107),
    CORE_FORMAT_R32G32B32A32_SINT(108),
    CORE_FORMAT_R32G32B32A32_SFLOAT(109),
    CORE_FORMAT_B10G11R11_UFLOAT_PACK32(122),
    CORE_FORMAT_E5B9G9R9_UFLOAT_PACK32(123),
    CORE_FORMAT_D16_UNORM(124),
    CORE_FORMAT_X8_D24_UNORM_PACK32(125),
    CORE_FORMAT_D32_SFLOAT(126),
    CORE_FORMAT_S8_UINT(127),
    CORE_FORMAT_D24_UNORM_S8_UINT(129),
    CORE_FORMAT_BC1_RGB_UNORM_BLOCK(131),
    CORE_FORMAT_BC1_RGB_SRGB_BLOCK(132),
    CORE_FORMAT_BC1_RGBA_UNORM_BLOCK(133),
    CORE_FORMAT_BC1_RGBA_SRGB_BLOCK(134),
    CORE_FORMAT_BC2_UNORM_BLOCK(135),
    CORE_FORMAT_BC2_SRGB_BLOCK(136),
    CORE_FORMAT_BC3_UNORM_BLOCK(137),
    CORE_FORMAT_BC3_SRGB_BLOCK(138),
    CORE_FORMAT_BC4_UNORM_BLOCK(139),
    CORE_FORMAT_BC4_SNORM_BLOCK(140),
    CORE_FORMAT_BC5_UNORM_BLOCK(141),
    CORE_FORMAT_BC5_SNORM_BLOCK(142),
    CORE_FORMAT_BC6H_UFLOAT_BLOCK(143),
    CORE_FORMAT_BC6H_SFLOAT_BLOCK(144),
    CORE_FORMAT_BC7_UNORM_BLOCK(145),
    CORE_FORMAT_BC7_SRGB_BLOCK(146),
    CORE_FORMAT_ETC2_R8G8B8_UNORM_BLOCK(147),
    CORE_FORMAT_ETC2_R8G8B8_SRGB_BLOCK(148),
    CORE_FORMAT_ETC2_R8G8B8A1_UNORM_BLOCK(149),
    CORE_FORMAT_ETC2_R8G8B8A1_SRGB_BLOCK(150),
    CORE_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK(151),
    CORE_FORMAT_ETC2_R8G8B8A8_SRGB_BLOCK(152),
    CORE_FORMAT_EAC_R11_UNORM_BLOCK(153),
    CORE_FORMAT_EAC_R11_SNORM_BLOCK(154),
    CORE_FORMAT_EAC_R11G11_UNORM_BLOCK(155),
    CORE_FORMAT_EAC_R11G11_SNORM_BLOCK(156),
    CORE_FORMAT_ASTC_4x4_UNORM_BLOCK(157),
    CORE_FORMAT_ASTC_4x4_SRGB_BLOCK(158),
    CORE_FORMAT_ASTC_5x4_UNORM_BLOCK(159),
    CORE_FORMAT_ASTC_5x4_SRGB_BLOCK(160),
    CORE_FORMAT_ASTC_5x5_UNORM_BLOCK(161),
    CORE_FORMAT_ASTC_5x5_SRGB_BLOCK(162),
    CORE_FORMAT_ASTC_6x5_UNORM_BLOCK(163),
    CORE_FORMAT_ASTC_6x5_SRGB_BLOCK(164),
    CORE_FORMAT_ASTC_6x6_UNORM_BLOCK(165),
    CORE_FORMAT_ASTC_6x6_SRGB_BLOCK(166),
    CORE_FORMAT_ASTC_8x5_UNORM_BLOCK(167),
    CORE_FORMAT_ASTC_8x5_SRGB_BLOCK(168),
    CORE_FORMAT_ASTC_8x6_UNORM_BLOCK(169),
    CORE_FORMAT_ASTC_8x6_SRGB_BLOCK(170),
    CORE_FORMAT_ASTC_8x8_UNORM_BLOCK(171),
    CORE_FORMAT_ASTC_8x8_SRGB_BLOCK(172),
    CORE_FORMAT_ASTC_10x5_UNORM_BLOCK(173),
    CORE_FORMAT_ASTC_10x5_SRGB_BLOCK(174),
    CORE_FORMAT_ASTC_10x6_UNORM_BLOCK(175),
    CORE_FORMAT_ASTC_10x6_SRGB_BLOCK(176),
    CORE_FORMAT_ASTC_10x8_UNORM_BLOCK(177),
    CORE_FORMAT_ASTC_10x8_SRGB_BLOCK(178),
    CORE_FORMAT_ASTC_10x10_UNORM_BLOCK(179),
    CORE_FORMAT_ASTC_10x10_SRGB_BLOCK(180),
    CORE_FORMAT_ASTC_12x10_UNORM_BLOCK(181),
    CORE_FORMAT_ASTC_12x10_SRGB_BLOCK(182),
    CORE_FORMAT_ASTC_12x12_UNORM_BLOCK(183),
    CORE_FORMAT_ASTC_12x12_SRGB_BLOCK(184),
    CORE_FORMAT_G8B8G8R8_422_UNORM(1000156000),
    CORE_FORMAT_B8G8R8G8_422_UNORM(1000156001),
    CORE_FORMAT_G8_B8_R8_3PLANE_420_UNORM(1000156002),
    CORE_FORMAT_G8_B8R8_2PLANE_420_UNORM(1000156003),
    CORE_FORMAT_G8_B8_R8_3PLANE_422_UNORM(1000156004),
    CORE_FORMAT_G8_B8R8_2PLANE_422_UNORM(1000156005),
    CORE_FORMAT_MAX_ENUM(Integer.MAX_VALUE);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreFormat swigToEnum(int swigValue2) {
        CoreFormat[] swigValues = (CoreFormat[]) CoreFormat.class.getEnumConstants();
        if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
            return swigValues[swigValue2];
        }
        for (CoreFormat swigEnum : swigValues) {
            if (swigEnum.swigValue == swigValue2) {
                return swigEnum;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreFormat.class + " with value " + swigValue2);
    }

    private CoreFormat() {
        this.swigValue = SwigNext.next;
        SwigNext.access$008();
    }

    private CoreFormat(int swigValue2) {
        this.swigValue = swigValue2;
        int unused = SwigNext.next = swigValue2 + 1;
    }

    private CoreFormat(CoreFormat swigEnum) {
        this.swigValue = swigEnum.swigValue;
        int unused = SwigNext.next = this.swigValue + 1;
    }

    private static class SwigNext {
        private static int next = 0;

        private SwigNext() {
        }

        static /* synthetic */ int access$008() {
            int i = next;
            next = i + 1;
            return i;
        }
    }
}
