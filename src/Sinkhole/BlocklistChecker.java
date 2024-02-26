package Sinkhole;
import java.util.ArrayList;
import java.util.HashMap;

public class BlocklistChecker {
    private HashMap<String, Boolean> blocklist;

    public BlocklistChecker(ArrayList<BlockObject> blockedDomains) {
        for(int i=0;i<blockedDomains.size();i++){
            BlockObject temp=blockedDomains.get(i);
            System.out.println("host:"+ temp.getHost() );
            System.out.println("type:"+ temp.getType() );


        }
        this.blocklist = new HashMap<>();

    }

    public boolean isBlocked(String domain) {
        return blocklist.containsKey(domain);
    }
}
