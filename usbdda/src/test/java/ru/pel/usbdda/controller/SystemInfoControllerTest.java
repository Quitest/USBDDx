package ru.pel.usbdda.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest
class SystemInfoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private String postJson = """
                        {
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
                        "usbDeviceList" : [
                {
                    "friendlyName" : "ADATA USB Flash Drive USB Device",
                        "guid" : "{5405623b-31de-11e5-8295-54a0503930d0}",
                        "pid" : "312b",
                        "productName" : "Superior S102 Pro",
                        "serial" : "1492710242260098",
                        "vendorName" : "A-DATA Technology Co., Ltd.",
                        "vid" : "125f",
                        "volumeName" : "TESTER_FlashDrive",
                        "revision" : "0.00",
                        "dateTimeFirstInstall" : [ 2021, 8, 7, 16, 27, 36 ],
                    "userAccountsList" : [ ],
                    "serialOSGenerated" : false
                },
                {
                    "friendlyName" : "",
                        "guid" : "{8a624430-8daa-11e7-82dc-54a0503930d0}",
                        "pid" : "",
                        "productName" : "",
                        "serial" : "EFF732B1",
                        "vendorName" : "",
                        "vid" : "",
                        "volumeName" : "UsbStorage",
                        "revision" : "",
                        "dateTimeFirstInstall" : [ -999999999, 1, 1, 0, 0 ],
                    "userAccountsList" : [ ],
                    "serialOSGenerated" : false
                }
              ]
            }""";


    @Test
    void postSystemInfo() throws Exception {
        mockMvc.perform(post("/systeminfo").contentType(MediaType.APPLICATION_JSON).content(postJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":1,")));
    }
}