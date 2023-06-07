package app.askresume.global.config

import app.askresume.global.resolver.memberinfo.MemberInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType
import org.springframework.boot.actuate.endpoint.ExposableEndpoint
import org.springframework.boot.actuate.endpoint.web.*
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.util.StringUtils
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.RequestParameterBuilder
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import java.util.*
import kotlin.collections.ArrayList


@Configuration
class SwaggerConfig {

    @Value("\${swagger.title}")
    private lateinit var TITLE: String

    @Value("\${swagger.description}")
    private lateinit var DESCRIPTION: String

    @Value("\${swagger.version}")
    private lateinit var VERSION: String

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.OAS_30)
            .select() // ApiSelectorBuilder 생성
            .apis(RequestHandlerSelectors.basePackage("app.askresume.api")) // API 패키지 경로
            .paths(PathSelectors.ant("/api/**")) // path 조건에 따라서 API 문서화
            .build()
            .apiInfo(apiInfo()) // API 문서에 대한 정보 추가
            .useDefaultResponseMessages(false) // swagger에서 제공하는 기본 응답 코드 설명 제거
            .securityContexts(listOf(securityContext()))
            .securitySchemes(listOf<SecurityScheme>(apiKey()))
            .globalRequestParameters(globalParameters())
        .ignoredParameterTypes(MemberInfo::class.java)
    }

    private fun apiInfo(): ApiInfo? {
        return ApiInfoBuilder()
            .title(TITLE)
            .description(DESCRIPTION)
            .version(VERSION)
            .build()
    }

    private fun globalParameters(): List<RequestParameter> {
        return listOf(
            RequestParameterBuilder()
                .name(HttpHeaders.ACCEPT_LANGUAGE)
                .description("한국어 ko-KR, 영어 en-US")
                .`in`(ParameterType.HEADER)
                .required(true)
                .build()
        )
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build()
    }

    private fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return listOf(SecurityReference(HttpHeaders.AUTHORIZATION, authorizationScopes))
    }

    private fun apiKey(): ApiKey {
        return ApiKey(HttpHeaders.AUTHORIZATION, HttpHeaders.AUTHORIZATION, "header")
    }

    // Swagger & Actuator 적용 환경에서 필요한 설정 L.93~131
    @Bean
    fun webEndpointServletHandlerMapping(
        webEndpointsSupplier: WebEndpointsSupplier,
        servletEndpointsSupplier: ServletEndpointsSupplier,
        controllerEndpointsSupplier: ControllerEndpointsSupplier,
        endpointMediaTypes: EndpointMediaTypes?,
        corsProperties: CorsEndpointProperties,
        webEndpointProperties: WebEndpointProperties,
        environment: Environment
    ): WebMvcEndpointHandlerMapping? {
        val allEndpoints: MutableList<ExposableEndpoint<*>?> = ArrayList()
        val webEndpoints: Collection<ExposableWebEndpoint?> = webEndpointsSupplier.getEndpoints()
        allEndpoints.addAll(webEndpoints)
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints())
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints())
        val basePath: String = webEndpointProperties.getBasePath()
        val endpointMapping = EndpointMapping(basePath)
        val shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath)
        return WebMvcEndpointHandlerMapping(
            endpointMapping,
            webEndpoints,
            endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping,
            null
        )
    }

    private fun shouldRegisterLinksMapping(
        webEndpointProperties: WebEndpointProperties,
        environment: Environment,
        basePath: String
    ): Boolean {
        return webEndpointProperties.getDiscovery()
            .isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment)
            .equals(ManagementPortType.DIFFERENT))
    }

}