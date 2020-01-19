package com.jagnat.blindsgf;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SgfReader {

    public static class Property {
        public String id = "";
        public ArrayList<String> values = new ArrayList<String>();
    }

    char[] sgf;
    int index = 0;

    public SgfReader() {

    }

    public GameNode parse(char[] sgfChars) {
        sgf = sgfChars;
        GameNode headNode = new GameNode();
        try {
            headNode = parseGametree(null);
        }
        catch(IndexOutOfBoundsException e) {
            System.out.println("Reached end of file without success! Something's up....");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return headNode;
    }

    public GameNode parseFromFile(String sgfPath) throws Exception {
        File sgfFile = new File(sgfPath);
        FileInputStream is = new FileInputStream(sgfFile);
        return parseFromStream(is);
    }

    public GameNode parseFromStream(FileInputStream inputStream) throws Exception {
        byte[] buf = new byte[(int)inputStream.getChannel().size()];
        inputStream.read(buf, 0, (int)inputStream.getChannel().size());
        return parse(new String(buf).toCharArray());
    }

    private GameNode parseGametree(GameNode parent) throws Exception {
        matchChar('(');

        GameNode headNode = parseSequence();
        headNode.parent = parent;
        GameNode tailNode = headNode;
        while (tailNode.children.size() != 0)
            tailNode = tailNode.children.get(0);

        skipWhitespace();
        while (peekChar() == '(')
        {
            tailNode.children.add(parseGametree(tailNode));
            skipWhitespace();
        }
        matchChar(')');
        return headNode;
    }

    private GameNode parseSequence() throws Exception {
        skipWhitespace();
        GameNode firstNode = parseNode();
        GameNode currentNode = firstNode;

        skipWhitespace();
        while (peekChar() == ';') {
            GameNode addedNode = parseNode();
            currentNode.children.add(addedNode);
            addedNode.parent = currentNode;
            currentNode = addedNode;
            skipWhitespace();
        }

        return firstNode;
    }

    private void printNodeRecursive(GameNode node, String indent, boolean last) {
        System.out.print(indent);

        if (last) {
            System.out.print("\\-");
            indent = indent + "  ";
        }
        else {
            System.out.print("|-");
            indent = indent + "| ";
        }
        String value = "";
        if (node.moveColor !=  GameNode.Color.NONE) {
            value = value + ((node.moveColor == GameNode.Color.WHITE) ? "W" : "B") + " ";
            if (node.movePos.x == -1 && node.movePos.y == -1) {
                value = value + "passes";
            }
            else
            {
                value = value + node.movePos.x + "," + node.movePos.y;
            }
        }
        System.out.println(value);

        int numChildren = node.children.size();

        for (int i = 0; i < numChildren; i++) {
            GameNode child = node.children.get(i);
            printNodeRecursive(child, indent, i + 1 == numChildren);
        }
    }

    private GameNode parseNode() throws Exception {
        GameNode node = new GameNode();
        ArrayList<Property> properties = new ArrayList<Property>();
        matchChar(';');
        skipWhitespace();
        while (peekChar () >= 'A' && peekChar() <= 'Z') {
            properties.add(parseProperty());
            skipWhitespace();
        }

        boolean foundMove = false;

        for (Property prop: properties) {
            switch(prop.id) {
                case "W":
                case "B":
                    if (foundMove) throw new Exception("Node can't contain 2 moves");
                    node.moveColor = prop.id.contentEquals("B") ? GameNode.Color.BLACK : GameNode.Color.WHITE;
                    node.movePos = getPositionFromValue(prop.values.get(0));
                    node.type = (node.movePos.x == -1 && node.movePos.y == -1)? GameNode.MoveType.PASS : GameNode.MoveType.MOVE;
                    foundMove = true;
                    break;
                case "AB":
                case "AW":
                case "AE":
                    ArrayList<GameNode.Position> points =
                            prop.id.contentEquals("AB") ? node.addedBlackPoints :
                                    (prop.id.contentEquals("AW") ? node.addedWhitePoints : node.clearedPoints);
                    for (String val : prop.values) {
                        points.add(getPositionFromValue(val));
                    }
                    break;
                case "C":
                    node.comment = prop.values.get(0);
                    break;
            }
        }

        return node;
    }

    private GameNode.Position getPositionFromValue(String value) throws Exception {
        GameNode.Position pos = new GameNode.Position();

        if (value.isEmpty() || value.contentEquals("tt")) { // Pass
            return pos;
        }

        if (value.length() != 2) throw new Exception("Invalid position in property");

        char xC = value.charAt(0);
        char yC = value.charAt(1);
        if (xC < 'a' || xC > 's' || yC < 'a' || yC > 's') throw new Exception("Invalid position in property");

        pos.x = xC - 'a' + 1;
        pos.y = yC - 'a' + 1;
        return pos;
    }

    private Property parseProperty() throws Exception {
        String propertyName = "";
        char c = peekChar();
        while ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            propertyName = propertyName + c;
            skipChar();
            c = peekChar();
        }

        Property property = new Property();
        property.id = propertyName;

        skipWhitespace();
        while (peekChar() == '[') {
            property.values.add(parsePropertyValue());
            skipWhitespace();
        }

        return property;
    }

    private String parsePropertyValue() throws Exception {
        matchChar('[');
        String propertyStr = "";
        while (peekChar() != ']') {
            if (peekChar() == '\\') {
                skipChar();
                if (Character.isWhitespace(peekChar()) && peekChar() != '\n' && peekChar() != '\r') {
                    propertyStr = propertyStr + ' ';
                }
                else if (!Character.isWhitespace(peekChar())) {
                    propertyStr = propertyStr + peekChar();
                }
            }
            else {
                propertyStr = propertyStr + peekChar();
            }
            skipChar();
        }
        matchChar(']');
        return propertyStr;
    }

    private void matchChar(char c) throws Exception {
        if (sgf[index] != c) {
            throw new Exception("Expected to see '" + c + "', got '" + sgf[index] + "'!");
        }
        index++;
    }

    private char peekChar() {
        return sgf[index];
    }

    private void skipChar() {
        index++;
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(sgf[index])) {
            index++;
        }
    }
}