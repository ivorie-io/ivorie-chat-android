package com.ivoriechat.android.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.ivoriechat.android.utils.BigDecimalConverter;

@Database(entities = {User.class}, version = 1, exportSchema = false)
@TypeConverters({BigDecimalConverter.class})
public abstract class IvorieDatabase extends RoomDatabase {
    public abstract UserDAO userDAO();

    private static IvorieDatabase INSTANCE;

    // Creates a RoomDatabase.Builder for a persistent database. Once a database is built, you should keep a reference to it and re-use it.
    public static IvorieDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), IvorieDatabase.class, "IvorieDatabase").build();
        }
        return INSTANCE;
    }
}
