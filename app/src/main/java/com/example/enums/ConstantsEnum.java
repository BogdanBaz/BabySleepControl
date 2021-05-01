package com.example.enums;

public enum ConstantsEnum {

    START_NIGHT("21"),
    END_NIGHT("07");

    private final int value;

    ConstantsEnum(String value) {
        int num = 12;
        try {
            num = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.value = num;
    }

    public int getValue() {
        return value;
    }

}
