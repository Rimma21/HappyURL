package shorturl;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlRecord, Long> {
    UrlRecord findByUrl(String url);
}
