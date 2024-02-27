package com.atguigu.gmall.model.list;

import lombok.Data;

import java.io.Serializable;

/**
 * 品牌实体类对象
 */
@Data
public class SearchResponseTmVo implements Serializable {
    // 品牌ID
    private Long tmId;
    // 属性名称
    private String tmName;  //  网络制式，分类
    // 图片名称
    private String tmLogoUrl;   // 网络制式，分类
}

