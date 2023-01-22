package org.mtcg.cards;

import org.mtcg.game.BattleLog;

public interface Attackable {
    public Card attack(Card opponent, BattleLog battleLog);
}
