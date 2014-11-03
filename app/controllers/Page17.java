package controllers;

import models.Listing;
import play.Logger;
import play.data.validation.Error;
import uk.gov.gds.dm.Fixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Page17 extends AuthenticatingQuestionPage {

    private static final Long PAGE_ID = 17l;

    public static void savePage(Long listingId, String p17q1, String p17q2, String return_to_summary) {

        Listing listing = Listing.getByListingId(listingId);

        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier.");
            notFound();
        }

        if (listing.serviceSubmitted) {
          redirect(listing.summaryPageUrl());
        }

        // Validate all fields on this page requiring validation
        validation.required(p17q1).key("p17q1");
        validation.maxSize(p17q1, 10);

        if(p17q1 != null && p17q1.toLowerCase().equals("yes")){
            validation.required(p17q2).key("p17q2");
        }
        validation.maxSize(p17q2, 100).message("Your answer must be less than 100 characters in length.");

        if(validation.hasErrors()) {
            flash.put("body", params.get("body"));
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

        // Save the form data as a Page into the correct page index
        Map<String, String> pageResponses = new HashMap<String, String>();
        pageResponses.put("p17q1", p17q1);
        pageResponses.put("p17q2", p17q2);
        saveResponseToPage(PAGE_ID, listing, pageResponses);
        if (return_to_summary.contains("yes")) {
          redirect(listing.summaryPageUrl(PAGE_ID));
        } else {
          redirect(listing.nextPageUrl(PAGE_ID, listing.id));
        }
    }
}
