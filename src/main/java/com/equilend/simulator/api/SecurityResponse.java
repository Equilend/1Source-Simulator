package com.equilend.simulator.api;

import java.util.List;

public class SecurityResponse {

    public class SecurityData {

        private String securityid;
        private Double price;
        private Double avgfee;
        private Double minfee;
        private Double maxfee;
        private Double avgrebate;
        private Double minrebate;
        private Double maxrebate;
        private String bucket;

        public SecurityData() {

        }

        public String getSecurityid() {
            return securityid;
        }

        public void setSecurityid(String securityid) {
            this.securityid = securityid;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getAvgfee() {
            return avgfee;
        }

        public void setAvgfee(Double avgfee) {
            this.avgfee = avgfee;
        }

        public Double getMinfee() {
            return minfee;
        }

        public void setMinfee(Double minfee) {
            this.minfee = minfee;
        }

        public Double getMaxfee() {
            return maxfee;
        }

        public void setMaxfee(Double maxfee) {
            this.maxfee = maxfee;
        }

        public Double getAvgrebate() {
            return avgrebate;
        }

        public void setAvgrebate(Double avgrebate) {
            this.avgrebate = avgrebate;
        }

        public Double getMinrebate() {
            return minrebate;
        }

        public void setMinrebate(Double minrebate) {
            this.minrebate = minrebate;
        }

        public Double getMaxrebate() {
            return maxrebate;
        }

        public void setMaxrebate(Double maxrebate) {
            this.maxrebate = maxrebate;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }


    }

    public class Result {

        private String businessdate;
        private List<SecurityData> securitydata;

        public Result() {

        }

        public String getBusinessdate() {
            return businessdate;
        }

        public void setBusinessdate(String businessdate) {
            this.businessdate = businessdate;
        }

        public List<SecurityData> getSecuritydata() {
            return securitydata;
        }

        public void setSecuritydata(List<SecurityData> securitydata) {
            this.securitydata = securitydata;
        }

    }

    private int code;
    private List<Result> results;

    public SecurityResponse() {

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    private SecurityData getSecurityData() {
        if (results == null || results.size() == 0 || results.get(0) == null) {
            return null;
        }
        Result priceResult = results.get(0);
        if (priceResult.getSecuritydata() == null || priceResult.getSecuritydata().size() == 0
            || priceResult.getSecuritydata().get(0) == null) {
            return null;
        }
        SecurityData securityData = priceResult.getSecuritydata().get(0);
        return securityData;
    }

    public double getPrice() {
        SecurityData securityData = getSecurityData();
        return securityData == null ? 0.0 : securityData.getPrice();
    }

    public double getAvgFee() {
        SecurityData securityData = getSecurityData();
        return securityData == null ? 0.0 : securityData.getAvgfee();
    }

    public double getAvgRebate() {
        SecurityData securityData = getSecurityData();
        return securityData == null ? 0.0 : securityData.getAvgrebate();
    }

}