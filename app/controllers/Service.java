package controllers;

import models.Document;
import models.Listing;
import models.Page;
import play.Logger;
import play.data.validation.Error;
import uk.gov.gds.dm.ServiceSubmissionJourneyFlows;
import uk.gov.gds.dm.Fixtures;

import java.util.*;

public class Service extends AuthenticatingController {

    public static void summaryPage(Long listingId) {
        
        Logger.info(String.format("Summary page for listing %d.", listingId));
        
        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        // Check listing belongs to authenticated user
        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }

        List<Long> flow = ServiceSubmissionJourneyFlows.getFlow(listing.lot);
        List<String> optionalQuestions = ServiceSubmissionJourneyFlows.getOptionalQuestions();

        Map<String, Collection<String>> allAnswers = new HashMap<String, Collection<String>>();

        for(Page p : listing.completedPages){
            if(p.responses != null){
                allAnswers.putAll(p.getUnflattenedResponses());
            }
        }

        renderArgs.put("content", Fixtures.getContentProperties());
        renderArgs.put("service_id", listingId);
        renderArgs.put("listing", listing);
        renderArgs.put("flow", flow);
        renderArgs.put("maxPossibleNumberOfQuestions", 20);
        renderArgs.put("optionalQuestions", optionalQuestions);
        renderArgs.put("pageIndex", 0);
        renderArgs.put("storedValues", allAnswers);
        renderArgs.put("pageTitle", "Service summary – " + listing.title + " – Digital Marketplace");
        render(listingId);
    }

    public static void newService() {
        Logger.info(String.format("Add new service page"));
        renderArgs.put("content", Fixtures.getContentProperties());
        render();
    }

    public static void createListing(String lot) {
        Logger.info(String.format("Creating new listing in lot: %s", lot));
        validation.required(lot).key("p0q1");
        validation.match(lot, "SaaS|IaaS|PaaS|SCS").key("p0q1");

        if(validation.hasErrors()) {
            for(Map.Entry<String, List<Error>> entry : validation.errorsMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().get(0).message();

                flash.put(key, value);
            }
            Logger.info(String.format("Validation errors: %s; reloading page.", validation.errorsMap().toString()));
            redirect("/addservice");
        }

        Listing listing = new Listing(getSupplierId(), params.get("lot"));
        listing.insert();

        redirect(String.format("/page/%d/%d", listing.firstPage(), listing.id));
    }

    public static void completeListing(Long listingId, String serviceCompleted){
        Logger.info(String.format("Completing listing: %d (%s)", listingId, serviceCompleted));
        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }

        validation.required(serviceCompleted);
        if (serviceCompleted != null){
            validation.isTrue(listing.allPagesHaveBeenCompleted()).key("service").message("This service is not complete.");
        }

        if (validation.hasErrors()){
          listing.serviceSubmitted = false;
          listing.save();
          flash.put("feedback_message", "returned to drafts");
        } else if (!listing.serviceSubmitted) { // Only complete the listing if not already completed
          listing.completeListing(getSupplierEmail());
          flash.put("feedback_message", "successfully submitted");
        }
        flash.put("feedback_relates_to_service_id", listingId);
        flash.put("feedback_relates_to_service_name", listing.title);
        redirect("/");
    }

    public static void markListingAsDraft(Long listingId){
        Logger.info(String.format("Marking listing as draft: %d", listingId));
        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }

        listing.serviceSubmitted = false;
        listing.save();

        flash.put("feedback_message", "has been returned to drafts");
        flash.put("feedback_relates_to_service_id", listingId);
        flash.put("feedback_relates_to_service_name", listing.title);
        redirect("/");
    }

    public static void showDeletePage(Long listingId, String showDeleteMessage){
        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }

        List<Long> flow = ServiceSubmissionJourneyFlows.getFlow(listing.lot);
        List<String> optionalQuestions = ServiceSubmissionJourneyFlows.getOptionalQuestions();

        Map<String, Collection<String>> allAnswers = new HashMap<String, Collection<String>>();

        for(Page p : listing.completedPages){
            if(p.responses != null){
                allAnswers.putAll(p.getUnflattenedResponses());
            }
        }

        renderArgs.put("content", Fixtures.getContentProperties());
        renderArgs.put("service_id", listingId);
        renderArgs.put("listing", listing);
        renderArgs.put("flow", flow);
        renderArgs.put("maxPossibleNumberOfQuestions", 20);
        renderArgs.put("optionalQuestions", optionalQuestions);
        renderArgs.put("pageIndex", 0);
        renderArgs.put("storedValues", allAnswers);
        if(showDeleteMessage != null){
            renderArgs.put("confirmDeleteMessage", "Are you sure you want to delete this service?");
        }
        renderTemplate("Service/summaryPage.html", listingId);
    }

    public static void delete(Long listingId){
        Listing listing = Listing.getByListingId(listingId);

        notFoundIfNull(listing);

        if(!listing.supplierId.equals(getSupplierId())) {
            Logger.error("Supplier id of listing did not match the logged in supplier. Expected: " + listing.supplierId + ", Found: " + getSupplierId());
            notFound();
        }

        for (Page page: listing.completedPages) {
            for (Document doc: page.submittedDocuments.values()) {
                doc.delete();
            }
            page.delete();
        }

        listing.delete();
        redirect("/");
    }
}
