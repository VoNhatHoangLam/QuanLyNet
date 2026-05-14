package DTO;

import java.time.LocalDateTime;

public class UsageSessionDTO {
    private int sessionId;
    private int computerId;
    private String computerName;
    private double priceAtStart; // Giá máy chốt tại thời điểm bắt đầu
    private LocalDateTime startTime;
    private LocalDateTime endTime; 
    private double totalPrice;      
    private PaymentStatus paymentStatus;
    

    public UsageSessionDTO() {
    }
    
    public int getSessionId() {
        return sessionId;
    }
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    public int getComputerId() {
        return computerId;
    }
    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }
    public double getPriceAtStart() {
        return priceAtStart;
    }
    public void setPriceAtStart(double priceAtStart) {
        this.priceAtStart = priceAtStart;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    
}
