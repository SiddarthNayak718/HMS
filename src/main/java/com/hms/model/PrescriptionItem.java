package com.hms.model;

public class PrescriptionItem {
    private int itemId;
    private int prescId;
    private int medicineId;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private int quantity;
    
    // Additional helpful field
    private String medicineName; 

    public PrescriptionItem() {}

    public PrescriptionItem(int itemId, int prescId, int medicineId, String dosage, String frequency, Integer durationDays, int quantity) {
        this.itemId = itemId;
        this.prescId = prescId;
        this.medicineId = medicineId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.quantity = quantity;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getPrescId() { return prescId; }
    public void setPrescId(int prescId) { this.prescId = prescId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
}
