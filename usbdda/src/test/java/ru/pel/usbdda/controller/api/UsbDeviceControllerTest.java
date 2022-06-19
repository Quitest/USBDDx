package ru.pel.usbdda.controller.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.pel.usbdda.entity.USBDevice;
import ru.pel.usbdda.exception.NoSuchDeviceException;
import ru.pel.usbdda.service.UsbDeviceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsbDeviceControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UsbDeviceService usbDeviceService;

    @Test
    @DisplayName("Запрос всех устройств. Ответ содержит список устройств")
    void responseOnGetAllDevices_expectedListOfUsbDevices() throws Exception {
        List<USBDevice> usbDeviceList = new ArrayList<>();
        USBDevice usbDevice1 = new USBDevice();
        usbDevice1.setSerial("1");
        USBDevice usbDevice2 = new USBDevice();
        usbDevice2.setSerial("2");
        USBDevice usbDevice3 = new USBDevice();
        usbDevice3.setSerial("3");
        usbDeviceList.add(usbDevice1);
        usbDeviceList.add(usbDevice2);
        usbDeviceList.add(usbDevice3);
        Mockito.when(usbDeviceService.getAllDevices()).thenReturn(usbDeviceList);

        mockMvc.perform(get("/api/usbdevices/all").accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$._embedded.uSBDeviceDtoes.*", hasSize(3)),
                        jsonPath("$._embedded.uSBDeviceDtoes[*].serial", hasItems("3", "2", "1"))
                );
    }

    @Test
    @DisplayName("Ответ содержит корректные _links: self, usbDevices (root)")
    void responseOnGetUsbDeviceBySerialContainsHAL() throws Exception {
        String serialNo = "123456SeRiAl";
        USBDevice usbDevice = new USBDevice();
        usbDevice.setSerial(serialNo);
        Mockito.when(usbDeviceService.getBySerial(serialNo)).thenReturn(Optional.of(usbDevice));

        mockMvc.perform(get("/api/usbdevices/" + serialNo).accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("serial", is(serialNo)),
                        jsonPath("$.._links.self.href", hasItem(containsString("/api/usbdevices/" + serialNo))),
                        jsonPath("$.._links.usbDevices.href", hasItems(containsString("/api/usbdevices/")))
                );
    }

    @Test
    @DisplayName("Запрос устройства с несуществующим серийником. Ожидается NoSuchDeviceException")
    void responseOnGetUsbDeviceBySerial_expectedNoSuchDeviceException() throws Exception {
        String notExistSerialNo = "1";
        Mockito.when(usbDeviceService.getBySerial(notExistSerialNo)).thenThrow(new NoSuchDeviceException(notExistSerialNo));

        mockMvc.perform(get("/api/usbdevices/" + notExistSerialNo))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("title", any(String.class)),
                        jsonPath("detail", any(String.class))
                );
    }

    @Test
    @DisplayName("Запрос любого валидного серийника. Ожидается статус 200 OK")
    void responseOnGetUsbDeviceBySerial_expectedStatusOk() throws Exception {
        Mockito.doReturn(Optional.of(new USBDevice())).when(usbDeviceService).getBySerial(anyString());

        mockMvc.perform(get("/api/usbdevices/anySerial").accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("serial", nullValue())
                );
    }
}