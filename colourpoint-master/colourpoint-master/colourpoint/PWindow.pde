class PWindow extends PApplet {
  boolean run = true;
  int y = 20;
  
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
    if (run){
      rect(30, 60, 10, 55);
      //run = false;
    }
  }
  
  void moveup () {
    rect (30+y, 60, 10, 55);
  }
  
  void movedown() {
    rect (30-y, 60, 10, 55);
  }
  
  void mousePressed() {
    println("mousePressed in secondary window");
  }
}
