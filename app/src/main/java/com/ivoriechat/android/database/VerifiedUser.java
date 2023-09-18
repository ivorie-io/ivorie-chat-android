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

    @ColumnInfo(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @ColumnInfo(name = "liked_stats")
    private Integer likedStats;

    @ColumnInfo(name = "follower_stats")
    private Integer followerStats;

    @ColumnInfo(name = "expected_usage")
    private Integer expectedUsage;

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getExpertiseSummary() {
        return expertiseSummary;
    }

    public void setExpertiseSummary(String expertiseSummary) {
        this.expertiseSummary = expertiseSummary;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Integer getLikedStats() {
        return likedStats;
    }

    public void setLikedStats(Integer likedStats) {
        this.likedStats = likedStats;
    }

    public Integer getFollowerStats() {
        return followerStats;
    }

    public void setFollowerStats(Integer followerStats) {
        this.followerStats = followerStats;
    }

    public Integer getExpectedUsage() {
        return expectedUsage;
    }

    public void setExpectedUsage(Integer expectedUsage) {
        this.expectedUsage = expectedUsage;
    }
}
