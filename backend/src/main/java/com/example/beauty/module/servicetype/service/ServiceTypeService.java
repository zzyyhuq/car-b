package com.example.beauty.module.servicetype.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.servicetype.dto.ServiceTypeQuery;
import com.example.beauty.module.servicetype.dto.ServiceTypeSaveDTO;
import com.example.beauty.module.servicetype.entity.ServiceType;

import java.util.List;

public interface ServiceTypeService {

    /** 管理端分页，可按名称/分类/状态筛选 */
    PageResult<ServiceType> page(ServiceTypeQuery query);

    /** 车主端可用项目，只返回上架的 */
    List<ServiceType> listOnSale();

    ServiceType getById(Long id);

    void create(ServiceTypeSaveDTO dto);

    void update(Long id, ServiceTypeSaveDTO dto);

    void updateStatus(Long id, Integer status);

    /** 逻辑删除 */
    void delete(Long id);
}
