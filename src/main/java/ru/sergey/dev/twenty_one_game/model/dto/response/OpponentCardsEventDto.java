package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;
import ru.sergey.dev.twenty_one_game.model.dto.CardDto;

import java.util.ArrayList;

public class OpponentCardsEventDto extends GameEventDto {
    @SerializedName("opponentId")
    String opponentId;
    @SerializedName("cards")
    ArrayList<CardDto> cards;

    OpponentCardsEventDto(String opponentId, ArrayList<CardDto> cards) {
        this.opponentId = opponentId;
        this.cards = cards;
    }

    public ArrayList<CardDto> getCards() {
        return cards;
    }

    public void setCards(ArrayList<CardDto> cards) {
        this.cards = cards;
    }
}
