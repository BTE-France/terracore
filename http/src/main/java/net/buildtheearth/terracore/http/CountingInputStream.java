package net.buildtheearth.terracore.http;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;

class CountingInputStream extends InputStream {

    @NotNull private final InputStream wrapped;

    @Getter private long count = 0;

    public CountingInputStream(@NonNull InputStream wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int read() throws IOException {
        this.count++;
        return this.wrapped.read();
    }

}
