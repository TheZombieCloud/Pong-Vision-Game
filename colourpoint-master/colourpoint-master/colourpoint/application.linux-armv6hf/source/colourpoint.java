import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import java.text.*; 
import gab.opencv.*; 
import processing.video.*; 
import java.awt.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class colourpoint extends PApplet {

class MyTechnique extends PointingTechnique {

  MyTechnique() {
    name = "PONG";
  }

  public void handle(Blob[][] blobs) {

    displayName();
    double firsta = 0;
    double seconda = 0;
    double thirda = 0;
    double fourtha = 0;
    for (int i = 0;i<blobs[0].length;i++){
      if (blobs[0][i] != null){
        firsta += blobs[0][i].area;
      }
      if (blobs[1][i] != null){
        seconda += blobs[1][i].area;
      }
      if (blobs[2][i] != null) {
        thirda += blobs[2][i].area;
      }
      if (blobs[3][i] != null) {
        fourtha += blobs[3][i].area;
      }
    }
    if (firsta>seconda){
      moveup();
    }
    else if (seconda>firsta){
      movedown();
    }
    else {
      dontmove();
    }
    if (thirda>fourtha){
      moveup2();
    }
    else if (fourtha>thirda){
      movedown2();
    }
    else {
      dontmove2();
    }
  }
  
}

// data strcuture for each blob found in the scene
class Blob {
  
  // the colour index of the blob
  int colour;
  // no blob detected
  boolean empty = true;
  // centroid of blob points
  float x;
  float y;
  // area of blob in sq pixels
  float area;
  // bounding box
  Rectangle bb;
  // all enclosing points
  ArrayList<PVector> points;
  // simplifed enclosing points
  ArrayList<PVector> simplePoints;
  // convex hull points
  ArrayList<PVector> convexHullPoints;
}
/*
 * Demo
 * displays random targets that can be toggled using the current 
 * pointing technique
 */

class Demo {

  ToggleTarget[] targets;

  int errorDisplay = -1001;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void setup() {
    reset();
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void reset() {
    int n = 5;
    targets = new ToggleTarget[n];

    float spacing = mainWidth / (n + 1.0f);
    for (int i = 0; i < n; i++) {
      float x = (i + 1) * spacing;
      float y = random(spacing / 2, mainHeight - spacing / 2);
      float s = random(50, spacing);
      targets[i] = new ToggleTarget(x, y, s);
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void draw() {
    for (Target t : targets) {
      t.draw();
    }

    if (millis() - errorDisplay < 250) {
      stroke(255, 0, 0);
      strokeWeight(6);
      noFill();
      rect(3, 3, mainWidth - 6, mainHeight - 6);
    }
  }
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void down(float x, float y) {
    boolean error = true;
    for (ToggleTarget t : targets) {
      if (t.isHit(x, y)) {
        t.toggle();
        error = false;
        break;
      }
    }
    if (error) {
      errorDisplay = millis();
      println("error ");
    }
  }
}
/*
 * Experiment
 * Displays a sequence of targets in a certain order and size
 * for people to click on so we can measure the speed and 
 * accuracy of a given technique.
 */

class Experiment {

  // relative logging path (can be absolute)
  String logPath = "logs/";

  // the global logger object
  Logger logger = new Logger(logPath);


  int targetToHit = 0;
  SequenceTarget[] targets;

  String[] script;
  int scriptLine = 0;
  int block = 0;

  int errorDisplay = -1001;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void setup() {
    reset();
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void reset() {

    // start the log
    String tn = "";
    if (technique != null)
      tn = technique.name;

    if (!experimentParticipant.equals(""))
      logger.newfile(tn + " " + experimentParticipant); 

    // load the script
    script = loadStrings(scriptFilename);
    scriptLine = 0;
    newBlock();
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


  public void newBlock() {
    // next line is block or end
    String[] token = script[scriptLine].split(",");

    if (token[0].equals("block")) {
      println(token[0]);
      block = PApplet.parseInt(token[1]);

      logger.logEvent("block," + block);

      int num = PApplet.parseInt(token[2]);
      println("block " + block + " targets " + num);
      targets = new SequenceTarget[num];

      // get all the targets
      scriptLine++;
      for (int i = 0; i < num; i++) {
        println(script[scriptLine]);
        token = script[scriptLine].split(",");
        float x = PApplet.parseFloat(token[1]);
        float y = PApplet.parseFloat(token[2]);
        float s = PApplet.parseFloat(token[3]); 
        targets[i] = new SequenceTarget(x, y, s, i + 1);
        scriptLine++;
      }
      // set first target to start
      targets[0].setState(SequenceTarget.NEXT);
      targetToHit = 1;
    } else {
      logger.close(); 
      targetToHit = -1;
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void draw() {
    if (targetToHit > 0) {
      for (SequenceTarget t : targets) {
        t.draw();
      }
    } else {
      fill(255);
      textSize(60);
      textAlign(CENTER, CENTER);
      text("done", mainWidth/2, mainHeight/2);
      textSize(15);
    }

    if (millis() - errorDisplay < 250) {
      stroke(255, 0, 0);
      strokeWeight(6);
      noFill();
      rect(3, 3, mainWidth - 6, mainHeight - 6);
    }

    textAlign(CENTER, TOP);
    textSize(13);
    fill(255);
    text("log: " + logger.loggingTo(), mainWidth / 2, 5);
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void down(float x, float y) {

    if (targetToHit > 0) {
      boolean error = true;
      for (SequenceTarget t : targets) {
        if (t.isHit(x, y) && t.num == targetToHit) {
          error = false;
          break;
        }
      }
      if (!error) {
        println("success " + targetToHit);
        SequenceTarget t = targets[ targetToHit - 1];
        logger.logEvent("target," + targetToHit);
        targets[ targetToHit - 1].setState(SequenceTarget.DONE);
        targetToHit++;
        if (targetToHit - 1 >= targets.length) {
          newBlock();
        } else {
          targets[targetToHit - 1].setState(SequenceTarget.NEXT);
        }
      } else {
        println("error " + targetToHit);
        logger.logEvent("error," + targetToHit);
        errorDisplay = millis();
      }
    }
  }
}



// writes a custom log file format 

// # all lines begin with timestamp
// yyyyMMdd,HHmmss.S (e.g. 20130116,130011.438)

// CSV continuous data:
// special line to define the CSV column names
// A,colname,colname,colname,colname,...
// timestamp,A,val,val,val,val,...

// event data
// timestamp,[event,subevent,{param:val,param:val}]
// use brackets for CSV val data

// helper function
public String joinString(String[] list, String separator)
{
  return join(list, separator);
}


// simple logging class
class Logger 
{

  boolean isRunning = false;

  long lastLogTime = 0;


  String path = "logs/";
  String filename = "";
  PrintWriter logfile = null;

  String header = "";

  // format for dates in log file
  SimpleDateFormat logDateFormater = new SimpleDateFormat("yyyyMMdd,HHmmss.S");

  SimpleDateFormat filenameDateFormater = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public String loggingTo() {
    if (logfile != null) {
      return filename;
    } else {
      return "NOT LOGGING";
    }
  }


  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public Logger(String path)
  {
    path = path;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  // must be even number of values
  public void logEvent(String type, Object ... values) {
    String[] list = new String[values.length / 2];

    int j = 0;
    for (int i = 0; i < list.length * 2; i += 2) {
      list[j] = values[i].toString() + ":" + values[i + 1].toString();
      j++;
    }
    log(type + ",{" + joinString(list, ",") + "}");
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public void logEvent(String data) {
    log(data);
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public void log(String s)
  {
    if (logfile != null) {
      long t = millis();
      logfile.println(t + "," + s);
      lastLogTime = t;
    } else {
      println("ERROR: log to closed file");
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public void comment(String s)
  {
    if (logfile != null) {
      logfile.println("# " + s);
    }
  }


  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  private void close() {
    if (logfile != null) {
      logfile.flush();
      logfile.close(); 
      println("closed log");
      logfile = null;
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  private boolean newfile(String prefix)
  {
    close();
    long t = System.currentTimeMillis();
    filename = path + prefix + " " + filenameDateFormater.format(t) + ".txt";
    try
    {
      logfile = createWriter(filename);
      println("opened log: '" + filename);
      lastLogTime = t;
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
    //comment(header);
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
class PWindow extends PApplet {
  boolean run = true;
  float y = 1;
  float y2 = 1;
  boolean mu = false;
  boolean md = false;
  boolean mu2 = false;
  boolean md2 = false;
  
  PWindow() {
    super();
    PApplet.runSketch(new String[] {this.getClass().getSimpleName()}, this);
  }

  public void settings() {
    size(1500, 600);
  }

  public void setup() {
    background(0);
  }

  public void draw() {
    rect(30, 200+y, 10, 55);
    rect (1250, 200+y2, 10, 55);
    if (mu&&200+y<=530){
      y += 1;
    }
    if (md&&200+y>=15){
      y -= 1;
    }
    if (mu2&&200+y2<=530){
      y2 += 1;
    }
    if (md2&&200+y2>=15){
      y2 -= 1;
    }
  }
  
  public void moveup () {
    mu = true;
    md = false;
  }
  
  public void movedown() {
    md = true;
    mu = false;
  }
  
  public void moveup2 () {
    mu2 = true;
    md2 = false;
  }
  
  public void movedown2 () {
    md2 = true;
    mu2 = false;
  }
  
  public void dontmove() {
    md = false;
    mu = false;
  }
  
  public void dontmove2() {
    md2 = false;
    mu2 = false;
  }
  
  public void mousePressed() {
    println("mousePressed in secondary window");
  }
}
/* 
 * Target
 * A simple target to display and interact with
 */

class Target {

  float x;
  float y;
  float s;

  int targetFill = color(255, 100);

  Target(float x, float y, float s) {
    this.x = x;
    this.y = y;
    this.s = s;  // size (diameter)
  }

  public void draw() {
    stroke(0);
    strokeWeight(2);
    fill(targetFill);
    ellipse(x, y, s, s);
  }

  public boolean isHit(float mx, float my) {
    return dist(mx, my, x, y) < s / 2;
  }
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


class ToggleTarget extends Target {

  boolean isOn = true;

  ToggleTarget(float x, float y, float s) {
    super(x, y, s);
  }

  public void toggle() {
    println("toggle", isOn);
    if (isOn) {
      targetFill = color(0, 100);
    } else {
      targetFill = color(255, 100);
    }
    isOn = !isOn;
  }
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


class SequenceTarget extends Target {

  static final int NEXT = 0;
  static final int FUTURE = 1;
  static final int DONE = 2;

  int num;
  int state = NEXT;
  int textFill;
  boolean wasError = false;

  SequenceTarget(float x, float y, float s, int num) {
    super(x, y, s);
    this.num = num;
    reset();
  }

  public void draw() {

    //if (wasError) {
    //  stroke(255, 0, 0);
    //  strokeWeight(4);
    //} else {
    stroke(0);
    strokeWeight(2);
    //}
    fill(targetFill);
    ellipse(x, y, s, s);
    fill(textFill);
    textSize(20);
    textAlign(CENTER, CENTER);
    text(num, x, y);
  }

  public void reset() {
    wasError = false;
    setState(FUTURE);
  }

  public void error() {
    wasError = true;
  }

  public void setState(int s) {
    // fill is based on state
    switch(s) {
    case NEXT:
      targetFill = color(255, 200);
      textFill = color(0);
      break;

    case FUTURE:
      targetFill = color(160, 200);
      textFill = color(60);
      break;

    case DONE:
      targetFill = color(100, 200);
      textFill = color(150);
      break;
    }
    state = s;
  }
}
/* 
 * Classes to build and demonstrate different pointing techniques
 */


// This is the base class for all pointing techniques
class PointingTechnique {

  String name = "no technique";

  boolean isDown = false;

  // 5% of image area usually works well
  float minBlobArea = 0.005f * processWidth * processHeight;

  // how blurry to make the frame before colour subtractions
  // (high blur will slow things down, 0 turns bluring off (but you'll get more noise)
  int blurIterations = 0;

  // morphological operations to clean up noise
  // (usually want erode and dilate iterations to be the same)
  int erodeIterations = 4;
  int dilateIterations = 4;

  public void displayName() {
    fill(255);
    textAlign(LEFT, TOP);
    textSize(12);
    text(name, 5, 5);
  }

  public void handle(Blob[][] blobs) {
    displayName();
  }
}



// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// This technique uses first colour blob for cursor position
// and clicks whenever it sees a second colour blob
class TwoColour extends PointingTechnique {

  TwoColour() {
    name = "twocolour";
  }

  public void handle(Blob[][] blobs) {

    displayName();

    // check if at least one blob is found for the first colour 
    if (blobs[0][0] != null) {

      // use the largest blob of colour 0 (the first colour)
      Blob b = blobs[0][0];

      // this is the centre position of the blob
      float x = b.x;
      float y = b.y;

      //  move the cursor to that position
      move(x, y);

      // now, check if at least one blob is found for the second colour 
      if (blobs[1][0] != null) {
        // if other colour blob is found, then click down
        down(x, y);
      } else {
        // otherwise there was no second colour, so click up
        up(x, y);
      }
    }
  }
}


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// This technique tracks a shape with narrow width 
// compared to its height (low aspect ratio)
// when the aspect ratio is closer to 1, then it
// clicks down
class RatioClick extends PointingTechnique {

  RatioClick() {
    name = "ratioclick";
  }

  public void handle(Blob[][] blobs) {

    displayName();

    // check if at least one blob is found for the first colour 
    if (blobs[0][0] != null) {

      // use the largest blob of colour 0 (the first colour)
      Blob b = blobs[0][0];

      // this is the centre position of the blob
      float x = b.x;
      float y = b.y;

      //  move the cursor to that position
      move(x, y);

      // calculate the ratio from blob width and height
      float ratio = b.bb.width / PApplet.parseFloat(b.bb.height);

      // if the ratio is greater than "square" then click
      float threshold = 1.0f;

      if (ratio > threshold) {
        down(x, y);
      } else {
        up(x, y);
      }

      // visualize feedback with some circles 
      noFill();
      stroke(200);
      strokeWeight(1);
      ellipse(x, y, threshold * 100, threshold * 100);
      stroke(255);  
      strokeWeight(2);
      ellipse(x, y, ratio * 100, ratio * 100);
    }
  }
}


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// Not really a technique, but shows how to get 
// Blob information
class ShowBlobFeatures extends PointingTechnique {

  ShowBlobFeatures() {
    name = "showblobfeatures";
  }

  public void handle(Blob[][] blobs) {

    displayName();

    // put cursor in centre
    move(mainWidth/2, mainHeight/2);

    if (blobs[0][0] != null) {
      Blob b = blobs[0][0];


      noFill();
      strokeWeight(1);
      stroke(255);
      ellipse(b.x, b.y, 10, 10); 

      // bounding box of blob
      strokeWeight(1);
      noFill();
      stroke(255);
      rect(b.bb.x, b.bb.y, b.bb.width, b.bb.height);

      // convext hull of blob
      stroke(255, 0, 0);
      strokeWeight(6);
      for (PVector v : b.convexHullPoints) {
        point(v.x, v.y);
      }       

      // simplified  points of blob shape
      stroke(0, 255, 0);
      strokeWeight(4);
      for (PVector v : b.simplePoints) {
        point(v.x, v.y);
      }  

      // all points of blob shape
      stroke(0, 0, 255);
      strokeWeight(2);
      for (PVector v : b.points) {
        point(v.x, v.y);
      }
    }
  }
}



// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// for faking mouse input when testing
class MousePoint extends PointingTechnique {

  MousePoint() {
    name = "mousepoint";
  }

  public void handle(Blob[][] blobs) {

    displayName();

    float x = mouseX;
    float y = mouseY;

    move(x, y);

    if (mousePressed && !isDown) {
      down(x, y);
      isDown = true;
    } else if (!mousePressed && isDown) {
      up(x, y);
      isDown = false;
    }
  }
}
/*
 * Main class to recognize and track coloured blobs
 */


boolean on = true;

class Tracker {

  boolean calibrate = false;

  int numColours = 4;
  int calibrateColour = 0;
  Colour[] trackMin = new Colour[numColours];
  Colour[] trackMax = new Colour[numColours];

  int trackExpand = 10;

  ArrayList<Contour> contours;

  int maxBlobsPerColour = 5;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void setup() {
    contours = new ArrayList<Contour>();
    for (int i = 0; i < numColours; i++) {
      trackMin[i] = new Colour();
      trackMax[i] = new Colour();
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


  public void train(int x, int y, int w, int h, boolean clear) {

    if (w < 0) {
      x  = x + w;
      w = abs(w);
    }

    if (h < 0) {
      y  = y + h;
      h = abs(h);
    }

    println("train ", x, y, w, h);
    println("clear ", clear);

    float ratio = captureWidth / PApplet.parseFloat(mainWidth);

    PImage img = lastFrame.get(PApplet.parseInt(x * ratio), PApplet.parseInt(y * ratio), PApplet.parseInt(w * ratio), PApplet.parseInt(h * ratio));

    //img.filter(BLUR, 3);
    img.loadPixels();

    if (clear) {
      trackMin[calibrateColour] = new Colour(256, 256, 256);
      trackMax[calibrateColour] = new Colour(-1, -1, -1);
    }

    Colour c = new Colour();

    for (int j = 0; j < img.height * img.width; j++) {

      c.fromColor(img.pixels[j]);

      if (c.h < trackMin[calibrateColour].h) 
        trackMin[calibrateColour].h = c.h;
      if (c.h > trackMax[calibrateColour].h) 
        trackMax[calibrateColour].h = c.h;
      if (c.s < trackMin[calibrateColour].s) 
        trackMin[calibrateColour].s = c.s;
      if (c.s > trackMax[calibrateColour].s) 
        trackMax[calibrateColour].s = c.s;
      if (c.b < trackMin[calibrateColour].b) 
        trackMin[calibrateColour].b = c.b;
      if (c.b > trackMax[calibrateColour].b) 
        trackMax[calibrateColour].b = c.b;
    }

    println("min", trackMin[calibrateColour].toString());
    println("max", trackMax[calibrateColour].toString());
    //img.updatePixels();
  }


  PImage temp;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void track(PImage img) {

    // scale down the image
    if (captureWidth != processWidth)
      img.resize(processWidth, processHeight);

    int scale = mainWidth / processWidth;

    img.filter(BLUR, technique.blurIterations);

    Blob[][] blobs = new Blob[numColours][maxBlobsPerColour];

    // for each colour
    for (int i = 0; i < numColours; i++) {

      PImage mask = imageInRange(img, trackMin[i], trackMax[i], trackExpand);

      // morphological operations
      int k;
      for (k = 0; k < technique.erodeIterations; k++)
        mask.filter(ERODE);
      for (k = 0; k < technique.dilateIterations; k++)
        mask.filter(DILATE);

      opencv.loadImage(mask);
      contours = opencv.findContours(true, true);

      int j = 0;

      if (showDebug) {
        pushMatrix();
        translate(mainWidth, 0);
        scale(scale, scale);

        image(mask, 0, 0);
        fill(0, 200);
        noStroke();
        rect(0, 0, mainWidth, mainHeight);

        stroke(trackMax[i].toColor());
        noFill();
        strokeWeight(3);
      }

      for (Contour contour : contours) {

        // contours are sorted, so break when they get too small
        if (contour.area() < technique.minBlobArea || j >= maxBlobsPerColour)
          break;

        Rectangle r = contour.getBoundingBox();

        if (showDebug) {
          // draw the blob for debug
          stroke(trackMax[i].toColor());
          noFill();
          strokeWeight(3);
          contour.draw();

          strokeWeight(1);
          rect(r.x, r.y, r.width, r.height);
        }

        // set the blob data
        Blob b = new Blob();
        b.empty = false;
        b.area = contour.area() * scale * scale;

        b.bb = r;
        b.bb.x *= scale;
        b.bb.y *= scale;
        b.bb.width *= scale;
        b.bb.height *= scale;
        b.x = r.x + r.width / 2;
        b.y = r.y + r.height / 2;

        //println(contour.getPolygonApproximationFactor());
        contour.setPolygonApproximationFactor(0.5f);
        Contour allPoints = contour.getPolygonApproximation();
        b.points =  allPoints.getPoints();

        contour.setPolygonApproximationFactor(5);
        Contour approxContour = contour.getPolygonApproximation();
        b.simplePoints =  approxContour.getPoints();

        b.convexHullPoints = contour.getConvexHull().getPoints();

        for (PVector v : b.points) {
          v.x *= scale;
          v.y *= scale;
        }

        for (PVector v : b.convexHullPoints) {
          v.x *= scale;
          v.y *= scale;
        }

        for (PVector v : b.simplePoints) {
          v.x *= scale;
          v.y *= scale;
        }

        blobs[i][j] = b;
        j++;
      }
      if (showDebug) 
        popMatrix();
    }

    pushMatrix();
    translate(mainWidth, 0);

    if (showDebug) {

      for (int i = 0; i < blobs.length; i++) {
        for (int j = 0; j < blobs[i].length; j++) {
          Blob b = blobs[i][j];

          if (b != null) {


            // draw the blob for debug
            stroke(trackMax[i].toColor());
            fill(255, 120);
            strokeWeight(3);
            // draw contour
            drawPoints(b.points);

            noFill();
            strokeWeight(1);
            rect(b.bb.x, b.bb.y, b.bb.width, b.bb.height);


            int x = b.bb.x + b.bb.width + 5;
            int y = b.bb.y + b.bb.height - 30;

            //stroke(255);
            //strokeWeight(1);
            //line(x - 10, y -10, b.x + r.width / 4, b.y + r.height / 4);
            fill(255);
            textSize(14);
            textAlign(LEFT, BOTTOM);
            text("colour " + i + " blob " + j, x, y);
            y += 15;
            text(" area: " + PApplet.parseInt(b.area), x, y); 
            y += 15;
            text(" size: " + PApplet.parseInt(b.bb.width) + " x " + PApplet.parseInt(b.bb.height), x, y);
          }
        }
      }
    } else {
      fill(0);
      noStroke();
      rect(0, 0, mainWidth, mainHeight);
      fill(255);
      textAlign(CENTER, CENTER);
      text("press SPACE to turn debug on", mainWidth/2, mainHeight/2);
      //rect(10, 10, 100, 100);
    }
    popMatrix();


    // call the technique
    if (technique != null)
      technique.handle(blobs);


    //// <9> Check to make sure we've found any contours
    //if (contours.size() > 0) {
    //  // <9> Get the first contour, which will be the largest one
    //  Contour biggestContour = contours.get(0);

    //  contour.draw();

    //  // <10> Find the bounding box of the largest contour,
    //  //      and hence our object.
    //  Rectangle r = biggestContour.getBoundingBox();

    //  //// <11> Draw the bounding box of our object
    //  //dbg.noFill(); 
    //  //dbg.strokeWeight(2); 
    //  //dbg.stroke(255, 0, 0);
    //  //dbg.rect(r.x, r.y, r.width, r.height);

    //  //// <12> Draw a dot in the middle of the bounding box, on the object.
    //  //dbg.noStroke(); 
    //  //dbg.fill(255, 0, 0);
    //  //dbg.ellipse(r.x + r.width/2, r.y + r.height/2, 30, 30);
    //}
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void draw() {

    if (showDebug) {
      // draw colour samples
      noFill();
      int s = 40;
      for (int i = 0; i < numColours; i++) { 
        noStroke();
        fill(trackMin[i].toColor());
        rect(mainWidth + i * s, mainHeight - s - 1, s/2, s);
        fill(trackMax[i].toColor());
        rect(mainWidth + (i * s) + 20, mainHeight - s - 1, s/2, s);

        if (calibrateColour == i && calibrate) {
          noFill();
          stroke(255);
          strokeWeight(1);
          rect(mainWidth + i * s, mainHeight - s - 1, s, s);
        }
      }
    } 

    if (calibrate) {
      stroke(255);
      int t = 4;
      if (clearCalibration) {
        t = 8;
      }
      strokeWeight(t);
      noFill();
      rect(t/2, t/2, mainWidth - t, mainHeight - t);

      if (drawSample) {
        stroke(255);
        strokeWeight(1);
        noFill();
        rect(sampleSize[0], sampleSize[1], sampleSize[2], sampleSize[3]);
      }
    }
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void save(JSONObject json) {

    for (int i = 0; i < numColours; i++) {
      trackMin[i].saveJSON("trackMin" + i, json); 
      trackMax[i].saveJSON("trackMax" + i, json);
    }
    json.setInt("trackExpand", trackExpand);
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  public void load(JSONObject json) {

    for (int i = 0; i < numColours; i++) {
      trackMin[i].loadJSON("trackMin" + i, json); 
      trackMax[i].loadJSON("trackMax" + i, json);
    }
    trackExpand = json.getInt("trackExpand");
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  // drawing rect when sampling colour area

  boolean drawSample = false; 
  int[] sampleSize = new int[4]; 
  boolean clearCalibration = false; 

  public void mousePressed() {
    if (calibrate) {
      drawSample = true; 
      sampleSize[0] = mouseX; 
      sampleSize[1] = mouseY; 
      sampleSize[2] = 0; 
      sampleSize[3] = 0;
    }
  }

  public void mouseDragged() {
    if (calibrate) {
      sampleSize[2] = mouseX - sampleSize[0]; 
      sampleSize[3] = mouseY - sampleSize[1];
    }
  }

  public void mouseMoved() {
  }

  public void mouseReleased() {
    if (calibrate) {
      drawSample = false; 
      tracker.train(sampleSize[0], sampleSize[1], sampleSize[2], sampleSize[3], clearCalibration);
      clearCalibration = false;
    }
  }

  public void keyPressed() {

    switch (key) {
    case 'C':
      clearCalibration = true;
      trackMin[calibrateColour] = new Colour(256, 256, 256);
      trackMax[calibrateColour] = new Colour(-1, -1, -1);

      break;
    case '1':
      calibrateColour = 0;
      break;

    case '2':
      calibrateColour = 1;
      break;
    case '3':
      calibrateColour = 2;
      break;
    case '4':
      calibrateColour = 3;
      break;
    }
    //if (key == CODED && keyCode == SHIFT) {
    //  shiftDown = true;
    //}
  }

  public void keyReleased() {
    //shiftDown = false;
  }
}
/* 
 * Colour
 * stores, loads, saves, and converts HSB colours
 */
 
class Colour {

  float h;
  float s;
  float b;

  Colour() {
    this.h = 0;
    this.s = 0;
    this.b = 0;
  }

  Colour(float h, float s, float b) {
    this.h = h;
    this.s = s;
    this.b = b;
  }

  public void fromColor(int c) {
    h = hue(c);
    s = saturation(c);
    b = brightness(c);
  }

  public String toString() {
    return h + "," + s + "," + b;
  }

  public Colour average(Colour c1, Colour c2) {
    return new Colour((c1.h + c2.h) / 2, 
      (c1.s + c2.s) / 2, 
      (c1.b + c2.b) / 2);
  }

  public int toColor() {
    colorMode(HSB, 255, 255, 255, 255);
    int c = color(h, s, b);
    colorMode(RGB, 255, 255, 255, 255);
    return c;
  }

  public void saveJSON(String name, JSONObject json) {
    json.setFloat(name + "_h", h);
    json.setFloat(name + "_s", s);
    json.setFloat(name + "_b", b);
  }

  public void loadJSON(String name, JSONObject json) {
    h = json.getFloat(name + "_h");
    s = json.getFloat(name + "_s");
    b = json.getFloat(name + "_b");
  }
}
/* 
 * Simple low pass filter
 */

/*
  Let's say Pnf the filtered position, Pn the non filtered position and
  Pn-1f the previous filtered position, Te the sampling period (in seconds)
  and tau a time constant calculated from the cut-off frequency fc.
  
 tau = 1 / (2 * pi * fc)
 Pnf = ( Pn + tau/Te * Pn-1f ) * 1/(1+ tau/Te)
 
 Attention: tau >= 10 * Te
 */

public class LowPassFilter
{
  // current value
  float value_;
  boolean reset_;

  float tau;
  float cutoffFreq;

  int time;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public LowPassFilter(float cf)
  {
    setCutoffFreq(cf);
  }  

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   
  
  public float update(int v)
  {
    return update(PApplet.parseFloat(v)); 
  }
  
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -     

  public float update(float v)
  {
    if (reset_)
    {
      time = millis(); 
      value_ = v;
      reset_ = false;
    }
    else
    {
      // the sampling period (in seconds)
      float te = (millis() - time) / 1000.0f;
      time = millis(); 
      if (te != 0)
        value_ = (v + (tau/te) * value_) * (1.0f / (1.0f + tau/te));
    }
    
    return value_;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public float get()
  {
    return value_;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public void reset()
  {
    reset_ = true;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   

  public void setCutoffFreq(float cf)
  {
    cutoffFreq = cf; 
    // a time constant calculated from the cut-off frequency
    tau = (float)(1.0f / (2 * Math.PI * cutoffFreq));
  }
}




Capture frame;
OpenCV opencv;

PWindow win;
PImage img;
PImage lastFrame;

// the object that tracks coloured blobs
Tracker tracker = new Tracker();
// the experiment logging and task code
Experiment experiment = new Experiment();
// a simple demo to show how the pointing works
Demo demo = new Demo();
// the current pointing tecnique to use
// (all pointing tecniques inerit from te PointingTechnique base class)
PointingTechnique technique;

// various state flags
static final int TASK_NONE = 0;
static final int TASK_DEMO = 1;
static final int TASK_EXPERIMENT = 2;
int task = 0;
boolean showDebug = true;
boolean fake = false;

String settingsFilename = "data/settings.json";

public void settings() {
  size(320, 420);
}

public void setup() {
  // create window
  surface.setResizable(true);
  surface.setSize(mainWidth*2, PApplet.parseInt(mainHeight));

  PImage img = new PImage(mainWidth, mainHeight);
  PImage lastFrame = new PImage(mainWidth, mainHeight);
  
  win = new PWindow();
  // access the camera
  // (you may have to change this line to get it to work)
  // uncomment this to get list of cameras 
  // (program will exit after, you then need to put your camera 
  // into the code to open a camera below)
  //getCameraList();

  // create camera and start it up (other examples commented out)
  //frame = new Capture(this, "name=Logitech BRIO,size=1024x540,fps=15");
  //frame = new Capture(this, "name=Microsoft® LifeCam HD-3000,size=640x400,fps=30"); 
  frame = new Capture(this, captureWidth, captureHeight);

  // create the openCV processing object
  opencv = new OpenCV(this, processWidth, processHeight);

  frame.start();

  tracker.setup();
  experiment.setup();
  demo.setup();

  // load the last settings
  load();

  // default technique when started
  pickTechnique('5');
}


public void draw() {
  if (frame.available()) {
    frame.read();

    img = imageMirror(frame);
    lastFrame = img.copy();

    image(lastFrame, 0, 0, mainWidth, mainHeight);

    //background(0);
    tracker.track(img);

    tracker.draw();
    if (task == TASK_EXPERIMENT)
      experiment.draw();

    if (task == TASK_DEMO)
      demo.draw();
    if (task != TASK_NONE) {
      // cursor crosshair
      float s = 20;
      noFill();
      strokeWeight(3);
      stroke(0);
      cross(pointX, pointY, s);
      strokeWeight(1);
      stroke(255);
      cross(pointX, pointY, s);
      if (pointDown) {
        strokeWeight(3);
        stroke(0);
        ellipse(pointX, pointY, s, s);
        strokeWeight(1);
        stroke(255);
        ellipse(pointX, pointY, s, s);
      }
    }

    fill(255);
    textSize(14);
    textAlign(RIGHT, BOTTOM);
    text(round(frameRate), mainWidth - 10, mainHeight - 10);
  }
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// handle pointing events from current technique

float pointX;
float pointY;
boolean pointDown = false;

public void down(float x, float y) {

  if (!pointDown) {
    println("click down at " + x + "," + y);

    pointX = x;
    pointY = y;
    pointDown = true;

    if (task == TASK_EXPERIMENT)
      experiment.down(x, y);

    if (task == TASK_DEMO)
      demo.down(x, y);
  }
}

public void up(float x, float y) {
  if (pointDown) {
    println("click up at " + x + "," + y);

    pointX = x;
    pointY = y;
    pointDown = false;
  }
}

public void move(float x, float y) {
  pointX = x;
  pointY = y;
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// clumbsy control using keyboard keys

public void keyPressed() {

  tracker.keyPressed();

  // keyboard commands
  switch (key) {

    // toggle debug
  case ' ':
    showDebug = !showDebug;
    break;

  case 'e':
    if (task != TASK_EXPERIMENT)
      task = TASK_EXPERIMENT;
    else
      task = TASK_NONE;
    break;

  case 'E':
    task = TASK_EXPERIMENT;
    experiment.reset();
    break;

  case 'd':
    if (task != TASK_DEMO) 
      task = TASK_DEMO;
    else
      task = TASK_NONE;
    break;

  case 'D':
    task = TASK_DEMO;
    demo.reset();
    break;

  case 'c':
    tracker.calibrate = !tracker.calibrate;
    if (tracker.calibrate) {
      //pickTechnique('1');
      task = TASK_NONE;
    }
    break;

    // save settings
  case 's':
    save();
    break;
  }


  if (!tracker.calibrate) {
    pickTechnique(key);
  } else {
  }
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void keyReleased() {
  tracker.keyReleased();
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void mousePressed() {
  tracker.mousePressed();
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void mouseReleased() {
  tracker.mouseReleased();
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void mouseDragged() {
  tracker.mouseDragged();
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void mouseMoved() {
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void save() {

  println("saving " + settingsFilename);
  JSONObject json = new JSONObject();

  tracker.save(json);

  saveJSONObject(json, settingsFilename);
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public void load() {
  println("loading " + settingsFilename);
  JSONObject json = new JSONObject();
  try {
    json = loadJSONObject(settingsFilename);
    tracker.load(json);
  } 
  // HACK: should catch just exception from bad json or missing file
  catch (Exception e) {
    println(e);
    println("Error loading " + settingsFilename);
  }
}

public void moveup () {
  win.moveup();
}

public void movedown () {
  win.movedown();
}

public void moveup2 () {
  win.moveup2();
}

public void movedown2 () {
  win.movedown2();  
}

public void dontmove() {
  win.dontmove();
}

public void dontmove2() {
  win.dontmove2();
}


// Keyboard Shortcuts
// - - - - - - - - - - - - - -
/*
 1, 2, 3, ... - choose technique
 d - toggle demo view 
     (D to randomize)
 E - start a new experiment
 e - toggle experiment view
 c - toggle colour calibration 
     (press 1 or 2 to switch colour to calibrate)
     (drag rectangle with mouse to pick area to sample colour)
     (C to reset current colour)
 s - save settings
 SPACE - toggle debug information
 */

// Useful Settings
// - - - - - - - - - - - - - -

// size of widow with targets and cursor (leave as is)
int mainWidth = 640;
int mainHeight = 480;

// smaller capture image to speed things up
int captureWidth = mainWidth / 2;
int captureHeight = mainHeight / 2;

// can make an even smaller processing image width if needed
int processWidth = captureWidth;
int processHeight = captureHeight;

// EXPERIMENT
// name of person clicking on targets in the experiment
// (leave blank to not log during experiment)
String experimentParticipant = "";

// filename for experiment target script
String scriptFilename = "data/script.txt";


// handles the keyboard even to pick from available techniques
public void pickTechnique(char k) {

  switch (k) {
  case '0':
    technique = new PointingTechnique();
    break;
  case '1':
    technique = new MousePoint();
    break;
  case '2':
    technique = new ShowBlobFeatures();
    break;
  case '3':
    technique = new RatioClick();
    break;
  case '4':
    technique = new TwoColour();
    break;
    
  // add your techniques here ...
  case '5':
    technique = new MyTechnique();
    break;
  case '6':
    //technique = ;
    break;
  case '7':
    //technique = ;
    break;
  }
}


int WHITE = color(255);
int BLACK = color(0);


public PImage imageAnd(PImage a, PImage b) {

  PImage out = createImage(a.width, a.height, RGB);
  out.loadPixels();
  for (int i = 0; i < a.height * a.width; i++) {
    float ac = blue(a.pixels[i]);
    float bc = blue(b.pixels[i]);

    if (ac > 128 && bc > 128) {
      out.pixels[i] = WHITE;
    }
  }
  out.updatePixels();

  return out;
}

public PImage imageMirror(PImage img) {

  PImage out = createImage(img.width, img.height, RGB);

  out.loadPixels();
  for (int i = 0; i < img.width; i++) {
    for (int j = 0; j < img.height; j++) {  
      out.pixels[j * img.width + i] = img.pixels[(img.width - i - 1) + j * img.width];
    }
  }
  out.updatePixels();

  return out;
}


public PImage imageInRange(PImage img, Colour cMin, Colour cMax, float expand) {

  PImage out = createImage(img.width, img.height, RGB);

  out.loadPixels();
  Colour c = new Colour();
  for (int i = 0; i < img.height * img.width; i++) {

    c.fromColor(img.pixels[i]);

    if ((c.h > cMin.h - expand && c.h < cMax.h + expand) &&
      (c.s > cMin.s - expand && c.s < cMax.s + expand) &&
      (c.b > cMin.b - expand && c.b < cMax.b + expand)) {
      out.pixels[i] = WHITE;
    } else {
      out.pixels[i] = BLACK;
    }
  }
  out.updatePixels();

  return out;
}


public void erode(int n) {
  for (int i = 0; i < n; i++) {
    opencv.erode();
  }
}


public void dilate(int n) {
  for (int i = 0; i < n; i++) {
    opencv.dilate();
  }
}


public void cross(float x, float y, float s) {
  line(x - s, y, x + s, y);
  line(x, y - s, x, y + s);
}


public void drawPoints(ArrayList<PVector> p) {
  beginShape();
  for (int i = 0; i < p.size(); i++) {
    vertex(p.get(i).x, p.get(i).y);
  }
  endShape(CLOSE);
}

// retrieves the complete list of cameras
public void getCameraList() {

  // this sometimes hangs, so make sure "done" eventually appears
  print("Getting list of cameras ... ");
  String[] cameras = Capture.list();
  println("done");

  if (cameras == null) {
    println("Failed to retrieve the list of available cameras, will try the default...");
  } else if (cameras.length == 0) {
    println("There are no cameras available for capture.");
  } else {
    println("Available cameras:");
    printArray(cameras);
  }

  // exit after getting list
  // (you need to put a specific camera in the setup code above)
  exit();
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "colourpoint" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
