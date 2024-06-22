package com.example.batterywarningsystem.service;

import com.example.batterywarningsystem.model.Warning;
import com.example.batterywarningsystem.service.WarningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WarningServiceTest {

    @InjectMocks
    private WarningService warningService;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // 测试 doesVehicleExist 方法
    @Test
    void testDoesVehicleExist() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        boolean result = warningService.doesVehicleExist(1);
        assertTrue(result);

    }

    // 测试 getWarnings 方法
    @Test
    void testGetWarnings() throws SQLException {
        String cacheKey = "warning:1:1:Mx-Mi:1.0";
        List<Warning> cachedWarnings = new ArrayList<>();
        cachedWarnings.add(new Warning(1, "三元电池", "电压差报警", "0"));

        when(redisTemplate.opsForValue().get(cacheKey)).thenReturn(cachedWarnings);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("vehicleNumber")).thenReturn(1);
        when(resultSet.getString("batteryType")).thenReturn("三元电池");
        when(resultSet.getString("ruleName")).thenReturn("电压差报警");
        when(resultSet.getString("level")).thenReturn("0");

        // First query (should hit the database)
        long startTime = System.currentTimeMillis();
        List<Warning> warnings = warningService.getWarnings(1, 1, "Mx-Mi", 1.0);
        long endTime = System.currentTimeMillis();
        long dbQueryTime = endTime - startTime;
        assertNotNull(warnings);
        assertEquals(1, warnings.size());

        // Second query (should hit the cache)
        startTime = System.currentTimeMillis();
        warnings = warningService.getWarnings(1, 1, "Mx-Mi", 1.0);
        endTime = System.currentTimeMillis();
        long cacheQueryTime = endTime - startTime;
        assertNotNull(warnings);
        assertEquals(1, warnings.size());

        System.out.println("Database query time: " + dbQueryTime + " ms");
        System.out.println("Cache query time: " + cacheQueryTime + " ms");

        assertTrue(cacheQueryTime < dbQueryTime);
    }


    // 性能测试，测试规则接口的性能
    @Test
    void testGetWarningsPerformance() throws SQLException {
        int totalRequests = 1000;
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getInt("vehicleNumber")).thenReturn(1);
            when(resultSet.getString("batteryType")).thenReturn("三元电池");
            when(resultSet.getString("ruleName")).thenReturn("电压差报警");
            when(resultSet.getString("level")).thenReturn("0");

            long startTime = System.nanoTime();
            List<Warning> warnings = warningService.getWarnings(1, 1, "Mx-Mi", 0.6);
            long endTime = System.nanoTime();

            responseTimes.add((endTime - startTime) / 1_000_000); // 转换为毫秒
        }

        Collections.sort(responseTimes);
        long p99ResponseTime = responseTimes.get((int) (totalRequests * 0.99) - 1);

        System.out.println("P99 Response Time: " + p99ResponseTime + " ms");
        assertTrue(p99ResponseTime < 1000, "P99 Response Time should be less than 1000 ms");
    }

    // 测试规则报警的成功率，模拟信号的计算逻辑
    @Test
    void testWarningSuccessRate() throws SQLException {
        // 设置模拟信号数据列表
        List<double[]> signalData = new ArrayList<>();
        signalData.add(new double[]{1.4, 0.3}); // Mx-Mi, Ix-Ii
        signalData.add(new double[]{3.0, 1.0});
        signalData.add(new double[]{0.6, 0.2});
        signalData.add(new double[]{1.2, 0.4});
        signalData.add(new double[]{5.0, 3.0});

        int totalTests = signalData.size();
        int successfulWarnings = 0;

        // 对每个信号模拟警告服务
        for (double[] signal : signalData) {
            double mxMi = signal[0];
            double ixIi = signal[1];

            // Mock for Mx-Mi
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getInt("vehicleNumber")).thenReturn(3);
            when(resultSet.getString("batteryType")).thenReturn("三元电池");
            when(resultSet.getString("ruleName")).thenReturn("电压差报警");
            when(resultSet.getString("level")).thenReturn("2");

            List<Warning> warnings = warningService.getWarnings(3, 0, "Mx-Mi", mxMi);
            if (!warnings.isEmpty()) {
                successfulWarnings++;
            }

            // Mock for Ix-Ii
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getInt("vehicleNumber")).thenReturn(3);
            when(resultSet.getString("batteryType")).thenReturn("三元电池");
            when(resultSet.getString("ruleName")).thenReturn("电流差报警");
            when(resultSet.getString("level")).thenReturn("2");

            warnings = warningService.getWarnings(3, 0, "Ix-Ii", ixIi);
            if (!warnings.isEmpty()) {
                successfulWarnings++;
            }
        }

        double successRate = (double) successfulWarnings / (totalTests * 2); // 每个信号集有两个测试（Mx-Mi 和 Ix-Ii）
        String successRatePercentage = String.format("%.2f", successRate * 100) + "%";
        System.out.println("预警成功率: " + successRatePercentage);
        assertTrue(successRate >= 0.9, "预警成功率应至少为 90%");
    }
}