package org.atoiks.seihou;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author YTENG
 */
public final class BulletPatternAssembler {

    private final Map<String, List<Float>> groups = new HashMap<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private final Deque<List<Float>> arr = new ArrayDeque<>();
    private final Map<String, Float> props = new HashMap<>();
    private List<Float> cached;

    private BulletPatternAssembler() {
        arr.addLast(cached = new ArrayList<>());
    }

    public static float[] assembleFromStream(InputStream is) {
        final BulletPatternAssembler a = new BulletPatternAssembler();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String s;
            while ((s = br.readLine()) != null) {
                a.assemblePart(s);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return a.getBuffer();
    }

    public static float[] assemble(String src) {
        final BulletPatternAssembler a = new BulletPatternAssembler();
        a.assemblePart(src);
        return a.getBuffer();
    }

    public void assemblePart(String src) {
        final String[] frags = src.split("\\s+");
        outer:
        for (int i = 0; i < frags.length; ++i) {
            switch (frags[i]) {
                case "":
                    break;
                case "delay":
                case "spacing":
                case "tilt":
                case "size":
                case "speed":
                case "x":
                case "y":
                case "limit":
                    props.put(frags[i], Float.parseFloat(frags[++i]));
                    break;
                case "{":
                    arr.addLast(cached = new ArrayList<>());
                    break;
                case "}":
                    groups.put(frags[++i], arr.removeLast());
                    cached = arr.getLast();
                    break;
                case "group":
                    cached.addAll(groups.get(frags[++i]));
                    break;
                case "label":
                    if (arr.size() != 1) {
                        System.err.println("WARNING: LABEL USED IN GROUP");
                    }
                    labels.put(frags[++i], cached.size());
                    break;
                case "jmp":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(9f); // jmp is type 9
                    cached.add((float) labels.getOrDefault(frags[++i], 0));
                    break;
                case "nop":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(0f); // nop is type 0
                    break;
                case "boss.radial":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(1f); // boss.radial is type 1
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    cached.add(props.getOrDefault("speed", 0f));
                    break;
                case "radial":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(2f); // radial is type 2
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(360f); // radial implies 360 degrees
                    cached.add(props.getOrDefault("size", 0f));
                    cached.add(props.getOrDefault("speed", 0f));
                    break;
                case "limrad":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(2f); // limrad is type 7
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("limit", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    cached.add(props.getOrDefault("speed", 0f));
                    break;
                case "boss.limrad":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(7f); // boss.limrad is type 7
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("limit", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    cached.add(props.getOrDefault("speed", 0f));
                    break;
                case "boss.setpos":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(3f); // boss.setpos is type 3
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    break;
                case "boss.incpos":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(4f); // boss.incpos is type 4
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    break;
                case "boss.setspd":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(5f); // boss.setspd is type 5
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    break;
                case "boss.incspd":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(6f); // boss.incspd is type 6
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    break;
                case "enemy.weak":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(8f); // enemy.weak is type 8
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    cached.add(props.getOrDefault("limit", 0f));
                    break;
                case "enemy.radial":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(10f); // enemy.radial is type 10
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    break;
                case "enemy.spiral":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(11f); // enemy.spiral is type 11
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    break;
                case "enemy.orbital":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(12f); // enemy.orbital is type 12
                    cached.add(props.getOrDefault("x", 0f));
                    cached.add(props.getOrDefault("y", 0f));
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    break;
                case "boss.orbital":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(13f); // boss.orbital is type 13
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    break;
                case "player.orbital":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(14f); // player.orbital is type 14
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    break;
                case "player.shield":
                    cached.add(props.getOrDefault("delay", 0f));
                    cached.add(15f); // player.shield is type 15
                    cached.add(props.getOrDefault("spacing", 0f));
                    cached.add(props.getOrDefault("tilt", 0f));
                    cached.add(props.getOrDefault("size", 0f));
                    break;
                default:
                    System.err.println("FAILED TO ASSEMBLE " + frags[i]);
                    break outer;
            }
        }
    }

    public float[] getBuffer() {
        final Float[] f = arr.stream().flatMap(x -> x.stream()).toArray(Float[]::new);
        final float[] ret = new float[f.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = f[i];
        }
        return ret;
    }
}
