package net.buildtheearth.terracore.minecraft.exceptions;

import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terracore.minecraft.BlockState;

/**
 * Indicates failure in an attempt to translate a foreign implementation into a Terracore substitute implementation
 * (e.g., failed to convert a Bukkit BlockData into a Terracore {@link BlockState}).
 *
 * @author Smyler
 */
@Getter
public class TranslateToSubstituteException extends SubstituteException {

    private final @NonNull Object foreignObject;
    private final @NonNull Class<?> substituteClass;

    public TranslateToSubstituteException(@NonNull Object foreignObject, @NonNull Class<?> substituteClass, @NonNull String errorMessage) {
        super(
                "Failed to convert foreign object " + foreignObject
                + " of class " + foreignObject.getClass()
                + " into a substitute of class " + substituteClass
                + ": " + errorMessage
        );
        this.foreignObject = foreignObject;
        this.substituteClass = substituteClass;
    }

}
