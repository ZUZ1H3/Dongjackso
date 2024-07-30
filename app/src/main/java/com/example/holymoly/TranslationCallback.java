package com.example.holymoly;

public interface TranslationCallback {
    void onSuccess(String translatedText);
    void onFailure(Throwable t);
}
