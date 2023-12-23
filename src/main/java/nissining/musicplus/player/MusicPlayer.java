package nissining.musicplus.player;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

/**
 * @author Nissining
 **/
@Getter
@RequiredArgsConstructor
public class MusicPlayer {

    public static final LinkedHashMap<String, MusicPlayer> players = new LinkedHashMap<>();

    protected final Player player;
    private final Config config;
    public float vol = 100;
    public boolean stopMusic = false;

    public void save() {
        config.set("vol", vol);
        config.set("music", stopMusic);
        config.save();
    }

}
