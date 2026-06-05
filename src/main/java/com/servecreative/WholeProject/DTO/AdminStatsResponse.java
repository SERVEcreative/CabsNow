package com.servecreative.WholeProject.DTO;

public class AdminStatsResponse {
    private long totalUsers;
    private long totalDrivers;
    private long totalDuties;
    private long completedDuties;
    private double totalRevenue;
    private long pendingPayments;

    public AdminStatsResponse() {}

    public AdminStatsResponse(long totalUsers, long totalDrivers, long totalDuties,
                              long completedDuties, double totalRevenue, long pendingPayments) {
        this.totalUsers = totalUsers;
        this.totalDrivers = totalDrivers;
        this.totalDuties = totalDuties;
        this.completedDuties = completedDuties;
        this.totalRevenue = totalRevenue;
        this.pendingPayments = pendingPayments;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalDrivers() { return totalDrivers; }
    public long getTotalDuties() { return totalDuties; }
    public long getCompletedDuties() { return completedDuties; }
    public double getTotalRevenue() { return totalRevenue; }
    public long getPendingPayments() { return pendingPayments; }
}
