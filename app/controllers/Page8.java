package controllers;

import models.Document;
import models.Listing;
import play.Logger;
import play.data.Upload;
import play.i18n.Messages;
import play.data.validation.Error;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import uk.gov.gds.dm.Fixtures;

import static uk.gov.gds.dm.DocumentUtils.*;

public class Page8 extends AuthenticatingQuestionPage {

    private static final Long PAGE_ID = 8l;

    public static void savePage(Long listingId, String p8q1MinPrice, String p8q1MaxPrice, String p8q1Unit, String p8q1Interval,
                                String p8q2, String p8q3, String p8q4, String p8q5, Upload p8q6, Upload p8q7, String p8q6_uploaded,
                                String return_to_summary) {

        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        Map<String,Document> docs = new HashMap<String,Document>();
        
        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }
        
        if (listing.serviceSubmitted) {
            Logger.info("Trying to edit a submitted service; redirect to summary page.");
            redirect(listing.summaryPageUrl());
        }

        Double min = null, max = null;

        validation.required(p8q1MinPrice).key("p8q1").message("validationNoMinPriceSpecified");
        validation.match(p8q1MinPrice, "[0-9]+(\\.\\d{1,5})?").key("p8q1").message("validationNotANumber");
        validation.match(p8q1MaxPrice, "[0-9]+(\\.\\d{1,5})?").key("p8q1").message("validationNotANumber");
        validation.maxSize(p8q1MinPrice, 100);
        validation.maxSize(p8q1MaxPrice, 100);
        validation.required(p8q1Unit).key("p8q1").message("validationNoUnitSpecified");
        validation.maxSize(p8q1Unit, 25);
        validation.maxSize(p8q1Interval, 25);
        validation.required(p8q2).key("p8q2");
        validation.maxSize(p8q2, 10);
        validation.required(p8q3).key("p8q3");
        validation.maxSize(p8q3, 10);

        if (!listing.lot.equals("SCS")) {
            validation.required(p8q4).key("p8q4");
            validation.required(p8q5).key("p8q5");
        }

        try {
            min = Double.valueOf(p8q1MinPrice);
            if (min < 0) {
                validation.addError("p8q1", Messages.getMessage("en", "validation.invalid"));
            }
        } catch(Exception ex) {
            validation.addError("p8q1", Messages.getMessage("en", "validation.invalid"));
        }
        try {
            if(p8q1MaxPrice != null && !p8q1MaxPrice.isEmpty()) {
                max = Double.valueOf(p8q1MaxPrice);
                if (max < 0) {
                    validation.addError("p8q1", Messages.getMessage("en", "validation.invalid"));
                }
            }
        } catch(Exception ex) {
            validation.addError("p8q1", Messages.getMessage("en", "validation.invalid"));
        }
        if (min != null && max != null) {
            if (min > max) {
                validation.addError("p8q1", Messages.getMessage("en", "validation.maxLessThanMin"));
            }
        }


        // Validate documents
        if (p8q6_uploaded == null || p8q6_uploaded.isEmpty()) {
            validation.required(p8q6).key("p8q6");
        }
        if(p8q6 != null) {
            if (!validateDocumentFormat(p8q6)) {
                validation.addError("p8q6", Messages.getMessage("en", "validation.file.wrongFormat"));
            }
            if (!validateDocumentFileSize(p8q6)) {
                validation.addError("p8q6", Messages.getMessage("en", "validation.file.tooLarge"));
            }
        }

        if(p8q7 != null){
            if(!validateDocumentFormat(p8q7)){
                validation.addError("p8q7", Messages.getMessage("en", "validation.file.wrongFormat"));
            }
            if(!validateDocumentFileSize(p8q7)){
                validation.addError("p8q7", Messages.getMessage("en", "validation.file.tooLarge"));
            }
        }

        if (p8q6 != null && !validation.hasErrors()) {
            try {
                Document p8q6Document = storeDocument(p8q6, getSupplierId(), listing.id, "p8q6");
                docs.put("p8q6", p8q6Document);
            } catch (Exception e) {
                Logger.error(e, "Could not upload document to S3. Cause: %s", e.getMessage());
                validation.addError("p8q6", Messages.getMessage("en", "validation.upload.failed"));
            }
        }
        
        if (p8q7 != null && !validation.hasErrors()) {
            try {
                Document p8q7Document = storeDocument(p8q7, getSupplierId(), listing.id, "p8q7");
                docs.put("p8q7", p8q7Document);
            } catch (Exception e) {
                Logger.error(e, "Could not upload document to S3. Cause: %s", e.getMessage());
                validation.addError("p8q7", Messages.getMessage("en", "validation.upload.failed"));
            }
        }

        if(validation.hasErrors()) {
            flash.put("body", "p8q1MinPrice=" + params.get("p8q1MinPrice") + "&p8q1MaxPrice=" + params.get("p8q1MaxPrice") +
                    "&p8q1Unit=" + params.get("p8q1Unit") + "&p8q1Interval=" + params.get("p8q1Interval") +
                    "&p8q2=" + params.get("p8q2") + "&p8q3=" +
                    params.get("p8q3") + "&p8q4=" + params.get("p8q4") + "&p8q5=" + params.get("p8q5") +
                    "&p8q6_upload=" + params.get("p8q6_upload") + "&p8q7_upload=" + params.get("p8q7_upload"));
            for(Map.Entry<String, List<Error>> entry : validation.errorsMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().get(0).message();

                flash.put(key, Fixtures.getErrorMessage(key, value));
            }
            Logger.info(String.format("Validation errors: %s; reloading page.", validation.errorsMap().toString()));
            if (return_to_summary.contains("yes")) {
              redirect(String.format("/page/%d/%d?return_to_summary=yes", PAGE_ID, listing.id));
            } else {
              redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
            }
        }

        String nicePrice = "£" + p8q1MinPrice;
        if (0 != p8q1MaxPrice.trim().length()) {
          nicePrice = nicePrice + " to £" + p8q1MaxPrice;
        }
        nicePrice = nicePrice + " per " + p8q1Unit.toLowerCase();
        if (0 != p8q1Interval.trim().length()) {
          nicePrice = nicePrice + " per " + p8q1Interval.toLowerCase();
        }

        Map<String, String> pageResponses = new HashMap<String, String>();
        pageResponses.put("p8q1", nicePrice);
        pageResponses.put("p8q1MinPrice", p8q1MinPrice);
        pageResponses.put("p8q1MaxPrice", p8q1MaxPrice);
        pageResponses.put("p8q1Unit", p8q1Unit);
        pageResponses.put("p8q1Interval", p8q1Interval);
        pageResponses.put("p8q2", p8q2);
        pageResponses.put("p8q3", p8q3);
        pageResponses.put("p8q4", p8q4);
        pageResponses.put("p8q5", p8q5);
        if (p8q6 != null) pageResponses.put("p8q6", p8q6.getFileName());
        if (p8q7 != null) pageResponses.put("p8q7", p8q7.getFileName());

        saveResponseToPage(PAGE_ID, listing, pageResponses, docs);
        if (return_to_summary.contains("yes")) {
          redirect(listing.summaryPageUrl(PAGE_ID));
        } else {
          redirect(listing.nextPageUrl(PAGE_ID, listing.id));
        }
   }
}
