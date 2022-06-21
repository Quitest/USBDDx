package ru.pel.usbdda.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.pel.usbdda.entity.SystemInfo;
import ru.pel.usbdda.service.impl.SystemInfoServiceImpl;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SystemInfoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemInfoServiceImpl service;

    private String postJson = """
            {
             "uuid" : "edcba098-7654-3210-fedc-ab9876543210",
             "scannedWithAdminPrivileges": "true",
             "osInfo" : {
                "osId" : "01234567-89ab-cdef-0123-4567890abcde",
                "osName" : "Windows 10",
                "osVersion" : 10.0,
                "osArch" : "amd64",
                "tmpdir" : "file:///C:/Users/Tester/AppData/Local/Temp/",
                "username" : "Tester",
                "homeDir" : "file:///C:/Users/Tester/",
                "currentDir" : "file:///D:/current/Project/dir/",
                "systemRoot" : "file:///C:/WINDOWS/",
                "computerName" : "TESTNOTE",
                "networkInterfaceList" : [ {
                    "name" : "lo",
                    "displayName" : "Software Loopback Interface 1",
                    "inetAddressList" : [ {
                    "hostAddress" : "127.0.0.1",
                    "hostName" : "127.0.0.1",
                    "canonicalName" : "127.0.0.1"
             }, {
             "hostAddress" : "0:0:0:0:0:0:0:1",
             "hostName" : "0:0:0:0:0:0:0:1",
             "canonicalName" : "0:0:0:0:0:0:0:1"
             } ]
             }, {
             "name" : "wlan1",
             "displayName" : "Qualcomm Atheros Wireless Network Adapter",
             "inetAddressList" : [ {
             "hostAddress" : "192.168.0.1",
             "hostName" : "host.d.internal",
             "canonicalName" : "host.d.internal"
             }, {
             "hostAddress" : "IPv6_testAddress%wlan1",
             "hostName" : "IPv6_testAddress%wlan1",
             "canonicalName" : "IPv6_testAddress%wlan1"
             } ]
             }, {
             "name" : "eth1",
             "displayName" : "Realtek PCIe GBE Family Controller",
             "inetAddressList" : [ {
             "hostAddress" : "IPv6_testAddress%eth1",
             "hostName" : "IPv6_testAddress%eth1",
             "canonicalName" : "IPv6_testAddress%eth1"
             } ]
             } ]
             },
             "usbDeviceList" : [ {
                  "friendlyName" : "Flash Drive",
                  "guid" : "{87654321-90ab-cdef-1234-567890abcdef}",
                  "pid" : "312b",
                  "productName" : "S102 Pro",
                  "productNameByRegistry" : "USB_Flash_Drive",
                  "serial" : "ser1234567890ser",
                  "vendorName" : "DATA Technology Co., Ltd.",
                  "vendorNameByRegistry" : "Super Puper Vendor",
                  "vid" : "125f",
                  "revision" : "0.00",
                  "diskId" : "{11111111-2222-3333-4444-555555555555}",
                  "dateTimeFirstInstall" : [ 2021, 8, 7, 16, 27, 36 ],
                  "volumeIdList" : [ 1, 1, 4165588826 ],
                  "userAccountsList" : [ {
                    "username" : "Tester",
                    "profileImagePath" : "file:///C:/Users/Tester/",
                    "securityId" : "S-1-5-21-1234567890-098765432-1234567890-1001"
                  },
                   {
                    "username" : "Tester2",
                    "profileImagePath" : "file:///C:/Users/Tester2/",
                    "securityId" : "S-1-5-21-1234567890-098765432-1234567890-1002"
                  }],
                  "volumeLabelList" : [ "TESTER", "rus", "Z", "spring" ],
                  "serialOSGenerated" : false
                },
             {
                  "friendlyName" : "USB Flash Drive USB Device",
                  "guid" : "{12345678-90ab-cdef-1234-567890abcdef}",
                  "pid" : "312b",
                  "productName" : "Superior S102 Pro",
                  "productNameByRegistry" : "USB_Flash_Drive",
                  "serial" : "123456SeRiAl",
                  "vendorName" : "DATA Technology Co., Ltd.",
                  "vendorNameByRegistry" : "DATA",
                  "vid" : "125f",
                  "revision" : "0.00",
                  "diskId" : "{11111111-2222-3333-4444-555555555555}",
                  "dateTimeFirstInstall" : [ 2021, 8, 7, 16, 27, 36 ],
                  "volumeIdList" : [ 1, 1, 4165588826 ],
                  "userAccountsList" : [ {
                    "username" : "Tester",
                    "profileImagePath" : "file:///C:/Users/Tester/",
                    "securityId" : "S-1-5-21-1234567890-098765432-1234567890-1001"
                  } ],
                  "volumeLabelList" : [ "TESTER", "русская", "Z", "весна" ],
                  "serialOSGenerated" : false
                }
              ]
             }""";

    @Test
    @DisplayName("Запрос существующей сущности. Ожидается статус 200 OK и JSON-ответ с заданным id")
    void getExistsSystemInfo_expectedStatus200OK() throws Exception {
        long id = 123L;
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setId(id);
        Mockito.when(service.getByKey(id)).thenReturn(Optional.of(systemInfo));

        mockMvc.perform(get("/systeminfo/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("id", is(123))
                );
    }

    @Test
    @DisplayName("Запрос несуществующей сущности. Ожидается NoSuchSystemInfoException")
    void getNotExistsSystemInfo_expectedNoSuchSystemInfo() throws Exception {
        long id = 999999999L;
        String exceptionMsg = "Не существует системы с id = " + id;
        Mockito.when(service.getByKey(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/systeminfo/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("detail", is(exceptionMsg))
                );
    }

    @Test
    void postSystemInfo() throws Exception {
        mockMvc.perform(post("/systeminfo").contentType(MediaType.APPLICATION_JSON).content(postJson))
                .andExpect(status().isOk())
                .andExpectAll(content().string(containsString("\"id\":1,")),
                        content().string(containsString("\"uuid\":\"edcba098-7654-3210-fedc-ab9876543210\",")));
    }

    @Test
    void postSystemInfoHasPropertyScannedWithAdminPrivileges() throws Exception {
        mockMvc.perform(post("/systeminfo").contentType(MediaType.APPLICATION_JSON).content(postJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"scannedWithAdminPrivileges\": \"true\"")));
    }
}