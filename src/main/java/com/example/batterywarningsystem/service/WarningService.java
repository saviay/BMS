package com.example.batterywarningsystem.service;

import com.example.batterywarningsystem.model.Warning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WarningService {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/battery_warning_system";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "zy310827";


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //检查车辆是否存在
    public boolean doesVehicleExist(int vehicleNumber) {
        String query = "SELECT COUNT(*) FROM vehicle_info WHERE vehicle_number = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, vehicleNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //获取报警信息
    public List<Warning> getWarnings(int vehicleNumber, int warnId, String signalType, double signalValue) {
        String cacheKey = String.format("warning:%d:%d:%s:%.2f", vehicleNumber, warnId, signalType, signalValue);
        List<Warning> warnings = (List<Warning>) redisTemplate.opsForValue().get(cacheKey);

        if (warnings == null) {
            warnings = queryWarningsFromDatabase(vehicleNumber, warnId, signalType, signalValue);
            redisTemplate.opsForValue().set(cacheKey, warnings, 12, TimeUnit.HOURS); // 缓存12小时
        }
        return warnings;
    }
    //从数据库中查询报警信息
    private List<Warning> queryWarningsFromDatabase(int vehicleNumber, int warnId, String signalType, double signalValue) {
        List<Warning> results = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             CallableStatement stmt = connection.prepareCall("{CALL getVehicleWarnings(?, ?, ?, ?)}")) {

            stmt.setInt(1, vehicleNumber);
            stmt.setInt(2, warnId);
            stmt.setString(3, signalType);
            stmt.setDouble(4, signalValue);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Warning warning = new Warning(
                        rs.getInt("vehicleNumber"),
                        rs.getString("batteryType"),
                        rs.getString("ruleName"),
                        rs.getString("level")
                );
                results.add(warning);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}