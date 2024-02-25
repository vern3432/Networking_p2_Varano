package Sinkhole;

public class DNSQuery {
    private final String domainName;
    private final int queryType;

    public DNSQuery(String domainName, int queryType) {
        this.domainName = domainName;
        this.queryType = queryType;
    }

    public String getDomainName() {
        return domainName;
    }

    public int getQueryType() {
        return queryType;
    }
}
