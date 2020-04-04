//Utility class for the search function
package SearchUtil;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import android.media.ExifInterface;

public class SearchUtil
{
    //Given the search criteria, master caption list, and master date list, returns a list of lists
    //containing the filtered filename list, caption list, and date list,
    //containing the information of only the pictures that match the search criteria.
    //"Context c" has been added for testability.
    public List Search(Context c, String get_caption, String returnStartTime, String returnEndTime,
                       String lat1, String lng1, String lat2, String lng2, List captionListM, List dateListM)
    {
        //Initialize date formats
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        SimpleDateFormat sdfUser = new SimpleDateFormat("yyyyMMdd_HHmmss");
        ParsePosition parsepos = new ParsePosition(0);
        //Set up the search criteria for date
        Date dStartTime;
        Date dEndTime;

        // setup new data type for geo location, converting string input to float below, and compare number later
        float flat1 ;
        float flat2 ;
        float flng1 ;
        float flng2 ;

        //if one or both dates were left empty, set the search criteria for date so that all image dates are accepted
        if (returnStartTime.isEmpty() || returnEndTime.isEmpty())
        {
            String minDate = "00010101_000000";
            String maxDate = "20500505_000000";
            Date minDateD = sdfUser.parse(minDate,parsepos);
            parsepos.setIndex(0);
            Date maxDateD = sdfUser.parse(maxDate,parsepos);
            parsepos.setIndex(0);
            dStartTime = minDateD;
            dEndTime = maxDateD;
        }
        //Else, set the search criteria for date to the user-entered values
        ////////////////////////////////////////////////////////////////////////
        ////////     the user input the date in a simple format that can't be compared with the format of the list
        ////          so we get the "simple format" string,
        else {
            dStartTime = sdfUser.parse(returnStartTime, parsepos);
            parsepos.setIndex(0);
            dEndTime = sdfUser.parse(returnEndTime, parsepos);
            parsepos.setIndex(0);
        }

        //if any of geo location were left empty, set the search criteria for geo location so that all image dates are accepted. IL
        if (lat1.isEmpty() && lat2.isEmpty() && lng1.isEmpty() && lng2.isEmpty() )
        {
            flat1 = 90;
            flat2 = -90;
            flng1 = 180;
            flng2 = -179;
        }
        // if only the lat are empty
        else if(lat1.isEmpty() || lat2.isEmpty())
        {
            flat1 = 90;
            flat2 = -90;
            flng1 =Float.parseFloat(lng1);
            flng2 =Float.parseFloat(lng2);
        }
        // if only hte long are empty
        else if(lng1.isEmpty() || lng2.isEmpty())
        {
            flat1 =Float.parseFloat(lat1);
            flat2 =Float.parseFloat(lat2);
            flng1 = 180;
            flng2 = -179;
        }
        else   // else use the user input. IL
        {
            flat1 =Float.parseFloat(lat1);
            flat2 =Float.parseFloat(lat2);
            flng1 =Float.parseFloat(lng1);
            flng2 =Float.parseFloat(lng2);
        }

        //Loop through all files, add the ones that match the search criteria to the filtered lists
        List filenameListF = new ArrayList();
        List captionListF = new ArrayList();
        List dateListF = new ArrayList();
        int cap_index = 0;
        File file = null;
        file = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES); //using Context c for testability


        // ------------------ for location search, my attempt at accessing location information in this function---------// IL
        float[] f = {0,0};   // the two values are stored here temporaely, long, lat
        boolean result_location = false;
        // ------------------- for location search, my attempt at accessing location information in this function---------// IL


        for (File fwm : file.listFiles()) {
            String str = captionListM.get(cap_index).toString();
            if (str.contains(get_caption)) {       ////////////////   comparsion
                Date d1 = new Date();
                d1 = sdf.parse(dateListM.get(cap_index).toString(), parsepos);
                parsepos.setIndex(0);
                if (d1.compareTo(dStartTime) > 0 && d1.compareTo(dEndTime) < 0) {

                    // I was not sure how to access the location data inside this function, so i copied the method in main activity and
                    // put below. The difference is that the string filename is "+fwm.getName();"
                    // instead of "filenameListF.get(currentElement).toString();"       IL

                    // ------------ for location search, my attempt at accessing location information in this function---------// IL
                    try {
                        String filename = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/"+fwm.getName();
                        ExifInterface exif = new ExifInterface(filename);
                        result_location = exif.getLatLong(f);     // does it get both lat and long and put it in the list?? this function "getLatLong()" take what kind of input ? IL
                    }
                    catch (IOException e) { }

                    // if the location of the picture is within user input, populate the lists (filelist, datalist, and captionlist) . IL
                    if(result_location == true)
                    {
                        if (flat1 > f[0] && flat2 < f[0]    &&   flng1 > f[1] && flng2 < f[1]  )   // the filter logic to get the img within boundary of interest. IL
                        {
                            filenameListF.add(fwm.getName()); // if all 3 condition above met (time, caption ,and location), populate the list. IL
                            captionListF.add(captionListM.get(cap_index).toString());
                            dateListF.add(dateListM.get(cap_index).toString());
                        }
                    }

                    else     // if there is no location information for the image, add the pic in list anyways. (time and caption requirement are met). IL,
                    {
                        //filenameListF.add(fwm.getName());
                        //captionListF.add(captionListM.get(cap_index).toString());
                        //dateListF.add(dateListM.get(cap_index).toString());

                    }


                    //      filenameListF.add(fwm.getName()); // if both condition above met (that image file is within user search criteria, add it to the list
                    //      captionListF.add(captionListM.get(cap_index).toString());
                    //      dateListF.add(dateListM.get(cap_index).toString());
                    // We don't need a location list then?, just write a logical that only
                    // increase the filename, caption, and data list of those picutre that are within the geo location IL.
                }
            }
            cap_index++;
        }//end for
        //Pack the filtered lists into a list of lists and return it
        List result = new ArrayList();
        result.add(filenameListF);
        result.add(captionListF);
        result.add(dateListF);
        return result;
    }//end Search()
}//end SearchUtil
