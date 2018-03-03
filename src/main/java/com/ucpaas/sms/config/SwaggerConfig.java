package com.ucpaas.sms.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Created by lpjLiu on 2017/7/22.
 */
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket apiAll() {
        return new Docket(DocumentationType.SWAGGER_2)
                .produces(Sets.newHashSet("application/json"))
                .consumes(Sets.newHashSet("application/json"))
                .protocols(Sets.newHashSet("http"/*, "https"*/))
                .forCodeGeneration(true)
                .select().paths(petstorePaths())
                .build()
                .apiInfo(apiInfo());
    }

    private Predicate<String> petstorePaths() {
        return or(
                regex("/accountInfo/clientgroup.*"),
                regex("/accountInfo/directclient.*"),
                regex("/accountInfo/client/edit/chargerule"),
                regex("/office.*"),
                regex("/managerCenter.*"),
                regex("/accountInfo/agentBalanceAlarm.*"),
                regex("/accountInfo/agent/discount.*"),
                regex("/finance/order/list"),
                regex("/finance/oem/agentOrder/list"),
                regex("/finance/oem/clientOrder/list"),
                regex("/finance/oem/agentInventory/list"),
                regex("/finance/oem/clientInventory/list"),
                regex("/finance/returnqty.*"),
                regex("/accountInfo/client/add"),
                regex("/agentInfo/add"),
                regex("/notice/noticeList"),
                regex("/notice/searchNotice"),
                regex("/notice/noticeDetails"),
                regex("/notice/addNotice"),
                regex("/notice/list"),
                regex("/notice/deleteNotice"),
                regex("/notice/updateStatus"),
                regex("/notice/edit"),
                regex("/index/menuright.*"),
                regex("/notice/edit"),
                regex("/comTemplate/comTemplateQuery"),
                regex("/operation/statistics/complaint/queryListForAccount"),
                regex("/operation/statistics/complaint/queryListForUser"),
                regex("/operation/statistics/complaint/queryListForChannel"),
                regex("/operation/statistics/complaint/searchComplaint"),
                regex("/operation/statistics/complaint/deleteById"),
                regex("/operation/statistics/complaint/addComplaintBatch"),
                regex("/operation/statistics/complaint/complaintListExport"),
                regex("/operation/statistics/channel/searchChannelOperationStatistics"),
                regex("/operation/statistics/channel/channelOperationStatisticsExport"),
                regex("/finance/invoice.*")
        );
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "运营平台 REST API文档",
                "运营平台 REST风格的可视化文档",
                "1.0.0",
                "http://operation.sms.ucpaas.com/v2/api-docs",
                "liulipengju@ucpaas.com",
                "Apache License",
                "http://www.apache.org/licenses/LICENSE-2.0.html"
        );
        return apiInfo;
    }
}
