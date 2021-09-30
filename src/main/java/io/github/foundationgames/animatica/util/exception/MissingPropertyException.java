package io.github.foundationgames.animatica.util.exception;

import net.minecraft.util.Identifier;

public class MissingPropertyException extends PropertyParseException {
    public MissingPropertyException(Identifier file, String key) {
        super(String.format("Expected property '%s' in file '%s'", key, file));
    }
}
