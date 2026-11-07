package net.buildtheearth.terracore.worldgen.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;

import net.buildtheearth.terracore.worldgen.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terracore.worldgen.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(LineParser.class)
@FunctionalInterface
public interface LineMapper extends OSMMapper<MultiLineString> {
}
