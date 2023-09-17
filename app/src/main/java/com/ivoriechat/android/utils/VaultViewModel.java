package com.ivoriechat.android.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VaultViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public VaultViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Vault fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}