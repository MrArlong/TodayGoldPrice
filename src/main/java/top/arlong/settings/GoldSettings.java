package top.arlong.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 金价设置持久化
 */
@State(
        name = "top.arlong.settings.GoldSettings",
        storages = @Storage("ObserveGoldSettings.xml")
)
public class GoldSettings implements PersistentStateComponent<GoldSettings> {

    public String priceType = "XAU";  // 默认显示国际金价
    public int updateInterval = 15;        // 更新间隔（分钟）
    public boolean enableNotification = false;  // 是否启用通知
    public String apiKey = "";  // 聚合数据 API Key

    // AU9999（国内金价）相关配置
    public double au9999HoldingWeight = 0.0;  // 持仓克重
    public double au9999CostPrice = 0.0;  // 成本均价（元/克）

    // XAU（国际金价）相关配置
    public double xauHoldingOunces = 0.0;  // 持仓盎司数
    public double xauCostPrice = 0.0;  // 成本均价（美元/盎司）

    public static GoldSettings getInstance() {
        return ApplicationManager.getApplication().getService(GoldSettings.class);
    }

    @Nullable
    @Override
    public GoldSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GoldSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
