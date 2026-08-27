package com.salestrack.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DealStageTest {

    @Test
    void newCanTransitionToQualified() {
        assertTrue(DealStage.NEW.canTransitionTo(DealStage.QUALIFIED));
    }

    @Test
    void newCanTransitionToLost() {
        assertTrue(DealStage.NEW.canTransitionTo(DealStage.LOST));
    }

    @Test
    void newCannotTransitionToWon() {
        assertFalse(DealStage.NEW.canTransitionTo(DealStage.WON));
    }

    @Test
    void wonCannotTransitionToAnything() {
        assertFalse(DealStage.WON.canTransitionTo(DealStage.NEW));
        assertFalse(DealStage.WON.canTransitionTo(DealStage.LOST));
    }

    @Test
    void lostCannotTransitionToAnything() {
        assertFalse(DealStage.LOST.canTransitionTo(DealStage.NEW));
        assertFalse(DealStage.LOST.canTransitionTo(DealStage.WON));
    }
}