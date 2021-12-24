package ru.pel.usbddc.entity;

import lombok.Getter;

import java.nio.file.Path;
import java.util.Objects;

@Getter
public class UserProfile {
    private String username; //по-моему, не всегда совпадает с именем папки профиля в profileImagePath
    private Path profileImagePath;
    private String securityId; //SID полезен для сбора доп. инфо о пользователе. Доп. инфа о SID:
    // - https://docs.microsoft.com/en-us/windows/win32/secauthz/well-known-sids
    // - https://docs.microsoft.com/en-us/windows/security/identity-protection/access-control/security-identifiers

    private UserProfile() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(username, that.username) && Objects.equals(profileImagePath, that.profileImagePath) && Objects.equals(securityId, that.securityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, profileImagePath, securityId);
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final UserProfile newUserProfile;

        private Builder() {
            newUserProfile = new UserProfile();
        }

        public UserProfile build() {
            return newUserProfile;
        }

        public Builder withProfileImagePath(Path profileImagePath) {
            newUserProfile.profileImagePath = profileImagePath;
            return this;
        }

        public Builder withSecurityId(String securityId) {
            newUserProfile.securityId = securityId;
            return this;
        }

        /*
         * Разбирает SID на составные части. Метод еще не реализован - в настоящей версии он не нужен, но пусть будет
         * заглушка-намек-приколюшка. :)
         *
//         * @param SID security identifier
//         * @return метод еще не реализован. Возвращает builder, который ничего не изменил в объекте.
         */
//        public Builder withSecurityId(SecurityId SID) {
//            return this;
//        }

        public Builder withUsername(String username) {
            newUserProfile.username = username;
            return this;
        }
    }
}
