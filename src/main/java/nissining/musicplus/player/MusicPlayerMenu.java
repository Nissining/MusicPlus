package nissining.musicplus.player;

import cn.hutool.core.collection.CollUtil;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementStepSlider;
import lombok.val;
import nissining.musicplus.utils.MyForm;

import java.util.LinkedList;

/**
 * @author Nissining
 **/
public class MusicPlayerMenu {

    public static LinkedList<MpmButton> buttons = CollUtil.newLinkedList(
            new MpmButton("调整音量", "items/hopper"),
            new MpmButton("播放/停止音乐", "items/slimeball")
    );

    public static void openMenu(Player player) {
        MusicPlayer mp = MusicPlayer.players.get(player.getName());
        if (mp == null) {
            player.sendMessage("出错！请重新进入服务器后再试");
            return;
        }
        MyForm.simple("我的音乐设置菜单", "", buttons, new MyForm(player) {
            @Override
            public void call() {
                if (wasClosed()) {
                    return;
                }
                switch (getButtonText()) {
                    case "调整音量" -> volMenu(mp);
                    case "播放/停止音乐" -> {
                        mp.stopMusic = !mp.stopMusic;
                        player.sendMessage("音乐： " + (mp.stopMusic ? "停止" : "播放"));
                    }
                }
            }
        });
    }

    private static void volMenu(MusicPlayer mp) {
        val ls = CollUtil.newArrayList("");
        for (int i = 0; i < 100; i++) {
            ls.add(String.valueOf(i));
        }
        MyForm.custom(
                "音量大小设置",
                CollUtil.newLinkedList(
                        new ElementStepSlider("请选择下面的数值 (当前音量： " + mp.getVol(), ls)),
                new MyForm(mp.player) {
                    @Override
                    public void call() {
                        if (wasClosed()) {
                            return;
                        }
                        mp.vol = Float.parseFloat(getButtonText());
                        mp.getPlayer().sendMessage("现在音量为： " + mp.vol);
                    }
                }
        );
    }

    public static class MpmButton extends ElementButton {
        public MpmButton(String s, String img) {
            super(s);
            this.addImage(new ElementButtonImageData("path", "textures/" + img + ".png"));
        }
    }

}
