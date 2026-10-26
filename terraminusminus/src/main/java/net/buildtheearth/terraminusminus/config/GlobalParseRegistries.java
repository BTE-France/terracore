package net.buildtheearth.terraminusminus.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraminusminus.config.condition.AndDC;
import net.buildtheearth.terraminusminus.config.condition.DoubleCondition;
import net.buildtheearth.terraminusminus.config.condition.EqualDC;
import net.buildtheearth.terraminusminus.config.condition.GreaterThanDC;
import net.buildtheearth.terraminusminus.config.condition.LessThanDC;
import net.buildtheearth.terraminusminus.config.condition.NotDC;
import net.buildtheearth.terraminusminus.config.condition.OrDC;
import net.buildtheearth.terraminusminus.config.scalarparse.d.AddDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.DivideDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.DoubleScalarParser;
import net.buildtheearth.terraminusminus.config.scalarparse.d.FlipXDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.FlipZDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.FromIntDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.MultiplyDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffAutoDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffFloatingPointDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTerrariumPngDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.ParseTiffIntDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.d.SwapAxesDSP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.AddISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.AndISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.FlipXISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.FlipZISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.GrayscaleExtractISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.IntScalarParser;
import net.buildtheearth.terraminusminus.config.scalarparse.i.ParseJpgISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.ParsePngISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.ParseTiffISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.RGBExtractISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.RequireOpaqueISP;
import net.buildtheearth.terraminusminus.config.scalarparse.i.SwapAxesISP;

import static java.util.Map.*;

/**
 * Identifies implementation classes by their type names.
 * <p>
 * Warning: modifying any of the fields in this class outside of initialization time may cause unexpected behavior.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class GlobalParseRegistries {
    public final BiMap<String, Class<? extends DoubleCondition>> DOUBLE_CONDITIONS = HashBiMap.create(ofEntries(
            //conditions
            entry("equal", EqualDC.class),
            entry("greater_than", GreaterThanDC.class),
            entry("less_than", LessThanDC.class),
            //logical operators
            entry("and", AndDC.class),
            entry("not", NotDC.class),
            entry("or", OrDC.class)
    ));

    public final BiMap<String, Class<? extends DoubleScalarParser>> SCALAR_PARSERS_DOUBLE = HashBiMap.create(ofEntries(
            //arithmetic operators
            entry("add", AddDSP.class),
            entry("divide", DivideDSP.class),
            entry("multiply", MultiplyDSP.class),
            //conversion operators
            entry("flip_x", FlipXDSP.class),
            entry("flip_z", FlipZDSP.class),
            entry("from_int", FromIntDSP.class),
            entry("swap_axes", SwapAxesDSP.class),
            //parse operators (including deprecated ParseTiffXXXXDSPs)
            entry("parse_png_terrarium", ParseTerrariumPngDSP.class),
            entry("parse_tiff", ParseTiffDSP.class),
            entry("parse_tiff_auto", ParseTiffAutoDSP.class),
            entry("parse_tiff_fp", ParseTiffFloatingPointDSP.class),
            entry("parse_tiff_int", ParseTiffIntDSP.class)
    ));

    public final BiMap<String, Class<? extends IntScalarParser>> SCALAR_PARSERS_INT = HashBiMap.create(ofEntries(
            //arithmetic operators
            entry("add", AddISP.class),
            //logical operators
            entry("and", AndISP.class),
            //conversion operators
            entry("flip_x", FlipXISP.class),
            entry("flip_z", FlipZISP.class),
            entry("grayscale_extract", GrayscaleExtractISP.class),
            entry("require_opaque", RequireOpaqueISP.class),
            entry("rgb_extract", RGBExtractISP.class),
            entry("swap_axes", SwapAxesISP.class),
            //parse operators
            entry("parse_jpg", ParseJpgISP.class),
            entry("parse_png", ParsePngISP.class),
            entry("parse_tiff", ParseTiffISP.class)
    ));

}
