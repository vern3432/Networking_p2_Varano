package Sinkhole;

public class BlockObject {
    public String type;
    public String host;
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public BlockObject(String type, String host) {
        this.type = type;
        this.host = host;
    }



}
