package dayum.dayumserver.client.s3.oauth2.apple;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class AppleJwtUtil {

  @Value("${apple.team-id}")
  private String teamId;

  @Value("${apple.client-id}")
  private String clientId;

  @Value("${apple.key-id}")
  private String keyId;

  @Value("${apple.private-key-path}")
  private String privateKeyPath;

  private final ResourceLoader resourceLoader;

  public AppleJwtUtil(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public String createClientSecret() throws Exception {
    // 1) .p8 키 로드 (classpath:, file: 모두 지원)
    String privateKeyPem =
        loadPem(privateKeyPath)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    ECPrivateKey privateKey = (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(keySpec);

    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(teamId)
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600_000)) // 1시간
            .audience("https://appleid.apple.com")
            .subject(clientId)
            .build();

    // 3) Header + Sign
    JWSHeader header =
        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).type(JOSEObjectType.JWT).build();

    SignedJWT signedJWT = new SignedJWT(header, claimsSet);
    signedJWT.sign(new ECDSASigner(privateKey));
    return signedJWT.serialize();
  }

  private String loadPem(String path) throws Exception {
    // 접두사가 없으면 classpath로 간주
    String resolved =
        (path.startsWith("classpath:") || path.startsWith("file:")) ? path : "classpath:" + path;
    Resource resource = resourceLoader.getResource(resolved);
    try (InputStream is = resource.getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
