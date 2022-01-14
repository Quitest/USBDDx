package ru.pel.usbdda.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NetworkInterface {

    @JsonProperty("name")
    private String name;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("inetAddressList")
    private List<InetAddress> inetAddressList;

    @Getter
    @Setter
    public static class InetAddress {
        @JsonProperty("hostAddress")
        private String hostAddress;

        @JsonProperty("hostName")
        private String hostName;

        @JsonProperty("canonicalName")
        private String canonicalName;
    }
}
