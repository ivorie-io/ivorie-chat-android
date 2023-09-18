package com.ivoriechat.android.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class VerifiedUserListViewModel extends AndroidViewModel {

    private LiveData<List<VerifiedUser>> mVerifiedUsersLiveData;

    public VerifiedUserListViewModel(@NonNull Application application) {
        super(application);
        IvorieDatabase database = IvorieDatabase.getDatabase(getApplication());
        mVerifiedUsersLiveData = database.verifiedUserDAO().getAll();
    }

    public LiveData<List<VerifiedUser>> getVerifiedUserList() {
        return mVerifiedUsersLiveData;
    }
}
