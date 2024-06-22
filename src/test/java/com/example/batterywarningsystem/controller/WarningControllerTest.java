package com.example.batterywarningsystem.controller;

import com.example.batterywarningsystem.model.Warning;
import com.example.batterywarningsystem.service.WarningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class WarningControllerTest {

    @InjectMocks
    private WarningController warningController;

    @Mock
    private WarningService warningService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(warningController).build();
    }

    // 测试 checkWarnings 方法，MockMvc 和 Mockito 来模拟 HTTP 请求和服务层的行为
    @Test
    void checkWarnings_ShouldReturnWarnings() throws Exception {
        List<Map<String, Object>> warnings = new ArrayList<>();
        Map<String, Object> warning = new HashMap<>();
        warning.put("carId", 3);
        warning.put("warnId", 1);
        warning.put("signal", "{\"Mx\":11.0,\"Mi\":9.6,\"Ix\":12.0,\"Ii\":11.7}");
        warnings.add(warning);

        Warning mockWarning = new Warning(3, "三元电池", "电压差报警", "0");
        when(warningService.doesVehicleExist(anyInt())).thenReturn(true);
        when(warningService.getWarnings(anyInt(), anyInt(), anyString(), anyDouble())).thenReturn(Collections.singletonList(mockWarning));

        mockMvc.perform(post("/api/warn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"carId\":3,\"warnId\":1,\"signal\":\"{\\\"Mx\\\":11.0,\\\"Mi\\\":9.6,\\\"Ix\\\":12.0,\\\"Ii\\\":11.7}\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("ok"))
                .andExpect(jsonPath("$.data[0].vehicleNumber").value(3))
                .andExpect(jsonPath("$.data[0].batteryType").value("三元电池"))
                .andExpect(jsonPath("$.data[0].warnName").value("电压差报警"))
                .andExpect(jsonPath("$.data[0].warnLevel").value("0"));
    }
}