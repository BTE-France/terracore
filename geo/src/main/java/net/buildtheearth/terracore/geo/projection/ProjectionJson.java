package net.buildtheearth.terracore.geo.projection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terracore.geo.projection.dymaxion.BTEDymaxionProjection;
import net.buildtheearth.terracore.geo.projection.dymaxion.ConformalDynmaxionProjection;
import net.buildtheearth.terracore.geo.projection.dymaxion.DymaxionProjection;
import net.buildtheearth.terracore.geo.projection.mercator.CenteredMercatorProjection;
import net.buildtheearth.terracore.geo.projection.mercator.TransverseMercatorProjection;
import net.buildtheearth.terracore.geo.projection.mercator.WebMercatorProjection;
import net.buildtheearth.terracore.geo.projection.transform.ClampProjectionTransform;
import net.buildtheearth.terracore.geo.projection.transform.FlipHorizontalProjectionTransform;
import net.buildtheearth.terracore.geo.projection.transform.FlipVerticalProjectionTransform;
import net.buildtheearth.terracore.geo.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terracore.geo.projection.transform.RotateProjectionTransform;
import net.buildtheearth.terracore.geo.projection.transform.ScaleProjectionTransform;
import net.buildtheearth.terracore.geo.projection.transform.SwapAxesProjectionTransform;
import net.daporkchop.lib.common.util.PorkUtil;

import java.io.IOException;

import static java.util.Map.*;
import static java.util.Map.entry;

@UtilityClass
class ProjectionJson {

    final JsonMapper MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .build();

    private final BiMap<String, Class<? extends GeographicProjection>> PROJECTIONS = HashBiMap.create(ofEntries(
            //normal projections
            entry("centered_mercator", CenteredMercatorProjection.class),
            entry("web_mercator", WebMercatorProjection.class),
            entry("transverse_mercator", TransverseMercatorProjection.class),
            entry("equirectangular", EquirectangularProjection.class),
            entry("sinusoidal", SinusoidalProjection.class),
            entry("equal_earth", EqualEarthProjection.class),
            entry("bte_conformal_dymaxion", BTEDymaxionProjection.class),
            entry("dymaxion", DymaxionProjection.class),
            entry("conformal_dymaxion", ConformalDynmaxionProjection.class),
            entry("lambert_azimuthal", LambertAzimuthalProjection.class),
            entry("azimuthal_equidistant", AzimuthalEquidistantProjection.class),
            entry("stereographic", StereographicProjection.class),
            //transformations
            entry("clamp", ClampProjectionTransform.class),
            entry("flip_horizontal", FlipHorizontalProjectionTransform.class),
            entry("flip_vertical", FlipVerticalProjectionTransform.class),
            entry("offset", OffsetProjectionTransform.class),
            entry("rotate", RotateProjectionTransform.class),
            entry("scale", ScaleProjectionTransform.class),
            entry("swap_axes", SwapAxesProjectionTransform.class)
    ));

    class Deserializer extends JsonDeserializer<GeographicProjection> {
        @Override
        public GeographicProjection deserialize(JsonParser p, DeserializationContext context) throws IOException {
            String name = p.nextFieldName();
            if (name == null) {
                throw JsonMappingException.from(p, "expected type name, found: " + p.currentToken());
            }

            Class<? extends GeographicProjection> clazz = PROJECTIONS.get(name);
            if (clazz == null) {
                throw JsonMappingException.from(p, "invalid type type name: " + name);
            }

            if (p.nextToken() != JsonToken.START_OBJECT) {
                throw JsonMappingException.from(p, "expected json object, but found: " + p.currentToken());
            }

            GeographicProjection value = context.readValue(p, PorkUtil.<Class<? extends GeographicProjection>>uncheckedCast(clazz));

            if (p.nextToken() != JsonToken.END_OBJECT) {
                throw JsonMappingException.from(p, "expected json object end, but found: " + p.currentToken());
            }

            return value;
        }
    }

    class Serializer extends JsonSerializer<GeographicProjection> {
        @Override
        public void serialize(GeographicProjection value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            String name = PROJECTIONS.inverse().get(value.getClass());
            if (name == null) {
                throw JsonMappingException.from(gen, "invalid value type: " + value.getClass());
            }

            gen.writeStartObject(value);
            gen.writeFieldName(name);

            // Recursion guard: force a fallback to generic serialization instead of re-entering this serializer
            MAPPER.rebuild().addMixIn(value.getClass(), MixIn.class).build().writeValue(gen, value);

            gen.writeEndObject();
        }
    }

    @JsonSerialize
    private class MixIn {} // Have this here to avoid a bug in forge Gradle (https://forums.minecraftforge.net/topic/40670-build-terminates-at-extractrangemapreplacedmain/)

}
