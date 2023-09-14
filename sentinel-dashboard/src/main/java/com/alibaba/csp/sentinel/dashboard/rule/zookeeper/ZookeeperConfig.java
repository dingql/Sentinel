/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.controller.FlowControllerV1;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ZookeeperConfig {

    @Value("${zookeeper.host}")
    private String zookeeperHost = "localhost:2181";

    private final Logger logger = LoggerFactory.getLogger(ZookeeperConfig.class);

    @Value("${zookeeper.username}")
    private String username = "";

    @Value("${zookeeper.password}")
    private String password = "";


    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public CuratorFramework zkClient() {
//        CuratorFramework zkClient =
//                CuratorFrameworkFactory.newClient(zookeeperHost,
//                        new ExponentialBackoffRetry(ZookeeperConfigUtil.SLEEP_TIME, ZookeeperConfigUtil.RETRY_TIMES));
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zookeeperHost)
//                .authorization("digest", (username + ":" + password).getBytes())
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(15000)
                .retryPolicy( new ExponentialBackoffRetry(ZookeeperConfigUtil.SLEEP_TIME, ZookeeperConfigUtil.RETRY_TIMES));
        if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
            builder.authorization("digest", (username + ":" + password).getBytes());
        }

        CuratorFramework zkClient = builder.build();

        zkClient.start();

        return zkClient;
    }
}