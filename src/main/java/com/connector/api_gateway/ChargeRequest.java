package com.connector.api_gateway;

public class ChargeRequest {
    String correlationID;
    String msisdn;
    String PartnerID;
    String chargableAmount;
    String ProductID;
    String remarks;
    String TransactionID;

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getPartnerID() {
        return PartnerID;
    }

    public void setPartnerID(String partnerID) {
        PartnerID = partnerID;
    }

    public String getChargableAmount() {
        return chargableAmount;
    }

    public void setChargableAmount(String chargableAmount) {
        this.chargableAmount = chargableAmount;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTransactionID() {
        return TransactionID;
    }

    public void setTransactionID(String transactionID) {
        TransactionID = transactionID;
    }

}
