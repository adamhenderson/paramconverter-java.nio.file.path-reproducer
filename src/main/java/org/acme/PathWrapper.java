package org.acme;

// This class should decorate the java.nio.file.Path class
public class PathWrapper {
    
    private java.nio.file.Path path;

    public PathWrapper(java.nio.file.Path path) {
        this.path = path;
    }

 /**
  * @return the path
  */
 public java.nio.file.Path getPath() {
     return path;
 }

    /**
    * @param path the path to set
    */
    public void setPath(java.nio.file.Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
    
    public String getFirstPart() {
        return path.getName(0).toString();
    }

}
