package com.ivoriechat.android.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PayingUsersViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PayingUsersViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Paying Users fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}