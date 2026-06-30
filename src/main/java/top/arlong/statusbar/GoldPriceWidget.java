package top.arlong.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.arlong.services.GoldPriceData;
import top.arlong.services.GoldPriceService;
import top.arlong.settings.GoldSettings;

import java.awt.event.MouseEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 金价显示 Widget
 */
public class GoldPriceWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

    private final Project project;
    private String displayText = "加载中...";
    private String tooltipText = "点击刷新金价";
    private ScheduledExecutorService scheduler;
    private StatusBar statusBar;

    public GoldPriceWidget(Project project) {
        this.project = project;
        startScheduledUpdate();
    }

    private void startScheduledUpdate() {
        scheduler = new ScheduledThreadPoolExecutor(1);
        GoldSettings settings = GoldSettings.getInstance();

        scheduler.scheduleAtFixedRate(() -> {
            updateGoldPrice();
        }, 0, settings.updateInterval, TimeUnit.MINUTES);
    }

    private void updateGoldPrice() {
        try {
            GoldSettings settings = GoldSettings.getInstance();

            // 根据配置获取金价数据
            GoldPriceData internationalData = GoldPriceService.getGoldPrice(settings.priceType);

            if(internationalData != null && internationalData.isValid()) {
                // 根据金价类型计算收益
                double totalProfit = 0;
                double todayProfit = 0;
                double holdingAmount = 0;
                double costPrice = 0;

                if("AU9999".equals(settings.priceType)) {
                    // AU9999：使用克和元/克
                    holdingAmount = settings.au9999HoldingWeight;
                    costPrice = settings.au9999CostPrice;
                    double currentPrice = internationalData.getPrice();
                    double changeAmount = internationalData.getChangeAmount();

                    // 累计收益 = (当前价 - 成本价) × 持仓克重
                    totalProfit = (currentPrice - costPrice) * holdingAmount;
                    // 今日收益 = 涨跌额 × 持仓克重
                    todayProfit = changeAmount * holdingAmount;
                }else {
                    // XAU：使用盎司和美元/盎司
                    holdingAmount = settings.xauHoldingOunces;
                    costPrice = settings.xauCostPrice;
                    double currentPrice = internationalData.getPrice();
                    double changeAmount = internationalData.getChangeAmount();

                    // 累计收益 = (当前价 - 成本价) × 持仓盎司数
                    totalProfit = (currentPrice - costPrice) * holdingAmount;
                    // 今日收益 = 涨跌额 × 持仓盎司数
                    todayProfit = changeAmount * holdingAmount;
                }

                String arrow = internationalData.getChangePercent() >= 0 ? "▲" : "▼";
                String newText = String.format("%s: %.2f %s %.2f%%",
                        settings.priceType,
                        internationalData.getPrice(),
                        arrow,
                        Math.abs(internationalData.getChangePercent()));

                // 构建详细的 tooltip 信息
                String internationalArrow = internationalData.getChangePercent() >= 0 ? "▲" : "▼";
                String changeColor = internationalData.getChangePercent() >= 0 ? "#FF0000" : "#00AA00";

                // 根据金价类型显示不同的标题和单位
                String priceTitle;
                String priceUnit;
                if("AU9999".equals(settings.priceType)) {
                    priceTitle = "国内金价 (人民币/克)";
                    priceUnit = "元/克";
                }else {
                    priceTitle = "国际金价 (美元/盎司)";
                    priceUnit = "美元/盎司";
                }

                // 构建收益信息部分
                String profitSection = "";
                if(holdingAmount > 0 && costPrice > 0) {
                    String totalProfitColor = totalProfit >= 0 ? "#FF0000" : "#00AA00";
                    String todayProfitColor = todayProfit >= 0 ? "#FF0000" : "#00AA00";
                    String totalProfitArrow = totalProfit >= 0 ? "▲" : "▼";
                    String todayProfitArrow = todayProfit >= 0 ? "▲" : "▼";

                    String holdingInfo;
                    String currencySymbol;

                    if("AU9999".equals(settings.priceType)) {
                        holdingInfo = String.format("持仓: %.2f 克 | 成本: %.2f 元/克", holdingAmount, costPrice);
                        currencySymbol = "元";
                    }else {
                        holdingInfo = String.format("持仓: %.2f 盎司 | 成本: %.2f 美元/盎司", holdingAmount, costPrice);
                        currencySymbol = "美元";
                    }

                    profitSection = String.format(
                            "<div style='margin-top: 10px; padding-top: 8px; border-top: 1px solid #CCCCCC;'>" +
                                    "<b style='font-size: 10px;'>持仓信息</b><br/>" +
                                    "<div style='margin-top: 4px; font-size: 10px;'>" +
                                    "%s<br/>" +
                                    "</div>" +
                                    "<div style='margin-top: 6px; color: %s;'>" +
                                    "<span style='font-weight: bold;'>累计收益: %s %.2f %s</span><br/>" +
                                    "</div>" +
                                    "<div style='margin-top: 4px; color: %s;'>" +
                                    "<span style='font-weight: bold;'>今日收益: %s %.2f %s</span><br/>" +
                                    "</div>" +
                                    "</div>",
                            holdingInfo,
                            totalProfitColor,
                            totalProfitArrow,
                            Math.abs(totalProfit),
                            currencySymbol,
                            todayProfitColor,
                            todayProfitArrow,
                            Math.abs(todayProfit),
                            currencySymbol
                    );
                }

                String finalTooltip = String.format(
                        "<html>" +
                                "<div style='padding: 5px;'>" +
                                "<b style='font-size: 11px;'>%s</b><br/>" +
                                "<div style='margin-top: 8px;'>" +
                                "<span style='font-size: 13px; font-weight: bold;'>%.2f</span><br/>" +
                                "</div>" +
                                "<div style='margin-top: 6px; color: %s;'>" +
                                "<span style='font-weight: bold;'>%s %.2f%%</span> " +
                                "<span style='font-size: 10px;'>(%.2f)</span><br/>" +
                                "</div>" +
                                "<div style='margin-top: 6px; font-size: 10px; color: #888888;'>" +
                                "更新时间: %s<br/>" +
                                "</div>" +
                                "%s" +
                                "<div style='margin-top: 8px; font-size: 9px; color: #999999; font-style: italic;'>" +
                                "点击刷新金价" +
                                "</div>" +
                                "</div>" +
                                "</html>",
                        priceTitle,
                        internationalData.getPrice(),
                        changeColor,
                        internationalArrow,
                        Math.abs(internationalData.getChangePercent()),
                        Math.abs(internationalData.getChangeAmount()),
                        internationalData.getUpdateTime(),
                        profitSection);

                ApplicationManager.getApplication().invokeLater(() -> {
                    displayText = newText;
                    tooltipText = finalTooltip;
                    if(statusBar != null) {
                        statusBar.updateWidget(ID());
                    }
                });
            }else {
                ApplicationManager.getApplication().invokeLater(() -> {
                    displayText = "数据无效";
                    tooltipText = "点击刷新金价";
                    if(statusBar != null) {
                        statusBar.updateWidget(ID());
                    }
                });
            }
        }catch(Exception e) {
            e.printStackTrace();
            ApplicationManager.getApplication().invokeLater(() -> {
                displayText = "获取失败: " + e.getMessage();
                tooltipText = "点击刷新金价";
                if(statusBar != null) {
                    statusBar.updateWidget(ID());
                }
            });
        }
    }

    @Override
    public @NotNull String ID() {
        return "GoldPriceWidget";
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {
        if(scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public @NotNull String getText() {
        return displayText;
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable String getTooltipText() {
        return tooltipText;
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> updateGoldPrice();
    }
}
