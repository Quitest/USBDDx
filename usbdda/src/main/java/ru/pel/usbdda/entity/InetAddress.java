package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class InetAddress {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    private String hostAddress;
    private String hostName;
    private String canonicalName;
    @ManyToOne
    @JoinColumn(name = "network_interface_id")
    private NetworkInterface networkInterface;
}
