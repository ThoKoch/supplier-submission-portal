package controllers;

import models.Listing;
import models.Page;
import play.mvc.Controller;

public class Page15 extends Controller {

    private static final Long PAGE_ID = 15l;

    public static void savePage(Long listingId, String p15q1) {

        Listing listing = Listing.getByListingId(listingId);
        
        // TODO: Validate all fields on this page requiring validation
//        validation.required(p15q1).message("p15q1:null");
        if(validation.hasErrors()) {
            flash.error("%s", validation.errors());
            redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
        }

        // Save the form data as a Page into the correct page index
        Page page = new Page(listingId, PAGE_ID);
        page.responses.put("p15q1", p15q1);
        page.insert();
        listing.addResponsePage(page, PAGE_ID);
        redirect(listing.nextPageUrl(PAGE_ID, listing.id));
    }
}
