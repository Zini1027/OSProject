package nachos.machine;

import java.util.ArrayList;
import java.util.List;

public class MemoryUsage {

    /**
     * Initialize the page list memUsageList. memUsageList tracks the usage of physical memory.
     * true: the page has been used false: the page is free section
     * */
    public MemoryUsage() {
        for (int i = 0; i < numPhysPages; i++) {
            memUsageList.add(false);
        }
    }

    /**
     * Initialize the page list memUsageList. memUsageList tracks the usage of physical memory.
     * true: the page has been used false: the page is free
     * 
     * numPages: the number of pages in physicall memory page: the starting page of compressed
     * section
     * */
    public void setMEMList(int numPages) {
        for (int i = 0; i < numPages; i++) {
            memUsageList.add(false);
        }
    }

    /** find one empty page */
    public int allocatePageInUncomp() {
        int page = -1;
        for (int i = 0; i < compStartPage; i++) {
            if (!memUsageList.get(i)) {
                page = i;
                break;
            }
        }
        // if there is no empty page, page == -1
        return page;
    }

    public List<Integer> allocateMultiPagesUncomp(int numPages) {
        List<Integer> freePages = new ArrayList<Integer>();
        for (int i = 0; i < compStartPage; i++) {
            if (freePages.size() < numPages && !memUsageList.get(i)) {
                freePages.add(i);
            }
        }

        if (freePages.size() == numPages) {
            return freePages;
        } else {
            return null;
        }
    }

    /**
     * find a set of continuous empty pages, return the start ppn
     * */
    public int allocateCtnPageInComp(int numRequiredPage) {
        int startPage = -1;
        int counter = 0;

        for (int i = compStartPage; i < memUsageList.size(); i++) {
            if (!memUsageList.get(i))
                counter++;
            else
                counter = 0;

            if (counter == numRequiredPage) {
                startPage = i - numRequiredPage + 1;
                break;
            }
        }

        // if there is no enough continuous empty page, startPage == -1
        return startPage;
    }

    /** mark the page which has been used by setting it to true */
    public void setPage(int targetPage) {
        memUsageList.set(targetPage, true);
    }

    /** mark the page which has been released by setting it to false */
    public void releasePage(int targetPage) {
        memUsageList.set(targetPage, false);
    }

    private int numPhysPages = Machine.processor().getNumPhysPages();

    /** the usage of compressed section */
    private List<Boolean> memUsageList = new ArrayList<Boolean>();

    /** which page is the start of compressed section */
    private int compStartPage = Machine.processor().getCompressMemStartingPPN();

}
