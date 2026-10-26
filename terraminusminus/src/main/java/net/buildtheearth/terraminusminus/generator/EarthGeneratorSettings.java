package net.buildtheearth.terraminusminus.generator;

import static java.util.Objects.*;
import static net.daporkchop.lib.common.util.PValidation.checkState;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@With
public class EarthGeneratorSettings {
    public static final int CONFIG_VERSION = 2;
    public static final String DEFAULT_SETTINGS;
    private static final LoadingCache<String, EarthGeneratorSettings> SETTINGS_PARSE_CACHE = CacheBuilder.newBuilder()
            .weakKeys().weakValues()
            .build(CacheLoader.from(EarthGeneratorSettings::parseUncached));
    public static final String BTE_DEFAULT_SETTINGS;

    static {
        try {
            try (InputStream in = EarthGeneratorSettings.class.getResourceAsStream("default_generator_settings.json5")) {
                requireNonNull(in);
                DEFAULT_SETTINGS = new String(StreamUtil.toByteArray(in));
            }
            try (InputStream in = EarthGeneratorSettings.class.getResourceAsStream("bte_generator_settings.json5")) {
                requireNonNull(in);
                BTE_DEFAULT_SETTINGS = new String(StreamUtil.toByteArray(in));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the given generator settings.
     *
     * @param generatorSettings the settings string to parse
     * @return the parsed settings
     */
    public static EarthGeneratorSettings parse(String generatorSettings) {
        if (Strings.isNullOrEmpty(generatorSettings)) {
            generatorSettings = DEFAULT_SETTINGS;
        }

        return SETTINGS_PARSE_CACHE.getUnchecked(generatorSettings);
    }

    /**
     * Parses the given generator settings without caching.
     *
     * @param generatorSettings the settings string to parse
     * @return the parsed settings
     */
    @SneakyThrows(IOException.class)
    public static EarthGeneratorSettings parseUncached(String generatorSettings) {
        if (Strings.isNullOrEmpty(generatorSettings)) {
            generatorSettings = DEFAULT_SETTINGS;
        }
        return TerraConstants.JSON_MAPPER.readValue(generatorSettings, EarthGeneratorSettings.class);
    }

    @NonNull
    @Getter(onMethod_ = { @JsonGetter })
    protected final GeographicProjection projection;

    @Getter(onMethod_ = { @JsonGetter })
    protected final boolean useDefaultHeights;
    @Getter(onMethod_ = { @JsonGetter })
    protected final boolean useDefaultTreeCover;

    protected transient final Cached<EarthBiomeProvider> biomeProvider = Cached.global(() -> new EarthBiomeProvider(this), ReferenceStrength.SOFT);
    protected transient final Cached<GeneratorDatasets> datasets = Cached.global(() -> new GeneratorDatasets(this), ReferenceStrength.SOFT);

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EarthGeneratorSettings(
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
            @JsonProperty(value = "cwg") String cwg,
            @JsonProperty(value = "useDefaultHeights") Boolean useDefaultHeights,
            @JsonProperty(value = "useDefaultTreeCover") @JsonAlias("useDefaultTrees") Boolean useDefaultTreeCover,
            @JsonProperty(value = "version", required = true) int version) {
        checkState(version == CONFIG_VERSION, "invalid version %d (expected: %d)", version, CONFIG_VERSION);

        this.projection = projection;
        this.useDefaultHeights = useDefaultHeights == null || useDefaultHeights;
        this.useDefaultTreeCover = useDefaultTreeCover == null || useDefaultTreeCover;
    }

    @Override
    public String toString() {
        try {
            return TerraConstants.JSON_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonGetter("version")
    private int version() {
        return CONFIG_VERSION;
    }

    public EarthBiomeProvider biomeProvider() {
        return this.biomeProvider.get();
    }

    public GeneratorDatasets datasets() {
        return this.datasets.get();
    }

}