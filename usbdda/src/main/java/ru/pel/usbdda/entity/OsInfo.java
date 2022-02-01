package ru.pel.usbdda.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class OsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    private String osId;
    private String osName;
    private double osVersion;
    private String osArch;
    private String tmpdir;
    private String username;
    private String homeDir;
    private String currentDir;
    private String systemRoot;
    private String computerName;

    @OneToMany(mappedBy = "osInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NetworkInterface> networkInterfaceList;
}
