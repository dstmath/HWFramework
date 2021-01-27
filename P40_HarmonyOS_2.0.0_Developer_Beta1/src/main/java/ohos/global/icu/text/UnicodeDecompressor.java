package ohos.global.icu.text;

import ohos.media.camera.params.adapter.InnerMetadata;
import ohos.media.recorder.Recorder;
import ohos.msdp.devicevirtualization.EventType;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.nfc.NfcKitsUtils;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public final class UnicodeDecompressor implements SCSU {
    private static final int BUFSIZE = 3;
    private byte[] fBuffer = new byte[3];
    private int fBufferLength = 0;
    private int fCurrentWindow = 0;
    private int fMode = 0;
    private int[] fOffsets = new int[8];

    public UnicodeDecompressor() {
        reset();
    }

    public static String decompress(byte[] bArr) {
        return new String(decompress(bArr, 0, bArr.length));
    }

    public static char[] decompress(byte[] bArr, int i, int i2) {
        UnicodeDecompressor unicodeDecompressor = new UnicodeDecompressor();
        int max = Math.max(2, (i2 - i) * 2);
        char[] cArr = new char[max];
        int decompress = unicodeDecompressor.decompress(bArr, i, i2, null, cArr, 0, max);
        char[] cArr2 = new char[decompress];
        System.arraycopy(cArr, 0, cArr2, 0, decompress);
        return cArr2;
    }

    public int decompress(byte[] bArr, int i, int i2, int[] iArr, char[] cArr, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        if (cArr.length < 2 || i4 - i3 < 2) {
            throw new IllegalArgumentException("charBuffer.length < 2");
        }
        int i14 = this.fBufferLength;
        if (i14 > 0) {
            if (i14 != 3) {
                int length = this.fBuffer.length - i14;
                int i15 = i2 - i;
                if (i15 >= length) {
                    i15 = length;
                }
                System.arraycopy(bArr, i, this.fBuffer, this.fBufferLength, i15);
                i13 = i15;
            } else {
                i13 = 0;
            }
            this.fBufferLength = 0;
            byte[] bArr2 = this.fBuffer;
            i6 = i3 + decompress(bArr2, 0, bArr2.length, null, cArr, i3, i4);
            i5 = i + i13;
        } else {
            i6 = i3;
            i5 = i;
        }
        while (true) {
            if (i5 < i2 && i6 < i4) {
                int i16 = this.fMode;
                int i17 = 1;
                if (i16 == 0) {
                    while (true) {
                        if (i5 < i2 && i6 < i4) {
                            i9 = i5 + 1;
                            int i18 = bArr[i5] & 255;
                            switch (i18) {
                                case 0:
                                case 9:
                                case 10:
                                case 13:
                                case 32:
                                case 33:
                                case 34:
                                case 35:
                                case 36:
                                case 37:
                                case 38:
                                case 39:
                                case 40:
                                case 41:
                                case 42:
                                case NfcKitsUtils.REGISTER_FOREGROUND_DISPATCH /* 43 */:
                                case NfcKitsUtils.SET_READER_MODE /* 44 */:
                                case 45:
                                case 46:
                                case 47:
                                case 48:
                                case 49:
                                case 50:
                                case 51:
                                case 52:
                                case 53:
                                case 54:
                                case 55:
                                case 56:
                                case 57:
                                case 58:
                                case 59:
                                case 60:
                                case 61:
                                case 62:
                                case 63:
                                case 64:
                                case 65:
                                case 66:
                                case 67:
                                case 68:
                                case 69:
                                case 70:
                                case 71:
                                case 72:
                                case 73:
                                case 74:
                                case 75:
                                case 76:
                                case 77:
                                case 78:
                                case 79:
                                case 80:
                                case 81:
                                case 82:
                                case 83:
                                case 84:
                                case 85:
                                case 86:
                                case 87:
                                case 88:
                                case 89:
                                case Recorder.OrientationHint.SECOND_PLAYBACK_DERGREE /* 90 */:
                                case MultimodalEvent.MUTE /* 91 */:
                                case 92:
                                case 93:
                                case 94:
                                case 95:
                                case ArabicShaping.DIGITS_EN2AN_INIT_LR /* 96 */:
                                case 97:
                                case 98:
                                case 99:
                                case 100:
                                case 101:
                                case 102:
                                case 103:
                                case 104:
                                case 105:
                                case 106:
                                case EventType.EVENT_DEVICE_SHOW_PIN_INPUT /* 107 */:
                                case 108:
                                case 109:
                                case EventType.EVENT_DEVICE_ACTIVE_DISCONNECT /* 110 */:
                                case 111:
                                case 112:
                                case 113:
                                case 114:
                                case 115:
                                case SystemAbilityDefinition.ABILITY_TOOLS_SERVICE_ID /* 116 */:
                                case InnerMetadata.SceneDetectionType.SMART_SUGGEST_MODE_BEAUTY /* 117 */:
                                case 118:
                                case 119:
                                case 120:
                                case 121:
                                case 122:
                                case 123:
                                case 124:
                                case 125:
                                case Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT /* 126 */:
                                case Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT /* 127 */:
                                    cArr[i6] = (char) i18;
                                    i5 = i9;
                                    i6++;
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                    if (i9 >= i2) {
                                        i7 = i9 - 1;
                                        int i19 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i19);
                                        this.fBufferLength = i19;
                                        i8 = this.fBufferLength;
                                        break;
                                    } else {
                                        int i20 = i9 + 1;
                                        int i21 = bArr[i9] & 255;
                                        int i22 = i6 + 1;
                                        if (i21 < 0 || i21 >= 128) {
                                            i10 = this.fOffsets[i18 - 1] - 128;
                                        } else {
                                            i10 = sOffsets[i18 - 1];
                                        }
                                        cArr[i6] = (char) (i21 + i10);
                                        i5 = i20;
                                        i6 = i22;
                                    }
                                case 11:
                                    int i23 = i9 + 1;
                                    if (i23 >= i2) {
                                        i7 = i9 - 1;
                                        int i24 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i24);
                                        this.fBufferLength = i24;
                                        i8 = this.fBufferLength;
                                        break;
                                    } else {
                                        int i25 = bArr[i9] & 255;
                                        this.fCurrentWindow = (i25 & 224) >> 5;
                                        this.fOffsets[this.fCurrentWindow] = (((bArr[i23] & 255) | ((i25 & 31) << 8)) * 128) + 65536;
                                        i5 = i23 + 1;
                                    }
                                case 12:
                                default:
                                    i5 = i9;
                                case 14:
                                    int i26 = i9 + 1;
                                    if (i26 >= i2) {
                                        i7 = i9 - 1;
                                        int i27 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i27);
                                        this.fBufferLength = i27;
                                        i8 = this.fBufferLength;
                                        break;
                                    } else {
                                        cArr[i6] = (char) ((bArr[i26] & 255) | (bArr[i9] << 8));
                                        i6++;
                                        i5 = i26 + 1;
                                    }
                                case 15:
                                    this.fMode = i17;
                                    break;
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                case 20:
                                case 21:
                                case 22:
                                case 23:
                                    this.fCurrentWindow = i18 - 16;
                                    i17 = 1;
                                    i5 = i9;
                                case 24:
                                case 25:
                                case 26:
                                case 27:
                                case 28:
                                case 29:
                                case 30:
                                case 31:
                                    if (i9 >= i2) {
                                        i7 = i9 - 1;
                                        int i28 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i28);
                                        this.fBufferLength = i28;
                                        i8 = this.fBufferLength;
                                        break;
                                    } else {
                                        this.fCurrentWindow = i18 - 24;
                                        this.fOffsets[this.fCurrentWindow] = sOffsetTable[bArr[i9] & 255];
                                        i5 = i9 + 1;
                                        i17 = 1;
                                    }
                                case 128:
                                case DateFormat.RELATIVE_LONG /* 129 */:
                                case 130:
                                case DateFormat.RELATIVE_SHORT /* 131 */:
                                case 132:
                                case 133:
                                case 134:
                                case 135:
                                case 136:
                                case 137:
                                case 138:
                                case 139:
                                case 140:
                                case 141:
                                case 142:
                                case 143:
                                case 144:
                                case 145:
                                case 146:
                                case 147:
                                case 148:
                                case 149:
                                case 150:
                                case 151:
                                case 152:
                                case 153:
                                case 154:
                                case 155:
                                case 156:
                                case 157:
                                case 158:
                                case 159:
                                case 160:
                                case 161:
                                case 162:
                                case 163:
                                case 164:
                                case 165:
                                case 166:
                                case 167:
                                case 168:
                                case 169:
                                case 170:
                                case 171:
                                case 172:
                                case 173:
                                case 174:
                                case 175:
                                case 176:
                                case 177:
                                case 178:
                                case SystemAbilityDefinition.ABILITY_TEST_SERVICE_ID /* 179 */:
                                case Recorder.OrientationHint.THIRD_PLAYBACK_DERGREE /* 180 */:
                                case 181:
                                case 182:
                                case 183:
                                case 184:
                                case 185:
                                case 186:
                                case 187:
                                case 188:
                                case 189:
                                case 190:
                                case 191:
                                case 192:
                                case 193:
                                case 194:
                                case 195:
                                case 196:
                                case 197:
                                case 198:
                                case SystemAbilityDefinition.SUBSYS_AAFWK_SYS_ABILITY_ID_END /* 199 */:
                                case 200:
                                case 201:
                                case 202:
                                case 203:
                                case EventType.EVENT_DEVICE_CAPABILITY_ENABLE /* 204 */:
                                case EventType.EVENT_DEVICE_CAPABILITY_DISABLE /* 205 */:
                                case EventType.EVENT_DEVICE_CAPABILITY_ABNORMAL /* 206 */:
                                case EventType.EVENT_DEVICE_CAPABILITY_BUSY /* 207 */:
                                case 208:
                                case 209:
                                case 210:
                                case 211:
                                case 212:
                                case 213:
                                case 214:
                                case 215:
                                case 216:
                                case 217:
                                case 218:
                                case 219:
                                case 220:
                                case 221:
                                case 222:
                                case 223:
                                case 224:
                                case SCSU.UCHANGE1 /* 225 */:
                                case SCSU.UCHANGE2 /* 226 */:
                                case SCSU.UCHANGE3 /* 227 */:
                                case SCSU.UCHANGE4 /* 228 */:
                                case SCSU.UCHANGE5 /* 229 */:
                                case SCSU.UCHANGE6 /* 230 */:
                                case SCSU.UCHANGE7 /* 231 */:
                                case SCSU.UDEFINE0 /* 232 */:
                                case SCSU.UDEFINE1 /* 233 */:
                                case SCSU.UDEFINE2 /* 234 */:
                                case SCSU.UDEFINE3 /* 235 */:
                                case SCSU.UDEFINE4 /* 236 */:
                                case SCSU.UDEFINE5 /* 237 */:
                                case SCSU.UDEFINE6 /* 238 */:
                                case SCSU.UDEFINE7 /* 239 */:
                                case SCSU.UQUOTEU /* 240 */:
                                case SCSU.UDEFINEX /* 241 */:
                                case SCSU.URESERVED /* 242 */:
                                case 243:
                                case 244:
                                case 245:
                                case 246:
                                case 247:
                                case 248:
                                case SCSU.LATININDEX /* 249 */:
                                case SCSU.IPAEXTENSIONINDEX /* 250 */:
                                case SCSU.GREEKINDEX /* 251 */:
                                case SCSU.ARMENIANINDEX /* 252 */:
                                case SCSU.HIRAGANAINDEX /* 253 */:
                                case SCSU.KATAKANAINDEX /* 254 */:
                                case 255:
                                    int[] iArr2 = this.fOffsets;
                                    int i29 = this.fCurrentWindow;
                                    if (iArr2[i29] <= 65535) {
                                        cArr[i6] = (char) ((i18 + iArr2[i29]) - 128);
                                        i5 = i9;
                                        i6++;
                                    } else {
                                        int i30 = i6 + 1;
                                        if (i30 >= i4) {
                                            i7 = i9 - 1;
                                            int i31 = i2 - i7;
                                            System.arraycopy(bArr, i7, this.fBuffer, 0, i31);
                                            this.fBufferLength = i31;
                                            i8 = this.fBufferLength;
                                            break;
                                        } else {
                                            int i32 = iArr2[i29] - 65536;
                                            cArr[i6] = (char) ((i32 >> 10) + 55296);
                                            i6 = i30 + 1;
                                            cArr[i30] = (char) ((i32 & 1023) + UTF16.TRAIL_SURROGATE_MIN_VALUE + (i18 & Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT));
                                            i5 = i9;
                                        }
                                    }
                                    i17 = 1;
                            }
                        }
                    }
                } else if (i16 != 1) {
                    continue;
                } else {
                    while (true) {
                        if (i5 < i2 && i6 < i4) {
                            i9 = i5 + 1;
                            int i33 = bArr[i5] & 255;
                            switch (i33) {
                                case 224:
                                case SCSU.UCHANGE1 /* 225 */:
                                case SCSU.UCHANGE2 /* 226 */:
                                case SCSU.UCHANGE3 /* 227 */:
                                case SCSU.UCHANGE4 /* 228 */:
                                case SCSU.UCHANGE5 /* 229 */:
                                case SCSU.UCHANGE6 /* 230 */:
                                case SCSU.UCHANGE7 /* 231 */:
                                    this.fCurrentWindow = i33 - 224;
                                    this.fMode = 0;
                                    break;
                                case SCSU.UDEFINE0 /* 232 */:
                                case SCSU.UDEFINE1 /* 233 */:
                                case SCSU.UDEFINE2 /* 234 */:
                                case SCSU.UDEFINE3 /* 235 */:
                                case SCSU.UDEFINE4 /* 236 */:
                                case SCSU.UDEFINE5 /* 237 */:
                                case SCSU.UDEFINE6 /* 238 */:
                                case SCSU.UDEFINE7 /* 239 */:
                                    if (i9 < i2) {
                                        this.fCurrentWindow = i33 - 232;
                                        this.fOffsets[this.fCurrentWindow] = sOffsetTable[bArr[i9] & 255];
                                        this.fMode = 0;
                                        i5 = i9 + 1;
                                        break;
                                    } else {
                                        i7 = i9 - 1;
                                        int i34 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i34);
                                        this.fBufferLength = i34;
                                        i8 = this.fBufferLength;
                                        break;
                                    }
                                case SCSU.UQUOTEU /* 240 */:
                                    if (i9 >= i2 - 1) {
                                        i7 = i9 - 1;
                                        int i35 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i35);
                                        this.fBufferLength = i35;
                                        i8 = this.fBufferLength;
                                        break;
                                    } else {
                                        int i36 = i9 + 1;
                                        i12 = i6 + 1;
                                        i11 = i36 + 1;
                                        cArr[i6] = (char) ((bArr[i36] & 255) | (bArr[i9] << 8));
                                        i6 = i12;
                                        i5 = i11;
                                    }
                                case SCSU.UDEFINEX /* 241 */:
                                    int i37 = i9 + 1;
                                    if (i37 < i2) {
                                        int i38 = bArr[i9] & 255;
                                        this.fCurrentWindow = (i38 & 224) >> 5;
                                        this.fOffsets[this.fCurrentWindow] = (((bArr[i37] & 255) | ((i38 & 31) << 8)) * 128) + 65536;
                                        this.fMode = 0;
                                        i5 = i37 + 1;
                                        break;
                                    } else {
                                        i7 = i9 - 1;
                                        int i39 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i39);
                                        this.fBufferLength = i39;
                                        i8 = this.fBufferLength;
                                        break;
                                    }
                                default:
                                    if (i9 >= i2) {
                                        i7 = i9 - 1;
                                        int i40 = i2 - i7;
                                        System.arraycopy(bArr, i7, this.fBuffer, 0, i40);
                                        this.fBufferLength = i40;
                                        i8 = this.fBufferLength;
                                        break;
                                    } else {
                                        i12 = i6 + 1;
                                        i11 = i9 + 1;
                                        cArr[i6] = (char) ((i33 << 8) | (bArr[i9] & 255));
                                        i6 = i12;
                                        i5 = i11;
                                    }
                            }
                        }
                    }
                }
                i5 = i9;
            }
        }
        i5 = i8 + i7;
        if (iArr != null) {
            iArr[0] = i5 - i;
        }
        return i6 - i3;
    }

    public void reset() {
        int[] iArr = this.fOffsets;
        iArr[0] = 128;
        iArr[1] = 192;
        iArr[2] = 1024;
        iArr[3] = 1536;
        iArr[4] = 2304;
        iArr[5] = 12352;
        iArr[6] = 12448;
        iArr[7] = 65280;
        this.fCurrentWindow = 0;
        this.fMode = 0;
        this.fBufferLength = 0;
    }
}
