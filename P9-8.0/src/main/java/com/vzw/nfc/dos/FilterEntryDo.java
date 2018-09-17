package com.vzw.nfc.dos;

public class FilterEntryDo extends VzwTlv {
    public static final int TAG = 161;
    private AidMaskDo mAidMaskDo = null;
    private AidRangeDo mAidRangeDo = null;
    private FilterConditionTagDo mFilterConditionTagDo = null;
    private RoutingModeDo mRoutingModeDo = null;
    private VzwPermissionDo mVzwArDo = null;

    public FilterEntryDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, 161, valueIndex, valueLength);
    }

    public FilterEntryDo(AidRangeDo aid_range, AidMaskDo aid_mask, RoutingModeDo routing_mode, FilterConditionTagDo filter_condition_tag) {
        super(null, 161, 0, 0);
        this.mAidMaskDo = aid_mask;
        this.mAidRangeDo = aid_range;
        this.mFilterConditionTagDo = filter_condition_tag;
        this.mRoutingModeDo = routing_mode;
    }

    public AidRangeDo getAidRangeDo() {
        return this.mAidRangeDo;
    }

    public AidMaskDo getAidMaskDo() {
        return this.mAidMaskDo;
    }

    public RoutingModeDo getRoutingModeDo() {
        return this.mRoutingModeDo;
    }

    public FilterConditionTagDo getFilterConditionTagDo() {
        return this.mFilterConditionTagDo;
    }

    public VzwPermissionDo getVzwArDo() {
        return this.mVzwArDo;
    }

    public void translate() throws DoParserException {
        this.mAidMaskDo = null;
        this.mAidRangeDo = null;
        this.mFilterConditionTagDo = null;
        this.mRoutingModeDo = null;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for FILTER_ENTRY_DO!");
        }
        do {
            VzwTlv temp = VzwTlv.parse(data, index);
            if (temp.getTag() == 195) {
                this.mAidMaskDo = new AidMaskDo(data, temp.getValueIndex(), temp.getValueLength());
                this.mAidMaskDo.translate();
            } else if (temp.getTag() == 194) {
                this.mAidRangeDo = new AidRangeDo(data, temp.getValueIndex(), temp.getValueLength());
                this.mAidRangeDo.translate();
            } else if (temp.getTag() == 178) {
                this.mRoutingModeDo = new RoutingModeDo(data, temp.getValueIndex(), temp.getValueLength());
                this.mRoutingModeDo.translate();
            } else if (temp.getTag() == 210) {
                this.mFilterConditionTagDo = new FilterConditionTagDo(data, temp.getValueIndex(), temp.getValueLength());
                this.mFilterConditionTagDo.translate();
            } else if (temp.getTag() == 227) {
                this.mVzwArDo = new VzwPermissionDo(data, temp.getValueIndex(), temp.getValueLength());
                this.mVzwArDo.translate();
            } else {
                throw new DoParserException("Invalid DO in FILTER_ENTRY_DO!");
            }
            index = temp.getValueIndex() + temp.getValueLength();
        } while (getValueIndex() + getValueLength() > index);
        if (this.mAidMaskDo == null || this.mAidRangeDo == null || this.mRoutingModeDo == null || this.mVzwArDo == null || this.mAidMaskDo.getAidMask().length != this.mAidRangeDo.getAidRange().length) {
            throw new DoParserException("missing DO in FILTER_ENTRY_DO!");
        }
    }
}
