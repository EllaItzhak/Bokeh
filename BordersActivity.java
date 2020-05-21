package com.example.ellai.bokeh2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.media.ExifInterface;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

//import org.opencv.osgi.OpenCVInterface;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static android.support.media.ExifInterface.ORIENTATION_ROTATE_180;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_270;
import static android.support.media.ExifInterface.ORIENTATION_ROTATE_90;

public class BordersActivity extends AppCompatActivity implements OpenCVApp.OpenCVAppInterface {

    public static final int EXIF_TAG_NOT_EXIST = -1;
    public static final String FULL_BODY = "full";
    public static final String UPPER_BODY = "up";
    public static final String LOWER_BODY = "low";
    public static final String PEDESTRAIN = "ped";
    public static final double TIMES_OF_HEAD_IN_BODY = 7.5;

    private OpenCVApp openCVApp;
    private ImageView imgV;
    private FrameLayout frameImg;
    private Bitmap imgBitmap;
    //private MainView myView;
    private int exifSubjectArea;
    private int exifSubjectLocation;
    private int exifSubjectDistanceRange;
    private double exifSubjectDistance;
    private Paint rectPaint;
    private int orientation;
    private int angle;
    private float exifFocalLength;
    private int pixelXDimension;
    private int pixelYDimension;
    private ArrayList<RectF> rectFFound;
    private Canvas canvas;
    private ArrayList<Point> pointsInAllFaces;
    private List<org.opencv.core.Rect> rectList;
    private ArrayList<Bitmap> procBitmaps;
    private Button btnOk;
    private String imgUriString;

    private static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        // If the rotate angle is 0, then return the original image, else return the rotated image
        if (angle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borders);


        Intent intent = getIntent();
        imgUriString = intent.getStringExtra("img");
        Uri uri = Uri.parse(imgUriString);
        getExifFromImage(uri);

        this.procBitmaps = new ArrayList<>();
        imgV = (ImageView)findViewById(R.id.imgV);
        btnOk = (Button)findViewById(R.id.button_ok_rects);

        btnOk.setVisibility(View.GONE);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDataToEditingActivity();
            }
        });

        this.rectPaint = new Paint();
        rectPaint.setStrokeWidth(11);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        this.rectFFound = new ArrayList<RectF>();


        imgBitmap = uriToBitmap(uri);
        pixelXDimension = imgBitmap.getWidth();
        pixelYDimension = imgBitmap.getHeight();

        this.openCVApp = new OpenCVApp(this, this);
        this.openCVApp.setOriginalMat(imgBitmap);
        //this.openCVApp.doBackgroundRemoval(imgBitmap);
//        // try to locate subject from exif info if available
//        if(exifSubjectArea != EXIF_TAG_NOT_EXIST || exifSubjectLocation != EXIF_TAG_NOT_EXIST){
//            findSubjectAuto(true);
//        }

        //this.openCVApp.filter(imgBitmap);
        this.openCVApp.detectObjects(imgBitmap);

       // this.openCVApp.doCanny(imgBitmap);
       //this.openCVApp.doBackgroundRemoval(imgBitmap);

//
        // this.openCVApp.test(imgBitmap);
//        this.openCVApp.detectBodyArea(imgBitmap, FULL_BODY);
//        this.openCVApp.detectBodyArea(imgBitmap, UPPER_BODY);
//        this.openCVApp.detectBodyArea(imgBitmap, LOWER_BODY);
//        this.openCVApp.detectBodyArea(imgBitmap, PEDESTRAIN);
//        this.openCVApp.detectBodyHOG(imgBitmap);

//        myView = new MainView(this, imgBitmap);
//        frameImg = (FrameLayout) findViewB yId(R.id.frameImg);
//        frameImg.addView(myView);

        //findFacesInImg();
//        rectList = this.openCVApp.getRectList();
//        this.openCVApp.drawRects(rectList);
    }


    public void passDataToEditingActivity(){
        Intent intent = new Intent(this, EditingActivity.class);
        //intent.putExtra("imgV", imageUri.toString());
        intent.putExtra("img", this.imgUriString);
        startActivity(intent);
    }

    public void getExifFromImage(Uri uri) {
        InputStream in = null;
        try {

            in = getContentResolver().openInputStream(uri);
            ExifInterface exifInterface = new ExifInterface(in);

            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {
                case ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    angle = 0;
                    break;
            }

            //focal length to calculate distance from camera
            exifFocalLength = exifInterface.getAttributeInt(ExifInterface.TAG_FOCAL_LENGTH, EXIF_TAG_NOT_EXIST);

            //checking exif tags to extract subject's location and distance
            exifSubjectArea = exifInterface.getAttributeInt(ExifInterface.TAG_SUBJECT_AREA, EXIF_TAG_NOT_EXIST);
            exifSubjectLocation = exifInterface.getAttributeInt(ExifInterface.TAG_SUBJECT_LOCATION, EXIF_TAG_NOT_EXIST);
            exifSubjectDistanceRange = exifInterface.getAttributeInt(ExifInterface.TAG_SUBJECT_DISTANCE_RANGE, EXIF_TAG_NOT_EXIST);
            exifSubjectDistance = exifInterface.getAttributeInt(ExifInterface.TAG_SUBJECT_DISTANCE, EXIF_TAG_NOT_EXIST);


            Log.d("test", "" + exifSubjectArea);
            Log.d("test", "" + exifSubjectLocation);
            Log.d("test", "" + exifSubjectDistanceRange);
            Log.d("test", "" + exifSubjectDistance);
            Log.d("test", "" + exifFocalLength);

            // Now you can extract any Exif tag you want
            // Assuming the image is a JPEG or supported raw format
        } catch (IOException e) {
            // Handle any errors
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void findFacesInImg() {

        final Bitmap tempBitmap = imgBitmap.copy(imgBitmap.getConfig(), true);

        final FirebaseVisionImage fbImage = FirebaseVisionImage.fromBitmap(tempBitmap);

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(false)
                        .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result = detector.detectInImage(fbImage)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                // Task completed successfully
                                // ...
                                Canvas canvas = new Canvas(tempBitmap);
                                //saveFacesLandmarks(faces, tempBitmap);
                                for (FirebaseVisionFace face : faces) {
                                    Rect bounds = face.getBoundingBox();
                                    int x1 = bounds.left;
                                    int y1 = bounds.top;
                                    int x2 = bounds.right;
                                    int y2 = bounds.bottom;
                                    RectF rectF = new RectF(x1, y1, x2, y2);

                                    int xx1, yy1, xx2, yy2;
                                    //canvas.drawRoundRect(rectF, 2, 2, rectPaint);

                                    int size = (x2 - x1) * (y2 - y1);
                                    int addToX = (int)(0.7 * (x2 - x1));

                                    float amountOfHeadInBody = (float) TIMES_OF_HEAD_IN_BODY;
                                    int temp = (int) (amountOfHeadInBody * (y2 - y1));

                                    if (y1 + temp > pixelYDimension) {
                                        if ((x1 - addToX) > 0 && (x2 + addToX) < pixelXDimension) {
                                           canvas.drawRect(x1 - addToX, y1, x2 + addToX, pixelYDimension, rectPaint);
                                           // openCVApp.addRectToList(x1 - addToX, y1, x2 + addToX, pixelYDimension);

                                        } else if ((x1 - addToX) > 0 && (x2 - addToX) > pixelXDimension) {
                                            canvas.drawRect(x1 - addToX, y1, pixelXDimension, pixelYDimension, rectPaint);
                                           // openCVApp.addRectToList(x1 - addToX, y1, pixelXDimension, pixelYDimension);

                                        } else if ((x1 - addToX) < 0 && (x2 - addToX) < pixelXDimension) {
                                            canvas.drawRect(0, y1, x2 + addToX, pixelYDimension, rectPaint);
                                            //openCVApp.addRectToList(0, y1, x2 + addToX, pixelYDimension);

                                        }
                                    } else {
                                        if ((x1 - addToX) > 0 && (x2 + addToX) < pixelXDimension) {
                                            canvas.drawRect(x1 - addToX, y1, x2 + addToX, y1 + temp, rectPaint);
                                            //openCVApp.addRectToList(x1 - addToX, y1, x2 + addToX, y1 + temp);

                                        } else if ((x1 - addToX) > 0 && (x2 - addToX) > pixelXDimension) {
                                            canvas.drawRect(x1 - addToX, y1, pixelXDimension, y1 + temp, rectPaint);
                                            //openCVApp.addRectToList(x1 - addToX, y1, pixelXDimension, y1 + temp);

                                        } else if ((x1 - addToX) < 0 && (x2 - addToX) < pixelXDimension) {
                                            canvas.drawRect(0, y1, x2 + addToX, y1 + temp, rectPaint);
                                            //openCVApp.addRectToList(0, y1, x2 + addToX, y1 + temp);

                                        }
                                        canvas.drawRect(x1 - addToX, y1, x2 + addToX, y1 + temp, rectPaint);
                                        //openCVApp.addRectToList(x1 - addToX, y1, x2 + addToX, y1 + temp);

                                    }
                                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                    // nose available):
                                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                    if (leftEar != null) {
                                        FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                        drawPoint(canvas, leftEar.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);
                                    if (rightEar != null) {
                                        FirebaseVisionPoint rightEarPos = rightEar.getPosition();
                                        drawPoint(canvas, rightEar.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                    if (leftEye != null) {
                                        FirebaseVisionPoint leftEyePos = leftEye.getPosition();
                                        drawPoint(canvas, leftEye.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                    if (rightEye != null) {
                                        FirebaseVisionPoint rightEyePos = rightEye.getPosition();
                                        drawPoint(canvas, rightEye.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark leftCheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
                                    if (leftCheek != null) {
                                        FirebaseVisionPoint leftCheekPos = leftCheek.getPosition();
                                        drawPoint(canvas, leftCheek.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark rightCheek = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK);
                                    if (rightCheek != null) {
                                        FirebaseVisionPoint rightCheekPos = rightCheek.getPosition();
                                        drawPoint(canvas, rightCheek.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_MOUTH);
                                    if (leftMouth != null) {
                                        FirebaseVisionPoint leftMouthPos = leftMouth.getPosition();
                                        drawPoint(canvas, leftMouth.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_MOUTH);
                                    if (rightMouth != null) {
                                        FirebaseVisionPoint rightMouthPos = rightMouth.getPosition();
                                        drawPoint(canvas, rightMouth.getPosition());
                                    }
                                    FirebaseVisionFaceLandmark noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                    if (noseBase != null) {
                                        FirebaseVisionPoint noseBasePos = noseBase.getPosition();
                                        drawPoint(canvas, noseBase.getPosition());
                                    }
                                    imgV.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

                                }//for faces
                            }//on success
                        })//listener
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
    }//findFacesInImg

    private void drawPoint(Canvas canvas, FirebaseVisionPoint point) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);

        float x = point.getX();
        float y = point.getY();

        canvas.drawCircle(x, y, 5, paint);
    }

    public void findSubjectAuto(boolean isExifLocationAreaExist) {


    }

    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                image = rotateBitmap(image, angle);
                return image;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void callbackSuccessful(Bitmap bitmap, OpenCVApp.RequestType type) {
        if (type == OpenCVApp.RequestType.FULL_BODY_DETECTION_SUCCESS || type == OpenCVApp.RequestType.UPPER_BODY_DETECTION_SUCCESS
                || type == OpenCVApp.RequestType.LOWER_BODY_DETECTION_SUCCESS || type == OpenCVApp.RequestType.PEDESTRIAN_DETECTION_SUCCESS) {
            imgV.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
            btnOk.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void callbackSuccessfulNew(List<org.opencv.core.Rect> rectList, OpenCVApp.RequestType type) {

        if (type == OpenCVApp.RequestType.FULL_BODY_DETECTION_SUCCESS || type == OpenCVApp.RequestType.UPPER_BODY_DETECTION_SUCCESS
                || type == OpenCVApp.RequestType.LOWER_BODY_DETECTION_SUCCESS || type == OpenCVApp.RequestType.PEDESTRIAN_DETECTION_SUCCESS
                || type == OpenCVApp.RequestType.HOG_DETECTION_SUCCESS) {
            openCVApp.drawRects(rectList);
            btnOk.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void callBackFailed(OpenCVApp.RequestType type) {
        imgV.setImageDrawable(new BitmapDrawable(getResources(), imgBitmap));
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_borders, menu);
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.nextButton:
//                Intent intent = new Intent(this, EditingActivity.class);
//                intent.putExtra("imgBitmap", imgBitmap);
//                startActivity(intent);
//                break;
//            case R.id.oneBackButton:
//
//                break;
//            case R.id.rotateButton:
//                imgBitmap = rotateBitmap(imgBitmap, 90);
//                imgV.setImageDrawable(new BitmapDrawable(getResources(), imgBitmap));
//                findFaceInImg();
//                break;
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void callbackSuccessful(Bitmap bitmap, OpenCVApp.RequestType type) {
//        if(type == OpenCVApp.RequestType.FULL_BODY_DETECTION_SUCCESS || type == OpenCVApp.RequestType.UPPER_BODY_DETECTION_SUCCESS
//                || type == OpenCVApp.RequestType.LOWER_BODY_DETECTION_SUCCESS){
//            imgV.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
//        }
//    }
//
//    @Override
//    public void callBackFailed(OpenCVApp.RequestType type) {
//
//    }

}
