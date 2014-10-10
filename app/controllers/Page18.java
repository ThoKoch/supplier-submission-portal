package controllers;

import com.google.gson.Gson;
import models.Listing;
import models.Page;
import play.data.validation.Error;
import uk.gov.gds.dm.ValidationUtils;

import java.util.Map;
import java.util.List;


public class Page18 extends AuthenticatingController {

    private static final Long PAGE_ID = 18l;

    public static void savePage(Long listingId, String[] p18q1) {

        Listing listing = Listing.getByListingId(listingId);

        // Validate all fields on this page requiring validation
        validation.required(p18q1).key("p18q1");
        validation.isTrue(ValidationUtils.stringArrayValuesAreNotTooLong(p18q1, 50)).key("p18q1").message("Invalid values");

        if(validation.hasErrors()) {

            for(Map.Entry<String, List<Error>> entry : validation.errorsMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().get(0).message();

                flash.put(key, value);
            }
            System.out.println(flash);
            redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
        }

        // Save the form data as a Page into the correct page index
        Page page = new Page(listingId, PAGE_ID);
        page.addFieldToPageResponse("p18q1", p18q1);
        page.insert();
        listing.addResponsePage(page, PAGE_ID);
        redirect(listing.nextPageUrl(PAGE_ID, listing.id));
    }
}
