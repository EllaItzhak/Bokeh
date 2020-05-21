package com.example.ellai.bokeh2;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.opencv.utils.Converters.vector_KeyPoint_to_Mat;

public class OpenCVApp extends AppCompatActivity   {
    private static final String TAG = "OpenCV::Activity";
    public static final int MIN_WIDTH_HOG = 400;
    public static final int MIN_WIDTH_BODY = 1300;
    public static final float SCORE_TRESHOLD = 0.65f;
    public static final String PEDESTRAIN = "ped";

    //private EventTypeImg bordersActivity;
    private OpenCVAppInterface activityInter;
    private Context activityContext;
    private CascadeClassifier fullBodyDetector;
    private CascadeClassifier pedestrianDetector;
    private MatOfRect matOfBodyDetections;
    private CascadeClassifier upperBodyDetector;
    private CascadeClassifier lowerBodyDetector;
    private Mat image;
    private Bitmap bmpBodyDetect;
    private boolean isDetectBodyArea = false;
    private Mat original;
    private Mat mat;
    private File cascadeFileFB;
    private File cascadeFileUB;
    private File cascadeFileLB;
    private File cascadeFileP;
    private List<Rect> rectList;
    private static final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};
    private Net net;
    private CameraBridgeViewBase mOpenCvCameraView;





    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(OpenCVApp.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /////////////////////////////////

                    try {

                        // load cascade file from application resources
//
//                        // // --------------------------------- load Full Body
//                        // classificator -----------------------------------
//                        InputStream is = activityContext.getResources().openRawResource(R.raw.haarcascade_fullbody);
//                        File cascadeDir = activityContext.getDir("cascade", Context.MODE_PRIVATE);
//                        cascadeFileFB = new File(cascadeDir, "haarcascade_fullbody.xml");
//                        FileOutputStream os = new FileOutputStream(cascadeFileFB);
//
//                        byte[] buffer = new byte[4096];
//                        int bytesRead;
//                        while ((bytesRead = is.read(buffer)) != -1) {
//                            os.write(buffer, 0, bytesRead);
//                        }
//                        is.close();
//                        os.close();
//
//                        fullBodyDetector = new CascadeClassifier(cascadeFileFB.getAbsolutePath());
//                        //must add this line
//                        fullBodyDetector.load(cascadeFileFB.getAbsolutePath());
//
//                        if (fullBodyDetector.empty()) {
//                            Log.e(TAG, "Failed to load cascade classifier");
//                            fullBodyDetector = null;
//                        } else {
//                            Log.i(TAG, "Loaded cascade classifier from " + cascadeFileFB.getAbsolutePath());
//                            cascadeDir.delete();
//
//                        }
//
//                        // --------------------------------- load Upper Body
//                        // classificator -----------------------------------
//                        InputStream isUB = activityContext.getResources().openRawResource(R.raw.haarcascade_upperbody);
//                        File cascadeDirUB = activityContext.getDir("cascadeUR", Context.MODE_PRIVATE);
//                        cascadeFileUB = new File(cascadeDirUB, "haarcascade_upperbody.xml");
//                        FileOutputStream osUB = new FileOutputStream(cascadeFileUB);
//
//                        byte[] bufferUB = new byte[4096];
//                        int bytesReadUB;
//                        while ((bytesReadUB = isUB.read(bufferUB)) != -1) {
//                            osUB.write(bufferUB, 0, bytesReadUB);
//                        }
//                        isUB.close();
//                        osUB.close();
//
//
//                        upperBodyDetector = new CascadeClassifier(cascadeFileUB.getAbsolutePath());
//                        //must add this line
//                        upperBodyDetector.load(cascadeFileUB.getAbsolutePath());
//
//                        if (upperBodyDetector.empty()) {
//                            Log.e(TAG, "Failed to load cascade classifier");
//                            upperBodyDetector = null;
//                        } else {
//                            Log.i(TAG, "Loaded cascade classifier from " + cascadeFileUB.getAbsolutePath());
//                        }
//
//
//                        // --------------------------------- load Lower Body
//                        // classificator -----------------------------------
//                        InputStream isLB = activityContext.getResources().openRawResource(R.raw.haarcascade_lowerbody);
//                        File cascadeDirLB = activityContext.getDir("cascadeLB", Context.MODE_PRIVATE);
//                        cascadeFileLB = new File(cascadeDirLB, "haarcascade_lowerbody.xml");
//                        FileOutputStream osLB = new FileOutputStream(cascadeFileLB);
//
//                        byte[] bufferLB = new byte[4096];
//                        int bytesReadLB;
//                        while ((bytesReadLB = isLB.read(bufferLB)) != -1) {
//                            osLB.write(bufferLB, 0, bytesReadLB);
//                        }
//                        isLB.close();
//                        osLB.close();
//
//                        lowerBodyDetector = new CascadeClassifier(cascadeFileLB.getAbsolutePath());
//                        //must add this line
//                        lowerBodyDetector.load(cascadeFileLB.getAbsolutePath());
//
//                        if (lowerBodyDetector.empty()) {
//                            Log.e(TAG, "Failed to load cascade classifier");
//                            lowerBodyDetector = null;
//                        } else {
//                            Log.i(TAG, "Loaded cascade classifier from " + cascadeFileLB.getAbsolutePath());
//                        }
//
//                        // --------------------------------- load Pedestrian
//                        // classificator -----------------------------------
//                        InputStream isP = activityContext.getResources().openRawResource(R.raw.haarcascade_pedestrian);
//                        File cascadeDirP = activityContext.getDir("cascadeP", Context.MODE_PRIVATE);
//                        cascadeFileP = new File(cascadeDirP, "haarcascade_pedestrian.xml");
//                        FileOutputStream osP = new FileOutputStream(cascadeFileP);
//
//                        byte[] bufferP = new byte[4096];
//                        int bytesReadP;
//                        while ((bytesReadP = isP.read(bufferP)) != -1) {
//                            osP.write(bufferP, 0, bytesReadP);
//                        }
//                        isP.close();
//                        osP.close();
//
//
//                        pedestrianDetector = new CascadeClassifier(cascadeFileP.getAbsolutePath());
//                        //must add this line
//                        pedestrianDetector.load(cascadeFileP.getAbsolutePath());
//                        Log.d(TAG, cascadeFileP.getAbsolutePath());
//
//                        if (pedestrianDetector.empty()) {
//                            Log.e(TAG, "Failed to load cascade classifier");
//                            pedestrianDetector = null;
//                        } else {
//                            Log.i(TAG, "Loaded cascade classifier from " + cascadeFileP.getAbsolutePath());
//                        }
                        //////////// end pedestrian

                        InputStream isA = activityContext.getAssets().open("MobileNetSSD_deploy.prototxt");
                        File cascadeDirA = activityContext.getDir("cascadeA", Context.MODE_PRIVATE);
                        File cascadeFileA = new File(cascadeDirA, "MobileNetSSD_deploy.prototxt");
                        FileOutputStream osA = new FileOutputStream(cascadeFileA);

                        byte[] bufferA = new byte[4096];
                        int bytesReadA;
                        while ((bytesReadA = isA.read(bufferA)) != -1) {
                            osA.write(bufferA, 0, bytesReadA);
                        }
                        isA.close();
                        osA.close();

                        String proto = cascadeFileA.getAbsolutePath();

                        InputStream isB = activityContext.getAssets().open("MobileNetSSD_deploy.caffemodel");
                        File cascadeDirB = activityContext.getDir("cascadeB", Context.MODE_PRIVATE);
                        File cascadeFileB = new File(cascadeDirB, "/MobileNetSSD_deploy.caffemodel");
                        FileOutputStream osB = new FileOutputStream(cascadeFileB);

                        byte[] bufferB = new byte[4096];
                        int bytesReadB;
                        while ((bytesReadB = isB.read(bufferB)) != -1) {
                            osB.write(bufferB, 0, bytesReadB);
                        }
                        isB.close();
                        osB.close();

                        String weights = cascadeFileB.getAbsolutePath();

                        net = Dnn.readNetFromCaffe(proto, weights);


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ////////////////////////////////
                }
                break;
                default: {
                    Log.i(TAG, "OpenCV N=O=T loaded successfully");

                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public OpenCVApp(OpenCVAppInterface bordersActivity, Context context) {
        this.activityInter = bordersActivity;
        this.activityContext = context;
        this.rectList = new ArrayList<Rect>();

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            boolean is = OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, OpenCVApp.this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void setOriginalMat(Bitmap bitmap){
        this.original = bitmapToMat(bitmap);
    }

    public void detectBodyHOG(Bitmap bitmap){

         Mat matColor = bitmapToMat(bitmap);
         Mat mat = bitmapToMat(bitmap);

        float r = (float)MIN_WIDTH_HOG / mat.width();
        float h = (float)r * mat.height();
        Size res = new Size(MIN_WIDTH_HOG,h);
        Imgproc.resize(mat, mat, res);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);

        final HOGDescriptor hog = new HOGDescriptor();
        Size s= hog.get_winSize();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
        final MatOfRect foundLocations = new MatOfRect();
        final MatOfDouble foundWeights = new MatOfDouble();
        final Size winStride = new Size(4, 4);
        final Size padding = new Size(8, 8);//32,32


        hog.detectMultiScale(mat, foundLocations, foundWeights, 0.0, winStride, padding, 1.05, 1.0, false);
        //hog.detectMultiScale(mat, foundLocations, foundWeights, 0.0, winStride, padding, 1.05, 1, true);
        //hog.detectMultiScale(mat, foundLocations, foundWeights);
        if (foundLocations.rows() > 0) {
            final List<Double> weightList = foundWeights.toList();
            List<Rect> rList = foundLocations.toList();

            for(Rect rect : rList){
                Rect re = new Rect((int)(rect.x/r), (int)(rect.y/r), (int)(rect.width/r), (int)(rect.height/r));
                this.rectList.add(re);
            }
        }

        if (foundLocations.rows() >0) {
            //bmpBodyDetect = matToBitmap(mat); //bitmap with detected bodies
            activityInter.callbackSuccessfulNew(rectList, RequestType.HOG_DETECTION_SUCCESS);
        } else {
            activityInter.callBackFailed(RequestType.HOG_DETECTION_FAIL);
        }
    }

    public void onResume()
    {
        super.onResume();

    }

    //detect
    public void detectBodyArea(Bitmap imgToChange, String bodyArea) {
        Mat imageMat = bitmapToMat(imgToChange);
        int min;

        if(bodyArea.equals(PEDESTRAIN)) {
            min = Math.min(MIN_WIDTH_HOG, imageMat.width());
        }
        else {
            min = Math.min(MIN_WIDTH_BODY, imageMat.width());
        }

        float r = (float)min / imageMat.width();
        int h = (int)(r * imageMat.height());

        Size res = new Size(min,h);
        Imgproc.resize(imageMat, imageMat, res);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
        bmpBodyDetect = null;
        boolean resultIsFound = false;
        matOfBodyDetections = new MatOfRect();

        switch (bodyArea) {
            case "full":
                fullBodyDetector.detectMultiScale(imageMat, matOfBodyDetections);
                resultIsFound = detectBodyHelper(matOfBodyDetections, imageMat, r);
                imageMat.release();

                if (resultIsFound) {
                    //activityInter.callbackSuccessful(bmpBodyDetect, RequestType.FULL_BODY_DETECTION_SUCCESS);
                } else {
                    activityInter.callBackFailed(RequestType.FULL_BODY_DETECTION_FAIL);
                }
                break;

            case "up":
                upperBodyDetector.detectMultiScale(imageMat, matOfBodyDetections);
                resultIsFound = detectBodyHelper(matOfBodyDetections, imageMat, r);
                imageMat.release();

                if (resultIsFound) {
                    //activityInter.callbackSuccessful(bmpBodyDetect, RequestType.UPPER_BODY_DETECTION_SUCCESS);
                    //activityInter.callbackSuccessfulNew(rectList, RequestType.PEDESTRIAN_DETECTION_SUCCESS);
                } else {
                    activityInter.callBackFailed(RequestType.UPPER_BODY_DETECTION_FAIL);
                }
                break;

            case "low":
                lowerBodyDetector.detectMultiScale(imageMat, matOfBodyDetections);
                resultIsFound = detectBodyHelper(matOfBodyDetections, imageMat, r);
                imageMat.release();

                if (resultIsFound) {
                    //activityInter.callbackSuccessful(bmpBodyDetect, RequestType.LOWER_BODY_DETECTION_SUCCESS);
                } else {
                    activityInter.callBackFailed(RequestType.LOWER_BODY_DETECTION_FAIL);
                }
                break;
            case "ped":
                pedestrianDetector.detectMultiScale(imageMat, matOfBodyDetections);
                resultIsFound = detectBodyHelper(matOfBodyDetections, imageMat, r);
                imageMat.release();

                if (resultIsFound) {
                    //activityInter.callbackSuccessfulNew(rectList, RequestType.PEDESTRIAN_DETECTION_SUCCESS);
                } else {
                    activityInter.callBackFailed(RequestType.PEDESTRIAN_DETECTION_FAIL);
                }
                break;

        }
    }

    private boolean detectBodyHelper(MatOfRect matOfBodyDetections, Mat imageMat, float r) {
        if (!matOfBodyDetections.empty()) {
            int size = matOfBodyDetections.toArray().length;
            for (Rect rect : matOfBodyDetections.toArray()) {
                //Imgproc.rectangle(imageMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 7);
                Rect re = new Rect((int)(rect.x/r), (int)(rect.y/r), (int)(rect.width/r), (int)(rect.height/r));
                this.rectList.add(re);
            }
            matOfBodyDetections.release();

            isDetectBodyArea = true;
            return true;
        }
        return false;
    }

    public void test(Bitmap bitmap){
        MatOfKeyPoint points = new MatOfKeyPoint();
        try {
            Mat mat = bitmapToMat(bitmap);
            FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);
            fast.detect(mat, points);

            // Sort and select 500 best keypoints
//            List<KeyPoint> listOfKeypoints = points.toList();
//            Collections.sort(listOfKeypoints, new Comparator<KeyPoint>() {
//                @Override
//                public int compare(KeyPoint kp1, KeyPoint kp2) {
//                    // Sort them in descending order, so the best response KPs will come first
//                    return (int) (kp2.response - kp1.response);
//                }
//            });
//            List<KeyPoint> listOfBestKeypoints = listOfKeypoints.subList(0, 50);
//
//            Mat mkp = vector_KeyPoint_to_Mat(listOfBestKeypoints);
//            points.release();
            MatOfKeyPoint matOfKeyPoint = new MatOfKeyPoint(points);
           // points = new MatOfKeyPoint(vector_KeyPoint_to_Mat(listOfBestKeypoints));

            Scalar redcolor = new Scalar(255, 0, 0);
            Mat mRgba = mat.clone();
            Imgproc.cvtColor(mat, mRgba, Imgproc.COLOR_RGBA2RGB, 4);

            Features2d.drawKeypoints(mRgba, matOfKeyPoint, mRgba, redcolor, 1);
            mat.release();
            activityInter.callbackSuccessful(matToBitmap(mRgba),RequestType.PEDESTRIAN_DETECTION_SUCCESS);
        }
        catch (Exception e)
        {
            Log.d("ERROR", e.getMessage());
        }
    }

//    public void doBackgroundRemoval(Bitmap bitmap){
//        Mat frame = bitmapToMat(bitmap);
//        Mat hsvImg = new Mat();
//
//        hsvImg.create(frame.size(), CvType.CV_8U);
//        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
//        Core.split(hsvImg, hsvPlanes);
//
//        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));
//        for (int h = 0; h < 180; h++)
//            average += (hist_hue.get(h, 0)[0] * h);
//        average = average / hsvImg.size().height / hsvImg.size().width;
//
//
//
//    }

    public void blurBackground(int ksize, Rect rect){
        //Creating an empty matrix to store the result
        Mat dst = new Mat();
        //Applying Blur effect on the Image
        Imgproc.blur(original, dst, new Size(ksize, ksize), new Point(20, 30), Core.BORDER_DEFAULT);

        //blur(Mat src, Mat dst, Size ksize, Point anchor, int borderType)
        activityInter.callbackSuccessful(matToBitmap(dst), RequestType.BLUR_BACKGROUND_SUCCESS);

    }




    public void doBackgroundRemoval(Bitmap bitmap)
    {
        // init
        Mat frame = bitmapToMat(bitmap);
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();


        int thresh_type = Imgproc.THRESH_BINARY;//_INV;
//        if (this.inverse.isSelected())
//            thresh_type = Imgproc.THRESH_BINARY;

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);//179.0

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);//179.0

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        activityInter.callbackSuccessful(matToBitmap(foreground), RequestType.PEDESTRIAN_DETECTION_SUCCESS);
        //return foreground;
    }

    private double getHistAverage(Mat hsvImg, Mat hueValues)
    {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    public List<Rect> getRectList(){
        return this.rectList;
    }
    public boolean getIsDetectFace() {
        return this.isDetectBodyArea;
    }

    public Bitmap getBmpBodyDetect() {
        return this.bmpBodyDetect;
    }

    public void addRectToList(int x1, int x2, int y1, int y2){
        Rect rect = new Rect(x1, x2, y1, y2);
        this.rectList.add(rect);

    }

    public void detectObjects(Bitmap bitmap) {
        final int IN_WIDTH = 300;
        final int IN_HEIGHT = 300;
        final float WH_RATIO = (float)IN_WIDTH / IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.007843;
        final double MEAN_VAL = 127.5;
        final double THRESHOLD = 0.2;

//
        // Get a new frame
        Mat frame = bitmapToMat(bitmap);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        // Forward image through network.
        Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR, new Size(IN_WIDTH, IN_HEIGHT), new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false, false);
        net.setInput(blob);
        Mat detections = net.forward();
        int cols = frame.cols();
        int rows = frame.rows();
        Size cropSize;
        if ((float)cols / rows > WH_RATIO) {
            cropSize = new Size(rows * WH_RATIO, rows);
        } else {
            cropSize = new Size(cols, cols / WH_RATIO);
        }
        int y1 = (int)(rows - cropSize.height) / 2;
        int y2 = (int)(y1 + cropSize.height);
        int x1 = (int)(cols - cropSize.width) / 2;
        int x2 = (int)(x1 + cropSize.width);
        Mat subFrame = frame.submat(y1, y2, x1, x2);
        cols = subFrame.cols();
        rows = subFrame.rows();
        detections = detections.reshape(1, (int)detections.total() / 7);
        for (int i = 0; i < detections.rows(); ++i) {
            double confidence = detections.get(i, 2)[0];
            if (confidence > THRESHOLD) {
                int classId = (int)detections.get(i, 1)[0];
                int xLeftBottom = (int)(detections.get(i, 3)[0] * cols);
                int yLeftBottom = (int)(detections.get(i, 4)[0] * rows);
                int xRightTop   = (int)(detections.get(i, 5)[0] * cols);
                int yRightTop   = (int)(detections.get(i, 6)[0] * rows);
                // Draw rectangle around detected object.
                Rect rect = new Rect((int)(xLeftBottom * 1.7), yLeftBottom, (int)((xRightTop-xLeftBottom) * 1.5), yRightTop-yLeftBottom);
                   rectList.add(rect);

                Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom),
                        new Point(xRightTop, yRightTop),
                        new Scalar(0, 255, 0),5);
                String label = classNames[classId] + ": " + confidence;
                int[] baseLine = new int[1];
                Size labelSize = Imgproc.getTextSize(label, Core.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
                // Draw background for label.
                Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom - labelSize.height),
                        new Point(xLeftBottom + labelSize.width, yLeftBottom + baseLine[0]),
                        new Scalar(255, 255, 255), Core.FILLED);
                // Write class name and confidence.
                Imgproc.putText(subFrame, label, new Point(xLeftBottom, yLeftBottom),
                        Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));

            }
        }
       // return frame;
       //activityInter.callbackSuccessful(matToBitmap(frame), RequestType.PEDESTRIAN_DETECTION_SUCCESS);
      activityInter.callbackSuccessfulNew(rectList,RequestType.PEDESTRIAN_DETECTION_SUCCESS);
    }

    public void doCanny(Bitmap bitmap){
        // init
        Mat frame = bitmapToMat(bitmap);
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, 10, 100);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);
        //return dest;
        activityInter.callbackSuccessful(matToBitmap(dest), RequestType.PEDESTRIAN_DETECTION_SUCCESS);
    }

    public void cannyAnd(Bitmap bitmap){
        // init
        final Size kernelSize = new Size(11, 11);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);

        Mat frame = bitmapToMat(bitmap);
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();
        Mat contours = new Mat();

        Imgproc.Canny(grayImage, detectedEdges,10, 100);
        Imgproc.dilate(detectedEdges, detectedEdges, kernel);
        Imgproc.erode(detectedEdges, detectedEdges, kernel);

       // Imgproc.
        //Imgproc.findContours(contours, detectedEdges, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

    }

    public void drawRects(List<Rect> rectList){
        final Point rectPoint1 = new Point();
        final Point rectPoint2 = new Point();
        final Scalar rectColor = new Scalar(255, 0, 0);

        for (final Rect rect : rectList) {
                rectPoint1.x = rect.x;
                rectPoint1.y = rect.y;
                rectPoint2.x = rect.x + rect.width;
                rectPoint2.y = rect.y + rect.height;
                // Draw rectangle around fond object
                Imgproc.rectangle(original, rectPoint1, rectPoint2, rectColor, 5);
        }
        activityInter.callbackSuccessful(matToBitmap(original), RequestType.PEDESTRIAN_DETECTION_SUCCESS);

    }
//
    public void filter(Bitmap bitmap) {
        Mat src = bitmapToMat(bitmap);
        final Mat dst = new Mat(src.rows(), src.cols(), src.type());
        src.copyTo(dst);

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2GRAY);

        final List<MatOfPoint> points = new ArrayList<>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(dst, points, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2BGR);
        for(int i=0; i<points.size(); i++){
            Imgproc.drawContours(dst, points, i,new Scalar(0,255,0), 5);

        }

        activityInter.callbackSuccessful(matToBitmap(dst), RequestType.PEDESTRIAN_DETECTION_SUCCESS);

//        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.adaptiveThreshold(src, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 20);
//        activityInter.callbackSuccessful(matToBitmap(dst), RequestType.PEDESTRIAN_DETECTION_SUCCESS);

    }

    public ArrayList<Rect> non_maximum_suppression_fast(MatOfRect matOfRect, float overlapThresh){
        ArrayList<Rect> listR = new ArrayList<Rect>();
        ArrayList<Integer> pick = new ArrayList<>();
        ArrayList<Float> x1 = new ArrayList<>();
        ArrayList<Float> x2 = new ArrayList<>();
        ArrayList<Float> y1 = new ArrayList<>();
        ArrayList<Float> y2 = new ArrayList<>();
        ArrayList<Float> areas = new ArrayList<>();
        ArrayList<Float> overlap = new ArrayList<>();
        int last;
        int temp;

        if (matOfRect.empty()) {
            return listR;
        }
        //create four vectors of rect's coordinates
        for(Rect rect : matOfRect.toList()){
            x1.add((float)rect.x);
            y1.add((float)rect.y);
            x2.add((float)rect.width + rect.x);
            y2.add((float)rect.height + rect.y );
            areas.add((float)rect.width * rect.height); //area of all boxes
        }

        ArrayList<Integer> idxs = new ArrayList<>();
        idxs = sortIndexsByValues(y2);

        while (idxs.size() > 0) {
            last = idxs.size() - 1; //index of last element
            temp = idxs.get(last); // value in the last index
            pick.add(temp);


            float xx1 = getMaxFromAllArratList(last, x1);
            float yy1 = getMaxFromAllArratList(last, y1);
            float xx2 = getMaxFromAllArratList(last, x2);
            float yy2 = getMaxFromAllArratList(last, y2);

            float w = Math.max(0, xx2 - xx1 + 1);
            float h = Math.max(0, yy2 - yy1 + 1);

            overlap = calcOverlap(w, h, areas, last, idxs);





        }//while
        return listR;
    }

    public ArrayList<Float> calcOverlap(float w, float h, ArrayList arrayList, int last, ArrayList<Integer> idxs){
        ArrayList<Float> res = new ArrayList<>();

        idxs.remove(last);
        for(int index : idxs){
            float num = (int)(w*h)/(int)arrayList.get(index);
            res.add(num);
        }
        return res;
    }


    public float getMaxFromAllArratList(int index, ArrayList<Float> arrayList){
        float max = arrayList.remove(index);

        for(int i = 0 ; i < arrayList.size(); i++){
            if(arrayList.get(i) > max){
                max = arrayList.get(i);
            }
        }
        return max;
    }
    //returns arrayList of y2 indexs sorted ascending
    public ArrayList<Integer> sortIndexsByValues(ArrayList<Float> arrayList) {

        ArrayList<Integer> res = new ArrayList<>();
        TreeMap<Float, Integer> map = new TreeMap<>();

        for (int i = 0; i < arrayList.size(); i++) {
            map.put(arrayList.get(i), i);
        }

        for (Map.Entry entry : map.entrySet()) {
            res.add((int) entry.getValue());
        }
        return res;
    }

    public ArrayList vMulV(ArrayList v1,ArrayList v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("dimensions disagree");
        }
        ArrayList<Float> c = new ArrayList();
        for (int i = 0; i < v1.size(); i++){
            float t1 = (float)v1.get(i);
            float t2 = (float)v2.get(i);
            c.add(t1 * t2);
        }
        return c;
    }

    public ArrayList vMinusV(ArrayList v1,ArrayList v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("dimensions disagree");
        }
        ArrayList<Float> c = new ArrayList();
        for (int i = 0; i < v1.size(); i++){
            float t1 = (float)v1.get(i);
            float t2 = (float)v2.get(i);
            c.add(t2 - t1 + 1);
        }
        return c;
    }

    public Mat bitmapToMat(Bitmap bitmap) {
        mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        return mat;
    }

    public Bitmap matToBitmap(Mat mat) {

        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }


    public interface OpenCVAppInterface {

        public void callbackSuccessful(Bitmap bitmap, RequestType type);
        public void callbackSuccessfulNew(List<Rect> rectList, RequestType type);

        public void callBackFailed(RequestType type);

    }

    public enum RequestType {
        FULL_BODY_DETECTION_SUCCESS, UPPER_BODY_DETECTION_SUCCESS, LOWER_BODY_DETECTION_SUCCESS,
        PEDESTRIAN_DETECTION_SUCCESS, HOG_DETECTION_SUCCESS, FULL_BODY_DETECTION_FAIL,
        UPPER_BODY_DETECTION_FAIL, LOWER_BODY_DETECTION_FAIL, PEDESTRIAN_DETECTION_FAIL,
        HOG_DETECTION_FAIL, BLUR_BACKGROUND_SUCCESS
    }
}

