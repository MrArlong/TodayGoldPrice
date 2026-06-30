package top.arlong.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 设置界面配置
 */
public class GoldSettingsConfigurable implements Configurable {

    private GoldSettingsComponent settingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "今日金价";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new GoldSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        GoldSettings settings = GoldSettings.getInstance();
        return !settingsComponent.getPriceType().equals(settings.priceType) ||
                settingsComponent.getUpdateInterval() != settings.updateInterval ||
                !settingsComponent.getApiKey().equals(settings.apiKey) ||
                settingsComponent.getHoldingWeight() != settings.au9999HoldingWeight ||
                settingsComponent.getCostPrice() != settings.au9999CostPrice ||
                settingsComponent.getXauHoldingOunces() != settings.xauHoldingOunces ||
                settingsComponent.getXauCostPrice() != settings.xauCostPrice;
    }

    @Override
    public void apply() {
        GoldSettings settings = GoldSettings.getInstance();
        settings.priceType = settingsComponent.getPriceType();
        settings.updateInterval = settingsComponent.getUpdateInterval();
        settings.apiKey = settingsComponent.getApiKey();
        settings.au9999HoldingWeight = settingsComponent.getHoldingWeight();
        settings.au9999CostPrice = settingsComponent.getCostPrice();
        settings.xauHoldingOunces = settingsComponent.getXauHoldingOunces();
        settings.xauCostPrice = settingsComponent.getXauCostPrice();
    }

    @Override
    public void reset() {
        GoldSettings settings = GoldSettings.getInstance();
        settingsComponent.setPriceType(settings.priceType);
        settingsComponent.setUpdateInterval(settings.updateInterval);
        settingsComponent.setApiKey(settings.apiKey);
        settingsComponent.setHoldingWeight(settings.au9999HoldingWeight);
        settingsComponent.setCostPrice(settings.au9999CostPrice);
        settingsComponent.setXauHoldingOunces(settings.xauHoldingOunces);
        settingsComponent.setXauCostPrice(settings.xauCostPrice);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
