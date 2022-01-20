package ru.pel.usbdda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
public class OSInfoDto {
    /**
     * Поле служит для идентификации ОС, а точнее конкретной инсталляции. Для чего это нужно? Что бы была хоть какая-то
     * возможность идентифицировать АРМ с высокой точностью. При использовании источников, указанных ниже, следует учитывать:
     *  <ul>
     *      <li>значения в них меняются после каждой переустановки;</li>
     *      <li>в случае использования образов для восстановления систем после сбоев или быстрого развертывания множества однотипных
     *      рабочих мест идентификаторы будут одинаковыми для всех АРМ.</li>
     * </ul>
     * Источники идентификаторов:
     *  <ul>
     *     <li> Linux: <a href=http://0pointer.de/blog/projects/ids.html>/etc/machine-id </a></li>
     *     <li> Windows: <a href=https://www.nextofwindows.com/the-best-way-to-uniquely-identify-a-windows-machine>HKLM\SOFTWARE\Microsoft\Cryptography\MachineGuid</a></li>
     *  </ul>
     */
    @JsonProperty("osId")
    private String osId;

    @JsonProperty("osName")
    private String osName;

    @JsonProperty("osVersion")
    private double osVersion;

    @JsonProperty("osArch")
    private String osArch;

    @JsonProperty("tmpdir")
    private Path tmpdir;

    @JsonProperty("username")
    private String username;

    @JsonProperty("homeDir")
    private Path homeDir;

    @JsonProperty("currentDir")
    private Path currentDir;

    @JsonProperty("systemRoot")
    private Path systemRoot;

    @JsonProperty("computerName")
    private String computerName;

    @JsonProperty("networkInterfaceDtoList")
    private List<NetworkInterfaceDto> networkInterfaceDtoList;
}
