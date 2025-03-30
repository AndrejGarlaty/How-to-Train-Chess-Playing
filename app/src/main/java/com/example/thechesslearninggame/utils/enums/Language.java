package com.example.thechesslearninggame.utils.enums;

public enum Language {

    SLOVAK("sk"),
    ENGLISH("en");

    private final String code;
    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
