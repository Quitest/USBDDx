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
public class UserProfile {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("profileImagePath")
    private String profileImagePath;

    @JsonProperty("securityId")
    private String securityId;
}
