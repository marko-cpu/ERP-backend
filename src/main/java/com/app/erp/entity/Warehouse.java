package com.app.erp.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "warehouses")
public class Warehouse implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "warehouse_name", nullable = false, unique = true)
    private String warehouseName;

    @Column(name = "location", nullable = false)
    private String location;

    // Konstruktori
    public Warehouse() {}

    public Warehouse(String warehouseName, String location) {
        this.warehouseName = warehouseName;
        this.location = location;
    }

    // Getteri i setteri
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Override toString
    @Override
    public String toString() {
        return String.format("Warehouse{id=%d, warehouseName='%s', location='%s'}",
                id, warehouseName, location);
    }

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Warehouse warehouse = (Warehouse) o;

        return id == warehouse.id &&
                warehouseName.equals(warehouse.warehouseName) &&
                location.equals(warehouse.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, warehouseName, location);
    }
}
