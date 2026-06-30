package top.arlong.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.arlong.settings.GoldSettings;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 金价数据服务
 */
public class GoldPriceService {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * 获取金价数据
     * 支持 AU9999（国内金价）和 XAU（国际金价）
     */
    public static GoldPriceData getGoldPrice(String priceType) throws IOException {
        String url = buildUrl(priceType);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://finance.sina.com.cn/")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .addHeader("Connection", "keep-alive")
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(!response.isSuccessful()) {
                throw new IOException("HTTP error code: " + response.code());
            }

            // 检查响应内容类型
            String contentType = response.header("Content-Type", "");
            System.out.println("Content-Type: " + contentType);

            String body = response.body().string();

            // 检查响应是否为空或过短
            if(body == null || body.trim().isEmpty()) {
                throw new IOException("响应内容为空");
            }

            if(body.length() < 10) {
                throw new IOException("响应内容过短: " + body);
            }

            // 检查是否为 HTML 错误页面
            if(body.trim().startsWith("<") && body.contains("html")) {
                System.err.println("API 返回了 HTML 页面，可能被阻止或重定向");
                throw new IOException("API 返回了 HTML 错误页面");
            }

            return parseGoldPrice(body, priceType);
        }
    }

    private static String buildUrl(String priceType) {
        GoldSettings settings = GoldSettings.getInstance();
        String apiKey = settings.apiKey;

        // 使用聚合数据 API
        if("AU9999".equals(priceType)) {
            // 国内金价：使用聚合数据 API
            if(apiKey != null && !apiKey.trim().isEmpty()) {
                return "http://web.juhe.cn:8080/finance/gold/shgold?key=" + apiKey;
            }else {
                // 如果没有配置 API Key，使用新浪财经作为备用
                return "https://hq.sinajs.cn/list=hf_GC";
            }
        }else if("XAU".equals(priceType)) {
            // 国际金价：COMEX 黄金期货
            return "https://hq.sinajs.cn/list=hf_XAU";
        }
        return "https://hq.sinajs.cn/list=hf_GC";
    }

    private static GoldPriceData parseGoldPrice(String data, String priceType) {
        try {
            System.out.println("API 返回的原始数据: " + data);

            // 判断是 JSON 格式还是新浪财经格式
            if(data.trim().startsWith("{")) {
                // JSON 格式 - 聚合数据 API
                return parseJuheApiResponse(data, priceType);
            }else {
                // 新浪财经格式
                return parseSinaApiResponse(data, priceType);
            }
        }catch(Exception e) {
            System.err.println("解析金价数据时发生异常: " + e.getMessage());
            e.printStackTrace();
            return new GoldPriceData();
        }
    }

    /**
     * 解析聚合数据 API 的 JSON 响应
     */
    private static GoldPriceData parseJuheApiResponse(String data, String priceType) {
        GoldPriceData goldPrice = new GoldPriceData();

        try {
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            int errorCode = json.get("error_code").getAsInt();

            if(errorCode != 0) {
                System.err.println("聚合数据 API 错误: " + json.get("reason").getAsString());
                return goldPrice;
            }

            JsonArray result = json.getAsJsonArray("result");
            if(result.size() == 0) {
                System.err.println("聚合数据 API 返回空数据");
                return goldPrice;
            }

            // 获取第一个对象（包含所有金价数据）
            JsonObject allData = result.get(0).getAsJsonObject();

            // 查找 Au99.99 的数据（key 是 "4"）
            JsonObject goldData = null;
            if(allData.has("4")) {
                goldData = allData.getAsJsonObject("4");
            }

            if(goldData == null) {
                System.err.println("未找到 Au99.99 金价数据");
                return goldPrice;
            }

            goldPrice.setCode("AU9999");
            goldPrice.setName("国内金价");

            // 获取最新价格
            String latestPriStr = goldData.get("latestpri").getAsString();
            if(latestPriStr.equals("--")) {
                System.err.println("Au99.99 金价数据为空");
                return goldPrice;
            }

            double latestPrice = Double.parseDouble(latestPriStr);
            goldPrice.setPrice(latestPrice);

            // 获取开盘价
            String openPriStr = goldData.get("openpri").getAsString();
            double openPrice = 0;
            if(!openPriStr.equals("--")) {
                openPrice = Double.parseDouble(openPriStr);
            }

            // 计算涨跌
            if(openPrice > 0) {
                goldPrice.setChangeAmount(latestPrice - openPrice);
                goldPrice.setChangePercent((latestPrice - openPrice) / openPrice * 100);
            }

            // 设置更新时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            goldPrice.setUpdateTime(sdf.format(new Date()));

            System.out.println("成功解析聚合数据金价: " + latestPrice + " 元/克");

        }catch(Exception e) {
            System.err.println("解析聚合数据 JSON 时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return goldPrice;
    }

    /**
     * 解析新浪财经 API 的文本响应
     */
    private static GoldPriceData parseSinaApiResponse(String data, String priceType) {
        GoldPriceData goldPrice = new GoldPriceData();

        try {
            // 新浪财经返回格式: var hf_GC="5119.992,,5116.900,5117.400,..."
            // 或者: hf_GC="5119.992,,5116.900,5117.400,..." (不带 var)

            // 检查数据是否包含必要的标识
            if(!data.contains("hf_") && !data.contains("=")) {
                System.err.println("解析失败: 数据不包含预期的格式标识");
                System.err.println("数据内容（前100字符）: " +
                        (data.length() > 100 ? data.substring(0, 100) : data));
                return goldPrice;
            }

            String[] parts = data.split("\"");
            if(parts.length < 2) {
                System.err.println("解析失败: 数据格式不正确，parts.length=" + parts.length);
                System.err.println("原始数据: " + data);

                // 尝试另一种解析方式：直接从等号后提取
                if(data.contains("=")) {
                    int startIndex = data.indexOf("=") + 1;
                    String remainingData = data.substring(startIndex).trim();

                    // 移除可能的引号
                    remainingData = remainingData.replace("\"", "").replace(";", "").trim();

                    if(!remainingData.isEmpty()) {
                        System.out.println("尝试备用解析方法，数据: " + remainingData);
                        String[] values = remainingData.split(",");
                        if(values.length >= 4) {
                            parts = new String[]{"", remainingData};
                            System.out.println("备用解析成功，继续处理");
                        }else {
                            return goldPrice;
                        }
                    }else {
                        return goldPrice;
                    }
                }else {
                    return goldPrice;
                }
            }

            String[] values = parts[1].split(",");
            System.out.println("解析后的字段数量: " + values.length);

            if(values.length < 4) {
                System.err.println("解析失败: 数据字段不足，values.length=" + values.length);
                return goldPrice;
            }

            goldPrice.setCode(priceType);
            goldPrice.setName(priceType.equals("AU9999") ? "国内金价" : "国际金价");

            // 查找第一个非空的价格字段
            double price = 0;
            for(int i = 0; i < values.length && i < 10; i++) {
                if(!values[i].trim().isEmpty()) {
                    try {
                        price = Double.parseDouble(values[i].trim());
                        if(price > 0) {
                            System.out.println("找到有效价格在 values[" + i + "]: " + price);
                            break;
                        }
                    }catch(NumberFormatException ignored) {
                    }
                }
            }

            goldPrice.setPrice(price);

            // 查找开盘价
            double openPrice = 0;
            for(int i = 1; i < values.length && i < 10; i++) {
                if(!values[i].trim().isEmpty()) {
                    try {
                        double tempPrice = Double.parseDouble(values[i].trim());
                        if(tempPrice > 0 && tempPrice != price) {
                            openPrice = tempPrice;
                            break;
                        }
                    }catch(NumberFormatException ignored) {
                    }
                }
            }

            if(openPrice == 0) {
                openPrice = price;
            }

            // 计算涨跌
            if(openPrice > 0 && price > 0) {
                goldPrice.setChangeAmount(price - openPrice);
                goldPrice.setChangePercent((price - openPrice) / openPrice * 100);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            goldPrice.setUpdateTime(sdf.format(new Date()));

            System.out.println("成功解析新浪金价数据: " + price);

        }catch(Exception e) {
            System.err.println("解析新浪数据时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return goldPrice;
    }
}
