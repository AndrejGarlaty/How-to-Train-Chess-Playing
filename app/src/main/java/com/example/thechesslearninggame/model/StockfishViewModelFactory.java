package com.example.thechesslearninggame.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.thechesslearninggame.modules.StockfishManager;

public class StockfishViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final StockfishManager stockfishManager;

    public StockfishViewModelFactory(Application application, StockfishManager stockfishManager) {
        this.application = application;
        this.stockfishManager = stockfishManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StockfishViewModel.class)) {
            return (T) new StockfishViewModel(application, stockfishManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
