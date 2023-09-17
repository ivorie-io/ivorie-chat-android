package com.ivoriechat.android.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UserListViewModel extends AndroidViewModel {

    private LiveData<List<User>> mUsersLiveData;

    public UserListViewModel(@NonNull Application application) {
        super(application);
        IvorieDatabase database = IvorieDatabase.getDatabase(getApplication());
        mUsersLiveData = database.userDAO().getAll();
    }

    public LiveData<List<User>> getUserList() {
        return mUsersLiveData;
    }
}
