package com.ivoriechat.android.utils;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

public class BigDecimalConverter {
    @TypeConverter
    public static String fromBigDecimal(BigDecimal value) {
        return value == null ? null : value.setScale(2).toPlainString();
    }

    @TypeConverter
    public static BigDecimal toBigDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

}
