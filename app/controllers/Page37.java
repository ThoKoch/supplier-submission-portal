package controllers;

import com.google.gson.Gson;
import models.Listing;
import models.Page;
import play.data.validation.Error;
import uk.gov.gds.dm.ValidationUtils;

import java.util.Map;
import java.util.List;

public class Page37 extends AuthenticatingController {

    private static final Long PAGE_ID = 37l;

    public static void savePage(Long listingId, String p37q1, String[] p37q2, String p37q3, String p37q3assurance, String p37q4, String p37q4assurance) {

        Listing listing = Listing.getByListingId(listingId);

        // Validate all fields on this page requiring validation
        if (!listing.lot.equals("SaaS")) {
            validation.required(p37q1).key("p37q1");
            validation.maxSize(p37q1, 10);
            validation.required(p37q2).key("p37q2");
            validation.isTrue(ValidationUtils.stringArrayValuesAreNotTooLong(p37q2, 20)).key("p37q2").message("Invalid values.");
        }
        validation.required(p37q3).key("p37q3");
        validation.maxSize(p37q3, 10);
        validation.required(p37q3assurance).key("p37q3assurance");
        validation.maxSize(p37q3assurance, 50);

        validation.required(p37q4).key("p37q4");
        validation.maxSize(p37q4, 10);
        validation.required(p37q4assurance).key("p37q4assurance");
        validation.maxSize(p37q4assurance, 50);

        if(validation.hasErrors()) {
            flash.put("body", params.get("body"));
            for(Map.Entry<String, List<Error>> entry : validation.errorsMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().get(0).message();

                flash.put(key, value);
            }
            System.out.println(flash);
            redirect(String.format("/page/%d/%d", PAGE_ID, listing.id));
        }

        Page page = new Page(listingId, PAGE_ID);
        page.addFieldToPageResponse("p37q1", p37q1);
        if (p37q2 != null) {
            page.addFieldToPageResponse("p37q2", p37q2);
        }
        else {
            page.addFieldToPageResponse("p37q2");
        }
        page.addFieldToPageResponse("p37q3", p37q3);
        page.addFieldToPageResponse("p37q4", p37q4);
        page.addFieldToPageResponse("p37q3assurance", p37q3assurance);
        page.addFieldToPageResponse("p37q4assurance", p37q4assurance);
        page.insert();
        listing.addResponsePage(page, PAGE_ID);
        redirect(listing.nextPageUrl(PAGE_ID, listing.id));
    }

}