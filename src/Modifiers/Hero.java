/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modifiers;

import GameModel.Hand;
import GameModel.PokerHandType;

// A Hero is the player's chosen character for the run: a single persistent
// passive ability picked once at the start of a new game.
public class Hero implements Modifier {

    // A small functional hook so HeroFactory can define many distinct
    // passives (e.g. "bonus mult if hand contains an Ace") without subclassing.
    @FunctionalInterface
    public interface HeroEffect {
        double apply(double currentMult, Hand hand, PokerHandType handType);
    }

    private final String name;
    private final String title;
    private final String description;
    private final HeroEffect effect;

    public Hero(String name, String title, String description, HeroEffect effect) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.effect = effect;
    }

    @Override
    public String getName() {
        return name + " the " + title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double modifyMult(double currentMult, Hand hand, PokerHandType handType) {
        return effect.apply(currentMult, hand, handType);
    }

    @Override
    public String toString() {
        return getName();
    }
}
