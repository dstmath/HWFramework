package com.vzw.nfc.dos;

public class ClfFilterDo extends VzwTlv {
    public static final int TAG = 254;
    private FilterEntryDo mFilterEntryAr = null;

    public ClfFilterDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, 254, valueIndex, valueLength);
    }

    public ClfFilterDo(FilterEntryDo filter_entry_do) {
        super(null, 254, 0, 0);
        this.mFilterEntryAr = filter_entry_do;
    }

    public FilterEntryDo getFilterEntryDo() {
        return this.mFilterEntryAr;
    }

    public void translate() throws DoParserException {
        this.mFilterEntryAr = null;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for CLF_FILTER_DO!");
        }
        do {
            VzwTlv temp = VzwTlv.parse(data, index);
            if (temp.getTag() == 161) {
                this.mFilterEntryAr = new FilterEntryDo(data, temp.getValueIndex(), temp.getValueLength());
                this.mFilterEntryAr.translate();
                index = temp.getValueIndex() + temp.getValueLength();
            } else {
                throw new DoParserException("Invalid FILTER_ENTRY_DO in CLF_FILTER_DO!");
            }
        } while (getValueIndex() + getValueLength() > index);
        if (this.mFilterEntryAr == null) {
            throw new DoParserException("Invalid FILTER_ENTRY_DO in CLF_FILTER_DO!");
        }
    }
}
