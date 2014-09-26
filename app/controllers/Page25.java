package controllers;

import models.Listing;
import models.Page;
import play.mvc.Controller;

public class Page25 extends Controller {

    private static final Long PAGE_ID = 25l;

    public static void savePage(Long listingId, String p25q1, String p25q2, String p25q3, String p25q4) {

        Listing listing = Listing.getByListingId(listingId);
        
        // Validate all fields on this page requiring validation
        validation.required(p25q1).message("p25q1 : null");
        validation.required(p25q2).message("p25q2 : null");
        validation.required(p25q3).message("p25q3 : null");
        validation.required(p25q4).message("p25q4 : null");
        if(validation.hasErrors()) {
            flash.error("%s", validation.errors());
            redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
        }

        // Save the form data as a Page into the correct page index
        Page page = new Page(listingId, PAGE_ID);
        page.responses.put("p25q1", p25q1);
        page.responses.put("p25q2", p25q2);
        page.responses.put("p25q3", p25q3);
        page.responses.put("p25q4", p25q4);
        page.insert();
        listing.addResponsePage(page, PAGE_ID);
        redirect(listing.nextPageUrl(PAGE_ID, listing.id));
    }
}