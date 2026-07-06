package com.hms.model;

import java.sql.Date;

public class TestOrder {
    private int orderId;
    private int visitId;
    private int patientId;
    private int testId;
    private int orderedBy; // doctor_id
    private Date orderDate;
    private String resultValue;
    private Date resultDate;
    private String status;

    private String testName;
    private String patientName;

    public TestOrder() {}

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getVisitId() { return visitId; }
    public void setVisitId(int visitId) { this.visitId = visitId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }

    public int getOrderedBy() { return orderedBy; }
    public void setOrderedBy(int orderedBy) { this.orderedBy = orderedBy; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }

    public Date getResultDate() { return resultDate; }
    public void setResultDate(Date resultDate) { this.resultDate = resultDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
