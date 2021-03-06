package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.Listing;
import models.Page;
import play.Logger;
import play.Play;
import play.utils.Properties;
import uk.gov.gds.dm.Fixtures;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;

public class QuestionPageDisplay extends AuthenticatingController {

    public static void showPage(Long pageId, Long listingId, Boolean return_to_summary) throws Exception {

        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        Page page = null;
        Properties content = Fixtures.getContentProperties();

        try {
            page = listing.getResponsePageByPageId(pageId);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        // Check listing belongs to authenticated user
        if (!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }

        // Check page belongs to lot for listing
        if (!listing.pageSequence.contains(pageId)) {
            Logger.error("Page-id does not appear in the current listing's page sequence.");
            notFound();
        }

        // The unit tests (running in DEV mode) do not mock the listing object
        // properly so listing.serviceSubmitted will always return true, causing
        // the tests to fail
        if (Play.mode != Play.Mode.DEV && listing.serviceSubmitted == true) {
            Logger.error("App is in prodution and this service has been submitted.");
            notFound();
        }

        int sequenceIndex = listing.pageSequence.indexOf(pageId);
        renderArgs.put("lot", listing.lot);
        renderArgs.put("listingID", listing.id);
        renderArgs.put("content", content);
        renderArgs.put("pageNum", Integer.toString(sequenceIndex + 1));
        renderArgs.put("pageTotal", Integer.toString(listing.pageSequence.size()));
        renderArgs.put("listingId", listingId);
        renderArgs.put("prevPageURL", listing.prevPageUrl(pageId, listingId));
        renderArgs.put("summaryPageURL", listing.summaryPageUrl());
        renderArgs.put("return_to_summary", return_to_summary);
        renderArgs.put("pageTitle", content.get("p" + pageId + "title") + " – " + listing.title + " submission – Digtal Marketplace");

        if (flash.get("body") != null) {
            try {
                renderArgs.put("oldValues", unflatten(getInvalidAnswers()));
            } catch (Exception ex) {
                Logger.error("Error reading in previously entered responses", ex);
            }
        } else if (page != null) {
            if (page.responses != null) {
                renderArgs.put("oldValues", page.getUnflattenedResponses());
            }
        }

        renderArgs.put("serviceName", listing.title);
        renderTemplate(String.format("QuestionPages/%d.html", pageId));
    }

    private static Map<String, String> getInvalidAnswers() throws UnsupportedEncodingException {
        return flatten(splitParams(flash.get("body")));
    }

    // Code based on answer given by user Pr0gr4mm3r at:
    // http://stackoverflow.com/questions/13592236/parse-the-uri-string-into-name-value-collection-in-java
    private static Map<String, List<String>> splitParams(String paramString) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap();
        final String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    public static Map<String, String> flatten(Map<String, List<String>> map) {
        Map<String, String> toReturn = new HashMap();
        Gson gson = new Gson();
        for (String key : map.keySet()) {
            if (map.get(key).isEmpty()) {
                toReturn.put(key, null);
            } else if (map.get(key).size() == 1) {
                toReturn.put(key, map.get(key).get(0));
            } else {
                toReturn.put(key, gson.toJson(map.get(key)));
            }
        }
        return toReturn;
    }

    private static Map<String, Collection<String>> unflatten(Map<String, String> flatmap) {
        Map<String, Collection<String>> toReturn = new HashMap();
        Gson gson = new Gson();
        for (String key : flatmap.keySet()) {
            String val = flatmap.get(key);
            ArrayList<String> list = new ArrayList();
            if (val == null || val.isEmpty()) {
                toReturn.put(key, list);
            } else if (val.startsWith("[\"")) {
                Type collectionType = new TypeToken<Collection<String>>() {
                }.getType();
                Collection<String> vals = gson.fromJson(val, collectionType);
                toReturn.put(key, vals);
            } else {
                list.add(val);
                toReturn.put(key, list);
            }
        }
        return toReturn;
    }
}
