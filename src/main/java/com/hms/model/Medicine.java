package com.hms.model;

import java.time.LocalDate;

// ============================================================
// Medicine
// ============================================================
public class Medicine {
    private int    medicineId;
    private String name;
    private String category;
    private double unitPrice;
    private int    stockQty;
    private String manufacturer;
    private LocalDate expiryDate;

    public Medicine() {}
    public Medicine(int medicineId, String name, String category,
                    double unitPrice, int stockQty,
                    String manufacturer, LocalDate expiryDate) {
        this.medicineId = medicineId; this.name = name; this.category = category;
        this.unitPrice = unitPrice; this.stockQty = stockQty;
        this.manufacturer = manufacturer; this.expiryDate = expiryDate;
    }

    public int    getMedicineId()           { return medicineId; }
    public void   setMedicineId(int v)      { medicineId = v; }
    public String getName()                 { return name; }
    public void   setName(String v)         { name = v; }
    public String getCategory()             { return category; }
    public void   setCategory(String v)     { category = v; }
    public double getUnitPrice()            { return unitPrice; }
    public void   setUnitPrice(double v)    { unitPrice = v; }
    public int    getStockQty()             { return stockQty; }
    public void   setStockQty(int v)        { stockQty = v; }
    public String getManufacturer()         { return manufacturer; }
    public void   setManufacturer(String v) { manufacturer = v; }
    public LocalDate getExpiryDate()        { return expiryDate; }
    public void   setExpiryDate(LocalDate v){ expiryDate = v; }
    @Override public String toString()      { return medicineId + " - " + name; }
}
