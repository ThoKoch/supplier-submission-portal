package controllers;

import models.Listing;
import models.Page;
import play.mvc.Controller;

public class Page4 extends Controller {

    private static final Long PAGE_ID = 4l;

    public static void savePage(Long listingId, String p2_q1, String p2_q2) {
        
        Listing listing = Listing.getByListingId(listingId);
        // Validate all fields on this page requiring validation
        validation.required(p2_q1).message("Please enter something.");
        validation.required(p2_q2).message("Please enter something else.");
        
        if(validation.hasErrors()) {
            flash.error("Validation failed: %s", validation.errors());
            redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
        }
        
        // Save the form data as a Page into the correct page index
        int index = listing.pageSequence.indexOf(PAGE_ID);
        Page page = new Page(listingId, PAGE_ID);
        page.responses.put("p2_q1", p2_q1);
        page.responses.put("p2_q2", p2_q2);
        listing.completedPages.add(index, page);
        listing.title = p2_q1;
        listing.description = p2_q2;
        listing.update();
        redirect(listing.nextPageUrl(PAGE_ID, listing.id));
    }
}