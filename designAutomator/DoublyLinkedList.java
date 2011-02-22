package designAutomator;

public class DoublyLinkedList<T> {
	
	DoublyLinkedListNode<T> head;
	DoublyLinkedListNode<T> tail;
	int size;
	public DoublyLinkedList() {
		super();
		size = 0;
	}
	public int size(){
		return size;
	}
	// Super costly operation
	public boolean find(T node){
		for(DoublyLinkedListNode<T> i = head; i != null; i=i.next){
			if(node == i.data){
				return true;
			}
		}
		return false;
	}
	
	public void addToEnd(T data){
		DoublyLinkedListNode<T> newNode = new DoublyLinkedListNode<T>(data);
		if(tail!=null){
			tail.insertAfter(newNode);
			tail = newNode;			
		}
		else {
			head = null;
			tail = null; 
		}
		size += 1;
	}
	public void addToBegin(T data){
		DoublyLinkedListNode<T> newNode = new DoublyLinkedListNode<T>(data);
		if(head!=null){
			head.insertBefore(newNode);
			head = newNode;
		}
		else{
			head = null;
			tail = null;
		}		
		size += 1;
	}
	public void remove(DoublyLinkedListNode<T> node){
		if(node.prev != null){
			node.prev.next = node.next;
		}
		if(node.next != null){
			node.next.prev = node.prev;
		}
		
		if(node == head){
			head = node.next;
		}
		if(node == tail){
			head = node.prev;
		}
		size -=1;
	}
	
}
