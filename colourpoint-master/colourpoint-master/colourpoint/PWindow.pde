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

  void settings() {
    size(1500, 600);
  }

  void setup() {
    background(0);
  }

  void draw() {
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
