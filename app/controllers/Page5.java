package controllers;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.gson.Gson;
import models.Listing;
import play.Logger;
import play.data.validation.Error;
import uk.gov.gds.dm.ValidationUtils;
import uk.gov.gds.dm.Fixtures;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Page5 extends AuthenticatingQuestionPage {

    private static final Long PAGE_ID = 5l;

    public static void savePage(Long listingId, String return_to_summary) throws UnsupportedEncodingException {

        Listing listing = Listing.getByListingId(listingId);

        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier.");
            notFound();
        }
        
        if (listing.serviceSubmitted) {
          redirect(listing.summaryPageUrl());
        }

        // Extract multiple values for list items
        Map<String, String[]> params = request.params.all();
        ArrayList<String> q1 = new ArrayList();
        ArrayList<String> q2 = new ArrayList();
        String s;

        StringBuilder paramString = new StringBuilder();
        for (int i=1; i<11; i++) {
            if(params.containsKey("p5q1val" + i)){
                s = params.get("p5q1val" + i)[0];
                if (!Strings.isNullOrEmpty(s)) {
                    validation.isTrue(ValidationUtils.isWordCountLessThan(s, 10)).key("p5q1").message("Too many words");
                    validation.maxSize(s, 100).key("p5q1");
                    q1.add(s);
                    paramString.append("p5q1=").append(URLEncoder.encode(s, "UTF-8")).append("&");
                }
            }

            if(params.containsKey("p5q2val" + i)){
                s = params.get("p5q2val" + i)[0];
                if (!Strings.isNullOrEmpty(s)) {
                    validation.isTrue(ValidationUtils.isWordCountLessThan(s, 10)).key("p5q2").message("Too many words");
                    validation.maxSize(s, 100).key("p5q2");
                    q2.add(s);
                    paramString.append("p5q2=").append(URLEncoder.encode(s, "UTF-8")).append("&");
                }
            }
        }
        // Validate all fields on this page requiring validation
        validation.isTrue("p5q1", q1.size() > 0).message("validation.required");
        validation.isTrue("p5q2", q2.size() > 0).message("validation.required");

        if(validation.hasErrors()) {
            flash.put("body", paramString.toString());
            for(Map.Entry<String, List<Error>> entry : validation.errorsMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().get(0).message();

                flash.put(key, Fixtures.getErrorMessage(key, value));
            }

            if (return_to_summary.contains("yes")) {
              redirect(String.format("/page/%d/%d?return_to_summary=yes", PAGE_ID, listing.id));
            } else {
              redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
            }
        }

        Map<String, String> pageResponses = new HashMap<String, String>();
        Gson gson = new Gson();
        pageResponses.put("p5q1", gson.toJson(q1));
        pageResponses.put("p5q2", gson.toJson(q2));
        saveResponseToPage(PAGE_ID, listing, pageResponses);
        if (return_to_summary.contains("yes")) {
          redirect(listing.summaryPageUrl(PAGE_ID));
        } else {
          redirect(listing.nextPageUrl(PAGE_ID, listing.id));
        }
    }
}
