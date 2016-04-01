package it.infn.mw.iam.config;

import org.mitre.jwt.signer.service.impl.ClientKeyCacheService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricKeyJWTValidatorCacheService;
import org.mitre.oauth2.service.IntrospectionResultAssembler;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.oauth2.service.impl.DefaultClientUserDetailsService;
import org.mitre.oauth2.service.impl.DefaultIntrospectionResultAssembler;
import org.mitre.oauth2.service.impl.DefaultSystemScopeService;
import org.mitre.oauth2.web.CorsFilter;
import org.mitre.openid.connect.filter.AuthorizationRequestFilter;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mitre.openid.connect.service.ClientLogoLoadingService;
import org.mitre.openid.connect.service.LoginHintExtracter;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.mitre.openid.connect.service.PairwiseIdentiferService;
import org.mitre.openid.connect.service.ScopeClaimTranslationService;
import org.mitre.openid.connect.service.StatsService;
import org.mitre.openid.connect.service.UserInfoService;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.mitre.openid.connect.service.impl.DefaultApprovedSiteService;
import org.mitre.openid.connect.service.impl.DefaultBlacklistedSiteService;
import org.mitre.openid.connect.service.impl.DefaultOIDCTokenService;
import org.mitre.openid.connect.service.impl.DefaultScopeClaimTranslationService;
import org.mitre.openid.connect.service.impl.DefaultStatsService;
import org.mitre.openid.connect.service.impl.DefaultUserInfoService;
import org.mitre.openid.connect.service.impl.DefaultWhitelistedSiteService;
import org.mitre.openid.connect.service.impl.DummyResourceSetService;
import org.mitre.openid.connect.service.impl.InMemoryClientLogoLoadingService;
import org.mitre.openid.connect.service.impl.MITREidDataService_1_0;
import org.mitre.openid.connect.service.impl.MITREidDataService_1_1;
import org.mitre.openid.connect.service.impl.MITREidDataService_1_2;
import org.mitre.openid.connect.service.impl.MatchLoginHintsAgainstUsers;
import org.mitre.openid.connect.service.impl.UUIDPairwiseIdentiferService;
import org.mitre.openid.connect.token.ConnectTokenEnhancer;
import org.mitre.openid.connect.web.AuthenticationTimeStamper;
import org.mitre.openid.connect.web.ServerConfigInterceptor;
import org.mitre.openid.connect.web.UserInfoInterceptor;
import org.mitre.uma.service.ResourceSetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

@Configuration
public class MitreServicesConfig {

  @Bean(name = "mitreUserInfoInterceptor")
  public AsyncHandlerInterceptor userInfoInterceptor() {

    return new UserInfoInterceptor();
  }

  @Bean(name = "mitreServerConfigInterceptor")
  public AsyncHandlerInterceptor serverConfigInterceptor() {

    return new ServerConfigInterceptor();
  }

  @Bean(name = "mitreAuthzRequestFilter")
  public GenericFilterBean authorizationRequestFilter() {

    return new AuthorizationRequestFilter();
  }

  @Bean
  public AuthenticationTimeStamper timestamper() {

    return new AuthenticationTimeStamper();
  }

  @Bean
  public Http403ForbiddenEntryPoint http403ForbiddenEntryPoint() {

    return new Http403ForbiddenEntryPoint();
  }

  @Bean
  public CorsFilter corsFilter() {

    return new CorsFilter();
  }

  @Bean
  public OAuth2AuthenticationEntryPoint oauth2AuthenticationEntryPoint() {

    OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
    entryPoint.setRealmName("openidconnect");
    return entryPoint;
  }

  @Bean
  public TokenEnhancer defaultTokenEnhancer() {

    return new ConnectTokenEnhancer();
  }

  @Bean(name = "clientUserDetailsService")
  public UserDetailsService defaultClientUserDetailsService() {

    return new DefaultClientUserDetailsService();
  }

  @Bean
  MITREidDataService_1_0 mitreDataService1_0() {

    return new MITREidDataService_1_0();
  }

  @Bean
  MITREidDataService_1_1 mitreDataService1_1() {

    return new MITREidDataService_1_1();
  }

  @Bean
  MITREidDataService_1_2 mitreDataService1_2() {

    return new MITREidDataService_1_2();
  }

  @Bean
  LoginHintExtracter defaultLoginHintExtracter() {

    return new MatchLoginHintsAgainstUsers();
  }

  @Bean
  ClientLogoLoadingService defaultClientLogoLoadingService() {

    return new InMemoryClientLogoLoadingService();
  }

  @Bean
  ScopeClaimTranslationService defaultScopeClaimTranslationService() {

    return new DefaultScopeClaimTranslationService();
  }

  @Bean
  IntrospectionResultAssembler defaultIntrospectionResultAssembler() {

    return new DefaultIntrospectionResultAssembler();
  }

  @Bean
  SymmetricKeyJWTValidatorCacheService defaultSimmetricKeyJWTValidatorCacheService() {

    return new SymmetricKeyJWTValidatorCacheService();
  }

  @Bean
  JWKSetCacheService defaultCacheService() {

    return new JWKSetCacheService();
  }

  @Bean
  OIDCTokenService defaultOIDCTokenService() {

    return new DefaultOIDCTokenService();
  }

  @Bean
  PairwiseIdentiferService defaultPairwiseIdentifierService() {

    return new UUIDPairwiseIdentiferService();
  }

  @Bean
  UserInfoService defaultUserInfoService() {

    return new DefaultUserInfoService();
  }

  @Bean
  ApprovedSiteService defaultApprovedSiteService() {

    return new DefaultApprovedSiteService();
  }

  @Bean
  StatsService defaultStatsService() {

    return new DefaultStatsService();
  }

  @Bean
  WhitelistedSiteService defaultWhitelistedSiteService() {

    return new DefaultWhitelistedSiteService();
  }

  @Bean
  BlacklistedSiteService defaultBlacklistedSiteService() {

    return new DefaultBlacklistedSiteService();
  }

  @Bean
  SystemScopeService defaultSystemScopeService() {

    return new DefaultSystemScopeService();
  }

  @Bean
  ResourceSetService defaultResourceSetService() {

    return new DummyResourceSetService();
  }

  @Bean
  ClientKeyCacheService defaultClientKeyCacheService() {

    return new ClientKeyCacheService();
  }
}
