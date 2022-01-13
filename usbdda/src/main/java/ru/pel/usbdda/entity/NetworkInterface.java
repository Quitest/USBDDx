package ru.pel.usbdda.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NetworkInterface {

    @JsonProperty("name")
    private String name;

    @JsonProperty("displayName")
    private String displayName;

    //    @JsonProperty("inetAddressList")
    private List<InetAddress> inetAddressList;

    @JsonProperty("inetAddressList")
    private void unpackInetAddresses(Map<String, String> inetAddresses) {
        if (inetAddressList == null) {
            inetAddressList = new ArrayList<>();
        }
        InetAddress inetAddress = new InetAddress();

    }

    @Getter
    @Setter
    public static class InetAddress {
        private String hostAddress;
        private String hostName;
        private String canonicalName;
    }
}
