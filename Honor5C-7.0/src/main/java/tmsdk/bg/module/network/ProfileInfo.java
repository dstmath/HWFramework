package tmsdk.bg.module.network;

/* compiled from: Unknown */
public class ProfileInfo {
    public int brand;
    public String carry;
    public int city;
    public String imsi;
    public int province;

    public ProfileInfo() {
        this.imsi = "";
        this.carry = "";
        this.brand = -1;
        this.city = -1;
        this.province = -1;
    }

    public String toString() {
        return "imsi:[" + this.imsi + "]province:[" + this.province + "]city:[" + this.city + "]carry:[" + this.carry + "]brand:[" + this.brand + "]";
    }
}
