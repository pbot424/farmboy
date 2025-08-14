package com.dailytree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TreePatch
{
        LUMBRIDGE("Lumbridge"),
        TAVERLEY("Taverley"),
        VARROCK("Varrock"),
        FALADOR("Falador"),
        FARMING_GUILD("Farming Guild");

        private final String displayName;
}
