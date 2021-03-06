package it.infn.mw.iam.test.scim.user;

import static org.hamcrest.Matchers.containsString;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import it.infn.mw.iam.IamLoginService;
import it.infn.mw.iam.api.scim.model.ScimOidcId;
import it.infn.mw.iam.api.scim.model.ScimSamlId;
import it.infn.mw.iam.api.scim.model.ScimSshKey;
import it.infn.mw.iam.api.scim.model.ScimUser;
import it.infn.mw.iam.api.scim.model.ScimUserPatchRequest;
import it.infn.mw.iam.api.scim.model.ScimX509Certificate;
import it.infn.mw.iam.test.ScimRestUtils;
import it.infn.mw.iam.test.TestUtils;
import it.infn.mw.iam.test.util.JacksonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IamLoginService.class)
@WebIntegrationTest
public class ScimUserProvisioningPatchReplaceTests {

  private String accessToken;
  private ScimRestUtils restUtils;

  private List<ScimUser> testUsers = new ArrayList<ScimUser>();

  @BeforeClass
  public static void init() {

    JacksonUtils.initRestAssured();
  }

  private void initTestUsers() {

    testUsers.add(restUtils
      .doPost("/scim/Users/",
          ScimUser.builder("john_lennon")
            .buildEmail("lennon@email.test")
            .buildName("John", "Lennon")
            .addOidcId(ScimOidcId.builder()
              .issuer(TestUtils.oidcIds.get(0).issuer)
              .subject(TestUtils.oidcIds.get(0).subject)
              .build())
            .addSshKey(ScimSshKey.builder()
              .value(TestUtils.sshKeys.get(0).key)
              .fingerprint(TestUtils.sshKeys.get(0).fingerprintSHA256)
              .primary(true)
              .build())
            .addSshKey(ScimSshKey.builder()
              .value(TestUtils.sshKeys.get(1).key)
              .fingerprint(TestUtils.sshKeys.get(1).fingerprintSHA256)
              .primary(false)
              .build())
            .addSamlId(ScimSamlId.builder()
              .idpId(TestUtils.samlIds.get(0).idpId)
              .userId(TestUtils.samlIds.get(0).userId)
              .build())
            .addX509Certificate(ScimX509Certificate.builder()
              .display(TestUtils.x509Certs.get(0).display)
              .value(TestUtils.x509Certs.get(0).certificate)
              .primary(true)
              .build())
            .addX509Certificate(ScimX509Certificate.builder()
              .display(TestUtils.x509Certs.get(1).display)
              .value(TestUtils.x509Certs.get(1).certificate)
              .primary(false)
              .build())
            .build())
      .extract().as(ScimUser.class));
  }

  @Before
  public void setupTest() {

    accessToken = TestUtils.getAccessToken("scim-client-rw", "secret", "scim:read scim:write");
    restUtils = ScimRestUtils.getInstance(accessToken);

    initTestUsers();
  }

  @After
  public void teardownTest() {

    testUsers.forEach(user -> restUtils.doDelete(user.getMeta().getLocation()));
  }

  private ScimUserPatchRequest getPatchAddRequest(ScimUser updates) {

    return ScimUserPatchRequest.builder().add(updates).build();
  }

  private ScimUserPatchRequest getPatchReplaceRequest(ScimUser updates) {

    return ScimUserPatchRequest.builder().replace(updates).build();
  }

  private ScimUserPatchRequest getPatchRemoveRequest(ScimUser updates) {

    return ScimUserPatchRequest.builder().remove(updates).build();
  }

  @Test
  @Ignore
  public void testPatchReplaceX509CertificateDisplay() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder()
      .buildX509Certificate(user.getX509Certificates().get(0).getDisplay() + "_updated",
          user.getX509Certificates().get(0).getValue(), null)
      .build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser updated_user =
        restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert
      .assertTrue(updated_user.getX509Certificates()
        .stream()
        .filter(cert -> cert.getValue().equals(user.getX509Certificates().get(0).getValue()) && cert
          .getDisplay().equals(user.getX509Certificates().get(0).getDisplay() + "_updated"))
        .findFirst()
        .isPresent());
  }

  @Test
  @Ignore
  public void testPatchReplaceX509CertificatePrimary() {

    ScimUser user = testUsers.get(0);

    ScimX509Certificate certToReplace = user.getX509Certificates().get(0);
    ScimUserPatchRequest req = getPatchReplaceRequest(
        ScimUser.builder().buildX509Certificate(null, certToReplace.getValue(), false).build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser updated_user =
        restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);
    Assert.assertTrue(
        updated_user.getX509Certificates().stream().filter(cert -> cert.isPrimary()).count() == 1);
  }

  @Test
  @Ignore
  public void testPatchReplaceX509CertificatePrimarySwitch() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder()
      .buildX509Certificate(null, user.getX509Certificates().get(0).getValue(), false)
      .buildX509Certificate(null, user.getX509Certificates().get(1).getValue(), true)
      .build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser updated_user =
        restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertTrue(updated_user.getX509Certificates()
      .stream()
      .filter(cert -> cert.getValue().equals(user.getX509Certificates().get(0).getValue())
          && !cert.isPrimary())
      .findFirst()
      .isPresent());

    Assert.assertTrue(updated_user.getX509Certificates()
      .stream()
      .filter(cert -> cert.getValue().equals(user.getX509Certificates().get(1).getValue())
          && cert.isPrimary())
      .findFirst()
      .isPresent());
  }

  @Test
  @Ignore
  public void testPatchReplaceX509CertificateNotFound() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(
        ScimUser.builder().buildX509Certificate("fake_display", "fake_value", true).build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.NOT_FOUND);
  }

  @Test
  @Ignore
  public void testPatchReplaceSshKeyDisplay() {

    ScimUser user = testUsers.get(0);

    ScimSshKey sshKeyToReplace = user.getIndigoUser().getSshKeys().get(0);
    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder()
      .buildSshKey("New ssh key label", sshKeyToReplace.getValue(),
          sshKeyToReplace.getFingerprint(), sshKeyToReplace.isPrimary())
      .build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser updated_user =
        restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertTrue(updated_user.getIndigoUser()
      .getSshKeys()
      .stream()
      .filter(sshKey -> sshKey.getValue().equals(sshKeyToReplace.getValue())
          && sshKey.getDisplay().equals("New ssh key label"))
      .findFirst()
      .isPresent());
  }

  @Test
  @Ignore
  public void testPatchReplaceSshKeyPrimary() {

    ScimUser user = testUsers.get(0);

    ScimSshKey keyToReplace = user.getIndigoUser().getSshKeys().get(0);
    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder()
      .buildSshKey(keyToReplace.getDisplay(), keyToReplace.getValue(),
          keyToReplace.getFingerprint(), false)
      .build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser updated_user =
        restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertTrue(updated_user.getIndigoUser()
      .getSshKeys()
      .stream()
      .filter(sshKey -> sshKey.isPrimary())
      .count() == 1);
  }

  @Test
  @Ignore
  public void testPatchReplaceSshKeyNotSupported() {

    String location = testUsers.get(0).getMeta().getLocation();
    String keyValue = testUsers.get(0).getIndigoUser().getSshKeys().get(0).getValue();

    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder()
      .addSshKey(ScimSshKey.builder().value(keyValue).display("NEW LABEL").build())
      .build());

    restUtils.doPatch(location, req, HttpStatus.BAD_REQUEST);
  }

  @Test
  @Ignore
  public void testPatchReplaceX509CertificateNotSupported() {

    String location = testUsers.get(0).getMeta().getLocation();
    String certValue = testUsers.get(0).getX509Certificates().get(0).getValue();

    ScimUserPatchRequest req =
        getPatchReplaceRequest(
            ScimUser.builder()
              .addX509Certificate(
                  ScimX509Certificate.builder().value(certValue).display("NEW LABEL").build())
              .build());

    restUtils.doPatch(location, req, HttpStatus.BAD_REQUEST);
  }

  @Test
  @Ignore
  public void testPatchReplaceSshKeyPrimarySwitch() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder()
      .buildSshKey(null, user.getIndigoUser().getSshKeys().get(0).getValue(), null, false)
      .buildSshKey(null, user.getIndigoUser().getSshKeys().get(1).getValue(), null, true)
      .build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser updated_user =
        restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    String sshKeyValue = user.getIndigoUser().getSshKeys().get(0).getValue();

    Assert.assertTrue(updated_user.getIndigoUser()
      .getSshKeys()
      .stream()
      .filter(sshKey -> (sshKey.getValue().equals(sshKeyValue) && !sshKey.isPrimary()))
      .findFirst()
      .isPresent());

    Assert.assertTrue(
        updated_user.getIndigoUser()
          .getSshKeys()
          .stream()
          .filter(sshKey -> sshKey.getValue()
            .equals(user.getIndigoUser().getSshKeys().get(1).getValue()) && sshKey.isPrimary())
          .findFirst()
          .isPresent());
  }

  @Test
  @Ignore
  @Deprecated
  public void testPatchReplaceSshKeyNotFound() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(
        ScimUser.builder().buildSshKey("fake_label", "fake_key", "fake_fingerprint", true).build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.NOT_FOUND);
  }


  @Test
  public void testReplaceEmailWithEmptyValue() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder().buildEmail("").build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.BAD_REQUEST).body("detail",
        containsString(
            "scimUserPatchRequest.operations[0].value.emails[0].value : may not be empty"));
  }

  @Test
  public void testReplaceEmailWithNullValue() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(ScimUser.builder().buildEmail(null).build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.BAD_REQUEST).body("detail",
        containsString(
            "scimUserPatchRequest.operations[0].value.emails[0].value : may not be empty"));
  }

  @Test
  public void testReplaceEmailWithSameValue() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req = getPatchReplaceRequest(
        ScimUser.builder().buildEmail(user.getEmails().get(0).getValue()).build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.NO_CONTENT);
  }

  @Test
  public void testReplaceEmailWithInvalidValue() {

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req =
        getPatchReplaceRequest(ScimUser.builder().buildEmail("fakeEmail").build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.BAD_REQUEST)
      .body("detail", containsString(
          "scimUserPatchRequest.operations[0].value.emails[0].value : not a well-formed email address"));
  }

  @Test
  public void testReplacePicture() {

    final String pictureURL = "http://iosicongallery.com/img/512/angry-birds-2-2016.png";
    final String pictureURL2 = "https://fallofthewall25.com/img/default-user.jpg";
    ScimUser user = testUsers.get(0);

    ScimUser uUser1 = restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertTrue(uUser1.getPhotos() == null);

    ScimUserPatchRequest req =
        getPatchAddRequest(ScimUser.builder().buildPhoto(pictureURL).build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    uUser1 = restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertFalse(uUser1.getPhotos().isEmpty());
    Assert.assertTrue(uUser1.getPhotos().get(0).getValue().equals(pictureURL));

    req = getPatchReplaceRequest(ScimUser.builder().buildPhoto(pictureURL).build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser uUser2 = restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertFalse(uUser2.getPhotos().isEmpty());
    Assert.assertTrue(uUser2.getPhotos().get(0).getValue().equals(pictureURL));
    // Assert
    // .assertTrue(uUser2.getMeta().getLastModified().equals(uUser1.getMeta().getLastModified()));

    req = getPatchReplaceRequest(ScimUser.builder().buildPhoto(pictureURL2).build());

    restUtils.doPatch(user.getMeta().getLocation(), req);

    ScimUser uUser3 = restUtils.doGet(user.getMeta().getLocation()).extract().as(ScimUser.class);

    Assert.assertFalse(uUser3.getPhotos().isEmpty());
    Assert.assertTrue(uUser3.getPhotos().get(0).getValue().equals(pictureURL2));
    // Assert
    // .assertFalse(uUser3.getMeta().getLastModified().equals(uUser2.getMeta().getLastModified()));

    req = getPatchRemoveRequest(ScimUser.builder().buildPhoto(pictureURL2).build());

    restUtils.doPatch(user.getMeta().getLocation(), req);
  }

  @Test
  public void testReplaceUsername() {

    final String ANOTHERUSER_USERNAME = "test";

    ScimUser user = testUsers.get(0);

    ScimUserPatchRequest req =
        getPatchReplaceRequest(ScimUser.builder().userName(ANOTHERUSER_USERNAME).build());

    restUtils.doPatch(user.getMeta().getLocation(), req, HttpStatus.CONFLICT);
  }
}
