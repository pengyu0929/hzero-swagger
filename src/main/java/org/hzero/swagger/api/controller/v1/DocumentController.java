package org.hzero.swagger.api.controller.v1;

import java.io.IOException;

import org.hzero.core.util.Results;
import org.hzero.swagger.app.DocumentService;
import org.hzero.swagger.infra.constant.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.web.bind.annotation.*;

import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;

/**
 * 获取swagger信息controller
 */
@RestController
@RequestMapping(value = "/docs")
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    /**
     * 获取服务id对应的版本的swagger json
     *
     * @param name    服务id，形如 uaa
     * @param version 服务版本
     * @return String
     */
    @Permission(permissionPublic = true)
    @ApiOperation("获取服务id对应的版本swagger json字符串")
    @GetMapping(value = "/{servicePrefix}")
    public ResponseEntity<String> get(@PathVariable("servicePrefix") String name,
                                      @RequestParam(value = "version", required = false,
                                              defaultValue = Versions.NULL_VERSION) String version) {
        String swaggerJson;
        try {
            swaggerJson = documentService.getSwaggerJson(name, version);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            String log = "服务" + name + " version " + version + "没有在运行";
            return new ResponseEntity<>(log, HttpStatus.NOT_FOUND);
        }
        if ("".equals(swaggerJson)) {
            return new ResponseEntity<>(swaggerJson, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(swaggerJson, HttpStatus.OK);
        }
    }

    /**
     * 手动刷新表中swagger和刷新权限
     *
     * @param serviceName 服务名
     * @param version     服务版本
     * @return null
     */
    @ApiOperation("手动刷新表中swagger")
    @PostMapping(value = "/swagger/refresh/{serviceName}")
    public ResponseEntity refresh(@PathVariable("serviceName") String serviceName,
                                  @RequestParam(value = "version", required = false, defaultValue = Versions.NULL_VERSION) String version) {
        try {
            documentService.manualRefresh(serviceName, version);
            return Results.success();
        } catch (RemoteAccessException e) {
            LOGGER.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
