package net.buildtheearth.terracore.worldgen.config.condition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import java.util.function.DoublePredicate;
import net.buildtheearth.terracore.worldgen.config.GlobalParseRegistries;
import net.buildtheearth.terracore.worldgen.config.TypedDeserializer;
import net.buildtheearth.terracore.worldgen.config.TypedSerializer;

/**
 * A condition that accepts a {@code double} value and checks if it is applicable to a given argument.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize(using = DoubleCondition.Deserializer.class)
@JsonSerialize(using = DoubleCondition.Serializer.class)
@FunctionalInterface
public interface DoubleCondition extends DoublePredicate {
    class Deserializer extends TypedDeserializer<DoubleCondition> {
        @Override
        protected Map<String, Class<? extends DoubleCondition>> registry() {
            return GlobalParseRegistries.DOUBLE_CONDITIONS;
        }
    }

    class Serializer extends TypedSerializer<DoubleCondition> {
        @Override
        protected Map<Class<? extends DoubleCondition>, String> registry() {
            return GlobalParseRegistries.DOUBLE_CONDITIONS.inverse();
        }
    }
}
