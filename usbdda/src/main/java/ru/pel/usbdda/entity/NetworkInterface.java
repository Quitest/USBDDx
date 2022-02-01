package ru.pel.usbdda.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class NetworkInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String displayName;

    @OneToMany(mappedBy = "networkInterface", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InetAddress> inetAddressList;

    @ManyToOne
    @JoinColumn(name = "os_info_id", referencedColumnName = "id")
    private OsInfo osInfo;
}
