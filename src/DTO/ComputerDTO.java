/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

/**
 *
 * @author ripni
 */
public class ComputerDTO {
    private int computerId;
    private String computerName;
    private double pricePerHour;
    private CompStatus status;
    
    public ComputerDTO() {}

    public ComputerDTO(int computerId, String computerName, double pricePerHour, CompStatus status) {
        this.computerId = computerId;
        this.computerName = computerName;
        this.pricePerHour = pricePerHour;
        this.status = status;
    }

    public int getComputerId() {
        return computerId;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public CompStatus getStatus() {
        return status;
    }

    public void setStatus(CompStatus status) {
        this.status = status;
    }
    
    
}
