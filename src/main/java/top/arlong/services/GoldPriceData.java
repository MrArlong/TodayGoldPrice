package top.arlong.services;

/**
 * 金价数据模型
 */
public class GoldPriceData {
    private String code;          // 代码 (如 AU9999)
    private String name;          // 名称
    private double price;         // 当前价格
    private double changePercent; // 涨跌幅百分比
    private double changeAmount;  // 涨跌金额
    private String updateTime;    // 更新时间

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return code != null && !code.isEmpty() && price > 0;
    }
}
