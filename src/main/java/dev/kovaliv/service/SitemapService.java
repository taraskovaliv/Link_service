package dev.kovaliv.service;

import dev.kovaliv.services.sitemap.AbstractSitemapService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.jiripinkas.jsitemapgenerator.ChangeFreq.NEVER;
import static dev.kovaliv.data.Repos.linkRepo;
import static java.lang.System.getenv;

@Service
public class SitemapService extends AbstractSitemapService {

    @Override
    protected Map<String, SMValue> getUrls() {
        Map<String, SMValue> urls = new HashMap<>();
        String hostUri = getenv("HOST_URI");
        urls.put(hostUri + "/qr", new SMValue(0.9));
        linkRepo().findAll().forEach(
                link -> urls.put(hostUri + "/" + link.getName(), new SMValue(0.7, NEVER))
        );
        return urls;
    }

    @Override
    protected List<String> disallowPaths() {
        List<String> disallowPaths = super.disallowPaths();
        disallowPaths.add("/statistic");
        disallowPaths.add("/statistic/");

        return disallowPaths;
    }
}
