package com.yintong.erp.mini.controller;

import com.yintong.erp.mini.domain.WxMiniUser;
import com.yintong.erp.mini.service.MiniAppService;

import com.yintong.erp.service.sale.SaleOrderService;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.SimpleRemote;
import com.yintong.erp.web.stock.StockController;
import java.io.IOException;
import java.text.MessageFormat;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.yintong.erp.mini.service.MiniAppService.UNBIND_OPENID_PREFIX;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;

/**
 * @author lucifer.chan
 * @create 2018-08-16 上午12:29
 * 微信小程序
 **/
@RestController
@RequestMapping("m")
public class MiniAppController {

    @Value("${wx.mini.url.openId}")
    private String openIdUrl;

    @Autowired StockController stockController;

    @Autowired MiniAppService miniAppService;

    @Autowired SaleOrderService saleOrderService;

    private SimpleRemote simpleRemote = SimpleRemote.instance();

    /**
     * 生成token
     * @param params 除了openId
     * @return {token : xxxx} token==unbind_{openId}时为当前小程序用户尚未绑定系统用户
     * @throws IOException
     */
    @PostMapping("token")
    public BaseResult makeToken(@RequestBody WxMiniUser params) throws IOException {
        String code = params.getCode();
        String url = MessageFormat.format(openIdUrl, code);
        JSONObject ret = JSONObject.fromObject(simpleRemote.get(url, null));
        String openId = ret.getString("openid");
        Assert.hasText(openId, ret.optString("errmsg", "调用微信后台出错"));
        params.setOpenid(openId);
        String token = miniAppService.makeToken(params);
        return new BaseResult().put("token", token);
    }

    /**
     * 登录
     * @param openId
     * @param loginName
     * @param password
     * @return
     */
    @PostMapping("token/login")
    public BaseResult login(String openId, String loginName, String password){
        Assert.hasText(openId, "openId不能为空");
        Assert.hasText(loginName, "登录名不能为空");
        Assert.hasText(password, "密码不能为空");
        Assert.isTrue(openId.startsWith(UNBIND_OPENID_PREFIX), "openId格式错误");
        String token = miniAppService.login(openId, loginName, password);
        return new BaseResult().put("token", token);
    }

    /**
     * 扫码查找单个仓位
     * @param barcode
     * @return
     */
    @GetMapping("scan/place")
    public BaseResult scanPlace(String barcode){
        Assert.isTrue(StringUtils.hasText(barcode) && barcode.startsWith(S000.name()), "请扫描仓位条码！");
        return stockController.one(barcode);
    }

    /**
     * 扫码销售订单
     * @param barcode
     * @return 包含明细
     */
    @GetMapping("scan/saleOrder")
    public BaseResult scanSaleOrder(String barcode){
        Assert.isTrue(StringUtils.hasText(barcode) && barcode.startsWith(X000.name()), "请扫描销售单条码！");
        return new BaseResult().addPojo(saleOrderService.findOrder4Out(barcode));
    }


}
