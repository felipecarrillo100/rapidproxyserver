package io.github.felipecarrillo100.controllers;

public class VaultAuthenticationValues {
    private  String mode;
    private String value;

    public VaultAuthenticationValues(String mode, String value) {
        this.mode = mode;
        this.value = value;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
