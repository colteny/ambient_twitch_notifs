package ambient_twitch_notifs;

import java.util.Scanner;
import java.util.InputMismatchException;


/**
 *@author Team Danbo
 *@date 11/12/15
 *@class ICS 414
 *
 * An interface that takes in a number that represents a change in the
 * Twitch website database and then creates a text box that states
 * the change and gives a link to the website.
 */
 
 public class notif_test {
 
 	public static void main(String args[]) {
 		Scanner input = new Scanner(System.in);
 		int change;
 		boolean loop = true;
 		
 		while(loop) {
 			try {
				System.out.print("Enter an integer: ");
 				change = input.nextInt();
 				System.out.println(change + " is currently streaming 'game'");
 				loop = false;	
 			} catch (InputMismatchException ime) {
 				System.out.println("Input is not an integer.");
 				loop = true;
 			}
 		}
	}	
}
