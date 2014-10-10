package controllers;

import com.google.gson.Gson;
import models.Listing;
import models.Page;
import play.data.validation.Error;
import uk.gov.gds.dm.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Page2 extends AuthenticatingController {

    private static final Long PAGE_ID = 2l;

    public static void savePage(Long listingId, String[] p2q1) {

        Listing listing = Listing.getByListingId(listingId);

        // Validate all fields on this page requiring validation
        validation.required(p2q1);
        validation.isTrue(ValidationUtils.stringArrayValuesAreNotTooLong(p2q1, 50)).key("p2q1").message("Invalid values");

        if(validation.hasErrors()) {
            flash.put("body", params.get("body"));
            for(Map.Entry<String, List<Error>> entry : validation.errorsMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().get(0).message();

                flash.put(key, value);
            }
            redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
        }

        // Save the form data as a Page into the correct page index
        Page page = new Page(listingId, PAGE_ID);
        page.addFieldToPageResponse("p2q1", p2q1);
        page.insert();
        listing.addResponsePage(page, PAGE_ID);
        redirect(listing.nextPageUrl(PAGE_ID, listing.id));
    }
}
