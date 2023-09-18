package com.ivoriechat.android.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VerifiedUserDAO {
    @Query("SELECT * FROM verified_user ORDER BY user_Id DESC")
    LiveData<List<VerifiedUser>> getAll();

    @Query("SELECT * FROM verified_user WHERE user_Id = :userId ")
    LiveData<VerifiedUser> findById(Integer userId);

    @Query("SELECT * FROM verified_user WHERE user_Id IN (:userIds)")
    LiveData<List<VerifiedUser>> loadAllByIds(Integer[] userIds);

    @Query("DELETE FROM verified_user")
    void deleteAllRows();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VerifiedUser> users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VerifiedUser user);

    @Delete
    void delete(VerifiedUser user);

    @Delete
    void deleteAll(List<VerifiedUser> users);
}
