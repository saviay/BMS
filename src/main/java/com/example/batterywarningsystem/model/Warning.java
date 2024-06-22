package com.example.batterywarningsystem.model;

public class Warning {
    private int vehicleNumber;
    private String batteryType;
    private String ruleName;
    private String level;

    // 无参构造函数
    public Warning() {}

    public Warning(int vehicleNumber, String batteryType, String ruleName, String level) {
        this.vehicleNumber = vehicleNumber;
        this.batteryType = batteryType;
        this.ruleName = ruleName;
        this.level = level;
    }

    public int getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(int vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
