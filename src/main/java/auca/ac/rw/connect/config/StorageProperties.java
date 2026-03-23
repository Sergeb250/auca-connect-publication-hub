package auca.ac.rw.connect.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Supabase S3-compatible bucket configuration.
 * Credentials should come from environment variables instead of source control.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String provider = "supabase-s3";
    private String endpoint = "https://yojaqlqoeachnxyoryas.storage.supabase.co/storage/v1/s3";
    private String region = "eu-west-1";
    private String bucket = "connect";
    private String accessKey;
    private String secretKey;
    private final Paths paths = new Paths();

    @Getter
    @Setter
    public static class Paths {
        private String avatars = "profile-avatars";
        private String memoirs = "memoirs";
        private String publicationFiles = "publication-files";
        private String githubSnapshots = "github-snapshots";
    }
}
