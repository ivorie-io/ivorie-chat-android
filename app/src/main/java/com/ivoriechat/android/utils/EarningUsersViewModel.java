package com.ivoriechat.android.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EarningUsersViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EarningUsersViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Earning Users fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}