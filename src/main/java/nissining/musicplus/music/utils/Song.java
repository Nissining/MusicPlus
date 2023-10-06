package nissining.musicplus.music.utils;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.HashMap;

@Data
@EqualsAndHashCode(of = {"songName", "title"})
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

    public Song(float speed,
                HashMap<Integer, Layer> layerHashMap,
                short songHeight,
                final short length,
                String title,
                String author,
                String description,
                File path) {
        this.speed = speed;
        this.delay = 20 / speed;
        this.layerHashMap = layerHashMap;
        this.songHeight = songHeight;
        this.length = length;
        this.title = title;
        this.author = author;
        this.description = description;
        this.path = path;
        this.songName = StrUtil.removeSuffix(path.getName(), ".nbs");
    }

    public String getFormatSongName(int index, Song targetSong) {
        if (targetSong.equals(this)) {
            return StrUtil.format("&l&a{}.{}", index + 1, getSongName());
        }
        return StrUtil.format("&7{}.{}", index + 1, targetSong.getSongName());
    }

}
