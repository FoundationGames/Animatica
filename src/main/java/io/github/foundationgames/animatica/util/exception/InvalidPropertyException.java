package io.github.foundationgames.animatica.util.exception;

import net.minecraft.util.Identifier;

public class InvalidPropertyException extends PropertyParseException {
    public InvalidPropertyException(Identifier file, String key, String expectedType) {
        super(String.format("Property '%s' in file '%s' expected to be of type: %s", key, file, expectedType));
    }
}
