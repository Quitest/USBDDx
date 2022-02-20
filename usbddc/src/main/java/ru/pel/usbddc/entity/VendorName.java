package ru.pel.usbddc.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Содержит имена производителя, полученные из разных источников.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class VendorName extends AbstractName {

}
