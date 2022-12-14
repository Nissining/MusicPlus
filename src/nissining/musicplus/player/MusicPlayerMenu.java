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
        add(new MpmButton("调整音量", "items/hopper"));
        add(new MpmButton("播放/停止音乐", "items/barrier"));
    }};

    public static void openMenu(Player player) {
        String t = "";
        MusicPlayer mp = MusicPlayer.getMusicPlayer(player.getName());
        if (mp != null) {
            FormWindowSimple f = new FormWindowSimple("MusicPlus Menu", "个人设置，不影响播放器");
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
                    }

                }
            };
            formAPI.sendToPlayer(player);
        } else {
            t = "打开失败！";
        }

        if (!t.isEmpty()) {
            player.sendMessage(t);
        }
    }

    private static void volMenu(MusicPlayer mp) {
        FormWindowSimple f = new FormWindowSimple(
                "Vol Menu",
                "音量设置 - 请选择下面的数值\n当前音量： " + mp.getVol());

        for (int i = 0; i <= 100; i += 25) {
            f.addButton(new ElementButton(i + ""));
        }

        FormAPI formAPI = new FormAPI(mp.getPlayer(), f) {
            @Override
            public void call() {
                if (wasClosed())
                    return;

                mp.setVol(Float.parseFloat(getButtonText()));
                mp.getPlayer().sendMessage("现在音量为： " + mp.vol);
            }
        };
        formAPI.sendToPlayer(mp.getPlayer());
    }

    public static class MpmButton extends ElementButton {
        public MpmButton(String s, String img) {
            super(s);
            this.addImage(new ElementButtonImageData("path", "textures/" + img + ".png"));
        }
    }

}
