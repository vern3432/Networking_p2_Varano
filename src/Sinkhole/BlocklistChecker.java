package Sinkhole;
import java.util.ArrayList;
import java.util.HashMap;

public class BlocklistChecker {










    public boolean isBlocked(String domain) {
        //this needs update
        if(blocklist.containsKey(domain)){
            return true;

        }

        return false;
    }
    
    @SuppressWarnings("rawtypes")
    private HashMap<String, ArrayList> blocklist;

    @SuppressWarnings("unchecked")
    public BlocklistChecker(ArrayList<BlockObject> blockedDomains) {
        this.blocklist = new HashMap<>();

        for(int i=0;i<blockedDomains.size();i++){
            BlockObject temp=blockedDomains.get(i);

            String domain=temp.getHost();
            String type=temp.getType();
            System.out.println("host:"+ domain );
            System.out.println("type:"+ type );
            if(blocklist.containsKey(domain)){
                blocklist.get(domain).add(type);
            }
            else{
                ArrayList<String> tempList =new ArrayList<>();
                tempList.add(type);
                blocklist.put(domain,tempList);

            }

        }
    

    }

    public boolean isBlocked(String domain,String type) {
        //this needs update
        if(blocklist.containsKey(domain)&&blocklist.get(domain).contains(type)){
            return true;

        }

        return false;
    }



}
