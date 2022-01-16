package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class InetAddress {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonProperty("hostAddress")
    private String hostAddress;

    @JsonProperty("hostName")
    private String hostName;

    @JsonProperty("canonicalName")
    private String canonicalName;
}
