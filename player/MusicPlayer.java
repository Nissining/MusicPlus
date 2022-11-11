package nissining.musicplus.player;

import cn.nukkit.Player;

public class MusicPlayer {

    protected Player player;
    public byte vol = 100;
    public boolean stopMusic = false;
    public int playId = 0;

    public void musicPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
