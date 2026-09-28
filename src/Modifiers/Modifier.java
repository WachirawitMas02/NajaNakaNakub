/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modifiers;

import GameModel.Hand;
import GameModel.PokerHandType;

public interface Modifier {
    // Basic identifier methods
    String getName();
    String getDescription();

    // Hook: modify raw chips (e.g., "+30 Chips")
    default int modifyChips(int currentChips, Hand hand, PokerHandType handType) {
        return currentChips; // Default: no change
    }

    // Hook: add to additive multiplier (e.g., "+4 Mult")
    default double modifyMult(double currentMult, Hand hand, PokerHandType handType) {
        return currentMult; // Default: no change
    }

    // Hook: multiply the multiplier (e.g., "x1.5 Mult")
    default double modifyXMult(double currentXMult, Hand hand, PokerHandType handType) {
        return currentXMult; // Default: no change
    }
}
