package com.example.batterywarningsystem.controller;

import com.example.batterywarningsystem.model.Warning;
import com.example.batterywarningsystem.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/warn")
public class WarningController {

    @Autowired
    private WarningService warningService;

    // 检查报警信息
    @PostMapping
    public Map<String, Object> checkWarnings(@RequestBody List<Map<String, Object>> warnings) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        // 遍历每一条报警信息
        for (Map<String, Object> warning : warnings) {
            Integer carId = (Integer) warning.get("carId");
            if (carId == null || !warningService.doesVehicleExist(carId)) {
                continue;
            }
            // 获取报警信息中的 warnId 和 signal
            Integer warnId = (Integer) warning.getOrDefault("warnId", 0);
            String signal = (String) warning.get("signal");
            Map<String, Double> signalMap = parseSignal(signal);

            // 用于存储报警信息
            List<Warning> results = new ArrayList<>();

            // 对于电压差和电流差的组合
            if (signalMap.containsKey("Mx") && signalMap.containsKey("Mi")) {
                String signalType = "Mx-Mi";
                double signalValue = signalMap.get("Mx") - signalMap.get("Mi");
                results.addAll(warningService.getWarnings(carId, warnId, signalType, signalValue));
            }
            if (signalMap.containsKey("Ix") && signalMap.containsKey("Ii")) {
                String signalType = "Ix-Ii";
                double signalValue = signalMap.get("Ix") - signalMap.get("Ii");
                results.addAll(warningService.getWarnings(carId, warnId, signalType, signalValue));
            }

            for (Warning result : results) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("vehicleNumber", result.getVehicleNumber());
                resultMap.put("batteryType", result.getBatteryType());
                resultMap.put("warnName", result.getRuleName());
                resultMap.put("warnLevel", result.getLevel());
                data.add(resultMap);
            }
        }

        response.put("status", 200);
        response.put("msg", "ok");
        response.put("data", data);

        return response;
    }

    // 解析信号
    private Map<String, Double> parseSignal(String signal) {
        Map<String, Double> signalMap = new HashMap<>();
        signal = signal.replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = signal.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            signalMap.put(keyValue[0].trim(), Double.parseDouble(keyValue[1].trim()));
        }

        return signalMap;
    }
}
