package org.oskari.capabilities.ogc;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import org.oskari.capabilities.CapabilitiesParser;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.RawCapabilitiesResponse;
import org.oskari.capabilities.ServiceConnectInfo;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class OGCCapabilitiesParser extends CapabilitiesParser {

    public String getType() {
        return this.getName().replaceAll("layer", "").toUpperCase();
    }

    protected String getExpectedContentType() {
        return "xml";
    }
    // allow overriding for OGC API services etc
    protected String getExpectedContentType(String version) {
        return getExpectedContentType();
    }
    protected abstract String getDefaultVersion();

    public String validateResponse(RawCapabilitiesResponse response) throws ServiceException {
        return CapabilitiesValidator.validateXmlResponse(response);
    }
    // allow overriding for OGC API services etc
    public String validateResponse(RawCapabilitiesResponse response, String version) throws ServiceException {
        return validateResponse(response);
    }

    public Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException {

        String capabilitiesUrl = contructCapabilitiesUrl(src.getUrl(), src.getVersion());
        RawCapabilitiesResponse response = fetchCapabilities(capabilitiesUrl, src.getUser(), src.getPass(), getExpectedContentType(src.getVersion()));
        String validResponse = validateResponse(response, src.getVersion());
        Map<String, LayerCapabilities> layers = parseLayers(validResponse, src.getVersion());
        layers.values().stream().forEach(l -> l.setUrl(response.getUrl()));
        return layers;
    }

    protected abstract Map<String, LayerCapabilities> parseLayers(String capabilities) throws ServiceException;

    // allow overriding for OGC API services etc
    protected Map<String, LayerCapabilities> parseLayers(String capabilities, String version) throws ServiceException {
        return parseLayers(capabilities);
    }

    protected String contructCapabilitiesUrl(String url, String version) {
        String urlLC = url.toLowerCase();

        final Map<String, String> params = new HashMap<>();
        // check existing params
        if (!urlLC.contains("service=")) {
            params.put("service", getType());
        }
        if (!urlLC.contains("request=")) {
            params.put("request", "GetCapabilities");
        }
        if (!urlLC.contains("version=")) {
            if (version == null || version.isEmpty()) {
                version = getDefaultVersion();
            }
            params.put(getVersionParamName(), version);
        }

        return IOHelper.constructUrl(url, params);
    }

    protected String getVersionParamName() {
        return "version";
    }
}
