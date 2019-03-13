import java.util.*;

class PWindow extends PApplet {
  boolean run = true;
  float y = 1;
  float y2 = 1;
  boolean mu = false;
  boolean md = false;
  boolean mu2 = false;
  boolean md2 = false;
  float xe = 650;
  float ye = 280;
  float diam = 20;
  Random random = new Random();
  float speedx = (float)(Math.random()*2+1)*(random.nextBoolean() ? -1:1);
  float speedy = (float)(Math.random()*2+1)*(random.nextBoolean() ? -1:1); 
  
  PWindow() {
    super();
    PApplet.runSketch(new String[] {this.getClass().getSimpleName()}, this);
  }

  void settings() {
    size(1300, 600);
  }

  void setup() {
    background(0);
  }

  void draw() {
    background(0);
    if (xe<0||xe>1300){
      xe = 650;
      ye = 280;
      diam = 20;
      speedx = (float)(Math.random()*2+1)*(random.nextBoolean() ? -1:1);
      speedy = (float)(Math.random()*2+1)*(random.nextBoolean() ? -1:1);
    }
    ellipse(xe, ye, diam, diam);
    xe += speedx;
    ye += speedy;
    //ellipse(100,590, diam, diam);
    rect(30, 200+y, 10, 110);
    rect (1250, 200+y2, 10, 110);
    if (mu&&200+y<=475){
      y += 2.6;
    }
    if (md&&200+y>=70){
      y -= 2.6;
    }
    if (mu2&&200+y2<=475){
      y2 += 2.6;
    }
    if (md2&&200+y2>=70){
      y2 -= 2.6;
    }
    if (ye<10||ye>590){
      speedx *= 1.2;
      speedy *= -1.2;
    }
    if ((xe<50&&xe>40&&ye>=200+y&&ye<=200+y+110)||(xe>1240&&xe<1250&&ye>=200+y2&&ye<=200+y2+110)){
      speedx *= -1;
    }
  }
  
  void moveup () {
    mu = true;
    md = false;
  }
  
  void movedown() {
    md = true;
    mu = false;
  }
  
  void moveup2 () {
    mu2 = true;
    md2 = false;
  }
  
  void movedown2 () {
    md2 = true;
    mu2 = false;
  }
  
  void dontmove() {
    md = false;
    mu = false;
  }
  
  void dontmove2() {
    md2 = false;
    mu2 = false;
  }
  
  void mousePressed() {
    println("mousePressed in secondary window");
  }
}
