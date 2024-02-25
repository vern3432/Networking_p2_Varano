package Sinkhole;

public class TypeAResourceRecord {
    private final String domainName;
    private final String ipAddress;

    public TypeAResourceRecord(String domainName, String ipAddress) {
        this.domainName = domainName;
        this.ipAddress = ipAddress;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
