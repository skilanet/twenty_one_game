package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;
import ru.sergey.dev.twenty_one_game.model.dto.CardDto;

public class CardDealtEventDto extends GameEventDto {
    @SerializedName("playerId")
    private String playerId;

    @SerializedName("card")
    private CardDto card;

    public CardDealtEventDto() {
        this.type = "CardDealt";
    }

    public String getPlayerId() {
        return playerId;
    }

    public CardDto getCard() {
        return card;
    }
}
