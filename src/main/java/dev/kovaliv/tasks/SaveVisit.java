package dev.kovaliv.tasks;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import dev.kovaliv.data.entity.Header;
import dev.kovaliv.data.entity.Link;
import dev.kovaliv.data.entity.Visit;
import lombok.extern.log4j.Log4j2;
import ua_parser.Parser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.data.Repos.visitRepo;
import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

@Log4j2
public class SaveVisit implements Runnable {
    private static final String CF_IPCOUNTRY = "cf-ipcountry";
    private static final String CF_CONNECTING_IP = "cf-connecting-ip";

    private static final String SEC_CH_UA_PLATFORM = "sec-ch-ua-platform";
    private static final String SEC_CH_UA_MOBILE = "sec-ch-ua-mobile";

    private static final String REFERER = "referer";
    private static final String USER_AGENT = "user-agent";
    private static final String ACCEPT_LANGUAGE = "accept-language";
    private static final String X_FORWARDED_FOR = "x-forwarded-for";

    private final Parser parser = new Parser();
    private final static WebServiceClient client;

    static {
        client = new WebServiceClient
                .Builder(parseInt(getenv("MAXMIND_ACCOUNT_ID")), getenv("MAXMIND_LICENCE_KEY"))
                .host("geolite.info")
                .build();
    }

    private String userAgent = "";
    private final Link link;
    private final String ip;
    private final HashMap<String, String> headersMap;
    private final HashMap<String, List<String>> queryParams;

    public SaveVisit(Link link, String ip, HashMap<String, String> headersMap, HashMap<String, List<String>> queryParams) {
        super();
        this.link = link;
        toLowerCaseKeys(headersMap);
        this.ip = getIp(ip, headersMap);
        this.headersMap = headersMap;
        this.queryParams = queryParams;
    }

    private void toLowerCaseKeys(HashMap<String, String> headersMap) {
        headersMap.keySet().stream().toList().forEach(key -> {
            String value = headersMap.get(key);
            headersMap.remove(key);
            headersMap.put(key.toLowerCase(), value);
        });
    }

    @Override
    public void run() {
        linkRepo().findById(link.getId()).ifPresent(l -> {
            userAgent = headersMap.get(USER_AGENT);
            Visit visit = Visit.builder()
                    .ip(ip)
                    .device(getDevice())
                    .browser(getBrowser())
                    .platform(getPlatform(headersMap))
                    .country(getCountry(headersMap, ip))
                    .region(getRegion(ip))
                    .city(getCity(ip))
                    .mobile(isMobile(headersMap))
                    .language(getLanguage(headersMap))
                    .campaign(getCampaign(queryParams))
                    .source(getReferer(headersMap))
                    .link(l)
                    .build();
            visit.setBot(isBot(visit));
            List<Header> headers = headersMap.entrySet().stream()
                    .map(entry -> Header.builder()
                            .name(entry.getKey().toLowerCase())
                            .value(entry.getValue())
                            .visit(visit)
                            .build())
                    .toList();
            visit.setHeaders(headers);
            visitRepo().save(visit);
        });
    }

    public static boolean isBot(Visit visit) {
        return visit.getDevice().equals("Spider") || visit.getBrowser().equals("Applebot");
    }

    public static String getReferer(HashMap<String, String> headersMap) {
        String source = "";
        if (headersMap.containsKey(REFERER)) {
            try {
                source = headersMap.get(REFERER);
                URI uri = new URI(source);
                source = uri.getHost();
                if (source.startsWith("www.")) {
                    source = source.substring(4);
                }
                source = replaceIfPredefinedName(source);
            } catch (URISyntaxException e) {
                return "";
            }
            headersMap.remove(REFERER);
        }
        return source;
    }

    private static String replaceIfPredefinedName(String source) {
        return switch (source) {
            case "google.com" -> "Google";
            case "bing.com" -> "Bing";
            case "yahoo.com" -> "Yahoo";
            case "t.co", "x.com", "twitter.com" -> "Twitter";
            case "facebook.com", "fb.com" -> "Facebook";
            case "instagram.com" -> "Instagram";
            case "linkedin.com" -> "LinkedIn";
            case "pinterest.com" -> "Pinterest";
            case "reddit.com" -> "Reddit";
            case "tumblr.com" -> "Tumblr";
            default -> source;
        };
    }

    private static String getCampaign(HashMap<String, List<String>> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        if (queryParams.containsKey("utm_campaign")) {
            return queryParams.get("utm_campaign").getFirst();
        }
        if (queryParams.containsKey("campaign")) {
            return queryParams.get("campaign").getFirst();
        }
        return "";
    }

    private static String getLanguage(HashMap<String, String> headersMap) {
        String language = "";
        if (headersMap.containsKey(ACCEPT_LANGUAGE)) {
            language = headersMap.get(ACCEPT_LANGUAGE);
            headersMap.remove(ACCEPT_LANGUAGE);
        }
        return language;
    }

    private static boolean isMobile(HashMap<String, String> headersMap) {
        boolean mobile = false;
        if (headersMap.containsKey(SEC_CH_UA_MOBILE)) {
            mobile = headersMap.get(SEC_CH_UA_MOBILE).equals("?1");
            headersMap.remove(SEC_CH_UA_MOBILE);
        }
        return mobile;
    }

    public static String getCountry(HashMap<String, String> headersMap, String ip) {
        String country = "";
        if (headersMap.containsKey(CF_IPCOUNTRY)) {
            country = headersMap.get(CF_IPCOUNTRY);
            headersMap.remove(CF_IPCOUNTRY);
        }
        if (country.isBlank()) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                country = client.country(inetAddress).getCountry().getIsoCode();
                if (country == null) {
                    return "";
                }
            } catch (IOException | GeoIp2Exception e) {
                return country;
            }
        }
        return country;
    }

    public static String getRegion(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            String region = client.city(inetAddress).getMostSpecificSubdivision().getName();
            if (region == null) {
                return "";
            }
            return region;
        } catch (IOException | GeoIp2Exception e) {
            return "";
        }
    }

    public static String getCity(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            String city = client.city(inetAddress).getCity().getName();
            if (city == null) {
                return "";
            }
            return city;
        } catch (IOException | GeoIp2Exception e) {
            return "";
        }
    }

    private static String getPlatform(HashMap<String, String> headersMap) {
        String platform = "";
        if (headersMap.containsKey(USER_AGENT)) {
            Parser parser = new Parser();
            parser.parse(headersMap.get(USER_AGENT));
            platform = parser.parseOS(headersMap.get(USER_AGENT)).family;
        }
        if (headersMap.containsKey(SEC_CH_UA_PLATFORM)) {
            platform = headersMap.get(SEC_CH_UA_PLATFORM).replaceAll("\"", "");
            headersMap.remove(SEC_CH_UA_PLATFORM);
        }
        return platform;
    }

    private String getDevice() {
        return parser.parseDevice(userAgent).family;
    }

    private String getBrowser() {
        return parser.parseUserAgent(userAgent).family;
    }

    private static String getIp(String ip, HashMap<String, String> headersMap) {
        if (headersMap.containsKey(CF_CONNECTING_IP)) {
            ip = headersMap.get(CF_CONNECTING_IP);
            headersMap.remove(CF_CONNECTING_IP);
        }
        if (headersMap.containsKey(X_FORWARDED_FOR)) {
            ip = headersMap.get(X_FORWARDED_FOR);
            headersMap.remove(X_FORWARDED_FOR);
        }
        return ip;
    }
}
