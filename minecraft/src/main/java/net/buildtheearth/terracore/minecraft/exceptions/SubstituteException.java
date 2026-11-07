package net.buildtheearth.terracore.minecraft.exceptions;

import net.buildtheearth.terracore.minecraft.Biome;
import net.buildtheearth.terracore.minecraft.BlockState;
import net.buildtheearth.terracore.minecraft.Identifier;

/**
 * Thrown when a problem is encountered working with content substitutes
 * (e.g. {@link Identifier}, {@link BlockState}, {@link Biome}, ...).
 *
 * @author Smyler
 */
public abstract class SubstituteException extends RuntimeException {

    public SubstituteException(String message) {
        super(message);
    }

}
