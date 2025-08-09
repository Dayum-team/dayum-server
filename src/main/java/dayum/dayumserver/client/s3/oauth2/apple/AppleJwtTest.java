package dayum.dayumserver.client.s3.oauth2.apple;

public class AppleJwtTest {
  public static void main(String[] args) throws Exception {
    String clientSecret = AppleJwtUtil.createClientSecret();
    System.out.println("🔥 Generated Apple JWT client_secret:");
    System.out.println(clientSecret);
  }
}
