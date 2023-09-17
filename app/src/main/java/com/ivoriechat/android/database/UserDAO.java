package com.ivoriechat.android.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDAO {
    @Query("SELECT * FROM user ORDER BY user_Id DESC")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM user WHERE user_Id = :userId ")
    LiveData<User> findById(Integer userId);

    @Query("SELECT * FROM user WHERE user_Id IN (:userIds)")
    LiveData<List<User>> loadAllByIds(Integer[] userIds);

    @Query("DELETE FROM user")
    void deleteAllRows();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Delete
    void delete(User user);

    @Delete
    void deleteAll(List<User> users);
}
