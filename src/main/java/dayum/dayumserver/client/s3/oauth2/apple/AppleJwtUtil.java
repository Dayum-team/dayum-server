package dayum.dayumserver.client.s3.oauth2.apple;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.*;
import java.util.*;

public class AppleJwtUtil {

  public static String createClientSecret() throws Exception {
    String teamId = "WC5T233HM3";
    String clientId = "com.dayum.app";
    String keyId = "FMM3DT22J7";
    String privateKeyPath = "src/main/resources/AuthKey_FMM3DT22J7.p8";

    // 1. .p8 키 로드
    String privateKeyPem =
        new String(Files.readAllBytes(Paths.get(privateKeyPath)))
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("EC");
    ECPrivateKey privateKey = (ECPrivateKey) kf.generatePrivate(keySpec);

    // 2. JWT Claims 생성
    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(teamId)
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1시간
            .audience("https://appleid.apple.com")
            .subject(clientId)
            .build();

    // 3. Header + Signing
    JWSHeader header =
        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).type(JOSEObjectType.JWT).build();

    SignedJWT signedJWT = new SignedJWT(header, claimsSet);
    JWSSigner signer = new ECDSASigner(privateKey);
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }
}
