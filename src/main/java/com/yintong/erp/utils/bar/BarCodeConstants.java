package com.yintong.erp.utils.bar;

import org.apache.commons.collections4.KeyValue;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;

public interface BarCodeConstants {

//    public static final BAR_CODE_PREFIX [] USER_TYPES = {
//
//    }

    /**
     * 成品类别
     * @return
     */
    static List<BAR_CODE_PREFIX> productTypes(){
        return Stream.of(BAR_CODE_PREFIX.values())
                .filter(b -> b.first().getKey().equals("P"))
                .collect(Collectors.toList());
    }

    /**
     * 供应商类别
     * @return
     */
    static List<BAR_CODE_PREFIX> supplierTypes(){
        return Stream.of(BAR_CODE_PREFIX.values())
                .filter(b -> b.first().getKey().equals("U") && b.second().getKey().equals("S"))
                .collect(Collectors.toList());
    }

    /**
     * 前缀的枚举
     */
    enum BAR_CODE_PREFIX {
        UE00("人员-员工"),
        UCC0("人员-客户-公司部"),
        UCS0("人员-客户-散户"),
        USC0("人员-供应商-企业"),
        USS0("人员-供应商-个体"),
        USE0("人员-供应商-外协"),
        PTT0("成品-触点-三复合银点"),
        PTD0("成品-触点-二复合银点"),
        PTW0("成品-触点-整体银点"),
        PTU0("成品-触点-铜触点"),
        PNR0("成品-柳钉-紫铜柳钉"),
        PNY0("成品-柳钉-黄铜柳钉"),
        PNM0("成品-柳钉-铝柳钉"),
        PNF0("成品-柳钉-铁柳钉"),
        PRT0("成品-废品-三复合银点"),
        PRD0("成品-废品-二复合银点"),
        PRW0("成品-废品-整体银点"),
        PRU0("成品-废品-铜触点"),
        PRR0("成品-废品-紫铜柳钉"),
        PRY0("成品-废品-黄铜柳钉"),
        PRM0("成品-废品-铝柳钉"),
        PRF0("成品-废品-铁柳钉"),
        MA00("原材料-银丝"),
        MZR0("原材料-铜丝-紫铜丝"),
        MZY0("原材料-铜丝-黄铜丝"),
        MZB0("原材料-铜丝-铜基丝"),
        MZN0("原材料-铜丝-铜镍丝"),
        MZQ0("原材料-铜丝-白铜丝"),
        MM00("原材料-铝丝"),
        MF00("原材料-铁丝"),
        MRA0("原材料-废品-银丝"),
        MRZR("原材料-废品-铜丝-紫铜丝"),
        MRZY("原材料-废品-铜丝-黄铜丝"),
        MRZB("原材料-废品-铜丝-铜基丝"),
        MRZN("原材料-废品-铜丝-铜镍丝"),
        MRZQ("原材料-废品-铜丝-白铜丝"),
        MRZ0("原材料-废品-铜丝"),
        EJ00("设备-三复合机"),
        EL00("设备-柳州机"),
        EH00("设备-上海机"),
        EQ00("设备-半空心机"),
        S000("仓位"),
        J000("销售计划单"),
        X000("销售单"),
        R000("生产计划单"),
        Q000("制令单"),
        O000("出库单"),
        I000("入库单"),
        V000("采购单"),
        D100("模具-模具"),
        D200("模具-银刀"),
        D300("模具-铜刀"),
        D400("模具-磨子材料"),
        D500("模具-钨钢刀片"),
        D600("模具-剪切座"),
        D700("模具-偏心管"),
        D800("模具-头冲");

        BAR_CODE_PREFIX(String description) {
            this.description = description;
        }

        private final String description;

        public String description() {
            return description;
        }

        public KeyValue<String, String> first(){
            return part(0);
        }

        public KeyValue<String, String> second(){
            return part(1);
        }

        public KeyValue<String, String> third(){
            return part(2);
        }

        public KeyValue<String, String> fourth(){
            return part(3);
        }

        private KeyValue<String, String> part(int at){
            Assert.isTrue(at>=0 && at <4, "位数区间必须介于[0,4)");
            String name = name();
            String [] desc = description.split("-");

            return new KeyValue<String, String>() {
                @Override
                public String getKey() {
                    return name.substring(at, at+1);
                }

                @Override
                public String getValue() {
                    return desc.length - 1 >= at ? desc[at] : "无";
                }
            };

        }

    }

    /**
     * id的最大位数
     */
    int ID_LENGTH = 9;

    /**
     * 空位补字段
     */
    String EMPTY_REPLACE = "K";

}
