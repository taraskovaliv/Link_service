package dev.kovaliv.tasks;

import dev.kovaliv.data.entity.Header;
import dev.kovaliv.data.entity.Link;
import dev.kovaliv.data.entity.Visit;
import ua_parser.Parser;

import java.util.HashMap;
import java.util.List;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.data.Repos.visitRepo;

public class SaveVisit extends Thread {
    private static final String CF_IPCOUNTRY = "cf-ipcountry";
    private static final String CF_CONNECTING_IP = "cf-connecting-ip";

    private static final String SEC_CH_UA_PLATFORM = "sec-ch-ua-platform";
    private static final String SEC_CH_UA_MOBILE = "sec-ch-ua-mobile";

    private static final String USER_AGENT = "user-agent";
    private static final String ACCEPT_LANGUAGE = "accept-language";
    private static final String X_FORWARDED_FOR = "x-forwarded-for";

    private final Parser parser = new Parser();
    private String userAgent = "";
    private final Link link;
    private final String ip;
    private final HashMap<String, String> headersMap;

    public SaveVisit(Link link, String ip, HashMap<String, String> headersMap) {
        super();
        this.link = link;
        toLowerCaseKeys(headersMap);
        this.ip = getIp(ip, headersMap);
        this.headersMap = headersMap;
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
                    .country(getCountry(headersMap))
                    .mobile(isMobile(headersMap))
                    .language(getLanguage(headersMap))
                    .link(l)
                    .build();
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

    private static String getCountry(HashMap<String, String> headersMap) {
        String country = "";
        if (headersMap.containsKey(CF_IPCOUNTRY)) {
            country = headersMap.get(CF_IPCOUNTRY);
            headersMap.remove(CF_IPCOUNTRY);
        }
        return country;
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
