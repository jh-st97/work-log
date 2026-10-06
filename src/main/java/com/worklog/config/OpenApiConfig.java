package com.worklog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

//Swagger 화면에 제목을 붙이고, 우측 상단에 "Authorize"(JWT 입력) 버튼을 만든다.
@Configuration
public class OpenApiConfig {
	
	// 아래 두 곳에서 같은 이름을 써야 연결되므로 상수로 둔다
	private static final String SCHEME_NAME = "bearerAuth";
	
	@Bean
	public OpenAPI openAPI() {
		SecurityScheme bearerScheme = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT");
		
		return new OpenAPI().info(new Info().title("work-log API").description("업무 관리 & 커리어 기록 서비스").version("v1"))
				// 모든 API에 위 인증 방식을 적용 => Authorize에 토큰을 한 번 넣으면 전부 자동으로 붙음
				.addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
				.components(new Components().addSecuritySchemes(SCHEME_NAME, bearerScheme));
	}

}
