package it.infn.mw.iam.test.oauth;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import it.infn.mw.iam.IamLoginService;
import it.infn.mw.iam.test.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IamLoginService.class)
@WebIntegrationTest
@Transactional
public class IntrospectionEndpointAuthenticationTests {

  private String accessToken;

  @Before
  public void initAccessToken() {


    accessToken = TestUtils.passwordTokenGetter()
      .username("test")
      .password("password")
      .scope("openid profile offline_access")
      .getAccessToken();

  }

  @Test
  public void testTokenIntrospectionEndpointBasicAuthenticationSuccess() {
    // @formatter:off
    given()
      .port(8080)
      .auth()
        .preemptive()
          .basic("password-grant", "secret")
      .formParam("token", accessToken)
    .when()
      .post("/introspect")
    .then()
      .log().body(true)
      .statusCode(HttpStatus.OK.value())
      .body("active", equalTo(true));
    // @formatter:on
  }

  @Test
  public void testTokenIntrospectionEndpointFormAuthenticationSuccess() {
    // @formatter:off
    given()
      .port(8080)
      .formParam("token", accessToken)
      .formParam("client_id", "client-cred")
      .formParam("client_secret", "secret")
      .log().all(true)
    .when()
      .post("/introspect")
    .then()
      .log().all(true)
      .statusCode(HttpStatus.OK.value())
      .body("active", equalTo(true));
    // @formatter:on
  }

  @Test
  public void testTokenIntrospectionEndpointFormAuthenticationFailue() {
    // @formatter:off
    given()
      .port(8080)
      .formParam("token", accessToken)
      .formParam("client_id", "fake-client-id")
      .formParam("client_secret", "fake-secret")
      .log().all(true)
    .when()
      .post("/introspect")
    .then()
      .log().all(true)
      .statusCode(HttpStatus.UNAUTHORIZED.value());
    // @formatter:on
  }
  
  @Test
  public void testTokenIntrospectionEndpointNoAuthenticationFailure() {
    // @formatter:off
    given()
      .port(8080)
      .formParam("token", accessToken)
    .when()
      .post("/introspect")
    .then()
      .log().body(true)
      .statusCode(HttpStatus.UNAUTHORIZED.value());
   // @formatter:on

  }
}
