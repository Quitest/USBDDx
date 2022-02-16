package ru.pel.usbddc.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Содержит имена продукта
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ProductName extends AbstractName{
}
