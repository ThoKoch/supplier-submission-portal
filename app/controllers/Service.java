package controllers;

import models.Listing;
import play.data.validation.Error;
import uk.gov.gds.dm.ServiceSubmissionJourneyFlows;

import java.util.List;

public class Service extends AuthenticatingController {

    public static void editPage(String id, Integer page) {
        render(id, page);
    }

    public static void summaryPage(String id) {
        long listingId = Long.parseLong(id);
        Listing listing = Listing.getByListingId(listingId);
        List<Long> flow = ServiceSubmissionJourneyFlows.getFlow(listing.lot);
        List<String> optionalQuestions = ServiceSubmissionJourneyFlows.getOptionalQuestions();

        renderArgs.put("content", Fixtures.getContentProperties());
        renderArgs.put("service_id", id);
        renderArgs.put("listing", listing);
        renderArgs.put("flow", flow);
        renderArgs.put("maxPossibleNumberOfQuestions", 20);
        renderArgs.put("optionalQuestions", optionalQuestions);
        renderArgs.put("pageIndex", 0);
        render(id);
    }

    public static void newService() {
        renderArgs.put("content", Fixtures.getContentProperties());
        render();
    }

    public static void createListing(String lot) {
        validation.required(lot);
        validation.match(lot, "SaaS|IaaS|PaaS|SCS");
        if(validation.hasErrors()) {
            for(Error error : validation.errors()) {
                System.out.println(error.message());
            }

            //TODO: Show flash error messages in page (add code to main.html to do this?)
            flash.error("Validation failed", validation.errors());
            redirect("/addservice");
        }

        Listing listing = new Listing(supplierDetailsFromCookie.get("supplierId"), params.get("lot"));
        listing.insert();

        // TODO: Get next page using page sequence saved in Listing object
        redirect(String.format("/page/%d/%d", listing.firstPage(), listing.id));
    }

    public static void submissionComplete(String listingId) {
        // TODO: Add flash message to say "All questions complete"
        redirect(String.format("/service/%d", listingId));
    }
}
