package com.yintong.erp.mini.controller;

import com.yintong.erp.domain.stock.ErpStockOptLog;
import com.yintong.erp.domain.stock.ErpStockOptLogRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.mini.domain.WxMiniUser;
import com.yintong.erp.mini.service.MiniAppService;

import com.yintong.erp.mini.service.MiniDtoWrapper;
import com.yintong.erp.service.basis.MouldService;
import com.yintong.erp.service.basis.ProductService;
import com.yintong.erp.service.basis.associator.SupplierMouldService;
import com.yintong.erp.service.basis.associator.SupplierProductService;
import com.yintong.erp.service.basis.associator.SupplierRawMaterialService;
import com.yintong.erp.service.prod.ProdOrderService;
import com.yintong.erp.service.purchase.PurchaseOrderService;
import com.yintong.erp.service.sale.SaleOrderService;
import com.yintong.erp.service.stock.StockOptService;
import com.yintong.erp.service.stock.StockPlaceService;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.base.BaseResult;

import com.yintong.erp.utils.base.JsonWrapper;
import com.yintong.erp.utils.common.SimpleCache;
import com.yintong.erp.utils.common.SimpleRemote;
import com.yintong.erp.web.stock.StockController;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.yintong.erp.mini.service.MiniAppService.UNBIND_OPENID_PREFIX;
import static com.yintong.erp.mini.service.MiniDtoWrapper.*;
import static com.yintong.erp.utils.bar.BarCodeConstants.*;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;
import static com.yintong.erp.utils.common.Constants.*;
import static com.yintong.erp.utils.common.Constants.StockOpt.*;
import static com.yintong.erp.utils.common.Constants.StockHolder.*;

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

    @Autowired StockPlaceService stockPlaceService;

    @Autowired MiniAppService miniAppService;

    @Autowired SaleOrderService saleOrderService;

    @Autowired PurchaseOrderService purchaseOrderService;

    @Autowired ProdOrderService prodOrderService;

    @Autowired ProductService productService;
    
    @Autowired SupplierProductService supplierProductService;

    @Autowired SupplierRawMaterialService supplierMaterialService;

    @Autowired MouldService mouldService;
    
    @Autowired SupplierMouldService supplierMouldService;

    @Autowired StockOptService stockOptService;

    @Autowired SimpleRemote simpleRemote;

    @Autowired SimpleCache<StockEntity> simpleCache;

    @Autowired ErpStockOptLogRepository stockOptLogRepository;

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
     * 1-扫码订单
     * @param stockOpt - 枚举：IN|OUT -> 出库|入库
     * @param stockHolder -枚举： 订单类型 -> SALE("销售订单")|REFUNDS("退货单")|PROD("制令单")|BUY("采购单")
     * @param barcode 订单的条形码
     * @return
     */
    @GetMapping("scan/order")
    public BaseResult scanOrder(String stockOpt, String stockHolder, String barcode){
        Assert.isTrue(IN.name().equals(stockOpt) || OUT.name().equals(stockOpt), "操作类型参数不正确");
        Assert.isTrue(StringUtils.hasText(barcode), "请先扫描条形码");
        Assert.isTrue(HOLDER_AND_PREFIX_MAP.keySet().contains(stockHolder), "条形码不合法！");
        String prefix = HOLDER_AND_PREFIX_MAP.get(stockHolder);
        Assert.isTrue(barcode.startsWith(prefix), "扫描错误，请扫描" + BAR_CODE_PREFIX.valueOf(prefix).description() + "的条形码");
        //根据操作类型和订单类型返回相应的订单／明细
        String functionKey = stockOpt + "_" + stockHolder;
        Function<String, BaseEntityWithBarCode> function = holderFunctionMap().get(functionKey);
        Assert.notNull(function, "未找到" + StockOpt.valueOf(stockOpt) + StockHolder.valueOf(stockHolder) + "的方法");

        BaseEntityWithBarCode order = function.apply(barcode);
        //noinspection unchecked
        return new BaseResult().add(buildOrder(order));

        //buildOrder
//        return new BaseResult().addPojo(function.apply(barcode));
    }

    /**
     * 2-扫码查找单个仓位
     * @param barcode
     * @return
     */
    @GetMapping("scan/place")
    public BaseResult scanPlace(String barcode){
        Assert.isTrue(StringUtils.hasText(barcode) && barcode.startsWith(S000.name()), "请扫描仓位条形码！");
        return stockController.one(barcode);
    }

    /**
     * 获取仓库的现有库存
     * @param placeId - 仓位id
     * @param waresCode - 货物条码
     * @param waresId - 货物模版id
     * @param strict - 是否严格（关联供应商|随意）
     * @return
     */
    @SuppressWarnings("unchecked")
    @GetMapping("place/remain")
    public BaseResult placeRemain(Long placeId, String waresCode, Long waresId, boolean strict){
        String firstChar = waresCode.substring(0,1);

        JSONObject empty = JsonWrapper.builder().add("total", -1).build();
        if(WaresType.D.name().equals(firstChar)) {
            List<ErpStockOptLog> optHistory = strict ?
                    stockOptLogRepository.findByStockPlaceIdAndMouldCode(placeId, waresCode) :
                    stockOptLogRepository.findByStockPlaceIdAndMouldId(placeId, waresId);
            JSONObject ret =
                    stockPlaceService.collect(optHistory, (log)->true, ErpStockOptLog::getMouldCode, "mouldId", "mouldCode")
                        .stream().findAny().orElse(empty);
            return new BaseResult().add(ret);
        }

        if(WaresType.P.name().equals(firstChar)) {
            List<ErpStockOptLog> optHistory = strict ?
                    stockOptLogRepository.findByStockPlaceIdAndProductCode(placeId, waresCode) :
                    stockOptLogRepository.findByStockPlaceIdAndProductId(placeId, waresId);
            JSONObject ret =
                    stockPlaceService.collect(optHistory, (log)->true, ErpStockOptLog::getProductCode, "productId", "productCode")
                            .stream().findAny().orElse(empty);
            return new BaseResult().add(ret);
        }

        return new BaseResult();
    }

    /**
     * 3-具体货物出入库(扫码货物|原材料仓位)
     * @param stockOpt - 枚举：IN|OUT -> 出库|入库
     * @param stockHolder  - 枚举： 订单类型 -> REFUNDS("退货单")|PROD("制令单")|BUY("采购单")|INIT("初始化")|SALE("销售")
     * @param placeId - 仓位id
     * @param orderId - 订单id
     * @param orderBarcode - 订单条形码 [INIT时为null]
     * @param barcode - 货物编码 - 若是原材料，改条码就是仓位的条形码
     * @param num - 数量
     * @return 仓位
     */
    @PostMapping("stock")
    public BaseResult scan2Stock(String stockOpt, String stockHolder, Long placeId, Long orderId, String orderBarcode, String barcode, Double num){
        orderId = -999 == orderId ? null : orderId;
        Assert.isTrue(IN.name().equals(stockOpt) || OUT.name().equals(stockOpt), "操作类型参数不正确");
        Assert.hasText(stockHolder, "订单类型不能为空");
        Assert.notNull(placeId, "仓位信息不能为空");
        Assert.isTrue(Objects.nonNull(num) && num > 0, "数量必须大于0");
        Assert.isTrue(Stream.of(StockHolder.values()).map(StockHolder::name).collect(Collectors.toList()).contains(stockHolder), "订单类型不正确");

        StockHolder holder = StockHolder.valueOf(stockHolder);
        StockOpt opt = StockOpt.valueOf(stockOpt);
        if(opt == StockOpt.IN && INIT != holder){
            //1- stockHolder不为INIT的时候需要orderId
            Assert.notNull(orderId, "订单不能为空，请先扫描订单条形码");
        } else if (opt == StockOpt.OUT){
            Assert.isTrue(Objects.nonNull(orderId) && StringUtils.hasText(orderBarcode), "订单不能为空");
        }
        //2-查找货物
        StockEntity stockEntity = findWaresByBarcode(barcode);

        //3- 调用StockOptService的出入库方法
        ErpStockPlace place = (opt == StockOpt.IN) ?
                stockOptService.stockIn(placeId, stockEntity, holder, orderId, orderBarcode, num) :
                stockOptService.stockOut(placeId, stockEntity, holder, orderId, orderBarcode, num);
        //4- 清理缓存中的stockEntity
        simpleCache.clearCache("MINI_" + barcode);
        return new BaseResult().addPojo(place);
    }

    /**
     * 根据条码找货物
     * @param barcode
     * @return
     */
    @GetMapping("scan/wares")
    public BaseResult findWares(String barcode){
        StockEntity entity = findWaresByBarcode(barcode);
        //noinspection unchecked
        return new BaseResult().add(buildWares(entity));
    }

    private StockEntity findWaresByBarcode(String barcode){
        Assert.isTrue(StringUtils.hasText(barcode) && (barcode.length() == WARES_BAR_CODE_TPL_LENGTH || barcode.length() == WARES_BAR_CODE_ASS_LENGTH) , "条形码有误");
        String firstCharOfBarcode = barcode.substring(0, 1);
        //原材料的情况
        if("S".equals(firstCharOfBarcode)){
            firstCharOfBarcode = "M";
        }
        Assert.isTrue(Stream.of(WaresType.values()).map(WaresType::name).collect(Collectors.toList()).contains(firstCharOfBarcode), "条形码不匹配");
        WaresType type = WaresType.valueOf(firstCharOfBarcode);
        //通过barcode判断具体货物类型 截取barcode长度，找到货物模版id
        String functionKey = type.name() + barcode.length();
        Function<String, StockEntity> function = waresFunctionMap().get(functionKey);
        Assert.notNull(function, "未找到匹配的根据条形码查找" + type.description() + "的方法");

        return simpleCache.getDataFromCache("MINI_" + barcode, value -> function.apply(barcode));

    }

    /**
     * 订单类型和订单前缀的映射
     * key-StockHolder , value-X000 Q000 V000
     */
    private static final Map<String, String> HOLDER_AND_PREFIX_MAP = new HashMap<String, String>(){{
        put(SALE.name(), X000.name()); //销售单（销售） - X000
        put(REFUNDS.name(), X000.name()); //销售单（退货） - X000
        put(PROD.name(), Q000.name()); //制令单 - Q000
        put(BUY.name(), V000.name()); //采购单 - V000
    }};

    /**
     * 操作+订单类型 和 执行方法（获取订单实例）的映射
     * @return apply -> 订单实例
     */
    private Map<String, Function<String, BaseEntityWithBarCode>> holderFunctionMap() {
        return new HashMap<String, Function<String, BaseEntityWithBarCode>>() {{
            put(IN.name() + "_" + BUY.name(), purchaseOrderService::findOrder4In);//采购单（销售） - 具体的入库信息
            put(IN.name() + "_" + REFUNDS.name(), saleOrderService::findOrder4In);//销售单（退货） - 具体的入库信息
            put(OUT.name() + "_" + SALE.name(), saleOrderService::findOrder4Out);//销售单（销售） - 具体的出库信息
            put(IN.name() + "_" + PROD.name(), prodOrderService::findOrder4In);//制令单（生产） - 具体的入库信息
            put(OUT.name() + "_" + PROD.name(), prodOrderService::findOrder4Out);//制令单（生产） - 具体的出库信息
        }};
    }

    /**
     * 货物类型+条码长度 和执行方法（获取货物模版实例）的映射
     * @return apply -> 货物模版实例 (不考虑废品)
     */
    private Map<String, Function<String, StockEntity>> waresFunctionMap() {
        return new HashMap<String, Function<String, StockEntity>>() {{
            put(WaresType.P.name() + WARES_BAR_CODE_TPL_LENGTH, productService::findByBarcode);//成品-模版条码
            put(WaresType.P.name() + WARES_BAR_CODE_ASS_LENGTH, supplierProductService::findByBarcode);//成品-关联条码
            //put(WaresType.M.name() + WARES_BAR_CODE_TPL_LENGTH, materialService::one);//原材料-模版条码
            put(WaresType.M.name() + WARES_BAR_CODE_ASS_LENGTH, supplierMaterialService::findByPlaceBarcode);//原材料-关联条码
            put(WaresType.D.name() + WARES_BAR_CODE_TPL_LENGTH, mouldService::findByBarcode);//模具-模版条码
            put(WaresType.D.name() + WARES_BAR_CODE_ASS_LENGTH, supplierMouldService::findByBarcode);//模具-关联条码
        }};
    }

}
