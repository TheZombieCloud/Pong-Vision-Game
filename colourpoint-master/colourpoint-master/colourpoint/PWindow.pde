class PWindow extends PApplet {
  boolean run = true;
  float y = 1;
  
  PWindow() {
    super();
    PApplet.runSketch(new String[] {this.getClass().getSimpleName()}, this);
  }

  void settings() {
    size(500, 200);
  }

  void setup() {
    background(0);
  }

  void draw() {
    rect(30, 60+y, 10, 55);
  }
  
  void moveup () {
    y += 1;
  }
  
  void movedown() {
    y -= 1;
  }
  
  void mousePressed() {
    println("mousePressed in secondary window");
  }
}
