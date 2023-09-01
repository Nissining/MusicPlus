package nissining.musicplus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Nissining
 */
public class PageBean<T> {
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 总页数
     */
    private int totalPages;
    /**
     * 总记录数
     */
    private int totalRecords;
    /**
     * 各个玩家当前页
     */
    public static HashMap<String, Integer> pageNumbers = new HashMap<>();

    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页记录数
     *
     * @param pageSize page size
     */
    public void setPageSize(int pageSize) {
        if (pageSize > 0) {
            this.pageSize = pageSize;
        } else {
            this.pageSize = 5;
        }
    }

    public int getTotalPages() {
        return totalPages;
    }

    /**
     * 设置总页数
     */
    public void setTotalPages() {
        this.totalPages = this.totalRecords % this.pageSize == 0 ? this.totalRecords / this.pageSize
                : this.totalRecords / this.pageSize + 1;
    }

    public int getTotalRecords() {
        return totalRecords;
    }


    /**
     * 设置总记录数
     *
     * @param totalRecords the totalRecords to set
     */
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = Math.max(totalRecords, 0);
    }

    public List<T> queryPager(int pageNo, int pageSize, List<T> list) {
        // 设置总记录数
        if (list != null && list.size() != 0) {
            this.setTotalRecords(list.size());
        } else {
            this.setTotalRecords(0);
        }
        // 设置每页记录数
        this.setPageSize(pageSize);
        // 设置总页数
        this.setTotalPages();
        // 设置当前页记录数
        if (list != null && list.size() != 0) {
            if (pageNo == getTotalPages()) {
                return list.subList((pageNo - 1) * pageSize, getTotalRecords());
            } else {
                return list.subList((pageNo - 1) * pageSize, pageNo * pageSize);
            }
        } else {
            return new ArrayList<>();
        }

    }

    public int getPageNoByPlayer(String player) {
        return pageNumbers.getOrDefault(player, 1);
    }

    /**
     * 是否能上一页
     *
     * @return boolean
     */
    public boolean isPrePage(String player) {
        return getPageNoByPlayer(player) > 1;
    }

    /**
     * 是否能下一页
     *
     * @return boolean
     */
    public boolean isNextPage(String player) {
        return getPageNoByPlayer(player) < this.totalPages;
    }

    /**
     * 上一页
     *
     * @param player Player
     */
    public boolean prePage(String player) {
        if (isPrePage(player)) {
            setPage(player, getPageNoByPlayer(player) - 1);
            return true;
        }
        return false;
    }

    /**
     * 下一页
     *
     * @param player Player
     */
    public boolean nextPage(String player) {
        if (isNextPage(player)) {
            setPage(player, getPageNoByPlayer(player) + 1);
            return true;
        }
        return false;
    }

    /**
     * 设置当前页
     *
     * @param player Player
     * @param page   Page
     */
    public void setPage(String player, int page) {
        pageNumbers.put(player, page);
    }

}
