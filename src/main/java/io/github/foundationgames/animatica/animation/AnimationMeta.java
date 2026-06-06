package io.github.foundationgames.animatica.animation;

import io.github.foundationgames.animatica.util.PropertyUtil;
import io.github.foundationgames.animatica.util.Utilities;
import io.github.foundationgames.animatica.util.exception.InvalidPropertyException;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

public record AnimationMeta(
        Identifier source, Identifier target, int targetX,
        int targetY, int width, int height, int defaultFrameDuration, boolean interpolate,
        int interpolationDelay, Map<Integer, Integer> frameMapping,
        Map<Integer, Integer> frameDurations
) {
    public static AnimationMeta of(Identifier file, Properties properties) throws PropertyParseException {
        Identifier source;
        Identifier target;
        try {
            source = Utilities.processPath(file, Identifier.parse(PropertyUtil.get(file, properties, "from")));
        } catch (IdentifierException ex) { throw new InvalidPropertyException(file, "from", "resource location"); }
        try {
            target = Utilities.processPath(file, Identifier.parse(PropertyUtil.get(file, properties, "to")));
        } catch (IdentifierException ex) { throw new InvalidPropertyException(file, "to", "resource location"); }
        return new AnimationMeta(
                source,
                target,
                PropertyUtil.getInt(file, properties, "x"),
                PropertyUtil.getInt(file, properties, "y"),
                PropertyUtil.getInt(file, properties, "w"),
                PropertyUtil.getInt(file, properties, "h"),
                PropertyUtil.getIntOr(file, properties, "duration", 1),
                PropertyUtil.getBoolOr(file, properties, "interpolate", false),
                PropertyUtil.getIntOr(file, properties, "skip", 0),
                PropertyUtil.intToIntMap(PropertyUtil.getSubProperties(properties, "tile")),
                PropertyUtil.intToIntMap(PropertyUtil.getSubProperties(properties, "duration"))
        );
    }

    public int getGreatestUsedFrame() {
        Set<Integer> frames = new HashSet<>(frameMapping.keySet());
        frames.addAll(frameDurations.keySet());

        int greatestFrame = 0;
        for (int frame : frames) {
            greatestFrame = Math.max(frame, greatestFrame);
        }

        return greatestFrame;
    }
}
