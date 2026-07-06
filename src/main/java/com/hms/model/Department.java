// ============================================================
// FILE: com/hms/model/Department.java
// ============================================================
package com.hms.model;

public class Department {
    private int    deptId;
    private String deptName;
    private String description;
    private String location;

    public Department() {}
    public Department(int deptId, String deptName, String description, String location) {
        this.deptId = deptId; this.deptName = deptName;
        this.description = description; this.location = location;
    }

    public int    getDeptId()            { return deptId; }
    public void   setDeptId(int v)       { deptId = v; }
    public String getDeptName()          { return deptName; }
    public void   setDeptName(String v)  { deptName = v; }
    public String getDescription()       { return description; }
    public void   setDescription(String v){ description = v; }
    public String getLocation()          { return location; }
    public void   setLocation(String v)  { location = v; }

    @Override public String toString() { return deptName; }
}
