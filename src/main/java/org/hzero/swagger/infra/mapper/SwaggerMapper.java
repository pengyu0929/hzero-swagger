package org.hzero.swagger.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hzero.swagger.domain.entity.Swagger;

import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author zhipeng.zuo
 * @date 2018/1/24
 */
public interface SwaggerMapper extends BaseMapper<Swagger> {

}
