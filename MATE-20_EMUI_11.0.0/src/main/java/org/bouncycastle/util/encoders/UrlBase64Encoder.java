package org.bouncycastle.util.encoders;

public class UrlBase64Encoder extends Base64Encoder {
    public UrlBase64Encoder() {
        this.encodingTable[this.encodingTable.length - 2] = 45;
        this.encodingTable[this.encodingTable.length - 1] = 95;
        this.padding = 46;
        initialiseDecodingTable();
    }
}
