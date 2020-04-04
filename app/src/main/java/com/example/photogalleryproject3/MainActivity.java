//To get a picture with the GPS info in the Exif Tags, do the following:
//- enable Location permissions for phone
//- enable Location permissions for camera app
//- in the camera app, go to Settings and enable Save Location
//- either go outside and wait until you get a GPS signal, or connect to wifi
//- take a picture using this app
//If a picture does not have the GPS info in the Exif Tags, "no location info" is displayed

package com.example.photogalleryproject3;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import Utils2.*; //Utility class containing helpful functions for Photo Gallery app
import SearchUtil.*; //Utility class containing search function for Photo Gallery app

public class MainActivity extends AppCompatActivity
{
    private static final String AndroidUploadServletURL = "http://192.168.1.72:8082/AndroidUploadServlet/upload";
    private HttpURLConnection urlConnection;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    public static final int SCAN_ACTIVITY_REQUEST_CODE = 4;
    public static final int VIEW_FOLDER_REQUEST_CODE = 2;
    public static final int ADD_TO_FOLDER_REQUEST_CODE = 3;
    private static final int BLANK_SCREEN = -1; //used with Go() function to tell it to go to a blank screen
    /////////////////////////////////////IL
    private String returnStartTime;     // 2 global variables that stores the time from 2nd activity
    private String returnEndTime;
    /////////////////////////////////////IL
    private String mCurrentPhotoPath;
    private String currentFileName = null; //only used to save the filename of the new picture to add to file name list
    private Date CurrentDate = null;       //only used to save the date of the new picture to add to date list
    //Master lists. These are used to keep track of all files, captions, and dates.
    private List captionListM = new ArrayList();
    private List filenameListM = new ArrayList();
    private List dateListM = new ArrayList(); ////<Date>(); //WM
    //Filtered lists. These are used to keep track of which content is to be displayed.
    private List captionListF = new ArrayList();
    private List filenameListF = new ArrayList();
    private List dateListF = new ArrayList(); /////<Date>(); //WM
    //Folder List.
    private List folderList = new ArrayList();
    //The element number of the current image. Refers to the element number in the FILTERED list.
    private int currentElement = 0;
    //Instantiate the utility classes that provide helpful functions for this app
    private Utils2 U = new Utils2();
    private SearchUtil S = new SearchUtil();
    //============================================================================================================================

    private void displayPhoto(String path) {
        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        iv.setImageBitmap(BitmapFactory.decodeFile(path));
    }
    //============================================================================================================================

    private List populateGallery() {         // getting photos from storage on phone, put them in to the photo gallery
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogalleryproject3/files/Pictures"); // put in our project name then it should work
        File[] fList = file.listFiles();
        List fl = new ArrayList();
        if (fList != null) {
            for (File f : file.listFiles()) {
                fl.add(f.getName());
            }
        }
        return fl;   // this is our filenamelist from before
    }
    //============================================================================================================================

    @Override
    public void onResume() {
        super.onResume();
    }

    //============================================================================================================================

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //On first run, create files to save captions and dates. Also get the filenames.
        File captionFile = U.GetFile(MainActivity.this, "captions");
        File dateFile = U.GetFile(MainActivity.this, "dates");
        //Populate master lists from files at startup of this activity
        filenameListM = populateGallery();
        captionListM = U.PopulateList(captionFile);
        dateListM = U.PopulateList(dateFile);
        //Clear filters
        filenameListF = U.copy(filenameListM);
        captionListF = U.copy(captionListM);
        dateListF = U.copy(dateListM);
        //Go to the first picture, if there is one
        if (filenameListF.size() > 0)
            Go(0);
            //Else, go to blank screen
        else
            Go(BLANK_SCREEN);
        //Hide upload status initially
        TextView textViewUpload = (TextView) findViewById(R.id.textViewUpload);
        textViewUpload.setText("");
        //Initialize Folder List
        List temp1 = new ArrayList();
        List temp2 = new ArrayList();
        List temp3 = new ArrayList();
        temp1.add("Folder 1"); //The first element in each sublist will be the folder name. The rest will be the filenames
        temp2.add("Folder 2");
        temp3.add("Folder 3");
        folderList.add(temp1); //Folder 1
        folderList.add(temp2); //Folder 2
        folderList.add(temp3); //Folder 3
    }
    //============================================================================================================================

    public void search(View view) {
        Intent intent = new Intent(this, Search.class);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }
    //============================================================================================================================
    public void scan(View view) {
        Intent intent = new Intent(this, QRScan.class);
        startActivityForResult(intent, SCAN_ACTIVITY_REQUEST_CODE);
    }
    //============================================================================================================================
    public void takePicture(View v)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogalleryproject3.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    //============================================================================================================================

    public File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("createImageFile", mCurrentPhotoPath);
        currentFileName = image.getName(); //Added WM to get the filename, for adding to filenameList.
        CurrentDate = new Date(image.lastModified());//for adding to dateList.
        return image;
    }
    //============================================================================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Do this if user took a picture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            //Update master lists with the new picture
            filenameListM.add(currentFileName);
            captionListM.add("Enter Caption");
            dateListM.add(CurrentDate.toString());           ////CurrentDate); //WM
            //Write master caption and date lists to files
            U.SaveToFile(MainActivity.this, captionListM, "captions");
            U.SaveToFile(MainActivity.this, dateListM, "dates");


            // add the photos with faces automatically into folder 1-------------IL

            // -----------find out the number of face in each photo ------------------- IL
            int x = faceRecognitionV2(mCurrentPhotoPath);
            // ----------adding the photo into folder 1 ----------------------- IL
            if (x>0)
            {
                DoAddToFolder(currentFileName, "Folder 1");

            }


            //Clear filters
            filenameListF = U.copy(filenameListM);
            captionListF = U.copy(captionListM);
            dateListF = U.copy(dateListM);
            //Go to the new picture
            Go(filenameListF.size()-1);
        }//end do this if user took a picture

        //Do this if user searched
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            // getting the result back from 2nd activity
            String get_caption = data.getStringExtra("CAPTION");
            returnStartTime = data.getStringExtra("STARTDATE");
            returnEndTime = data.getStringExtra("ENDDATE");

            String lat1 = data.getStringExtra("LAT_FROM"); //these will hold the user-entered latitude and longitude
            String lng1 = data.getStringExtra("LONG_FROM");
            String lat2 = data.getStringExtra("LAT_TO");
            String lng2 = data.getStringExtra("LONG_TO"); //depending on the search logic these two might not be needed

            //Clear filtered lists in preparation for receiving the results from Search()
            filenameListF.clear();
            captionListF.clear();
            dateListF.clear();
            //Search!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            List result = new ArrayList();
            result = S.Search(MainActivity.this,get_caption,returnStartTime,returnEndTime,lat1,lng1,lat2,lng2,captionListM,dateListM);
            //Unpack result
            filenameListF = (List) result.get(0);
            captionListF = (List) result.get(1);
            dateListF = (List) result.get(2);
            //If the search is cleared, re-enable the snap button
            Button button = findViewById(R.id.btnSnap);
            if(get_caption.isEmpty())
                button.setClickable(true);
                //Otherwise disable the snap button. To prevent crashes
            else
                button.setClickable(false);
            //If the search returned something, go to the first image in the filtered list
            if(captionListF.size() != 0)
                Go(0);
            else //go to a blank screen
                Go(BLANK_SCREEN);
        }//end do this if user searched

        //Do this if user pressed "Folders" to view a folder
        if (requestCode == VIEW_FOLDER_REQUEST_CODE && resultCode == RESULT_OK)
        {
            //If the folder that was chosen is "All" then just clear the filters
            String folderName = data.getStringExtra("FOLDER");
            if(folderName.equals("All"))
            {
                filenameListF = U.copy(filenameListM);
                captionListF = U.copy(captionListM);
                dateListF = U.copy(dateListM);
            }
            else
            {
                //Navigate to the folder that was chosen
                String folderNameRead = "null";
                int index;
                for (index = 0; index < folderList.size() && !folderNameRead.equals(folderName); index++)
                    folderNameRead = ((ArrayList) folderList.get(index)).get(0).toString();
                index--; //I assume there is at least one folder.
                List currentFolder = new ArrayList();
                currentFolder = (ArrayList) folderList.get(index);

                //Set filtered lists to the files in that folder
                filenameListF.clear();
                captionListF.clear();
                dateListF.clear();
                for (int i = 1; i < currentFolder.size(); i++) {
                    //Filename
                    String filename = currentFolder.get(i).toString();
                    filenameListF.add(filename);

                    //Preparatory work for date and caption (determine current file index in master lists)
                    String fileNameRead = "null";
                    int fileIndex;
                    for (fileIndex = 0; fileIndex < filenameListM.size() && !fileNameRead.equals(filename); fileIndex++)
                        fileNameRead = filenameListM.get(fileIndex).toString();
                    fileIndex--;

                    //Caption
                    String caption = captionListM.get(fileIndex).toString();
                    captionListF.add(caption);

                    //Date
                    String date = dateListM.get(fileIndex).toString();
                    dateListF.add(date);
                }
            }//end else

            //Go to the first picture, if any, in that folder
            if(filenameListF.size() > 0)
            {
                currentElement = 0;
                Go(currentElement);
            }
            else
                Go(BLANK_SCREEN);
        }//end "do this if user pressed "Folders"

        //Do this if user pressed "Add to Folder" to add the picture to a folder
        if (requestCode == ADD_TO_FOLDER_REQUEST_CODE && resultCode == RESULT_OK)
        {
            //Get filename of picture to add, and folder name of folder to add it to
            String filename = filenameListF.get(currentElement).toString();
            String folderName = data.getStringExtra("FOLDER");

            //Only if the user didn't choose "All"!!!
            if(!folderName.equals("All"))
                DoAddToFolder(filename, folderName);
        }//end "do this if user pressed "Add to Folder"
    }
    //============================================================================================================================

    //Function to simplify adding a picture to a folder.
    private void DoAddToFolder(String filename, String foldername)
    {
        //Navigate to the folder that was chosen
        String folderNameRead = "null";
        int index;
        for (index = 0; index < folderList.size() && !folderNameRead.equals(foldername); index++)
            folderNameRead = ((ArrayList)folderList.get(index)).get(0/*Folder name*/).toString();
        index--; //I assume there is at least one folder.

        //Add the current picture to the folder
        ((ArrayList)folderList.get(index)).add(filename);
    }
    //=============================================================================================================================

    public void saveCaption(View view)
    {
        if(mCurrentPhotoPath != null) //If there is an image in the imageview
        {
            //Get the caption
            TextView textView = (TextView) findViewById(R.id.editTextCaption);
            String caption = textView.getText().toString();
            //Change the caption for the current image in the filtered list
            captionListF.set(currentElement, caption);
            //Change the caption for the current image in the master list
            String filename = filenameListF.get(currentElement).toString();
            for(int i = 0; i < filenameListM.size(); i++)
            {
                if(filenameListM.get(i).toString().contains(filename))
                {
                    captionListM.set(i, caption);
                    i = filenameListM.size();//exit loop
                }
            }
            //Update caption file
            U.SaveToFile(MainActivity.this, captionListM, "captions");
        }
    }
    //============================================================================================================================

    //Move to the newer image
    public void Left(View view)
    {
        //See if the number of images is greater than 1.
        int filenameListSize = filenameListF.size();
        if(filenameListSize > 1)
        {
            //See if the current image is an older image.
            //The current image is an older image if currentElement is not the last element number.
            if(currentElement != (filenameListSize - 1))
                Go(currentElement+1); //Go to the newer image
        }
    }
    //============================================================================================================================

    //Display the older image
    public void Right(View view)
    {
        //See if the number of images is greater than 1.
        int filenameListSize = filenameListF.size();
        if(filenameListSize > 1)
        {
            //See if the current image is a newer image.
            //The current image is a newer image if currentElement is not the first element number.
            if(currentElement != 0)
                Go(currentElement-1); //Go to the newer image
        }
    }
    //============================================================================================================================

    public void Delete(View view)
    {
        //Only delete if there's at least one picture
        if(filenameListF.size() > 0)
        {
            //0. Delete current picture from all folders that it's in
            for(int i = 0; i < folderList.size(); i++)
            {
                int size = ((ArrayList)folderList.get(i)).size();
                for(int j = 1; j < size; j++)
                {
                    if(((ArrayList)folderList.get(i)).get(j).toString().equals(filenameListF.get(currentElement).toString()))
                    {
                        ((ArrayList) folderList.get(i)).remove(j);
                        size--;
                        j--;
                    }
                }
            }
            //1. Delete current picture from the file system
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + filenameListF.get(currentElement).toString());
            f.delete();
            //2. Delete current picture from master filename list
            String s = filenameListF.get(currentElement).toString();
            int i = filenameListM.indexOf(s);
            filenameListM.remove(i);
            //3. Delete current picture from master caption list
            s = captionListF.get(currentElement).toString();
            i = captionListM.indexOf(s);
            captionListM.remove(i);
            //4. Delete current picture from master date list
            s = dateListF.get(currentElement).toString();
            i = dateListM.indexOf(s);
            dateListM.remove(i);
            //5. Delete current picture from filtered filename list
            filenameListF.remove(currentElement);
            //6. Delete current picture from filtered caption list
            captionListF.remove(currentElement);
            //7. Delete current picture from filtered date list
            dateListF.remove(currentElement);
            //8. Delete current picture from the caption file
            U.SaveToFile(MainActivity.this, captionListM, "captions");
            //9. Delete current picture from the date file
            U.SaveToFile(MainActivity.this, dateListM, "dates");
            //10. Go to a different picture, or a blank screen if no pictures left
            if (filenameListF.size() == 0)
            {
                //Don't worry about currentElement, it will get set correctly when a picture is taken.
                Go(BLANK_SCREEN);
            }
            else if (currentElement >= filenameListF.size())
            {
                currentElement--;
                Go(currentElement);
            }
            else
            {
                //currentElement is in bounds so leave it as is
                Go(currentElement);
            }
        }//end if
    }//end Delete
    //============================================================================================================================

    public void ViewFolder(View view)
    {
        Intent intent = new Intent(this, Folder.class);
        startActivityForResult(intent, VIEW_FOLDER_REQUEST_CODE);
    }
    //============================================================================================================================

    public void AddToFolder(View view)
    {
        //Only if there is a current picture!
        if(filenameListF.size() > 0)
        {
            Intent intent = new Intent(this, Folder.class);
            startActivityForResult(intent, ADD_TO_FOLDER_REQUEST_CODE);
        }
    }
    //============================================================================================================================

    //Goes to the specified element in the filtered list. -1 means go to blank screen.
    public void Go(int element)
    {
        if(element != BLANK_SCREEN)
        {
            //1. Specify that the given image is the current image
            currentElement = element;
            //2. Set the current filename to the filename of the given image
            mCurrentPhotoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/"+filenameListF.get(currentElement).toString();

            //3. Display the given image in the correction orientation --------- IL
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
            mImageView.setImageBitmap(CorrectPhotoRotation(mCurrentPhotoPath));   // using the function that corrects the orientation IL
            //4. Set the caption to the caption of the given image
            TextView textView = (TextView) findViewById(R.id.editTextCaption);
            textView.setText((CharSequence) captionListF.get(currentElement));
            //5. Set the date to the date of the given image
            TextView textViewforDate = findViewById(R.id.DatetextView);
            textViewforDate.setText((CharSequence) dateListF.get(currentElement).toString());

            //6. Find out the number of faces in the photo--------------IL
            //int x = faceRecognitionV2(mCurrentPhotoPath);
            //7. -display the number of face detected if any. ----------------IL
            //TextView textViewforFaceNum = findViewById(R.id.FaceNum_TextView);
            //textViewforFaceNum.setText("number of face : " + String.valueOf(x));


            //8 . Set the location information to the location information of the given image
            float[] f = {0,0};   // the two values are stored here temporaely, long, lat
            boolean result = false;
            try {
                String filename = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/"+filenameListF.get(currentElement).toString();
                ExifInterface exif = new ExifInterface(filename);
                result = exif.getLatLong(f);               // does it get both lat and long and put it in the list?? this function "getLatLong()" take what kind of input ? IL
            }
            catch (IOException e) { }
            TextView tv = (TextView) findViewById(R.id.gpsTextView);
            if(result == true)
                tv.setText(String.valueOf(f[0]) + " " + String.valueOf(f[1]));  // displaying on screen, converting the first and second value (result) into string
            else
                tv.setText("No location information");
        }
        //
        else //Go to blank screen
        {
            //1. There is no current element so leave currentElement as is
            //2. Set the current filename to null
            mCurrentPhotoPath = null;
            //3. Display nothing
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
            mImageView.setImageDrawable(null);
            //4. Set the caption to "no files found"
            TextView textView = (TextView) findViewById(R.id.editTextCaption);
            textView.setText("No files found");
            //5. Set the date to "No date information"
            TextView textViewforDate = findViewById(R.id.DatetextView);
            textViewforDate.setText("No date information");
            //6. Set the location information to "No location information"
            TextView textViewforLocation = findViewById(R.id.gpsTextView);
            textViewforLocation.setText("No location information");
        }
    }
    //============================================================================================================================

    //Share the image
    public void Share(View view)
    {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogalleryproject3/files/Pictures"); // put in our project name then it should work

        String photoname = "/storage/emulated/0/Android/data/com.example.photogalleryproject3/files/Pictures/" + filenameListF.get(currentElement).toString();

        Uri share_photoURI = Uri.parse(photoname);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        //shareIntent.putExtra(Intent.EXTRA_TEXT, ((EditText) findViewById(R.id.etCaption)).getText().toString());
        //.putExtra(Intent.EXTRA_SUBJECT, "" + ((EditText) findViewById(R.id.etCaption)).getText().toString());
        //File file = new File(photos.get(index));
        shareIntent.putExtra(Intent.EXTRA_STREAM, share_photoURI);
        shareIntent.setType("image/*");
        shareIntent.setPackage("com.facebook.katana");
        //shareIntent.setPackage("com.discord");
        startActivity(Intent.createChooser(shareIntent, "Share image to..."));


    }

    //============================================================================================================================

    //Upload the image to tomcat server
    public void Upload(View view)
    {
        //Disable upload button until upload complete (server can currently only handle one upload at a time)
        Button button = findViewById(R.id.btnUpload);
        button.setClickable(false);
        //Update status
        TextView textViewUpload = (TextView) findViewById(R.id.textViewUpload);
        textViewUpload.setText("Upload in progress");
        //Start upload task
        String filename;
        filename = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + filenameListF.get(currentElement).toString();
        UploadTask uploadTask = new UploadTask();
        uploadTask.execute(AndroidUploadServletURL, filename);
    }

    private class UploadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)//[0] = server URL, [1] = filename
        {
            //Variable declarations, initializations
            String response = "Initial value";
            FileInputStream fin = null;
            OutputStream outputStream = null;
            long fileSize = 0;
            long maxFileSize = 10485760; //10 MB

            //Determine file size, make sure it's not too big or too small
            try
            {
                File testfile = new File(params[1]);
                fileSize = (long) (testfile.length());
                if (fileSize > maxFileSize)
                    return "File too large";
                else if(fileSize <= 0)
                    return "No file";
            }
            catch (Exception e)
            {
                return "Error opening file";
            }

            //Send file in a POST message to the android upload servlet
            try
            {
                //Get URL connection
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                //Set timeout
                //https://docs.oracle.com/javase/8/docs/api/java/net/URLConnection.html
                urlConnection.setConnectTimeout(60000); //Allow 1 minute max to connect to URL
                urlConnection.setReadTimeout(900000); //Allow 15 minutes max to transfer data

                //Enable connection output
                //https://stackoverflow.com/questions/44305351/send-data-in-request-body-using-httpurlconnection
                //The method is inherited from URLConnection, that's why it doesn't show up in the docs for HttpURLConnection
                urlConnection.setDoOutput(true);

                //Set method to POST
                urlConnection.setRequestMethod("POST");

                //THIS LINE IS CRITICAL IN ANDROID. WITHOUT THIS LINE, THE POST WILL NOT BE SENT.
                //I got this from here:
                //https://blog.codavel.com/how-to-integrate-httpurlconnection
                //It is mentioned in the Android documentation, but they incorrectly say it's optional.
                //https://developer.android.com/reference/java/net/HttpURLConnection
                urlConnection.setChunkedStreamingMode(0);

                //Add some key-value pairs to the POST message
                //https://developer.android.com/reference/java/net/URLConnection#addRequestProperty(java.lang.String,%20java.lang.String)
                urlConnection.addRequestProperty("Caption", captionListF.get(currentElement).toString()); //Date and Location are in the Exif tags
                urlConnection.addRequestProperty("FileSize", String.valueOf(fileSize)); //So that the server knows the file size

                //Get output stream
                outputStream = urlConnection.getOutputStream();
                if (outputStream == null)
                    return "Could not get output stream";

                //Write entire file to output stream
                fin = new FileInputStream(params[1]);
                int fileByte = 0;
                fileByte = fin.read();
                long counter = 0;
                while(fileByte >= 0 && counter < fileSize)
                {
                    outputStream.write(fileByte);
                    fileByte = fin.read();
                    counter++;
                }
                //Important! Got this from here:
                //https://blog.codavel.com/how-to-integrate-httpurlconnection
                outputStream.flush();

                //Receive confirmation response from server
                InputStream in = null;
                int valueRead = 0;
                char charRead = 0;
                String ResponseString = "";
                try {
                    in = urlConnection.getInputStream();
                    valueRead = in.read();
                    for(counter = 0; (counter < 30 && valueRead != -1); counter++)
                    {
                        charRead = (char)valueRead;
                        ResponseString += String.valueOf(charRead);
                        valueRead = in.read();
                    }
                    if(ResponseString.contains("ok"))
                        response = "Upload complete";
                    else //something from the server other than ok
                        response = ResponseString;
                }
                catch (Exception e)
                {
                    response = "No reply, not sure if upload OK";
                }
                finally
                {
                    try{ in.close(); } catch(Exception e) {}
                }
            }
            catch (Exception e)
            {
                response = "Exception during file upload";
            }
            finally
            {
                //This line: urlConnection.disconnect(); was causing an exception on the server,
                //"Unexpected EOF read on socket". So I close the connection in OnDestroy instead.
                try{ fin.close(); } catch (Exception e){}
                try{ outputStream.close(); } catch (Exception e){}
            }
            return response;
        }//end doInBackground

        @Override
        protected void onPostExecute(String result)
        {
            //Re-enable upload button
            Button button = findViewById(R.id.btnUpload);
            button.setClickable(true);
            //Update status
            TextView textViewUpload = (TextView) findViewById(R.id.textViewUpload);
            textViewUpload.setText(result);
        }
    }//end UploadTask

    @Override
    protected void onDestroy ()
    {
        try { urlConnection.disconnect(); } catch (Exception e){}
        super.onDestroy();
    }



    // ----------------face recognition -------------------------- IL
    private int faceRecognitionV2(String imagePath) {
        // load image file to bitmap
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap_image = CorrectPhotoRotation(imagePath);
        // detect face
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        Frame frame = new Frame.Builder().setBitmap(bitmap_image).build();
        SparseArray<Face> faces = detector.detect(frame);

        return faces.size();
        // faces.size();
    }


    // --------------------rotate photo --------------------------- IL
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    // ---------------- determine the correction photo orientation and return the bitmap-----------IL
    private Bitmap CorrectPhotoRotation(String photoPath)
    {
        int orientation = 0;
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException ex) {

            Toast.makeText(this, "Orientation detection failed", Toast.LENGTH_SHORT).show();
            // Error occurred while creating the File
        }

        // load image file to bitmap
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);



        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateBitmap(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateBitmap(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateBitmap(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;
    }






}//end MainActivity


