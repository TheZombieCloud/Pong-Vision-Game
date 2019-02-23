class MyTechnique extends PointingTechnique {

  MyTechnique() {
    name = "PONGGGGGG BOIS";
  }

  void handle(Blob[][] blobs) {

    displayName();

    Blob firstco = blobs[0][0];
    Blob secondco = blobs[1][0];
    if (firstco!=null){
      moveup(); 
    }
    else if (secondco!=null){
      movedown();
    }
  }
  
}
