package nissining.musicplus.player;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import nissining.musicplus.utils.FormAPI;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerMenu {

    public static List<MpmButton> buttons = new ArrayList<>() {{
        add(new MpmButton("调整音量", "items/Egg"));
        add(new MpmButton("播放/停止音乐", "items/Egg"));
        add(new MpmButton("上一首", "items/Egg"));
        add(new MpmButton("下一首", "items/Egg"));
    }};

    public void openMenu(MusicPlayer mp) {
        Player player = mp.getPlayer();
        if (player == null)
            return;

        FormWindowSimple f = new FormWindowSimple("Music Menu", "个人设置，不影响播放器");

        buttons.forEach(f::addButton);

        FormAPI formAPI = new FormAPI(player, f) {
            @Override
            public void call() {
                if (wasClosed())
                    return;

                switch (getButtonText()) {
                    case "调整音量":
                        volMenu(mp);
                        break;
                    case "播放/停止音乐":
                        mp.stopMusic = !mp.stopMusic;
                        player.sendMessage("音乐： " + (mp.stopMusic ? "停止" : "播放"));
                        break;
                    case "上一首":
                        break;
                    case "下一首":
                        break;
                }

            }
        };

        formAPI.sendToPlayer(player);
    }

    private void volMenu(MusicPlayer mp) {
        FormWindowSimple f = new FormWindowSimple("Vol Menu", "音量设置 - 请选择下面的数值");

        for (int i = 0; i < 100; i += 25) {
            f.addButton(new ElementButton(i + ""));
        }

        FormAPI formAPI = new FormAPI(mp.getPlayer(), f) {
            @Override
            public void call() {
                if (wasClosed())
                    return;

                mp.vol = Byte.parseByte(getButtonText());
                mp.getPlayer().sendMessage("现在音量为： " + mp.vol);
            }
        };
        formAPI.sendToPlayer(mp.getPlayer());
    }

    public static class MpmButton extends ElementButton {
        public MpmButton(String s, String img) {
            super(s);
            this.addImage(new ElementButtonImageData("path", "texture/" + img));
        }
    }

}
