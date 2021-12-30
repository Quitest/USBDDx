package ru.pel.usbddc.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NetworkInterface {
    private String name;
    private String displayName;
    private List<InetAddress> inetAddressList;

    @Getter
    @Setter
    public static class InetAddress {
        private String hostAddress;
        private String hostName;
        private String canonicalName;
    }
}
