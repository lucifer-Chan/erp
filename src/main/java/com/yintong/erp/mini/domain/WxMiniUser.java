package com.yintong.erp.mini.domain;

import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.CommonUtil;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lucifer.chan
 * @create 2018-08-16 上午10:34
 * 微信小程序用户
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WxMiniUser extends BaseEntity{

    @Id
    private String openid;
    @Column(columnDefinition = "varchar(100) comment '昵称'")
    private String nickName;
    @Column(columnDefinition = "varchar(10) comment 'gender'")
    private String gender;
    @Column(columnDefinition = "varchar(10) comment 'language'")
    private String language;
    @Column(columnDefinition = "varchar(10) comment 'city'")
    private String city;
    @Column(columnDefinition = "varchar(10) comment 'province'")
    private String province;
    @Column(columnDefinition = "varchar(10) comment 'country'")
    private String country;
    @Column(columnDefinition = "varchar(400) comment '头像链接'")
    private String avatarUrl;

    @Transient
    private String code;

    public String getNickName(){
        return CommonUtil.unconvert4ByteChar(nickName);
    }

    public void setNickName(String nickName){
        this.nickName = CommonUtil.convert4ByteChar(nickName);
    }
}
