package nissining.musicplus.player;

import cn.nukkit.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nissining
 **/
public class MusicPlayer {

    private static final List<MusicPlayer> mps = new ArrayList<>();

    public static MusicPlayer getMusicPlayer(String p) {
        return mps.stream()
                .filter(mp -> mp.isSameName(p))
                .findFirst()
                .orElse(null);
    }

    public static void addMusicPlayer(Player player) {
        if (getMusicPlayer(player.getName()) == null) {
            mps.add(new MusicPlayer(player));
        }
    }

    public static void removeMusicPlayer(Player player) {
        MusicPlayer mp;
        if ((mp = getMusicPlayer(player.getName())) != null) {
            mps.remove(mp);
        }
    }

    public static List<MusicPlayer> getMps() {
        return mps;
    }

    protected Player player;
    public float vol = 100;
    public boolean stopMusic = false;

    public MusicPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSameName(String n) {
        return player.getName().equalsIgnoreCase(n);
    }

    public void setVol(float vol) {
        this.vol = vol;
    }

    public float getVol() {
        return vol;
    }

    public boolean isStopMusic() {
        return stopMusic;
    }

}
