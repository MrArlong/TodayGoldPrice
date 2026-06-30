package top.arlong.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

/**
 * 设置界面组件
 */
public class GoldSettingsComponent {

    private final JPanel mainPanel;
    private final JComboBox<String> priceTypeCombo = new JComboBox<>(new String[]{"AU9999", "XAU"});
    private final JBTextField updateIntervalField = new JBTextField();
    private final JBTextField apiKeyField = new JBTextField();

    // AU9999 字段
    private final JBTextField au9999HoldingWeightField = new JBTextField();
    private final JBTextField au9999CostPriceField = new JBTextField();
    private final JBLabel au9999HoldingLabel = new JBLabel("持仓克重:");
    private final JBLabel au9999CostLabel = new JBLabel("成本均价(元/克):");

    // XAU 字段
    private final JBTextField xauHoldingOuncesField = new JBTextField();
    private final JBTextField xauCostPriceField = new JBTextField();
    private final JBLabel xauHoldingLabel = new JBLabel("持仓盎司数:");
    private final JBLabel xauCostLabel = new JBLabel("成本均价(美元/盎司):");

    private JPanel holdingPanel;

    public GoldSettingsComponent() {
        // 创建持仓信息面板
        holdingPanel = new JPanel();
        updateHoldingPanel();

        // 监听金价类型变化
        priceTypeCombo.addActionListener(e -> updateHoldingPanel());

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("金价类型:"), priceTypeCombo, 1, false)
                .addLabeledComponent(new JBLabel("更新间隔(分钟):"), updateIntervalField, 1, false)
                .addLabeledComponent(new JBLabel("聚合数据 API Key:"), apiKeyField, 1, false)
                .addSeparator()
                .addComponent(holdingPanel, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void updateHoldingPanel() {
        holdingPanel.removeAll();

        String selectedType = (String) priceTypeCombo.getSelectedItem();
        FormBuilder builder = FormBuilder.createFormBuilder();

        if("AU9999".equals(selectedType)) {
            builder.addLabeledComponent(au9999HoldingLabel, au9999HoldingWeightField, 1, false)
                    .addLabeledComponent(au9999CostLabel, au9999CostPriceField, 1, false);
        }else {
            builder.addLabeledComponent(xauHoldingLabel, xauHoldingOuncesField, 1, false)
                    .addLabeledComponent(xauCostLabel, xauCostPriceField, 1, false);
        }

        JPanel newPanel = builder.getPanel();
        holdingPanel.setLayout(new BoxLayout(holdingPanel, BoxLayout.Y_AXIS));
        holdingPanel.add(newPanel);
        holdingPanel.revalidate();
        holdingPanel.repaint();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public String getPriceType() {
        return (String) priceTypeCombo.getSelectedItem();
    }

    public void setPriceType(String priceType) {
        priceTypeCombo.setSelectedItem(priceType);
    }

    public int getUpdateInterval() {
        try {
            return Integer.parseInt(updateIntervalField.getText());
        }catch(NumberFormatException e) {
            return 5;
        }
    }

    public void setUpdateInterval(int interval) {
        updateIntervalField.setText(String.valueOf(interval));
    }

    public String getApiKey() {
        return apiKeyField.getText();
    }

    public void setApiKey(String apiKey) {
        apiKeyField.setText(apiKey);
    }

    public double getHoldingWeight() {
        try {
            return Double.parseDouble(au9999HoldingWeightField.getText());
        }catch(NumberFormatException e) {
            return 0.0;
        }
    }

    public void setHoldingWeight(double weight) {
        au9999HoldingWeightField.setText(String.valueOf(weight));
    }

    public double getCostPrice() {
        try {
            return Double.parseDouble(au9999CostPriceField.getText());
        }catch(NumberFormatException e) {
            return 0.0;
        }
    }

    public void setCostPrice(double price) {
        au9999CostPriceField.setText(String.valueOf(price));
    }

    // XAU 字段的 getter/setter
    public double getXauHoldingOunces() {
        try {
            return Double.parseDouble(xauHoldingOuncesField.getText());
        }catch(NumberFormatException e) {
            return 0.0;
        }
    }

    public void setXauHoldingOunces(double ounces) {
        xauHoldingOuncesField.setText(String.valueOf(ounces));
    }

    public double getXauCostPrice() {
        try {
            return Double.parseDouble(xauCostPriceField.getText());
        }catch(NumberFormatException e) {
            return 0.0;
        }
    }

    public void setXauCostPrice(double price) {
        xauCostPriceField.setText(String.valueOf(price));
    }
}
