package com.ymcmp.seihou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author YTENG
 */
public final class BulletPatternAssembler {

    private BulletPatternAssembler() {
    }

    public static float[] assemble(String src) {
        return assemble(src, new HashMap<>());
    }

    public static float[] assemble(String src, Map<String, Float> props) {
        final List<Float> arr = new ArrayList<>();
        final String[] frags = src.split("\\s+");
        outer:
        for (int i = 0; i < frags.length; ++i) {
            switch (frags[i]) {
            case "delay":
            case "spacing":
            case "tilt":
            case "size":
            case "speed":
                props.put(frags[i], Float.parseFloat(frags[++i]));
                break;
            case "boss.radial":
                arr.add(props.getOrDefault("delay", 0f));
                arr.add(1f); // boss.radial is type 1
                arr.add(props.getOrDefault("spacing", 0f));
                arr.add(props.getOrDefault("tilt", 0f));
                arr.add(props.getOrDefault("size", 0f));
                arr.add(props.getOrDefault("speed", 0f));
                break;
            default:
                break outer;
            }
        }

        final float[] ret = new float[arr.size()];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = arr.get(i);
        }
        return ret;
    }
}
