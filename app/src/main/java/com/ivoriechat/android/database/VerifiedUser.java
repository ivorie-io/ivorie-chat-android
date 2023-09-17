package com.ivoriechat.android.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.math.BigDecimal;

@Entity(tableName = "verified_user")
public class VerifiedUser extends User {

    @ColumnInfo(name = "average_rating")
    private BigDecimal averageRating;

    @ColumnInfo(name = "profile")
    private String profile;

    @ColumnInfo(name = "qualification")
    private String qualification;

    @ColumnInfo(name = "expertise_summary")
    private String expertiseSummary;

    @ColumnInfo(name = "monthly_rate")
    private BigDecimal monthlyRate;

    @ColumnInfo(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @ColumnInfo(name = "liked_stats")
    private Integer likedStats;

    @ColumnInfo(name = "follower_stats")
    private Integer followerStats;


}
