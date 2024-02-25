package Sinkhole;

public class TypeAAAAResourceRecord {
    private final String domainName;
    private final String ipAddressV6;

    public TypeAAAAResourceRecord(String domainName, String ipAddressV6) {
        this.domainName = domainName;
        this.ipAddressV6 = ipAddressV6;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getIpAddressV6() {
        return ipAddressV6;
    }
}
