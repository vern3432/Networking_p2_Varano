package Sinkhole;

public class BlockDNS{
    private String host;
    private String type;

    public BlockDNS(String host, String type){
        this.host = host;
        this.type = type;
    }

    public String getHost(){
        return host;
    }

    public String getType(){
        return type;
    }


    //Override toString method for debugging/logging purposes
    @Override
    public String toString(){
        return "BlockDNS{" + "host='" + ", \'" + "type='" + '\'' + '}';
    }
}