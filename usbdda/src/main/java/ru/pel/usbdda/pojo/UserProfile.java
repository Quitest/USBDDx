package ru.pel.usbdda.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
public class UserProfile {

    @JsonProperty("username")
    private String username;

    @JsonProperty("profileImagePath")
    private Path profileImagePath;

    @JsonProperty("securityId")
    private String securityId;
}
