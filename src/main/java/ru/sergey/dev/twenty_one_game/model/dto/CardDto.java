package ru.sergey.dev.twenty_one_game.model.dto;

public class CardDto {
    private String value;
    private String suit;
    private int price;

    public CardDto() {
    }

    public CardDto(String value, String suit, int price) {
        this.value = value;
        this.suit = suit;
        this.price = price;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSuit() {
        return suit;
    }

    public String getImagePath() {
        String value;
        if (price == 6) value = String.valueOf(price);
        else if (price == 7) value = String.valueOf(price);
        else if (price == 8) value = String.valueOf(price);
        else if (price == 9) value = String.valueOf(price);
        else if (price == 10) value = String.valueOf(price);
        else value = this.value;
        return String.format("/images/cards/%s/%s_%s.png",
                suit.toLowerCase(),
                suit.toLowerCase(),
                value.toLowerCase());
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return value + " of " + suit + " (" + price + ")";
    }
}
