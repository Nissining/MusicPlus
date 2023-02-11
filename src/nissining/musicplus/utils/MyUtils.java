package nissining.musicplus.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * @author Nissining
 **/
public class MyUtils {

    /**
     * 进度条
     * <p>
     * 1---------------
     * -1--------------
     * --1-------------
     * ---1------------
     *
     * @param now  当前值
     * @param max  最大值
     * @param show 显示数值
     * @return 进度条
     */
    public static String addPar(double now, double max, boolean show) {
        BigDecimal rate = BigDecimal.valueOf(now / max * 20);

        StringBuilder s = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            if (i < rate.intValue()) {
                s.append("§a|");
            } else if (i == rate.intValue()) {
                s.append("§e§l|");
            } else {
                s.append("§r§7|");
            }
        }

        // 显示百分比数值
        if (show) {
            BigDecimal i = BigDecimal.valueOf(now / max * 100).setScale(1, RoundingMode.HALF_UP);
            s.append(" ");
            s.append(i.doubleValue());
            s.append("%");
        }

        return s.toString();
    }

    public static String getFt(int time) {
        int sec, miu, hour;
        int t = time;
        sec = t % 60;
        t = t / 60;
        miu = t % 60;
        t = t / 60;
        hour = t % 12;
        return String.format("%02d:%02d:%02d", hour, miu, sec);
    }

    public static int rand(int min, int max) {
        if (min == max) {
            return max;
        }
        return new Random().nextInt(max + 1 - min) + min;
    }

}
