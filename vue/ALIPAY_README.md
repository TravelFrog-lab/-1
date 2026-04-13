# 支付宝沙箱支付 - 后端说明

前端「确认支付」会请求：`POST /api/alipay/pay`，后端已提供该接口，你**只需配置密钥和网关**即可。

---

## 1. 添加 Maven 依赖

在项目的 **pom.xml** 里加入支付宝 SDK（若已有可忽略）：

```xml
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>4.38.157.ALL</version>
</dependency>
```

---

## 2. 配置项（你只需改这些）

在 **application.properties** 或 **application.yml** 中增加以下配置（值改成你在沙箱里拿到的）：

### application.properties 示例

```properties
# 沙箱 AppID（支付宝开放平台 - 沙箱应用）
alipay.app-id=你的沙箱AppID

# 应用私钥（你自己生成的 RSA2 私钥，可去掉头尾和换行）
alipay.private-key=你的应用私钥

# 支付宝公钥（沙箱页面里的「支付宝公钥」，不是「应用公钥」）
alipay.alipay-public-key=支付宝公钥

# 沙箱网关（一般不用改）
alipay.gateway-url=https://openapi-sandbox.dl.alipaydev.com/gateway.do

# 支付成功后浏览器跳转的地址（可填前端首页）
alipay.return-url=http://localhost:8080
```

### application.yml 示例

```yaml
alipay:
  app-id: 你的沙箱AppID
  private-key: 你的应用私钥
  alipay-public-key: 支付宝公钥
  gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  return-url: http://localhost:8080
```

---

## 3. 从哪里获取这些值

1. 打开 [支付宝开放平台](https://open.alipay.com/) → 登录 → **开发者中心** → **沙箱环境**。
2. **AppID**：沙箱应用下面有。
3. **应用私钥**：用支付宝提供的工具生成密钥对，把**私钥**复制到 `alipay.private-key`（可去掉 `-----BEGIN PRIVATE KEY-----` 等头尾和换行）。
4. **支付宝公钥**：沙箱里「接口加签方式」中上传你的应用公钥后，会显示「支付宝公钥」，复制到 `alipay.alipay-public-key`。
5. **return-url**：支付完成后要跳回的地址，例如前端首页 `http://localhost:8080` 或你的前端域名。

---

## 4. 接口说明

- **POST /api/alipay/pay**：创建支付订单，返回 `{ "code": "0", "data": { "payUrl": "https://...", "orderNo": "..." } }`，前端会跳转到 `payUrl` 打开支付宝沙箱支付页。
- 请求体由前端传：`amount`、`subject`、`outTradeNo`/`businessOrderNo`、`returnUrl`、`notifyUrl` 等，后端已兼容。

配置好上述项并重启后端，再在前端点击「确认支付」即可正常跳转支付宝沙箱。
