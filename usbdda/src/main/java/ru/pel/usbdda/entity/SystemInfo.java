package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@Entity
public class SystemInfo {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "os_info_id")
    @JsonProperty("osInfo")
    private OSInfo osInfo;

    @JsonProperty("usbDeviceMap")
    @OneToMany
    private Map<String, USBDevice> usbDeviceMap;
}
