package com.android.server.hdmi;

import android.net.util.NetworkConstants;
import android.util.SparseArray;

public final class HdmiCecMessageValidator {
    private static final int DEST_ALL = 3;
    private static final int DEST_BROADCAST = 2;
    private static final int DEST_DIRECT = 1;
    static final int ERROR_DESTINATION = 2;
    static final int ERROR_PARAMETER = 3;
    static final int ERROR_PARAMETER_SHORT = 4;
    static final int ERROR_SOURCE = 1;
    static final int OK = 0;
    private static final int SRC_UNREGISTERED = 4;
    private static final String TAG = "HdmiCecMessageValidator";
    private final HdmiControlService mService;
    final SparseArray<ValidationInfo> mValidationInfo = new SparseArray();

    interface ParameterValidator {
        int isValid(byte[] bArr);
    }

    private static class FixedLengthValidator implements ParameterValidator {
        private final int mLength;

        public FixedLengthValidator(int length) {
            this.mLength = length;
        }

        public int isValid(byte[] params) {
            return params.length < this.mLength ? 4 : 0;
        }
    }

    private class PhysicalAddressValidator implements ParameterValidator {
        /* synthetic */ PhysicalAddressValidator(HdmiCecMessageValidator this$0, PhysicalAddressValidator -this1) {
            this();
        }

        private PhysicalAddressValidator() {
        }

        public int isValid(byte[] params) {
            if (params.length < 2) {
                return 4;
            }
            return HdmiCecMessageValidator.toErrorCode(HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 0));
        }
    }

    private class ReportPhysicalAddressValidator implements ParameterValidator {
        /* synthetic */ ReportPhysicalAddressValidator(HdmiCecMessageValidator this$0, ReportPhysicalAddressValidator -this1) {
            this();
        }

        private ReportPhysicalAddressValidator() {
        }

        public int isValid(byte[] params) {
            boolean z = false;
            if (params.length < 3) {
                return 4;
            }
            if (HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 0)) {
                z = HdmiCecMessageValidator.isValidType(params[2]);
            }
            return HdmiCecMessageValidator.toErrorCode(z);
        }
    }

    private class RoutingChangeValidator implements ParameterValidator {
        /* synthetic */ RoutingChangeValidator(HdmiCecMessageValidator this$0, RoutingChangeValidator -this1) {
            this();
        }

        private RoutingChangeValidator() {
        }

        public int isValid(byte[] params) {
            boolean z = false;
            if (params.length < 4) {
                return 4;
            }
            if (HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 0)) {
                z = HdmiCecMessageValidator.this.isValidPhysicalAddress(params, 2);
            }
            return HdmiCecMessageValidator.toErrorCode(z);
        }
    }

    private class SystemAudioModeRequestValidator extends PhysicalAddressValidator {
        /* synthetic */ SystemAudioModeRequestValidator(HdmiCecMessageValidator this$0, SystemAudioModeRequestValidator -this1) {
            this();
        }

        private SystemAudioModeRequestValidator() {
            super(HdmiCecMessageValidator.this, null);
        }

        public int isValid(byte[] params) {
            if (params.length == 0) {
                return 0;
            }
            return super.isValid(params);
        }
    }

    private static class ValidationInfo {
        public final int addressType;
        public final ParameterValidator parameterValidator;

        public ValidationInfo(ParameterValidator validator, int type) {
            this.parameterValidator = validator;
            this.addressType = type;
        }
    }

    private static class VariableLengthValidator implements ParameterValidator {
        private final int mMaxLength;
        private final int mMinLength;

        public VariableLengthValidator(int minLength, int maxLength) {
            this.mMinLength = minLength;
            this.mMaxLength = maxLength;
        }

        public int isValid(byte[] params) {
            return params.length < this.mMinLength ? 4 : 0;
        }
    }

    public HdmiCecMessageValidator(HdmiControlService service) {
        this.mService = service;
        PhysicalAddressValidator physicalAddressValidator = new PhysicalAddressValidator(this, null);
        addValidationInfo(130, physicalAddressValidator, 6);
        addValidationInfo(157, physicalAddressValidator, 1);
        addValidationInfo(132, new ReportPhysicalAddressValidator(this, null), 6);
        addValidationInfo(128, new RoutingChangeValidator(this, null), 6);
        addValidationInfo(129, physicalAddressValidator, 6);
        addValidationInfo(NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT, physicalAddressValidator, 2);
        addValidationInfo(112, new SystemAudioModeRequestValidator(this, null), 1);
        FixedLengthValidator noneValidator = new FixedLengthValidator(0);
        addValidationInfo(255, noneValidator, 1);
        addValidationInfo(159, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2, noneValidator, 5);
        addValidationInfo(113, noneValidator, 1);
        addValidationInfo(143, noneValidator, 1);
        addValidationInfo(140, noneValidator, 5);
        addValidationInfo(70, noneValidator, 1);
        addValidationInfo(131, noneValidator, 5);
        addValidationInfo(125, noneValidator, 1);
        addValidationInfo(4, noneValidator, 1);
        addValidationInfo(192, noneValidator, 1);
        addValidationInfo(11, noneValidator, 1);
        addValidationInfo(15, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL, noneValidator, 1);
        addValidationInfo(HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS, noneValidator, 1);
        addValidationInfo(196, noneValidator, 1);
        addValidationInfo(NetworkConstants.ICMPV6_ROUTER_SOLICITATION, noneValidator, 6);
        addValidationInfo(54, noneValidator, 7);
        addValidationInfo(197, noneValidator, 1);
        addValidationInfo(13, noneValidator, 1);
        addValidationInfo(6, noneValidator, 1);
        addValidationInfo(5, noneValidator, 1);
        addValidationInfo(69, noneValidator, 1);
        addValidationInfo(139, noneValidator, 3);
        FixedLengthValidator oneByteValidator = new FixedLengthValidator(1);
        addValidationInfo(9, new VariableLengthValidator(1, 8), 1);
        addValidationInfo(10, oneByteValidator, 1);
        addValidationInfo(158, oneByteValidator, 1);
        addValidationInfo(50, new FixedLengthValidator(3), 2);
        VariableLengthValidator maxLengthValidator = new VariableLengthValidator(0, 14);
        addValidationInfo(NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION, new FixedLengthValidator(3), 2);
        addValidationInfo(137, new VariableLengthValidator(1, 14), 5);
        addValidationInfo(160, new VariableLengthValidator(4, 14), 7);
        addValidationInfo(138, maxLengthValidator, 7);
        addValidationInfo(100, maxLengthValidator, 1);
        addValidationInfo(71, maxLengthValidator, 1);
        addValidationInfo(141, oneByteValidator, 1);
        addValidationInfo(142, oneByteValidator, 1);
        addValidationInfo(68, new VariableLengthValidator(1, 2), 1);
        addValidationInfo(144, oneByteValidator, 1);
        addValidationInfo(0, new FixedLengthValidator(2), 1);
        addValidationInfo(122, oneByteValidator, 1);
        addValidationInfo(163, new FixedLengthValidator(3), 1);
        addValidationInfo(164, oneByteValidator, 1);
        addValidationInfo(114, oneByteValidator, 3);
        addValidationInfo(126, oneByteValidator, 1);
        addValidationInfo(154, oneByteValidator, 1);
        addValidationInfo(248, maxLengthValidator, 6);
    }

    private void addValidationInfo(int opcode, ParameterValidator validator, int addrType) {
        this.mValidationInfo.append(opcode, new ValidationInfo(validator, addrType));
    }

    int isValid(HdmiCecMessage message) {
        ValidationInfo info = (ValidationInfo) this.mValidationInfo.get(message.getOpcode());
        if (info == null) {
            HdmiLogger.warning("No validation information for the message: " + message, new Object[0]);
            return 0;
        } else if (message.getSource() == 15 && (info.addressType & 4) == 0) {
            HdmiLogger.warning("Unexpected source: " + message, new Object[0]);
            return 1;
        } else {
            if (message.getDestination() == 15) {
                if ((info.addressType & 2) == 0) {
                    HdmiLogger.warning("Unexpected broadcast message: " + message, new Object[0]);
                    return 2;
                }
            } else if ((info.addressType & 1) == 0) {
                HdmiLogger.warning("Unexpected direct message: " + message, new Object[0]);
                return 2;
            }
            int errorCode = info.parameterValidator.isValid(message.getParams());
            if (errorCode == 0) {
                return 0;
            }
            HdmiLogger.warning("Unexpected parameters: " + message, new Object[0]);
            return errorCode;
        }
    }

    private boolean isValidPhysicalAddress(byte[] params, int offset) {
        if (!this.mService.isTvDevice()) {
            return true;
        }
        int path = HdmiUtils.twoBytesToInt(params, offset);
        if ((path == NetworkConstants.ARP_HWTYPE_RESERVED_HI || path != this.mService.getPhysicalAddress()) && this.mService.pathToPortId(path) == -1) {
            return false;
        }
        return true;
    }

    static boolean isValidType(int type) {
        if (type < 0 || type > 7 || type == 2) {
            return false;
        }
        return true;
    }

    private static int toErrorCode(boolean success) {
        return success ? 0 : 3;
    }
}
