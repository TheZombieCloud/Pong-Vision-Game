class PWindow extends PApplet {
  boolean run = true;
  float y = 1;
  boolean mu = false;
  boolean md = false;
  
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
    rect(30, 60+y, 10, 55);
    if (mu){
      y += 1;
    }
    if (md){
      y -= 1;
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
  
  void mousePressed() {
    println("mousePressed in secondary window");
  }
}
