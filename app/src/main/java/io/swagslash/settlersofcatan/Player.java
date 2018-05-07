package io.swagslash.settlersofcatan;

import io.swagslash.settlersofcatan.pieces.Board;
import io.swagslash.settlersofcatan.pieces.items.Inventory;

/**
 * Created by wedenigc on 19.03.18.
 */

public class Player {

    public static final int MAX_SETTLEMENTS = 5;
    public static final int MAX_CITIES = 4;
    public static final int MAX_ROADS = 15;

    public enum Color {
        RED(0xFFFF0000),
        BLUE(0xFF0000FF),
        YELLOW(0xFFFFFF00),
        WHITE(0xFFCCCCCC);

        int val;

        Color(int color) {
            this.val = color;
        }

        public int getVal() {
            return val;
        }
    }

    private int playerNumber;
    private Color color;
    private String playerName;
    protected int numOwnedSettlements;
    protected int numOwnedCities;
    private int longestTradeRoute;
    private Inventory inventory;

    protected transient Board board;

    public Player(Board board, int playerNumber, Color color, String playerName) {
        this.board = board;
        this.playerNumber = playerNumber;
        this.color = color;
        this.playerName = playerName;

        this.numOwnedCities = 0;
        this.numOwnedSettlements = 0;
        this.longestTradeRoute = 0;

        this.inventory = new Inventory();
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Color getColor() {
        return color;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getNumOwnedSettlements() {
        return numOwnedSettlements;
    }

    public int getNumOwnedCities() {
        return numOwnedCities;
    }

    public int getLongestTradeRoute() {
        return longestTradeRoute;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
