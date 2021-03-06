package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class InetAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String hostAddress;
    private String hostName;
    private String canonicalName;
    @ManyToOne
    @JoinColumn(name = "network_interface_id", referencedColumnName = "id")
    @JsonBackReference
    private NetworkInterface networkInterface;
}
