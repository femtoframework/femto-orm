package org.femtoframework.orm.domain;

import lombok.Data;

@Data
public class Device {

    private int id;

    private String model;

    private String productNo;

    private String uuid;
}
