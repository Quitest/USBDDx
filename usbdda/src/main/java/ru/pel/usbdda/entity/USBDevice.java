package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class USBDevice {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonProperty("vid")
    private String vid;

    @JsonProperty("serial")
    private String serial;

    @JsonProperty("dateTimeFirstInstall")
    private LocalDateTime dateTimeFirstInstall;

    @JsonProperty("volumeName")
    private String volumeName;

    @JsonProperty("serialOSGenerated")
    private boolean serialOSGenerated;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("pid")
    private String pid;

    @JsonProperty("userAccountsList")
    private List<UserProfile> userAccountsList;

    @JsonProperty("vendorName")
    private String vendorName;

    @JsonProperty("friendlyName")
    private String friendlyName;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("revision")
    private String revision;
}