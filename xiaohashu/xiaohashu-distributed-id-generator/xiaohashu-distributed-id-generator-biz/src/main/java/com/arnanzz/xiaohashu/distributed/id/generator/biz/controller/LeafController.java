package com.arnanzz.xiaohashu.distributed.id.generator.biz.controller;

import com.arnanzz.xiaohashu.distributed.id.generator.biz.core.common.Result;
import com.arnanzz.xiaohashu.distributed.id.generator.biz.core.common.Status;
import com.arnanzz.xiaohashu.distributed.id.generator.biz.exception.LeafServerException;
import com.arnanzz.xiaohashu.distributed.id.generator.biz.exception.NoKeyException;
import com.arnanzz.xiaohashu.distributed.id.generator.biz.service.SegmentService;
import com.arnanzz.xiaohashu.distributed.id.generator.biz.service.SnowflakeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class LeafController {
    private Logger logger = LoggerFactory.getLogger(LeafController.class);

    @Autowired
    private SegmentService segmentService;
    @Autowired
    private SnowflakeService snowflakeService;

    /**
     * 获取号段id
     */
    @RequestMapping(value = "/segment/get/{key}")
    public String getSegmentId(@PathVariable("key") String key) {
        return get(key, segmentService.getId(key));
    }

    /**
     * 雪花算法 id
     */
    @RequestMapping(value = "/snowflake/get/{key}")
    public String getSnowflakeId(@PathVariable("key") String key) {
        return get(key, snowflakeService.getId(key));
    }

    private String get(@PathVariable("key") String key, Result id) {
        Result result;
        if (key == null || key.isEmpty()) {
            throw new NoKeyException();
        }
        result = id;
        if (result.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(result.toString());
        }
        return String.valueOf(result.getId());
    }
}
