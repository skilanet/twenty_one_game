package ru.sergey.dev.twenty_one_game.model.dto;

import com.google.gson.annotations.SerializedName;

public class RankDto {
    @SerializedName("price")
    public int price;

    public int getPrice() {
        return price;
    }
}