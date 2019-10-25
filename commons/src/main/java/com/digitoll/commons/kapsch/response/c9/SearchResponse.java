package com.digitoll.commons.kapsch.response.c9;

import java.util.List;

public class SearchResponse {

    private boolean hasMoreRecords;
    private Integer lastRecord;

    private List<SearchVignette> eVignetteList;

    public boolean isHasMoreRecords() {
        return hasMoreRecords;
    }

    public void setHasMoreRecords(boolean hasMoreRecords) {
        this.hasMoreRecords = hasMoreRecords;
    }



    public List<SearchVignette> geteVignetteList() {
        return eVignetteList;
    }

    public void seteVignetteList(List<SearchVignette> eVignetteList) {
        this.eVignetteList = eVignetteList;
    }

    public Integer getLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(Integer lastRecord) {
        this.lastRecord = lastRecord;
    }
}
