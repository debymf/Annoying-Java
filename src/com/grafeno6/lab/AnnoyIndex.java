package com.grafeno6.lab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class AnnoyIndex {

	int dimensions; //set by user
	int numTrees; //number of random trees that are going to be build, set by user
	int nnsNumber; //number of nns to search, set by user or default 5% number of items
	int itemsPerLeaf; //itens in each leaf of the tree (k)
	int totalItems = 0; //total number of items added by user
	String metric; //metric for distance, in this implementation is euclidian or manhattan
	ArrayList<Point> items;
	ArrayList<Node> roots;

	public AnnoyIndex(int dim, String metric, int nnsNumber) {
		this.nnsNumber = nnsNumber;
		this.roots = new ArrayList<Node>();
		this.items = new ArrayList<Point>();
		this.dimensions = dim;
		if (!(metric.equals("euclidian") || metric.equals("manhattan"))) {
			System.out.println("Metric not available, setting default metric (euclidian)");
			this.metric = "euclidian";
		} else {
			this.metric = metric;
		}
	}

	public AnnoyIndex(int dim, int nnsNumber) {
		this.nnsNumber = nnsNumber;
		this.roots = new ArrayList<Node>();
		this.items = new ArrayList<Point>();
		this.metric = "euclidian";
		this.dimensions = dim;
	}

	public AnnoyIndex(int dim) {
		this.roots = new ArrayList<Node>();
		this.items = new ArrayList<Point>();
		this.metric = "euclidian";
		this.dimensions = dim;
	}

	public void addItem(int i, Vector<Double> v) {
		totalItems++;
		Point p = new Point();
		p.setIndex(i);
		p.setDimensions(v);
		this.items.add(p);
	}

	public void build(int numTrees) {
		if (this.nnsNumber == 0) {
			nnsNumber = (int) (totalItems * 0.05);
		}
		this.itemsPerLeaf = nnsNumber;

		this.numTrees = numTrees;
		for (int i = 0; i < numTrees; i++) {
			roots.add(buildTree(this.items));
		}
	}

	public Vector<Double> computeAverage(Vector<Double> first, Vector<Double> second) {

		Vector<Double> average = new Vector<Double>();

		for (int i = 0; i < dimensions; i++) {
			average.add(((first.get(i)) + (second.get(i))) / 2);
		}

		return average;

	}

	public Vector<Double> computeNormalVector(Vector<Double> first, Vector<Double> second) {

		Vector<Double> norm = new Vector<Double>();

		for (int i = 0; i < dimensions; i++) {
			norm.add(((second.get(i)) - (first.get(i))));
		}
		return norm;

	}

	public Vector<Double> computeHyperplane(Vector<Double> points, Vector<Double> normal) {

		Vector<Double> hyperplane = new Vector<Double>();

		double d = 0;
		for (int i = 0; i < dimensions; i++) {
			hyperplane.add(normal.get(i));
			d = d + (points.get(i) * normal.get(i));
		}
		hyperplane.add(d);

		return hyperplane;

	}

	public Node buildTree(ArrayList<Point> points) {

		if (points.size() <= itemsPerLeaf) {

			Node leaf = new Node();
			leaf.setItemsSet(points);
			leaf.setLeft(null);
			leaf.setRight(null);
			return leaf;
		}

		Random generator = new Random();
		int firstIndex = generator.nextInt(points.size());
		int secondIndex = generator.nextInt(points.size());

		if ((firstIndex == secondIndex) && (firstIndex != 0)) {
			secondIndex--;
		}

		Vector<Double> average = computeAverage(points.get(firstIndex).getDimensions(),
				points.get(secondIndex).getDimensions());
		Vector<Double> normal = computeNormalVector(points.get(firstIndex).getDimensions(),
				points.get(secondIndex).getDimensions());
		Vector<Double> hyperplane = computeHyperplane(average, normal);

		ArrayList<Point> left = new ArrayList<Point>();
		ArrayList<Point> right = new ArrayList<Point>();
		Node n = new Node();

		n.setHyperplane(hyperplane);

		double tmpTotal;

		for (int i = 0; i < points.size(); i++) {
			tmpTotal = 0;
			for (int j = 0; j < dimensions; j++) {
				tmpTotal = tmpTotal + (points.get(i).getDimensions().get(j) * hyperplane.get(j));
			}
			tmpTotal = tmpTotal - hyperplane.get(dimensions);
			if (tmpTotal <= 0) {
				right.add(points.get(i));
			} else {
				left.add(points.get(i));
			}
		}
		n.setRight(buildTree(right));
		n.setLeft(buildTree(left));

		return n;
	}

	public Set<Entry<Integer, Double>> getNNSByItem(int index) {
		Vector<Double> search = getItemVector(index);
		return (getNNSByVector(search));
	}

	public Set<Entry<Integer, Double>> getNNSByVector(Vector<Double> v) {
		ArrayList<ArrayList<Point>> result = new ArrayList<ArrayList<Point>>();
		for (int i = 0; i < numTrees; i++) {
			result.add(findNNS(v, roots.get(i), nnsNumber));
		}

		ArrayList<Result> distance = new ArrayList<Result>();

		for (int i = 0; i < result.size(); i++) {
			for (int j = 0; j < result.get(i).size(); j++) {
				Result r = new Result();
				r.setDistance(computeDistance(v, result.get(i).get(j).getDimensions()));
				r.setIndex(result.get(i).get(j).getIndex());
				distance.add(r);
			}
		}
		distance.sort(new Comparator<Result>() {
			@Override
			public int compare(Result r2, Result r1) {

				return Double.compare(r2.distance, r1.distance);
			}
		});

		Map<Integer, Double> finalResult = new HashMap<Integer, Double>();
		int i = 0;
		while ((i < distance.size()) && (finalResult.size() != nnsNumber)) {
			if (distance.get(i).getDistance() != 0) {
				finalResult.putIfAbsent(distance.get(i).getIndex(), distance.get(i).getDistance());
			}
			i++;
		}

		return finalResult.entrySet();

	}

	public ArrayList<Point> findNNS(Vector<Double> v, Node n, int total) {
		double tmpTotal = 0;
		Node root = n;
		ArrayList<Boolean> path = new ArrayList<Boolean>();
		ArrayList<Point> resul = new ArrayList<Point>();

		while ((n.getLeft() != null) && (n.getRight() != null)) {
			tmpTotal = 0;
			for (int j = 0; j < dimensions; j++) {
				tmpTotal = tmpTotal + (v.get(j) * n.getHyperplane().get(j));
			}

			tmpTotal = tmpTotal - n.getHyperplane().get(dimensions);

			if (tmpTotal <= 0) {
				path.add(true);
				n = n.getRight();
			} else {
				path.add(false);
				n = n.getLeft();
			}
		}

		while ((n.getLeft() != null) && (n.getRight() != null)) {
			tmpTotal = tmpTotal - n.getHyperplane().get(dimensions);

			if (tmpTotal <= 0) {
				path.add(true);
				n = n.getRight();
			} else {
				path.add(false);
				n = n.getLeft();
			}
		}

		for (int i = 0; i < n.getItemsSet().size(); i++) {
			resul.add(n.getItemsSet().get(i));
		}

		n = root;
		if (resul.size() < total) {
			int k = 0;
			path.set((path.size()) - 1, !(path.get((path.size()) - 1)));
			while (((n.getLeft() != null) && (n.getRight() != null)) && (k < path.size())) {
				if (path.get(k)) {
					n = n.getRight();
				} else {
					n = n.getLeft();
				}
				k++;
			}

			while ((n.getLeft() != null) && (n.getRight() != null)) {
				Random r = new Random();
				if (r.nextBoolean()) {
					n = n.getRight();
				} else {
					n = n.getLeft();
				}

			}
			for (int i = 0; i < n.getItemsSet().size(); i++) {
				resul.add(n.getItemsSet().get(i));
			}

		}
		return resul;

	}

	public Vector<Double> getItemVector(int index) {

		for (int i = 0; i < totalItems; i++) {
			if (items.get(i).getIndex() == index) {
				return items.get(i).getDimensions();
			}
		}
		return null;
	}

	public double computeEuclidianDistance(Vector<Double> v1, Vector<Double> v2) {
		double total = 0;
		for (int i = 0; i < dimensions; i++) {
			total = total + Math.pow((v1.get(i) - v2.get(i)), 2);
		}
		return Math.sqrt(total);
	}

	public double computeManhattanDistance(Vector<Double> v1, Vector<Double> v2) {
		double total = 0;
		for (int i = 0; i < dimensions; i++) {
			total = total + v1.get(i) - v2.get(i);
		}
		return total;
	}

	public double computeDistance(Vector<Double> v1, Vector<Double> v2) {

		switch (this.metric) {
		case ("manhattan"): {
			return computeManhattanDistance(v1, v2);
		}

		default: {
			return computeEuclidianDistance(v1, v2);

		}
		}
	}

	public Double getDistance(int i, int j) {
		Vector<Double> v1 = getItemVector(i);
		Vector<Double> v2 = getItemVector(j);

		return computeDistance(v1, v2);
	}

	public int getNItems() {
		return totalItems;
	}

}
