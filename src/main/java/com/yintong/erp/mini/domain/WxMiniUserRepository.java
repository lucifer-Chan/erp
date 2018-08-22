package com.yintong.erp.mini.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WxMiniUserRepository extends JpaRepository<WxMiniUser, Long>{

    WxMiniUser findByOpenid(String openId);

}
