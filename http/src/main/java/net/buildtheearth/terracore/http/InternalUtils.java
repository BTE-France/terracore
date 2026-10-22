package net.buildtheearth.terracore.http;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.Long.*;

@UtilityClass
class InternalUtils {

    boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    Optional<Long> parseOptionalLong(@NotNull String string) {
        try {
            return Optional.of(parseLong(string));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    <T, U, V> Optional<V> optionalBiMap(Optional<T> o1, Optional<U> o2, BiFunction<? super T, ? super U, ? extends V> biFunction) {
        if (o1.isEmpty() || o2.isEmpty()) {
            return Optional.empty();
        }
        T t = o1.get();
        U u = o2.get();
        return Optional.ofNullable(biFunction.apply(t, u));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    <T, U, V> Supplier<Optional<V>> optionalBiMapSupplier(Optional<T> o1, Optional<U> o2, BiFunction<? super T, ? super U, ? extends V> biFunction) {
        return () -> optionalBiMap(o1, o2, biFunction);
    }

    final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    String hexString(byte @NotNull [] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            builder.append(HEX_DIGITS[(b >> 4) & 0X0f]);
            builder.append(HEX_DIGITS[ b       & 0x0f]);
        }
        return builder.toString();
    }

}
