package com.hms.model;

public class LabTest {
    private int testId;
    private String testName;
    private String description;
    private double price;
    private String normalRange;

    public LabTest() {}

    public LabTest(int testId, String testName, String description, double price, String normalRange) {
        this.testId = testId;
        this.testName = testName;
        this.description = description;
        this.price = price;
        this.normalRange = normalRange;
    }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }
    
    @Override
    public String toString() {
        return testName + " (₹" + price + ")";
    }
}
