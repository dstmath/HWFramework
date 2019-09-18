package com.android.internal.telephony.cat;

import android.content.res.Resources;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.cat.BearerDescription;
import com.android.internal.telephony.cat.Duration;
import com.android.internal.telephony.cat.InterfaceTransportLevel;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

abstract class ValueParser {
    ValueParser() {
    }

    static CommandDetails retrieveCommandDetails(ComprehensionTlv ctlv) throws ResultException {
        CommandDetails cmdDet = new CommandDetails();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            cmdDet.compRequired = ctlv.isComprehensionRequired();
            cmdDet.commandNumber = rawValue[valueIndex] & 255;
            cmdDet.typeOfCommand = rawValue[valueIndex + 1] & 255;
            cmdDet.commandQualifier = rawValue[valueIndex + 2] & 255;
            return cmdDet;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static DeviceIdentities retrieveDeviceIdentities(ComprehensionTlv ctlv) throws ResultException {
        DeviceIdentities devIds = new DeviceIdentities();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            devIds.sourceId = rawValue[valueIndex] & 255;
            devIds.destinationId = rawValue[valueIndex + 1] & 255;
            return devIds;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
    }

    static Duration retrieveDuration(ComprehensionTlv ctlv) throws ResultException {
        Duration.TimeUnit timeUnit = Duration.TimeUnit.SECOND;
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            return new Duration(rawValue[valueIndex + 1] & 255, Duration.TimeUnit.values()[rawValue[valueIndex] & 255]);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static Item retrieveItem(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        try {
            return new Item(rawValue[valueIndex] & 255, HwTelephonyFactory.getHwUiccManager().adnStringFieldToStringForSTK(rawValue, valueIndex + 1, length - 1));
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static int retrieveItemId(ComprehensionTlv ctlv) throws ResultException {
        try {
            return ctlv.getRawValue()[ctlv.getValueIndex()] & 255;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static IconId retrieveIconId(ComprehensionTlv ctlv) throws ResultException {
        IconId id = new IconId();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int valueIndex2 = valueIndex + 1;
        try {
            id.selfExplanatory = (rawValue[valueIndex] & 255) == 0;
            id.recordNumber = rawValue[valueIndex2] & 255;
            return id;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static ItemsIconId retrieveItemsIconId(ComprehensionTlv ctlv) throws ResultException {
        CatLog.d("ValueParser", "retrieveItemsIconId:");
        ItemsIconId id = new ItemsIconId();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        boolean z = true;
        int numOfItems = ctlv.getLength() - 1;
        id.recordNumbers = new int[numOfItems];
        int valueIndex2 = valueIndex + 1;
        try {
            int index = 0;
            if ((rawValue[valueIndex] & 255) != 0) {
                z = false;
            }
            id.selfExplanatory = z;
            while (true) {
                int index2 = index;
                if (index2 >= numOfItems) {
                    return id;
                }
                index = index2 + 1;
                int valueIndex3 = valueIndex2 + 1;
                try {
                    id.recordNumbers[index2] = rawValue[valueIndex2];
                    valueIndex2 = valueIndex3;
                } catch (IndexOutOfBoundsException e) {
                    int i = valueIndex3;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            }
        } catch (IndexOutOfBoundsException e2) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static List<TextAttribute> retrieveTextAttribute(ComprehensionTlv ctlv) throws ResultException {
        ArrayList<TextAttribute> lst = new ArrayList<>();
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        int itemCount = length / 4;
        int valueIndex2 = valueIndex;
        int i = 0;
        while (i < itemCount) {
            try {
                int start = rawValue[valueIndex2] & 255;
                int format = rawValue[valueIndex2 + 1] & 255;
                byte b = rawValue[valueIndex2 + 2] & 255;
                byte b2 = rawValue[valueIndex2 + 3] & 255;
                byte b3 = b & 3;
                TextAlignment align = TextAlignment.fromInt(b3);
                int sizeValue = (b >> 2) & 3;
                FontSize size = FontSize.fromInt(sizeValue);
                if (size == null) {
                    size = FontSize.NORMAL;
                }
                FontSize size2 = size;
                boolean strikeThrough = true;
                boolean bold = (b & 16) != 0;
                boolean italic = (b & 32) != 0;
                int i2 = sizeValue;
                boolean underlined = (b & 64) != 0;
                if ((b & 128) == 0) {
                    strikeThrough = false;
                }
                byte b4 = b3;
                byte b5 = b2;
                byte b6 = b;
                int i3 = format;
                TextAttribute textAttribute = new TextAttribute(start, format, align, size2, bold, italic, underlined, strikeThrough, TextColor.fromInt(b2));
                lst.add(textAttribute);
                i++;
                valueIndex2 += 4;
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        return lst;
    }

    static String retrieveAlphaId(ComprehensionTlv ctlv) throws ResultException {
        boolean noAlphaUsrCnf;
        String str = null;
        if (ctlv != null) {
            byte[] rawValue = ctlv.getRawValue();
            int valueIndex = ctlv.getValueIndex();
            int length = ctlv.getLength();
            if (length != 0) {
                try {
                    return HwTelephonyFactory.getHwUiccManager().adnStringFieldToStringForSTK(rawValue, valueIndex, length);
                } catch (IndexOutOfBoundsException e) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } else {
                CatLog.d("ValueParser", "Alpha Id length=" + length);
                return null;
            }
        } else {
            try {
                noAlphaUsrCnf = Resources.getSystem().getBoolean(17957031);
            } catch (Resources.NotFoundException e2) {
                noAlphaUsrCnf = false;
            }
            if (!noAlphaUsrCnf) {
                str = "Default Message";
            }
            return str;
        }
    }

    static String retrieveTextString(ComprehensionTlv ctlv) throws ResultException {
        String text;
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int textLen = ctlv.getLength();
        if (textLen == 0) {
            return null;
        }
        int textLen2 = textLen - 1;
        try {
            byte codingScheme = (byte) (rawValue[valueIndex] & 12);
            if (codingScheme != 0) {
                if (codingScheme != 12) {
                    if (codingScheme == 4) {
                        text = GsmAlphabet.gsm8BitUnpackedToString(rawValue, valueIndex + 1, textLen2);
                    } else if (codingScheme == 8) {
                        text = new String(rawValue, valueIndex + 1, textLen2, "UTF-16");
                    } else {
                        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                    }
                    return text;
                }
            }
            text = GsmAlphabet.gsm7BitPackedToString(rawValue, valueIndex + 1, (textLen2 * 8) / 7);
            return text;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        } catch (UnsupportedEncodingException e2) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static int retrieveBufferSize(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            return ((rawValue[valueIndex] & 255) << 8) | (rawValue[valueIndex + 1] & 255);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static InterfaceTransportLevel retrieveInterfaceTransportLevel(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            return new InterfaceTransportLevel(((rawValue[valueIndex + 1] & 255) << 8) | (rawValue[valueIndex + 2] & 255), InterfaceTransportLevel.TransportProtocol.values()[rawValue[valueIndex] & 255]);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static int retrieveChannelDataLength(ComprehensionTlv ctlv) throws ResultException {
        try {
            return ctlv.getRawValue()[ctlv.getValueIndex()] & 255;
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static byte[] retrieveChannelData(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        byte[] data = new byte[ctlv.getLength()];
        System.arraycopy(rawValue, valueIndex, data, 0, data.length);
        return data;
    }

    static byte[] retrieveOtherAddress(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        if (ctlv.getLength() == 0) {
            return new byte[0];
        }
        try {
            int addrType = rawValue[valueIndex] & 255;
            if (addrType != 33 && addrType != 87) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            } else if ((addrType != 33 || ctlv.getLength() == 5) && (addrType != 87 || ctlv.getLength() == 17)) {
                byte[] addr = new byte[(ctlv.getLength() - 1)];
                System.arraycopy(rawValue, valueIndex + 1, addr, 0, addr.length);
                return addr;
            } else {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static String retrieveBIPAlphaId(ComprehensionTlv ctlv) throws ResultException {
        if (ctlv == null) {
            return null;
        }
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        int length = ctlv.getLength();
        if (length == 0) {
            return null;
        }
        try {
            return IccUtils.adnStringFieldToString(rawValue, valueIndex, length);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    static String retrieveNetworkAccessName(ComprehensionTlv ctlv) throws ResultException {
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        String networkAccessName = null;
        int len = ctlv.getLength() + valueIndex;
        while (valueIndex < len) {
            try {
                byte labelLen = rawValue[valueIndex];
                CatLog.d("ValueParser", "labelLen:" + labelLen);
                if (labelLen <= 0) {
                    return networkAccessName;
                }
                String label = GsmAlphabet.gsm8BitUnpackedToString(rawValue, valueIndex + 1, labelLen);
                valueIndex += labelLen + 1;
                CatLog.d("ValueParser", "valueIndex:" + valueIndex + "label:" + label + "labelLen:" + labelLen);
                if (networkAccessName == null) {
                    networkAccessName = label;
                } else {
                    networkAccessName = networkAccessName + "." + label;
                }
                CatLog.d("ValueParser", "valueIndex:" + valueIndex + "label:" + label + "labelLen:" + labelLen + "networkAccessName:" + networkAccessName);
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        return networkAccessName;
    }

    static BearerDescription retrieveBearerDescription(ComprehensionTlv ctlv) throws ResultException {
        BearerDescription.BearerType type = null;
        byte[] rawValue = ctlv.getRawValue();
        int valueIndex = ctlv.getValueIndex();
        try {
            BearerDescription.BearerType[] values = BearerDescription.BearerType.values();
            int length = values.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                BearerDescription.BearerType bt = values[i];
                if (bt.value() == rawValue[valueIndex]) {
                    type = bt;
                    break;
                }
                i++;
            }
            if (type != null) {
                byte[] parameters = new byte[(ctlv.getLength() - 1)];
                System.arraycopy(rawValue, valueIndex + 1, parameters, 0, parameters.length);
                return new BearerDescription(type, parameters);
            }
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }
}
