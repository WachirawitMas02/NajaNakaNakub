/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modifiers;

import GameModel.Hand;
import GameModel.PokerHandType;

public class Joker implements Modifier {
    private final String name;
    private final String description;
    private final int bonusChips;
    private final double bonusMult;
    private final double xMult;
    private final int price;
    private String imgpath;
    // If non-null, this joker's bonuses only trigger when the played hand
    // matches this poker hand type (e.g. a "Flush Joker" only fires on flushes).
    private final PokerHandType requiredType;

    // 1. Full constructor (8 parameters)
    public Joker(String name, String description, int bonusChips, double bonusMult, double xMult, int price,
                 String imgpath,PokerHandType requiredType) {
        this.name = name;
        this.description = description;
        this.bonusChips = bonusChips;
        this.bonusMult = bonusMult;
        this.xMult = xMult;
        this.price = price;
        this.requiredType = requiredType;
        this.imgpath = imgpath;
    }

    // 2. Constructor WITH image, WITHOUT required hand type (7 parameters)
    public Joker(String name, String description, int bonusChips, double bonusMult, double xMult, int price, String imgpath) {
        this(name, description, bonusChips, bonusMult, xMult, price, imgpath,null);
    }

    // 3. Fallback constructor with neither (6 parameters)
    public Joker(String name, String description, int bonusChips, double bonusMult, double xMult, int price) {
        this(name, description, bonusChips, bonusMult, xMult, price, null, null);
    }

    @Override
    public String getName() {
        return name;
    }
    public String getimgpath(){
        return imgpath;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    private boolean applies(PokerHandType handType) {
        return requiredType == null || requiredType == handType;
    }

    @Override
    public int modifyChips(int currentChips, Hand hand, PokerHandType handType) {
        return applies(handType) ? currentChips + bonusChips : currentChips;
    }

    @Override
    public double modifyMult(double currentMult, Hand hand, PokerHandType handType) {
        return applies(handType) ? currentMult + bonusMult : currentMult;
    }

    @Override
    public double modifyXMult(double currentXMult, Hand hand, PokerHandType handType) {
        return applies(handType) ? currentXMult * xMult : currentXMult;
    }

    @Override
    public String toString() {
        return name;
    }
}
