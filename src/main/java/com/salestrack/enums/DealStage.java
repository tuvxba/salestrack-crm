package com.salestrack.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum DealStage {

    NEW,
    QUALIFIED,
    PROPOSAL,
    NEGOTIATION,
    WON,
    LOST;

    private static final Map<DealStage, Set<DealStage>> ALLOWED_TRANSITIONS = new EnumMap<>(DealStage.class);

    static {
        ALLOWED_TRANSITIONS.put(NEW, Set.of(QUALIFIED, LOST));
        ALLOWED_TRANSITIONS.put(QUALIFIED, Set.of(NEW, PROPOSAL, LOST));
        ALLOWED_TRANSITIONS.put(PROPOSAL, Set.of(QUALIFIED, NEGOTIATION, LOST));
        ALLOWED_TRANSITIONS.put(NEGOTIATION, Set.of(PROPOSAL, WON, LOST));
        ALLOWED_TRANSITIONS.put(WON, Set.of());
        ALLOWED_TRANSITIONS.put(LOST, Set.of());
    }

    public boolean canTransitionTo(DealStage target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}