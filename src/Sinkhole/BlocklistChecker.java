package Sinkhole;
import java.util.ArrayList;
import java.util.HashMap;

public class BlocklistChecker {
    private HashMap<String, Boolean> blocklist;

    public BlocklistChecker(ArrayList<String> blockedDomains) {
        this.blocklist = new HashMap<>();
        for (String domain : blockedDomains) {
            blocklist.put(domain, true);
        }
    }

    public boolean isBlocked(String domain) {
        return blocklist.containsKey(domain);
    }
}
