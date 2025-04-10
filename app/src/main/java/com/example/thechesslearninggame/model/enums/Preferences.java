package com.example.thechesslearninggame.model.enums;

public enum Preferences {

    NAME("AppSetings"),

    LANGUAGE("selected_language"),
    VOICE_INPUT("voice_input");

    private final String value;

    Preferences(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
