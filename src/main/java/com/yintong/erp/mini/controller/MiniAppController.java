package com.yintong.erp.mini.controller;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.basis.security.ErpEmployeeRepository;
import com.yintong.erp.domain.prod.ErpProdGarbageHistory;
import com.yintong.erp.domain.prod.ErpProdGarbageHistoryRepository;
import com.yintong.erp.domain.prod.ErpProdHalfFlowRecord;
import com.yintong.erp.domain.prod.ErpProdHalfFlowRecordRepository;
import com.yintong.erp.domain.prod.ErpProdOrder;
import com.yintong.erp.domain.prod.ErpProdOrderRepository;
import com.yintong.erp.domain.stock.ErpStockOptLog;
import com.yintong.erp.domain.stock.ErpStockOptLogRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
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
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.common.SessionUtil;
import com.yintong.erp.utils.common.SimpleCache;
import com.yintong.erp.utils.common.SimpleRemote;
import com.yintong.erp.web.stock.StockController;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.yintong.erp.mini.service.MiniAppService.UNBIND_OPENID_PREFIX;
import static com.yintong.erp.mini.service.MiniDtoWrapper.buildOrder;
import static com.yintong.erp.mini.service.MiniDtoWrapper.buildWares;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.Q000;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.S000;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.V000;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.X000;
import static com.yintong.erp.utils.bar.BarCodeConstants.WARES_BAR_CODE_ASS_LENGTH;
import static com.yintong.erp.utils.bar.BarCodeConstants.WARES_BAR_CODE_TPL_LENGTH;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage.PROD_STAGE_1;
import static com.yintong.erp.utils.common.Constants.StockHolder;
import static com.yintong.erp.utils.common.Constants.StockHolder.BUY;
import static com.yintong.erp.utils.common.Constants.StockHolder.INIT;
import static com.yintong.erp.utils.common.Constants.StockHolder.PROD;
import static com.yintong.erp.utils.common.Constants.StockHolder.REFUNDS;
import static com.yintong.erp.utils.common.Constants.StockHolder.SALE;
import static com.yintong.erp.utils.common.Constants.StockOpt;
import static com.yintong.erp.utils.common.Constants.StockOpt.IN;
import static com.yintong.erp.utils.common.Constants.StockOpt.OUT;
import static com.yintong.erp.utils.common.Constants.StockPlaceType;
import static com.yintong.erp.utils.common.Constants.WaresType;

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

    @Autowired ErpStockPlaceRepository stockPlaceRepository;

    @Autowired ErpEmployeeRepository employeeRepository;

    @Autowired ErpProdOrderRepository prodOrderRepository;

    @Autowired ErpProdGarbageHistoryRepository garbageHistoryRepository;

    @Autowired ErpProdHalfFlowRecordRepository flowRecordRepository;

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
     * 获取小程序权限
     * @return
     */
    @GetMapping("roles")
    public BaseResult roles(){
        Map<String, String> roles = miniAppService.miniRoles(SessionUtil.getCurrentUserId());
        return new BaseResult().add(roles);
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
        return new BaseResult().add(buildOrder(order, StockOpt.valueOf(stockOpt)));
    }

    /**
     * 扫描员工二维码，获取制令单供出库
     * @param barcode
     * @return
     */
    @GetMapping("scan/employee")
    public BaseResult scanEmployee(String barcode){
        Assert.hasText(barcode, "请先扫描条形码");
        ErpEmployee employee = employeeRepository.findByBarCode(barcode).orElseThrow(() -> new IllegalArgumentException("未找到编号为[".concat(barcode).concat("的员工")));
        List<ErpProdOrder> orders = prodOrderRepository.findByEmployeeIdAndFinishDateIsNotNull(employee.getId());
        Assert.notEmpty(orders, "未找到员工".concat(employee.getName()).concat("未完成的制令单"));
        List<JSONObject> list = orders.stream().map(it -> MiniDtoWrapper.buildOrder(it, null)).collect(Collectors.toList());
        return new BaseResult().addList(list);
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
     * 根据条码找货物
     * @param barcode
     * @return
     */
    @GetMapping("scan/wares")
    public BaseResult findWares(String barcode){
        StockEntity entity = findWaresByBarcode(barcode, true);
        //noinspection unchecked
        return new BaseResult().add(buildWares(entity));
    }

    /**
     * 获取单个仓位的库存明细
     * @param barcode - 仓位的条码
     * @return {
     *     //place : {type : 'M|P|D|R', barCode, name, current, limit, statusCode, statusName, description}
     *     place : {}
     *     contents : {name, barCode, category, specification, total, supplierName}
     * }
     */
    @GetMapping("scan/place/detail")
    public BaseResult scanPlace4Detail(String barcode){
        ErpStockPlace stockPlace = stockPlaceService.one(barcode);
        JSONObject place = new BaseResult().addPojo(stockPlace).getRet();
        StockPlaceType placeType = StockPlaceType.valueOf(stockPlace.getStockPlaceType());

        if(StockPlaceType.M == placeType){
            StockEntity material = supplierMaterialService.findByPlaceBarcode(barcode);
            return new BaseResult().put("place", place).addList("contents", Collections.singletonList(buildWares(material)));
        }

        List<ErpStockOptLog> optHistory = stockOptLogRepository.findByStockPlaceId(stockPlace.getId());
        if (StockPlaceType.D == placeType){
            List<JSONObject> contents = stockPlaceService.collect(optHistory, log -> StringUtils.hasText(log.getMouldCode()), ErpStockOptLog::getMouldCode, "mouldCode")
                    .stream()
                    .peek(json -> json.putAll(buildWares(findWaresByBarcode(json.getString("mouldCode"), false))))
                    .collect(Collectors.toList());
            return new BaseResult().put("place", place).addList("contents", contents);
        } else if(StockPlaceType.P == placeType) {
            List<JSONObject> contents = stockPlaceService.collect(optHistory, log -> StringUtils.hasText(log.getProductCode()), ErpStockOptLog::getProductCode, "productCode")
                    .stream()
                    .peek(json -> json.putAll(buildWares(findWaresByBarcode(json.getString("productCode"), false))))
                    .collect(Collectors.toList());
            return new BaseResult().put("place", place).addList("contents", contents);
        } else if(StockPlaceType.R == placeType) {
            return new BaseResult().put("place", place)
                    .addList("contents", stockPlaceService.collect(optHistory, log -> StringUtils.hasText(log.getRubbishName()), ErpStockOptLog::getRubbishName, "rubbishName"));
        }

        throw new IllegalArgumentException("条码类型不正确[" + barcode + "]");
    }

    /**
     * 获取单个货物的库存明细
     * @param barcode - 货物的条码，原材料时为仓位
     * @return
     */
    @GetMapping("scan/wares/detail")
    public BaseResult scanWares4Detail(String barcode){
        StockEntity stockEntity = findWaresByBarcode(barcode, false);
        JSONObject wares = buildWares(stockEntity);
        WaresType waresType = stockEntity.waresType();
        if(waresType == WaresType.M){
            List<JSONObject> places = stockPlaceRepository.findByMaterialSupplierBarCode(stockEntity.entity().getBarCode())
                    .stream()
                    .sorted(Comparator.comparing(ErpStockPlace::getCurrentStorageNum).reversed())
                    .map(place -> {
                        JSONObject json = place.toJSONObject(false);
                        json.put("total", place.getCurrentStorageNum());
                        return json;
                    })
                    .collect(Collectors.toList());
            return new BaseResult().put("wares", wares).addList("places", places);
        }

        List<ErpStockOptLog> optHistory = new ArrayList<>();
        if(waresType == WaresType.P){
            optHistory = stockOptLogRepository.findByProductCodeLike(barcode + "%");
        } else if(waresType == WaresType.D){
            optHistory = stockOptLogRepository.findByMouldCodeLike(barcode + "%");
        }

        List<JSONObject> places = stockPlaceService.collect(optHistory, log->true, ErpStockOptLog::getStockPlaceId, "stockPlaceId")
                .stream()
                .peek(json -> {
                    ErpStockPlace stockPlace = stockPlaceService.one(json.getLong("stockPlaceId"));
                    json.putAll(stockPlace.toJSONObject(false));
                })
                .collect(Collectors.toList());
        return new BaseResult().put("wares", wares).addList("places", places);
    }

    /**
     * 获取仓库的某一货物现有库存
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
        StockEntity stockEntity = findWaresByBarcode(barcode, true);

        //3- 调用StockOptService的出入库方法
        ErpStockPlace place = (opt == StockOpt.IN) ?
                stockOptService.stockIn(placeId, stockEntity, holder, orderId, orderBarcode, num) :
                stockOptService.stockOut(placeId, stockEntity, holder, orderId, orderBarcode, num);
        //4- 清理缓存中的stockEntity
        simpleCache.clearCache("MINI_" + barcode);
        return new BaseResult().addPojo(place);
    }

    /**
     * 扫描制令单，准备废料入库
     * @param barcode
     * @return
     */
    @GetMapping("scan/garbage")
    public BaseResult scan2Garbage(String barcode){
        ErpProdOrder order = prodOrderService.findOrder4In(barcode);
        List<ErpProdGarbageHistory> maList = garbageHistoryRepository.findByProdOrderIdAndTypeCode(order.getId(), "MA");
        List<ErpProdGarbageHistory> mzList = garbageHistoryRepository.findByProdOrderIdAndTypeCode(order.getId(), "MZ");
        //废银数量
        double maNum = maList.stream().mapToDouble(ErpProdGarbageHistory::getNum).sum();
        double mzNum =  mzList.stream().mapToDouble(ErpProdGarbageHistory::getNum).sum();
        return new BaseResult()
                .put("maNum", maNum)
                .put("mzNum", mzNum)
                .put("barCode", barcode)
                .put("time", DateUtil.getDateString(order.getCreatedAt()))
                .put("prodOrderId", order.getId())
                ;
    }

    /**
     * 废料入库
     * @param prodOrderId
     * @param maNum
     * @param mzNum
     * @return
     */
    @PostMapping("recycle/in/garbage")
    public BaseResult recycleInGarbage(Long prodOrderId, double maNum, double mzNum){
        prodOrderService.findOneOrder(prodOrderId);
        Assert.isTrue(maNum >= 0, "废银数量不能小于0");
        Assert.isTrue(mzNum >= 0, "废铜数量不能小于0");
        if(maNum == 0 && mzNum == 0){
            throw new IllegalArgumentException("废银数量和废铜数量不能同时为0");
        }

        if(maNum > 0){
            garbageHistoryRepository.save(
                    ErpProdGarbageHistory.builder()
                            .typeCode("MA")
                            .typeName("废银")
                            .prodOrderId(prodOrderId).num(maNum)
                            .build()
            );
        }

        if(mzNum > 0){
            garbageHistoryRepository.save(
                    ErpProdGarbageHistory.builder()
                            .typeCode("MZ")
                            .typeName("废铜")
                            .prodOrderId(prodOrderId).num(mzNum)
                            .build()
            );
        }

        return new BaseResult().setErrmsg("操作成功");
    }

    /**
     * 扫码制令单
     * @param barcode
     * @return
     */
    @GetMapping("scan/prod")
    public BaseResult scan4CreateFlow(String barcode){
        ErpProdOrder order = prodOrderService.findOrder4In(barcode);
        return new BaseResult().addPojo(order, "yyyy-MM-dd");
    }

    /**
     * 新增工序卡
     * @param prodOrderId
     * @param kg
     * @return
     */
    @PostMapping("prod/flow")
    public BaseResult createFlow(Long prodOrderId, Double kg){
        ErpProdOrder order = prodOrderService.findOneOrder(prodOrderId);
        ErpBaseEndProduct product = order.getProduct();
        Assert.notNull(product, "未找到制令单的产品");
        Assert.notNull(kg, "输入的重量不能为空");
        Assert.isTrue(kg > 0, "输入的重量必须大于0");
        int num = CommonUtil.kg2Num(product, kg);
        ErpProdHalfFlowRecord record = flowRecordRepository.save(
                ErpProdHalfFlowRecord.builder()
                        .prodOrderId(prodOrderId)
                        .stage(1).stage1Kg(kg).stage1Num(num).stage1Time(new Date())
                    .build()
        );

        prodOrderService.afterSaveFlow(record, PROD_STAGE_1);

        return new BaseResult().addPojo(prodOrderService.findOneOrder(prodOrderId), "yyyy-MM-dd");
    }


    /**
     * 扫描工序卡
     * @param barcode
     * @return
     */
    @GetMapping("scan/flow")
    public BaseResult scan2OptFlow(String barcode){
        ErpProdHalfFlowRecord record = flowRecordRepository.findByBarCode(barcode).orElseThrow(()->new IllegalArgumentException("未找到工序卡[".concat(barcode).concat("]")));
        return new BaseResult().addPojo(record);
    }

    /**
     * 输入流转单的重量,保存
     * @param next 接下来的状态
     * @param barcode 流转单的条码
     * @param kg
     * @return
     */
    @PostMapping("prod/flow/at")
    public BaseResult flow(int next, String barcode, Double kg){
        ErpEmployee employee = SessionUtil.getCurrentUser();
        ErpProdHalfFlowRecord record = flowRecordRepository.findByBarCode(barcode).orElseThrow(()->new IllegalArgumentException("未找到工序卡[".concat(barcode).concat("]")));
        ErpProdOrder order = prodOrderService.findOneOrder(record.getProdOrderId());
        ErpBaseEndProduct product = order.getProduct();
        Assert.notNull(product, "未找到制令单的产品");
        Assert.notNull(kg, "输入的重量不能为空");
        Assert.isTrue(kg > 0, "输入的重量必须大于0");
        ProdFlowStage currentStage = ProdFlowStage.val(record.getStage());
        ProdFlowStage nextStage = ProdFlowStage.val(next);
        Assert.isTrue((currentStage.stage + 1) == nextStage.stage, "当前工序为".concat(currentStage.description).concat("，不能进行").concat(nextStage.description).concat("操作"));
        int num = CommonUtil.kg2Num(product, kg);
        if(next == 2){
            record.setStage2Kg(kg);
            record.setStage2Num(num);
            record.setStage2Time(new Date());
            record.setStage2UserId(employee.getId());
            record.setStage2UserName(employee.getName());
        } else if (next == 3){
            record.setStage3Kg(kg);
            record.setStage3Num(num);
            record.setStage3Time(new Date());
            record.setStage3UserId(employee.getId());
            record.setStage3UserName(employee.getName());
        } else if (next ==4){
            record.setStage4Kg(kg);
            record.setStage4Num(num);
            record.setStage4Time(new Date());
            record.setStage4UserId(employee.getId());
            record.setStage4UserName(employee.getName());
        }
        record.setStage(next);

        record = flowRecordRepository.save(record);
        prodOrderService.afterSaveFlow(record, ProdFlowStage.val(next));

        return new BaseResult().addPojo(record);
    }


    private StockEntity findWaresByBarcode(String barcode, boolean cacheAble){
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

        return cacheAble ? simpleCache.getDataFromCache("MINI_" + barcode, value -> function.apply(barcode))
                : function.apply(barcode);
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
            put(IN.name() + "_" + BUY.name(), purchaseOrderService::findOrder4In);//采购单（采购） - 具体的入库信息
            put(OUT.name() + "_" + BUY.name(), purchaseOrderService::findOrder4Out);//采购单（退货） - 具体的出库信息
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
            put(WaresType.M.name() + WARES_BAR_CODE_ASS_LENGTH, supplierMaterialService::findByPlaceBarcode);//仓位条码
            put(WaresType.D.name() + WARES_BAR_CODE_TPL_LENGTH, mouldService::findByBarcode);//模具-模版条码
            put(WaresType.D.name() + WARES_BAR_CODE_ASS_LENGTH, supplierMouldService::findByBarcode);//模具-关联条码
        }};
    }

}
