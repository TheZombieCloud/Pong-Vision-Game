class MyTechnique extends PointingTechnique {

  MyTechnique() {
    name = "PONG";
  }

  void handle(Blob[][] blobs) {

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
    else if (seconda!=0){
      movedown();
    }
    if (thirda>fourtha){
      moveup2();
    }
    else if (fourtha!=0){
      movedown2();
    }
  }
  
}
