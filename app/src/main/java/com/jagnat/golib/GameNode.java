package com.jagnat.golib;

import java.util.*;

public class GameNode {

    public enum Color {
        NONE, BLACK, WHITE
    }

    public enum MoveType {
        NONE, MOVE, PASS, RESIGN
    }

    public static class Position {
        public int x = -1, y = -1;
        public Position() {}
        public Position(int x, int y) { this.x = x; this.y = y; }
    }

    public Position movePos = new Position();
    public Color moveColor = Color.NONE;
    public MoveType type = MoveType.NONE;
    public String comment = "";

    public ArrayList<Position> addedBlackPoints = new ArrayList<Position>();
    public ArrayList<Position> addedWhitePoints = new ArrayList<Position>();
    public ArrayList<Position> clearedPoints = new ArrayList<Position>();

    public LinkedList<GameNode> children = new LinkedList<GameNode>();
    public GameNode parent = null;

    public GameNode() {}
}
