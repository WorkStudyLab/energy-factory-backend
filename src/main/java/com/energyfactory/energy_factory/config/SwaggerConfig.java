package com.energyfactory.energy_factory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Energy Factory API")
                        .description("백엔드 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Energy Factory Team")
                                .email("contact@energyfactory.com")))
                .paths(customPaths())
                .components(new Components()
                        .addSchemas("LoginRequest", createLoginRequestSchema())
                        .addSchemas("LoginResponse", createLoginResponseSchema())
                        .addSchemas("ApiResponse", createApiResponseSchema()));
    }
    
    private Paths customPaths() {
        Paths paths = new Paths();
        
        // Login 엔드포인트 수동 추가
        PathItem loginPath = new PathItem()
                .post(new Operation()
                        .tags(java.util.List.of("인증"))
                        .summary("로그인")
                        .description("이메일과 비밀번호로 로그인합니다. JWT 토큰을 발급받습니다.")
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/LoginRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("로그인 성공")
                                        .content(new Content()
                                                .addMediaType("application/json",
                                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/LoginResponse")))))
                                .addApiResponse("401", new ApiResponse()
                                        .description("로그인 실패 - 잘못된 이메일 또는 비밀번호")
                                        .content(new Content()
                                                .addMediaType("application/json",
                                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResponse")))))));
        
        paths.addPathItem("/api/auth/login", loginPath);
        return paths;
    }
    
    private Schema<?> createLoginRequestSchema() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setRequired(java.util.List.of("email", "password"));
        
        Map<String, Schema> properties = new HashMap<>();
        
        Schema<?> emailSchema = new Schema<>();
        emailSchema.setType("string");
        emailSchema.setDescription("사용자 이메일");
        emailSchema.setExample("user@example.com");
        properties.put("email", emailSchema);
        
        Schema<?> passwordSchema = new Schema<>();
        passwordSchema.setType("string");
        passwordSchema.setDescription("비밀번호");
        passwordSchema.setExample("password123");
        properties.put("password", passwordSchema);
        
        schema.setProperties(properties);
        return schema;
    }
    
    private Schema<?> createLoginResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        
        Map<String, Schema> properties = new HashMap<>();
        
        properties.put("status", new Schema<>().type("integer").example(200));
        properties.put("code", new Schema<>().type("string").example("20000000"));
        properties.put("desc", new Schema<>().type("string").example("로그인에 성공했습니다."));
        
        Schema<?> dataSchema = new Schema<>();
        dataSchema.setType("object");
        Map<String, Schema> dataProperties = new HashMap<>();
        dataProperties.put("accessToken", new Schema<>().type("string").example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."));
        dataProperties.put("refreshToken", new Schema<>().type("string").example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."));
        dataProperties.put("tokenType", new Schema<>().type("string").example("Bearer"));
        dataSchema.setProperties(dataProperties);
        
        properties.put("data", dataSchema);
        
        schema.setProperties(properties);
        return schema;
    }
    
    private Schema<?> createApiResponseSchema() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        
        Map<String, Schema> properties = new HashMap<>();
        properties.put("status", new Schema<>().type("integer"));
        properties.put("code", new Schema<>().type("string"));
        properties.put("desc", new Schema<>().type("string"));
        properties.put("data", new Schema<>().type("object").nullable(true));
        
        schema.setProperties(properties);
        return schema;
    }
}