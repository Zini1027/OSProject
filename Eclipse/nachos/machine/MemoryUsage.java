package nachos.machine;

import java.util.ArrayList;
import java.util.List;

public class MemoryUsage {

    public MemoryUsage() {

    }
    
    /**initialize the page list*/
    public void setMEMList(int numCompPages) {
        for (int i = 0; i < numCompPages; i++) {
            memUsageList.add(false);
        }
    }

    /**find a set of continuous empty pages*/
    public int findCtnPage(int numRequiredPage) {
        int startPage = -1;
      
        for(int i = 0; i < memUsageList.size(); i++){
            int counter = 0;
            if( !memUsageList.get(i) ) counter++;
            else counter = 0;
            
            if(counter == numRequiredPage) {
                startPage = i - numRequiredPage + 1;
                break;
            }
        }
        
        //if there is no enough continuous empty page, startPage == -1
        return startPage; 
    }
    
    
    /**mark the page which has been used by setting it to true*/
    public void setPage(int targetPage) {
        memUsageList.set(targetPage, true);
    }
    
    /**mark the page which has been released by setting it to false*/
    public void releasePage(int targetPage) {
        memUsageList.set(targetPage, false);
    }

    /** the usage of compressed section*/
    public List<Boolean> memUsageList = new ArrayList<Boolean>();

}
