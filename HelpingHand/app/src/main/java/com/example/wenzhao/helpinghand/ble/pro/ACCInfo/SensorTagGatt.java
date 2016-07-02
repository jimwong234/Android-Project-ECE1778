package com.example.wenzhao.helpinghand.ble.pro.ACCInfo;

import java.util.UUID;

import static java.util.UUID.fromString;

public class SensorTagGatt {
  public final static UUID
      UUID_MOV_SERV = fromString("f000aa80-0451-4000-b000-000000000000"),
      UUID_MOV_DATA = fromString("f000aa81-0451-4000-b000-000000000000"),
      UUID_MOV_CONF = fromString("f000aa82-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
      UUID_MOV_PERI = fromString("f000aa83-0451-4000-b000-000000000000"); // Period in tens of milliseconds
}
