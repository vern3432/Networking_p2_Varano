package Sinkhole;
import java.util.ArrayList;
import java.util.HashMap;

public class BlocklistChecker {


    
    @SuppressWarnings("rawtypes")
    public HashMap<String, ArrayList> blocklist;

    @SuppressWarnings("unchecked")
    public BlocklistChecker(ArrayList<BlockObject> blockedDomains) {
        this.blocklist = new HashMap<>();

        for(int i=0;i<blockedDomains.size();i++){
            BlockObject temp=blockedDomains.get(i);

            String domain=temp.getHost();
            String type=temp.getType();
            // System.out.println("host:"+ domain );
            // System.out.println("type:"+ type );
            if(blocklist.containsKey(domain)){
                this.blocklist.get(domain.trim()).add(type.trim());
            }
            else{
                ArrayList<String> tempList =new ArrayList<>();
                tempList.add(type);
                this.blocklist.put(domain,tempList);

            }

        }
    

    }

    public void addBlockSite(BlockObject newBlockObject){
        String domain=newBlockObject.getHost().toLowerCase();
        String type=newBlockObject.getType().toLowerCase() ;
        
        if(blocklist.containsKey(domain)){
            this.blocklist.get(domain).add(type);
        }

        else{
            ArrayList<String> tempList =new ArrayList<>();
            tempList.add(type);
            this.blocklist.put(domain,tempList);

        }


    }

    public boolean isBlocked(String domain,String type) {
        //this needs update
        if(blocklist.containsKey(domain.toLowerCase())&&blocklist.get(domain).contains(type.toLowerCase())){
            return true;

        }
        if (blocklist.containsKey(domain.toLowerCase())&&blocklist.get(domain).contains(type.toUpperCase())){
            return true;

        }

        return false;
    }



}
