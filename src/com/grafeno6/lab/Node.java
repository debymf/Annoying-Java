package com.grafeno6.lab;

import java.util.ArrayList;
import java.util.Vector;

public class Node {
	Vector<Double> hyperplane;
	Node left;
	Node right;
	ArrayList<Point> itemsSet;
	
	public Vector<Double> getHyperplane() {
		return this.hyperplane;
	}
	public void setHyperplane(Vector<Double> hyperplane) {
		this.hyperplane=hyperplane;
	}
	public Node getLeft() {
		return left;
	}
	public void setLeft(Node left) {
		this.left = left;
	}
	public Node getRight() {
		return right;
	}
	public void setRight(Node right) {
		this.right = right;
	}
	public ArrayList<Point> getItemsSet() {
		return itemsSet;
	}
	public void setItemsSet(ArrayList<Point> itemsSet) {
		this.itemsSet = itemsSet;
	}
	
	
	
}
