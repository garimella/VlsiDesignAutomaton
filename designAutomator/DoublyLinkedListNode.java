package designAutomator;

public class DoublyLinkedListNode<T> {
	DoublyLinkedListNode<T> next;
	DoublyLinkedListNode<T> prev;
	
	T data;
	
	public DoublyLinkedListNode(T data) {
		this.next = null;
		this.prev = null;
		this.data = data;
	}

	public void insertAfter(DoublyLinkedListNode<T> newNode){
		newNode.prev = this;
		newNode.next = this.next;
		this.next = newNode;
	}
	public void insertBefore(DoublyLinkedListNode<T> newNode){
		newNode.next = this;
		newNode.prev = this.prev;
		this.prev = newNode;
	}
	public DoublyLinkedListNode<T> prev(){
		return this.prev;
	}
	public DoublyLinkedListNode<T> next(){
		return this.next;
	}
}