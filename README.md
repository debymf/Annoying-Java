# Annoying-Java - What it is about?

This library computes the Approximate Nearest Neighbor, it was implemented in Java and was heavily inspired by [Annoy Library](https://github.com/spotify/annoy). This library was implemented for my personal learning.
___

# How does it works?

This library works in the following way:

1. We have a set of points;
2. We split this set of points several times, selecting two random points and finding the hyperplane that is between those two points.
3. Each time we split our set of points, we create a new branch of our tree.
4. Continue splitting until a we have groups of size k.
5. Repeat this process to build different trees.
6. Select a point to perform the NNS, search this point in all the trees and get the optimal result.
7. Compare the results from all the different trees and select the results that are the nearest neighbors.
___

# How can I use this?

Include the .jar file to your build path
First instantiate the class AnnoyIndex with the dimensions, metric to measure distance (optional, default is euclidian, but also accepts manhattan) and number of neighbors to search (optional, default is 5% of the total number of items).

Next step is to add all the elements for computation using the method addItem with the index and the value por the points.

Then you need to build the trees, using the method build with the number of trees to build.

Then you need to use the method getNNSByItem (if you want to use index) or getNNSByVector (if you want to use the vector). This method receives as argument the point to be compared and returns the nearest neighbors and the value for the distances to that point.
___

# Example


```
import java.util.Random;
import java.util.Vector;

import com.grafeno6.lab.AnnoyIndex;

public class TestNNS {

	public static void main(String[] args) {
		Random r = new Random();
		AnnoyIndex ai = new AnnoyIndex(40);
		for (int i=0; i<1000;i++) {
			Vector<Double> v = new Vector<Double>();
			for (int j=0;j<40;j++) {
				v.add(r.nextGaussian());
			}

			ai.addItem(i,v);
		}	
		
		ai.build(10);

		System.out.println(ai.getNNSByItem(100));
	}

}
```

# How can I contact you?

If you have any suggestions or questions, feel free to send me a message.

