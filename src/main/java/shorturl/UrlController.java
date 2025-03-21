package shorturl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PropertySource("classpath:application.properties")
public class UrlController {

    @Value("${custom.domain}")
    private String domain;

    @Value("${server.port}")
    private int port;

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("shorten_simple")
    public UrlResultDTO shorten(@RequestParam String url) { // Jackson / GSON
        var urlDTO = new UrlDTO();
        urlDTO.setUrl(url);

        long id = urlService.saveUrl(urlDTO);

        var result = new UrlResultDTO();
        result.setUrl(urlDTO.getUrl());
        result.setShortUrl(buildUrl(domain, port, id));

        return result;
    }

    @PostMapping("shorten")
    public UrlResultDTO shorten(@RequestBody UrlDTO urlDTO) { // Jackson / GSON
        long id = urlService.saveUrl(urlDTO);

        var result = new UrlResultDTO();
        result.setUrl(urlDTO.getUrl());
        result.setShortUrl(buildUrl(domain, port, id));

        return result;
    }

    /*
        302
        Location: https://goto.com
        Cache-Control: no-cache, no-store, must-revalidate
     */

    @GetMapping("/my/{id}")
    public ResponseEntity<Void> redirect(@PathVariable("id") Long id) {
        var url = urlService.getUrl(id);

        var headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("stat")
    public List<UrlStatDTO> stat() {
        return urlService.getStatistics();
    }

    private String buildUrl(String domain, int port, long id) {
        return String.format("http://%s:%d/my/%d", domain, port, id);
    }
}
