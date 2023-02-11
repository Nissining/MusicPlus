package nissining.musicplus.music.utils;

import java.io.File;
import java.util.HashMap;

public class Song {

    private final HashMap<Integer, Layer> layerHashMap;
    private final short songHeight;
    private final short length;
    private final String title;
    private final File path;
    private final String author;
    private final String description;
    private final float speed;
    private final float delay;
    private final String songName;

    public Song(float speed, HashMap<Integer, Layer> layerHashMap,
                short songHeight, final short length, String title, String author,
                String description, File path) {
        this.speed = speed;
        delay = 20 / speed;
        this.layerHashMap = layerHashMap;
        this.songHeight = songHeight;
        this.length = length;
        this.title = title;
        this.author = author;
        this.description = description;
        this.path = path;
        this.songName = path.getName().substring(0, path.getName().indexOf(".nbs"));
    }

    public HashMap<Integer, Layer> getLayerHashMap() {
        return layerHashMap;
    }

    public short getSongHeight() {
        return songHeight;
    }

    public short getLength() {
        return length;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public File getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public float getSpeed() {
        return speed;
    }

    public float getDelay() {
        return delay;
    }

    public String getSongName() {
        return songName;
    }

    public String getFormatSongName(String tsn) {
        return (tsn.equalsIgnoreCase(getSongName()) ? "§l§a" : "§f") + getSongName();
    }

}
