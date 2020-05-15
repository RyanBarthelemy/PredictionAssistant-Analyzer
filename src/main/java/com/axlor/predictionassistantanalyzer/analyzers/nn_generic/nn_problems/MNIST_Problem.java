package com.axlor.predictionassistantanalyzer.analyzers.nn_generic.nn_problems;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

//This class is going to be used to read in the MNIST data and store that data in a way that can be easily accessed by other classes/programs/whatever
//The dataset for the MNIST problem is stored in a somewhat unusual but very efficient way so we need to do some magic to get it in a state we desire.
//The MNIST problem info and files can be found and downloaded here: http://yann.lecun.com/exdb/mnist/
public class MNIST_Problem implements Serializable{

    private final int numImages;
    private final int numLabels;

    private final int numRows;
    private final int numColumns;

    private final double[][] imagesPixelValues;
    private final double[] imageLabels;

    //use this as a baseline of where to start and what the reading in algorithm is going to be: https://github.com/encog/encog-java-core/blob/master/src/main/java/org/encog/util/data/MNISTReader.java
    //goal is to have one matrix and one vector when finished.
    //the matrix is a list of vectors, each member of a vector represents a pixel in the input image
    //inputImagesMatrix[the image number index][the pixel of that image we are on] = the pixel's color/greyscale value
    //The vector is the label of the corresponding image
    //imageLabels[image#] = some # 0-9
    //so inputImagesMatrix.length = imageLabels.length
    //inputImagesMatrix[0] is an input vector and imageLabels is the target, will convert this in a different class that sets up data for my TDS class
    public MNIST_Problem(String imagesFile, String labelsFile) {
        System.out.println("-------------------------------------------------------");
        System.out.println("Attempting to build MNIST_Problem data object:");

        try {
            DataInputStream imagesData = new DataInputStream(new FileInputStream(imagesFile));
            DataInputStream labelsData = new DataInputStream(new FileInputStream(labelsFile));

            int checkNumber = imagesData.readInt();
            if (checkNumber != 2051) { //this is the first value in the file assuming we are using the correct file. It's value should be 0x00000803 in hex, or 1000 0000 0011 in binary. This is 2051 as a 32 bit integer.
                throw new Error("ERROR: Images file has wrong magic number. See http://yann.lecun.com/exdb/mnist/ for more info on file setup requirements.");
            }
            System.out.println("Magic number check for images passed. Value: " + checkNumber);

            checkNumber = labelsData.readInt();
            if (checkNumber != 2049) {//same idea as in the images file. First 4 bytes of the labels file contains a magic check number, in this case as a 32bit integer that number is 2049
                throw new Error("ERROR: Labels file has wrong magic number. See http://yann.lecun.com/exdb/mnist/ for more info on file setup requirements.");
            }
            System.out.println("Magic number check for labels passed. Value: " + checkNumber);

            //the first 4 bytes of both files is the magic check number that we just looked at above.
            //the next 4 bytes of each file is a 32 bit integer representing the number of images/labels in the relevant file, so we do that next.
            numImages = imagesData.readInt();
            numLabels = labelsData.readInt();

            //every image needs a corresponding label describing what digit that image is a picture of... so these two values should be the same size.
            if (numImages != numLabels) {
                throw new Error("ERROR: Number of images and number of labels reported by files' metadata do not match.");
            }
            System.out.println("Number of image and label pairs in this set: " + numImages);

            //at byte 8 in labels we are now off to the races, but the images data file has a little more metadata to handle.
            //each image has a number of rows and columns representing the size of the image in pixels, these are the next 4x2 bytes in the images data file.       
            numRows = imagesData.readInt();
            numColumns = imagesData.readInt();
            System.out.println("Number of rows: " + numRows);
            System.out.println("Number of columns: " + numColumns);

            
            System.out.println("Reading in data for all " + numImages + " images. This may take a while...");
            System.out.println("Reading in roughly " + ((numRows*numColumns*numImages)/(1024*1024)) + " megabytes of data");
            //now we can read the rest of the images data file. Each pixel is only one byte worth of data since it is just a greyscale black and white image.
            imagesPixelValues = new double[numImages][];
            for (int i = 0; i < numImages; i++) {
                imagesPixelValues[i] = new double[numRows * numColumns];
                //System.out.println("Created new array for image #" + i + ". Image contains " + imagesPixelValues[i].length + " pixels.");
                for (int j = 0; j < imagesPixelValues[i].length; j++) {
                    imagesPixelValues[i][j] = imagesData.read();
                    //System.out.println((j/numRows) + "," + (numColumns%28) + " ----Pixel value: " + imagesPixelValues[i][j]);
                }
                if (i % (numImages/10) == 0) {
                    System.out.println("Read in Image# " + i);
                }
            }
            System.out.println("Image data successfully read in.");

            //next we have the label data.
            //In the MNIST problem the labels are just the 0-9 digit that the image happens to be a picture of.
            //CAUTION: while you might think of reading in the next int, each label only needs 1 byte to represent a 0-9 digit. So we read in one byte at a time and store that digit as an int
            System.out.println("Reading in data for all " + numLabels + " labels. This should go much faster...");
            imageLabels = new double[numLabels];
            for (int i = 0; i < numLabels; i++) {
                imageLabels[i] = labelsData.read();
                //System.out.println("Image Label for image " + i + ": " + imageLabels[i]);
            }
            System.out.println("Label data successfully read in.");
            System.out.println("Successfully created MNIST_Problem data object");

        } catch (FileNotFoundException ex) {
            throw new Error("ERROR: Attempted to read in data for MNIST_Problem from files:" + imagesFile + " and " + labelsFile + " ... Caught FileNotFoundException while attempting this, throwing error");
        } catch (IOException ex) {
            throw new Error("ERROR: Attempted to read in data for MNIST_Problem from files:" + imagesFile + " and " + labelsFile + " ... Caught IOException while attempting this, throwing error.");
        }
    }

    public int getNumImages() {
        return numImages;
    }

    public int getNumLabels() {
        return numLabels;
    }

    public double[][] getImagesPixelValues() {
        return imagesPixelValues;
    }

    public double[] getImageLabels() {
        return imageLabels;
    }
    
    public void save_MNIST_data(String file){
        File saveFile = new File(file);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(this);
            
            oos.flush();
            oos.close();
            System.out.println("Successfully saved MNIST_Problem object to " + file);
            
        } catch (IOException ex) {
            System.out.println("Caught IOException, did NOT save network to file:" + file);
        }
        
    }
    
    public static MNIST_Problem load_MNIST_data(String file){
        File inputFile = new File(file);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile));
            MNIST_Problem problem_data = (MNIST_Problem) ois.readObject();
            ois.close();
            
            return problem_data;
            
        } catch (IOException ex) {
            System.out.println("Caught an IOException trying to load data from file: " + file);
            System.out.println("Returning null.");
        } catch (ClassNotFoundException ex) {
            System.out.println("Caught ClassNotFoundException trying to caste object to MNIST_Problem, returning null.");
        }
          
        return null;
    }
}
